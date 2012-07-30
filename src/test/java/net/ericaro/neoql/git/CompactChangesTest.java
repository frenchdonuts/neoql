package net.ericaro.neoql.git;

import java.util.Map;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.ContentTable;
import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Property;

import org.junit.Test;

public class CompactChangesTest {
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
	public void testCompactChanges() {
		
//		Repository repo = new Repository();
//		Git git = Git.clone(repo );
//		ContentTable<Tester> t = git.createTable(Tester.class, NAME, COUNT);
//		Commit init = git.tag();
//		Tester v = git.insert(t, NAME.set("a"));
//		Property<Tester> c = NeoQL.track(t, v);
//		git.commit("added a");
//		git.update(t, NeoQL.is(c), NAME.set("b"));
//		Commit end = git.commit("renamed to b");
//		ChangeSet cs = new ChangeSet(repo.changePath(init, end) );
//		System.out.println("changes to compact");
//		System.out.println(cs);
//		Map<Object, RowChange> map = CompactChanges.compact(cs);
//		
//		
//		assert map.keySet().size() == 1 : "wrong keyset size,there should be one, and only one entry for "+v;
//		RowChange r = map.get(v);
//		assert r !=null: "v should be the one";
		
	}
}
