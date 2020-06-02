<!--
This template is used in a few places througout Switchboard and the only differences are inclusion of the pagination
elements and swapping out which JS method the 'View' button calls. The 'mainList' variable denotes this and refers to
the full list view that is filterable and paginated accessible in from the nav bar
-->
<table id="records-table" class="table table-hover table-condensed" >
    <thead>
        <g:if test="${mainList}" >
            <tr>
                <td colspan="2" >
                    <a href="#" onclick="goToRecordPage('0')" > ${"<< First"}</a>
                    <a href="#" onclick="goToRecordPage('${Math.max(0, offset.toInteger() - max.toInteger())}')" > ${"< Prev"}</a>
                    <p class="inline current-page" >${currentPage}</p>
                    <p class="hidden offset" >${offset}</p>
                    <a href="#" onclick="goToRecordPage('${Math.min(count.toInteger(), offset.toInteger() + max.toInteger())}')" > ${"Next >"}</a>
                    <a href="#" onclick="goToRecordPage('${count.toInteger() - max.toInteger()}')" > ${"Last >>"}</a>
                </td>
                <td colspan="2" >
                    Records ${offset.toInteger() + 1} - ${Math.min(offset.toInteger() + max.toInteger(), count.toInteger())} of ${count} total
                </td>
                <td colspan="2" >
                    Display:
                    <input type="text" id="record-pagination-max" title="Number of call records to display per page" maxlength="3"
                           value="${max ?: 10}" onchange="goToPage(${offset ?: 0})"/>
                </td>
                <td colspan="2" >
                    Last updated: ${new Date().format("hh:mm:ss")}
                </td>
            </tr>
        </g:if>
        <tr>
            <th>Start Time</th>
            <th>Direction</th>
            <th>Contact</th>
            <th>Customer ID</th>
            <th>Agent</th>
            <th>Duration</th>
            <th>Report Submitted</th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${list}" var="record" >
            <tr>
                <td>${record.startDateString}</td>
                <td>${record.direction}</td>
                <td>"${record.customerName}" <${record.customerNumber}></td>
                <td>${record.customerId ?: 'Pending'}</td>
                <td class="record-table-agent-name" >${record.agentName}</td>
                <td>${record.compactDurationString}</td>
                <td>${record.reportSubmittedDateString}</td>
                <td>
                    <g:if test="${mainList}" >
                        <a class="btn btn-sm btn-primary" onclick="showCallRecordModal('${record.uniqueId}')" > View</a>
                    </g:if>
                    <g:else>
                        <a class="btn btn-sm btn-primary" onclick="showReportRecordDetail('${record.uniqueId}')" > View</a>
                    </g:else>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>