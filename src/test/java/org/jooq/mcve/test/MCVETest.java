/*
 * This work is dual-licensed
 * - under the Apache Software License 2.0 (the "ASL")
 * - under the jOOQ License and Maintenance Agreement (the "jOOQ License")
 * =============================================================================
 * You may choose which license applies to you:
 *
 * - If you're using this work with Open Source databases, you may choose
 *   either ASL or jOOQ License.
 * - If you're using this work with at least one commercial database, you must
 *   choose jOOQ License
 *
 * For more information, please visit http://www.jooq.org/licenses
 *
 * Apache Software License 2.0:
 * -----------------------------------------------------------------------------
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * jOOQ License and Maintenance Agreement:
 * -----------------------------------------------------------------------------
 * Data Geekery grants the Customer the non-exclusive, timely limited and
 * non-transferable license to install and use the Software under the terms of
 * the jOOQ License and Maintenance Agreement.
 *
 * This library is distributed with a LIMITED WARRANTY. See the jOOQ License
 * and Maintenance Agreement for more details: http://www.jooq.org/licensing
 */
package org.jooq.mcve.test;

import static org.jooq.mcve.Tables.TEST;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.mcve.tables.records.TestRecord;

import org.jooq.tools.StopWatchListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MCVETest {

    Connection connection;
    DSLContext ctx;

    @Before
    public void setup() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:~/mcve", "sa", "");
        DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
        //NOTE!
        //Comment out the below line when using Jooq 3.9.1 to see original behaviour.
        //The StopWatchListener has a stop watch per listener instance. If there is only one listener for all queries,
        //it will always use the same stop watch on every thread and for every query execution. It is better to
        //create a new StopWatchListener per query execution, by wrapping it in an ExecuteListenerProvider
        jooqConfiguration.setExecuteListenerProvider(StopWatchListener::new);
        jooqConfiguration.setConnection(connection);
        ctx = DSL.using(jooqConfiguration);
    }

    @After
    public void after() throws Exception {
        ctx = null;
        connection.close();
        connection = null;
    }


    @Test
    public void mcveTest3_11_10() throws InterruptedException {
        //Quite crude, but you will see that the sleeps here are added to the "Finishing" log outputs in a cumulative fashion for the entire test run.
        //i.e. here we have a 10second wait, below we have a 5 second wait. The last Finishing entry will be 15 seconds plus.
        //
        Thread.sleep(10000);
        TestRecord result =
                ctx.insertInto(TEST)
                        .columns(TEST.VALUE)
                        .values(42)
                        .returning(TEST.ID)
                        .fetchOne();

        result.refresh();
        assertEquals(42, (int) result.getValue());

        //second query execution, you should see a log in the output like:
        //13:36:04,762 DEBUG [org.jooq.tools.StopWatch                          ] - Finishing                : Total: 15.506s, +0.806ms
        Thread.sleep(5000);
        TestRecord result2 =
                ctx.insertInto(TEST)
                        .columns(TEST.VALUE)
                        .values(42)
                        .returning(TEST.ID)
                        .fetchOne();

        result2.refresh();
        assertEquals(42, (int) result2.getValue());
    }

//    @Test
//    public void mcveTest3_9_1() throws InterruptedException {
//        //in 3.9.1 you will see no such cumulative behaviour of the "Finishing" times log entries
//        //
//        Thread.sleep(10000);
//        TestRecord result =
//                ctx.insertInto(TEST)
//                        .columns(TEST.VALUE)
//                        .values(42)
//                        .returning(TEST.ID)
//                        .fetchOne();
//
//
//        Thread.sleep(5000);
//        TestRecord result2 =
//                ctx.insertInto(TEST)
//                        .columns(TEST.VALUE)
//                        .values(42)
//                        .returning(TEST.ID)
//                        .fetchOne();
//
//
//    }

}

