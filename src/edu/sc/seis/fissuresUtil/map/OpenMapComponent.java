package edu.sc.seis.fissuresUtil.map;

/**
 * OpenMapComponent.java
 *
 * @author Created by Charlie Groves
 */

import com.bbn.openmap.Environment;
import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.gui.ToolPanel;
import com.bbn.openmap.util.Debug;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.util.Iterator;
import java.util.Properties;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JMenuBar;

public class OpenMapComponent extends JComponent  implements BeanContextMembershipListener, BeanContextChild, PropertyConsumer {

    public OpenMapComponent(){
        setLayout(new BorderLayout());
    }

    /**
     * BeanContextChildSupport object provides helper functions for
     * BeanContextChild interface.
     */
    private BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport(this);

    /**
     * Called when the OpenMapComponent is added to a BeanContext, and
     * when other objects are added to the BeanContext.  The
     * OpenMapFrame looks for objects that it knows how to place upon
     * itself (MapBean, ToolPanel, JMenuBar, InformationDelegator).
     * The OpenMapComponent does not check to see if the objects looked
     * for are already added to itself.  It assumes that if some
     * object type is getting added to it, the caller must know what
     * they are doing - just like a regular JComponent.
     *
     * @param it Iterator to use to go through the BeanContext objects.
     */
    protected void findAndInit(Iterator it) {
        Object someObj;
        while (it.hasNext()) {
            someObj = it.next();
            if (someObj instanceof MapBean) {
                // do the initializing that need to be done here
                MapBean mapBean = (MapBean)someObj;
                if (Debug.debugging("basic")) {
                    Debug.output("OpenMapComponent: found a MapBean, size " + mapBean.getSize() +
                                     ", preferred size " + mapBean.getPreferredSize() +
                                     ", " + mapBean.getProjection());
                }
                add((MapBean)someObj, BorderLayout.CENTER);
            }

            if (someObj instanceof ToolPanel) {
                // do the initializing that need to be done here
                Debug.message("basic", "OpenMapFrame: found a ToolPanel.");
                ToolPanel toolPanel = (ToolPanel)someObj;
                toolPanel.setFloatable(false);
                add(toolPanel, BorderLayout.NORTH);
            }

            if (someObj instanceof JMenuBar) {
                System.out.println("sorry, JMenus aren't allowed on OpenMapComponents");
            }

            if (someObj instanceof InformationDelegator) {
                Debug.message("basic", "OpenMapFrame: found an InfoDelegator.");
                InformationDelegator info = (InformationDelegator)someObj;
                info.setFloatable(false);
                add(info, BorderLayout.SOUTH);
            }
        }
    }

    /**
     * BeanContextMembership interface method.  Called when objects
     * are added to the BeanContext.
     *
     * @param bcme contains an Iterator that lets you go through the
     * new objects.
     */
    public void childrenAdded(BeanContextMembershipEvent bcme) {
        findAndInit(bcme.iterator());
    }

    /**
     * BeanContextMembership interface method.  Called by BeanContext
     * when children are being removed.  Unhooks itself from the
     * objects that are being removed if they are contained within the
     * Component.
     *
     * @param bcme event that contains an Iterator to use to go
     * through the removed objects.
     */
    public void childrenRemoved(BeanContextMembershipEvent bcme) {
        Object someObj;
        Iterator it = bcme.iterator();
        while (it.hasNext()) {
            someObj = it.next();
            if (someObj instanceof MapBean) {
                Debug.message("basic", "OpenMapFrame: MapBean is being removed from frame");
                // if it's not on the content pane, no foul...
                remove((MapBean)someObj);
            }

            if (someObj instanceof ToolPanel) {
                Debug.message("basic", "OpenMapFrame: ToolPanel is being removed from frame");
                // if it's not on the content pane, no foul...
                remove((ToolPanel)someObj);
            }

            if (someObj instanceof JMenuBar) {
                //no menubars on this object
            }

            if (someObj instanceof InformationDelegator) {
                Debug.message("basic", "OpenMapFrame: InfoDelegator being removed.");
                // if it's not on the content pane, no foul...
                remove((InformationDelegator)someObj);
            }
        }
    }

