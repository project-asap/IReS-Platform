# IReS-Platform
Intelligent, Multi-Engine Resource Scheduler for Big
Data Analytics Workflows

<h2>Links</h2>
<ul>
<li><a href="http://www.cslab.ntua.gr/~dtsouma/index_files/modde764.pdf">IReS Paper</a> </li>
<li><a href="http://www.asap-fp7.eu/">ASAP Project Official Page</a></li>
</ul>

<h2>Using IReS-Platform</h2>

Before using IReS it should be configured properly. This setting consists of 3 main steps,
<ol>
<li><b>Cloning</b> IReS-Platform to the server. For a quick reference of how to use git, click here <a href="https://rogerdudler.github.io/git-guide/" target="_blank">Git - the simple guide</a>
<li><b>Running install.sh</b> that 
	<ul>
	<li>builds IReS-Platform using <i>Maven</i>. A tutorial about Maven can be found here <a href="https://maven.apache.org/guides/getting-started/" target="_blank">Maven Getting Started Guide</a>.</li>
	<li>connects IReS to Hadoop YARN.</li>
	<li>updates configuration files and folders appropriately.</li>
	</ul>
<li><b>Setting</b> cluster resources and services monitoring</li>
</ol>

For demonstration reasons a Linux operating system like Ubuntu is assumed throughout this text. In Windows or other Linux distributions the equivalents should be done. The local home directory of the IReS-Platform project is depicted as <p><code>$IRES\_HOME</code></p> following bash script variable notation. Similarly, for Hadoop YARN the local home directory is denoted as <p><code>$YARN\_HOME</code>.</p>

<h3>Hands On</h3>

<ol>
<li>
<h3>Clone</h3>
Open a terminal( Linux) and navigate to a desired directory (create it if does not exist) where IReS-Platform files will be cloned. In <a href="https://github.com/project-asap/IReS-Platform" target="_blank">IReS-Platform</a> github page, under the green drop down list "Clone or download", the clone url can be found. Copy this url and execute in terminal the command, <p><code>git clone clone_url</code></p>
</li>
<li>
<h3>Run install.sh</h3>

After successful cloning of IReS-Platform inside the <code>$IRES_HOME</code> various folders and files can be found. Among them there exists <code>install.sh</code>. <p><code>install.sh</code> <bold>is your friend!</bold></p> You can run install.sh from any directory you would like. Here for demnostration reasons is assumed that the current working directory is $IRES_HOME.

Executing, <p><code>./install.sh</code></p> will start building IReS-Platform. Upon successful building you will be prompted to provide the path where Hadoop YARN is located in your computer. By doing this, IReS gets connected with Hadoop YARN. You can skip this step and the installation will be finished.

<b>NOTE:</b>
<ol>
<li>if you do not provide an existing YARN installation, then IReS will not be able to execute any workflow. Also, resources and cluster services monitoring will not be functioning.</li>
<li>you can provide YARN installation path afterwards as it will be shown straight ahead.</li>
</ol>

<h4>Connecting IReS to Hadoop YARN</h4>

Executing, <p><code>./install.sh -c $YARN\_HOME,$IRES\_HOME</code></p> will make the connection of IReS and YARN, where $YARN\_HOME and $IRES_HOME correspond to the absolute paths of YARN's and IReS's home folder.
</li>
<li>
<h3>Cluster Monitoring</h3>

This step requires the connection of IReS with YARN. Assuming that this connection has been established, then the user should update the file <p><code>$YARN_HOME/etc/hadoop/yarn-site.xml</code></p> and more specifically the values of the following properties,
<ul>
<li>yarn.nodemanager.services-running.per-node</li>
<li>yarn.nodemanager.services-running.check-availability</li>
<li>yarn.nodemanager.services-running.check-status</li>
</ul>

These properties and some others have been added during the connection of IReS and YARN to enable IReS run workflows over YARN and cluster resources and services monitoring. Although details about filling these values are provided into <code>$YARN_HOME/etc/hadoop/yarn-site.xml</code>, roughly speaking, <code>yarn.nodemanager.services-running.per-node</code> property describes the cluster services running per node. The property, <code>yarn.nodemanager.services-running.check-availability</code> provides the commands per service that "tell" if the relative service runs or not. Finally, the property <code>yarn.nodemanager.services-running.check-status</code> has the statuses per service that the corresponding service has when it runs.
</li>
</ol>

<h3>Validate installation</h3>

Here are some tips to confirm IReS installation.
<ol>
<li>If anything goes wrong during the build process of IReS, error messages will be print out and a log file will be provided.</li>
<li>Run ASAP server by running the command <p><code>./install.sh -r start</p></code>No exception should be raised. Also, the <code>jps</code> command should print a "Main" process running that corresponds to ASAP server.</li>
<li>Run ASAP server web user interface at <i>http://your_hostname:1323/web/main</i>. IReS home page should be displayed.</li>
<li>Run a workflow, for example run "hello_world" from <bold>"Abstrack Workflows"</bold> tab and see what happens not only in IReS web interface but also in YARN and HDFS web interfaces. Make sure that YARN has been started before running any workflow.</li>
<li>Click on <bold>"Cockpit"</bold> tab to verify that the expected services to run are really running.</li>
</ol>

<h3>Appendix</h3>
To see all available functionalities of <code>install.sh</code> run <p><code>./install.sh -h</code></p> However, in this section some details about <code>install.sh</code> script are given.

To begin with, install.sh supports can be run from user's working directory, it is not needed navigating to <code>IRES_HOME</code> folder. Moreover, user can access server's logs by running <code>install.sh</code> script using <code>-l</code> flag.

To see all available functionalities of <code>install.sh</code> run <p><code>./install.sh -h</code></p> Now assume that for some reason the IRES\_HOME has been changed. For example you would like to rename IReS home folder or move it to another folder. In this case, all configuration files of IReS should be updated with the new value of $IRES\_HOME. To do so, run the command <p><code>./install.sh -s NEW\_IRES\_HOME</code></p>
