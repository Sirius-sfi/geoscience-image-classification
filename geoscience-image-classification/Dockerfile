FROM tomcat:9.0.14-jre8

# replace Tomcat's root application with our war-file
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/ROOT"]
ADD target/geoscience-image-classification-0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

CMD ["catalina.sh", "run"]