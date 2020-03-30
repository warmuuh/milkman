package milkman.plugin.scripting.graaljs;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.Proxy;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * allows map[key] syntax for objects and maps
 */
@RequiredArgsConstructor
public class DynamicReadonlyProxy implements Proxy {

    private static List<Class> primitiveTypes = List.of(
            String.class,
            Integer.class,
            Long.class,
            Short.class,
            Double.class,
            Boolean.class,
            Byte.class,
            Float.class,
            Character.class
    );


    public static Object from(Object object) {
        if (object.getClass().isPrimitive())
            return object;
        if (primitiveTypes.contains(object.getClass()))
            return object;

        if (object instanceof Map) {
            return new MapProxy((Map)object);
        } else if (object instanceof  List){
            return  new ArrayProxy((List)object);
        }

        return new ObjectProxy(object);
    }


    public static class ObjectProxy extends DynamicReadonlyProxy implements ProxyObject {

        private final Object object;
        private final BeanInfo beanInfo;

        @SneakyThrows
        public ObjectProxy(Object object) {
            this.object = object;
            beanInfo = Introspector.getBeanInfo(object.getClass());
        }

        @Override
        public Object getMember(String key) {
            return Arrays.stream(beanInfo.getPropertyDescriptors())
                    .filter(pd -> pd.getName().equals(key))
                    .findAny()
                    .map(pd -> {
                        try {
                            return DynamicReadonlyProxy.from(pd.getReadMethod().invoke(object));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .orElseThrow(() -> new RuntimeException("Cannot find member '" + key + "'"));
        }

        @Override
        public Object getMemberKeys() {
            return Arrays.stream(beanInfo.getPropertyDescriptors()).map(pd -> pd.getName()).collect(Collectors.toList());
        }

        @Override
        public boolean hasMember(String key) {
            return Arrays.stream(beanInfo.getPropertyDescriptors()).anyMatch(pd -> pd.getName().equals(key));
        }

        @Override
        public void putMember(String key, Value value) {

        }
    }

    @RequiredArgsConstructor
    public static class MapProxy extends DynamicReadonlyProxy implements ProxyObject {

        private final Map map;

        @Override
        public Object getMember(String key) {
            return DynamicReadonlyProxy.from(map.get(key));
        }

        @Override
        public Object getMemberKeys() {
            return new ArrayList(map.keySet());
        }

        @Override
        public boolean hasMember(String key) {
            return map.containsKey(key);
        }

        @Override
        public void putMember(String key, Value value) {

        }
    }

    @RequiredArgsConstructor
    public static class ArrayProxy extends DynamicReadonlyProxy implements ProxyArray {

        private final List<Object> items;

        @Override
        public Object get(long index) {
            return DynamicReadonlyProxy.from(items.get((int)index));
        }

        @Override
        public void set(long index, Value value) {

        }

        @Override
        public long getSize() {
            return items.size();
        }
    }
}
