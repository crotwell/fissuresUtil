/**
 * Blockette1000Test.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.mseed;



    /**
 * DataHeaderTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

import edu.iris.Fissures.model.ISOTime;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import junit.framework.TestCase;
import edu.sc.seis.fissuresUtil.mseed.DataHeader;
import edu.sc.seis.fissuresUtil.mseed.Utility;


public class Blockette1000Test extends TestCase {

    public Blockette1000Test(String name) {
        super(name);
    }


    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWrite() throws Exception {
        Blockette1000 blockette = new Blockette1000();
        byte encoding = (byte)10;
        byte wordOrder = (byte)1;
        byte dataLength = (byte)12;
        byte reserved = (byte)0;
        short nextOffset = (short)(48+8);
        blockette.setEncodingFormat(encoding);
        blockette.setWordOrder(wordOrder);
        blockette.setDataRecordLength(dataLength);
        blockette.setReserved(reserved);

        ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        blockette.write(new DataOutputStream(bos), nextOffset);

        byte[] out = bos.toByteArray();
//        for (int i = 0; i < 20; i++) {
//            System.out.print(out[i]);
//        }
//        System.out.println("");
        assertEquals("blockette length", 8, out.length);
        assertEquals("type", (short)blockette.getType(), Utility.bytesToShort(out[0], out[1], false));
        assertEquals("type", nextOffset, Utility.bytesToShort(out[2], out[3], false));

        assertEquals("encoding", encoding, out[4]);
        assertEquals("wordOrder", wordOrder, out[5]);
        assertEquals("dataLength", dataLength, out[6]);
        assertEquals("reserved", reserved, out[7]);

    }


}

