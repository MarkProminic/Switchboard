<g:set var="carriers" value="${[[name: "Verizon", email: "vtext.com"], [name: "Sprint", email: "messaging.sprintpcs.com"],
[name: "Ting (T-Mobile)", email: "tmomail.net"], [name: "Virgin Mobile", email: "vmobl.com"],
[name: "Ting (Sprint)", email: "messaging.ting.com"], [name: "T-Mobile", email: "tmomail.net"]]}" />
<div class='modal-dialog' >
    <div class='modal-content' >
        <div class='modal-header' >
            <h2 class='modal-title' >Create Agent</h2>
        </div>
        <div class='modal-body form-horizontal' >
            <h5 id="agent-creation-message" class="message" ></h5>
            <h4>User</h4>
            <div class='form-group' >
                <label class='control-label col-md-3' >Name</label>
                <div class='col-md-6' >
                    <input class='form-control' type='text' id='new-agent-name' />
                </div>
            </div>
            <div class='form-group' >
                <label class='control-label col-md-3' >Email</label>
                <div class='col-md-6' >
                    <input class='form-control' type='text' id='new-agent-email' />
                </div>
            </div>
            <h4>Primary Extension</h4>
            <div class='form-group' >
                <label class='control-label col-md-3' >Location</label>
                <div class='col-md-6' >
                    <select class='form-control' id='new-agent-extension-name' ></select>
                </div>
            </div>
            <h4>Voicemail Alerts</h4>
            <h5><input id="voicemail-sms-opt-in" type="checkbox" onclick="toggleVoicemailSMSFields()" /> I want SMS alerts upon receiving voicemails</h5>
            <div class='form-group sms-field-panel hidden' >
                <label class='control-label col-md-3' >Mobile Number</label>
                <div class='col-md-6' >
                    <input class='form-control' type='text' id='new-agent-pager-number' />
                </div>
            </div>
            <div class='form-group sms-field-panel hidden' >
                <label class='control-label col-md-3' >Carrier</label>
                <div class='col-md-6' >
                    <select id='new-agent-pager-carrier' from="" class='form-control' >
                        <option></option>
                    </select>
                </div>
            </div>
        </div>
        <div class='modal-footer' >
            <a id="agent-creation-submit-btn" class='btn btn-success' onclick='createNewAgent()' >Create</a>
            <a class='btn btn-default' onclick='hideNewAgentModal()' >Close</a>
        </div>
    </div>
</div>