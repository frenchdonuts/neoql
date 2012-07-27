package net.ericaro.neoql;

import static org.junit.Assert.*;

import net.ericaro.neoql.ContentTableTest.Tester;

import org.junit.Test;

public class CursorTest {

	public static Column<Tester, String> NAME = NeoQL.column(Tester.class, "name", String.class, false);
	public static Column<Tester, Integer> COUNT= NeoQL.column(Tester.class, "count", Integer.class, false);
	
	
	public static class Tester{
		private String name ;
		private int count = 2;
		@Override
		public String toString() {
			return "Tester [name=" + name + ", count=" + count + "]";
		}
	}
	
	@Test
	public void testBasic() {
		
		Database db = new Database();
		ContentTable<Tester> t = db.createTable(Tester.class, NAME, COUNT);
		Cursor<Tester> c =  db.createCursor(t);
		
		db.insert(t, NAME.set("1"), COUNT.set(0) );
		Tester v = db.insert(t, NAME.set("2"), COUNT.set(1) );
		db.moveTo(c, v);
		db.commit();
		
		assert c.get() == v : "cursor wrongly initialized";
		assert c.get().count == 1: "cursor points to a wrong value";
		
		v = NeoQL.select(t, COUNT.is(0)).iterator().next();
		db.moveTo(c, v);
		db.commit();
		
		assert c.get().count == 0 : "cursor didn't move" ;
		
	}

}
