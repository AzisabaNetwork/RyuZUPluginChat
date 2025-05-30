package net.azisaba.ryuzupluginchat.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestStringUtil {
    @ParameterizedTest(name = "[{index}] repeat(\"{0}\", {1}) => \"{2}\"")
    @MethodSource("provideRepeatCases")
    void testRepeat(String input, int count, String expected) {
        assertEquals(expected, StringUtil.repeat(input, count));
    }

    static Stream<Arguments> provideRepeatCases() {
        return Stream.of(
                Arguments.of("a", 3, "aaa"),
                Arguments.of("abc", 2, "abcabc"),
                Arguments.of("", 5, ""),               // 空文字の繰り返し
                Arguments.of("x", 0, ""),              // 0回の繰り返し
                Arguments.of("あ", 3, "あああ"),        // Unicode 文字
                Arguments.of(" ", 4, "    "),           // 空白文字
                Arguments.of("ab", 1, "ab"),            // 1回繰り返し
                Arguments.of("xy", 5, "xyxyxyxyxy")     // 複数回・複数文字
        );
    }
}
