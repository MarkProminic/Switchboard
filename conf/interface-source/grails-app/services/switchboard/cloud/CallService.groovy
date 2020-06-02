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

import dialer.CallAs
import grails.gorm.transactions.Transactional
import grails.util.Environment
import org.asteriskjava.manager.AuthenticationFailedException
import org.asteriskjava.manager.ManagerConnection
import org.asteriskjava.manager.ManagerConnectionFactory
import org.asteriskjava.manager.action.*
import org.asteriskjava.manager.response.ManagerResponse
import security.Agent
import switchboard.cloud.AsteriskCall
import switchboard.cloud.CallRecord
import switchboard.cloud.Channel
import switchboard.cloud.Extension

import java.util.concurrent.TimeoutException

@Transactional
class CallService {
    ManagerConnection managerConnection

    def ChannelService
    def ConfigLineService
    def springSecurityService
    def CallRecordService

    boolean connected = false

    ManagerConnection connect(String username, String password){
        String host = ConfigLineService.getConfigValue("general", "asterisk_host")

        Environment.executeForCurrentEnvironment {
            development {
                log.info "Connecting to the Asterisk instance configured in /etc/hosts."
            }

            production {
                log.info "Connecting to the Asterisk instance configured in /etc/hosts."
            }
        }

            ManagerConnectionFactory factory = new ManagerConnectionFactory(host, username, password)

            ManagerConnection connection = factory.createManagerConnection()

            try {
                // connect to Asterisk and log in
                connection.login();

                connection.sendAction(new StatusAction())

                log.info "Successfully connected to the Asterisk Server"

                return connection

            }
            catch (AuthenticationFailedException e) {
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

            return null
        }

    void disconnect(ManagerConnection connection){
        log.info "Disconnecting from $connection.hostname"
        connection.logoff()
    }

    ManagerResponse sendAction(ManagerAction action, Long timeout = 30000l){

        String username = ConfigLineService.getConfigValue("manager", "username")
        String password = ConfigLineService.getConfigValue("manager", "password")

        def connection = connect(username, password)

        ManagerResponse response = connection.sendAction(action, timeout)

        disconnect(connection)

        response
    }

    def listActiveCalls(Agent agent){
        def calls =  AsteriskCall.findAllByPrimaryOwnerOrSecondaryOwner(agent.name, agent.name)

        calls.removeAll { AsteriskCall call ->
            !(call.state in ['active', 'conference'])
        }

        calls
    }

    def putCallOnHold(AsteriskCall call, Agent user){
        Channel nonUserChannel

        try {
            call.state = 'onhold'
            call.save(flush: true)

            nonUserChannel = call.channels.find { Channel channel ->
                channel.ownerName != user.name
            }
            managerConnection.sendAction( new RedirectAction(nonUserChannel.channelId, 'put-on-hold', "s", 1) )

            //TODO: Implement response handling to indicate success
        } catch (Exception e) {
            log.error "Put Call On Hold threw ${e.class}: ${e.message}"
        }
    }

    def takeCallOffHold(AsteriskCall call, Agent user){
        Channel nonUserChannel

        nonUserChannel = call.channels.find { Channel channel ->
            channel.ownerName != user.name
        }

        try {
            managerConnection.sendAction( new RedirectAction(nonUserChannel.channelId, 'take-off-hold', "s", 1) )
            Agent callOwner = Agent.findByName(call.primaryOwner)
            callOwner.callState = 'oncall'
            callOwner.save()

            // TODO: Implement response handling to indicated success
        } catch (Exception e) {
            log.error "Take Call On Hold threw ${e.class}: ${e.message}"
        }
    }

    ManagerResponse transferCall(Extension destination, AsteriskCall transferCall, Agent user){
        String destAgent = destination.agent? destination.agent.name : 'EXTEN_NO_AGENT'

        log.info "Transferring call $transferCall to $destAgent($destination)"

        ManagerResponse result = new ManagerResponse()
        Channel nonUserChannel = ChannelService.findNonUserChannel(transferCall, user.name)

        log.debug "Transferring channel $nonUserChannel"

        if(nonUserChannel) {

            nonUserChannel.status = 'active'

            try {
                result = sendAction(new RedirectAction(nonUserChannel.channelId, 'agents', destination.phoneNumber, 1))
            } catch (Exception e) {
                log.error "Transfer Call threw ${e.class}: $e.message"
                return result
            }

            if(result.message == "Redirect successful"){
                log.debug "Response: $result.response"

                transferCall.state = 'active'

                log.debug "Updating ownership of call $transferCall to $destAgent"
                updatePrimaryOwner(transferCall, destAgent)

                transferCall.save()

            }
            else {
                log.error "Error transferring call $transferCall to $destAgent: '$result.message'"
            }
        }
        else {
            String channelData =""

            transferCall.channels.each { Channel channel ->
                channelData += "\t$channel\n"
            }

            log.error "$user.name tried to transfer call $transferCall, but customer's channel could not be found.\n Channel data:\n$channelData"
        }

        return result
    }

    ManagerResponse transferCallToVM(Long agentId, AsteriskCall asteriskCall, Agent user){
        Agent destAgent = Agent.get(agentId)
        String context = ConfigLineService.getConfigValue("dialplan", "transfer_to_vm_context") ?: 'agents-vm'

        Channel channel = ChannelService.findNonUserChannel(asteriskCall, user.name)

        if(channel) {
            log.info "Transferring $asteriskCall to $destAgent.name's voicemail"

            updatePrimaryOwner(asteriskCall, destAgent.name)

            asteriskCall.state = 'vm'

            return sendAction(new RedirectAction(channel: channel.channelId, context: context, exten: agentId as String, priority: 1))
        }
        else {
            String message = "Could not identify which channel of call $asteriskCall to transfer to voicemail"

            log.error message
            return new ManagerResponse(message: "ERROR: $message")
        }

    }

    ManagerResponse parkCall(AsteriskCall call, Agent user){
        log.info "User $user.name is parking call '$call.uniqueId'"
        Channel userChannel, nonUserChannel

        userChannel = ChannelService.findUserChannel(call, user.name)

        nonUserChannel = ChannelService.findNonUserChannel(call, user.name)

        log.debug "User: $userChannel"
        log.debug "NonUser: $nonUserChannel"

        ManagerResponse response

        if(userChannel && nonUserChannel) {
            response = sendAction(new ParkAction(nonUserChannel.channelId, userChannel.channelId))
            log.debug "Park result $response.message"

            if(user.name == call.primaryOwner){
                updatePrimaryOwner(call, '')
            } else if (user.name == call.secondaryOwner){
                updateSecondaryOwner(call, '')
            } else {  // ERROR
                log.error "Park was called by ${user.name} on ${call.uniqueId} when they do not own that call."
                return
            }

            call.channels.each { Channel channel ->
                if(channel.status != 'local') {
                    channel.status = 'parked'
                    channel.save()
                }
            }

            call.lastParked = new Date().time
            call.state = 'parked'
            call.parkedBy = user.name
            call.save()
        }
        else {
            String message = ""

            if(userChannel){
                message = "ERROR: Cannot park call, channel for the parkee cannot be found!"
            }
            else if(nonUserChannel){
                message = "ERROR: Cannot park call, channel for the parker cannot be found!"
            } else {
                message = "ERROR: Cannot park call, neither parker nor parkee channel could be found!"
            }

            response = new ManagerResponse(message: message)
        }

        response
    }

    void placeCall(String number, String callerIdName, Agent user) {
        def searchedCallerID
        String areaCode

        if (number.contains("(") && number.contains(")")) {
            areaCode = number.substring(number.indexOf("(") + 1, number.indexOf(")"))
        }
        else {
            areaCode = number.substring(0, 3)
        }

        String locale = (number.startsWith("+1") && !(areaCode in CallAs.canadaCodes)) ? 'Domestic' : 'International'

        def agentCallAs
        if (callerIdName == 'Self') {
            agentCallAs = user.callerIds.find { CallAs callerId ->
                callerId.type == locale
            }
            searchedCallerID = agentCallAs.callerID
        }
        else {
            searchedCallerID = CallAs.findByNameAndType(callerIdName, locale).callerID
        }

        log.info "$user.name is calling '$number' from $user.primaryExtension.name as '$callerIdName'($searchedCallerID)."

        String parsedNumber = parsePhoneNumber(number)

        log.debug "$number -> $parsedNumber"

        OriginateAction action = new OriginateAction(context: "agents", exten: parsedNumber, priority: 1,
                channel: "Local/$user.primaryExtension.number@agents", callerId: searchedCallerID, async: true)

        sendAction(action)
    }

    ManagerResponse placeCRMCall(String number, String callerIdName, Agent user) {
        def searchedCallerID
        String areaCode

        if (number.contains("(") && number.contains(")")) {
            areaCode = number.substring(number.indexOf("(") + 1, number.indexOf(")"))
        }
        else {
            areaCode = number.substring(0, 3)
        }

        String locale = (number.startsWith("+1") && !(areaCode in CallAs.canadaCodes)) ? 'Domestic' : 'International'

        def agentCallAs
        if (callerIdName == 'Self') {
            agentCallAs = user.callerIds.find { CallAs callerId ->
                callerId.type == locale
            }
            searchedCallerID = agentCallAs.callerID
        }
        else {
            searchedCallerID = CallAs.findByNameAndType(callerIdName, locale).callerID
        }

        log.info "$user.name is calling '$number' from $user.primaryExtension.name as '$callerIdName'($searchedCallerID)."

        String parsedNumber = parsePhoneNumber(number)

        log.debug "$number -> $parsedNumber"

        OriginateAction action = new OriginateAction(context: "agents", exten: parsedNumber, priority: 1,
                channel: "Local/$user.primaryExtension.number@agents", callerId: searchedCallerID, async: true)

        sendAction(action)
    }

    void updatePrimaryOwner(AsteriskCall asteriskCall, String owner){
        Agent oldOwner = Agent.findByName(asteriskCall.primaryOwner)
        CallRecord record = CallRecord.get(asteriskCall.uniqueId)

        if(oldOwner && record){
            log.debug "Resetting call state for old owner $oldOwner.name"

            oldOwner.removeFromPendingCallRecords(record)
            log.info "Owner of call $asteriskCall updated, unassigning record from $oldOwner"

            oldOwner.resetCallState()
            oldOwner.save(failOnError: true)
        }
        else {
            if(!record){
                if(asteriskCall.direction == 'internal'){
                    log.info "No call record exists for $asteriskCall because it is an internal call, skipping updates"
                }
                else {
                    log.error "No call record exists for $asteriskCall and the call is not internal. Possible sequence break or creation bug"
                }

                return
            }
            else {
                log.warn "The current owner of call $asteriskCall '${oldOwner?.name}' (${oldOwner?.id}) does not appear to exist. "
            }
        }

        log.info "Setting owner of call '$asteriskCall.uniqueId' to '$owner'."
        asteriskCall.primaryOwner = owner

        UpdateNamesAndNumbers(asteriskCall)

        if(owner) {
            Map updateValues = [calleeName: asteriskCall.calleeName, calleeNumber: asteriskCall.calleeNumber,
                                callerName: asteriskCall.callerName, callerNumber: asteriskCall.callerNumber,
                                agents: owner]

            CallRecordService.update(asteriskCall.uniqueId, updateValues)

            Agent newOwner = Agent.findByName(owner)

            newOwner.addToPendingCallRecords(record)
            log.info "Assigning record for call $asteriskCall to $newOwner"

            newOwner.save(failOnError: true)
        }

        asteriskCall.save(flush: true)
    }

    void updateSecondaryOwner(AsteriskCall asteriskCall, String owner){
        Agent oldOwner = Agent.findByName(asteriskCall.secondaryOwner)

        if(oldOwner){
            log.debug "Resetting call state for old secondary owner $oldOwner.name"
            oldOwner.resetCallState()
            oldOwner.save()
        }

        log.info "Setting secondary owner of call $asteriskCall.uniqueId to $owner."
        asteriskCall.secondaryOwner = owner

//        UpdateNamesAndNumbers(asteriskCall)

        if(owner) {
            Map updateValues = [calleeName: asteriskCall.calleeName, calleeNumber: asteriskCall.calleeNumber,
                                callerName: asteriskCall.callerName, callerNumber: asteriskCall.callerNumber,
                                agents: Agent.findByName(owner)]

            CallRecordService.update(asteriskCall.uniqueId, updateValues)
        }

        asteriskCall.save(flush: true)
    }

    String parsePhoneNumber(String number){
        log.debug "Unparsed Phone Number: $number"

        String internationalDialOutCode = ConfigLineService.getConfigValue("dialer", "international_dial_out_code") ?: '011'
        String parsedNumber  = number

        if (number.startsWith("+")) {
            // need to replace the + with the international dial-out code, but only if we're actually dialing
            // internationally
            if(!number.startsWith("+1")) {
                parsedNumber = parsedNumber.replace("+", internationalDialOutCode)
            }
            else {
                //remove the +1 for dialing 'locally'
                // TODO: Generalize this localization according to a DB configLine value
                parsedNumber = parsedNumber.substring(1)
            }
        }

        log.debug "Parsed Phone Number: ${parsedNumber.replaceAll("[^0-9]", '')}"

        parsedNumber.replaceAll("[^0-9]", '')
    }

    List<AsteriskCall> listQueueCalls(){
        Agent user = springSecurityService.currentUser

        AsteriskCall.findAll {
            direction != 'internal'
            state == 'active'
            queue != null && queue != ''
            primaryOwner != user.name
        }
    }

    private void UpdateNamesAndNumbers(AsteriskCall asteriskCall){
        if(asteriskCall.primaryOwner){
            if(asteriskCall.direction == 'inbound'){
                asteriskCall.calleeName = asteriskCall.primaryOwner
                asteriskCall.calleeNumber = Agent.findByName(asteriskCall.primaryOwner).primaryExtension.number
            }
            else if(asteriskCall.direction == 'outbound'){
                asteriskCall.callerName = asteriskCall.primaryOwner
                asteriskCall.callerNumber = Agent.findByName(asteriskCall.primaryOwner).primaryExtension.number
            }
            else {
                log.warn "Tried to update the caller/callee names and numbers on $asteriskCall but it is neither " +
                        "an inbound nor outbound call ($asteriskCall.direction); leaving call as is"
            }
        }
        else {
            log.warn "Tried to update the caller/callee names and numbers on $asteriskCall but the primary owner is null."
        }
    }
}