package net.ericaro.neoql.eventsupport;

import net.ericaro.neoql.git.Commit;

import java.util.EventListener;

public interface HeadListener extends EventListener {

	/**
	 * Called when the HEAD changed (because of a commit,
	 * checkout, merge, etc)
	 */
	void headChanged(Commit from, Commit to);

}
