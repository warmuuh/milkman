package milkman.utils;

import java.util.ArrayList;
import java.util.List;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import lombok.SneakyThrows;

public class WeakEvent {

  List<WeakReference<Runnable>> listeners = new ArrayList<>();



  public void add(Runnable listener) {
    listeners.add(new WeakReference<>(listener));
  }

  @SneakyThrows
  public void invoke() {
    Iterator<WeakReference<Runnable>> it = listeners.iterator();
    while (it.hasNext()) {
      Runnable l = it.next().get();
      if (l == null) {
        // reference cleared, remove from list
        it.remove();
      } else {
        l.run();
      }
    }
  }

  public void clear() {
    listeners.clear();
  }

}
