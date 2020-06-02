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

class Queue {

    String name
    String musiconhold = "Prominic"
    String announce = "no"
    String context = "queues"
    int    timeout = 22
    String ringinuse = 'no'
    String setinterfacevar = 'yes'
    String setqueuevar = 'yes'
    String setqueueentryvar = 'yes'
    String monitorFormat = "wav"
    String membermacro
    String membergosub
    String queueYouarenext = "silence/1"
    String queueThereare = "silence/1"
    String queueCallswaiting = "silence/1"
    String queueQuantity1 = "silence/1"
    String queueQuantity2 = "silence/1"
    String queueHoldtime = "silence/1"
    String queueMinutes = "silence/1"
    String queueSeconds = "silence/1"
    String queueThankyou = "silence/1"
    String queueCallerannounce = "silence/1"
    String queueReporthold = "silence/1"
    int    announceFrequency = 30
    String announceToFirstUser = 'no'
    int    minAnnounceFrequency = 15
    int    announceRoundSeconds = 0
    String announceHoldtime = "no"
    String announcePosition = "no"
    int    announcePositionLimit
    String periodicAnnounce  = "silence/1"
    int    periodicAnnounceFrequency = 0
    String relativePeriodicAnnounce = 'yes'
    String randomPeriodicAnnounce = 'no'
    int    retry = 12
    int    wrapuptime = 10
    int    penaltymemberslimit
    String autofill = 'yes'
    String monitorType = "Mixmonitor"
    String autopause  = 'no'
    int    autopausedelay = 120
    String autopausebusy = 'no'
    String autopauseunavail = 'no'
    int    maxlen = 0
    int    servicelevel = 60
    String strategy = "fewestcalls"
    String joinempty = "yes"
    String leavewhenempty = "no"
    String reportholdtime = 'no'
    int    memberdelay = 0
    int    weight = 0
    String timeoutrestart = 'yes'
    String defaultrule
    String timeoutpriority = "conf"
    String sharedLastcall = "no"
    String eventmemberstatus = 'yes'
    String logMembernameAsAgent = 'yes'
    String checkStateUnkown = 'yes'
    String eventwhencalled = 'vars'
    String updatecdr='yes'

    static yes_no_values= ['yes', 'no']

    static queue_strategy_values = ['ringall', 'fewestcalls', 'random', 'rrmemory', 'linear', 'wrandom', 'rrordered']

    static queue_autopause_values = ['yes', 'no', 'all']

    static event_when_called_values = ['yes', 'no', 'vars']

//    static hasMany = [members: QueueMember]

    static constraints = {
        name nullable: false
        monitorFormat maxlen: 8

        ringinuse inList: this.yes_no_values
        setinterfacevar inList: this.yes_no_values
        setqueuevar inList: this.yes_no_values
        setqueueentryvar inList: this.yes_no_values
        announceToFirstUser inList: this.yes_no_values
        relativePeriodicAnnounce inList: this.yes_no_values
        randomPeriodicAnnounce inList: this.yes_no_values
        autofill inList: this.yes_no_values
        autopausebusy inList: this.yes_no_values
        autopauseunavail inList: this.yes_no_values
        reportholdtime inList: this.yes_no_values
        timeoutrestart inList: this.yes_no_values
        eventmemberstatus inList: this.yes_no_values
        sharedLastcall inList: this.yes_no_values
        logMembernameAsAgent inList: this.yes_no_values
        checkStateUnkown inList: this.yes_no_values
        updatecdr inList: this.yes_no_values

        strategy inList: this.queue_strategy_values

        autopause inList: this.queue_autopause_values

        eventwhencalled inList: this.event_when_called_values
    }

    static mapping = {
        version false
        table name: 'queues'//, schema: 'asteriskinfo'
        id(name: 'name', generator:'assigned')
    }

    String toString(){
        return "$name"
    }

    List<QueueMember> getMembers(){
        return QueueMember.findAllByQueueName(name, [sort: 'membername'])
    }
}
