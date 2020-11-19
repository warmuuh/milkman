package milkman.templater;

import milkman.domain.Environment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentTemplaterTest {


    @Test
    void shouldReplaceLiterally() {
        var testEnv = new Environment("test");
        testEnv.setOrAdd("testKey", "testValue$d2");
        var templater = new EnvironmentTemplater(Optional.of(testEnv), List.of(), null);
        var result = templater.replaceTags("{{testKey}}");
        assertThat(result).isEqualTo("testValue$d2");
    }

}