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
 The org.transitime.ipc.rmi package is for making it easy to have remote RMI 
 access to objects on the server. In particular, it is intended for "command"
 type of objects that do not change. They are simply used for changing
 state of a server or reading information from it.
 <p>
 The RMI registry needs to be run for this to work. Because don't want to 
 interfere with other possible uses of the RMI registry a special port 2099 is 
 used instead of the default 1099. Since it can be a nuisance to make sure that
 the rmiregistry is running the registry will be started automatically if it  
 isn't currently running. If you want to run it manually, the registry comes
 with the JDK, which means the executable can be found in a place such as
 C:/Program Files/Java/jdk1.7.0_25/bin/rmiregistry.exe . With changes to the
 RMI system with version 1.7_20 of Java you need to specify the codebase
 if you run the rmiregistry manually. It would be started using something like:
 <code>./rmiregistry.exe -J-Djava.rmi.server.codebase=file:/Users/Mike/git/testProject/testProject/bin/ 2099</code>
 <p>
 The purpose of these classes is to make the system robust and easy to use.
 The server not only initializes the object but also will reconnect if the
 registry restarts for some reason. On the client side the objects are
 initialized and when a remote call is made an InvocationHandler is used
 to both instrument the call but also to handle exceptions and retry
 communication as appropriate. If an RMI stub needs to be recreated
 due to server restart etc it is automatically redone. This means that
 the connection to the server is robust and easy to use.
 <p>
 Proxy objects are used so that each RMI call is instrumented. Since each
 RMI object is a proxy RmiCallInvocationHandler.invoke() is called for
 each RMI call. In invoke() the calls are logged and timed. Also, 
 the number of current calls to a project are limited so that the
 server cannot be overwhelmed. This prevents problems due to denial of
 service attack or the project doing stop the world garbage collecting
 and therefore being unavailable for a while.
 <p>
 To create a RMI object do the following:
 <ul>
 <li> Create an interface class that inherits from java.rmi.Remote class. Each
 method that should be accessible remotely should throw java.rmi.RemoteException.</li>
 <li> Create a server class that extends AbstractServer and implements the interface class.</li>
 <li> The server class needs a constructor that calls super(projectId, objectName)</li>
 <li> The server class should implement the remote methods defined in the interface.</li>
 <li> For the client create an object such as (for the "Hello" interface class in
 this example) Hello hello = ClientFactory.getInstance(projectId, Hello.class);</li>
 </ul>
 <p>
 Exceptions:
 <ul>
 <li>If get a ConnectException then likely the RMI registry is not running.</li>
 <li>If get NotBoundException then the server object hasn't been created</li>
 </ul>
 
  @author SkiBu Smith
 */
package org.transitime.ipc.rmi;