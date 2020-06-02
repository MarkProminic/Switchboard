<%@ page import="security.Agent" %>

<g:if test="${agents}" >
    <g:each in="${agents}" var="agent" >
        <g:set var="agentCall" value="${agentCall}" />
    <li id='agent-${agent.id}' class='list-group-item ${agentCall ? 'agent-on-call' : ''} ' >
        <div class='row' >
            <div class='col-md-6 row' >
                <h4 class='list-group-item-heading text-info col-md-12' >${agent.name}</h4>
                <g:if test="${agentCall}" >
                    <div class="agent-owned-call col-md-12 row" >
                        <span class="col-md-12" >${agentCall.displayName}</span>
                        <span class="col-md-12" >${agentCall.displayNumber}</span>
                        <span class="col-md-12" >${agentCall.queue ?: ''}</span>
                    </div>
                </g:if>
                <g:else>
                    <small class='location col-md-12' >${agent.primaryExtension?.displayName}</small>
                </g:else>
            </div>
            <div class='col-md-6 on-call-btns ${userOnCall ? '' : 'hidden' }' >
                <div class='btn-group btn-group-sm transfer-call-btn' >
                    <a class='btn btn-warning' href='#' onclick='transferCallTo("${agent.id}", "primary")' >
                        <i class='fa fa-arrow-left fa-fw' ></i> Transfer</a>
                    <a class='btn btn-default dropdown-toggle' data-toggle='dropdown' href='#' >
                        <span class='fa fa-caret-down' title='Toggle dropdown menu' ></span>
                    </a>
                    <ul class='dropdown-menu dropdown-menu-right' >
                        <li><a href='#' onclick='transferCallTo(${agent.id}, "voicemail")' >To Voicemail</a></li>
                        <li class='divider' ></li>
                        <g:render template="transferOption" collection="${agent.extensions}" />
                    </ul>
                </div>
                <div class='btn-group btn-group-sm add-agent-to-call-btn' >
                    <a class='btn btn-success' onclick='addAgentToCall("${agent.id}", "primary")' >
                        <i class='fa fa-plus' ></i> Add to Call
                    </a>
                    <a class='btn btn-default dropdown-toggle' data-toggle='dropdown' href='#' >
                        <span class='fa fa-caret-down' title='Toggle dropdown menu' ></span>
                    </a>
                    <ul class='dropdown-menu dropdown-menu-right' >
                        <g:render template="addToCallOption" collection="${agent.extensions}" />
                    </ul>
                </div>
            </div>
            <div class='col-md-6 idle-btns ${userOnCall ? 'hidden' : '' }' >
                <div class='btn-group btn-group-sm call-agent-btn' >
                    <a class='btn btn-primary' onclick='callAgent("${agent.id}")' href='#' ><i class='fa fa-phone fa-fw' ></i> Call</a>
                    <a class='btn btn-default dropdown-toggle' data-toggle='dropdown' href='#' >
                        <span class='fa fa-caret-down' title='Toggle dropdown menu' ></span>
                    </a>
                    <ul class='dropdown-menu dropdown-menu-right dropdown-menu dropdown-menu-right-right' >
                        <g:render template="callOption" collection="${agent.extensions}" />
                    </ul>
                </div>
                <div class='btn-group btn-group-sm add-to-queue-btn' >
                    <a class='btn btn-success dropdown-toggle' data-toggle='dropdown' href='#' >
                        <span class='fa fa-plus fa-fw' ></span><p class='inline' > Add to Queue</p>
                        <span class='fa fa-caret-down fa-fw' title='Toggle dropdown menu' ></span>
                    </a>
                    <ul class='dropdown-menu dropdown-menu-right dropdown-menu dropdown-menu-right-right queue-list' >
                        <g:render template="availQueueList" model="${[agentId: agent.id, queues: agent.availableQueues]}" />
                    </ul>
                </div>
            </div>
        </div>
    </li>
</g:each>
</g:if>
<g:else>
    <h4 class="text-center" >No Agents to list</h4>
    <sec:access expression="hasRole('ROLE_ADMIN')">
        <h4 class="text-center" >
            <a href="#" onclick="showAdminPanel()" >Create a new Agent</a>
        </h4>
    </sec:access>
</g:else>
