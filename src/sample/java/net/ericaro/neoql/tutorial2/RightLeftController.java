package net.ericaro.neoql.tutorial2;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import net.ericaro.neoql.RandName;
import net.ericaro.neoql.tutorial2.RightLeftModel.Student;

public class RightLeftController extends JFrame{
	
	RightLeftModel model;
	private JList outs;
	private JList ins;
	
	public RightLeftController() {
		setTitle("Demo for Right Left");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(640,480);
		setLocationByPlatform(true);
		JToolBar toolBar = new JToolBar();
		getContentPane().add(toolBar, BorderLayout.NORTH);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onAdd();
			}

		});
		toolBar.add(btnAdd);
		
		JButton btnSelect = new JButton("Select");
		btnSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			onSelect();
			}
		});
		toolBar.add(btnSelect);
		
		JButton btnUnselect = new JButton("Unselect");
		btnUnselect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			onDeselect();
			}
		});
		toolBar.add(btnUnselect);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		
		ins = new JList();
		scrollPane.setViewportView(ins);
		
		JLabel lblSelected = new JLabel("Selected");
		scrollPane.setColumnHeaderView(lblSelected);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		splitPane.setRightComponent(scrollPane_1);
		
		outs = new JList();
		scrollPane_1.setViewportView(outs);
		
		JLabel lblUnselected = new JLabel("Unselected");
		scrollPane_1.setColumnHeaderView(lblUnselected);
		
	}

	private void onAdd() {
		this.model.createStudent(RandName.next());
		
	}
	
	private void onSelect() {
		for(Object t: outs.getSelectedValues())
			this.model.selectStudent((Student)t);
		
	}
	
	private void onDeselect() {
		for(Object t: ins.getSelectedValues())
			this.model.deselectStudent((Student)t);
	}
	
	public void setModel(RightLeftModel model) {
		this.model = model;
		this.ins.setModel(model.getIns());
		this.outs.setModel(model.getOuts());
		
	}
	
	public static void main(String[] args) {
		RightLeftController c = new RightLeftController();
		c.setModel(new RightLeftModel());
		c.setVisible(true);
	}
	
	
}