    /** Method for BeanContextChild interface. */
    public BeanContext getBeanContext() {
        return beanContextChildSupport.getBeanContext();
    }

    /** Method for BeanContextChild interface.
     *
     * @param BeanContext in_bc The context to which this object is being added
     */
    public void setBeanContext(BeanContext in_bc)
        throws PropertyVetoException {
        if (in_bc != null) {
            in_bc.addBeanContextMembershipListener(this);
            beanContextChildSupport.setBeanContext(in_bc);
            findAndInit(in_bc.iterator());
        }
    }

    /** Method for BeanContextChild interface. */
    public void addVetoableChangeListener(String propertyName,
                                          VetoableChangeListener in_vcl) {
        beanContextChildSupport.addVetoableChangeListener(propertyName,
                                                          in_vcl);
    }

    /** Method for BeanContextChild interface. */
    public void removeVetoableChangeListener(String propertyName,
                                             VetoableChangeListener in_vcl) {
        beanContextChildSupport.removeVetoableChangeListener(propertyName,
                                                             in_vcl);
    }

    // Implementation of PropertyConsumer Interface
    /**
     * Method to set the properties in the PropertyConsumer.  It is
     * assumed that the properties do not have a prefix associated
     * with them, or that the prefix has already been set.
     *
     * @param setList a properties object that the PropertyConsumer
     * can use to retrieve expected properties it can use for
     * configuration.
     */
    public void setProperties(Properties setList) {
    }

    /**
     * Method to set the properties in the PropertyConsumer.  The
     * prefix is a string that should be prepended to each property
     * key (in addition to a separating '.') in order for the
     * PropertyConsumer to uniquely identify properies meant for it, in
     * the midst of of Properties meant for several objects.
     *
     * @param prefix a String used by the PropertyConsumer to prepend
     * to each property value it wants to look up -
     * setList.getProperty(prefix.propertyKey).  If the prefix had
     * already been set, then the prefix passed in should replace that
     * previous value.
     * @param setList a Properties object that the PropertyConsumer
     * can use to retrieve expected properties it can use for
     * configuration.
     */
    public void setProperties(String prefix, Properties setList) {}

    /**
     * Method to fill in a Properties object, reflecting the current
     * values of the PropertyConsumer.  If the PropertyConsumer has a
     * prefix set, the property keys should have that prefix plus a
     * separating '.' prepended to each propery key it uses for
     * configuration.
     *
     * @param getList a Properties object to load the PropertyConsumer
     * properties into.  If getList equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer.
     */
    public Properties getProperties(Properties getList) {
        if (getList == null) {
            getList = new Properties();
        }

        getList.setProperty(Environment.Width, Integer.toString(getWidth()));
        getList.setProperty(Environment.Height, Integer.toString(getHeight()));

        return getList;
    }

    /**
     * Method to fill in a Properties object with values reflecting
     * the properties able to be set on this PropertyConsumer.  The
     * key for each property should be the raw property name (without
     * a prefix) with a value that is a String that describes what the
     * property key represents, along with any other information about
     * the property that would be helpful (range, default value,
     * etc.).
     *
     * @param getList a Properties object to load the PropertyConsumer
     * properties into.  If getList equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer.
     */
    public Properties getPropertyInfo(Properties list) {
        if (list == null) {
            list = new Properties();
        }
        return list;
    }

    /**
     * Set the property key prefix that should be used by the
     * PropertyConsumer.  The prefix, along with a '.', should be
     * prepended to the property keys known by the PropertyConsumer.
     *
     * @param prefix the prefix String.
     */
    public void setPropertyPrefix(String prefix) {}

    /**
     * Get the property key prefix that is being used to prepend to
     * the property keys for Properties lookups.
     *
     * @param String prefix String.
     */
    public String getPropertyPrefix() {
        return Environment.OpenMapPrefix;

    }
}
