package coreutilities;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;

import java.awt.Point;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.lang.reflect.Method;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class Utilities
{
  public static void copy(InputStream is, OutputStream os) throws IOException
  {
    synchronized (is)
    {
      synchronized (os)
      {
        byte[] buffer = new byte[256];
        while (true)
        {
          int bytesRead = is.read(buffer);
          if (bytesRead == -1)
            break;
         os.write(buffer, 0, bytesRead);
        }
      }
    }
  }  

  public static File findFileName(String str) throws Exception
  {
    File file = null;
    boolean go = true;
    int i = 1;
    while (go)
    {
      String newName = str + "_" + Integer.toString(i);
      File f = new File(newName);
      if (f.exists())
        i++;
      else
      {
        file = f;
        go = false;
      }
    }
    return file;
  }

  /**
   * 
   * @param filename
   * @param extension with the preceeding ".", like ".ptrn"
   * @return
   */
  public static String makeSureExtensionIsOK(String filename, String extension)
  {
    if (!filename.toLowerCase().endsWith(extension)) 
      filename += extension;
    return filename;  
  }
  
  public static String makeSureExtensionIsOK(String filename, String[] extension, String defaultExtension)
  {
    boolean extensionExists = false;
    for (int i=0; i<extension.length; i++)
    {
      if (filename.toLowerCase().endsWith(extension[i].toLowerCase())) 
      {
        extensionExists = true;
        break;
      }
    }
    if (!extensionExists) 
      filename += defaultExtension;
    return filename;  
  }
  
  public static String getMacAddress() throws IOException
  {
    String macAddress = null;
    if (System.getProperty("os.name").indexOf("Windows") > -1)
    {
      String command = "ipconfig /all";
      Process pid = Runtime.getRuntime().exec(command);
      BufferedReader in = new BufferedReader(new InputStreamReader(pid.getInputStream()));
      while (true)
      {
        String line = in.readLine();
        if (line == null)
          break;
        Pattern p = Pattern.compile(".*Physical Address.*: (.*)");
        Matcher m = p.matcher(line);
        if (m.matches())
        {
          macAddress = m.group(1);
          break;
        }
      }
      in.close();
    }
    else
      macAddress = "Unknown";
    return macAddress;
  }

//@SuppressWarnings("unchecked")
  public static void addURLToClassPath(URL url)
  {
    try
    {
      URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
      Class<?> c = /*(Class<URLClassLoader>)*/Class.forName("java.net.URLClassLoader");
      Class<?>[] parameterTypes = new Class<?>[1];
      parameterTypes[0] = /*(Class<?>)*/Class.forName("java.net.URL");
      Method m = c.getDeclaredMethod("addURL", parameterTypes);
      m.setAccessible(true);
      Object[] args = new Object[1];
      args[0] = url;
      m.invoke(urlClassLoader, args);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public static void openInBrowser(String page) throws Exception
  {
    URI uri = new URI(page);
    Desktop.getDesktop().browse(uri);
    
//    String os = System.getProperty("os.name");
//    if (os.indexOf("Windows") > -1)
//    {
//      String cmd = "";
//      if (page.indexOf(" ") != -1)
//        cmd = "cmd /k start \"" + page + "\"";
//      else
//        cmd = "cmd /k start " + page + "";
//      System.out.println("Command:" + cmd);
//      Runtime.getRuntime().exec(cmd); // Can contain blanks...
//    }
//    else if (os.indexOf("Linux") > -1) // Assuming htmlview
//      Runtime.getRuntime().exec("htmlview " + page);
//    else
//    {
//      throw new RuntimeException("OS [" + os + "] not supported yet");
//    }
  }
  
  public static void showFileSystem(String where) throws Exception
  {
    String os = System.getProperty("os.name");
    if (os.indexOf("Windows") > -1)
    {
      String cmd = "cmd /k start /D\"" + where + "\" .";
  //    System.out.println("Executing [" + cmd + "]");
      Runtime.getRuntime().exec(cmd); // Can contain blanks...
    }
    else if (os.indexOf("Linux") > -1) 
      Runtime.getRuntime().exec("nautilus " + where);
    else
    {
      throw new RuntimeException("OS [" + os + "] not supported yet");
    }
  }    
  
  public static int sign(double d)
  {
    int s = 0;
    if (d > 0.0D)
      s = 1;
    if (d < 0.0D)
      s = -1;
    return s;
  }

  public static void makeSureTempExists() throws IOException
  {
    File dir = new File("temp");
    if (!dir.exists())
      dir.mkdirs();
  }
  
  /**
   * remove leading and trailing blanks, CR, NL
   * @param str
   * @return
   */
  public static String superTrim(String str)
  {
    String str2 = "";
    char[] strChar = str.toCharArray();
    // Leading
    int i = 0;
    while (strChar[i] == ' ' ||
           strChar[i] == '\n' ||
           strChar[i] == '\r')
    {
      i++;
    }
    str2 = str.substring(i);
    while(str2.endsWith("\n") || str2.endsWith("\r"))
      str2 = str2.substring(0, str2.length() - 2);
    return str2.trim();    
  }
  
  public static String replaceString(String orig, String oldStr, String newStr)
  {
    String ret = orig;
    int indx = 0;
    for (boolean go = true; go;)
    {
      indx = ret.indexOf(oldStr, indx);
      if (indx < 0)
      {
        go = false;
      } 
      else
      {
        ret = ret.substring(0, indx) + newStr + ret.substring(indx + oldStr.length());
        indx += 1 + oldStr.length();
      }
    }
    return ret;
  }  

  public static byte[] appendByte(byte c[], byte b)
  {
    int newLength = c != null ? c.length + 1 : 1;
    byte newContent[] = new byte[newLength];
    for(int i = 0; i < newLength - 1; i++)
      newContent[i] = c[i];

    newContent[newLength - 1] = b;
    return newContent;
  }
    
  public static byte[] appendByteArrays(byte c[], byte b[], int n)
  {
    int newLength = c != null ? c.length + n : n;
    byte newContent[] = new byte[newLength];
    if (c != null)
    {
      for (int i=0; i<c.length; i++)
        newContent[i] = c[i];
    }
    int offset = (c!=null?c.length:0);
    for (int i=0; i<n; i++)
      newContent[offset + i] = b[i];
    return newContent;
  }
    
  public static String chooseFile(int mode,
                                  String flt,
                                  String desc,
                                  String title,
                                  String buttonLabel)
  {
    String fileName = "";
    JFileChooser chooser = new JFileChooser();
    if (title != null)
      chooser.setDialogTitle(title);
    if (buttonLabel != null)
      chooser.setApproveButtonText(buttonLabel);    
    if (flt != null)
    {
      ToolFileFilter filter = new ToolFileFilter(flt,
                                                 desc);                                               
      chooser.addChoosableFileFilter(filter);
      chooser.setFileFilter(filter);
    }
    chooser.setFileSelectionMode(mode);
    // Set current directory
    File f = new File(".");
    String currPath = f.getAbsolutePath();
    f = new File(currPath.substring(0, currPath.lastIndexOf(File.separator)));
    chooser.setCurrentDirectory(f);

    int retval = chooser.showOpenDialog(null);
    switch (retval)
    {
      case JFileChooser.APPROVE_OPTION:
        fileName = chooser.getSelectedFile().toString();
        break;
      case JFileChooser.CANCEL_OPTION:
        break;
      case JFileChooser.ERROR_OPTION:
        break;
    }
    return fileName;
  }  
  
  public static int drawPanelTable(String[][] data, Graphics gr, Point topLeft, int betweenCols, int betweenRows)
  {
    return drawPanelTable(data, gr, topLeft, betweenCols, betweenRows, null);
  }
  
  public final static int LEFT_ALIGNED   = 0;
  public final static int RIGHT_ALIGNED  = 1;
  public final static int CENTER_ALIGNED = 2;
  
  public static int drawPanelTable(String[][] data, Graphics gr, Point topLeft, int betweenCols, int betweenRows, int[] colAlignment)
  {
    Font f = gr.getFont();
    int[] maxLength = new int[data[0].length];
    for (int i=0; i<maxLength.length; i++)
      maxLength[i] = 0;
    
    // Identify the max length for each column
    for (int row=0; row<data.length; row++)
    {
      for (int col=0; col<data[row].length; col++)
      {
        int strWidth  = gr.getFontMetrics(f).stringWidth(data[row][col]);
        maxLength[col] = Math.max(maxLength[col], strWidth);
      }
    }
    int x = topLeft.x;
    int y = topLeft.y;
    
    // Now display
    for (int row=0; row<data.length; row++)
    {
      for (int col=0; col<data[row].length; col++)
      {
        int _x = x;
        for (int c=1; c<=col; c++)
          _x += (betweenCols + maxLength[c - 1]);
        if (colAlignment != null && colAlignment[col] != LEFT_ALIGNED)
        {
          int strWidth  = gr.getFontMetrics(f).stringWidth(data[row][col]);
          switch (colAlignment[col])
          {
            case RIGHT_ALIGNED:
              _x += (maxLength[col] - strWidth);
              break;
            case CENTER_ALIGNED:
              _x += ((maxLength[col] - strWidth) / 2);
              break;
            default:
              break;
          }
        }
        gr.drawString(data[row][col], _x, y);
      }
      y += (f.getSize() + betweenRows);
    }    
    return y;
  }
  
  static class ToolFileFilter extends FileFilter
  {
    private Hashtable<String, FileFilter> filters = null;
    private String description = null;
    private String fullDescription = null;
    private boolean useExtensionsInDescription = true;

    public ToolFileFilter()
    {
      this((String) null, (String) null);
    }

    public ToolFileFilter(String extension)
    {
      this(extension, null);
    }

    public ToolFileFilter(String extension, String description)
    {
      this(new String[] {extension}, description);
    }

    public ToolFileFilter(String[] filters)
    {
      this(filters, null);
    }

    public ToolFileFilter(String[] filters, String description)
    {
      this.filters = new Hashtable<String, FileFilter>(filters.length);
      for (int i = 0; i < filters.length; i++)
      {
        // add filters one by one
        addExtension(filters[i]);
      }
      setDescription(description);
    }

    public boolean accept(File f)
    {
      if(f != null)
      {
        if(f.isDirectory())
        {
          return true;
        }
        String extension = getExtension(f);
        if(extension != null && filters.get(getExtension(f)) != null)
        {
          return true;
        };
      }
      return false;
    }

    public String getExtension(File f)
    {
      if(f != null)
      {
        String filename = f.getName();
        int i = filename.lastIndexOf('.');
        if(i>0 && i<filename.length()-1)
        {
          return filename.substring(i+1).toLowerCase();
        };
      }
      return null;
    }

    public void addExtension(String extension)
    {
      if(filters == null)
      {
        filters = new Hashtable<String, FileFilter>(5);
      }
      filters.put(extension.toLowerCase(), this);
      fullDescription = null;
    }

    public String getDescription()
    {
      if(fullDescription == null)
      {
        if(description == null || isExtensionListInDescription())
        {
          if(description != null)
          {
            fullDescription = description;
          }
          fullDescription += " (";
          // build the description from the extension list
          Enumeration extensions = filters.keys();
          if (extensions != null)
          {
            fullDescription += "." + (String) extensions.nextElement();
            while (extensions.hasMoreElements())
            {
              fullDescription += ", " + (String) extensions.nextElement();
            }
          }
          fullDescription += ")";
        }
        else
        {
          fullDescription = description;
        }
      }
      return fullDescription;
    }

    public void setDescription(String description)
    {
      this.description = description;
      fullDescription = null;
    }

    public void setExtensionListInDescription(boolean b)
    {
      useExtensionsInDescription = b;
      fullDescription = null;
    }

    public boolean isExtensionListInDescription()
    {
      return useExtensionsInDescription;
    }
  }
}
