package milkman.templater;

import milkman.ui.plugin.Templater;
import milkman.ui.plugin.TemplateParameterResolverPlugin;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrefixedTemplaterTest {

    TemplateParameterResolverPlugin prefixTempl = new TemplateParameterResolverPlugin() {
        @Override
        public String getPrefix() {
            return "LOW";
        }

        @Override
        public String lookupValue(String input) {
            return input.toLowerCase();
        }
    };

    @ParameterizedTest
    @CsvSource({
            "AaBbCc,-",
            "LOW:AaBbCc,aabbcc",
            ":AaBbCc,-",
            "unknown:AaBbCc,-",
            "unknown:,-",
            "LOW:,-",
    })
    void shouldUseDefaultOnNoPrefix(String input, String output){
        PrefixedTemplaterResolver sut = new PrefixedTemplaterResolver(List.of(prefixTempl));
        assertThat(sut.resolveViaPluginTemplater(input).orElse("-")).isEqualTo(output);
    }

}