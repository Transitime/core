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

/**
 * Notes about Hibernate.
 * <p>
 * Hibernate was selected as the ORM (Object Relational Mapping) tool because
 * it is both very widely used yet very simple. Once one gets the hang of it
 * it greatly reduces the amount of code that needs to be written for reading
 * and writing objects.
 * <p>
 * It is intended that annotations be used (as opposed to an xml based schema
 * config file) to specify how an object is mapped to the database. This way
 * the mapping info is directly in the class definition and one doesn't need
 * to deal with a separate file. 
 * <p>
 * Every object to be persisted needs a unique Id per row. That way Hibernate
 * can compare objects for equality and such. This also means that each
 * table should have ah primary key, which of course is also expected for any
 * table in a relational database. If multiple columns are needed to establish
 * a unique id/primary key then can simply annotated multiple columns with @Id.
 * It should be noted that can have null values for primary key columns. This
 * is unfortunate because null should be considered a valid identifier. So for
 * AVLReports can't have block be part of the primary key because sometimes
 * it will be null.
 * <p>
 * The primary key will automatically create an index so that the db can 
 * quickly confirm that the object being inserted is unique. So don't
 * need to create a separate index on the primary key column to speed up queries
 * that would benefit from an index on that column. It already exists. But
 * if the primary key is on multiple columns then things are much more 
 * complicated. When multiple columns are used for a primary key then
 * an index is created but most likely it will simply use a concatenation
 * of what is in the two columns. This is adequate for the db to quickly make
 * sure that an object is unique before inserting it but it will not be
 * adequate for speeding up some queries. For example, for AvlReport the
 * primary key is on time and vehicleId. The index can therefore be
 * something like time||vehicleId (concatenation). If one does a query for
 * reports between a certain time frame the index unfortunately won't help
 * since it is really just a string that includes time and vehicleId. Can't
 * find rows based on time. So for this case need a separate index to speed
 * up such queries.
 * <p>
 * Storing time is important yet a nuisance when MySQL is used. Previously
 * MySQL did not support fractional seconds. But we really do want to store
 * msec as part of times. This is especially true for AVL data where need
 * to avoid duplicate key problems with respect to a primary key that uses
 * a timestamp. Fortunately since MySQL 5.6.4 one can specify fractional
 * timestamps (and other time values). See 
 * http://dev.mysql.com/doc/refman/5.6/en/fractional-seconds.html for details.
 * When specifying timestamp need to specify number of digits past the decimal
 * point. The default for MySQL is 0 for backwards compatibility. For other
 * databases the default is 6! So best to explicitly specify the precision
 * to TIMESTAMP(3) (or perhaps 6) so that fractional seconds will work with
 * any database. 
 * <p>
 * The C3P0 db connection pooler is used because the one that comes with
 * Hibernate is not intended for production use. And C3P0 appears to be
 * widely used. 
 * If you want to get rid of the C3P0 status that is printed, by default, 
 * when hibernate starts, you need to recompile C3P0 sources after changing 
 * com.mchange.v2.c3p0.Debug.DEBUG to false. This is a public static final field 
 * that cannot be changed by configuration files.
 * <p>
 * With Hibernate 4.0 ran into lots of problems with the documentation, 
 * including online, being out of date. For example, the Hibernate @Entity
 * tag has been deprecated and one must now use the JPA one by specifying
 * the appropriate import. Even the whole way of creating a session has
 * changed and will continue to change. This is a nuisance, but bearable.
 * <p>
 * One other subtle gotcha with Hibernate is that Sessions are not
 * threadsafe. Don't pass them between threads!
 * 
 * @author SkiBu Smith
 *
 */
package org.transitime.db.hibernate;