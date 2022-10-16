package net.azisaba.ryuzupluginchat.localization;

import org.intellij.lang.annotations.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// OK:
// - a.b.c.d
// Not OK:
// - a.b.c.
// - .
// - AnythingOtherThanLowercaseAlphanumericAndUnderscoreAndPeriod
@Pattern("^(?:[a-z0-9_]+\\.?)+(?<!\\.)$")
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface TranslatableKey {
}
