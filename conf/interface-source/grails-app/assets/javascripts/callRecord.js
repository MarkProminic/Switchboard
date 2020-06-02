/*
 * Developed by Luis Alcantara
 *
 * Copyright (C) 2016-2019 Prominic.NET, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 *
 * As a special exception, the copyright holders give permission to link the
 * code of portions of this program with the OpenSSL library under certain
 * conditions as described in each individual source file and distribute
 * linked combinations including the program with the OpenSSL library. You
 * must comply with the Server Side Public License in all respects for
 * all of the code used other than as permitted herein. If you modify file(s)
 * with this exception, you may extend this exception to your version of the
 * file(s), but you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version. If you delete this
 * exception statement from all source files in the program, then also delete
 * it in the license file.
 */

function refreshPendingCallRecords(){
    $.ajax({
        url:"/callRecord",
        success: function (data) {
            $('#pending-call-record-list').html(data)
        },
        error: function () {
            $('#pending-call-record-list').html(`<h3>Error loading pending call records</h3>`);
            showErrorBanner()
        },
        complete: function(data) {
            if(data.statusText === 'error'){
                showErrorBanner()
            } else {
                setTimeout(function () {
                    refreshPendingCallRecords()
                }, 5000)
            }
        },
        timeout: 10000
    })
}

function showCallRecordModal(id){
    $('.modal-backdrop').show();
    $('#call-record-modal').show();

    $.ajax({
        url: "/callRecord/finalizeRecord",
        data: {"uniqueId": id},
        success: function (data) {
            $('#call-record-modal').html(data)
        },
        error: function (data) {
            $('#call-record-modal').html(`<h3>Error loading Call Details</h3>`)
        }
    })
}

function refreshAdminTable(){
    let filtered = $('#admin-records-panel').attr("filtered") === "true";
    let target = filtered ? $('#record-search-table-container') : $('#record-table-container');

    let max = target.find('.pagination-max').val() ? target.find('.pagination-max').val() : "10";
    let offset = target.find('.offset').html() ? target.find('.offset').html() : "0";

    let searchCriteria = $('#admin-records-panel').attr("filtered") === "true" ? $('#record-search-form').serialize() : {};

        $.ajax({
            url: `/callRecord/list?max=${max}&offset=${offset}`,
            data: searchCriteria,
            success: function (data) {
                target.html(data);
                if ($('#nav-admin-records-link').length === 0) {
                    $('#nav-logout-link').before(
                        `<li id="nav-admin-records-link" ><a href="#" onclick="toggleAdminTable()" >Call Records List</a></li>`
                    )
                }
        },
            error: function (data) {
                console.log(data)
            },
            complete: function (data) {
                if (data.statusText === 'error') {
                    showErrorBanner()
                } else {
                    setTimeout(function () {
                        refreshAdminTable()
                    }, 10 * 1000)
                }
            },
            timeout: 6000
        })
}

function toggleAdminTable(){
    let tableVisible = $('#admin-records-panel')[0].style.display === 'block';

    if(tableVisible){
        $('#admin-records-panel').hide();
        $('#interface-panel').toggle("slide", {direction: "left"});
    }
    else {
        $('#interface-panel').hide();
        $('#admin-records-panel').toggle("slide", {direction: "left"});
    }

}

function hideCallRecordModal(){
    $('.modal-backdrop').hide();
    $('#call-record-modal').hide();

    $('#call-record-modal .modal-header').html(`<h1 class="modal-title" >After Call Report</h1>`);
    $("#call-record-modal .modal-body").html(`<h3>Loading call details...</h3>`)
}

function confirmHideCallRecordModal(){
    if(confirm("Are you sure?")) {
        hideCallRecordModal()
    }
}

function confirmHideCallRecordEditModal(){
    if(confirm("Are you sure?")) {
        hideCallRecordEditModal()
    }
}

function hideCallRecordEditModal(){
    $('.modal-backdrop').hide();
    $('#call-record-edit-modal').hide();

    $('#call-record-edit-modal .modal-header').html(`<h1 class="modal-title" >Edit Call Report</h1>`);
    $("#call-record-edit-modal .modal-body").html(`<h3>Loading call details...</h3>`)
}

function toggleFormInputs(){
    $('.assignee-toggle').toggle();
}

