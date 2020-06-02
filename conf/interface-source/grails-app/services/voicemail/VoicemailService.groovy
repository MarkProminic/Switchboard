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

package voicemail

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import grails.gorm.transactions.Transactional
import grails.util.Environment
import groovy.sql.Sql
import security.Agent

@Transactional
class VoicemailService implements GrailsConfigurationAware {

    Sql sql
    def ConfigLineService

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

    boolean createAgentVoiceMail(Long agentId, String email, boolean smsOptIn, String carrierEmail, String pagerExten){
        Agent agent = Agent.get(agentId)

        String pager = smsOptIn ? "$pagerExten@$carrierEmail" : ""
        boolean success

        List results = sql.executeInsert("INSERT INTO voicemail_config ('category', 'var_name', 'var_val', 'cat_metric', 'filename', 'commented', 'var_metric') " +
                "VALUES(:vmContext, :agentId, ':pin,:name,:email,:pager,tz=:timezone', null, 'voicemail.conf', '0', null)",
                [vmContext: "prominic", agentId: agent.id, pin: "1234", name: agent.name, email: email, pager: pager, timezone: "central"])

        success = !results.isEmpty()

        sql.close()

        success
    }

    private ConnectToSql(){
        try {
            sql = Sql.newInstance(
                    url: sqlConnectionString,
                    user: sqlUsername,
                    password: sqlPassword,
                    driver: sqlDriver
            )
        }
        catch(Exception e){
            log.error "Couldn't connect to host configured in applicaiton.yml. Voicemail related insertions and updates " +
                    "(as well as most dataSource interactions most likely) will fail: ${e.message}"
        }
    }
}
