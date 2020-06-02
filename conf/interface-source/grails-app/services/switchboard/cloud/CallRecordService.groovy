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
import switchboard.cloud.AsteriskCall
import switchboard.cloud.CallRecord

import java.text.SimpleDateFormat

@Transactional
class CallRecordService {

    def ContactService
    def ConfigLineService
    def springSecurityService

    def create(Map callProperties){
        if(CallRecord.findByUniqueId(callProperties.uniqueId)){
            log.info "A Call Record already exists for UID '$callProperties.uniqueId', duplicate creation will be skipped"
        }
        else {
            CallRecord record = new CallRecord(uniqueId: callProperties.uniqueId, startTime: callProperties.startTime)

            AsteriskCall asteriskCall = AsteriskCall.get(callProperties.uniqueId)

            record.contactId = LookupContactId(record)

            try {
                BindCallProperties(record, asteriskCall)
            }
            catch (Exception e) {
                log.error "Exception thrown during CallRecord creation:\n\t$e.message"
            }

            log.info "CallRecord created for call $record.uniqueId"

            record.save()
        }
    }

    def update(String uniqueId, Map callProperties){
        callProperties.put("uniqueId", uniqueId)

        update(callProperties)
    }

    def update(Map callProperties){
        CallRecord record
        if(callProperties.containsKey("uniqueId")) {
            AsteriskCall asteriskCall = AsteriskCall.get(callProperties.uniqueId)
            record = CallRecord.get(callProperties.uniqueId)
            if (record) {
                if(callProperties.containsKey("agents")){
                    if(callProperties.agents) {
                        try {
                            record.addToAgents(callProperties.agents)
                        }
                        catch (Exception e) {
                            log.error "Exception thrown adding agent '$callProperties.agents' to CallRecord $callProperties.uniqueId:\n\t$e.message"
                        }
                    }
                }

                if(callProperties.containsKey("queues")){
                    if(callProperties.queues) {
                        try {
                            record.addToQueues(callProperties.queues)
                        }
                        catch (Exception e) {
                            log.error "Exception thrown adding queue '$callProperties.queues' to CallRecord $callProperties.uniqueId:\n\t$e.message"
                        }
                    }
                }

                BindCallProperties(record, asteriskCall)

                record.recordingFileName = "${record.uniqueId}.wav"

                if(!record.contactId){
                    record.contactId = LookupContactId(record)
                }

                record.save()

                if(record.hasErrors()){
                    String errorList = ""

                    record.errors.each { error ->
                        errorList += "\n\t$error"
                    }

                    log.error "Error(s) saving CallRecord: $errorList"
                }

                String updateList = (callProperties.keySet().intersect(record.properties.keySet() ) ).join(", ")

                log.info "Record '$record.uniqueId' has had the following fields updated: [$updateList]"
            }
            else {
                log.error "Tried to update the call record for call '$callProperties.uniqueId' but no such record exists"
            }
        }
        else {
            log.error "Tried to update a call record but no UniqueId was provided"
        }
    }

    def closeOut(String uniqueId){
        CallRecord record = CallRecord.get(uniqueId)
        AsteriskCall asteriskCall = AsteriskCall.get(uniqueId)

        if(record) {
            record.status = DetermineCallStatus(record, asteriskCall.state)
            record.duration = ((new Date().time - asteriskCall.startTime) / 1000) as int

            BindCallProperties(record, asteriskCall)

            record.save()
        }
        else {
            log.error "Tried to close out a call record with uniqueId '$uniqueId' however no such record exists"
        }
    }

    def completeReport(String uniqueId, Agent user, properties){
        CallRecord record = CallRecord.get(uniqueId)

        String customerId = properties.customerId == 'Other' ? properties.customerIdOther : properties.customerId
        String rolelessCustId = customerId.length() > 6 ? customerId.substring(0, 6) : customerId

        if(record) {
            record.customerName = properties.customerName == 'Other' ? properties.customerNameOther : properties.customerName

            record.customerId = customerId

            if(customerId && customerId != 'None') {
                record.customerAccountName = ContactService.findCompanyNameById(rolelessCustId)
            }

            record.billableDuration = properties.billableDuration as int

            record.notes = properties.notes
            record.customerSatisfaction = properties.customerSatisfaction
            record.reportSubmitted = new Date().time
            record.reportCompleted = true

            user.removeFromPendingCallRecords(record)

            record.save()
            user.save()
        }
        else {
            log.error "Tried to finalize a call record with UniqueId '$uniqueId' however no such record exists"
        }
    }

