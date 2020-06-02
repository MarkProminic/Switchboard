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
import groovy.json.StreamingJsonBuilder
import org.springframework.http.HttpStatus
import security.Agent
import voicemail.Message

@Transactional
class VoicemailController {

    def MessageService
    def ConfigLineService

    def index() { }

    def refreshMailbox() {
        Agent user = getAuthenticatedUser() as Agent

        MessageService.discoverVoicemails(user.id)

        List<String> displayFolders = ConfigLineService.getConfigValue("voicemail", "display_folders")?.split(",") ?: ['INBOX', 'Old']

//        List<Message> messages = Message.findAll {
//            agentId == user.id
//            location in displayFolders
//        }
        List<Message> messages = Message.findAll(sort: "createdOn", order: "desc") {
            agentId == user.id
            location in displayFolders
        }

        int newCount = messages.count { it.isNew }
        int oldCount = messages.count { it.isOld }

        String tooltip = "$newCount new messages, $oldCount old"

        render template: "/voicemail/panel", model: [messages: messages, messageCount: messages.size(), tooltip: tooltip]
//        respond([messageList: messageList, inboxCount: messageList.count{ it.isNew }, oldCount: messageList.count{ it.isOld }])
    }

    @Transactional(readOnly = false)
    def delete(Message message){
        int status = 200

        if(message) {
            try {
                MessageService.markAs(message, "DELETED")
            } catch (Exception e) {
                log.error("Error deleting message $message: ${e.message}")
            }
        } else {
            status = 400
            Agent user = getAuthenticatedUser() as Agent
            log.warn("${user.name} tried to delete message $message, but no message with that id exists")
        }

        render status: status
    }

    @Transactional(readOnly = false)
    def hardDelete(String id){
        String host = "localhost:55555"

        String fullURL = "http://$host/voicemailhelper/$id"

        def url = new URL(fullURL)

        def conn = url.openConnection()

        conn.useCaches = false

        conn.requestMethod = 'POST'

        conn.connect()
        Thread.sleep(10) //wait for request to complete

        render status: 200
    }

    @Transactional(readOnly = false)
    def markAsListened(Message message){
        def status = HttpStatus.OK

        try {
            MessageService.markAs(message, "Old")
        } catch (Exception e) {
            log.error("Error updating message $message: ${e.message}")
            status = HttpStatus.INTERNAL_SERVER_ERROR
        }

        render status: status
    }

    def streamFile(Message message){
        File audio

        if(message) {
            audio = new File(message.audioSourcePath)
        } else {
            Agent user = getAuthenticatedUser() as Agent

            log.error "${user.name} tried to stream message $message, but no message with that id exists"
        }

        render file: audio, contentType: 'audio/wav'
//        render status: HttpStatus.OK
    }

}