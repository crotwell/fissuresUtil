package edu.sc.seis.fissuresUtil.database;

import java.sql.*;
import org.hsqldb.*;
import org.apache.log4j.*;
/**
 * This class is used to create a UniqueIdentifier when the method
 * getUniqueIdentifier is called. It makes use of database for returning
 * an uniqueIdentifier.
 * 
 * UniqueNumberGenerator.java
 *
 *
 * Created: Thu Feb 20 11:14:14 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class UniqueNumberGenerator extends AbstractDb{
    private UniqueNumberGenerator (){
	create();
	insert(-1);
    }

    public void create() {
	try {
	    connection = getConnection();
	    Statement stmt = connection.createStatement();
	    stmt.executeUpdate(" CREATE TABLE uniqueIdentifier "+
			       " ( number INTEGER)");
	} catch(SQLException sqle) {
	    //  logger.debug("error while creating the table uniqueIdentifier");
	}
	
	try {
	    insertStmt = connection.prepareStatement("INSERT INTO uniqueIdentifier "+
						     "VALUES(?)");
	    getStmt = connection.prepareStatement(" SELECT number FROM uniqueIdentifier");
	    
	    updateStmt = connection.prepareStatement(" UPDATE uniqueIdentifier SET number = ? ");
						    
	} catch(SQLException sqle) {
	    //logger.debug("error while creating prepared Statements");
	}
	
    }

    private void insert(int num) {

	try {
	    insertStmt.setInt(1, num);
	    insertStmt.executeUpdate();
	} catch(SQLException sqle) {
	    // logger.debug("ERROR while inserting ", sqle);
	}
    }

    
    public  static int getUniqueIdentifier() {
	try {
	    int num = getId();
	    ung.updateStmt.setInt(1, (num + 1));
	    ung.updateStmt.executeUpdate();
	    return (num + 1);
	} catch(SQLException sqle) {
	    //logger.debug("ERROR while updating ", sqle);
	}
	return -1;
    }

    private static int getId() {
	try {
	    ResultSet rs = ung.getStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt(1);
	    }
	} catch(SQLException sqle) {
	    // ung.logger.debug("ERROR while retrieveing ", sqle);
	}
	return -1;
    }

    public static void main(String[] args) {
	//	for(int counter = 0; counter < 10; counter++) 
	// System.out.println("The number is "+UniqueNumberGenerator.getUniqueIdentifier());
    }
    
    private static UniqueNumberGenerator ung = new UniqueNumberGenerator();

    private PreparedStatement insertStmt;

    private  PreparedStatement getStmt;

    private  PreparedStatement updateStmt;
   
    static Category logger = Category.getInstance(UniqueNumberGenerator.class.getName());
    
}// UniqueNumberGenerator
