
package com.dc.esc.monitor;


import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tuscany.sca.host.http.DefaultResourceServlet;
import org.apache.tuscany.sca.host.http.ServletMappingException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.thread.ThreadPool;

import com.dc.esb.container.protocol.http.HTTPProtocolConfig;
import com.dc.esb.container.protocol.http.server.HTTPServerConnector;
import com.dc.esb.container.protocol.http.server.HTTPServiceListenerServlet;
import com.dc.esb.container.protocol.http.server.JettyThreadPool;

public class HttpServer {
	
	private static Log LOG = LogFactory.getLog( HttpServer.class );
	
	private static final HttpServer ourInstance = new HttpServer();

	private final Object joinLock = new Object();

	private boolean sendServerVersion;
//	private WorkScheduler workScheduler;
	private int defaultPort = 8080;

	public static HttpServer getInstance() {
		return ourInstance;
	}

	private HttpServer() {
	}

	private Map< Integer, Port > ports = new HashMap< Integer, Port >();

	private String contextPath = "/";

	public void setDefaultPort( int port ) {
		defaultPort = port;
	}

	public int getDefaultPort() {
		return defaultPort;
	}

	public void setSendServerVersion( boolean sendServerVersion ) {
		this.sendServerVersion = sendServerVersion;
	}

	/**
	 * Stop all the started servers.
	 */
	public void stop() {
		synchronized ( joinLock ) {
			joinLock.notifyAll();
		}
		try {
			Set< Map.Entry< Integer, Port >> entries = new HashSet< Map.Entry< Integer, Port >>(ports.entrySet());
			for ( Map.Entry< Integer, Port > entry : entries ) {
				Port port = entry.getValue();
				Server server= port.getServer();
				server.stop();
				ports.remove( entry.getKey() );
			}
		} catch ( Exception e ) {
			LOG.error( e.getMessage(), e );
		}
	}

	public void addServletMapping( String suri, Servlet servlet )
			throws ServletMappingException {
		URI uri = URI.create( suri );

		// Get the URI scheme and port
		String scheme = uri.getScheme();
		if ( scheme == null ) {
			scheme = "http";
		}
		int portNumber = uri.getPort();
		if ( portNumber == -1 ) {
			portNumber = defaultPort;
		}
		Port port = ports.get( portNumber );
		if ( port == null ) {
			try {
				Server server = new Server();
				if ( "https".equals( scheme )) {
					SslSocketConnector sslConnector = new SslSocketConnector();
					sslConnector.setPort( portNumber );
					configureSSL( sslConnector,((HTTPServiceListenerServlet)servlet).getConnector().getConfig() );
					server.setConnectors( new Connector[] { sslConnector } );
				} else {
					SelectChannelConnector selectConnector = new SelectChannelConnector();
					
					if(HTTPServiceListenerServlet.class.isInstance(servlet)){
						HTTPServiceListenerServlet hsls = ( HTTPServiceListenerServlet )servlet;
						HTTPServerConnector hsc = hsls.getConnector();
						HTTPProtocolConfig config = hsc.getConfig();
						selectConnector.setSoLingerTime( config.getAdvancedParams().getSoLinger() );
						selectConnector.setReuseAddress( config.getAdvancedParams().isReuseAddress() );
						selectConnector.setRequestBufferSize( config.getAdvancedParams().getReadBufferSize() );
						selectConnector.setResponseBufferSize( config.getAdvancedParams().getWriteBufferSize() );
						selectConnector.setLowResourcesConnections( config.getAdvancedParams().getMaxConnCount() );
						selectConnector.setAcceptQueueSize( 2000 );
						selectConnector.setPort( portNumber );
						selectConnector.setThreadPool( (ThreadPool) new JettyThreadPool( config.getAdvancedParams().getThreadCount() ) );
					} else {
						selectConnector.setSoLingerTime(5);
			            selectConnector.setReuseAddress(true);
			            selectConnector.setRequestBufferSize(64000);
			            selectConnector.setLowResourcesConnections(1000);
			            selectConnector.setAcceptQueueSize(2000);
			            selectConnector.setPort(portNumber);
			            selectConnector.setThreadPool(new JettyThreadPool(700));
					}

					server.setConnectors( new Connector[] { selectConnector } );
					
				}

				ServletContextHandler handler = new ServletContextHandler();
				server.setHandler(handler);
				SessionHandler sessionHandler = new SessionHandler();
				ServletHandler servletHandler = new ServletHandler();
				sessionHandler.setHandler(servletHandler);
				handler.setServletHandler(servletHandler);
				server.setStopAtShutdown(true);
				server.setSendServerVersion(sendServerVersion);
				server.start();
				// Keep track of the new server and Servlet handler
				port = new Port(server, servletHandler);
				ports.put(portNumber, port);

			} catch ( Exception e ) {
				throw new ServletMappingException( e );
			}
		}

		// Register the Servlet mapping
		ServletHandler servletHandler = port.getServletHandler();
		ServletHolder holder;
		if ( servlet instanceof DefaultResourceServlet ) {
			throw new IllegalAccessError("servlet should not be kind of DefaultResourceServlet.");
			// // Optimize the handling of resource requests, use the Jetty
			// default Servlet
			// // instead of our default resource Servlet
			// String servletPath = uri.getPath();
			// if (servletPath.endsWith("*")) {
			// servletPath = servletPath.substring(0, servletPath.length() - 1);
			// }
			// if (servletPath.endsWith("/")) {
			// servletPath = servletPath.substring(0, servletPath.length() - 1);
			// }
			// if (!servletPath.startsWith("/")) {
			// servletPath = '/' + servletPath;
			// }
			//
			// DefaultResourceServlet resourceServlet = (DefaultResourceServlet)
			// servlet;
			// DefaultServlet defaultServlet = new
			// JettyDefaultServlet(servletPath,
			// resourceServlet.getDocumentRoot());
			// holder = new ServletHolder(defaultServlet);

		} else {
			holder = new ServletHolder( servlet );
		}
		servletHandler.addServlet( holder );

		ServletMapping mapping = new ServletMapping();
		mapping.setServletName( holder.getName() );
		String path = uri.getPath();

		if ( !path.startsWith( "/" ) ) {
			path = '/' + path;
		}

		if ( !path.startsWith( contextPath ) ) {
			path = contextPath + path;
		}

		mapping.setPathSpec( path );
		servletHandler.addServletMapping( mapping );

		// Compute the complete URL
		String host;
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch ( UnknownHostException e ) {
			host = "localhost";
		}
		URL addedURL;
		try {
			addedURL = new URL( scheme, host, portNumber, path );
		} catch ( MalformedURLException e ) {
			throw new ServletMappingException( e );
		}
		LOG.info( "Added Servlet mapping: " + addedURL );
	}

