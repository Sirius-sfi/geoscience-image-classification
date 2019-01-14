FROM tomcat:9.0.14-jre8

# replace Tomcat's root application with our war-file
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/ROOT"]
ADD target/geoscience-image-classification-0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# copy the setenv file which sets the java system property for the workingDirectory into Tomcat
ADD target/geoscience-image-classification-0.1-SNAPSHOT/resources/setenv.sh /usr/local/tomcat/bin/

CMD ["catalina.sh", "run"]