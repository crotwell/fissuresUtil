package edu.sc.seis.fissuresUtil.rt130ToLocalSeismogramImplTest;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.BasicConfigurator;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCTearDown;
import edu.sc.seis.fissuresUtil.database.seismogram.JDBCSeismogramFiles;
import edu.sc.seis.fissuresUtil.rt130.RT130FormatException;
import edu.sc.seis.fissuresUtil.rt130.RT130ToLocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.simple.Initializer;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;


/**
 * @author fenner
 * Created on Jun 14, 2005
 */
public class RT130ToLocalSeismogramImplTest  extends JDBCTearDown {

    public RT130ToLocalSeismogramImplTest() {
        BasicConfigurator.configure();
    }
    
    public void setUp() throws SQLException{
        //filePath = "/private/Network/Servers/roo.seis.sc.edu/Volumes/home/Users/fenner/tohpc Folder/2005130/9307/1/221325740_0036EE80";
        //filePath = "/private/Network/Servers/roo.seis.sc.edu/Volumes/home/Users/fenner/tohpc Folder/2005130/9307/2/121254115_0036EE80";
        //filePath = "/private/Network/Servers/roo.seis.sc.edu/Volumes/home/Users/fenner/tohpc Folder/2005129/9307/1/231354265_57D5D7C0";
        filePath = "/private/Network/Servers/roo.seis.sc.edu/Volumes/home/Users/fenner/20050617_huddle/2005168/91EB/1/200332765_0036EE80";
    }

    public void testGeneral() throws SQLException{
        File seismogramFile = new File(filePath);
        FileInputStream fis = null;
        try {
            Connection conn = null;
            try {
                conn = ConnMgr.createConnection();
            } catch(SQLException e) {
                System.err.println("Error creating connection.");
                e.printStackTrace();
            }
            
            fis = new FileInputStream(seismogramFile);
	        BufferedInputStream bis = new BufferedInputStream(fis);
	        DataInputStream dis = new DataInputStream(bis);
	        RT130ToLocalSeismogramImpl toSeismogram = new RT130ToLocalSeismogramImpl(dis, true);
		    LocalSeismogramImpl[] seismogramArray = toSeismogram.readEntireDataFile();
            
            JDBCSeismogramFiles jdbcSeisFile = new JDBCSeismogramFiles(conn);
            for(int i = 0; i < seismogramArray.length; i++){
                jdbcSeisFile.saveSeismogramToDatabase(seismogramArray[i].channel_id,
                                                      seismogramArray[i],
                                                      filePath, 
                                                      SeismogramFileTypes.RT_130);
                System.err.println(i);
            }
	    } catch(FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();    
        } catch(RT130FormatException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch(IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    
    private String filePath;
}
