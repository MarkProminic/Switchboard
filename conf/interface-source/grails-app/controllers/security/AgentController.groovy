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

import dialer.CallAs
import grails.gorm.transactions.Transactional
import groovy.json.JsonBuilder
import org.asteriskjava.manager.action.CommandAction
import org.asteriskjava.manager.response.CommandResponse
import switchboard.cloud.Extension
import switchboard.cloud.ExtensionLocation

@Transactional(readOnly = false)
class AgentController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def AMIService
    def AgentService
    def ExtensionService
    def FollowmeService
    def CallService
    def ContactService
    def VoicemailService
    def ConfigLineService

    def springSecurityService

    def login(){
        Agent agent = Agent.findByAgentId(params.id)
        if(!AMIService.connected){
            String host = ConfigLineService.getConfigValue("general", "asterisk_host")
            String managerUsername = ConfigLineService.getConfigValue("manager", "username")
            String managerPassword = ConfigLineService.getConfigValue("manager", "password")
            
            AMIService.connect(host, managerUsername, managerPassword)
        }
        AMIService.login(agent)

        redirect action: "index"
    }

    def logout(){
        getAuthenticatedUser()?.loggedIn = false

        redirect "/logout"
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Agent.list(params), model:[agentCount: Agent.count()]
    }

    def show(Agent agent) {
        respond agent
    }

    def create() {
        respond new Agent(params)
    }

    def createNew(String agentName, String agentEmail, String extensionPrefix, boolean smsOptIn, String pager, String carrier){
        String password = AgentService.generatePassword()

        def result = AgentService.createNewAgent(agentName, agentEmail, password)

        Agent agent = result.agent
        def status = result.status
        String message = result.message

        if(result.success){

            result = ExtensionService.createExtension(extensionPrefix, agent.id)

            if(result){
                result = FollowmeService.createFollowme(agent.id)

                if(result){
                    result = CreateCallAs(agent.id)

                    if(result) {
                        AgentService.emailNewAgent(agentName, agentEmail, password)

                        VoicemailService.createAgentVoiceMail(agent.id, agentEmail, smsOptIn, pager, carrier)
                    }
                }
            }
        }

        render status: status, message: message
    }

    def testEmail(){
        test()
        render text:"It worked!", status: 200
    }

    def edit(Agent agent) {
        respond agent
    }

    @Transactional
    def update(Agent agent) {
        if (agent == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (agent.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond agent.errors, view:'edit'
            return
        }

        agent.assignPrimaryExtension(params.primary)
        agent.save flush:true

        request.withFormat {
            form multipartForm {
//                flash.message = message(code: 'default.updated.message', args: [message(code: 'agent.label', default: 'Agent'), agent.id])
                redirect controller: 'queue', action: 'index'
            }
            '*'{ respond agent, [status: OK] }
        }
    }

    @Transactional
    def delete(Agent agent) {
        def agentRoles = AgentRole.findAllByAgent(agent)

        agentRoles.each {
            it.delete()
        }

        agent.extensions.each {
            it.delete()
        }

        agent.ringGroups.each {
            it.delete()
        }

        agent.delete flush:true
    }

    def callAgent(Long agentId, String locationNumber){
        Agent toAgent = Agent.findById(agentId)
        Agent user = getAuthenticatedUser() as Agent

        Extension toAgentExt = locationNumber == 'primary' ? toAgent.primaryExtension : Extension.get(Long.parseLong(locationNumber))

        def status = 200

        log.info "${user.name} is calling $toAgent.name at $toAgentExt.name."

        CallService.placeCall(toAgentExt.number, 'Self', user)

        render status: status, message: "Call placed to ${toAgent.name} at $toAgentExt.name"
    }

    def refreshAgentList(){
        def agents = Agent.findAllByEnabled(true)
        Agent user = getAuthenticatedUser() as Agent

        boolean userOnCall = CallService.listActiveCalls(user).size() > 0

        agents.remove(user)

        render template: "agentList", model: [agents: agents, userOnCall: userOnCall]
    }

    def getNewAgentExtensionOptions(){
        def optionList = ExtensionLocation.findAllBySipAllowed(true)

        def json = new JsonBuilder()

        json.options optionList, { ExtensionLocation location ->
            value location.prefix
            name location.name
        }

        render json
    }

    def testIVR(){
        CommandResponse response = CallService.sendAction(new CommandAction("ari show apps"), 10000)

        log.info "Command Action responded with '${response.message}'."
        render text: response.message
    }

    def agentTest(){
        render template: "agentList", model:[agent: Agent.findByName("Luis Alcantara")]
    }

    def disable(Agent agent){
        agent.enabled = false

        AgentService.deleteQueueMembers(agent)

        agent.save()
    }

    def reenable(Agent agent){
        agent.enabled = true

        agent.save()
    }

    private boolean CreateCallAs(Long agentId){
        boolean success = true
        Agent agent = Agent.get(agentId)

        String internationalCID = ConfigLineService.getConfigValue("dialer", "international_cid")
        String domesticCID = ConfigLineService.getConfigValue("dialer", "domestic_cid")

        CallAs internationalID = new CallAs(name: "Self", type: "International", callerID: "\"$agent.name\" <$internationalCID>").save()
        CallAs domesticID = new CallAs(name: "Self", type: "Domestic", callerID: "\"$agent.name\" <$domesticCID>").save()

        agent.addToCallerIds(internationalID)
        agent.addToCallerIds(domesticID)

        try {
            agent.save()
        }
        catch (Exception e) {
            success = false
            log.error "Creation of Caller ID entries for agent $agent.name has failed:\n\t$e.message"
        }

        success
    }

}