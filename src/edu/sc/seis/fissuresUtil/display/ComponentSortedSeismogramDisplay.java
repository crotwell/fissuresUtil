package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.borders.Border;
import edu.sc.seis.fissuresUtil.display.borders.TitleBorder;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.IndividualizedAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * ComponentSortedSeismogramDisplay.java Created: Tue Nov 12 13:49:33 2002
 * 
 * @author <a href="mailto:">Charlie Groves </a>
 * @version
 */
public class ComponentSortedSeismogramDisplay extends VerticalSeismogramDisplay {

    public void add(DataSetSeismogram[] dss) {
        DataSetSeismogram[][] componentSorted = DisplayUtils.sortByComponents(dss);
        addZ(componentSorted[2], tc);
        addNorth(componentSorted[0], tc);
        addEast(componentSorted[1], tc);
        setBorders();
    }

    private void addNorth(DataSetSeismogram[] newNorth, TimeConfig displayTC) {
        north = addToDisplay(north, newNorth, displayTC, DisplayUtils.NORTH);
    }

    private void addEast(DataSetSeismogram[] newEast, TimeConfig displayTC) {
        east = addToDisplay(east, newEast, displayTC, DisplayUtils.EAST);
    }

    private void addZ(DataSetSeismogram[] newZ, TimeConfig displayTC) {
        z = addToDisplay(z, newZ, displayTC, DisplayUtils.UP);
    }

    private BasicSeismogramDisplay addToDisplay(BasicSeismogramDisplay display,
                                                DataSetSeismogram[] seismos,
                                                TimeConfig displayTC,
                                                String orientation) {
        if(seismos.length > 0) {
            if(display == null) {
                display = new BasicSeismogramDisplay(displayTC);
                display.setParentDisplay(this);
                int position = -1;
                if(orientation == DisplayUtils.UP) {
                    position = 0;
                } else if(orientation == DisplayUtils.NORTH) {
                    if(z != null) {
                        position = 1;
                    } else {
                        position = 0;
                    }
                }
                initializeBSD(display, position, orientation);
            }
            display.add(seismos);
        }
        return display;
    }

    private void initializeBSD(BasicSeismogramDisplay disp,
                               int position,
                               String orientation) {
        getCenter().add(disp, position);
        disp.add(new TitleBorder(Border.ASCENDING, Border.RIGHT, orientation),
                 BorderedDisplay.CENTER_RIGHT);
    }

    public void setIndividualizedAmpConfig(AmpConfig ac) {
        Class configClass = ac.getClass();
        for(int i = 0; i < cp.getComponentCount(); i++) {
            try {
                AmpConfig newAmp = new IndividualizedAmpConfig((AmpConfig)configClass.newInstance());
                ((SeismogramDisplay)cp.getComponent(i)).setAmpConfig(newAmp);
            } catch(IllegalAccessException e) {
                GlobalExceptionHandler.handle("Problem creating ampConfig from class",
                                              e);
            } catch(InstantiationException e) {
                GlobalExceptionHandler.handle("Problem creating ampConfig from class",
                                              e);
            }
        }
    }

    public boolean removeDisplay(BasicSeismogramDisplay display) {
        if(display == north) north = null;
        else if(display == east) east = null;
        else z = null;
        return super.removeDisplay(display);
    }

    private BasicSeismogramDisplay north, east, z;
}// ComponentSortedSeismogramDisplay
