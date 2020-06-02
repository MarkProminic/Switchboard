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

function refreshUserLocationList(){
    $.ajax({
        url: "/user/userExtensions",
        success: function(data){
            $('#user-location-list').replaceWith(data)
        }
    })
}

function showUserSettingsModal(){
    $('.modal-backdrop').show();
    $('#user-settings-modal').show()
}

function hideUserSettingsModal() {
    $('.modal-backdrop').hide();
    $('#user-settings-modal').hide();

    resetUserSettingsModal()
}

function addUserExtension() {
    let newExtensionRow = $('#new-extension-row');
    let prefix = newExtensionRow.find('#new-extension-name').val();
    let extensionType = newExtensionRow.find('#new-extension-is-sip').prop("checked") ? "Local" : "Remote";
    let remoteNumber = newExtensionRow.find('#new-extension-remote-number').val();

    $.ajax({
        url: "/user/addExtension",
        data: {"prefix": prefix, "type": extensionType, "remoteNumber": remoteNumber},
        success: function (result) {
            hideNewExtensionForm(false);

            newExtensionRow.find('#new-extension-name').val("");
            newExtensionRow.find('#new-extension-number').val("");
            newExtensionRow.find('#new-extension-remote-number').val("");

            let message = `Extension '${result.phoneNumber ? result.displayName : result.number}' created.`;
            showModalMessage('user-extension', message);

            refreshUserLocationList();
            addExtensionToAgentSettings(result);
            addExtensionToNewRingGroupRow(result)
        }
    })
}

function addExtensionToAgentSettings(result) {
    let extensionRow = createExtensionRow(result);

    $('#new-extension-row').after(extensionRow)
}

function addExtensionToNewRingGroupRow(result){
    let rgExtRow = createRingGroupAddExtensionRow(result);
    $('#new-ring-group-extension-list').append(rgExtRow)
}

function createExtensionRow(extension) {
    return `<li id='agent-extension-${extension.id}' class='list-group-item row'>
                <div class='col-md-9 row'>
                    <h5 class='col-md-3 agent-extension-name'>${extension.name}</h5>
                    <h5 class='col-md-3 agent-extension-number'>${extension.phoneNumber}</h5>
                    <div class='col-md-6'>
                        <input type='text' class='form-control agent-extension-name hidden' initial='${extension.name}' value='${extension.name}' />
                    </div>
                    <div class='col-md-6'>
                        <input type='text' class='form-control hidden agent-extension-number' initial='${extension.number}' value='${extension.number}' />
                    </div>
                </div>
                <div class='col-md-3'>
                    <div class='btn-group-sm edit-controls pull-right'>
                        <a class='btn btn-success agent-extension-check hidden' onclick='updateExtension(${extension.id})'><i class='fa fa-check' ></i></a>
                        <a class='btn btn-danger agent-extension-check hidden' onclick='hideExtensionFields(${extension.id}, true)'><i class='fa fa-ban' ></i></a>
                    </div>
                    <div class='btn-group-sm management-controls pull-right'>
                        <a class='btn btn-warning agent-extension-edit' onclick='showExtensionFields(${extension.id})'><i class='fa fa-edit fa-fw'></i></a>
                        <a class='btn btn-danger agent-extension-delete' onclick='showDeletionControls(${extension.id})'><i class='fas fa-times fa-fw'></i></a>
                    </div>
                    <div class='hidden btn-group-sm deletion-controls row'>
                        <h5 class='col-md-8'>Are you sure?</h5>
                        <a class='col-md-2 btn btn-primary agent-extension-delete' onclick='deleteExtension(${extension.id})'> Yes</a>
                        <a class='col-md-2 btn btn-danger agent-extension-edit' onclick='showManagementControls(${extension.id})'> No</a>
                    </div>
                </div>
            </li>`
}

function showDeletionControls(extension) {
    let container = $('#agent-extension-' + extension);

    container.find('.management-controls').hide();
    container.find('.deletion-controls').slideDown()
}

function showManagementControls(extension) {
    let container = $('#agent-extension-' + extension);

    container.find('.deletion-controls').hide();
    container.find('.management-controls').show()
}

function deleteExtension(extension) {
    $.ajax({
        url: "/user/deleteExtension",
        data: {'extensionId': extension},
        success: function (result) {
            let message = `Extension '${result.phoneNumber ? result.displayName : result.number}' deleted.`;

            $('#agent-extension-' + extension).remove();
            $('#agent-ring-group-new-extension-' + extension).remove();
            showModalMessage('user-extension', message);

            refreshUserLocationList()
        },
        error: function() {
            showModalMessage('user-extension', "Extension deletion failed.")
        }
    })
}

function showNewExtensionForm() {
    $('#new-extension-row').slideDown()
}

function hideNewExtensionForm(slide) {
    let newExtensionRow = $('#new-extension-row');

    if(slide) {
        newExtensionRow.slideUp()
    }
    else {
        newExtensionRow.hide()
    }
}

