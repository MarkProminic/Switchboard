<% int prevOffset = params.offset - params.max %>
<% int nextOffset = params.offset + params.max %>
<% int lastOffset = totalCount - params.max %>

<div id="recordings-search-bar" class="row" >
     <div class="col-md-3" >
         <label>Caller #: </label><input id="recordings-search-caller" type="text" value="${params.caller}" onchange="filterRecordings(0)" /><br />
         <label>Callee #: </label><input id="recordings-search-callee" type="text" value="${params.callee}" onchange="filterRecordings(0)" />
     </div>
     <div class="col-md-3" >
         <input id="on" name="date-range-toggle" type="radio" ${(!params.dateType || params.dateType == "on") ? "checked" : ""} onclick="showOnDateField()" /><label>On</label>
         <input id="from-to" name="date-range-toggle" type="radio" ${params.dateType == "from-to" ? "checked" : ""} onclick="showDateRangeFields()" /><label>From-To</label><br />
         <label class="${(!params.dateType || params.dateType == "on") ? "" : "hidden"}" >On: <input id="recordings-search-on-date"  type="date" value="${params."date[on]"}" /></label>
         <label class="${params.dateType == "from-to" ? "" : "hidden"}" >From: <input id="recordings-search-from-date" type="date" value="${params."date[from]"}" /></label><br />
         <label class="${params.dateType == "from-to" ? "" : "hidden"}" >To: <input id="recordings-search-to-date" type="date" value="${params."date[to]"}" /></label>
     </div>
     <div class="col-md-3" >
         <label>Call ID: </label><input  id="recordings-search-uniqueid" type="text" onchange="filterRecordings(0)" value="${params.uniqueId}"/>
     </div>
     <div class="col-md-3" >
         <a id="recordings-search-btn" class="btn btn-sm btn-primary" onclick="filterRecordings(0)" >Search</a>
         <a id="recordings-clear-btn" class="btn btn-sm btn-danger ${params.filtered == "true" ? "" : "hidden"}" onclick="clearFilter()" >Clear Filter</a>
     </div>
</div>
<table id="recordings-table" class="table table-striped table-hover" filtered="${params.filtered}"  >
    <thead>
         <tr id="recordings-table-headers" >
                 <th>Date</th>
                 <th>CallerID</th>
                 <th>Callee #</th>
                 <th>Call ID</th>
             </tr>
     </thead>
    <tbody>
        <div class="loading-message hidden" >Loading audio...</div>
        <g:each in="${recordings}" var="recording" >
            <tr id="recording-${recording.id}" class="recording" >
                <td>${recording.startString}</td>
                <td>${recording.callerID}</td>
                <td>${recording.destName}</td>
                <td>${recording.linkedId}</td>
                <td>
                   <g:render template="audioPlayer" model="${[recording: recording]}" />
                </td>
            </tr>
        </g:each>
    </tbody>
    <tfoot>
        <tr>
            <td colspan="5" >
                <a class="pagination-link" onclick="goToPage('0')" >&lt;&lt; First</a>
                <a class="pagination-link" onclick="goToPage('${Math.max(prevOffset, 0)}')" >&lt; Prev</a>
                <a class="pagination-link" onclick="goToPage('${Math.max(0, Math.min(nextOffset, lastOffset))}')" >Next &gt;</a>
                <a class="pagination-link" onclick="goToPage('${Math.max(0, lastOffset)}')" >Last &gt;&gt;</a>
                Showing recordings
                <span id="recordings-range-start" >${(params.offset ?: 0) +1 }</span>
                to
                <span id="recordings-range-end" >${Math.min(nextOffset, totalCount)}</span>
                of
                <span id="recordings-total-count" >${totalCount}</span>
                Display:
                <input type="text" id="recordings-pagination-max" title="Number of recordings to display per page" maxlength="3"
                       value="${params.max ?: 10}" onchange="goToPage(${params.offset ?: 0})"/>
            </td>
        </tr>
    </tfoot>
</table>