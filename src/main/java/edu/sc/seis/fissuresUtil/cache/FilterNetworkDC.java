package edu.sc.seis.fissuresUtil.cache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.omg.CORBA.NO_IMPLEMENT;

import edu.iris.Fissures.IfNetwork.NetworkDCOperations;
import edu.iris.Fissures.IfNetwork.NetworkExplorer;
import edu.iris.Fissures.IfNetwork.NetworkFinder;

/**
 * @author groves Created on Dec 1, 2004 The extension of VestingNetworkDC is a
 *         hack so this thing can be passed around as a vestingdc. It really
 *         just uses the one passed into it and not the superclass methods
 */
public class FilterNetworkDC extends VestingNetworkDC {

    public FilterNetworkDC(ProxyNetworkDC wrappedDC, Pattern[] patterns) {
        super(wrappedDC);
        this.patterns = patterns;
    }

    public NetworkExplorer a_explorer() {
        throw new NO_IMPLEMENT();
    }

    public NetworkFinder a_finder() {
        return new FilterNetworkFinder(super.a_finder(),
                                       patterns);
    }

    public static Pattern[] readPattern(String filterURL) throws IOException {
        InputStream filterStream = new URL(filterURL.trim()).openStream();
        Reader reader = new BufferedReader(new InputStreamReader(filterStream));
        int curInt;
        StringBuffer buff = new StringBuffer();
        List gottenPatterns = new ArrayList();
        while((curInt = reader.read()) != -1) {
            char curChar = (char)curInt;
            if(curChar != '\n') {
                buff.append(curChar);
            } else {
                gottenPatterns.add(Pattern.compile(buff.toString()));
                buff = new StringBuffer();
            }
        }
        return (Pattern[])gottenPatterns.toArray(new Pattern[0]);
    }

    public NetworkDCOperations getWrappedDC(Class wrappedClass) {
        if(this.getClass().isAssignableFrom(wrappedClass)) {
            return this;
        }
        NetworkDCOperations tmp = getWrappedDC();
        if(tmp instanceof ProxyNetworkDC) {
            return ((ProxyNetworkDC)tmp).getWrappedDC(wrappedClass);
        }
        throw new IllegalArgumentException("Can't find class "
                + wrappedClass.getName());
    }

    private Pattern[] patterns;
}