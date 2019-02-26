# Geoscience Image Classification - Preliminary Readme

## Project Setup

#### JWT
Unfortunately the JWT library we use as a frontend is currently not available in public Maven repositories.
For now, we include the original JAR files (including license file) as downloaded from here: https://github.com/emweb/jwt/archive/3.3.11.zip

To use it, the library must be added to the local Maven repository by executing the following two commands from the same directory the pom-file can be found in:

`mvn install:install-file -Dfile=lib/jwt/jwt-3.3.11.jar -DpomFile=lib/jwt/jwt-3.3.11.pom`

`mvn install:install-file -Dfile=lib/jwt/jwt-auth-3.3.11.jar -DpomFile=lib/jwt/jwt-auth-3.3.11.pom`

#### Backend requirements

1. **Basic requirements:** git, maven and java 1.8
2. **Clone repository that deals with the ontology projection:** [https://gitlab.com/ernesto.jimenez.ruiz/ontology-services-toolkit](https://gitlab.com/ernesto.jimenez.ruiz/ontology-services-toolkit)
> git clone https://gitlab.com/ernesto.jimenez.ruiz/ontology-services-toolkit.git
3. **Manage RDFox dependency:** The library "ontology-services-toolkit" makes use of RDFox which is platform dependent. 
	1. In the lib folder, the different pre-compiled libraries for RDFox are provided.
	2. Follow the instructions in lib/mvn_install_jrdfox.txt to install the RDFox library in the local maven repository.
	For example, after selecting the JRDFox jar file according to yoru distribution:
	`mvn install:install-file -Dfile=/home/ernesto/git/ontology-services-toolkit/lib/JRDFox-mac.jar -DgroupId=uk.ox.jrdfox -DartifactId=jrdfox -Dversion=1.2776.2017 -Dpackaging=jar`
4. **Run mvn install** under the ontology-services-toolkit project:
> mvn clean install



**After successful completion of these commands, the Maven build should work using standard Maven commands such as `mvn clean install`.**

## License
This project is licensed under the terms of the GNU General Public License version 2
as published by the Free Software Foundation.

## Open source software used
- **JWt web GUI library**

    Product website: https://www.webtoolkit.eu/jwt
    
    Download the sources from: https://github.com/emweb/jwt/archive/3.3.11.zip

- **Ontology services toolkit:** https://gitlab.com/ernesto.jimenez.ruiz/ontology-services-toolkit
