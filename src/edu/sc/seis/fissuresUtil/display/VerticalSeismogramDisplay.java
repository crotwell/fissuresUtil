package edu.sc.seis.fissuresUtil.display;
import java.util.*;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.drawable.Drawable;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableIterator;
import edu.sc.seis.fissuresUtil.display.drawable.Selection;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.BasicTimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RMeanAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.ExceptionHandlerGUI;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import javax.swing.BoxLayout;
import org.apache.log4j.Category;
import java.awt.event.MouseEvent;

/**
 * VerticalSeismogramDisplay(VSD) is a JComponent that can contain multiple
 * BasicSeismogramDisplays(BSD) and also controls the selection windows and
 * particle motion windows created by its BSDs.
 *
 *
 * Created: Tue Jun  4 10:52:23 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public abstract class VerticalSeismogramDisplay extends SeismogramDisplay{
    /**
     * Creates a <code>VerticalSeismogramDisplay</code> without a parent
     *
     */
    public VerticalSeismogramDisplay(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void add(DataSetSeismogram[] dss){ addDisplay(dss); }

    public void remove(DataSetSeismogram[] dss){
        Iterator it = basicDisplays.iterator();
        while(it.hasNext()){
            ((BasicSeismogramDisplay)it.next()).remove(dss);
        }
    }

    public boolean contains(DataSetSeismogram seismo){
        Iterator it = basicDisplays.iterator();
        while(it.hasNext()){
            if(((BasicSeismogramDisplay)it.next()).contains(seismo)){
                return true;
            }
        }
        return false;
    }

    /**
     * adds the given seismograms to the VSD with their seismogram names as suggestions
     *
     *
     * @param dss the seismograms to be added
     * @return the BSD the seismograms were added to
     */
    public abstract BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss);

    /**
     * adds the seismograms to the VSD with the passed timeConfig
     *
     * @param dss the seismograms to be added
     * @param tc the time config for the new seismograms
     * @return the BSD the seismograms were added to
     */
    public abstract BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc);

    /**
     * adds the seismograms to the VSD with the passed amp config
     * @param dss the seismograms to be added
     * @param ac the amp config for the new seismograms
     * @return the BSD the seismograms were added to
     */
    public abstract BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, AmpConfig ac);

    /**
     * adds the seismograms to the VSD with the passed timeConfig and ampConfig
     *
     * @param dss the seismograms for the new BSD
     * @param tc the time config for the new BSD
     * @param ac the amp config for the new BSD
     * @return the BSD the seismograms were added to
     *
     */
    public abstract BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc, AmpConfig ac);

    public void add(Drawable drawable){
        BasicSeismogramDisplay disp = (BasicSeismogramDisplay)basicDisplays.get(0);
        if(disp != null){
            disp.add(drawable);
        }
    }

    public void remove(Drawable drawable){
        BasicSeismogramDisplay disp = (BasicSeismogramDisplay)basicDisplays.get(0);
        if(disp != null){
            disp.remove(drawable);
        }
    }

    public DrawableIterator getDrawables(MouseEvent e) {
        // TODO
        return null;
    }

    public DataSetSeismogram[] getSeismograms(){
        java.util.List seismogramList = new ArrayList();
        Iterator e = basicDisplays.iterator();
        while(e.hasNext()){
            DataSetSeismogram[] seismos = ((BasicSeismogramDisplay)e.next()).getSeismograms();
            for(int i = 0; i < seismos.length; i++){
                seismogramList.add(seismos[i]);
            }
        }
        return ((DataSetSeismogram[])seismogramList.toArray(new DataSetSeismogram[seismogramList.size()]));
    }

    /**
     * <code>getDisplays</code> returns a list of all the displays directly held by this VSD
     *
     * @return a list of all direcly held displays
     */
    public LinkedList getDisplays(){ return basicDisplays; }

    public void print(){
        SeismogramPrinter.print(getDisplayArray());
        revalidate();
    }

    /**
     * <code>getDisplayArray</code> returns an array containing all of
     * the displays directly held by this VSD
     *
     * @return all displays directly held by this vsd
     */
    public BasicSeismogramDisplay[] getDisplayArray(){
        return ((BasicSeismogramDisplay[])basicDisplays.toArray(new BasicSeismogramDisplay[basicDisplays.size()]));
    }

    public void remove(Selection selection){
        Iterator it = basicDisplays.iterator();
        while(it.hasNext()){
            ((BasicSeismogramDisplay)it.next()).remove(selection);
        }
    }

    public DrawableIterator iterator(Class drawableClass){
        return new DrawableIterator(drawableClass, getAllDrawables(drawableClass,
                                                                   basicDisplays));
    }

    private static List getAllDrawables(Class drawableClass, List displays){
        List allDrawables = new LinkedList();
        Iterator it = displays.iterator();
        while(it.hasNext()){
            SeismogramDisplay cur = (SeismogramDisplay)it.next();
            DrawableIterator drawIt = cur.iterator(drawableClass);
            while(drawIt.hasNext()){
                allDrawables.add(drawIt.next());
            }
        }
        return allDrawables;
    }

    public void clearSelections(){
        Iterator e = basicDisplays.iterator();
        while(e.hasNext()){
            ((BasicSeismogramDisplay)e.next()).clearSelections();
        }
    }

    public void clear(){
        removeAll();
    }

    /**
     * <code>removeAll</code> clears this display and all of its children,
     * and if it has a parent, removes it from the parent as well
     *
     */
    public void removeAll(){
        logger.debug("removing all displays");
        super.removeAll();
        basicDisplays.clear();
        tc = new BasicTimeConfig();
        ac = new RMeanAmpConfig();
        repaint();
    }

    /**
     * <code>removeDisplay</code> removes a BSD from the VSD
     *
     * @param display the BSD to be removed
     * @return true if the display is removed
     */
    public boolean removeDisplay(BasicSeismogramDisplay display){
        if(basicDisplays.contains(display)){
            if(basicDisplays.size() == 1){
                this.removeAll();
                return true;
            }
            super.remove(display);
            basicDisplays.remove(display);
            ((BasicSeismogramDisplay)basicDisplays.getFirst()).addTopTimeBorder();
            ((BasicSeismogramDisplay)basicDisplays.getLast()).addBottomTimeBorder();
            super.revalidate();
            display.destroy();
            repaint();
            return true;
        }
        return false;
    }

    protected void setTimeBorders(){
        for (int i = 1; i < getComponentCount() - 1; i++){
            BasicSeismogramDisplay cur = (BasicSeismogramDisplay)getComponent(i);
            cur.removeTopTimeBorder();
            cur.removeBottomTimeBorder();
        }
        BasicSeismogramDisplay top = (BasicSeismogramDisplay)getComponent(0);
        top.addTopTimeBorder();
        top.removeBottomTimeBorder();
        BasicSeismogramDisplay bottom = (BasicSeismogramDisplay)getComponent(getComponentCount() - 1);
        bottom.removeTopTimeBorder();
        bottom.addBottomTimeBorder();
    }

    public void setAmpConfig(AmpConfig ac){
        this.ac = ac;
    }

    public void setGlobalizedAmpConfig(AmpConfig ac){
        setAmpConfig(ac);
        Iterator it = basicDisplays.iterator();
        while(it.hasNext()){
            ((BasicSeismogramDisplay)it.next()).setAmpConfig(ac);
        }
        globalizedAmp = true;
        tc.addListener(ac);
    }

    public void setIndividualizedAmpConfig(AmpConfig ac){
        Iterator it = basicDisplays.iterator();
        Class configClass = ac.getClass();
        while(it.hasNext()){
            try{
                ((BasicSeismogramDisplay)it.next()).setAmpConfig((AmpConfig)configClass.newInstance());
            }catch(IllegalAccessException e){
                ExceptionHandlerGUI.handleException("Problem creating ampConfig from class", e);
            }catch(InstantiationException e){
                ExceptionHandlerGUI.handleException("Problem creating ampConfig from class", e);
            }
        }
        tc.removeListener(ac);
        globalizedAmp = false;
    }

    public AmpConfig getAmpConfig(){
        return ac;
    }
    public void setTimeConfig(TimeConfig config){
        Iterator it = basicDisplays.iterator();
        while(it.hasNext()){
            BasicSeismogramDisplay current = (BasicSeismogramDisplay)it.next();
            if(current.getTimeConfig().equals(tc)){
                current.setTimeConfig(config);
            }
        }
        if(globalizedAmp){
            config.addListener(ac);
        }
        tc = config;
    }

    public TimeConfig getTimeConfig(){
        return tc;
    }

    public void reset(){
        tc.reset();
        ac.reset();
        Iterator e = basicDisplays.iterator();
        while(e.hasNext()){
            ((BasicSeismogramDisplay)e.next()).reset();
        }
    }

    public void reset(DataSetSeismogram[] seismos){
        Iterator e = basicDisplays.iterator();
        while(e.hasNext()){
            ((BasicSeismogramDisplay)e.next()).reset(seismos);
        }
    }

    protected boolean globalizedAmp = false;

    protected TimeConfig tc = new BasicTimeConfig();

    protected AmpConfig ac = new RMeanAmpConfig();

    protected LinkedList basicDisplays = new LinkedList();

    private static Category logger = Category.getInstance(VerticalSeismogramDisplay.class.getName());
}// VerticalSeismogramDisplay
