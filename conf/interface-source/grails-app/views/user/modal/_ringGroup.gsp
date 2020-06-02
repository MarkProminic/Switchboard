<li id="user-ring-group-${group.id}" class="list-group-item row" >
    <div class="col-md-9 row" onclick="toggleRingGroupExtensions('${group.id}')" >
        <h5 class="col-md-3 agent-ring-group-name info-field" >${group.name}</h5>
        <h5 class="info-field info" >${group.memberList}</h5>
        <div class="col-md-3" >
            <input type="text" class="form-control agent-ring-group-name edit-field hidden" value="${group.name}" >
        </div>
        <div class="col-md-9 group-edit-extension-list edit-field hidden" >
            <g:each in="${this.user.extensions}" var="extension" >
                <div class='col-md-3 checkbox' >
                    <label>
                        <input id='${extension.id}' type='checkbox' class='edit-option extension'
                            ${(extension in group.extensions)? "checked": ""} >${extension.displayName}
                    </label>
                </div>
            </g:each>
        </div>
    </div>
    <div class="col-md-3" >
        <div class="btn-group-sm edit-controls pull-right hidden" >
            <a class="btn btn-success agent-ring-group-check" onclick="updateRingGroup('${group.id}')" ><i class="fa fa-check" ></i></a>
            <a class="btn btn-danger agent-ring-group-check" onclick="hideRingGroupFields('${group.id}')" ><i class="fa fa-ban" ></i></a>
        </div>
        <div class="btn-group-sm management-controls pull-right" >
            <a class="btn btn-warning agent-ring-group-edit" onclick="showRingGroupFields('${group.id}')" ><i class="fa fa-edit fa-fw" ></i></a>
            <a class="btn btn-danger agent-ring-group-delete" onclick="showRingGroupDeletionControls('${group.id}')" ><i class="fas fa-times fa-fw" ></i></a>
        </div>
        <div class="btn-group-sm deletion-controls row hidden" >
            <h5 class="col-md-8" >Are you sure?</h5>
            <a class="col-md-2 btn btn-primary agent-ring-group-delete" onclick="deleteRingGroup('${group.id}')" > Yes</a>
            <a class="col-md-2 btn btn-danger agent-ring-group-edit" onclick="showRingGroupManagementControls('${group.id}')" > No</a>
        </div>
    </div>
</li>