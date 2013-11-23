package derby3260;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.PooledConnection;

import org.apache.derby.jdbc.ClientConnectionPoolDataSource;

public class ExecutorThread extends Thread {
	public static int counter =300;
	public void run()
	{
		 Statement s;
		try {
			//@GenericActivationHolder
			for(int i=0;i<10;i++){
				s = Derby3260.conn.createStatement();
				 s.execute("insert into t11_AutoGen(c11) values(999)");
	
			        String sqlStmt="update t11_AutoGen set c11=" + i;
			        s.execute(sqlStmt, Statement.RETURN_GENERATED_KEYS);
	
			        s.executeUpdate(sqlStmt, Statement.RETURN_GENERATED_KEYS);
	
			        s.close();
			}

//		        PreparedStatement ps =
//		                Derby3260.conn.prepareStatement(sqlStmt, Statement.RETURN_GENERATED_KEYS);
//		        
//		        
//		        
//		        
//		        ps.execute();
//
//		        ps.executeUpdate();
//
//		        ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
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
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
