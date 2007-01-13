package edu.sc.seis.fissuresUtil.database.problem;

import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;

public class ProblemComponentTimeRange {

    public ProblemComponentTimeRange(String station,
                                     String problemType,
                                     String status,
                                     String component,
                                     String startPasscalTime,
                                     String endPasscalTime) {
        this(new Problem(station, problemType, status),
             component,
             startPasscalTime,
             endPasscalTime,
             getCalendarInstance());
    }

    private ProblemComponentTimeRange(Problem problem,
                                      String component,
                                      String startPasscalTime,
                                      String endPasscalTime,
                                      Calendar cal) {
        this(problem,
             component,
             getTime(startPasscalTime, cal),
             getTime(endPasscalTime, cal));
    }

    public ProblemComponentTimeRange(Problem problem,
                                     String component,
                                     Time start,
                                     Time end) {
        this.problem = problem;
        this.component = component;
        this.start = start;
        this.end = end;
    }

    public Problem getProblem() {
        return problem;
    }

    public String getComponent() {
        return component;
    }

    public Time getStart() {
        return start;
    }

    public Time getEnd() {
        return end;
    }

    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(obj instanceof ProblemComponentTimeRange) {
            ProblemComponentTimeRange prob = (ProblemComponentTimeRange)obj;
            if(prob.problem.equals(problem) && prob.component.equals(component)) {
                if(prob.start != null && start != null) {
                    if(!new MicroSecondDate(prob.start).equals(new MicroSecondDate(start))) {
                        return false;
                    }
                } else if(prob.start != null || start != null) {
                    return false;
                    // one's null and the other is not
                }
                if(prob.end != null && end != null) {
                    if(!new MicroSecondDate(prob.end).equals(new MicroSecondDate(end))) {
                        return false;
                    }
                } else if(prob.end != null || end != null) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<");
        buf.append(super.toString());
        buf.append('\n');
        buf.append("Problem: ");
        buf.append(problem);
        buf.append('\n');
        buf.append("Component: ");
        buf.append(component);
        buf.append('\n');
        buf.append("Start: ");
        buf.append(new MicroSecondDate(start));
        buf.append('\n');
        buf.append("End: ");
        buf.append(new MicroSecondDate(end));
        buf.append(">");
        return buf.toString();
    }

    public static Calendar getCalendarInstance() {
        return Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    }

    public static Time getTime(String passcalDate, Calendar cal) {
        if(cal == null) {
            cal = getCalendarInstance();
        }
        StringTokenizer tok = new StringTokenizer(passcalDate, ":");
        cal.set(Calendar.YEAR, Integer.parseInt(tok.nextToken()));
        cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(tok.nextToken()));
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(tok.nextToken()));
        cal.set(Calendar.MINUTE, Integer.parseInt(tok.nextToken()));
        String secondsString = tok.nextToken();
        tok = new StringTokenizer(secondsString, ".");
        cal.set(Calendar.SECOND, Integer.parseInt(tok.nextToken()));
        cal.set(Calendar.MILLISECOND, Integer.parseInt(tok.nextToken()));
        MicroSecondDate date = new MicroSecondDate(cal.getTime());
        return date.getFissuresTime();
    }

    private Problem problem;

    private String component;

    private Time start, end;
}
