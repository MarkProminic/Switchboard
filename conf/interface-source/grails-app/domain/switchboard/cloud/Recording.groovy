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

package switchboard.cloud

import grails.gorm.DetachedCriteria
import grails.util.Environment
import security.Agent

class Recording {

    String source
    String destination
    String callerID
    String accountCode
    String uniqueId
    String linkedId
    String disposition
    String recordingFile

    int duration
    int billsec

    Date start

    transient ContactService
    transient ConfigLineService

    static transients = ['ContactService', 'ConfigLineService']

    static mapping = {
            table name: 'cdr'

        version false

        sort "start"
        order "desc"

        source column: 'src'
        destination column: 'dst'
        callerID column: 'clid'
        uniqueId column: 'uniqueid'
        linkedId column: 'linkedid'
    }

    static constraints = {}

    static DetachedCriteria<Recording> recordingsView = where {
        destination != '2200' && uniqueId == linkedId && disposition == 'ANSWERED'
    }

    String getAudioSourcePath(){
        ConfigLineService.getConfigValue("recording", "base_directory") + "/" + "${uniqueId}.wav"
    }

    String getSourceName(){
        return lookupNumber(source)
    }

    String getDestName(){
        return lookupNumber(destination)
    }

    private String lookupNumber(String number){
        if(number) {
            if(number.matches(/[0-9+]/)){
                if (number.length() < 6) {
                    Extension ext = Extension.findByNumber(number)

                    if (ext) {
                        return ext.agent ? ext.agent.name : number
                    }

                    Long agentId = Long.parseLong(number.substring(number.length() - 3))
                    Agent agent = Agent.findById(agentId)

                    if (agent) {
                        return agent.name
                    }
                }
            }
            else {
                return number
            }

            InboundRoute route = InboundRoute.findByDidNumber(number)

            if (route) {
                return route.destinationName
            }
        }

//        String contactName = ProminicContactService.findNameByNumber(number)
//
//        return contactName != 'Unknown' ? contactName : number
        return number
    }

    String getDurationString(){
        def hours = duration / 3600 as Integer
        def minutes = ((duration / 60) as Integer)% 60
        def seconds = duration % 60

        if(hours < 10){ hours = "0$hours"}
        if(minutes < 10){ minutes = "0$minutes" }
        if(seconds < 10){ seconds = "0$seconds" }

        String durationString = "$hours:$minutes:$seconds"

        return durationString
    }

    String getStartString(){
        start?.format("EEE MMM dd yyyy hh:mm a")
    }

    String toString(){
        "$source -> $destination ($uniqueId)"
    }
}
