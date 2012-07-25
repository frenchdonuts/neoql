/*
 * Created by JFormDesigner on Mon Jun 18 13:58:50 CEST 2012
 */

package net.ericaro.neoql.smarttree;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import net.ericaro.neoql.RandName;
import net.ericaro.neoql.eventsupport.PropertyListener;
import net.ericaro.neoql.smarttree.TreeTesterModel.Student;
import net.ericaro.neoql.smarttree.TreeTesterModel.Teacher;
import net.ericaro.neoql.smarttree.tree.JNode;
import net.ericaro.neoql.smarttree.tree.JTreeNode;
import net.ericaro.neoql.smarttree.tree.NodeModel;

/**
 * @author User #1
 */
public class TreeTester extends JPanel {
	private TreeTesterModel	model;
	JTextField textfield1 ;
	
	public TreeTester() {
		initComponents();
		model = new TreeTesterModel();
		model.getEditingTeacher().addPropertyListener(new PropertyListener<TreeTesterModel.Teacher>() {
			public void updated(Teacher oldValue, Teacher newValue) {
				System.out.println(oldValue+ "->"+ newValue);
			}
		});
		
		availableStudents.setModel(model.getAvailableStudents());
		model.getEditingTeacherName().addPropertyListener( new PropertyListener<String>() {
			public void updated(String oldValue, String newValue) { whenTeacherNameChanged();	}});
		
		treeNode1.addNode(new JTeachersNode(model));
		treeNode1.addNode(new JStudentsNode(model));
	}
	
	private void onAddStudent(){
		System.out.println("adding student");
		model.addStudent(RandName.next());
	}
	
	public void onValidateName() { // callback to validate the text field
		String value = textfield1.getText() ;
		whenTeacherNameChanged(); // act like if the name add changed (i.e reset the correct name)
		model.renameEditingTeacher(value);
		
	}
	
	public void whenTeacherNameChanged() {
		textfield1.setText( model.getEditingTeacherName().get() );
	}
	
	protected void onAddTeacher() {
		model.addTeacher(RandName.next());
	}

	private void onTreeSelectionChanged() {
		JNode node=(JNode) ( (DefaultMutableTreeNode) treeNode1.getLastSelectedPathComponent()).getUserObject();
		Object selection = node.getModel();
		if (selection instanceof Teacher)
			model.select((Teacher) selection);
		
	}

	
	private void onRename() {
		JNode node=(JNode) ( (DefaultMutableTreeNode) treeNode1.getLastSelectedPathComponent()).getUserObject();
		Object selection = node.getModel();
		if (selection instanceof Teacher)
			model.rename((Teacher) selection, RandName.next());
		if (selection instanceof Student)
			model.rename((Student) selection, RandName.next());
		
		
	}
	protected void onLink() {
		JNode node=(JNode) ( (DefaultMutableTreeNode) treeNode1.getLastSelectedPathComponent()).getUserObject();
		Object selection = node.getModel();
		if (selection instanceof Teacher)
			onLink((Teacher) selection, asStudents( availableStudents.getSelectedValues() ) );
	}
	
	private Student[] asStudents(Object[] selection) {
		Student[] s = new Student[selection.length];
		System.arraycopy(selection, 0, s, 0, selection.length);
		return s;
	}
	
	

	

	
private void onLink(Teacher teacher, Student[] selectedValuesList) {
	for(Student student : selectedValuesList)
		model.link(student, teacher);		
	}

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
		button3 = new JButton();
		button4 = new JButton();
		textfield1 = new JTextField();
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

					//---- button3 ----
					button3.setText("link");
					button3.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							onLink();
						}
					});
					toolBar1.add(button3);
					
					textfield1.addFocusListener(new FocusAdapter() {
						public void focusLost(FocusEvent e) {
							onValidateName() ;
						}});
					textfield1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							onValidateName();
						}
					});
					toolBar1.add(textfield1);
					
					//---- button4 ----
					button4.setText("Rename");
					button4.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							onRename();
						}
					});
					toolBar1.add(button4);
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
	private JButton button3;
	private JButton button4;
	private JScrollPane scrollPane1;
	private JTreeNode treeNode1;
	private JLabel label1;
	private JScrollPane scrollPane2;
	private JList availableStudents;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
