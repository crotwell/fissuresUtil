package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.IndividualizedAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RMeanAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.Iterator;

/**
 * ComponentSortedSeismogramDisplay.java
 *
 *
 * Created: Tue Nov 12 13:49:33 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class ComponentSortedSeismogramDisplay extends VerticalSeismogramDisplay {
    public void add(DataSetSeismogram[] dss) {
        DataSetSeismogram[][] componentSorted = DisplayUtils.sortByComponents(dss);
        addNorth(componentSorted[0], tc);
        addEast(componentSorted[1], tc);
        addZ(componentSorted[2], tc);
        setBorders();
    }

    private void addNorth(DataSetSeismogram[] newNorth, TimeConfig tc){
        north = addToDisplay(north, newNorth, tc,DisplayUtils.NORTH);
    }

    private void addEast(DataSetSeismogram[] newEast, TimeConfig tc){
        east = addToDisplay(east, newEast, tc, DisplayUtils.EAST);
    }

    private void addZ(DataSetSeismogram[] newZ, TimeConfig tc){
        z = addToDisplay(z, newZ, tc, DisplayUtils.UP);
    }

    private BasicSeismogramDisplay addToDisplay(BasicSeismogramDisplay display, DataSetSeismogram[] seismos,
                                                TimeConfig tc, String orientation){
        if(seismos.length > 0){
            if(display == null){
                display = new BasicSeismogramDisplay(tc);
                display.setParentDisplay(this);
                initializeBSD(display, 0, orientation);
            }
            display.add(seismos);
        }
        return display;
    }

    private void initializeBSD(BasicSeismogramDisplay disp, int position, String orientation){
        getCenter().add(disp, position);
    }

    public void setIndividualizedAmpConfig(AmpConfig ac){
        Class configClass = ac.getClass();
        for (int i = 0; i < cp.getComponentCount(); i++) {
            try{
                AmpConfig newAmp = new IndividualizedAmpConfig((AmpConfig)configClass.newInstance());
                ((SeismogramDisplay)cp.getComponent(i)).setAmpConfig(newAmp);
            }catch(IllegalAccessException e){
                GlobalExceptionHandler.handle("Problem creating ampConfig from class", e);
            }catch(InstantiationException e){
                GlobalExceptionHandler.handle("Problem creating ampConfig from class", e);
            }
        }
    }

    public boolean removeDisplay(BasicSeismogramDisplay display){
        if(display == north) north = null;
        else if(display == east) east = null;
        else z = null;
        return super.removeDisplay(display);
    }

    private BasicSeismogramDisplay north, east, z;
}// ComponentSortedSeismogramDisplay
