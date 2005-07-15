package edu.sc.seis.fissuresUtil.sac;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import edu.iris.Fissures.IfNetwork.ComplexNumberErrored;
import edu.iris.Fissures.IfNetwork.Filter;
import edu.iris.Fissures.IfNetwork.FilterType;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.PoleZeroFilter;
import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.IfNetwork.Stage;
import edu.iris.Fissures.IfNetwork.TransferType;
import edu.iris.Fissures.model.UnitImpl;
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

    public SacPoleZero(Response response) {
        Stage stage = response.stages[0];
        Filter filter = stage.filters[0];
        if(filter.discriminator().value() != FilterType._POLEZERO) {
            throw new IllegalArgumentException("Unexpected response type "
                    + filter.discriminator().value());
        }
        PoleZeroFilter pz = filter.pole_zero_filter();
        int gamma = 0;
        UnitImpl unit = (UnitImpl)stage.input_units;
        if(unit.isConvertableTo(UnitImpl.METER_PER_SECOND)) {
            gamma = 1;
        } else if(unit.isConvertableTo(UnitImpl.METER_PER_SECOND_PER_SECOND)) {
            gamma = 2;
        }
        int num_zeros = pz.zeros.length + gamma;
        double mulFactor = 1;
        if(stage.type == TransferType.ANALOG) {
            mulFactor = 2 * Math.PI;
        }
        zeros = initCmplx(num_zeros);
        for(int i = 0; i < pz.zeros.length; i++) {
            zeros[i] = new Cmplx(pz.zeros[i].real * mulFactor, pz.zeros[i].imaginary * mulFactor);
        }
        poles = initCmplx(pz.poles.length);
        for(int i = 0; i < pz.poles.length; i++) {
            poles[i] = new Cmplx(pz.poles[i].real * mulFactor, pz.poles[i].imaginary * mulFactor);
        }
        constant = stage.the_normalization[0].ao_normalization_factor;
        if(stage.type == TransferType.ANALOG) {
            constant *= Math.pow(2 * Math.PI, pz.poles.length - pz.zeros.length);
        }
        constant *= response.the_sensitivity.sensitivity_factor;
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

    static void addToTable(TextTable table,
                           String[] header,
                           Cmplx[] values) {
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
        ArrayList lines = new ArrayList();
        String s;
        while((s = in.readLine()) != null) {
            lines.add(s);
        }
        Cmplx[] poles = new Cmplx[0];
        Cmplx[] zeros = new Cmplx[0];
        float constant = 1;
        Iterator it = lines.iterator();
        while(it.hasNext()) {
            String line = (String)it.next();
            if(line.startsWith(POLES)) {
                String num = line.substring(POLES.length()).trim();
                int numPoles = Integer.parseInt(num);
                poles = initCmplx(numPoles);
                for(int i = 0; i < poles.length && it.hasNext(); i++) {
                    line = (String)it.next();
                    if(line.matches("^-?\\d+\\.\\d+\\s+-?\\d+\\.\\d+\\s+")) {
                        poles[i] = parseCmplx(line);
                    } else {
                        break;
                    }
                }
            } else if(line.startsWith(ZEROS)) {
                String num = line.substring(ZEROS.length()).trim();
                int numZeros = Integer.parseInt(num);
                zeros = initCmplx(numZeros);
                for(int i = 0; i < zeros.length && it.hasNext(); i++) {
                    line = (String)it.next();
                    if(line.matches("^-?\\d+\\.\\d+\\s+-?\\d+\\.\\d+\\s+")) {
                        zeros[i] = parseCmplx(line);
                    } else {
                        break;
                    }
                }
            } else if(line.startsWith(CONSTANT)) {
                line = line.replaceAll("\\s+", " ");
                String[] sline = line.split(" ");
                constant = Float.parseFloat(sline[1]);
            }
        }
        return new SacPoleZero(poles, zeros, constant);
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

    public boolean equals(Object obj) {
        if(super.equals(obj)) {
            return true;
        }
        if(obj instanceof SacPoleZero) {
            SacPoleZero spz = (SacPoleZero)obj;
            if(spz.constant != constant || spz.poles.length != poles.length
                    || spz.zeros.length != zeros.length) {
                return false;
            } else {
                for(int i = 0; i < poles.length; i++) {
                    if(spz.poles[i].i != poles[i].i
                            || spz.poles[i].r != poles[i].r) {
                        return false;
                    }
                }
                for(int i = 0; i < zeros.length; i++) {
                    if(spz.zeros[i].i != zeros[i].i
                            || spz.zeros[i].r != zeros[i].r) {
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