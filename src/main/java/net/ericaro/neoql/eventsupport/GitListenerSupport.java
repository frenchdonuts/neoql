package net.ericaro.neoql.eventsupport;

import net.ericaro.neoql.git.Commit;

import javax.swing.event.EventListenerList;


public class GitListenerSupport {

	EventListenerList listeners = new EventListenerList();

	public GitListenerSupport() {
		super();
	}

	public void addGitListener(GitListener l) {
		listeners.add(GitListener.class, l);
	}

	public void removeGitListener(GitListener l) {
		listeners.remove(GitListener.class, l);
	}

	private GitListener[] listeners() {
		return listeners.getListeners(GitListener.class);
	}

	public int getListenerCount() {
		return listeners.getListenerCount(GitListener.class);
	}

	public void fireHeadChanged(Commit from, Commit to) {
		for (GitListener l : listeners())
			l.headChanged(from, to);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (GitListener l : listeners())
			sb.append(l.toString()).append("\n");
		return sb.toString();
	}
}
