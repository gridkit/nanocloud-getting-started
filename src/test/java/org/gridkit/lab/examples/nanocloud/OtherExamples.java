package org.gridkit.lab.examples.nanocloud;

import java.net.Inet4Address;
import java.util.concurrent.Callable;

import org.gridkit.nanocloud.Cloud;
import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.nanocloud.RemoteNode;
import org.gridkit.vicluster.ViNode;
import org.junit.Test;

public class OtherExamples {

    @Test
    public void remote_hallo_world() {
        
        Cloud cloud = CloudFactory.createCloud();
        RemoteNode.at(cloud.node("**")).useSimpleRemoting();
        
        ViNode node = cloud.node("myserver.acme.com");
        node.exec(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                String localHost = Inet4Address.getLocalHost().getHostName();
                System.out.println("Hi! I'm running on " + localHost);
                return null;
            }
        });    
    }
}
