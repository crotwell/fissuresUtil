package edu.sc.seis.fissuresUtil.flow.querier;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.flow.tester.ANDTester;
import edu.sc.seis.fissuresUtil.flow.tester.TestResult;
import edu.sc.seis.fissuresUtil.flow.tester.Tester;
import edu.sc.seis.fissuresUtil.flow.tester.event.MagnitudeValueTester;
import edu.sc.seis.fissuresUtil.flow.tester.model.AreaTester;
import edu.sc.seis.fissuresUtil.flow.tester.model.DepthTester;
import edu.sc.seis.fissuresUtil.flow.tester.model.TimeTester;

public class EventFinderQuery implements Tester {

    /**
     * Emulates a call to an event finder for o. Currently only tests magnitude
     * value, time, depth and area
     */
    public TestResult test(Object o) {
        return new ANDTester(new Tester[] {magTester,
                                           timeTester,
                                           depth,
                                           areaTester}).test(o);
    }

    public Area getArea() {
        return areaTester.getArea();
    }

    public void setArea(Area a) {
        this.areaTester = new AreaTester(a);
    }

    public float getMinMag() {
        return magTester.getMin();
    }

    public void setMinMag(float min) {
        magTester = new MagnitudeValueTester(min, magTester.getMax());
    }

    public float getMaxMag() {
        return magTester.getMax();
    }

    public void setMaxMag(float max) {
        magTester = new MagnitudeValueTester(magTester.getMin(), max);
    }

    public MicroSecondTimeRange getTime() {
        return timeTester.getRange();
    }

    public void setTime(MicroSecondTimeRange range) {
        timeTester = new TimeTester(range);
    }

    public Quantity getMinDepthQuantity() {
        return depth.getMin();
    }

    /**
     * @return - minimum depth in kilometers of events matched by this query
     */
    public double getMinDepth() {
        return depth.getMin().getValue(UnitImpl.KILOMETER);
    }

    /**
     * @param min -
     *            new minimum depth in kilometers of events matched by this
     *            query
     */
    public void setMinDepth(double min) {
        depth = new DepthTester(new QuantityImpl(min, UnitImpl.KILOMETER),
                                depth.getMax());
    }

    public Quantity getMaxDepthQuantity() {
        return depth.getMax();
    }

    /**
     * @return - maximum depth in kilometers of events matched by this query
     */
    public double getMaxDepth() {
        return depth.getMax().getValue(UnitImpl.KILOMETER);
    }

    /**
     * @param max -
     *            new maximum depth in kilometers of events matched by this
     *            query
     */
    public void setMaxDepth(double max) {
        depth = new DepthTester(depth.getMin(),
                                new QuantityImpl(max, UnitImpl.KILOMETER));
    }

    public String[] getTypes() {
        return magTypes;
    }

    public String[] getCatalogs() {
        return catalogs;
    }
    public void setCatalogs(String[] catalogs) {
        this.catalogs = catalogs;
    }

    public String[] getContributors() {
        return contributors;
    }
    public void setContributors(String[] contributors) {
        this.contributors = contributors;
    }

    private AreaTester areaTester = new AreaTester();

    private DepthTester depth = new DepthTester();

    private TimeTester timeTester = new TimeTester();

    private MagnitudeValueTester magTester = new MagnitudeValueTester();

    private String[] magTypes = new String[] {"%"};

    private String[] catalogs = new String[0];

    private String[] contributors = new String[0];

}
