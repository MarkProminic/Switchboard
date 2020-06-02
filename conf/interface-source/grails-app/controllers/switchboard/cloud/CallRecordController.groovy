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

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import org.springframework.http.HttpStatus
import security.Agent

@Transactional(readOnly = true)
class CallRecordController {

    def CallRecordService
    def ContactService
    def MailService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index() {
        Agent user = getAuthenticatedUser() as Agent

        render template: "callRecordList", model: [callRecordList: user.pendingCallRecords.sort { a,b -> b.startTime <=> a.startTime } ]
    }

    def finalizeRecord(String uniqueId) {
        CallRecord record = CallRecord.get(uniqueId)

        def agentNames = []
        List<String> customerNames = []
        List<String> custIds = []

        String userName = ""
        String customerNumber

        if(record) {
            agentNames = Agent.list().name
            userName = getAuthenticatedUser().name

            customerNames = ContactService.findAllNamesByNumber(record.customerNumber) ?: []
            custIds = ContactService.findAllCustIdsByNumber(record.customerNumber) ?: []
        }

        customerNames = customerNames.sort().subList(0, Math.min(customerNames.size(), 24))
        custIds = custIds.sort().subList(0, Math.min(custIds.size(), 24))

        render template:"finalizeRecord", model: [callRecord: record, userName: userName, agentNames: agentNames,
                                                  customerNames: customerNames, custIds: custIds]
    }

    def email(CallRecord record){
        respond record
    }

    @Transactional
    def closeOut(){
        Agent user = getAuthenticatedUser() as Agent
        try {
            CallRecordService.completeReport(params.uniqueId, user, params)
        }
        catch(Exception e){
            log.error "Exception thrown while finishing record for call '$params.uniqueId':\n\t$e.message"
            return e.message
        }

        MailService.sendCallRecordEmail(params.uniqueId)

        render HttpStatus.OK
    }

    def lookupCustomerIds(String name){
        def custIds = ContactService.findAllCustIdsByName(name)

        render template: "customerIdOption", collection: custIds
    }

    def reassign(CallRecord record, String agentName){
        Agent oldAssignee = getAuthenticatedUser() as Agent
        Agent newAssignee = Agent.findByName(agentName)

        def status = HttpStatus.INTERNAL_SERVER_ERROR

        if(newAssignee) {
            status = HttpStatus.OK
            oldAssignee.removeFromPendingCallRecords(record)
            newAssignee.addToPendingCallRecords(record)

            oldAssignee.save()
            newAssignee.save()
            record.save()
        }

        render status: status
    }

    def submissionReportInputs(){
        render template: "submissionReportInputs", model: [agents: Agent.findAllByEnabled(true, [sort: 'name'])]
    }

    def submissionReport(){
        def report = CallRecordService.generateSubmissionReport(params)

        render template: "submissionReport", model: [report: report]
    }

    def customerInteractionInputs(){
        render template: "customerInteractionInput", model: [agents: Agent.findAllByEnabled(true, [sort: 'name']) ]
    }

    def customerInteractionReport(){
        def report = CallRecordService.generateCustomerInteractionReport(params)
        List<String> appliedCriteria = []

        for(Map.Entry<String, String> entry in report.appliedCriteria){
            appliedCriteria.add("$entry.key: $entry.value")
        }

        render template: "customerInteractionResults", model: [list: report.list, count: report.count,
                                                               totalTime: report.totalTime, report: true,
                                                               appliedCriteria: appliedCriteria]
    }

    def edit(CallRecord record){
        List<String> customerNames = []

        if(record){
            customerNames = ContactService.findAllNamesByNumber(record.customerNumber) ?: []
            customerNames.remove(record.customerName)
        }

        render template: "editRecord", model:[callRecord: record, customerNames: customerNames]
    }

    def list(){
        String offset = params.offset ?: "0"
        String max = params.max ?: "15"

        String currentPage = ((offset.toInteger() / max.toInteger())as int) + 1

        DetachedCriteria<CallRecord> query = CallRecordService.search(params)

        render template: "table", model: [list: query.list(sort: 'startTime', order: 'desc', offset: offset, max: max), mainList: true,
                                          count: query.count(), offset: offset, max: max, currentPage: currentPage]
    }

    def agentRecordList(Agent agent){

        def records =  CallRecord.findAllByCalleeNameOrCallerName(agent.name, agent.name, [sort: "startTime", order: "desc"])

        render template: "table", model: [list: records, mainList: false]
    }

    def userTable(){
        Agent user = getAuthenticatedUser() as Agent

        render template: "table", model: [list: user.pendingCallRecords.sort { a,b -> b.startTime <=> a.startTime }, mainList: false]
    }

    def show(CallRecord record){
        respond record
    }

    @Transactional
    def save(){
        CallRecord record = CallRecord.get(params.uniqueId)

        if(record){
            record.billableDuration = params.billableDuration

            record.customerName = params.customerName

            record.customerId = params.customerId
            record.customerSatisfaction = params.customerSatisfaction

            record.notes = params.notes
        }

        record.save()
    }
}
