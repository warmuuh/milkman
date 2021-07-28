package milkman.utils.fxml;

import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
 
public class GenericBinding<O, T> extends ObjectPropertyBase<T> {
   private final Function<O, T> getter;
   private final BiConsumer<O, T> setter;
 
   private O obj;
 
   
   public static <OS, TS> GenericBinding<OS, TS> of(Function<OS, TS> getter, BiConsumer<OS, TS> setter){
	   return new GenericBinding<>(getter, setter);
   }
   
   public static <OS, TS> GenericBinding<OS, TS> of(Function<OS, TS> getter, BiConsumer<OS, TS> setter, OS obj){
	   GenericBinding<OS, TS> b = new GenericBinding<>(getter, setter);
	   b.obj = obj;
	   return b;
   }
   
   
    private GenericBinding(Function<O, T> getter, BiConsumer<O, T> setter) {
        this.getter = getter;
		this.setter = setter;
    }
    
    @Override
    public void set(T v) {
        try {
            fireValueChangedEvent();
            setter.accept(obj, v);
            super.set(v);
        } catch (Throwable t) {
            throw new RuntimeException("Unable to set value: " + v, t);
        }
    }

    @Override
    public T get() {
        try {
            // TODO : here we are lazily loading the property which will prevent any property listeners
            // from receiving notice of a direct model field change until the next time the get method
            // is called on the PathProperty
            T prop = getter.apply(obj);
            if (!Objects.equals(super.get(),prop)) {
                super.set(prop);
            }
            return super.get();
        } catch (Throwable t) {
            throw new RuntimeException("Unable to get value", t);
        }
    }
    
    
    public void bindTo(Property<T> property, O obj) {
    	this.obj = obj;
    	property.unbindBidirectional(this);
    	property.bindBidirectional(this);
    }

    public void bindToUni(ReadOnlyProperty<T> property, O obj) {
        this.obj = obj;
        property.addListener((obs, o, n) -> set(n));
    }
    
	@Override
	public Object getBean() {
		return null;
	}

	@Override
	public String getName() {
		return "";
	}
	
	
	public EventStream<T> toStream(){
		//this instead of EventStream.nonNullValues bc we want to omit initial (artificial) value
		return EventStreams.changesOf(this).filterMap(c -> Optional.ofNullable(c.getNewValue()));
	}

}