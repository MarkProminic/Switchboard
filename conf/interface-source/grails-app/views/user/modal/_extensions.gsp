<div id="user-extensions" >
    <div class="row" >
        <h3 class="col-md-3 marginless" >Extensions</h3>
        <div class=" col-md-9" >
            <a class="btn btn-sm btn-success pull-right" onclick="showNewExtensionForm()" >
                <i class="fa fa-plus fa-fw" ></i> Create Extension
            </a>
        </div>
    </div>
    <div>
        <h5 id="user-extension-message" ></h5>
    </div>
    <div id="new-extension-row" class="row hidden" >
        <div class="col-md-9 row" >
            <div class="col-md-4" >
                <select id="new-extension-name" class="form-control"  onchange="checkSipAllowed()" >
                    <g:each in="${this.user.availablePrefixes}" var="location" >
                        <option value="${location.prefix}" sip-allowed="${location.sipAllowed}" >${location.name}</option>
                    </g:each>
                </select>
            </div>
            <div class="col-md-2" >
                <input id="new-extension-is-sip" type="checkbox" onclick="toggleRemoteNumberField()" >
                <label for="new-extension-is-sip" > is SIP</label>
            </div>
            <div class="col-md-6" >
                <input id="new-extension-remote-number" type="text" class="form-control" placeholder="Remote Number" >
            </div>
        </div>
        <div class="col-md-3" >
            <div class="btn-group-sm" >
                <a class="btn btn-success" onclick="addUserExtension()" ><i class="fa fa-check" ></i></a>
                <a class="btn btn-danger" onclick="hideNewExtensionForm(true)" ><i class="fa fa-ban" ></i></a>
            </div>
        </div>
    </div>
    <ul id="user-extension-list" class="list-group"  >
        <g:each in="${this.user.extensions.sort{a,b -> a.number <=> b.number}}" var="extension" >
            <li id="agent-extension-${extension.id}" class="list-group-item row" >
                <div class="col-md-9 row" >
                    <h5 class="col-md-3 agent-extension-name" >${extension.name}</h5>
                    <h5 class="col-md-3 agent-extension-number" >${extension.phoneNumber}</h5>
                    <div class="col-md-6" >
                        <input type="text" class="form-control agent-extension-name hidden"
                               initial="${extension.name}" value="${extension.name}" >
                    </div>
                    <div class="col-md-6" >
                        <input type="text" class="form-control hidden agent-extension-number"
                               initial="${extension.phoneNumber}" value="${extension.phoneNumber}" >
                    </div>
                </div>
                <div class="col-md-3" >
                    <div class="btn-group-sm edit-controls pull-right" >
                        <a class="btn btn-success agent-extension-check hidden" onclick="updateExtension('${extension.id}')" >
                            <i class="fa fa-check" ></i>
                        </a>
                        <a class="btn btn-danger agent-extension-check hidden" onclick="hideExtensionFields('${extension.id}', true)" >
                            <i class="fa fa-ban" ></i>
                        </a>
                    </div>
                    <div class="btn-group-sm management-controls pull-right" >
                        <a class="btn btn-warning agent-extension-edit" onclick="showExtensionFields('${extension.id}')" ><i class="fa fa-edit fa-fw" ></i></a>
                        <a class="btn btn-danger agent-extension-delete" onclick="showDeletionControls('${extension.id}')" ><i class="fas fa-times fa-fw" ></i></a>
                    </div>
                    <div class="hidden btn-group-sm deletion-controls row" >
                        <h5 class="col-md-8" >Are you sure?</h5>
                        <a class="col-md-2 btn btn-primary agent-extension-delete" onclick="deleteExtension('${extension.id}')" > Yes</a>
                        <a class="col-md-2 btn btn-danger agent-extension-edit" onclick="showManagementControls('${extension.id}')" > No</a>
                    </div>
                </div>
            </li>
        </g:each>
    </ul>
</div>