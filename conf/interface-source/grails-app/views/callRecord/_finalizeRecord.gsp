<%@ page import="switchboard.cloud.CallRecord" %>
<div class='modal-dialog' >
    <div class='modal-content' >
        <div class='modal-header' >
            <h1 class='modal-title' >After Call Report</h1>
            <div class="error-message" ></div>
        </div>
        <form id="record-form" class="form-group-lg" >
            <div class='modal-body scrollable-modal' >
                <g:if test="${this.callRecord}" >
                    <input id="record-uniqueid" name="uniqueId" type="hidden" value="${this.callRecord.uniqueId}" />
                    <g:if test="${this.callRecord.direction != 'conference'}" >
                        <h3 class="bold" >${this.callRecord.direction.capitalize()} call ${this.callRecord.direction == 'inbound' ? 'from':'to'} ${this.callRecord.customerNumber}</h3>
                    </g:if>
                    <g:else>
                        <h3 class="bold" >${this.callRecord.direction.capitalize()} call with ${this.callRecord.memberRecords.name.join(", ")}</h3>
                    </g:else>
                    <h4 class="bold" >Call Started: ${this.callRecord.startDateString}</h4>
                    <h4 class="bold" >Duration: ${this.callRecord.formattedDurationString}</h4>
                    <div id="record-reassign" class="checkbox row" >
                        <h4 class="col-xs-offset-0 col-xs-12" ><input name="recordReassign" type="checkbox" onclick="toggleFormInputs()" /> Not my call</h4>
                        <h4 class="assignee-toggle hidden" > Who's call is it? </h4>
                        <g:select class="assignee-toggle hidden form-control" name="recordAssigneeName" from="${agentNames}" />
                    </div>
                    <div class="record-recording" >
                        <a class="btn btn-sm btn-primary" onclick="loadRecordRecording('${this.callRecord.uniqueId}')" >View Recording</a>
                    </div>
                    <br />
                    <div id="record-billable-duration" class="assignee-toggle row" >
                        <h4 class='col-md-3' title="This should be the amount of time that was actually spent on the phone with the
                            customer in minutes and defaults to the total duration in minutes, rounded down" >Billable Duration (mins):</h4>
                        <div class='col-md-5' >
                            <input class="form-control" name="billableDuration" type="number" value="${this.callRecord.calculateBillableDuration()}" />
                        </div>
                    </div>
                    <br />
                    <div id="record-customer-name" class="assignee-toggle row" >
                        <h4 class='col-md-3' >Contact Name:</h4>
                        <div class='col-md-5' >
                            <select class="form-control" name="customerName" onchange="processContactNameSelection()" >
                                <g:each in="${this.customerNames}" var="name" >
                                    <option value="${name}" >${name}</option>
                                </g:each>
                                <option value="Other" >Other...</option>
                            </select>
                        </div>
                    </div>
                    <div id="record-customer-name-other" class="${this.customerNames ? 'hidden' : ''} row" >
                        <h4 class='col-md-3' >Contact Name(Other...):</h4>
                        <div class='col-md-5' >
                            <input name="customerNameOther" class="form-control" type="text" onblur="updateCustomerIdOptions()" />
                        </div>
                    </div>
                    <div id="record-customer-id" class="assignee-toggle row" >
                        <h4 class='col-md-3' >Customer ID:</h4>
                        <div class='col-md-5' >
                            <h3 id="record-customer-id-loading" class="hidden" ></h3>
                            <select class="form-control" name="customerId" onchange="processCustomerIdSelection()" >
                                <g:each in="${this.custIds}" var="custId" >
                                    <option value="${custId}" >${custId}</option>
                                </g:each>
                                <option value="None" >None</option>
                                <option value="Other" >Other...</option>
                            </select>
                        </div>
                    </div>
                    <div id="record-customer-id-other" class="hidden row" >
                        <h4 class='col-md-3' >Customer ID(Other...):</h4>
                        <div class='col-md-5' >
                            <input name="customerIdOther" class="form-control" type="text" />
                        </div>
                    </div>
                    <br />
                    <div id="record-customer-satisfaction" class="assignee-toggle form-group" >
                        <p>Customer Satisfaction: </p>
                        <div class="well row" >
                            <label class="col-md-2 radio-inline" ><input name="customerSatisfaction" value="5" type="radio" /> 5(Highly Satisfied)</label>
                            <label class="col-md-2 radio-inline" ><input name="customerSatisfaction" value="4" type="radio" /> 4</label>
                            <label class="col-md-2 radio-inline" ><input name="customerSatisfaction" value="3" type="radio" /> 3</label>
                            <label class="col-md-2 radio-inline" ><input name="customerSatisfaction" value="2" type="radio" /> 2</label>
                            <label class="col-md-3 radio-inline" ><input name="customerSatisfaction" value="1" type="radio" /> 1(Highly Dissatisfied)</label>
                        </div>
                    </div>
                    <div id="record-notes" class="assignee-toggle form-group" >
                        <p>Notes:</p>
                        <div class="row" >
                            <textarea required name="notes" class="col-md-12" rows="15" ></textarea>
                        </div>
                    </div>
                </g:if>
                <g:else >
                    <h3>Loading call details...</h3>
                </g:else>
            </div>
            <div class='modal-footer' >
                <a id="record-close-btn" href='#' onclick='confirmHideCallRecordModal()' class='btn btn-default' >Close</a>
                <a id="record-submit-btn" href="#" onclick="submitCallRecord()" class="btn btn-success assignee-toggle" >Submit</a>
                <a id="record-reassign-btn" href="#" onclick="reassignCallRecord()" class="hidden btn btn-primary assignee-toggle" >Reassign</a>
            </div>
        </form>
    </div>
</div>