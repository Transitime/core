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

package org.transitclock.core.predictiongenerator;

import org.transitclock.config.StringConfigValue;
import org.transitclock.core.Indices;
import org.transitclock.core.PredictionGeneratorDefaultImpl;
import org.transitclock.core.VehicleState;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.utils.ClassInstantiator;

import java.util.ArrayList;
import java.util.List;

public class CascadingPredictionGenerator extends PredictionGeneratorDefaultImpl implements PredictionComponentElementsGenerator {

	private static StringConfigValue classNames = 
			new StringConfigValue("transitclock.core.predictionGeneratorClasses",
					"org.transitclock.core.predictiongenerator.kalman.KalmanPredictionGeneratorImpl,"
					+ "org.transitclock.core.predictiongenerator.average.HistoricalAveragePredictionGeneratorImpl,"
					+ "org.transitclock.core.predictiongenerator.lastvehicle.LastVehiclePredictionGeneratorImpl",
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
	public long getTravelTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {
		for (PredictionComponentElementsGenerator generator : generators) {
			if (generator.hasDataForPath(indices, avlReport)) {
				return generator.getTravelTimeForPath(indices, avlReport, vehicleState);
			}
		}
		return defaultGenerator.getTravelTimeForPath(indices, avlReport,vehicleState);
	}	

	@Override
	public long getStopTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {
		for (PredictionComponentElementsGenerator generator : generators) {
			if (generator.hasDataForPath(indices, avlReport)) {
				return generator.getStopTimeForPath(indices, avlReport, vehicleState);
			}
		}
		return defaultGenerator.getStopTimeForPath(indices, avlReport, vehicleState);
	}
	
	private static PredictionComponentElementsGenerator getInstance(String name) {
		 return ClassInstantiator.instantiate(name, PredictionComponentElementsGenerator.class);
	}
}
