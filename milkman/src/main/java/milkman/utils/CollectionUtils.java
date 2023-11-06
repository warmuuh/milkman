package milkman.utils;

import java.util.List;
import java.util.function.Predicate;

public class CollectionUtils {


  public static <T> int indexOfFirst(List<T> items, Predicate<T> predicate) {
    for (int i = 0; i < items.size(); i++) {
      T item = items.get(i);
      if (predicate.test(item)) {
        return i;
      }
    }
    return -1;
  }
}
