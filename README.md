About
=======================

This package contains common functionality shared between EBF java projects.  It is intended to be used as a maven dependency.

Currently, the following projects depend on java.utilities

 - telekom.billing
 - telekom.billing.collector
 - telekom.billing.xtc

How to deploy
=======================

		mvn deploy

This will package the project into a jar and upload it to the configured github maven repository currently located at:

 [https://github.com/ebf/java.utilities.maven](https://github.com/ebf/java.utilities.maven)