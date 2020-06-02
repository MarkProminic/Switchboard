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

package voicemail

import grails.gorm.transactions.Transactional
import groovy.io.FileType
import security.Agent

import java.sql.Timestamp

@Transactional
class MessageService {

    def ContactService
    def ConfigLineService

    private static FilenameFilter filter = new FilenameFilter() {
        @Override
        boolean accept(File dir, String name) {
            name.endsWith(".wav")
        }
    }
    
    private static File baseDirectory

    void discoverVoicemails(Long id){
        Agent agent = Agent.get(id)

        // Default Admin is not a full user and therefore should have no voicemail box configured
        if(agent.name != 'Default Admin') {
            String baseDir = ConfigLineService.getConfigValue("voicemail", "base_directory") ?: ""

            List<String> displayFolders = ConfigLineService.getConfigValue("voicemail", "display_folders")?.split(",") ?: ['INBOX', 'Old']

            if (baseDir) {
                String vmDirPath = "$baseDir/$id"
                baseDirectory = new File(vmDirPath)

                if (baseDirectory.exists()) {
                    displayFolders.each { String folderName ->
                        int messagesInDB = Message.countByAgentIdAndLocation(id, folderName)
                        File folder = new File(baseDirectory, folderName)

                        String[] messagesOnFS = folder.list(filter) ?: []

                        if (messagesOnFS.size() != messagesInDB) {
                            log.debug "Processing directory $folderName for $baseDirectory.name"
                            ProcessFolder(folderName)
                        }
                    }

                } else {
                    log.error "Directory not found: '$vmDirPath', check the interface_config table and filesystem for consistency"
                }
            }
        }
    }

    void markAs(Message message, String status){
        if(message.location != status) {
            boolean moved = MoveMessage(message, status)

            if(moved) {
                message.location = status

                message.validate(['location'])

                if (!message.hasErrors()) {
                    message.save()


                } else {
                    log.error "Invalid location '$status'"
                }
            }
            else {
                log.error "Couldn't move message $message.name for Agent $message.agentId"
            }
        }
    }

    private void ProcessFolder(String folderName) {
        File messageDir = new File(baseDirectory, folderName)

        if(messageDir.exists()) {
            messageDir.eachFile(FileType.FILES) { File file ->
                if (file.name.endsWith(".txt")) {
                    if (file.canRead()) {
                        Properties messageProps = new Properties()
                        file.withInputStream {
                            messageProps.load(it)
                        }

                        String formattedCallerId = GetFormattedCallerId(messageProps.callerid) ?: "Unknown"

                        String filename = file.name.substring(0, file.name.indexOf("."))

                        Long agentId = ((String) messageProps.origmailbox).toLong()

                        Timestamp createdOn = new Timestamp(((String) messageProps.origtime).toLong() * 1000l)

                        log.debug "Checking for an existing message from $createdOn ($filename)"

                        Agent messageAgent = Agent.get(agentId)
                        Message existingMessage = Message.findByCreatedOnAndAgentId(createdOn, agentId)

                        if(!existingMessage){
                            new Message(agentId: agentId, callerId: formattedCallerId, duration: ((String) messageProps.duration).toInteger(),
                                    createdOn: createdOn, location: folderName, name: filename).save()

                            log.info "Created Message '$filename' for $messageAgent.name."
                        }
                        else {
                            log.debug "Message already exists with filename '$filename' for $messageAgent.name, skipping"
                        }
                    } else {
                        log.warn "Cannot read '$file.name'"
                    }
                }
            }
        }
    }

    private String GetFormattedCallerId(callerId) {
        if(callerId) {
            String formattedCallerId = callerId

            if(!callerId.matches(/".*" <[0-9]*>/)) {
                String number

                if (callerId.matches(/"[0-9]*" <[0-9]*>/)) {
                    number = callerId.substring(callerId.indexOf("\"")+1, callerId.lastIndexOf("\""))
                } else if (callerId.matches(/[0-9]*/)) {
                    number = callerId
                }
                else {
                    log.debug "Unrecognized format: $callerId"
                    return number
                }

                String name = ContactService.findNameByNumber(number)

                if(!name.matches(/[A-Z|a-z]* [A-Z|a-z]*/)) {
                    name = "Unknown"
                }

                formattedCallerId = "\"$name\" <$number>"

                log.debug "$callerId -> $formattedCallerId"
            }

            return formattedCallerId
        }

        null
    }

    private boolean MoveMessage(Message message, String location){
        File messageAudio = new File(message.audioSourcePath)
        File messageInfo = new File(message.infoSourcePath)

        String baseDir = ConfigLineService.getConfigValue("voicemail", "base_directory")

        String newLocation = "$baseDir/$message.agentId/$location/$message.name"

        File destinationPath = new File(newLocation + '.wav')
        int duplicateNum = 1

        String baseName = message.name

        while(destinationPath.exists()){
            message.name = "$baseName($duplicateNum)"
            destinationPath = new File("$baseDir/$message.agentId/$location/${message.name}.wav")

            duplicateNum++
        }

        newLocation = "$baseDir/$message.agentId/$location/$message.name"

        message.save()

        boolean movedAudio = messageAudio.renameTo(newLocation + ".wav")
        boolean movedInfo = messageInfo.renameTo(newLocation + ".txt")

        log.info "Move attempted for $message.name($message.agentId) to $location ($movedAudio, $movedInfo)"

        (movedAudio && movedInfo)
    }
}
