/*
 *  mod_cluster
 *
 *  Copyright(c) 2009 Red Hat Middleware, LLC,
 *  and individual contributors as indicated by the @authors tag.
 *  See the copyright.txt in the distribution for a
 *  full listing of individual contributors.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library in the file COPYING.LIB;
 *  if not, write to the Free Software Foundation, Inc.,
 *  59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
 *
 * @author Jean-Frederic Clere
 * @version $Revision$
 */

package org.jboss.mod_cluster;

import java.io.IOException;
import java.net.ServerSocket;

import junit.framework.TestCase;

import org.apache.catalina.Engine;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardServer;

public class TestDigest extends TestCase {

    /* Test Digest that is working
     * That needs something like AdvertiseSecurityKey secret in httpd.conf
     */
    public void testDigest() {

        boolean clienterror = false;
        StandardServer server = Maintest.getServer();
        JBossWeb service = null;
        LifecycleListener cluster = null;

        System.out.println("TestDigest Started");
        try {

            service = new JBossWeb("node1",  "localhost");
            service.addConnector(8011);
            server.addService(service);
 
            cluster = Maintest.createClusterListener("224.0.1.105", 23364, false, null, true, false, true, "secret");
            server.addLifecycleListener(cluster);

        } catch(IOException ex) {
            ex.printStackTrace();
            fail("can't start service");
        }

        // start the server thread.
        ServerThread wait = new ServerThread(3000, server);
        wait.start();
         
        // Wait until httpd as received the nodes information.
        int tries = Maintest.WaitForHttpd(cluster, 60);
        if (tries == -1) {
            fail("can't find PING-RSP in proxy response");
        }
        if (tries == 60) {
            fail("can't find proxy");
        }

        // Stop the jboss and remove the services.
        try {
            wait.stopit();
            wait.join();

            server.removeService(service);
            server.removeLifecycleListener(cluster);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            fail("can't stop service");
        }

        // Wait until httpd as received the stop messages.
        int countinfo = 0;
        while ((!Maintest.checkProxyInfo(cluster, null)) && countinfo < 20) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            countinfo++;
        }
        System.gc();


        try {

            service = new JBossWeb("node1",  "localhost");
            service.addConnector(8011);
            server.addService(service);
 
            cluster = Maintest.createClusterListener("224.0.1.105", 23364, false, null, true, false, true, "public");
            server.addLifecycleListener(cluster);

        } catch(IOException ex) {
            ex.printStackTrace();
            fail("can't start service");
        }

        // start the server thread.
        wait = new ServerThread(3000, server);
        wait.start();
         
        // We shouldn't be able to find httpd to proxy for us.
        tries = Maintest.WaitForHttpd(cluster, 30);
        if (tries == -1) {
            fail("can't find PING-RSP in proxy response");
        }
        if (tries != 30) {
            fail("Shouldn't find a proxy");
        }

        // Stop the jboss and remove the services.
        try {
            wait.stopit();
            wait.join();

            server.removeService(service);
            server.removeLifecycleListener(cluster);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            fail("can't stop service");
        }

        // Wait until httpd as received the stop messages.
        try {
            Thread.sleep(20000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.gc();
        System.out.println("TestDigest Done");
    }
}
