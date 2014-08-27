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

package cz.cuni.mff.d3s.tools.perfdoc.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;

/**
 *
 * @author Jakub Naplava
 */
public class ResultCache {
//    static void run()
//    {
//        Connection conn = null;
//        ArrayList<Statement> statements = new ArrayList<Statement>(); // list of Statements, PreparedStatements
//        PreparedStatement psInsert;
//        PreparedStatement psUpdate;
//        Statement s;
//        ResultSet rs = null;
//        try
//        {
//            String dbName = "someDBname"; // the name of the database
//
//            conn = DriverManager.getConnection(protocol + dbName
//                    );
//
//            System.out.println("Connected to and created database " + dbName);
//
//            // We want to control transactions manually. Autocommit is on by
//            // default in JDBC.
//            conn.setAutoCommit(false);
//
//            /* Creating a statement object that we can use for running various
//             * SQL statements commands against the database.*/
//            s = conn.createStatement();
//            statements.add(s);
//
//            // We create a table...
//            s.execute("create table location(num int, addr varchar(40))");
//            System.out.println("Created table location");
//        } catch (Exception e) {}
//
//    }
}
