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
import grails.util.Environment
import org.asteriskjava.manager.AuthenticationFailedException
import org.asteriskjava.manager.ManagerConnection
import org.asteriskjava.manager.ManagerConnectionFactory
import org.asteriskjava.manager.ManagerEventListener
import org.asteriskjava.manager.action.StatusAction
import org.asteriskjava.manager.event.*
import security.Agent

import java.util.concurrent.TimeoutException

@Transactional
class ManagerService implements ManagerEventListener{

    ManagerConnection managerConnection

    def ContactService
    def ConfigLineService
    def CallService
    def CallRecordService
    def springSecurityService

    boolean connected = false

    void connect() {

        Environment.executeForCurrentEnvironment {
            development {
                log.info "Connecting to the Asterisk instance configured in /etc/hosts"
            }

            production {
                log.info "Connecting to the Asterisk instance configured in /etc/hosts"
            }
        }

        if (!connected) {
            ManagerConnectionFactory factory

            if(managerConnection){
                managerConnection.logoff()
            }
            String host = ConfigLineService.getConfigValue("general", "asterisk_host")
            String managerUsername = ConfigLineService.getConfigValue("manager", "username")
            String managerPassword = ConfigLineService.getConfigValue("manager", "password")

            factory = new ManagerConnectionFactory(host, managerUsername, managerPassword)

            this.managerConnection = factory.createManagerConnection()

            managerConnection.addEventListener(this)

            try {
                // connect to Asterisk and log in
                managerConnection.login()

                managerConnection.sendAction(new StatusAction())

                log.info "Successfully connected to the Asterisk Server ($host)"
                connected = true

                return
            } catch (AuthenticationFailedException e) {
                log.error "Authentication failed, check Manager credentials: ${e.message}"
            } catch (TimeoutException e) {
                log.error "Connection to AMI timed out, check network: ${e.message}"
            } catch (IllegalStateException e) {
                log.error "Illegal state reached, unable to connect to manager: ${e.message}"
            } catch (IOException e) {
                log.error "IO error, unable to connect to manager: ${e.message}"
            } catch (Exception e) {
                e.printStackTrace()
            }

            connected = false
        }
    }

    void disconnect(){
        connected = false
        managerConnection.logoff()
    }

