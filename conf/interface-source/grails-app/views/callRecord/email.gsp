<html>
    <head>
        %{--<asset:stylesheet src="application.css" />--}%
        %{--<asset:javascript src="application.js" />--}%
    </head>
    <body>
    <%@ page import="switchboard.cloud.Recording" %>
    <g:set var="recording" value="${Recording.findByUniqueId(this.callRecord.uniqueId)}" />
        <div id="record-email-body" class="container" >
            <h3>
                ${this.callRecord.direction.capitalize()} call
                ${this.callRecord.direction == 'inbound' ? 'from':'to'}
                ${this.callRecord.customerNumber}
            </h3>
            <table style="table-layout: fixed; width: 75%;" >
                <tbody>
                    <tr>
                        <td>
                            Call Started:
                        </td>
                        <td>
                            ${this.callRecord.startDateString}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Duration:
                        </td>
                        <td>
                            ${this.callRecord.formattedDurationString}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Billable Duration:
                        </td>
                        <td>
                            ${this.callRecord.billableDuration}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Agent Name:
                        </td>
                        <td>
                            ${this.callRecord.agentName}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Contact Name:
                        </td>
                        <td>
                            ${this.callRecord.customerName}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Customer ID:
                        </td>
                        <td>
                            ${this.callRecord.customerId}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Customer Account Name:
                        </td>
                        <td>
                            ${this.callRecord.customerAccountName}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Customer Satisfaction:
                        </td>
                        <td>
                            ${this.callRecord.customerSatisfaction}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Notes:
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" style="word-wrap: break-spaces;" >
                            <h3 style="font-weight: normal;" >${this.callRecord.notes}</h3>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Call Recording:
                        </td>
                        <td>
                            <g:if test="${recording}" >
                                <a href="https://phoneinterface.pbx.prominic.net/recordings/${recording.id}" >
                                    ${this.callRecord.uniqueId}.wav
                                </a>
                            </g:if>
                            <g:else>
                                No recording found for call '${this.callRecord.uniqueId}'
                            </g:else>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </body>
</html>