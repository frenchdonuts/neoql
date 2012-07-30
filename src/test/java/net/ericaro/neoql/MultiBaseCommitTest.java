package net.ericaro.neoql;

import java.util.Iterator;

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
		git.atomicCreateTable(Tester.class, NAME, COUNT);
		Commit start = git.commit();
		
		ContentTable<Tester> t = git.getTable(Tester.class);
		Tester v = git.insert(t, NAME.set("2"), COUNT.set(1));
		Property<Tester> c = NeoQL.track(t,v);
		Commit first   = git.commit("first");
		db2.checkout(first); // move db2 to first
		
		ContentTable<Tester> t2 = db2.getTable(Tester.class);
		
		Iterator<Tester> i = NeoQL.select(t2).iterator();
		
		Property<Tester> c2 = NeoQL.track(t2, i.next());
		
		assert !i.hasNext() : "table should only contain one value";
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
