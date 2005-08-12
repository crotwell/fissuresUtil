package edu.sc.seis.fissuresUtil.display.particlemotion;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.SeismogramContainerListener;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramIterator;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.display.drawable.Drawable;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * @author hedx Created on Jun 23, 2005
 */
public class ParticleMotionDisplayDrawable implements Drawable,
        SeismogramContainerListener {

    public ParticleMotionDisplayDrawable(SeismogramDisplay parent,
                                         SeisContainer cont) {
        this(parent, cont, colorGenerator());
    }

    protected ParticleMotionDisplayDrawable(SeismogramDisplay parent,
                                            SeisContainer cont,
                                            Color color) {
        this(parent, cont, new ParticleMotionShape(parent, cont), color);
    }

    protected ParticleMotionDisplayDrawable(SeismogramDisplay parent,
                                            SeisContainer cont,
                                            ParticleMotionShape shape,
                                            Color color) {
        this.parent = parent;
        this.color = color;
        this.shape = shape;
        this.container = cont;
        setVisibility(defaultVisibility);
    }

    private static Color colorGenerator() {
        return new Color(genNumber(), genNumber(), genNumber());
    }

    private static float genNumber() {
        return (float)Math.random();
    }

    public void draw(Graphics2D canvas,
                     Dimension size,
                     TimeEvent currentTime,
                     AmpEvent currentAmp) {
       if(shape.update(currentTime, currentAmp, size)) {
            canvas.setColor(color);
            canvas.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
            canvas.draw(shape);
        } else {
            throw new RuntimeException("We are out of points to draw.");
        }
        if(firstPaint) {
            parent.repaint();
            firstPaint = false;
        }
        count++;
    }

    private double getVal(SeismogramIterator it,
                          double minAmp,
                          double maxAmp,
                          int size) {
        double itVal = ((QuantityImpl)it.next()).getValue();
        if(Double.isNaN(itVal)) {//Gap in trace
            itVal = Integer.MAX_VALUE;
        } else {
            itVal = Math.round(SimplePlotUtil.linearInterp(minAmp,
                                                           0,
                                                           maxAmp,
                                                           size,
                                                           itVal));
        }
        return itVal;
    }

    public boolean equals(SeismogramDisplay parent, SeisContainer container) {
        if(this.parent.equals(parent) && this.container.equals(container)) {
            return true;
        }
        return false;
    }

    public void setVisibility(boolean vis) {
        DataSetSeismogram[] seis = container.getSeismograms();
        if(vis) {
            parent.getTimeConfig().add(seis);
            parent.getAmpConfig().add(seis);
        } else {
            parent.getTimeConfig().remove(seis);
            parent.getAmpConfig().remove(seis);
        }
        if(visible != vis) {
            parent.repaint();
        }
        visible = vis;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color c) {
        this.color = c;
    }

    public SeisContainer getSeismogram() {
        return this.container;
    }

    public String getVName() {
        return container.getVert().getName();
    }

    public String getHName() {
        return container.getHorz().getName();
    }

    public void updateData() {
        parent.repaint();
    }

    public static void setDefaultVisibility(boolean visible) {
        defaultVisibility = visible;
    }

    public void setTopTitle(ParticleMotionDirectionBorder top) {
        this.top = top.addLabel(getHName(), this);
        this.top.setTitleColor(color);
    }

    public void setRightTitle(ParticleMotionDirectionBorder right) {
        this.right = right.addLabel(getVName(), this);
        this.right.setTitleColor(color);
    }

    public ParticleMotionSelfDrawableTitleProvider getTopTitle() {
        return top;
    }

    public ParticleMotionSelfDrawableTitleProvider getRightTitle() {
        return right;
    }

    public SeismogramDisplay getParent() {
        return parent;
    }

    public float[] getNumPoints() {
        return numPoints;
    }

    public void setNumPoints(float[] numPoints) {
        this.numPoints = numPoints;
    }

    public byte[] getNumTypes() {
        return numTypes;
    }

    public void setNumTypes(byte[] numTypes) {
        this.numTypes = numTypes;
    }

    private Color color;

    private boolean visible = true;

    List sectors = new LinkedList();

    private SeismogramDisplay parent;

    private SeisContainer container;

    private static final boolean DEFAULT_VISIBILITY = true;

    private static boolean defaultVisibility = true;

    private ParticleMotionSelfDrawableTitleProvider top, right;

    private int count = 0;

    private MicroSecondDate startTime, endTime;

    private ParticleMotionShape shape;

    private boolean firstPaint = true;

    private final int DRAGGED_LEFT = 0, DRAGGED_RIGHT = 1, ZOOMED_IN = 2,
            ZOOMED_OUT = 3;

    private TimeInterval ONE_MILLI_SECOND = new TimeInterval(1,
                                                             UnitImpl.MILLISECOND);

    private byte[] numTypes;

    private float[] numPoints;
}
