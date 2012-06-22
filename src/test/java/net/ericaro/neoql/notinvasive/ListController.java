/*
 * Created by JFormDesigner on Thu Jun 21 08:45:51 CEST 2012
 */

package net.ericaro.neoql.notinvasive;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import net.ericaro.neoql.RandName;

/**
 * @author User #1
 */
public class ListController extends JFrame {
	
	StuffListModel model;
	
	public ListController() {
		initComponents();
		setModel(new StuffListModel());
	}
	public StuffListModel getModel() {
		return model;
	}
	
	public void setModel(StuffListModel model) {
		this.model = model;
		list1.setSelectionModel(model.getListSelectionModel());
		list1.setModel( this.model.getListModel() );
	}

	private void onExit() {
		System.exit(0);
	}

	private void onAdd() {
		model.addStuff(RandName.next());
	}

	private void onRemove() {
		model.removeStuff((Stuff) list1.getSelectedValue());
	}

	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new ListController().setVisible(true);
			}
		});
	}
	
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		scrollPane1 = new JScrollPane();
		list1 = new JList();
		toolBar1 = new JToolBar();
		button1 = new JButton();
		button2 = new JButton();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		//======== this ========
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new BorderLayout());

				//======== scrollPane1 ========
				{
					scrollPane1.setViewportView(list1);
				}
				contentPanel.add(scrollPane1, BorderLayout.CENTER);

				//======== toolBar1 ========
				{

					//---- button1 ----
					button1.setText("+");
					button1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							onAdd();
						}
					});
					toolBar1.add(button1);

					//---- button2 ----
					button2.setText("-");
					button2.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							onRemove();
						}
					});
					toolBar1.add(button2);
				}
				contentPanel.add(toolBar1, BorderLayout.SOUTH);
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
				((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

				//---- okButton ----
				okButton.setText("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						onExit();
					}
				});
				buttonBar.add(okButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						onExit();
					}
				});
				buttonBar.add(cancelButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		setSize(400, 300);
		setLocationRelativeTo(null);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JScrollPane scrollPane1;
	private JList list1;
	private JToolBar toolBar1;
	private JButton button1;
	private JButton button2;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
