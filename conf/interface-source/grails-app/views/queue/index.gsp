<%@ page import="switchboard.cloud.ConfigLine; security.Agent; switchboard.cloud.SIPPeer; switchboard.cloud.ConfigLine; switchboard.cloud.CallRecord" %>+
<html>
    <head>
        <meta name="layout" content="main" />
    </head>
    <body class="container-fluid row" >
        <div id="interface-modals" >
            <div class='modal modal-backdrop' ></div>
            <div id='asterisk-settings-modal' class='modal asterisk-settings-modal' >
                <div class='modal-dialog' >
                <div class='modal-content' >
                    <div class='modal-header' >
                        <h1 class='modal-title' >Asterisk Settings</h1>
                    </div>
                    <div class='modal-body scrollable-modal' ></div>
                    <div class='modal-footer' >
                        <a href='#' onclick='hideSettingsModal("asterisk")' class='btn btn-default' >Close</a>
                    </div>
                </div>
            </div>
            </div>
            <div id='user-settings-modal' class="modal" >
                <g:render template="/user/modal/userSettingsModal" model="${[user: this.user]}" />
            </div>
            <div id="call-record-modal" class="modal" >
                <g:render template="/callRecord/finalizeRecord" />
            </div>
            <div id="call-record-report-modals" >
                <div id="call-record-report-inputs" class="modal" ></div>
                <div id="call-record-report-results" class="modal" ></div>
                <div id="call-record-report-results-detail" class="modal" >
                    <div class="modal-dialog" >
                        <div class="modal-content" >
                            <div class="modal-header"></div>
                            <div class="modal-body scrollable-modal"></div>
                            <div class="modal-footer">
                                <a class="btn btn-default" onclick="hideReportResultDetail()">Close</a>
                            </div>
                        </div>
                    </div>
                </div>
                <div id="call-record-report-record-detail" class="modal" ></div>
            </div>
            <div id="call-record-edit-modal" class="modal" >
                <g:render template="/callRecord/editRecord" />
            </div>
            <div id="config-menu-modal" class="modal" >
                <g:render template="/configLine/menu" model="${[categories: ConfigLine.list(sort: "category").category.toSet()]}" />
            </div>
            <div id="admin-panel-modal" class="modal" ></div>
            <div id="connection-error-banner" class="hidden" >
                <h4>Connection to the server has been lost. Please refresh and report this outage if the problem persists.</h4>
            </div>
        </div>
        <div id="navbar-master-pane" >
            <div id="navbar-pane" class="body-pane container" >
                <nav id="navbar" class="navbar navbar-default" >
                    <div class="container-fluid" >
                        <!-- Brand and toggle get grouped for better mobile display -->
                        <div class="navbar-header" >
                            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false" >
                                <span class="sr-only" >Toggle navigation</span>
                                <span class="icon-bar" ></span>
                                <span class="icon-bar" ></span>
                                <span class="icon-bar" ></span>
                            </button>
                            <a class="navbar-brand" >
                                Switchboard
                                <small>ver. <g:meta name="info.app.version" /></small>
                            </a>
                        </div>
                        <!-- Collect the nav links, forms, and other content for toggling -->
                        <div class="collapse navbar-collapse" >
                            <ul id="default-nav-container" class="nav navbar-nav" >
                                <li><a href="#" onclick="showConfigMenu()" >Interface Config</a></li>
                                <sec:access expression="hasRole('ROLE_ADMIN')" >
                                    <li><a href="#" onclick="showAdminPanel()" >Admin Panel</a></li>
                                </sec:access>
                                <li><a href="#" onclick="showUserSettingsModal()" >User Settings</a></li>
                                <li id="nav-recordings-link" ><a href="#" onclick="showRecordingsPane()" >View Recordings</a></li>
                                <li id="nav-admin-records-link" ><a href="#" onclick="toggleAdminTable()" >Call Records List</a></li>
                                <li><a href="#" onclick="showSettingsModal('asterisk')" >Asterisk Settings</a></li>
                                <li id="user-queue-list" class="dropdown" >
                                    <a href="#" class="dropdown-toggle" data-toggle="dropdown" >Add to Queue<b class="caret" ></b></a>
                                    <ul class="dropdown-menu" >
                                        <g:render template="/agent/availQueueList" model="${[queues: this.user.availableQueues, agentId: this.user.id]}" />
                                    </ul>
                                </li>
                                <li id="nav-logout-link" class="dropdown" >
                                    <a href="#" class="dropdown-toggle" data-toggle="dropdown" >Log Out<b class="caret" ></b></a>
                                    <ul class="dropdown-menu" >
                                        <li><a href="#" onclick="logOffUser()" >Log Off Only</a></li>
                                        <li class="divider" ></li>
                                        <li><a class="no-hover" >Log Out of Interface and:</a></li>
                                        <li class="divider" ></li>
                                        <li class="mobile-option" ><a href="#" onclick="logOffAndSwitch()" >Switch to Mobile</a></li>
                                        <li><a href="#" onclick="logOffAll()" >All Queues</a></li>
                                    </ul>
                                </li>
                            </ul>
                        </div>
                    </div><!-- /.container-fluid -->
                </nav>
            </div>
            <div id="navbar-toggle-pane" onclick="toggleNavbar()" >
                <div id="toggle-pane-caret" class="fa fa-caret-left" ></div>
            </div>
        </div>
        <div id="main-pane" class="body-pane container" >
            <div id='recordings-panel' class="hidden" ></div>
            <div id="admin-records-panel" class="hidden" filtered="false" >
                <g:render template="/callRecord/list" />
            </div>
            <div id="user-pending-records-table" ></div>
            <div id="interface-panel" >
                <div id="view-left-col" class="view-col container col-xs-12 col-sm-4 well" >
                    <div id="search-panel" >
                        <div class="row form-group" >
                            <div class="col-md-1" >
                                <h5 class="text-center" >Call</h5>
                            </div>
                            <div class="col-md-5" >
                                <input type="text" id="remote-phone-number" placeholder="Phone Number"
                                       class="form-control" maxlength="20" min="3" />
                            </div>
                            <div class="col-md-1" >
                                <h5 class="text-center" >As</h5>
                            </div>
                            <div class="col-md-5" >
                                <select id="callerID" class="form-control col-md-3 callerid-select" >
                                    <option>Support</option>
                                    <option>Self</option>
                                    <option>Sales</option>
                                </select>
                            </div>
                        </div>
                        <div class="row" id="originate-message" ></div>
                        <div class="form-group" >
                            <div id="pending-call-gadget" ></div>
                            <a id="place-call-btn" class="idle-btns btn btn-primary btn-sm pull-right enter-target" onclick="placeCall()" >Place Call</a>
                            <a id="add-to-call-btn" class="hidden on-call-btns btn btn-success btn-sm pull-right" href="#" onclick="addToCall()" >Add to Call</a>
                        </div>
                        <br />
                        <div id="contacts-pending-gadget" class="row" >
                            <div id="contacts-pending-spinner" class="col-md-1" ><i class="fa fa-spinner fa-pulse fa-2x " ></i></div>
                            <h5 class="col-md-offset-1 col-md-8" >Refreshing contact data ..</h5>
                        </div>
                        <div id="typeahead-menu" class="row form-group" >
                            <div class="col-md-2" >
                                <h5 class="control-label" >Search</h5>
                            </div>
                            <div class="col-md-8" >
                                <input type="text" id="search-contact" class="form-control typeahead pull-right"/>
                            </div>
                        </div>
                        <div class="row" >
                            <div class="col-md-offset-9 col-md-3" >
                                <a class="btn btn-danger btn-sm pull-right btn-block" onclick="clearResults(event)" >Clear</a>
                            </div>
                        </div>
                        <div id="extension-hint" class="list-group-item hidden" ><h3></h3></div>
                        <div id="search-results" class="list-group list-container col-xs-12" ></div>
                    </div>
                    <div id="favorites-panel" >
                        <h4>Favorites</h4>
                        <ul id="favorites-list" class="list-group" ></ul>
                    </div>
                    <div id="call-record-panel" >
                        <h4>Pending Call Records <a class="pull-right" href="#" onclick="showUserPendingTable()" >View All</a></h4>
                        <ul id="pending-call-record-list" class="list-group" >
                            <g:if test="${callRecordList}" >
                                <g:render template="/callRecord/callRecordTile" collection="${callRecordList}" />
                            </g:if>
                            <g:else>
                                <h4 class="text-center" >No Pending Call Reports</h4>
                            </g:else>
                        </ul>
                    </div>
                    <br />
                    <div id="voicemail-panel" >
                        <h4>Voicemails</h4>
                        <h4 class="text-center" >Loading voicemails... <i class="fa fa-spinner fa-pulse" ></i></h4>
                    </div>
                </div>
                <div id="view-middle-col" class="view-col container col-xs-12 col-sm-4 well" >
                        <div id="queue-panel" >
                            <h5>Incoming Queue Calls</h5>
                            <ul class="list-group" id="queue-call-list" ></ul>
                            <hr />
                            <h4>Queues</h4>
                            <ul class="list-group" id="queue-list" ></ul>
                        </div>
                        <div id="parking-lot-panel" >
                            <h4>Parking Lot</h4>
                            <ul class="list-group" id="parking-lot" ></ul>
                        </div>
                        <div id="conference-room-panel" >
                            <h4>Conference Rooms</h4>
                            <ul class="list-group" id="conference-room-list" ></ul>
                        </div>
                </div>
                <div id="view-right-col" class="view-col container col-xs-12 col-sm-4 well" >
                    <div id="user-container" class="user-data-target" >
                        <g:render template="/user/tile" model="${[user: user]}" />
                        <div id="user-call-list" ></div>
                    </div>
                    <div id="agent-list-container" >
                        <h4>Agents</h4>
                        <ul id="agent-list" class="list-group" ></ul>
                    </div>
                </div>
            </div>
        </div>
        <script>
            $(document).ready(function(){
                createAsteriskSettingsModal();
                createRecordingsPanel();

                loadAdminPanel();
                refreshQueueList(false);
                refreshCallList();
                refreshPendingCallRecords();
                refreshParkingLot();
                refreshAgentList();
                refreshUserCalls();
                refreshAdminTable();
                refreshMailbox();
                refreshRoomList();
                refreshRecordings(true);

                getFavoritesList();

                initializeTypeahead();

                $('[name="agent-filter-check"]').click(function(){
                    var idList = [];

                    $('[name="agent-filter-check"]:checked').each(function(){
                        idList.push(this.id)
                    });
                    $.ajax({
                        url: "/queue/filterAgentList",
                        data: {idList: idList},
                        success: function(data){
                            $('#agent-list').html(data)
                        }
                    })
                });

                $('#extension-hint').click(function () {
                    $('#extension-hint').slideUp()
                });

                $('#remote-phone-number').keydown(function (event) {
                    if(event.which === 13){
                        enterBtnEventHandler()
                    }
                });

                $('.typeahead').bind('typeahead:select', function(ev, suggestion) {
                    displayResult(suggestion)
                });

                $('.typeahead').bind('typeahead:idle', function(ev, suggestion) {
                    var value = $('#search-contact').typeahead('val');
                    if(!value){
                        $('#search-results').html("")
                    }
                });

                $('.typeahead').bind('typeahead:render', function(){
//                    if(arguments.length == 2){
//                        $("#search-contact").typeahead('close')
//                        displayResult(arguments[1])
//                    } else {
//                        $('#search-results').html("")
//                    }
                });

                $(document).ajaxComplete(function(event, xhr, settings){
                    if(settings.url == 'contact/contacts'){
                        $('#contacts-pending-gadget').hide();
                        console.log("Suggestions request completed")
                    }
                })

            });

            function toggleDetailPane(name){

                $("div.detail-container:not([id *= name])").hide();

                $('#' + name + '-detail').toggle("drop", {direction: "right"}, 300);
                $('#'+name).toggleClass('active-flyout');

                $('#'+name+'-icon').children().toggleClass('active-action-pane-icon').toggleClass('action-pane-icon');

                $('#'+name+'-icon').toggleClass('active-action-pane-list-item').toggleClass('action-pane-list-item')
            }

            function hideTextFlyout(name){
                $('#' + name).fadeOut(500, function () {
                    $('#' + name).parent().css({zIndex: -1})
                })
            }

            function showTextFlyout(name){
                if(! ($('#'+name).hasClass('active-flyout')) ) {
                    $('#' + name).parent().css({zIndex: 1});
                    $('#' + name).fadeIn(250)
                }
            }

            function showErrorBanner(){
                $('#connection-error-banner').show()
            }
        </script>
    </body>
</html>