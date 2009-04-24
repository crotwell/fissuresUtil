package edu.sc.seis.fissuresUtil.namingService;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.orb.iiop.IIOPProfile;
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingIteratorHolder;
import org.omg.CosNaming.BindingListHolder;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.simple.Initializer;

public class NameServiceTreeModel implements TreeModel {

    NameServiceTreeModel(FissuresNamingService ns) {
        this.ns = ns;
    }

    FissuresNamingService ns;

    public Object getRoot() {
        return new NCWrapper(ns.getNameService(), new NameComponent[0]);
    }

    public Object getChild(Object arg0, int arg1) {
        NamingContext nc = null;
        if(arg0 instanceof NCWrapper) {
            nc = ((NCWrapper)arg0).nc;
            BindingListHolder blHold = new BindingListHolder();
            nc.list(100, blHold, new BindingIteratorHolder());
            Binding[] bindings = blHold.value;
            if(bindings[arg1].binding_type.equals(BindingType.ncontext)) {
                try {
                    NamingContext childNC = NamingContextHelper.narrow(nc.resolve(bindings[arg1].binding_name));
                    NCWrapper wrapper = new NCWrapper(childNC,
                                                      bindings[arg1].binding_name);
                    return wrapper;
                } catch(NotFound e) {
                	return handleWithBinding(e, bindings, arg1);
                } catch(Exception e) {
                	return handleWithBinding(e, bindings, arg1);
                }
            } else {
                NameComponent name = bindings[arg1].binding_name[bindings[arg1].binding_name.length - 1];
                String IOR;
                IIOPAddress addr = null;
                try {
                    IOR = Initializer.getORB().object_to_string(nc.resolve(bindings[arg1].binding_name));
                    ParsedIOR parsed = new ParsedIOR((org.jacorb.orb.ORB)Initializer.getORB(),
                                                     IOR);
                    IIOPProfile profile = (IIOPProfile)parsed.getProfiles().get(0);
                    addr = (IIOPAddress)profile.getAddress();
                } catch(NotFound e) {
                } catch(CannotProceed e) {
                } catch(InvalidName e) {
                }
                return name.id + "." + name.kind+(addr==null?"":" ("+addr.getIP()+":"+addr.getPort()+")");
            }
        }
        return "dummy";
    }
    
    private String handleWithBinding(Throwable e, Binding[] bindings, int arg1) {
        String name = "Root:";
        for(int i = 0; i < bindings[arg1].binding_name.length; i++) {
        	name += "/";
        	name +=  bindings[arg1].binding_name[i].id+"."+bindings[arg1].binding_name[i].kind;
        }
        GlobalExceptionHandler.handle(name, e);
        return "exception " + name;
    }

    public int getChildCount(Object arg0) {
        NamingContext nc = null;
        if(arg0 instanceof NCWrapper) {
            nc = ((NCWrapper)arg0).nc;
        } else if(arg0 instanceof Binding) {
            Binding binding = (Binding)arg0;
            if(binding.binding_type.equals(BindingType.ncontext)) {
                try {
                    nc = NamingContextHelper.narrow(ns.getNameService()
                            .resolve(binding.binding_name));
                } catch(Exception e) {
                    GlobalExceptionHandler.handle(e);
                }
            }
        } else if(arg0 instanceof NamingContext) {
            nc = (NamingContext)arg0;
        }
        if(nc != null) {
            BindingListHolder blHold = new BindingListHolder();
            nc.list(100, blHold, new BindingIteratorHolder());
            Binding[] bindings = blHold.value;
            return bindings.length;
        }
        return 0;
    }

    public boolean isLeaf(Object arg0) {
        if(arg0 instanceof NCWrapper) {
            return false;
        } else {
            return true;
        }
    }

    public void valueForPathChanged(TreePath arg0, Object arg1) {
    // TODO Auto-generated method stub
    }

    public int getIndexOfChild(Object arg0, Object arg1) {
        if(arg0 instanceof NamingContext) {
            NamingContext nc = (NamingContext)arg0;
            BindingListHolder blHold = new BindingListHolder();
            nc.list(100, blHold, new BindingIteratorHolder());
            Binding[] bindings = blHold.value;
            for(int i = 0; i < bindings.length; i++) {
                if(bindings[i].equals(arg1)) {
                    return i;
                }
            }
        }
        return 0;
    }

    public void addTreeModelListener(TreeModelListener arg0) {
    // TODO Auto-generated method stub
    }

    public void removeTreeModelListener(TreeModelListener arg0) {
    // TODO Auto-generated method stub
    }
}

class NCWrapper {

    NCWrapper(NamingContext nc, NameComponent[] name) {
        this.nc = nc;
        this.name = name;
    }

    public String toString() {
        if(name.length != 0) {
            return name[name.length - 1].id + "." + name[name.length - 1].kind;
        } else {
            return "root";
        }
    }

    NameComponent[] name;

    NamingContext nc;
}
