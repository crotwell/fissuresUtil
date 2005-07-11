package edu.sc.seis.fissuresUtil.display.particlemotion;

import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * @author hedx Created on Jun 30, 2005 A Class to hold the vertical and
 *         horizontal seismograms.
 */
public class SeisContainer {

    public SeisContainer(DataSetSeismogram horizontal,
                         DataSetSeismogram vertical) {
        this.horz = horizontal;
        this.vert = vertical;
        this.seismo = new DataSetSeismogram[] {horz, vert};
    }

    public boolean equals(SeisContainer cont) {
        if(this.horz.equals(cont.getHorz()) && this.vert.equals(cont.getVert())) {
            return true;
        }
        return false;
    }

    public DataSetSeismogram[] getSeismograms() {
        return seismo;
    }

    public DataSetSeismogram getHorz() {
        return horz;
    }

    public DataSetSeismogram getVert() {
        return vert;
    }

    private DataSetSeismogram horz, vert;

    private DataSetSeismogram[] seismo;
}
