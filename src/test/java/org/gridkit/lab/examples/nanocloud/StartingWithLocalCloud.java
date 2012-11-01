package org.gridkit.lab.examples.nanocloud;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Callable;

import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViNodeSet;
import org.gridkit.vicluster.telecontrol.jvm.JvmProps;
import org.junit.Test;

public class StartingWithLocalCloud extends BaseCloudTest {

	@Test
	public void test_hello_world__version1() {
		// Let's create simple local cloud first
		cloud = CloudFactory.createLocalCloud();
		
		// This line says that 'node1' should exists
		// all initialization are lazy and asynchronous
		// so this line will not trigger any process creation
		cloud.node("node1");
		
		sayHelloWorld(cloud);		
	}

	@Test
	public void test_hello_world__version2() {
		cloud = CloudFactory.createLocalCloud();

		// let's create a few more nodes this time
		cloud.nodes("node1", "node2", "node3", "node4");
		
		sayHelloWorld(cloud);		
	}

	@Test
	public void test_hello_world__version3() throws InterruptedException {
		// Let's create simple local cloud first
		cloud = CloudFactory.createLocalCloud();
		
		cloud.nodes("node1", "node2", "node3", "node4");
		
		// let's make sure that all nodes are initialized
		// before saying 'hello' this time
		warmUp(cloud);
		
		// Console output is pulled asynchronously so we have to give it
		// few milliseconds to catch up.
		Thread.sleep(300);
		
		// Now we should see quite good chorus
		sayHelloWorld(cloud);			
	}

	@Test
	public void test_jvm_args__version1() throws InterruptedException {
		cloud = CloudFactory.createLocalCloud();
		
		// let's create a couple of node1
		cloud.node("node1");
		cloud.node("node2");
		
		// now let's adjust JVM command line options used to start slave process
		JvmProps.at(cloud.node("node1")).addJvmArg("-Xms256m").addJvmArg("-Xmx256m");
		JvmProps.at(cloud.node("node2")).addJvmArg("-Xms512m").addJvmArg("-Xmx512m");
		
		warmUp(cloud);
		Thread.sleep(300);
		
		// Let's see how much memory is available to our childs
		reportMemory(cloud);			
	}	
	
	protected void sayHelloWorld(ViNodeSet cloud) {

		// two starts will match any node name
		ViNode allNodes = cloud.node("**");
		
		allNodes.exec(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				String jvmName = ManagementFactory.getRuntimeMXBean().getName();
				System.out.println("My name is '" + jvmName + "'. Hello!");
				return null;
			}
		});
	}

	protected void reportMemory(ViNodeSet cloud) {
		
		// two starts will match any node name
		ViNode allNodes = cloud.node("**");
		
		allNodes.exec(new Callable<Void>() {
			
			@Override
			public Void call() throws Exception {
				String jvmName = ManagementFactory.getRuntimeMXBean().getName();
				long totalMemory = Runtime.getRuntime().maxMemory();
				System.out.println("My name is '" + jvmName + "'. Memory limit is " + (totalMemory >> 20) + "MiB");
				return null;
			}
		});
	}

	/**
	 * This method will force initialization of all declared nodes.
	 */
	protected void warmUp(ViNodeSet cloud) {
		// two starts will match any node name
		ViNode allNodes = cloud.node("**");
		
		// ViNode object may represent a single node or a group
		
		// ViNode.exec(...) call has blocking semantic so it will force all 
		// lazy initialization to finish and wait until runnable is executed 
		// on every node in group 
		allNodes.exec(new Runnable() {
			@Override
			public void run() {
				// do nothing
			}
		});
	}	
}
