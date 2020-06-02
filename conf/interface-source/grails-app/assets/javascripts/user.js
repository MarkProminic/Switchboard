/**
 * user.js
 * Created by Luis Alcantara on 1/24/17.
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

function refreshUserCalls() {
    $.ajax({
        url: "/user",
        success: function(data){
            let callList = $('#user-call-list');
            callList.html(data);

            if(callList[0].children.length > 0){
                showOnCallDialer();
                showOnCallAgentList()
            }
            else {
                showIdleDialer();
                showIdleAgentList()
            }
        },
        error: function (data) {
            console.log("Unable to refresh user info.")
        },
        complete: function(data) {
            if(data.statusText === 'error') {
                showErrorBanner()
            }else {
                setTimeout(function () {
                    refreshUserCalls()
                }, 1000)
            }
        }
    })
}

function showOnCallDialer(){
    let placeCallBtn = $('#place-call-btn');
    let addToCallBtn = $('#add-to-call-btn');

    placeCallBtn.removeClass("enter-target");
    addToCallBtn.addClass("enter-target");

    placeCallBtn.hide();
    addToCallBtn.show()
}

function showIdleDialer(){
    let placeCallBtn = $('#place-call-btn');
    let addToCallBtn = $('#add-to-call-btn');

    addToCallBtn.removeClass("enter-target");
    placeCallBtn.addClass("enter-target");

    addToCallBtn.hide();
    placeCallBtn.show()
}

function showOnCallAgentList(){
    let agentList = $('#agent-list');

    agentList.find('.idle-btns').hide();
    agentList.find('.on-call-btns').show()
}

function showIdleAgentList(){
    let agentList = $('#agent-list');

    agentList.find('.on-call-btns').hide();
    agentList.find('.idle-btns').show()
}

function changeLocation(id) {
    let location = id;
    if(!id){
        location = $('#user-location-list').val()
    }

    $.ajax({
        url: "/user/setLocation",
        data: {"id": location},
        success: function(data) {
            let list = $('#followme-list');
            list.children()[0].remove();
            list.prepend(data)
        }
    })
}

function createAsteriskSettingsModal(){
    let routeList = "";

    $.ajax({
        url:"/inboundRoute",
        success: function (html) {
            $('#asterisk-settings-modal').find('.modal-body').html(html)
        }
    })

}

function addBlacklistNumber() {
    let newNumber = $('#new-blacklist-number-number').val();

    $.ajax({
        url: "asteriskCall/addBlacklistNumber",
        data: {"newNumber": newNumber},
        error: function (data) {
            let message = JSON.parse(data.responseText);

            $('#blacklist-message').html(message)
        },
        success: function (data) {
            $('#new-blacklist-number-number').val("");
            $('#blacklist-message').html("The Number " + newNumber + " is now blacklisted.")
        }
    })
}

function parkCall(call){
    $.ajax({
        url: "asteriskCall/parkCall",
        data: {"callId": call},
        error: function () {
            setUserMessage("Error Parking Call, try '#75' to park manually.", false)
        }
    })
}

function resetUserMessage(){
    $('#user-message').slideUp()
}

function killPhantomCall(id){
    $.ajax({
        url: "/asteriskCall/killPhantomCall",
        data: {"id": id},
        success: function (displayId) {
            $(`user-call-${displayId}`).remove()
        }
    })
}

function logOffUser() {
    $.ajax({url: "/user/logOffUser"});

    window.location.replace("/logoff")
}

function logOffAndSwitch() {
    changeLocation("Mobile");
    logOffUser()
}

function logOffAll() {
    $.ajax({
        url: "/user/logOffAllQueues",
        complete: function () {
            logOffUser()
        }
    })
}

function toggleAdditionalCustIds(callId){
    let callTile = $('#user-call-' + callId);

    callTile.find('.additional-custid').toggle();
    callTile.find('.custid-ctrl').toggleClass('hidden inline-block')
}