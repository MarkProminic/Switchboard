<div class='modal-dialog' >
    <div class='modal-content' >
        <div class='modal-header' >
            <h1 class='modal-title' >Call Record Submission Report</h1>
            <div class="error-message" ></div>
        </div>
        <div class='modal-body scrollable-modal' >
            <form id="record-submission-inputs" >
                <h3>Include reports from:</h3>
                <h5>Agents</h5>
                <div class="form-group form-group-sm well well-sm row" >
                    <g:each in="${agents}" var="agent" >
                        <div class="col-xs-3" >
                            <label class="checkbox-inline" >
                                <g:checkBox id="record-submission-agent-${agent.id}" name="agents" value="${agent.id}" />
                                ${agent.name}
                            </label>
                        </div>
                    </g:each>
                </div>
                <h5>Dates</h5>
                <select id="record-submission-date-range" name="dateRange" class="form-control" >
                    <option value="day" >Today</option>
                    <option value="week" >This week</option>
                    <option value="month" >This month</option>
                    <option value="all" selected >Any</option>
                </select>
            </form>
        </div>
        <div class='modal-footer' >
            <a id="report-close-btn" class="btn btn-primary" onclick="hideReportInputs()" >Close</a>
            <a id="report-submit-btn" class="btn btn-success" onclick="generateRecordSubmissionReport()" >Generate</a>
        </div>
    </div>
</div>