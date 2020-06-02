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
import org.apache.commons.lang.NotImplementedException
import switchboard.cloud.AsteriskCall
import switchboard.cloud.Channel

@Transactional
class ChannelService {

    Channel findUserChannel(AsteriskCall call, String userName) {
        Channel userChannel = call.channels.find { Channel c ->
            userName == c.ownerName && c.status != 'local'
        }

        if (!userChannel) {
            def outboundChannels = call.channels.findAll { Channel c ->
                c.direction == 'outbound'
            }

            int numberOfOutboundChannels = outboundChannels.size()
            if (numberOfOutboundChannels == 1) {
                userChannel = outboundChannels[0]
            } else {
                log.error "Tried to identify parker channel by direction but found '$numberOfOutboundChannels'"
            }
        }

        userChannel
    }

    Channel findNonUserChannel(AsteriskCall call, String userName) {
        Channel channel

        if(call.direction == 'internal') {
            channel = call.channels.find { Channel c -> c.ownerName && c.ownerName != userName }
        }
        else if(call.direction in ['inbound', 'outbound']){
            Collection<Channel> possibilities = call.channels.findAll { Channel c -> !c.ownerName }

            if(possibilities.size() > 0) {
                if (call.direction == 'inbound') {
                    int inboundChannels = possibilities.count { Channel c -> c.direction == 'inbound' }

                    if (inboundChannels == 1) {
                        channel = possibilities.find { Channel c -> c.direction == 'inbound' }
                    } else {
                        if (inboundChannels > 1) {
                            log.error "Call $call is an inbound call, but there are $inboundChannels unowned inbound channels, so the Channel data must be malformed"
                        } else {
                            log.error "Call $call is an inbound call, but there are no unowned inbound channels, so the Call direction data is incorrect"
                        }

                    }
                } else if (call.direction == 'outbound') {
                    int outboundChannels = possibilities.count { Channel c -> c.direction == 'outbound' }

                    if (outboundChannels == 1) {
                        channel = possibilities.find { Channel c -> c.direction == 'outbound' }
                    } else {
                        if (outboundChannels > 1) {
                            log.error "Call $call is an outbound call, but there are $outboundChannels unowned outbound channels, so the Channel data must be malformed"
                        } else {
                            log.error "Call $call is an outbound call, but there are no unowned outbound channels, so the Call direction data is incorrect"
                        }
                    }
                }
            }
            else {
                log. error "Call $call has no unowned calls and is NOT internal. This call is malformed and will not park or transfer correctly!"
            }
        }
        else {
            log.error "Logic not implemented for a call of direction $call.direction, call may be malformed"
            throw new NotImplementedException("NonUserChannel logic for calls with directions other than outbound, inbound and internal is unimplemented")
        }


        if(channel){
            log.debug "Nonuser Channel: $channel"
        }
        else {
            log.error "Couldn't identify the non-user channel for call $call"
        }

        channel
    }
}
