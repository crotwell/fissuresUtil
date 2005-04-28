package edu.sc.seis.fissuresUtil.display.borders;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;

public abstract class Border extends JComponent {

    public Border(int side, int order) {
        setSide(side);
        setOrder(order);
        borderFormats = createFormats();
    }

    public void setTitleAndTickColor(Color c) {
        color = c;
    }

    public void setOrder(int o) {
        this.order = o;
    }

    public void setSide(int s) {
        this.side = s;
        if(tickLength == 0 && labelTickLength == 0) {
            tickHeight = 0;
            tickWidth = 0;
            labelTickHeight = 0;
            labelTickWidth = 0;
        } else {
            if(side == TOP) {
                labelTickHeight = -labelTickLength;
                tickHeight = -tickLength;
            } else
                if(side == BOTTOM) {
                    labelTickHeight = labelTickLength;
                    tickHeight = tickLength;
                } else
                    if(side == RIGHT) {
                        labelTickWidth = labelTickLength;
                        tickWidth = tickLength;
                    } else
                        if(side == LEFT) {
                            labelTickWidth = -labelTickLength;
                            tickWidth = -tickLength;
                        } else
                            throw new IllegalArgumentException("side must be LEFT, RIGHT, BOTTOM, or TOP as defined in Border");
        }
        if(side == LEFT || side == RIGHT) {
            this.direction = VERTICAL;
            labelTickHeight = 0;
            tickHeight = 0;
        } else {
            this.direction = HORIZONTAL;
            labelTickWidth = 0;
            tickWidth = 0;
        }
        fixSize();
    }

    public void setClipTicks(double minTickVal, double maxTickVal) {
        this.minTickValue = minTickVal;
        this.maxTickValue = maxTickVal;
    }

