    <g:if test="${flash.message}" >
        <div class="message" role="status" >${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${this.agent}" >
        <ul class="errors" role="alert" >
            <g:eachError bean="${agent}" var="error" >
                <li <g:if test="${error in org.springframework.validation.FieldError}" >data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
            </g:eachError>
        </ul>
    </g:hasErrors>
    <g:form resource="${agent}" method="PUT" id="${agent?.id}" >
            <div class="panel panel-default" >
                <div class="panel-heading" >
                    <p class="panel-title" >Extensions</p>
                </div>
                <div class="panel-body" >
                    <div id="primary-ext" class="row" >
                        <g:set var="primaryExt" value="${agent?.retrievePrimaryExtension()}" />
                        <div class="col-lg-3 col-lg-offset-3 " >
                            <g:if test="${primaryExt?.type == 'Local'}" >
                                <i class="fa fa-phone" ></i>
                            </g:if>
                            <g:else>
                                <i class="fa fa-mobile-phone" ></i>
                            </g:else>
                            <a href="/extension/edit/${primaryExt?.id}" >${primaryExt?.toString()}</a>
                        </div>
                        <label class="col-lg-4 col-lg-offset-1" ><g:radio name="primary" value="${primaryExt?.number}" checked="true" /> Call me here</label>
                    </div>
                    <g:findAll in="${agent?.extensions}" expr="it.primaryExt == false" >
                        <div class="row" >
                            <span class="col-lg-3 col-lg-offset-3" >
                                <g:if test="${it?.type == 'Local'}" >
                                    <i class="fa fa-phone" ></i>
                                </g:if>
                                <g:else>
                                    <i class="fa fa-mobile-phone" ></i>
                                </g:else>
                                <a href="/extension/edit/${it?.id}" >${it?.toString()}</a>
                            </span>
                            <label class="col-lg-4 col-lg-offset-1" ><g:radio name="primary" value="${it?.number}" onclick="this.form.submit()" /> Call me here</label>
                        </div>
                    </g:findAll>

                </div>
                <div class="row" >
                    <a href="/extension/create?agent=${agent?.id}" class="fa btn btn-xl fa-plus-square text-success col-lg-offset-10" ></a>
                </div>
            </div>

    </g:form>