/**
 * voicemail
 * Created by Luis Alcantara on 11/16/16.
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

function refreshMailbox() {
    let listening = $('#vm-active').val();
    $.ajax({
        url: "voicemail/refreshMailbox",
        success: function (data) {
            if(!listening) {
                $('#voicemail-panel').html(data);
                $('#vm-message-count-info').tooltip()
            }
        },
        complete: function (data) {
            if (data.statusText === 'error') {
                showErrorBanner()
            } else {
                setTimeout(function () {
                    refreshMailbox()
                }, 30 * 1000)
            }
        },
        timeout: 1000
    })
}

function markAsListened(id) {
    $('#vm-active').val(true);
    $.ajax({
        url: "voicemail/markAsListened",
        data: {"id": id},
        success: function () {
            $('#vm-message-' + id).removeClass("new-message")
        }
    })
}

function createMessageRow(message){
    return `<li id='vm-message-${message.id}' class='message list-group-item ${message.isNew ? "new-message" : ""}'>
                <div class='row' >
                    <div class='col-md-3 vm-message-date-column'>
                        <p class='vm-message-date' >${message.createdOn}</p>
                    </div>
                    <div class='col-md-3'>
                        <h5 class='vm-message-callerid' title='${message.callerId}' >${message.callerName}</h5>
                    </div>
                    <div class='vm-controls col-md-5'>
                        <audio id='vm-player-${message.id}' preload='none' controls onplaying="markAsListened('${message.id}')" >
                            <source type='audio/wav' src='/voicemails/${message.id}' />
                        </audio>
                        <div class='vm-btn inline'>
                            <a id='delete-btn-${message.id}' class='btn btn-sm btn-danger pull-right' onclick='confirmDelete(${message.id})'>
                                <i class='fas fa-times' ></i>
                            </a>
                        </div>
                    </div>
                </div>
                <div id='delete-controls-${message.id}' class='row hidden' >
                    <small class="col-md-offset-2 col-md-4" >Are you sure?</small>
                    <div class='col-md-6 btn btn-group-xs'>
                        <a class='btn btn-danger' onclick='hideDeleteControls(${message.id})'>No</a>
                        <a class='btn btn-success' onclick='deleteMessage(${message.id})' >Yes</a>
                    </div>
                </div>
            </li>`
}

function deleteMessage(id) {
    $.ajax({
        url: "voicemail/delete",
        data: {"id": id},
        success: function () {
            $('#vm-message-' + id).remove();

            let messageCount = $('#vm-message-count');
            let count = Number(messageCount.html());

            messageCount.html(count--)

        }
    })
}

function hardDeleteMessage(id){
    $.ajax({
        url: "/voicemail/hardDelete",
        data: {"id": id},
        success: function(){
            // $('#vm-message-' + id).remove()
            alert("SUCCESS")
        },
        error: function () {
            alert("ERROR")
        }
    })
}

function confirmDelete(id) {
    $("#vm-audio").hide();
    $('#delete-controls-' + id).slideDown()
}

function hideDeleteControls(id) {
    $('#delete-controls-' + id).slideUp();
    $("#vm-audio").fadeIn(350)
}

function playVM(id){
    let player = $('#vm-player-' + id)[0];
    player.play();

    let div = $('#vm-message-' + id);

    div.find('.vm-play-btn').hide();
    div.find('.vm-pause-btn').show()
}

function pauseVM(id){
    $('#vm-active').val(false);
    let player = $('#vm-player-' + id)[0];
    player.pause();

    let div = $('#vm-message-' + id);

    div.find('.vm-pause-btn').hide();
    div.find('.vm-play-btn').show()

}