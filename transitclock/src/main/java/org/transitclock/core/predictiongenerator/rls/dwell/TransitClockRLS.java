package org.transitclock.core.predictiongenerator.rls.dwell;

import java.io.Serializable;

import org.transitclock.config.DoubleConfigValue;

import smile.regression.RLS;

public class TransitClockRLS implements Serializable {


	/**
	 * @param lambda 
	 * 
	 */
	public TransitClockRLS(double lambda) {		
		super();
		this.lambda=lambda;
	}
	
	public RLS getRls() {
		return rls;
	}
	private double lambda;
	Double firstx=null;
	Long firsty=null;
	RLS rls=null;
	private static final long serialVersionUID = -5863984357400905560L;

	
	
	public void addSample(double d, long dwellTime)
	{
		
		if(firstx==null&&firsty==null)
		{
			this.firstx=d;
			this.firsty=dwellTime;
		}else
		{
			
						
								
			if(rls==null)
			{
				double samplex[][]=new double[2][1];
				double sampley[]=new double[2];
				
				samplex[0][0]=firstx;
				sampley[0]=firsty;						
				
				samplex[1][0]=d;
				sampley[1]=dwellTime;	
				rls=new RLS(samplex, sampley, lambda);
			}
			else
			{
				double samplex[][]=new double[1][1];
				double sampley[]=new double[1];										
				
				samplex[0][0]=d;
				sampley[0]=dwellTime;
				
				rls.learn(samplex, sampley);
			}
		}
	}

}
