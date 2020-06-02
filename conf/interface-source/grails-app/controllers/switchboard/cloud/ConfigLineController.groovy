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

import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class ConfigLineController {

    def index(String category, String property) {
        Set<String> categories = ConfigLine.list(sort: "category").category
        List<String> properties = []
        String value = ""

        if(category){
            properties = ConfigLine.findAllByCategory(category).property

            if(properties.size() == 1){
                property = properties[0]
            }

            if(property){
                value = ConfigLine.findByCategoryAndProperty(category, property).value
            }
        }

        render template: "menu", model: [categories: categories, properties: properties, category: category, property: property, value: value]
    }

    @Transactional
    def update(String category, String property, String value){
        String message = ""
        ConfigLine line = ConfigLine.findByCategoryAndProperty(category, property)

        line.value = value
        line.save()

        if(line.hasErrors()){
            line.errors.each { Error error ->
                message += "$error.message\n"
            }
        }
        else{
            message = "Property '$property' in category '$category' successfully updated"
        }

        Set<String> categories = ConfigLine.list(sort: "category").category
        Set<String> properties = ConfigLine.findAllByCategory(category).property

        render template: "menu", model: [categories: categories, properties: properties, category: category, property: property, value: value, message: message]
    }
}
