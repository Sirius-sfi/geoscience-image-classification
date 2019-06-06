FROM tomcat:9.0.14-jre8

# replace Tomcat's root application with our war-file
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/ROOT"]
ADD target/geoscience-image-classification-0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# add SemFacet application to Tomcat
ADD semfacet/semFacet-gic.war /usr/local/tomcat/webapps/semFacet.war


# remove standard tomcat-users and replace with the proved one with manager-access - DEBUG ONLY
#RUN ["rm", "-fr", "/usr/local/tomcat/conf/tomcat-users.xml"]
#ADD semfacet/tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml

# remove manager's standard context and replace with the proved one without access restriction - DEBUG ONLY
#RUN ["rm", "-fr", "/usr/local/tomcat/webapps/manager/context.xml"]
#ADD semfacet/context.xml /usr/local/tomcat/webapps/manager/META-INF/context.xml


# copy the setenv file which sets the java system property for the workingDirectory into Tomcat
ADD target/geoscience-image-classification-0.1-SNAPSHOT/resources/setenv.sh /usr/local/tomcat/bin/

CMD ["catalina.sh", "run"]