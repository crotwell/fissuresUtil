/**
 * EventTable.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.database.event;

import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import java.sql.Connection;

public class EventTable extends JDBCTable{
    public EventTable(String name, Connection conn){ super(name, conn); }

    static{
        System.out.println("LOADING EVENT PROPS");
        ConnMgr.addPropsLocation("edu/sc/seis/fissuresUtil/database/props/event/");
    }
}

