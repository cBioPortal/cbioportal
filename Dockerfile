# Dockerfile for cBioPortal
FROM elementolab/jamm:1.3
MAINTAINER Alexandros Sigaras <als2076@med.cornell.edu>
#== Set Default Config & Build from Source ==#
ADD / /cbioportal
RUN	echo "export PORTAL_HOME=/cbioportal" >> /root/.bashrc; . /root/.bashrc; \
	cp $PORTAL_HOME/src/main/resources/portal.properties.EXAMPLE $PORTAL_HOME/src/main/resources/portal.properties; \
	cp $PORTAL_HOME/src/main/resources/log4j.properties.EXAMPLE $PORTAL_HOME/src/main/resources/log4j.properties; \
	/bin/cp -u --force $PORTAL_HOME/docker/build/context.xml $CATALINA_HOME/conf/context.xml; \
	/bin/cp -u --force $PORTAL_HOME/docker/build/gene_sets.txt $PORTAL_HOME/core/src/main/resources/; \
	mkdir /root/.m2/; cp /cbioportal/docker/build/settings.xml /root/.m2/; \
	cd $PORTAL_HOME; mvn -DskipTests clean install
#======== Copy cBioPortal .war file ========#
RUN . /root/.bashrc; cp $PORTAL_HOME/portal/target/cbioportal.war $CATALINA_HOME/webapps/
#======== cBioPortal Startup ===============#
CMD . /root/.bashrc; /bin/bash $PORTAL_HOME/docker/scripts/entrypoint.sh;
#===========================================#