package edu.sc.seis.fissuresUtil.chooser;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;


public class ThreadSafeDecimalFormat extends NumberFormat {
    
    public ThreadSafeDecimalFormat(String pattern) {
        this.pattern = pattern;
    }
    
    final String pattern;
    
    private final ThreadLocal<DecimalFormat> threadLocal = new ThreadLocal<DecimalFormat>() {  
        @Override  
        protected DecimalFormat initialValue() {  
            return (new DecimalFormat(pattern));  
        }  
    };

    @Override
    public StringBuffer format(double d, StringBuffer sb, FieldPosition p) {
        return threadLocal.get().format(d, sb, p);
    }

    @Override
    public StringBuffer format(long l, StringBuffer sb, FieldPosition p) {
        return threadLocal.get().format(l, sb, p);
    }

    @Override
    public Number parse(String s, ParsePosition p) {
        return threadLocal.get().parse(s, p);
    } 
}
