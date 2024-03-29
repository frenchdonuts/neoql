package net.ericaro.neoql;

import net.ericaro.neoql.git.Commit;
import net.ericaro.neoql.git.Git;
import net.ericaro.neoql.git.Repository;

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

	public static Column<TesterCursor, Tester> TESTER= NeoQL.column(TesterCursor.class, "target", Tester.class, true);
	public static class TesterCursor {
		Tester target;
		public Tester get() {
			return target;
		}
		@Override
		public String toString() {
			return "*->"+target ;
		}
	}





	@Test
	public void testBasic() {

		Repository repo = new Repository();
		Git git = Git.clone(repo);

		ContentTable<Tester> t = git.createTable(Tester.class, NAME, COUNT);
		ContentTable<TesterCursor> tc = git.createTable(TesterCursor.class, TESTER);

		git.insert(t, NAME.set("1"), COUNT.set(0));
		Tester v = git.insert(t, NAME.set("2"), COUNT.set(1));

		TesterCursor vc = git.insert(tc, TESTER.set(v));
		Property<TesterCursor> tp = NeoQL.track(tc, vc);
		Property<Tester> c = NeoQL.track(tp, TESTER);


		Commit first = 	git.commit("first");
		assert c.get() == v : "cursor wrongly initialized: "+c.get()+ " <> "+ v;
		assert c.get().count == 1 : "cursor points to a wrong value";

		v = NeoQL.select(t, COUNT.is(0)).iterator().next();
		git.update(tc, NeoQL.is(tp), TESTER.set(v));
		//git.moveTo(c, v);

		git.commit("moving cursor");

		git.update(t, NeoQL.is(c), NAME.set("tutu"));
		git.commit("intern step");
		git.update(t, NeoQL.is(c), NAME.set("tata"));
		Commit b = git.commit("basecamp1");
		//Commit b = git.head();

		git.update(t, NeoQL.is(c), NAME.set("titi"));
		git.commit("explore1");
		Commit explore1 = git.head();


		System.out.println("head="+git.head());
		git.checkout(b);
		System.out.println("head="+git.head());
		git.update(t, NeoQL.is(c), NAME.set("b-titi"));
		git.commit("derive1- step1");
		git.update(t, NeoQL.is(c), NAME.set("b-tutu"));
		git.commit("derive1-step2");
		System.out.println("head="+git.head());

		git.checkout(explore1);
		git.update(t, NeoQL.is(c), NAME.set("a-tutu"));
		git.commit("explore-2");

		//JungUtils.disp(git.getRepositoryGraph(), true, false, true);
		assert c.get().count == 0 : "cursor didn't move";


	}

}
