FROM ubuntu:16.04
MAINTAINER Omar Erminy (omar.erminy.ugueto@gmail.com)

# Basic configuration
RUN apt-get update -y \
 && apt-get install software-properties-common -y \
 && echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections \
 && add-apt-repository ppa:webupd8team/java -y \
# && add-apt-repository  -y "deb http://archive.ubuntu.com/ubuntu $(lsb_release -sc) main universe restricted multiverse" \
 && apt-get update -y \ 
 && apt-get install -y oracle-java8-installer \
 					   ant \
 					   maven \
 					   git \
 					   mercurial \
 					   junit \
 					   build-essential \
 					   python
 					   
# Environment

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle
ENV JUNIT_HOME /usr/share/java

RUN mkdir /jpf-symspark-project
ENV JPF_HOME /jpf-symspark-project

# SSH keys and permissions

RUN mkdir /root/.ssh
COPY docker/ssh/ /root/.ssh/
RUN chmod 600 /root/.ssh/* \
 && ssh-keyscan -H github.com > /root/.ssh/known_hosts

# JPF dependencies

WORKDIR ${JPF_HOME}

# jpf-core
RUN hg clone http://babelfish.arc.nasa.gov/hg/jpf/jpf-core \ 
 && cd jpf-core \
 && hg update -r 31 \
 && ant

# create site.properties 
RUN mkdir /root/.jpf \
 && echo "alias jpf='java -jar /jpf-symspark-project/jpf-core/build/RunJPF.jar'"  >> /root/.bashrc \
 && echo "jpf-core = ${JPF_HOME}/jpf-core" >> /root/.jpf/site.properties \
 && echo "jpf-symbc = ${JPF_HOME}/jpf-symbc" >> /root/.jpf/site.properties \
 && echo "jpf-symspark = ${JPF_HOME}/jpf-symspark" >> /root/.jpf/site.properties \
 && echo "extensions=\${jpf-core}" >> /root/.jpf/site.properties

# jpf-symbc 
RUN git clone git@jpf-symbc.github.com:omrsin/jpf-symbc.git \
 && cd jpf-symbc \
 && ant
 
# jpf-symspark
RUN git clone git@jpf-symspark.github.com:omrsin/jpf-symspark.git \
 && cd jpf-symspark \
 && ant