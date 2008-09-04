package edu.sc.seis.fissuresUtil.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import edu.iris.Fissures.Unit;
import edu.iris.Fissures.UnitBase;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;
/**
 * JDBCUnit.java
 *
 * All methods are unsyncronized, the calling application should make sure
 * that a single instance of this class is not accessed by more than one
 * thread at a time. Because of the use of prepared statements and a single
 * connection per instance, this class IS NOT THREAD-SAFE!
 *
 * Created: Tue Mar 21 12:39:50 2000
 *
 * @author Ramesh Danala, Philip Crotwell
 * @version
 */


public class JDBCUnit extends JDBCTable {
    public  JDBCUnit(Connection conn) throws SQLException {
        super("unit", conn);
        seq = new JDBCSequence(conn, "unit_seq");
        TableSetup.setup(getTableName(), conn, this, "edu/sc/seis/fissuresUtil/database/props/default.props");
        putStmt = conn.prepareStatement("INSERT INTO unit" +
                                            " ( unit_id, unit_name, unit_base, unit_subunit_id, "+
                                            " unit_exponent, unit_multFactor, unit_power )"+
                                            " VALUES ( ?, ?, ?, ?, ?, ?, ? )");
        getDBIdStmt = conn.prepareStatement("SELECT unit_id FROM unit WHERE "+
                                                " unit_name = ? ");

        getStmt = conn.prepareStatement("SELECT * FROM unit WHERE unit_id = ?");
        // cache common units
        createCache();
    }

    public Unit get(int dbid) throws SQLException, NotFound {
        Unit cachedUnit = (Unit)cache.get(new Integer(dbid));
        if (cachedUnit != null) { 
            return cachedUnit;
        }
        getStmt.setInt(1, dbid);
        ResultSet results =  getStmt.executeQuery();
        if (results.next()) {
            String name  =  results.getString("unit_name");
            int exponent =  results.getInt("unit_exponent");
            double multFactor = results.getDouble("unit_multFactor");
            int power =  results.getInt("unit_power");
            String base = results.getString("unit_base");
            if (base == null || base.equals("COMPOSITE")) {
                String strSequence =  results.getString("unit_subunit_id");
                ArrayList intList = new ArrayList();
                int start = strSequence.indexOf('{')+1;
                int end = strSequence.indexOf(',',start);
                if(end == -1) {
                    end = strSequence.indexOf('}',start);
                }
                do {
                    String strInt = strSequence.substring(start,end);
                    intList.add(new Integer(strInt));
                    start= end +1;
                    end = strSequence.indexOf(',',start);
                    if (end == -1)  end = strSequence.indexOf('}',start);
                } while (end != -1);
                Unit[] subunits = new Unit[intList.size()];
                Iterator iter = intList.iterator();
                int unitNum = 0;
                while (iter.hasNext()) {
                    Integer subunit_dbid = (Integer)iter.next();
                    subunits[unitNum++] = get(subunit_dbid.intValue());
                }
                return new UnitImpl(subunits, power, name, multFactor,exponent);
            } else {
                return new UnitImpl(stringToBase(base), power, name, multFactor,
                                    exponent);
            }
        }
        throw new NotFound("dbid="+dbid);
    }

    /** adds the unit to database */
    public int put(UnitImpl aUnit) throws SQLException {
        // unit already in table???
        try {
            return getDBId(aUnit);
        } catch (NotFound e) {
            // not here yet so add
        }
        Unit[] subunits;
        int[] subunit_dbid;
        String base;
        String subunitString;

        if (aUnit.isBaseUnit()) {
            subunits = new UnitImpl[0];
            subunit_dbid = new int[0];
            base = UnitImpl.baseToString(aUnit.getBaseUnit());
            subunitString = "";
        } else {
            subunits = aUnit.getSubUnits();
            subunit_dbid = new int[subunits.length];
            subunitString = "{";
            for (int i=0; i<subunits.length; i++) {
                subunit_dbid[i] = put((UnitImpl)subunits[i]);
                subunitString += subunit_dbid[i];
                if (i != subunits.length-1) subunitString += ",";
            }
            subunitString += "}";
            base = UnitImpl.baseToString(aUnit.getBaseUnit());
        }
        String unitName = aUnit.name;
        if ( ! aUnit.isNamed())  unitName = aUnit.toString();
        int nextDBId = seq.next();
        putStmt.setInt(1, nextDBId);
        putStmt.setString(2, unitName);
        putStmt.setString(3, base);
        putStmt.setString(4, subunitString);
        putStmt.setInt(5, aUnit.getExponent());
        putStmt.setDouble(6, aUnit.getMultiFactor());
        putStmt.setInt(7, aUnit.getPower());
        putStmt.executeUpdate();
        cache.put(new Integer(nextDBId), aUnit);
        return nextDBId;
    }

