package org.transitclock.avl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.feed.zmq.ZmqQueueReaderFactory;
import org.transitclock.modules.Module;
import org.zeromq.ZMQ;

import java.io.InputStream;
import java.util.Collection;

public class ZeroMQAvlModule extends PollUrlAvlModule {

    private static final Logger logger = LoggerFactory
            .getLogger(ZeroMQAvlModule.class);

    protected ZMQ.Context _context = null;
    protected ZMQ.Socket _socket = null;
    protected ZMQ.Poller _poller = null;


    public static StringConfigValue zeromqHost =
            new StringConfigValue("transitclock.avl.zeromq.host",
                    "queue.dev.obanyc.com",
                    "The host of the ZeroMQ queue to use.");

    public static IntegerConfigValue zeromqPort =
            new IntegerConfigValue("transitclock.avl.zeromq.port",
                    5567,
                    "The port of the ZeroMQ queue to use.");

    public static StringConfigValue zeromqTopic =
            new StringConfigValue("transitclock.avl.zeromq.topic",
                    "inference_queue",
                    "The topic of the ZeroMQ queue to use.");


    // Initialize ZMQ with the given args
    protected synchronized void initializeQueue(String host, String queueName,
                                                Integer port) {
        String bind = "tcp://" + host + ":" + port;
        logger.warn("binding to " + bind + " with topic=" + queueName);

        if (_context == null) {
            _context = ZMQ.context(1);
            _socket = _context.socket(ZMQ.SUB);
            _poller = _context.poller(2);
            _poller.register(_socket, ZMQ.Poller.POLLIN);
            _socket.connect(bind);
            _socket.subscribe(queueName.getBytes());
            logger.warn("queue " + queueName + " is listening on " + bind);
        }
    }

    public ZeroMQAvlModule(String agencyId){
        super(agencyId);
        initializeQueue(zeromqHost.getValue(), zeromqTopic.getValue(), zeromqPort.getValue());
    }

    /**
     * Reads and processes the data. Called by AvlModule.run().
     * Reading from ZeroMQ does use InputStream so overriding
     * getAndProcessData().
     */
    @Override
    protected void getAndProcessData() {

        // prefer a java sleep to a native block
        _poller.poll(0 * 1000); // microseconds for 2.2, milliseconds for 3.0
        if (_poller.pollin(0)) {

            String address = new String(_socket.recv(0));
            byte[] buff = _socket.recv(0);

            if(address == null || !address.equals(zeromqTopic.getValue())){
                return;
            }

            try {
                String contents = new String(buff);

                // Convert to an AvlReport
                AvlReport avlReport = ZmqQueueReaderFactory.getInstance().getAvlReport(zeromqTopic.getValue(), contents);

                // Process the individual AVL Report
                processAvlReport(avlReport);

            } catch(Exception ex) {
                logger.error("#####>>>>> processMessage() failed, exception was: " + ex.getMessage(), ex);
            }

        } else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.warn("exiting ZMQ thread (interrupted)");
                return;
            }
        }
    }

    @Override
    protected Collection<AvlReport> processData(InputStream in) throws Exception {
        return null;
    }

    /**
     * Just for debugging
     */
    public static void main(String[] args) {
        // Create a ZeroMQAvlModule for testing
        Module.start("org.transitclock.avl.ZeroMQAvlModule");
    }
}
