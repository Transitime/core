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
import javax.swing.JTextArea;
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
		frmTransitimequickstart = new JFrame();
		frmTransitimequickstart.setTitle("transiTimeQuickStart");
		frmTransitimequickstart.setBounds(100, 100, 408, 381);
		frmTransitimequickstart.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmTransitimequickstart.getContentPane().setLayout(null);
		
		JTextArea txtrEx = new JTextArea();
		txtrEx.setText(message+"\n"+ex.getMessage());
		
		txtrEx.setBounds(12, 57, 366, 264);
		frmTransitimequickstart.getContentPane().add(txtrEx);
		
		JLabel lblErrorIn = new JLabel("Error in starting TransitimeQuickStart:");
		lblErrorIn.setFont(new Font("Arial", Font.PLAIN, 16));
		lblErrorIn.setBounds(12, 13, 366, 31);
		frmTransitimequickstart.getContentPane().add(lblErrorIn);
	}
}
