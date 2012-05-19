package net.ericaro.neoql;

 import java.util.HashSet;
import java.util.Set;

import static net.ericaro.neoql.EntityModel.*;

import net.ericaro.neoql.StudentModel.Binome;

import org.junit.Test;


public  class InnerJoinTableTest {

	
	@Test public void testSimple() {
		
		Database db = new Database();
		
		db.execute(
		new Script() {{
		createTable(ENTITYA);
		createTable(ENTITYB);
		
		insertInto(ENTITYA).set(EntityA.CODE, "alpha").set(EntityA.NAME, "toto");
		insertInto(ENTITYA).set(EntityA.CODE, "beta").set(EntityA.NAME, "titi");
		insertInto(ENTITYA).set(EntityA.CODE, "gamma").set(EntityA.NAME, "tutu");
		
		insertInto(ENTITYB).set(EntityB.CODE, "alpha").set(EntityB.NAME, "btoto");
		insertInto(ENTITYB).set(EntityB.CODE, "beta").set( EntityB.NAME, "btiti");
		}});
		
		Table<EntityA> left  = db.tableFor(ENTITYA);
		Table<EntityB> right = db.tableFor(ENTITYB);
		
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
		
		db.execute(new Script() {{
			update(ENTITYA).set(EntityA.CODE, "alpha2").where(NeoQL.is(EntityA.CODE, "alpha"));
		}});
		
		System.out.println("ENITY A UPDATED ##################");
		for(EntityA a: left)
			System.out.println(a);
		System.out.println(" JOIN UPDATED ##################");
		for (Pair<EntityA, EntityB> p: t)
			System.out.println(p);
		
	}
	
	
	
	

	 static class Model {

		Database								db;
		private Table<Pair<Binome, Binome>>	mates;
		private TableData<Binome>				students;
		TableDef<Pair<Binome, Binome>> MATES = NeoQL.innerJoin(Binome.TABLE, Binome.TABLE, new Predicate<Pair<Binome, Binome>>() {

			@Override
			public  boolean eval(Pair<Binome, Binome> t) {
				return t.getLeft().mate == t.getRight();
			}
		});

		 Model() {
			super();
			db = new Database();
			db.execute(
			new Script() {
				{
					createTable(Binome.TABLE);
				}
			}); // init script

			students = db.tableFor(Binome.TABLE);
			// create accessible queries
			mates = db.tableFor(MATES);

		}

		 Table<Binome> students() {
			return students;
		}

		 void editStudent(final String t, final String mate) {
			System.out.println("pairing student "+t+" with "+mate);
			db.execute( new Script() {
				{
					update(Binome.TABLE).set(Binome.MATE, getStudent(mate)).where(Binome.NAME.is( t));
					
				}
			});
		}
		
		 Binome getStudent(final String t) {
			return db.iterator(NeoQL.select(Binome.TABLE, Binome.NAME.is(t))).next();
		}

		 void addStudent(final String name) {
			db.execute( new Script() {
				{
					insertInto(Binome.TABLE).set(Binome.NAME, name);
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
		
		for(Pair<Binome, Binome>  p : m.mates)
			System.out.println(p);
		
		System.out.println("# closing the loop");
		m.editStudent("Gerard","Alphonse" );
		Set<Binome> students = new HashSet<Binome>();
		for(Pair<Binome, Binome>  p : m.db.tableFor(NeoQL.select(m.MATES)) )
			students.add(p.getLeft());
		// I trust the inner join algorithm to be correct if build from scratch, 
		assert students.size() == 2 : "wrong final pair size";
		
		// but I'm testing the incremental one 
		
		int count=0;
		for(Pair<Binome, Binome>  p : m.mates) {
			count++;
			assert students.contains(p.getLeft()) : p+" pair is missing from the golden set" ;
		}
		assert count == 2 : "wrong incremental inner join's size";
		
		
	}
	
}
