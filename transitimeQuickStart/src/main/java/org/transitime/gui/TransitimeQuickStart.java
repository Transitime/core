package org.transitime.gui;

import java.awt.EventQueue;

public class TransitimeQuickStart {
	public static void main (String args[])
	{
		WelcomePanel window = new WelcomePanel();
		window.WelcomePanelstart();
		InputPanel windowinput = new InputPanel();
		windowinput.InputPanelstart();
		OutputPanel windowoutput = new OutputPanel();
		windowoutput.OutputPanelstart();
	}
}


