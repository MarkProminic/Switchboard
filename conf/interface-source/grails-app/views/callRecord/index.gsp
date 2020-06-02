<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <asset:stylesheet src="main.css" />
        <g:set var="entityName" value="${message(code: 'callRecord.label', default: 'CallRecord')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <a href="#list-callRecord" class="skip" tabindex="-1" ><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div id="list-callRecord" class="content scaffold-list" role="main" >
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}" >
                <div class="message" role="status" >${flash.message}</div>
            </g:if>
            <table>
                <thead>
                    <tr>
                        <th>Start</th>
                        <th>Direction</th>
                        <th>Caller Name</th>
                        <th>Caller Number</th>
                        <th>Callee Name</th>
                        <th>Callee Number</th>
                        <th>Dialed Number</th>
                        <th>Duration (secs)</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                <g:each in="${callRecordList}" var="record" >
                    <tr>
                        <td>${new Date(record.startTime)}</td>
                        <td>${record.direction}</td>
                        <td>${record.callerName}</td>
                        <td>${record.callerNumber}</td>
                        <td>${record.calleeName}</td>
                        <td>${record.calleeNumber}</td>
                        <td>${record.dialedNumber}</td>
                        <td>${record.callDuration}</td>
                        <td>${record.status}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
            <div class="pagination" >
                <g:paginate total="${callRecordCount ?: 0}" />
            </div>
        </div>
    </body>
</html>