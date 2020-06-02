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
import grails.rest.RestfulController
import org.asteriskjava.manager.response.ManagerResponse
import org.springframework.http.HttpStatus
import security.Agent

@Transactional
class AsteriskCallController extends RestfulController<AsteriskCall> {

    static responseFormats = ['json']

    AsteriskCallController(){
        super(AsteriskCall)
    }

    def CallService
    def ConferenceRoomService
    def ConfigLineService

    def springSecurityService

    def transferCallTo(Long agentId, String location){
        Agent destAgent = Agent.findById(agentId)
        Agent user = (Agent) getAuthenticatedUser()
        def status = HttpStatus.OK

        AsteriskCall call = AsteriskCall.findByPrimaryOwnerAndState(user.name, 'active')

        Extension destination

        if(location == 'primary') {
            destination = destAgent.primaryExtension
        }
        else if(location == 'voicemail'){
            destination = null
        }
        else {
            destination = Extension.get(location.toLong())
        }

        if(destination) {
            CallService.transferCall(destination, call, user)
//            call.primaryOwner = destAgent.name
        }
        else if(location == 'voicemail'){
            CallService.transferCallToVM(agentId, call, user)
        }
        else {
            status = HttpStatus.BAD_REQUEST
            log.error "$user.name tried to transfer their call to $destAgent.name at an invalid location '$location'"
        }

        render status: status
    }

    def placeCall(String number, String callerIdName){
        Agent user = (Agent) getAuthenticatedUser()

        if (number && callerIdName) {
            CallService.placeCall(number, callerIdName, user)
        }

        render text: "Originate successfully queued", status: HttpStatus.OK
    }

    def refreshParkingLot(){
        Agent user =  getAuthenticatedUser() as Agent

//        List<AsteriskCall> parkedCalls = AsteriskCall.findAll {
//            state == 'parked'
//        }
        List<AsteriskCall> parkedCalls = AsteriskCall.findAll("from AsteriskCall c WHERE c.state=?", ['parked'])

        respond parkedCalls: parkedCalls
    }

    def answerCall(String callId){
        Agent user = getAuthenticatedUser() as Agent
        def status = HttpStatus.INTERNAL_SERVER_ERROR

        AsteriskCall asteriskCall = AsteriskCall.get(callId)
        ManagerResponse transferResult = new ManagerResponse(message: "EMPTY_RESPONSE")

        if(asteriskCall) {
            try {
                transferResult = CallService.transferCall(user.primaryExtension, asteriskCall, user)
            }
            catch(Exception e){
                log.error "Error answering call $asteriskCall: $e.message"
            }
        }
        else {
            log.error "Tried to answer '$callId', but that call doesn't exist!"
        }

        if(transferResult.message == 'Redirect successful') {
            log.info "${user.name} is answering call '${callId}'"
            status = HttpStatus.OK
        }
        else {
            log.error "Error answering call: $transferResult.message"
        }

        render status: status
    }

    def holdCall(String callId){
        AsteriskCall userCall = AsteriskCall.get(callId)

        Agent user = (Agent) getAuthenticatedUser()

//        userCall.channels.each { Channel channel ->
//            channel.status = 'onhold'
//            channel.save(flush: true)
//        }
//
//        CallService.parkCall(userCall, user)

        CallService.putCallOnHold(userCall, user)

        render status: 200

    }

    def unholdCall(String callId){
        AsteriskCall userCall = AsteriskCall.get(callId)

        CallService.takeCallOffHold(userCall, (Agent) getAuthenticatedUser())

        render status: 200
    }

    def parkCall(String callId){
        AsteriskCall userCall = AsteriskCall.get(callId)

        int status = 200

        if(userCall) {
            if(userCall.channels?.size() < 2) {
                log.warn "Call ${userCall.uniqueId} has less than 2 channels and is therefore malformed. Parking will still be attempted."
            }

            ManagerResponse response = CallService.parkCall(userCall, (Agent) getAuthenticatedUser())

            if(response.message.contains("ERROR")){
                status = 500
                log.error "Call ${userCall.uniqueId} cannot be parked: ${response.message}"
            } else {
                log.info "Successfully parked call ${userCall.uniqueId}."
            }
        } else {
            status = 500
            log.error "No call with Unique Id '${callId}' found, yet parking was requested on it."
        }

        render status: status
    }

    def mergeCalls(String callId){
        AsteriskCall parkedCall = AsteriskCall.get(callId)

        Agent user = (Agent) getAuthenticatedUser()

        def ownedCalls = AsteriskCall.findAllByPrimaryOwnerOrSecondaryOwner(user.name, user.name)

        AsteriskCall activeCall = ownedCalls.find { AsteriskCall ownedCall ->
            ownedCall.state == 'active'
        }

        ConferenceRoomMember userMember = ConferenceRoomMember.findByName(user.name)

        if(activeCall){
            ConferenceRoomService.mergeCalls(activeCall, parkedCall)
        } else if(userMember){
            ConferenceRoomService.addParkedCallToConference(parkedCall, userMember.conference.number)
        }
        else {
            redirect answerCall(callId)
        }
    }

    def addBlacklistNumber(String newNumber) {
        try
        {
            BlacklistedNumber newBlacklistedNumber = new BlacklistedNumber(number: newNumber, blacklistedBy: ((Agent) getAuthenticatedUser()).name, blacklistedOn: new Date(), hitCount: 0)
            newBlacklistedNumber.save(failOnError: true)
        }
        catch (Exception ex)
        {
            def message = "Unable to save $newNumber to the BlacklistedNumber database: ${ex.message}"
            log.warn(message)

            render message: message, status: 500
        }

        render status: 200
    }

    def killPhantomCall(AsteriskCall phantom){
//        AsteriskCall phantom = AsteriskCall.get(params.id)
        log.debug "call: $phantom"
        if(phantom.duration >= 3600){
            Channel.where {
                asteriskCall == phantom
            }.deleteAll()

            phantom.delete()
        }

        respond phantom
    }

}