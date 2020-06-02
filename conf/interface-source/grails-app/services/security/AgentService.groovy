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

import org.springframework.http.HttpStatus
import switchboard.cloud.FollowMeConfig
import switchboard.cloud.FollowMeNumber
import switchboard.cloud.QueueMember

class AgentService {

    def MailService
    def DialPlanService
    def AgentService

    String generatePassword(int length = 8){
        def password = ""

        List<String> chars = (('A'..'Z')+('0'..'9')+('a'..'z')+['!', '@', '#', '$', '%', '^', '&', '*', '?', '-', '+', '_'])
        int charsetSize = chars.size()

            def random = new Random()

            for (int i = 0; i < length; i++) {
                int idx = random.nextInt(chars.size())
                password += chars[idx]

                if(length < charsetSize) {
                    chars.remove(idx)
                }
            }

        password
    }

    void emailNewAgent(String agentName, String email, String password){
        def bodyProps = [name: agentName, email: email, password: password]

        String body = MailService.buildBody("New Agent", bodyProps)
        String subject = "Welcome to the new Prominic Phone Interface!"

        def props = [to: email, body: body, subject: subject]

        MailService.sendMessage(props)
    }

    void emailPasswordReset(Agent agent, String password){
        def bodyProps = [agent: agent, password: password]

        String body = MailService.buildBody("Password Reset", bodyProps)
        String subject = "Your password for the Prominic Phone Interface has been reset"

        def props = [to: agent.username, body: body, subject: subject]

        MailService.sendMessage(props)
    }
    
    List<FollowMeNumber> findFollowmeSteps(Agent agent){
        FollowMeConfig agentFMConfig = FollowMeConfig.findByName(agent.id as String)
        List<FollowMeNumber> followmeSteps = []

        if(agentFMConfig){
            followmeSteps = FollowMeNumber.findAllByName(agentFMConfig.name, [sort: "ordinal", order: "asc"])
        }
        else {
            agentFMConfig = new FollowMeConfig(name: agent.id as String)
            agentFMConfig.save()

            DialPlanService.createFollowMe(agent)
        }

        if(!followmeSteps){
            followmeSteps = [new FollowMeNumber(name: agent.id as String, phoneNumber: agent.primaryExtension.phoneNumber, ordinal: 1).save()]
        }

        followmeSteps
    }

    Map createNewAgent(String name, String email, String password){
        def status = HttpStatus.OK
        String message = "Agent successfully created"

        Long agentId = Agent.listOrderById(order: "desc", max: 1)[0].id + 1

        Agent agent = new Agent(name: name, password: password, username: email)
        agent.setId(agentId)

        Agent sameName = Agent.findByName(name)
        if(sameName){
            status = HttpStatus.CONFLICT // code 409
            if(sameName.enabled) {
                message = "Another Agent already exists with name '$name'. If this is not in error, consider making the new Agent distinct with a middle initial"
            }
            else {
                message = "A disabled Agent already exists with that name. This Agent needs to continue to exist for Call Records they were involved with. " +
                        "If this Agent's name ($name) was not duplicated in error, consider making the new Agent distinct with a middle initial"
            }
        }
        else if(Agent.findByUsername(email)) {
            status = HttpStatus.CONFLICT // code 409
            message = "Another Agent already exists with the email '$email'. An Agent's email is also their username so there cannot be duplicates"
        }
        else{
            AgentRole role
            try {
                agent.save(failOnError: true)
                role = AgentRole.create(agent, Role.findByAuthority("ROLE_USER"))      // grant user role to new agent
            }
            catch(Exception e){
                agent.delete()
                role.delete()

                log.error "Failed to create agent $name and/or assign their user role: ${e.message}"
                status = HttpStatus.INTERNAL_SERVER_ERROR
                message = "Agent creation failed"
            }
        }

        [success: (status == HttpStatus.OK), status: status, message: message, agent: agent]
    }

    void deleteQueueMembers(Agent agent){
        def agentMembers = QueueMember.findAllByMembername(agent.name)

        log.info "Deleting ${agentMembers.size()} QueueMembers belonging to $agent.name"

        agentMembers.each {
            it.delete()
        }
    }

}
