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

import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.runners.DirectRunner;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.runners.MethodReflectionRunner;
import cz.cuni.mff.d3s.tools.perfdoc.server.LockBase;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ResultCache;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ResultDatabaseCache;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkloadImpl;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.WorkloadImpl;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author Jakub Naplava
 */
public class MethodMeasurer {

    private static final Logger log = Logger.getLogger(MethodMeasurer.class.getName());

    private final MeasureRequest measureRequest;
    private final ResultCache resultCache;
    private final LockBase lockBase;

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
     */
    public JSONObject measure() {
        //requested measurement quality
        MeasurementQuality mQuality = measureRequest.getMeasurementQuality();
        
        int priority = mQuality.getPriority();

        WorkloadImpl workloadImpl = new WorkloadImpl();
        ServiceWorkloadImpl serviceImpl = new ServiceWorkloadImpl();

        //passing the priority to generator
        serviceImpl.setPriority(priority);

        //list to store measured Statistics
        List<BenchmarkResult> result = new ArrayList<>();

        //values chosen from rangeValue to measure data in
        double step = MeasuringUtils.findStepValue(measureRequest.getWorkload(), measureRequest.getRangeVal());
        Object rangeArgument = measureRequest.getValues()[measureRequest.getRangeVal()];

        //note that this might end with IllegalArgument | NumberFormat Exception, that is being handled by the caller
        double[] valuesToMeasure = MeasuringUtils.getValuesToMeasure(rangeArgument, step, mQuality.getNumberOfPoints());

        //for every point, that should be measured, we perform a measurement
        for (int i = 0; i < valuesToMeasure.length; i++) {

            //the arguments for the generator 
            Object[] args = MeasuringUtils.prepareArgsToCall(measureRequest, workloadImpl, serviceImpl, valuesToMeasure[i]);
            BenchmarkSetting benSetting = new BenchmarkSettingImpl(measureRequest, new MethodArgumentsImpl(args));

            //if cache contains results for given settings, we do not have to perform measurement
            BenchmarkResult res = resultCache.getResult(benSetting);
            if (res != null) {
                result.add(res);
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
                    runner = new DirectRunner();
                    break;
            }

            //wait until we can measure (there is no lock for our hash)
            lockBase.waitUntilFree(measureRequest.getUserID());
            result.add(new BenchmarkResultImpl(runner.measure(benSetting), benSetting));
            lockBase.freeLock(measureRequest.getUserID());            
        }

        log.log(Level.CONFIG, "Measurement succesfully done");

        JSONObject jsonResults = processBenchmarkResults(result, valuesToMeasure);

        if (resultCache != null) {
            //we do not need the connection to database anymore
            resultCache.closeConnection();
        }
        return jsonResults;
    }

    /**
     * Returns results saved in JSONObject that can be sent to end-user. This
     * result will contain measured results and its units. Every result will be
     * saved into database as well.
     *
     * @param list measured BenchmarkResults
     * @param valuesInWhichWasMeasured
     * @return
     */
    private JSONObject processBenchmarkResults(List<BenchmarkResult> list, double[] valuesInWhichWasMeasured) {
        JSONObject jsonResults = new JSONObject();

        List<Long> computedMeans = new ArrayList<>();
        List<Long> computedMedians = new ArrayList<>();
        for (BenchmarkResult br : list) {
            computedMeans.add(br.getStatistics().computeMean());
            computedMedians.add(br.getStatistics().computeMedian());
        }
        String units = MeasuringUtils.convertUnits(computedMeans, computedMedians);
        
        for (int i = 0; i < list.size(); i++) {
            BenchmarkResult benRes = list.get(i);
            resultCache.insertResult(benRes);
            jsonResults.accumulate("data", new Object[]{valuesInWhichWasMeasured[i], computedMeans.get(i), computedMedians.get(i)});
        }
        jsonResults.accumulate("units", units);

        return jsonResults;
    }
}
