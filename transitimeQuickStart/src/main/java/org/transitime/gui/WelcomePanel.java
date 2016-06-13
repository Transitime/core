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

public class WelcomePanel {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public void WelcomePanelstart() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WelcomePanel window = new WelcomePanel();
					window.frame.setVisible(true);
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
		frame = new JFrame();
		frame.setBounds(100, 100, 408, 381);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel lblNewLabel = new JLabel("Welcome to the transiTime QuickStart guide!");
		lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 19));
		
		JButton btnNewButton = new JButton("Next");
		btnNewButton.setBackground(SystemColor.menu);
		btnNewButton.setFont(new Font("Arial", Font.PLAIN, 19));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				InputPanel windowinput = new InputPanel();
				windowinput.InputPanelstart();
				frame.dispose();

			}
		});
		
		JRadioButton rdbtnAdvancedMode = new JRadioButton("Advanced mode");
		
		JRadioButton rdbtnBasicMode = new JRadioButton("Basic mode");
		rdbtnBasicMode.setBackground(SystemColor.menu);
		rdbtnBasicMode.setFont(new Font("Tahoma", Font.PLAIN, 13));
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		btnCancel.setBackground(SystemColor.menu);
		btnCancel.setFont(new Font("Arial", Font.PLAIN, 19));
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
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
		frame.getContentPane().setLayout(groupLayout);
	}

}
