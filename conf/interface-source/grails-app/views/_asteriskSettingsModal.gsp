<div id="sip-trunks" >
    <div class="row" >
        <h2 class="col-xs-4 marginless" >SIP Trunks</h2>
        <div class="col-xs-8" >
            <a class="btn btn-sm btn-success pull-right" onclick="showSiptrunkCreateInputs()" >
                <i class="fa fa-plus" ></i> Add SIP Trunk
            </a>
        </div>
    </div>
    <ul id="sip-trunk-list" class="list-group row scrollable-list" >
        <g:each in="${trunks}" var="trunk" >
            <li id="trunk-${trunk.id}" class="list-group-item" >
                <h4 class="inline trunk-info" >${trunk.name} (${trunk.host}) </h4>
                <h5 class="inline trunk-info" >${trunk.description ?: ''}</h5>
                <input name="name" type="text" class="trunk-edit-field hidden" value="${trunk.name}" />
                <input name="host" type="text" class="trunk-edit-field hidden" value="${trunk.host}" />
                <input name="description" type="text" class="trunk-edit-field hidden" value="${trunk.description}" maxlength="50" />
                <a class="btn btn-sm btn-warning pull-right" onclick="showSipTrunkEditInputs('${trunk.id}')" >
                    <i class="fa fa-edit" ></i> Edit
                </a>
            </li>
        </g:each>
    </ul>
</div>
<hr />
<div id="inbound-routes" >
    <div class='row' >
        <h2 class='col-xs-4 marginless' >Inbound Routes</h2>
        <div class='col-xs-8' >
            <a class='btn btn-sm btn-success pull-right' onclick='showNewInboundRouteForm()' >
                <i class='fa fa-plus fa-fw' ></i> Add Inbound Route
            </a>
        </div>
    </div>
    <div class='row' >
        <h4 id='inbound-route-message' class='col-xs-12' ></h4>
    </div>
    <div id='new-route-row' class='hidden' >
        <div class='row' >
            <div class='col-xs-4' >
                <input id='new-route-did' type='text' class='form-control' placeholder='Dialed Number' />
            </div>
            <div class="inbound-route-dest-options" >
                <div class='col-xs-4' >
                    <select class='form-control new-route-type' onchange='getDestNameOptions("new")' >
                        <option value='Agent' selected >Agent</option>
                        <option value='Queue' >Queue</option>
                    </select>
                </div>
                <div class='col-xs-4' >
                    <select class="form-control new-route-destination-name" >
                        <g:each in="${destOptions}" var="option" >
                            <option value="${option.id ?: option.name}" >${option.name}</option>
                        </g:each>
                    </select>
                </div>
            </div>
        </div>
        <div class='row' >
            <div class="col-xs-8" >
                <input type="text"  name="description" maxlength="50" class="form-control" placeholder="Description" />
            </div>
            <div class='col-xs-2 btn-group-sm pull-right' >
                <a class='btn btn-success' onclick='createNewInboundRoute()' ><i class='fa fa-check' ></i></a>
                <a class='btn btn-danger' onclick='hideNewInboundRouteForm(true)' ><i class='fa fa-ban' ></i></a>
            </div>
        </div>
    </div>
    <div id='inbound-route-list' >
        <ul class='list-group' >
            <g:each in="${routes}" var="route" >
                <g:render template="route" model="[route: route, destOptions: destOptions]"/>
            </g:each>
        </ul>
    </div>
</div>
<hr />
<div id="blacklisted-numbers" >
    <div class='row' >
        <h2 class='col-xs-4 marginless' >Blacklisted Numbers</h2>
        <input id='new-blacklist-number-number' type='tel' class='input-sm col-xs-3 marginless' />
        <div class='col-xs-5' ><a class='btn btn-sm btn-success pull-right' onclick='addBlacklistNumber()' >
            <i class='fa fa-plus fa-fw' ></i> Add</a>
        </div>
    </div>
    <div class='row' >
        <h4 id='blacklist-message' class='col-xs-12' ></h4>
    </div>
</div>