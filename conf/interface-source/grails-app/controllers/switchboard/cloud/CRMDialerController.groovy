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


import groovy.json.JsonBuilder
import org.asteriskjava.manager.response.ManagerResponse
import security.Agent

class CRMDialerController {

    def ContactService
    def CallService

    def dial(String agentName, String callAs, String remoteNumber, String key) {
        if (key == "34h53hj4vg53n4b5v") {
            Agent agent = Agent.findByName(agentName)
            Agent user = getAuthenticatedUser() as Agent
            if (agent) {
                log.info "Making call from $agentName to $remoteNumber using CRMDialer"

                boolean success = false
                String dialerResult
                ManagerResponse response
                try {
                    response = CallService.placeCRMCall(remoteNumber, callAs ?: 'Self', user)
                    success = true
                    dialerResult = "Originate successfully queued"
                }
                catch (Exception e) {
                    dialerResult = "Error placing call to '$remoteNumber' from $user.name as '$callAs'"

                    log.error dialerResult
                }

                def json = new JsonBuilder()

                json.callResult {
                    result success
                    info dialerResult
                }

                render json
            }
        }
    }

    def submitRecord(String uniqueId, String notes, String key){
        if(key){
            CallRecord record = CallRecord.findByUniqueId(uniqueId)
            if(record){
                record.notes = notes
                record.save()
            }
            else {
                log.error "Someone tried to submit a call record from Notes with Unique ID '$uniqueId', but no record exists"
            }
        }
    }
}