    public abstract String getMaxLengthFormattedString();

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g.setColor(getBackground());
        g2d.fillRect(0, 0, getSize().width, getSize().height);
        paintBorder(g2d);
    }

    protected void paintBorder(Graphics2D g2d) {
        BorderFormat bf = getFormat(g2d);
        if(bf != null) {
            bf.draw(getRange(), g2d);
        }
    }

    public BorderFormat getFormat(Graphics2D g2d) {
        Iterator it = borderFormats.iterator();
        while(it.hasNext()) {
            BorderFormat cur = (BorderFormat)it.next();
            if(cur.willFit(getRange(), g2d)) {
                return cur;
            }
        }
        return null;
    }

    /**
     * Adds a title to this border. If you just want to title a border with an
     * unchanging string, pass a UnchangingTitleProvider to this method
     */
    public void add(TitleProvider tp) {
        titles.add(0, tp);
        fixSize();
    }

    protected void fixSize() {
        int tpHeight = 0;
        Iterator it = titles.iterator();
        while(it.hasNext()) {
            //TODO - calculate based on actual string, font metrics
            TitleProvider tp = (TitleProvider)it.next();
            tpHeight += tp.getTitleFont().getSize();
        }
        if(direction == HORIZONTAL) {
            int height = 25 + labelTickLength + tpHeight;
            if(labelTickLength == 0) {
                height = tpHeight + 10;
            }
            setMinimumSize(new Dimension(0, height));
            setPreferredSize(new Dimension(100, height));
        } else {
            int width = 6 * getMaxLengthFormattedString().length()
                    + labelTickLength + tpHeight;
            if(labelTickLength == 0) {
                width = tpHeight + 10;
            }
            setMinimumSize(new Dimension(width, 0));
            setPreferredSize(new Dimension(width, 100));
        }
    }

    private List titles = new ArrayList();

    protected abstract List createFormats();

    protected abstract UnitRangeImpl getRange();

    protected List borderFormats;

    protected int side, direction, order, labelTickHeight, labelTickWidth,
            tickHeight, tickWidth, type;

    protected double minTickValue = Double.NEGATIVE_INFINITY;

    protected double maxTickValue = Double.POSITIVE_INFINITY;

    public abstract class BorderFormat {

        /**
         * The division size determines the number of divisions that will be
         * created for a given unit range. The UnitRangeImpl returned by the
         * getRange method of implementing classes should use the same units as
         * whatever was used to get this division size If ticks per division = 5
         * then there will be 1 major tick followed by 4 minor ticks for every
         * division. If it's 10, there will be 1 major tick followed by 9 minor
         * ticks
         */
        public BorderFormat(double divisionSize, int ticksPerDivision) {
            divSize = divisionSize;
            ticksPerDiv = ticksPerDivision;
        }

        public boolean willFit(UnitRangeImpl range, Graphics2D g2d) {
            double numDivisions = (range.max_value - range.min_value) / divSize;
            double labelSize = getLimitingLabelSize(g2d);
            int numTicks = (int)Math.ceil(numDivisions * ticksPerDiv);
            double maxSize = getLimitingSize();
            return numTicks * tickPad < maxSize
                    && labelSize * numDivisions < maxSize;
        }

        private double getLimitingLabelSize(Graphics2D g2d) {
            FontMetrics fm = g2d.getFontMetrics();
            Rectangle2D stringBounds = fm.getStringBounds(getMaxString(), g2d);
            if(direction == VERTICAL)
                return stringBounds.getHeight();
            else
                return stringBounds.getWidth();
        }

        protected double getLimitingSize() {
            if(direction == VERTICAL)
                return getSize().getHeight();
            return getSize().getWidth();
        }

        public abstract String getMaxString();

        public void draw(UnitRangeImpl range, Graphics2D g2d) {
            Iterator it = titles.iterator();
            int cumulativeTitleHeight = 0;
            while(it.hasNext()) {
                TitleProvider tp = (TitleProvider)it.next();
                g2d.setFont(tp.getTitleFont());
                if(tp.getTitleColor() != null) {
                    g2d.setColor(tp.getTitleColor());
                } else {
                    g2d.setColor(color);
                }
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D titleBounds = fm.getStringBounds(tp.getTitle(), g2d);
                cumulativeTitleHeight += titleBounds.getHeight();
                if(direction == VERTICAL) {
                    double y = (int)(getSize().height / 2 + titleBounds.getWidth() / 2);
                    double x;
                    if(side == LEFT)
                        x = cumulativeTitleHeight - 5;
                    else
                        x = getWidth() - cumulativeTitleHeight
                                + (int)titleBounds.getHeight() - 5;
                    g2d.translate(x, y);
                    g2d.rotate(-Math.PI / 2);
                    g2d.drawString(tp.getTitle(), 0, 0);
                    g2d.rotate(Math.PI / 2);
                    g2d.translate(-x, -y);
                } else {
                    int x = (int)(getWidth() / 2 - titleBounds.getWidth() / 2);
                    int y;
                    if(side == TOP)
                        y = cumulativeTitleHeight - 5;
                    else
                        y = getHeight() - cumulativeTitleHeight
                                + (int)titleBounds.getHeight() - 5;
                    g2d.drawString(tp.getTitle(), x, y);
                }
            }
            g2d.setColor(color);
            if(range != null) {
                double numDivisions = (range.max_value - range.min_value)
                        / divSize;
                double pixelsPerLabelTick = getLimitingSize() / numDivisions;
                double pixelsPerMinorTick = pixelsPerLabelTick / ticksPerDiv;
                double labelValPerTick = divSize / ticksPerDiv;
                int numLabelTicks = (int)Math.ceil(numDivisions) + 1;
                //Create tick shapes
                GeneralPath labelTickShape = new GeneralPath();
                GeneralPath minorTickShape = new GeneralPath();
                float[] nextLabelPoint = getFirstPoint();
                double labelValue = getFirstLabelValue(range);
                for(int i = 0; i < numLabelTicks; i++) {
                    if((labelValue >= minTickValue && labelValue <= maxTickValue)) {
                        labelTickShape.moveTo(nextLabelPoint[0],
                                              nextLabelPoint[1]);
                        labelTickShape.lineTo(nextLabelPoint[0]
                                + labelTickWidth, nextLabelPoint[1]
                                + labelTickHeight);
                        float[] nextMinorPoint = getNextPoint((float)pixelsPerMinorTick,
                                                              nextLabelPoint);
                        double tempVal = labelValue;
                        for(int j = 0; j < ticksPerDiv - 1; j++) {
                            if(tempVal >= maxTickValue) {
                                break;
                            }
                            minorTickShape.moveTo(nextMinorPoint[0],
                                                  nextMinorPoint[1]);
                            minorTickShape.lineTo(nextMinorPoint[0] + tickWidth,
                                                  nextMinorPoint[1]
                                                          + tickHeight);
                            nextMinorPoint = getNextPoint((float)pixelsPerMinorTick,
                                                          nextMinorPoint);
                            tempVal += labelValPerTick;
                        }
                    }
                    nextLabelPoint = getNextPoint((float)pixelsPerLabelTick,
                                                  nextLabelPoint);
                    labelValue += divSize;
                }
                //Figure out how much to translate this generic tick shape to
                // match the actual time
                double[] translation = getTranslation(range);
                /*
                 * Casing the translation to an int removes tick jitter in
                 * borders where the values have changed, but the range hasn't.
                 * Since they move by int amounts, they move in lockstep. This
                 * introduces inaccuracy in translations that wouldn't naturally
                 * be an int. Be forewarned!
                 */
                g2d.translate((int)translation[0], (int)translation[1]);
                g2d.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
                g2d.draw(labelTickShape);
                g2d.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
                g2d.draw(minorTickShape);
                double value = getFirstLabelValue(range);
                nextLabelPoint = getFirstPoint();
                g2d.setFont(getFont());
                for(int i = 0; i < numLabelTicks; i++) {
                    if((value >= minTickValue && value <= maxTickValue)) {
                        label(getLabel(value),
                              nextLabelPoint,
                              g2d,
                              translation[1]);
                    }
                    value += divSize;
                    nextLabelPoint = getNextPoint((float)pixelsPerLabelTick,
                                                  nextLabelPoint);
                }
                g2d.translate(-(int)translation[0], -(int)translation[1]);
            }
        }

        private void label(String label,
                           float[] nextLabelPoint,
                           Graphics2D g2d,
                           double trans) {
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(label,
                                                                      g2d);
            float x, y;
            if(direction == VERTICAL) {
                y = nextLabelPoint[1] + (float)(bounds.getHeight() / 4);
                int xMod = labelTickLength + 2;
                if(side == LEFT)
                    x = nextLabelPoint[0] - xMod - (int)bounds.getWidth();
                else
                    x = nextLabelPoint[0] + xMod;
            } else {//Must be horizontal
                x = nextLabelPoint[0] - (int)(bounds.getWidth() / 2);
                if(side == TOP)
                    y = nextLabelPoint[1] - labelTickLength - 3;
                else
                    y = labelTickLength + (float)bounds.getHeight() - 3;
            }
            if(y + trans <= getSize().height
                    && y - bounds.getHeight() + trans >= 0)
                g2d.drawString(label, x, y);
        }

        //returns the position of the first label tick. 1st element is x, 2nd
        //element is y
        protected float[] getFirstPoint() {
            float[] point = new float[2];
            if(direction == VERTICAL) {
                if(side == LEFT) {
                    point[0] = (float)getSize().getWidth();
                } else {//Must be right
                    point[0] = 0;
                }
                if(order == ASCENDING)
                    point[1] = (float)getSize().getHeight();
                else
                    point[1] = 0;
            } else {//Must be horizontal
                if(order == ASCENDING)
                    point[0] = 0;
                else
                    point[0] = (float)getSize().getWidth();
                if(side == TOP) {
                    point[1] = (float)getSize().getHeight();
                } else {//Must be bottom
                    point[1] = 0;
                }
            }
            return point;
        }

        //returns the values in curPoint incremented by the increment value in
        //the direction of this border
        protected float[] getNextPoint(float increment, float[] curPoint) {
            float[] nextPoint = {curPoint[0], curPoint[1]};
            if(direction == VERTICAL) {
                if(order == ASCENDING)
                    nextPoint[1] -= increment;
                else
                    nextPoint[1] += increment;//Must be descending
            } else {//Must be horizontal
                if(order == ASCENDING)
                    nextPoint[0] += increment;
                else
                    nextPoint[0] -= increment;
            }
            return nextPoint;
        }

        /*
         * This value is the first value below the minimum that would fall
         * evenly on a major tick boundary. Therefore, we're labelling and
         * creating borders that are slightly larger and start before the actual
         * unit ranges passed in in most cases. Thats why the translation is
         * used in the draw step.
         */
        private double getFirstLabelValue(UnitRangeImpl r) {
            double min = r.min_value;
            double divisions = Math.floor(min / divSize);
            return divisions * divSize;
        }

        private double[] getTranslation(UnitRangeImpl r) {
            double[] translation = {0, 0};
            double range = r.max_value - r.min_value;
            double min = r.min_value;
            double val = getFirstLabelValue(r);
            double diff = Math.abs(val - min);
            double percentageDiff = diff / range;
            if(direction == VERTICAL) {
                double shiftAmount = percentageDiff * getSize().getHeight();
                if(order == ASCENDING)
                    translation[1] = shiftAmount;
                else
                    translation[1] = -shiftAmount;
            } else {//Must be horizontal
                double shiftAmount = percentageDiff * getSize().getWidth();
                if(order == ASCENDING)
                    translation[0] = -shiftAmount;
                else
                    translation[0] = shiftAmount;
            }
            return translation;
        }

        public double getDivSize() {
            return divSize;
        }

        public abstract String getLabel(double value);

        protected double divSize, ticksPerDiv;
    }

    /**
     * To be used with the side argument of the constructor to indicate which
     * side of a component this will be bordering
     */
    public static final int LEFT = 0, RIGHT = 1, TOP = 2, BOTTOM = 3;

    /**
     * To be used with the order argument of the constructor to indicate if the
     * border is going from low to high or high to low
     */
    public static final int ASCENDING = 0, DESCENDING = 4;

    //LEFT and RIGHT borders are VERTICAL, TOP and BOTTOM are HORIZONTAL
    protected static final int VERTICAL = 0, HORIZONTAL = 1;

    protected int tickPad = 3, labelTickLength = 10, tickLength = 4;

    protected Color color = Color.BLACK;
}