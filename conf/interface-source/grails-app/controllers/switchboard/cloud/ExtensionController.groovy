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
class ExtensionController {

    def dialPlanService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Extension.list(params), model:[extensionCount: Extension.count()]
    }

    def show(Extension extension) {
        respond extension
    }

    def create() {
        respond new Extension(params)
    }

    @Transactional
    def updateRemote(){
        RemotePeer peer = emotePeer.findById(params.id)
        peer.phoneNumber = params.phoneNumber
        peer.save(flush: true, failOnError: true)

        dialPlanService.updateRemotePeerPhoneNumber(peer?.phoneNumber, params.phoneNumber)

        redirect controller: 'queue', action: 'index'
    }

    @Transactional
    def saveLocal() {
        SIPPeer sippeer = new SIPPeer(name: params.number, secret: params.secret)
        Extension extension = new Extension(number: params.number, type:'Local', name: params.name)

        Agent agent = Agent.findById(params.agent)
        extension.agent = agent

        if(Extension.countByAgent(agent) == 0){
            extension.primaryExt = true
        }

        extension.save(flush: true, failOnError: true)
        sippeer.save(flush: true, failOnError: true)

        redirect controller: 'agent', action: 'edit', id: params.agent
    }

    @Transactional
    def saveRemote(){
        Extension extension = new Extension(number: params.number, type: 'Remote', name: params.name)

        Agent agent = Agent.findById(params.agent)
        if(Extension.countByAgent(agent) == 0){
            extension.primaryExt = true
        }

        extension.agent = agent
        extension.save(flush: true, failOnError: true)

        String id = dialPlanService.createRemotePeer(params.number, params.phoneNumber)

        RemotePeer remotePeer = new RemotePeer(exten: params.number, extensions_id: id, phoneNumber: params.phoneNumber).save(flush: true, failOnError: true)

        redirect controller: 'agent', action: 'edit', id: params.agent
    }

    def edit(Extension extension) {
        SIPPeer localPeer
        RemotePeer remotePeer

        if(extension.type == 'Local'){
           localPeer = SIPPeer.findByName(extension.number)
        }
        else {
            remotePeer = RemotePeer.findByExten(extension.number)
        }

        [localPeer: localPeer, remotePeer: remotePeer]
    }

}
