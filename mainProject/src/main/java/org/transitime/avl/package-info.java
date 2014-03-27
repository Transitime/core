/**
 * Goal for the AVL module software was to be able to easily handle many
 * different types of feeds. This includes XML pull feeds where a thread
 * pulls data from XML feed on regular interval, XML push feed where the
 * client writes XML data to a website, and more. Also wanted test projects
 * to easily be able to read from a feed.
 * 
 * To satisfy these goals JMS is used for the feeds. This allows for
 * multiple subscribers so can easily have test projects using the same
 * AVL data. Also allows components of the feed to reside on different
 * servers so can easily have a pull feed on the predictor machine or a
 * push feed on a separate web server machine.
 * 
 * An AVL feed inherits from the org.transitime.modules.Module class. This
 * means it is very easy to start. Also means that the Module.getProjectId()
 * method is used specify which database the data is to be written to and
 * the name of the JMS topic for the feed. 
 * 
 * To startup a pull AVL feed (such as for the NextBus feed) one simply
 * does Module.start("org.transitime.avl.NextBusAvlModule"); 
 * The module will automatically run the AVL feed in a separate thread.
 * Module.projectId() will be used to determine the name of the db to use.
 * The Module.projectId() in turn gets the projectId from  
 * CoreConfig.getProjectId().
 * 
 * The JMS topic name used for the AVL feed is the same of the projectId.
 * The topic names are dynamically configured so that one doesn't need
 * to update a configuration file when a new agency is dealt with.
 * 
 */
package org.transitime.avl;
import org.transitime.configData.CoreConfig;
import org.transitime.modules.Module;
