package edu.sc.seis.fissuresUtil.database;


import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JDBCQuantity  {
    public JDBCQuantity(Connection conn) throws SQLException{
        this(new JDBCUnit(conn));
    }
    
    public JDBCQuantity(JDBCUnit unit){ this.jdbcUnit = unit; }
    
    /**
     * Inserts the details of the object quantity into a PreparedStatement.
     * @return int - the resulting index.
     */
    public int insert(Quantity quantity, PreparedStatement stmt, int index)
        throws SQLException {
        stmt.setDouble(index++,quantity.value);
        stmt.setInt(index++,jdbcUnit.put((UnitImpl)quantity.the_units));
        return index;
    }
    
    /**
     * Returns the object QuantityImpl given the dbid of Unit  and the value
     */
    public QuantityImpl extract(int id,double value) throws SQLException,NotFound {
        return new QuantityImpl(value, jdbcUnit.get(id));
    }
    
    /**
     * returns the name of the UnitTable
     * @return String - the name of the UnitTable
     */
    public String getUnitTableName() {
        return jdbcUnit.getTableName();
    }
    
    private JDBCUnit jdbcUnit;
} // JDBCQuantity
