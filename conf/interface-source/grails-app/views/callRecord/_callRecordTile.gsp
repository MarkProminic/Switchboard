<li id="call-record-${it.uniqueId}" class="message list-group-item " >
    <div class="row" >
        <div class="col-md-4" >
            <small class="vm-message-date" >${it.startDateString}</small>
        </div>
        <div class="col-md-4" >
            <h5>${it.customerName} ${it.customerNumber} </h5>
        </div>
        <div class="col-md-4" >
            <a href="#" class="btn btn-block btn-sm btn-primary view-record-btn" onclick="showCallRecordModal('${it.uniqueId}')" >View</a>
        </div>
    </div>
</li>