package org.gridkit.lab.examples.nanocloud;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViProps;
import org.junit.Before;
import org.junit.Test;

public class BasicViNodeUsage extends BaseCloudTest {

	@Before
	public void prepareCloud() {
		cloud = CloudFactory.createCloud();
		// this will configure "local" vi-node type by default
		ViProps.at(cloud.node("**")).setLocalType();
	}
	
	@Test
	public void selecting_nodes() {
		
		// ViNode interface may represent single node
		// or group of nodes.
		// Our cloud object provides us with wild card based selectors.

		// let's declare couple of nodes
		// if requested node name doesn't have wild cards
		// node would be "materialized"
		cloud.nodes("node1", "node1.1", "node1.1.1");
		
		// Two start will match every possible name
		List<String> result = cloud.node("**").massExec(new Callable<String>() {
			@Override
			public String call() throws Exception {
				// this system property will hold node name
				return System.getProperty("vinode.name");
			}
		});
		
		System.err.println("'**' have matched: " + result);
		
		Assert.assertEquals(3, result.size());

		// One start will match any text between dots
		result = cloud.node("node1.*").massExec(new Callable<String>() {
			@Override
			public String call() throws Exception {
				// this system property will hold node name
				return System.getProperty("vinode.name");
			}
		});
		
		System.err.println("'node1.' have matched: " + result);
		
		Assert.assertEquals(2, result.size());

		// Heading and trailing dots in wild card below are treated 
		// in special way. Selector below will actually match "node1.1.1"
		result = cloud.node("*.node1.1.1.*").massExec(new Callable<String>() {
			@Override
			public String call() throws Exception {
				// this system property will hold node name
				return System.getProperty("vinode.name");
			}
		});
		
		System.err.println("'*.node1.1.1.*' have matched: " + result);
		
		Assert.assertEquals(1, result.size());		
	}

	@Test
	public void configuring_nodes() {
		// Only "materialized" vi-nodes will participate in task execution.
		// All configuration applied to wild card selectors
		// will be memorized and applied to newly materializing nodes.
		
		cloud.node("node1"); // let node materialize
		
		cloud.node("node*").setProp("test", "A");
		
		List<String> props = cloud.node("**").massExec(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return System.getProperty("test");
			}
		});
		
		Collections.sort(props);
		
		// Obviously, we have just one node and one instance of property value
		Assert.assertEquals(Arrays.asList("A"), props);

		// Now let's "materialize" next node
		
		cloud.node("node2");

		props = cloud.node("**").massExec(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return System.getProperty("test");
			}
		});

		Collections.sort(props);

		// Though, 'node2' was "materialized" after setProp() call,
		// it was memorized for "node*" pattern and will apply to every node
		// "materialized" afterwards
		Assert.assertEquals(Arrays.asList("A", "A"), props);		
	}
	
	@Test
	public void executing_code() throws InterruptedException, ExecutionException {
		// let's make 2 test vi-nodes
		cloud.nodes("node1", "node2");
		
		// ViNode has a bunch of methods for executing
		// Runnable`s and Callable`s
		// Instances of tasks should be either serializable
		// or anonymous classes (this is a special case added for convenience).
		
		// ViNode instance could be either single node or a group
		ViNode allNodes = cloud.node("**");

		// exec() will invoke task synchronously (but in parallel across nodes)
		allNodes.exec(new Runnable() {
			@Override
			public void run() {
				System.out.println("Hello");
			}
		});

		// In case of non anonymous class it should be serializeable
		allNodes.exec(new Hello());
		
	 
		// Asynchronous invocation is supported too
		Future<Void> f = allNodes.submit(new Hello());
		f.get();
		
		// mass* version of submit()/exec() allow you to express your intent 
		// in multiple target execution.
		// "mass" versions are different from "non-mass" version only in return types.
		final List<String> pids = allNodes.massExec(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return ManagementFactory.getRuntimeMXBean().getName();
			}
		});
		
		System.err.println("Slave pids: " + pids);

		// Non-mass version could be used with groups too
		// only one result will be returned.
		final String randompid = allNodes.exec(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return ManagementFactory.getRuntimeMXBean().getName();
			}
		});
		
		System.err.println("Random PID: " + randompid);

		// Even if we will get only one result, exec() will
		// wait for all task to complete and rethrow exception
		// if any of executions has failed. 
		try {
			allNodes.exec(new Callable<String>() {
				@Override
				public String call() throws Exception {
					if (!randompid.equals(ManagementFactory.getRuntimeMXBean().getName())) {
						System.out.println("I'm going to explode!");
						throw new IOException("BOOM");
					}
					return ManagementFactory.getRuntimeMXBean().getName();
				}
			});
			Assert.assertFalse("Shouldn't reach here", true);
		}
		catch(Exception e) {
			// Should catch IOException from slave process
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("serial")
	private static class Hello implements Runnable, Serializable {
		@Override
		public void run() {
			System.out.println("Hello");
		}
	}
}
