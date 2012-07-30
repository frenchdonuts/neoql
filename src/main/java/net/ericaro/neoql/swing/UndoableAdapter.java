package net.ericaro.neoql.swing;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.eventsupport.TransactionListener;
import net.ericaro.neoql.patches.Patch;
import net.ericaro.neoql.patches.Patches;

public class UndoableAdapter {

	UndoableEditListenerSupport support = new UndoableEditListenerSupport();
	private Database	source;
	private TransactionListener	listener;
	
	public UndoableAdapter(final Object eventSource, Database source) {
		this.source = source;
		listener = new TransactionListener() {
			
			
			@Override
			public void rolledBack(Patch change) {}
			
			
			@Override
			public void committed(Patch change) {
				fireUndoableEditEvent(new UndoableEditEvent(eventSource, new ChangeUndoableEdit(change)));
			}
		};
		source.addTransactionListener(listener);
	}

	public void addUndoableEditListener(UndoableEditListener listener) {
		support.addUndoableEditListener(listener);
	}

	public void removeUndoableEditListener(UndoableEditListener listener) {
		support.removeUndoableEditListener(listener);
	}

	protected void fireUndoableEditEvent(UndoableEditEvent e) {
		support.fireUndoableEditEvent(e);
	}
	
	
	class ChangeUndoableEdit extends AbstractUndoableEdit {

		Patch change;
		
		
		public ChangeUndoableEdit(Patch change) {
			super();
			this.change = change;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			try {
				source.removeTransactionListener(listener);
				source.apply(Patches.reverse(change) );
			}finally {
				source.addTransactionListener(listener);
			}
			
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			try {
				source.removeTransactionListener(listener);
				source.apply(change) ;
			}finally {
				source.addTransactionListener(listener);
			}
		}
	}
}
