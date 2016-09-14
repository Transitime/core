/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitime.core.predictiongenerator;

import java.util.ArrayList;
import java.util.List;

import org.transitime.config.StringConfigValue;
import org.transitime.core.Indices;
import org.transitime.core.PredictionGeneratorDefaultImpl;
import org.transitime.db.structs.AvlReport;
import org.transitime.utils.ClassInstantiator;

public class CascadingPredictionGenerator extends PredictionGeneratorDefaultImpl implements PredictionComponentElementsGenerator {

	private static StringConfigValue classNames = 
			new StringConfigValue("transitime.core.predictionGeneratorClasses", 
					"org.transitime.core.predictiongenerator.kalman.KalmanPredictionGeneratorImpl,"
					+ "org.transitime.core.predictiongenerator.average.HistoricalAveragePredictionGeneratorImpl,"
					+ "org.transitime.core.predictiongenerator.lastvehicle.LastVehiclePredictionGeneratorImpl",
					"Specifies, in order, the names of the classes used for generating " +
					"prediction data.");
	
	private PredictionComponentElementsGenerator defaultGenerator = new PredictionGeneratorDefaultImpl();
	
	private List<PredictionComponentElementsGenerator> generators;
	
	public CascadingPredictionGenerator() {
		generators = new ArrayList<PredictionComponentElementsGenerator>();
		for (String name : classNames.getValue().split(",")) {
			generators.add(getInstance(name));
		}
	}
	
	@Override
	public long getTravelTimeForPath(Indices indices, AvlReport avlReport) {
		for (PredictionComponentElementsGenerator generator : generators) {
			if (generator.hasDataForPath(indices, avlReport)) {
				return generator.getTravelTimeForPath(indices, avlReport);
			}
		}
		return defaultGenerator.getTravelTimeForPath(indices, avlReport);
	}	

	@Override
	public long getStopTimeForPath(Indices indices, AvlReport avlReport) {
		for (PredictionComponentElementsGenerator generator : generators) {
			if (generator.hasDataForPath(indices, avlReport)) {
				return generator.getStopTimeForPath(indices, avlReport);
			}
		}
		return defaultGenerator.getStopTimeForPath(indices, avlReport);
	}
	
	private static PredictionComponentElementsGenerator getInstance(String name) {
		 return ClassInstantiator.instantiate(name, PredictionComponentElementsGenerator.class);
	}
}
