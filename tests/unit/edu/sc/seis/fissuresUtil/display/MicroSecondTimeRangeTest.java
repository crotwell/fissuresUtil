/**
 * MicroSecondTimeRangeTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import junitx.extensions.EqualsHashCodeTestCase;
import edu.iris.Fissures.model.MicroSecondDate;

public class MicroSecondTimeRangeTest extends EqualsHashCodeTestCase{

    public MicroSecondTimeRangeTest(String name){
        super(name);
    }

    protected Object createInstance() throws Exception {
        return new MicroSecondTimeRange(new MicroSecondDate(0),
                                        new MicroSecondDate(5000));
    }

    protected Object createNotEqualInstance() throws Exception {
        return new MicroSecondTimeRange(new MicroSecondDate(5000),
                                        new MicroSecondDate(10000));
    }

}

