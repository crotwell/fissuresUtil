package edu.sc.seis.fissuresUtil.sac;

import edu.sc.seis.fissuresUtil.xml.*;

import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelIdUtil;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import org.w3c.dom.Element;

/**
 * SacDirToDataSet.java
 *
 *
 * Created: Tue Feb 26 11:43:08 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version $Id: SacDirToDataSet.java 9761 2004-07-23 14:33:11Z crotwell $
 */

public class SacDirToDataSet implements StdDataSetParamNames {
    public SacDirToDataSet (URL base,
                            File directory,
                            String dsName,
                            List excludes,
                            Map paramRefs){
        this.base = base;
        this.directory = directory;
        this.dsName = dsName;
        this.excludes = excludes;
        this.paramRefs = paramRefs;
    }

    void process() throws ParserConfigurationException,
        MalformedURLException,
        IOException {

        dirURL = base;
        System.out.println(" dirURL is "+dirURL.toString());
        System.out.println(" directory name is "+directory.getName());

        dirURL = new URL(dirURL.toString()+"/"+directory.getName()+"/");
        System.out.println("updated dirURL is "+dirURL.toString());

        AuditInfo[] audit = new AuditInfo[1];
        audit[0] = new AuditInfo(userName, "Dataset created from SAC files in "+directory.getPath());
        dataset
            = new MemoryDataSet("genid"+Math.round(Math.random()*Integer.MAX_VALUE),
                                dsName,
                                userName,
                                audit);
        Element dsElement = dataSetToXML.createDocument(dataset, directory, SeismogramFileTypes.SAC);

        Iterator it = paramRefs.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            //loadParameterRef(key, (String)paramRefs.get(key));

        } // end of while (it.hasNext())

