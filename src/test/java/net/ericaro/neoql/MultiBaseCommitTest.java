package net.ericaro.neoql;

import static org.junit.Assert.*;

import net.ericaro.neoql.CursorTest.Tester;
import net.ericaro.neoql.Git.Commit;
import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.eventsupport.TransactionListener;

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
		Database db1 = new Database() ;
		final Database db2 = new Database() ;
		
		Git git = new Git(db1);
		ContentTable<Tester> t = db1.createTable(Tester.class, NAME, COUNT);
		ContentTable<Tester> t2 = db2.createTable(Tester.class, NAME, COUNT);
		Cursor<Tester> c = db1.createCursor(t);
		Cursor<Tester> c2 = db2.createCursor(t);
		
		Commit start = git.tag();
		
		db1.addTransactionListener(new TransactionListener() {
			public void rolledBack(Change change) {}
			public void committed(Change change) {
				db2.apply(change);
			}
		});
		
		Tester v = db1.insert(t, NAME.set("2"), COUNT.set(1));
		db1.moveTo(c, v);
		
		Commit first   = git.commit("first");
		
		assert c2.get() == c.get(): "clone as failed";
		assert "2".equals(c2.get().name): "clone as failed";
		git.checkout(start);
		
		assert c2.get() == null : "clone as failed";
		
		git.checkout(first);
		assert "2".equals(c2.get().name): "clone as failed";
		assert c2.get() == c.get(): "clone as failed";
		
		
	}

}
