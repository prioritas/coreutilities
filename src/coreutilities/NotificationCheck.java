package coreutilities;

import java.io.StringReader;

import java.net.URLEncoder;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import java.util.TimeZone;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;

import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.NodeList;

public class NotificationCheck
{
  private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  static
  {
    SDF.setTimeZone(TimeZone.getTimeZone("GMT"));    
  }
  
  private final static boolean verbose = false;
  
  private final static String NOTIFICATION_URL = "http://donpedro.lediouris.net/php/notification/notification.php"; 
  
  private String date = "";
  private String prod = "";
  
  private String productName = "";
  
  public NotificationCheck(String product)
  {
    this(product, null);
  }
  
  public NotificationCheck(String product, String date) 
  {
    super();
    this.prod = product;
    this.date = date;
  }
  
  public Map<Date, String> check()
  {
    Hashtable<Date, String> map = new Hashtable<Date, String>();
    try
    {
      String url = NOTIFICATION_URL + "?after=" + URLEncoder.encode(date, "UTF-8") + "&prod=" + prod;
      System.out.println(url);
      String notification = HTTPClient.getContent(url);
      if (verbose)
        System.out.println(notification);
      DOMParser parser = new DOMParser();
      parser.parse(new StringReader(notification));
      XMLDocument doc = parser.getDocument();
      String xPath = "//product";
      NodeList product = doc.selectNodes(xPath);
      if (product.getLength() > 0)
      {  
        for (int i=0; i<product.getLength(); i++)
        {
          XMLElement xe = (XMLElement)product.item(i);
          productName = xe.getAttribute("name");
        }
      }
      xPath = "//message";
      NodeList messageList = doc.selectNodes(xPath);
      if (messageList.getLength() > 0)
      {  
        for (int i=0; i<messageList.getLength(); i++)
        {
          XMLElement xe = (XMLElement)messageList.item(i);
          
          String nDate   = xe.getAttribute("date");
          String content = xe.getTextContent();
          map.put(SDF.parse(nDate), content);
        }
      }
      else
        System.out.println("No new notification");
    }
    catch (Exception ex)
    {
      System.err.println(ex.getLocalizedMessage());
//    ex.printStackTrace();
    }
    return map;
  }
  
  public static SimpleDateFormat getDateFormat()
  {
    return (SimpleDateFormat)SDF.clone();
  }

  public String getProductName()
  {
    return productName;
  }
}
