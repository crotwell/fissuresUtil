package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.registrar.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfSeismogramDC.DataCenter;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.fissuresUtil.chooser.SeisTimeFilterSelector;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.ArrayList;
import javax.swing.border.Border;
import org.apache.log4j.Category;
import edu.iris.Fissures.IfEvent.EventAccessOperations;


/**
 *
 * Created: Tue Jun 11 15:22:30 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class ParticleMotionDisplay extends JPanel implements TimeListener, AmpListener {

    public ParticleMotionDisplay(DataSetSeismogram datasetSeismogram,
                                 Registrar registrar,
                                 boolean advancedOption) {

        JFrame displayFrame = new JFrame();
        JPanel informationPanel = new JPanel();
        String message = " Please Wait ....For the Particle Motion Window";
        JLabel jLabel = new JLabel(message);
        JTextArea textArea = new JTextArea("LKJDLKJFDJFLKDJFLKDJFKLDJFLKDJFKLDJLKJDFL", 80,40);
        textArea.setVisible(true);
        informationPanel.setLayout(new BorderLayout());
        informationPanel.add(textArea, BorderLayout.CENTER);
        informationPanel.setSize(new java.awt.Dimension(500, 300));

        displayFrame.getContentPane().setLayout(new BorderLayout());
        displayFrame.getContentPane().add(jLabel, BorderLayout.CENTER);
        displayFrame.setSize(new java.awt.Dimension(500, 300));
        informationPanel.setVisible(true);
        displayFrame.pack();
        displayFrame.setVisible(true);
        this.registrar = registrar;
        particleDisplayPanel = new JLayeredPane();
        OverlayLayout overlayLayout = new OverlayLayout(particleDisplayPanel);

        radioPanel = new JPanel();
        this.setLayout(new BorderLayout());
        particleDisplayPanel.setLayout(overlayLayout);
        radioPanel.setLayout(new GridLayout(1, 0));

        view = new ParticleMotionView(this);
        view.setSize(new java.awt.Dimension(300, 300));
        particleDisplayPanel.add(view, PARTICLE_MOTION_LAYER);

        hAmpScaleMap = new AmpScaleMapper(50, 4, registrar);
        vAmpScaleMap = new AmpScaleMapper(50, 4, registrar);

        ScaleBorder scaleBorder = new ScaleBorder();
        scaleBorder.setBottomScaleMapper(hAmpScaleMap);
        scaleBorder.setLeftScaleMapper(vAmpScaleMap);
        hTitleBorder = new BottomTitleBorder("X - axis Title");
        vTitleBorder = new LeftTitleBorder("Y - axis Title");
        Border hVTitleBorder = BorderFactory.createCompoundBorder(hTitleBorder,
                                                                  vTitleBorder);
        Border bevelBorder = BorderFactory.createRaisedBevelBorder();
        Border bevelHVBorder = BorderFactory.createCompoundBorder(bevelBorder,
                                                                  hVTitleBorder);
        Border lowBevelBorder = BorderFactory.createLoweredBevelBorder();
        Border scaleBevelBorder = BorderFactory.createCompoundBorder(scaleBorder,
                                                                     lowBevelBorder);
        particleDisplayPanel.setBorder(BorderFactory.createCompoundBorder(bevelHVBorder,
                                                                          scaleBevelBorder));
        add(particleDisplayPanel, BorderLayout.CENTER);
        radioPanel.setVisible(false);
        add(radioPanel, BorderLayout.SOUTH);

        if(registrar != null) {
            registrar.addListener((AmpListener)this);
            registrar.addListener((TimeListener)this);
        }
        addComponentListener(new ComponentAdapter() {
                    public void componentResized(ComponentEvent e) {
                        resize();
                    }
                    public void componentShown(ComponentEvent e) {
                        resize();
                    }
                });

        ParticleMotionDisplayThread t = new ParticleMotionDisplayThread(datasetSeismogram,
                                                                        registrar,
                                                                        advancedOption,
                                                                        true,
                                                                        this);
        t.execute();
        initialized = t.getCompletion();
        displayFrame.dispose();
    }

    /**
     * returns an array of seismograms for the given ChannelId, startTime, endTime and dataCenter
     * @param startTime an <code>edu.iris.Fissures.Time</code> value
     * @param endTime an <code>edu.iris.Fissures.Time</code> value
     * @param channelId a <code>ChannelId</code> value
     * @param dataCenter a <code>DataCenter</code> value
     * @return a <code>LocalSeismogram[]</code> value
     * @exception edu.iris.Fissures.FissuresException if an error occurs
     */
    public LocalSeismogram[] retreiveSeismograms(Time startTime,
                                                 Time endTime,
                                                 ChannelId channelId,
                                                 DataCenter dataCenter) throws FissuresException {
        RequestFilter[] filters = {new RequestFilter(channelId,
                                                     startTime,
                                                     endTime
                                                    )};

        SeisTimeFilterSelector selector = new SeisTimeFilterSelector();
        return selector.getFromGivenFilters(dataCenter, filters);
    }



    public void updateAmp(AmpEvent event){
        hAmpScaleMap.setUnitRange(event.getAmp());
        vAmpScaleMap.setUnitRange(event.getAmp());
    }


    public void updateTime(edu.sc.seis.fissuresUtil.display.registrar.TimeEvent timeEvent) {
        view.updateTime();
    }

    /**
     * udpates the amplitude of the verticalScale.
     * @param r an <code>UnitRangeImpl</code> value
     */
    public void updateHorizontalAmpScale(UnitRangeImpl r) {
        hAmpScaleMap.setUnitRange(r);
        resize();
    }

    /**
     * sets the amplitude of he verticalScale to the given value.
     * @param r an <code>UnitRangeImpl</code> value
     */
    public void updateVerticalAmpScale(UnitRangeImpl r) {
        vAmpScaleMap.setUnitRange(r);
        resize();
    }

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
                                                      Registrar registrar) {
        this.registrar = registrar;
        boolean buttonPanel = this.displayButtonPanel;
        displayButtonPanel = false;
        ParticleMotionDisplayThread t = new ParticleMotionDisplayThread(datasetSeismogram,
                                                                        registrar,
                                                                        false,
                                                                        buttonPanel,
                                                                        this);
        t.execute();

    }

    /**
     * calculates the backAzimuthAngle and displays the azimuth angle and a sector surrouding
     * the azimuth angle.
     *
     * @param dataset an <code>edu.sc.seis.fissuresUtil.xml.DataSet</code> value
     * @param chanId a <code>ChannelId</code> value
     */
    public synchronized void displayBackAzimuth(edu.sc.seis.fissuresUtil.xml.DataSet dataset, ChannelId chanId) {
        EventAccessOperations cacheEvent = dataset.getEvent();
        Channel channel = dataset.getChannel(chanId);
        if(cacheEvent != null) {
            try {
                Origin origin = cacheEvent.get_preferred_origin();
                Station station = channel.my_site.my_station;
                double azimuth = SphericalCoords.azimuth(station.my_location.latitude,
                                                         station.my_location.longitude,
                                                         origin.my_location.latitude,
                                                         origin.my_location.longitude);
                double angle = 90 - azimuth;

                addAzimuthLine(angle);
                addSector(angle+5, angle-5);
            } catch(NoPreferredOrigin npoe) {
                logger.debug("no preferred origin");
            }
        }
    }

    /*
     * adds an azimuthLine to the display at angle of degrees
     * @param degrees a <code>double</code> value
     */
    public synchronized void addAzimuthLine(double degrees) {
        view.addAzimuthLine(degrees);
    }

    /**
     * adds a sector to the display
     * @param degreeone a <code>double</code> value
     * @param degreetwo a <code>double</code> value
     */
    public synchronized void addSector(double degreeone, double degreetwo) {
        view.addSector(degreeone, degreetwo);
    }

    /**
     * returns the ParticleMotionView corresponding to this ParticleMotionDisplay
     * @return a <code>ParticleMotionView</code> value
     */
    public synchronized ParticleMotionView getView() {
        return view;
    }

    /**
     * fires AmpRangeChangeEvent to all the registered Listeners
     * @param event an <code>AmpSyncEvent</code> value
     */
    public void shaleAmp(double shift, double scale) {
        registrar.shaleAmp(shift, scale);
    }

    /**
     * builds a checkBoxPanel. using the checkBox Panel the particleMotions of
     * all the three planes can be viewed simultaneouly by overlapping.
     * @param channelGroup a <code>ChannelId[]</code> value
     */
    public void formCheckBoxPanel(ChannelId[] channelGroup) {
        ArrayList arrayList = new ArrayList();
        for(int counter = 0; counter < channelGroup.length; counter++) {
            for(int subcounter = counter+1; subcounter < channelGroup.length; subcounter++) {
                String labelStr = DisplayUtils.getOrientationName(channelGroup[counter].channel_code)+"-"+
                    DisplayUtils.getOrientationName(channelGroup[subcounter].channel_code);
                JCheckBox radioButton = new JCheckBox(labelStr);
                radioButton.setActionCommand(labelStr);
                radioButton.addItemListener(new RadioButtonListener());
                arrayList.add(radioButton);
            }
        }
        JCheckBox[] checkBoxes = new JCheckBox[arrayList.size()];
        checkBoxes = (JCheckBox[])arrayList.toArray(checkBoxes);
        initialButton = checkBoxes[0];
        view.setDisplayKey(checkBoxes[0].getText());
        for(int counter = 0; counter < channelGroup.length; counter++) {
            radioPanel.add(checkBoxes[counter]);
        }
        radioPanel.setVisible(true);
    }

    /**
     * builds a radioButtonPanel. using the radioButton Panel only one of the
     * particleMotions among all the three planes can be viewed at a time.
     * @param channelGroup a <code>ChannelId[]</code> value
     */
    public void formRadioSetPanel(ChannelId[] channelGroup) {
        ArrayList arrayList = new ArrayList();
        for(int counter = 0; counter < channelGroup.length; counter++) {
            for(int subcounter = counter + 1; subcounter < channelGroup.length; subcounter++) {
                String labelStr = DisplayUtils.getOrientationName(channelGroup[counter].channel_code)+"-"+
                    DisplayUtils.getOrientationName(channelGroup[subcounter].channel_code);
                JRadioButton radioButton = new JRadioButton(labelStr);
                radioButton.setActionCommand(labelStr);
                radioButton.addItemListener(new RadioButtonListener());
                arrayList.add(radioButton);
            }
        }

        JRadioButton[] radioButtons = new JRadioButton[arrayList.size()];
        radioButtons = (JRadioButton[])arrayList.toArray(radioButtons);
        initialButton = radioButtons[0];

        ButtonGroup buttonGroup = new ButtonGroup();
        view.setDisplayKey(radioButtons[0].getText());
        for(int counter = 0; counter < channelGroup.length; counter++) {
            buttonGroup.add(radioButtons[counter]);
            radioPanel.add(radioButtons[counter]);
        }
        radioPanel.setVisible(true);
    }

    /**
     * sets the initialRadioButton or checkBox checked.
     */
    public void setInitialButton() {
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
    public boolean getInitializationStatus(){ return initialized; }

    private boolean initialized = false;

    public static final Integer PARTICLE_MOTION_LAYER = new Integer(2);

    protected AmpScaleMapper hAmpScaleMap, vAmpScaleMap;

    protected LeftTitleBorder vTitleBorder;

    protected BottomTitleBorder hTitleBorder;

    protected ParticleMotionView view;

    private Registrar registrar;

    private JLayeredPane particleDisplayPanel;

    private JPanel radioPanel;

    private boolean displayButtonPanel = true;

    private AbstractButton initialButton;

    static Category logger =
        Category.getInstance(ParticleMotionDisplay.class.getName());

    private class RadioButtonListener implements ItemListener {
        public void itemStateChanged(ItemEvent ae) {
            if(ae.getStateChange() == ItemEvent.SELECTED) {
                view.addDisplayKey(((AbstractButton)ae.getItem()).getText());
                setHorizontalTitle(view.getSelectedParticleMotion()[0].hseis.toString());
                setVerticalTitle(view.getSelectedParticleMotion()[0].vseis.toString());
                view.updateTime();
            } else if(ae.getStateChange() == ItemEvent.DESELECTED){
                view.removeDisplaykey(((AbstractButton)ae.getItem()).getText());
            }
            repaint();
        }
    }
}// ParticleMotionDisplay
