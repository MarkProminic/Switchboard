/**
 * audioplayer
 * Created by Luis Alcantara on 5/3/17.
 */

/*
 * Developed by Luis Alcantara
 *
 * Copyright (C) 2016-2019 Prominic.NET, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 *
 * As a special exception, the copyright holders give permission to link the
 * code of portions of this program with the OpenSSL library under certain
 * conditions as described in each individual source file and distribute
 * linked combinations including the program with the OpenSSL library. You
 * must comply with the Server Side Public License in all respects for
 * all of the code used other than as permitted herein. If you modify file(s)
 * with this exception, you may extend this exception to your version of the
 * file(s), but you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version. If you delete this
 * exception statement from all source files in the program, then also delete
 * it in the license file.
 */

function playAudio(audioIdNum){
    let recordingItem = $('#recording-' + audioIdNum);

    let player = recordingItem.find('audio').get(0);
    let playerDiv = recordingItem.find(".player-div");

    let playButton = recordingItem.find(".play-button");
    let pauseButton = recordingItem.find(".pause-button");

    $('audio').each(function(){
        pauseAudio(this.id)
    });

    player.play();

    playerDiv.css('background-color','#3d80ff');

    playButton.hide();
    pauseButton.show();
}

function pauseAudio(audioIdNum){
    let recordingItem = $('#recording-' + audioIdNum);

    let player = recordingItem.find('.player').get(0);;
    let playerDiv = recordingItem.find(".player-div");

    let playButton = recordingItem.find(".play-button");
    let pauseButton = recordingItem.find(".pause-button");

    if(player){
        player.pause();
    }


    playerDiv.css('background-color', '#AAA');

    pauseButton.hide();
    playButton.show()
}

function dragStopHandler(event, playerIdNum){
    console.log("dragstop")

    let recording = $('#recording-' + playerIdNum);
    let audio = recording.find("audio").get(0);

    let scrubber = recording.find(".player-scrubber")
    let scrubberWidth = scrubber.width();

    if (audio.readyState == 0) {
        audio.load()
    }

    let x = event.clientX - scrubber.offset().left;
    let percent;
    
    if(x > scrubberWidth) {
        percent = 1
    }
    else if(x < 0){
        percent = 0
    }
    else{
        percent = x / scrubberWidth
    }
    
    updateProgressWidth(percent, playerIdNum);
    updateAudioTime(percent, playerIdNum)
}

function handleDragHandler(event, playerIdNum){
    console.log("drag")

    let recording = $('#recording-' + playerIdNum);
    let audio = recording.find('audio').get(0);

    let scrubber = recording.find(".player-scrubber")
    let scrubberWidth = scrubber.width();

    if (audio.readyState == 0) {
        audio.load()
    }

    let offset = scrubber.offset().left;

    let x = event.clientX - offset;

    let percent;

    if(x > scrubberWidth) {
        percent = 1
    }
    else if(x < 0){
        percent = 0
    }
    else{
        percent = x / scrubberWidth
    }

    updateSeekingTimeDisplay(percent, playerIdNum);
    updateProgressWidth(percent, playerIdNum)
}

function updateAudioTime(percent, playerIdNum){
    let recording = $('#recording-' + playerIdNum);

    let audio = recording.find('audio').get(0);
    let duration = recording.find(".audio-duration").html();
    
    audio.currentTime = percent * duration;
    updateTimeDisplay(playerIdNum)
}

function updateSeekingTimeDisplay(percent, playerIdNum){
    let recording = $('#recording-' + playerIdNum);

    let timeDisplay = recording.find(".player-time-display");
    let totalSeconds = recording.find(".audio-duration").html();

    let time = totalSeconds * percent;
    let seconds = Math.floor(time % 60);
    let minutes = Math.floor((time/60) % 60);
    let hours = Math.floor(time/3600);

    if(hours < 10){hours = '0' + hours}
    if(minutes < 10){minutes = '0' + minutes}
    if(seconds < 10){seconds = '0' + seconds}
    
    timeDisplay.html(hours + ":" + minutes + ":" + seconds)
}

function updateTimeDisplay(playerIdNum){
    let recording = $('#recording-' + playerIdNum);

    let audio = recording.find('audio');
    let timeDisplay = recording.find(".player-time-display");

    let timeString = formatDuration(audio.get(0).currentTime);
    
    timeDisplay.html(timeString);
}

function audioTimeUpdateHandler(playerIdNum){
    let row = $('#recording-' + playerIdNum)

    let player = row.find('.player-div');
    let duration = row.find('.audio-duration').html();
    let audio = $('#' + playerIdNum).get(0);

    if(audio.readyState === 4) {
        let percent = audio.currentTime / duration;

        if (percent * 150 > 142.5) {
            percent = 1
        }

        updateProgressWidth(percent, playerIdNum);
        updateTimeDisplay(playerIdNum)
    }
}

function updateProgressWidth(percent, playerIdNum){
    let recording = $('#recording-' + playerIdNum);

    let progress = recording.find(".progress-bar");
    let handle = recording.find(".progress-handle");

    if(percent <= 1 && percent >= 0){
        let left = percent * 150;

        if(left > 7.5) {
            handle.css("left", left - 7.5 + "px");
        }
        progress.css("width", left + "px")
    }
}

function formatDuration(duration){
    let seconds = Math.floor(duration % 60);
    let minutes = Math.floor(duration / 60) % 60;
    let hours = Math.floor((duration / 60) / 60);

    if(hours < 10){
        hours = `0${hours}`;
    }
    if(minutes < 10){
        minutes = `0${minutes}`;
    }
    if(seconds < 10){
        seconds = `0${seconds}`;
    }

    return `${hours}:${minutes}:${seconds}`;

}

function seekToHere(event, playerId){
    let seekBar = $(event.target);
    let left = event.clientX - seekBar.offset().left;

    let percent = left / seekBar.width();

    updateProgressWidth(percent, playerId);
    updateSeekingTimeDisplay(percent, playerId);
    updateAudioTime(percent, playerId);
}