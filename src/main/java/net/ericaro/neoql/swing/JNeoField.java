package net.ericaro.neoql.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Format;
import java.text.ParseException;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

import net.ericaro.neoql.PropertyListener;
import net.ericaro.neoql.Property;
import net.ericaro.neoql.eventsupport.PropertyListenerSupport;

/**
 * Uses a Property as a model. It strictly observe the property, and display its value.
 * When editable, it tries to commit the new value.
 * If the commit is triggered buy a focus lost, and it fails to parse the string as an object, then it reads the focuslost strategy
 * to determine if it should yield the focus, or simply revert (or revert without parsing).
 * Anyhow, absolutely no changes are made to the the model (it is impossible). But a PropertyListener.update(old, new) event is fired,
 * to act as an "action performed". Users are in charge of turning this update into an actual database write.
 * 
 * 
 * @author eric
 * 
 * @param <T>
 */
public class JNeoField<T> extends JTextField {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	public static enum FocusLostStrategy {
		COMMIT_OR_YIELD, COMMIT_OR_REVERT, REVERT;
	};

	PropertyListenerSupport<T>	support				= new PropertyListenerSupport<T>();
	private PropertyListener<T>	listener			= new MyPropertyListener();

	private Property<T>		model;
	private Format				format;
	FocusLostStrategy			focusLostStrategy	= FocusLostStrategy.COMMIT_OR_REVERT;

	public JNeoField() {
		super.setInputVerifier(new JCFieldVerifier());
		setEnabled(model != null);
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!commit())
					revert();
			}
		});
	}

	public void setModel(Property<T> t, Format format) {
		if (this.model != null)
			this.model.removePropertyListener(listener);
		this.model = t;
		this.format = format;
		if (this.model != null)
			this.model.addPropertyListener(listener);
		setEnabled(model != null);
		revert();
	}

	private class MyPropertyListener implements PropertyListener<T> {
		public void updated(T oldValue, T newValue) {
			revert();
		}
	}

	public class JCFieldVerifier extends InputVerifier {
		public boolean verify(JComponent input) {
			assert input == JNeoField.this : "wrong verifier";
			if (focusLostStrategy == FocusLostStrategy.REVERT)
				revert(); // can leave focus
			else if (!commit()) {
				if (focusLostStrategy == FocusLostStrategy.COMMIT_OR_YIELD)
					return false; // yield
				else
					revert();
			}
			return true; // default case
		}

		public boolean shouldYieldFocus(JComponent input) {
			return verify(input);
		}
	}

	public void revert() {
		if (format == null)
			setText("");
		else
			setText(format.format(getValue()));
	}

	/**
	 * commit the actual value in the textfield
	 * 
	 * @return false if it fails to prepare data for commit, otherwise send the value.
	 */
	public boolean commit() {
		String txt = getText();
		T newValue;
		try {
			newValue = (T) format.parseObject(txt);
			revert(); // always revert before commit
		} catch (ParseException e) {
			return false;
		}
		T old = getValue();
		if (old == null) {
			if (newValue != null)
				support.fireUpdated(old, newValue);
		} else if (!old.equals(newValue))
			support.fireUpdated(old, newValue);

		return true;
	}

	private T getValue() {
		return model.get();
	}

	public void addPropertyListener(PropertyListener<T> l) {
		support.addPropertyListener(l);
	}

	public void removePropertyListener(PropertyListener<T> l) {
		support.removePropertyListener(l);
	}

	public FocusLostStrategy getFocusLostStrategy() {
		return focusLostStrategy;
	}

	public void setFocusLostStrategy(FocusLostStrategy focusLostStrategy) {
		this.focusLostStrategy = focusLostStrategy;
	}

}
