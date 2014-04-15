// Console appender
appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) { pattern = "%date{HH:mm:ss.SSS} %level ['%thread' %file:%line] - %msg%n" }
}

// Every test has the same logging settings
root(INFO, ["STDOUT"])
