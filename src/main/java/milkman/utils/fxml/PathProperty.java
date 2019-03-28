package milkman.utils.fxml;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Objects;

import javafx.beans.property.ObjectPropertyBase;
 
public class PathProperty<B, T> extends ObjectPropertyBase<T> {
    private final String fieldPath;
    private PropertyMethodHandles propMethHandles;
    private final B bean;
 
    public PathProperty(final B bean, final String fieldPath, final Class<T> type) {
        super();
        this.bean = bean;
        this.fieldPath = fieldPath;
        try {
            this.propMethHandles = PropertyMethodHandles.build(getBean(), getName());
        } catch (final Throwable t) {
            throw new RuntimeException(String.format(
                    "Unable to instantiate expression %1$s on %2$s",
                    getBean(), getName()), t);
        }
    }
    @Override
    public void set(T v) {
        try {
            getPropMethHandles().getSetter().invoke(v);
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
            final T prop = (T) getPropMethHandles().getAccessor().invoke();
            if (!Objects.equals(super.get(),prop)) {
                super.set(prop);
            }
            return super.get();
        } catch (final Throwable t) {
            throw new RuntimeException("Unable to get value", t);
        }
    }
    @Override
    public B getBean() {
        return bean;
    }
    @Override
    public String getName() {
        return fieldPath;
    }
    public PropertyMethodHandles getPropMethHandles() {
        return propMethHandles;
    }
 
    public static class PropertyMethodHandles {
        private final String fieldName;
        private final MethodHandle accessor;
        private final MethodHandle setter;
        private Object setterArgument;
 
        protected PropertyMethodHandles(final Object target, final String fieldName,
                final boolean insertSetterArgument) throws NoSuchMethodException {
            this.fieldName = fieldName;
            this.accessor = buildGetter(target, fieldName);
            this.setter = buildSetter(getAccessor(), target, fieldName, insertSetterArgument);
        }
        public static PropertyMethodHandles build(final Object initialTarget,
                final String expString) throws NoSuchMethodException, IllegalStateException {
            final String[] expStr = expString.split("\\.");
            Object target = initialTarget;
            PropertyMethodHandles pmh = null;
            for (int i = 0; i < expStr.length; i++) {
                pmh = new PropertyMethodHandles(target, expStr[i], i < (expStr.length - 1));
                target = pmh.getSetterArgument();
            }
            return pmh;
        }
        protected MethodHandle buildGetter(final Object target, final String fieldName)
                        throws NoSuchMethodException {
            final MethodHandle mh = buildAccessor(target, fieldName, "get", "is", "has");
            if (mh == null) {
                throw new NoSuchMethodException(fieldName);
            }
            return mh;
        }
        protected MethodHandle buildSetter(final MethodHandle accessor,
                final Object target, final String fieldName,
                final boolean insertSetterArgument) {
            if (insertSetterArgument) {
                try {
                    this.setterArgument = accessor.invoke();
                } catch (final Throwable t) {
                    this.setterArgument = null;
                }
                if (getSetterArgument() == null) {
                    try {
                        this.setterArgument = accessor.type().returnType().newInstance();
                    } catch (final Exception e) {
                        throw new IllegalArgumentException(
                                String.format("Unable to build setter expression for %1$s using %2$s.",
                                        fieldName, accessor.type().returnType()));
                    }
                }
            }
            try {
                final MethodHandle mh1 = MethodHandles.lookup().findVirtual(target.getClass(),
                        buildMethodName("set", fieldName),
                        MethodType.methodType(void.class,
                                accessor.type().returnType())).bindTo(target);
                if (getSetterArgument() != null) {
                    mh1.invoke(getSetterArgument());
                }
                return mh1;
            } catch (final Throwable t) {
                throw new IllegalArgumentException("Unable to resolve setter "
                        + fieldName, t);
            }
        }
        protected static MethodHandle buildAccessor(final Object target,
                final String fieldName, final String... fieldNamePrefix) {
            final String accessorName = buildMethodName(fieldNamePrefix[0], fieldName);
            try {
                return MethodHandles.lookup().findVirtual(target.getClass(), accessorName,
                        MethodType.methodType(
                                target.getClass().getMethod(
                                        accessorName).getReturnType())).bindTo(target);
            } catch (final NoSuchMethodException e) {
                return buildAccessor(target, fieldName,
                        Arrays.copyOfRange(fieldNamePrefix, 1,
                                fieldNamePrefix.length));
            } catch (final Throwable t) {
                throw new IllegalArgumentException(
                        "Unable to resolve accessor " + accessorName, t);
            }
        }
        
        
        public static String buildMethodName(final String prefix,
                final String fieldName) {
            return (fieldName.startsWith(prefix) ? fieldName : prefix +
                fieldName.substring(0, 1).toUpperCase() +
                    fieldName.substring(1));
        }
        public String getFieldName() {
            return fieldName;
        }
        public MethodHandle getAccessor() {
            return accessor;
        }
        public MethodHandle getSetter() {
            return setter;
        }
        public Object getSetterArgument() {
            return setterArgument;
        }
    }
}