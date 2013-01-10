package coreutilities.sql;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SQLiteUtil 
{
  // Default values  
  private static String dbName   = ".";
  
  /**
   * Get SQLite Connection in standalone.
   * 
   * http://www.zentus.com/sqlitejdbc/
   * 
   * @param dbLoc
   * @return SQL Lite Connection
   * @throws Exception
   */
  public static Connection getConnection(String dbLoc) 
         throws Exception
  {
    dbName   = dbLoc;
    Connection cConnection = null;
    try 
    {
      DriverManager.registerDriver(new org.sqlite.JDBC());
      String connectString = "jdbc:sqlite:" + dbName;
      System.out.println("Connecting to " + connectString);
      cConnection = DriverManager.getConnection(connectString); //, userName, password);
      cConnection.setAutoCommit(false);
      System.out.println("Using " + cConnection.getMetaData().getDriverName() + ", " + 
                                    cConnection.getMetaData().getDatabaseProductName() +  " " + 
                                    cConnection.getMetaData().getDatabaseProductVersion()); 
    } 
    catch(Exception e) 
    {
      System.err.println("From " + SQLiteUtil.class.getName() + ": SQLite Error: " + e.getMessage());
      throw e;
    }
    return cConnection;
  }
  
  public static String rPad(String str, int len)
  {
    return rPad(str, len, " ");
  }
  
  public static String rPad(String str, int len, String c)
  {
    String s = (str==null?"[null]":str);
    while (s.length() < len)
      s += c;
    return s;
  }
  
  public static void shutdown(Connection c) throws Exception
  {
    try
    {
//    c.rollback(); // Whatever has not been saved is rolled back.
      c.close();
    }
    catch (Exception e)
    { throw e; }
  }
  
  /**
   * Sample
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception
  {
    String dbLocation = "C:\\_mywork\\dev-corner\\olivsoft\\TideEngine\\sqlite\\tidedb";
    if (args.length > 0)
      dbLocation = args[0];
    long before = System.currentTimeMillis();
    Connection conn = getConnection(dbLocation);
    long after = System.currentTimeMillis();
    System.out.println("Connected in " + Long.toString(after - before) + " ms.");
    
    before = System.currentTimeMillis();
    PreparedStatement pStmt = conn.prepareStatement("select count(*) from stations");
    ResultSet rs = pStmt.executeQuery();
    while (rs.next())
    {
      int i = rs.getInt(1);
      System.out.println("Returned " + Integer.toString(i) + " tide station(s).");  
    }
    rs.close();
    after = System.currentTimeMillis();
    System.out.println("Query took " + Long.toString(after - before) + " ms.");
    
    conn.close();
    System.out.println("Everything's closed.");
  }
}
