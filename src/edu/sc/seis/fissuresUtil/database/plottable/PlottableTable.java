package edu.sc.seis.fissuresUtil.database.plottable;

import java.sql.Connection;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCTable;


/**
 * @author groves
 * Created on Oct 4, 2004
 */
public class PlottableTable extends JDBCTable {

    public PlottableTable(String tableName, Connection conn) {
        super(tableName, conn);
    }
    static{
        ConnMgr.addPropsLocation("edu/sc/seis/fissuresUtil/database/props/plottable/");
    }
}
