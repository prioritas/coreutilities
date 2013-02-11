package coreutilities.sql;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class HSQLPlus
{
  private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
  private static BufferedReader input = stdin;
  private static boolean fromConsole = true;
  
  public static String userInput(String prompt)
  {
    String retString = "";
    if (fromConsole) 
      System.err.print(prompt);
    try
    {
      retString = input.readLine();
    }
    catch(Exception e)
    {
      System.out.println(e);
      String s;
      try
      {
        s = userInput("<Oooch/>");
      }
      catch(Exception exception) { }
    }
    return retString;
  }

  private static boolean connected     = false;
  private static Connection connection = null;
  private static boolean keepworking   = true;
  
  private static String database = "";
  private static String username = "";
  private static String password = "";  
  
  public static void main(String[] args) throws Exception
  {
    if (args.length > 0)
    {
      if (args.length > 0) database = args[0];
      if (args.length > 1) username = args[1];
      if (args.length > 2) password = args[2];
    }
    
    while (keepworking)
    {
      String str = userInput("hSql > ");
      manageCommand(str);
    }
    if (connected)
    {
      // disconnect
      SQLUtil.shutdown(connection);
    }
    System.out.println("Bye...");
  }
  
  private static void manageCommand(String str) throws Exception
  {
    if (str.trim().toUpperCase().equals("EXIT") ||
        str.trim().toUpperCase().equals("EXIT;") ||
        str.trim().toUpperCase().equals("QUIT") ||
        str.trim().toUpperCase().equals("QUIT;"))
    {
      keepworking = false;
    }
    else if (str.trim().toUpperCase().equals("HELP") ||
             str.trim().toUpperCase().equals("HELP;"))
    {
      displayHelp();
    }
    else if (str.trim().toUpperCase().equals("CONNECT") ||
             str.trim().toUpperCase().equals("CONNECT;"))
    {
      if (database.trim().length() > 0)
        connection = SQLUtil.getConnection(".", database, username, password);
      else
        connection = SQLUtil.getConnection();
      connected = true;
    }
    else if (str.toUpperCase().startsWith("ECHO "))
    {
      System.out.println(str.substring("ECHO ".length()));
    }
    else if (str.toUpperCase().startsWith("--"))
    {
      // Comment
    }
    else if (str.toUpperCase().startsWith("@"))
    {
      String filename = str.substring("@".length());
      try
      {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        input = br;
        fromConsole = false;
        String s = null;
        while ((s = input.readLine()) != null)
        {
//        System.out.println("[" + s + "]");
          manageCommand(s);
        }
        input.close();
        input = stdin;
        fromConsole = true;
      }
      catch (Exception ex)
      {
        System.err.println("Ooops");
        ex.printStackTrace();
      }
    }
    else if (str.toUpperCase().trim().startsWith("SELECT")) // No trailing blank!!
    {
      if (connected)
      {
        boolean completed = str.trim().endsWith(";");
        while (!completed)
        {
          str += (" " + userInput("   + > "));          
          completed = str.trim().endsWith(";");
        }
        try
        {
          Statement stmt = connection.createStatement();
          ResultSet rs = stmt.executeQuery(str);
          ResultSetMetaData rsmd = rs.getMetaData();
          
          String header = "";
          for (int i=0; i<rsmd.getColumnCount(); i++)
          {          
            String colName = rsmd.getColumnName(i + 1);
            int ds        = rsmd.getColumnDisplaySize(i + 1);
            int scale     = rsmd.getScale(i + 1);
            int precision = rsmd.getPrecision(i + 1);
            header += (colName + "  ");
          }  
          System.out.println(header);
          for (int i=0; i<header.length(); i++)
            System.out.print("-");
          System.out.println();
          while (rs.next())
          {
            for (int i=0; i<rsmd.getColumnCount(); i++)
            {            
              String col = rs.getString(i + 1);
              System.out.print(col + "  ");
            }
            System.out.println();
          }
        }
        catch (Exception ex)
        {
          System.out.println("Oops:" + ex.toString());
        }
      }
      else
        System.out.println("Not connected. Connect first.");
    }
    else if (str.trim().toUpperCase().equals("COMMIT;"))
    {
      if (connected)
        connection.commit();
      else
        System.out.println("Not connected...");
    }
    else if (str.trim().toUpperCase().equals("ROLLBACK;"))
    {
      if (connected)
        connection.rollback();
      else
        System.out.println("Not connected...");
    }
    else if (str.toUpperCase().trim().startsWith("INSERT") ||
             str.toUpperCase().trim().startsWith("DELETE") ||
             str.toUpperCase().trim().startsWith("UPDATE"))
    {
      boolean completed = str.trim().endsWith(";");
      while (!completed)
      {
        str += (" " + userInput("   + > "));          
        completed = str.trim().endsWith(";");
      }
      if (connected)
      {
        try
        {
          Statement stmt = connection.createStatement();
          stmt.execute(str.trim());
        }
        catch (Exception ex)
        {
          System.err.println(ex.getMessage());
        }
      }
      else
        System.out.println("Not connected...");
    }
    else if (str.trim().length() > 0) // Other commands ( CREATE TABLE, etc)
    {
      boolean completed = str.trim().endsWith(";");
      while (!completed)
      {
        str += (" " + userInput("   + > "));          
        completed = str.trim().endsWith(";");
      }
      if (connected)
      {
        try
        {
          Statement stmt = connection.createStatement();
          stmt.execute(str.trim());
        }
        catch (Exception ex)
        {
          System.err.println(ex.getMessage());
        }
      }
      else
        System.out.println("Not connected...");
    }      
  }  
  
  private static void displayHelp()
  {
    System.out.println("connect[;]");
    System.out.println("select ... ;");
    System.out.println("commit;");
    System.out.println("rollback;");
    System.out.println("echo <whatever string>");
    System.out.println("@<SQL Command File>");
    System.out.println("help[;]");
    System.out.println("exit[;]");
    System.out.println("quit[;]");
    System.out.println("More help comes soon...");
  }
}
