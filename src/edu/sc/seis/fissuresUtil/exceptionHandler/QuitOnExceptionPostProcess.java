package edu.sc.seis.fissuresUtil.exceptionHandler;

public class QuitOnExceptionPostProcess implements PostProcess {

    public QuitOnExceptionPostProcess(Class c) {
        this(c, 1);
    }
    public QuitOnExceptionPostProcess(Class c, int processResult) {
        quitType = c;
        this.processResult = processResult;
    }

    public void process(String message, Throwable thrown) {
        if(quitType.isInstance(thrown)) {
            logger.fatal("Quiting ...caught an exception of type: "
                    + quitType.getName()+"  message="+message, thrown);
            System.exit(1);
        } else if (thrown.getCause() != null) {
            process(message, thrown.getCause());
        }
    }
    
    int processResult;

    Class quitType;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(QuitOnExceptionPostProcess.class);
}
