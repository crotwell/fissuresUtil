/**
 * EventTable.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.database.event;

import java.sql.Connection;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCTable;

public class EventTable extends JDBCTable{
    public EventTable(String name, Connection conn){ super(name, conn); }

    static{
        ConnMgr.addPropsLocation("edu/sc/seis/fissuresUtil/database/props/event/");
    }
}

