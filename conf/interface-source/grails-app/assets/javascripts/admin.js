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

function showAdminPanel(){
    $.ajax({
        url: "/admin",
        success:function (data) {
            $('.modal-backdrop').show();
            $('#admin-panel-modal').html(data).show()
        }
    })
}

function hideAdminPanel(){
    $('.modal-backdrop').hide();
    $('#admin-panel-modal').hide()
}

function loadAdminPanel(){
    $.ajax({
        url:"/admin/index",
        success: function (data) {
            $('#admin-panel-modal').html(data);
        },
        error: function(){
            $('#admin-panel-modal').html(`
            <div><h3>Could not load Admin Panel</h3></div>
            `)
        },
        complete: function () {
            setTimeout(function(){
                refreshAdminPanel()
            }, 30*1000)
        }
    })
}

function refreshAdminPanel(){
    refreshAdminAgentList();
    refreshAdminPeerList();
}

function refreshAdminAgentList(){
    $.ajax({
        url: "/admin/agents",
        success: function(data){
            $('#admin-agent-list').html(data)
        },
        error: function(){
            $('#admin-agent-list').html(`
            <div><h3>Could not load Agent list</h3></div>
            `)
        },
        complete: function () {
            setTimeout(function(){
                refreshAdminAgentList()
            }, 30*1000)
        }
    })
}

function refreshAdminPeerList(){
    $.ajax({
        url: "/admin/peers",
        success: function(data){
            $('#admin-peer-list').html(data)
        },
        error: function(){
            $('#admin-peer-list').html(`
            <div><h3>Could not load SIP Peer list</h3></div>
            `)
        },
        complete: function () {
            setTimeout(function(){
                refreshAdminPeerList()
            }, 30*1000)
        }
    })
}

function showAgentCreationPanel(){
    $('#admin-create-agent-panel').slideDown()
}

function hideAgentCreationPanel(){
    document.getElementById('admin-agent-create').reset();
    $('#admin-create-agent-panel').slideUp()
}

function createNewAgent(){
    let name = $("#new-agent-name").val();
    let extensionPrefix = $("#new-agent-extension-name").val();
    let email = $("#new-agent-email").val();
    let smsOptIn = ($("#voicemail-sms-opt-in :checked").length > 0);

    let pager = $('#new-agent-pager-number').val();
    let carrier = $('#new-agent-pager-carrier').val();

    if(name && email && (!smsOptIn || smsOptIn && pager && carrier)){
        $.ajax({
            url: "/agent/createNew",
            data: {"agentName": name, "agentEmail": email, "extensionPrefix": extensionPrefix,
                "smsOptIn": smsOptIn, "pager": pager, "carrier": carrier },
            success: function(data){
                $('#agent-creation-message').html(data.responseJSON.message);
            },
            error: function(data){
                $('#agent-creation-message').html(data.responseJSON.message)
            }
        })
    }
    else {
        $('#agent-creation-message').html("New Agents require a name and email as well as cellphone information " +
            "if voicemail SMS alerts are desired")
    }
}

function disableAgent(id){
    $.ajax({
        url: "/agent/disable",
        data: {"id": id}
    })
}

function reenableAgent(id){
    $.ajax({
        url: "/agent/reenable",
        data: {"id": id}
    })
}

function showPeerCreationPanel(){
    $('#admin-create-peer-panel').slideDown()
    fillPrefixOptions()
}

function fillPrefixOptions(){
    let id = $('#admin-peer-agent').find('select').val()

    $.ajax({
        url: "/extensionLocation/available",
        data: {"id": id},
        success: function(data){
            $('#admin-peer-prefix').find('select').html(data)
        }
    })
}

function togglePeerCreationInputs(type){
    if(type === 'peer'){
        $('#admin-peer-create').find('.extension-input').hide();
        $('#admin-peer-create').find('.trunk-input').fadeIn()
    }
    else if (type === 'friend'){
        $('#admin-peer-create').find('.trunk-input').hide();
        $('#admin-peer-create').find('.extension-input').fadeIn()
    }
    else {
        console.log('Unexpected Peer type of ' + type)
    }
}

function makeAdmin(id){
    $.ajax({
        url: "/admin/grant",
        data: {"id": id},
        success: function (data) {
            $('#admin-message').html(`<h4>${data}</h4>`);
            $('#admin-agent-' + id).find('.admin-grant-btn').replaceWith(`<a class="btn btn-sm btn-warning admin-revoke-btn" onclick="removeAdmin('${id}')" ><i class="fa fa-minus-circle" ></i> Remove as Admin</a>`)
        },
        error: function (data) {
            $('#admin-message').html(`<h4 class="failure" >${data}</h4>`)
        }
    })
}

function removeAdmin(id){
    $.ajax({
        url: "/admin/revoke",
        data: {"id": id},
        success: function (data) {
            $('#admin-message').html(`<h4>${data}</h4>`);
            $('#admin-agent-' + id).find('.admin-revoke-btn').replaceWith(`<a class="btn btn-sm btn-success admin-grant-btn" onclick="makeAdmin('${id}')" ><i class="fa fa-plus-circle" ></i> Make Admin</a>`)
        },
        error: function (data) {
            $('#admin-message').html(`<h4 class="failure" >${data}</h4>`)
        }
    })
}