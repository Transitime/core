package org.transitime.QuickStartGui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
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

public class InputPanel extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 571, 544);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JLabel lblWelcomeToThe = new JLabel("Enter the locations of each object in the fields below");
		lblWelcomeToThe.setFont(new Font("Arial", Font.PLAIN, 19));
		GridBagConstraints gbc_lblWelcomeToThe = new GridBagConstraints();
		gbc_lblWelcomeToThe.anchor = GridBagConstraints.NORTH;
		gbc_lblWelcomeToThe.gridheight = 2;
		gbc_lblWelcomeToThe.gridwidth = 24;
		gbc_lblWelcomeToThe.insets = new Insets(0, 0, 5, 0);
		gbc_lblWelcomeToThe.gridx = 0;
		gbc_lblWelcomeToThe.gridy = 3;
		contentPane.add(lblWelcomeToThe, gbc_lblWelcomeToThe);
		
		JLabel lblGtfsFileLocation = new JLabel("GTFS file location:");
		lblGtfsFileLocation.setFont(new Font("Arial", Font.PLAIN, 19));
		GridBagConstraints gbc_lblGtfsFileLocation = new GridBagConstraints();
		gbc_lblGtfsFileLocation.gridheight = 3;
		gbc_lblGtfsFileLocation.gridwidth = 11;
		gbc_lblGtfsFileLocation.insets = new Insets(0, 0, 5, 5);
		gbc_lblGtfsFileLocation.gridx = 0;
		gbc_lblGtfsFileLocation.gridy = 5;
		contentPane.add(lblGtfsFileLocation, gbc_lblGtfsFileLocation);
		
		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridheight = 3;
		gbc_textField.gridwidth = 9;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 13;
		gbc_textField.gridy = 5;
		contentPane.add(textField, gbc_textField);
		textField.setColumns(10);
		
		Button button_2 = new Button("i");
		button_2.setBackground(SystemColor.textHighlight);
		GridBagConstraints gbc_button_2 = new GridBagConstraints();
		gbc_button_2.gridheight = 2;
		gbc_button_2.insets = new Insets(0, 0, 5, 5);
		gbc_button_2.gridx = 22;
		gbc_button_2.gridy = 5;
		contentPane.add(button_2, gbc_button_2);
		
		JLabel lblGtfsrealtimeFeedLocationurl = new JLabel("GTFS-realtime feed location/URL:");
		lblGtfsrealtimeFeedLocationurl.setFont(new Font("Arial", Font.PLAIN, 19));
		GridBagConstraints gbc_lblGtfsrealtimeFeedLocationurl = new GridBagConstraints();
		gbc_lblGtfsrealtimeFeedLocationurl.gridheight = 3;
		gbc_lblGtfsrealtimeFeedLocationurl.gridwidth = 10;
		gbc_lblGtfsrealtimeFeedLocationurl.insets = new Insets(0, 0, 5, 5);
		gbc_lblGtfsrealtimeFeedLocationurl.gridx = 1;
		gbc_lblGtfsrealtimeFeedLocationurl.gridy = 9;
		contentPane.add(lblGtfsrealtimeFeedLocationurl, gbc_lblGtfsrealtimeFeedLocationurl);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.gridheight = 3;
		gbc_textField_1.gridwidth = 9;
		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 13;
		gbc_textField_1.gridy = 9;
		contentPane.add(textField_1, gbc_textField_1);
		
		Button button_1 = new Button("i");
		button_1.setBackground(SystemColor.textHighlight);
		GridBagConstraints gbc_button_1 = new GridBagConstraints();
		gbc_button_1.gridheight = 4;
		gbc_button_1.insets = new Insets(0, 0, 5, 5);
		gbc_button_1.gridx = 22;
		gbc_button_1.gridy = 8;
		contentPane.add(button_1, gbc_button_1);
		
		JLabel lblInstallLocation = new JLabel("Install location:");
		lblInstallLocation.setFont(new Font("Arial", Font.PLAIN, 19));
		GridBagConstraints gbc_lblInstallLocation = new GridBagConstraints();
		gbc_lblInstallLocation.gridwidth = 10;
		gbc_lblInstallLocation.gridheight = 3;
		gbc_lblInstallLocation.insets = new Insets(0, 0, 5, 5);
		gbc_lblInstallLocation.gridx = 1;
		gbc_lblInstallLocation.gridy = 13;
		contentPane.add(lblInstallLocation, gbc_lblInstallLocation);
		
		textField_2 = new JTextField();
		textField_2.setColumns(10);
		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
		gbc_textField_2.gridwidth = 9;
		gbc_textField_2.gridheight = 3;
		gbc_textField_2.insets = new Insets(0, 0, 5, 5);
		gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_2.gridx = 13;
		gbc_textField_2.gridy = 13;
		contentPane.add(textField_2, gbc_textField_2);
		
		Button button = new Button("i");
		button.setBackground(SystemColor.textHighlight);
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.gridheight = 4;
		gbc_button.insets = new Insets(0, 0, 5, 5);
		gbc_button.gridx = 22;
		gbc_button.gridy = 12;
		contentPane.add(button, gbc_button);
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.SOUTH;
		gbc_btnCancel.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCancel.gridheight = 4;
		gbc_btnCancel.gridwidth = 5;
		gbc_btnCancel.insets = new Insets(0, 0, 5, 5);
		gbc_btnCancel.gridx = 10;
		gbc_btnCancel.gridy = 19;
		contentPane.add(btnCancel, gbc_btnCancel);
		
		JButton btnHelp = new JButton("Help");
		GridBagConstraints gbc_btnHelp = new GridBagConstraints();
		gbc_btnHelp.anchor = GridBagConstraints.SOUTH;
		gbc_btnHelp.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnHelp.gridheight = 3;
		gbc_btnHelp.gridwidth = 8;
		gbc_btnHelp.insets = new Insets(0, 0, 5, 5);
		gbc_btnHelp.gridx = 2;
		gbc_btnHelp.gridy = 20;
		contentPane.add(btnHelp, gbc_btnHelp);
		
		JButton btnNext = new JButton("Next");
		btnNext.setVerticalAlignment(SwingConstants.BOTTOM);
		GridBagConstraints gbc_btnNext = new GridBagConstraints();
		gbc_btnNext.anchor = GridBagConstraints.SOUTH;
		gbc_btnNext.insets = new Insets(0, 0, 5, 0);
		gbc_btnNext.gridheight = 4;
		gbc_btnNext.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNext.gridwidth = 8;
		gbc_btnNext.gridx = 15;
		gbc_btnNext.gridy = 19;
		contentPane.add(btnNext, gbc_btnNext);
	}

}
