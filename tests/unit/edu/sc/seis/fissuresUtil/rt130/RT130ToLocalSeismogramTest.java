package edu.sc.seis.fissuresUtil.rt130ToLocalSeismogramImplTest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.log4j.BasicConfigurator;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.ConnectionCreator;
import edu.sc.seis.fissuresUtil.database.JDBCTearDown;
import edu.sc.seis.fissuresUtil.database.seismogram.JDBCSeismogramFiles;
import edu.sc.seis.fissuresUtil.rt130.PacketType;
import edu.sc.seis.fissuresUtil.rt130.RT130FormatException;
import edu.sc.seis.fissuresUtil.rt130.RT130FileReader;
import edu.sc.seis.fissuresUtil.rt130.RT130ToLocalSeismogram;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;

/**
 * @author fenner Created on Jun 14, 2005
 */
public class RT130ToLocalSeismogramImplTest extends JDBCTearDown {

    public RT130ToLocalSeismogramImplTest() {
        BasicConfigurator.configure();
    }

    public void setUp() throws SQLException {
        // filePath =
        // "/private/Network/Servers/roo.seis.sc.edu/Volumes/home/Users/fenner/tohpc
        // Folder/2005130/9307/1/221325740_0036EE80";
        // filePath =
        // "/private/Network/Servers/roo.seis.sc.edu/Volumes/home/Users/fenner/tohpc
        // Folder/2005130/9307/2/121254115_0036EE80";
        // filePath =
        // "/private/Network/Servers/roo.seis.sc.edu/Volumes/home/Users/fenner/tohpc
        // Folder/2005129/9307/1/231354265_57D5D7C0";
        filePath = "/private/Network/Servers/roo.seis.sc.edu/Volumes/home/Users/fenner/20050617_huddle/2005168/91EB/1/200332765_0036EE80";
    }

    public void testGeneral() throws SQLException, RT130FormatException,
            IOException {
        ConnectionCreator connCreator = new ConnectionCreator("jdbc:hsqldb:.",
                                                              "HSQL",
                                                              "SA",
                                                              "");
        Connection conn = connCreator.createConnection();
        RT130FileReader toPackets = new RT130FileReader(filePath, true);
        PacketType[] packetArray = toPackets.processRT130Data();
        RT130ToLocalSeismogram toSeismogram = new RT130ToLocalSeismogram(conn,
                                                                         null,
                                                                         null);
        LocalSeismogramImpl[] seismogramArray = toSeismogram.ConvertRT130ToLocalSeismogram(packetArray);
        JDBCSeismogramFiles jdbcSeisFile = new JDBCSeismogramFiles(conn);
        for(int i = 0; i < seismogramArray.length; i++) {
            jdbcSeisFile.saveSeismogramToDatabase(seismogramArray[i].channel_id,
                                                  seismogramArray[i],
                                                  filePath,
                                                  SeismogramFileTypes.RT_130);
            System.err.println(i);
        }
    }

    private String filePath;
}
