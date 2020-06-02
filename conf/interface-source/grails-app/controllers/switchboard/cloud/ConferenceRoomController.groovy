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

import grails.rest.RestfulController
import org.springframework.http.HttpStatus
import security.Agent

class ConferenceRoomController extends RestfulController {

    static responseFormats = ['json', 'xml']

    ConferenceRoomController(){
        super(ConferenceRoom)
    }

    def ConferenceRoomService
    def springSecurityService
    def ConfigLineService
    def CallService

    def index() { }

    def mute(Long member, String room){
        ConferenceRoomService.mute(ConferenceRoomMember.findById(member), ConferenceRoom.findByNumber(room))
    }

    def unmute(Long member, String room){
        ConferenceRoomService.unmute(ConferenceRoomMember.findById(member), ConferenceRoom.findByNumber(room))
    }

    def kick(Long member, String room){
        ConferenceRoomService.kick(ConferenceRoomMember.findById(member), ConferenceRoom.findByNumber(room))
    }

    def kickAll(String roomNumber){
        ConferenceRoom room = ConferenceRoom.findByNumber(roomNumber)
        ConferenceRoomService.kickAll(room)

        render status: 200
    }

    def join(String roomNumber){
        Agent user = getAuthenticatedUser() as Agent
        boolean success = ConferenceRoomService.addAgentToConference(roomNumber, user, user.primaryExtension)

        render status: success ? 200: 400
    }

    def addAgent(Long agentId, String location){
        log.debug "Adding agent $agentId @ location '$location'"

        Agent agent = Agent.findById(agentId)

        Extension extension = (location == "primary") ? agent.primaryExtension : Extension.findById(Long.parseLong(location))

        Agent user = getAuthenticatedUser() as Agent

        boolean success = false

        def userMemberList = ConferenceRoomMember.findAllByNameAndConferenceIsNotNull(user.name)

        if(userMemberList) {
            if (userMemberList.size() == 1) {
                log.debug "Found Conference Member ${userMemberList[0]} "

                ConferenceRoomMember userMember = userMemberList.get(0)
                ConferenceRoomMember agentMember = ConferenceRoomMember.findByConferenceAndName(userMember.conference, agent.name)

                if(!agentMember) {
                    log.info "Adding $agent.name to Conference $userMember.conference.name"
                    success = ConferenceRoomService.addAgentToConference(userMember.conference.number, agent, extension)
                }
                else {
                    log.info "Both $agent.name and $user.name are already in conference $userMember.conference.name, ignoring action"
                    success = true
                }
            }
            else {
                //TODO: Implement a way for the user to specify the conference room they want to invite the Agent to if they are a member of more than one
                log.error "Found ${userMemberList.size()} Conference Room Members for user $user.name"
                success = false
            }
        }
        else {
            AsteriskCall activeCall = AsteriskCall.findByPrimaryOwnerAndState(user.name, 'active')

            if(!activeCall){
                activeCall = AsteriskCall.findBySecondaryOwnerAndState(user.name, 'active')
            }

            if (activeCall) {
                log.info "Found active call $activeCall, adding $agentId @ location '$location'"

                String roomNumber = ConferenceRoomService.findNextAvailableRoom()

                if(roomNumber) {
                    success = ConferenceRoomService.createConferenceRoom(roomNumber, activeCall, agent, extension)

                    if (success) {
                        activeCall.state = 'conference'
                        activeCall.save(flush: true)

                        agent.callState = 'oncall'
                        agent.save()
                    }
                }
                else {
                    log.error "Adding Agent to call failed, there are no available conference rooms."
                    return
                }
            }
            else { // User is neither in a conference room, nor on a asteriskCall. This action was called in error
                log.error "Error: ${user.name} is neither in a conference room, nor on a asteriskCall; this action was called in error."
            }
        }

        render status: success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR
    }

    def addRemoteNumber(String remoteNumber){
        Agent user = (Agent) getAuthenticatedUser()

        boolean success = false

        def memberList = ConferenceRoomMember.findAllByNameAndConferenceIsNotNull(user.name)
        String conferenceNumber

        String parsedNumber = CallService.parsePhoneNumber(remoteNumber)
        // From here on the parsedNumber should be used, with remoteNumber only used for display/logging purposes

        log.debug "Input '$remoteNumber' parsed as '$parsedNumber'"

        if(memberList){
            if(memberList.size() == 1){
                conferenceNumber = memberList.get(0).conference.number

                log.info "$user.name is in a conference already, adding '$remoteNumber' to conference room $conferenceNumber."

                success = ConferenceRoomService.addRemoteNumberToConference(conferenceNumber, parsedNumber, user)
            }
            else {
                log.error "Found ${memberList.size()} members for Agent '${user.name}'"
            }
        }
        else {
            List<AsteriskCall> ownedCalls = AsteriskCall.findAllByPrimaryOwnerOrSecondaryOwner(user.name, user.name)
            AsteriskCall activeCall = ownedCalls.find { AsteriskCall ownedCall ->
                ownedCall.state == 'active'
            }

            def roomList = ConferenceRoom.listOrderByNumber(order: 'desc')

            if (activeCall) {
                int nextRoom = roomList ? (Integer.parseInt(roomList[0].number, 10) + 1) : 1000

                log.info "$user.name is actively on call $activeCall, transferring all parties to conference room $nextRoom."

                success = ConferenceRoomService.createConferenceRoom(nextRoom.toString(), activeCall, parsedNumber, user)

            }
            else { // User is neither in a conference room, nor on a call. This action was called in error
                log.error "Error: ${user.name} is neither in a conference room, nor on a call; this action was called in error."
            }
        }

        render status: success ? 200 : 400
    }

    def refreshRoomList(){
        Agent user = getAuthenticatedUser() as Agent

        def roomList = ConferenceRoom.listOrderByNumber()

        List<ConferenceRoom> visibleRoomList = []

        roomList.each { ConferenceRoom room ->
            ConferenceRoomMember userMember = ConferenceRoomMember.findByConferenceAndName(room, user.name)
            if((!room.hidden || userMember) && room.members.size() > 0){
                // room should be visible to the user iff the room is not hidden or the user is in that room, but only if the room has members
                visibleRoomList.add(room)
            }
        }

        respond confRooms: visibleRoomList, user: user
    }

    def toggleVisibility(String roomNumber){
        ConferenceRoom room = ConferenceRoom.findByNumber(roomNumber)
        room.hidden = !room.hidden

        room.save()

        render status: 200
    }
}