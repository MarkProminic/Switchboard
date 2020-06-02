<div class='modal-dialog' >
    <div class='modal-content' >
        <div class='modal-header' >
            <h1 class='modal-title' >Interface Configuration</h1>
            <div class="error-message" ></div>
            <div id="interface-config-missing-values-table" >
                <g:render template="/configLine/missingValues" />
            </div>
        </div>
        <div class='modal-body scrollable-modal container' >
            <g:if test="${message}" >
                <h4>${message}</h4>
            </g:if>
            <form id="interface-config-inputs" >
                <div class="row" >
                    <h4 class="col-md-3" >Category: </h4>
                    <div class="col-md-3" >
                        <g:select class="form-control" id="config-category" name="category" from="${categories}" value="${category}" oninput="updateConfigMenu('category')" />
                    </div>
                </div>
                <g:if test="${properties}" >
                    <div class="row" >
                        <h4 class="col-md-3" >Property Name: </h4>
                        <div class="col-md-3" >
                            <g:select class="form-control" id="config-property" name="property" from="${properties}" value="${property}" oninput="updateConfigMenu('property')" />
                        </div>
                    </div>
                </g:if>
                <g:if test="${value}" >
                    <div class="row" >
                        <h4 class="col-md-3" >Property Value: </h4>
                        <div class="col-md-3" >
                            <input type="text" class="form-control" id="config-value" name="value" value="${value}" />
                        </div>
                    </div>
                    <div class="row" >
                        <div class="col-md-offset-3 col-md-3" >
                            <a class="btn btn-sm btn-success" onclick="updateConfigValue()" >Update</a>
                        </div>
                    </div>
                </g:if>
            </form>
        </div>
        <div class='modal-footer' >
            <a id="report-close-btn" class="btn btn-primary" onclick="hideConfigMenu()" >Close</a>
        </div>
    </div>
</div>