        File[] files = directory.listFiles();
        for (int i=0; i<files.length; i++) {
            try {
                if (files[i].isDirectory()) {
                    // skip
                    continue;
                }
                String filename = files[i].getName();
                System.out.println("Process "+filename);
                // maybe an image?
                if (filename.endsWith(".gif") ||
                    filename.endsWith(".GIF") ||
                    filename.endsWith(".png") ||
                    filename.endsWith(".PNG") ||
                    filename.endsWith(".jpeg") ||
                    filename.endsWith(".JPEG") ||
                    filename.endsWith(".jpg") ||
                    filename.endsWith(".JPG") ) {
                    String name = filename.substring(0, filename.lastIndexOf('.'));
                    loadParameterRef(dsElement, filename, filename);
                } else {
                    // try as a sac file
                    loadSacFile(dsElement, files[i]);
                } // end of else

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Caught exception on "
                                       +files[i].getName()+", continuing...");
            } // end of try-catch
        } // end of for (int i=0; i<sacFiles.length; i++)

        dataSetToXML.save(dataset, new File(System.getProperty("user.dir")), SeismogramFileTypes.SAC);
    }

    void loadParameterRef(Element dsElement, String paramName, String paramFile) {
        AuditInfo[] audit = new AuditInfo[1];
        audit[0] = new AuditInfo(userName,
                                 "Added parameter "+paramName+" for "+paramFile);

        Element paramElement = dsElement.getOwnerDocument().createElement("parameter");
        dsElement.appendChild(paramElement);
        dataSetToXML.insertParameter(paramElement,
                                     paramName,
                                     "http://www.w3.org/TR/xlink/",
                                     "xml:xlink",
                                     paramName);

    }

    void loadSacFile(Element dsElement, File sacFile) throws IOException, FissuresException {
        if (excludes.contains(sacFile.getName())) {
            return;
        } // end of if (excludes.contains(sacFile.getName()))
        if (paramRefs.containsValue(sacFile.getName())) {
            return;
        } // end of if (excludes.contains(sacFile.getName()))

        SacTimeSeries sac = new SacTimeSeries();
        sac.read(sacFile.getCanonicalPath());
        AuditInfo[] audit = new AuditInfo[1];
        audit[0] = new AuditInfo(userName+" via SacDirToDataSet",
                                 "seismogram loaded from "+sacFile.getCanonicalPath());
        URL seisURL = new URL(dirURL, sacFile.getName());
        //      System.out.println(" the seisURL is "+seisURL.toString());
        //      DataInputStream dis = new DataInputStream(new BufferedInputStream(seisURL.openStream()));
        //      SacTimeSeries sac = new SacTimeSeries();
        //sac.read(dis);
        edu.iris.Fissures.seismogramDC.LocalSeismogramImpl seis = SacToFissures.getSeismogram(sac);

        edu.sc.seis.fissuresUtil.cache.CacheEvent event =
            SacToFissures.getEvent(sac);

        if (event != null && dataset.getParameter(EVENT) == null) {
            String eventName = event.get_attributes().name;

            String eName = eventName.replace(' ', '_');
            Element paramElement = dsElement.getOwnerDocument().createElement("parameter");
            dsElement.appendChild(paramElement);
            dataSetToXML.insert(paramElement,
                                StdDataSetParamNames.EVENT,
                                event);
            AuditInfo[] eventAudit = new AuditInfo[1];
            eventAudit[0] = new AuditInfo(System.getProperty("user.name"),
                                          "event loaded from sac file.");
            dataset.addParameter(StdDataSetParamNames.EVENT,
                                 event,
                                 eventAudit);
        } // end of if (event != null)

        Channel channel =
            SacToFissures.getChannel(sac);
        String channelParamName =
            StdDataSetParamNames.CHANNEL+ChannelIdUtil.toString(seis.channel_id);


        if (channel != null &&
            dataset.getParameter(channelParamName) == null) {

            // add channel
            AuditInfo[] chanAudit = new AuditInfo[1];
            chanAudit[0] = new AuditInfo(System.getProperty("user.name"),
                                         "channel loaded from sac file.");

            dataset.addParameter(channelParamName,
                                 channel,
                                 chanAudit);
            Element paramElement = dsElement.getOwnerDocument().createElement("parameter");
            dsElement.appendChild(paramElement);
            dataSetToXML.insert(paramElement,
                                channelParamName,
                                channel);


        }


        String seisName = sacFile.getName();
        if (seisName.endsWith(".SAC")) {
            seisName = seisName.substring(0,seisName.length()-4);
        } // end of if (seisName.endsWith(".SAC"))
        MemoryDataSetSeismogram memDSS = new MemoryDataSetSeismogram(seis, dataset, seisName);

        AuditInfo[] seisAudit = new AuditInfo[1];
        seisAudit[0] = new AuditInfo(System.getProperty("user.name"),
                                     "seismogram loaded from sac file.");

        dataset.addDataSetSeismogram(memDSS, seisAudit);
    }

    String userName = System.getProperty("user.name");
    URL base;
    URL dirURL;
    File directory;
    String dsName;
    DataSet dataset;
    List excludes;
    Map paramRefs;

    public static void main (String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: java edu.sc.seis.fissuresUtil.sac.SacDirToDataSet -base url -dir directoryPath -name datasetname [-exclude file] [-paramRef name file]");
            return;
        } // end of if (args.length != 2)
        BasicConfigurator.configure(new NullAppender());
        String dirName = null;
        URL base = null;
        String baseStr = "";
        String dsName = "default dataset name";
        LinkedList excludes = new LinkedList();
        HashMap params = new HashMap();
        int i=0;
        while (i<args.length) {
            System.out.println(i+" "+args[i]);
            if (args[i].equals("-dir")) {
                dirName = args[i+1];
                i+=2;
            } else  if (args[i].equals("-name")) {
                String tmp = args[i+1];
                if (tmp.endsWith("/") ||
                    tmp.endsWith("\\") ||
                    tmp.endsWith(":") ||
                    tmp.endsWith(".") ) {
                    tmp = tmp.substring(0, tmp.length()-1);
                } // end of if (dsName.endsWith('/'))
                tmp = tmp.replace(' ','_');
                if (tmp.length() > 0) {
                    dsName = tmp;
                } // end of if (tmp.length() > 0)
                i+=2;
            } else  if (args[i].equals("-base")) {
                baseStr = args[i+1];
                System.out.println("The baseStr is "+baseStr);
                i+=2;
            } else  if (args[i].equals("-exclude")) {
                excludes.add(args[i+1]);
                i+=2;
            } else  if (args[i].equals("-paramRef")) {
                params.put(args[i+1], args[i+2]);
                i+=3;
            } else {
                System.out.println("Don't understand "+args[i++]);
            }
        } // end of for (int i=0; i<args.length; i++)


        try {
            base = new URL(baseStr);
            System.out.println("base is "+base.toString());
            File f = new File(dirName);
            if (dirName != null) {
                if ( ! f.exists()) { f.mkdirs(); }
                if ( f.isDirectory()) {
                    SacDirToDataSet sdir = new SacDirToDataSet(base, f, dsName, excludes, params);
                    sdir.process();
                } else {
                    System.err.println("Not a directory: "+args[1]);
                } // end of else
            }
        } catch (Exception e) {
            e.printStackTrace();
        } // end of try-catch


    } // end of main ()

    static DataSetToXML dataSetToXML = new DataSetToXML();

}// SacDirToDataSet



