<div class='modal-dialog' >
    <div class='modal-content' >
        <div class='modal-header' >
            <h1 class='modal-title' >Customer Interaction Report</h1>
            <div class="error-message" ></div>
        </div>
        <div class='modal-body scrollable-modal' >
            <form id="record-customer-interaction-inputs" >
                <h3>Customer Information</h3>
                <h5>Any customer information fields that are left blank will be taken as wildcard or 'anything goes' values</h5>
                <div id="report-customer-name" class="row" >
                    <div class='col-md-offset-1 col-md-3' >
                        <h4>Contact Name:</h4>
                    </div>
                    <div class='col-md-4' >
                        <input name="customerName" class="form-control" type="text" />
                    </div>
                </div>
                <div id="report-customer-number" class="row" >
                    <div class='col-md-offset-1 col-md-3' >
                        <h4>Contact Number:</h4>
                    </div>
                    <div class='col-md-4' >
                        <input name="customerNumber" class="form-control" type="text" />
                    </div>
                </div>
                <div id="report-customer-id" class="row" >
                    <div class='col-md-offset-1 col-md-3' >
                        <h4>Customer ID:</h4>
                    </div>
                    <div class='col-md-4' >
                        <input name="customerId" class="form-control" type="text" />
                    </div>
                </div>
                <div id="report-range" class="row" >
                    <div class='col-md-offset-1 col-md-3' >
                        <h4>Date Range:</h4>
                    </div>
                    <div class="col-md-4" >
                        <select id="date-range-select" name="dateRangeSelect" class="form-control" onchange="processDateRangeSelection()" >
                            <option selected value="all" >All</option>
                            <option value="day" >Today</option>
                            <option value="week" >This Week</option>
                            <option value="month" >This Month</option>
                            <option value="year" >This Year</option>
                            <option value="custom" >Custom Range...</option>
                        </select>
                    </div>
                    <div id="custom-date-range" class='hidden col-md-offset-4 col-md-4' >
                        <h4>From: <input name="fromDate" class="form-control" type="date" /></h4>
                        <h4>To: <input name="toDate" class="form-control" type="date" /></h4>
                    </div>
                </div>
                <h3>Agent Information</h3>
                <div id="report-agent-name" >
                    <div class="row" >
                        <div class='col-md-offset-1 col-md-3' >
                            <h4>Agent Name(s):</h4>
                        </div>
                        <div class="col-md-4" >
                            <h5 class="radio" ><input type="radio" name="agentNameSelection" value="all" checked onchange="toggleAgentSelectionWell()" /> All</h5>
                            <h5 class="radio" ><input type="radio" name="agentNameSelection" value="selected" onchange="toggleAgentSelectionWell()" /> Selected</h5>
                        </div>
                    </div>
                    <div id="report-agent-name-selection" class="row hidden" >
                        <div class='col-md-offset-4 col-md-4 well well-lg' >
                            <g:each in="${agents}" var="agent" >
                                <h5 class="checkbox" ><input type="checkbox" name="agentNames" value="${agent}" /> ${agent}</h5>
                            </g:each>
                        </div>
                    </div>
                </div>
            </form>
        </div>
        <div class='modal-footer' >
            <a id="report-close-btn" class="btn btn-primary" onclick="hideReportInputs()" >Close</a>
            <a id="report-submit-btn" class="btn btn-success" onclick="generateCustomerInteractionReport()" >Generate</a>
        </div>
    </div>
</div>