	public Servlet removeServletMapping( String suri ) {
		URI uri = URI.create( suri );

		// Get the URI port
		int portNumber = uri.getPort();
		if ( portNumber == -1 ) {
			portNumber = defaultPort;
		}

		// Get the port object associated with the given port number
		Port port = ports.get( portNumber );
		if ( port == null ) {
			throw new IllegalStateException(
					"No servlet registered at this URI: " + suri );
		}

		// Remove the Servlet mapping for the given Servlet
		ServletHandler servletHandler = port.getServletHandler();
		Servlet removedServlet = null;
		List< ServletMapping > mappings = new ArrayList< ServletMapping >(
				Arrays.asList( servletHandler.getServletMappings() ) );
		String path = uri.getPath();

		if ( !path.startsWith( "/" ) ) {
			path = '/' + path;
		}

		if ( !path.startsWith( contextPath ) ) {
			path = contextPath + path;
		}

		for ( ServletMapping mapping : mappings ) {
			if ( Arrays.asList( mapping.getPathSpecs() ).contains( path ) ) {
				try {
					removedServlet = servletHandler.getServlet(
							mapping.getServletName() ).getServlet();
				} catch ( ServletException e ) {
					throw new IllegalStateException( e );
				}
				mappings.remove( mapping );
				LOG.info( "Removed Servlet mapping: " + path );
				break;
			}
		}
		if ( removedServlet != null ) {
			servletHandler.setServletMappings( mappings
					.toArray( new ServletMapping[ mappings.size() ] ) );

			// Stop the port if there are no servlet mappings on it anymore
			if ( mappings.size() == 0 ) {
				try {
					port.getServer().stop();
				} catch ( Exception e ) {
					throw new IllegalStateException( e );
				}
				ports.remove( portNumber );
			}

		} else {
			LOG.warn( "Trying to Remove servlet mapping: " + path
					+ " where mapping is not registered" );
		}

		return removedServlet;
	}

	@SuppressWarnings("deprecation")
	private void configureSSL( SslSocketConnector connector, HTTPProtocolConfig config ) {
		connector.setProtocol( config.getSecurity().getProtocol() );
		connector.setKeystore( config.getSecurity().getKeyStore().getPath() );
		connector.setKeyPassword( config.getSecurity().getKeyStore().getKeyPass() );
		connector.setKeystoreType( config.getSecurity().getKeyStore().getType() );
		
		if ( config.getSecurity().getTrustStore() != null ) {
			connector.setTruststore( config.getSecurity().getTrustStore().getPath() );
			connector.setTrustPassword( config.getSecurity().getTrustStore().getKeyPass() );
			connector.setTruststoreType( config.getSecurity().getTrustStore().getType() );
		}
		connector.setNeedClientAuth( config.getSecurity().isBidirectional() );
	}
	
	private class Port {
		private Server server;
		private ServletHandler servletHandler;

		private Port( Server server, ServletHandler servletHandler ) {
			this.server = server;
			this.servletHandler = servletHandler;
		}

		public Server getServer() {
			return server;
		}

		public ServletHandler getServletHandler() {
			return servletHandler;
		}
	}
	
}
