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
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitime.avl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.AvlReport;

/**
 * A queue of AvlClient runnables that can be used with a ThreadPoolExecutor.
 * Implements by subclassing a ArrayBlockingQueue<Runnable> where the Runnable
 * is an AvlClient. Also keeps track of the last AVL report per vehicle. When
 * getting data from queue, if the data is obsolete (a new AVL report has been
 * received for the vehicle) then that element from the queue is thrown out and
 * the next item is retrieved until a non-obsolete one is found.
 * <p>
 * Extended ArrayBlockingQueue class so that don't have to create a blocking
 * queue from scratch.
 * <p>
 * Note: wanted to extend from ArrayBlockingQueue<AvlClient> but that didn't
 * work for the ThreadPoolExecutor which expects a BlockingQueue<Runnable>. So
 * had to resort to doing ugly casts.
 * 
 * @author SkiBu Smith
 *
 */
public class AvlQueue extends ArrayBlockingQueue<Runnable> {

	// For keeping track of the last AVL report for each vehicle. Used to
	// determine if AVL report from queue is obsolete.
	ConcurrentMap<String, AvlReport> avlDataPerVehicleMap =
			new ConcurrentHashMap<String, AvlReport>();

	private static final long serialVersionUID = 6587642826604552096L;

	private static final Logger logger = LoggerFactory
			.getLogger(AvlQueue.class);

	/********************** Member Functions **************************/

	/**
	 * Constructs the queue to have specified size.
	 * 
	 * @param queueSize
	 *            How many AVL elements can be put into the queue before it
	 *            blocks.
	 */
	public AvlQueue(int queueSize) {
		super(queueSize);
	}

	/**
	 * Adds the AVL report to the map of last AVL report for each vehicle
	 * 
	 * @param runnable the AvlClient
	 */
	private void addToAvlDataPerVehicleMap(Runnable runnable) {
		if (!(runnable instanceof AvlClient))
			throw new IllegalArgumentException("Runnable must be AvlClient.");
		
		AvlReport avlReport = ((AvlClient) runnable).getAvlReport();
		avlDataPerVehicleMap.put(avlReport.getVehicleId(), avlReport);
	}

	/**
	 * Returns true if the AVL report is older than the latest one for the
	 * vehicle and is therefore obsolete and doesn't need to be processed.
	 * 
	 * @param avlReportFromQueue
	 *            AvlClient from the queue containing an AvlReport
	 * @return true of obsolete
	 */
	private boolean isObsolete(Runnable runnableFromQueue) {
		if (!(runnableFromQueue instanceof AvlClient))
			throw new IllegalArgumentException("Runnable must be AvlClient.");

		AvlReport avlReportFromQueue =
				((AvlClient) runnableFromQueue).getAvlReport();
		
		AvlReport lastAvlReportForVehicle =
				avlDataPerVehicleMap.get(avlReportFromQueue.getVehicleId());
		boolean obsolete =
				lastAvlReportForVehicle != null
						&& avlReportFromQueue.getTime() < lastAvlReportForVehicle
								.getTime();
		if (obsolete) {
			logger.debug("AVL report from queue is obsolete (there is a newer "
					+ "one for the vehicle). Therefore ignoring this report so "
					+ "can move on to next valid report for another vehicle. "
					+ "From queue {}. Last AVL report in map {}. Size of queue "
					+ "is {}",	
					avlReportFromQueue, lastAvlReportForVehicle, size());
		}
		return obsolete;
	}
	
	/**
	 * Calls superclass add() method but also updates the AVL data per vehicle
	 * map. Doesn't seem to be used by ThreadPoolExecutor but still included
	 * for completeness.
	 */
	@Override
	public boolean add(Runnable runnable) {
		addToAvlDataPerVehicleMap(runnable);
		return super.add(runnable);
	}

	/**
	 * Calls superclass put() method but also updates the AVL data per vehicle
	 * map. Doesn't seem to be used by ThreadPoolExecutor but still included
	 * for completeness.
	 */
	@Override
	public void put(Runnable runnable) throws InterruptedException {
		super.put(runnable);
		addToAvlDataPerVehicleMap(runnable);
	}

	/**
	 * Calls superclass offer() method but also updates the AVL data per vehicle
	 * map. Used by ThreadPoolExecutor.
	 */
	@Override
	public boolean offer(Runnable runnable) {
		AvlReport avlReport = ((AvlClient)runnable).getAvlReport();
		logger.debug("offer() remainingCapacity={} {}", 
				remainingCapacity(), avlReport);

		boolean successful = super.offer(runnable);
		if (successful)
			addToAvlDataPerVehicleMap(runnable);
		
		logger.debug("offer() returned {} for {}", 
				successful, avlReport);
		return successful;
	}

	/**
	 * Calls superclass offer(timeout, unit) method but also updates the AVL
	 * data per vehicle map. Doesn't seem to be used by ThreadPoolExecutor but
	 * still included for completeness.
	 */
	@Override
	public boolean offer(Runnable runnable, long timeout, TimeUnit unit)
			throws InterruptedException {
		boolean successful = super.offer(runnable, timeout, unit);
		if (successful)
			addToAvlDataPerVehicleMap(runnable);
		
		return successful; 
	}

	/**
	 * Calls superclass poll() method until it gets AVL data that is not
	 * obsolete. Doesn't seem to be used by ThreadPoolExecutor but still
	 * included for completeness.
	 */
	@Override
	public Runnable poll() {
		Runnable runnable;
		do {
			runnable = super.poll();
		} while (isObsolete(runnable));
		return runnable;
	}

	/**
	 * Calls superclass poll(timeout, unit) method until it gets AVL data that
	 * is not obsolete. Used by ThreadPoolExecutor.
	 */
	@Override
	public Runnable poll(long timeout, TimeUnit unit)
			throws InterruptedException {
		logger.debug("In poll(t,u) timeout={} units={}", timeout, unit);

		Runnable runnable;
		do {
			runnable = super.poll(timeout, unit);
		} while (runnable != null && isObsolete(runnable));

		if (runnable != null) {
			logger.debug("poll(t,u) in AvlQueue returned {}",
					((AvlClient) runnable).getAvlReport());
		}
		return runnable;
	}

	/**
	 * Calls superclass take() method until it gets AVL data that is not
	 * obsolete. Doesn't seem to be used by ThreadPoolExecutor but
	 * still included for completeness.
	 */
	@Override
	public Runnable take() throws InterruptedException {
		Runnable runnable;
		do {
			runnable = super.take();
		} while (isObsolete(runnable));
		return runnable;
	}
}
