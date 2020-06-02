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

package dialer

class CallAs {
    String name
    String callerID
    String type

    static canadaCodes = ['403', '587', '780', '236', '250', '604', '778',
                          '204', '431', '506', '709', '902', '782', '226',
                          '249', '289', '343', '365', '416', '437', '519',
                          '613', '647', '705', '807', '905', '418', '438',
                          '450', '514', '579', '581', '819', '873', '306',
                          '639', '867']

    static constraints = {
        name unique: ['type', 'callerID']
        type inList: ['International', 'Domestic']
    }

    static mapping = {
        version false

//        table schema: 'interface'
    }

    static transients = ['canadaCodes']

    @Override
    public String toString() {
        this.callerID
    }

    public String getCallerName(){
        return callerID.substring(callerID.indexOf("\""), callerID.lastIndexOf("\""))
    }

    public String getCallerNumber(){
        return callerID.substring(callerID.indexOf("<"), callerID.lastIndexOf(" >"))
    }
}
