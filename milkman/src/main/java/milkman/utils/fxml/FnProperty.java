package milkman.utils.fxml;
import java.util.Objects;
import java.util.function.Supplier;

import javafx.beans.property.ObjectPropertyBase;
import milkman.utils.CheckedConsumer;
 
public class FnProperty<T> extends ObjectPropertyBase<T> {
   private final Supplier<T> getter;
   private final CheckedConsumer<T> setter;
   
 
    public FnProperty(Supplier<T> getter, CheckedConsumer<T> setter) {
        super();
		this.getter = getter;
		this.setter = setter;
    }
    
    @Override
    public void set(T v) {
        try {
            setter.apply(v);
            super.set(v);
        } catch (final Throwable t) {
            throw new RuntimeException("Unable to set value: " + v, t);
        }
    };
    @Override
    public T get() {
        try {
            // TODO : here we are lazily loading the property which will prevent any property listeners
            // from receiving notice of a direct model field change until the next time the get method
            // is called on the PathProperty
            final T prop = getter.get();
            if (!Objects.equals(super.get(),prop)) {
                super.set(prop);
            }
            return super.get();
        } catch (final Throwable t) {
            throw new RuntimeException("Unable to get value", t);
        }
    }

	@Override
	public Object getBean() {
		return null;
	}

	@Override
	public String getName() {
		return "";
	}
}