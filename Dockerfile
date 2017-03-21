# Dockerfile for cBioPortal
FROM elementolab/jamm:1.0
MAINTAINER Alexandros Sigaras <als2076@med.cornell.edu>
#===========================#
# Clone cBioPortal			#
#===========================#
#RUN git clone https://github.com/cBioPortal/cbioportal.git; \
ADD / /cbioportal
RUN	echo "export PORTAL_HOME=/cbioportal" >> /root/.bash_profile;
#===========================#
# Setup MySQL				#
#===========================#
RUN chkconfig mysqld on; \
	service mysqld start; \
	mysql_secret=$(cat /var/log/mysqld.log | grep "A temporary password is generated for" | awk '{print $NF}'); \
	new_pass="P@ssword1"; \
	cbio_user="cbio"; \
	cbio_pass="P@ssword1"; \
	mysql -u root -p$mysql_secret --connect-expired-password -e "set password=password('$new_pass'); flush privileges;"; \
	mysql -u root -p$new_pass -e "create database cbioportal; create database cgds_test; CREATE USER '$cbio_user'@'localhost' IDENTIFIED BY '$cbio_pass'; GRANT ALL ON cbioportal.* TO '$cbio_user'@'localhost'; GRANT ALL ON cgds_test.* TO '$cbio_user'@'localhost'; flush privileges;" ;
#===========================#
# ADD Default Config		#
#===========================#
RUN cp /cbioportal/docker/config/* /cbioportal/src/main/resources/; \
	source /root/.bash_profile; /bin/cp -u --force /cbioportal/docker/config/context.xml $CATALINA_HOME/conf/context.xml;
#===========================#
# Build From Source			#
#===========================#
RUN cp /cbioportal/docker/build/gene_sets.txt /cbioportal/core/src/main/resources/; \
	mkdir ~/.m2/; cp /cbioportal/docker/build/settings.xml ~/.m2/; \
	source /root/.bash_profile; cd $PORTAL_HOME; mvn -DskipTests clean install
#===========================#
# Copy cBioPortal .war file	#
#===========================#
RUN source /root/.bash_profile; \
	cp $PORTAL_HOME/portal/target/cbioportal.war $CATALINA_HOME/webapps/
#===========================#
# Startup					#
#===========================#
CMD /bin/bash /cbioportal/docker/scripts/entrypoint.sh;
#===========================================================================================#