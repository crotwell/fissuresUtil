
package edu.sc.seis.fissuresUtil.mseed;

import java.io.*;
import java.lang.*;
import java.lang.reflect.*;

/** Superclass of all seed blockettes. The actual blockettes do not store
 * either their blockette type or their length in the case of ascii blockettes
 * or next blockettes offset in the case of data blockettes as these are either
 * already known (ie type) or may change after reading due to data changes.
 * Instead each of these values are calculated based on the data.
 */
public abstract class  Blockette {

    public Blockette() {
    }

    public static Blockette parseBlockette(int type, byte[] bytes)
    throws IOException {

    try {
            //System.out.println(" Class.forName    Blockette"+type);
        Class blocketteClass = Class.forName("edu.sc.seis.fissuresUtil.mseed.Blockette"+type);
            //System.out.println(" Class.forName suceeded");

        Class[] argTypes = new Class[1];
        //      argTypes[0] = Class.forName("byte[]");
        argTypes[0] = byte[].class;
        Constructor read = blocketteClass.getConstructor(argTypes);
        Object[] arguments = new Object[1];
        arguments[0] = bytes;
            //System.out.println("Constructor  suceeded");

        Blockette blockette = (Blockette)read.newInstance(arguments);


            //System.out.println("read suceeded");
        return blockette;
    } catch (ClassNotFoundException e) {
        // must not be installed, read an  unknownblockette
System.out.println(" Class.forName failed: "+type);
        Blockette blockette = new BlocketteUnknown(bytes, type);

        return blockette;
    } catch ( NoSuchMethodException e) {
        // must not be installed, skip this blockette
        return null;
    } catch (InstantiationException e) {
        // must not be installed, skip this blockette
        return null;
    } catch (IllegalAccessException  e) {
        // must not be installed, skip this blockette
        return null;
    } catch (InvocationTargetException  e) {
        // must not be installed, skip this blockette
        return null;
    }
    }

    public abstract int getType();

    public abstract String getName();

    public abstract int getSize();

    public abstract byte[] toBytes();

    public String toString() {
    String s = getType()+": "+getName();
    return s;
    }

}

