package edu.sc.seis.fissuresUtil.cache;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.mockFissures.IfSeismogramDC.NamedMockSeisDC;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public class NSSeismogramDCTest extends TestCase {
	public void testWithEveryOtherFailure() throws NotFound, CannotProceed,
			InvalidName {
		BasicConfigurator.configure();
		FissuresNamingService fns = new FissuresNamingService(new Properties());
		NSSeismogramDC nsSeis = new NSSeismogramDC(FissuresNamingService.MOCK_DNS,
				NamedMockSeisDC.SOMETIMES_AVAIL_UNKNOWN, fns);
		nsSeis.available_data(new RequestFilter[0]);
	}
}
