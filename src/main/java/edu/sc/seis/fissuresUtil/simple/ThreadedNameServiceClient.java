package edu.sc.seis.fissuresUtil.simple;

import edu.sc.seis.fissuresUtil.cache.ProxyEventDC;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;

public class ThreadedNameServiceClient {

    public void exercise() {
        try {
            Tester.runAll(createRunnables());
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }

    Runnable[] createRunnables() {
        return new Runnable[] {new EventWalker(),
                               new NetWalker(),
                               new SeisWalker()};
    }

    public static void main(String[] args) {
        Initializer.init(args);
        ThreadedNameServiceClient walker = new ThreadedNameServiceClient();
        walker.exercise();
    }
}

class EventWalker implements Runnable {

    public void run() {
        ProxyEventDC[] eventdcs = Initializer.getNS().getAllEventDC();
        System.out.println("Got " + eventdcs.length + " event dcs");
    }

    public String toString() {
        return "EventWalker";
    }
}

class NetWalker implements Runnable {

    public void run() {
        ProxyNetworkDC[] dcs = Initializer.getNS().getAllNetworkDC();
        System.out.println("Got " + dcs.length + " net dcs");
    }

    public String toString() {
        return "EventWalker";
    }
}

class SeisWalker implements Runnable {

    public void run() {
        ProxySeismogramDC[] dcs = Initializer.getNS().getAllSeismogramDC();
        System.out.println("Got " + dcs.length + "  dcs");
    }

    public String toString() {
        return "SeisWalker";
    }
}