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

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import grails.gorm.transactions.Transactional
import grails.util.Environment
import groovy.sql.Sql
import org.asteriskjava.manager.action.CommandAction
import security.Agent
import switchboard.cloud.InboundRoute
import switchboard.cloud.Queue

@Transactional
class DialPlanService implements GrailsConfigurationAware {

    def CallService
    def ConfigLineService

    def inboundRouteContext = 'inbound-routes'

    Sql sql

    private sqlConnectionString
    private sqlUsername
    private sqlPassword
    private sqlDriver

    @Override
    void setConfiguration(Config config){

        sqlConnectionString = config.getProperty("dataSource.url")
        sqlUsername = config.getProperty("dataSource.username")
        sqlPassword = config.getProperty("dataSource.password")
        sqlDriver = config.getProperty("dataSource.driverClassName")

    }

    def createRemotePeer(String exten, String phoneNumber){
        ConnectToSQL()
        String extensions_id = CreateOrUpdateRemotePeer(exten, phoneNumber)
        sql.close()

        extensions_id
    }

    def updateRemotePeerPhoneNumber(String exten, String newNumber){
        ConnectToSQL()
        CreateOrUpdateRemotePeer(exten, newNumber)
        sql.close()
    }

    def deletePeer(String extension){
        ConnectToSQL()
        DeleteDialPlanRows(extension)
        sql.close()
    }

    def createLocalPeer(String exten){
        ConnectToSQL()
        String agent_id = InsertLocalPeerRows(exten)
        sql.close()

        agent_id
    }

    def createInboundRoute(InboundRoute route){
        ConnectToSQL()
        InsertInboundRouteRows(route.didNumber, route.destinationType, route.destinationName)
        sql.close()
    }

    def updateInboundRoute(InboundRoute route, destination){
        ConnectToSQL()
        UpdateInboundRouteRows(route, destination)
        sql.close()
    }

    boolean deleteInboundRoute(InboundRoute route){
        ConnectToSQL()
        boolean success = DeleteInboundRouteRows(route)
        sql.close()

        success
    }

    def createVoicemail(String agentId, String vmPIN, String vmUser, String email){
        ConnectToSQL()
        InsertVoicemailConfigRow(agentId, vmPIN, vmUser, email)
        sql.close()

        try {
            CallService.sendAction(new CommandAction("voicemail reload"))
        } catch (Exception e){}
    }

    def createFollowMe(Agent user){
        ConnectToSQL()
        CreateFollowMeRows(user)
        sql.close()
    }

    private CreateFollowMeRows(Agent user) {
        def followmeRows = sql.rows("SELECT * FROM extensions WHERE context=? AND exten=?",
                ['agents', user.id as String])

        if(followmeRows.size() < 2) {
            sql.execute("DELETE FROM extensions WHERE context=? AND exten=?",
                    ['agents', user.id as String])

            sql.execute("INSERT INTO extensions(context, exten, priority, app, appdata) VALUES(?, ?, ?, ?, ?)",
                    ["agents", user.id as String, "1", "Followme", "$user.id,an" as String])

            sql.execute("INSERT INTO extensions(context, exten, priority, app, appdata) VALUES(?, ?, ?, ?, ?)",
                    ["agents", user.id as String, "2", "Voicemail", "$user.id@prominic" as String])
        }
    }

    private Sql ConnectToSQL(){
        try {
            sql = Sql.newInstance(
                    url: sqlConnectionString,
                    user: sqlUsername,
                    password: sqlPassword,
                    driver: sqlDriver,
            )
        }
        catch(Exception e){
            log.error "Couldn't connect to host configured in applicaiton.yml. " +
                    "Dialplan insertions and updates (as well as most dataSource interactions most likely): ${e.message}"
            return null
        }

        String tableCreationQuery = "CREATE TABLE IF NOT EXISTS extensions(" +
                                        "id int8 PRIMARY KEY, " +
                                        "context varchar(40) NOT NULL, " +
                                        "exten varchar(40) NOT NULL, " +
                                        "priority varchar(10) NOT NULL, " +
                                        "app varchar(40) NOT NULL, " +
                                        "appdata varchar(256) NOT NULL)"

        sql.execute(tableCreationQuery)

        sql
    }

    private InsertLocalPeerRows(String exten){
        String appdata = "SIP/$exten,24,\${DIALOPTIONS}"

        sql.execute("DELETE FROM extensions WHERE context=? AND exten=?", ['agents', exten])
        try {
            sql.execute("INSERT INTO extensions(context, exten, priority, app, appdata) VALUES(?, ?, ?, ?, ?)",
                    ['agents', exten, '1', 'Dial', appdata])
        }
        catch(Exception e){
            log.error "Error creating dialplan rows for '$exten':\n\t$e.message"
        }

        sql.firstRow("SELECT * FROM extensions WHERE appdata=?", [appdata])["id"]
    }

