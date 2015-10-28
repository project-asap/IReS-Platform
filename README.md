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
<li><bold>Update</bold> configuration files appropriately.</li>
<li><bold>Build</bold> IReS-Platform project using maven. A tutorial about maven can be found here <a href="https://maven.apache.org/guides/getting-started/" target="_blank">Maven Getting Started Guide</a>.</li>
</ol>

<h5>Clone</h5>
Open a terminal (Linux) or a cmd (Windows) and navigate to a desired directory (create it if does not exist) where IReS-Platform files will be cloned e.g. asap. If asap directory has not have any git repository, create an empty one by executing

<code>git init</code>

In the github page of the IReS-Platform, https://github.com/project-asap/IReS-Platform, at the right sidebar, under the label "HTTPS clone URL" the clone url can be found. Copy this url and from inside the terminal execute the command

<code>git clone clone_url</code>
<h5>Update</h5>

For demostration reasons a Linux operating system like Ubuntu it is assumed in this step. In Windows or other Linux distributions the equivalents should be done.


The local home directory of the IReS-Platform project is

<code>IRES_HOME=/home/$USER/asap/IReS-Platform</code>

where 

<ul>
<li>the "$USER" part of the IRES_HOME corresponds to the currently logged in user</li>
<li>it is assumed that the project has been cloned into directory /home/$USER/asap</li>
</ul>

<h5>Build</h5>
