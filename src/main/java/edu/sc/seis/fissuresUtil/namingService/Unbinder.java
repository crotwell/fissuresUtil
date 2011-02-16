package edu.sc.seis.fissuresUtil.namingService;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.sc.seis.fissuresUtil.simple.Initializer;

public class Unbinder {

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        Initializer.init(args);
        FissuresNamingService unbindFrom = Initializer.getNS();
        String[] unbinders = extractCommaDelimitedProp("unbinders");
        for(int l = 0; l < unbinders.length; l++) {
            String[] dns = extractCommaDelimitedProp(unbinders[l] + ".dns");
            String[] names = extractCommaDelimitedProp(unbinders[l] + ".names");
            String[] interfaces = extractCommaDelimitedProp(unbinders[l]
                    + ".interfaces");
            for(int i = 0; i < dns.length; i++) {
                for(int j = 0; j < names.length; j++) {
                    for(int k = 0; k < interfaces.length; k++) {
                        unbindFrom.unbind(dns[i], interfaces[k], names[j]);
                    }
                }
            }
        }
    }

    private static String[] extractCommaDelimitedProp(String propName) {
        String commaDelimited = Initializer.getProps().getProperty(propName);
        List strings = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(commaDelimited, ",");
        while(tokenizer.hasMoreTokens()) {
            strings.add(tokenizer.nextToken());
        }
        return (String[])strings.toArray(new String[0]);
    }
}
