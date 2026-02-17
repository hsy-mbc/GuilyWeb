package org.hsy.spring.common.formatter;

import java.time.format.DateTimeFormatter;

public class DateTimeFormatters {

    private DateTimeFormatters() {}

    public static final DateTimeFormatter DEFAULT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static final DateTimeFormatter FULL =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
}
