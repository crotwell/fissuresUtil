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
        this.side = side;
        this.order = order;
        this.type = side + order;
        if(side == TOP) {
            labelTickHeight = -LABEL_TICK_LENGTH;
            tickHeight = -TICK_LENGTH;
        } else if(side == BOTTOM) {
            labelTickHeight = LABEL_TICK_LENGTH;
            tickHeight = TICK_LENGTH;
        } else if(side == RIGHT) {
            labelTickWidth = LABEL_TICK_LENGTH;
            tickWidth = TICK_LENGTH;
        } else if(side == LEFT) {
            labelTickWidth = -LABEL_TICK_LENGTH;
            tickWidth = -TICK_LENGTH;
        } else throw new IllegalArgumentException("side must be LEFT, RIGHT, BOTTOM, or TOP as defined in Border");
        if(side == LEFT || side == RIGHT) {
            this.direction = VERTICAL;
            labelTickHeight = 0;
            tickHeight = 0;
            setMinimumSize(new Dimension(65, 100));
        } else {
            this.direction = HORIZONTAL;
            labelTickWidth = 0;
            tickWidth = 0;
            setMinimumSize(new Dimension(100, 45));
        }
        borderFormats = createFormats();
    }

    public void setColor(Color c) {
        color = c;
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g.setColor(getBackground());
        g2d.fillRect(0, 0, getSize().width, getSize().height);
        g.setColor(color);
        paintBorder(g2d);
    }

    protected void paintBorder(Graphics2D g2d) {
        Iterator it = borderFormats.iterator();
        while(it.hasNext()) {
            BorderFormat cur = (BorderFormat)it.next();
            if(cur.willFit(getRange(), g2d)) {
                cur.draw(getRange(), g2d);
                return;
            }
        }
    }

    /**
     * Adds a title to this border. If you just want to title a border with an
     * unchanging string, pass a UnchangingTitleProvider to this method
     */
    public void add(TitleProvider tp) {
        titles.add(0, tp);
        Dimension curSize = getPreferredSize();
        if(direction == HORIZONTAL) {
            setPreferredSize(new Dimension(curSize.width, curSize.height + 10));
        } else {
            setPreferredSize(new Dimension(curSize.width + 10, curSize.height));
        }
    }

    private List titles = new ArrayList();

    protected abstract List createFormats();

    protected abstract UnitRangeImpl getRange();

    protected List borderFormats;

    protected int side, direction, order, labelTickHeight, labelTickWidth,
            tickHeight, tickWidth, type;

    protected boolean displayNegatives = true;

    protected abstract class BorderFormat {

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
            if(numTicks * TICK_PAD < maxSize
                    && labelSize * numDivisions < maxSize) return true;
            return false;
        }

        private double getLimitingLabelSize(Graphics2D g2d) {
            FontMetrics fm = g2d.getFontMetrics();
            Rectangle2D stringBounds = fm.getStringBounds(getMaxString(), g2d);
            if(direction == VERTICAL) return stringBounds.getHeight();
            else return stringBounds.getWidth();
        }

        private double getLimitingSize() {
            if(direction == VERTICAL) return getSize().getHeight();
            return getSize().getWidth();
        }

        public abstract String getMaxString();

        public void draw(UnitRangeImpl range, Graphics2D g2d) {
            FontMetrics fm = g2d.getFontMetrics();
            Iterator it = titles.iterator();
            int cumulativeTitleHeight = 0;
            while(it.hasNext()) {
                TitleProvider tp = (TitleProvider)it.next();
                g2d.setFont(tp.getFont());
                Rectangle2D titleBounds = fm.getStringBounds(tp.getTitle(), g2d);
                cumulativeTitleHeight += titleBounds.getHeight();
                if(direction == VERTICAL) {
                    double y = (int)(getSize().height / 2 + titleBounds.getWidth() / 2);
                    double x;
                    if(side == LEFT) x = cumulativeTitleHeight;
                    else x = getWidth() - cumulativeTitleHeight;
                    g2d.translate(x, y);
                    g2d.rotate(-Math.PI / 2);
                    g2d.drawString(tp.getTitle(), 0, 0);
                    g2d.rotate(Math.PI / 2);
                    g2d.translate(-x, -y);
                } else {
                    int x = (int)(getWidth() / 2 - titleBounds.getWidth() / 2);
                    int y;
                    if(side == TOP) y = cumulativeTitleHeight;
                    else y = getHeight() - cumulativeTitleHeight;
                    g2d.drawString(tp.getTitle(), x, y);
                }
            }
            if(range != null) {
                double numDivisions = (range.max_value - range.min_value)
                        / divSize;
                double pixelsPerLabelTick = getLimitingSize() / numDivisions;
                double pixelsPerMinorTick = pixelsPerLabelTick / ticksPerDiv;
                int numLabelTicks = (int)Math.ceil(numDivisions) + 1;
                //Create tick shapes
                GeneralPath labelTickShape = new GeneralPath();
                GeneralPath minorTickShape = new GeneralPath();
                float[] nextLabelPoint = getFirstPoint();
                double labelValue = getFirstLabelValue(range);
                double labelMaxVal = getLastLabelValue(range);
                for(int i = 0; i < numLabelTicks; i++) {
                    if(displayNegatives
                            || (labelValue >= 0 && labelValue <= labelMaxVal)) {
                        labelTickShape.moveTo(nextLabelPoint[0],
                                              nextLabelPoint[1]);
                        labelTickShape.lineTo(nextLabelPoint[0]
                                + labelTickWidth, nextLabelPoint[1]
                                + labelTickHeight);
                        float[] nextMinorPoint = getNextPoint((float)pixelsPerMinorTick,
                                                              nextLabelPoint);
                        for(int j = 0; j < ticksPerDiv - 1; j++) {
                            minorTickShape.moveTo(nextMinorPoint[0],
                                                  nextMinorPoint[1]);
                            minorTickShape.lineTo(nextMinorPoint[0] + tickWidth,
                                                  nextMinorPoint[1]
                                                          + tickHeight);
                            nextMinorPoint = getNextPoint((float)pixelsPerMinorTick,
                                                          nextMinorPoint);
                        }
                    }
                    nextLabelPoint = getNextPoint((float)pixelsPerLabelTick,
                                                  nextLabelPoint);
                    labelValue += divSize;
                }
                //Figure out how much to translate this generic tick shape to
                // match
                //the actual time
                double[] translation = getTranslation(range);
                //Casing the translation to an int removes tick jitter in
                // borders
                //where the values have changed, but the range hasn't. Since
                // they
                //move by an integral amount, they move in lockstep. This
                // introduces
                //inaccuracy in translations that wouldn't naturally be an int.
                //Be forewarned!
                g2d.translate((int)translation[0], (int)translation[1]);
                g2d.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
                g2d.draw(labelTickShape);
                g2d.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
                g2d.draw(minorTickShape);
                double value = getFirstLabelValue(range);
                nextLabelPoint = getFirstPoint();
                for(int i = 0; i < numLabelTicks; i++) {
                    if(displayNegatives || value >= 0) {
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
                int xMod = LABEL_TICK_LENGTH + 2;
                if(side == LEFT) x = nextLabelPoint[0] - xMod
                        - (int)bounds.getWidth();
                else x = nextLabelPoint[0] + xMod;
            } else {//Must be horizontal
                x = nextLabelPoint[0] - (int)(bounds.getWidth() / 2);
                if(side == TOP) y = nextLabelPoint[1] - LABEL_TICK_LENGTH - 3;
                else y = LABEL_TICK_LENGTH + (float)bounds.getHeight() - 3;
            }
            if(y + trans <= getSize().height
                    && y - bounds.getHeight() + trans >= 0) g2d.drawString(label,
                                                                           x,
                                                                           y);
        }

        //returns the position of the first label tick. 1st element is x, 2nd
        //element is y
        private float[] getFirstPoint() {
            float[] point = new float[2];
            if(direction == VERTICAL) {
                if(side == LEFT) {
                    point[0] = (float)getSize().getWidth();
                } else {//Must be right
                    point[0] = 0;
                }
                if(order == ASCENDING) point[1] = (float)getSize().getHeight();
                else point[1] = 0;
            } else {//Must be horizontal
                if(order == ASCENDING) point[0] = 0;
                else point[0] = (float)getSize().getWidth();
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
        private float[] getNextPoint(float increment, float[] curPoint) {
            float[] nextPoint = {curPoint[0], curPoint[1]};
            if(direction == VERTICAL) {
                if(order == ASCENDING) nextPoint[1] -= increment;
                else nextPoint[1] += increment;//Must be descending
            } else {//Must be horizontal
                if(order == ASCENDING) nextPoint[0] += increment;
                else nextPoint[0] -= increment;
            }
            return nextPoint;
        }

        //This value is the first value below the minimum that would fall
        // evenly
        //on a major tick boundary. Therefore, we're labelling and creating
        // borders
        //that are slightly larger and start before the actual unit ranges
        // passed
        //in in most cases. Thats why the translation is used in the draw step.
        private double getFirstLabelValue(UnitRangeImpl r) {
            double min = r.min_value;
            double divisions = Math.floor(min / divSize);
            return divisions * divSize;
        }

        private double getLastLabelValue(UnitRangeImpl r) {
            double max = r.max_value;
            double divisions = Math.floor(max / divSize);
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
                if(order == ASCENDING) translation[1] = shiftAmount;
                else translation[1] = -shiftAmount;
            } else {//Must be horizontal
                double shiftAmount = percentageDiff * getSize().getWidth();
                if(order == ASCENDING) translation[0] = -shiftAmount;
                else translation[0] = shiftAmount;
            }
            return translation;
        }

        public double getDivSize() {
            return divSize;
        }

        public abstract String getLabel(double value);

        protected double divSize;

        private double ticksPerDiv;
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
    private static final int VERTICAL = 0, HORIZONTAL = 1;

    private static final int TICK_PAD = 3, LABEL_TICK_LENGTH = 10,
            TICK_LENGTH = 4;

    private Color color = Color.BLACK;
}