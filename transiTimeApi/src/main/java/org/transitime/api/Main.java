package org.transitime.api;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

/**
 * Main class.
 *
 */
public class Main {   

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @param baseUrl 
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer(String baseUrl) {
        // create a resource config that scans for JAX-RS resources and providers    	
        final ResourceConfig rc = new ApiApplication();

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at baseUrl
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUrl), rc);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
   
	public static void main(String[] args) throws IOException {
    	Options options = new Options();
    	Option helpOption = new Option("h", "help", false, "Display usage and help info.");
		Option urlOption=new Option("u", "url", true, "This s the url of to start the server on");		
		options.addOption(urlOption);
		options.addOption(helpOption);

		// Parse the options
		CommandLineParser parser = new BasicParser();
		CommandLine cmd;
		try {
			cmd = parser.parse( options, args);
			String baseUrl=null;
			
			if (cmd.hasOption("h")) 
			{			
				printHelp(options);				
			}else
			
			if (cmd.hasOption("url")) 
			{	
				baseUrl=cmd.getOptionValue("url");
				System.out.println("Trying to start server on:"+baseUrl);
				final HttpServer server = startServer(baseUrl);
				System.out.println(String.format("Jersey app started with WADL available at "
	                + "%sapplication.wadl\nHit enter to stop it...", baseUrl));
				System.in.read();
				server.stop();
			}else
			{
				printHelp(options);	
			}
		} catch (ParseException e) {
			
			printHelp(options);
			System.exit(0);
		}
		
    }
	static void printHelp(Options options)
	{
		final String commandLineSyntax = "java transitime.jar";
		final PrintWriter writer = new PrintWriter(System.out);
		final HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(writer,
								80, // printedRowWidth
								commandLineSyntax,
								"args:", // header
								options,
								2,             // spacesBeforeOption
								2,             // spacesBeforeOptionDescription
								null,          // footer
								true);         // displayUsage
		writer.close();
		
	}
}

