/**
 * queueCalls
 * Created by Luis Alcantara on 2/6/17.
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

function refreshCallList(){
    $.ajax({
        url: "/queue/refreshQueueCalls",
        success: function (queueCalls) {
            // let json = JSON.parse(data);
            // let calls = data.calls;
            let displayedCalls = $(".queue-pending-call, .queue-active-call");

            if(queueCalls.length >= displayedCalls.length) {
                for(let queueCall of queueCalls) {
                    let incomingSelector = $('#queue-call-' + queueCall.displayId);
                    let activeSelector = $("#queue-" + queueCall.queue + "-" + queueCall.owners.primary + ".queue-active-call");

                    if (incomingSelector.length != 0 || activeSelector.length != 0) {
                        updateQueueCall(queueCall)
                    } else {
                        addQueueCall(queueCall)
                    }
                }
            } else {
                $("#queue-call-list").html("");  // rebuild entire call list
                $(".queue-member-list").html("");
                $(".queue-active-call").remove();

                refreshQueueList(true);

                for(let queueCall of queueCalls) {
                    addQueueCall(queueCall);
                }
            }
        },
        complete: function(data) {
            if(data.statusText === 'error'){
                showErrorBanner()
            }else {
                setTimeout(function () {
                    refreshCallList()
                }, 1000)
            }
        },
        timeout: 5000
    })
}

function updateQueueCall(call){
    let incomingCall = $('#queue-call-list').find("#queue-call-" + call.displayId);
    let activeCall =  $("#queue-" + call.queue + "-" + call.owners.primary + ".queue-active-call");
    let queueCall;

    if(incomingCall.length != 0 && activeCall.length == 0 && call.owners.primary != '') {
        incomingCall.remove();
        addQueueCall(call)
    }

    if(incomingCall.length != 0) {
        queueCall = incomingCall
    } else {
        queueCall = activeCall
    }

    queueCall.find(".queue-call-duration").html(call.durationString);
    queueCall.find(".queue-call-name").html(call.name);
    queueCall.find(".queue-call-queue").html(call.queue)

}

function addQueueCall(call){
    let queue = "";

    if(call.queue){
        queue = call.queue
    }


    if(call.owners.primary == ''){
        $('#queue-call-list').append(
            `<li id='queue-call-${call.displayId}' class='list-group-item queue-pending-call'>
                <div class='row'>
                    <div class='col-md-5'>
                        <p class='queue-call-name' >${call.caller.name}</p>
                        <p class='queue-call-queue' >${queue}</p>
                    </div>
                    <div class='col-md-5'>
                        <p class='queue-call-number text-right' >${call.caller.number}</p>
                        <p class='queue-call-duration text-right'>${call.durationString}</p>
                    </div>
                    <div class='col-md-2 queue-call-btn'>
                        <a class='btn btn-sm btn-success pull-right' href='#' onclick='answerCall("${call.id}")'><i class='fa fa-phone fa-fw'></i></a>
                    </div>
                </div>
            </li>`)
    } else {
        let memberList = $('#queue-' + queue);
        let member = memberList.find("#queue-" + queue + "-" + call.owners.primary);
        let activeMember = 
            `<li id='queue-${queue}-${call.owners.primary}' class='list-group-item queue-active-call'>
                <div class='row'>
                    <div class='col-md-5'>
                        <p class='queue-call-member' >${call.owners.primary.replace(/_/g, " ")}</p>
                        <p class='queue-call-name' >${call.caller.name}</p>
                    </div>
                    <div class='col-md-5 pull-right'>
                        <p class='queue-call-duration text-right'>${call.durationString}</p>
                        <p class='queue-call-number text-right' >${call.caller.number}</p>
                    </div>
                </div>
            </li>`;

        if(member.length != 0) {
            $('#queue-' + queue).find('.queue-active-calls').append(activeMember)
        } else {
            memberList.append(activeMember)
        }
    }
}

function answerCall(callId){
    $.ajax({
        url: "asteriskCall/answerCall",
        data: {"callId": callId}
    })
}