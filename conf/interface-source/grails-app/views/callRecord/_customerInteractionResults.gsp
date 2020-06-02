<div class='modal-dialog' >
    <div class='modal-content' >
        <div class='modal-header' >
            <h1 class='modal-title' >Customer Interaction Report</h1>
            <div class="error-message" ></div>
        </div>
        <div class='modal-body' >
            <h3>Total time spent with customer: ${totalTime} minutes</h3>
            <g:if test="${appliedCriteria.size() > 0}" >
                <h4>Report Criteria:</h4>
                <g:each in="${appliedCriteria}" var="criterion" status="i" >
                    <h5 class="report-criterion" >${appliedCriteria[i]}</h5>
                </g:each>
            </g:if>
            <h3>Records (${count})</h3>
            <div class="scrollable bordered" >
                <g:render template="table" model="[list: list, count: count]" />
            </div>
        </div>
        <div class='modal-footer' >
            <a id="report-back-btn" class="btn btn-warning" onclick="restoreCustomerInteractionInputs()" >Reset</a>
            <a id="report-close-btn" class="btn btn-primary" onclick="hideReportResults()" >Close</a>
        </div>
    </div>
</div>