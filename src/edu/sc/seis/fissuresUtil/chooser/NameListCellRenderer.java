package edu.sc.seis.fissuresUtil.chooser;

import javax.swing.*;
   
class NameListCellRenderer extends DefaultListCellRenderer {
    NameListCellRenderer(boolean useNames){
	this.useNames = useNames;
    }

    public Component getListCellRendererComponent(JList list,
						  Object value,
						  int index,
						  boolean isSelected,
						  boolean cellHasFocus) {
	String name = "XXXX";
	if (value instanceof NetworkAccess) {
	    if (useNames) {
		name = ((NetworkAccess)value).get_attributes().name;
		if (name == null || name.length() == 0) {
		    name = ((NetworkAccess)value).get_attributes().get_code();
		    if (name.startsWith("X") || name.startsWith("Y") || name.startsWith("Z")) {
			edu.iris.Fissures.Time start = 
			    ((NetworkAccess)value).get_attributes().get_id().begin_time;
			name += start.date_time.substring(2,4);
		    } // end of if (name.startsWith("X"))
		    
		}
	    } else {
		name = ((NetworkAccess)value).get_attributes().get_code();
	    }
	}
	if (value instanceof Station) {
	    if (useNames) {
		name = ((Station)value).name;
		// assume name of length 1 isn't a name
		if (name == null || name.length() <= 1) {
		    name = ((Station)value).get_code();
		}
	    } else {
		name = ((Station)value).get_code();
	    }
	}
	if (value instanceof Site) {
	    if (useNames) {
		name = ((Site)value).get_code();
		if (name == null || name.length() == 0) {
		    name = ((Site)value).get_code();
		}
	    } else {
		name = ((Site)value).get_code();
	    }
	}
	
	if (value instanceof Channel) {
	    if (useNames) {
		name = ((Channel)value).name;
		if (name == null || name.length() == 0) {
		    name = ((Channel)value).get_code();
		}
	    } else {
		name = ((Channel)value).get_code();
	    }
	}
	
	if (value instanceof String) {
	    name = (String)value;
	} // end of if (value instanceof String)
	
	
	return super.getListCellRendererComponent(list, 
						  name, 
						  index, 
						  isSelected, 
						  cellHasFocus);
        }
    boolean useNames;
}
