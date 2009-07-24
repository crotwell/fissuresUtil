package edu.sc.seis.fissuresUtil.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import org.apache.log4j.Category;
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
    private UniqueNumberGenerator (String directoryName, String databaseName){
        super(directoryName, databaseName);
        create();
        insert(-1);
    }

    public static UniqueNumberGenerator getUNG(String directoryName, String databaseName) {
        if (ung == null) {
            ung = new UniqueNumberGenerator( directoryName, databaseName);
        }
        return ung;
    }

    public void create() {
        try {
            connection = getConnection();
            if(connection==null){
                Object[] options = { "Exit GEE" };
                JOptionPane.showOptionDialog(null,
                                             "It appears that another copy of GEE is running right now.\nPlease use it instead of starting another copy.\nIf you feel you are getting this message in error, please remove the cache directory\n"+directoryName,
                                             "Multiple instances of GEE running",
                                             JOptionPane.OK_OPTION,
                                             JOptionPane.ERROR_MESSAGE,
                                             null, options, "Exit GEE");
                System.exit(0);
            }
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


    public int getUniqueIdentifier() {
        try {
            int num = getId();
            ung.updateStmt.setInt(1, (num + 1));
            ung.updateStmt.executeUpdate();
            return (num + 1);
        } catch(SQLException sqle) {
            logger.debug("ERROR while updating ", sqle);
        }
        return -1;
    }

    private int getId() {
        try {
            ResultSet rs = ung.getStmt.executeQuery();
            if(rs.next()) {
                return rs.getInt(1);
            }
        } catch(SQLException sqle) {
            logger.debug("ERROR while retrieveing ", sqle);
        }
        return -1;
    }

    private static UniqueNumberGenerator ung = null;

    private PreparedStatement insertStmt;

    private  PreparedStatement getStmt;

    private  PreparedStatement updateStmt;

    static Category logger = Category.getInstance(UniqueNumberGenerator.class.getName());

}// UniqueNumberGenerator
