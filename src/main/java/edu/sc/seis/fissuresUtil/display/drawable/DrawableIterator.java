package edu.sc.seis.fissuresUtil.display.drawable;

/**
 * DrawableIterator.java
 *
 * @author Created by Charlie Groves
 */

import java.util.Iterator;
import java.util.List;

public class DrawableIterator implements Iterator {

    public DrawableIterator(Class iteratorClass, List drawables) {
        this.iteratorClass = iteratorClass;
        it = drawables.iterator();
    }

    public boolean hasNext() {
        if ( nextObj != null) {
            return true;
        } // end of if ()
        if ( finished ) {
            return false;
        } // end of if ()
        //find next
        while ( it.hasNext()) {
            Object n = it.next();
            if ( iteratorClass.isInstance(n) ) {
                nextObj = n;
                return true;
            } // end of if ()
        } // end of while ()
        finished = true;
        return false;
    }

    public Object next() {
        if ( hasNext() == false) {
            return null;
        } // end of if ()
        // hasNext will populate nextObj if it returned true
        Object out = nextObj;
        nextObj=null;
        return out;
    }

    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("You can not remove from a DrawableIterator");
    }

    private Iterator it;

    private Class iteratorClass;

    private Object nextObj;

    private boolean finished = false;
}

