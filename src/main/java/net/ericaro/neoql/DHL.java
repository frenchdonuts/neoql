package net.ericaro.neoql;

import net.ericaro.neoql.eventsupport.CommitListener;
import net.ericaro.neoql.eventsupport.HeadListener;
import net.ericaro.neoql.git.Commit;

/** Data History Language Interface
 *
 * @author gaetan
 *
 */
public interface DHL {

    Commit head();

    void addHeadListener(HeadListener l);
    void removeHeadListener(HeadListener l);

    void addCommitListener(CommitListener l);
    void removeCommitListener(CommitListener l);

}
