package org.transitclock.core.predictiongenerator.scheduled.dwell;

import java.io.Serializable;

import org.transitclock.config.DoubleConfigValue;
import org.transitclock.core.predictiongenerator.scheduled.dwell.rls.TransitClockRLS;
/**
 * 
 * @author scrudden
 *  
 *
 */
public class DwellRLS implements DwellModel,Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9082591970192068672L;

	private TransitClockRLS rls = null;
	
	public TransitClockRLS getRls() {
		return rls;
	}

	public void setRls(TransitClockRLS rls) {
		this.rls = rls;
	}

	private static DoubleConfigValue lambda = new DoubleConfigValue("transitclock.prediction.rls.lambda", 0.75, "This sets the rate at which the RLS algorithm forgets old values. Value are between 0 and 1. With 0 being the most forgetful.");
	
	
	public DwellRLS() {
		super();
		rls=new TransitClockRLS(lambda.getValue());
	}

	@Override
	public Integer predict(Integer headway, Integer demand) {		
		double[] arg0 = new double[1];
		arg0[0]=headway;
		if(rls.getRls()!=null)		
			return (int) Math.pow(10, rls.getRls().predict(arg0));
		else
			return null;
	}

	@Override
	public void putSample(Integer dwelltime, Integer headway, Integer demand) {

		rls.addSample(headway, Math.log10(dwelltime));
	}

}
