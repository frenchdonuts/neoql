package net.ericaro.neoql;

import net.ericaro.neoql.Git.Commit;

import org.junit.Test;

public class CursorTest {

	public static Column<Tester, String>	NAME	= NeoQL.column(Tester.class, "name", String.class, false);
	public static Column<Tester, Integer>	COUNT	= NeoQL.column(Tester.class, "count", Integer.class, false);

	public static class Tester {
		private String	name;
		private int		count	= 2;

		@Override
		public String toString() {
			return "Tester [name=" + name + ", count=" + count + "]";
		}
	}

	@Test
	public void testBasic() {

		Database db = new Database();
		
		Git git = new Git(db);
		ContentTable<Tester> t = db.createTable(Tester.class, NAME, COUNT);
		Cursor<Tester> c = db.createCursor(t);

		db.insert(t, NAME.set("1"), COUNT.set(0));
		Tester v = db.insert(t, NAME.set("2"), COUNT.set(1));
		db.moveTo(c, v);
		git.commit("first");
		Commit first = git.tag();
		

		assert c.get() == v : "cursor wrongly initialized";
		assert c.get().count == 1 : "cursor points to a wrong value";

		v = NeoQL.select(t, COUNT.is(0)).iterator().next();
		db.moveTo(c, v);
		git.commit("moving cursor");
		
		db.update(t, NeoQL.is(c), NAME.set("tutu"));
		git.commit("intern step");
		db.update(t, NeoQL.is(c), NAME.set("tata"));
		git.commit("basecamp1");
		Commit b = git.tag();
		
		db.update(t, NeoQL.is(c), NAME.set("titi"));
		git.commit("explore1");
		Commit explore1 = git.tag();
		
		
		System.out.println("head="+git.tag());
		git.checkout(b);
		System.out.println("head="+git.tag());
		db.update(t, NeoQL.is(c), NAME.set("b-titi"));
		git.commit("derive1- step1");
		db.update(t, NeoQL.is(c), NAME.set("b-tutu"));
		git.commit("derive1-step2");
		System.out.println("head="+git.tag());
		
		git.checkout(explore1);
		db.update(t, NeoQL.is(c), NAME.set("a-tutu"));
		git.commit("explore-2");
		
		JungUtils.disp(git.graph, true, false, true);
		assert c.get().count == 0 : "cursor didn't move";
		

	}

}
