package edu.sc.seis.fissuresUtil.display;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BoxLayout;
import org.apache.log4j.Logger;
import edu.sc.seis.fissuresUtil.display.borders.TimeBorder;
import edu.sc.seis.fissuresUtil.display.drawable.Drawable;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableIterator;
import edu.sc.seis.fissuresUtil.display.drawable.Selection;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.BasicTimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RMeanAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * VerticalSeismogramDisplay(VSD) is a JComponent that can contain multiple
 * BasicSeismogramDisplays(BSD)
 *
 * Created: Tue Jun  4 10:52:23 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public abstract class VerticalSeismogramDisplay extends SeismogramDisplay{

    public SeismogramDisplayProvider createCenter() {
        if(cp == null) cp = new CenterPanel();
        return cp;
    }

    protected class CenterPanel extends SeismogramDisplayProvider{
        public CenterPanel(){
            BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
            setLayout(layout);
        }

        public SeismogramDisplay provide() {
            return VerticalSeismogramDisplay.this;
        }

        public boolean contains(SeismogramDisplay sd){
            for (int i = 0; i < getComponentCount(); i++) {
                if(getComponent(i) == sd) return true;
            }
            return false;
        }

        public void setBorders(){
            if(getComponentCount() >= 1){
                if(!get(0).isFilled(TOP_CENTER)){
                    get(0).add(new TimeBorder(get(0)), TOP_CENTER);
                }
                if(getComponentCount() > 1){
                    get(0).clear(BOTTOM_CENTER);
                    getLast().clear(TOP_CENTER);
                }
                for (int i = 1; i < getComponentCount() - 1; i++) {
                    get(i).clear(TOP_CENTER);
                    get(i).clear(BOTTOM_CENTER);
                }
                if(!getLast().isFilled(BOTTOM_CENTER)){
                    getLast().add(new TimeBorder(getLast(), TimeBorder.BOTTOM),
                                  BOTTOM_CENTER);
                }
            }
            validate();
        }

        private SeismogramDisplay get(int pos){
            return (SeismogramDisplay)super.getComponent(pos);
        }

        private SeismogramDisplay getLast(){
            return get(getComponentCount() - 1);
        }
    }

    private static final Color EVEN = new Color(0, 0, 0, 0), ODD = Color.WHITE;

    public abstract void add(DataSetSeismogram[] dss);

    public void remove(DataSetSeismogram[] dss){
        for (int i = 0; i < cp.getComponentCount(); i++) {
            ((BasicSeismogramDisplay)cp.getComponent(i)).remove(dss);
        }
    }

    public boolean contains(DataSetSeismogram seismo){
        for (int i = 0; i < cp.getComponentCount(); i++) {
            if(((SeismogramDisplay)cp.getComponent(i)).contains(seismo)){
                return true;
            }
        }
        return false;
    }

    public void add(Drawable drawable){}//NO IMPL

    public void remove(Drawable drawable){} //NO IMPL

    public DrawableIterator getDrawables(MouseEvent e) {
        // TODO
        return null;
    }

    public LinkedList getDisplays() {
        LinkedList disps = new LinkedList();
        for (int i = 0; i < cp.getComponentCount(); i++) {
            disps.add(cp.getComponent(i));
        }
        return disps;
    }

    public DataSetSeismogram[] getSeismograms(){
        List grams = new ArrayList();
        for (int i = 0; i < cp.getComponentCount(); i++) {
            DataSetSeismogram[] seismos = ((SeismogramDisplay)cp.getComponent(i)).getSeismograms();
            for(int j = 0; j< seismos.length; j++)  grams.add(seismos[j]);
        }
        return ((DataSetSeismogram[])grams.toArray(new DataSetSeismogram[grams.size()]));
    }

    public void remove(Selection selection){
        for (int i = 0; i < cp.getComponentCount(); i++) {
            ((SeismogramDisplay)cp.getComponent(i)).remove(selection);
        }
    }

    public DrawableIterator iterator(Class drawableClass){
        List basicDisplays = new ArrayList();
        for (int i = 0; i < cp.getComponentCount(); i++) {
            basicDisplays.add(cp.getComponent(i));
        }
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

    protected void setBorders(){ cp.setBorders(); }

    public void clearSelections(){
        for (int i = 0; i < cp.getComponentCount(); i++) {
            ((BasicSeismogramDisplay)cp.getComponent(i)).clearSelections();
        }
    }

    public void clear(){
        cp.removeAll();
        tc.reset();
        ac.reset();
        repaint();
    }

    public void print(){}

    /**
     * <code>removeAll</code> clears this display and all of its children,
     * and if it has a parent, removes it from the parent as well
     *
     */
    public void removeAll(){ clear(); }

    /**
     * <code>removeDisplay</code> removes a BSD from the VSD
     *
     * @param display the BSD to be removed
     * @return true if the display is removed
     */
    public boolean removeDisplay(BasicSeismogramDisplay display){
        if(cp.contains(display)){
            if(cp.getComponentCount() == 1) clear();
            cp.remove(display);
            setBorders();
            revalidate();
            return true;
        }
        return false;
    }

    public void setAmpConfig(AmpConfig ac){ this.ac = ac; }

    public void setGlobalizedAmpConfig(AmpConfig ac){
        setAmpConfig(ac);
        for (int i = 0; i < cp.getComponentCount(); i++) {
            ((SeismogramDisplay)cp.getComponent(i)).setAmpConfig(ac);
        }
        globalizedAmp = true;
        tc.addListener(ac);
    }

    public void setIndividualizedAmpConfig(AmpConfig ac){
        Class configClass = ac.getClass();
        for (int i = 0; i < cp.getComponentCount(); i++) {
            SeismogramDisplay cur = (SeismogramDisplay)cp.getComponent(i);
            try{
                cur.setAmpConfig((AmpConfig)configClass.newInstance());
            }catch(IllegalAccessException e){
                GlobalExceptionHandler.handle("Problem creating ampConfig from class", e);
            }catch(InstantiationException e){
                GlobalExceptionHandler.handle("Problem creating ampConfig from class", e);
            }
        }
        tc.removeListener(ac);
        globalizedAmp = false;
    }

    public AmpConfig getAmpConfig(){ return ac; }

    public void setTimeConfig(TimeConfig config){
        for (int i = 0; i < cp.getComponentCount(); i++) {
            SeismogramDisplay cur = (SeismogramDisplay)cp.getComponent(i);
            if(cur.getTimeConfig().equals(tc)) cur.setTimeConfig(config);
        }
        if(globalizedAmp) config.addListener(ac);
        tc = config;
    }

    public TimeConfig getTimeConfig(){ return tc; }

    public void reset(){
        tc.reset();
        ac.reset();
        for (int i = 0; i < cp.getComponentCount(); i++) {
            ((SeismogramDisplay)cp.getComponent(i)).reset();
        }
    }

    public void reset(DataSetSeismogram[] seismos){
        for (int i = 0; i < cp.getComponentCount(); i++) {
            ((SeismogramDisplay)cp.getComponent(i)).reset(seismos);
        }
    }

    protected CenterPanel cp;

    protected boolean globalizedAmp = false;

    protected TimeConfig tc = new BasicTimeConfig();

    protected AmpConfig ac = new RMeanAmpConfig();

    private static Logger logger = Logger.getLogger(VerticalSeismogramDisplay.class);
}// VerticalSeismogramDisplay
