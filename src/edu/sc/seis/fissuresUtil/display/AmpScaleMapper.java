package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.AmpListener;
import edu.sc.seis.fissuresUtil.display.registrar.LazyAmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.Registrar;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

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

    public AmpScaleMapper(int totalPixels,
                          int hintPixels,
                          Registrar reg){
        super(totalPixels, hintPixels, true);
        this.reg = reg;
        setUnitRange(DisplayUtils.ONE_RANGE);
        reg.addListener(this);
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

    public void setRegistrar(Registrar reg){
        this.reg.removeListener(this);
        this.reg = reg;
        reg.addListener(this);
    }

    public String getAxisLabel(){
        return "Amplitude (" + unitDisplayUtil.getNameForUnit(getUnit()) + ")";
    }

    private UnitDisplayUtil unitDisplayUtil = new UnitDisplayUtil();

    private Registrar reg;
} // AmpScaleMapper

