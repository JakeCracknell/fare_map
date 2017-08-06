package com.cracknellj.fare.ws.resource;

import javax.ws.rs.client.ClientBuilder;

import com.cracknellj.fare.ws.Main;
import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HealthCheckResourceTest {
    private HttpServer server;

    @Before
    public void setUp() throws Exception {
        Main.setServerAddress("localhost");
        server = Main.startServer();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdownNow();
    }

    @Test
    public void testGetHealthCheckEndpoint() {
        String responseMsg = ClientBuilder.newClient()
                .target(Main.baseUri).path("healthcheck")
                .request().get(String.class);
        assertEquals("Got it!", responseMsg);
    }

    @Test
    public void testGetStaticContent() {
        String css = ClientBuilder.newClient().target(
                Main.baseUri.replace("fare/", "fare/pricechanges.css")
        ).request().get(String.class);

        assertTrue(css.contains("background-color"));
    }
}
