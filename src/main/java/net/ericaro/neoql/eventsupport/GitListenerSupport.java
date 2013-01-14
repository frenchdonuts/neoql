package net.ericaro.neoql.eventsupport;

import net.ericaro.neoql.git.Commit;
import net.ericaro.neoql.patches.Patch;

import javax.swing.event.EventListenerList;


public class GitListenerSupport {

	EventListenerList listeners = new EventListenerList();

	public GitListenerSupport() {
		super();
	}


	// Head listeners

	public void addHeadListener(HeadListener l) {
		listeners.add(HeadListener.class, l);
	}

	public void removeHeadListener(HeadListener l) {
		listeners.remove(HeadListener.class, l);
	}

	private HeadListener[] headListeners() {
		return listeners.getListeners(HeadListener.class);
	}

	public int getHeadListenerCount() {
		return listeners.getListenerCount(HeadListener.class);
	}

	public void fireHeadChanged(Commit from, Commit to) {
		for (HeadListener l : listeners.getListeners(HeadListener.class))
			l.headChanged(from, to);
	}


	// Commit listeners

	public void addCommitListener(CommitListener l) {
		listeners.add(CommitListener.class, l);
	}

	public void removeCommitListener(CommitListener l) {
		listeners.remove(CommitListener.class, l);
	}

	private CommitListener[] commitListeners() {
		return listeners.getListeners(CommitListener.class);
	}

	public int getCommitListenerCount() {
		return listeners.getListenerCount(CommitListener.class);
	}

	public void fireCommitCreated(Commit onto, Patch p, Commit c) {
		for (CommitListener l : listeners.getListeners(CommitListener.class))
			l.commitCreated(onto, p, c);
	}




	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (HeadListener l : headListeners())
			sb.append(l.toString()).append("\n");
		for (CommitListener l : commitListeners())
			sb.append(l.toString()).append("\n");
		return sb.toString();
	}
}
