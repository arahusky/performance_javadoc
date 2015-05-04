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

import cz.cuni.mff.d3s.tools.perfdoc.annotations.Workload;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Jakub Naplava
 */
public class DatabaseInsert {

    /**
     * <p>Very slow implementation of database insert. Does not use any batch
     * operations, but all inserts are done separately.</p>
     *
     * <p>Note that, when running benchmarks against it, results will be collected
     * pretty long even for low priority.</p>
     *
     * @param conn connection to DB
     * @param id identifier common for all records
     * @param times array containing records
     * @throws SQLException
     */
    @Workload("example005.DatabaseInsertGenerator#generateInput")
    public static void insertIndividual(Connection conn, int id, Long[] times) throws SQLException {

        Statement stmt = conn.createStatement();

        for (long time : times) {
            String query = "INSERT INTO measurement_detailed (id, time) "
                    + "VALUES (" + id + ", " + time + ")";

            stmt.execute(query);
        }
    }

    /**
     * Better implementation of database insert. Uses batch operations to insert
     * multiple records.
     *
     * @param conn connection to DB
     * @param id identifier common for all records
     * @param times array containing records
     * @throws SQLException
     */
    @Workload("example005.DatabaseInsertGenerator#generateInput")
    public static void insertBatch(Connection conn, int id, Long[] times) throws SQLException {
        //in order to increase performance of inserting multiple records, we turn the autocommit mode to false and commit transaction in the end
        String query = "INSERT INTO measurement_detailed (id, time) "
                + "VALUES (" + id + ",?)";

        conn.setAutoCommit(false);
        PreparedStatement preparedStmt = conn.prepareStatement(query);

        for (long val : times) {
            preparedStmt.setLong(1, val);
            preparedStmt.addBatch();
        }

        preparedStmt.executeBatch();
        conn.commit();

        conn.setAutoCommit(true);
    }
}
