package org.gridkit.lab.examples.nanocloud;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Callable;

import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViManager;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViProps;
import org.gridkit.vicluster.telecontrol.jvm.JvmProps;
import org.junit.Test;

public class StartingWithLocalCloud extends BaseCloudTest {

	public ViManager createLocalCloud() {
		ViManager cloud = CloudFactory.createCloud();
		// this will configure "local" vi-node type by default
		ViProps.at(cloud.node("**")).setLocalType();
		return cloud;
	}

	
	@Test
	public void test_hello_world__version1() {
		// Let's create simple cloud where slaves will run on same box with master
		cloud = createLocalCloud();
		
		// This line says that 'node1' should exists
		// all initialization are lazy and asynchronous
		// so this line will not trigger any process creation
		cloud.node("node1");
		
		// two starts will match any node name
		ViNode allNodes = cloud.node("**");
		
		// let our node to say hello
		allNodes.exec(new Callable<Void>() {
		
			@Override
			public Void call() throws Exception {
				String jvmName = ManagementFactory.getRuntimeMXBean().getName();
				System.out.println("My name is '" + jvmName + "'. Hello!");
				return null;
			}
		});		
	}

	@Test
	public void test_hello_world__version2() {
		cloud = createLocalCloud();

		// let's create a few more nodes this time
		cloud.nodes("node1", "node2", "node3", "node4");
		
		// say hello
		cloud.node("**").exec(new Callable<Void>() {
		
			@Override
			public Void call() throws Exception {
				String jvmName = ManagementFactory.getRuntimeMXBean().getName();
				System.out.println("My name is '" + jvmName + "'. Hello!");
				return null;
			}
		});		
	}

	@Test
	public void test_hello_world__version3() throws InterruptedException {
		// Let's create simple local cloud first
		cloud = createLocalCloud();
		
		cloud.nodes("node1", "node2", "node3", "node4");
		
		// let's make sure that all nodes are initialized
		// before saying 'hello' this time.
		// touch() will force nodes to be initialized.
		cloud.node("**").touch();
		
		// Console output is pulled asynchronously so we have to give it
		// few milliseconds to catch up.
		Thread.sleep(300);
		
		// Now we should see quite good chorus
		cloud.node("**").exec(new Callable<Void>() {
		
			@Override
			public Void call() throws Exception {
				String jvmName = ManagementFactory.getRuntimeMXBean().getName();
				System.out.println("My name is '" + jvmName + "'. Hello!");
				return null;
			}
		});		
	}

	@Test
	public void test_jvm_args__version1() throws InterruptedException {
		cloud = createLocalCloud();
		
		// let's create a couple of node1
		cloud.node("node1");
		cloud.node("node2");
		
		// now let's adjust JVM command line options used to start slave process
		JvmProps.at(cloud.node("node1")).addJvmArg("-Xms256m").addJvmArg("-Xmx256m");
		JvmProps.at(cloud.node("node2")).addJvmArg("-Xms512m").addJvmArg("-Xmx512m");
		
		cloud.node("**").touch();
		
		// Let's see how much memory is available to our slaves
		reportMemory(cloud);			
	}		
}
