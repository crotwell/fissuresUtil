package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RMeanAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.Registrar;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
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
    /**
     * Creates a <code>MultiSeismogramWindowDisplay</code> without a parent
     *
     */
    public MultiSeismogramWindowDisplay(SeismogramSorter sorter){
        this(null, sorter);
    }

    /**
     * Creates a <code>MultiSeismogramWindowDisplay</code> with a parent
     *
     * @param parent the VSD that controls this VSD
     */
    public MultiSeismogramWindowDisplay(VerticalSeismogramDisplay parent,
                                        SeismogramSorter sorter){
        super(parent);
        this.sorter = sorter;
    }

    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss){
        return addDisplay(dss, registrar, new RMeanAmpConfig(dss));
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
        return addDisplay(dss, registrar, ac);
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
        if(tc == registrar && registrar == null){
            registrar = new Registrar(dss);
            tc = registrar;
        }
        BasicSeismogramDisplay disp = null;
        for(int i = 0; i < dss.length; i++){
            if(contains(dss[i])){
                continue;
            }
            DataSetSeismogram[] seismos = { dss[i] };
            disp = new BasicSeismogramDisplay(seismos, tc, ac, this);
            if(currentTimeFlag){
                disp.setCurrentTimeFlag(currentTimeFlag);
            }
            int j = sorter.sort(dss[i]);
            super.add(disp, j);
            basicDisplays.add(j, disp);
            addTimeBorders();
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

}// MultiSeismogramWindowDisplay
