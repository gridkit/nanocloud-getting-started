package org.gridkit.lab.examples.nanocloud;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Callable;

import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViProps;
import org.gridkit.vicluster.telecontrol.ssh.RemoteNodeProps;
import org.junit.Test;

public class StartingWithDistributedCloud extends BaseCloudTest {

	@Test
	public void test_distributed_hello_world__version1() throws InterruptedException {
		
		// Using SSH for remote execution requires some configuration
		// it could be done programmaticaly, but we will use config file in this example
		cloud = CloudFactory.createSshCloud("resource:cbox-cluster.viconf");
		
		// Our cloud config (box-cluster.viconf) is using first segment of node name 
		// for mapping of node node to hostname
		cloud.node("cbox1.node1");
		cloud.node("cbox2.node1");
		
		// Alternatively we could override configuration for particular node
		// host, java command and jar cache path should be configured for node to start
		RemoteNodeProps.at(cloud.node("extranode"))
			.setRemoteHost("cbox1")
			.setRemoteJavaExec("java")
			.setRemoteJarCachePath("/tmp/extra");
				
		
		// now we have 3 nodes configured to run across two servers
		// let say them hello
		
		// warm up is optional, but it makes console out put less messy
		warmUp(cloud);
		sayHelloWorld(cloud);

		// give console output a chance to reach us from remote node
		Thread.sleep(300);
	}
	
	@Test
	public void test_distributed_hello_world__with_debug() throws InterruptedException {
		
		// Using SSH for remote execution requires some configuration
		// it could be done programmaticaly, but we will use config file in this example
		cloud = CloudFactory.createSshCloud("resource:cbox-cluster.viconf");
		
		// Our cloud config (box-cluster.viconf) is using first segment of node name 
		// for mapping of node node to hostname
		cloud.node("cbox1.node1");
		cloud.node("cbox2.node1");
		
		// Alternatively we could override configuration for particular node
		// host, java command and jar cache path should be configured for node to start
		RemoteNodeProps.at(cloud.node("extranode"))
			.setRemoteHost("cbox1")
			.setRemoteJavaExec("java")
			.setRemoteJarCachePath("/tmp/extra");
				
		// Now imagine that you want to debug one of slave processes
		// if it could be run on your dev. box (no OS dependencies, etc)
		// you could easy redirect on of slave node to run inside of master JVM.
		// You can achieve this but using either in-process or isolate node type
		ViProps.at(cloud.node("cbox1.node1")).setInProcessType();		
		
		ViNode allNodes = cloud.node("**");

		allNodes.touch(); // warm up, equivalent to sending empty runnable
		
		// you can set break point in runnable and catch cbox1.node1 executing it
		// other vinodes are running as separate processes, so they out of reach
		allNodes.exec(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				String jvmName = ManagementFactory.getRuntimeMXBean().getName();
				System.out.println("My name is '" + jvmName + "'. Hello!");
				return null;
			}
		});
		
		// Notice that output of "in-process" vinode are still prefixed for your convenience.
		// Same isolation applies to system properties too. 
		
		// give console output a chance to reach us from remote node
		Thread.sleep(300);
	}	
}
