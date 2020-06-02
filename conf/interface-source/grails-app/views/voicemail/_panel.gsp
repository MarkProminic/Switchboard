<h4 class="inline-block" >Voicemail</h4>
<input id="vm-active" type="hidden" value="false" />
<h5 class="inline-block" >(<span id="vm-message-count" >${messageCount}</span>)</h5>
<i id="vm-message-count-info" class="fa fa-info-circle" title="${tooltip}" ></i>
<div id="vm-test-icons" class="inline pull-right" >
    <div id="progress" class="hidden" >
        <i class="fa fa-2x fa-spinner fa-pulse" ></i>
    </div>
    <div id="success" class="hidden" >
        <i class="fa fa-2x fa-check-circle text-success" ></i>
    </div>
    <div id="failure" class="hidden" >
        <i class="fa fa-2x fa-times-circle text-danger" ></i>
    </div>
</div>
<ul id="vm-messages-list" class="list-group" >
    <g:if test="${messages}" >
        <g:render collection="${messages}" template="/voicemail/message" var="message" />
    </g:if>
    <g:else>
        <h4 class="text-center" >No messages</h4>
    </g:else>
</ul>