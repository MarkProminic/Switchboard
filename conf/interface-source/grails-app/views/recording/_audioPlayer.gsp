<g:if test="${recording}" >
    <div id="recording-${recording.id}" >
        <div class="player-div row" >
            <div class="col-md-2" >
                <a class="btn btn-xs btn-primary play-button" onClick="playAudio('${recording.id}'); return false" >Play</a>
                <a class="btn btn-xs btn-default pause-button hidden" onClick="pauseAudio('${recording.id}'); return false" >Pause</a>
            </div>
            <div class="col-md-2" >
                <small class="player-time-display" >00:00:00</small>
            </div>
            <div class="col-md-6" >
                <div class="player-scrubber" >
                    <div class="progress-bar" >
                        <div class="progress-handle" ></div>
                    </div>
                    <div class="handle-container" onmousedown="seekToHere(event, '${recording.id}')" ></div>
                </div>
            </div>
            <div class="col-md-2" >
                <small class="player-duration-display" >${recording.durationString}</small>
                <p class="audio-duration" style="display:none;" >${recording.duration}</p>
            </div>
        </div>
        <audio id='${recording.id}' class="player hidden" preload="none" onTimeUpdate="audioTimeUpdateHandler('${recording.id}')" >
            <source src="/recordings/${recording.id}" >
        </audio>
    </div>
</g:if>
<g:else>
    <h3>No Recording found for call '${uniqueId}'</h3>
</g:else>