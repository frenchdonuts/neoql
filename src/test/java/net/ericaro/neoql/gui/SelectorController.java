/*
 * Created by JFormDesigner on Sat May 19 16:18:47 CEST 2012
 */

package net.ericaro.neoql.gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import net.ericaro.neoql.gui.SelectorModel.Person;

/**
 * @author eric
 */
public class SelectorController extends JFrame {
	// keep a reference to a model
	SelectorModel	model;

	public SelectorController() {
		initComponents();
	}


	public void start() {
		
		this.model = new SelectorModel();// creates the model
		listBottom.setModel(model.getAllPersons());
		rightList.setModel(model.getSelected());
		leftList.setModel(model.getUnSelected());
		setVisible(true);
	}


	/**
	 * Callbacks from the view to trigger a move right
	 * 
	 */
	public void onMoveRight() {
		for (Person p : getSelection(leftList))
			model.selectPerson(p, true);
	}

	/**
	 * Callbacks from the view to trigger a move left
	 * 
	 */
	public void onMoveLeft() {
		for (Person p : getSelection(rightList))
			model.selectPerson(p, false);
	}
	
	public static void main(String[] args) {
		new SelectorController().start();
	}
	
	// util to retrieve the selected items in a list but casted as Person, unfortunately until java 7 there is no
	// way to do 
	private Person[] getSelection(JList list) {
		Object[] selected = list.getSelectedValues();
		Person[] persons = new Person[selected.length];
		System.arraycopy(selected, 0, persons, 0, selected.length);
		return persons;
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY //GEN-BEGIN:initComponents
		label1 = new JLabel();
		button1 = new JButton();
		button2 = new JButton();
		label2 = new JLabel();
		scrollPane1 = new JScrollPane();
		leftList = new JList();
		scrollPane2 = new JScrollPane();
		rightList = new JList();
		label3 = new JLabel();
		scrollPane3 = new JScrollPane();
		listBottom = new JList();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0, 0, 12, 0, 0, 0};
		((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
		((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};
		((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 0.0, 1.0E-4};

		//---- label1 ----
		label1.setText("Selected");
		contentPane.add(label1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(6, 0, 0, 0), 0, 0));

		//---- button1 ----
		button1.setText("->");
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onMoveRight();
			}
		});
		contentPane.add(button1, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
			new Insets(0, 0, 0, 0), 0, 0));

		//---- button2 ----
		button2.setText("<-");
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onMoveLeft();
			}
		});
		contentPane.add(button2, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
			new Insets(0, 0, 0, 0), 0, 0));

		//---- label2 ----
		label2.setText("Unselected");
		contentPane.add(label2, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(6, 0, 0, 0), 0, 0));

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(leftList);
		}
		contentPane.add(scrollPane1, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//======== scrollPane2 ========
		{
			scrollPane2.setViewportView(rightList);
		}
		contentPane.add(scrollPane2, new GridBagConstraints(4, 1, 2, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//---- label3 ----
		label3.setText("Alls");
		contentPane.add(label3, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(6, 0, 0, 0), 0, 0));

		//======== scrollPane3 ========
		{
			scrollPane3.setViewportView(listBottom);
		}
		contentPane.add(scrollPane3, new GridBagConstraints(1, 3, 5, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		setSize(485, 410);
		setLocationRelativeTo(null);
		// JFormDesigner - End of component initialization //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY //GEN-BEGIN:variables
	private JLabel label1;
	private JButton button1;
	private JButton button2;
	private JLabel label2;
	private JScrollPane scrollPane1;
	private JList leftList;
	private JScrollPane scrollPane2;
	private JList rightList;
	private JLabel label3;
	private JScrollPane scrollPane3;
	private JList listBottom;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
