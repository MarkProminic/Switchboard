<%@ page import="switchboard.cloud.CallRecord" %>
<div class='modal-dialog' >
    <div class='modal-content' >
        <div class='modal-header' >
            <h1 class='modal-title inline-block' >Edit Call Report</h1>
            <g:if test="${this.callRecord}" >
                <a class="btn btn-sm btn-default inline-block" href="/callRecord/show?id=${this.callRecord.uniqueId}" target="_blank" >Direct Link to this Record</a>
            </g:if>
            <div class="error-message" ></div>
            <g:hasErrors bean="${this.callRecord}"  >
                <ul class="errors" role="alert" >
                    <g:eachError bean="${this.callRecord}" var="error" >
                        <li <g:if test="${error in org.springframework.validation.FieldError}" >data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
                    </g:eachError>
                </ul>
            </g:hasErrors>
        </div>
        <g:if test="${this.callRecord}" >
            <form id="edit-record-form" class="form-group-lg" >
                <div class='modal-body scrollable-modal' >
                    <input id="edit-record-uniqueid" name="uniqueId" type="hidden" value="${this.callRecord.uniqueId}" />
                    <g:if test="${this.callRecord.direction != 'conference'}" >
                        <h3 class="bold" >${this.callRecord.direction.capitalize()} call ${this.callRecord.direction == 'inbound' ? 'from':'to'} ${this.callRecord.customerNumber}</h3>
                    </g:if>
                    <g:else>
                        <h3 class="bold" >${this.callRecord.direction.capitalize()} call with ${this.callRecord.memberRecords.name.join(", ")}</h3>
                    </g:else>
                    <h4 class="bold" >Call Started: ${this.callRecord.startDateString}</h4>
                    <h4 class="bold" >Duration: ${this.callRecord.formattedDurationString}</h4>
                    <div class="record-recording" >
                        <a class="btn btn-sm btn-primary" onclick="loadRecordRecording('${this.callRecord.uniqueId}')" >View Recording</a>
                    </div>
                    <br />
                    <div id="edit-record-billable-duration" class="row" >
                        <h4 class='col-md-3' title="This should be the amount of time that was actually spent on the phone with the
                            customer in minutes and defaults to the total duration in minutes, rounded down" >Billable Duration (mins):</h4>
                        <div class='col-md-5' >
                            <input class="form-control" name="billableDuration" type="number" value="${this.callRecord.billableDuration}" />
                        </div>
                    </div>
                    <br />
                    <div id="edit-record-customer-name" class="row" >
                        <h4 class='col-md-3' >Contact Name:</h4>
                        <div class='col-md-5' >
                            <select class="form-control" name="customerName" onchange="processContactNameSelection()" >
                                <option value="${this.callRecord.customerName}" selected >${this.callRecord.customerName}</option>
                                <g:each in="${this.customerNames}" var="name" >
                                    <option value="${name}" >${name}</option>
                                </g:each>
                                <option value="Other" >Other...</option>
                            </select>
                        </div>
                    </div>
                    <div id="edit-record-customer-name-other" class="hidden row" >
                        <h4 class='col-md-3' >Contact Name(Other...):</h4>
                        <div class='col-md-5' >
                            <input name="customerNameOther" class="form-control" type="text" onblur="updateCustomerIdOptions()" />
                        </div>
                    </div>
                    <div id="edit-record-customer-id" class="row" >
                        <h4 class='col-md-3' >Customer ID:</h4>
                        <div class='col-md-5' >
                            <h3 id="record-customer-id-loading" class="hidden" ></h3>
                            <select class="form-control" name="customerId" onchange="processCustomerIdSelection()" >
                                <option value="${this.callRecord.customerId}" >${this.callRecord.customerId}</option>
                                <g:each in="${this.custIds}" var="custId" >
                                    <option value="${custId}" >${custId}</option>
                                </g:each>
                                <option value="None" >None</option>
                                <option value="Other" >Other...</option>
                            </select>
                        </div>
                    </div>
                    <div id="edit-record-customer-id-other" class="hidden row" >
                        <h4 class='col-md-3' >Customer ID(Other...):</h4>
                        <div class='col-md-5' >
                            <input name="customerIdOther" class="form-control" type="text" />
                        </div>
                    </div>
                    <br />
                    <div id="edit-record-customer-satisfaction" class="form-group" >
                        <p>Customer Satisfaction: </p>
                        <div class="well row" >
                            <label class="col-md-2 radio-inline" >
                                <input name="customerSatisfaction" ${this.callRecord.customerSatisfaction == "5" ? 'checked' : ''}
                                       value="5" type="radio" /> 5(Highly Satisfied)</label>
                            <label class="col-md-2 radio-inline" >
                                <input name="customerSatisfaction" ${this.callRecord.customerSatisfaction == "4" ? 'checked' : ''}
                                       value="4" type="radio" /> 4</label>
                            <label class="col-md-2 radio-inline" >
                                <input name="customerSatisfaction" ${this.callRecord.customerSatisfaction == "3" ? 'checked' : ''}
                                       value="3" type="radio" /> 3</label>
                            <label class="col-md-2 radio-inline" >
                                <input name="customerSatisfaction" ${this.callRecord.customerSatisfaction == "2" ? 'checked' : ''}
                                       value="2" type="radio" /> 2</label>
                            <label class="col-md-3 radio-inline" >
                                <input name="customerSatisfaction" ${this.callRecord.customerSatisfaction == "1" ? 'checked' : ''}
                                       value="1" type="radio" /> 1(Highly Dissatisfied)</label>
                        </div>
                    </div>
                    <div id="edit-record-notes" class="form-group" >
                        <p>Notes:</p>
                        <div class="row" >
                            <textarea name="notes" class="col-md-12" rows="15" >${this.callRecord.notes}</textarea>
                        </div>
                    </div>
                </div>
                <div class='modal-footer' >
                    <a id="edit-record-close-btn" href='#' onclick='confirmHideCallRecordEditModal()' class='btn btn-default' >Close</a>
                    <a id="edit-record-submit-btn" href="#" onclick="saveCallRecord()" class="btn btn-success" >Save</a>
                </div>
            </form>
        </g:if>
        <g:else>
            <h3>Loading call details...</h3>
        </g:else>
    </div>
</div>