/*
 Copyright 2014 Jakub Naplava
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.mff.d3s.tools.perfdoc.server.measuring;

import cz.cuni.mff.d3s.tools.perfdoc.server.LockBase;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ResultCache;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ResultDatabaseCache;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.exception.MeasurementException;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.runners.DirectRunner;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.runners.MethodReflectionRunner;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.MeasurementStatistics;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkloadImpl;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.WorkloadImpl;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * This class performs measurement.
 *
 * It contains counting points, in which the measurement will be performed,
 * searching cache for results and if no results are found, then also preparing
 * arguments for BenchmarkRunners, which are then called. After measurement is
 * done, measured results can be via this class saved into cache.
 *
 * @author Jakub Naplava
 */
public class MethodMeasurer {

    private static final Logger log = Logger.getLogger(MethodMeasurer.class.getName());

    private final MeasureRequest measureRequest;
    private final ResultCache resultCache;
    private final LockBase lockBase;

    //measured results
    private List<BenchmarkResult> results = new ArrayList<>();

    //the i-th item says, whether i-th result was found in cache (true), thus no need to save him back into cache
    private final List<Boolean> resultsMask = new ArrayList<>();

    /**
     * Creates new instance of MethodMeasurer for given MeasureRequest.
     *
     * @param measureRequest
     * @param lockBase
     * @throws SQLException
     */
    public MethodMeasurer(MeasureRequest measureRequest, LockBase lockBase) throws SQLException {
        this.lockBase = lockBase;
        this.measureRequest = measureRequest;
        resultCache = new ResultDatabaseCache(ResultDatabaseCache.JDBC_URL);
    }

