package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.AmpListener;
import edu.sc.seis.fissuresUtil.display.registrar.LazyAmpEvent;

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
     */
    public AmpScaleMapper(int totalPixels, int hintPixels){
        this(totalPixels, hintPixels, null);
    }


    /**
     * this constructor uses the passed in amp config to determine the value
     * for the scale mapper
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
            setUnitRange(event.getAmp());
        }
    }

    public String getAxisLabel(){
        return "Amplitude (" + UnitDisplayUtil.getNameForUnit(getUnit()) + ")";
    }

    private UnitDisplayUtil unitDisplayUtil = new UnitDisplayUtil();
} // AmpScaleMapper
