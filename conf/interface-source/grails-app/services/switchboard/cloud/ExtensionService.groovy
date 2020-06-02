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
import security.Agent
import switchboard.cloud.Extension
import switchboard.cloud.SIPPeer

@Transactional
class ExtensionService {

    static extensionNames = ["1": "Champaign Office", "2": "Rantoul Office", "3": "Burlington Office",
                             "4": "Home Office", "5": "Bucharest Office", "6": "Savoy Office", "7": "LAX Office"]

    def DialPlanService

    boolean createExtension(String prefix, Long agentId){
        boolean success = true

        Agent agent = Agent.get(agentId)
        String exten = prefix + agent.id
        Extension extension = new Extension(number: exten, type: 'Local', name: extensionNames[prefix], primaryExt: true)
        extension.agent = agent

        try {
            extension.save(failOnError: true)
        }
        catch(Exception e){
            success = false
            log.error "Failed to create Extension $exten for Agent $agent.name: ${e.message}"
        }

        agent.addToExtensions(extension)

        try {
            agent.save(failOnError: true)
        }
        catch(Exception e) {
            success = false
            log.error "Failed to attach Extension $exten to Agent $agent.name: ${e.message}"

            agent.delete()
            extension.delete()
        }

        SIPPeer peer
        try {
            peer = new SIPPeer(name: exten, mailbox: "{$agent.id}@prominic").save(flush: true, failOnError: true)
        }
        catch (Exception e){
            success = false
            log.error "Failed creating SIPPeer for Extension $exten ($agent.name): ${e.message}"

            agent.delete()
            extension.delete()
            peer?.delete()
        }

        DialPlanService.createLocalPeer(exten)

        success
    }
}
