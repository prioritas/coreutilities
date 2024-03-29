package coreutilities.sql;

// import org.hsql.Profile;
import java.io.File;

import java.sql.DriverManager;
import java.sql.Connection;

public class SQLUtil 
{
  // Default values  
  private static String dbName   = ".";
  private static String dbURL    = "//localhost:80/";
  private static String userName = "SA"; // System Admin
  private static String password = "";
  
  public static Connection getConnection() throws Exception 
  {
    return getConnection(".");
  }
  
  public static Connection getConnection(String dbLoc) 
         throws Exception
  {
    return getConnection(dbLoc, dbName, userName, password);
  }
  
  /**
   * Get hSQL Connection in standalone.
   * 
   * @param dbLoc
   * @param db
   * @param user
   * @param pwd
   * @return
   * @throws Exception
   */
  public static Connection getConnection(String dbLoc, String db, String user, String pwd) 
         throws Exception
  {
    dbName   = db;
    userName = user;
    password = pwd;
    Connection cConnection = null;
//  int max            = 500;
//  boolean persistent = true;
//  boolean update     = false;
//  String dbPath = System.getProperty("user.dir") + dbLoc;
//  dbPath = dbPath.replace(File.separatorChar, '/');
    String connectString = "jdbc:hsqldb:file:" + (dbLoc.length() > 0?(dbLoc + File.separator):"") + dbName;
//  String connectString = "jdbc:hsqldb:file:" + (dbPath.length() > 0?(dbPath + "/"):"") + dbName;
    try 
    {
      DriverManager.registerDriver(new org.hsqldb.jdbcDriver());
      System.out.println("Connecting to " + connectString);
//    cConnection = DriverManager.getConnection("jdbc:HypersonicSQL:" + dbName, userName, password);
      cConnection = DriverManager.getConnection(connectString, userName, password);
//    Profile.listUnvisited();
      cConnection.setAutoCommit(false);
      System.out.println("Using " + cConnection.getMetaData().getDriverName() + ", " + 
                                    cConnection.getMetaData().getDatabaseProductName() +  " " + 
                                    cConnection.getMetaData().getDatabaseProductVersion()); 
    } 
    catch(Exception e) 
    {
      System.err.println("With connect string [" + connectString + "] " + userName + "/" + password);
      System.err.println("hSQL Error: " + e.getMessage());
      throw e;
    }
    return cConnection;
  }
  
  public static Connection getServerConnection(String dburl, String user, String pwd) 
         throws Exception
  {
    dbURL   = dburl;
    userName = user;
    password = pwd;
    Connection cConnection = null;
  //  int max            = 500;
  //  boolean persistent = true;
  //  boolean update     = false;
    try 
    {
      DriverManager.registerDriver(new org.hsqldb.jdbcDriver());
      String connectString = "jdbc:hsqldb:hsql:" + dbURL;
      System.out.println("Connecting to " + connectString);
  //    cConnection = DriverManager.getConnection("jdbc:HypersonicSQL:" + dbName, userName, password);
      cConnection = DriverManager.getConnection(connectString, userName, password);
  //    Profile.listUnvisited();
      cConnection.setAutoCommit(false);
      System.out.println("Using " + cConnection.getMetaData().getDriverName() + ", " + 
                                    cConnection.getMetaData().getDatabaseProductName() +  " " + 
                                    cConnection.getMetaData().getDatabaseProductVersion()); 
    } 
    catch(Exception e) 
    {
      System.err.println("hSQL Error: " + e.getMessage());
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
}