    /**
     * Performs measurement on given MeasureRequest.
     *
     * @return JSONObject that contains measured results.
     * @throws MeasurementException
     */
    public JSONObject measure() throws MeasurementException {
        //requested measurement quality
        MeasurementQuality mQuality = measureRequest.getMeasurementQuality();

        int priority = mQuality.getPriority();

        WorkloadImpl workloadImpl = new WorkloadImpl();
        ServiceWorkloadImpl serviceImpl = new ServiceWorkloadImpl();

        //passing the priority to generator
        serviceImpl.setPriority(priority);

        //values chosen from rangeValue to measure data in
        double step = MeasuringUtils.findStepValue(measureRequest.getGenerator(), measureRequest.getRangeVal());
        Object rangeArgument = measureRequest.getValues()[measureRequest.getRangeVal()];

        //list, containing for each priority (respectively priority..4) values in which the measurement (for corresponding) priority, will be performed
        List<double[]> valuesToMeasureList = new ArrayList<>();
        for (int i = priority; i <= 4; i++) {
            //note that this might end with IllegalArgument | NumberFormat Exception, that is being handled by the caller
            double[] valuesToMeasure = MeasuringUtils.getValuesToMeasure(rangeArgument, step, new MeasurementQuality(i).getNumberOfPoints());
            valuesToMeasureList.add(valuesToMeasure);
        }

        int cachedPriority = checkWhetherCannotGiveBetterResults(valuesToMeasureList);

        if (cachedPriority == 0) {
            cachedPriority = measureRequest.getMeasurementQuality().getPriority();

            //values in which the measurement will be performed
            double[] valuesToMeasure = valuesToMeasureList.get(0);

            //for every point, that should be measured, we perform a measurement
            for (int i = 0; i < valuesToMeasure.length; i++) {

                //the arguments for the generator 
                Object[] args = MeasuringUtils.prepareArgsToCall(measureRequest, workloadImpl, serviceImpl, valuesToMeasure[i]);
                BenchmarkSetting benSetting = new BenchmarkSettingImpl(measureRequest, new MethodArgumentsImpl(args));

                //if cache contains results for given settings, we do not have to perform measurement
                BenchmarkResult res = resultCache.getResult(benSetting);
                if (res != null) {
                    results.add(res);
                    resultsMask.add(true);
                    log.log(Level.CONFIG, "The value for measuring was found in cache.");
                    continue;
                }

                BenchmarkRunner runner = null;
                switch (priority) {
                    case 1:
                    case 2:
                    case 3:
                        runner = new MethodReflectionRunner();
                        break;
                    case 4:
                        if (MeasurementConfiguration.getCodeGenerationFlag()) {
                            runner = new DirectRunner();
                        } else {
                            runner = new MethodReflectionRunner();
                        }
                        break;
                }

                //wait until we can measure (there is no lock for our hash)
                lockBase.waitUntilFree(measureRequest.getUserID());

                try {
                    MeasurementStatistics statistics = runner.measure(benSetting);
                    statistics.removeOutliers();                    
                    results.add(new BenchmarkResultImpl(statistics, benSetting));
                } catch (IllegalAccessException ex) {
                    String msg = "An IllegalAccessException occured while measuring code.";
                    log.log(Level.SEVERE, msg, ex);
                    throw new MeasurementException(msg);
                } catch (IllegalArgumentException ex) {
                    /*
                     The only situation, when other measurements may work (not aborting whole measurement).
                     However we must save that an exception occured.  
                     */
                    log.log(Level.SEVERE, "Wrong arguments were passed to measured method/generator.", ex);
                    //null denoting corrupted measurement
                    results.add(new BenchmarkResultImpl(null, benSetting));
                } catch (InstantiationException ex) {
                    String msg = "Could not istantiate generator class while trying to perform measurement.";
                    log.log(Level.SEVERE, msg, ex);
                    throw new MeasurementException(msg);
                } catch (InvocationTargetException ex) {
                    String msg = "An InvocationTargetException occured when trying to invoke generator/measured method";
                    log.log(Level.SEVERE, msg, ex);
                    throw new MeasurementException(msg);
                } catch (Throwable ex) {
                    String msg = "An unknown exception occured when trying to measure results.";
                    log.log(Level.SEVERE, msg, ex);
                    throw new MeasurementException(msg);
                }

                lockBase.freeLock(measureRequest.getUserID());

                //the result was not found in cache
                resultsMask.add(false);
            }
        }

        log.log(Level.CONFIG, "Measurement succesfully done");

        //values, in which the measurement was performed
        double[] valuesToMeasure = valuesToMeasureList.get(cachedPriority - priority);

        return processBenchmarkResults(valuesToMeasure, cachedPriority);
    }

    /**
     * Checks cache for better results with better or equal priority.
     *
     * @param valuesToMeasure list of values, where an array on i-th index is
     * array of valuesToMeasure for priority: (priority + i).
     * @return priority priority of found results, 0 if no better or equal
     * found.
     */
    private int checkWhetherCannotGiveBetterResults(List<double[]> valuesToMeasure) {
        int priority = measureRequest.getMeasurementQuality().getPriority();
        List<BenchmarkResult> list = new ArrayList<>();

        //whether there is priority having all BenchmarkResults not equal to null 
        boolean errorIndicator = false;

        //starting with priority 4 down to priority
        for (int i = 4; i >= priority; i--) {
            MeasurementQuality mq = new MeasurementQuality(i);

            //for all valuesToMeasure
            for (double val : valuesToMeasure.get(i - priority)) {
                Object[] args = MeasuringUtils.prepareArgsToCall(measureRequest, null, null, val);

                BenchmarkSetting benSetting = new BenchmarkSettingImpl(measureRequest.getMeasuredMethod(),
                        measureRequest.getGenerator(), new MethodArgumentsImpl(args), mq);

                BenchmarkResult benRes = resultCache.getResult(benSetting);

                //if there is valueToMeasure with benchmarkResult null, current priority can not be satisfied
                if (benRes == null) {
                    errorIndicator = true;
                    break;
                } else {
                    list.add(benRes);
                }
            }

            //if there were found results for all valuesToMeasure, we satisfied priority = i
            if (!errorIndicator) {
                results = list;

                //all benchmarkResults were found in cache
                for (BenchmarkResult result : results) {
                    resultsMask.add(true);
                }

                return i;
            } else {
                errorIndicator = false;
                list.clear();
            }
        }

        return 0;
    }

