<div id="user-tile" class="list-group-item" >
    <div class="row" >
        <div class="col-md-5" >
            <h4 class="list-group-item-heading text-info" >${user.name}</h4>
            <h4 class="list-group-item-heading text-info" title="Your extension number" >x${user.id}</h4>
        </div>
        <div class="col-md-5" >
            <g:select from="${user.extensions}" name="user-location-list" class="form-control"
                      optionValue="displayName" optionKey="id" value="${user.primaryExtension.id}" onchange="changeLocation()" />
        </div>
    </div>
</div>
<div id="user-message" class="row" ></div>