    @Override
    void onManagerEvent(ManagerEvent event) {

        try {
            if (event.class == NewChannelEvent) {
                event = event as NewChannelEvent

                log.debug "Call from ${event.callerIdNum} to ${event.exten}"

                String uniqueId = (event.uniqueId.contains("-")) ? event.uniqueId.substring(event.uniqueId.lastIndexOf("-") + 1) : event.uniqueId
                String linkedId = (event.linkedid.contains("-")) ? event.linkedid.substring(event.linkedid.lastIndexOf("-") + 1) : event.linkedid

                String direction = DetermineDirection(event)
                AsteriskCall linkedCall = AsteriskCall.findByUniqueId(linkedId)

                if (!linkedCall) {   //first channel created for a call
                    linkedCall = CreateCall(event, uniqueId, direction)

                    if(direction != 'internal') {
                        CallRecordService.create(linkedCall.properties)
                    }

                    log.info "Call '$uniqueId' created."
                }

                Channel channel = createChannel(event, linkedCall, linkedId, direction)

                linkedCall.addToChannels(channel).save()

                log.info "Channel $event.channel (UID: $uniqueId, LID:$linkedId) created."
            }
            else if (event.class == VarSetEvent) {
                event = event as VarSetEvent

                if (event.variable.startsWith('originated')) {
                    AsteriskCall originatedCall = AsteriskCall.findByUniqueId(event.linkedId)

                    if(originatedCall) {
                        switch (event.variable) {
                            case "originatedCalleeName":
                                originatedCall.calleeName = event.value ?: originatedCall.calleeName
                                Agent agent = Agent.findByName(event.value)
                                if (agent) {
                                    CallService.updateSecondaryOwner(originatedCall, event.value)
//                                    originatedCall.secondaryOwner = event.value
                                    log.info "Setting seconday owner of call '${originatedCall.uniqueId}' to '${event.value}'"
                                }
                                break
                            case "originatedCalleeNumber":
                                originatedCall.calleeNumber = event.value
                                break
                            case "originatedOwnerName":
                                CallService.updatePrimaryOwner(originatedCall, event.value)
                                originatedCall.callerName = event.value
                                Channel originatedChannel = originatedCall.channels?.find { Channel channel ->
                                    channel.channelId == ((VarSetEvent) event).channel
                                }
                                originatedChannel.ownerName = event.value
                                originatedChannel.save(flush: true, failOnError: true)
                                log.info "Setting ownership and caller name of call '${originatedCall.uniqueId}' to '${event.value}'"
                                break
                            default:
                                break
                        }
                        originatedCall.save(flush: true)
                    }
                }
                else if (event.variable == "conferenceCalleeName") {
                    log.info "VarSet for '${event.variable}' caught, creating/updating ConferenceRoomMember for ${event.channel}."

                    ConferenceRoomMember member = ConferenceRoomMember.findOrSaveByChannel(event.channel)

                    if(event.value) {
                        if (event.value.matches(/[0-9]+/)) {
                            member.name = ContactService.findNameByNumber(event.value)
                        }
                        else {
                            // CallerID provided an actual name, just use it
                            member.name = event.value
                        }
                    }
                    else {
                        member.name = 'Unknown Caller'
                    }

                    member.save()
                }
                else if (event.variable == "conferenceLinkedid"){
                    AsteriskCall baseCall = AsteriskCall.findByUniqueId(event.value)

                    CallRecord baseRecord = CallRecord.findByUniqueId(event.value)

                    if (baseCall.state != 'conference') {
                        // direction should no longer be inbound, nor outbound as conference calls are treated differently
                        // also the record will now inherit the direction of 'conference'
                        baseCall.direction = 'conference'
                        baseCall.state = 'conference'
                    }
                    AsteriskCall externalCall = AsteriskCall.findByUniqueId(event.uniqueId)
                    externalCall.linkedId = event.value

                    externalCall.save()
                    baseCall.save()

                    if (!baseRecord) {
                        CallRecordService.create(baseCall.properties)
                    }

                }
                else if (event.variable == "conferenceLeader"){
                    Agent leader = Agent.findById(event.value.toLong())
                    AsteriskCall leaderCall = leader.activeCall

                    if(leader){
                        if(leaderCall) {
                            CallRecord confRecord = CallRecord.findByUniqueId(leaderCall.uniqueId)

                            if (!confRecord) {
                                confRecord = CallRecordService.create(leaderCall.properties)
                            }

                            leader.addToPendingCallRecords(confRecord)

                            log.info "Assigning Call Record with UID '$confRecord.uniqueId' to Agent '$leader.name'"

                            leader.save()
                            confRecord.save()
                        }
                        else {
                            log.error "Conference leader with Agent ID '$event.value' has no active call, so the associated " +
                                    "Call Record cannot be retrieved and updated"
                        }
                    }
                    else {
                        log.error "Could not find Agent with ID '$event.value' to assign the conference record with UID '$event.uniqueId' to"
                    }
                }
                else if (event.variable == 'callername'){
                    AsteriskCall call = AsteriskCall.findByUniqueId(event.uniqueId)

                    if(call) {
                        if(event.value) {
                            log.info "Changing callerName for call ${call.uniqueId} from '${call.callerName}' to '${event.value}'."

                            call.callerName = event.value
                            call.save(flush: true)
                        }
                    } else {
                        log.warn "VarSet for ${event.variable} caught with UID '${event.uniqueId}' which is NOT a call."
                    }
                }
                else if (event.variable == "ExpectedQueue" || event.variable == "destName"){
                    AsteriskCall call = AsteriskCall.findByUniqueId(event.uniqueId)
                    if(call) {

                        Queue queue = Queue.findByName(event.value)
                        if(queue){
                            CallRecordService.update(call.uniqueId, ["queues": queue])
                        }
                        call.queue = event.value
                        call.save(flush: true)
                    }
                }
                else if (event.variable == 'VerifiedCaller'){
                    AsteriskCall call = AsteriskCall.findByUniqueId(event.linkedId)
                    call.verified = event.value as Boolean
                }
            }
            else if (event.class == HangupEvent) {
                event = event as HangupEvent

                log.info "Hangup event caught, deleting channel ${event.channel}"

                Channel deadChannel = Channel.findByChannelId(event.channel)
                if(deadChannel) {
                    AsteriskCall asteriskCall = deadChannel.asteriskCall

                    asteriskCall.removeFromChannels(deadChannel)
                    asteriskCall.save()

                    if (asteriskCall.channels.size() == 0) {
                        log.info "All channels closed, removing call $asteriskCall from active list"

                        Agent callOwner = Agent.findByName(asteriskCall.primaryOwner)
                        Agent secondaryOwner = Agent.findByName(asteriskCall.secondaryOwner)

                        if (callOwner) {
                            callOwner.resetCallState()
                            callOwner.save()
                        }

                        if(secondaryOwner){
                            secondaryOwner.resetCallState()
                            secondaryOwner.save()
                        }

                        CallRecordService.closeOut(asteriskCall.uniqueId)

                        asteriskCall.delete()
                    }
                }
                else {
                    log.warn "Hangup event received for channel $event.channel, but that channel doesn't exist. Possible duplicate event."
                }
            }
            else if (event.class == NewStateEvent) {
                event = event as NewStateEvent

                String linkedId = (event.linkedId.contains("-")) ? event.linkedId.substring(event.linkedId.lastIndexOf("-") + 1) : event.linkedId

                log.info "Channel ${event.channel} ($linkedId) is now '${event.getChannelStateDesc()}'"

                AsteriskCall call = Channel.findByLinkedid(linkedId).asteriskCall
                Agent callOwner = Agent.findByName(call.primaryOwner)

                if (!callOwner){
                    log.warn "Unable to find Agent '${call.primaryOwner}' who currently owns call ${call.uniqueId}."
                }

                if (event.getChannelStateDesc() == 'Up') {

                    if (callOwner){
                        log.debug("Setting ${callOwner.name} state to 'oncall'")

                        callOwner.callState = 'oncall'
                        callOwner.save()
                    }

                    if(call.calleeName == "Calling:" && call.direction != 'inbound' && event.exten){
                        call.calleeName = ContactService.findNameByNumber(event.exten)
                    }

                    // When the channel changes state to 'Up', the call is either active or conference, depending on if it was already marked as conference
                    call.state = (call.state == 'conference') ? 'conference' : 'active'
                    call.save(flush: true, failOnError: true)
                }
                else if (event.getChannelStateDesc() == 'Ring')
                {
                    log.debug("${event.channel} is now Ringing")

                    if (callOwner)
                    {
                        log.debug("Setting ${callOwner.name} state to 'ringing'")
                        callOwner.callState = 'ringing'
                        callOwner.save()
                    }
                }
            }
            else if (event.class == AgentConnectEvent) {
                event = event as AgentConnectEvent

                String linkedId = (event.linkedId.contains("-")) ? event.linkedId.substring(event.linkedId.lastIndexOf("-") + 1) : event.linkedId
                log.debug "Membername '$event.memberName' had AgentConnectEvent fired for call '$linkedId''"

                AsteriskCall answeredCall = AsteriskCall.findByUniqueId(linkedId)

                if(answeredCall) {
                    Agent owner = Agent.findByName(event.memberName)
                    if(owner) {
                        log.info "Agent '$owner.name' connected to call $linkedId"

                        CallService.updatePrimaryOwner(answeredCall, owner.name)
                    }
                    else {
                        log.warn "Tried to update $answeredCall's primary owner, but the membername '$event.memberName' doesn't match any Agents"
                    }
                }
                else {
                    log.error "AgentConnectEvent fired for a call with UniqueId '$linkedId', but no such call exists"
                }
            }
            else if (event.class == ConfbridgeStartEvent) {
                event = event as ConfbridgeStartEvent
                log.debug "Conference room $event.conference started"

                ConferenceRoom conference = new ConferenceRoom(number: event.conference, name: "CONF${event.conference}",
                        startTime: new Date().getTime())
                conference.save()
            }
            else if (event.class == ConfbridgeJoinEvent) {
                event = event as ConfbridgeJoinEvent
                log.info "$event.channel joining conference '$event.conference'"

                ConferenceRoom conference = ConferenceRoom.findByNumber(event.conference)

                if (!conference) {
                    conference = new ConferenceRoom(number: event.conference, name: "CONF${event.conference}",
                                    startTime: event.dateReceived.getTime())
                    conference.save()
                }

                log.debug "CLID '$event.callerIdNum' has joined conference '$conference.name' on channel ${event.channel}."

                Channel channel = Channel.findByChannelId(event.channel)
                ConferenceRoomMember member = ConferenceRoomMember.findOrSaveByChannel(event.channel)

                String channelName = channel.channelId
                String remoteNumber = event.callerIdNum
                String device

                Long agentId
                Agent agent

                if(!member.name) {
                    int endIndex = channelName.contains("@") ? channelName.indexOf("@") : channelName.lastIndexOf("-")
                    device = channelName.substring(4, endIndex)

                    if (device.matches(/[0-9]*/)) {
                        agentId = Long.parseLong(device.substring(device.length() - 3))
                    }
                    else {
                        log.info "Detected outgoing channel, checking for Remote Agent"

                        agentId = Long.parseLong(remoteNumber.substring(remoteNumber.length() - 3))
                    }

                    agent = Agent.findById(agentId)

                    if (agent && remoteNumber.length() <= 5) {
                        log.info "Agent '${agent.name}' has been recognized."

                        member.admin = (event.admin == 'Yes' && agent)//true
                        member.name = agent.name
                    }
                    else {
                        String contactName = ContactService.findNameByNumber(event.callerIdNum)
                        member.name = contactName != 'Unknown' ? contactName : event.callerIdNum
                    }
                }

                conference.addToMembers(member)

                String uniqueId = channel.asteriskCall.uniqueId
                String linkedId = channel.asteriskCall.linkedId

                CallRecord confRecord = CallRecord.findByUniqueIdOrUniqueId(uniqueId, linkedId)
                if(confRecord) {
                    ConferenceRoomMemberRecord memberRecord = ConferenceRoomMemberRecord.findOrSaveByNameAndCallRecordAndDialString(member.name, confRecord, event.callerIdNum)
                    confRecord.addToMemberRecords(memberRecord)
                    confRecord.save()
                }


                conference.save()
                member.save()
            }
            else if (event.class == ConfbridgeLeaveEvent) {
                event = event as ConfbridgeLeaveEvent

                ConferenceRoom room = ConferenceRoom.findByNumber(event.conference)
                ConferenceRoomMember member = ConferenceRoomMember.findByChannel(event.channel)

                log.debug "$member.name left conference room $event.conference"

                room.removeFromMembers(member)
                room.save()

                member.delete()
            }
            else if (event.class == ConfbridgeEndEvent) {
                event = event as ConfbridgeEndEvent

                ConferenceRoom.findByNumber(event.conference)?.delete(flush: true)
            }
            else if (event.class == ParkedCallEvent) {
                event = event as ParkedCallEvent

                AsteriskCall parkedCall = Channel.findByChannelId(event.parkeeChannel).asteriskCall

                if(parkedCall) {
                    parkedCall.startTime = new Date().getTime()
                    parkedCall.parkingSpace = event.parkingSpace

                    if (parkedCall.state != 'parked') {
                        parkedCall.state = "parked"
                        CallService.updatePrimaryOwner(parkedCall, '')
//                        parkedCall.primaryOwner = ''

                        if (!(Agent.findWhere(name: event.parkeeCallerIDName))) {
                            CallService.updateSecondaryOwner(parkedCall, '')
//                            parkedCall.secondaryOwner = ''
                        }
                    }

                    log.info "Call $parkedCall was parked in $event.parkinglot space $event.parkingSpace"
                    parkedCall.save()
                }
                else {
                    log.error "ParkedCallEvent fired for channel $event.parkeeChannel, but no call could be found for it"
                }
            }
            else if (event.class == NewConnectedLineEvent) {
                event = event as NewConnectedLineEvent

                if (event.exten.startsWith("7") && event.exten.length() == 3) {
                    log.warn "Not sure what this code accomplishes!!!"

                    AsteriskCall call = AsteriskCall.findByParkingSpace(event.exten)

                    call.delete(flush: true)
                }
            }
            else if (event.class == HoldEvent) {
                event = event as HoldEvent

                Channel channel = Channel.findByChannelId(event.channel)
                channel.status = event.status ? 'onhold' : 'active'
            }
            else if (event.class == PeerStatusEvent){
                event = event as PeerStatusEvent

                String peer = event.peer

                log.info "Extension '$peer' is now '${event.peerStatus}'"

                if(event.peerStatus == 'Unreachable'){
                    def members = QueueMember.findAllByStateInterface(peer)
                    members.each {QueueMember member ->
                        if(member.paused == 0){
                            log.info "Pausing member '${member.membername}' in Queue '${member.queueName}'"
                            member.paused = 2
                            member.save(failOnError: true)
                        }
                    }
                } else if(event.peerStatus == 'Reachable'){
                    def members = QueueMember.findAllByStateInterface(peer)
                    members.each {QueueMember member ->
                        if(member.paused == 2){
                            log.info "Unpausing member '${member.membername}' in Queue '${member.queueName}'"
                            member.paused = 0
                            member.save(failOnError: true)
                        }
                    }
                }
            }
            else if (event.class == OriginateResponseEvent){
                event = event as OriginateResponseEvent

                AsteriskCall asteriskCall = AsteriskCall.get(event.uniqueId)
                Channel channel = Channel.findByChannelId(event.channel)

                if(asteriskCall && channel) {
                    String srcName, destName

                    String channelId = channel.channelId

                    int endIndex = channelId.contains("@") ? channelId.indexOf("@") : channelId.lastIndexOf("-")

                    String srcNumber = channelId.substring(channelId.indexOf("/") +1, endIndex)
                    String destNumber = event.exten

                    log.debug "OriginateResponse Numbers: src: '$srcNumber', dst: '$destNumber'"

                    Extension srcExt = Extension.findByNumber(srcNumber)

                    if(srcExt){
                        srcName = srcExt.agent.name
                    }
                    else {
                        srcName = 'UNKNOWN AGENT'
                        log.error "Call $asteriskCall was originated, but the source number '$srcNumber' does not match any registered Extensions!"
                    }

                    if(destNumber.length() < 5){
                        Extension destExt = Extension.findByNumber(destNumber)
                        if(destExt){
                            destName = destExt.agent.name
                            asteriskCall.direction = 'internal'
                        }
                        else {
                            destName = "UNKNOWN AGENT"
                            log.warn "Call made to '$destExt' appears to be to an Agent but corresponds to no known Extension"
                        }
                    }
                    else {
                        destName = ContactService.findNameByNumber(destNumber)
                        asteriskCall.direction = 'outbound'
                    }

                    asteriskCall.callerNumber = srcNumber
                    asteriskCall.callerName = srcName

                    asteriskCall.calleeName = destName
                    asteriskCall.calleeNumber = destNumber

                    asteriskCall.dialedNumber = event.exten

                    asteriskCall.save()

                    if(asteriskCall.direction != 'internal') {
                        CallRecordService.create(asteriskCall.properties)
                    }
                    CallService.updatePrimaryOwner(asteriskCall, srcExt.agent.name)
                }
                else {
                    if(!asteriskCall) {
                        log.error "Originate Response caught for call $event.uniqueId, but no such call exists"
                    }
                    if(!channel){
                        log.error "Originate Response caught for channel $event.channel, but no such channel exists"
                    }
                }
            }
//            else{
//                String eventType = event.class.toString()
//                log.debug "${eventType.substring(eventType.lastIndexOf(".") + 1)}: Channel: ${event.channel ?: "N/A"}"
//            }
        } catch (Exception e){
            String className = event.class.toString().substring(event.class.toString().lastIndexOf("."))
            log.error "Manager Event Listener threw ${e.class} on Event '$className' ${event.variable ? "($event.variable)" : ''}:\n\t${e.message}"
        }
    }

