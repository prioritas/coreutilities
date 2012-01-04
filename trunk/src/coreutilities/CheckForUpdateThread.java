package coreutilities;

import coreutilities.ctx.CoreContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;
import java.net.URLConnection;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

import java.util.List;

import oracle.xml.parser.v2.DOMParser;

import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XMLParser;

import org.w3c.dom.NodeList;

public class CheckForUpdateThread extends Thread
{
  private final String softid;
  private DOMParser parser = null;
  private String structureFileName = null;
  private boolean proceed;
  private boolean verbose = false;
  private boolean force = false;

  public CheckForUpdateThread(String softid, 
                              DOMParser parser, 
                              String structureFileName, 
                              boolean proceed)
  {
    this(softid, parser, structureFileName, proceed, false, false);
  }
  
  public CheckForUpdateThread(String softid, 
                              DOMParser parser, 
                              String structureFileName, 
                              boolean proceed,
                              boolean verbose,
                              boolean force)
  {
    this.softid = softid;
    this.parser = parser;
    this.structureFileName = structureFileName;
    this.proceed = proceed;
    this.verbose = verbose;
    this.force = force;
  }

  public void run()
  {
    String downloadMess = "";
    List<String> updatedFiles = new ArrayList<String>();
    if (verbose) System.out.println("Checking update from " + System.getProperty("user.dir"));
    List<String[]> resource = null;
    boolean restartRequired = false;
    try
    {
      // 1 - Parse structure file
      XMLDocument doc = null;
      synchronized (parser)
      {
        parser.setValidationMode(XMLParser.NONVALIDATING);
        parser.parse(new File(structureFileName).toURI().toURL());
        doc = parser.getDocument();
      }
      NodeList nl = doc.selectNodes("//soft[@id='" + softid + "']/data");
      int nbResource = nl.getLength();
      if (verbose) System.out.println("Checking for update for " + nbResource + " file(s).");
      resource = new ArrayList<String[]>(nbResource);
      for (int i = 0; i < nbResource; i++)
      {
        XMLElement data = (XMLElement) nl.item(i);
        String url = data.getAttribute("url");
        String file = data.getAttribute("file");
        String restart = data.getAttribute("require-restart");
        if (verbose) System.out.println("Checking " + file);
        resource.add(new String[] { url, file, restart });
      }
    }
    catch (Exception ex)
    {
//    Context.getInstance().fireExceptionLogging(ex);
      ex.printStackTrace();
    }
    // 3 - Create XML Document 
    XMLDocument updateDoc = new XMLDocument();
    XMLElement root = (XMLElement) updateDoc.createElement("update-root");
    updateDoc.appendChild(root);

    // 4 - Compare local and remote
    int nbUpdate = 0;
    boolean online = true;
    if (resource != null)
    {
      for (String[] pair : resource)
      {
        try
        {
          URL url = new URL(pair[0]);
          File localFile = new File(pair[1]);
          boolean restart = pair[2].equals("y");

          Date fileDate = null;
          if (localFile.exists())
          {
            try
            {
              fileDate = new Date(localFile.lastModified());
            }
            catch (Exception ex)
            {
              ex.printStackTrace();
            }
          }
          Date urlDate = null;

          URLConnection conn = url.openConnection();
          conn.connect(); // Triggers exception if necessary
          if (conn != null)
          {
            if (verbose) System.out.println("-- Opened connection for " + url.toString());
            // List all the response headers from the server.
            // Note: The first call to getHeaderFieldKey() will implicit send
            // the HTTP request to the server.
            for (int j = 0; ; j++)
            {
              try
              {
                String headerName = conn.getHeaderFieldKey(j);
                String headerValue = conn.getHeaderField(j);
  
                if (headerName == null && headerValue == null)
                {
                  // No more headers
                  break;
                }
                if (headerName == null)
                {
                  // The header value contains the server's HTTP version
                }
                else if (headerName.equals("Last-Modified"))
                {
    //            System.out.println(headerName + " = " + headerValue);
                  try
                  {
                    SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");
                    urlDate = formatter.parse(headerValue);
                  }
                  catch (ParseException e)
                  {
  //                Context.getInstance().fireExceptionLogging(e);
                    e.printStackTrace();
                  }
                }
              }
              catch (Exception fnfe)
              {
                System.out.println("Trying to update.\n" + fnfe.toString());
              }
            }
            
            if (force || ( /*nbUpdate < 4 || */(fileDate == null && urlDate != null) || (fileDate != null && urlDate != null && fileDate.getTime() < urlDate.getTime())))
            {
              nbUpdate++;
              System.out.println("Update available for " + pair[1]);
  
              if (proceed)
              {
                if (restart)
                {
                  restartRequired = true;
                  // Write XML Doc
                  XMLElement update = (XMLElement) updateDoc.createElement("update");
                  root.appendChild(update);
                  update.setAttribute("id", Integer.toString(nbUpdate));
                  update.setAttribute("destination", pair[1]);
                  String tempFile = "update" + File.separator + pair[1].substring(pair[1].lastIndexOf("/") + 1);
                  update.setAttribute("origin", tempFile);
  
                  File updateDir = new File("update");
                  if (!updateDir.exists())
                    updateDir.mkdirs();
  
                  // Write the file to copy later
                  if (verbose) System.out.println("Writing files to update [" + tempFile + "]");
                  InputStream urlIs = conn.getInputStream();
                  OutputStream os = new FileOutputStream(new File(tempFile));
                  Utilities.copy(urlIs, os);
                  os.close();
                }
                // 1 - Rename original file
                try
                {
                  if (localFile.exists())
                  {
                    File backup = Utilities.findFileName(pair[1]);
                    if (verbose) System.out.println("Renaming " + pair[1] + " (" + localFile.getName() + ") to " + backup.getName());
                    Utilities.copy(new FileInputStream(localFile), new FileOutputStream(backup));
                  }
                }
                catch (Exception ex)
                {
                  System.err.println("Renaming old files");
                  ex.printStackTrace();
                }
                // 2 - Download
                /*
                 * This is now done on exit if restart is required
                 */
                if (!restart)
                {
                  try
                  {
                    InputStream urlIs = conn.getInputStream();
                    if (!localFile.exists())
                    {
                      File dir = new File(localFile.getAbsolutePath().substring(0, localFile.getAbsolutePath().lastIndexOf(File.separator)));
                      if (!dir.exists())
                        dir.mkdirs();
                    }
                    OutputStream os = new FileOutputStream(localFile);
                    Utilities.copy(urlIs, os);
                    os.close();
                  }
                  catch (Exception ioe)
                  {
                    ioe.printStackTrace();
                  }
                }
              }
              downloadMess += (pair[1] + "\n");
              updatedFiles.add(pair[1]);
            }
            else
            {
              if (verbose) System.out.println(pair[1] + " is up to date");
            }
          }
          else
          {
            if (verbose) System.out.println("Not on line, or network not accessible");
            online = false;
         // throw new RuntimeException("Cannot Connect to the Network");
            break;
          }
        }
        catch (Exception ex)
        {
          if (verbose) System.out.println("Not on line, or network not accessible");
          online = false;
      //  throw new RuntimeException("Cannot Connect to the Network", ex);
          break;
        }
      }
      CoreContext.getInstance().fireNetworkOk(online);
      if (online)
        System.out.println("software-up-to-date");
      if (online && nbUpdate > 0)
      {
//      System.out.println("Update message sent");
//      sendPing("Software update requested for:\n" + downloadMess);
        System.out.println("soft-update-available");

        if (proceed)
          System.out.println("following-updated");
        else
          System.out.println("update-available");
//      mess += downloadMess; // File List
        System.out.println(downloadMess);
        if (proceed && restartRequired) // TODO deprecate restartRequired
        {
          System.out.println("restart-required");
          try
          {
            File updateFile = new File("update" + File.separator + "update.xml");
            updateDoc.print(new FileOutputStream(updateFile));
            if (verbose) System.out.println("UpdateFile:" + updateFile.toURI().toURL().toExternalForm());
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        }
        CoreContext.getInstance().fireUpdateCompleted(updatedFiles);
      }
    }
  }
}

