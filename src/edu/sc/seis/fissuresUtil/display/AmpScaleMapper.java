package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.registrar.*;

import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdAuxillaryDataNames;
import java.text.DecimalFormat;
import org.apache.log4j.Logger;

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
        if (event.getSeismograms().length != 0) {
            setUnitRange(unitDisplayUtil.getRealWorldUnitRange(event.getAmp(),
                                                               event.getSeismograms()[0]));
        } else {
            setUnitRange(event.getAmp());
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
