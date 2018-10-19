# Geoscience Image Classification - Preliminary Readme

## Project Setup

#### JWT
Unfortunately the JWT library we use as a frontend is currently not available in public Maven repositories.
For now, we include the original JAR files as downloaded from here: https://github.com/emweb/jwt/archive/3.3.11.zip

To use it, the library must be added to the local Maven repository by executing the following two commands from the same directory the pom-file can be found in:

`mvn install:install-file -Dfile=lib/jwt/jwt-3.3.11.jar -DpomFile=lib/jwt/jwt-3.3.11.pom`

`mvn install:install-file -Dfile=lib/jwt/jwt-auth-3.3.11.jar -DpomFile=lib/jwt/jwt-auth-3.3.11.pom`

#### TODO: Backend requirements



**After successful completion of these commands, the Maven build should work using standard Maven commands such as `mvn clean install`.**
