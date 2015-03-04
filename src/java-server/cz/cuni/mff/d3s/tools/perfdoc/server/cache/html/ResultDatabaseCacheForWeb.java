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
                String methodName = rs.getString("method");
                String generator = rs.getString("workload");
                String data = rs.getString("workload_arguments");
                int idQuality = rs.getInt("idQuality");
                long time = rs.getLong("average");
                
                BenchmarkSetting bs = new BenchmarkSettingImpl(new MethodInfo(methodName), new MethodInfo(generator), new MethodArgumentsImpl(data), getMeasureQualityFromID(idQuality));
                BenchmarkResult item = new BenchmarkResultImpl(new Statistics("{" + time + "}"), bs);
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
    public Collection<MethodInfo> getDistinctTestedMethods() {
        Collection<MethodInfo> list = new ArrayList<>();

        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT DISTINCT method FROM measurement_information";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String methodName = rs.getString("method");

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
            String query = "SELECT DISTINCT method "
                    + "FROM measurement_information "
                    + "WHERE method LIKE ?";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, className + "#%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String methodName = rs.getString("method");

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
            String query = "SELECT DISTINCT workload "
                    + "FROM measurement_information "
                    + "WHERE method = ?";

            //it is very important to use PreparedStatement here to avoid any kind of SQL injection
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, method.toString());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String generator = rs.getString("workload");

                list.add(new MethodInfo(generator));
            }
            return list;
        } catch (SQLException e) {
            log.log(Level.INFO, "Unable to retrieve results from database", e);
            return null;
        }
    }

@Override
    public List<BenchmarkResult> getResults(MethodInfo testedMethod, MethodInfo generator) {
        List<BenchmarkResult> list = new ArrayList<>();

        try {
            String query = "SELECT * "
                    + "FROM measurement_information "
                    + "WHERE (method = ? AND workload = ?)";
             //it is very important to use PreparedStatement here to avoid any kind of SQL injection
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, testedMethod.toString());
            stmt.setString(2, generator.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String data = rs.getString("workload_arguments");
                int idQuality = rs.getInt("idQuality");
                long time = rs.getLong("average");

                BenchmarkResult item = new BenchmarkResultImpl(new Statistics("{" + time + "}"),
                        new BenchmarkSettingImpl(testedMethod, generator, new MethodArgumentsImpl(data), getMeasureQualityFromID(idQuality)));
                
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
                list.add(new Object[] {id, time});
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
        int warmupCycles = rs.getInt("warmup_cycles");
        int measurementTime = rs.getInt("measurement_time");
        int measurementCycles = rs.getInt("measurement_cycles");
        
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
                int measurementCycles = rs.getInt("measurement_cycles");
                int warmupTime = rs.getInt("warmup_time");
                int warmupCycles = rs.getInt("warmup_cycles");
                int priority = rs.getInt("priority");
                
                MeasurementQuality mq = new MeasurementQuality(priority, warmupTime, warmupCycles, measurementTime, measurementCycles, 0);
                    
                list.add(new Object[] {idQuality, mq});
            }
            return list;
        } catch (SQLException e) {
            log.log(Level.INFO, "Unable to retrieve results from database", e);
            return null;
        }
    }
}
