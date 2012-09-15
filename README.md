VIRP (Value In Right Place)
===========================

An ORM for [Cassandra](http://cassandra.apache.org/). VIRP offers basic CRUD suppor, but was just my excuess to play with Cassandra and is no longer in active development.

Features
--------

* Annotation driven mapping
* Classes mapped to rows with support for primitive types and strings
* Time to live support - per class for row or per property for column. Property ttl's may be defined by
  a static annotation, or by another integer property.
* Updates via setting properties on a row attached to a session (configurable)
* Both string and numeric column names

Getting started
---------------

Documentation is currently well, lacking to say the least & at this time no binaries have been published, so you'll
need at least haven maven installed to build and use VIRP. The sample app demos some basic operations & uses spring
to setup a VirpConfig - so take a look at the sample app, clone the project, run mvn install and add virp-hector
to your list of dependencies.

Note that VIRP's not trying to be a JPA implementation for Cassandra, and as such it doesn't share any of JPA's
annotations such as @Entity and @Column - all annotations VIRP uses can be found in the com.jshnd.vipr.annotation
package.

VIRP's future
-------------

Its pretty bleak - although it does do some things that the other cassandra ORM's that are out there dont do 
(ttls), it would be more productive to add those features to an established project rather than continue maintenance.
