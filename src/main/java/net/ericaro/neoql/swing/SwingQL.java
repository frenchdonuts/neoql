package net.ericaro.neoql.swing;

import java.beans.PropertyChangeListener;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.Database;
import net.ericaro.neoql.PropertyListener;
import net.ericaro.neoql.Singleton;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.TableData;
import net.ericaro.neoql.notinvasive.Stuff;

/** Query Langage Adapter for Swing
 * 
 * @author eric
 *
 */
public class SwingQL {

	/** generates a java swing ListModel based on a table.
	 * 
	 * @param table
	 * @return
	 */
	public static <T, U extends ListModel> U listFor(Table<T> table) {
		return (U) new TableList<T>(table);
	}

	
	
	public static <T> PropertyListener<T> addPropertyChangeListener(Singleton<T> source, String propertyName, PropertyChangeListener listener) {
		PropertyChangeAdapter<T> l = new PropertyChangeAdapter<T>(source, propertyName, listener);
		source.addPropertyListener(l);
		return l;
	}
	
	public static <T> void removePropertyChangeListener(Singleton<T> source, PropertyListener<T> adapter) {
		source.removePropertyListener(adapter);
	}
}
