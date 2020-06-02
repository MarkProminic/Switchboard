<g:set var="routeId" value="${route.id}" />
<li id='inbound-route-${route.id}' class='list-group-item row' >
    <div class="inbound-route-info" >
        <h5 class='col-xs-2' >${route.didNumber}</h5>
        <h5 class='col-xs-2' >${route.destinationName}</h5>
        <h5 class='col-xs-5' >${route.description}</h5>
        <div class="col-xs-3 inbound-route-btns" >
            <div class='pull-right btn-group-sm' >
                <a class='btn btn-warning' onclick='showInboundRouteFields("${route.id}")' >
                        <i class='fa fa-edit' ></i> Edit
                    </a>
                <a class='btn btn-danger' onclick='showInboundRouteDeletionConfirmation("${route.id}")' >
                        <i class='fas fa-times' ></i> Remove
                    </a>
            </div>
        </div>
        <div class="col-xs-3 inbound-route-delete-btns hidden" >
            <div class="btn-group-sm row" >
                <p class="col-xs-7" >Are you sure?</p>
                <a class="btn btn-danger" onclick="hideInboundRouteDeletionConfirmation('${route.id}')" >
                    No
                </a>
                <a class="btn btn-success" onclick="deleteInboundRoute('${route.id}')" >
                    Yes
                </a>
            </div>
        </div>
    </div>
    <div class="inbound-route-edit-fields hidden" >
        <div class="row" >
            <h5 class="col-xs-5" >${route.didNumber} should terminate at:</h5>
            <h5 class="col-xs-7" >Description:</h5>
        </div>
        <div class="row" >
            <div class="inbound-route-dest-options" >
                <g:render template="destinationOptions" model="[destOptions: destOptions, routeId: routeId]" />
            </div>
            <div class="col-xs-5" >
                <input type="text" class="form-control inbound-route-description" value="${route.description}" maxlength="50" />
            </div>
            <div class='col-xs-2 btn-group-sm' >
                <a class='btn btn-success' onclick='updateInboundRoute("${route.id}")' ><i class='fa fa-check' ></i></a>
                <a class='btn btn-danger' onclick='hideInboundRouteFields("${route.id}")' ><i class='fa fa-ban' ></i></a>
            </div>
        </div>
    </div>
</li>