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

class SIPPeer {

    String type = 'friend'
    String name
    String secret = 'password'
    String context = 'agents'
    String host = 'dynamic'
    String ipaddr
    String port
    String regseconds
    String defaultuser
    String fullcontact
    String regserver
    String useragent
    String lastms = '0'
    String insecure = 'port,invite'
    String callbackextension = ''
    String permit = '0.0.0.0/0.0.0.0'
    String deny = "0.0.0.0/0.0.0.0"
    String md5secret
    String remotesecret
    String dtmfmode = "rfc2833"
    String nat = 'no'
    String callgroup
    String pickupgroup
    String language
    String disallow
    String allow
    String trustrpid = 'no'
    String progressinband = 'never'
    String promiscredir = 'no'
    String useclientcode = 'no'
    String accountcode
    String setvar
    String callerid
    String amaflags
    String busylevel = 0
    String allowoverlap = 'yes'
    String allowsubscribe = 'yes'
    String videosupport = 'no'
    String maxcallbitrate
    String mailbox
    String session_expires = ''
    String t38pt_usertpsource
    String regexten
    String fromdomain
    String fromuser
    String qualify = 'yes'
    String defaultip
    String rtptimeout
    String rtpholdtimeout
    String sendrpid = 'no'
    String outboundproxy
    String contactpermit
    String contactdeny
    String usereqphone = 'no'
    String auth
    String fullname
    String trunkname
    String cid_number
    String callingpres = 'allowed_not_screened'
    String mohinterpret
    String mohsuggest
    String subscribemwi = 'yes'
    String vmexten = "asterisk"
    String rtpkeepalive
    String path
    String mask
    String MACAddress
    String directmedia = 'no'
    String description = ""

    static type_values = ['friend', 'agent', 'peer']
    static insecure_values = [ 'no', 'invite', 'port', 'port,invite']
    static sip_dtmfmode_values = ['rfc2833', 'info', 'shortinfo', 'inband', 'auto']
    static yes_no_values = ['yes', 'no']
    static sip_progressinband_values = ['yes', 'no', 'never']
    static sip_callingpres_values = ['allowed_not_screened', 'allowed_passed_screen', 'allowed_failed_screen',
                                     'allowed', 'prohib_not_screened', 'prohib_passed_screen', 'prohib_failed_screen', 'prohib', 'unavailable']

    static constraints = {
        name nullable: false, unique: true
        dtmfmode inList: this.sip_dtmfmode_values
        type inList: this.type_values
        insecure inList: this.insecure_values
        trustrpid inList: this.yes_no_values
        promiscredir inList: this.yes_no_values
        useclientcode inList: this.yes_no_values
        allowoverlap inList: this.yes_no_values
        allowsubscribe inList: this.yes_no_values
        videosupport inList: this.yes_no_values
        sendrpid inList: this.yes_no_values
        usereqphone inList: this.yes_no_values
        subscribemwi inList: this.yes_no_values
        progressinband inList: this.sip_progressinband_values
        callingpres inList: this.sip_callingpres_values
    }

    static mapping = {
        version false
        table name: "sippeers"//, schema: 'asteriskinfo'
        session_expires column: '`session-expires`'
    }

    def beforeUpdate(){
        setValues this
    }
    def beforeInsert(){
        setValues this
    }

    def setValues = { extension ->
        extension.mailbox = extension.mailbox ?: "${name.substring(name.length()-3)}@prominic"
        extension.defaultuser = extension.defaultuser ?: name
    }

    String toString(){
        return name
    }

    Agent getOwner(){
        Agent owner = null

        if(type == 'friend') {
            if (this.partnerExtension) {
                owner = this.partnerExtension.agent
            } else {
                log.warn "There exists no parallel Extension domain object for SIPPeer $name. " +
                        "This is likely due to incomplete creation or deletion of this pairing and will likely cause issues moving forward."
            }
        }

        owner
    }

    Extension getPartnerExtension(){
        Extension.findByNumber(name)
    }

    String getDisplayName(){
        String displayName
        if(type == 'friend'){
            if(this.partnerExtension){
                displayName = "${this.partnerExtension.name} ($name)"
            }
            else {
                log.error "SIP Peer $name has type '$type' but no Extension instance required for Switchboard management"

                displayName = name
            }
        }
        else {
            displayName = name
        }

        displayName
    }

    String getDisplayIPAddr(){
        String ipAddr
        if(host == 'dynamic'){
            ipAddr = ipaddr
        }
        else {
            ipAddr = host
        }

        ipAddr
    }

    String getStatus(){
        String status
        if(type == 'friend'){
            if(lastms == '0'){
                status = "Unregistered"
            }
            else {
                status = "OK ($lastms ms)"
            }
        }
        else {
            status = "N/A"
        }

        status
    }
}
