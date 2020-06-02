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


import security.Agent

class Extension implements RingElement{

    String name //Name of the Extension like Mobile, Home Office, etc
    String number //Extension number, 100 - 999
    String type = 'Local' //Distinguish between Local and Remote
    boolean primaryExt = false

    static belongsTo = [agent: Agent]

    static constraints = {
        type inList: ['Local', 'Remote']
    }

    static mapping = {
        version false
        table name: 'extension'//, schema: 'interface'
        sort 'name'
    }

    String toString(){
        return number
    }

    String getMemberInterface(){
        return "Local/$number@agents"
    }

    String getPhoneNumber(){
        if(type == 'Local'){
            return number
        }
        else{
            RemotePeer peer = RemotePeer.findByExten(number)
            if(!peer){
                return null
            }
            else {
                return peer.phoneNumber
            }
        }
    }

    String getStateInterface(){
        if(type == 'Local') {
            return "SIP/$number"
        }
        else{
            return null
        }
    }

    String getPrefix(){
        if(number?.contains(agent.id as String)) {
            return number.substring(0, number.indexOf(agent.id as String))
        }
        else return null
    }

    String getDisplayName(){
        if(type == 'Local'){
            return name
        } else{
            return "$name ($phoneNumber)"
        }
    }
}
