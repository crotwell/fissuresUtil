package edu.sc.seis.fissuresUtil.rt130;

import java.io.File;
import java.util.Properties;

public class PropParser {

    private Properties confProps;

    public PropParser(Properties props) {
        this.confProps = props;
    }

    public String getPath(String propertyName) {
        String path = getString(propertyName);
        if(!new File(path).exists()) {
            throw new IllegalArgumentException(propertyName
                    + " indicated there would be a file at " + path
                    + " but I can't find it");
        }
        return path;
    }

    public int getInt(String propertyName) {
        try {
            return Integer.parseInt(getString(propertyName));
        } catch(NumberFormatException nfe) {
            throw new IllegalArgumentException(propertyName
                    + " must contain an integer like 7 or 12");
        }
    }

    public String getString(String propertyName) {
        if(confProps.containsKey(propertyName)) {
            return confProps.getProperty(propertyName);
        }
        throw new IllegalArgumentException("The properties must contain "
                + propertyName);
    }
}
