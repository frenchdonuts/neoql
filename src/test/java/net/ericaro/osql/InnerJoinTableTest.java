package net.ericaro.osql;

import org.junit.Test;


public class InnerJoinTableTest {

	public static class EntityA{
		public static final Column<EntityA, String> CODE = new Column<EntityA, String>("code");
		public static final Column<EntityA, String> NAME = new Column<EntityA, String>("name");
		
		String code;
		String name;
		public String getCode() {
			return code;
		}
		public String getName() {
			return name;
		}
		@Override
		public String toString() {
			return "EntityA [code=" + code + ", name=" + name + "]";
		}
		
		
	}
	public static class EntityB{
		public static final Column<EntityB, String> CODE = new Column<EntityB, String>("code");
		public static final Column<EntityB, String> NAME = new Column<EntityB, String>("name");
		
		String code;
		String name;
		public String getCode() {
			return code;
		}
		public String getName() {
			return name;
		}
		@Override
		public String toString() {
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
		
		Table<EntityA> left  = db.tables.get(EntityA.class);
		Table<EntityB> right = db.tables.get(EntityB.class);
		
		Predicate<Pair<EntityA, EntityB>> where = new Predicate<Pair<EntityA,EntityB> >(){

			@Override
			public boolean eval(Pair<EntityA, EntityB> t) {
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
		s.update(EntityA.class).set(EntityA.CODE, "alpha2").where(DQL.columnIs(EntityA.CODE, "alpha"));
		s.executeOn(db);
		
		System.out.println("ENITY A UPDATED ##################");
		for(EntityA a: left)
			System.out.println(a);
		System.out.println(" JOIN UPDATED ##################");
		for (Pair<EntityA, EntityB> p: t)
			System.out.println(p);
		
	}
	
}
