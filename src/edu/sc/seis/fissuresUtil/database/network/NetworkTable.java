/**
 * NetworkTable.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.database.network;

import java.sql.Connection;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCTable;

public abstract class NetworkTable extends JDBCTable{
    public NetworkTable(String name, Connection conn){ super(name, conn); }

    static{
        ConnMgr.addPropsLocation("edu/sc/seis/fissuresUtil/database/props/network/");
    }
}

