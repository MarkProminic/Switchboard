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

package security

import grails.gorm.transactions.Transactional
import groovy.json.JsonBuilder

import switchboard.cloud.*

@Transactional(readOnly = false)
class UserController {
    def ContactService
    def CallService
    def DialPlanService

    def index(){
        Agent user = (Agent) getAuthenticatedUser()
        def userCalls = CallService.listActiveCalls(user) ?: []

        if(user.state == 'offline')
        {
            log.debug "Marking offline user ${user.name} as online."

            user.state = 'available'
            user.save(flush: true)
        }

        render template: '/user/callList', model: [userCalls: userCalls]
    }

    def setLocation(Long id){
        Agent user = getAuthenticatedUser() as Agent

        user.extensions.each { Extension extension ->
            extension.primaryExt = (extension.id == id)

            extension.save(flush: true)
        }

        log.info "$user.name has changed location to $user.primaryExtension.displayName"

        FollowMeNumber firstStep = FollowMeNumber.findOrSaveByNameAndOrdinal(user.id as String, "1")

        def stepExtensions = []
        if(firstStep.extensions){
            stepExtensions = Extension.findAllByIdInList(firstStep.extensions.id as List<String>)
        }

        stepExtensions.each {
            firstStep.removeFromExtensions(it)
        }

        Extension primary = user.primaryExtension

        firstStep.addToExtensions(primary)

        firstStep.phoneNumber = primary.phoneNumber

        firstStep.save(flush: true)

        List<Extension> locationOptions = []

        locationOptions.addAll(user.extensions)
        locationOptions.remove(user.primaryExtension)

        def userMembers = QueueMember.findAllByMembername(user.name)

        if(userMembers) {
            log.debug "Updating ${userMembers.size()} Queue Members for ${user.name}."

            // For whatever reason, updating a queue member DOES NOT update both interfaces in Asterisk
            // SO DON'T CHANGE THIS AGAIN

            userMembers.each { QueueMember member ->
                QueueMember replacement = new QueueMember(membername: member.membername, memberInterface: primary.memberInterface,
                        stateInterface: primary.stateInterface, queueName: member.queueName)
                replacement.save(flush: true)

                member.delete(flush: true)
            }
        }

        render template: "/user/modal/followmeStep", model: [step: firstStep, i: 0]
    }

    def logOffAllQueues(){
        Agent user = getAuthenticatedUser()

        QueueMember.findAllByMembername(user.name).each { QueueMember member ->
            member.delete(flush: true)
        }

        user.setAgentState('offline')
        user.save()

        render status: 200
    }

    def logOffUser(){
        Agent user = getAuthenticatedUser()

        user.setAgentState('offline')
        user.save()

        render status: 200
    }

    def getFavorites(){
        Agent user = getAuthenticatedUser()
        def favoritesList = [:]
        if(user.favorites.size() > 0 ) {

            def results = ContactService.findUserFavorites(user.favorites)

//            def results = ContactService.getContactInfo(query)

            if (results instanceof Contact) {
                favoritesList.put("1", results)
            } else {
                favoritesList = results
            }
        }

        def json = new JsonBuilder()

        json.data{
            favorites favoritesList.values(), { Contact contact ->
                id contact.contactId
                name contact.fullName
                phoneNumbers contact.phoneNumbers, { String phoneNumber ->
                    number phoneNumber
                }
            }
        }

        render json
    }

    def addFavorite(String contactID){
        String realId = contactID.replaceAll("_", ".")
        Agent user = getAuthenticatedUser()

        user.addToFavorites(realId)
        user.save()

        Map<String, Contact> favoriteInfo = ContactService.findByContactId(realId)

        def json = new JsonBuilder()

        if(favoriteInfo.size() != 1) {
            render staus: 400, message: "Multiple contacts returned for contactId $realId"
        }

        json.data favoriteInfo.values(), { Contact contact ->
            id contact.contactId
            name contact.fullName
            phoneNumbers contact.phoneNumbers, { String phoneNumber ->
                number phoneNumber
            }
        }

        render json
    }

    def removeFavorite(String contactID){
        String realId = contactID.replaceAll("_", ".")
        Agent user = getAuthenticatedUser()

        user.removeFromFavorites(realId)

        render status: 200
    }

