<li id="vm-message-${message.id}" class="message list-group-item ${message.isNew ? 'new-message' : '' }" >
    <div class="row" >
        <div class="col-md-4" >
            <small>${message.formattedCreatedOn}</small>
        </div>
        <div class="col-md-5" >
            <h5 class="vm-message-callerid" title="${message.callerId}" >${message.callerName}</h5>
        </div>
        <div class="vm-controls col-md-1" >
            <audio id="vm-player-${message.id}" preload="none" onplaying="markAsListened('${message.id}')" class="hidden"
               onended="pauseVM('${message.id}')" >
                <source type="audio/wav" src="/voicemails/${message.id}" >
            </audio>
            <a class="btn btn-sm btn-default pull-right vm-play-btn" onclick="playVM('${message.id}')" ><i class="fa fa-play" ></i></a>
            <a class="btn btn-sm btn-default pull-right vm-pause-btn hidden" onclick="pauseVM('${message.id}')" ><i class="fa fa-pause" ></i></a>
        </div>
        <div class="vm-controls col-md-1" >
            <a id="delete-btn-${message.id}" class="btn btn-sm btn-danger pull-right" onclick="confirmDelete('${message.id}')" ><i class="fas fa-times" ></i></a>
        </div>
    </div>
    <div id="delete-controls-${message.id}" class="row hidden" >
        <small class="col-md-offset-2 col-md-4" >Are you sure?</small>
        <div class="col-md-6 btn btn-group-xs" >
            <a class="btn btn-danger" onclick="hideDeleteControls('${message.id}')" >No</a>
            <a class="btn btn-success" onclick="deleteMessage('${message.id}')" >Yes</a>
        </div>
    </div>
</li>