function processContactNameSelection(){
    let value = $('#record-customer-name select').val();

    let otherNameInput = $('#record-customer-name-other');

    if(value === "Other"){
        otherNameInput.css( "border", "1px solid black;" );
        otherNameInput.show()
    }
    else {
        otherNameInput.hide();
        updateCustomerIdOptions()
    }
}

function updateCustomerIdOptions(){
    let name = $('#record-customer-name select').val();

    if(name === "Other") {
        name = $('#record-customer-name-other input').val();
    }

    let defaultOptions = `<option value="None" >None</option>
                                <option value="Other" >Other...</option>`;

    if(name) {
        $('#record-customer-name-other input').css(
            "border", "1px solid #CCC"
        );
        $.ajax({
            url: "/callRecord/lookupCustomerIds",
            data: {"name": name},
            success: function (data) {
                $('#record-customer-id select').html(data + defaultOptions)
            }
        })
    } else {
        $('#record-customer-id select').html(defaultOptions);
        $('#record-customer-name-other input').css(
            "border", "1.5px solid red"
        )
    }
}

function processCustomerIdSelection(){
    let value = $('#record-customer-id select').val();

    if(value === "Other"){
        $('#record-customer-id-other').show()
    }
    else {
        $('#record-customer-id-other').hide()
    }
}

function submitCallRecord(){
    let satisfaction = $('#record-customer-satisfaction :checked').val();
    let notes = $('#record-notes textarea').val();
    let customerName = $('#record-customer-name select').val();

    if(!customerName || customerName === 'Other'){
        customerName = $('#record-customer-name-other input').val();
    }

    if(notes && satisfaction && customerName) {
        $.ajax({
            url: "/callRecord/closeOut",
            data: $('#record-form').serialize(),
            beforeSend: function () {
                $('#call-record-modal .error-message').html('');

                $('#record-submit-btn').hide();
                $('#record-close-btn').replaceWith(`<a href="#" class="btn btn-default" onclick="hideCallRecordModal()" >Close</a>`);
                $("#call-record-modal .modal-body").html(`<h1><i class="fa fa-pulse fa-spinner" ></i>Submitting Report...</h1>`)
            },
            complete: function (data) {
                $('#record-close-btn').show();
                $('#call-record-modal .modal-header').html(`<h1 class="modal-title" ><i class="fa fa-check-circle text-success" ></i> Report Submitted</h1>`);
                $("#call-record-modal .modal-body").replaceWith(data);
            },
            timeout: 5000
        })
    }
    else {
        $('#call-record-modal .error-message').html(`<h3>All reports must include a contact name, satisfaction rating and call notes.</h3>`)
    }
}

function reassignCallRecord(){
    $.ajax({
        url: "/callRecord/reassign",
        data: {"id": $('#record-uniqueid').val(), "agentName": $('#record-reassign select').val() },
        beforeSend: function () {
            $('#record-submit-btn').hide();
            $('#record-close-btn').replaceWith(`<a href="#" class="btn btn-default" onclick="hideCallRecordModal()" >Close</a>`);
            $("#call-record-modal .modal-body").html(`<h1><i class="fa fa-pulse fa-spinner" ></i>Reassigning Report...</h1>`)
        },
        complete: function (data) {
            $('#record-close-btn').show();
            $('#call-record-modal .modal-header').html(`<h1 class="modal-title" ><i class="fa fa-check-circle text-success" ></i> Report x</h1>`);
            $("#call-record-modal .modal-body").replaceWith(data);
        },
        timeout: 5000
    })
}

function loadRecordRecording(uniqueId){
    $.ajax({
        url:"/recording/recordPlayer",
        data: {"uniqueId": uniqueId},
        beforeSend: function () {
          $('.record-recording').html(`<h4><i class="fa fa-spinner fa-pulse" ></i> Loading...</h4>`)
        },
        success: function (data) {
            $('.record-recording').html(data)
        },
        error: function (data) {
            $('.record-recording').html(`<h4>Error loading recording: ${data.statusText}</h4>`)
        },
        timeout: 15000
    })
}

function showSubmissionInputs(){
    $.ajax({
        url: "/callRecord/submissionReportInputs",
        success: function (data) {
            $('#call-record-report-results').hide();

            let modal = $('#call-record-report-inputs');

            modal.html(data);
            modal.show();

            $('.modal-backdrop').show()
        }
    })

}

