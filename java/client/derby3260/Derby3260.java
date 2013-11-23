package derby3260;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.PooledConnection;

import org.apache.derby.jdbc.ClientConnectionPoolDataSource;
import org.apache.derby.jdbc.ClientDriver;
import org.apache.derby.jdbc.EmbeddedDriver;

//10.3.2.1
/**
 * This class tests the thread-safeness of the Derby database system, using the embedded driver.  With the
 * proper choice of main arguments, which are probably different for different machines, it will sometimes show an
 * SQLException with the following output from printStackTrace().  It may need to be run more than once, as having
 * leftover views existing from prior runs sometimes seems to have an effect.
 *  
 * <pre>
 * java.lang.NullPointerException
 *     at org.apache.derby.iapi.sql.dictionary.TableDescriptor.getObjectName(TableDescriptor.java:758)
 *    at org.apache.derby.impl.sql.depend.BasicDependencyManager.getPersistentProviderInfos(BasicDependencyManager.java:677)
 *     at org.apache.derby.impl.sql.compile.CreateViewNode.bindViewDefinition(CreateViewNode.java:287)
 *     at org.apache.derby.impl.sql.compile.CreateViewNode.bind(CreateViewNode.java:183)
 *     at org.apache.derby.impl.sql.GenericStatement.prepMinion(GenericStatement.java:345)
 *     at org.apache.derby.impl.sql.GenericStatement.prepare(GenericStatement.java:119)
 *     at org.apache.derby.impl.sql.conn.GenericLanguageConnectionContext.prepareInternalStatement(GenericLanguageConnectionContext.java:745)
 *     at org.apache.derby.impl.jdbc.EmbedStatement.execute(EmbedStatement.java:568)
 *     at org.apache.derby.impl.jdbc.EmbedStatement.execute(EmbedStatement.java:517)
 *     at TestEmbeddedMultiThreading.executeStatement(TestEmbeddedMultiThreading.java:109)
 *     at TestEmbeddedMultiThreading.access$100(TestEmbeddedMultiThreading.java:10)
 *     at TestEmbeddedMultiThreading$ViewCreatorDropper.run(TestEmbeddedMultiThreading.java:173)
 *     at java.lang.Thread.run(Thread.java:534)
 * Stop here.
 * </pre>
 */
public class Derby3260
{
	public static String dbName = "";
    /**
     * Invoke the test, providing a number of threads and a number of iterations.
     * @param s arguments to the function, must be two integers: number of thread and number of iterations
     */
	
private static final String[][] TABLES = {
        
        { "t11_AutoGen", 
          "create table t11_AutoGen (c11 int, " +
          "c12 int generated always as identity (increment by 1))" },

        { "t31_AutoGen",
          "create table t31_AutoGen (c31 int, " +
          "c32 int generated always as identity (increment by 1), " +
          "c33 int default 2)" },

        { "t21_noAutoGen",
          "create table t21_noAutoGen (c21 int not null unique, c22 char(5))" },
    };

public static Connection conn = null;
    static public void main(String[] args)
    { ////@GenericActivationHolder
    	
    	// original 972, new: duration: 1037
    	
    	try
        {
    		dbName="DERBY3260" + System.currentTimeMillis();// avoid duplicated database from run to run.
            EmbeddedDriver driver = new EmbeddedDriver();
            
            
            try
            {
            	conn=java.sql.DriverManager.getConnection("jdbc:derby:"+ dbName + ";create=true");
            }
            catch (java.sql.SQLException e)
            {
                System.out.println("Exception creating database...assuming already exists");
            }

            
           
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            
            for (int i = 0; i < TABLES.length; i++) {
                s.execute(TABLES[i][1]);
            }
            
            for (int i = 0; i < TABLES.length; i++) {
                s.execute("DELETE FROM " + TABLES[i][0]);
            }
            s.execute("ALTER TABLE t11_AutoGen ALTER COLUMN c12 RESTART WITH 1");
            s.execute("ALTER TABLE t31_AutoGen ALTER COLUMN c32 RESTART WITH 1");
            s.close();
            conn.commit();
            
            
            new Derby3260().doit(4);// number of threads
        }
    	
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }


    


    
    private void executeStatement(java.sql.Connection conn, String sql) throws java.sql.SQLException
    {
        //System.out.println("" + Thread.currentThread() + " executing " + sql);
        java.sql.Statement stmt = null;
        try
        {
            stmt = conn.createStatement();
            stmt.execute(sql);
        }
        finally
        {
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                }
                catch (java.sql.SQLException e)
                {
                    System.out.println("Eating close() exception: " + e.getMessage());
                }
            }
        }
    }

    private void doit(int numThreads) throws Exception
    {

    	 ExecutorThread[] threads = new  ExecutorThread[numThreads];
          for(int i=0 ; i<threads.length; i++)
          {
        	  threads[i] = new ExecutorThread();
          }
          long start = System.currentTimeMillis();
          for(int i=0 ; i<threads.length; i++)
          {
        	  threads[i].start();
          }
          
          
      
          
          for(int i=0 ; i<threads.length; i++)
          {
        	  threads[i].join();
          }
          
          long end = System.currentTimeMillis();
          System.out.println("duration: " + (end-start));
    }

    /**
     *  Returns a new connection to the test database
     * @return a newly create connection
     * @throws java.sql.SQLException thrown if the connection cannot be created
     */
    private java.sql.Connection getConnection() throws java.sql.SQLException
    {
        return java.sql.DriverManager.getConnection("jdbc:derby:DERBY2861");
    }

    /**
     * Creates and executes a new SQL statement on the connection, ensuring that the statement is closed, regardless
     * of whether the statement execution throws an exception
     * @param conn the connection against which to run the statement
     * @param sql the SQL to execute
     * @throws java.sql.SQLException thrown if there is any SQL error executing the statement (or creating it)
     */
    

}
