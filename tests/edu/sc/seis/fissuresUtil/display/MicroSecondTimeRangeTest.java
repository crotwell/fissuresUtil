/**
 * MicroSecondTimeRangeTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.MicroSecondDate;
import junitx.extensions.EqualsHashCodeTestCase;

public class MicroSecondTimeRangeTest extends EqualsHashCodeTestCase{

    public MicroSecondTimeRangeTest(String name){
        super(name);
    }

    protected Object createInstance() throws Exception {
        return new MicroSecondTimeRange(new MicroSecondDate(),
                                        new MicroSecondDate(5000));
    }

    protected Object createNotEqualInstance() throws Exception {
        return new MicroSecondTimeRange(new MicroSecondDate(5000),
                                        new MicroSecondDate(10000));
    }

}

