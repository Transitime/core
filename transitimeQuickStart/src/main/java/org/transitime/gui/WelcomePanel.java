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
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.JRadioButton;
import java.awt.Color;
import java.awt.SystemColor;
import net.miginfocom.swing.MigLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Canvas;
import java.awt.Window.Type;
/**
 * 
 * @author Brendan Egan
 *
 */
public class WelcomePanel {

	private JFrame frmTransitimequickstart;

	/**
	 * Launch the application.
	 */
	public void WelcomePanelstart() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WelcomePanel window = new WelcomePanel();
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
	public WelcomePanel() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmTransitimequickstart = new JFrame();
		frmTransitimequickstart.setTitle("transiTimeQuickStart");
		frmTransitimequickstart.setBounds(100, 100, 408, 381);
		frmTransitimequickstart.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel lblNewLabel = new JLabel("Welcome to the transiTime QuickStart guide!");
		lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 19));
		
		JButton btnNewButton = new JButton("Next");
		btnNewButton.setBackground(SystemColor.menu);
		btnNewButton.setFont(new Font("Arial", Font.PLAIN, 19));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				InputPanel windowinput = new InputPanel();
				windowinput.InputPanelstart();
				frmTransitimequickstart.dispose();

			}
		});
		
		JRadioButton rdbtnAdvancedMode = new JRadioButton("Advanced mode");
		
		JRadioButton rdbtnBasicMode = new JRadioButton("Basic mode");
		rdbtnBasicMode.setBackground(SystemColor.menu);
		rdbtnBasicMode.setFont(new Font("Tahoma", Font.PLAIN, 13));
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmTransitimequickstart.dispose();
			}
		});
		btnCancel.setBackground(SystemColor.menu);
		btnCancel.setFont(new Font("Arial", Font.PLAIN, 19));
		GroupLayout groupLayout = new GroupLayout(frmTransitimequickstart.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(11)
							.addComponent(lblNewLabel))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(144)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(rdbtnAdvancedMode)
								.addComponent(rdbtnBasicMode))))
					.addContainerGap(100, Short.MAX_VALUE))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(32)
					.addComponent(btnCancel, GroupLayout.PREFERRED_SIZE, 159, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnNewButton, GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
					.addGap(24))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(7)
					.addComponent(lblNewLabel)
					.addGap(151)
					.addComponent(rdbtnAdvancedMode)
					.addGap(18)
					.addComponent(rdbtnBasicMode)
					.addGap(41)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnNewButton)
						.addComponent(btnCancel))
					.addContainerGap())
		);
		frmTransitimequickstart.getContentPane().setLayout(groupLayout);
	}

}
