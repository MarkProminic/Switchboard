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

class FollowmeController {

    def springSecurityService

    def index() { }

    def create(){
        Agent user = getAuthenticatedUser() as Agent

        FollowMeConfig userConfig = FollowMeConfig.findOrSaveByName(user.id as String)

//        String paramsList = ""
//
//        params.each {paramsList += "$it, "}
//
//        log.debug "Params: $paramsList"

        def ringGroups = []
        def extensions = []

        if(params."ringGroups[]") {
            if (params."ringGroups[]".class == String) {
                ringGroups << params."ringGroups[]"
            } else {
                ringGroups.addAll(params."ringGroups[]")
            }
        }

        if(params."extensions[]") {
            if (params."extensions[]".class == String) {
                extensions << params."extensions[]"
            } else {
                extensions.addAll(params."extensions[]")
            }
        }

        def stepRingGroups = []
        def stepExtensions = []

        if(ringGroups) {
            stepRingGroups = RingGroup.findAllByIdInList(ringGroups)
        }

        if(extensions) {
            stepExtensions = Extension.findAllByIdInList(extensions)
        }

        int existingSteps = FollowMeNumber.countByName(user.id as String)

        FollowMeNumber step = new FollowMeNumber(name: user.id as String, ordinal: (existingSteps + 1) as String)

        if (stepRingGroups) {
            stepRingGroups.each { RingGroup group ->
                step.addToRingGroups(group)
            }
        }

        if(step.validate()) {
            log.debug "Added ${stepRingGroups.size()} Ring Group(s) to the new Follow Me Step."
        }
        else {
            String errorMessage = ""

            step.errors.allErrors.each {
                errorMessage += "$it \n"
            }

            log.error "Error adding Ring Groups to new step for ${user.name}: $errorMessage"

            respond view: "error", model: [errorMessage: "Error adding Ring Groups to new step for ${user.name}."]
        }

        if (stepExtensions) {
            stepExtensions.each { Extension extension ->
                step.addToExtensions(extension)
            }
        }

        if(step.validate()) {
            log.debug "Added ${stepExtensions.size()} Extension(s) to the new Follow Me Step."
        }
        else{
            String errorMessage = ""

            step.errors.allErrors.each {
                errorMessage += "$it \n"
            }

            log.error "Error adding Extensions to new step for ${user.name}: $errorMessage"

            respond view: "error", model: [errorMessage: "Error adding Extensions to new step for ${user.name}: ${e.message}: "]
        }

        step.generatePhoneNumber()
        step.save(flush: true)

        respond step: step, user: user
    }

    def show(FollowMeNumber step){
        Agent user = getAuthenticatedUser() as Agent

        respond user: user, step: step
    }

    def showFirstStep(){
        Agent user = getAuthenticatedUser() as Agent

        FollowMeNumber step = FollowMeNumber.findByNameAndOrdinal(user.id as String, "1")

        render view: "show", model:[user: user, step: step]
    }

    def delete(FollowMeNumber step) {
        def laterSteps = FollowMeNumber.findAllByOrdinalGreaterThan(step.ordinal, [sort: "ordinal", order: "asc"])
        int status = 200

        try {
            step.delete(flush: true)

            log.info "Removed step ${step.ordinal} from Follow Me '${step.name}'."

            log.debug "Updating ${laterSteps.size()} step(s) to preserve sequence."

            try {
                laterSteps?.each { FollowMeNumber laterStep ->
                    laterStep.ordinal = (Integer.parseInt(laterStep.ordinal) - 1) as String
                    laterStep.save(flush: true, failOnError: true)
                }
            }
            catch(Exception e){
                status = 500

                log.error "Error updating steps to preserve sequence: ${e.message}"
            }
        }
        catch(Exception e1){
            status = 500

            log.error "Error deleting step ${step.ordinal}(${step.id}) from Follow Me '${step.name}': ${e1.message}"
        }

        render status: status
    }

    def updateSteps(){
        Agent user = getAuthenticatedUser()

        def steps = FollowMeNumber.findAllByName(user.id as String, [sort: "ordinal", order: "asc"])

        int newOrdinal = 1

        steps.each{ FollowMeNumber step->
            if((step.extensions.size() + step.ringGroups.size()) < 1){
                log.info "Deleting step ${step.name}(${step.ordinal}), as it no longer has any valid elements."

                step.delete(flush:true)
            }
            else {
                step.ordinal = newOrdinal
                newOrdinal++

                step.generatePhoneNumber()

                step.save(flush: true)
            }
        }

        respond steps: FollowMeNumber.findAllByName(user.id as String, [sort: "ordinal", order: "asc"]), user: user
    }

    def updateStep(FollowMeNumber step){
        def ringGroups = []
        def extensions = []

        if(step.extensions) {
            Extension.findAllByIdInList(step.extensions.id as List<String>).each {
                step.removeFromExtensions(it)
            }
        }

        if(step.ringGroups) {
            RingGroup.findAllByIdInList(step.ringGroups.id as List<String>).each {
                step.removeFromRingGroups(it)
            }
        }

        if(params."ringGroups[]") {
            if (params."ringGroups[]".class == String) {
                ringGroups << params."ringGroups[]"
            } else {
                ringGroups.addAll(params."ringGroups[]")
            }
        }

        if(params."extensions[]") {
            if (params."extensions[]".class == String) {
                extensions << params."extensions[]"
            } else {
                extensions.addAll(params."extensions[]")
            }
        }

        if(ringGroups) {
            def stepRingGroups = RingGroup.findAllByIdInList(ringGroups)

            log.debug "Follow Me step $step will now have ${stepRingGroups.size()} Ring Groups."

            stepRingGroups.each {
                step.addToRingGroups(it)
            }

        }

        if(extensions) {
            def stepExtensions = Extension.findAllByIdInList(extensions)

            log.debug "Follow Me step $step will now have ${stepExtensions.size()} Extensions."

            stepExtensions.each {
                step.addToExtensions(it)
            }
        }

        step.generatePhoneNumber()

        if(step.ordinal == "1"){
            Agent user = getAuthenticatedUser() as Agent

            if(step.extensions.size() == 1){
                user.primaryExtension = step.extensions[0]
            }
        }

        try {
            step.save(flush: true, failOnError: true)
        }
        catch(Exception e) {
            log.error "Error updating step $step."

            step.discard()
        }

        respond step: step, user: getAuthenticatedUser() as Agent
    }

}
