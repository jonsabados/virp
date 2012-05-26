VIRP (Value In Right Place)
===========================

An ORM for [Cassandra](http://cassandra.apache.org/). Currently in in it's infancy but with basic CRUD support.

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

At some point in the near future the following things are likely to be added to Virp:

* Support for per session consistency levels
* Automagic ID generation
* Lazily loading collections of other entities based on a secondary index (maybe...)

If there is something else you would like to see, or if you found a bug go ahead and create an
[issue](https://github.com/jonsabados/virp/issues), or if you feel up to it fork the project, make your changes &
send a pull request.