package coreutilities;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;


public class HTTPClient
{
  public static String getContent(String url) throws Exception
  {
    String ret = null;
    try
    {
      byte content[] = readURL(new URL(url));
      ret = new String(content);
    }
    catch(Exception e)
    {
      throw e;
    }
    return ret;
  }

  private static byte[] readURL(URL url) throws Exception
  {
    byte content[] = null;
    try
    {
      URLConnection newURLConn = url.openConnection();
      InputStream is = newURLConn.getInputStream();
      byte aByte[] = new byte[2];
      int nBytes;
      long started = System.currentTimeMillis();
      int nbLoop = 1;
      while((nBytes = is.read(aByte, 0, 1)) != -1) 
      {
        content = Utilities.appendByte(content, aByte[0]);
        if (content.length > (nbLoop * 1000))
        {
          long now = System.currentTimeMillis();
          long delta = now - started;
          double rate = (double)content.length / ((double)delta / 1000D);
          System.out.println("Downloading at " + rate + " bytes per second.");
          nbLoop++;
        }
      }
    }
    catch(IOException e)
    {
      System.err.println("ReadURL for " + url.toString() + "\nnewURLConn failed :\n" + e);
      throw e;
    }
    catch(Exception e)
    {
      System.err.println("Exception for: " + url.toString());
    }
    return content;
  }
}
