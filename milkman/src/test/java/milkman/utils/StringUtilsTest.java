package milkman.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {


    @ParameterizedTest
    @CsvSource({
            "test, true",
            "tt, true",
            "ttt, false",
            "es, true",
            "se, false",
            ", true"
    })
    public void test(String value, boolean expected){
        assertThat(StringUtils.containsLettersInOrder("test", value)).isEqualTo(expected);
    }

}