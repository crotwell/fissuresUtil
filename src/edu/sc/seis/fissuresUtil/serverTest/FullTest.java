package edu.sc.seis.fissuresUtil.serverTest;

public class FullTest{
    public static void main(String[] args){
        new ThreadedNetClient(args).runAll();
        new ThreadedEventClient(args).runAll();
    }
}
