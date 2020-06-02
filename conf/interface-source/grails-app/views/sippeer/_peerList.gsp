<g:if test="${peers}" >
    <table class="table table-bordered table-responsive" >
    <thead>
        <tr>
            <th>Owner</th>
            <th>Name</th>
            <th>IP Address</th>
            <th>Status</th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${peers}" var="peer" >
            <tr class="${peer.status == 'Unregistered' ? 'admin-unregistered-peer' : ''}" >
                <td>${peer.owner?.name ?: 'None'}</td>
                <td>${peer.displayName}</td>
                <td>${peer.displayIPAddr}</td>
                <td>${peer.status}</td>
            </tr>
        </g:each>
    </tbody>
</table>
</g:if>
<g:else>
    <h4>No configured SIP Trunks</h4>
</g:else>