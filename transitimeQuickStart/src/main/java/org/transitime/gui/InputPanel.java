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

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import java.awt.GridLayout;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.Button;
import java.awt.Color;
import java.awt.SystemColor;
import java.awt.event.InputMethodListener;
import java.io.File;
import java.awt.event.InputMethodEvent;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.transitime.db.webstructs.ApiKey;
import org.transitime.db.webstructs.ApiKeyManager;
import org.transitime.gui.TransitimeQuickStart;
/**
 * 
 * @author Brendan Egan
 *
 */
public class InputPanel extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private String filelocation=null;
	private String realtimefeedURL=null;
	private String loglocation=null;
	
	FileBrowser browse = new FileBrowser();
	private JRadioButton rdbtnStartWebapp;

	/**
	 * Launch the application.
	 */
	public void InputPanelstart(){
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					InputPanel frame = new InputPanel();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public InputPanel() {
		setFont(new Font("Dialog", Font.BOLD, 12));
		setTitle("transiTimeQuickStart");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 632, 546);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JLabel lblWelcomeToThe = new JLabel("Enter the locations of each object in the fields below");
		lblWelcomeToThe.setFont(new Font("Arial", Font.PLAIN, 19));
		
		JLabel lblGtfsFileLocation = new JLabel("GTFS file location:");
		lblGtfsFileLocation.setFont(new Font("Arial", Font.PLAIN, 19));
		
		textField = new JTextField();
		
			
	
		textField.setColumns(10);
		
		Button button_2 = new Button("i");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				InformationPanel infopanel = new InformationPanel();
				infopanel.InformationPanelstart();
			}
		});
		button_2.setBackground(SystemColor.textHighlight);
		
		JButton btnNewButton = new JButton("Browse");
		btnNewButton.setBackground(SystemColor.menu);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
			
			            int returnVal = fc.showOpenDialog(browse);

			            if (returnVal == JFileChooser.APPROVE_OPTION) {
			                File file = fc.getSelectedFile();
			                filelocation=file.getPath();
			                textField.setText(filelocation);
			            }
				
			}
		});
		
		JLabel lblGtfsrealtimeFeedLocationurl = new JLabel("GTFS-realtime feed location/URL:");
		lblGtfsrealtimeFeedLocationurl.setFont(new Font("Arial", Font.PLAIN, 19));
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		
		Button button_1 = new Button("i");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				InformationPanel infopanel = new InformationPanel();
				infopanel.InformationPanelstart();
				
			}
		});
		button_1.setBackground(SystemColor.textHighlight);
		
		JLabel lblInstallLocation = new JLabel("Log location:");
		lblInstallLocation.setFont(new Font("Arial", Font.PLAIN, 19));
		
		textField_2 = new JTextField();
		textField_2.setColumns(10);
		
		Button button = new Button("i");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				InformationPanel infopanel = new InformationPanel();
				infopanel.InformationPanelstart();
			}
		});
		button.setBackground(SystemColor.textHighlight);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBackground(SystemColor.menu);
		btnCancel.setFont(new Font("Arial", Font.PLAIN, 13));
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		JButton btnHelp = new JButton("Help");
		btnHelp.setBackground(SystemColor.menu);
		btnHelp.setFont(new Font("Arial", Font.PLAIN, 13));
		
		JButton btnNext = new JButton("Next");
		btnNext.setBackground(SystemColor.menu);
		btnNext.setFont(new Font("Arial", Font.PLAIN, 13));
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//reads in URL
				loglocation=textField_2.getText();
				realtimefeedURL=textField_1.getText();
				//Starts the GtfsFileProcessor ,ApiKey and core
				
				TransitimeQuickStart start=new TransitimeQuickStart();
				start.StartGtfsFileProcessor(filelocation);
				
				ApiKey apikey=start.CreateApikey();
				String apikeystring=apikey.getKey();
				
				start.StartCore(realtimefeedURL,loglocation);
				start.StartJettyapi(apikeystring);
				ApiKeyManager manager = ApiKeyManager.getInstance();
				boolean test=manager.isKeyValid(apikey.getKey());
				if(getRdbtnStartWebappSelected()==true)
				{
				start.StartJettyWebapp();
				}
				//Makes output Panel
				OutputPanel windowinput = new OutputPanel(apikeystring);
				windowinput.OutputPanelstart();
				dispose();
				
			}
		});
		btnNext.setVerticalAlignment(SwingConstants.BOTTOM);
		
		rdbtnStartWebapp = new JRadioButton("Start webapp");
		rdbtnStartWebapp.setFont(new Font("Arial", Font.PLAIN, 16));
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(58)
					.addComponent(lblWelcomeToThe))
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblGtfsFileLocation, GroupLayout.PREFERRED_SIZE, 223, GroupLayout.PREFERRED_SIZE)
					.addGap(19)
					.addComponent(btnNewButton)
					.addGap(18)
					.addComponent(textField, GroupLayout.PREFERRED_SIZE, 235, GroupLayout.PREFERRED_SIZE)
					.addGap(5)
					.addComponent(button_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblGtfsrealtimeFeedLocationurl, GroupLayout.PREFERRED_SIZE, 309, GroupLayout.PREFERRED_SIZE)
					.addGap(26)
					.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, 235, GroupLayout.PREFERRED_SIZE)
					.addGap(5)
					.addComponent(button_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblInstallLocation, GroupLayout.PREFERRED_SIZE, 309, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
					.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, 235, GroupLayout.PREFERRED_SIZE)
					.addGap(5)
					.addComponent(button, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(btnHelp, GroupLayout.PREFERRED_SIZE, 205, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(btnCancel, GroupLayout.PREFERRED_SIZE, 165, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(rdbtnStartWebapp, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnNext, GroupLayout.PREFERRED_SIZE, 197, GroupLayout.PREFERRED_SIZE)))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(4)
					.addComponent(lblWelcomeToThe)
					.addGap(53)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(btnNewButton)
							.addComponent(lblGtfsFileLocation))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(1)
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(button_2, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
					.addGap(49)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(2)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblGtfsrealtimeFeedLocationurl)))
						.addComponent(button_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(35)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(2)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblInstallLocation)))
						.addComponent(button, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(125)
					.addComponent(rdbtnStartWebapp, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnNext)
						.addComponent(btnHelp)
						.addComponent(btnCancel))
					.addContainerGap(27, Short.MAX_VALUE))
		);
		contentPane.setLayout(gl_contentPane);
	}
	public boolean getRdbtnStartWebappSelected() {
		return rdbtnStartWebapp.isSelected();
	}
	public void setRdbtnStartWebappSelected(boolean selected) {
		rdbtnStartWebapp.setSelected(selected);
	}
}
