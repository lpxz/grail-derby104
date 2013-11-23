package derby5561;



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
public class Derby5561
{
	public static String dbName = "jdbcDemoDB";
    /**
     * Invoke the test, providing a number of threads and a number of iterations.
     * @param s arguments to the function, must be two integers: number of thread and number of iterations
     */
    static public void main(String[] args)
    {  // 6408 original   13147 afix
    	try
        {
    		dbName=dbName + System.currentTimeMillis();// avoid duplicated database from run to run.
            Derby5561 instance = new Derby5561();
            
            System.out.println("here");
            
            instance.doit(1);// number of threads

        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Creates the test object, registering the Derby embedded driver.  If the test database does not already exists,
     * creates that Derby database.
     * @throws ClassNotFoundException thrown if the driver class could not be found, probably due to classpath problems
     * @throws SQLException 
     */
    
    private Derby5561() throws ClassNotFoundException, SQLException
    {
    	      String driver = "org.apache.derby.jdbc.ClientDriver";
    	      String connectionURL = "jdbc:derby://localhost:1527/" + dbName + ";create=true";
    	      PreparedStatement psInsert;
    	      ResultSet myWishes;
    	      String printLine = "  __________________________________________________";
    	      String createString = "CREATE TABLE WISH_LIST  "
    	        +  "(WISH_ID INT NOT NULL GENERATED ALWAYS AS IDENTITY " 
    	        +  "   CONSTRAINT WISH_PK PRIMARY KEY, " 
    	        +  " ENTRY_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
    	        +  " WISH_ITEM VARCHAR(32) NOT NULL) " ;
    	      String answer;
    	      Class.forName(driver); 

        
        
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
          
          
          //lpxz, removed to fix bugs.
//          ClientConnectionPoolDataSource cpds =threads[0].cpds; // main and thread 0 share the clientConnectionPool, which leads to derby5561
//          if(cpds!=null){
//        	  cpds.setServerName("localhost");
//              cpds.setDatabaseName(dbName);
//              cpds.setCreateDatabase("create");
//              PooledConnection pc=null;
//    			try {
//    				pc = cpds.getPooledConnection();
//    				 pc.getConnection();//"jdbc:derby:DERBY2861;create=true"
//    			} catch (SQLException e1) {
//    				// TODO Auto-generated catch block
//    				e1.printStackTrace();
//    			}
//    			int local = -1;
//    	    	synchronized (ExecutorThread.class) {
//    				local = (ExecutorThread.counter++);
//    			}
//    	        try
//    	        {//lpxz
//    	        	executeStatement(pc.getConnection(), "CREATE VIEW viewSource"+local + " AS SELECT col1, col2 FROM schemamain.SOURCETABLE");
//    	        	 executeStatement(pc.getConnection(),  "DROP VIEW " + "viewSource"+local );
//    	        }catch(Exception e){
//    	        	e.printStackTrace();
//    	        }
//          }
          
          
          
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
