package org.transitime.gui;

import javax.swing.JProgressBar;
import javax.swing.JTextPane;

import org.transitime.db.webstructs.ApiKey;
import org.transitime.quickstart.resource.QuickStartException;

public class TransitimeQuickStartThread implements Runnable{
	String filelocation = null;
	String realtimefeedURL;
	String loglocation;
	boolean startwebapp;
	String apikeystring;
	JTextPane progresstextPane;
	JProgressBar progressBar;
	InputPanel windowinput;
	@Override
	public void run() {

		
		try{
		progressBar.setValue(0);
		progresstextPane.setText("Extracting resources");
		TransitimeQuickStart start = new TransitimeQuickStart();
		start.extractResources();
		progressBar.setValue(10);
		progresstextPane.setText("Starting database");
		start.startDatabase();
		progressBar.setValue(15);
		progresstextPane.setText("Importing GTFS File to database, may take several minutes. Larger files will take longer to complete");
		start.startGtfsFileProcessor(filelocation);
		progressBar.setValue(50);
		progresstextPane.setText("Creating ApiKey");
		start.createApiKey();
		//creates an apikey, holds the key as a string in apikeystring
		ApiKey apikey = start.getApiKey();
		apikeystring = apikey.getKey();
		progressBar.setValue(55);
		progresstextPane.setText("Starting Core");
		
		start.startCore(realtimefeedURL, loglocation);
		progressBar.setValue(75);
		progresstextPane.setText("Adding api to server");
		start.addApi();
		
		//Starts webapp if selected in gui
		if (startwebapp == true) {
			progressBar.setValue(75);
			progresstextPane.setText("Adding Webapp to server");
			start.addWebapp();
			progressBar.setValue(80);
			progresstextPane.setText("Adding Webagency to server");
			start.webAgency();
		}
		progressBar.setValue(90);
		progresstextPane.setText("Starting server");
		start.startJetty(startwebapp);
		progressBar.setValue(100);
		progresstextPane.setText("Finished");
		//progress.dispose();
		//creates the output panel
		OutputPanel windowinput = new OutputPanel(apikeystring);
		windowinput.OutputPanelstart();
		}catch(QuickStartException qe)
		{
			
		}
		
	}
	
}
