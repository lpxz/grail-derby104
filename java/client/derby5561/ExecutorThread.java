package derby5561;

import java.sql.SQLException;

import javax.sql.PooledConnection;

import org.apache.derby.jdbc.ClientConnectionPoolDataSource;

public class ExecutorThread extends Thread {
	public ClientConnectionPoolDataSource cpds;
	public static int counter =300;
	public void run()
	{
    	// prepare the table
    	 cpds =
                new ClientConnectionPoolDataSource();
            cpds.setServerName("localhost");
            cpds.setDatabaseName(Derby5561.dbName);
            cpds.setCreateDatabase("create");
            PooledConnection pc=null;
			try {
				pc = cpds.getPooledConnection();
				 pc.getConnection();//"jdbc:derby:DERBY2861;create=true"
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
             
    	try {
    		  executeStatement(pc.getConnection(), "CREATE TABLE schemamain.SOURCETABLE (col1 int, col2 char(10), col3 varchar(20), col4 decimal(10,5))");
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
		for(int i=0; i<=200;i++){
			int local = -1;
	    	synchronized (ExecutorThread.class) {
				local = (counter++);
			}
	        try
	        {//lpxz
	        	executeStatement(pc.getConnection(), "CREATE VIEW viewSource"+local + " AS SELECT col1, col2 FROM schemamain.SOURCETABLE");
	        	 executeStatement(pc.getConnection(),  "DROP VIEW " + "viewSource"+local );
	        }catch(Exception e){//shutup
	        	//e.printStackTrace();
	        }
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
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
