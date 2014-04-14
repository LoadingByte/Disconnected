/*
 * The log name is the name of the application part that currently logs.
 * The default name is "launcher". It is used when the application jar is ran and the launcher is started.
 * Afterwards, the launcher sets the "logName" system property to the name of the application.
 */
def logNameDefault = "launcher"
def logName = System.getProperty("logName", logNameDefault)

// Console appender
appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) { pattern = "<$logName> %date{HH:mm:ss.SSS} %level ['%thread' %file:%line] - %msg%n" }
}

// Log file appender
appender("FILE", FileAppender) {
    file = "${logName}.log"
    encoder(PatternLayoutEncoder) { pattern = "%date %level ['%thread' %file:%line] - %msg%n" }
}

// Every part of the application (launcher and main application) uses the same settings
root(INFO, ["STDOUT", "FILE"])
