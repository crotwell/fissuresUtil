package edu.sc.seis.fissuresUtil;



/**
 * CommandLineArguments.java
 *
 *
 * Created: Wed Oct 17 11:40:01 2001
 *
 * @author <a href="mailto: "Srinivasa Telukutla</a>
 * @version
 */

public class CommandLineArguments {
    public CommandLineArguments (String args[]){
	this.args = args;
	
    }
    public String processCommandLineArgument(String argStr) {
	int i;
	for( i = 0; i < args.length; i++) {
	    if(args[i].equals(argStr)) {
		return args[i+1];
	    }
	    
	}
	return null;

    }
    public String processCommandLineArgument(String argStr, String defaultValue) {
	String rtnValue = processCommandLineArgument(argStr);
	if(rtnValue == null) 
	    return defaultValue;
	else
	    return rtnValue;
    }

    public boolean getBoolean(String str) {
	String rtn = processCommandLineArgument(str);
	if(str.toUpperCase().equals("TRUE")) return true;
	else return false;
	
    }

    public int getInt(String str) {
	String rtn = processCommandLineArgument(str);
	return Integer.parseInt(rtn);
    }    

    public double getDouble(String str) {
	String rtn = processCommandLineArgument(str);
	return Double.parseDouble(rtn);
   }

    public String getString(String str) {
	String rtn = processCommandLineArgument(str);
	return rtn;
    }

    public boolean getBoolean(String str, boolean defaultValue) {
	String rtn = processCommandLineArgument(str);
	if(rtn == null) return defaultValue;
	if(str.toUpperCase().equals("TRUE")) return true;
	else return false;
	
    }

    public int getInt(String str, int defaultValue) {
	String rtn = processCommandLineArgument(str);
	if(rtn == null) return defaultValue;
	return Integer.parseInt(rtn);
    }    

    public double getDouble(String str, double defaultValue) {
	String rtn = processCommandLineArgument(str);
	if(rtn == null) return defaultValue;
	return Double.parseDouble(rtn);
   }

    public String getString(String str, String defaultValue) {
	String rtn = processCommandLineArgument(str);
	if(rtn == null) return defaultValue;
	return rtn;
    }
	
    private String[] args;
	
}// CommandLineArguments
