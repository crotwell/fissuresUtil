/**
 * ReverseMultiWindowDisplay.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.display;

public class ReverseMultiWindowDisplay extends MultiSeismogramWindowDisplay {

    public ReverseMultiWindowDisplay(SeismogramSorter sorter) {
        super(sorter);
    }

    protected void addBSD(BasicSeismogramDisplay disp, int pos) {
        super.addBSD(disp, getCenter().getComponentCount() - pos);
    }
}
