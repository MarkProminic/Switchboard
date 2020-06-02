/**
 * parkingLot
 * Created by Luis Alcantara on 9/16/16.
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

function refreshParkingLot() {
        $.ajax({
            url: "/asteriskCall/refreshParkingLot",
            success: function (parkedCalls) {
                // let json = JSON.parse(data);
                // let calls = data.calls;
                let displayedCalls = $("#parking-lot").find(".parked-call");
                
                if(parkedCalls.length >= displayedCalls.length) {
                    for(let parkedCall of parkedCalls) {
                        let container = $('#parked-call-' + parkedCall.displayId);
                        
                        if (container.length !== 0) {
                            updateParkedCall(parkedCall, container)
                        } else {
                            addParkedCall(parkedCall)
                        }
                    }
                } else {
                    $("#parking-lot").html("");  // rebuild entire call list
                    
                    for(let parkedCall of parkedCalls) {
                        addParkedCall(parkedCall);
                    }
                }
            },
            complete: function(data) {
                if(data.statusText === 'error'){
                    showErrorBanner()
                }else {
                    setTimeout(function () {
                        refreshParkingLot()
                    }, 1000)
                }
            },
            timeout: 5000
        })
}

function updateParkedCall(call, container){
    let parkedBy = container.find('.parked-info');
    let duration = container.find(".parked-call-duration");
    let queue = container.find(".parked-call-queue");

    if(call.state === "parked"){
        parkedBy.html(`Parked by ${call.parkedBy} @ ${call.parkingSpace}`)
    }

    duration.html(call.durationString)
}

function addParkedCall(call){

    $('#parking-lot').append(`<li id='parked-call-${call.displayId}' class='list-group-item parked-call'>
                                <div class='row'>
                                    <div class='col-md-5'>
                                        <p class='parked-call-name' >${call.display.name}</p>
                                        <p class='parked-call-number' >${call.display.number}</p>
                                    </div>
                                    <div class='col-md-5'>
                                        <p class='parked-call-duration text-right'>${call.durationString}</p>
                                        <p class='text-danger text-right parked-info'>${call.parkedBy ? "Parked by " + call.parkedBy : ""}${call.parkingSpace ? " @ " + call.parkingSpace : ""}</p>
                                    </div>
                                    <div class='col-md-2'>
                                        <div class='btn btn-group-sm pull-right'>
                                            <a class='btn btn-success' href='#' onclick='answerCall("${call.id}")'><i class='fa fa-phone fa-fw'></i></a>
                                            <!-- <a class='btn btn-success' href='#' onclick='addParkedCallToCall("${call.id}")'><i class='fa fa-plus fa-fw'></i></a> -->
                                        </div>
                                    </div>
                                </div>
                            </li>`)
}

function answerCall(callId){
    $.ajax({
        url: "asteriskCall/answerCall",
        data: {"callId": callId}
    })
}

function addParkedCallToCall(callId){
    $.ajax({
        url: "asteriskCall/mergeCalls",
        data: {"callId": callId}
    })
}