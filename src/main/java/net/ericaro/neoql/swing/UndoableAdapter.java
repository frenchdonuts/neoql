package net.ericaro.neoql.swing;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.eventsupport.TransactionListener;

public class UndoableAdapter {

	UndoableEditListenerSupport support = new UndoableEditListenerSupport();
	private Database	source;
	private TransactionListener	listener;
	
	public UndoableAdapter(final Object eventSource, Database source) {
		this.source = source;
		listener = new TransactionListener() {
			
			
			@Override
			public void rolledBack(Change change) {
			}
			
			@Override
			public void reverted(Change change) {
			}
			
			@Override
			public void committed(Change change) {
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

		Change change;
		
		
		public ChangeUndoableEdit(Change change) {
			super();
			this.change = change;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			try {
				source.removeTransactionListener(listener);
				change.revert() ;
			}finally {
				source.addTransactionListener(listener);
			}
			
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			try {
				source.removeTransactionListener(listener);
				change.commit() ;
			}finally {
				source.addTransactionListener(listener);
			}
		}
	}
}
