<%@ page import="switchboard.cloud.CallRecord" %>

<html>
    <head>
        <asset:stylesheet src="application.css" />
        <asset:javascript src="application.js" />
    </head>
    <body>
        <div class='modal-dialog' >
            <div class='modal-content' >
                <div class='modal-header' >
                    <h1 class='modal-title' >Call Report Details</h1>
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
                    <div class='modal-body scrollable-modal' >
                        <g:if test="${this.callRecord}" >
                            <h3 class="bold" >
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
                            <div id="record-billable-duration" class="row" >
                                <h4 class='col-md-3' >Billable Duration (mins):</h4>
                                <h4 class='col-md-5' >${this.callRecord.billableDuration}</h4>
                            </div>
                            <br />
                            <div id="record-customer-name" class="row" >
                                <h4 class='col-md-3' >Contact Name:</h4>
                                <h4 class='col-md-5' >${this.callRecord.customerName}</h4>
                            </div>
                            <div id="record-customer-id" class="row" >
                                <h4 class='col-md-3' >Customer ID:</h4>
                                <h4 class='col-md-5' >${this.callRecord.customerId}</h4>
                            </div>
                            <br />
                            <div id="record-customer-satisfaction" class="row" >
                                <h4 class='col-md-3' >Customer Satisfaction:</h4>
                                <h4 class='col-md-5' >${this.callRecord.customerSatisfaction}</h4>
                            </div>
                            <div id="record-notes" class="row" >
                                <h4 class="col-md-12" >Notes:</h4>
                                <div class="col-md-12" >
                                    <p>${this.callRecord.notes}</p>
                                </div>
                            </div>
                        </g:if>
                        <g:else >
                            <h3>No such call record</h3>
                        </g:else>
                    </div>
                    <div class='modal-footer' >
                    </div>
                </g:if>
            </div>
        </div>
    </body>
</html>