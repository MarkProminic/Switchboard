<g:if test="${!callRecordList}" >
    <h4 class="text-center" >No Pending Call Reports</h4>
</g:if>
<g:else>
    <g:render template="callRecordTile" collection="${callRecordList}" />
</g:else>