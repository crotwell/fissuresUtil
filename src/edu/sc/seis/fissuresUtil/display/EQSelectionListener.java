package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import java.util.EventListener;

public interface EQSelectionListener extends EventListener{

    public void eqSelectionChanged(EQSelectionEvent eqSelectionEvent);

}
