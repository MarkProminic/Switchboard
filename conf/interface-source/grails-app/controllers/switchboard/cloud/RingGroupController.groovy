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


import security.Agent

class RingGroupController {

    def index() { }

    def create(String name){
        def groupExtensions
        Agent user = getAuthenticatedUser()

        int status = 200

        groupExtensions = Extension.findAllByIdInList(params.'extensions[]' as List<String>)

        RingGroup group = new RingGroup(name: name)

        try {
            group.save(failOnError: true)
        } catch(Exception e){
            log.error "Error occurred saving RingGroup '$name': ${e.message}"
            status = 500
        }

        groupExtensions.each { Extension extension ->
            try {
                group.addToExtensions(extension)

                extension.save(failOnError: true)
                group.save(failOnError: true)
            } catch (Exception e) {
                log.error "Error occurred adding Extensions to RingGroup '$name': ${e.message}"
                status = 500
            }
        }

        try {
            user.addToRingGroups(group)
            user.save(failOnError: true)
        } catch(Exception e){
            log.error "Error adding RingGroup '$name' to user '${user.name}': ${e.message}"
            status = 500
        }

        log.info "Successfully created RingGroup '$name' with Extensions ${group.extensions.displayName} for ${user.name}."

        respond group: group, user: user
    }

    def delete(String id){
        Agent user = getAuthenticatedUser()

        RingGroup rg = RingGroup.get(id as Long)

        FollowMeNumber.findAllByName(user.id as String).each { FollowMeNumber step ->
            step.removeFromRingGroups(rg)
        }

        user.removeFromRingGroups(rg)

        rg.delete()

        render status: (rg.exists()) ? 500 : 200, message: (rg.exists()) ? "Error deleteing Ring Group '${rg.name}'." : "Ring Group '${rg.name}' successfully deleted."
    }

    def removeExtension(String group, String extension){
        RingGroup ringGroup = RingGroup.findById(Long.parseLong(group))
        int status = 200
        String message = ""

        if(ringGroup) {
            Extension rgExtension = ringGroup.extensions.find { Extension ext ->
                ext.id = Long.parseLong(extension)
            }

            if (rgExtension) {
                ringGroup.removeFromExtensions(rgExtension)
                log.info "Removing ${rgExtension.displayName} from ${ringGroup.name}."
            } else {
                log.warn "No Extension with id '$extension' is a part of ${ringGroup.name}. Returning success as this is a removal"
            }
            message = "${rgExtension.displayName} successfully removed from ${ringGroup.name}."
        } else {
            status = 500
            log.error "No RingGroup with id '$group' exists!"
        }

        render status: status, message: message
    }

    def update(RingGroup group){
        def updateParams = params
        def extensions = []

        if(group.extensions) {
            Extension.findAllByIdInList(group.extensions.id as List<String>).each {
                group.removeFromExtensions(it)
            }
        }

        params.each {
            println "Param: $it"
        }

        if(params."extensions[]") {
            if (params."extensions[]".class == String) {
                extensions << params."extensions[]"
            } else {
                extensions.addAll(params."extensions[]")
            }
        }

        if(extensions) {
            def groupExtensions = Extension.findAllByIdInList(extensions)

            log.debug "Ring Group $group will now have ${groupExtensions.size()} Extensions."

            groupExtensions.each {
                group.addToExtensions(it)
            }
        }

        group.name = params.name ?: group.name

        try {
            group.save(flush: true, failOnError: true)
        }
        catch(Exception e) {
            log.error "Error updating group $group."

            group.discard()
        }

        respond group: group, user: getAuthenticatedUser() as Agent
    }
}
