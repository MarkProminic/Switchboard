<form id="admin-agent-create" >
    <div class='modal-body form-horizontal' >
        <h5 id="agent-creation-message" class="message" ></h5>
        <div class="row" >
            <div class="row" >
                <h4 class="col-md-offset-8 col-md-4" >Primary Extension Location</h4>
            </div>
            <div class="row" >
                <div class='col-md-4' >
                    <input id='new-agent-name' name="name" class='form-control' type='text' placeholder="Agent Name" autocomplete="off" />
                </div>
                <div class='col-md-4' >
                    <input id='new-agent-email' name="email" class='form-control' type='text' placeholder="Email" autocomplete="off" />
                </div>
                <div class='col-md-4'>
                    <g:select name='new-agent-extension-name' from="${sipLocations}" class='form-control'
                              optionValue="name" optionKey="prefix" />
                </div>
            </div>
        </div>
        <div class="row" >
            <div class="col-md-3">
                <h4>Voicemail Alerts</h4>
                <h5>
                    <input id="voicemail-sms-opt-in" type="checkbox" onclick="toggleVoicemailSMSFields()" />
                    Enable SMS alerts upon receiving voicemails
                </h5>
            </div>
        </div>
        <div class='form-group sms-field-panel hidden' >
            <div class='col-md-6' >
                <input class='form-control' type='text' id='new-agent-pager-number' placeholder="Mobile Number" autocomplete="off" />
            </div>
        </div>
        <div class='form-group sms-field-panel hidden' >
            <div class='col-md-6' >
                <h4>
                    Carrier
                    <g:select name='new-agent-pager-carrier' from="${carriers}" class='form-control'
                          optionKey="email" optionValue="name" />
                </h4>
            </div>
        </div>
        <div class="pull-right" >
            <a href="#" class="btn btn-sm btn-success" onclick="createNewAgent()" ><i class="fa fa-check"></i></a>
            <a href="#" class="btn btn-sm btn-danger" onclick="hideAgentCreationPanel()" ><i class="fa fa-ban"></i></a>
        </div>
    </div>
</form>