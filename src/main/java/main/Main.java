package main;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Created by vladislav on 18.03.16.
 */
public class Main {

    public static boolean isNumeric(String s) {
        return s.matches("\\d+");
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    public static void main(String[] args) throws Exception {
        int port = -1;
        if (args.length == 1 && isNumeric(args[0])) {
            port = Integer.valueOf(args[0]);
        } else {
            System.err.println("Specify port");
            System.exit(1);
        }

        System.out.append("Starting at port: ").append(String.valueOf(port)).append('\n');

        final Server server = new Server(port);
        final ServletContextHandler contextHandler = new ServletContextHandler(server, "db/api/", ServletContextHandler.SESSIONS);

        final ServletHolder servletHolder = new ServletHolder(ServletContainer.class);
        servletHolder.setInitParameter("javax.ws.rs.Application", "rest.RestApplication");

        server.setHandler(contextHandler);
        contextHandler.addServlet(servletHolder, "/*");
        server.start();
        server.join();
    }
}