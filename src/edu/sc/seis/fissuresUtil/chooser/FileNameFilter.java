package edu.sc.seis.fissuresUtil.chooser;



import java.io.File;

/**
 * FileNameFilter.java
 *
 *
 * Created: Wed Feb 13 12:43:31 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class FileNameFilter extends javax.swing.filechooser.FileFilter{
    public FileNameFilter (String[] extensions){
	
	this.extensions = extensions;
	

    }

    public boolean accept(File f) {

	if(f.isDirectory()) return true;
	else {

	    String ext = null;
	    String s = f.getName();
	    int i = s.lastIndexOf('.');
	    
	    if (i > 0 &&  i < s.length() - 1) {
		ext = s.substring(i+1).toLowerCase();
	    }
	    
	    for(i = 0; i < extensions.length; i++) {
	   
		if( extensions[i].equals(ext) ) return true;

	    }
	}
	return false;

    }

    public String getDescription() {

	String ext = new String(extensions[0]);
	
	for( int counter = 1; counter < extensions.length; counter++) {

	    ext = ext +","+extensions[counter];
	    
	}
	return ext;
	
    }

    String[] extensions;
    
}// FileNameFilter
