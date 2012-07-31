package net.ericaro.neoql.git;

import java.util.Map;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.ContentTable;
import net.ericaro.neoql.JungUtils;
import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Property;
import net.ericaro.neoql.patches.Patch;
import net.ericaro.neoql.patches.PatchBuilder;
import net.ericaro.neoql.patches.PatchSet;

import org.junit.Test;

public class MergeTest {
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
		
		Repository repo = new Repository();
		Git g1 = Git.clone(repo );
		Git g2= Git.clone(repo );
		
		g1.atomicCreateTable(Tester.class, NAME, COUNT);
		Commit init = g1.commit("create empty tables");
		g2.checkout(init);
		
		ContentTable<Tester> t1 = g1.getTable(Tester.class);
		ContentTable<Tester> t2 = g2.getTable(Tester.class);
		
		Tester v1 = g1.insert(t1, NAME.set("a1"));
		Property<Tester> c1 = NeoQL.track(t1, v1);
		Commit localTag = g1.commit("added a1");
		
		Tester v2 = g2.insert(t2, NAME.set("a2"));
		Property<Tester> c2 = NeoQL.track(t2, v2);
		Commit remoteTag = g2.commit("added a2");
		
		// g1 and g2 have diverge, let them merge together
		PatchBuilder local = new PatchBuilder();
		System.out.println("LOCAL :");
		for (Patch p : repo.changePath(init, localTag) ) {
			System.out.println(p);
			local.apply(p);
		}
		
		System.out.println("REMOTE :");
		PatchBuilder remote = new PatchBuilder();
		for (Patch p : repo.changePath(init, remoteTag) ) { 
			System.out.println(p);
			remote.apply(p);
		}
		
		Merge m = new Merge(local, remote);
		Patch mergePatch = m.merged.build();
		System.out.println("MERGED :"+mergePatch);
		
		
		Commit merged = repo.commit(mergePatch, init, null, "automerge from git");
		
		Git gm = Git.clone(repo);
		gm.checkout(merged);
		ContentTable<Tester> tm = gm.getTable(Tester.class);
		for(Tester t: tm) 
			System.out.println(t);
		
		
//		JungUtils.disp(repo.getGraph(), true, false, true);
	}
}
