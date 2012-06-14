package net.ericaro.neoql.tutorial2;

import javax.swing.ListModel;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.lang.ClassTableDef;
import net.ericaro.neoql.lang.NeoQL;
import net.ericaro.neoql.lang.Script;
import net.ericaro.neoql.system.Column;
import net.ericaro.neoql.system.TableDef;

public class RightLeftModel {

	
	public static class Student{
		
		public static ClassTableDef<Student> TABLE = NeoQL.table(Student.class);
		public static Column<Student, String> NAME = TABLE.addColumn("name");
		public static Column<Student, Boolean> IN = TABLE.addColumn("in");
		
		private String name;
		String getName() {
			return name;
		}
		boolean isIn() {
			return in;
		}
		private boolean in = true;
		@Override
		public String toString() {
			return name;
		}
	}

	
	private Database	database;
	private ListModel<Student>	ins;
	private ListModel<Student>	outs;
	private static TableDef<Student> INS = NeoQL.select(Student.TABLE, Student.IN.is(true));

	public RightLeftModel() {
		super();
		database = new Database();
		database.execute(new Script() {{
			createTable(Student.TABLE);
			// TODO also create the tables from the definition
			//createTable(INS);
		}});
		
		/// creates the relations
		ins  = database.listFor(INS	);
		outs = database.listFor(NeoQL.select(Student.TABLE, Student.IN.is(false))	);
	}

	ListModel<Student> getIns() {
		return ins;
	}

	ListModel<Student> getOuts() {
		return outs;
	}

	public void createStudent(final String name) {
		database.execute(new Script() {{
			insertInto(Student.TABLE).set(Student.NAME, name);
		}});
		
	}

	public void selectStudent(final Student student) {
		setSelected(student, true);
		
	}

	protected void setSelected(final Student student, final boolean value) {
		database.execute(new Script() {{
			update(Student.TABLE).where(Student.TABLE.is(student)).set(Student.IN, value);
		}});
	}

	public void deselectStudent(Student t) {
		setSelected(t,false);
	}
	
}
