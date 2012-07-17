package edu.sc.seis.fissuresUtil.display.border;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import junit.framework.TestCase;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.borders.Border;
import edu.sc.seis.fissuresUtil.display.borders.UnitRangeBorder;

/**
 * @author groves Created on Apr 28, 2005
 */
public class UnitRangeBorderTest extends TestCase {

    public void testNumDivs() throws Exception {
        UnitRangeBorder urb = new UnitRangeBorder(Border.LEFT,
                                                  Border.ASCENDING,
                                                  "HELLO");
        urb.setSize(100, 100);
        Graphics2D g2d = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB).createGraphics();
        for(int i = 1; i < 10000; i++) {
            UnitRangeImpl r = new UnitRangeImpl(-i, i, UnitImpl.COUNT);
            urb.setRange(r);
            Border.BorderFormat bf = urb.getFormat(g2d);
            double numDivs = (r.getMaxValue() - r.getMinValue())
                    / bf.getDivSize();
            assertTrue(r.toString() + " " + bf + " " + numDivs, numDivs > 3);
        }
    }
}
