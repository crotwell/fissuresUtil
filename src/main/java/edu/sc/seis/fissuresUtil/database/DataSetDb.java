package edu.sc.seis.fissuresUtil.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataSetDb.java
 *
 *
 * Created: Wed Jan 29 11:00:55 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class DataSetDb {
    public DataSetDb (){
	create();
    }
    
    public void create() {
	connection = getConnection();
	try {
	    Statement stmt = connection.createStatement();
	    stmt.executeUpdate(" create table "+getDatabaseName()+" ( "+
			       " seismogramid VARCHAR PRIMARY KEY, "+
			       " datasetid VARCHAR, "+
 			       " storagetype VARCHAR_IGNORECASE, "+
			       " path VARCHAR_IGNORECASE) ");
	    stmt.executeUpdate(" create table geedb ( "+
			       " datasetid VARCHAR PRIMARY KEY, "+
			       " datasetname VARCHAR_IGNORECASE) ");
			       
	} catch(SQLException sqle) {
	    //  sqle.printStackTrace();
	}
	try { 
	    insertStmt = connection.prepareStatement(" INSERT INTO "+getDatabaseName()+
						 " VALUES(?, ?, ?) ");
	    getPathStmt = connection.prepareStatement(" SELECT path from "+getDatabaseName()+
						      " WHERE seismogramID = ? ");
	    
	    getStorageTypeStmt = connection.prepareStatement(" SELECT storagetype from "+getDatabaseName()+
							     " WHERE seismogramID = ? ");

	    getNamesStmt = connection.prepareStatement(" SELECT seismogramID from "+getDatabaseName());
						      
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	
    }

    public Connection getConnection() {
	try {
	    if(connection == null) {
		String driverName = new String("org.hsqldb.jdbcDriver");
		Class.forName(driverName).newInstance();
		connection = DriverManager.getConnection("jdbc:hsqldb:"+getDatabaseName(), "sa", "");
	    } 
	    return connection;
	} catch(Exception sqle) {
	    sqle.printStackTrace();
	    return null;
	}
	
    }

    public void insert(String seismogramID,
		       String storageType,
		       String path) {
	try {
	    insertStmt.setString(1, seismogramID);
	    insertStmt.setString(2, storageType);
	    insertStmt.setString(3, path);
       	    insertStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    public String getPath(String seismogramID) {
	try {
	    getPathStmt.setString(1, seismogramID);
	    ResultSet rs = getPathStmt.executeQuery();
	    if(rs.next()) {
		return rs.getString("path");
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return null;
    }

    public String getStorageType(String seismogramID) {
	try {
	    getStorageTypeStmt.setString(1, seismogramID);
	    ResultSet rs = getStorageTypeStmt.executeQuery();
	    if(rs.next()) {
		return rs.getString("storagetype");
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	return null;
    }

    public String[] getSeismogramNames() {
	ArrayList arrayList = new ArrayList();
	try {
	    ResultSet rs = getNamesStmt.executeQuery();
	    while(rs.next()) {
		arrayList.add(rs.getString("seismogramID"));
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
	String[] rtnValues = new String[arrayList.size()];
	rtnValues = (String[]) arrayList.toArray(rtnValues);
	return rtnValues;
    }
    

    private String getDatabaseName() {
	return "datasetDB";
    }


    private Connection connection;
    
    private PreparedStatement insertStmt; 
    
    private PreparedStatement getPathStmt;

    private PreparedStatement getStorageTypeStmt;

    private PreparedStatement getNamesStmt;

    static Logger logger = LoggerFactory.getLogger(DataSetDb.class.getName());

}// DataSetDb
