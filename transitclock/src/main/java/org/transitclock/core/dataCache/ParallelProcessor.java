package org.transitclock.core.dataCache;


import org.hibernate.Criteria;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.dataCache.ehcache.StopArrivalDepartureCache;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ArrivalDeparture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

/**
 * Processor for managing a thread queue of parallel active work.  Currently used for
 * Cache loading on startup.
 */
public class ParallelProcessor {

    public static IntegerConfigValue parallelThreads = new IntegerConfigValue("transitclock.core.parallelThreads",
            Runtime.getRuntime().availableProcessors(),
            "Number of threads to run in parallel for cache loading.  Default uses all cpu, 1 is no parallelism at all");
    private static final Logger logger = LoggerFactory
            .getLogger(ParallelProcessor.class);

    private List<ParallelTask> list = Collections.synchronizedList(new ArrayList<ParallelTask>());
    private ArrayBlockingQueue<TaskWrapper> runQueue;
    private boolean shutDown = false;
    private long startTime;

    public ParallelProcessor() {
        int parallelThreadCount = Runtime.getRuntime().availableProcessors();
        if (parallelThreads.getValue() > 0)
            parallelThreadCount = parallelThreads.getValue();
        runQueue = new ArrayBlockingQueue<TaskWrapper>(parallelThreadCount);
    }

    /**
     * add a task to the queue.
     * @param task
     */
    public void enqueue(ParallelTask task) {
        list.add(task);
    }

    /**
     * signal that running threads should exit.
     */
    public void shutdown() {
        shutDown = true;
    }

    /**
     * milliseconds that processor has been running for.
     * @return
     */
    public long getRuntime() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * if all tasks are complete.  An exception counts as complete.
     * @return
     */
    public boolean isDone() {
        return runQueue.isEmpty() && list.isEmpty();
    }

    /**
     * how many threads are actively running.
     * @return
     */
    public int getRunQueueSize() {
        return runQueue.size();
    }

    /**
     * how many threads are waiting to run.
     * @return
     */
    public int getWaitQueueSize() {
        return list.size();
    }

    /**
     * start processing.  Use shutdown() to cleanly shutdown.
     */
    public void startup() {
        startTime = System.currentTimeMillis();
        startRunThread();
    }

    private void startRunThread() {
        RunThread rt = new RunThread(this);
        new Thread(rt).start();
    }


    private void remove(TaskWrapper tw) {
        runQueue.remove(tw);
    }

    public String getDebugInfo() {
        TaskWrapper taskWrapper = runQueue.peek();
        if (taskWrapper != null)
            return taskWrapper.task.toString();
        return "(none)";
    }

    /**
     * asynchronously retrieve list of ArrivalDepartures from database via a background thread.
     */
    public Callable<List<ArrivalDeparture>> asyncQuery(Date startDate, Date endDate) {
        return new QueryThread(startDate, endDate);
    }


    /**
     * add waiting jobs to the run queue, launching them in a new thread
     * as the runqueue becomes available.
     */
    public static class RunThread implements Runnable {
        private ParallelProcessor pp;
        public RunThread(ParallelProcessor pp) {
            this.pp = pp;
        }

        public void run() {
            Thread.currentThread().setName("Parallel Processor Run Thread");
            int taskCount = 0;
            while (!pp.shutDown) {

                if (!pp.list.isEmpty()) {
                    ParallelTask toRun = pp.list.remove(0);
                    TaskWrapper tw = new TaskWrapper(pp, toRun, taskCount);
                    boolean success = false;
                    while (!success) {
                        success = pp.runQueue.offer(tw);
                        if (!success) {
                            try {
                                logger.debug("waiting to offer task {} to run queue", taskCount);
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {
                                pp.shutDown = true;
                                return;
                            }
                        }
                    }
                    logger.debug("inserted running task {}...", taskCount);
                    new Thread(tw).start();
                    taskCount++;

                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    logger.error("exception {}", ie.toString(), ie);
                }
            }
            logger.info("CACHE COMPLETE:  exiting after {} s ", pp.getRuntime()/1000);
        }
    }

    /**
     * Wrapper around the task that can be safely run as a thread.
     */
    public static class TaskWrapper implements Runnable {
        private ParallelProcessor pp;
        private ParallelTask task;
        private int taskNumber;
        private boolean started = false;
        private boolean done = false;

        public TaskWrapper(ParallelProcessor pp, ParallelTask task, int taskNumber) {
            this.pp = pp;
            this.task = task;
            this.taskNumber = taskNumber;
        }

        public void run() {
            Thread.currentThread().setName(task.toString() + "-" + taskNumber);
            logger.debug("starting task {}", taskNumber);
            started = true;
            try {
                task.run();
            } catch (Exception e) {
                logger.error("task {} exited with {}", taskNumber, e.toString(), e);
            } finally {
                logger.debug("completing task {}", taskNumber);
                done = true;
                pp.remove(this);
            }
        }
    }

    public static class QueryThread implements Callable<List<ArrivalDeparture>> {

        private Date startDate;
        private Date endDate;
        public QueryThread(Date startDate, Date endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        public List<ArrivalDeparture> call() throws Exception {
            Session session = HibernateUtils.getSession();
            Criteria criteria = session.createCriteria(ArrivalDeparture.class);
            List<ArrivalDeparture> results = StopArrivalDepartureCache.createArrivalDeparturesCriteria(criteria, startDate, endDate);
            return results;
        }
    }
}
