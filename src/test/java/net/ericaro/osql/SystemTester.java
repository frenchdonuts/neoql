package net.ericaro.osql;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JSplitPane;
import javax.swing.Timer;

import net.ericaro.osql.SystemTester.Student;

public class SystemTester {

	public static class Student {
		public static Column<Student, String> NAME = new Column<Student, String>(
				"name");
		public static Column<Student, Boolean> SELECTED = new Column<Student, Boolean>(
				"selected");
		private String name;
		private boolean selected;

		@Override
		public String toString() {
			return "Student [name=" + name + ", selected=" + selected + "]";
		}

	}

	public static class Model {

		Database db;
		private Table<Student> selected;
		private Table<Student> unselected;

		public Model() {
			super();
			db = new Database();
			new Script() {
				{
					createTable(Student.class);

					executeOn(db);
				}
			}; // init script

			// create accessible queries
			selected = db.tableFor(DQL.select(Student.class,
					DQL.columnIs(Student.SELECTED, true)));
			unselected = db.tableFor(DQL.select(Student.class,
					DQL.columnIs(Student.SELECTED, false)));

		}

		public Iterable<Student> students() {
			return db.select(Student.class);
		}

		public void selectStudent(final Student t, final boolean selected) {
			new Script() {
				{
					update(Student.class).set(Student.SELECTED, selected)
							.where(DQL.columnIs(Student.NAME, t.name));
					executeOn(db);
				}
			};
		}

		public void addStudent(final String name, final boolean selected) {
			new Script() {
				{
					insertInto(Student.class).set(Student.NAME, name).set(
							Student.SELECTED, selected);
					executeOn(db);
				}
			};
		}
	}

	public static void main(String[] args) {
		final Model m = new Model();

		JFrame jf = new JFrame("tester");
		JList<Student> left = new JList<Student>();
		JList<Student> right = new JList<Student>();

		// uses the observable queries selected, and unselected.
		// uses a wrapper to listmodel to put it in a list model
		left.setModel(new TableList<Student>(m.selected)); 
		right.setModel(new TableList<Student>(m.unselected));

		jf.getContentPane().add(
				new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right));
		// there are wrapper around the table observable to a listmodel

		jf.setBounds(400, 300, 800, 600);
		// thread to do something: fill the list, and then every 2 s toggle a student selected status
		new Thread() {

			@Override
			public void run() {

				try {
					sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// fill the database
				m.addStudent("Alphonse", true);
				m.addStudent("Gerard", false);
				m.addStudent("Antoine", false);
				m.addStudent("Martin", true);

				while (true) {

					for (Student t : m.students()) {
						System.out.println("toogling state for " + t);
						m.selectStudent(t, !t.selected);
						try {
							sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				}
			}

		}.start();

		jf.setVisible(true);

	}
}
