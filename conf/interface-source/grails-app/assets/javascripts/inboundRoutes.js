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

function showNewInboundRouteForm() {
    getDestNameOptions("new");
    $('#new-route-row').slideDown()
}

function hideNewInboundRouteForm() {
    $('#new-route-row').slideUp()
}

function getDestNameOptions(id) {
    let destType = "NONE";
    let target;

    if(id === 'new') {
        target = $('#new-route-row');
        destType = $('.new-route-type').val();
    }
    else {
        target = $('#inbound-route-'+ id );
        destType = target.find(".inbound-route-type").val();
    }

    let options = "";

    $.ajax({
        url: "/inboundRoute/destinationOptions",
        data: {"type": destType, "routeId": id},
        success: function (html) {
            target.find('.inbound-route-dest-options').html(html)
        }
    });
}

function showDestNameOptions() {
    let options = getDestNameOptions();

    $("#new-route-dest-name").html(options)
}

function createNewInboundRoute() {
    let form = $('#new-route-row');
    let number = form.find('#new-route-did').val().replace(/[^0-9]/g, '');
    let type = form.find('#new-route-type').val();
    let destination = form.find('#new-route-dest-name').val();
    let description =  "";  //form.find('#inbound-route-description').val()

    if(((number.length === 10 && number[0] !== "1") || number.length === 11) && number.match(/[0-9]+/) ) {  // Checking for valid numbers that are either "1" + a nine digit number or just a nine digit number
        $('#inbound-route-message').html("");

        $.ajax({
            url: "/inboundRoute/create",
            data: {
                "number": number, "type": type,
                "destination": destination,
                "description": number + " -> " + type + ": " + destination
            },
            error: function (data) {
                let message = JSON.parse(data.responseText);

                $('#inbound-route-message').html(message)
            },
            success: function (data) {
                let route = JSON.parse(data).route;

                hideNewInboundRouteForm();
                $('input#new-route-did').val("");
                $('#inbound-route-message').html("Inbound Route successfully created.");

                $('#inbound-route-list').find('ul').append(
                    `<li id='inbound-route-` + route.id + `' class='list-group-item row'>
                        <h5 class='col-md-3'>` + route.routeNumber + `</h5>
                        <h5 class='col-md-3'>` + route.destName + `</h5>
                        <h5 class='col-md-6'>` + route.routeDesc + `</h5>
                        <div class='col-md-3 btn-group-sm'>
                            <a class='btn btn-warning' onclick='editInboundRoute(` + route.id + `)'><i class='fa fa-edit'></i> Edit</a>
                            <a class='btn btn-danger' onclick='deleteInboundRoute(` + route.id + `)'><i class='fa fa-ban'></i> Delete</a>
                        </div>
                    </li>`)
            }
        })
    } else {
        $('#inbound-route-message').html("Number entered is invalid. Please enter only numbers and enter the full phone number (9 or 10 digits).")
    }

}

function updateInboundRoute(id){
    let route = $('#inbound-route-' +id);

    let data = {id: id,
        "type": route.find('.inbound-route-type').val(),
        "destNameId": route.find('.inbound-route-destination-name').val(),
        "description": route.find('.inbound-route-description').val()
    };

    $.ajax({
        url: "/inboundRoute/update",
        data: data,
        success:function (html) {
            route.replaceWith(html)
        }
    })
}

function showInboundRouteFields(id){
    let route = $('#inbound-route-' + id);

    route.find('.inbound-route-info').hide();
    route.find('.inbound-route-edit-fields').fadeIn();
}

function hideInboundRouteFields(id){
    let route = $('#inbound-route-' + id);

    route.find('.inbound-route-edit-fields').hide();
    route.find('.inbound-route-info').fadeIn();
}

function showInboundRouteDeletionConfirmation(id){
    let route = $('#inbound-route-'+ id);

    route.find(".inbound-route-btns").hide();
    route.find(".inbound-route-delete-btns").slideDown()
}

function hideInboundRouteDeletionConfirmation(id){
    let route = $('#inbound-route-'+ id);

    route.find(".inbound-route-delete-btns").hide();
    route.find(".inbound-route-btns").fadeIn()
}

function deleteInboundRoute(id){
    $.ajax({
        url: "/inboundRoute/delete",
        data: {"id": id},
        success: function(){
            $('#inbound-route-' +id).remove();
            let message = $('#inbound-route-message');

            message.html("Inbound route successfully removed").show();
            setTimeout(function(){
                message.fadeOut()
            }, 5000)
        },
        error: function(data){

        }
    })
}