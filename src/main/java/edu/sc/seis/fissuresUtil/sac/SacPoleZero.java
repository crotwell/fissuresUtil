package edu.sc.seis.fissuresUtil.sac;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import edu.sc.seis.fissuresUtil.display.TextTable;
import edu.sc.seis.fissuresUtil.freq.Cmplx;

/**
 * @author crotwell Created on Jul 15, 2005
 */
public class SacPoleZero {

    public SacPoleZero(Cmplx[] poles, Cmplx[] zeros, float constant) {
        this.poles = poles;
        this.zeros = zeros;
        this.constant = constant;
    }

    public float getConstant() {
        return constant;
    }

    public Cmplx[] getPoles() {
        return poles;
    }

    public Cmplx[] getZeros() {
        return zeros;
    }

    public String toString() {
        TextTable textTable = new TextTable(2);
        String[] zeroHeader = new String[2];
        zeroHeader[0] = ZEROS;
        zeroHeader[1] = "" + zeros.length;
        addToTable(textTable, zeroHeader, zeros);
        String[] poleHeader = new String[2];
        poleHeader[0] = POLES;
        poleHeader[1] = "" + poles.length;
        addToTable(textTable, poleHeader, poles);
        String[] sensitivityHeader = new String[2];
        sensitivityHeader[0] = CONSTANT;
        sensitivityHeader[1] = "" + constant;
        textTable.addRow(sensitivityHeader);
        return textTable.toString();
    }

    static void addToTable(TextTable table, String[] header, Cmplx[] values) {
        table.addRow(header);
        for(int cnt = 0; cnt < values.length; cnt++) {
            if(values[cnt].r != 0 || values[cnt].i != 0) {
                String[] realImg = new String[2];
                realImg[0] = formatter.format(values[cnt].r);
                realImg[1] = formatter.format(values[cnt].i);
                table.addRow(realImg);
            }
        }
    }

