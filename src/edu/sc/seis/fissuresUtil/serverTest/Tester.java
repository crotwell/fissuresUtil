package edu.sc.seis.fissuresUtil.serverTest;

public class Tester{
    
    /**Runs all the runnables returned by client.createRunnables() against each
     * other pair wise
     */
    public static void runAll(AbstractThreadedClient client){
        Runnable[] runnables = client.createRunnables();
        for (int i = 0; i < runnables.length; i++) {
            for (int j = i; j < runnables.length; j++) {
                Thread one = new Thread(runnables[i], runnables[i] + " thread");
                Thread two = new Thread(runnables[j], runnables[j] + " thread");
                System.out.println("Starting " + runnables[i] + " against " + runnables[j]);
                one.start();
                two.start();
                boolean joined = false;
                while(!joined){
                    try{
                        one.join();
                        two.join();
                        joined = true;
                    }catch(InterruptedException e){}
                }
            }
        }
    }
    public static void main(String[] args){
        runAll(new ThreadedSeisClient(args));
        runAll(new ThreadedNetClient(args));
        runAll(new ThreadedEventClient(args));
    }
}
