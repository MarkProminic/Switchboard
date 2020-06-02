/*
 * Developed by Luis Alcantara
 *
 * Copyright (C) 2016-2019 Prominic.NET, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 *
 * As a special exception, the copyright holders give permission to link the
 * code of portions of this program with the OpenSSL library under certain
 * conditions as described in each individual source file and distribute
 * linked combinations including the program with the OpenSSL library. You
 * must comply with the Server Side Public License in all respects for
 * all of the code used other than as permitted herein. If you modify file(s)
 * with this exception, you may extend this exception to your version of the
 * file(s), but you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version. If you delete this
 * exception statement from all source files in the program, then also delete
 * it in the license file.
 */

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import grails.util.Environment

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d [%logger{0}] %level - %msg%n"
    }
}

appender("RollingFileAppender", RollingFileAppender) {
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "/var/log/phoneinterface/phoneinterface.%d{yyyy-MM-dd}.log"
        maxHistory = 14
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%d [%logger{0}] %level - %msg%n"
    }
    append = true
}

if(Environment.current in [Environment.DEVELOPMENT, Environment.CUSTOM] ){
    logger("switchboard.cloud", DEBUG, ['STDOUT', "RollingFileAppender"], false)
    logger("security", DEBUG, ['STDOUT', "RollingFileAppender"], false)
    logger("voicemail", DEBUG, ['STDOUT', "RollingFileAppender"], false)
    logger("dialer", DEBUG, ['STDOUT', "RollingFileAppender"], false)
//    logger("grails.plugin.springsecurity", DEBUG, ['STDOUT'], false)
}
else if(Environment.current == Environment.PRODUCTION){
    logger("switchboard.cloud", INFO, ['STDOUT', "RollingFileAppender"], false)
    logger("security", INFO, ['STDOUT', "RollingFileAppender"], false)
    logger("voicemail", INFO, ['STDOUT', "RollingFileAppender"], false)
    logger("dialer", INFO, ['STDOUT', "RollingFileAppender"], false)
}

root(ERROR, ['STDOUT'])
