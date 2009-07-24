package edu.sc.seis.fissuresUtil.flow;


public interface Sender {
    
    public void addReceiver(Receiver r);
    
    public void removeReceiver(Receiver r);
}