function generateRecordSubmissionReport(){
    let agentIds = [];
    $("input[name='agents']:checked").each(function(){ agentIds.push($(this).val()) });

    let data = {
                "agents": agentIds,
                "dateRange": $('#record-submission-date-range').val()
                };

    $.ajax({
        url: "/callRecord/submissionReport",
        data: data,
        beforeSend: function(){
            $('#report-submit-btn').replaceWith(`<span>Generating...<i class="fa fa-2x fa-spinner fa-pulse" /></span>`)
        },
        success: function (data) {
            $('.modal-backdrop').show();
            $('#call-record-report-results').html(data);

            showReportResults()
        },
        error: function(data){
            $('#admin-records-table').hide();
            $('#admin-records-report-table').html(`<h3>Error loading Report: ${data.statusText}</h3>`).show()
        }
    })
}

function viewAgentRecordList(id){
    $.ajax({
        url: "/callRecord/agentRecordList",
        data: {"id": id},
        success: function (data) {
            $('#call-record-report-results-detail .modal-body').html(data);

            let backlink = `<h4><a href="#" onclick='showReportResults()' >${"<<"} Back</a></h4>`;
            let agentName = $('#call-record-report-results-detail .record-table-agent-name').filter(":first").html();
            let agentNameHeader = `<h3>${agentName}'s records</h3>`;

            $('#call-record-report-results-detail .modal-header').html(backlink + agentNameHeader);

            showReportResultDetail()
        }
    })
}

function restoreCustomerInteractionInputs(){
    $.ajax({
        async: true,
        url: "/callRecord/customerInteractionInputs",
        success: function (data) {
            let modal = $('#call-record-report-inputs');

            modal.html(data);
            modal.show();

            $('#call-record-report-results').hide()
        }
    })
}

function showCustomerInteractionInputs(backdrop){
    $.ajax({
        async: true,
        url: "/callRecord/customerInteractionInputs",
        success: function (data) {
            let modal = $('#call-record-report-inputs');

            modal.html(data);
            modal.show();

            $('.modal-backdrop').show()
        }
    })
}

function showReportResults(){
    $('#call-record-report-inputs').hide();
    $('#call-record-report-results-detail').hide();
    $('#call-record-report-record-detail').hide();

    $('#call-record-report-results').show()
}

function showReportResultDetail(){
    $('#call-record-report-inputs').hide();
    $('#call-record-report-results').hide();
    $('#call-record-report-record-detail').hide();

    $('#call-record-report-results-detail').show()
}

function showReportRecordDetail(id) {
    $.ajax({
        url: "/callRecord/show",
        data: {"id": id},
        success: function (data) {
            $('#call-record-report-inputs').hide();
            $('#call-record-report-results').hide();
            $('#call-record-report-results-detail').hide();

            $('#call-record-report-record-detail').html(data);

            let backlink =  `<h4><a href="#" onclick='showReportResultDetail()' >${"<<"} Back</a></h4>`;

            $('#call-record-report-record-detail .modal-header').html(backlink);
            $('#call-record-report-record-detail .modal-footer').html(``);

            $('#call-record-report-record-detail').show()
        },
        error: function (data) {
            $('#call-record-report-record-detail').html(`<h3>Error loading Call Details</h3>`)
        }
    })
}

function hideReportInputs(){
    $('#call-record-report-inputs').hide();
    $('.modal-backdrop').hide();
}

function processDateRangeSelection(){
    let val = $('#date-range-select').val();

    if(val === 'custom'){
        $('#custom-date-range').slideDown()
    }
    else {
        $('#custom-date-range').slideUp()
    }
}

function hideReportResults(){
    $('#call-record-report-results').hide();
    $('.modal-backdrop').hide();
}

function hideReportResultDetail(){
    $('#call-record-report-results-detail').hide();
    $('.modal-backdrop').hide();
}

function generateCustomerInteractionReport() {
    $.ajax({
        url: "/callRecord/customerInteractionReport",
        data: $('#record-customer-interaction-inputs').serialize(),
        beforeSend: function(){
            $('#report-submit-btn').replaceWith(`<span>Generating...<i class="fa fa-2x fa-spinner fa-pulse" /></span>`)
        },
        success: function (data) {
            $('#call-record-report-inputs').hide();
            $('#report-submit-btn').hide();

            let results = $('#call-record-report-results');
            results.html(data);
            results.show()
        }
    })
}

