package coreutilities.samples;

import coreutilities.NotificationCheck;

import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

public class NotificationTest
{
//private final static SimpleDateFormat SDF = new SimpleDateFormat("E dd MMM yyyy, HH:mm:ss z");
  private final static String NOTIFICATION_PROP_FILE_NAME = "notification.properties";

  public static void main(String[] args) throws Exception
  {
    String notificationDate = "";
    Calendar now = new GregorianCalendar();
    now.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
    Date providedDate = now.getTime();
    
    Properties props = new Properties();
    try
    {
      FileInputStream fis = new FileInputStream(NOTIFICATION_PROP_FILE_NAME);
      props.load(fis);
      fis.close();
      notificationDate = props.getProperty("date"); // UTC date
      
      if (providedDate != null)
      {
        Date propertiesDate = NotificationCheck.getDateFormat().parse(notificationDate);
        System.out.println("Properties Date:" + propertiesDate.toString() + ", Provided Date:" + providedDate.toString());
        if (propertiesDate.before(providedDate))
          notificationDate = NotificationCheck.getDateFormat().format(providedDate);          
      }
    }
    catch (Exception ex)
    {
      System.out.println("Properties file not found");
    }    

    NotificationCheck nc = new NotificationCheck("WW", notificationDate);
    Map<Date, String> map = nc.check();
    for (Date d : map.keySet())
    {
      String mess = map.get(d);
      System.out.println(d.toString() + "\n" + mess);
    }
    
    props.setProperty("date", NotificationCheck.getDateFormat().format(new Date())); // Write local date
    FileOutputStream fos = new FileOutputStream(NOTIFICATION_PROP_FILE_NAME);
    props.store(fos, "Last notification date");
    fos.close();
  }
}
