package edu.sc.seis.fissuresUtil.display;
import org.apache.log4j.Logger;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * MultiSeismogramWindowDisplay displays every seismogram added to it
 * in a new Basic Seismogram Display
 *
 *
 * Created: Mon Nov 11 16:01:35 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class MultiSeismogramWindowDisplay extends VerticalSeismogramDisplay {
    public MultiSeismogramWindowDisplay(SeismogramSorter sorter){
        this.sorter = sorter;
    }

    public void add(DataSetSeismogram[] dss) {
        BasicSeismogramDisplay disp = null;
        for(int i = 0; i < dss.length; i++){
            if(contains(dss[i])) continue;
            DataSetSeismogram[] seismos = { dss[i] };
            disp = new BasicSeismogramDisplay(tc);
            disp.setParentDisplay(this);
            disp.add(seismos);
            int j = sorter.sort(dss[i]);
            addBSD(disp, j);
            disp.addSoundPlay();
        }
        setBorders();
    }

    protected void addBSD(BasicSeismogramDisplay disp, int pos){
        cp.add(disp, pos);
    }

    public void remove(DataSetSeismogram[] dss){
        removeFromSorter(dss);
        super.remove(dss);
    }

    public boolean removeDisplay(BasicSeismogramDisplay disp){
        DataSetSeismogram[] dss = disp.getSeismograms();
        boolean removed = super.removeDisplay(disp);
        if(removed) removeFromSorter(dss);
        return removed;
    }

    private void removeFromSorter(DataSetSeismogram[] dss){
        for (int i = 0; i < dss.length; i++) sorter.remove(dss[i]);
    }

    public void clear(){
        sorter.clear();
        super.clear();
    }

    private SeismogramSorter sorter;
    private static Logger logger = Logger.getLogger(MultiSeismogramWindowDisplay.class);

}// MultiSeismogramWindowDisplay

