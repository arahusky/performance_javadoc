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
package example005;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.AfterBenchmark;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.AfterMeasurement;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

/**
 * Workload generator for DatabaseInsert class.
 *
 * @author Jakub Naplava
 */
public class DatabaseInsertGenerator {

    private static final String DRIVER = "org.sqlite.JDBC";
    public static final String JDBC_URL = "jdbc:sqlite:test.db";

    private Connection connection;

    //generator must still have empty ctor so that our harness can instantiate him
    public DatabaseInsertGenerator() {

    }

    public DatabaseInsertGenerator(Connection connection) {
        this.connection = connection;
    }

    @Generator(description = "Generates table and records to be inserted into this table", name = "Generator")
    public static void generateInput(
            Workload workload,
            ServiceWorkload service,
            @ParamNum(description = "Number of records", min = 1, max = 50000, step = 1) int recordsCount
    ) throws ClassNotFoundException, SQLException {
        Class.forName(DRIVER);

        Connection conn = createConnection(JDBC_URL);

        createTable(conn);

        Random r = new Random();

        int id = r.nextInt();
        Long[] times = new Long[recordsCount];

        for (int i = 0; i < recordsCount; i++) {
            times[i] = r.nextLong();
        }

        workload.addCall(null, new Object[]{conn, id, times});

        workload.setHooks(new DatabaseInsertGenerator(conn));
    }

    private static void createTable(Connection conn) {
        String query = "CREATE TABLE measurement_detailed ("
                + " id INTEGER, "
                + " time BIGINT"
                + ")";
        try {
            conn.createStatement().execute(query);
        } catch (SQLException ex) {
            System.out.println("Table already exists.");
        }
    }

    private static Connection createConnection(String connection_url) throws SQLException {
        try {
            return DriverManager.getConnection(connection_url);
        } catch (SQLException e) {
            throw e;
        }
    }

    @AfterMeasurement
    public void emptyTables(Object instance, Object[] methodArgs) throws SQLException {
        String query = "DELETE FROM measurement_detailed";

        Statement stmt = connection.createStatement();
        stmt.execute(query);
    }

    @AfterBenchmark
    public void close() throws SQLException {
        dropTable();

        connection.close();

        try {
            DriverManager.getConnection("jdbc:derby:testDatabase;shutdown=true");
        } catch (SQLException se) {
            if (((se.getErrorCode() == 50000)
                    && ("XJ015".equals(se.getSQLState())))) {
                // we got the expected exception
            }
        }

        deleteDir(new File("./testDatabase"));
    }

    public void deleteDir(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDir(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        directory.delete();
    }

    private void dropTable() throws SQLException {
        String query = "DROP TABLE measurement_detailed";

        Statement stmt = connection.createStatement();
        stmt.execute(query);
    }
}