    def generateCustomerInteractionReport(Map params){
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        Map<String, String> appliedCriteria = DetectAppliedCriteria(params)
        Long start, end

        if(params.dateRangeSelect == "custom") {
            start = params.fromDate ? parser.parse("${params.fromDate} 00:00:00").time : 0
            end = params.toDate ? parser.parse("${params.toDate} 23:59:59").time : new Date().time
        }
        else {
            Calendar cal = Calendar.getInstance()

            end = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            switch(params.dateRangeSelect){
                case "day":
                    start = cal.timeInMillis
                    break
                case "week":
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    start = cal.timeInMillis
                    break
                case "month":
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    start = cal.timeInMillis
                    break
                case "year":
                    cal.set(Calendar.DAY_OF_YEAR, 1)
                    start = cal.timeInMillis
                    break
                case "all":
                    start = 0
                    break
                default:
                    start = 0
                    end = 0
                    break
            }
        }

        log.debug("Range: ${params.dateRangeSelect}, Start: ${new Date(start)}, End: ${new Date(end)}")

        String searchCustId = "${params.customerId.substring(0, Math.min(6, params.customerId.length()))}%"

        Set<String> phoneNumberList = ['%']

        // if a CustomerID or Name is supplied, pull in additional phone numbers from Contacts DB
        if(!params.customerNumber) {
            if (params.customerId) {
                phoneNumberList.addAll(ContactService.findAllNumbersByCompanyId(searchCustId))
                phoneNumberList.remove('%')
            }

            if (params.customerName) {
                phoneNumberList.addAll(ContactService.findAllNumbersByName(params.customerName))
                phoneNumberList.remove('%')
            }
        }

        log.debug("Phone Numbers (${phoneNumberList.size()}):\n['${phoneNumberList.join("', '")}']")

        /**
         * If number is supplied, search using only number
         * If custid is supplied:
         *     - If number is supplied, don't use phoneNumberList
         *     - Else, use phoneNumberList and custid
         * If name is supplied:
         *     - If number is supplied, don't use phoneNumberList
         *     - Else, use phoneNumberList and name
         */

        def query = CallRecord.where {

            customerId =~ searchCustId || calleeNumber in phoneNumberList || callerNumber in phoneNumberList
            (calleeNumber =~ "${params.customerNumber}%" || callerNumber =~ "${params.customerNumber}%")
            (calleeName =~ "${params.customerName}%" || callerName =~ "${params.customerName}%") || calleeNumber in phoneNumberList || callerNumber in phoneNumberList

            if (params.agentNameSelection == 'selected') {
                callerName in params.agentNames || calleeName in params.agentNames
            }

            startTime >= start && startTime <= end

        }

        List<CallRecord> records = query.list(sort: 'startTime', order: 'desc')

        double fractionalTotalTime = 0

        records.each { CallRecord r ->
            fractionalTotalTime += (r.duration / 60)
        }

        [list: records, count: query.count(), totalTime: ((int) Math.floor(fractionalTotalTime)), appliedCriteria: appliedCriteria]
    }

    def generateSubmissionReport(Map params){
        List<String> agentIds = []

        if(params."agents[]".class != String[]){
            agentIds.add(params."agents[]")
        }
        else{
            agentIds = params."agents[]" as List<String>
        }

        List<Agent> agents = Agent.findAllByIdInList(agentIds)

        Map<Long, List<String>> report = [:]

        agents.each { Agent agent ->
            List<CallRecord> agentRecords = CallRecord.findAllByCalleeNameOrCallerName(agent.name, agent.name)
            if(params.dateRange != 'all'){
                agentRecords = FilterRecordsByDate(agentRecords, params.dateRange)
            }
            Long totalLagTime = 0
            int completedRecords = 0
            int pendingRecords = 0

            agentRecords.each { CallRecord record ->
                if(record.reportCompleted){
                    Long lagTime = record.reportSubmitted - record.startTime

                    totalLagTime += lagTime
                    completedRecords++
                }
                else {
                    pendingRecords++
                }
            }

            Long averageSubmissionLag = completedRecords > 0 ? (totalLagTime / completedRecords) : 0

            String formattedSubmissionLag = FormatSubmissionLag(averageSubmissionLag)

            report.put(agent.id, [agent.name, formattedSubmissionLag, pendingRecords.toString(), completedRecords.toString(), agentRecords.size().toString()])
        }

        report
    }

    def search(Map params){
        Date startDate, endDate
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        String today = new Date().format("yyyy-MM-dd")

        String baseCustId = ''

        if(params.customerId) {
            //first six characters are the base CustId, the rest is the role
            baseCustId = params.customerId?.substring(0, 6)
        }

        if (params.dateRangeToggle == 'single') {
            startDate = parser.parse("${params.onDate ?: today} 00:00:00")
            endDate = parser.parse("${params.onDate ?: today} 23:59:59")
        } else {
            startDate = parser.parse("${params.fromDate ?: today} 00:00:00")
            endDate = parser.parse("${params.toDate ?: today} 23:59:59")
        }

        String searchCustomerName = "${params.customerName ?: ''}%"
        String searchCustomerNumber = "${params.customerNumber ?: ''}%"
        String searchAgentName = "${params.agentName ?: ''}%"
        String searchCustId = "${baseCustId}%"
        String searchDirection = params.direction ?: '%'
        boolean desiredReportStatus = params.reportStatus == 'true'
        def ignoreList = ConfigLineService.getConfigValue('call_record', 'ignore_list')

        CallRecord.where {

            (calleeName =~ searchCustomerName  || callerName =~ searchCustomerName)
            (calleeNumber =~ searchCustomerNumber || callerNumber =~ searchCustomerNumber)

            customerId =~ searchCustId

            direction =~ searchDirection

            (callerName =~  searchAgentName|| calleeName =~ searchAgentName)

            !(calleeNumber in ignoreList || callerNumber in ignoreList)

            if(params.reportStatus){
                reportCompleted == desiredReportStatus
            }

            if(params.duration){
                if(params.durationRange == 'lte'){
                    duration <= params.duration.toInteger()
                }
                else {
                    duration > params.duration.toInteger()
                }
            }

            if(params.onDate || (params.fromDate && params.toDate)) {
                startTime >= startDate.time && startTime <= endDate.time
            }
        }
    }

