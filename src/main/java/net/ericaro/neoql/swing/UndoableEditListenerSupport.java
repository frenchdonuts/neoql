package net.ericaro.neoql.swing;

import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;

import net.ericaro.neoql.Table;

/** Provide an event support for UndoEditListener
 * 
 * @author eric
 *
 * @param <T>
 */
public class UndoableEditListenerSupport {

	EventListenerList listeners = new EventListenerList();

	public UndoableEditListenerSupport() {
		super();
	}

    public void addUndoableEditListener(UndoableEditListener listener) {
	listeners.add(UndoableEditListener.class, listener);
    }

    public void removeUndoableEditListener(UndoableEditListener listener) {
	listeners.remove(UndoableEditListener.class, listener);
    }

	private UndoableEditListener[] listeners() {
		return listeners.getListeners(UndoableEditListener.class);
	}

	public int getListenerCount() {
		return listeners.getListenerCount(UndoableEditListener.class);
	}

	public void fireUndoableEditEvent(UndoableEditEvent e) {
		for (UndoableEditListener l : listeners())
			l.undoableEditHappened(e);
	}
}