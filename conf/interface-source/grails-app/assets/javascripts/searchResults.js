/**
 * searchResults
 * Created by joehe on 3/3/2016.
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

function expandHeader(index){
    $("li:not([class ~='active'])").slideUp();

    let indexFinder = "." + index;
    $(indexFinder).slideDown();
}

function injectPN(id){
    let phoneNumber = event.target.innerHTML;
    let hintItem = $('#extension-hint');

    phoneNumber = phoneNumber.substring(phoneNumber.lastIndexOf("- ")+1);

    if(phoneNumber.indexOf('ex:') > -1) {
        let extension = phoneNumber.substring(phoneNumber.indexOf(":") + 1);
        phoneNumber = phoneNumber.substring(0, phoneNumber.indexOf(" ex:"));

        hintItem.slideDown();
        hintItem.find("h3").html("Ext: <b>"+extension+"</b><br /><small>Click to dismiss</small>")
    } else {
        hintItem.slideUp()
    }

    $("#remote-phone-number").val(phoneNumber);
    $('#favorites-list, #search-results').find(".list-group-item.active").removeClass("active");
    $(event.target).addClass("active");
}

function search(event){
    event.preventDefault();
    let number = $('#search-contact').val();
    let callSearchButton = $("#call-search-btn");
    let searchMessage = $('#search-message');

    if(number.length > 2) {
        searchMessage.html("");

        $.ajax({
            data: {"contactId": number},
            url: '/contact/search',
            beforeSend: function () {

                callSearchButton.html("");
                callSearchButton.removeClass("btn btn-success btn-sm col-md-12");
                callSearchButton.addClass("fa fa-2x fa-spin fa-spinner col-md-offset-5");
            },
            success: function (data) {
                let json = JSON.parse(data);
                let results = json.data;

                $('#search-results').html("");

                for(let i = 0; i < results.length; i++){
                    displayResult(results[i])
                }

            },
            complete: function () {
                callSearchButton.removeClass("fa fa-2x fa-spin fa-spinner col-md-offset-5");
                callSearchButton.addClass("btn btn-success btn-sm col-md-12");
                callSearchButton.html("Search");
            }
        })
    } else {
        searchMessage.html("Query must be longer than 2 characters")
    }
}

function displayResult(contact){
    let numberList = "";
    let searchResults = $('#search-results');
    
    for(let phoneNumber of contact.numbers) {
        numberList += `<li class='list-group-item' onclick='injectPN()'>${phoneNumber.number}</li>`
    }

    searchResults.addClass("populated");
    searchResults.html(
        `<div id='contact-${contact.id}' class='list-group col-xs-12'>
            <div class='list-group-item-heading row' >
                <div class='col-xs-11'>
                    <span>${contact.name}</span>
                </div>
                <div class='col-xs-1'>
                    <a href='#' class='favorite' onclick='addFavorite("${contact.id}")'>
                        <i class='fa fa-star fa-2x text-muted' ></i>
                    </a>
                    <a href='#' class='unfavorite' onclick='removeFavorite("${contact.id}")'>
                        <i class='fa fa-star fa-2x text-warning' ></i>
                    </a>
                </div>
            </div>
            <ul class='list-group'>
              ${numberList}
            </ul>
        </div>`);
    if(contact.favorite) {
        $('#contact-'+contact.id).find(".favorite").hide()
    } else {
        $('#contact-'+contact.id).find(".unfavorite").hide()
    }
}

function clearResults(){
    $('#search-results').removeClass("populated").html('');
    $('.visuallyhidden').hide();

    $('#remotePhoneNumber').val('');
    $('#search-contact').val('')
}

function enterBtnEventHandler(){
    let target = $('.enter-target');
    if(target.attr("id") === "add-to-call-btn"){
        addToCall()
    }
    else {
        placeCall()
    }
}

function placeCall(){
    let phoneNumber = $('#remote-phone-number').val();
    let callerId = $('#callerID').val();
    let placeCallButton = $('#place-call-btn');

    if (phoneNumber !== "") {
        $.ajax({
            url: "/asteriskCall/placeCall",
            data: {"number": phoneNumber, "callerIdName": callerId},
            beforeSend: function () {
                $('#place-call-btn').hide();
                $('#pending-call-gadget').html(`<i id="pending-call-gadget" class="fa fa-2x fa-pulse fa-spinner" ></i>`)
            },
            success:function (data) {
                console.log("Call placed successfully!");
                showSuccess();

                clearResults()
            },
            error: function (data) {
                let message = data.responseText;
                console.log("ERROR: " + message);
                showFailure(message);

                clearResults()
            }
        })
    }
}

function addToCall(){
    let phoneNumber = $('#remote-phone-number').val();
    let addToCallButton = $('#add-to-call-btn');

    if (phoneNumber !== "") {
        $.ajax({
            url: "/conferenceRoom/addRemoteNumber",
            data: {"remoteNumber": phoneNumber.replace(/[a-zA-Z\-\s]/g, '')},
            beforeSend: function () {
                $('#add-to-call-btn').hide();
                $('#pending-call-gadget').html(`<i id="pending-call-gadget" class="fa fa-2x fa-pulse fa-spinner" ></i>`)
            },
            success: function (data) {
                console.log("Call placed successfully!");
                showSuccess();
                clearResults()
            },
            error: function (data) {
                console.log("ERROR: " + data);
                showFailure(`Add to call failed: ${data.statusText}`);
            }
        })
    }
}

function showSuccess() {
    $('#originate-message').html(
        `<div class="col-md-2" >
            <i class="fa fa-2x fa-check-circle text-success" ></i>
        </div>
        <h5 class="col-md-6" >Call placed successfully!</h5>
        <div class="col-md-4" onclick="resetOriginateMessage()" >
            <h6 class="text-primary" >Click to dismiss</h6>
        </div>`)
        .slideDown();

    $('#remote-phone-number').val("");

    $('#pending-call-gadget').html('');
    $('#add-to-call-btn').show()
}

function showFailure(message){
    $('#originate-message').html(
        `<div class="col-md-2" >
            <i class="fa fa-2x fa-times-circle text-danger" ></i>
        </div>
        <h5 class="col-md-6" >${message}</h5>
        <div class="col-md-4" onclick="resetOriginateMessage()" >
            <h6 class="text-primary" >Click to dismiss</h6>
        </div>`)
        .slideDown();

    $('#pending-call-gadget').html('');
    $('#place-call-btn').show()
}

function resetOriginateMessage(){
    $('#originate-message').slideUp()
}

function showContactsAsPending(){
    $('#contacts-pending-gadget').show()
}

function initializeTypeahead(){
    let contacts = new Bloodhound({
        datumTokenizer: Bloodhound.tokenizers.obj.whitespace('tokens'),
        queryTokenizer: Bloodhound.tokenizers.ngram,
        sufficient: 5,
        prefetch: {
            url: 'contact/contacts',
            cacheKey: 'contactSuggestions',
            ttl: 0
        },
        remote: {
            url: 'contact/query?val=%QUERY',
            wildcard: '%QUERY'
        },
        identify: function(datum){
            return datum.id
        }
    });

    $('#typeahead-menu').find('.typeahead').typeahead(
        {
            highlight: true,
            classNames: {
                menu: 'list-group tt-dropdown'
            }
        },
        {
            name: 'contacts',
            display: 'name',
            limit: 10,
            source: contacts,
            templates: {
                notFound: [
                    '<div>',
                        'No contacts found',
                    '</div>'
                ].join('\n'),
                pending: function(query){
                    return "<div id='pending-message' class='tt-suggestion'>" +
                            "<div class='list-group-item'>Fetching results for \'" + query.query +"\'</div>" +
                            "</div>"
                },
                suggestion: function(suggestion){
                    let custIdList = "";
                    let numberList = "";
                    let value = $('#search-contact').typeahead('val');

                    if(value[0].match(/[0-9+]/)) {
                        let numbers = suggestion.numbers;

                        numberList = "<div class='number-suggestion'>";

                        for(let number of numbers) {
                            numberList += "<div class='list-group-item'><h6>" + number.number + "</h6></div>"
                        }

                        numberList += "</div>"
                    }

                    else if(value.match(/^(A5|a5)/)) {
                        let custIds = suggestion.custIds;
                        custIdList = "<div class='custId-suggestion'>";

                        for(let custId of custIds) {
                            custIdList += "<div class='list-group-item'><h6>" + custId.id + "</h6></div>"
                        }
                        custIdList += "</div>"
                    }

                    return "<div id='{{id}}' class='tt-suggestion'>" +
                                    "<div class='list-group-item name-suggestion'>" + suggestion.name +"</div>" +
                                       custIdList + numberList +
                                "</div>";
                }
            }
        });

    // set the suggestions to refresh once an hour (or on refresh)
    setInterval(function () {
        showContactsAsPending();
        contacts.initialize(true)
    }, 3600*1000)
}