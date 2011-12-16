package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

/**
 * SeisDataChangeEvent.java
 *
 *
 * Created: Thu Feb 20 13:43:59 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class SeisDataChangeEvent {
 public SeisDataChangeEvent (DataSetSeismogram source,
                             Object initiator){
     this(new LocalSeismogramImpl[0], source, initiator);
 }

 public SeisDataChangeEvent (LocalSeismogramImpl[] seismos,
                             DataSetSeismogram source,
                             Object initiator){
  this.seismos = seismos;
  this.source = source;
  this.initiator = initiator;
 }
 
 private LocalSeismogramImpl[] seismos;
 
 public LocalSeismogramImpl[] getSeismograms() {
  return seismos;
 }
 
 private DataSetSeismogram source;
 
 public DataSetSeismogram getSource() {
  return source;
 }
 
 private Object initiator;
 
 public Object getInitiator() {
  return initiator;
 }
 
}// SeisDataChangeEvent
