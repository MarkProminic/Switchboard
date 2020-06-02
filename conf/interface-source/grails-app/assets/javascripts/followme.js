/**
 * followme
 * Created by Luis Alcantara on 3/21/17.
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

function createFollowMeStepRow(step) {
    let stepElementList = "";

    for(let extension of step.extensions) {
        stepElementList += `<div class='col-md-3 ${step.ordinal === "1" ? "radio" : "checkbox" }'>
                                <label>
                                    <input id='${extension.id}' name='step-${step.id}-element' type='${step.ordinal === "1" ? "radio" : "checkbox" }' class='edit-option extension' ${(extension.active)? "checked": ""}>${extension.name}
                                </label>
                            </div>`
    }

    if(step.ordinal !== "1") {
        for (let group of step.groups) {
            stepElementList += `<div class='col-md-3 checkbox'>
                                <label>
                                    <input id='${group.id}' name='step-${step.id}-element' type='checkbox' class='edit-option rg' ${(group.active) ? "checked" : ""}>${group.name}
                                </label>
                            </div>`
        }
    }

    let deleteButtons = `<div class='hidden btn-group-sm deletion-controls row'>
                            <h5 class='col-md-8'>Are you sure?</h5>
                            <a class='col-md-2 btn btn-primary followme-step-delete' onclick='deleteStep(${step.id})'> Yes</a>
                            <a class='col-md-2 btn btn-danger followme-step-edit' onclick='hideStepDeletionControls(${step.id})'> No</a>
                        </div>`;

    return `<li id='followme-step-${step.id}' class='list-group-item row'>
                <div class='col-md-9 row '>
                    <h5 class='col-md-3 followme-step-ordinal'>${step.ordinal}</h5>
                    <h5 class='col-md-9 followme-step-number step-info'>${step.name}</h5>
                    <div class='col-md-9 hidden step-field'>
                        <div class='row followme-step-element-list'>${stepElementList}</div>
                    </div>
                </div>
                <div class='col-md-3'>
                    <div class='hidden btn-group-sm edit-controls pull-right'>
                        <a class='btn btn-success followme-step-check' onclick='updateStep(${step.id})'><i class='fa fa-check' ></i></a>
                        <a class='btn btn-danger followme-step-check' onclick='hideStepFields(${step.id})'><i class='fa fa-ban' ></i></a>
                    </div>
                    <div class='btn-group-sm management-controls pull-right'>
                        <a class='btn btn-warning followme-step-edit' onclick='showStepFields(${step.id})'><i class='fa fa-edit fa-fw'></i></a>
                        ${step.ordinal !== "1" ? "<a class='btn btn-danger followme-step-delete' onclick='showStepDeletionControls(${step.id})'><i class='fas fa-times fa-fw'></i></a>" : ""}
                    </div>
                    ${step.ordinal !== "1" ? deleteButtons : ""}
                </div>
            </li>`
}

function createNewStepOptionList(options){
    let newStepOptionList = "";

    if(options) {
        if (options.extensions) {
            for (let extension of options.extensions) {
                newStepOptionList += createNewStepExtensionOptionRow(extension)
            }
        }
        if (options.groups) {
            for (let group of options.groups) {
                newStepOptionList += createNewStepRingGroupOptionRow(group)
            }
        }
    }
    
    return newStepOptionList
}

function createNewStepExtensionOptionRow(extension){
    return `<div class='extension col-md-4 checkbox'>
                <label>
                    <input id='followme-option-${extension.id}' type='checkbox' class='extension' >${extension.name}
                </label>
            </div>`
}

function createNewStepRingGroupOptionRow(group){
    return `<div class='group col-md-4 checkbox'>
                <label>
                    <input id='followme-option-${group.id}' type='checkbox' class='group' >${group.name}
                </label>
            </div>`
}

function addFollowMeStep(){
    let elements = {extensions: [], ringGroups: []};

    let followMeMessage = $('#user-followme-message');
    let form = $('#new-step-form');

    form.find('.extension:checked').each(function () {
        let extId = this.id.substring(this.id.lastIndexOf("-") + 1, this.id.length);
        elements.extensions.push(extId)
    });

    form.find('.group:checked').each(function () {
        let groupId = this.id.substring(this.id.lastIndexOf("-") + 1, this.id.length);
        elements.ringGroups.push(groupId)
    });

    if((elements.ringGroups.length + elements.extensions.length) < 1){
        followMeMessage.html("Follow Me steps must contain at least 1 element.").css({color: "red"})
    }
    else {
        $.ajax({
            url: "followme/create",
            data: elements,
            success: function (step) {
                // let group = JSON.parse(data).group
                $('#followme-list').append(createFollowMeStepRow(step));
                $('#new-step-form').slideUp();
                resetStepFields();
                followMeMessage.html("Follow Me step successfully created.").css({color: "black"})
            }
        })
    }
}

function updateStep(id){
    let extensions = [];
    let ringGroups = [];

    let step = $('#followme-step-' + id);

    let list = $('#followme-list').get(0);
    let firstStep = list.children[0];
    let stepDOM = step.get(0);

    let stepId = stepDOM.id.substring(stepDOM.id.indexOf("step-") + 5);
    let firstStepId = firstStep.id.substring(firstStep.id.indexOf("step-") + 5);

    step.find(".edit-option.extension:checked").each(function () {
        extensions.push(this.id)
    });

    step.find(".edit-option.rg:checked").each(function () {
        ringGroups.push(this.id)
    });

    if(extensions.length + ringGroups.length > 1 && stepId === firstStepId){
        $('#user-followme-message').html("Step 1 cannot have more than one element.-").css({"color": red});
    }
    else if(extensions.length + ringGroups.length < 1){
        $('#user-followme-message').html("FollowMe steps must ring at least one Extension or Ring Group.").css({"color": red});
    }
    else {
        $.ajax({
            url: "followme/updateStep",
            data: {"id": id, "extensions": extensions, "ringGroups": ringGroups},
            success: function (updatedStep) {
                step.after(createFollowMeStepRow(updatedStep));
                step.remove();

                if (updatedStep.ordinal === 1) {
                    changeLocation(updatedStep.id)
                }

                $('#user-followme-message').html(`Step ${updatedStep.ordinal} successfully updated.`).css({"color": black});
            }
        })
    }
}

function updateFirstStep(step){
    let list = $('#followme-list').get(0);
    let firstStep = list.children[0];

    if(firstStep){
        let firstStepObj = $('#' + firstStep.id);
        firstStepObj.replaceWith(createFollowMeStepRow(step))
    }
    else {
        list.append(createFollowMeStepRow(step))
    }

}

function hideStepFields(id){
    let step = $('#followme-step-' + id);

    step.find(".step-field").hide();
    step.find('.edit-controls').hide();
    step.find('.step-info').fadeIn();
    step.find('.management-controls').fadeIn();

    resetStepFields(step)
}

function resetStepFields(step){
    let form = $('#new-step-form');

    form.find("input[type='checkbox']").each( function () {
        $(this).prop({checked: false})
    });

    let ordinal = form.find('#new-followme-ordinal').val();

    form.find('#new-followme-ordinal').val(Number(ordinal) + 1)
}

function showStepFields(id){
    let step = $('#followme-step-' + id);

    step.find(".step-info").hide();
    step.find('.management-controls').hide();
    step.find('.step-field').fadeIn();
    step.find('.edit-controls').fadeIn()
}

function showStepDeletionControls(id){
    let step = $('#followme-step-' + id);

    step.find('.management-controls').hide();
    step.find('.deletion-controls').slideDown()
}

function deleteStep(id){
    $.ajax({
        url: "/followme/delete",
        data: {"id": id},
        success: function () {
            let stepDOM = $('#followme-step-' + id);
            stepDOM.slideUp();
            setTimeout(function(){
                stepDOM.remove();
                let remainingSteps = $('#followme-list').children();

                remainingSteps.each(function(index){
                    $('#'+this.id).find(".followme-step-ordinal").html(index +1)
                });

                $('#new-followme-ordinal').val(remainingSteps.length +1)
            }, 250);

            $('#user-followme-message').html("Follow Me step successfully deleted.").css({"color": "black"})
        },
        error: function(){
            $('#user-followme-message').html("Error deleting follow me step.").css({"color": "red"})
        }
    })
}

function hideStepDeletionControls(id){
    let step = $('#followme-step-' + id);

    step.find('.deletion-controls').hide();
    step.find('.management-controls').fadeIn()
}

function toggleNewStepForm(){
    $('#user-followme-message').html('');
    $('#new-step-form').slideToggle()
}

function updateFollowMeSteps(group){
    $.ajax({
        url: "followme/updateSteps",
        success: function (data){
            let steps = data.steps;

            let followmeSteps = "";

            for(let step of steps){
                followmeSteps += createFollowMeStepRow(step)
            }

            $('#followme-list').html(followmeSteps);
            $('#user-followme-message').html("Removed the deleted Ring Group from any Follow Me steps that included it.")
        }
    })
}