package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.AmpListener;
import edu.sc.seis.fissuresUtil.display.registrar.LazyAmpEvent;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import sun.security.krb5.internal.ac;

/**
 * AmpScaleMapper.java
 *
 *
 * Created: Fri Oct 22 14:47:39 1999
 *
 * @author Philip Crotwell
 * @version
 */

public class AmpScaleMapper extends UnitRangeMapper implements AmpListener {

    /**
     * this constructor assumes that some outside entity is going to set the
     * range on this scalemapper
     *
     * @param    totalPixels         an int
     * @param    hintPixels          an int
     *
     */
    public AmpScaleMapper(int totalPixels, int hintPixels){
        this(totalPixels, hintPixels, null);
    }


    /**
     * this constructor uses the passed in amp config to determine the value
     * for the scale mapper
     *
     */
    public AmpScaleMapper(int totalPixels,
                          int hintPixels,
                          AmpConfig ac){
        super(totalPixels, hintPixels, true);
        setUnitRange(DisplayUtils.ONE_RANGE);
        if(ac != null){
            ac.addListener(this);
        }
    }

    public void updateAmp(AmpEvent event){
        if(event instanceof LazyAmpEvent){
            ((LazyAmpEvent)event).addCalculateListener(this);
        }else{
            DataSetSeismogram seis = event.getSeismograms()[0];
            if(seis != null){
                setUnitRange(unitDisplayUtil.getBestForDisplay(event.getAmp(seis)));
            }else{
                setUnitRange(event.getAmp());
            }
        }
    }

    public String getAxisLabel(){
        return "Amplitude (" + unitDisplayUtil.getNameForUnit(getUnit()) + ")";
    }

    private UnitDisplayUtil unitDisplayUtil = new UnitDisplayUtil();
} // AmpScaleMapper

