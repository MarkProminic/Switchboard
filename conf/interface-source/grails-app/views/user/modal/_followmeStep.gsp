<g:if test="${step}" >
    <li id="followme-step-${step.id}" class="list-group-item row" >
    <div class="col-md-9 row " >
        <h5 class="col-md-3 followme-step-ordinal" >${i +1}</h5>
        <h5 class="col-md-9 followme-step-number step-info" >${step.displayName}</h5>
        <div class="col-md-9 hidden step-field" >
            <div class="row followme-step-element-list" >
                <div class="col-md-3 ${i == 0 ? 'radio' : ''}" >
                    <label>
                        <g:if test="${i != 0}" >
                            <g:each in="${this.user.extensions}" var="extension" >
                                <label>
                                    <g:checkBox name="step-${step.id}-element" value="${extension.id}" />
                                    ${extension.name}
                                </label>
                            </g:each>
                        </g:if>
                    </label>
                </div>
            </div>
        </div>
    </div>
    <div class="col-md-3" >
        <g:if test="${i != 0}" >
            <div class="hidden btn-group-sm edit-controls pull-right" >
                <a class="btn btn-success followme-step-check" onclick="updateStep('${step.id}')" ><i class="fa fa-check" ></i></a>
                <a class="btn btn-danger followme-step-check" onclick="hideStepFields('${step.id}')" ><i class="fa fa-ban" ></i></a>
            </div>
            <div class="btn-group-sm management-controls pull-right" >
                <a class="btn btn-warning followme-step-edit" onclick="showStepFields('${step.id}')" ><i class="fa fa-edit fa-fw" ></i></a>
                <a class="btn btn-danger followme-step-delete" onclick="showStepDeletionControls('${step.id}')" ><i class="fas fa-times fa-fw" ></i></a>
            </div>
        </g:if>
    </div>
</li>
</g:if>
<g:else>
    <li class="list-group-item" ><h4>Followme Step was null</h4></li>
</g:else>