package org.transitclock.core.predictiongenerator.bias;

import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

public class BiasAdjusterFactory {
	private static BiasAdjuster singleton=null;
	
	// The name of the class to instantiate
	private static StringConfigValue className = new StringConfigValue("transitclock.core.predictiongenerator.biasabjuster",
				"org.transitclock.core.predictiongenerator.bias.ExponentialBiasAdjuster",
				"Specifies the name of the class used to adjust the bias of a predction.");
	
	public static BiasAdjuster getInstance()
	{
		if(className!=null && className.getValue()!=null && className.getValue().length()>0)
		{
			if(singleton==null)
				singleton=ClassInstantiator.instantiate(className.getValue(), BiasAdjuster.class);
			return singleton;
		
		}else
		{
			return null;
		}
	}
}
