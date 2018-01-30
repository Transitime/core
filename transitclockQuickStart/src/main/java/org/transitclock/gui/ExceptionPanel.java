/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitclock.gui;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import javax.swing.JScrollPane;
/**
 * 
 * @author Brendan Egan
 *
 */
public class ExceptionPanel {
	String message = null;
	Exception ex = null;
	private JFrame frmTransitimequickstart;

	/**
	 * Launch the application.
	 */
	public void ExceptionPanelstart() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ExceptionPanel window = new ExceptionPanel(message,ex);
					window.frmTransitimequickstart.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ExceptionPanel(String message, Exception ex) {
		
		this.message=message;
		this.ex=ex;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		JPanel middlePanel = new JPanel ();
	    middlePanel.setBorder ( new TitledBorder ( new EtchedBorder (), "Error Starting TransitimeQuickStart" ) );

	    // create the middle panel components

	    JTextArea display = new JTextArea ( 35, 90 );
	    display.setEditable ( false ); // set textArea non-editable
	    JScrollPane scroll = new JScrollPane ( display );
	    scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );

	    //Add Textarea in to middle panel
	    middlePanel.add ( scroll );

	   
	    JFrame frame = new JFrame ();
	    frame.add ( middlePanel );
	    frame.pack ();
	    frame.setLocationRelativeTo ( null );
	    frame.setVisible ( true );
	    String stackTrace = ExceptionUtils.getStackTrace(ex);
	    display.setText(message+"\n"+ex.toString()+"\n"+stackTrace);
	}
}     
   
