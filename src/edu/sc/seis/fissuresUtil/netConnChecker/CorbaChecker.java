package edu.sc.seis.fissuresUtil.netConnChecker;

import org.apache.log4j.Category;
import org.omg.CORBA.COMM_FAILURE;

public class CorbaChecker extends ConcreteConnChecker {

    protected CorbaChecker(String description) {
        super(description);
    }

    public CorbaChecker(org.omg.CORBA.Object obj, String description) {
        super(description);
        this.obj = obj;
    }

    public void run() {
        try {
            if(obj._non_existent() == true) {
                reason = "got non existent";
                setFinished(true);
                setTrying(false);
                setSuccessful(false);
                fireStatusChanged(getDescription(), ConnStatus.FAILED);
            } else {
                setFinished(true);
                setTrying(false);
                setSuccessful(true);
                fireStatusChanged(getDescription(), ConnStatus.SUCCESSFUL);
            }
            return;
        } catch(COMM_FAILURE cf) {
            cause = cf;
            setFinished(true);
            setTrying(false);
            setSuccessful(false);
            fireStatusChanged(getDescription(), ConnStatus.FAILED);
        }// close run
    }

    protected org.omg.CORBA.Object obj;

    static Category logger = Category.getInstance(CorbaChecker.class);
}// CorbaChecker class
