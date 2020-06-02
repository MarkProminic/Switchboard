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

@Transactional(readOnly = true)
class QueueMemberController {
    def springSecurityService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond QueueMember.list(params), model:[queueMemberCount: QueueMember.count()]
    }

    def show(QueueMember queueMember) {
        respond queueMember
    }

    def create() {
        respond new QueueMember(params)
    }

    def edit(QueueMember queueMember) {
        respond queueMember
    }

    @Transactional
    def addAgentToQueue(){
        Agent agent = Agent.findById(params.agent)

        if(agent){
            QueueMember member = new QueueMember(membername: agent.name, memberInterface: agent.retrievePrimaryExtension()?.getMemberInterface(),
                    queueName: params.queue, stateInterface: agent.retrievePrimaryExtension()?.getStateInterface()).save(flush: true, failOnError: true)
        }

        redirect controller: 'queue', action: 'index'
    }

    @Transactional
    def togglePauseMember(){
        QueueMember member = QueueMember.findByUniqueid(params.memberId)
        member.paused = (member.paused != 0)? 0:1
        member.save(flush: true, failOnError: true)

        render status: 200
    }

    @Transactional
    def removeAgent(){
        QueueMember member = QueueMember.findByUniqueid(params.memberId)

        String queue = member.queueName

        member.delete(flush: true, failOnError: true)

        respond queue: queue, user: getAuthenticatedUser() as Agent
    }

}
