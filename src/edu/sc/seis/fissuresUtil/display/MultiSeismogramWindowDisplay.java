package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.drawable.SoundPlay;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RMeanAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import org.apache.log4j.Logger;

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
    /**
     * Creates a <code>MultiSeismogramWindowDisplay</code> without a parent
     *
     */
    public MultiSeismogramWindowDisplay(SeismogramSorter sorter){
        this.sorter = sorter;
    }

    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss){
        return addDisplay(dss, tc, new RMeanAmpConfig(dss));
    }

    /**
     * creates a new BSD with an individual RMeanAmpConfig and the passed in TImeConfig
     * and adds it to the display
     *
     * @param dss the seismograms for the new BSD
     * @param tc the time config for the new BSD
     * @param name the BSD's name
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc){
        return addDisplay(dss, tc, new RMeanAmpConfig(dss));
    }

    /**
     * creates a new BSD with the passed in amp config and the global TImeConfig
     * and adds it to the display
     *
     * @param dss the seismograms for the new BSD
     * @param ac the amp config for the new BSD
     * @param name the BSD's name
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, AmpConfig ac){
        return addDisplay(dss, tc, ac);
    }

    /**
     * creates a new BSD with the passed in amp and time configs and adds it to
     * the display
     *
     * @param dss the seismograms for the new BSD
     * @param tc the time config for the new BSD
     * @param ac the amp config for the new BSD
     * @param name the BSD's name
     * @return the created BSD
     * @return a <code>BasicSeismogramDisplay</code> value
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc, AmpConfig ac){
        BasicSeismogramDisplay disp = null;

        for(int i = 0; i < dss.length; i++){
            if(contains(dss[i])){
                continue;
            }
            DataSetSeismogram[] seismos = { dss[i] };
            disp = new BasicSeismogramDisplay(tc, ac, this);
            disp.add(seismos);
            int j = sorter.sort(dss[i]);
            super.add(disp, j);
            basicDisplays.add(j, disp);
            setTimeBorders();
            disp.addSoundPlay();
        }
        return disp;
    }

    public void remove(DataSetSeismogram[] dss){
        for (int i = 0; i < dss.length; i++){
            sorter.remove(dss[i]);
        }
        super.remove(dss);
    }

    public void removeAll(){
        sorter.clear();
        super.removeAll();
    }

    private SeismogramSorter sorter;
    private SoundPlay soundPlay;

    private static Logger logger = Logger.getLogger(MultiSeismogramWindowDisplay.class);

}// MultiSeismogramWindowDisplay

