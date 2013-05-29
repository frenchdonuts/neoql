package net.ericaro.neoql.properties;

import net.ericaro.neoql.DHL;
import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Property;
import net.ericaro.neoql.eventsupport.HeadListener;
import net.ericaro.neoql.eventsupport.PropertyListener;
import net.ericaro.neoql.eventsupport.PropertyListenerSupport;
import net.ericaro.neoql.git.Commit;
import net.ericaro.neoql.patches.Patch;

/**
 * Property that follows another property, but changes only when the HEAD of a NeoQL "Git" changes.
 * 
 * @author gaetan
 */
public class HeadProperty<T> implements Property<T> {

    private final Property<T> source;
    private final DHL dhl;
    private final HeadListener listener;
    private final PropertyListenerSupport<T> support;

    private T value;

    public HeadProperty(DHL dhl, Property<T> source) {
        this.dhl = dhl;
        this.source = source;
        this.listener = new Listener();
        this.support = new PropertyListenerSupport<T>();
        this.value = source.get();

        dhl.addHeadListener(this.listener);
    }

    @Override
    public T get() {
        return this.value;
    }

    private void follow(T newValue) {
        T oldValue = this.value;
        this.value = newValue;
        if (!NeoQL.eq(newValue, oldValue))
            this.support.fireUpdated(oldValue, newValue);
    }

    @Override
    public void drop() {
        this.dhl.removeHeadListener(this.listener);
        this.source.drop();
        this.follow(null);
    }

    @Override
    public Class<T> getType() {
        return this.source.getType();
    }

    @Override
    public void addPropertyListener(PropertyListener<T> l) {
        this.support.addPropertyListener(l);
    }

    @Override
    public void removePropertyListener(PropertyListener<T> l) {
        this.support.removePropertyListener(l);
    }

    private class Listener implements HeadListener {

        @Override
        public void headChanged(Commit from, Commit to, Iterable<Patch> patches) {
            HeadProperty.this.follow(HeadProperty.this.source.get());
        }

    }

}
