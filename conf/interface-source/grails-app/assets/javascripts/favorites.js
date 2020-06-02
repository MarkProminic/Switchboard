/**
 * favorites
 * Created by Luis Alcantara on 9/20/16.
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

function addFavorite(contact) {
    $.ajax({
        url: "/user/addFavorite",
        data: {"contactID": contact},
        success: function (data) {
            let json = JSON.parse(data);
            let contactList = json.data;
            addFavoriteItem(contactList[0])
        }
    });

    let contactItem = $('#contact-'+contact);

    contactItem.find(".favorite").hide();
    contactItem.find(".unfavorite").show();

}

function addFavoriteItem(contact){
    let phoneNumberList = "";

    for(let phoneNumber of contact.phoneNumbers) {
        phoneNumberList += `<li class='list-group-item phone-list-item hidden' title="Click to fill this phone number into the 'Call' box above" onclick='injectPN()'>${phoneNumber.number}</li>`
    }

    $('#favorites-list').append(`<li id='contact-${contact.id}' class='favorite list-group-item' onclick='togglePhoneList("${contact.id}")' title="Click to show associated phone numbers" >
                                    <div class='row'>
                                        <div class='col-md-9' >
                                            <h5>${contact.name}</h5>
                                        </div>
                                        <div class='col-md-3'>
                                            <div class='pull-right favorite-controls hidden'>
                                                <a href='#' title="Remove from favorites list" class='btn btn-sm btn-danger ' onclick='removeFavorite("${contact.id}")'><i class='fa fa-minus' ></i></a>
                                            </div>
                                        </div>
                                    </div>
                                    <div class='row'>
                                        <ul class='list-group'>
                                           ${phoneNumberList}
                                        </ul>
                                    </div>
                                </li>`)
}

function getFavoritesList(){
    $.ajax({
        url: "/user/getFavorites",
        success: function (data) {
            let json = JSON.parse(data);
            let favorites = json.data.favorites;

            for(let favorite of favorites) {
                addFavoriteItem(favorite)
            }
        }
    })
}

function removeFavorite(contact){
    $.ajax({
        url: "/user/removeFavorite",
        data: {"contactID": contact},
        success: function () {
            let contactItem = $('#contact-' + contact);

            contactItem.find(".unfavorite").hide();
            contactItem.find(".favorite").show();
            $('#favorites-list').html("");

            getFavoritesList()
        }
    })
}

function togglePhoneList(contact){
    let favorite = $('#favorites-list').find('#contact-'+contact);
    let favoritePhoneNumbers = favorite.find(".phone-list-item");

    favoritePhoneNumbers.slideToggle();
    favorite.find('.favorite-controls').fadeToggle()
}