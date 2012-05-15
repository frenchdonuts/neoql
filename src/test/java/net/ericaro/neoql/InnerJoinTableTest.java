package net.ericaro.neoql;

import java.util.HashSet;
import java.util.Set;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Database;
import net.ericaro.neoql.InnerJoinTable;
import net.ericaro.neoql.Pair;
import net.ericaro.neoql.Predicate;
import net.ericaro.neoql.Script;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.TableData;
import net.ericaro.neoql.TableDef;

import org.junit.Test;


public  class InnerJoinTableTest {

	 public static class EntityA{
		public static final Column<EntityA, String> CODE = new Column<EntityA, String>("code");
		public static final Column<EntityA, String> NAME = new Column<EntityA, String>("name");
		
		String code;
		String name;
		 String getCode() {
			return code;
		}
		 String getName() {
			return name;
		}
		@Override
		public  String toString() {
			return "EntityA [code=" + code + ", name=" + name + "]";
		}
		
		
	}
	 public static class EntityB{
		 static final Column<EntityB, String> CODE = new Column<EntityB, String>("code");
		 static final Column<EntityB, String> NAME = new Column<EntityB, String>("name");
		
		String code;
		String name;
		 String getCode() {
			return code;
		}
		 String getName() {
			return name;
		}
		@Override
		public  String toString() {
			return "EntityB [code=" + code + ", name=" + name + "]";
		}
		
	}	
	@Test public void testSimple() {
		
		Database db = new Database();
		Script s = new Script();
		s.createTable(EntityA.class);
		s.createTable(EntityB.class);
		
		s.insertInto(EntityA.class).set(EntityA.CODE, "alpha").set(EntityA.NAME, "toto");
		s.insertInto(EntityA.class).set(EntityA.CODE, "beta").set(EntityA.NAME, "titi");
		s.insertInto(EntityA.class).set(EntityA.CODE, "gamma").set(EntityA.NAME, "tutu");
		
		s.insertInto(EntityB.class).set(EntityB.CODE, "alpha").set(EntityB.NAME, "btoto");
		s.insertInto(EntityB.class).set(EntityB.CODE, "beta").set( EntityB.NAME, "btiti");
		
		s.executeOn(db);
		
		Table<EntityA> left  = db.tableFor(EntityA.class);
		Table<EntityB> right = db.tableFor(EntityB.class);
		
		Predicate<Pair<EntityA, EntityB>> where = new Predicate<Pair<EntityA,EntityB> >(){

			@Override
			public  boolean eval(Pair<EntityA, EntityB> t) {
				return t.getLeft().getCode().equals(t.getRight().getCode());
			}
			
		};
		System.out.println("ENTITYA ##################");
		for(EntityA a: left)
			System.out.println(a);
		System.out.println("JOIN ##################");
		
		InnerJoinTable<EntityA, EntityB> t = new InnerJoinTable<EntityA, EntityB>(left, right, where);
		for (Pair<EntityA, EntityB> p: t)
			System.out.println(p);
		
		s= new Script();
		s.update(EntityA.class).set(EntityA.CODE, "alpha2").where(NeoQL.is(EntityA.CODE, "alpha"));
		s.executeOn(db);
		
		System.out.println("ENITY A UPDATED ##################");
		for(EntityA a: left)
			System.out.println(a);
		System.out.println(" JOIN UPDATED ##################");
		for (Pair<EntityA, EntityB> p: t)
			System.out.println(p);
		
	}
	
	
	
	 public static class Student {
		public static Column<Student, String>	NAME	= new Column<Student, String>("name");
		public static Column<Student, Student>	MATE	= new Column<Student, Student>("mate", Student.class);

		private String							name;
		private Student							mate;

		@Override
		public  String toString() {
			return "Student [name=" + name + (mate!=null?", mate=" + mate.name:"") + "]";
		}

	}

	 static class Model {

		Database								db;
		private Table<Pair<Student, Student>>	mates;
		private TableData<Student>				students;
		TableDef<Pair<Student, Student>> MATES = NeoQL.innerJoin(Student.class, Student.class, new Predicate<Pair<Student, Student>>() {

			@Override
			public  boolean eval(Pair<Student, Student> t) {
				return t.getLeft().mate == t.getRight();
			}
		});

		 Model() {
			super();
			db = new Database();
			db.execute(
			new Script() {
				{
					createTable(Student.class);
				}
			}); // init script

			students = db.tableFor(Student.class);
			// create accessible queries
			mates = db.tableFor(MATES);

		}

		 Table<Student> students() {
			return students;
		}

		 void editStudent(final String t, final String mate) {
			System.out.println("pairing student "+t+" with "+mate);
			db.execute( new Script() {
				{
					update(Student.class).set(Student.MATE, getStudent(mate)).where(NeoQL.is(Student.NAME, t));
					
				}
			});
		}
		
		 Student getStudent(final String t) {
			return db.select(Student.class, NeoQL.is(Student.NAME, t)).iterator().next();
		}

		 void addStudent(final String name) {
			db.execute( new Script() {
				{
					insertInto(Student.class).set(Student.NAME, name);
				}
			});
		}
	}

	@Test public	 void autojoin() {
		Model m = new Model();
		m.addStudent("Alphonse");
		m.addStudent("Gerard");
		m.addStudent("Antoine");
		m.addStudent("Martin");
		
		m.editStudent("Alphonse", "Gerard" );
		
		for(Pair<Student, Student>  p : m.mates)
			System.out.println(p);
		
		System.out.println("# closing the loop");
		m.editStudent("Gerard","Alphonse" );
		Set<Student> students = new HashSet<Student>();
		for(Pair<Student, Student>  p : m.db.tableFor(NeoQL.select(m.MATES)) )
			students.add(p.getLeft());
		// I trust the inner join algorithm to be correct if build from scratch, 
		assert students.size() == 2 : "wrong final pair size";
		
		// but I'm testing the incremental one 
		
		int count=0;
		for(Pair<Student, Student>  p : m.mates) {
			count++;
			assert students.contains(p.getLeft()) : p+" pair is missing from the golden set" ;
		}
		assert count == 2 : "wrong incremental inner join's size";
		
		
	}
	
}
