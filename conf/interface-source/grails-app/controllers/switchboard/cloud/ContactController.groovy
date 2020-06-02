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

package switchboard.cloud

import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonBuilder
import security.Agent

class ContactController {

    def ContactService

    def contacts(){
        def json = new JsonBuilder()
        Agent user = getAuthenticatedUser()

        Map<String, Contact> compactContacts = ContactService.list(3000)

        json compactContacts.values(), { Contact contact ->
            id contact.contactId
            name contact.fullName
            numbers contact.phoneNumbers, { String phoneNumber ->
                number phoneNumber
            }
            favorite (contact.contactId in user.favorites)
            custIds contact.custIds, { String custId ->
                id custId
            }

            tokens contact.tokens
        }

        render json
    }

    def query(String val){

        //replace any and all characters that are not letters, numbers or internal spaces
        String value = val.replaceAll("[^0-9,A-Z,a-z,\\s]", "").trim()


        String query = "SELECT contact.id, contact.contactid, contact.name_first, contact.name_last, " +
                "custid_list.custidaccess_list_string AS custId, phone_list.phone_list_string AS phoneNumber " +
                "FROM contacts contact " +
                "LEFT OUTER JOIN contacts_custidaccess_list custid_list ON contact.id=custid_list.contact_id " +
                "LEFT OUTER JOIN contacts_phone_list phone_list ON contact.id=phone_list.contact_id "

        def json = new JsonBuilder()

        Agent user = getAuthenticatedUser()

        if(value.startsWith('A5')){ // search by custid
            query += "WHERE custidaccess_list_string ILIKE '$value%'"
        }
        else if(value[0].matches(/[0-9+]/)){  // search by phone number
            String valueRegex = ""
            for(number in value){
                valueRegex += "$number.*"
            }
            query += "WHERE phone_list.phone_list_string ~ '<idc>$valueRegex</number>' " +
                    "OR phone_list.phone_list_string ~ '<areacode>$valueRegex</number>'" +
                    "OR phone_list.phone_list_string ~ '<number>$valueRegex</number>' "
        }
        else if(value[0].matches(/[A-Za-z]/)){ // search by name
            query += "WHERE "
            def valWords = value.split(/\s+/)
            if(valWords.size() > 1) {
                if (valWords.size() == 2) {
                    query += "name_first ILIKE '${valWords[0]}%' AND name_last ILIKE '${valWords[1]}%'"
                } else {
                    def tokens = ContactService.buildSearchTokens(valWords)
                    tokens.each { String[] nameTokens ->
                        query += "(name_first ILIKE '${nameTokens[0]}%' AND name_last ILIKE '${nameTokens[1]}%' ) OR "
                    }
                    query = query.substring(0, query.length() - 3)  // trim last ' OR ' from query
                }
            } else {
                query += "name_first ILIKE '$value%' OR name_last ILIKE '$value%'"
            }
        }
        else{
            log.error "Input for search query invalid: $value "
        }

        query += " AND phone_list_string IS NOT NULL ORDER BY name_last asc, name_first asc"

        Map<String, Contact> contactList = ContactService.getContactInfo(query)

//        log.info "Query '$value' resulted in ${contactList.size()} result(s)"

        json contactList.values(), { Contact contact ->
            id contact.contactId
            name contact.fullName
            numbers contact.phoneNumbers, { String phoneNumber ->
                number phoneNumber
            }
            favorite(contact.contactId in user.favorites)
            custIds contact.custIds, { String custId ->
                id custId
            }

            tokens contact.tokens

        }

        render json
    }

    @Secured("hasRole('ROLE_ADMIN')")
    def reconnect(){
        ContactService.resetFailureCount()

        log.info "Resetting DB connection failure count, connection will retry on next refresh."

        render text: "Successfully reset connection counter"
    }

}