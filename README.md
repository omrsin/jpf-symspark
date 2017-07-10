JPF-SymSpark
===============================================

JPF-SymSpark is a **[Java PathFinder (JPF)](http://babelfish.arc.nasa.gov/trac/jpf/wiki/intro/start)** module whose goal is to coordinate the symbolic execution of **[Apache Spark](http://spark.apache.org/)** programs to produce a reduced input dataset that ensures full path coverage on a regular execution. It builds on top of **[Symbolic PathFinder (SPF)](http://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-symbc)** to delegate the handling of symbolic expressions while it focus on how to interconnect Spark's transformations and actions in order to reason coherently over the execution flow of the program.

Table of contents
=================
 
  * [Intallation](#installation)
    * [Docker approach](#docker-approach)
    * [Manual approach](#manual-approach)    
  * [Usage](#usage)
  
Installation
============

Docker approach
---------------
**[Docker](https://www.docker.com/)** is a widely supported platform for the creation and maintenance of virtual containers. It is designed to provide self-contained, portable environments that are ideal for distributing software with a fixed set of dependencies.

For this reason, we provide the description of a Docker container that prepares an environment with all the dependencies and configurations required by JPF-SymSpark. This installation method is the preferred approach given its simplicity and tested behavior. The following instructions guide the process for creating the Docker container; Docker is assumed to be installed  already.

- Clone the \textit{JPF-SymSpark} repository
	```sh
       git clone https://github.com/omrsin/jpf-symbc.git
    ```	
 - Go to the root directory of the project and build the container
 	```sh
       docker build -t jpf-symspark .
    ```	
- Once the container has been successfully built, run a container shell
	```sh
       docker run -it jpf-symspark
    ```
- Inside the container, the installation of the module can be validated by running
	```sh
       cd jpf-symspark/src/examples/de/tudarmstadt/thesis/symspark/examples/java/applied
       jpf WordCountExample.jpf
    ```
  The output should display the outcome of the analysis run on the WordCountExample.java program.

The created container could serve as a template for any future projects that aim to execute analyses on Spark programs.

Manual approach
---------------

This section lists all the dependencies and configuration requirements that are needed to execute JPF and JPF-SymSpark correctly. By no means it should be considered as an extensive guide that works under all platforms; the described approach will only be focused on explaining the steps used in the environment where to project was developed. The following steps should be executed in an [Ubuntu 16.04 Desktop OS](https://www.ubuntu.com/desktop) with at least 2 gigabytes of memory.

Prerequisites
=============
In order to make this project work in your computer, there are quite a few configurations that have to be done first. The following dependencies need to be installed first:

- **Java** Installed normally using apt-get having added the repo that gets the binaries from oracle. I am using java 1.8.0_111
- **[Mercurial](https://www.mercurial-scm.org/)** Installed normally using apt-get. I am using version 3.7.3
- **[Ant](http://ant.apache.org/)** Installed normally using apt-get. I am using version 1.9.6
- **[JUnit](http://junit.org/)** Download the latest jars from the official resources. In this case junit.jar hamcrest-core.jar and place them in a known directory.
 
jpf-core
--------
This is the main module of **[Java Pathfinder](http://babelfish.arc.nasa.gov/trac/jpf/wiki/intro/start) (jpf)**. Here are the installation steps on Ubuntu 16.04. Always check the official installation instructions because the repositories could have been moved.

1. Clone the project **jpf-core** from the official repository using: 
    ```sh
       hg clone http://babelfish.arc.nasa.gov/hg/jpf/jpf-core
    ```
    
2. Create the **site.properties** file as suggested in the official jpf site. Make sure to be pointing the jpf-core property to the directory where you cloned the project. Also be sure to remove or comment the other references to modules in the example file provided.
3. In order to be able to build the project, JUnit libraries need to be in the classpath. The ant script requires the JUNIT_HOME directory to be specified. This could be done by creating the JUNIT_HOME environment variable and placing it in the path or what I prefer, is changing the **build.xml** file of the jpf-core project and replacing the value property **junit.home** with the value of the directory where you placed the jars. For me this is better because you avoid polluting your classpath with variables that might potentially generate conflicts with other programs.
4. Finally, go to the jpf-core directory and execute: 
   ```sh 
      ant test 
   ```
   
   With this, JPF should be available.
5. In order to test if the build was actually successful is to go to the jpf-core directory and execute:
   ```sh 
      java -jar build/RunJPF.jar src/examples/Racer.jpf
   ```
   
   JPF should run a basic model checking analysis on the Racer example.

jpf-symbc
---------
**WARNING: I have modified this module in order to avoid problems with some java constructs. The source of this customized version will be provided eventually, as soon as a good distribution mean becomes available.**

This is a symbolic execution module built on top of JPF knows as **[SPF or Symbolic Pathfinder](http://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-symbc)**. Here are the installation steps on Ubuntu 16.04. Always check the official installation instructions because the repositories could have been moved.

1. Clone the project **jpf-symbc** from the official repository on the same root directory where **jpf-core** was cloned using: 
    ```sh
       hg clone http://babelfish.arc.nasa.gov/hg/jpf/jpf-symbc
    ```
    
2. Update the **site.properties** file as suggested in the official jpf site. Every time a new module is downloaded, this file must be updated. Make sure to be pointing the jpf-symbc property to the directory where you cloned the project
3. In order to be able to build the project, JUnit libraries need to be in the classpath. The ant script requires the JUNIT_HOME directory to be specified. This could be done by creating the JUNIT_HOME environment variable and placing it in the path or what I prefer, is changing the **build.xml** file of the jpf-symbc project and replacing the value property **junit.home** with the value of the directory where you placed the jars. For me this is better because you avoid polluting your classpath with variables that might potentially generate conflicts with other programs.
4. Finally, go to the jpf-core directory and execute: 
   ```sh 
      ant test 
   ```
   If the test target generates errors then ant build would be sufficient (considering no build errores were found). With this, SPF should be available.

eclipse-jpf
-----------
Be sure to install jpf-core before installing the plugin.

This is very simple and it is the most convenient way to run the analyses. In Eclipse, go to **Help -> Install New Software** and add the following update site http://babelfish.arc.nasa.gov/trac/jpf/raw-attachment/wiki/projects/eclipse-jpf/update. Finally install the corresponding plugin.

The analyses can be run by right-clicking a jpf file and choosing the option **Verify**.

Content
=======
Next, the contents of the repository are briefly explained.

jpf-test
--------
Simple java maven project used as a sandbox to test and validate different properties regarding jpf-core and jpf-symbc.

spark-java
----------
Java maven project consisting of different Spark applications used as subject under test. These examples are executed symbolically using jpf-symbc.
