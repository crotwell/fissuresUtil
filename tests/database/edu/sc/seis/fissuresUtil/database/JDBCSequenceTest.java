package edu.sc.seis.fissuresUtil.database;

import java.sql.SQLException;
import junit.framework.TestCase;

public class JDBCSequenceTest extends JDBCTearDown {

    public void testNext() throws SQLException{
        next5(createTestSeq());
    }

    private int next5(JDBCSequence seq) throws SQLException{
        int initialVal = seq.next() + 1;
        for (int i = initialVal; i < initialVal + 4; i++){
            assertEquals(i, seq.next());
        }
        return initialVal + 5;
    }

    public void testDoubleCreate() throws SQLException{
        int valueAfterInitialRun = next5(createTestSeq());
        int valueAfterSecondRun = next5(createTestSeq());
        assertEquals(valueAfterInitialRun, valueAfterSecondRun -5);
    }

    private JDBCSequence createTestSeq() throws SQLException{
        return new JDBCSequence(ConnMgr.createConnection(), "testSeq");
    }

    public void testNextWhileOtherThreadIsIncrementing()throws SQLException{
        SequenceGrabber[] grabbers = new SequenceGrabber[5];
        for (int i = 0; i < grabbers.length; i++) {
            grabbers[i] = new SequenceGrabber();
        }
        for (int i = 0; i < grabbers.length; i++) {
            grabbers[i].start();
        }
        for (int i = 0; i < grabbers.length; i++) {
            try {
                grabbers[i].join();
            } catch (InterruptedException e) {}//try to join again if interrupted
        }

        for (int i = 0; i < grabbers.length; i++) {
            //this tests that a single thread hasn't gotten the same value twice
            for (int j = 0; j < grabbers[i].results.length; j++) {
                for(int k = j + 1; k < grabbers[i].results.length ; k++){
                    assertFalse(grabbers[i].results[j] == grabbers[i].results[k]);
                }
            }
            //this tests that all threads have unique values
            for (int j = i+1; j < grabbers.length; j++) {
                for(int k = 0; k < grabbers[i].results.length; k++){
                    assertFalse(grabbers[i].results[k] == grabbers[j].results[k]);
                }
            }
        }
    }

    private class SequenceGrabber extends Thread{
        public SequenceGrabber() throws SQLException{
            seq = createTestSeq();
        }

        public void run() {
            for (int i = 0; i < results.length; i++){
                try {
                    results[i] = seq.next();
                } catch (SQLException e) { throw new RuntimeException(e);}
            }
        }

        JDBCSequence seq;

        int[] results = new int[50];
    }
}
