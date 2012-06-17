package net.ericaro.neoql.tutorial2;

import javax.swing.ListModel;

import net.ericaro.neoql.ClassTableDef;
import net.ericaro.neoql.Column;
import net.ericaro.neoql.ColumnValue;
import net.ericaro.neoql.Database;
import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.TableData;
import net.ericaro.neoql.TableDef;

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
	private ListModel	ins;
	private ListModel   outs;

	public RightLeftModel() {
		super();
		database = new Database();
		TableData<Student> students = database.createTable(Student.TABLE);
		NeoQL.select(students, Student.IN.is(true));
		/// creates the relations
		ins  = NeoQL.listFor(students );
		outs = NeoQL.listFor(NeoQL.where(students, Student.IN.is(false))	);
	}

	ListModel getIns() {
		return ins;
	}

	ListModel getOuts() {
		return outs;
	}

	public void createStudent(String name) {
		database.insert(Student.NAME.set(name) );
	}

	public void selectStudent(final Student student) {
		setSelected(student, true);
		
	}

	protected void setSelected(final Student student, final boolean value) {
		database.update(student, Student.IN.set(value));
	}

	public void deselectStudent(Student t) {
		setSelected(t,false);
	}
	
}
