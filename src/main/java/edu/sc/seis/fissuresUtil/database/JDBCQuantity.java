package edu.sc.seis.fissuresUtil.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;

public class JDBCQuantity  extends JDBCTable {
    public JDBCQuantity()throws SQLException{
        this(ConnMgr.createConnection());
    }

    public JDBCQuantity(Connection conn) throws SQLException{
        this(new JDBCUnit(conn), conn);
    }

    public JDBCQuantity(JDBCUnit unit, Connection conn) throws SQLException{
    	super("quantity", conn);
        this.jdbcUnit = unit;
        seq = new JDBCSequence(conn, "QuantitySeq");

        TableSetup.setup(getTableName(), conn, this, "edu/sc/seis/fissuresUtil/database/props/default.props");
        put = conn.prepareStatement(" INSERT INTO quantity ( quantity_id, "+
                                        "quantity_unit_id, quantity_value) "+
                                        "VALUES (?,?,?)");
        get = conn.prepareStatement(" SELECT quantity_unit_id, quantity_value FROM quantity"+
                                        " WHERE quantity_id= ? ");
        getDBId = conn.prepareStatement("SELECT quantity_id FROM quantity WHERE " +
                                            " quantity_unit_id = ? AND quantity_value = ?");
    }

    public int put(Quantity quantity)throws SQLException {
        try{
            return getDBId(quantity);
        } catch(NotFound ex) {
            int id = seq.next();
            put.setInt(1,id);
            insert(quantity, put, 2);
            put.executeUpdate();
            return id;
        }
    }

    public QuantityImpl get(int dbId) throws SQLException, NotFound{
        get.setInt(1, dbId);
        ResultSet rs = get.executeQuery();
        if(rs.next()){
            return new QuantityImpl(rs.getDouble("quantity_value"),
                                    jdbcUnit.get(rs.getInt("quantity_unit_id")));
        }
        throw new NotFound("No quantity at id " + dbId);
    }

    public int getDBId(Quantity q) throws NotFound, SQLException{
        insert(q, getDBId, 1);
        ResultSet rs = getDBId.executeQuery();
        if(rs.next()){ return rs.getInt("quantity_id"); }
        throw new NotFound(q + " is not in the quantity table");
    }

    /**
     * Inserts the details of the object quantity into a PreparedStatement.
     * @return int - the resulting index.
     */
    public int insert(Quantity quantity, PreparedStatement stmt, int index)throws SQLException {
        stmt.setInt(index++,jdbcUnit.put((UnitImpl)quantity.the_units));
        stmt.setDouble(index++,quantity.value);
        return index;
    }

    /**
     * Returns the object QuantityImpl given the dbid of Unit  and the value
     */
    public QuantityImpl extract(int id,double value) throws SQLException,NotFound {
        return new QuantityImpl(value, jdbcUnit.get(id));
    }

    private JDBCSequence seq;

    private PreparedStatement get, put, getDBId;

    private JDBCUnit jdbcUnit;
} // JDBCQuantity
