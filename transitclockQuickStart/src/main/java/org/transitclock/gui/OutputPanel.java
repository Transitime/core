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
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.datatransfer.*;
import java.awt.Toolkit;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.awt.event.ActionEvent;
/** 
 * shows the output screen with the value needed to enter into the onebusaway transitimeQuickStart
 * 
 * @author Brendan Egan
 *
 */
public class OutputPanel {

	private JFrame frmTransitimequickstart;
	private JTextField textField;
	private JTextField textField_1;
	private String apiKey;
	private JTextField txtHttpweb;
	private String agencyid=null;

	/**
	 * Launch the application.
	 */
	public void OutputPanelstart(){
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					OutputPanel window = new OutputPanel(apiKey);
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
	public OutputPanel(String apikey) {
		apiKey=apikey;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmTransitimequickstart = new JFrame();
		frmTransitimequickstart.setTitle("transiTimeQuickStart");
		frmTransitimequickstart.setBounds(100, 100, 590, 440);
		frmTransitimequickstart.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel lblInstallationIsNow = new JLabel("Installation is now up and running! you can copy and paste ");
		lblInstallationIsNow.setFont(new Font("Arial", Font.PLAIN, 19));
		
		JLabel lblTheseLinksInto = new JLabel("these links into the OneBusawayQuickstart");
		lblTheseLinksInto.setFont(new Font("Arial", Font.PLAIN, 19));
		
		JLabel lblTripUpdatesUrl = new JLabel("Trip Updates URL");
		lblTripUpdatesUrl.setFont(new Font("Arial", Font.PLAIN, 16));
		
		textField = new JTextField();
		textField.setColumns(10);
		//gets the agencyid
		agencyid=System.getProperties().getProperty("transitclock.core.agencyId");
		textField.setText("http://127.0.0.1:8080/api/v1/key/"+apiKey+"/agency/"+agencyid+"/command/gtfs-rt/tripUpdates");
		
		JLabel lblVechiclePositionsUrl = new JLabel("Vechicle Positions URL");
		lblVechiclePositionsUrl.setFont(new Font("Arial", Font.PLAIN, 16));
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setText("http://127.0.0.1:8080/api/v1/key/"+apiKey+"/agency/"+agencyid+"/command/gtfs-rt/vehiclePositions");
		
		txtHttpweb = new JTextField();
		txtHttpweb.setColumns(10);
		txtHttpweb.setText("http://127.0.0.1:8080/web");
		JLabel lblWebappAddress = new JLabel("transitime Webapp address");
		lblWebappAddress.setFont(new Font("Arial", Font.PLAIN, 16));
		
		JButton btnCopy = new JButton("Copy");
		btnCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			String copy = textField.getText();
			StringSelection stringSelection = new StringSelection(copy);
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(stringSelection, null);
			}
		});
		
		JButton button = new JButton("Copy");
		button.addActionListener(new ActionListener() {
			//Copys the content of the textfield to clipboard.
			public void actionPerformed(ActionEvent e) {
				String copy = textField_1.getText();
				StringSelection stringSelection = new StringSelection(copy);
				Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
				clpbrd.setContents(stringSelection, null);
			}
		});
		
		JButton btnOpenInBrowser = new JButton("Open in browser");
		btnOpenInBrowser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					java.awt.Desktop.getDesktop().browse(new java.net.URI("http://127.0.0.1:8080/web"));
				} catch (IOException e1) {
					
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					
					e1.printStackTrace();
				}
			}
		});
		
		JButton btnOpenInBroswer = new JButton("Open in browser");
		btnOpenInBroswer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					//opens in default browser at the location specfied
					java.awt.Desktop.getDesktop().browse(new java.net.URI("http://127.0.0.1:8080/api/v1/key/"+apiKey+"/agency/"+agencyid+"/command/gtfs-rt/tripUpdates?format=human"));
				} catch (IOException e1) {
					
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					
					e1.printStackTrace();
				}
			}
		});
		
		JButton btnNewButton = new JButton("Open in browser");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					//opens in default browser at the location specfied
					java.awt.Desktop.getDesktop().browse(new java.net.URI("http://127.0.0.1:8080/api/v1/key/"+apiKey+"/agency/"+agencyid+"/command/gtfs-rt/vehiclePositions?format=human"));
				} catch (IOException e1) {
				
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					
					e1.printStackTrace();
				}
			}
		});
		
		JButton btnStop = new JButton("Stop transiTime");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		//automatically generated code, created by the jwing window editor
		GroupLayout groupLayout = new GroupLayout(frmTransitimequickstart.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addContainerGap(50, Short.MAX_VALUE)
							.addComponent(lblInstallationIsNow, GroupLayout.PREFERRED_SIZE, 511, GroupLayout.PREFERRED_SIZE))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, 481, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnCopy, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(textField_1, GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(button, GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblWebappAddress, GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
							.addGap(83)
							.addComponent(btnNewButton))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addGap(84)
							.addComponent(lblTheseLinksInto))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblVechiclePositionsUrl)
							.addPreferredGap(ComponentPlacement.RELATED, 258, Short.MAX_VALUE)
							.addComponent(btnOpenInBroswer))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblTripUpdatesUrl))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(txtHttpweb, GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE))
						.addGroup(Alignment.TRAILING, groupLayout.createParallelGroup(Alignment.LEADING, false)
							.addComponent(btnStop, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(btnOpenInBrowser, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(7)
					.addComponent(lblInstallationIsNow, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
					.addGap(2)
					.addComponent(lblTheseLinksInto)
					.addGap(24)
					.addComponent(lblTripUpdatesUrl, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnCopy))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblVechiclePositionsUrl)
						.addComponent(btnOpenInBroswer))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(button))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnNewButton)
							.addGap(8))
						.addComponent(lblWebappAddress))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(txtHttpweb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnOpenInBrowser)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnStop)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		frmTransitimequickstart.getContentPane().setLayout(groupLayout);
	}
}
