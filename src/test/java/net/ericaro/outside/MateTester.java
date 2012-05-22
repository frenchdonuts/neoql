package net.ericaro.outside;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JSplitPane;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.TableList;
import net.ericaro.neoql.StudentModel.Binome;
import net.ericaro.neoql.lang.ClassTableDef;
import net.ericaro.neoql.lang.NeoQL;
import net.ericaro.neoql.lang.Script;
import net.ericaro.neoql.system.Pair;
import net.ericaro.neoql.system.Predicate;
import net.ericaro.neoql.system.TableDef;

public class MateTester {

	
	static class Model {
		public static final ClassTableDef<Binome> BINOME = Binome.TABLE;
		public static TableDef<Pair<Binome, Binome>>			MATES	= NeoQL.innerJoin(BINOME, BINOME, new Predicate<Pair<Binome, Binome>>() {

														@Override
														public boolean eval(Pair<Binome, Binome> t) {
															return t.getLeft().getMate() == t.getRight();
														}
													});
		public static TableDef<String>			NAMES = NeoQL.select(Binome.NAME, BINOME);		
		

		Database							db;
		TableList<Pair<Binome, Binome>>	mates;
		TableList<Binome>					allStudents;

		TableList<String> allStudentNames;

		Model() {
			super();
			db = new Database();

			db.execute(new Script() {
				{
					createTable(BINOME);
				}
			}); // init script

		}
		
		
		public TableList<Binome> getAllStudents() {
			if (allStudents == null)
				allStudents = db.listFor(BINOME);	
			return allStudents;
		}
		public TableList<String> getAllStudentNames() {
			if (allStudentNames == null)
				allStudentNames = db.listFor(NAMES);	
			return allStudentNames;
		}
		
		public TableList<Pair<Binome,Binome>> getMates() {
			if (mates== null)
				mates = db.listFor(MATES);	
			return mates;
		}

		void editStudent(final Binome t, final Binome mate) {
			System.out.println("pair student" + t + " with " + mate);
			db.execute(new Script() {
				{
					update(BINOME).set(Binome.MATE, mate).where(Binome.NAME.is(t.getName()) );

				}
			});
		}

		void addStudent(final String name) {
			db.execute(new Script() {
				{
					insertInto(BINOME).set(Binome.NAME, name);
				}
			});
		}


		public Iterable<Binome> students() {
			return db.select(BINOME);
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

						for (Binome t : m.students()) {
							for (Binome t2 : m.students()) {
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
