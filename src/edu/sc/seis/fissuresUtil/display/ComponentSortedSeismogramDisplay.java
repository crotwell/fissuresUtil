package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.borders.Border;
import edu.sc.seis.fissuresUtil.display.borders.TitleBorder;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.IndividualizedAmpConfig;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * ComponentSortedSeismogramDisplay.java Created: Tue Nov 12 13:49:33 2002
 * 
 * @author <a href="mailto:">Charlie Groves </a>
 * @version
 */
public class ComponentSortedSeismogramDisplay extends VerticalSeismogramDisplay {

    public ComponentSortedSeismogramDisplay() {
        this(true);
    }

    public ComponentSortedSeismogramDisplay(boolean handleBorders) {
        this.handleBorders = handleBorders;
    }

    public void add(DataSetSeismogram[] dss) {
        DataSetSeismogram[][] componentSorted = DisplayUtils.sortByComponents(dss);
        z = addToDisplay(z, componentSorted[2], DisplayUtils.UP);
        north = addToDisplay(north, componentSorted[0], DisplayUtils.NORTH);
        east = addToDisplay(east, componentSorted[1], DisplayUtils.EAST);
        if(handleBorders) {
            setBorders();
        }
    }

    public void setZ(BasicSeismogramDisplay sd) {
        z = sd;
        setupDisplay(sd, DisplayUtils.EAST);
    }

    public void setNorth(BasicSeismogramDisplay sd) {
        north = sd;
        setupDisplay(sd, DisplayUtils.NORTH);
    }

    public void setEast(BasicSeismogramDisplay sd) {
        east = sd;
        setupDisplay(sd, DisplayUtils.EAST);
    }

    private BasicSeismogramDisplay addToDisplay(BasicSeismogramDisplay display,
                                                DataSetSeismogram[] seismos,
                                                String orientation) {
        if(seismos.length > 0) {
            if(display == null) {
                display = new BasicSeismogramDisplay();
                setupDisplay(display, orientation);
                addOrientationTitleBorder(display, orientation);
            }
            display.add(seismos);
        }
        return display;
    }

    private int setupDisplay(BasicSeismogramDisplay display, String orientation) {
        display.setTimeConfig(tc);
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
        getCenter().add(display, position);
        return position;
    }

    private void addOrientationTitleBorder(BasicSeismogramDisplay disp,
                                           String orientation) {
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
        if(display == north)
            north = null;
        else if(display == east)
            east = null;
        else
            z = null;
        return super.removeDisplay(display);
    }

    private boolean handleBorders = true;

    private BasicSeismogramDisplay north, east, z;
}// ComponentSortedSeismogramDisplay
