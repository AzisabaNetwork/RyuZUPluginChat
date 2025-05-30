package net.azisaba.ryuzupluginchat.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static net.azisaba.ryuzupluginchat.util.StringUtil.repeat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestArgsConnectUtils {
    @ParameterizedTest(name = "[{index}] {0} => \"{1}\"")
    @MethodSource("provideConnectTestCases")
    @DisplayName("connect Test Cases")
    void testConnect(String[] input, String expected) {
        assertEquals(expected, ArgsConnectUtils.connect(input));
    }

    public static Stream<Arguments> provideConnectTestCases() {
        return Stream.of(
                Arguments.of(
                        new String[]{"Hello", "world"}, "Hello world"),
                Arguments.of(
                        new String[]{"The", "quick", "brown", "fox"}, "The quick brown fox"),
                Arguments.of(
                        new String[]{}, ""),
                Arguments.of(
                        new String[]{"single"}, "single"),
                Arguments.of(
                        new String[]{"a", "", "b"}, "a  b"),
                Arguments.of(
                        new String[]{" a", "b ", " c "}, " a b   c "),
                Arguments.of(
                        new String[]{"こんにちは", "世界"}, "こんにちは 世界"),
                Arguments.of(
                        new String[]{"123", "456"}, "123 456"),
                Arguments.of(
                        new String[]{"@#", "$%^", "&*()"}, "@# $%^ &*()"),
                Arguments.of(
                        new String[]{repeat("a", 1000), repeat("b", 1000)},
                        repeat("a", 1000) + " " + repeat("b", 1000)
                )
        );
    }
}
