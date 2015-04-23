#!/bin/bash

export TOMCAT_INSTALL_DIR="/var/lib/tomcat7"
export GIT_URL="https://github.com/giagiannis/cloud-benchmark-apps"

configure(){
JAVA_DEFAULT=$(update-java-alternatives -l | grep 7 | awk '{print  $1}')
update-java-alternatives -s $JAVA_DEFAULT

MEMORY_MB=$(free -m | grep -i mem | awk '{print $2}')
JAVA_DEFAULT=$(update-java-alternatives -l | grep 7 | awk '{print  $3}')
sed -i "s|-Xmx128m|-Xmx${MEMORY_MB}m|g" /etc/default/tomcat7
sed -i "s|#JAVA_HOME=/usr/lib/jvm/openjdk-6-jdk|JAVA_HOME=$JAVA_DEFAULT|g" /etc/default/tomcat7

service tomcat7 restart

git clone $GIT_URL /tmp/webapps/
mvn -f /tmp/webapps/webapp-java/pom.xml package >> /tmp/mvn_logs.txt
mv /tmp/webapps/webapp-java/target/webapp-java-1.0.war $TOMCAT_INSTALL_DIR/webapps/webapp-java.war
rm -rf /tmp/webapps/
}

configure