package edu.sc.seis.fissuresUtil.database.seismogram;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.ISOTime;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import junit.framework.TestCase;

public class DatabasePopulationReportTest extends TestCase {

    public void setUp() {
        report = new RT130Report();
    }

    public void testAddMSeedSeismogram() {
        report.addMSeedSeismogram();
        report.addMSeedSeismogram();
        report.addMSeedSeismogram();
        report.addMSeedSeismogram();
        report.addMSeedSeismogram();
        assertEquals(5, report.getNumMSeedFiles());
    }

    public void testAddSacSeismogram() {
        report.addSacSeismogram();
        report.addSacSeismogram();
        report.addSacSeismogram();
        report.addSacSeismogram();
        report.addSacSeismogram();
        assertEquals(5, report.getNumSacFiles());
    }

    public void testGetNumStations() {
        Channel channel1 = MockChannel.createChannel();
        Channel channel2 = MockChannel.createOtherNetChan();
        ISOTime beginTime = new ISOTime(2005, 115, 12, 30, (float)15.0);
        ISOTime endTime = new ISOTime(2005, 115, 13, 30, (float)15.0);
        report.addRefTekSeismogram(channel1,
                                   beginTime.getDate(),
                                   endTime.getDate());
        report.addRefTekSeismogram(channel2,
                                   beginTime.getDate(),
                                   endTime.getDate());
        assertEquals(2, report.getNumStations());
    }

    public void testGetNumChannels() {
        Channel channel1 = MockChannel.createChannel();
        Channel channel2 = MockChannel.createOtherNetChan();
        ISOTime beginTime = new ISOTime(2005, 115, 12, 30, (float)15.0);
        ISOTime endTime = new ISOTime(2005, 115, 13, 30, (float)15.0);
        report.addRefTekSeismogram(channel1,
                                   beginTime.getDate(),
                                   endTime.getDate());
        report.addRefTekSeismogram(channel2,
                                   beginTime.getDate(),
                                   endTime.getDate());
        assertEquals(2, report.getNumChannels());
    }

    public void testGetNumIncontiguousChannels() {
        Channel channel1 = MockChannel.createChannel();
        Channel channel2 = MockChannel.createOtherNetChan();
        ISOTime beginTime1 = new ISOTime(2005, 115, 12, 30, (float)15.0);
        ISOTime endTime1 = new ISOTime(2005, 115, 13, 30, (float)15.0);
        ISOTime beginTime2 = new ISOTime(2005, 115, 13, 30, (float)15.0);
        ISOTime endTime2 = new ISOTime(2005, 115, 14, 30, (float)15.0);
        ISOTime beginTime3 = new ISOTime(2005, 115, 15, 30, (float)15.0);
        ISOTime endTime3 = new ISOTime(2005, 115, 16, 30, (float)15.0);
        report.addRefTekSeismogram(channel1,
                                   beginTime1.getDate(),
                                   endTime1.getDate());
        report.addRefTekSeismogram(channel1,
                                   beginTime2.getDate(),
                                   endTime2.getDate());
        report.addRefTekSeismogram(channel2,
                                   beginTime1.getDate(),
                                   endTime1.getDate());
        report.addRefTekSeismogram(channel2,
                                   beginTime3.getDate(),
                                   endTime3.getDate());
        assertEquals(1, report.getNumIncontiguousChannels());
    }

    public void testPrintReport() {
        report.addMSeedSeismogram();
        report.addMSeedSeismogram();
        report.addMSeedSeismogram();
        report.addSacSeismogram();
        report.addSacSeismogram();
        report.addSacSeismogram();
        report.addProblemFile("File location01", "Problem description01");
        report.addProblemFile("File location02", "Problem description02");
        report.addProblemFile("File location03", "Problem description03");
        Channel channel1 = MockChannel.createChannel();
        Channel channel2 = MockChannel.createOtherNetChan();
        ISOTime beginTime1 = new ISOTime(2005, 115, 12, 30, (float)15.0);
        ISOTime endTime1 = new ISOTime(2005, 115, 13, 30, (float)15.0);
        ISOTime beginTime2 = new ISOTime(2005, 115, 13, 30, (float)15.0);
        ISOTime endTime2 = new ISOTime(2005, 115, 14, 30, (float)15.0);
        ISOTime beginTime3 = new ISOTime(2005, 115, 15, 30, (float)15.0);
        ISOTime endTime3 = new ISOTime(2005, 115, 16, 30, (float)15.0);
        report.addRefTekSeismogram(channel1,
                                   beginTime1.getDate(),
                                   endTime1.getDate());
        report.addRefTekSeismogram(channel1,
                                   beginTime2.getDate(),
                                   endTime2.getDate());
        report.addRefTekSeismogram(channel2,
                                   beginTime1.getDate(),
                                   endTime1.getDate());
        report.addRefTekSeismogram(channel2,
                                   beginTime3.getDate(),
                                   endTime3.getDate());
        report.printReport();
    }

    public void testMakeReportImage() {
        report.addMSeedSeismogram();
        report.addMSeedSeismogram();
        report.addMSeedSeismogram();
        report.addSacSeismogram();
        report.addSacSeismogram();
        report.addSacSeismogram();
        report.addProblemFile("File location01", "Problem description01");
        report.addProblemFile("File location02", "Problem description02");
        report.addProblemFile("File location03", "Problem description03");
        Channel channel1 = MockChannel.createChannel();
        Channel channel2 = MockChannel.createOtherNetChan();
        ISOTime beginTime1 = new ISOTime(2005, 115, 12, 30, (float)15.0);
        ISOTime endTime1 = new ISOTime(2005, 115, 13, 30, (float)15.0);
        ISOTime beginTime2 = new ISOTime(2005, 115, 13, 30, (float)15.0);
        ISOTime endTime2 = new ISOTime(2005, 115, 14, 30, (float)15.0);
        ISOTime beginTime3 = new ISOTime(2005, 115, 15, 30, (float)15.0);
        ISOTime endTime3 = new ISOTime(2005, 115, 16, 30, (float)15.0);
        report.addRefTekSeismogram(channel1,
                                   beginTime1.getDate(),
                                   endTime1.getDate());
        report.addRefTekSeismogram(channel1,
                                   beginTime2.getDate(),
                                   endTime2.getDate());
        report.addRefTekSeismogram(channel2,
                                   beginTime1.getDate(),
                                   endTime1.getDate());
        report.addRefTekSeismogram(channel2,
                                   beginTime3.getDate(),
                                   endTime3.getDate());
        report.makeReportImage();
    }
    
    private RT130Report report;
}
