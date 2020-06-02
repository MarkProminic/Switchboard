<%@ page import="switchboard.cloud.Extension" %>
<%@ page import="security.Agent" %>
<div id="user-followme" >
    <div class="row" >
        <h3 class="col-md-3 marginless" >Follow Me</h3>
        <div class="col-md-9" >
            <a class="btn btn-sm btn-success pull-right" onclick="toggleNewStepForm()" ><i class="fa fa-plus fa-fw" ></i> Add Step</a>
        </div>
    </div>
    <h5 id="user-followme-message" ></h5>
    <h6 class="bold" >Note that the first step will always be only your current location</h6>
    <ul id="followme-list" class="list-group" >
        <g:if test="${this.user.followmeSteps}" >
            <g:each in="${this.user.followmeSteps}" var="step" status="i" >
                <g:render template="/user/modal/followmeStep" model="${[step: step, i: i]}" />
            </g:each>
        </g:if>
        <g:else>
            <li id="followme-empty-step" class="list-group-item row" >
                <h4>No FollowMe steps are defined</h4>
            </li>
        </g:else>
    </ul>
    <div id="new-step-form" class="list-group-item row hidden" >
        <div class="col-md-9" >
            <div class="row" >
                <div class="col-md-2" >
                    <label for="new-followme-ordinal" >Ordinal</label>
                    <input type="number" id="new-followme-ordinal" class="form-control" value="${this.user.followmeSteps.size() + 1}" readonly="" >
                </div>
                <div class="col-md-10" >
                    <label>Elements</label>
                    <div id="new-step-element-list" class="row" >
                        <g:each in="${this.user.ringElements}" var="element" >
                            <div class="extension col-md-4 checkbox" >
                                <label>
                                    <input id="followme-option-'${element.id}'" type="checkbox"
                                           class="${element.class == Extension ? 'extension': 'ringGroup'}" >${element.name}
                                </label>
                            </div>
                        </g:each>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-3" >
            <div class="btn-group-sm" >
                <a class="btn btn-success" onclick="addFollowMeStep()" ><i class="fa fa-check" ></i></a>
                <a class="btn btn-danger" onclick="toggleNewStepForm()" ><i class="fa fa-ban" ></i></a>
            </div>
        </div>
    </div>
</div>