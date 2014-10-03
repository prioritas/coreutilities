package coreutilities.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.text.SimpleDateFormat;

import java.util.Date;

public class Logger
{
  public final static int NONE    = 0;
  public final static int INFO    = 1;
  public final static int WARNING = 2;
  public final static int DEBUG   = 3;
  public final static int VERBOSE = 4;
  
  private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss");
  
  public static void log(String mess, int level)
  {
    int requiredLevel = NONE;
    try { requiredLevel = Integer.parseInt(System.getProperty("log.level", "0")); } catch (Exception ex)
    {
      System.err.println("Logger:" + ex.getLocalizedMessage());
    }
    if (level <= requiredLevel)
    {
      String fileName = System.getProperty("log.file.name", "log.log");
      File logFile = new File(fileName);
      try
      {
        BufferedWriter br = new BufferedWriter(new FileWriter(logFile, true));
        // Who called me
        Throwable t = new Throwable(); 
        StackTraceElement[] elements = t.getStackTrace();         
        String message = SDF.format(new Date()) + " : " + (elements.length > 1 ? (elements[1].toString() + " : ") : "") + mess;
        br.write(message + "\n");
        br.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }
}
