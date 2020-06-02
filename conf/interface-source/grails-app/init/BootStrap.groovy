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


import grails.util.Environment
import security.Agent
import security.AgentRole
import security.RequestMap
import security.Role
import switchboard.cloud.Extension
import switchboard.cloud.ExtensionLocation

class BootStrap {

    def ManagerService
    def ConfigLineService

    def init = { servletContext ->

        log.info "Beginning start-up procedures (${Environment.current})"

        Role adminRole = Role.findOrSaveByAuthority("ROLE_USER")
        Role userRole = Role.findOrSaveByAuthority("ROLE_ADMIN")
        Role.findOrSaveByAuthority("ROLE_EMSUPPORT")

        ManagerService.connect()

        Agent admin = Agent.findByName("Default Admin")

        if(!admin) {
            log.info("No default Admin account found, creating.")

            admin = new Agent(name: "Default Admin", username: "admin", password: "Abc123")
            admin.setId(1)

            admin.save()
        }

        def assignedRoles = AgentRole.findAllByAgent(admin).role

        if (!(adminRole in assignedRoles)) {
            AgentRole.create admin, adminRole
        }

        if (!(userRole in assignedRoles)) {
            AgentRole.create admin, userRole
        }

        if(!(admin.extensions)) {
            Extension placeholder = new Extension(name: "Placeholder")
            placeholder.save()

            admin.addToExtensions(placeholder)
        }

        log.info "Creating anonymous request map entries where needed."

        for (String url in ['/error', '/index', '/index.gsp', '/**/favicon.ico', '/shutdown',
                            '/**/js/**', '/**/css/**', '/**/images/**', '/assets/**','/login',
                            '/login.*', '/login/*', '/logout', '/logout.*', '/logout/*', '/CRMDialer'])
        {
            RequestMap.findOrSaveWhere(url: url, configAttribute: 'permitAll')
        }

        log.info "Creating user secured request map entries where needed."

        for (String url in [
                '/', '/queue/**', '/queueMember/**', '/agent/**', '/extension/**', '/remotePeer/**',
                '/inboundRoute/**', '/asteriskCall/**', '/topic/**', '/queue/**', '/app/**', '/stomp/**',
                '/conferenceRoom/**', '/voicemail/**', '/contact/**', '/recording/**', '/followme/**', '/ringGroup/**',
                '/recordings/**', '/voicemails/**', '/callRecord/**', '/configLine/**', '/extensionLocation/**', '/user/**']) {
            RequestMap.findOrSaveWhere(url: url, configAttribute: 'ROLE_USER')
        }

        for (String url in ['/admin/**']) {
            RequestMap.findOrSaveWhere(url: url, configAttribute: 'ROLE_ADMIN')
        }


        ExtensionLocation.findOrSaveWhere(prefix: "1", name: "Champaign Office", sipAllowed: true)
        ExtensionLocation.findOrSaveWhere(prefix: "2", name: "Rantoul Office", sipAllowed: true)
        ExtensionLocation.findOrSaveWhere(prefix: "3", name: "Burlington Office", sipAllowed: true)
        ExtensionLocation.findOrSaveWhere(prefix: "4", name: "Home Office", sipAllowed: true)
        ExtensionLocation.findOrSaveWhere(prefix: "5", name: "Bucharest Office", sipAllowed: true)
        ExtensionLocation.findOrSaveWhere(prefix: "6", name: "Savoy Office", sipAllowed: true)
        ExtensionLocation.findOrSaveWhere(prefix: "7", name: "LAX Office", sipAllowed: true)

        for (int i=1; i<10; i++) {
            ExtensionLocation.findOrSaveWhere(prefix: "8$i" as String, name: "Landline", sipAllowed: false)
            ExtensionLocation.findOrSaveWhere(prefix: "9$i" as String, name: "Mobile", sipAllowed: false)
        }

        Set<String> voicemailFolders = ConfigLineService.getConfigValue("voicemail", "folders")?.split(",")
        Set<String> voicemailDisplayFolders = ConfigLineService.getConfigValue("voicemail", "display_folders")?.split(",")

        if(!voicemailFolders.containsAll(voicemailDisplayFolders)){
            Set<String> voicemailInvalidFolders = voicemailDisplayFolders - voicemailFolders
            log.error("The following directories are specified as display folders for Voicemail, but not general folders:\n\t" +
                    "${voicemailInvalidFolders.join(", ")}\n\t" +
                    "This needs to be corrected for voicemail discovery to function properly")
        }

        if(voicemailFolders != voicemailDisplayFolders){
            Set<String> voicemailNonDisplayFolders = voicemailFolders - voicemailDisplayFolders
            log.warn("The following folder(s) are specified as Voicemail folders but are not displayed:\n\t" +
                    "${voicemailNonDisplayFolders.join(", ")}\n\t" +
                    "This may be intended")
        }



        log.info "Application is now available for service (${Environment.current})."
    }

    def destroy = {

        log.info "Application shutting down"

        ManagerService.disconnect()

    }
}
