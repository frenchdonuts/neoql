/*
 * Created by JFormDesigner on Mon Jun 18 13:58:50 CEST 2012
 */

package net.ericaro.neoql.smarttree;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;

import net.ericaro.neoql.RandName;
import net.ericaro.neoql.smarttree.TreeTesterModel.Teacher;
import net.ericaro.neoql.smarttree.tree.JTreeNode;
import net.ericaro.neoql.smarttree.tree.NodeModel;
import org.jdesktop.beansbinding.*;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;

/**
 * @author User #1
 */
public class TreeTester extends JPanel {
	private TreeTesterModel	model;
	private NodeModel	nodeModel;
	
	public TreeTester() {
		initComponents();
		model = new TreeTesterModel();
		nodeModel = new NodeModel();
		treeNode1.setNodeModel(nodeModel);
		treeNode1.getSelectionModel().add
		
		availableStudents.setModel(model.getAvailableStudents());
		nodeModel.addNode(new JTeachersNode(model));
		nodeModel.addNode(new JStudentsNode(model));
	}
	
	private void onAddStudent(){
		System.out.println("adding student");
		model.addStudent(RandName.next());
	}
	
	protected void onAddTeacher() {
		model.addTeacher(RandName.next());
	}

	private void onTreeSelectionChanged() {
		Object selection = treeNode1.getLeadSelectionPath().getLastPathComponent();
		System.out.println(selection);
	}
	


	
//	protected void onLink() {
//		Teacher teacher = (Teacher) allTeachers.getSelectedValue();
//		for(Object student : allStudents.getSelectedValues() )
//			model.link((Student) student, teacher);
//	}
	
public static void main(String[] args) {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
			new TreeTester().frame1.setVisible(true);
		}
	});
}

	
	private void initComponents() {
		
		
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		frame1 = new JFrame();
		toolBar1 = new JToolBar();
		button1 = new JButton();
		button2 = new JButton();
		scrollPane1 = new JScrollPane();
		treeNode1 = new JTreeNode();
		label1 = new JLabel();
		scrollPane2 = new JScrollPane();
		availableStudents = new JList();

		//======== frame1 ========
		{
			frame1.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			Container frame1ContentPane = frame1.getContentPane();
			frame1ContentPane.setLayout(new BorderLayout());

			//======== this ========
			{
				this.setLayout(new GridBagLayout());
				((GridBagLayout)getLayout()).columnWidths = new int[] {0, 5, 0, 0};
				((GridBagLayout)getLayout()).rowHeights = new int[] {0, 149, 5, 0, 5, 0, 0};
				((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

				//======== toolBar1 ========
				{

					//---- button1 ----
					button1.setText("Add Teacher");
					button1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							onAddTeacher();
						}
					});
					toolBar1.add(button1);

					//---- button2 ----
					button2.setText("Add Student");
					button2.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							onAddStudent();
						}
					});
					toolBar1.add(button2);
				}
				this.add(toolBar1, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));

				//======== scrollPane1 ========
				{

					//---- treeNode1 ----
					treeNode1.setShowsRootHandles(true);
					treeNode1.setRootVisible(false);
					treeNode1.addTreeSelectionListener(new TreeSelectionListener() {
						@Override
						public void valueChanged(TreeSelectionEvent e) {
							onTreeSelectionChanged();
						}
					});
					scrollPane1.setViewportView(treeNode1);
				}
				this.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));

				//---- label1 ----
				label1.setText("Available students");
				this.add(label1, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					new Insets(0, 0, 0, 0), 0, 0));

				//======== scrollPane2 ========
				{
					scrollPane2.setViewportView(availableStudents);
				}
				this.add(scrollPane2, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			frame1ContentPane.add(this, BorderLayout.CENTER);
			frame1.setSize(450, 390);
			frame1.setLocationRelativeTo(frame1.getOwner());
		}
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  / /GEN-BEGIN:variables
	private JFrame frame1;
	private JToolBar toolBar1;
	private JButton button1;
	private JButton button2;
	private JScrollPane scrollPane1;
	private JTreeNode treeNode1;
	private JLabel label1;
	private JScrollPane scrollPane2;
	private JList availableStudents;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
