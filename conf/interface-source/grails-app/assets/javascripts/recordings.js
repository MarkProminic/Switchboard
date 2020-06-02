/**
 * recordings
 * Created by Luis Alcantara on 5/2/17.
 */

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

function createRecordingsPanel(){
    goToPage(0);
}

function showOnDateField(){
    $('#recordings-search-from-date, #recordings-search-to-date').parent().hide();
    $('#recordings-search-on-date').parent().fadeIn()
}

function showDateRangeFields(){
    $('#recordings-search-on-date').parent().hide();
    $('#recordings-search-from-date, #recordings-search-to-date').parent().fadeIn()
}

function refreshRecordings(poll){
    $.ajax({
        url: "/recording",
        success: function (data) {
            let recordingsListDOM = $('#recordings-list');
            let update = recordingsListDOM.attr("update");

            if(update) {
                // let recordings = data.recordings;
                //
                // let recordingsList = "";
                //
                // let lastTotalItem = $('#recordings-total-count');
                // let lastTotal = lastTotalItem.html() !== "" ? lastTotalItem.html() : 0;
                // let totalCount = data.totalCount;
                //
                // if (lastTotal < totalCount) {
                //     for (let recording of recordings) {
                //         recordingsList += createRecordingRow(recording)
                //     }
                //
                //     recordingsListDOM.html(recordingsList);
                //     lastTotalItem.html(totalCount);
                //
                //     updatePaginationControls(data.paginationInfo, data.recordings.length)
                // }
                $('#recordings-panel').html(data);
            }
        },
        complete: function(data) {
            if(data.statusText === 'error') {
                showErrorBanner()
            } else {
                // attachEventListeners();
                // initializeAllPlayers();
                if(poll) {
                    setTimeout(function () {
                        refreshRecordings();
                    }, 10 * 1000)
                }
            }
        },
        timeout: 10 * 1000
    })

}

function filterRecordings(offset){
    let caller = $('#recordings-search-caller').val();
    let callee = $('#recordings-search-callee').val();
    let uniqueId = $('#recordings-search-uniqueid').val();

    let dateCriteriaType = $("input[name='date-range-toggle']:checked").get(0).id;

    let dateCriteria = {};

    if(dateCriteriaType === "on") {
        dateCriteria = {on: $("#recordings-search-on-date").val()}
    }
    else {
        dateCriteria = {from: $('#recordings-search-from-date').val(),
                        to: $('#recordings-search-to-date').val()}
    }

    let criteria = {caller: caller, callee: callee, uniqueId: uniqueId, date: dateCriteria, dateType: dateCriteriaType};

    let max = $('#recordings-pagination-max').val();

    $.ajax({
        url: `/recording/filter?max=${max}&offset=${offset}`,
        data: criteria,
        success: function (data) {
            $('#recordings-panel').html(data);
            $('#recordings-table').attr("filtered", "true");
            $('#recordings-clear-btn').show();
        }
    })
}

function clearFilter(){
    $('#recordings-search-caller').val('');
    $('#recordings-search-callee').val('');
    $('#recordings-search-uniqueid').val('');

    $('#recordings-search-on-date').val('');
    $('#recordings-search-to-date').val('');
    $('#recordings-search-from-date').val('');

    $('#recordings-table').attr("filtered", "false");
    $('#recordings-clear-btn').hide();
    // $('#recordings-total-count').html("0");

    goToPage(0);
}

function createDurationString(duration){
    let hours = Math.floor(duration / 3600);
    let minutes = Math.floor(duration / 60 )% 60;
    let seconds = Math.floor(duration % 60);

    if(hours < 10){ hours = `0${hours}`}
    if(minutes < 10){ minutes = `0${minutes}` }
    if(seconds < 10){ seconds = `0${seconds}` }

    return `${hours}:${minutes}:${seconds}`
}

function goToPage(offset){
    let onPageMax = $('#recordings-pagination-max').val();
    let max = onPageMax ? onPageMax : 10;
    let filtered = $('#recordings-table').attr("filtered");

     if(filtered === "true"){
        filterRecordings(offset)
    }
    else {
        $.ajax({
            url: `/recording?offset=${offset}&max=${max}`,
            success: function (html) {
                $('#recordings-panel').html(html)
            }
        });
    }
}