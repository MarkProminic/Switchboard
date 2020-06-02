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

class FollowMeConfig {

    String name
    String musiconhold = 'Prominic'
    String context = 'agents' // the context used for 'dialing' the FollowMeNumbers ex: agents w/ a number of 1112 'dials' 1112@agents
    String takecall = '1'
    String declinecall = '2'
    String callFromPrompt = 'followme/call-from'
    String norecordingPrompt = 'followme/no-recording'
    String optionsPrompt = 'followme/options'
    String holdPrompt = 'followme/pls-hold'
    String statusPrompt = 'followme/status'
    String sorryPrompt = 'followme/sorry'

    static mapping = {
        version false
        table name: 'followme'//, schema: 'interface'
    }

    static constraints = {
        takecall maxSize: 1
        declinecall maxSize: 1
        name nullable: false, unique: true
    }

}
