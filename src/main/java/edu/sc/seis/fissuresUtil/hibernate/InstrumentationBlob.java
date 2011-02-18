package edu.sc.seis.fissuresUtil.hibernate;

import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.InstrumentationHelper;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.simple.Initializer;


public class InstrumentationBlob {
    
    /** for hibernate. */
    protected InstrumentationBlob() {}
    
    public InstrumentationBlob(ChannelImpl chan, Instrumentation inst) {
        this.chan = chan;
        this.inst = inst;
    }
    
    public byte[] getInstrumentationAsBlob() {
        org.jacorb.orb.CDROutputStream cdrOut = new org.jacorb.orb.CDROutputStream(Initializer.getORB());
        InstrumentationHelper.write(cdrOut, inst);
        return cdrOut.getBufferCopy();
    }
    
    protected void setInstrumentationAsBlob(byte[] instBytes) {
        if (instBytes.length > 0) {
            org.jacorb.orb.CDRInputStream cdrIn = new org.jacorb.orb.CDRInputStream(Initializer.getORB(), instBytes);
            inst = InstrumentationHelper.read(cdrIn);
        } else {
            inst = null;
        }
    }
        
    public Instrumentation getInstrumentation() {
        return inst;
    }
    
    public ChannelImpl getChannel() {
        return chan;
    }
    
    public void setChannel(ChannelImpl chan) {
        this.chan = chan;
    }

    ChannelImpl chan;
    Instrumentation inst;
    int dbid;
    
    public int getDbid() {
        return dbid;
    }

    
    public void setDbid(int dbid) {
        this.dbid = dbid;
    }
}
