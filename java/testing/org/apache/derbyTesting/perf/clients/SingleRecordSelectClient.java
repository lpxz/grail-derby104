/*

Derby - Class org.apache.derbyTesting.perf.clients.SingleRecordSelectClient

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package org.apache.derbyTesting.perf.clients;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

/**
 * Client which performs single-record lookups on tables generated by
 * {@code SingleRecordFiller}. Each time the client's {@code doWork()} method
 * is called, it will pick one of the tables randomly, and select one random
 * record in that table.
 */
public class SingleRecordSelectClient implements Client {

    private Connection conn;

    private final PreparedStatement[] pss;
    private final Random r;
    private final int tableSize;

    /**
     * Construct a new single-record select client.
     *
     * @param records the number of records in each table in the test
     * @param tables the number of tables in the test
     */
    public SingleRecordSelectClient(int records, int tables) {
        tableSize = records;
        r = new Random();
        pss = new PreparedStatement[tables];
    }

    public void init(Connection c) throws SQLException {
        for (int i = 0; i < pss.length; i++) {
            String tableName = SingleRecordFiller.getTableName(tableSize, i);
            String sql = "SELECT * FROM " + tableName + " WHERE ID = ?";
            pss[i] = c.prepareStatement(sql);
        }
        c.setAutoCommit(false);
        conn = c;
    }

    public void doWork() throws SQLException {
        PreparedStatement ps = pss[r.nextInt(pss.length)];
        ps.setInt(1, r.nextInt(tableSize));
        ResultSet rs = ps.executeQuery();
        rs.next();
        rs.getInt(1);
        rs.getString(2);
        rs.close();
        conn.commit();
    }
}
