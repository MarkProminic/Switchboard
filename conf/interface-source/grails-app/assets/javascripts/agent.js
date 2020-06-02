/**
 * agent.js
 * Created by Luis Alcantara on 9/14/16.
 *
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

function refreshAgentList(){
    $.ajax({
        url: `/agent/refreshAgentList`,
        success: function (data) {
            $('#agent-list').html(data)
        },
        complete: function(data) {
            if(data.statusText === 'error') {
                showErrorBanner()
            }else {
                setTimeout(function () {
                    refreshAgentList()
                }, 5*1000)
            }
        },
        timeout: 5 * 1000
    })
}

function transferCallTo(agent, location){
    // console.log(agent);
    $.ajax({
        url: "/asteriskCall/transferCallTo",
        data: { 'agentId': agent, 'location': location },
        method: 'POST',
        success: function(){
            $('#call-status'+ agent).html("Call transferred")
        },
        failure: function(){
            $('call-status'+agent).html("Call failed to transfer")
        }
    })
}

function addToQueue(agent, queue){
    $.ajax({
        url: "queue/addMember",
        data: {"agentId": agent, "queueName": queue},
        success: function () {
            refreshQueueList(true);

            $('#available-queue-' + queue).remove()
        }
    })
}

function showNewAgentModal(){
    refreshNewAgentExtensionOptions();
    refreshVoicemailPagerCarriers();

    $('.modal-backdrop').show();
    $('#new-agent-modal').show()
}

function hideNewAgentModal(){
    let newAgentModal = $('#new-agent-modal');

    newAgentModal.find('input').val("");
    $('.sms-field-panel').hide();
    $('#voicemail-sms-opt-in').attr("checked", "false");
    $('#agent-creation-message').html('');
    $('#agent-creation-submit-btn').show();

    $('.modal-backdrop').hide();
    newAgentModal.hide()
}

function refreshNewAgentExtensionOptions(){
    $.ajax({
        url: "agent/getNewAgentExtensionOptions",
        complete: function (data) {
            let options = JSON.parse(data.responseText).options;
            let optionList = "";

            for(let option of options){
                optionList += `<option value='${option.value}' >${option.name}</option>`
            }

            $('#new-agent-extension-name').html(optionList)
        }
    })
}

function refreshVoicemailPagerCarriers(){
    $.ajax({
        url: "voicemail/carriers",
        success: function (options) {
            let optionList = "";

            for(let option of options){
                optionList += `<option value='${option.email}'>${option.name}</option>`
            }

            $("#new-agent-pager-carrier").html(optionList)
        }
    })
}

function callAgent(agent, location){
    let number = location;

    if(!location){
        number = "primary"
    }

    $.ajax({
        url: "/agent/callAgent",
        data: {"agentId": agent, "locationNumber": number},
        beforeSend: function(){
            logAction('Calling agent')
        }
    })
}

function addAgentToCall(agent, location){
    $.ajax({
        url: "/conferenceRoom/addAgent",
        data: {"agentId": agent, "location": location},
        error: function () {
            setUserMessage(`Error adding ${agent}(${location}) to your call.`, false)
        }
    })
}

function toggleVoicemailSMSFields(){
    $(".sms-field-panel").fadeToggle()
}