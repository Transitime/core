package org.transitclock.core.dataCache;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDeparture;

public abstract class StopArrivalDepartureCacheInterface {

	abstract  public  List<IpcArrivalDeparture> getStopHistory(StopArrivalDepartureCacheKey key);

	abstract  public StopArrivalDepartureCacheKey putArrivalDeparture(ArrivalDeparture arrivalDeparture);

	public void populateCacheFromDb(Session session, Date startDate, Date endDate) {
		Criteria criteria = session.createCriteria(ArrivalDeparture.class);

		@SuppressWarnings("unchecked")
		List<ArrivalDeparture> results = criteria.add(Restrictions.between("time", startDate, endDate)).addOrder(Order.asc("time")).list();				

		for (ArrivalDeparture result : results) {
			this.putArrivalDeparture(result);
			//TODO might be better with its own populateCacheFromdb
			DwellTimeModelCacheFactory.getInstance().addSample(result);
		}
	}

}