function editRecord(uniqueId){
    $.ajax({
        url: "/callRecord/edit",
        data: {"id": uniqueId},
        success: function (data) {
            let modal = $('#call-record-edit-modal');

            modal.html(data);

            modal.show();
            $('.modal-backdrop').show()
        },
        error: function (data) {

        }
    })
}

function saveCallRecord(){
    let satisfaction = $('#edit-record-customer-satisfaction :checked').val();
    let notes = $('#edit-record-notes textarea').val();
    let customerName = $('#edit-record-customer-name select').val();

    if(!customerName || customerName === 'Other'){
        customerName = $('#edit-record-customer-name-other input').val();
    }

    if(notes && satisfaction && customerName) {
        $.ajax({
            url: "/callRecord/save",
            type: "POST",
            data: $('#edit-record-form').serialize(),
            beforeSend: function () {
                $('#call-record-edit-modal .error-message').html('');

                $('#edit-record-submit-btn').hide();
                $('#edit-record-close-btn').replaceWith(`<a href="#" class="btn btn-default" onclick="hideCallRecordModal()" >Close</a>`);
                $("#call-record-edit-modal .modal-body").html(`<h1><i class="fa fa-pulse fa-spinner" ></i>Submitting Changes...</h1>`)
            },
            complete: function () {
                $('#edit-record-close-btn').show();
                $('#call-record-edit-modal .modal-header').html(`<h1 class="modal-title" ><i class="fa fa-check-circle text-success" ></i> Changes Saved!</h1>`);
                $("#call-record-edit-modal .modal-body").replaceWith("");
            },
        })
    }
    else {
        $('#call-record-edit-modal .error-message').html(`<h3>All reports must include a contact name, satisfaction rating and call notes.</h3>`)
    }
}

function showRecordOnDateField(){
    $('#record-search-from-date, #record-search-to-date').parent().hide();
    $('#record-search-on-date').parent().fadeIn()
}

function showRecordDateRangeFields(){
    $('#record-search-on-date').parent().hide();
    $('#record-search-from-date, #record-search-to-date').parent().fadeIn()
}

function searchRecords(){
    let custId = $('#record-search-customer-id').val().substring(0, 6);
    let max = $('#record-pagination-max').val() ? $('#record-pagination-max').val() : "10";

    if(!custId || custId.match(/[0-9|A-F|a-f]{6}/)) {
        $.ajax({
            url: `/callRecord/list?max=${max}`,
            data: $('#record-search-form').serialize(),
            success: function (data) {
                let searchTable = $('#record-search-table-container');
                searchTable.html(data);

                $('#record-table-container').hide();
                $('#record-search-count').show();
                searchTable.show();

                $('#record-clear-btn').show();
                $('#admin-records-panel').attr("filtered", "true")
            },
            error: function (data) {
                let table = $('#record-table-container');

                table.html(`<h2>Error loading Call Records: ${data}</h2>`);
                table.show()
            }
        })
    }
}

function clearRecordFilter(){
    $('#record-search-table-container').hide();
    $('#record-table-container').show();

    $('#record-clear-btn').hide();
    $('#record-search-form')[0].reset();
    $('#admin-records-panel').attr("filtered", "false")
}

function goToRecordPage(offset){
    let max = $('#record-pagination-max').val() ? $('#record-pagination-max').val() : "10";
    let criteria = $('#admin-records-panel').attr("filtered") === "true" ? $('#record-search-form').serialize() : {};

    $.ajax({
        url: `/callRecord/list?max=${max}&offset=${offset}`,
        data: criteria,
        success: function (data) {
            let filtered = $('#admin-records-panel').attr("filtered") === "true";
            let target = filtered ? $('#record-search-table-container') : $('#record-table-container');

            target.html(data)
        }
    })
}

function toggleAgentSelectionWell(){
    $('#report-agent-name-selection').fadeToggle()
}

function showUserPendingTable(){
    $.ajax({
        url:"/callRecord/userTable",
        success:function (data) {
            let header = `
                <h3 style="margin-top: 0;" class="list-group-item" >My Pending Call Records</h3>
                <a class="list-group-item" href="#" onclick="hideUserPendingTable()" >&lt;&lt; Back to Interface</a>
            `;

            $('#interface-panel').hide();

            $('#user-pending-records-table').html(header + data);
            $('#user-pending-records-table').show("slide")
        }
    })
}

function hideUserPendingTable(){
    $('#user-pending-records-table').hide();
    $('#interface-panel').show("slide")
}