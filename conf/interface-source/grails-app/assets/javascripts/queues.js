/**
 * queues.js
 * Created by Luis Alcantara on 9/15/16.
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

function refreshQueueList(once){
    $.ajax({
        url: "/queue/refreshQueueList",
        success: function(queues) {
            // let json = JSON.parse(data);
            // let queues = json.queues;
            let displayedQueues = $("#queue-list");
            
            if(queues.length >= displayedQueues.length) {
                for(let queue of queues) {
                    let container = $('#queue-' + queue.name);
                    
                    if (container.size() > 0) {
                        let members = queue.members;
                        let memberList = container.find(".queue-member-list");

                        updateHeader(container, queue);
                        
                        if(members.length >= memberList.children().length) {
                            for(let member of members) {
                                let memberItem = memberList.find("#queue-" + queue.name + "-" + member.name.replace(/\W/g, "_"));

                                if (memberItem.length !== 0) {
                                    updateQueueMember(member, memberItem)
                                } else { // new member has joined
                                    addQueueMember(memberList, member, queue)
                                }
                            }
                        } else {
                            container.find(".queue-member-list").html("");  // member has left, rebuild list
                            for(let member of members) {
                                addQueueMember(memberList, member, queue)
                            }
                        }
                    }
                    else {
                        addQueue(queue)
                    }
                }
            } else {
                displayedQueues.html("");  // rebuild entire queue list
                
                for(let queue of queues) {
                    let container = $('#queue-' + queue.name);
                    let memberList = container.find(".queue-member-list");
                    
                    addQueue(queue);                 
                    
                    for(let member of queue.members) {
                        addQueueMember(memberList, member, queue)
                    }
                }
            }
        },
        complete: function(data) {
            if(data.statusText === 'error'){
                showErrorBanner()
            } else {
                if (!once) {
                    setTimeout(function () {
                        refreshQueueList()
                    }, 5000)
                }
            }
        },
        timeout: 2000
    })
}

function createQueueOptionRow(queue, user){
    return `<li id="available-queue-${queue.name}" ><a href='#' onclick='addToQueue(${user.id},"${queue.name}")'>${queue.name}</a></li>`;
}

function addQueue(queue){
    $("#queue-list").append(`
        <div class='queue' id='queue-${queue.name}' >
            <div class="queue-header" onclick="toggleQueueMembers('${queue.name}')" title="Click to show/hide the members of this Queue" >
                <h5 class="inline" >${queue.name}</h5><h6 class="inline" > (<span class="queue-size" >${queue.members.length}</span> members) </h6><i class="inline queue-header-chevron fa fa-chevron-down" ></i>
            </div>
            <ul class='hidden list-group queue-member-list'></ul>
            <ul class="list-group queue-active-calls" ></ul>
        </div>`);

    let memberList = $('#queue-' + queue.name).find(".queue-member-list");
    
    for(let member of queue.members) {
        addQueueMember(memberList, member, queue);
        let memberItem = memberList.find("#queue-" + queue.name + "-" + member.id);
        updateQueueMember(member, memberItem)
    }
}

function updateQueueMember(member, memberItem){
    if (member.paused !== 0) {  // set muted status of members
        memberItem.find(".pause").hide();
        memberItem.find(".play").show();
    } else {
        memberItem.find(".play").hide();
        memberItem.find(".pause").show()
    }
}

function addQueueMember(memberList, member, queue){
    let pauseHidden = member.paused !== 0 ? "hidden": "";
    let playHidden = member.paused !== 0 ? "" : "hidden";

    memberList.append(
        `<li id='queue-` + queue.name + `-` + member.name.replace(/\W/g, "_") + `' class='list-group-item queue-member'>
            <div class='row'>
                <div class='col-md-8'>
                    <h4>` + member.name + `</h4>
                </div>
                <div class='col-md-4'>
                    <div class='queue-member-controls btn-group-sm pull-right' >
                        <a class='btn btn-success ` + pauseHidden + ` pause' onclick='togglePauseAgent(` + member.id + `)'><i class='fa fa-pause'></i></a>
                        <a class='btn btn-success ` + playHidden + ` play' onclick='togglePauseAgent(` + member.id + `)'><i class='fa fa-play'></i></a>
                        <a class='btn btn-danger' onclick='removeAgent(` + member.id + `)'><i class='fas fa-times'></i></a>
                    </div>
                </div>
            </div>
        </li>`);

    let numMembersSpan = $('#queue-' + queue.name).find('.queue-size');

    let numMembers = Number(numMembersSpan.html());

    numMembersSpan.html(numMembers++)

}

function togglePauseAgent(id){
    $.ajax({
        url: '/queueMember/togglePauseMember',
        data: {memberId: id},
        success: function () {
            refreshQueueList(true)
        }
    })
}

function removeAgent(id){
    $.ajax({
        url: '/queueMember/removeAgent',
        data: {memberId: id},
        success: function (data) {
            refreshQueueList(true);
            $('#available-queue-list').append(createQueueOptionRow(data.queue, data.user))
        }
    })
}

function createQueueSettingsModal(){
    $.ajax({
        url: "/queue/refreshQueueList",
        success: function (queues) {
            let queueOptionList;
            let queueList;

            if(queues){
                for(let queue of queues){
                    queueOptionList += `<option class="failover-option" id="${queue.name}" >${queue.name}</option>`;
                    queueList += `<li id='queue-settings-${queue.name}' class='list-group-item row'>
                                        <div class='col-md-9 row' >
                                            <h5 class='col-md-3' >${queue.name}</h5>
                                        </div>
                                        <div class='col-md-3'>
                                            <div class='btn-group-sm management-controls pull-right' >
                                                <a class='btn btn-danger agent-ring-group-delete' onclick='showQueueDeletionControls("${queue.name}")'><i class='fas fa-times fa-fw'></i></a>
                                            </div>
                                            <div class='btn-group-sm deletion-controls row hidden' >
                                                <h5 class='col-md-8'>Are you sure?</h5>
                                                <a class='col-md-2 btn btn-primary agent-ring-group-delete' onclick='deleteQueue("${queue.name}")'> Yes</a>
                                                <a class='col-md-2 btn btn-danger agent-ring-group-edit' onclick='showQueueManagementControls("${queue.name}")'> No</a>
                                            </div>
                                        </div>
                                    </li>`;
                }
            } else {
                queueOptionList = `<option disabled >No Queues Exist</option>`;
                queueList = "";
            }

            $('body').prepend(
                `<div id='queue-settings-modal' class='modal' >
                    <div class='modal-dialog'>
                        <div class='modal-content'>
                            <div class='modal-header'>
                                <div class="row" >
                                    <h2 class='modal-title col-md-2' >Queues</h2>
                                    <div class="col-md-10" >
                                        <a class="pull-right btn btn-sm btn-success" onclick="toggleNewQueueRow()" ><i class="fa fa-plus" ></i> Add Queue</a>
                                    </div>
                                </div>  
                            </div>
                            <div class='modal-body form-horizontal'>
                                <div id='new-queue-row' class='row hidden' >
                                    <div class='col-md-9 row'>
                                        <div class='col-md-6'>
                                            <input type="text" class='form-control' id='new-queue-name' placeholder="Queue Name" /></label>
                                        </div>
                                        <div class='col-md-3'>
                                            <input id='new-queue-timeout' type='checkbox' />
                                            <label for='new-extension-is-sip'> Failover?</label>
                                        </div>
                                        <div class="col-md-3" >
                                            <select id="new-queue-failover-name" class="form-control" >
                                                ${queueOptionList}
                                            </select>
                                        </div>
                                    </div>
                                    <div class='col-md-3'>
                                        <div class='btn-group-sm'>
                                            <a class='btn btn-success' onclick='createQueue()'><i class='fa fa-check' ></i></a>
                                            <a class='btn btn-danger' onclick='toggleNewQueueRow()'><i class='fa fa-ban' ></i></a>
                                        </div>
                                    </div>
                                </div>
                                <ul id="queue-settings-list" >${queueList}</ul>
                            </div>
                            <div class='modal-footer'>
                                <button type='button' onclick='hideQueueSettingsModal()' class='btn btn-default'>Close</button>
                            </div>
                        </div>
                    </div>`);
        }
    });
}

function hideQueueSettingsModal(){
    $('#queue-settings-modal').hide();
    $('.modal-backdrop').hide()
}

function toggleNewQueueRow(){
    $('#new-queue-row').slideToggle();

    $('#new-queue-name').html("");
}

function deleteQueue(name){
    $.ajax({
        url: "/queue/delete",
        data: {"name": name},
        success: function () {
            $('#queue-settings-' + name).hide();

            $('#queue-settings-message').html("Successfully deleted '" + name + "'.")
        }
    });
}

function showQueueManagementControls(name){
    let queueItem = $('#queue-settings-' + name);

    queueItem.find('.deletion-contorls').hide();
    queueItem.find('.management-controls').fadeIn();
}

function showQueueDeletionControls(name){
    let queueItem = $('#queue-settings-' + name);

    queueItem.find('.management-controls').hide();
    queueItem.find('.deletion-contorls').fadeIn();

}

function createQueue(){
    let name = $('#new-queue-name').val();

    let failover = $('#');
    let failoverName = $('#new-queue-failover-name').val()
}

function toggleQueueMembers(name){
    let queue = $('#queue-' + name);

    queue.find('.queue-member-list').slideToggle();
    queue.find('.queue-header-chevron').toggleClass('fa-chevron-down').toggleClass('fa-chevron-up')
}

function updateHeader(container, queue){
    let header = container.find('.queue-header');

    header.html(`<h5 class="inline" >${queue.name}</h5>
                 <h6 class="inline" > (<span class="queue-size" >${queue.members.length}</span> members) </h6>
                 <i class="inline queue-header-chevron fa fa-chevron-down" ></i>`)
}