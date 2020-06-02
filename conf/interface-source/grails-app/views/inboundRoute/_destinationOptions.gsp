<div class='col-xs-4' >
    <select class='form-control ${routeId == 'new' ? 'new' : 'inbound'}-route-type' onchange='getDestNameOptions("${routeId}")' >
        <option value='Agent' ${params.type == 'Agent' ? "selected" : ""}>Agent</option>
        <option value='Queue' ${params.type == 'Queue' ? "selected" : ""}>Queue</option>
    </select>
</div>
<div class='col-xs-4' >
    <select class="form-control ${routeId == 'new' ? 'new' : 'inbound'}-route-destination-name" >
        <g:each in="${destOptions}" var="option" >
            <option value="${option.id ?: option.name}" >${option.name}</option>
        </g:each>
    </select>
</div>