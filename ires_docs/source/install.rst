#########################
Installation & Deployment
#########################

========================
Installing IReS-Platform
========================

--------
Overview
--------
Installation of IRes-Platform requires 3 steps

----------------------------------
Clone IReS-Platform to the server 
----------------------------------

For a quick reference of how to use git, click `here <https://rogerdudler.github.io/git-guide/>`_.
Open a terminal (Linux) and navigate to a desired directory where IReS-Platform files will be cloned e.g. asap. Then, clone the project by entering the following command

.. code:: bash
	
	git clone git@github.com:project-asap/IReS-Platform.git

---------------
Run install.sh
---------------

After successful cloning of IReS-Platform inside the $IRES_HOME various folders and files can be found. Among them there exists install.sh.

You can run install.sh from any directory you would like. Here for demnostration reasons is assumed that the current working directory is $IRES_HOME. Executing,

.. code:: bash

	./install.sh

will start building IReS-Platform. Upon successful building you will be prompted to provide the path where Hadoop YARN is located in your computer. By doing this, IReS gets connected with Hadoop YARN. You can skip this step and the installation will be finished. 

NOTE: if you do not provide an existing YARN installation, then IReS will not be able to execute any workflow. Also, resources and cluster services monitoring will not be functioning.
you can provide YARN installation path afterwards as it will be shown straight ahead.
Connecting IReS to Hadoop YARN

Executing,

.. code:: bash
	
	./install.sh -c $YARN_HOME,$IRES_HOME

will make the connection of IReS and YARN, where $YARN_HOME and $IRES_HOME correspond to the absolute paths of YARN's and IReS's home folder.
Cluster Monitoring

This step requires the connection of IReS with YARN. Assuming that this connections has been established, then the user should update the file

$YARN_HOME/etc/hadoop/yarn-site.xml

and more specifically the values of the following properties,

.. code:: bash

	yarn.nodemanager.services-running.per-node
	yarn.nodemanager.services-running.check-availability
	yarn.nodemanager.services-running.check-status

These properties and some others have been added during the connection of IReS and YARN to enable IReS run workflows over YARN and cluster resources and services monitoring. Although details about filling these values are provided into $YARN_HOME/etc/hadoop/yarn-site.xml, roughly speaking, yarn.nodemanager.services-running.per-node property describes the cluster services running per node. The property, yarn.nodemanager.services-running.check-availability provides the commands per service that "tell" if the relative service runs or not. Finally, the property yarn.nodemanager.services-running.check-status has the statuses per service that the corresponding service has when it runs.

-----------------------
Validate installation
-----------------------

Here are some tips to confirm IReS installation.

If anything goes wrong during the build process of IReS, error messages will be print out and a log file will be provided.

----------------------
Start the IReS server
----------------------

Run IReS server by running the command

.. code:: bash

	./install.sh -r start

No exception should be raised. Also, the jps command should print a "Main" process running that corresponds to ASAP server.
Run ASAP server web user interface at http://your_hostname:1323/web/main. IReS home page should be displayed.
Run a workflow, for example run "hello_world" from "Abstrack Workflows" tab and see what happens not only in IReS web interface but also in YARN and HDFS web interfaces. Make sure that YARN has been started before running any workflow.
Click on "Cockpit" tab to verify that the expected services to run are really running.


================================
Running the HelloWorld workflow
================================

The HelloWorld is a simple workflow constists of just a single operator, designed for demonstration purposes. To run the HelloWolrd follow the next steps:

1. Go to IReS UI: http://localhost:1323/web/main

.. figure:: ireshome.png
	
	IReS Home Page

2. Go to the **Abstract Workflows** tab and select the **HelloWorld** workflow

.. figure:: abstractworkflows.png
	
	Abstract Workflows Tab

3. Then click on **Materialize Workflow** button

.. figure:: abstracthello.png
	
	Abstract HelloWorld Workflow

4. Click on the **Execute Workflow** button to start the execution

.. figure:: materializedhello.png
	
	The materialized HelloWorld workflow

In the figures below we can see the execution process

.. figure:: exec1.png
   :width: 150%

   The execution has been started