    /**
     * Returns results saved in JSONObject that can be sent to end-user. This
     * result will contain measured results and its units.
     *
     * @param list measured BenchmarkResults
     * @param valuesInWhichWasMeasured
     * @return
     */
    private JSONObject processBenchmarkResults(double[] valuesInWhichWasMeasured, int priority) throws MeasurementException {
        JSONObject jsonResults = new JSONObject();

        List<Long> computedMeans = new ArrayList<>();
        List<Long> computedStandardDeviations = new ArrayList<>();
        List<Long> computedMedians = new ArrayList<>();
        List<Long> computetFirstQ = new ArrayList<>();
        List<Long> computetThirdQ = new ArrayList<>();

        boolean corruptedMeasurement = true;

        for (BenchmarkResult br : results) {
            //if current measurement is not corrupted
            if (br.getStatistics() != null) {
                long mean = br.getStatistics().getMean();

                //there's at least one good measurement
                if (mean != -1) {
                    corruptedMeasurement = false;
                }

                computedMeans.add(mean);
                computedStandardDeviations.add(br.getStatistics().getStandardDeviation());
                computedMedians.add(br.getStatistics().getMedian());
                computetFirstQ.add(br.getStatistics().getFirstQuartile());
                computetThirdQ.add(br.getStatistics().getThirdQuartile());
            } else {
                computedMeans.add(-1L);
                computedMedians.add(-1L);
            }
        }

        //if all results are corrupted, we throw an exception
        if (corruptedMeasurement) {
            throw new MeasurementException("Wrong arguments were passed to measured method/generator for all points, thus no results could be measured.");
        }

        String units = MeasuringUtils.convertUnits(computedMeans, computedMedians);
        int divideBy = 1;
        switch(units) {
            case "s": divideBy = 1000 * 1000 * 1000;
                break;
            case "ms": divideBy = 1000 * 1000;
                break;
            case "µs": divideBy = 1000;
                break;
        }
        
        for (int i = 0; i<computetFirstQ.size(); i++) {
            computetFirstQ.set(i, computetFirstQ.get(i) / divideBy);
            computetThirdQ.set(i, computetThirdQ.get(i) / divideBy);
            computedStandardDeviations.set(i, computedStandardDeviations.get(i) / divideBy);
        }
        
        for (int i = 0; i < computedMeans.size(); i++) {
            if (computedMeans.get(i) != -1) {
                long mean = computedMeans.get(i).longValue();
                long standardDeviation = computedStandardDeviations.get(i).longValue();
                long median = computedMedians.get(i).longValue();
                long firstQ = computetFirstQ.get(i).longValue();
                long thirdQ = computetThirdQ.get(i).longValue();
                jsonResults.accumulate("data", new Object[]{valuesInWhichWasMeasured[i], 
                    new Object[] {mean - standardDeviation,mean, mean + standardDeviation},
                    new Object[] {firstQ,median, thirdQ}                        
                });
            }
        }
        jsonResults.accumulate("units", units);
        jsonResults.accumulate("priority", priority);

        if (corruptedMeasurement) {
            jsonResults.accumulate("error", "Wrong arguments were passed to measured method/generator during some measurement, thus just some points were measured.");
        }

        System.out.println(jsonResults);
        return jsonResults;
    }

    /**
     * Saves results into cache and closes the connection with database.
     */
    public void saveResultsAndCloseDatabaseConnection() {
        for (int i = 0; i < results.size(); i++) {
            BenchmarkResult benRes = results.get(i);
            //if the result was not obtained from cache
            if (resultsMask.get(i) == false) {
                resultCache.insertResult(benRes);
            }
        }

        if (resultCache != null) {
            //we do not need the connection to database anymore
            resultCache.closeConnection();
        }
    }
}