    private CreateOrUpdateRemotePeer(String exten, String phoneNumber) {
        String query

        def result = sql.firstRow("SELECT * FROM extensions WHERE exten = '$exten' AND context = 'agents' AND app = 'Dial'")
        String trunkName = ConfigLineService.getConfigValue("dialplan", "primary_trunk")

        if(result) {
            query = "UPDATE extensions SET appdata = '$trunkName/$phoneNumber,30,\${DIALOPTIONS}' WHERE exten = '$exten' AND context = 'agents'"

            result = sql.execute(query)
        } else {
            String appdata = "$trunkName/$phoneNumber,30,\${DIALOPTIONS}"

            query = "INSERT INTO extensions(context, exten, priority, app, appdata) VALUES(?, ?, ?, ?, ?)"

            def args = ['agents', exten, '1', 'Dial', appdata]

            result = sql.execute(query, args)
        }

        result
    }

    private DeleteDialPlanRows(String extension) {
        String deleteQuery = "DELETE from extensions WHERE exten = '$extension' AND context = 'agents' "
        sql.execute(deleteQuery)
    }

    private InsertInboundRouteRows(String did, String destinationType, String destinationName){
        String appdata = ''
        String didPattern = did
        def result
        if(destinationType == 'Queue'){
            appdata = "queues,$destinationName,1"
        }
        else if(destinationType == 'Agent'){
            Agent agent = Agent.findByName(destinationName)
//            Extension primary = agent.primaryExtension
            appdata = "${agent.id},an"
        }

        int rowCount = sql.rows("SELECT * FROM extensions WHERE exten=$didPattern").size()
        if(rowCount == 0) {
            String initialAppdata = ""
            String initialDid = ""
            String routeContext = "inbound-routes" // TODO: Make configuration value for inbound routes context

            if(did.startsWith("1")){
                didPattern = did.substring(1)

                initialDid = did
                initialAppdata = "$routeContext,$didPattern,1"

            } else {
                initialDid = "1" + did
                initialAppdata = "$routeContext,$did,1"
            }

            sql.executeInsert("INSERT INTO extensions(context, exten, priority, app, appdata) VALUES(?, ?, ?, ?, ?)",
                    [routeContext, initialDid, '1', "Goto", initialAppdata])

            result = sql.executeInsert("INSERT INTO extensions(context, exten, priority, app, appdata) VALUES(?, ?, ?, ?, ?)",
                    [routeContext, didPattern, '1', 'Set', "DIALOPTIONS=\${INBOUNDOPTIONS}"])
            if(result) {
                result = sql.executeInsert("INSERT INTO extensions(context, exten, priority, app, appdata) VALUES(?, ?, ?, ?, ?)",
                        [routeContext, didPattern, '2', 'FollowMe', appdata])
                if(result) {
                    result = sql.executeInsert("INSERT INTO extensions(context, exten, priority, app, appdata) VALUES(?, ?, ?, ?, ?)",
                            [routeContext, didPattern, '3', 'Hangup', ""])
                }
            }

            return result
        } else {

            return null
        }
    }

    private boolean DeleteInboundRouteRows(InboundRoute route){
        // Remove all rows that correspond to this route, including the rows meant to deal with
        // optional prefixes such as country codes
        String query = "DELETE FROM extensions WHERE exten LIKE '%$route.didNumber' AND context = 'inbound-routes' "
        try {
            sql.execute(query)
        }
        catch(Exception e){
            log.error "Error deleting SQL rows for inbound route '$route'"

            return false
        }

        return true
    }

    private UpdateInboundRouteRows(InboundRoute route, destination){
        String appdata = ""


        if(destination.class == Queue) {
            destination = destination as Queue

            String template = ConfigLineService.getConfigValue("dialplan", "queue_dest_appdata")
            appdata = template.replace("QUEUENAME", destination.name)
        }
        else if(destination.class == Agent){
            destination = destination as Agent

            String template = ConfigLineService.getConfigValue("dialplan", "agent_dest_appdata")
            appdata = template.replace("AGENTID", destination.id as String)
        }
        else {
            log.error "Invalid destination of type '${destination.class}'"
        }

        if(appdata) {
            def result = sql.executeUpdate "UPDATE extensions SET appdata=:appdata WHERE context=:context AND exten=:exten AND app ILIKE 'Goto'",
                    [appdata: appdata, context: inboundRouteContext, exten: route.didNumber]

            log.debug "Result: $result"
        }
    }

    private InsertVoicemailConfigRow(String agentId, String vmPIN, String vmUser, String email){
        sql.execute("INSERT INTO voicemail_config(category, var_name, var_val) VALUES(?, ?, ?)",
        ['vm', agentId, "$vmPIN,$vmUser,$email" as String])
    }

}
