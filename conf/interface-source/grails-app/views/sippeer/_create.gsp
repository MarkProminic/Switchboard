<form id="admin-peer-create" >
    <div id="admin-peer-type" >
        <h5 class="radio" >
            <input name="type" type="radio" value="friend" checked onchange="togglePeerCreationInputs('friend')" />
            Agent Extension
        </h5>
        <h5 class="radio" ><input name="type" type="radio" value="peer" onchange="togglePeerCreationInputs('peer')" />
            Trunk
        </h5>
    </div>
    <div class="well well-sm" >
        <div id="admin-peer-agent" class="row extension-input" >
            <div class="col-md-2" >
                <h5>Agent:</h5>
            </div>
            <div class="col-md-3" >
                <g:select name="agent" from="${agents}" class="form-control" onchange="fillPrefixOptions()" optionKey="id" />
            </div>
        </div>
        <div id="admin-peer-prefix" class="row extension-input" >
            <div class="col-md-2" >
                <h5>Location:</h5>
            </div>
            <div class="col-md-3" >
                <select name="prefix" class="form-control" ></select>
            </div>
        </div>
        <div id="admin-peer-trunk-name" class="hidden row trunk-input" >
            <div class="col-md-2" >
                <h5>Name:</h5>
            </div>
            <div class="col-md-3" >
                <input name="trunkName" type="text" autocomplete="off" />
            </div>
        </div>
        <div id="admin-peer-trunk-host" class="hidden row trunk-input" >
            <div class="col-md-2" >
                <h5>Host:</h5>
            </div>
            <div class="col-md-3" >
                <input name="host" type="text" autocomplete="off" />
            </div>
        </div>
    </div>
</form>
<a class="btn btn-sm btn-success" onclick="createPeer()" > Create</a>