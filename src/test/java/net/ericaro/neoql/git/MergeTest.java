package net.ericaro.neoql.git;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.ContentTable;
import net.ericaro.neoql.JungUtils;
import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Property;

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
	public void testBugMerge() {
		Repository repo = new Repository();

		Git git = Git.clone(repo);
		Branch master = git.getBranch();
		ContentTable<Tester> t = git.createTable(Tester.class, NAME, COUNT);
		
		Commit common = git.tag();
		Branch remote = git.checkoutNewBranch();
		git.insert(t, NAME.set("a0"));
		git.commit();
		git.insert(t, NAME.set("a1"));
		git.commit();
		git.insert(t, NAME.set("a2"));
		git.commit();
		git.insert(t, NAME.set("a3"));
		git.commit();
		git.insert(t, NAME.set("a4"));
		git.commit();
		
		git.checkout(master); // goes bach to the common ancestor
		git.insert(t, NAME.set("b0"));
		git.commit();
		git.insert(t, NAME.set("b1"));
		git.commit();
		git.insert(t, NAME.set("b2"));
		git.commit();
		git.insert(t, NAME.set("b3"));
		git.commit();
		git.insert(t, NAME.set("b4"));
		git.commit();
		
		
		assert repo.commonAncestor(remote.getCommit(), master.getCommit()) == common : "failed to compute the common ancestor";
		git.merge(remote);
		assert true: "bug is there";
		
		
	}

	@Test
	public void testBasic() {

		Repository repo = new Repository();

		Git git = Git.clone(repo);
		Branch master = git.getBranch();
		System.out.println("commit init " + master.getCommit());

		ContentTable<Tester> t = git.createTable(Tester.class, NAME, COUNT);
		Tester v1 = git.insert(t, NAME.set("a0"));
		Property<Tester> c1 = NeoQL.track(t, v1);
		git.commit("inserted A");
		System.out.println("commit  last in master " + master.getCommit());

		Commit base = git.tag();
		Branch deriv = git.createBranch(); // create the branch but do not check it out

		// git.insert(t, NAME.set("a2"));
		// git.commit("added a2");
		git.update(t, NeoQL.is(c1), NAME.set("a1"));
		git.commit("updated A");
		Merge m = git.merge(deriv);
		assert m.isNothingToUpdate() && !m.isFastForward() && !m.hasConflicts() : "wrong merge";
		git.apply(m);

		git.checkout(deriv);
		master = git.createBranch();

		git.insert(t, NAME.set("a2"));
		git.commit("added a2");
		git.checkout(master);
		git.update(t, NeoQL.is(c1), NAME.set("a1"));
		git.commit("updated A");

		m = git.merge(deriv);
		assert !m.isNothingToUpdate() && !m.isFastForward() && !m.hasConflicts() : "wrong merge";
		git.apply(m);

		git.checkout(base); // goes back to the nexus
		// doing a fast forward
		master = git.createBranch();
		deriv = git.checkoutNewBranch();

		git.insert(t, NAME.set("a4"));
		git.commit("added a4");
		git.checkout(master);
		m = git.merge(deriv);
		assert !m.isNothingToUpdate() && m.isFastForward() && !m.hasConflicts() : "wrong merge";
		git.apply(m);

		if (m.hasConflicts())
			for (Conflict c : m.allConflicts())
				c.resolveRemote();

		git.checkout(base);
		master = git.checkoutNewBranch();
		git.merge(master);

		ContentTable<Tester> tm = git.getTable(Tester.class);
		for (Tester tt : tm)
			System.out.println(tt);

		// JungUtils.disp(repo.getGraph(), true, false, true);
	}
}