    private Channel createChannel(NewChannelEvent event, AsteriskCall linkedCall, String linkedId, String direction) {
        String deviceNumber

        if (event.channel.contains("@")) {
            deviceNumber = event.channel.substring(event.channel.indexOf("/") + 1, event.channel.indexOf("@"))
        }
        else {
            deviceNumber = event.channel.substring(event.channel.indexOf("/") + 1, event.channel.lastIndexOf("-"))
        }

        String channelOwner = (Extension.findByNumber(deviceNumber)) ? Extension.findByNumber(deviceNumber).agent?.name : linkedCall.secondaryOwner

        String status = (event.channel.startsWith('Local')) ? 'local' : 'active'

        Channel channel = new Channel(channelId: event.channel, linkedid: linkedId, ownerName: channelOwner, direction: direction, status: status)

        if(!channel.validate()){
            String errors
            channel.errors.each {
                errors += "\n\t$it"
            }

            log.error "Error(s) creating channel '$event.channel':$errors"

            channel = null
        }
        else{
            channel.save()
        }

        channel
    }

    private AsteriskCall CreateCall(NewChannelEvent event, String uniqueId, String direction) {
        Long now = new Date().time

        Extension callerExtension = Extension.findByNumber(event.callerIdNum)

        String calleeName = "Calling:"
        String primaryOwner = ''
        String secondaryOwner = ''
        String callerName = 'Unknown'

        if (direction == 'internal') {
            Extension secondary = Extension.findByNumber(event.exten)
            callerName = callerExtension ? callerExtension.agent.name : event.callerIdNum
            secondaryOwner = secondary ? secondary.agent.name : ''
        }
        else if (direction == 'outbound'){
            log.info "Outbound call, creating empty call awaiting the OriginateResponse"
        }
        else if (direction == 'inbound') {
            Agent calleeAgent = Extension.findByNumber(event.exten)?.agent

            if (calleeAgent) {
                primaryOwner = calleeAgent.name
                calleeName = calleeAgent.name

                calleeAgent.callState = 'oncall'
                calleeAgent.save()
            } else {
                InboundRoute route = InboundRoute.findByDidNumber(event.exten)
                if (route) {
                    calleeName = route.destinationName

                    if (route.destinationType == 'Agent') {
                        calleeAgent = Agent.findByName(route.destinationName)
                        if(calleeAgent){
                            calleeAgent.callState = 'oncall'
                            calleeAgent.save()
                        }

                        primaryOwner = route.destinationName
                    }
                }
            }
        }
        else {
            log.error "Invalid direction $direction"
            return null
        }

        String state = (event.exten.startsWith("10") && event.exten.length() == 4) ? 'conference' : 'active'

        //TODO: Need to implement faster phone number lookups for non-agent callees
        AsteriskCall call = new AsteriskCall(uniqueId: uniqueId, callerNumber: event.callerIdNum, callerName: callerName,
                calleeName: calleeName, calleeNumber: event.exten, direction: direction,
                startTime: now, state: state, primaryOwner: primaryOwner, secondaryOwner: secondaryOwner, dialedNumber: event.exten)

        if(!call.validate()){
            call.errors.each {
                log.error it
            }

            call = null
        }

        call.save()
    }

    private String DetermineDirection(NewChannelEvent event) {
        String trunkContext = ConfigLineService.getConfigValue("dialplan", "trunk_context")

        log.debug "Dialplan info for '$event.channel': $event.context,$event.exten,$event.priority"

        //Outbound calls will have their direction set by the OriginateResponseEvent, so inbound vs. internal is the only
        //decision that needs to be made here
        event.context == trunkContext ? 'inbound' : 'internal'
    }
}
