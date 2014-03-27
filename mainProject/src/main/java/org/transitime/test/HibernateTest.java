/**
 * 
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
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitime.test;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.db.structs.AvlReport;
import org.transitime.utils.IntervalTimer;

/**
 * For testing writing data to database. Compares batch to stateless processing.
 * Makes it easy to see what happens when turn on logging and such.
 * @author SkiBu Smith
 *
 */
public class HibernateTest {


	/********************** Member Functions **************************/
	
	private static final int BATCH_SIZE = 25;
	
	/**
	 * For comparing timing of doing a large number of inserts. Can either
	 * use regular batch mode where there is a specified batch size but
	 * the caching and other internal features are still used. Or can
	 * use a StatelessSession to bypass all of that internal stuff.
	 * This way can easily determine which is the best method.
	 * @param cnt
	 * @param batch
	 */
	private static void timeBatchStore(int cnt, boolean batch) {
		System.out.println((batch?"Batch":"Stateless") + " storing " + cnt + " records");
		IntervalTimer timer = null;
		
		try {
			SessionFactory sessionFactory = HibernateUtils.getSessionFactory("test");
			
			// Start timer after session factory is obtained since first time it is
			// obtained it takes a long time. This timer is used to determine how long
			// things take to run.
			timer = new IntervalTimer();
			
			// Get ready to write
			StatelessSession statelessSession = sessionFactory.openStatelessSession();			
			Session batchSession = sessionFactory.openSession();
			System.out.println("Opening session took " + timer.elapsedMsec() + " msec");
			try {
				Transaction tx = batch ? batchSession.beginTransaction() : statelessSession.beginTransaction();
								
				// Store some AVLReportss
				long initialTime = System.currentTimeMillis();
				for (int i=0; i<cnt; ++i) {
					AvlReport report = new AvlReport((batch?"batch":"stateless")+i, initialTime + i, 1.23, 4.56);
					if (batch)
						batchSession.save(report);
					else
						statelessSession.insert(report);
					if (batch && ((i+1) % BATCH_SIZE == 0)) {
						batchSession.flush();
						batchSession.clear();
					}
				}
				
				// Finish up writing
				tx.commit();
			} catch (Exception e) {
				// If should rollback the transaction it would be done here
				e.printStackTrace();
			} finally {
				// No matter what, close the session
				batchSession.close();				
			}
		} catch (HibernateException e) {
			e.printStackTrace();
		}

		System.out.println((batch?"Batch":"Stateless") + " storing " + cnt + " records took " + 
				timer.elapsedMsec() + " msec");
	}
	
	public static void main(String[] args) {
		int cnt = 20000;
		timeBatchStore(cnt, true);
		timeBatchStore(cnt, false);
		timeBatchStore(cnt, true);
		timeBatchStore(cnt, false);
		timeBatchStore(cnt, true);
		timeBatchStore(cnt, false);
	}
}
