/**
 * FissuresNamingServiceTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.namingService;

import edu.sc.seis.fissuresUtil.simple.Initializer;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;

public class FissuresNamingServiceTest extends TestCase{
    public FissuresNamingServiceTest(String name){ super(name); }

    public void testGetAllNetDC(){
        BasicConfigurator.configure();
        Initializer.init(new String[0]);
        FissuresNamingService fisName = Initializer.getNS();
        assertEquals(3, fisName.getAllNetworkDC().length);
    }
}

