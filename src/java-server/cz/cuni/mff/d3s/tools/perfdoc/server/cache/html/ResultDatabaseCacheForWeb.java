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
package cz.cuni.mff.d3s.tools.perfdoc.server.cache.html;

import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ResultDatabaseCache;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResult;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResultImpl;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSetting;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSettingImpl;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MeasurementQuality;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MethodArgumentsImpl;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.BasicStatistics;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.MeasurementStatistics;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ResultCacheForWeb.
 *
 * @author Jakub Naplava
 */
public class ResultDatabaseCacheForWeb extends ResultDatabaseCache implements ResultCacheForWeb {

    private static final Logger log = Logger.getLogger(ResultDatabaseCacheForWeb.class.getName());

    public ResultDatabaseCacheForWeb(String connection_url) throws SQLException {
        super(connection_url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BenchmarkResult> getMainTableResults() {
        List<BenchmarkResult> list = new ArrayList<>();

        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM measurement_information";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String methodName = rs.getString("measured_method");
                String generator = rs.getString("generator");
                String data = rs.getString("generator_arguments");
                int idQuality = rs.getInt("idQuality");

                BenchmarkSetting bs = new BenchmarkSettingImpl(new MethodInfo(methodName), new MethodInfo(generator), new MethodArgumentsImpl(data), getMeasureQualityFromID(idQuality));
                
                BasicStatistics basicStat = getBasicStatisticsFromResultSet(rs);
                BenchmarkResult item = new BenchmarkResultImpl(basicStat, bs);
                list.add(item);
            }
            return list;
        } catch (SQLException e) {
            log.log(Level.INFO, "Unable to retrieve results from database", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<MethodInfo> getDistinctMeasuredMethods() {
        Collection<MethodInfo> list = new ArrayList<>();

        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT DISTINCT measured_method FROM measurement_information";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String methodName = rs.getString("measured_method");

                list.add(new MethodInfo(methodName));
            }
            return list;
        } catch (SQLException e) {
            log.log(Level.INFO, "Unable to retrieve results from database", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<MethodInfo> getDistinctClassMethods(String className) {
        Collection<MethodInfo> list = new ArrayList<>();

        try {
            String query = "SELECT DISTINCT measured_method "
                    + "FROM measurement_information "
                    + "WHERE measured_method LIKE ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, className + "#%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String methodName = rs.getString("measured_method");

                list.add(new MethodInfo(methodName));
            }
            return list;
        } catch (SQLException e) {
            log.log(Level.INFO, "Unable to retrieve results from database", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<MethodInfo> getDistinctGenerators(MethodInfo method) {
        Collection<MethodInfo> list = new ArrayList<>();

        try {
            String query = "SELECT DISTINCT generator "
                    + "FROM measurement_information "
                    + "WHERE measured_method = ?";

            //it is very important to use PreparedStatement here to avoid any kind of SQL injection
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, method.toString());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String generator = rs.getString("generator");

                list.add(new MethodInfo(generator));
            }
            return list;
        } catch (SQLException e) {
            log.log(Level.INFO, "Unable to retrieve results from database", e);
            return null;
        }
    }

    @Override
    public List<BenchmarkResultDB> getResults(MethodInfo testedMethod, MethodInfo generator) {
        List<BenchmarkResultDB> list = new ArrayList<>();

        try {
            String query = "SELECT * "
                    + "FROM measurement_information "
                    + "WHERE (measured_method = ? AND generator = ?)";
            //it is very important to use PreparedStatement here to avoid any kind of SQL injection
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, testedMethod.toString());
            stmt.setString(2, generator.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String data = rs.getString("generator_arguments");
                int idQuality = rs.getInt("idQuality");
                BasicStatistics bs = getBasicStatisticsFromResultSet(rs);
                
                BenchmarkResultDB item = new BenchmarkResultDBImpl(bs,
                        new BenchmarkSettingImpl(testedMethod, generator, new MethodArgumentsImpl(data), getMeasureQualityFromID(idQuality)), id);

                list.add(item);
            }
            return list;
        } catch (SQLException e) {
            log.log(Level.INFO, "Unable to retrieve results from database", e);
            return null;
        }
    }

    @Override
    public Collection<Object[]> getDetailedTableResults() {
        List<Object[]> list = new ArrayList<>();

        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM measurement_detailed";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("id");
                long time = rs.getLong("time");
                list.add(new Object[]{id, time});
            }
            return list;
        } catch (SQLException e) {
            log.log(Level.INFO, "Unable to retrieve results from database", e);
            return null;
        }
    }

    private MeasurementQuality getMeasureQualityFromID(int idQuality) throws SQLException {
        String query = "SELECT * FROM measurement_quality "
                + "WHERE idQuality=" + idQuality;

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        if (!rs.next()) {
            return null;
        }

        int priority = rs.getInt("priority");
        int warmupTime = rs.getInt("warmup_time");
        int warmupCycles = rs.getInt("warmup_measurements");
        int measurementTime = rs.getInt("measurement_time");
        int measurementCycles = rs.getInt("measurement_count");

        return new MeasurementQuality(priority, warmupTime, warmupCycles, measurementTime, measurementCycles, priority);
    }

    @Override
    public Collection<Object[]> getQualityResults() {
        List<Object[]> list = new ArrayList<>();

        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM measurement_quality";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int idQuality = rs.getInt("idQuality");
                int measurementTime = rs.getInt("measurement_time");
                int measurementCycles = rs.getInt("measurement_count");
                int warmupTime = rs.getInt("warmup_time");
                int warmupCycles = rs.getInt("warmup_measurements");
                int priority = rs.getInt("priority");

                MeasurementQuality mq = new MeasurementQuality(priority, warmupTime, warmupCycles, measurementTime, measurementCycles, 0);

                list.add(new Object[]{idQuality, mq});
            }
            return list;
        } catch (SQLException e) {
            log.log(Level.INFO, "Unable to retrieve results from database", e);
            return null;
        }
    }

    private BasicStatistics getBasicStatisticsFromResultSet(ResultSet rs) throws SQLException {
        long mean = rs.getLong("mean");
        long median = rs.getLong("median");
        long deviation = rs.getLong("deviation");
        long firstQuartile = rs.getLong("firstQuartile");
        long thirdQuartile = rs.getLong("mean");
        
        return new BasicStatistics(mean, median, deviation, firstQuartile, thirdQuartile);
    }

    @Override
    public Statistics getResults(int id) {
        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM "
                        + " (SELECT * FROM measurement_information"
                        + "     WHERE id = " + id + ") AS info"
                        + " INNER JOIN measurement_detailed AS detailed"
                        + " ON (info.id = detailed.id)"
                        ;
            ResultSet rs = stmt.executeQuery(query);
            return getStatisticsFromResultsSet(rs);
        } catch (SQLException e) {
            log.log(Level.INFO, "Unable to retrieve results from database", e);
            return null;
        }
    }
    
    private Statistics getStatisticsFromResultsSet(ResultSet rs) throws SQLException {
        MeasurementStatistics statistics = new MeasurementStatistics();
        
        while (rs.next()) {
            long time = rs.getLong("time");
            statistics.addResult(time);
        }

        return statistics;
    }
}
