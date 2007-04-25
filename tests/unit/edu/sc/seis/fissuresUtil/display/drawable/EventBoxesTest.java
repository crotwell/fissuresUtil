package edu.sc.seis.fissuresUtil.display.drawable;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.SeismicPhase;
import edu.sc.seis.TauP.TauModel;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.PlottableDisplay;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;

/**
 * @author groves Created on May 9, 2005
 */
public class EventBoxesTest extends TestCase {

    private static final double TWO_HOURS = 2 * 60 * 60d;

    public void testBoxOnOneLine() {
        assertSpansRow((Rectangle)(createBox(0, TWO_HOURS).getBoxes().get(0)));
    }

    public void testBoxesOnTwoLines() {
        List boxes = createBox(.75, TWO_HOURS).getBoxes();
        assertOnRightEdge((Rectangle)(boxes.get(0)));
        assertOnLeftEdge((Rectangle)(boxes.get(1)));
    }

    public void testBoxesOnThreeLines() {
        List boxes = createBox(0, TWO_HOURS * 3).getBoxes();
        assertEquals(3, boxes.size());
        Iterator it = boxes.iterator();
        while(it.hasNext()) {
            assertSpansRow((Rectangle)it.next());
        }
    }

    /*
     * For a very close station 4kmps takes less than 5 mintes to arrive so we
     * set the box to be 5 minutes in size
     */
    public void testVeryCloseStation() {
        List boxes = createBox(0, 60).getBoxes();
        assertEquals(1, boxes.size());
        Rectangle box = (Rectangle)(boxes.get(0));
        assertEquals(defaultPlottableDisplayWidth / 24, box.width);
    }

    /*
     * since there are two hours per line, offsetting 1.97 hours makes the 5
     * minute box wrap
     */
    public void testWrappingCloseStation() {
        List boxes = createBox(1.97, 60).getBoxes();
        assertEquals(2, boxes.size());
        Rectangle firstBox = (Rectangle)(boxes.get(0));
        Rectangle secondBox = (Rectangle)(boxes.get(1));
        assertOnRightEdge(firstBox);
        assertOnLeftEdge(secondBox);
        assertTrue(firstBox.y != secondBox.y);
    }

    private void assertSpansRow(Rectangle box) {
        assertOnLeftEdge(box);
        assertOnRightEdge(box);
    }

    private void assertOnLeftEdge(Rectangle box) {
        assertEquals(PlottableDisplay.LABEL_X_SHIFT, box.x);
    }

    private void assertOnRightEdge(Rectangle box) {
        assertEquals(PlottableDisplay.LABEL_X_SHIFT
                + defaultPlottableDisplayWidth, box.x + box.width);
    }

    private static int defaultPlottableDisplayWidth = new PlottableDisplay().getRowWidth();

    private static EventBoxes createBox(double hoursDisplayOffsetFromOriginTime,
                                        double arrivalTimeInSeconds) {
        CacheEvent ev = MockEventAccessOperations.createEvent();
        PlottableDisplay disp = new PlottableDisplay();
        Arrival arr = new Arrival(new SeismicPhase("3kmps", TauPUtil.getTauPUtil().getTauModel()),
                                  arrivalTimeInSeconds,
                                  1d,
                                  1d,
                                  1,
                                  "3kmps",
                                  "HOO-AH",
                                  1d);
        Arrival[][] arrivals = new Arrival[1][];
        arrivals[0] = new Arrival[] {arr};
        TimeInterval shift = new TimeInterval(hoursDisplayOffsetFromOriginTime,
                                              UnitImpl.HOUR);
        MicroSecondDate plottableStartTime = new MicroSecondDate(ev.getOrigin().origin_time).subtract(shift);
        disp.setPlottable(new Plottable[0],
                          "test",
                          "NORTH",
                          plottableStartTime,
                          null,
                          new EventAccessOperations[] {ev},
                          arrivals);
        return new EventBoxes(disp, ev, arrivals[0]);
    }
}
