<g:each in="${this.queues}" var="queue" >
    <li><a href='#' onclick='addToQueue(${this.agentId}, "${queue.name}")' >${queue.name}</a></li>
</g:each>