    private static void BindCallProperties(CallRecord record, AsteriskCall asteriskCall){
        record.calleeName = asteriskCall.calleeName ?: record.calleeName
        record.calleeNumber = asteriskCall.calleeNumber ?: record.calleeNumber

        record.callerName = asteriskCall.callerName ?: record.callerName
        record.callerNumber = asteriskCall.callerNumber ?: record.callerNumber

        record.dialedNumber = asteriskCall.dialedNumber ?: record.dialedNumber

        record.direction = asteriskCall.direction ?: record.direction
    }

    private String LookupContactId(CallRecord record){
        String contactNumber

        if(record.direction == 'inbound'){
            log.debug "Caller #: $record.callerNumber"
            contactNumber = record.callerNumber
        }
        else if(record.direction == 'outbound'){
            log.debug "Callee #: $record.calleeNumber"
            contactNumber = record.calleeNumber
        }
        else{
            log.info "ContactID lookup is undefined for Call Records with direction '$record.direction'"
            return null
        }

        ContactService.findCompanyIdByNumber(contactNumber)
    }

    private static String DetermineCallStatus(CallRecord record, String state){
        String status = record.direction

        if(state == 'parked'){
            status = 'abandoned'
        }
        else if(record.direction == 'inbound'){
            status = (record.dialedNumber == record.calleeNumber) ? 'unanswered' : 'answered'
        }

        status
    }

    private static String FormatSubmissionLag(Long lag){
        int seconds = Math.floor(lag / 1000)

        int secs = seconds % 60
        int mins = Math.floor(seconds  / 60)  % 60
        int hrs = Math.floor(seconds / 3600)

        "${hrs < 10 && hrs >= 0 ? "0$hrs" : hrs} hrs ${mins < 10 && mins >= 0 ? "0$mins" : mins} mins ${secs < 10 && secs >= 0 ? "0$secs" : secs} secs"
    }

    private static Map DetectAppliedCriteria(Map params){
        Map<String, String> appliedCriteria = [:]

        for(String key in params.keySet()){
            if(params[key]) {
                switch (key) {
                    case "dateRangeSelect":
                        if(params.dateRangeSelect == 'custom'){
                            appliedCriteria.put("Calls after", params.dateFrom)
                            appliedCriteria.put("Calls before", params.dateTo)
                        }
                        else if(params.dateRangeSelect == 'day'){
                            appliedCriteria.put("Calls from", "Today")
                        }
                        else {
                            if(params.dateRangeSelect != 'all') {
                                appliedCriteria.put("Calls from", "This ${params.dateRangeSelect}")
                            }
                        }
                        break
                    case "customerName":
                        if (appliedCriteria.containsKey("Customer ID")){
                            appliedCriteria.put("Customer Name", params[key])
                        }
                        else {
                            appliedCriteria.put("Customer Name", "${params[key]} and associated phone numbers")
                        }
                        break
                    case "customerNumber":
                        appliedCriteria.put("Customer Number", params[key])
                        break
                    case "customerId":
                        if (appliedCriteria.containsKey("Customer Name")){
                            appliedCriteria.put("Customer ID", params[key])
                        }
                        else {
                            appliedCriteria.put("Customer ID", "${params[key]} and associated phone numbers")
                        }
                        break
                    case "agentNames":
                        appliedCriteria.put("Agents", params[key])
                        break
                    default:
                        break
                }
            }
        }

        appliedCriteria
    }

    private List FilterRecordsByDate(List records, String range){
        List filteredRecords = []
        Long start, end

        Calendar cal = Calendar.getInstance() // Calendar instantiates to represent 'now'

        // Our search always has an end at 'now'
        end = cal.timeInMillis

        //Set up Calendar instance to calculate start
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        switch(range){
            case "day":
                start = cal.timeInMillis
                break
            case "week":
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                start = cal.timeInMillis
                break
            case "month":
                cal.set(Calendar.DAY_OF_MONTH, 1)
                start = cal.timeInMillis
                break
            default:
                log.warn "Unsupported date range for submission report '$range', leaving records unfiltered"
                start = 0
                end = 0
                break
        }

        records.each { CallRecord record ->
            if(record.startTime >= start && record.startTime <= end){
                filteredRecords.add(record)
            }
        }

        filteredRecords
    }

}
