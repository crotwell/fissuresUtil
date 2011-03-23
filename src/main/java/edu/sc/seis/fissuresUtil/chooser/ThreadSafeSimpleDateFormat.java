package edu.sc.seis.fissuresUtil.chooser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ThreadSafeSimpleDateFormat {

    public ThreadSafeSimpleDateFormat(String format) {
        this.format = format;
    }
    
    protected ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(format);
        }
    };

    public final SimpleDateFormat get() {
    return threadLocal.get();
    }
    
    public String format(Date e) {
        return get().format(e);
    }
    
    public Date parse(String s) throws ParseException {
        return get().parse(s);
    }
    
    String format;
}
