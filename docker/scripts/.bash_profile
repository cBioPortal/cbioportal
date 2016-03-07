# .bash_profile

# Get the aliases and functions
if [ -f ~/.bashrc ]; then
	. ~/.bashrc
fi

# User specific environment and startup programs

PATH=$PATH:$HOME/bin

export PATH
export JAVA_HOME=/usr/java/jdk1.8.0_72
export JRE_HOME=/usr/java/jdk1.8.0_72/jre
export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/java/jdk1.8.0_20/bin:/usr/java/jdk1.8.0_72/jre/bin
export CATALINA_HOME=/opt/apache-tomcat-8.0.30
export M2_HOME=/usr/local/apache-maven
export M2=/usr/local/apache-maven/bin
export PATH=/usr/local/apache-maven/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/java/jdk1.8.0_72/bin:/usr/java/jdk1.8.0_72/jre/bin
export PORTAL_HOME=/cbioportal
alias cbio_start="source ~/.bash_profile; sh $CATALINA_HOME/bin/startup.sh && tail -f $CATALINA_HOME/logs/catalina.out"