package net.ericaro.neoql.lang;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JSplitPane;
import javax.swing.ListModel;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.TableList;

 class SelectTester {

	 static class Student {
		 static Column<Student, String> NAME = new Column<Student, String>(
				"name");
		 static Column<Student, Boolean> SELECTED = new Column<Student, Boolean>(
				"selected");
		
		private String name;
		private boolean selected;

		@Override
		public  String toString() {
			return "Student [name=" + name + ", selected=" + selected + "]";
		}

	}

	 static class Model {

		Database db;
		private TableList<Student> selected;
		private TableList<Student> unselected;

		 Model() {
			super();
			db = new Database();
			new Script() {
				{
					createTable(Student.class);

					executeOn(db);
				}
			}; // init script

			// create accessible queries
			selected = db.listFor(NeoQL.select(Student.class,
					NeoQL.is(Student.SELECTED, true)));
			unselected = db.listFor(NeoQL.select(Student.class,
					NeoQL.is(Student.SELECTED, false)));

		}

		 Iterable<Student> students() {
			return db.select(Student.class);
		}

		 void selectStudent(final Student t, final boolean selected) {
			new Script() {
				{
					update(Student.class).set(Student.SELECTED, selected)
							.where(NeoQL.is(Student.NAME, t.name));
					executeOn(db);
				}
			};
		}

		 void addStudent(final String name, final boolean selected) {
			new Script() {
				{
					insertInto(Student.class).set(Student.NAME, name).set(
							Student.SELECTED, selected);
					executeOn(db);
				}
			};
		}
	}

	 static void main(String[] args) {
		final Model m = new Model();

		JFrame jf = new JFrame("tester");
		JList left = new JList();
		JList right = new JList();

		// uses the observable queries selected, and unselected.
		// uses a wrapper to listmodel to put it in a list model
		left.setModel(m.selected); 
		right.setModel(m.unselected);

		jf.getContentPane().add(
				new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right));
		// there are wrapper around the table observable to a listmodel

		jf.setBounds(400, 300, 800, 600);
		// thread to do something: fill the list, and then every 2 s toggle a student selected status
		new Thread() {

			@Override
			public  void run() {

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
