package org.gridkit.lab.examples.nanocloud;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Callable;

import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.nanocloud.RemoteNode;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViProps;
import org.gridkit.vicluster.telecontrol.ssh.RemoteNodeProps;
import org.junit.Test;

public class StartingWithDistributedCloud extends BaseCloudTest {

	@Test
	public void test_distributed_hello_world__basic_example() throws InterruptedException {
		
		// How cloud will create its nodes is defined by configuration.
		// Helper method below will create a preconfigured cloud,
		// where node name is interpreted by hostname.
		// If you do not have paswordless SSH to that node, 
		// additional credentials configuration may be required.
		cloud = CloudFactory.createCloud();
		RemoteNode.at(cloud.node("**")).useSimpleRemoting();
		
		// "cbox1" - "cbox3" are hostnames of VMs I'm using for testing.
		// You can either put FDQN names of your servers below
		// or use /etc/hosts to map these short names (as I do).
		cloud.node("cbox1");
		cloud.node("cbox2");
		cloud.node("cbox3");
		
		// Optionally you may want to specify java executable.
		// Default value is "java", so if java is on your PATH you do not need to do it.
		RemoteNodeProps.at(cloud.node("**")).setRemoteJavaExec("java");
		
		// now we have 3 nodes configured to run across two servers
		// let them say hello
		
		// cloud.node("**").touch() will force immediate intialization
		// of all nodes currently declared in cloud.
		// It is optional, but it makes console out put less messy
		cloud.node("**").touch();
		
		// Say hello world
		cloud.node("**").exec(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				String jvmName = ManagementFactory.getRuntimeMXBean().getName();
				System.out.println("My name is '" + jvmName + "'. Hello!");
				Thread.sleep(10000);
				return null;
			}
		});
		
		// Finally, we will give console output a chance to reach us from remote node.
		// Console communications are asynchronous, so them may be delayed by few miliseconds. 
		Thread.sleep(300);
	}

	@Test
	public void test_distributed_hello_world__with_node_configuration_tweaking() throws InterruptedException {

		// Continue to use simple SSH cloud
        cloud = CloudFactory.createCloud();
        RemoteNode.at(cloud.node("**")).useSimpleRemoting();
		
		// "cbox1" - "cbox3" are hostnames of VMs I'm using for testing.
		// You can either put FDQN names of your servers below
		// or use /etc/hosts to map these short names (as I do).
		cloud.node("cbox1");
		cloud.node("cbox2");
		cloud.node("cbox3");
		
		// By default for simple SSH cloud, node name is used as host name.
		// But imagine that "cbox3" is not available and you want to
		// host that vi-node on other server.
		// We can override this by configuring specific vi-node.

		// RemoteNodeProps is a helper use for convenience.
		// Same configuration tweaks could be made via .viconf file or setProp(...) method.
		RemoteNodeProps.at(cloud.node("cbox3"))
			.setRemoteHost("cbox1")
			.setRemoteJavaExec("java")
			.setRemoteJarCachePath("/tmp/extra");
		
		// now we have 3 nodes configured to run across two servers
		// let's verify this be saying hello
		
		// Warm up is optional
		cloud.node("**").touch();
		
		// Say hello world
		cloud.node("**").exec(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				String jvmName = ManagementFactory.getRuntimeMXBean().getName();
				System.out.println("My name is '" + jvmName + "'. Hello!");
				return null;
			}
		});
		
		// You could see that "cbox3" node is actually running on "cbox1" server

		// Quick wait to catch up with console
		Thread.sleep(300);
	}
	
	@Test
	public void test_distributed_hello_world__with_debug() throws InterruptedException {
		
		// Let's create a SSH cloud as in our first example
        cloud = CloudFactory.createCloud();
        RemoteNode.at(cloud.node("**")).useSimpleRemoting();
		
		cloud.node("cbox1");
		cloud.node("cbox2");
						
		// But now, we would like to debug one of slave processes.
		// If it could be run on your desktop (no OS dependencies, etc),
		// you could easy redirect one of slaves to run inside of master JVM.
		// You can achieve this but using either in-process or isolate node type
		ViProps.at(cloud.node("cbox1")).setIsolateType();		
		
		ViNode allNodes = cloud.node("**");

		// warming up cluster as usual
		allNodes.touch();
		
		System.out.println("Master JVM name is '" + ManagementFactory.getRuntimeMXBean().getName() + "'");
		// you can set break point in runnable and catch cbox1.node1 executing it
		// other vi-nodes are running as separate processes, so they out of reach
		allNodes.exec(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				String jvmName = ManagementFactory.getRuntimeMXBean().getName();
				System.out.println("My name is '" + jvmName + "'. Hello!");
				return null;
			}
		});
		
		// You could notice, that output of "in-process" vi-node 
		// are still prefixed for your convenience.
		// Same isolation applies to system properties too. 
		
		// Quick wait to catch up with console
		Thread.sleep(300);
	}	
}