    def addExtension(String prefix, String type, String remoteNumber){
        Agent user = getAuthenticatedUser()
        String extenName = ExtensionLocation.findByPrefix(prefix).name
        String exten = "$prefix${user.id}"

        Extension extension = new Extension(name: extenName, number: exten, type: type)

        extension.agent = user
        extension.save(failOnError: true)

        user.addToExtensions(extension)

        user.save(failOnError: true)

        if(type == 'Local'){
            SIPPeer peer = new SIPPeer(name: exten).save(failOnError: true)
            DialPlanService.createLocalPeer(exten)
        } else if(type == 'Remote') {
            RemotePeer peer = new RemotePeer(exten: exten, phoneNumber: remoteNumber, extensions_id: extension.id).save(failOnError: true)
            DialPlanService.createRemotePeer(exten, remoteNumber.replaceAll("[^0-9]", ""))
        }

        respond extension
    }

    def updateExtension(String id, String name, String number){
        Agent user = getAuthenticatedUser()
        Extension extension = Extension.findByIdAndAgent(Long.parseLong(id), user)
        def json = new JsonBuilder()
        String updateMessage = ""
        boolean success = false

        if(extension){
            extension.name = name ?: extension.name
            if(extension.type == 'Remote') {
                RemotePeer peer = RemotePeer.findByExten(extension.number)

                peer.phoneNumber = number
                peer.save(flush: true)

                updateMessage = "Extension successfully updated."
                success = true
            }
            else if(extension.type == 'Local' && number && number.endsWith(user.id as String) ) {
                extension.number = number
                updateMessage = "Extension successfully updated."
                success = true
            } else {
                updateMessage = "Invalid value '$number' for Extension number; must be or end with your Agent Id (${user.id})."
            }

            extension.save(flush: true)

            json.result {
                extname name
                extnumber number
                message updateMessage
                status success
            }
        }



        render json
    }

    def deleteExtension(String extensionId){
        Agent user = getAuthenticatedUser() as Agent
        Extension extension = Extension.findByAgentAndId(user, Long.parseLong(extensionId))

        if(extension){
            // Find and remove this extension from any ring groups it is a part of
            user.ringGroups.each {
                it.removeFromExtensions(extension)
                it.save()
            }

            // Find any Follow Me steps that ring this extension independent of any ring groups,
            // as this extension has already been removed from all Ring Groups
            FollowMeNumber.findAllByName(user.id as String).each {
                it.removeFromExtensions(extension)
                it.save()
            }

            if(extension.type == 'Remote'){
                RemotePeer.findByExten(extension.number)?.delete(flush: true)
            }
            else {
                SIPPeer.findByName(extension.number)?.delete(flush: true)
            }

            DialPlanService.deletePeer(extension.number)
            extension.delete(flush: true)
        }
        else {
            String cause
            if(extension){
                cause = "that extension does not belong to $user.name so the deletion was cancelled."
            }
            else {
                cause = "no Extension exists with that id."
            }

            log.error "Deletion request was submitted by $user.name for extension '$extensionId', however $cause"
        }

        respond extension

    }

    def settings(){
        Agent user = getAuthenticatedUser() as Agent

        respond user: user
    }

    def changePassword(String password){
        Agent user = getAuthenticatedUser()
        user.password = password

        try {
            user.save(flush: true, failOnError: true)
        } catch(Exception e){
            log.error "Unable to change password for ${user.name}. No change was made"
            render status: 400, message: "Unable to change password for ${user.name}. No change was made"
        }

        render status: 200
    }

    def setUserState(String state){
        Agent user = getAuthenticatedUser()
        def availableQueues = QueueMember.findAllByMembername(user.name)

//        log.info "${user.name} state changed to ${state}"

        user.setAgentState(state)
        user.save()

        if(state == "busy")
        {
            availableQueues.each {QueueMember queue ->
                queue.paused = 1
                queue.save()
            }
        }

        if(state == "available")
        {
            availableQueues.each {QueueMember queue ->
                queue.paused = 0
                queue.save()
            }
        }
        render status: 200
    }

    def extensions(){
        render template: "/user/userExtensions", model: [user: getAuthenticatedUser()]
    }
}
