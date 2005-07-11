package edu.sc.seis.fissuresUtil.display.particlemotion;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.LinkedList;
import java.util.List;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SeismogramContainer;
import edu.sc.seis.fissuresUtil.display.SeismogramContainerFactory;
import edu.sc.seis.fissuresUtil.display.SeismogramContainerListener;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramIterator;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.display.drawable.Drawable;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
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
        this.parent = parent;
        this.container = cont;
        this.color = color;
        horiz = SeismogramContainerFactory.create(this, cont.getHorz());
        vert = SeismogramContainerFactory.create(this, cont.getVert());
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
        SeismogramIterator hiter = horiz.getIterator();
        SeismogramIterator viter = vert.getIterator();
        hiter.setTimeRange(currentTime.getTime(container.getHorz()));
        viter.setTimeRange(viter.getSeisTime());
        if(horiz.getIterator(currentTime.getTime()).numPointsLeft() <= 0
                || vert.getIterator(currentTime.getTime()).numPointsLeft() <= 0) {
            return;
        }
        canvas.setColor(color);
        canvas.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
        GeneralPath generalPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        boolean prevPointBad = true;//first point needs to be a move to
        SeismogramIterator hIt = horiz.getIterator(currentTime.getTime());
        UnitImpl horizUnit = hIt.getUnit();
        UnitRangeImpl horizRange = currentAmp.getAmp(horiz.getDataSetSeismogram())
                .convertTo(horizUnit);
        double hMin = horizRange.getMinValue();
        double hMax = horizRange.getMaxValue();
        SeismogramIterator vIt = vert.getIterator(currentTime.getTime());
        UnitImpl vertUnit = vIt.getUnit();
        UnitRangeImpl vertRange = currentAmp.getAmp(vert.getDataSetSeismogram())
                .convertTo(vertUnit);
        double vMin = vertRange.getMinValue();
        double vMax = vertRange.getMaxValue();
        while(hIt.hasNext() && vIt.hasNext()) {
            double hVal = getVal(hIt, hMin, hMax, size.height);
            double vVal = getVal(vIt, vMin, vMax, size.height);
            if(hVal == Integer.MAX_VALUE || vVal == Integer.MAX_VALUE) {
                prevPointBad = true;
            } else {
                vVal *= -1;
                vVal += size.height;
                if(prevPointBad) {
                    generalPath.moveTo((int)hVal, (int)vVal);
                    prevPointBad = false;
                } else {
                    generalPath.lineTo((int)hVal, (int)vVal);
                }
            }
        }
        canvas.draw(generalPath);
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

    private Color color;

    private boolean visible = true;

    List sectors = new LinkedList();

    private SeismogramDisplay parent;

    private SeisContainer container;

    private static final boolean DEFAULT_VISIBILITY = true;

    private List children;

    private SeismogramContainer horiz, vert;

    private TimeConfig tc;

    private AmpConfig ac;

    private boolean horizPlane;

    protected Shape s;

    private MicroSecondTimeRange timeRange;

    private static boolean defaultVisibility = true;

    private ParticleMotionSelfDrawableTitleProvider top, right;
}
