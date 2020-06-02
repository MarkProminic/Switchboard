<div class='modal-dialog' >
    <div class='modal-content' >
        <div class='modal-header' >
            <h1 class='modal-title' >Record Submission Report</h1>
            <h4>Double-click an Agent's name for list view of that Agent's records</h4>
            <h4><a href="#" onclick='showSubmissionInputs()' >${"<<"} Back</a></h4>
            <div class="error-message" ></div>
        </div>
        <div class='modal-body scrollable-modal' >
            <table class="table table-bordered table-hover" >
                <thead>
                    <tr>
                        <th>Agent</th>
                        <th>Avg. Time to Submit</th>
                        <th>Pending Records</th>
                        <th>Completed Records</th>
                        <th>Total Records</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${report}" var="entry" >
                        <tr ondblclick="viewAgentRecordList('${entry.key}')" >
                            <td>${entry.value[0]}</td>
                            <td>${entry.value[1]}</td>
                            <td>${entry.value[2]}</td>
                            <td>${entry.value[3]}</td>
                            <td>${entry.value[4]}</td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>
        <div class="modal-footer" >
            <a class="btn btn-default" onclick="hideReportResults()" >Close</a>
        </div>
    </div>
</div>
