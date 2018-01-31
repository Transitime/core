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
import javax.swing.border.EmptyBorder;

import org.transitclock.quickstart.resource.FileBrowser;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.Button;
import java.awt.SystemColor;
import java.io.File;

import javax.swing.JProgressBar;
import javax.swing.JTextPane;
import javax.swing.UIManager;

/**
 * This is the First and main gui element of the gui, all values needed for the
 * transitimeQuickStart are entered in this panel install jswing should allow
 * you to see and edit the gui.
 * 
 * @author Brendan Egan
 *
 */
public class InputPanel extends JFrame {
	/**
	 * @parem filelocation where the gtfs is located when selected by the user,
	 *        will use a default if nothing entered
	 * @parem realtimefeedURL the realtimefeed entered by the user, will use a
	 *        default if nothing entered
	 * @parem loglocation where the logs will be outputted to, entered by the
	 *        user, will use a default if nothing entered, default is currently
	 *        the directory the application was called
	 */

	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private String filelocation = null;
	private String realtimefeedURL = null;
	private String loglocation = null;

	FileBrowser browse = new FileBrowser();
	private JRadioButton rdbtnStartWebapp;

	/**
	 * Launch the application.
	 */
	public void InputPanelstart() {
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
		setBounds(100, 100, 500, 475);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblWelcomeToThe = new JLabel("Enter the locations of each object in the fields below:");
		lblWelcomeToThe.setBounds(17, 18, 442, 23);
		lblWelcomeToThe.setFont(new Font("Arial", Font.PLAIN, 19));

		JLabel lblGtfsFileLocation = new JLabel("GTFS file location:");
		lblGtfsFileLocation.setBounds(17, 85, 223, 23);
		lblGtfsFileLocation.setFont(new Font("Arial", Font.PLAIN, 19));

		textField = new JTextField();
		textField.setBounds(17, 122, 331, 22);

		textField.setColumns(10);

		Button button_2 = new Button("i");
		button_2.setBounds(442, 122, 17, 25);
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				InformationPanel infopanel = new InformationPanel();
				infopanel.InformationPanelstart();
			}
		});
		button_2.setBackground(SystemColor.textHighlight);

		JButton btnNewButton = new JButton("Browse");
		btnNewButton.setBounds(353, 122, 83, 25);
		btnNewButton.setBackground(SystemColor.menu);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/**
				 * creates a file browse on selection of the browse button
				 */
				JFileChooser fc = new JFileChooser();

				int returnVal = fc.showOpenDialog(browse);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					/**
					 * @parem file will be the chosen file selected by the file
					 *        browser
					 */
					File file = fc.getSelectedFile();
					filelocation = file.getPath();
					textField.setText(filelocation);
				}

			}
		});

		JLabel lblGtfsrealtimeFeedLocationurl = new JLabel("GTFS-realtime feed location/URL:");
		lblGtfsrealtimeFeedLocationurl.setBounds(17, 163, 309, 23);
		lblGtfsrealtimeFeedLocationurl.setFont(new Font("Arial", Font.PLAIN, 19));

		textField_1 = new JTextField();
		textField_1.setBounds(17, 194, 419, 22);
		textField_1.setColumns(10);

		Button button_1 = new Button("i");
		button_1.setBounds(442, 194, 17, 24);
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// creates an information panel on selecting of the I buttons
				InformationPanel infopanel = new InformationPanel();
				infopanel.InformationPanelstart();

			}
		});
		button_1.setBackground(SystemColor.textHighlight);

		JLabel lblInstallLocation = new JLabel("Log location:");
		lblInstallLocation.setBounds(17, 227, 309, 23);
		lblInstallLocation.setFont(new Font("Arial", Font.PLAIN, 19));

		textField_2 = new JTextField();
		textField_2.setBounds(17, 256, 419, 22);
		textField_2.setColumns(10);

		Button button = new Button("i");
		button.setBounds(442, 256, 17, 24);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// creates an information panel on selecting of the I buttons
				InformationPanel infopanel = new InformationPanel();
				infopanel.InformationPanelstart();
			}
		});
		button.setBackground(SystemColor.textHighlight);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(17, 391, 223, 23);
		btnCancel.setBackground(SystemColor.menu);
		btnCancel.setFont(new Font("Arial", Font.PLAIN, 13));
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
				dispose();
			}
		});

		JButton btnNext = new JButton("Next");
		btnNext.setBounds(247, 391, 223, 23);
		btnNext.setBackground(SystemColor.menu);
		btnNext.setFont(new Font("Arial", Font.PLAIN, 13));
		btnNext.addActionListener(new ActionListener() {
			/**
			 * calls the methods of the transitimeQuickStart
			 */
			public void actionPerformed(ActionEvent e) {
				/**
				 * calls the methods of the transitimeQuickStart
				 */
				setBounds(100, 100, 500, 603);
				JProgressBar progressBar = new JProgressBar();
				progressBar.setBounds(17, 497, 453, 31);
				contentPane.add(progressBar);
				JTextPane textPane = new JTextPane();
				textPane.setBackground(UIManager.getColor("Button.highlight"));
				textPane.setFont(new Font("Arial", Font.PLAIN, 16));
				textPane.setBounds(17, 427, 453, 57);
				contentPane.add(textPane);
				String apikeystring = null;
				// reads in URL
				loglocation = textField_2.getText();
				realtimefeedURL = textField_1.getText();
				if (loglocation.equals("")) {
					// uses current directory if one none specified by user.
					loglocation = System.getProperty("user.dir");
				}
				System.getProperties().setProperty("transitclock.logging.dir", loglocation);
				// Creates a thread which calls all the methods of the transitimeQuickStart

				TransitimeQuickStartThread quickstartthread = new TransitimeQuickStartThread();
				quickstartthread.filelocation = filelocation;
				quickstartthread.realtimefeedURL = realtimefeedURL;
				quickstartthread.loglocation = loglocation;
				quickstartthread.startwebapp = getRdbtnStartWebappSelected();
				quickstartthread.apikeystring = apikeystring;
				quickstartthread.progressBar=progressBar;
				quickstartthread.progresstextPane=textPane;
			
				Thread t = new Thread(quickstartthread);
				t.start();
				
			}
			
		});
	
		btnNext.setVerticalAlignment(SwingConstants.BOTTOM);
		rdbtnStartWebapp = new JRadioButton("Start webapp");
		rdbtnStartWebapp.setBounds(17, 309, 150, 57);
		rdbtnStartWebapp.setFont(new Font("Arial", Font.PLAIN, 16));
		contentPane.setLayout(null);
		contentPane.add(lblGtfsFileLocation);
		contentPane.add(button_2);
		contentPane.add(lblGtfsrealtimeFeedLocationurl);
		contentPane.add(button_1);
		contentPane.add(lblWelcomeToThe);
		contentPane.add(textField_2);
		contentPane.add(lblInstallLocation);
		contentPane.add(button);
		contentPane.add(rdbtnStartWebapp);
		contentPane.add(textField);
		contentPane.add(btnNewButton);
		contentPane.add(textField_1);
		contentPane.add(btnCancel);
		contentPane.add(btnNext);
		
		
		
	}

	public boolean getRdbtnStartWebappSelected() {
		return rdbtnStartWebapp.isSelected();
	}

	public void setRdbtnStartWebappSelected(boolean selected) {
		rdbtnStartWebapp.setSelected(selected);
	}
}