.. figure:: yarn.png
   :width: 150%

   The submitted YARN application

.. figure:: exec2.png
   :width: 150%

   The execution has been finished



===================================
Create a new workflow from scratch
===================================

In this section we describe the process of design a new workflow from scratch.

--------------------------------
1. Creating Abstract Operators
--------------------------------

In order to create a new workflow the definition of the abstract operators is needed. To define the **HelloWorld** abstract operator go to the **Abstract Operators** tab and enter the operator description in the text box. To create and save the new abstract operator click the "Add operator" button.

.. image:: newabstractoperator.png
   :width: 150%

-----------------------------------
2. Creating Materialized Operators
-----------------------------------

Currently, to add a materialized operator a folder with the least required files is needed. 

i. From the bash shell, go to the **asapLibrary/operators** folder in the IReS installation directory.

.. code:: bash

	cd $ASAP_HOME/target/asapLibrary/operators

ii. Then, create a new folder named with the new materialized operator's name. 

.. code:: bash

	mkdir HelloWorld

iii. Create the **description** file and enter the information below

.. code:: bash

	$ nano description

.. code:: javascript

	Constraints.Engine=Spark
	Constraints.Output.number=1
	Constraints.Input.number=1
	Constraints.OpSpecification.Algorithm.name=HelloWorld
	Optimization.model.execTime=gr.ntua.ece.cslab.panic.core.models.UserFunction
	Optimization.model.cost=gr.ntua.ece.cslab.panic.core.models.UserFunction
	Optimization.outputSpace.execTime=Double
	Optimization.outputSpace.cost=Double
	Optimization.cost=1.0
	Optimization.execTime=1.0
	Execution.Arguments.number=1
	Execution.Argument0=testout
	Execution.Output0.name=$HDFS_OP_DIR/testout
	Execution.copyFromLocal=testout

iv. Create the .lua file with the execution instructions

.. code:: bash

	$ nano HelloWorld.lua

.. code:: javascript

	operator = yarn {
	  name = "Execute Hello world",
	  timeout = 10000,
	  memory = 1024,
	  cores = 1,
	  container = {
	    instances = 1,
	    --env = base_env,
	    resources = {
	    ["HelloWorld.sh"] = {
	       file = "asapLibrary/operators/HelloWorld/HelloWorld.sh",
	                type = "file",               -- other value: 'archive'
	                visibility = "application"  -- other values: 'private', 'public'
	        }
	    },
	    command = {
	        base = "./HelloWorld.sh"
	    }
	  }
	}

v. Create the executable

.. code:: bash

	$ nano HelloWorld.sh

.. code:: javascript

	#!/bin/bash
	echo "Hello world" >> $1

vi. Restart the IReS server

.. code:: bash
	
	$ $IRES_HOME/asap-server/src/main/scripts/asap-server restart

-----------------------------------
3. Creating the Abstract Workflow
-----------------------------------

Now we will combine everything we created in the above steps to generate the new workflow. Go to the **Abstract Workflows** tab and click the "New Workflow" button.

.. image:: newworkflow1.png
   :width: 150%

Then we add the workflow parts one-by-one. First we add the **crawlDocuments** dataset from the dataset library. Select the **Materialized Dataset** radio button and enter the dataset name in the **Comma seperated list** text box. Then click the **Add nodes** button to add the dataset node to the workflow graph. Repeat this step to add an output node with name **d1**. Just enter the name **d1** to the text box and click the **Add nodes** button.

.. image:: newworkflow2.png
   :width: 150%


.. image:: newworkflow3.png
   :width: 150%

Add the **HelloWorld** abstract operator to the workflow. Select the **Abstract Operator** radio button, enter the operator's name (HelloWold) in the text box and click again the **Add nodes** button.

.. image:: newworkflow4.png
   :width: 150%

Describe the workflow by connecting the graph nodes defined in the previous steps as shown in the figure bellow and click the **Change graph** button.

.. image:: newworkflow5.png
   :width: 150%

In the figure bellow we can see the generated **Abstract Workflow**. Now click the **Materialize workflow**

.. image:: newworkflow6.png
   :width: 150%

The resulting materialized workflow

.. figure:: materializedhello.png