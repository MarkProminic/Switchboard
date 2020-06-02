<ul class="list-group" >
    <g:each in="${agents.enabled}" var="agent" >
        <li id="admin-agent-${agent.id}" class="list-group-item" >
            <h4 class="inline" >${agent.name}</h4>
            <div class="inline pull-right" >
                <g:if test="${!agent.hasRole('ROLE_ADMIN')}" >
                    <a class="btn btn-sm btn-success admin-grant-btn" onclick="makeAdmin('${agent.id}')" ><i class="fa fa-plus-circle" ></i> Make Admin</a>
                </g:if>
                <g:else>
                    <a class="btn btn-sm btn-warning admin-revoke-btn" onclick="removeAdmin('${agent.id}')" ><i class="fa fa-minus-circle" ></i> Remove as Admin</a>
                </g:else>
                %{--<a class="btn btn-sm btn-primary" onclick="resetPassword('${agent.id}')" ><i class="fa fa-key" ></i> Reset Password</a>--}%
                <a class="btn btn-sm btn-primary" href="${createLink(controller: "admin", action: "resetPassword", params: [id: agent.id])}" >
                    <i class="fa fa-key" ></i> Reset Password</a>
                <a class="btn btn-sm btn-danger" onclick="disableAgent('${agent.id}')" ><i class="fas fa-times" ></i> Disable</a>
            </div>
        </li>
    </g:each>
    <g:each in="${agents.disabled}" var="agent" >
        <li id="admin-agent-${agent.id}" class="list-group-item" >
            <h4 class="inline" >${agent.name}</h4>
            <div class="inline pull-right" >
                <a class="btn btn-sm btn-success" onclick="reenableAgent('${agent.id}')" ><i class="fa fa-check" ></i> Re-enable</a>
            </div>
        </li>
    </g:each>
</ul>