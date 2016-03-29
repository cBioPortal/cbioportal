# Dockerfile for cBioPortal
FROM elementolab/jamm:1.2
MAINTAINER Alexandros Sigaras <als2076@med.cornell.edu>
#============== MySQL Setup ================#
RUN chkconfig mysqld on; service mysqld start; \
	mysql_secret=$(cat /var/log/mysqld.log | grep "A temporary password is generated for" | awk '{print $NF}'); \
	new_pass="P@ssword1"; cbio_user="cbio"; cbio_pass="P@ssword1"; \
	mysql -u root -p$mysql_secret --connect-expired-password -e "set password=password('$new_pass'); flush privileges;"; \
	mysql -u root -p$new_pass -e "create database cbioportal; create database cgds_test; CREATE USER '$cbio_user'@'localhost' IDENTIFIED BY '$cbio_pass'; GRANT ALL ON cbioportal.* TO '$cbio_user'@'localhost'; GRANT ALL ON cgds_test.* TO '$cbio_user'@'localhost'; flush privileges;" ;
#== Set Default Config & Build from Source ==#
ADD / /cbioportal
RUN	echo "export PORTAL_HOME=/cbioportal" >> /root/.bashrc; . /root/.bashrc; \
	cp $PORTAL_HOME/src/main/resources/portal.properties.EXAMPLE $PORTAL_HOME/src/main/resources/portal.properties; \
	cp $PORTAL_HOME/src/main/resources/log4j.properties.EXAMPLE $PORTAL_HOME/src/main/resources/log4j.properties; \
	cp -u --force $PORTAL_HOME/docker/build/context.xml $CATALINA_HOME/conf/context.xml; \
	cp -u --force $PORTAL_HOME/docker/build/gene_sets.txt $PORTAL_HOME/core/src/main/resources/; \
	mkdir /root/.m2/; cp /cbioportal/docker/build/settings.xml ~/.m2/; \
	cd $PORTAL_HOME; mvn -DskipTests clean install
#======== Copy cBioPortal .war file ========#
RUN . /root/.bashrc; cp $PORTAL_HOME/portal/target/cbioportal.war $CATALINA_HOME/webapps/
#======== cBioPortal Startup ===============#
CMD . /root/.bashrc; /bin/bash $PORTAL_HOME/docker/scripts/entrypoint.sh;
#===========================================#