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

import org.springframework.http.HttpStatus
import security.Agent
import security.AgentRole
import security.Role

class AdminController {

    def AgentService
    def MailService

    def index() {
        Agent user = getAuthenticatedUser() as Agent

        //the logged in user should be excluded from this list for simplicity's sake
        def enabledAgents = Agent.findAllByEnabledAndIdNotEqual(true, user.id, [sort: "name", order: "asc"])
        def disabledAgents = Agent.findAllByEnabledAndIdNotEqual(false, user.id, [sort: "name", order: "asc"])

        def peers = SIPPeer.listOrderByName(order: 'asc')

        def carriers = [[name: "Verizon", email: "vtext.com"], [name: "Sprint", email: "messaging.sprintpcs.com"],
                        [name: "Ting (T-Mobile)", email: "tmomail.net"], [name: "Virgin Mobile", email: "vmobl.com"],
                        [name: "Ting (Sprint)", email: "messaging.ting.com"], [name: "T-Mobile", email: "tmomail.net"]]

        def sipLocations = ExtensionLocation.findAllBySipAllowed(true)


        //noinspection GroovyAssignabilityCheck
        render template: 'index', model: [agents: [enabled: enabledAgents, disabled: disabledAgents],
                                          peers: peers, carriers: carriers, sipLocations: sipLocations]
    }

    def agents(){
        Agent user = getAuthenticatedUser() as Agent

        //the logged in user should be excluded from this list for simplicity's sake
        def enabledAgents = Agent.findAllByEnabledAndIdNotEqual(true, user.id, [sort: "name", order: "asc"])
        def disabledAgents = Agent.findAllByEnabledAndIdNotEqual(false, user.id, [sort: "name", order: "asc"])

        render template: '/agent/adminList', model: [agents: [enabled: enabledAgents, disabled: disabledAgents]]
    }

    def peers(){
        def peers = SIPPeer.listOrderByName(order: 'asc')

        render template: '/sippeer/peerList', model: [peers: peers]
    }

    def grant(Agent agent){
        String message = "Successfully granted $agent.name ($agent.id) Admin priviledges"
        def status = HttpStatus.OK

        try {
            AgentRole.create agent, Role.findByAuthority('ROLE_ADMIN')
        }
        catch(Exception e){
            message = "Could not grant $agent.name ($agent.id) Admin priviledges:\n\t$e.message"
            status = HttpStatus.INTERNAL_SERVER_ERROR

            log.error message
        }

        render status: status, text: message
    }

    def revoke(Agent agent){
        String message = "Successfully revoked $agent.name ($agent.id) of their Admin priviledges"
        def status = HttpStatus.OK

        try {
            AgentRole.remove agent, Role.findByAuthority('ROLE_ADMIN')
        }
        catch(Exception e){
            message = "Could not revoke $agent.name ($agent.id) of their Admin priviledges:\n\t$e.message"
            status = HttpStatus.INTERNAL_SERVER_ERROR

            log.error message
        }

        render status: status, text: message
    }

    def resetPassword(Agent agent){
        String newPassword = AgentService.generatePassword()
        agent.password = newPassword
        agent.save()

        AgentService.emailPasswordReset(agent, newPassword)

        render
    }

}

