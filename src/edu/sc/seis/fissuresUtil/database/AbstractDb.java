package edu.sc.seis.fissuresUtil.database;

import java.sql.*;

import org.hsqldb.*;
import org.apache.log4j.*;

/**
 * AbstractDb.java
 *
 *
 * Created: Fri Feb  7 10:45:30 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public abstract class AbstractDb {
    public AbstractDb (){
	
    }

    public Connection getConnection() {
	try {
	    if(connection == null) {
		String driverName = new String("org.hsqldb.jdbcDriver");
		Class.forName(driverName).newInstance();
		connection = DriverManager.getConnection("jdbc:hsqldb:testhsqldb", "sa", "");
	    } 
	    return connection;
	} catch(Exception sqle) {
	    sqle.printStackTrace();
	    return null;
	}
	
    }

    public abstract void create();
    
    protected Connection connection;
    
}// AbstractDb
