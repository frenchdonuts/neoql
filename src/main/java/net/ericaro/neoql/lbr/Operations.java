package net.ericaro.neoql.lbr;

import net.ericaro.neoql.DML;
import net.ericaro.neoql.DQL;
import net.ericaro.neoql.DTL;
import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.ChangeSet;

public interface Operations {
	void commit(DQL local) throws CommitException;
	<D extends DML&DQL> void fetch(D remote) throws FetchException;
	<L extends DML&DQL> void merge(L local, DQL remote, DQL base, Change localChanges, Change remoteChange) throws MergeException;
}
