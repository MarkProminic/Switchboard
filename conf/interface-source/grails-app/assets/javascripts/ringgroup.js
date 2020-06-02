/**
 * ringgroup
 * Created by Luis Alcantara on 3/24/17.
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

function createRingGroupRow(group) {
    let groupExtensionList = "";
    let groupEditExtensionList = "";

    for(let extension of group.extensions) {
        if(extension.active) {
            groupExtensionList += createRingGroupExtensionRow(extension, group.id)
        }

        groupEditExtensionList += createRingGroupExtensionOptionRow(extension)
    }

    return `<li id='user-ring-group-${group.id}' class='list-group-item row'>
                <div class='col-md-9 row' onclick='toggleRingGroupExtensions(${group.id})'>
                    <h5 class='col-md-3 user-ring-group-name info-field'>${group.name}</h5>
                    <i class='fa fa-caret-down pull-left ring-group-caret info-field' ></i>
                    <div class='col-md-3'>
                        <input type='text' class='form-control user-ring-group-name edit-field hidden' value='${group.name}' />
                    </div>
                    <div class='col-md-9 group-edit-extension-list edit-field hidden'>
                        ${groupEditExtensionList}
                    </div>
                </div>
                <div class='col-md-3'>
                    <div class='btn-group-sm edit-controls pull-right hidden' >
                        <a class='btn btn-success user-ring-group-check' onclick='updateRingGroup("${group.id}")'><i class='fa fa-check' ></i></a>
                        <a class='btn btn-danger user-ring-group-check' onclick='toggleRingGroupFields("${group.id}")'><i class='fa fa-ban' ></i></a>
                    </div>
                    <div class='btn-group-sm management-controls pull-right' >
                        <a class='btn btn-warning user-ring-group-edit' onclick='toggleRingGroupFields("${group.id}")'><i class='fa fa-edit fa-fw'></i></a>
                        <a class='btn btn-danger user-ring-group-delete' onclick='showRingGroupDeletionControls("${group.id}")'><i class='fas fa-times fa-fw'></i></a>
                    </div>
                    <div class='btn-group-sm deletion-controls row hidden' >
                        <h5 class='col-md-8'>Are you sure?</h5>
                        <a class='col-md-2 btn btn-primary user-ring-group-delete' onclick='deleteRingGroup("${group.id}")'> Yes</a>
                        <a class='col-md-2 btn btn-danger user-ring-group-edit' onclick='showRingGroupManagementControls("${group.id}")'> No</a>
                    </div>
                </div>
            </li>
            <ul id='ring-group-extension-list-${group.id}' class='list-group hidden'>
             ${groupExtensionList}
            </ul>`
}

function createRingGroupExtensionRow(extension, groupId){
    return `<li id='user-ring-group-${groupId}-extension-${extension.id}' class='list-group-item row'>
                <div class='col-md-9'>
                    <h5 class='user-ring-group-extension-name'>${extension.name}</h5>
                </div>
            </li>`
}

function createRingGroupExtensionOptionRow(extension){
    return `<div class='col-md-3 checkbox'>
                <label>
                    <input id='${extension.id}' type='checkbox' class='edit-option extension' ${(extension.active)? "checked": ""}>${extension.name}
                </label>
            </div>`
}

function createRingGroupAddExtensionRow(extension) {
    return `<div id='user-ring-group-new-extension-${extension.id}' class='col-md-4 checkbox'>
                <label>
                    <input id='${extension.id}' type='checkbox' class='new-ring-group-extension'>${extension.displayName}
                </label>
            </div>`
}

function addRingGroup(){
    let name = $("#new-ring-group-name").val();
    let ringGroupMessage = $('#user-ring-group-message');
    let extensions = [];

    $(".new-ring-group-extension:checked").each(function () {
        extensions.push(this.id)
    });

    if(extensions.length < 2){
        ringGroupMessage.html("Ring groups must contain at least two extensions.").css({color: "red"})
    }
    else if(!name){
        ringGroupMessage.html("Ring groups must have a name.").css({color: "red"})
    }
    else {
        $.ajax({
            url: "ringGroup/create",
            data: {"name": name, "extensions": extensions},
            success: function (group) {
                // let group = JSON.parse(data).group
                $('#ring-group-list').prepend(createRingGroupRow(group));
                $('#new-ring-group-form').slideUp();

                resetRingGroupForm();

                ringGroupMessage.html("Ring group '" + name + "' successfully created.").css({color: "black"});

                addRingGroupToNewStepForm(group)
            }
        })
    }
}

function resetRingGroupForm(){
    let form = $('#new-ring-group-form');

    form.find("#new-ring-group-name").val("");
    form.find("input[type='checkbox']").each( function () {
        $(this).prop({checked: false})
    })
}

function showRingGroupExtensionDeletionControls(extension, group){
    let container = $('#user-ring-group-' + group + '-extension-' + extension);

    container.find('.management-controls').hide();
    container.find('.deletion-controls').slideDown();
}

function showRingGroupExtensionManagementControls(extension, group){
    let container = $('#user-ring-group-' + group + '-extension-' + extension);

    container.find('.deletion-controls').hide();
    container.find('.management-controls').show()
}

function showRingGroupDeletionControls(group){
    let ringGroup = $('#user-ring-group-' + group);

    ringGroup.find(".management-controls").hide();
    ringGroup.find(".deletion-controls").slideDown()
}

function showRingGroupManagementControls(group){
    let ringGroup = $('#user-ring-group-' + group);

    ringGroup.find(".deletion-controls").hide();
    ringGroup.find(".management-controls").show()
}

function deleteRingGroup(group){
    $.ajax({
        url: "ringGroup/delete",
        data: {"id": group},
        success: function () {
            $('#user-ring-group-' + group).remove();
            $('#ring-group-extension-list-' + group).remove();

            removeRingGroupFromNewStepForm(group);
            updateFollowMeSteps(group);

            $('#user-ring-group-message').html("Successfully deleted Ring Group.")
        },
        error: function () {
            $('#user-ring-group-message').html("Cannot delete Ring Group.")
        }
    })
}

function removeExtensionFromRingGroup(extension, group){
    $.ajax({
        url: "ringGroup/removeExtension",
        data: {"group": group, "extension": extension},
        success: function () {
            $('#user-ring-group-' + group + "-extension-" + extension).remove();
            let remainingExtensions = $('#ring-group-extension-list-' + group).children().length;
            if(remainingExtensions < 2){
                $('#user-ring-group-message').html("")
            }
        }
    })
}

function toggleNewRingGroupForm() {
    $('#new-ring-group-form').slideToggle()
}

function addRingGroupToNewStepForm(group){
    $('#new-step-element-list').append(createNewStepRingGroupOptionRow(group))
}

function removeRingGroupFromNewStepForm(group){
    $('#new-step-element-list').find("#followme-option-" + group + ".group").parent().parent().remove()
}

function showRingGroupFields(id){
    let group = $('#user-ring-group-' + id);

    group.find(".management-controls").hide();
    group.find(".edit-controls").show();

    group.find(".info-field").hide();
    group.find(".edit-field").fadeIn();
}

function hideRingGroupFields(id){
    let group = $('#user-ring-group-' + id);

    group.find(".edit-controls").hide();
    group.find(".management-controls").show();

    group.find(".edit-field").hide();
    group.find(".info-field").fadeIn()
}

function updateRingGroup(id){
    let extensions = [];

    let group = $('#user-ring-group' + id);

    group.find(".edit-option:checked").each(function () {
        extensions.push(this.id)
    });

    let name = group.find("#user-ring-group-name").val();

    if(extensions.length > 0) {
        $.ajax({
            url: "ringGroup/update",
            data: {"id": id, "extensions": extensions, "name": name},
            success: function (updatedGroup) {
                group.after(createRingGroupRow(updatedGroup));
                group.remove();

                $('#user-ring-group-message').html(`Group ${updatedGroup.ordinal} successfully updated.`)
            }
        })
    }
    else {
        $('#user-ring-group-message').html('Ring groups must contain at least one Extension').css({"color": "red"})
    }
}