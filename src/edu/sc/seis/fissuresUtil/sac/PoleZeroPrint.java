package edu.sc.seis.fissuresUtil.sac;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.ComplexNumberErrored;
import edu.iris.Fissures.IfNetwork.Filter;
import edu.iris.Fissures.IfNetwork.FilterType;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.PoleZeroFilter;
import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.IfNetwork.Stage;
import edu.iris.Fissures.IfNetwork.TransferType;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.TextTable;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class PoleZeroPrint {
	
	public static String printPoleZeroSac(NetworkAccess network, Channel chan) {
		TextTable textTable = new TextTable(2);
		try {
			
			ChannelId channel_id = chan.get_id();
			Instrumentation inst = network.retrieve_instrumentation(channel_id,channel_id.begin_time);
			Response response = inst.the_response;
			Stage stage = response.stages[0];
			Filter filter = stage.filters[0];
			if (filter.discriminator().value() != FilterType._POLEZERO ) {
				throw new IllegalArgumentException("Unexpected response type " + filter.discriminator().value());
			}
			PoleZeroFilter pz = filter.pole_zero_filter();
			int gamma = 0;
			UnitImpl unit = (UnitImpl)stage.input_units;
			if(unit.isConvertableTo(UnitImpl.METER_PER_SECOND)) {
				gamma = 1;
			}else if(unit.isConvertableTo(UnitImpl.METER_PER_SECOND_PER_SECOND)){
				gamma= 2;
			}
			int num_zeros = pz.zeros.length + gamma;
			
			String[] zeroHeader = new String[2];
			zeroHeader[0] = "ZEROS";
			zeroHeader[1] =  ""+num_zeros;
			addToTable(textTable,zeroHeader,pz.zeros,stage);
			
			String[] poleHeader = new String[2];
			poleHeader[0] = "POLES";
			poleHeader[1] = ""+pz.poles.length;
			addToTable(textTable,poleHeader,pz.poles,stage);
			
			//DecimalFormat f = new java.text.DecimalFormat("+0.0");
			String[] sensitivityHeader = new String[2];
			sensitivityHeader[0] = "CONSTANT";
			float constant = 1.0f;
			constant *= stage.the_normalization[0].ao_normalization_factor;
			
			if(stage.type == TransferType.ANALOG) {
				constant *= Math.pow(2 * PI,
					    (double)(pz.poles.length - pz.zeros.length));
			}
			constant *= response.the_sensitivity.sensitivity_factor;
			sensitivityHeader[1] = ""+ constant;
			textTable.addRow(sensitivityHeader);
			
		}catch(ChannelNotFound cnf) {
			GlobalExceptionHandler.handle("Error while getting polezero response",cnf);
			
		}
		return textTable.toString();
	}
	
	static void addToTable(TextTable table,String[] header,ComplexNumberErrored[] values,Stage stage) {
		table.addRow(header);
		java.text.DecimalFormat f = new java.text.DecimalFormat("0.0000;-0.0000");
		for(int cnt=0;cnt<values.length;cnt++) {
			float real = values[cnt].real;
			float imag = values[cnt].imaginary;
			if(stage.type == TransferType.ANALOG) {
				real *=  2 * PI;
				imag *=  2 * PI;
			}
			if(values[cnt].real != 0 || values[cnt].imaginary != 0) {
				String[] realImg = new String[2];
				realImg[0] = f.format(real);
				realImg[1] = f.format(imag);
				table.addRow(realImg);
			}
		}
	}
	
	public static final float PI = 3.14159f;
}
