package net.ericaro.neoql.git;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.ContentTable;
import net.ericaro.neoql.JungUtils;
import net.ericaro.neoql.NeoQL;

import org.junit.Test;

public class DualThreadTester {

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
	public void testDualThread() {
		final Repository repo = new Repository(); // the shared memory

		final Git local = Git.clone(repo);
		final ContentTable<Tester> t = local.createTable(Tester.class, NAME, COUNT);
		final Commit common = local.head();
		final Git remote = Git.clone(repo);
		remote.checkout(common); // both local and remote are at the same point now
		final ContentTable<Tester> remoteT = remote.getTable(Tester.class);
		remote.checkoutNewBranch();

		final AtomicBoolean running = new AtomicBoolean(true);
		final CyclicBarrier end = new CyclicBarrier(2);

		Runnable localEdit = new Runnable() {
			public void run() {
				try {
					int i = 0;
					while (running.get()) {
						i++;
						local.insert(t, NAME.set("loc" + i));

						local.commit("setting local name " + i);
						Thread.sleep(3);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		Runnable remoteEdit = new Runnable() {
			public void run() {
				remote.checkout(common); // both local and remote are at the same point now
				ContentTable<Tester> remoteT = remote.getTable(Tester.class);
				remote.checkoutNewBranch();
				int i = 0;
				while (running.get()) {
					i++;
					remote.insert(remoteT, NAME.set("rem" + i));
					remote.commit("setting remote name " + i);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};

		Runnable mergeEdit = new Runnable() {
			public void run() {
				while (running.get()) {
					if (!local.isClean())
						local.commit("commit before merge");

					Merge m = local.merge(remote.getBranch());
					System.out.println(m.isNothingToUpdate());
					local.apply(m);

					try {
						Thread.sleep(13);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}
		};

		new Thread(localEdit).start();
		new Thread(remoteEdit).start();
		new Thread(mergeEdit).start();
		try {
			Thread.sleep(1000);
			running.set(false);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JungUtils.disp(repo.getGraph(), true, false, true);
		System.out.println("done");

	}
}
