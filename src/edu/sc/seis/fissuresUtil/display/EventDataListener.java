package edu.sc.seis.fissuresUtil.display;

import java.util.EventListener;

public interface EventDataListener extends EventListener{

    public void eventDataChanged(EQDataEvent eqDataEvent);

    public void eventDataAppended(EQDataEvent eqDataEvent);

    public void eventDataCleared();

}
