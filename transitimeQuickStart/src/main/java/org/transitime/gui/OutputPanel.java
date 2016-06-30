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
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import net.miginfocom.swing.MigLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.SystemColor;
/**
 * 
 * @author Brendan Egan
 *
 */
public class OutputPanel {

	private JFrame frmTransitimequickstart;
	private JTextField textField;
	private JTextField textField_1;

	/**
	 * Launch the application.
	 */
	public void OutputPanelstart(){
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					OutputPanel window = new OutputPanel();
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
	public OutputPanel() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmTransitimequickstart = new JFrame();
		frmTransitimequickstart.setTitle("transiTimeQuickStart");
		frmTransitimequickstart.setBounds(100, 100, 543, 606);
		frmTransitimequickstart.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel lblInstallationIsNow = new JLabel("Installation is now up and running! you can copy and paste ");
		lblInstallationIsNow.setFont(new Font("Arial", Font.PLAIN, 19));
		
		JLabel lblTheseLinksInto = new JLabel("these links into the OneBusawayQuickstart");
		lblTheseLinksInto.setFont(new Font("Arial", Font.PLAIN, 19));
		
		JLabel lblTripUpdatesUrl = new JLabel("Trip Updates URL");
		lblTripUpdatesUrl.setFont(new Font("Arial", Font.PLAIN, 16));
		
		textField = new JTextField();
		textField.setColumns(10);
		
		JLabel lblVechiclePositionsUrl = new JLabel("Vechicle Positions URL");
		lblVechiclePositionsUrl.setFont(new Font("Arial", Font.PLAIN, 16));
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		
		JButton btnMinimize = new JButton("Minimize");
		btnMinimize.setBackground(SystemColor.menu);
		
		JButton btnShowMap = new JButton("Show map");
		btnShowMap.setBackground(SystemColor.menu);
		GroupLayout groupLayout = new GroupLayout(frmTransitimequickstart.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(7)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblInstallationIsNow, GroupLayout.PREFERRED_SIZE, 511, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblTheseLinksInto)
								.addGroup(groupLayout.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(lblVechiclePositionsUrl)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, 335, GroupLayout.PREFERRED_SIZE))))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(btnMinimize, GroupLayout.PREFERRED_SIZE, 249, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(btnShowMap, GroupLayout.PREFERRED_SIZE, 247, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblTripUpdatesUrl)
							.addPreferredGap(ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, 335, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(7)
					.addComponent(lblInstallationIsNow, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
					.addGap(4)
					.addComponent(lblTheseLinksInto)
					.addGap(57)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblTripUpdatesUrl, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE))
					.addGap(61)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblVechiclePositionsUrl)
						.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED, 196, Short.MAX_VALUE)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnMinimize, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnShowMap, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		frmTransitimequickstart.getContentPane().setLayout(groupLayout);
	}
}
