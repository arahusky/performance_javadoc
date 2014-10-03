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
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.MeasurementResult;
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
     public List<MeasurementResult> getResults() {

        ArrayList<MeasurementResult> list = new ArrayList<>();

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

                MeasurementResult item = new MeasurementResult(new MethodInfo(methodName), new MethodInfo(generator), data, numberOfMeasurements, time);
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
    public ArrayList<String> getDistinctTestedMethods() {
        ArrayList<String> list = new ArrayList<>();

        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT DISTINCT methodName FROM results";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String methodName = rs.getString("methodName");

                list.add(methodName);
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
    public ArrayList<String> getDistinctClassMethods(String className) {
        ArrayList<String> list = new ArrayList<>();

        try {
            String query = "SELECT DISTINCT methodName "
                    + "FROM results "
                    + "WHERE methodName LIKE ?";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, className + "#%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String methodName = rs.getString("methodName");

                list.add(methodName);
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
    public ArrayList<String> getDistinctGenerators(String methodName) {
        ArrayList<String> list = new ArrayList<>();

        try {
            String query = "SELECT DISTINCT generator "
                    + "FROM results "
                    + "WHERE methodName = ?";

            //it is very important to use PreparedStatement here to avoid any kind of SQL injection
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, methodName);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String generator = rs.getString("generator");

                list.add(generator);
            }
            return list;
        } catch (SQLException e) {
            log.log(Level.INFO, "Unable to retrieve results from database", e);
            return null;
        }
    }

@Override
    public List<MeasurementResult> getResults(String testedMethod, String generator) {
        ArrayList<MeasurementResult> list = new ArrayList<>();

        try {
            String query = "SELECT * "
                    + "FROM results "
                    + "WHERE (methodName = ? AND generator = ?)";
             //it is very important to use PreparedStatement here to avoid any kind of SQL injection
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, testedMethod);
            stmt.setString(2, generator);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String data = rs.getString("data");
                int numberOfMeasurements = rs.getInt("numberOfMeasurements");
                long time = rs.getLong("time");

                MeasurementResult item = new MeasurementResult(new MethodInfo(testedMethod), new MethodInfo(generator), data, numberOfMeasurements, time);

                list.add(item);
            }
            return list;
        } catch (SQLException e) {
            log.log(Level.INFO, "Unable to retrieve results from database", e);
            return null;
        }
    }
}