    public static SacPoleZero read(BufferedReader in) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        String s;
        while((s = in.readLine()) != null) {
            lines.add(s.trim());
        }
        Cmplx[] poles = new Cmplx[0];
        Cmplx[] zeros = new Cmplx[0];
        float constant = 1;
        Iterator<String> it = lines.iterator();
        String line = nextLine(it);
        while(!line.equals("")) {
            if(line.startsWith(POLES)) {
                String num = line.substring(POLES.length()).trim();
                int numPoles = Integer.parseInt(num);
                poles = initCmplx(numPoles);
                line = nextLine(it);
                for(int i = 0; i < poles.length && it.hasNext(); i++) {
                    if(line.matches("^-?\\d+\\.\\d+\\s+-?\\d+\\.\\d+")) {
                        poles[i] = parseCmplx(line);
                        line = nextLine(it);
                    } else {
                        break;
                    }
                }
            } else if(line.startsWith(ZEROS)) {
                String num = line.substring(ZEROS.length()).trim();
                int numZeros = Integer.parseInt(num);
                zeros = initCmplx(numZeros);
                line = nextLine(it);
                for(int i = 0; i < zeros.length && it.hasNext(); i++) {
                    if(line.matches("^-?\\d+\\.\\d+\\s+-?\\d+\\.\\d+")) {
                        zeros[i] = parseCmplx(line);
                        line = nextLine(it);
                    } else {
                        break;
                    }
                }
            } else if(line.startsWith(CONSTANT)) {
                line = line.replaceAll("\\s+", " ");
                String[] sline = line.split(" ");
                constant = Float.parseFloat(sline[1]);
                line = nextLine(it);
            } else {
                throw new IOException("Unknown line in SAC polezero file: "
                        + line);
            }
        }
        return new SacPoleZero(poles, zeros, constant);
    }

    private static String nextLine(Iterator it) {
        if(it.hasNext()) {
            return (String)it.next();
        } else {
            return "";
        }
    }

    static Cmplx[] initCmplx(int length) {
        Cmplx[] out = new Cmplx[length];
        for(int i = 0; i < out.length; i++) {
            out[i] = new Cmplx(0, 0);
        }
        return out;
    }

    static Cmplx parseCmplx(String line) throws IOException {
        line = line.trim().replaceAll("\\s+", " ");
        String[] sline = line.split(" ");
        return new Cmplx(Float.parseFloat(sline[0]), Float.parseFloat(sline[1]));
    }

    public boolean close(Object obj) {
        if(super.equals(obj)) {
            return true;
        }
        if(obj instanceof SacPoleZero) {
            SacPoleZero spz = (SacPoleZero)obj;
            if(!close(spz.constant, constant)) {
                System.out.println("const not close");
                return false;
            } else {
                return closeButConstant(obj);
            }
        } else {
            return false;
        }
    }

    public boolean closeButConstant(Object obj) {
        if(super.equals(obj)) {
            return true;
        }
        if(obj instanceof SacPoleZero) {
            SacPoleZero spz = (SacPoleZero)obj;
            if(spz.poles.length != poles.length
                    || spz.zeros.length != zeros.length) {
                return false;
            } else {
                for(int i = 0; i < poles.length; i++) {
                    if( ! closeFourDigit(spz.poles[i], poles[i])) {
                        System.out.println("pole " + i + " not equal"
                                + spz.poles[i].i + " " + poles[i].i + " "
                                + spz.poles[i].r + " " + poles[i].r);
                        return false;
                    }
                }
                for(int i = 0; i < zeros.length; i++) {
                    if( ! closeFourDigit(spz.zeros[i], zeros[i])) {
                        System.out.println("zero " + i + " not equal");
                        return false;
                    }
                }
                return true;
            }
        } else {
            return false;
        }
    }

    private static boolean close(double a, double b) {
        if(Math.abs(a - b) / a > 0.0001) {
            System.out.println("fail close " + a + " " + b + " "
                    + (Math.abs(a - b) / a) + " ratio=" + (a / b));
            return false;
        }
        return true;
    }

    private static boolean closeFourDigit(Cmplx a, Cmplx b) {
        return closeFourDigit(a.r, b.r) && closeFourDigit(a.i, b.i);
    }

    private static boolean closeFourDigit(double a, double b) {
        if(Math.abs(a - b) > 0.0001) {
            System.out.println("fail closeFourDigit " + a + " " + b + " "
                    + (Math.abs(a - b)) + " ratio=" + (a / b));
            return false;
        }
        return true;
    }

    public boolean equals(Object obj) {
        if(super.equals(obj)) {
            return true;
        }
        if(obj instanceof SacPoleZero) {
            SacPoleZero spz = (SacPoleZero)obj;
            if((Math.abs(spz.constant - constant) / constant) > .001) {
                System.out.println("const not equal");
                return false;
            } else if(spz.poles.length != poles.length
                    || spz.zeros.length != zeros.length) {
                return false;
            } else {
                for(int i = 0; i < poles.length; i++) {
                    if(spz.poles[i].i != poles[i].i
                            || spz.poles[i].r != poles[i].r) {
                        System.out.println("pole " + i + " not equal"
                                + spz.poles[i].i + " " + poles[i].i + " "
                                + spz.poles[i].r + " " + poles[i].r);
                        return false;
                    }
                }
                for(int i = 0; i < zeros.length; i++) {
                    if(spz.zeros[i].i != zeros[i].i
                            || spz.zeros[i].r != zeros[i].r) {
                        System.out.println("zero " + i + " not equal");
                        return false;
                    }
                }
                return true;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        int i = 17;
        i = 37 * i + poles.length;
        i = 37 * i + zeros.length;
        for(int j = 0; j < poles.length; j++) {
            long tmp = Double.doubleToLongBits(poles[j].i);
            i = 37 * i + (int)(tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(poles[j].r);
            i = 37 * i + (int)(tmp ^ (tmp >>> 32));
        }
        for(int j = 0; j < zeros.length; j++) {
            long tmp = Double.doubleToLongBits(zeros[j].i);
            i = 37 * i + (int)(tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(zeros[j].r);
            i = 37 * i + (int)(tmp ^ (tmp >>> 32));
        }
        return i;
    }

    Cmplx[] poles;

    Cmplx[] zeros;

    float constant;

    static String POLES = "POLES";

    static String ZEROS = "ZEROS";

    static String CONSTANT = "CONSTANT";

    protected static DecimalFormat formatter = new DecimalFormat("0.0000;-0.0000");
}