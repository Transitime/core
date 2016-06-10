package org.transitime.gui;

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
import java.awt.event.InputMethodListener;
import java.awt.event.InputMethodEvent;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class InputPanel extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;

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
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileBrowser browse = new FileBrowser();
				browse.fileBrowser();
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
		
		JLabel lblInstallLocation = new JLabel("Install location:");
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
		
		JButton btnHelp = new JButton("Help");
		
		JButton btnNext = new JButton("Next");
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OutputPanel windowinput = new OutputPanel();
				windowinput.OutputPanelstart();
				dispose();
			}
		});
		btnNext.setVerticalAlignment(SwingConstants.BOTTOM);
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(58)
							.addComponent(lblWelcomeToThe))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(lblGtfsFileLocation, GroupLayout.PREFERRED_SIZE, 223, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnNewButton)
							.addGap(18)
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, 235, GroupLayout.PREFERRED_SIZE)
							.addGap(5)
							.addComponent(button_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(lblGtfsrealtimeFeedLocationurl, GroupLayout.PREFERRED_SIZE, 309, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, 235, GroupLayout.PREFERRED_SIZE)
							.addGap(5)
							.addComponent(button_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(lblInstallLocation, GroupLayout.PREFERRED_SIZE, 309, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, 235, GroupLayout.PREFERRED_SIZE)
							.addGap(5)
							.addComponent(button, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(1)
							.addComponent(btnHelp, GroupLayout.PREFERRED_SIZE, 205, GroupLayout.PREFERRED_SIZE)
							.addGap(5)
							.addComponent(btnCancel, GroupLayout.PREFERRED_SIZE, 165, GroupLayout.PREFERRED_SIZE)
							.addGap(5)
							.addComponent(btnNext, GroupLayout.PREFERRED_SIZE, 197, GroupLayout.PREFERRED_SIZE)))
					.addGap(26))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(4)
					.addComponent(lblWelcomeToThe)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(53)
							.addComponent(lblGtfsFileLocation))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(53)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
								.addComponent(button_2, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
									.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(btnNewButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(51)
							.addComponent(lblGtfsrealtimeFeedLocationurl))
						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
							.addGroup(gl_contentPane.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(button_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
								.addGap(51)
								.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(21)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addComponent(button, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
									.addGap(16)
									.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(37)
							.addComponent(lblInstallLocation)))
					.addGap(126)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(btnHelp)
						.addComponent(btnCancel)
						.addComponent(btnNext)))
		);
		contentPane.setLayout(gl_contentPane);
	}

}
