package net.ericaro.neoql.tutorial;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.JToolBar;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import net.ericaro.neoql.RandName;
import net.ericaro.neoql.tutorial.TutorialModel.Student;
import net.ericaro.neoql.tutorial.TutorialModel.Teacher;

public class TutorialController {

	private JFrame frame;
	private TutorialModel model;
	private JList selectedStudents;
	private JList allTeachers;
	private JList allStudents;
	private JList selectedTeachers;

	//Controller section
	
	private void onTeacherSelectionChanged() {
		List selected = Arrays.asList(allTeachers.getSelectedValues());
		for(Teacher t: model.teachers())
				model.selectTeacher(t, selected.contains(t) );
	}
	
	private void onAddStudent(){
		model.addStudent(RandName.next());
	}
	
	protected void onAddTeacher() {
		model.addTeacher(RandName.next());
	}
	
	protected void onLink() {
		Teacher teacher = (Teacher) allTeachers.getSelectedValue();
		for(Object student : allStudents.getSelectedValues() )
			model.link((Student) student, teacher);
	}
	
	public void setModel(TutorialModel model) {
		this.model = model;
		
		allStudents.setModel(model.getStudents());
		allTeachers.setModel(model.getTeachers());
		selectedTeachers.setModel(model.getSelectedTeachers());
		selectedStudents.setModel(model.getSelectedStudents());
		
		
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TutorialController window = new TutorialController();
					window.setModel(new TutorialModel());
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public TutorialController() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 661, 475);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JToolBar toolBar = new JToolBar();
		frame.getContentPane().add(toolBar, BorderLayout.NORTH);
		
		JButton btnNewButton = new JButton("Add Student");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddStudent();
			}
		});
		toolBar.add(btnNewButton);
		
		JButton btnLinkSelected = new JButton("Link Selected");
		btnLinkSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onLink();
			}
		});
		toolBar.add(btnLinkSelected);
		
		JButton btnAddTeacher = new JButton("Add Teacher");
		btnAddTeacher.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddTeacher();
			}
		});
		toolBar.add(btnAddTeacher);
		
		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		frame.getContentPane().add(splitPane_1, BorderLayout.CENTER);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane_1.setLeftComponent(splitPane);
		
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		
		allTeachers = new JList();
		allTeachers.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				onTeacherSelectionChanged();	
			}
		});
		scrollPane.setViewportView(allTeachers);
		
		JLabel lblStudents = new JLabel("All Teachers");
		lblStudents.setOpaque(true);
		lblStudents.setBackground(Color.WHITE);
		scrollPane.setColumnHeaderView(lblStudents);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		splitPane.setRightComponent(scrollPane_1);
		
		allStudents = new JList();
		scrollPane_1.setViewportView(allStudents);
		
		JLabel lblTeachers = new JLabel("All Students");
		lblTeachers.setOpaque(true);
		lblTeachers.setBackground(Color.WHITE);
		scrollPane_1.setColumnHeaderView(lblTeachers);
		
		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_1.setRightComponent(splitPane_2);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		splitPane_2.setLeftComponent(scrollPane_2);
		
		selectedTeachers = new JList();
		scrollPane_2.setViewportView(selectedTeachers);
		
		JLabel lblNewLabel = new JLabel("Selected Teachers");
		scrollPane_2.setColumnHeaderView(lblNewLabel);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		splitPane_2.setRightComponent(scrollPane_3);
		
		selectedStudents = new JList();
		scrollPane_3.setViewportView(selectedStudents);
		
		JLabel lblNewLabel_1 = new JLabel("Their Students");
		scrollPane_3.setColumnHeaderView(lblNewLabel_1);
	}


	
}
