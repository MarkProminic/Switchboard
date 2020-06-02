<div class="modal-dialog" >
    <div class="modal-content" >
        <div class="modal-header" >
            <h2 class="modal-title" >User Settings</h2>
        </div>
        <div class="modal-body" >
            <g:render template="/user/modal/extensions" model="${[user: user]}" />
            <br />
            <g:render template="/user/modal/ringGroups" model="${[user: user]}" />
            <br />
            <g:render template="/user/modal/followme" model="${[user: user]}" />
            <g:render template="/user/security" />
        </div>
        <div class="modal-footer" >
            <a href="#" onclick="hideUserSettingsModal()" class="btn btn-default" >Close</a>
        </div>
    </div>
</div>