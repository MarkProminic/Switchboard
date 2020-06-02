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
import grails.rest.RestfulController
import security.Agent

@Transactional(readOnly= true)
class QueueController extends RestfulController<Queue>{

    QueueController(){
        super(Queue)
    }

    static responseFormats = ['json']

    def springSecurityService

    def CallService

    def index() {
        Agent user = authenticatedUser as Agent
        [callRecordList: user.pendingCallRecords, user: user]
    }

    def refreshQueueList(){
        respond Queue.list(sort: 'name')
    }

    @Transactional
    def addMember(Long agentId, String queueName){
        Agent agent = Agent.get(agentId)
        QueueMember member = new QueueMember(membername: agent.name, memberInterface: agent.primaryExtension?.getMemberInterface(),
                queueName: queueName, stateInterface: agent.primaryExtension?.getStateInterface()).save(flush: true, failOnError: true)
    }

    def refreshQueueCalls(){
        respond queueCalls: CallService.listQueueCalls()
    }

    @Transactional
    def updateLocal(String name, String MACAddress){
        Extension extension = Extension.findByName(name)
        if(extension) {
            SIPPeer peer = SIPPeer.findByName(extension.number)
            if (MACAddress) {
                peer.MACAddress = MACAddress
                peer.save(flush: true, failOnError: true)
                render status: 200
            }
        }
        else render status: 400
    }

    @Transactional
    def updateRemote(String name, String phoneNumber){
        RemotePeer peer = RemotePeer.findByName(name)
        if(phoneNumber) {
            peer.phoneNumber = phoneNumber
            peer.save(flush: true, failOnError: true)
            render status: 200
        }
        else render status: 400
    }

    @Transactional
    def switchAndLogout(String name){
        Agent agent = getAuthenticatedUser()

        if(name){
            agent.assignPrimaryExtension(Extension.findByName(name)?.number)
        }
        else{
            agent.assignPrimaryExtension(Extension.findByTypeAndAgent('Remote', agent)?.number)
        }

        redirect uri: "/logout"
    }

    @Transactional
    def logoutCompletely(){
        Agent agent = getAuthenticatedUser()

        def memberList = QueueMember.findAllByMembername(agent.name)
        memberList.each { member ->
            member.delete(flush: true, failOnError: true)
        }

        redirect uri: "/logout"
    }

    def createSimple(){}

    def createFull(){}

    def show(Queue queue) {
        respond queue
    }

    def create() {
        respond new Queue(params)
    }

    def edit(Queue queue) {
        respond queue
    }

    def filterAgentList(){
        def agentList = new ArrayList<Agent>()
        def queues = new ArrayList<String>()

        if(params.'idList[]' != null) {
            if (params.'idList[]'.class == String){
                queues << params.'idList[]'
            } else {
                params.'idList[]'.each {
                    queues << it
                }
            }

            def memberList = null

            if (queues.contains("All")) {
                memberList = QueueMember.list()
            } else {
                memberList = QueueMember.findAllByQueueNameInList(queues)
            }

            memberList.each { member ->
                Agent agent = Agent.findByName(member?.membername)
                if (!agentList.contains(agent)) {
                    agentList << agent
                }
            }
        } else {
            agentList = Agent.findAllByExtensionsIsNotEmpty()
        }

        render template: "agentList", model:[agentList: agentList, loggedInAgent: getAuthenticatedUser()]
    }
}
