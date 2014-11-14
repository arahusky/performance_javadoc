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
        resultCache = new ResultDatabaseCache();
    }

    /**
     * Performs measurement on given MeasureRequest.
     *
     * @return JSONObject that contains measured results.
     */
    public JSONObject measure() {
        int priority = measureRequest.getPriority();

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
        double[] valuesToMeasure = MeasuringUtils.getValuesToMeasure(rangeArgument, step, MeasurementConfiguration.returnHowManyValuesToMeasure(priority));

        //how many times to measure the method in one cycle
        int howManyTimesToMeasure = MeasurementConfiguration.returnHowManyTimesToMeasure(priority);

        //passing the amount of wanted results to generator
        serviceImpl.setNumberCalls(howManyTimesToMeasure);

        for (int i = 0; i < valuesToMeasure.length; i++) {

            //the arguments for the generator 
            Object[] args = MeasuringUtils.prepareArgsToCall(measureRequest, workloadImpl, serviceImpl, valuesToMeasure[i]);
            BenchmarkSetting benSetting = new BenchmarkSettingImpl(measureRequest, new MethodArgumentsImpl(args));

            //checking for results in cache
            BenchmarkResult res = resultCache.getResult(benSetting, howManyTimesToMeasure);
            if (res != null && !res.getStatistics().isEmpty()) {
                result.add(res);
                log.log(Level.CONFIG, "The value for measuring was found in cache.");
                continue;
            }
            BenchmarkRunner runner = null;
            switch (priority) {
                case 1:
                case 2:
                case 3:
                case 4:
                    runner = new MethodReflectionRunner();
                    break;
            }

            //wait until we can measure (there is no lock for our hash)
            lockBase.waitUntilFree(measureRequest.getUserID());
            result.add(new BenchmarkResultImpl(runner.measure(benSetting), benSetting));
            lockBase.freeLock(measureRequest.getUserID());
        }

        log.log(Level.CONFIG, "Measurement succesfully done");

        JSONObject jsonResults = processBenchmarkResults(result, valuesToMeasure, howManyTimesToMeasure);

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
     * @param howManyTimesWasMeasured
     * @return
     */
    private JSONObject processBenchmarkResults(List<BenchmarkResult> list, double[] valuesInWhichWasMeasured, int howManyTimesWasMeasured) {
        JSONObject jsonResults = new JSONObject();

        List<Long> computedResults = new ArrayList<>();
        for (BenchmarkResult br : list) {
            computedResults.add(br.getStatistics().compute());
        }
        String units = MeasuringUtils.convertUnits(computedResults);
        
        for (int i = 0; i < list.size(); i++) {
            BenchmarkResult benRes = list.get(i);
            long res = benRes.getStatistics().compute();
            resultCache.insertResult(benRes.getBenchmarkSetting(), howManyTimesWasMeasured, res);
            jsonResults.accumulate("data", new Object[]{valuesInWhichWasMeasured[i], computedResults.get(i)});
        }
        //jsonResults.accumulate("units", units);
        jsonResults.accumulate("units", units);

        return jsonResults;
    }
}
