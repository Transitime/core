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
package org.transitime.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.datatransfer.*;
import java.awt.Toolkit;
import org.transitime.db.webstructs.ApiKey;

import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.SystemColor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
/**
 * 
 * @author Brendan Egan
 *
 */
public class OutputPanel {

	private JFrame frmTransitimequickstart;
	private JTextField textField;
	private JTextField textField_1;
	private String apiKey;
	private JTextField textField_2;
	private JTextField textField_3;

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
		frmTransitimequickstart.setBounds(100, 100, 590, 538);
		frmTransitimequickstart.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel lblInstallationIsNow = new JLabel("Installation is now up and running! you can copy and paste ");
		lblInstallationIsNow.setFont(new Font("Arial", Font.PLAIN, 19));
		
		JLabel lblTheseLinksInto = new JLabel("these links into the OneBusawayQuickstart");
		lblTheseLinksInto.setFont(new Font("Arial", Font.PLAIN, 19));
		
		JLabel lblTripUpdatesUrl = new JLabel("Trip Updates URL");
		lblTripUpdatesUrl.setFont(new Font("Arial", Font.PLAIN, 16));
		
		textField = new JTextField();
		textField.setColumns(10);
		//TODO get agency id automatically using system.getproperty
		textField.setText("http://127.0.0.1:8080/api/v1/key/"+apiKey+"/agency/02/command/gtfs-rt/tripUpdates?format=human");
		
		JLabel lblVechiclePositionsUrl = new JLabel("Vechicle Positions URL");
		lblVechiclePositionsUrl.setFont(new Font("Arial", Font.PLAIN, 16));
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		//TODO get agency id automatically using system.getproperty
		textField_1.setText("http://127.0.0.1:8080/api/v1/key/"+apiKey+"/agency/02/command/gtfs-rt/vehiclePositions?format=human");
		JButton btnMinimize = new JButton("Minimize");
		btnMinimize.setBackground(SystemColor.menu);
		
		JButton btnShowMap = new JButton("Show map");
		btnShowMap.setBackground(SystemColor.menu);
		
		textField_2 = new JTextField();
		textField_2.setColumns(10);
		textField_2.setText("http://127.0.0.1:8080/api/");
		
		JLabel lblApikey = new JLabel("transitime Server address");
		lblApikey.setFont(new Font("Arial", Font.PLAIN, 16));
		
		textField_3 = new JTextField();
		textField_3.setColumns(10);
		textField_3.setText("http://127.0.0.1:8081/webapp/");
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
		GroupLayout groupLayout = new GroupLayout(frmTransitimequickstart.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addContainerGap(55, Short.MAX_VALUE)
							.addComponent(lblInstallationIsNow, GroupLayout.PREFERRED_SIZE, 511, GroupLayout.PREFERRED_SIZE))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addGap(31)
							.addComponent(btnMinimize, GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
							.addGap(91)
							.addComponent(btnShowMap, GroupLayout.PREFERRED_SIZE, 201, GroupLayout.PREFERRED_SIZE)
							.addGap(29))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, 481, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnCopy, GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(textField_1, GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(button, GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblVechiclePositionsUrl))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblTripUpdatesUrl)))
					.addContainerGap())
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblApikey, GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
					.addGap(351))
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(textField_2, 279, 279, 279)
					.addContainerGap(287, Short.MAX_VALUE))
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblWebappAddress, GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
					.addGap(359))
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(textField_3, 279, 279, 279)
					.addContainerGap(287, Short.MAX_VALUE))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(84)
					.addComponent(lblTheseLinksInto)
					.addContainerGap(134, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(7)
					.addComponent(lblInstallationIsNow, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
					.addGap(2)
					.addComponent(lblTheseLinksInto)
					.addGap(18)
					.addComponent(lblTripUpdatesUrl, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnCopy))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblVechiclePositionsUrl)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(button))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblApikey)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(lblWebappAddress)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(textField_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(69)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnMinimize, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnShowMap, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addGap(23))
		);
		frmTransitimequickstart.getContentPane().setLayout(groupLayout);
	}
}