    public int getDBId(Unit aUnit) throws SQLException, NotFound {
        // check cache first
        if (cache.containsValue(aUnit)) {
            Iterator it = cache.keySet().iterator();
            while(it.hasNext()) {
                Integer id = (Integer)it.next();
                if (((Unit)cache.get(id)).equals(aUnit)) {
                    return id.intValue();
                }
            }
        }
        // not in cache so check db
        getDBIdStmt.setString(1, aUnit.toString());
        ResultSet rs = getDBIdStmt.executeQuery();
        if (rs.next())  return rs.getInt("unit_id");
        throw new NotFound(aUnit.toString());
    }

    public static final UnitBase stringToBase(String inString) {
        if(inString.equals("METER")) return UnitBase.from_int(UnitBase._METER);
        if(inString.equals("GRAM"))return UnitBase.from_int(UnitBase._GRAM);
        if(inString.equals("SECOND"))return UnitBase.from_int(UnitBase._SECOND);
        if(inString.equals("AMPERE"))return UnitBase.from_int(UnitBase._AMPERE);
        if(inString.equals("KELVIN"))return UnitBase.from_int(UnitBase._KELVIN);
        if(inString.equals("MOLE"))return  UnitBase.from_int(UnitBase._MOLE);
        if(inString.equals("CANDELA"))return UnitBase.from_int(UnitBase._CANDELA);
        if(inString.equals("COUNT")) return  UnitBase.from_int(UnitBase._COUNT);
        if(inString.equals("COMPOSITE")) return UnitBase.from_int(UnitBase._COMPOSITE);
        return null;
    }

    protected void createCache() throws SQLException {
        ArrayList common = new ArrayList();
        common.add(UnitImpl.SECOND);
        common.add(UnitImpl.MILLISECOND);
        common.add(UnitImpl.MICROSECOND);
        common.add(UnitImpl.NANOSECOND);
        common.add(UnitImpl.HERTZ);
        common.add(UnitImpl.METER);
        common.add(UnitImpl.KILOMETER);
        common.add(UnitImpl.MILLIMETER);
        common.add(UnitImpl.MICROMETER);
        common.add(UnitImpl.NANOMETER);
        common.add(UnitImpl.METER_PER_SECOND);
        common.add(UnitImpl.GRAM);
        common.add(UnitImpl.KILOGRAM);
        common.add(UnitImpl.METER_PER_SECOND_PER_SECOND);
        common.add(UnitImpl.NEWTON);
        common.add(UnitImpl.RADIAN);
        common.add(UnitImpl.DEGREE);
        common.add(UnitImpl.AMPERE);
        common.add(UnitImpl.JOULE);
        common.add(UnitImpl.COULOMB);
        common.add(UnitImpl.VOLT);
        common.add(UnitImpl.MICROMETER_PER_SECOND);
        common.add(UnitImpl.NANOMETER_PER_SECOND);
        common.add(UnitImpl.COUNT);

        int dbid;
        Iterator iter = common.iterator();
        UnitImpl unit;
        while (iter.hasNext()) {
            unit = (UnitImpl)iter.next();
            dbid = put(unit);
        }
    }

    private JDBCSequence seq;

    protected PreparedStatement putStmt;

    protected PreparedStatement getDBIdStmt;

    protected PreparedStatement getStmt;

    protected HashMap cache = new HashMap();
}
