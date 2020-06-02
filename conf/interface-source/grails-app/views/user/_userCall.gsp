<div id="user-call-${call.uniqueId}" class="list-group-item user-call" >
    <div class="row" >
        <h4 class="message text-danger inline" ></h4>
    </div>
    <div class="row" >
        <h5 class="col-md-2 duration" >${call.durationString}</h5>
        <h5 class="col-md-10 name pull-left" >${call.displayName} -- ${call.displayNumber}</h5>
    </div>
    <div class="row" >
        <div class="btn-group btn-group-sm pull-right" >
            <g:if test="${call.direction != 'conference'}" >
                <a class="btn btn-sm btn-warning call-btn park" onclick="parkCall('${call.uniqueId}')" href="#" >
                    <i class="fas fa-parking" ></i> Park
                </a>
            </g:if>
            <g:if test="${call.duration > 3600}" >
                <a class="btn btn-sm btn-danger  call-btn kill-phantom" onclick="killPhantomCall('${call.uniqueId}')" href="#" >
                    <i class="fa fa-ban" ></i> Kill Phantom Call
                </a>
            </g:if>
        </div>
    </div>
</div>