/**
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

function refreshRoomList(){
    $.ajax({
        url: "/conferenceRoom/refreshRoomList",
        success: function(data) {
            // let json = JSON.parse(data);
            let rooms = data.rooms ? data.rooms : {length: 0};
            let displayedRooms = $(".conf-room");

            if(rooms.length >= displayedRooms.length) {
                for(let room of rooms){
                    let container = $('#conf-' + room.number);

                    if (container.length !== 0) {
                        container.find(".conf-duration").html(room.duration);
                        updateButtons(container, room);
                        
                        let members = room.members;
                        let memberList = container.find(".conf-member-list");

                        if(members.length >= memberList.children().length) {
                            for(let member of members){
                                let memberItem = memberList.find("#conf-" + room.number + "-" + member.id);

                                if (memberItem.length !== 0) {
                                    updateMember(member, memberItem)
                                } else { // new member has joined
                                    addMember(memberList, member, room)
                                }
                            }
                        } else {
                            container.find(".conf-member-list").html("");  // member has left, rebuild list

                            for(let member of members) {
                                addMember(memberList, member, room)
                            }
                        }
                    } else {
                        addConference(room)
                    }
                }
            }
            else {
                $('#conference-room-list').html("");  // rebuild entire conference list

                for(let room of rooms) {
                    let container = $('#conf-' + room.number);

                    addConference(room);

                    let members = room.members;
                    let memberList = container.find(".conf-member-list");

                    for(let member of members) {
                        addMember(memberList, member, room)
                    }
                }
            }
        },
        complete: function(data) {
            if(data.statusText == 'error') {
                showErrorBanner()
            } else {
                setTimeout(function () {
                    refreshRoomList()
                }, 1000)
            }
        },
        timeout: 5000
    })
}

function updateMember(member, memberItem){
    if (member.muted) {  // set muted status of members
        memberItem.find(".unmute").show();
        memberItem.find(".mute").hide()
    } else {
        memberItem.find(".unmute").hide();
        memberItem.find(".mute").show()
    }
}

function updateButtons(container, room){
    let buttonContainer = container.find(".room-buttons");
    let buttons = "";
    updateMemberButtons(container, room.userIsMember);
    
    if(room.userIsMember) {
        if (room.hidden) {
            buttons += "<a href='#' class='btn btn-sm btn-success' onclick='toggleRoomVisibility(\"" + room.number + "\")'><i class='fa fa-eye'></i> Make Public</a>"
        } else {
            buttons += "<a href='#' class='btn btn-sm btn-danger' onclick='toggleRoomVisibility(\"" + room.number + "\")'><i class='fa fa-eye-slash'></i> Make Private</a>"
        }
        buttons += "<a href='#' class='btn btn-sm btn-warning' onclick='kickAll(\"" + room.number + "\")'><i class='fas fa-times'></i> Kick All</a>";

        container.switchClass("panel-default", "panel-primary")

    } else {
        buttons = "<a href='#' class='btn btn-sm btn-success' onclick='joinRoom(\"" + room.number + "\")'><i class='fa fa-sign-in'></i> Join Room</a>";
        container.switchClass("panel-primary", "panel-default")
    }
    buttonContainer.html(buttons)
}

function updateMemberButtons(container, isMember){
    let muteControls = container.find(".mute-controls");
    let kickControls = container.find(".kick-control");
    
    if(isMember) {
        muteControls.show();
        kickControls.show()
    } else {
        muteControls.hide();
        kickControls.hide()
    }
}

function addConference(room){
    // check if room is hidden to display proper privacy button
    let buttons = "<div class='row'><div class='col-md-11 room-buttons'>";
    let roomTheme = "panel-default";

    if(room.userIsMember) {
        roomTheme = "panel-primary";
        
        if (room.hidden) {
            buttons += `<a href='#' class='btn btn-sm btn-success' onclick='toggleRoomVisibility("${room.number}")'><i class='fa fa-eye'></i> Make Public</a>`
        } else {
            buttons += `<a href='#' class='btn btn-sm btn-danger' onclick='toggleRoomVisibility("${room.number}")'><i class='fa fa-eye-slash'></i> Make Private</a>`
        }
        buttons += `<a href='#' class='btn btn-sm btn-warning' onclick='kickAll("${room.number}")'><i class='fas fa-times'></i> Kick All</a>`
    } else {
        buttons += `<a href='#' class='btn btn-sm btn-success' onclick='joinRoom("${room.number}")'><i class='fa fa-sign-in'></i> Join Room</a>`
    }

    buttons += "</div></div>";

    $("#conference-room-list").append(`<div id='conf-${room.number}' class='panel ${roomTheme} conf-room'>
                                        <div class='panel-heading'>
                                            <div id='conf-${room.number}-header' class='row conf-header' onclick='toggleConferenceRoom("${room.number}")' >
                                                <div class='col-md-1'>
                                                    <div class='chevron right-chevron'><i class='fa fa-2x fa-chevron-right' ></i></div>
                                                    <div class='chevron down-chevron hidden'><i class='fa fa-2x fa-chevron-down' ></i></div>
                                                </div>
                                                <div class='col-md-10'>
                                                    <h4>${room.name}</h4>
                                                </div>
                                            </div>
                                            ${buttons}
                                        </div>
                                        <div class='panel-body hidden'>
                                            <span class='conf-" + room.number + "-info'>
                                                <h5>Conf. Extension: ${room.number}</h5>
                                            </span>
                                            <span>
                                                <h5>Duration: <span class='conf-duration'>${room.durationString}</span></h5>
                                            </span>
                                            <ul class='conf-member-list list-group'></ul>
                                        </div>
                                        </div>`)
}

function addMember(memberList, member, room){
    memberList.append( `<li id='conf-${room.number}-${member.id}' class='list-group-item row'>
                            <h4 class='list-group-title col-md-8' >${member.name}</h4>
                            <div class='mute-controls col-md-2'>
                                <i class='fa fa-microphone-slash mute' onclick='mute("${member.id}", "${room.number}")'></i>
                                <i class='fa fa-microphone unmute hidden' onclick='unmute("${member.id}", "${room.number}")'></i>
                            </div>
                            <div class='kick-control col-md-2'>
                                <i class='fas fa-times' onclick='kick("${member.id}", "${room.number}")'></i>
                            </div>
                        </li>`)
}

function mute(member, room){
    $.ajax({
        url: "/conferenceRoom/mute",
        data: {'member': member, 'room': room}
    })
}

function unmute(member, room){
    $.ajax({
        url: "/conferenceRoom/unmute",
        data: {'member': member, 'room': room}
    })
}

function kick(member, room){
    $.ajax({
        url: "/conferenceRoom/kick",
        data: {'member': member, 'room': room},
        success: function(){
//                $("#conf-" + room).find(".message").html(member + " removed from " + room)
        }
    })
}

function toggleRoomVisibility(roomNumber){
    $.ajax({
        url: "conferenceRoom/toggleVisibility",
        data: {roomNumber: roomNumber}
    })
}

function joinRoom(roomNumber){
    $.ajax({
        url: "conferenceRoom/join",
        data: {roomNumber: roomNumber}
    })
}

function kickAll(roomNumber){
    $.ajax({
        url: "conferenceRoom/kickAll",
        data: {roomNumber: roomNumber}
    })
}

function toggleConferenceRoom(number){
    let room = $('#conf-' + number);
    room.find('.right-chevron').toggle();
    room.find('.down-chevron').toggle();
    room.find('.panel-body').slideToggle()
}