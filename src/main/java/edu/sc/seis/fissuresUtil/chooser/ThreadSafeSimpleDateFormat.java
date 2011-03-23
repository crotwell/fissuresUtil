package edu.sc.seis.fissuresUtil.chooser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class ThreadSafeSimpleDateFormat {

    public ThreadSafeSimpleDateFormat(String format) {
        this(format, TimeZone.getTimeZone("GMT"));
    }
    
    public ThreadSafeSimpleDateFormat(String format, TimeZone zone) {
        this.format = format;
        this.zone = zone;
    }
    
    protected ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(zone);
            return sdf;
        }
    };

    protected final SimpleDateFormat get() {
        return threadLocal.get();
    }
    
    public String format(Date e) {
        return get().format(e);
    }
    
    public Date parse(String s) throws ParseException {
        return get().parse(s);
    }
    
    public String toPattern() {
        return format;
    }
    
    protected String format;
    
    protected TimeZone zone;
}
