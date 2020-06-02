<div id="user-ring-groups" >
    <div class="row" >
        <h3 class="col-md-3 marginless" >Ring Groups</h3>
        <div class=" col-md-9" >
            <a class="btn btn-sm btn-success pull-right" onclick="toggleNewRingGroupForm()" >
                <i class="fa fa-plus fa-fw" ></i> Add Ring Group
            </a>
        </div>
    </div>
    <h5 id="user-ring-group-message" ></h5>
    <div id="new-ring-group-form" class="list-group-item row hidden" >
        <div class="col-md-9" >
            <div class="row" >
                <div class="col-md-4" >
                    <input type="text" id="new-ring-group-name" class="form-control" value="" placeholder="Name" >
                </div>
                <div id="new-ring-group-extension-list" class="col-md-8 row" >
                    <g:each in="${this.user.extensions}" var="extension" >
                        <div id="agent-ring-group-new-extension-'${extension.id}'" class="col-md-4 checkbox" >
                            <label>
                                <input id="'${extension.id}'" type="checkbox" class="new-ring-group-extension" >${extension.name}
                            </label>
                        </div>
                    </g:each>
                </div>
            </div>
        </div>
        <div class="col-md-3" >
            <div class="btn-group-sm" >
                <a class="btn btn-success" onclick="addRingGroup()" ><i class="fa fa-check" ></i></a>
                <a class="btn btn-danger" onclick="toggleNewRingGroupForm()" ><i class="fa fa-ban" ></i></a>
            </div>
        </div>
    </div>
    <ul id="ring-group-list" class="list-group" >
        <g:render template="/user/modal/ringGroup" collection="${this.user.ringGroups}" var="group" />
    </ul>
</div>