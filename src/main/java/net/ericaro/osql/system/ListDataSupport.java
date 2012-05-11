package net.ericaro.osql.system;

import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


public class ListDataSupport {


    EventListenerList listeners = new EventListenerList();
    Object source;

    
    public ListDataSupport(Object source) {
		super();
		this.source = source;
	}

	public void addListDataListener(ListDataListener l) {
        listeners.add(ListDataListener.class, l);
    }

    public void removeListDataListener(ListDataListener l) {
        listeners.remove(ListDataListener.class, l);
    }
    

    /**
     * @param index
     */
    public void fireContentChanged(int index) {
        ListDataEvent e = null;
        for (ListDataListener l : listeners.getListeners(ListDataListener.class)) {
            if (e == null)
                e = new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, index, index);
            l.contentsChanged(e);
        }
    }

    /**
     * @param index
     */
    public void fireContentChanged(int index0, int index1) {
        ListDataEvent e = null;
        for (ListDataListener l : listeners.getListeners(ListDataListener.class)) {
            if (e == null)
                e = new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, index0, index1);
            l.contentsChanged(e);
        }
    }
    
    /**
     * @param index
     */
    public void fireIntervalAdded(int index) {
        ListDataEvent e = null;
        for (ListDataListener l : listeners.getListeners(ListDataListener.class)) {
            if (e == null)
                e = new ListDataEvent(source, ListDataEvent.INTERVAL_ADDED, index, index);
            l.intervalAdded(e);
        }
    }

    /**
     * @param index
     */
    public void fireIntervalRemoved(int index) {
        ListDataEvent e = null;
        for (ListDataListener l : listeners.getListeners(ListDataListener.class)) {
            if (e == null)
                e = new ListDataEvent(source, ListDataEvent.INTERVAL_REMOVED, index, index);
            l.intervalRemoved(e);
        }
    }
}
