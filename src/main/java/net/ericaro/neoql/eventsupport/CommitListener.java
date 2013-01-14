package net.ericaro.neoql.eventsupport;

import net.ericaro.neoql.git.Commit;
import net.ericaro.neoql.patches.Patch;

import java.util.EventListener;

public interface CommitListener extends EventListener {

	/**
	 * Called when a new commit is created
	 */
	void commitCreated(Commit onto, Patch patch, Commit newHead);

}
