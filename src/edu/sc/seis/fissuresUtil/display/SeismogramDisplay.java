package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.mouse.*;

import edu.sc.seis.fissuresUtil.display.borders.Border;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.drawable.Drawable;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableIterator;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableSeismogram;
import edu.sc.seis.fissuresUtil.display.drawable.Selection;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.DataSetSeismogramReceptacle;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;

public abstract class SeismogramDisplay extends JComponent implements DataSetSeismogramReceptacle{
    public SeismogramDisplay(){
        this(mouseForwarder, motionForwarder);
    }

    public SeismogramDisplay(SDMouseForwarder mf, SDMouseMotionForwarder mmf){
        mouseForwarder = mf;
        motionForwarder = mmf;
        if(mouseForwarder == null || motionForwarder == null){
            mouseForwarder = new SDMouseForwarder();
            motionForwarder = new SDMouseMotionForwarder();
        }
        setLayout(new GridBagLayout());
        add(getCenterPanel(), CENTER);
    }

    public void add(SeismogramDisplayListener listener){
        listeners.add(listener);
    }

    public void remove(SeismogramDisplayListener listener){
        listeners.remove(listener);
    }

    public abstract SeismogramDisplayProvider getCenterPanel();

    public void removeAll(){
        for (int i = 0; i < borders.length; i++) { clear(i); }
        super.removeAll();
    }

    public Color getColor(){ return null; }

    public Color getNextColor(Class colorGroupClass){
        int[] usages = new int[COLORS.length];
        for (int i = 0; i < COLORS.length; i++){
            Iterator it = iterator(colorGroupClass);
            while(it.hasNext()){
                Drawable cur = (Drawable)it.next();
                if(cur.getColor().equals(COLORS[i])){
                    usages[i]++;
                }
                if(cur instanceof DrawableSeismogram){
                    DrawableSeismogram curSeis = (DrawableSeismogram)cur;
                    Iterator childIterator = curSeis.iterator(colorGroupClass);
                    while(childIterator.hasNext()){
                        Drawable curChild = (Drawable)childIterator.next();
                        if(curChild.getColor().equals(COLORS[i])){
                            usages[i]++;
                        }
                    }
                }
            }
        }
        for(int minUsage = 0; minUsage >= 0; minUsage++){
            for (int i = 0; i < usages.length; i++){
                if(usages[i] == minUsage){
                    return COLORS[i];
                }
            }
        }
        return COLORS[i++%COLORS.length];
    }

    public MicroSecondDate getTime(MouseEvent e){
        return SimplePlotUtil.getValue(getWidth() - getInsets().left - getInsets().right,
                                       getTimeConfig().getTime().getBeginTime(),
                                       getTimeConfig().getTime().getEndTime(),
                                       e.getX() - getInsets().left);
    }

    //this may become abstract later
    //this reaks of HACK!
    public int countDrawables(){
        return 0;
    }

    private int i = 0;

    public abstract void add(Drawable drawable);

    public abstract void remove(Drawable drawable);

    public abstract DrawableIterator getDrawables(MouseEvent e);

    public abstract DrawableIterator iterator(Class drawableClass);

    public abstract void setTimeConfig(TimeConfig timeConfig);

    public abstract TimeConfig getTimeConfig();

    public abstract void setAmpConfig(AmpConfig ampConfig);

    public abstract void setGlobalizedAmpConfig(AmpConfig ampConfig);

    public abstract void setIndividualizedAmpConfig(AmpConfig ampConfig);

    public abstract AmpConfig getAmpConfig();

    public abstract DataSetSeismogram[] getSeismograms();

    public abstract void print();

    public void remove(Selection selection){}

    public static void setMouseMotionForwarder(SDMouseMotionForwarder mf){
        motionForwarder = mf;
    }

    protected void add(JComponent comp, int position){
        if(position == CENTER){
            center = comp;
        }
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;//Fill all panels in both directions
        gbc.gridx = position%3;
        gbc.gridy = position/3;
        if(gbc.gridx == 1) gbc.weightx = 1;//All row 1 components have a x
        else gbc.weightx = 0;//weight of 1
        if(gbc.gridy == 1) gbc.weighty = 1;// All column 1 components have y
        else gbc.weighty = 0;//weight of 1
        super.add(comp, gbc);
    }

    public void addBorder(Border border, int position) {
        add(border, position);
    }

    public void addTitle(String title, int position){

    }

    public void clear(int position){
        if(position == CENTER){
            remove(center);
            center = null;
        }else if(isFilled(position)){
            remove(borders[position]);
            borders[position] = null;
        }
    }

    public boolean isFilled(int position){ return borders[position] != null; }

    public static SDMouseMotionForwarder getMouseMotionForwarder(){
        return motionForwarder;
    }

    public static void setMouseForwarder(SDMouseForwarder mf){
        mouseForwarder = mf;
    }

    public static SDMouseForwarder getMouseForwarder(){ return mouseForwarder; }

    public static Set getActiveFilters(){ return activeFilters; }

    public static void setCurrentTimeFlag(boolean visible){
        currentTimeFlag = visible;
    }

    public static boolean getCurrentTimeFlag(){ return currentTimeFlag; }

    private static SDMouseMotionForwarder motionForwarder;

    private static SDMouseForwarder mouseForwarder;

    private List listeners = new ArrayList();

    private static boolean currentTimeFlag = false;

    protected static Set activeFilters = new HashSet();

    public static final int TOP_LEFT = 0, TOP_CENTER = 1, TOP_RIGHT = 2,
        CENTER_LEFT = 3, CENTER_RIGHT = 5, BOTTOM_LEFT = 6, BOTTOM_CENTER = 7,
        BOTTOM_RIGHT = 8;

    protected static final int CENTER = 4;

    private JComponent center;

    private Border[] borders = new Border[9];

    public static final Color[] COLORS = {Color.BLUE, new Color(217, 91, 23), new Color(179, 182,46), new Color(141, 18, 69),new Color(65,200,115),new Color(27,36,138), new Color(130,145,230), new Color(54,72,21), new Color(119,17,136)};
}