function showExtensionFields(extension) {
    let container = $('#agent-extension-' + extension);

    container.find("h5.agent-extension-name").hide();
    container.find("h5.agent-extension-number").hide();
    container.find(".management-controls").hide();

    container.find("input.agent-extension-name").show();
    container.find(".edit-controls").show();
    container.find("input.agent-extension-number").show();
    container.find(".agent-extension-check").show()
}

function updateExtension(extension) {
    let container = $('#agent-extension-' + extension);
    let name = container.find("input.agent-extension-name").val();
    let number = container.find("input.agent-extension-number").val();
    $.ajax({
        url: "/user/updateExtension",
        data: {"id": extension, "name": name, "number": number},
        success: function (data) {
            let result = JSON.parse(data).result;

            setExtensionFields(extension, result);
            hideExtensionFields(extension, false);
            $('#user-extension-message').html("Extension '" + result.extname + "' has been updated.")
        }
    })
}

function setExtensionFields(extension, result) {
    let container = $('#agent-extension-' + extension);

    container.find("input.agent-extension-name").val(result.extname).attr("initial", result.name);
    container.find("input.agent-extension-number").val(result.extnumber).attr("initial", result.number);

    container.find("h5.agent-extension-name").html(result.extname);
    container.find("h5.agent-extension-number").html(result.extnumber);

    if(result.status) {
        container.find("#user-extension-message").html("<i class='fa fa-success fa-check'></i>" + result.message)
    } else {
        container.find("#user-extension-message").html("<i class='fa fa-danger fa-close'></i>" + result.message)
    }

}

function hideExtensionFields(extension, reset) {
    let container = $('#agent-extension-' + extension);

    container.find("input.agent-extension-name").hide();
    container.find(".edit-controls").hide();
    container.find(".agent-extension-number").hide();
    container.find(".agent-extension-btn").hide();

    container.find("h5.agent-extension-name").show();
    container.find("h5.agent-extension-number").show();
    container.find(".management-controls").show();

    if(reset) {
        let initialName = container.find("input.agent-extension-name").attr("initial");
        let initialNumber = container.find("input.agent-extension-number").attr("initial");

        container.find("input.agent-extension-name").val(initialName);
        container.find("input.agent-extension-number").val(initialNumber)
    }
}

function toggleRemoteNumberField(){
    $('#new-extension-remote-number').fadeToggle()
}

function resetUserSettingsModal() {
    $('#new-extension-row').hide();
    $('.deletion-controls').hide();
    $('.edit-controls').hide();
    $('#user-extension-message').html("");

    $('input.agent-extension-name').each( function (){
        $(this).val($(this).attr("initial"));
        $(this).hide()
    });
    $('input.agent-extension-number').each( function () {
        $(this).val($(this).attr("initial"));
        $(this).hide()
    });

    $('h5.agent-extension-name').show();
    $('h5.agent-extension-number').show();
    $('.management-controls').show()
}

function changePassword() {
    let userNewPassword = $('#user-new-password');
    let userConfirmPassword = $('#user-confirm-password');
    let newPassword = userNewPassword.val();
    let confirmPassword = userConfirmPassword.val();
    let message = "Note: Resetting your password will log you out of the interface. <b>Passwords must contain at least one capital letter, at least one number, and be 8 characters or longer</b>";

    if(!(newPassword === confirmPassword && confirmPassword !== '')) {
        if(newPassword.length < 8) {
            message = "Password must be at least 8 characters in length"
        } else if(!(newPassword.match(/.*[A-Z].*/) && newPassword.match(/.*[0-9].*/))) {
            message = "Passwords must contain at least one capital letter, at least one number"
        }
        else {
            message = "Passwords do not match. Please retype"
        }

        $('#user-security-message').html(message);
        userNewPassword.addClass("invalid-field").val("");
        userConfirmPassword.addClass("invalid-field").val("")
    }
    else{
        $.ajax({
            url: "/agent/changePassword",
            data: {"password": newPassword},
            success: function () {
                $('#user-new-password').removeClass("invalid-field").val("");
                $('#user-confirm-password').removeClass("invalid-field").val("");
                logoutUser()
            },
            error: function (data) {
                $('#user-security-message').html(data.responseText)
            }
        })
    }
}

function checkSipAllowed() {
    let nameValue = $('#new-extension-name').val();
    let sipAllowed = $("option[value='" + nameValue + "']").attr("sip-allowed");
    let newExtensionSIP = $("#new-extension-is-sip");

    if(sipAllowed === "true"){
        newExtensionSIP.prop("checked", true);

        $('#new-extension-remote-number').fadeOut().val("");

        newExtensionSIP.parent().fadeIn()
    }
    else{
        newExtensionSIP.prop("checked", false);

        $('#new-extension-remote-number').val("").fadeIn();

        newExtensionSIP.parent().fadeOut()
    }
}