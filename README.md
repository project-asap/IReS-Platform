# IReS-Platform
Intelligent, Multi-Engine Resource Scheduler for Big
Data Analytics Workflows

<h4>Links</h4>
<ul>
<li>IReS Paper: http://www.cslab.ntua.gr/~dtsouma/index_files/modde764.pdf </li>
<li>ASAP Project Official Page: http://www.asap-fp7.eu/</li>
</ul>
<h4>Using IReS-Platform</h4>
Usage of IRes-Platform requires 3 steps

<ol>
<li><bold>Clone</bold> IReS-Platform to the server. For a quick reference of how to use git, click here <a href="https://rogerdudler.github.io/git-guide/" target="_blank">Git - the simple guide</a>
<li><bold>Build</bold> IReS-Platform project using maven. A tutorial about maven can be found here <a href="https://maven.apache.org/guides/getting-started/" target="_blank">Maven Getting Started Guide</a>.</li>
<li><bold>Update</bold> configuration files and folders appropriately.</li>
</ol>

<h5>Clone</h5>
Open a terminal (Linux) or a cmd (Windows) and navigate to a desired directory (create it if does not exist) where IReS-Platform files will be cloned e.g. asap.

In the github page of the IReS-Platform, https://github.com/project-asap/IReS-Platform, at the right sidebar, under the label "HTTPS clone URL" the clone url can be found. Copy this url and from inside the terminal execute the command

<code>git clone clone_url</code>

<h5>Build</h5>
For demostration reasons a Linux operating system like Ubuntu it is assumed in this step. In Windows or other Linux distributions the equivalents should be done.

The local home directory of the IReS-Platform project is

<code>IRES_HOME=/home/$USER/asap/IReS-Platform</code>

NOTE: 

<ul>
<li>the "$USER" part of the IRES_HOME corresponds to the currently logged in user</li>
<li>it is assumed that the project has been cloned into the directory /home/$USER/asap</li>
<li>to refer to the value of IRES_HOME the <a href="http://tldp.org/HOWTO/Bash-Prog-Intro-HOWTO-5.html" target="_blank">bash script variable</a> notation is used i.e. $IRES_HOME
</ul>

To build the project use version 3 of maven because the previous version has bugs with jetty and jersey packages. In Ubuntu just execute

<code>sudo apt-get install maven</code>

and confirm the maven version by running

<code>mvn -v</code>.

Then update IReS-Platform pom.xml files to point to the currently installed YARN version.

To do so, the pom.xml file of the <code>asap-platform</code> and <code>cloudera-kitten</code> folders should be updated in order for the Maven to take in account the currently YARN version installed during the building phase. To do so, navigate to <code>$IRES_HOME</code> folder and then

<ul>
  <li>for the <code>asap-platform</code> edit the file <code>$IRES_HOME/asap-platform/pom.xml</code>. In particular, find the line <code>`<hadoop.version></hadoop.version>`</code> and between the tags <code><hadoop.version>, </hadoop.version></code> write the version number of the currently installed YARN version e.g. <code><hadoop.version>2.7.1</hadoop.version></code> if the currently installed YARN is hadoop-2.7.1
  </li>
  <li>for the <code>cloudera-kitten</code> edit the file <code>$IRES_HOME/cloudera-kitten/pom.xml</code>. Again, find the line <code><hadoop.version></hadoop.version></code> and write the version number of the currently installed YARN version like before.
  </li>
</ul>

Then, in each of the directories below

<ol>
<li>$IRES_HOME/cloudera-kitten</li>
<li>$IRES_HOME/panic</li>
<li>$IRES_HOME/asap-platform</li>
</ol>

run the command

<code>sudo mvn clean install -DskipTests</code>

to build IReS-Platform project. In the end of each build you should see a "BUILD SUCCESS" message. The order of the directories above should be followed.

Apart from the "BUILD SUCCESS" message, you should also see a newly created folder by the name "target" for each of the directories above i.e. cloudera-kitten, panic and asap-platform if it did not already exist.

<h5>Update</h5>
To run asap-server successfully and correctly, two things must be done. The first thing is to define the home folder of the ASAP server. The second one is to copy the cluster configuration files to the corresponding folder of the ASAP server.

--> As for the ASAP server's home folder, the corresponding file

<code>$IRES_HOME/asap-platform/asap-server/src/main/scripts/asap-server</code>

should be updated. In this file the variable "ASAP_SERVER_HOME" should be assigned the path of the folder "asap-platform/asap-server/target" and thus under the line

<code>#IRES_HOME=/path/to/IReS-Platform_project</code>

set the
<code>IRES_HOME=</code>

to your custom IRES_HOME e.g.

<code>IRES_HOME=/home/$USER/asap/IReS-Platform</code>

Notice that the folder "$IRES_HOME/asap-platform/asap-server/target" has been created during the building phase.

--> As for the cluster configuration files, it is assumed that a YARN( or Hadoop 2.0) cluster is already set up and that YARN can be found for example in

<code>YARN=/home/$USER/yarn</code>

Copy the <code>$YARN/etc/hadoop/core-site.xml</code> and <code>$YARN/etc/hadoop/yarn-site.xml</code> files into the <code>$IRES_HOME/asap-platform/asap-server/target/conf</code> directory. Finally, the yarn-site.xml must have a minimum set of properties in order for the IReS-Platform to work correctly. This minimum set of properties can be found in yarn-site-min.xml file of this repository. Similarly for the core-site.xml file there is a core-site-min.xml into this repository.
