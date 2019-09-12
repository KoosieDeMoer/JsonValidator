package com.entersekt.jsonvalidatorrest;

import java.net.UnknownHostException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.entersekt.hub.common.RestCommonService;
import com.entersekt.topology.NodeRegisterService;
import com.entersekt.topology.NodeType;
import com.entersekt.utils.AppUtils;
import com.entersekt.utils.SwaggerUtils;

public class App {

	public static final String ME_WHO = "contractvalidator";
	private static final String HUB_WHO = "hub";
	static String myHostname;
	static int portNo;
	static String hubHostname;
	static int hubPortNo;
	static String persistanceHostname;
	static int persistancePort;

	private static final Logger log = LoggerFactory.getLogger(App.class);

	private NodeRegisterService nodeRegisterService = GuiceBindingsModule.injector
			.getInstance(NodeRegisterService.class);

	public static void main(String[] args) throws Exception {

		usage(args);
		new App().start();
	}

	public void start() throws Exception, UnknownHostException, InterruptedException {
		SwaggerUtils.buildSwaggerBean("Json Validator", "Json Validator", RestService.class.getPackage().getName()
				+ "," + RestCommonService.class.getPackage().getName());

		final HandlerList handlers = new HandlerList();

		ResourceConfig resourceConfig = new ResourceConfig();

		resourceConfig.packages(RestService.class.getPackage().getName(), RestCommonService.class.getPackage()
				.getName());

		SwaggerUtils.attachSwagger(handlers, App.class, resourceConfig);

		ServletContainer servletContainer = new ServletContainer(resourceConfig);
		ServletHolder jerseyServlet = new ServletHolder(servletContainer);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.addServlet(jerseyServlet, "/*");

		ContextHandler buildContext = context;
		handlers.addHandler(buildContext);

		Server jettyServer = new Server(portNo);

		jettyServer.setHandler(handlers);

		// tell the Hub about me
		nodeRegisterService.registerNodeWithHub(NodeType.CONTRACT_VALIDATOR, ME_WHO, ME_WHO, myHostname, portNo, "",
				hubHostname, hubPortNo);

		try {
			jettyServer.start();
			jettyServer.join();
		} finally {
			nodeRegisterService.deregister(ME_WHO, nodeRegisterService.getNode(HUB_WHO));
			jettyServer.destroy();
		}
	}

	private static void usage(String[] args) {

		if (args.length < 6) {
			log.error("Usage requires command line parameters MY_HOSTNAME MY_PORT HUB_HOSTNAME HUB_PORT PERSISTANCE_HOSTNAME PERSISTENCE_PORT, eg 192.168.99.100 8081 192.168.99.100 8080  192.168.99.100 5984");
			System.exit(0);
		} else {
			myHostname = args[0];
			portNo = AppUtils.extractPortNumber(args[1]);
			hubHostname = args[2];
			hubPortNo = AppUtils.extractPortNumber(args[3]);
			persistanceHostname = args[4];
			persistancePort = AppUtils.extractPortNumber(args[5]);
		}
	}

}
