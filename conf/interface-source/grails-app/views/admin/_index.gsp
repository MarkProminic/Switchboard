<div class="modal-dialog" >
    <div class="modal-content" >
        <div class="modal-header" >
            <h2 class="modal-title" >Admin Panel</h2>
        </div>
        <div class="modal-body scrollable-modal" >
            <div id="admin-message" class="message" ></div>
            <div id="admin-agent-panel" >
                <h3 class="inline" >Agents (${agents.enabled.size() + agents.disabled.size()})</h3>
                <a class="inline btn btn-sm btn-success" onclick="showAgentCreationPanel()" ><i class="fa fa-plus" ></i> Create Agent</a>
                <div id="admin-create-agent-panel" >
                    <g:render template="/agent/create" model="${[carriers: this.carriers, sipLocations: this.sipLocations]}" />
                </div>
                <div id="admin-agent-list" >
                    <g:render template="/agent/adminList" model="${[agents: agents]}" />
                </div>
            </div>
            <hr />
            <div id="admin-peer-panel" >
                <h3 class="inline" >SIP Peers (${peers.size()})</h3>
                <a class="inline btn btn-sm btn-success" onclick="showPeerCreationPanel()" ><i class="fa fa-plus" ></i> Create Peer</a>
                <div id="admin-peer-panel-body" >
                    <div id="admin-create-peer-panel" class="hidden" >
                        <g:render template="/sippeer/create" model="${[agents: agents.enabled]}"/>
                    </div>
                    <br />
                    <div id="admin-peer-list" >
                        <g:render template="/sippeer/peerList" model="${[peers: peers]}" />
                    </div>
                </div>
            </div>
        </div>
        <div class="modal-footer" >
            <a class="btn btn-default" onclick="hideAdminPanel()" >Close</a>
        </div>
    </div>
</div>