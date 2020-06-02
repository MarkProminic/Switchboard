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

import grails.gorm.transactions.Transactional
import org.asteriskjava.manager.action.*
import org.asteriskjava.manager.response.ManagerResponse
import security.Agent

@Transactional
class ConferenceRoomService {

    def springSecurityService
    def CallService
    def ConfigLineService
    def ContactService

    def createConferenceRoom(String roomNumber, AsteriskCall call, Agent agent, Extension extension){
        def nonLocalChannels = call.channels.findAll { Channel c ->
            c.status != 'local'
        }

        if(nonLocalChannels.size() == 2) {

            boolean agentAdded = addAgentToConference(roomNumber, agent, extension)

            if(agentAdded) {
                CallService.sendAction(new RedirectAction(nonLocalChannels[0].channelId, nonLocalChannels[1].channelId, "agents", roomNumber, 1))
                return true
            }
            else{
                log.warn "Agent $agent did not join conference $roomNumber, cancelling transfer of ${call.primaryOwner ?: 'NOAGENT'}'s call"
                return false
            }
        } else {
            log.warn "Malformed call: $call; incorrect number of channels to create a Conference Room. Need at least two, had ${call.channels.size()}."
            return false
        }
    }

    def createConferenceRoom(String roomNumber, AsteriskCall call, String number, Agent user){
        def nonLocalChannels = call.channels.findAll { Channel c ->
            c.status != 'local'
        }

        if(nonLocalChannels.size() == 2) {
            boolean agentAdded = addRemoteNumberToConference(roomNumber, number, user)

            if(agentAdded) {
                CallService.sendAction(new RedirectAction(nonLocalChannels[0].channelId, nonLocalChannels[1].channelId,
                        "agents", roomNumber, 1))
                return true
            }
            else {
                return false
            }
        } else {
            log.warn "Malformed call: $call; incorrect number of channels to create a Conference Room. Need two, had ${call.channels.size()}."
            return false
        }
    }

    boolean addAgentToConference(String roomNumber, Agent agent, Extension extension){
        String channelName = "Local/$extension.number@agents"
        String internationalCID = ConfigLineService.getConfigValue("dialer", "international_cid")

        OriginateAction originateAction = new OriginateAction(channel: channelName, callerId: "\"Prominic-Conf\" <$internationalCID>",
                context: "agents", exten: roomNumber, priority: 1)
        originateAction.variables = ["MEETME_ROOMNUM": roomNumber, "conferenceCalleeName": agent.name]

        ManagerResponse result = CallService.sendAction(originateAction)

        log.debug "Result: $result.message"

        return result.message.contains("Originate successfully queued")
        
    }

    boolean addRemoteNumberToConference(String roomNumber, String remoteNumber, Agent user){
        String conferenceOutboundContext = ConfigLineService.getConfigValue("dialer", "conference_outbound_context")

        String channelName = "Local/$remoteNumber@$conferenceOutboundContext"
        String internationalCID = ConfigLineService.getConfigValue("dialer", "international_cid")

        String calleeName = ContactService.findNameByNumber(remoteNumber)

        OriginateAction originateAction = new OriginateAction(channel: channelName, callerId: "\"Prominic-Conf\" <$internationalCID>",
                                            context: "agents", exten: roomNumber, priority: 1)
        originateAction.variables = ["MEETME_ROOMNUM": roomNumber, "conferenceCalleeName": calleeName,
                                     "conferenceLinkedid": user.activeCall.uniqueId, "conferenceLeader": user.id.toString()]

        ManagerResponse result = CallService.sendAction(originateAction)

        log.debug "Result: $result.message"

        return result.message.contains("Originate successfully queued")
    }

    def mergeCalls(AsteriskCall activeCall, AsteriskCall parkedCall){
        String roomNumber = findNextAvailableRoom()
        def response
        if(roomNumber) {
            response = CallService.sendAction(new RedirectAction(parkedCall.channels[0].channelId, "agents", roomNumber, 1))
            if (response) {
                response = CallService.sendAction(new RedirectAction(activeCall.channels[0].channelId, activeCall.channels[1].channelId, "agents", roomNumber, 1))
            }
        }
        else {
            log.error "Merge failed, there are no available conference rooms."
        }

        response
    }

    def addParkedCallToConference(AsteriskCall parkedCall, String roomNumber){
        ConferenceRoom room = ConferenceRoom.findByNumber("1000")
        room
    }

    def mute(ConferenceRoomMember member, ConferenceRoom room){
        def result
        try {
            result = CallService.sendAction( new ConfbridgeMuteAction( room.number, member.channel ) )
            member.muted = true
            member.save(flush: true)
        } catch (Exception e) {
            log.error "Mute threw ${e.class}: ${e.message}"
        }

        result
    }

    def unmute(ConferenceRoomMember member, ConferenceRoom room){
        def result
        try {
            result = CallService.sendAction( new ConfbridgeUnmuteAction( room.number, member.channel ) )
            member.muted = false
            member.save(flush: true)
        } catch (Exception e) {
            log.error "Unmute threw ${e.class}: ${e.message}"
        }

        result
    }

    def kick(ConferenceRoomMember member, ConferenceRoom room){
        def result

        try {
            result = CallService.sendAction(new ConfbridgeKickAction(room.number, member.channel) )
            room.removeFromMembers(member)

            room.save(flush: true)
        } catch (Exception e) {
            log.error "Kick threw ${e.class}: ${e.message}"
        }

        result
    }

    def kickAll(ConferenceRoom room){
        def memberList = ConferenceRoomMember.findAllByConference(room)

        try {
            memberList.each { ConferenceRoomMember member ->
                CallService.sendAction( new ConfbridgeKickAction( room.number, member.channel ) )
                room.removeFromMembers(member)
            }

            room.save()
        } catch (Exception e) {
            log.error "Kick All threw ${e.class}: ${e.message}"
        }
    }

    String findNextAvailableRoom(){
        def roomNumberList = ConferenceRoom.executeQuery("SELECT c.number from ConferenceRoom c")
        Set<Integer> roomIntegers = []
        List<Integer> numList = 1000..1094

        roomNumberList.each { String roomNumber ->
            roomIntegers << Integer.parseInt(roomNumber)
        }

        def availableSet = numList.toSet() - roomIntegers

        availableSet.min() as String
    }
}
