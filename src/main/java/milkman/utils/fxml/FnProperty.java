package milkman.utils.fxml;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Objects;

import io.vavr.CheckedConsumer;
import io.vavr.Function0;
import javafx.beans.property.ObjectPropertyBase;
 
public class FnProperty<T> extends ObjectPropertyBase<T> {
   private final Function0<T> getter;
   private final CheckedConsumer<T> setter;
   
 
    public FnProperty(Function0<T> getter, CheckedConsumer<T> setter) {
        super();
		this.getter = getter;
		this.setter = setter;
    }
    
    @Override
    public void set(T v) {
        try {
            setter.accept(v);
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
            final T prop = getter.apply();
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