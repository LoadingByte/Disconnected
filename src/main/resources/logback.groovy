/*
 * The log name is the name of the application part that currently logs.
 * The default name is "launcher". It is used when the application jar is ran and the launcher is started.
 * Afterwards, the launcher sets the "logName" system property to the name of the application.
 */
def logNameDefault = "launcher"
def logName = System.getProperty("logName", logNameDefault)

// Console appender
appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) { pattern = "<$logName> %-22(%date{\"HH:mm:ss,SSS\"} [%thread]) %-5level %-31logger{32} - %msg%n" }
}

// Log file appender
appender("FILE", FileAppender) {
    file = "${logName}.log"
    encoder(PatternLayoutEncoder) { pattern = "%-33(%date [%thread]) %-5level %-31logger{32} - %msg%n" }
}

// Every part of the application (launcher and main application) uses the same settings
root(INFO, ["STDOUT", "FILE"])
