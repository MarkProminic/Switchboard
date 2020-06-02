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

import security.Agent

class AsteriskCall {

    transient springSecurityService
    transient ContactService

    String callerNumber
    String callerName
    String calleeNumber
    String calleeName
    String uniqueId
    String linkedId
    String direction
    String state = 'active'
    String primaryOwner = ''
    String secondaryOwner = ''
    String parkedBy = ''
    String parkingSpace = ''
    String queue = ''
    String dialedNumber = ''

    Long startTime = new Date().time
    Long lastParked = new Date().time

    boolean verified = false

    static hasMany = [channels: Channel, custIds: String]

    static transients = ['springSecurityService', 'ContactService']

    static constraints = {
        state inList: ['hold', 'parked', 'active', 'conference']
        direction inList: ['inbound', 'outbound', 'internal', 'conference']
        startTime nullable: false
        verified nullable: false
    }

    static mapping = {
        version false
        id name: 'uniqueId', generator: 'assigned'
        channels cascade: 'all-delete-orphan'
        autowire true
    }

    String toString(){
        uniqueId
    }

    int getDuration(){
        if(state == 'parked'){
            return(new Date().getTime() - lastParked) / 1000
        }
        else {
           return (new Date().getTime() - startTime) / 1000
        }
    }

    String getDurationString(){
        int seconds = getDuration()
        int mins = seconds / 60
        int hours = mins / 60

        String durationString = ''

        if(hours<10){
            durationString ='0'
        }
        durationString += "$hours:"

        if((mins%60)<10){
            durationString +='0'
        }
        durationString += "${mins%60}:"

        if((seconds%60)<10){
            durationString +='0'
        }
        durationString += "${seconds%60}"

        durationString
    }

    String getDisplayName(){
        if(direction == 'inbound'){
            return callerName
        } else if(direction == 'outbound'){
            return calleeName
        } else { //internal call
            Agent user = springSecurityService.getCurrentUser()
            return (user.name == primaryOwner)? calleeName: callerName
        }
    }

    String getDisplayNumber(){
        if(direction == 'inbound'){
            return callerNumber
        } else if(direction == 'outbound'){
            return calleeNumber
        }  else { //internal call
            Agent user = springSecurityService.getCurrentUser()
            if(user.name == primaryOwner){
                def calleeExtensions = Agent.findByName(calleeName)?.extensions
                Extension location = null
                if(calleeExtensions) {
                    location = calleeExtensions.find { Extension extension ->
                        extension.phoneNumber == calleeNumber
                    }
                }

                return location ? location.displayName : calleeNumber
            } else {
                return callerNumber
            }
        }
    }

//    String getUserDisplayName(){
//
//    }
//
//    String getUserDisplayNumber(){
//        Agent user = springSecurityService.getCurrentUser()
//        if(user.name == primaryOwner){
//            def calleeExtensions = Agent.findByName(calleeName)?.extensions
//            Extension location = null
//            if(calleeExtensions) {
//                location = calleeExtensions.find { Extension extension ->
//                    extension.phoneNumber == calleeNumber
//                }
//            }
//
//            return location ? location.displayName : calleeNumber
//        } else {
//            return callerNumber
//        }
//    }

    String getDisplayId(){
        uniqueId.substring(0, uniqueId.indexOf("."))
    }

    def findCustIds(){
        if(!custIds){
            String[] freshCustIds = []
            if(direction == 'inbound' && callerNumber){
                freshCustIds = ContactService.findAllCustIdsByNumber(callerNumber)
            }
            else if(direction == 'outbound' && calleeNumber) {
                freshCustIds = ContactService.findAllCustIdsByNumber(calleeNumber)
            }

            freshCustIds.each {
                this.addToCustIds(it)
            }
        }

        custIds
    }

    def beforeUpdate(){
        if(secondaryOwner == null){
            secondaryOwner = ''
        }

        if(primaryOwner == null){
            primaryOwner = ''
        }
    }
}
