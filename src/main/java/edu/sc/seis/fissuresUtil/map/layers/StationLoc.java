package edu.sc.seis.fissuresUtil.map.layers;

import edu.iris.Fissures.IfNetwork.Station;

/**
 * @author groves Created on Aug 25, 2004
 */
public class StationLoc {
	private Station station;

	private int[] yCoords;

	private int[] xCoords;

	public StationLoc(Station sta, int[] xCoords, int[] yCoords) {
		this.station = sta;
		this.xCoords = xCoords;
		this.yCoords = yCoords;
	}

	public Station getStation() {
		return station;
	}

	public int getX(int point) {
		return xCoords[point];
	}

	public int getY(int point) {
		return yCoords[point];
	}

	public int getNumPoints() {
		return yCoords.length;
	}
    
    public String getImageMapStylePoly(){
        StringBuffer buf = new StringBuffer();
        for (int j = 0; j < getNumPoints(); j++) {
            buf.append(getX(j)+ "," + getY(j));
            if (j != getNumPoints() - 1) {
                buf.append(',');
            }
        }
        return buf.toString();
    }
}