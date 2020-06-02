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

package voicemail

import groovy.transform.ToString

import java.sql.Timestamp

@ToString
class Message {

    transient ConfigLineService

    Long agentId
    String callerId
    String name
    int duration
    Timestamp createdOn

    String location = 'INBOX'

    static transients = ['ConfigLineService']

    static mapping = {
        version false

        sort "createdOn"
        order "desc"

//        datasource 'voicemail'
//        table name: "voicemail"//, schema: 'interface'
    }

    static constraints = {
        createdOn unique: 'agentId'
        location inList: ['INBOX', 'Old', 'DELETED']
    }

    String getFormattedCreatedOn(){
         createdOn?.format("MM/dd/yy hh:mm a")
    }

    String getCallerName(){
        if(callerId.contains("<") && callerId.contains(" >")) {
            def number = callerId.substring(callerId.indexOf("<") + 1, callerId.indexOf(" >"))

            if (callerId.matches(/"[A-Z|a-z|\s]*" <[0-9]*>/)) {
                def name = callerId.substring(callerId.indexOf("\"") + 1, callerId.lastIndexOf("\""))

                return (name == 'Unknown') ? number : name
            }
            else return number
        }

        callerId


    }

    String getAudioSourcePath(){
        ConfigLineService.getConfigValue("voicemail", "base_directory") + "/$agentId/$location/${name}.wav"
    }

    String getInfoSourcePath(){
        ConfigLineService.getConfigValue("voicemail", "base_directory") + "/$agentId/$location/${name}.txt"
    }

    boolean getIsOld(){ location == 'Old'}

    boolean getIsNew(){ location == 'INBOX' }

    boolean getIsDeleted(){ location == 'DELETED' }
}
