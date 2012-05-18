package net.ericaro.outside;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JSplitPane;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.Database;
import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Pair;
import net.ericaro.neoql.Predicate;
import net.ericaro.neoql.Script;
import net.ericaro.neoql.TableDef;
import net.ericaro.neoql.TableList;

public class MateTester {

	public static class Student {
		public static Column<Student, String>	NAME	= new Column<Student, String>("name");
		public static Column<Student, Student>	MATE	= new Column<Student, Student>("mate", Student.class);

		private String					name;
		private Student					mate;

		@Override
		public String toString() {
			return "Student [name=" + name + (mate != null ? ", mate=" + mate.name : "") + "]";
		}

	}

	
	static class Model {

		public static TableDef<Pair<Student, Student>>			MATES	= NeoQL.innerJoin(Student.class, Student.class, new Predicate<Pair<Student, Student>>() {

														@Override
														public boolean eval(Pair<Student, Student> t) {
															return t.getLeft().mate == t.getRight();
														}
													});
		public static TableDef<String>			NAMES = NeoQL.select(Student.NAME, Student.class);		
		

		Database							db;
		TableList<Pair<Student, Student>>	mates;
		TableList<Student>					allStudents;

		TableList<String> allStudentNames;

		Model() {
			super();
			db = new Database();

			db.execute(new Script() {
				{
					createTable(Student.class);
				}
			}); // init script

		}
		
		
		public TableList<Student> getAllStudents() {
			if (allStudents == null)
				allStudents = db.listFor(Student.class);	
			return allStudents;
		}
		public TableList<String> getAllStudentNames() {
			if (allStudentNames == null)
				allStudentNames = db.listFor(NAMES);	
			return allStudentNames;
		}
		
		public TableList<Pair<Student,Student>> getMates() {
			if (mates== null)
				mates = db.listFor(MATES);	
			return mates;
		}

		void editStudent(final Student t, final Student mate) {
			System.out.println("pair student" + t + " with " + mate);
			db.execute(new Script() {
				{
					update(Student.class).set(Student.MATE, mate).where(NeoQL.is(Student.NAME, t.name));

				}
			});
		}

		void addStudent(final String name) {
			db.execute(new Script() {
				{
					insertInto(Student.class).set(Student.NAME, name);
				}
			});
		}


		public Iterable<Student> students() {
			return db.select(Student.class);
		}
	}

	public static void main(String[] args) {
		final Model m = new Model();

		JFrame jf = new JFrame("tester");
		JList left = new JList();
		JList right = new JList();

		// uses the observable queries selected, and unselected.
		// uses a wrapper to listmodel to put it in a list model
		left.setModel(m.getMates());
		right.setModel(m.getAllStudentNames() );

		jf.getContentPane().add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right));
		// there are wrapper around the table observable to a listmodel

		jf.setBounds(400, 300, 800, 600);
		// thread to do something: fill the list, and then every 2 s toggle a student selected status
		new Thread() {

			@Override
			public void run() {

				try {
					sleep(5000);
					// fill the database
					m.addStudent("Alphonse");
					sleep(1000);
					m.addStudent("Gerard");
					sleep(1000);
					m.addStudent("Antoine");
					sleep(1000);
					m.addStudent("Martin");
					sleep(1000);

					while (true) {

						for (Student t : m.students()) {
							for (Student t2 : m.students()) {
								if (t != t2)
									m.editStudent(t, t2);
								sleep(2000);
							}
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();

		jf.setVisible(true);
	}
}
