package milkman.docs;

import static java.util.stream.Collectors.groupingBy;
import static org.reflections.scanners.Scanners.SubTypes;

import java.beans.Introspector;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestAspect;
import milkman.domain.ResponseAspect;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.RequestTypePlugin;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

@Slf4j
public class ScriptingReferenceGeneratorTest {

  Reflections reflections = new Reflections("milkman");


  @Test
  void generateScriptingApiDocs() throws IOException {


    StringBuilder b = new StringBuilder(IOUtils.toString(getClass().getResourceAsStream("/script-template.md")));

    Set<Class<?>> plugins = reflections.get(SubTypes.of(RequestAspectsPlugin.class).asClass());

    Set<Class<?>> scriptClasses = new HashSet<>();
    scriptClasses.addAll(reflections.get(SubTypes.of(RequestAspect.class).asClass()));
    scriptClasses.addAll(reflections.get(SubTypes.of(ResponseAspect.class).asClass()));

    scriptClasses.stream().collect(groupingBy(aspect -> lookupPluginId(aspect, plugins)))
        .entrySet().stream().sorted(Entry.comparingByKey())
        .forEach(e -> {
          b.append("### Request Type: " + e.getKey() + "\n\n");
          StringBuilder reqDesc = new StringBuilder("#### Request properties\n\n");
          StringBuilder resDesc = new StringBuilder("#### Response properties\n\n");
          e.getValue().stream().forEach(aspect -> {
            if (RequestAspect.class.isAssignableFrom(aspect)) {
              RequestAspect req = getInstance(aspect);
              reqDesc.append("* Aspect `" + req.getName() + "` (" + aspect.getSimpleName() + ")\n");
              addProperties(reqDesc, aspect);
            } else { //response aspect
              ResponseAspect res = getInstance(aspect);
              resDesc.append("* Aspect `" + res.getName() + "` (" + aspect.getSimpleName() + ")\n");
              addProperties(resDesc, aspect);
            }
          });
          b.append(reqDesc);
          b.append(resDesc);
          b.append("\n");
        });

    System.out.println(Path.of(".").toAbsolutePath());
    IOUtils.write(b, new FileOutputStream("../docs/script-api.md"));
  }

  @SneakyThrows
  private static void addProperties(StringBuilder reqDesc, Class<?> aspect) {
//    List.of(aspect.getFields())
//        .forEach(p -> reqDesc.append("  * " + p.getName() + "\n"));
    List.of(Introspector.getBeanInfo(aspect).getPropertyDescriptors())
        .stream()
        .filter(f -> !List.of("class", "dirty", "onDirtyChange", "onInvalidate").contains(f.getName()))
        .forEach(p -> reqDesc.append("  * " + p.getName() + " (" + p.getPropertyType().getSimpleName() + ")" + "\n"));
  }

  @SneakyThrows
  private static <T> T getInstance(Class<?> aspect) {
    try {
      return (T) aspect.getConstructor().newInstance();
    } catch (NoSuchMethodException e) { //no no-args constructor found
      //just create a new instance with nulls as arg
      Constructor<?> constructor = aspect.getConstructors()[0];
      Object[] args = IntStream.range(0, constructor.getParameterCount())
          .mapToObj(x -> null)
          .toArray();
      return (T) constructor.newInstance(args);
    }
  }

  private String lookupPluginId(Class<?> aspectClass, Set<Class<?>> plugins) {
    for (Class<?> plugin : plugins) {
      if (aspectClass.getPackageName().startsWith(plugin.getPackageName())) {
        //try to guess package name:
        if (RequestTypePlugin.class.isAssignableFrom(plugin)){
          RequestTypePlugin pluginInstance = getInstance(plugin);
          return pluginInstance.getRequestType();
        } else {
          return "defined at `" + plugin.getSimpleName() + "`";
        }
      }
    }
    log.error("Could not find plugin class for: {}", aspectClass);
    return "unknown";
  }
}
