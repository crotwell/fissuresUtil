package edu.sc.seis.fissuresUtil.display;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import org.apache.log4j.Logger;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.freq.NamedFilter;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;


/**
 *
 * Created: Tue Jun 11 15:22:30 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class ParticleMotionDisplay extends JPanel{
    
    public ParticleMotionDisplay(DataSetSeismogram datasetSeismogram,
                                 TimeConfig tc, Color color) {
        particleDisplayPanel = new JPanel(new BorderLayout());
        radioPanel = new JPanel(new GridLayout(1, 0));
        setLayout(new BorderLayout());
        
        view = new ParticleMotionView(this);
        view.setSize(new Dimension(300, 300));
        particleDisplayPanel.add(view);
        
        hAmpScaleMap = new UpdatingAmpScaleMapper(50, 4);
        vAmpScaleMap = new UpdatingAmpScaleMapper(50, 4);
        
        ScaleBorder scaleBorder = new ScaleBorder();
        scaleBorder.setBottomScaleMapper(hAmpScaleMap);
        scaleBorder.setLeftScaleMapper(vAmpScaleMap);
        hTitleBorder = new BottomTitleBorder("X - axis Title");
        vTitleBorder = new LeftTitleBorder("Y - axis Title");
        Border titleBorder = BorderFactory.createCompoundBorder(hTitleBorder,
                                                                vTitleBorder);
        Border bevelBorder = BorderFactory.createRaisedBevelBorder();
        Border bevelTitleBorder = BorderFactory.createCompoundBorder(bevelBorder,
                                                                     titleBorder);
        Border lowBevelBorder = BorderFactory.createLoweredBevelBorder();
        Border scaleBevelBorder = BorderFactory.createCompoundBorder(scaleBorder,
                                                                     lowBevelBorder);
        particleDisplayPanel.setBorder(BorderFactory.createCompoundBorder(bevelTitleBorder,
                                                                          scaleBevelBorder));
        add(particleDisplayPanel);
        radioPanel.setVisible(false);
        addComponentListener(new ComponentAdapter() {
                    public void componentResized(ComponentEvent e) {
                        resize();
                    }
                    public void componentShown(ComponentEvent e) {
                        resize();
                    }
                });
        ParticleMotionDisplayThread t = new ParticleMotionDisplayThread(datasetSeismogram,
                                                                        tc,
                                                                        this, color);
        t.execute();
        formRadioSetPanel();
        initialized = t.getCompletion();
        if(initialized){
            setInitialButton();
        }
        
        add(radioPanel, BorderLayout.SOUTH);
    }
    
    private class UpdatingAmpScaleMapper extends AmpScaleMapper{
        public UpdatingAmpScaleMapper(int totalPixels, int hintPixels){
            super(totalPixels, hintPixels);
        }
        
        public void updateAmp(AmpEvent e){
            setUnitRange(e.getAmp());
            repaint();}
    }
    
    public void setActiveAmpConfig(AmpConfig ac) {
        hAmpScaleMap.setAmpConfig(ac);
        vAmpScaleMap.setAmpConfig(ac);
    }
    
    public ParticleMotionView getView(){ return view; }
    
    /**
     * the method resize() is overridden so that the view of the
     * particleMotionDisplay is always a square.
     */
    public synchronized void resize() {
        if(getSize().width == 0 || getSize().height == 0) return;
        Dimension dim = view.getSize();
        Insets insets = view.getInsets();
        int width = particleDisplayPanel.getSize().width;
        int height = particleDisplayPanel.getSize().height;
        width = width - particleDisplayPanel.getInsets().left - particleDisplayPanel.getInsets().right;
        height = height - particleDisplayPanel.getInsets().top - particleDisplayPanel.getInsets().bottom;
        if(width < height) {
            particleDisplayPanel.setSize(new Dimension(particleDisplayPanel.getSize().width,
                                                       width + particleDisplayPanel.getInsets().top + particleDisplayPanel.getInsets().bottom));
        } else {
            particleDisplayPanel.setSize(new Dimension(height  + particleDisplayPanel.getInsets().left + particleDisplayPanel.getInsets().right,
                                                       particleDisplayPanel.getSize().height));
        }
        view.resize();
        if(hAmpScaleMap != null) {
            hAmpScaleMap.setTotalPixels(dim.width  - insets.left - insets.right);
            vAmpScaleMap.setTotalPixels(dim.height  - insets.top - insets.bottom);
        }
        repaint();
    }
    
    public synchronized void addParticleMotionDisplay(DataSetSeismogram datasetSeismogram,
                                                      TimeConfig tc, Color color) {
        ParticleMotionDisplayThread t = new ParticleMotionDisplayThread(datasetSeismogram,
                                                                        tc,
                                                                        this, color);
        t.execute();
    }
    
    /**
     * calculates the backAzimuthAngle and displays the azimuth angle and a sector surrouding
     * the azimuth angle.
     *
     * @param dataset an <code>edu.sc.seis.fissuresUtil.xml.DataSet</code> value
     * @param chanId a <code>ChannelId</code> value
     */
    public synchronized void displayBackAzimuth(DataSet dataset, ChannelId chanId, Color color) {
        EventAccessOperations cacheEvent = dataset.getEvent();
        Channel channel = dataset.getChannel(chanId);
        if(cacheEvent != null) {
            try {
                Origin origin = cacheEvent.get_preferred_origin();
                Station station = channel.getSite().getStation();
                double azimuth = SphericalCoords.azimuth(station.getLocation().latitude,
                                                         station.getLocation().longitude,
                                                         origin.getLocation().latitude,
                                                         origin.getLocation().longitude);
                double angle = 90 - azimuth;
                view.addAzimuthLine(angle, color);
                view.addSector(angle+5, angle-5);
            } catch(NoPreferredOrigin npoe) {
                logger.debug("no preferred origin");
            }
        }
    }
    
    /**
     * builds a radioButtonPanel. using the radioButton Panel only one of the
     * particleMotions among all the three planes can be viewed at a time.
     * @param channelGroup a <code>ChannelId[]</code> value
     */
    private void formRadioSetPanel() {
        JRadioButton[] radioButtons = new JRadioButton[3];
        for(int i = 0; i < radioButtons.length; i++) {
            radioButtons[i] = new JRadioButton(labelStrings[i]);
            radioButtons[i].setActionCommand(labelStrings[i]);
            radioButtons[i].addItemListener(new RadioButtonListener());
        }
        initialButton = radioButtons[0];
        ButtonGroup buttonGroup = new ButtonGroup();
        for(int counter = 0; counter < radioButtons.length; counter++) {
            buttonGroup.add(radioButtons[counter]);
            radioPanel.add(radioButtons[counter]);
        }
        radioPanel.setVisible(true);
    }
    
    /**
     * sets the initialRadioButton or checkBox checked.
     */
    private void setInitialButton() {
        initialButton.setSelected(true);
    }
    
    
    /**
     * sets the title of the Horizontal Border
     * @param name a <code>String</code> value
     */
    public void setHorizontalTitle(String name) {
        hTitleBorder.setTitle(name);
    }
    
    /**
     * sets the title of the vertical Border
     * @param name a <code>String</code> value
     */
    public void setVerticalTitle(String name) {
        vTitleBorder.setTitle(name);
    }
    
    /**
     *@returns true if the ParticleMotionThread has correctly initialized the display
     */
    public boolean initialized(){ return initialized; }
    
    public void add(NamedFilter filter){
        view.add(filter);
    }
    
    public void remove(NamedFilter filter){
        view.remove(filter);
    }
    
    public void setOriginal(boolean visible){
        view.setOriginal(visible);
    }
    
    private boolean initialized = false;
    
    public static final Integer PARTICLE_MOTION_LAYER = new Integer(2);
    
    protected AmpScaleMapper hAmpScaleMap, vAmpScaleMap;
    
    protected LeftTitleBorder vTitleBorder;
    
    protected BottomTitleBorder hTitleBorder;
    
    protected ParticleMotionView view;
    
    private JPanel particleDisplayPanel;
    
    private JPanel radioPanel;
    
    private AbstractButton initialButton;
    
    static Logger logger = Logger.getLogger(ParticleMotionDisplay.class);
    
    private static final String[] labelStrings = { DisplayUtils.NORTHEAST,
            DisplayUtils.UPNORTH,
            DisplayUtils.UPEAST};
    
    private class RadioButtonListener implements ItemListener {
        public void itemStateChanged(ItemEvent ae) {
            if(ae.getStateChange() == ItemEvent.SELECTED) {
                String orientation = ((AbstractButton)ae.getItem()).getText();
                view.setDisplayKey(orientation);
                if(orientation.equals(labelStrings[0])){
                    setVerticalTitle("North-South");
                    setHorizontalTitle("East-West");
                }else if(orientation.equals(labelStrings[1])){
                    setVerticalTitle("Up-Down");
                    setHorizontalTitle("North-South");
                }else{
                    setVerticalTitle("Up-Down");
                    setHorizontalTitle("East-West");
                }
                repaint();
            }
            repaint();
        }
    }
}// ParticleMotionDisplay
