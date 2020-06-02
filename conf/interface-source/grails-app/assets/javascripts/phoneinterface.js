/**
 * phoneinterface
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

function logoutUser(){
    window.location.href = "/logout"
}

function toggleNavbar(){
    let paneWidth = $('#navbar-pane').css("width");

    if(paneWidth === "0px"){
        showNavbar()
    } else {
        hideNavbar()
    }
}

function hideNavbar() {
    $('#navbar-pane').animate({width: "0px"}, 400, "swing", function () {
        $("#navbar").hide()
    });
    $('#main-pane').animate({width: "99%"}, 400);

    let togglePaneCaret = $('#toggle-pane-caret');

    togglePaneCaret.removeClass("fa-caret-left");
    togglePaneCaret.addClass("fa-caret-right")

}

function showNavbar() {
    $("#navbar").show();

    $('#navbar-pane').animate({width: "130px"}, 400);
    $('#main-pane').animate({width: "88.9%"}, 400);

    let togglePaneCaret = $('#toggle-pane-caret');

    togglePaneCaret.removeClass("fa-caret-right");
    togglePaneCaret.addClass("fa-caret-left")
}

function showSettingsModal(name){
    $('.modal-backdrop').show();
    $('#'+ name +'-settings-modal').show()
}

function hideSettingsModal(name){
    $('.modal-backdrop').hide();
    $('#' + name + '-settings-modal').hide()
}

function updateState(container, state, ownedCall){
    let stateIcon = container.find(".state-icon");

    if(state === 'available'){
        stateIcon.html("<i title='Available' class='fa fa-user fa-3x list-group-item-text text-success' ></i>")
    }

    if(state === 'oncall'){
        stateIcon.html(`<i title='On Call${(ownedCall) ? " for " + ownedCall.duration : ""} ${(ownedCall) ? " with " + ownedCall.display.name : ""}' class='fa fa-user fa-3x list-group-item-text text-warning' ></i>`)
    }

    if(state === 'busy'){
        stateIcon.html("<i title='Busy' class='fa fa-user fa-3x list-group-item-text text-danger' ></i>")
    }

    if(state === 'offline'){
        stateIcon.html("<i title='Offline' class='fa fa-user fa-3x list-group-item-text text-muted' ></i>")
    }
}

function showRecordingsPane(){
    $('#nav-recordings-link').hide();
    $('#nav-interface-link').show();

    $('#interface-panel').hide();
    $('#recordings-panel').show("slide", {direction: "left"})
}

function showInterfacePane(){
    $('#nav-interface-link').hide();
    $('#nav-recordings-link').show();

    $('#recordings-panel').hide("slide", {direction: "left"});
    $('#interface-panel').show()
}

function setUserMessage(message, success){
    let successIcon =`<i class="fa fa-2x ${success ? 'fa-check-circle text-success' : 'fa-exclamation-circle text-danger'}" ></i>`;


    $('#user-message').html(`<div class="col-md-1" >${successIcon}</div>
                              <h5 class="col-md-8" >${message}</h5>
                              <div class="col-md-3" onclick="resetUserMessage()" >
                                <small class="text-primary" >Click to dismiss</small>
                              </div>`).slideDown()
}

function showModalMessage(target, message){
    let messageDiv = $('#' + target + '-message');
    messageDiv.html(message);

    setTimeout(function(){
        messageDiv.fadeOut();
        messageDiv.html('');
        messageDiv.show();
    }, 10 * 1000);
}

function logAction(message){
    $('#action-log').prepend(`<p class='list-group-item' >${getISODateTime()} -- ${message}</p>`);
}

function getISODateTime(){
    let dt = new Date();

    let year = dt.getFullYear();
    let month = dt.getMonth()+1;
    let day = dt.getDate();

    let date = `${year}-${month < 10 ? '0'+month : month}-${day < 10 ? '0'+day : day}`;

    let hours = dt.getHours();
    let minutes = dt.getMinutes();
    let seconds = dt.getSeconds();

    let time = `${hours < 10 ? '0'+hours : hours }:${minutes < 10 ? '0'+minutes : minutes }:${seconds < 10 ? '0'+seconds : seconds }`;


    return `${date} ${time}`
}

function showConfigMenu(){
    $.ajax({
        url: "/configLine",
        success:function (data) {
            $('.modal-backdrop').show();
            $('#config-menu-modal').html(data).show()
        }
    })
}

function updateConfigMenu(field){
    let data;
    switch(field){
        case 'category':
            data = {category: $('#config-category').val()};
            break;
        case 'property':
            data = $('#interface-config-inputs').serialize();
            break;
        default:
            data = {};
            break
    }

    $.ajax({
        url: "/configLine",
        data: data,
        success:function (data) {
            $('#config-menu-modal').html(data)
        }
    })
}

function updateConfigValue(){
    $.ajax({
        url: "/configLine/update",
        data: $('#interface-config-inputs').serialize(),
        success: function(data){
            $('#config-menu-modal').html(data)
        }
    })
}

function hideConfigMenu(){
    $('.modal-backdrop').hide();
    $('#config-menu-modal').hide()
}