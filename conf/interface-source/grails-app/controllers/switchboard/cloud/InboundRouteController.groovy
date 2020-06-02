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
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.http.HttpStatus
import security.Agent

@Transactional
class InboundRouteController {

    def DialPlanService

    def index(){
        def destOptions = []

        render template:"/asteriskSettingsModal", model:[routes: InboundRoute.list(),
                                                         destOptions: Agent.findAllByEnabled(true),
                                                         trunks: SIPPeer.findAllByType('peer')]
    }

    def destinationOptions(String type, String routeId){
        def availableOptions = []

        params.type = type

        if(type == 'Agent'){
            availableOptions = Agent.findAllByEnabled(true)
        }
        else if(type == 'Queue'){
            availableOptions = Queue.list()
        }
        else {
            params.type = ""

            log.error "Type '$type' is invalid, check UI options."
        }

        render template: "/inboundRoute/destinationOptions", model:[destOptions: availableOptions, routeId: routeId]
    }

    @Secured("hasRole('ROLE_ADMIN')")
    def create(String number, String type, String destination, String description) {
        String didNumber = (number.startsWith("1")) ? number.substring(1): number

        InboundRoute route = new InboundRoute(didNumber: didNumber, destinationType: type, destinationName: destination, description: description)
        try {
            route.save(flush: true, failOnError: true)
        } catch(Exception e){
            render status: 400, message: e.message
        }

        render template: "route", model: [route: route]
    }

    @Secured("hasRole('ROLE_ADMIN')")
    def update(){
        InboundRoute route = InboundRoute.get(params.id)
        def destOptions = []

        if(route) {
            def destination

            if (params.type == 'Queue') {
                destination = Queue.findByName(params.destNameId)
                destOptions = Queue.list()

            }
            else if (params.type == 'Agent') {
                destination = Agent.get(Long.parseLong(params.destNameId))
                destOptions = Agent.findAllByEnabled(true)
            }
            else {
                log.error "Unsupported Inbound Route type '$params.type'. Check form 'type' select"
            }

            if (destination) {
                route.destinationName = destination.name
                route.destinationType = params.type
            }
            else {
                log.error "No $params.type found with id '$params.destNameId'. Check form 'destination-name' select"
            }

            route.description = params.description

            route.save()
        }

        render template: "route", model:[route: route, destOptions: destOptions]
    }

    @Secured("hasRole('ROLE_ADMIN')")
    def delete(InboundRoute route){
        boolean success = false

        if(route) {
            success = DialPlanService.deleteInboundRoute(route)

            if(success){
                route.delete()

                log.info "Inbound route for '$route.didNumber' to $route.destinationName(${route.destinationType}) deleted"
            }
        }
        else {
            log.error "No route found with id '${params.id}', cannot delete"
        }

        render status: success ? HttpStatus.OK : HttpStatus.BAD_REQUEST
    }
}
