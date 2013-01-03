package net.ericaro.neoql.eventsupport;

import net.ericaro.neoql.git.Commit;

import java.util.EventListener;

public interface GitListener extends EventListener {

	/**
	 * Called whenever the HEAD changed (because of a commit,
	 * checkout, merge, etc)
	 */
	void headChanged(Commit from, Commit to);

}
