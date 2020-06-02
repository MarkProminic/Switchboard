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

package security

import dialer.CallAs
import groovy.transform.EqualsAndHashCode
import switchboard.cloud.*

@EqualsAndHashCode(includes='username')
class Agent implements Serializable {

	private static final long serialVersionUID = 1

	transient springSecurityService
	def callService
	def DialPlanService

	String name
	String username
	String password
	String state = 'offline'
	String callState = 'available'

	boolean enabled = true
	boolean accountExpired = false
	boolean accountLocked = false
	boolean passwordExpired = false
	boolean loggedIn = false

	static hasMany = [extensions: Extension, callerIds: CallAs, favorites: String, ringGroups: RingGroup, pendingCallRecords: CallRecord]

	static agent_states = ['available', 'busy', 'offline']

	static call_states = ['oncall', 'ringing', 'available', 'busy', 'offline']

	Set<Role> getAuthorities() {
		AgentRole.findAllByAgent(this)*.role
	}

	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
			log.info "Password is being updated for $name"
		}
	}

	protected void encodePassword() {
		password = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(password) : password
	}

	static transients = ['springSecurityService', 'logger', 'primaryExtension']

	static constraints = {
		password blank: false, password: true
		username blank: false, unique: true
		name unique: true, nullable: false

		callState inList: this.call_states
		state inList: this.agent_states
	}

	static mapping = {
		version false
		password column: '`password`'
		id generator: 'assigned'
		autowire true

		sort "name"

//		table schema: 'interface'
	}

	Extension getPrimaryExtension(){
		Extension primary = Extension.findByPrimaryExtAndAgent(true, this)
		if(!primary) {
			def extensions = Extension.findAllByAgent(this, [sort: "id", order: "asc"])
			if (extensions) {
				primary = extensions[0]
				primary.primaryExt = true

				primary.save()
			} else {
				primary = null
				log.warn " No extensions exist for Agent '$name'. This could cause errors regarding calling, queues, etc."
			}
		}

		primary
	}

	void setPrimaryExtension(Extension extension){
		extensions.each {
			it.primaryExt = (it.id == extension.id)

			it.save()
		}
	}

	List<RingElement> getRingElements(){
		def elements = []

		elements.addAll(extensions.sort {a,b -> a.displayName <=> b.displayName })
		elements.addAll(ringGroups.sort {a,b -> a.name <=> b.name })

		return elements ?: []
	}

    void resetCallState(){
		log.debug("Resetting Call State for ${this.name}:  From: ${this.callState} to ${this.state}")

		this.callState = this.state
	}

	void setAgentState(String state){
		log.debug("Setting state for $name to: $state")

		this.callState = state
		this.state = state

		log.debug "$name states: $state, $callState"
	}

	boolean isAvailable(){
		((AsteriskCall.findByPrimaryOwnerOrSecondaryOwner(name, name) == null) && loggedIn)
	}

	String toString(){
		return name
	}

	AsteriskCall getActiveCall(){
		def userCalls = callService.listActiveCalls(this)

		AsteriskCall ownedCall = userCalls.find { AsteriskCall call ->
			call.state == 'active'
		}

		ownedCall
	}

	List<Queue> getAvailableQueues(){
		def allQueues = Queue.findAll().name.toSet()
		def memberQueues = QueueMember.findAllByMembername(name).queueName.toSet()

		List<String> availQueueNames = (allQueues - memberQueues).toList()

		return (availQueueNames)? Queue.findAllByNameInList(availQueueNames) : []
	}

	List<ExtensionLocation> getAvailablePrefixes(){
		List<ExtensionLocation> options = ExtensionLocation.where {
			!(prefix in extensions.prefix)
		}.list(sort: "prefix", order: 'asc')

		options
	}

	List<FollowMeNumber> getFollowmeSteps() {
		FollowMeConfig agentFMConfig = FollowMeConfig.findByName(this.id as String)
		List<FollowMeNumber> followmeSteps = []

		if (agentFMConfig) {
			followmeSteps = FollowMeNumber.findAllByName(agentFMConfig.name, [sort: "ordinal", order: "asc"])
		} else {
			agentFMConfig = new FollowMeConfig(name: this.id as String)
			agentFMConfig.save()

			DialPlanService.createFollowMe(this)
		}

		if (!followmeSteps) {
			followmeSteps = [new FollowMeNumber(name: this.id as String, phoneNumber: this.primaryExtension.phoneNumber, ordinal: 1).save()]
		}

		followmeSteps
	}

	boolean hasRole(String roleName){
		List<String> roleNames = authorities.authority

		(roleName in roleNames)
	}
}