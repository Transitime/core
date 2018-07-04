package org.transitclock.core.dataCache;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.transitclock.db.structs.ArrivalDeparture;

public abstract class StopArrivalDepartureCacheInterface {

	abstract  public  List<ArrivalDeparture> getStopHistory(StopArrivalDepartureCacheKey key);

	abstract  public StopArrivalDepartureCacheKey putArrivalDeparture(ArrivalDeparture arrivalDeparture);

	public void populateCacheFromDb(Session session, Date startDate, Date endDate) {
		Criteria criteria = session.createCriteria(ArrivalDeparture.class);

		@SuppressWarnings("unchecked")
		List<ArrivalDeparture> results = criteria.add(Restrictions.between("time", startDate, endDate)).list();

		for (ArrivalDeparture result : results) {
			this.putArrivalDeparture(result);
		}
	}

}