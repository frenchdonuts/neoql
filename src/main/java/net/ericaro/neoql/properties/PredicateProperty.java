package net.ericaro.neoql.properties;

import java.util.Iterator;

import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Predicate;
import net.ericaro.neoql.Property;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.eventsupport.PropertyListener;
import net.ericaro.neoql.eventsupport.PropertyListenerSupport;
import net.ericaro.neoql.eventsupport.TableListener;

/**
 * Property that follows a row satisfying a given predicate. If two or more rows satisfy the predicate at the same time,
 * one is chosen arbitrarily. If no row satisfy the predicate at a given time, the value of the property is
 * <tt>null</tt>.
 * <p>
 * This property can be used in manner similar to NeoQL's ObservableCursors. The benefit is that when the tracked row is
 * deleted, a PredicateProperty can switch to another "identical" row if it exists (or if one is inserted later). The
 * definition of "identical" is controlled by the predicate.
 * 
 * @author gaetan
 */
public class PredicateProperty<T> implements Property<T> {

    private final Table<T> source;
    private final Predicate<T> predicate;
    private final TableListener<T> listener;
    private final PropertyListenerSupport<T> support = new PropertyListenerSupport<T>();

    private T value;

    public PredicateProperty(Table<T> source, Predicate<T> predicate) {
        super();
        this.source = source;
        this.predicate = predicate;
        this.listener = new Listener();
        this.value = this.findOne();
        source.addTableListener(this.listener);
    }

    @Override
    public T get() {
        return this.value;
    }

    @Override
    public void drop() {
        this.source.removeTableListener(this.listener);
        this.follow(null); // also nullify the value
    }

    @Override
    public Class<T> getType() {
        return this.source.getType();
    }

    private T findOne() {
        Iterator<T> candidates = NeoQL.select(this.source, this.predicate).iterator();
        return candidates.hasNext() ? candidates.next() : null;
    }

    private void follow(T newValue) {
        T oldValue = this.value;
        this.value = newValue;
        if (!NeoQL.eq(newValue, oldValue))
            this.support.fireUpdated(oldValue, newValue);
    }

    @Override
    public void addPropertyListener(PropertyListener<T> l) {
        this.support.addPropertyListener(l);
    }

    @Override
    public void removePropertyListener(PropertyListener<T> l) {
        this.support.removePropertyListener(l);
    }

    private class Listener implements TableListener<T> {

        @Override
        public void updated(T oldRow, T newRow) {
            if (PredicateProperty.this.value == null && PredicateProperty.this.predicate.eval(newRow))
                PredicateProperty.this.follow(newRow);
            else if (PredicateProperty.this.value == oldRow)
                PredicateProperty.this.follow(PredicateProperty.this.predicate.eval(newRow) ? newRow
                        : PredicateProperty.this.findOne());
        }

        @Override
        public void deleted(T oldRow) {
            if (oldRow == PredicateProperty.this.value)
                PredicateProperty.this.follow(PredicateProperty.this.findOne());
        }

        @Override
        public void inserted(T newRow) {
            if (PredicateProperty.this.value == null && PredicateProperty.this.predicate.eval(newRow))
                PredicateProperty.this.follow(newRow);
        }

        @Override
        public void dropped(Table<T> table) {
            PredicateProperty.this.drop();
        }
    }

}
