package edu.sc.seis.fissuresUtil.database.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;

public class JDBCCatalog extends EventTable {
    public JDBCCatalog(Connection conn) throws SQLException{
        this(conn, new JDBCContributor(conn));
    }

    public JDBCCatalog(Connection conn, JDBCContributor jdbcContributor)
        throws SQLException {
        super("catalog", conn);
        this.jdbcContributor = jdbcContributor;
        seq = new JDBCSequence(conn, "CatalogSeq");
        TableSetup.setup(this,
        "edu/sc/seis/fissuresUtil/database/props/event/default.props");
        put = conn.prepareStatement(" INSERT INTO catalog (catalog_id,"+
                                        " catalog_contributor_id, catalog_name )"+
                                        " VALUES( ?, ?, ?) ");
        get = conn.prepareStatement("SELECT catalog_name, catalog_contributor_id"+
                                        " FROM catalog WHERE catalog_id = ?");
        getDBId = conn.prepareStatement("SELECT catalog_id FROM catalog"+
                                            " WHERE catalog_name = ? ");
        getAll = conn.prepareStatement("SELECT DISTINCT catalog_name FROM catalog");
    }

    public int insert(String catalog, PreparedStatement stmt,  int index, int contributorid)
        throws SQLException {
        stmt.setInt(index++, contributorid);
        stmt.setString(index++, catalog);
        return index;
    }

    /**
     * This function inserts a row into the Catalog table
     * @param catalog - the catalog name
     * @return int - the dbid
     */
    public int put(String catalog, String contributor) throws SQLException{
        try {
            return getDBId(catalog);
        } catch(NotFound nfe) {
            int id = seq.next();
            put.setInt(1,id);
            int contributorid = jdbcContributor.put(contributor);
            insert(catalog, put,  2, contributorid);
            put.executeUpdate();
            return id;
        }
    }

    /***
     * This function returns the dbid given the catalog name
     * @ param catalog - the catalog name
     * @ return int - the dbid
     */
    public int getDBId(String  catalog) throws SQLException, NotFound {
        getDBId.setString(1, catalog);
        ResultSet rs = getDBId.executeQuery();
        if(rs.next()) {
            return rs.getInt("catalog_id");
        }
        throw new NotFound("the entry for the given origin object is not found");
    }


    /**
     * This method returns the catalog name given the dbid
     * @param id - dbid
     * @return String - the name of the catalog
     */
    public String get(int id) throws SQLException, NotFound {
        get.setInt(1,id);
        ResultSet rs = get.executeQuery();
        if(rs.next()) extract(rs);
        throw new NotFound(" there is no Catalog name is associated  to the id "+id);
    }

    /**
     * This method returns the catalog names
     * @return String[] - an array of all the catalogs
     */
    public String[] getAllCatalogs() throws SQLException {
        ArrayList aList = new ArrayList();
        ResultSet rs = getAll.executeQuery();

        while( rs.next() ) {
            aList.add(extract(rs));
        }

        String[] catalogs = new String[aList.size()];
        catalogs = (String[])aList.toArray(catalogs);
        return catalogs;

    }

    /**
     * This function returns the contributor given the dbid of the contributor
     * @param id - the dbid of the contributor
     * @return string - the contributor
     */
    public String getContributorOnContributorId(int id) throws SQLException, NotFound {
        return jdbcContributor.get(id);
    }



    public String getContributor(int id) throws SQLException, NotFound {
        get.setInt(1,id);
        ResultSet rs = get.executeQuery();
        if(rs.next()) {
            return jdbcContributor.get(rs.getInt("catalog_contributor_id"));
        }
        throw new NotFound(" there is no Catalog name is associated  to the id "+id);
    }

    public String extract(ResultSet rs) throws SQLException {
        return rs.getString("catalog_name");
    }
    
    public JDBCContributor getJDBCContributor() {
        return jdbcContributor;
    }
    
    protected JDBCContributor jdbcContributor;

    protected PreparedStatement getDBId;

    protected PreparedStatement get;

    protected PreparedStatement put;

    protected PreparedStatement getAll;

    private JDBCSequence seq;
} // JDBCCatalog
