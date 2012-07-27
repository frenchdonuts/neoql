package net.ericaro.neoql;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.eventsupport.TransactionListener;
import net.ericaro.neoql.git.Commit;
import net.ericaro.neoql.git.Git;
import net.ericaro.neoql.git.Repository;

import org.junit.Test;

public class MultiBaseCommitTest {

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
	public void testMulti() {
		
		// test that commits from a base can be applied to another one
		Repository repo = new Repository();
		
		Git git= Git.clone(repo);
		Git db2= Git.clone(repo);
		
		ContentTable<Tester> t = git.createTable(Tester.class, NAME, COUNT);
		Cursor<Tester> c = git.createCursor(t);
		Commit start = git.commit();
		Tester v = git.insert(t, NAME.set("2"), COUNT.set(1));
		git.moveTo(c, v);
		Commit first   = git.commit("first");
		db2.checkout(first); // move db2 to first
		Cursor<Tester> c2 = db2.getCursor(c.getKey());
		
		assert c2.get() == c.get(): "clone as failed "+ c2.get()+" <> "+ c.get();
		assert "2".equals(c2.get().name): "clone as failed";
		git.checkout(start);
		db2.checkout(start);
		
		assert c2.get() == null : "clone as failed";
		
		git.checkout(first);
		db2.checkout(first);
		assert "2".equals(c2.get().name): "clone as failed";
		assert c2.get() == c.get(): "clone as failed";
		
		
	}

}
