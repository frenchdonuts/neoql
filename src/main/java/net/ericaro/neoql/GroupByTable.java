package net.ericaro.neoql;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.ericaro.neoql.eventsupport.TableListener;
import net.ericaro.neoql.eventsupport.TableListenerSupport;



/** A table that group all equivalent values from a column.
 * Turns 
 * <table>
 * <tr><th>Column 1</th><th>Col2</th></tr>
 * <tr><td>toto    </td><td>1</td></tr>
 * <tr><td>titi    </td><td>2</td></tr>
 * <tr><td>titi    </td><td>3</td></tr>
 * <tr><td>tutu    </td><td>4</td></tr>
 * <tr><td>tutu    </td><td>5</td></tr>
 * <tr><td>tutu    </td><td>6</td></tr>
 * </table>
 * Into 
 * <table>
 * <tr><th>Col1</th></tr>
 * <tr><td>toto</td></tr>
 * <tr><td>titi</td></tr>
 * <tr><td>tutu</td></tr>
 * </table>
 * 
 * @author eric
 *
 * @param <S> The basic table type 
 * @param <T> the group column type
 */
public class GroupByTable<S, T> implements Table<T> {

	private Table<S>				table;
	private TableListenerSupport<T>	events		= new TableListenerSupport<T>();
	private Column<S, T>			groupByColumn;
	private TableListener<S>		listener;
	private Set<T>					equivalents	= new HashSet<T>();

	GroupByTable(Column<S, T> groupBy, Table<S> table) {
		super();
		this.table = table;
		this.groupByColumn = groupBy;
		
		// creates the sub class listener
		listener = new TableListener<S>() {

			public void inserted(S row) { 
				// new row in the sub table, hence
				insertedByCol(groupByColumn.get(row) ); // mimic a column's insertion
			}

			// called to mimic a column's insertion
			private void insertedByCol(T v) { 
				if (equivalents.contains(v) ) // there is already an equivalent column's value
					return ; // do nothing
				// this is a new value, 
				equivalents.add(v);// remember the value to be able to perform equivalent tests
				events.fireInserted(v); // because it's a new value, fire the insertion
			}

			public void deleted(S row) { // removing row
				deletedByCol(groupByColumn.get(row)); // mimic a column deletion
			}

			private void deletedByCol(T v) {
				// I know that a column's value has been deleted. I should fire a deletion, iif it was the latest representant
				for (S r : GroupByTable.this.table) {
					if (v.equals(groupByColumn.get(r))) // I'm not alone, cool
						return;
				}
				// well it was the latest of his kind. aknowledge that mamuth have disappeared.
				equivalents.remove(v);
				events.fireDeleted(v); // tells everyone about this story
			}

			public void updated(S old, S row) { // a row has changed.
				T vold = groupByColumn.get(old);
				T vnew = groupByColumn.get(row);

				if (vold.equals(vnew)) // shortcut to skip long and complicated tests
					return; // as far as the group by column is concerned, this is in fact no change.
				deletedByCol(vold); // act like a deletion followed by an insertion
				insertedByCol(vnew);
			}

			@Override
			public void dropped(Table<S> table) {
				drop();
			}
			
			
		};

		// fake call to fireinserted, to insert already presents values.
		for (S r : table)
			listener.inserted(r); // cause events to be fire just like if the items where appended
		
		table.addTableListener(listener); // register to actual changes after the loop so that no changes can be tested twice
	}

	
	@Override
	public void drop() {
		table.removeTableListener(listener);
		equivalents.clear() ;
		events.fireDrop(this);
	}

	@Override
	public Iterator<T> iterator() {
		return equivalents.iterator();
	}

	public void addTableListener(TableListener<T> l) {
		events.addTableListener(l);
	}

	public void removeTableListener(TableListener<T> l) {
		events.removeTableListener(l);
	}
}