package edu.sc.seis.fissuresUtil.display.borders;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Iterator;

import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.LayoutData;
import edu.sc.seis.fissuresUtil.display.registrar.LayoutEvent;

/**
 * @author groves Created on Apr 21, 2005
 */
public class TriangleBorder extends NoTickBorder {

    public TriangleBorder(RecordSectionDisplay rsd, int side, int order,
            Color[] colors) {
        super(side, order);
        //borders get spacing for label ticks, so even though we're not using
        // ticks here we'll use the label tick length to get space for the
        // triangles
        if(direction == Border.HORIZONTAL) {
            labelTickLength = 1;
        } else {
            labelTickLength = 25;
        }
        fixSize();
        this.rsd = rsd;
        this.colors = colors;
    }

    protected BorderFormat createNoTickFormat() {
        return new TriangleFormat();
    }

    private class TriangleFormat extends BorderFormat {

        public TriangleFormat() {
            super(0, 0);
        }

        public String getMaxString() {
            return null;
        }

        public String getLabel(double value) {
            return null;
        }

        public void draw(UnitRangeImpl range, Graphics2D g2d) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);
            double size = getLimitingSize();//total amount of space to fill
            LayoutEvent ev = rsd.getLayoutConfig().getLayout();
            Iterator it = ev.iterator();
            int i = 0;
            float[] firstPoint = getFirstPoint();
            while(it.hasNext()) {
                LayoutData cur = (LayoutData)it.next();
                float centerPoint = (float)((cur.getStart() + (cur.getEnd() - cur.getStart()) / 2) * size);
                float[] point = getNextPoint(centerPoint, firstPoint);
                if(side == LEFT) {
                    point[0] -= 10;
                } else if(side == RIGHT) {
                    point[0] += 10;
                } else if(side == BOTTOM) {
                    point[1] += 25;
                }
                drawTriangle(g2d,
                             colors[(i++ % colors.length)],
                             (int)point[0],
                             (int)point[1] - TRIANGLE_WIDTH);
            }
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            super.draw(range, g2d);
        }
    }

    public static void drawTriangle(Graphics2D g2d, Color c) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        drawTriangle(g2d, c, TRIANGLE_WIDTH / 2, TRIANGLE_WIDTH / 2);
    }

    public static void drawTriangle(Graphics2D g2d,
                                    Color c,
                                    int centerX,
                                    int centerY) {
        g2d.setColor(c);
        int[][] triangle = makeTriangle(centerX, centerY);
        g2d.fillPolygon(triangle[0], triangle[1], 3);
        g2d.setColor(new Color(64, 44, 127));//REV Purple
        g2d.drawPolygon(triangle[0], triangle[1], 3);
    }

    private static int[][] makeTriangle(int centerX, int centerY) {
        int halfWidth = TRIANGLE_WIDTH / 2;
        int[] x = {centerX - halfWidth, centerX, centerX + halfWidth};
        int[] y = {centerY + halfWidth,
                   centerY - halfWidth,
                   centerY + halfWidth};
        return new int[][] {x, y};
    }

    private Color[] colors;

    public static final int TRIANGLE_WIDTH = 15;

    RecordSectionDisplay rsd;
}
