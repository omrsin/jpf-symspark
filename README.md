JPF-SymSpark: Symbolic Execution of Spark Programs using Java PathFinder
========================================================================

JPF-SymSpark is a **[Java PathFinder (JPF)](http://babelfish.arc.nasa.gov/trac/jpf/wiki/intro/start)** module whose goal is to coordinate the symbolic execution of **[Apache Spark](http://spark.apache.org/)** programs to produce a reduced input dataset that ensures full path coverage on a regular execution. It builds on top of **[Symbolic PathFinder (SPF)](http://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-symbc)** to delegate the handling of symbolic expressions while it focus on how to interconnect Spark's transformations and actions in order to reason coherently over the execution flow of the program.

Table of contents
=================
 
  * [Intallation](#installation)
    * [Docker approach](#docker-approach)
    * [Manual approach](#manual-approach)
      * [Prerequisites](#prerequisites)
      * [jpf-core](#jpf-core)
      * [jpf-symbc](#jpf-symbc)
      * [jpf-symspark](#jpf-symspark)
      * [eclipse-jpf (optional)](#eclipse-jpf-(optional%29))  
  * [Usage](#usage)
  
Installation
============

Docker approach
---------------
**[Docker](https://www.docker.com/)** is a widely supported platform for the creation and maintenance of virtual containers. It is designed to provide self-contained, portable environments that are ideal for distributing software with a fixed set of dependencies.

For this reason, we provide the description of a Docker container that prepares an environment with all the dependencies and configurations required by JPF-SymSpark. This installation method is the preferred approach given its simplicity and tested behavior. The following instructions guide the process for creating the Docker container; Docker is assumed to be installed  already.

1. Clone the \textit{JPF-SymSpark} repository
	```sh
       git clone https://github.com/omrsin/jpf-symbc.git
    ```	
2. Go to the root directory of the project and build the container
 	```sh
       docker build -t jpf-symspark .
    ```	
3. Once the container has been successfully built, run a container shell
	```sh
       docker run -it jpf-symspark
    ```
4. Inside the container, the installation of the module can be validated by running
	```sh
       cd jpf-symspark/src/examples/de/tudarmstadt/thesis/symspark/examples/java/applied
       jpf WordCountExample.jpf
    ```
  The output should display the outcome of the analysis run on the WordCountExample.java program.

The created container could serve as a template for any future projects that aim to execute analyses on Spark programs.

Manual approach
---------------

This section lists all the dependencies and configuration requirements that are needed to execute JPF and JPF-SymSpark correctly. By no means it should be considered as an extensive guide that works under all platforms; the described approach will only be focused on explaining the steps used in the environment where to project was developed. The following steps should be executed in an [Ubuntu 16.04 Desktop OS](https://www.ubuntu.com/desktop) with at least 2 gigabytes of memory.

### Prerequisites

The following dependencies need to be installed first:

- **Java** Installed normally using apt-get having added the repo that gets the binaries from oracle. I am using java 1.8.0_111
- **[Mercurial](https://www.mercurial-scm.org/)** Installed normally using apt-get. I am using version 3.7.3
- **[Ant](http://ant.apache.org/)** Installed normally using apt-get. I am using version 1.9.6
- **[JUnit](http://junit.org/)** Download the latest jars from the official resources. In this case junit.jar hamcrest-core.jar and place them in a known directory.
 
### jpf-core

This is the main module of **[Java Pathfinder](http://babelfish.arc.nasa.gov/trac/jpf/wiki/intro/start) (jpf)**. In addition to the following installation steps, always check the official installation instructions because the repositories could have been moved.

1. Clone the project **jpf-core** from the official repository using: 
    ```sh
       hg clone http://babelfish.arc.nasa.gov/hg/jpf/jpf-core
    ```
    
2. Create the **site.properties** file as suggested in the official jpf site. Make sure to be pointing the jpf-core property to the directory where you cloned the project. Also be sure to remove or comment the other references to modules in the example file provided.
3. In order to be able to build the project, JUnit libraries need to be in the classpath. The ant script requires the JUNIT_HOME directory to be specified. This could be done by creating the JUNIT_HOME environment variable and placing it in the path. However, the **build.xml** file of the jpf-core project could be modified by replacing the value property **junit.home** with the value of the directory where the JUnit library was placed. This option is more convenient because it avoids polluting the classpath with variables that might potentially generate conflicts with other programs.
4. Finally, build the jpf-core project. Go to the directory where it is located and execute  
   ```sh 
      ant test 
   ```
   
   With this, JPF should be available.
5. In order to test if the build was successful, go to the jpf-core directory and execute
   ```sh 
      java -jar build/RunJPF.jar src/examples/Racer.jpf
   ```
   
   JPF should run a basic model checking analysis on the Racer example.

### jpf-symbc

This is the symbolic execution module built on top of JPF known as SPF or Symbolic Pathfinder. In addition to the following installation steps, always check the official installation instructions. The version used in this project is a modified version of the module. At the moment of this publication, the repository of this customized version might not be public yet.

1. Clone the project **jpf-symbc** from the official repository on the same root directory where **jpf-core** was cloned 
    ```sh
       git clone https://github.com/omrsin/jpf-symbc.git
    ```
    
2. Update the **site.properties** file as suggested in the official jpf site. Every time a new module is downloaded, this file must be updated. Make sure to be pointing the jpf-symbc property to the directory where the project was cloned.
3. Set the JUNIT_HOME environment variable or update the ***build.xml** file of the project in a similar fashion as explained for jpf-core.
4. Finally, go to the jpf-symbc directory and execute: 
   ```sh 
      ant test 
   ```
   If the test target generates errors then executing **ant build** would be sufficient (considering no build errors were found). With this, SPF should be available.
   
### jpf-symspark

This is the module that we developed based on SPF. It provides the mechanisms to carry out symbolic
executions of Spark programs. The installation steps follow the same pattern as in the case of jpf-symbc.

1. Clone the **jpf-symspark** project from our hosted repository on the same root directory where **jpf-core** and **jpf-symbc** were cloned
	```sh 
      https://github.com/omrsin/jpf-symspark.git
   	```	
2. Update the **site.properties** file as suggested in the official JPF site. Every time a new module is downloaded this file must be updated. Make sure to be pointing the jpf-symspark property to the directory where the project was cloned.
3. Build the jpf-symspark project. Go to the directory where it is located and execute
	```sh 
      ant test
   	```	
4. In order to test if the build was successful, go to the jpf-symspark directory and execute
	```sh 
      java -jar build/RunJPF.jar |
      	src/examples/de/tudarmstadt/thesis/symspark/examples/java/applied/WordCountExample.jpf
   	```		
	This will execute an analysis on an example program and produce a reduced input dataset that explores all possible paths.

### eclipse-jpf (optional)

Be sure to install jpf-core before installing the plugin.

This is very simple and it is the most convenient way to run the analyses. In Eclipse, go to **Help -> Install New Software** and add the following update site http://babelfish.arc.nasa.gov/trac/jpf/raw-attachment/wiki/projects/eclipse-jpf/update. Finally install the corresponding plugin.

The analyses can be run by right-clicking a jpf file and choosing the option **Verify**.

Usage
=====

The JPF-SymSpark module is used in a similar way as SPF. However, some additional **.properties** were added to the \textit{properties} file of the module or must be included in the **.jpf** file used for a particular analysis in order execute correctly.

The properties used are:

- **spark.methods**: Used to indicate which spark operations are to be analyzed by the module. Every time a Spark operation with this name is found in the program it will be executed symbolically. This option replaces the use of the **symbolic.method** property used by SPF given that the target methods to be analyzed will be set dynamically during the analysis based on the spark operations defined. The values supported for this property are: *filter*, *map*, *reduce* and *flatMap*; multiple values must be separated with a semicolon.
- **spark.reduce.iterations**: Used to indicate how many iterations of a reduce action will be analyzed. The value must be greater than 0. This option is only relevant if the reduce action was included among the methods to be analyzed in the *spark.methods* property.
- **listener**: Defines the listener to be used during the analysis. By default, the module uses the *SparkMethodListener* as defined in its *.properties* file.
- **jvm.insn_factory.class**: This property is used to specify the instruction factory used by the module. By default, the module uses the *SparkSymbolicInstructionFactory* as defined in the *.properties* file.

Other SPF specific properties are still supported.

The analysis can be run by means of the previously mentioned Eclipse plug-in or by execution through the command line as shown in the installation steps. Additional examples can be found in the **examples** directory of the JPF-SymSpark} module.