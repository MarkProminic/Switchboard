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
import grails.gorm.transactions.Transactional
import switchboard.cloud.Recording

import java.text.SimpleDateFormat

@Transactional
class RecordingService {

    DetachedCriteria<Recording> filterRecordings(params){
        String caller = "%${params.caller.startsWith("1") ? params.caller.substring(1) : params.caller}%"
        String callee = "%${params.callee.startsWith("1") ? params.callee.substring(1) : params.callee}%"
        String paramUID = "%${params.uniqueId}%"

        Date startDate = new Date(0L)
        Date endDate = new Date()
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        if(params."date[on]"){
            startDate = parser.parse("${params."date[on]"} 00:00:00")
            endDate = parser.parse("${params."date[on]"} 23:59:59")
        }
        else {
            if (params."date[from]") {
                startDate = parser.parse("${params."date[from]"} 00:00:00")
            }
            if (params."date[to]") {
                endDate = parser.parse("${params."date[to]"} 23:59:59")
            }
        }

        def filteredRecordings = Recording.recordingsView.where {
            source ==~ caller && destination ==~ callee && start >= startDate && start <= endDate && (uniqueId ==~ paramUID || linkedId ==~ paramUID)
        }

        log.debug "Found ${filteredRecordings.count()} recordings matching ['$caller', '$callee', $startDate, $endDate, '$paramUID']"

        filteredRecordings
    }
}
