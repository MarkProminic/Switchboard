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

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false, ignoreNulls = true)
class CallRecord {

    transient ContactService

    // properties taken from corresponding AsteriskCall
    Long startTime

    String direction
    String uniqueId
    String callerName = ''
    String callerNumber = ''
    String calleeName = ''
    String calleeNumber = ''
    String dialedNumber
    String status
    String recordingFileName

    // other properties
    String contactId
    String customerId  = ''
    String customerSatisfaction
    String customerAccountName = ''
    String notes

    Long reportSubmitted
    boolean reportCompleted = false

    int duration = -1
    int billableDuration
    int queueRetries

    static adminRecordList = CallRecord.find {
        calleeName != 'Copia Facts'
        callerName != 'Copia Facts'
    }

    static hasMany = [agents: String, queues: String, memberRecords: ConferenceRoomMemberRecord]

    static transients = ['ContactService', 'customerName', 'customerNumber', 'agentName', 'agentNumber']

    static mapping = {
        version false
        id name: "uniqueId", generator: "assigned"
        uniqueId unique: true
        notes type: 'text'
    }

    static constraints = { }

    String getFormattedDurationString(){
        int hours = 0
        int mins = 0
        int secs = 0

        if(duration >= 0) {
            hours = duration / 3600 as Integer
            mins = ((duration / 60) as Integer) % 60
            secs = duration % 60
        }

        "${hours <= 9 ? '0':''}$hours hrs ${mins <= 9 ? '0':''}$mins mins ${secs <= 9 ? '0':''}$secs secs"

    }

    String getCompactDurationString(){
        int hours = 0
        int mins = 0
        int secs = 0

        if(duration >= 0) {
            hours = duration / 3600 as Integer
            mins = ((duration / 60) as Integer) % 60
            secs = duration % 60
        }

        "${hours <= 9 ? '0':''}$hours:${mins <= 9 ? '0':''}$mins:${secs <= 9 ? '0':''}$secs"
    }

    int calculateBillableDuration(){
        (duration / 60) as int
    }

    void setCustomerName(String name){
        if(direction == 'inbound') {
            callerName = name
        }
        else if(direction == 'outbound') {
            calleeName = name
        }
    }

    void setCustomerNumber(String number){
        if(direction == 'inbound') {
            callerNumber = number
        }
        else if(direction == 'outbound') {
            calleeNumber = number
        }
    }

    void setAgentName(String name){
        if(direction == 'inbound') {
            calleeName = name
        }
        else if(direction == 'outbound') {
            callerName = name
        }
    }

    String getCustomerName(){
        direction == 'inbound' ? callerName : calleeName
    }

    String getCustomerNumber(){
        direction == 'inbound' ? callerNumber : calleeNumber
    }

    String getAgentName(){
        direction == 'inbound' ? calleeName : callerName
    }

    String getAgentNumber(){
        String extenNumber = direction == 'inbound' ? calleeNumber : callerNumber

        Extension.findByNumber(extenNumber)?.name ?: extenNumber
    }

    String getStartDateString(){
        new Date(startTime).format("EEE MMM dd hh:mm aa yyyy")
    }

    String getReportSubmittedDateString(){
        if(reportSubmitted) {
            return new Date(reportSubmitted).format("EEE MMM dd hh:mm aa yyyy")
        }

        return "Pending"
    }

//    boolean getReportCompleted(){
//        (notes && customerSatisfaction && customerName)
//    }

}
