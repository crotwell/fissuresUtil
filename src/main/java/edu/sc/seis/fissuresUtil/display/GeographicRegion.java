/**
 * GeographicRegion.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.FlinnEngdahlType;

public class GeographicRegion extends FlinnEngdahlRegion{
    public GeographicRegion(SeismicRegion parent, String name, int num){
        type = FlinnEngdahlType.GEOGRAPHIC_REGION;
        number = num;
        this.name = name;
        this.parent = parent;
    }

    public String getName(){ return name; }

    public SeismicRegion getParent(){ return parent; }

    public int getNumber(){ return number; }

    private SeismicRegion parent;
    private String name;
}

