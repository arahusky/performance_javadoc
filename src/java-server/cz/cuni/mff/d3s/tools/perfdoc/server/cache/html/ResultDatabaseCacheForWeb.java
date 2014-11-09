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
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MethodArgumentsImpl;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

    public ResultDatabaseCacheForWeb() throws SQLException {
        super();
    }

    public ResultDatabaseCacheForWeb(Boolean test) throws SQLException {
        super(test);
    }

    /**
     * {@inheritDoc}
     */
    @Override
     public List<BenchmarkResult> getResults() {
        List<BenchmarkResult> list = new ArrayList<>();

        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM results";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String methodName = rs.getString("methodName");
                String generator = rs.getString("generator");
                String data = rs.getString("data");
                int numberOfMeasurements = rs.getInt("numberOfMeasurements");
                long time = rs.getLong("time");
                
                BenchmarkSetting bs = new BenchmarkSettingImpl(new MethodInfo(methodName), new MethodInfo(generator), new MethodArgumentsImpl(data), numberOfMeasurements);
                //TODO proper statistics must be added after creating next table containing statistics
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
    public List<MethodInfo> getDistinctTestedMethods() {
        List<MethodInfo> list = new ArrayList<>();

        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT DISTINCT methodName FROM results";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String methodName = rs.getString("methodName");

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
    public List<MethodInfo> getDistinctClassMethods(String className) {
        List<MethodInfo> list = new ArrayList<>();

        try {
            String query = "SELECT DISTINCT methodName "
                    + "FROM results "
                    + "WHERE methodName LIKE ?";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, className + "#%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String methodName = rs.getString("methodName");

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
    public ArrayList<MethodInfo> getDistinctGenerators(MethodInfo method) {
        ArrayList<MethodInfo> list = new ArrayList<>();

        try {
            String query = "SELECT DISTINCT generator "
                    + "FROM results "
                    + "WHERE methodName = ?";

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
    public List<BenchmarkResult> getResults(MethodInfo testedMethod, MethodInfo generator) {
        ArrayList<BenchmarkResult> list = new ArrayList<>();

        try {
            String query = "SELECT * "
                    + "FROM results "
                    + "WHERE (methodName = ? AND generator = ?)";
             //it is very important to use PreparedStatement here to avoid any kind of SQL injection
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, testedMethod.toString());
            stmt.setString(2, generator.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String data = rs.getString("data");
                int numberOfMeasurements = rs.getInt("numberOfMeasurements");
                long time = rs.getLong("time");

                BenchmarkResult item = new BenchmarkResultImpl(new Statistics("{" + time + "}"),
                        new BenchmarkSettingImpl(testedMethod, generator, new MethodArgumentsImpl(data), numberOfMeasurements));
                
                list.add(item);
            }
            return list;
        } catch (SQLException e) {
            log.log(Level.INFO, "Unable to retrieve results from database", e);
            return null;
        }
    }
}
