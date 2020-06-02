<div id="record-table-header" class="list-group-item" >
    <div id="record-search-inputs" class="row" >
        <form id="record-search-form" >
            <h4>Search</h4>
            <div class="col-md-3 row" >
                <small class="col-md-6" >Contact Name: </small><input class="col-md-6"  id="record-search-customer-name" name="customerName" type="text" /><br />
                <small class="col-md-6" >Contact #: </small><input  class="col-md-6" id="record-search-customer-number" name="customerNumber" type="text" /><br />
                <small class="col-md-6" >Customer ID: </small><input class="col-md-6" id="record-search-customer-id" name="customerId" type="text" /><br />
                <small class="col-md-6" >Agent Name: </small><input class="col-md-6"  id="record-search-agent-name" name="agentName" type="text" />
            </div>
            <div class="col-md-3" >
                <small>Direction:</small><br />
                <small><input name="direction" value="%" type="radio" checked /> Any</small>
                <small><input name="direction" value="inbound" type="radio" /> Inbound</small>
                <small><input name="direction" value="outbound" type="radio" /> Outbound</small>
                <small><input name="direction" value="internal" type="radio" /> Internal</small><br />

                <small>Report Status:</small><br />
                <small><input name="reportStatus" type="radio" value="" checked /> Any</small>
                <small><input name="reportStatus" type="radio" value="true" /> Completed</small>
                <small><input name="reportStatus" type="radio" value="false" /> Pending</small>
            </div>
            <div class="col-md-2" >
                <small>Date:</small>
                <small ><input name="dateRangeToggle" value="single" type="radio" checked onclick="showRecordOnDateField()" /> On</small>
                <small ><input name="dateRangeToggle" value="range" type="radio" onclick="showRecordDateRangeFields()" /> From-To</small><br />
                <small><input id="record-search-on-date" name="onDate" type="date" /></small>
                <small class="hidden" >From: <input id="record-search-from-date" name="fromDate" type="date" /></small><br />
                <small class="hidden" >To: <input id="record-search-to-date" name="toDate" type="date" /></small><br />

                <small>Duration (secs):</small>
                <small ><input name="durationRange" value="lte" type="radio" checked /> ${"<="}</small>
                <small ><input name="durationRange" value="gt" type="radio" /> ${" >"}</small><br />
                <input name="duration" type="number" />
            </div>
            <div class="col-md-2" >
                <a id="record-search-btn" class="btn btn-sm btn-primary" onclick="searchRecords()" >Search</a>
                <a id="record-clear-btn" class="btn btn-sm btn-danger hidden" onclick="clearRecordFilter()" >Clear Filter</a>
            </div>
        </form>
    </div>
    <hr />
    <div id="record-reports-container" >
        <a class="btn btn-sm btn-success" onclick="showSubmissionInputs()" >Generate Record Submission Report</a>
        <a class="btn btn-sm btn-primary" onclick="showCustomerInteractionInputs()" >Generate Customer Interaction Report</a>
    </div>
</div>
<div id="record-table-container" ></div>
<div id="record-search-table-container" ></div>