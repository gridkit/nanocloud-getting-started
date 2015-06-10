package org.gridkit.lab.examples.nanocloud;

import java.lang.management.ManagementFactory;
import java.rmi.Remote;
import java.util.concurrent.Callable;

import org.gridkit.nanocloud.Cloud;
import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViProps;
import org.junit.Test;

public class TransaprentRmi extends BaseCloudTest {

	public Cloud createLocalCloud() {
		Cloud cloud = CloudFactory.createCloud();
		// this will configure "local" vi-node type by default
		ViProps.at(cloud.node("**")).setLocalType();
		return cloud;
	}
	
	// Extending java.rmi.Remote may interface eligible
	// for automatic exporting.
	public static interface EchoServer extends Remote {
		
		public String echo(String text);
		
	}
	
	@Test
	public void master_to_slave_auto_export() throws InterruptedException {
		// let's create our cloud
		cloud = createLocalCloud();
		
		// below is echo server implementation
		final EchoServer server = new EchoServer() {
			
			@Override
			public String echo(String text) {
				String vmname = ManagementFactory.getRuntimeMXBean().getName();
				System.out.println("(" + vmname +") echo: " + text);
				return vmname + ":" + text;
			}
		};
		
		
		cloud.node("slave").exec(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				
				String vmname = ManagementFactory.getRuntimeMXBean().getName();
				
				// we can access to final local variables from remote node
				// (but not to fields of outter class)
				String echo = server.echo("Ping from " + vmname);
				System.out.println("ECHO REPLAY: " + echo);
				
				return null;
			}
		});
		
		// As you can see calling server from slave, really invokes
		// instance created in master JVM.
		// Magic is happening during serialization. Instead of serializing instance
		// of service it is replaced by stub, which automatically wrapped in dynamic proxy 
		// during deserialization at slave.
		
		// catch up with console output
		Thread.sleep(300);
	}

	@Test
	public void slave_to_master_auto_export() throws InterruptedException {
		// let's create our cloud
		cloud = createLocalCloud();
		
		// In this example we will create server on slave
		// and return reference to master.
		EchoServer server =	cloud.node("slave").exec(new Callable<EchoServer>() {

			@Override
			public EchoServer call() throws Exception {

				// below is echo server implementation
				final EchoServer server = new EchoServer() {
					
					@Override
					public String echo(String text) {
						String vmname = ManagementFactory.getRuntimeMXBean().getName();
						System.out.println("(" + vmname +") echo: " + text);
						return vmname + ":" + text;
					}
				};
				
				System.out.println("Server created");

				return server;
			}
		});
		
		// console output is asynchronous, so let's give it chance to catch up
		Thread.sleep(100);
		
		System.out.println("ECHO REPLAY: " + server.echo("master"));
		System.out.println("server.getClass() - " + server.getClass().getName());
		
		
		// Magic of auto export works both ways.
		// As you can see, reference to real server object was
		// automatically replaced by dynamic proxy.
		
		// catch up with console output
		Thread.sleep(300);
	}	

	@Test
	public void transitive_auto_export() throws InterruptedException {
		// let's create our cloud
		cloud = createLocalCloud();
		
		// in this example we need 2 slaves
		cloud.nodes("slave1", "slave2").touch();
		
		// Let's create server on one slave
		final EchoServer server =	cloud.node("slave1").exec(new Callable<EchoServer>() {

			@Override
			public EchoServer call() throws Exception {

				// below is echo server implementation
				final EchoServer server = new EchoServer() {
					
					@Override
					public String echo(String text) {
						String vmname = ManagementFactory.getRuntimeMXBean().getName();
						System.out.println("(" + vmname +") echo: " + text);
						return vmname + ":" + text;
					}
				};
				
				System.out.println("Server created");

				return server;
			}
		});
		
		// console output is asynchronous, so let's give it chance to catch up
		Thread.sleep(100);

		// and then try to use it from another slave
		
		cloud.node("slave2").exec(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				
				String vmname = ManagementFactory.getRuntimeMXBean().getName();
				
				// we can access to final local variables from remote node
				// (but not to fields of outter class)
				String echo = server.echo("Ping from " + vmname);
				System.out.println("ECHO REPLAY: " + echo);
				
				return null;
			}
		});

		
		// All communication in cloud are master-to-slave only.
		// There is no slave-to-slave communications.
		// In example above, master acts as relay.
		// Please keep it in mind.
		
		// catch up with console output
		Thread.sleep(300);
	}	
}
