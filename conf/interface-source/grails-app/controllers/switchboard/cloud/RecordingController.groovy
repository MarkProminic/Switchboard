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

import grails.gorm.DetachedCriteria
import security.Agent

class RecordingController {

    def RecordingService

    def index() {
        params.max = params.max ? Integer.parseInt(params.max) : 10
        params.offset = params.offset ? Integer.parseInt(params.offset) : 0

        params.filtered = "false"

        render template: "table", model:[recordings: Recording.recordingsView.list(max: params.max, offset: params.offset), totalCount: Recording.recordingsView.count()]
//        respond recordings: Recording.recordingsView.list(max: params.max, offset: params.offset ?: 0), totalCount: Recording.recordingsView.count(), paginationInfo: params
    }

    def filter(){
        params.max = params.max ? Integer.parseInt(params.max) : 10
        params.offset = params.offset ? Integer.parseInt(params.offset) : 0

        params.filtered = "true"

        log.debug "max: $params.max, offset: $params.offset, filtered: $params.filtered"

        DetachedCriteria<Recording> filteredRecordings = RecordingService.filterRecordings(params)

        render template: "table", model:[recordings: filteredRecordings.list(max: params.max, offset: params.offset, sort: "start", order: "desc")
                                         ,totalCount: filteredRecordings.count()]
//        respond recordings: filteredRecordings, paginationInfo: params
    }

    def streamFile(Recording recording){
        File audio

        if(recording) {
            audio = new File(recording.audioSourcePath)
        } else {
            Agent user = getAuthenticatedUser() as Agent

            log.error("${user.name} tried to stream recording $recording, but no message with that id exists")
        }

        byte[] fileContents = []

        try{
            fileContents = audio.getBytes()
        }
        catch (Exception e){
            log.error "Cannot stream recording $recording: $e.message"
        }

        render file: fileContents, contentType: 'audio/wav'
//        render status: HttpStatus.OK
    }

    def recordPlayer(String uniqueId){
        render template: "audioPlayer", model: [recording: Recording.findByUniqueId(uniqueId), uniqueId: uniqueId]
    }
}
