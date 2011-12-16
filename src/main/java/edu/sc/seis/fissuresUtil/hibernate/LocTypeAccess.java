package edu.sc.seis.fissuresUtil.hibernate;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.Setter;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;

public class LocTypeAccess implements PropertyAccessor {

    public Getter getGetter(Class arg0, String arg1)
            throws PropertyNotFoundException {
        return new Getter() {

            public Object get(Object owner) throws HibernateException {
                return new Integer(((Location)owner).type.value());
            }

            public Object getForInsert(Object target,
                                       Map mergeMap,
                                       SessionImplementor session)
                    throws HibernateException {
                return get(target);
            }

            public Method getMethod() {
                return null;
            }

            public String getMethodName() {
                return null;
            }

            public Class getReturnType() {
                return Integer.class;
            }
            
            /** this is new in hibernate 3.5 I think, not sure what the right impl is. */
            public Member getMember() {
                return getMethod();
            }
        };
    }

    public Setter getSetter(Class arg0, String arg1)
            throws PropertyNotFoundException {
        return new Setter() {

            public Method getMethod() {
                return null;
            }

            public String getMethodName() {
                return null;
            }

            public void set(Object target,
                            Object value,
                            SessionFactoryImplementor factory)
                    throws HibernateException {
                ((Location)target).type = LocationType.from_int(((Integer)value).intValue());
            }
        };
    }
}
