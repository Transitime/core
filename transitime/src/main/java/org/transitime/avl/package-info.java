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

/**
 * Contains the classes associated with AVL feeds including reading data 
 * from an AVL feed and providing it to the main application usually via JMS for live feeds
 * but also by reading data and then directly calling AvlProcessor.processAvlReport(avlReport) for
 * other feeds, especially batch feeds used for debugging or testing.
 * <p>
 * Goal for the AVL module software was to be able to easily handle many
 * different types of live feeds. This includes XML pull feeds where a thread
 * pulls data from XML feed on regular interval, XML push feed where the
 * client writes XML data to a website, and more. Also wanted test projects
 * to easily be able to read from the same feed without having to poll again.
 * Also, for large systems want to be able to use multiple threads for 
 * processing the data so get more throughput per machine.
 * <p>
 * To satisfy these goals for the live feeds JMS can be used so that the feed
 * and the AVL clients can reside on different servers. This allows for
 * multiple subscribers so can easily have test projects using the same
 * AVL data. Also allows components of the feed to reside on different
 * servers so can easily have a pull feed on the predictor machine or a
 * push feed on a separate web server machine. Since JMS is a queue the
 * client can use multiple threads to read and process the AVL data.
 * But JMS doesn't need to be used
 * if the feed is polled, such that the feed polling and the AVL client
 * can reside on the same server, and you don't want to run JMS server.
 * <p>
 * The JMS topic name used for the AVL feed is the same of the projectId.
 * The topic names are dynamically configured in JMS so that one doesn't need
 * to update a configuration file when a new agency is dealt with. This
 * makes maintaining JMS much simpler.
 * <p>
 * An AVL feed inherits from the org.transitime.modules.Module class. This
 * means it is very easy to start. Also means that the Module.getProjectId()
 * method is used specify which database the data is to be written to and
 * the name of the JMS topic for the feed. This also means that the
 * command line param -Dtransitime.modules.optionalModulesList=XXX is
 * used to start up the desired AVL modules.
 * <p>
 * Configuration parameters for AVL modules are listed in the AvlConfig.java 
 * class. Important params include -Dtransitime.core.storeDataInDatabase=false
 * if the generated data such as arrivals/departures should not be stored
 * in the database. This is important for when in playback mode for 
 * debugging or such. Another important parameter is 
 * -Dtransitime.avl.shouldUseJms=true if you want to use JMS. In that case you
 * also need to specify both an AVL feed module and a JMS client module, such as
 * -Dtransitime.modules.optionalModulesList=org.transitime.avl.MuniNextBusAvlModule;org.transitime.avl.AvlJmsClientModule
 */
package org.transitime.avl;

