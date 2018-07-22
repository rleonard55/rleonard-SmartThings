// This is just a start, but I hope others will take interest and help make it better.
// It would be nice to add Current Status

include 'asynchttp_v1'

def clientVersion() {
    return "00.02.02"
}

/**
*  Smart Start 2.0
*
*  Copyright 2018 Rob Leonard
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
* Change log:
* 2018-05-03 - (v00.02.01) Updated Instrumentation
* 2018-06-16 - (v00.02.02) Lots of code cleanup, added proper encoding for user/pass & properties for Alexa
*/

metadata {
    definition(
        name: "Smart Start 2.0",
        namespace: "rleonard55",
        author: "Rob Leonard",	
        description: "Start/stop/arm/disarm/panic vehicle",
        singleInstance: false)
    {
        capability "timedSession"
        capability "lock"
        capability "switch"

        command "panic"
        command "trunk"
        command "locate"
        command "test"
        command "intentComplete"
        command "endRunning"
    }

    preferences {
        input("Username", "string", title:"SmartStart Username", description: "Your SmartStart Username" , required: true, displayDuringSetup: true)
        input("Password", "password", title:"SmartStart Password", description: "Your SmartStart Password", required: true, displayDuringSetup: true)
        input("VehicleName", "string", title:"SmartStart Vehicle Name", description: "Your SmartStart Vehicle Name", required: true, displayDuringSetup: true)
        input("GPS", "bool", title: "GPS Features", defaultValue: false, displayDuringSetup: true)
        input("Trunk", "bool", title:"Trunk Feature", defaultValue: false,  displayDuringSetup: true)
        input("StartMinutes","number",title:"Starter timeout minutes", description: "5 - 30 (15 is typical)", required: true,range: "5..30", displayDuringSetup:true)
        input(name: "LoggingLevel", type: "enum", title: "Logging Level", options: ["trace","debug","info","warn","error"],defaultValue: trace, displayDuringSetup: true)
    }

    simulator { }

    tiles (scale: 2) {
        multiAttributeTile(name: "info", type:"generic", width:6, height:4) {
            tileAttribute("device.info", key: "PRIMARY_CONTROL") {
                attributeState("default", label:'${currentValue}',backgroundColor:"#000000")
            }
            tileAttribute("device.lastUpdate", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'Updated: ${currentValue}')
            }
        }
        standardTile("command", "command", width:3, height:2){
            state "default", label: 'Lock', icon: "st.Transportation.transportation8", backgroundColor: "#ffffff", action: "lock"//, nextState:"lock"
            state "arm", label: 'Locking', icon: "st.bmw.doors-locked", backgroundColor: "#00a0dc"
            state "disarm", label: 'Unocking', icon: "st.bmw.doors-unlocked", backgroundColor: "#00a0dc"
            state "start", label: 'Starting', icon: "st.samsung.da.RC_ic_power", backgroundColor: "#00a0dc"
            state "trunk", label: 'Trunk', icon: "st.bmw.trunk_open", backgroundColor: "#00a0dc"
            state "panic", label: 'Panic', icon: "st.Office.office6", backgroundColor: "#00a0dc"
            state "locate", label: 'Locating', icon: "st.Office.office13", backgroundColor: "#00a0dc"
            state "error", label: 'Error', icon: "st.Seasonal Fall.seasonal-fall-010", backgroundColor: "#ff0065"
        }
        tileToggle("arm","lock","st.bmw.doors-locked","lock")
        tileToggle("disarm","unlock","st.bmw.doors-unlocked","unlock")
        tileToggle("remote","start","st.samsung.da.RC_ic_power","start")
        tileToggle("trunk","trunk","st.bmw.trunk_open","trunk")
        tileToggle("panic","panic","st.Office.office6","panic")
        tileToggle("locate","locate","st.Office.office13","locate")
        tileToggle("blank","Smart Start","https://github.com/rleonard55/rleonard-SmartThings/raw/master/devicetypes/rleonard55/smart-start-2-0.src/viper_smart_start.png","",6,1)
        standardTile("test","Test", width:3, height:2){
            state "active", label: 'Test', icon: "st.Office.office6", backgroundColor: "#ff9999", action: "test"
        }

        main (["command"])
        //details(["info","lock","unlock","remote","panic","trunk","locate","test"])
        details( ["info","arm","disarm","remote","blank","panic","trunk","locate"])
    }
}
private tileToggle(varName, label, icon, method,width=2,height=3) {
    return  standardTile("${varName}", "device.${varName}", inactiveLabel:false, decoration:"flat", width:width, height:height) {
        state "active", label:"${label}", icon:"${icon}", action:"${method}",nextState:"sending"//,backgroundColor: "#ffffff"
        state "sending", label:"Sending\n${label}", icon:"${icon}"//, backgroundColor: "#00a0dc"
        state "inactive", label:"${label}", icon:"https://github.com/rleonard55/rleonard-SmartThings/raw/master/devicetypes/rleonard55/smart-start-2-0.src/Loading.gif"//, backgroundColor: "#d3d3d3" 
        state "NA", label:""
        //state "NA", label:"${label}",backgroundColor: "#ffffff", icon: "https://github.com/rleonard55/rleonard-SmartThings/raw/master/devicetypes/rleonard55/smart-start-2-0.src/NA.png"
    }
}
def getServerUrl() { return "https://colt.calamp-ts.com" }
def getLoginUrl(user, pass) { return getServerUrl()+"/auth/login/${encode(user)}/${encode(pass)}"}
def getVehicleIdUrl() { return getServerUrl()+"/device/advancedsearch?sessid=${state.SessionId}"}
def getSendCommandUrl() {return getServerUrl()+"/device/sendcommand/${state.VechicleId}/${state.Intent}?sessid=${state.SessionId}"}

def void lock() {
    info "Received Lock Request"
    send("arm","Locking")
}
def void unlock() {
    info "Received Unlock Request"
    send("disarm","Unlocking")
}

def void on() {
    start()
}
def void off() {
    stop()
}
def void start() {
    info "Received Start Request"
    state.remoteType="Start"
    send("remote","Starting")
}
def void stop() {
    info "Received Stop Request"
    state.remoteType="Stop"
    send("remote","Stopping")
}

def void trunk(){
    info "Received Trunk Open Request"
    send("trunk","Sending Trunk")
}
def void panic(){
    info "Received Panic Request"
    send("panic","Sending Panic")
}
def void locate(){
    info "Received Locate Request"
    send("locate","Locating")
}

def void installed(){
	debug "installed"
    updateStatus "Smart Start: Ready"

    sendEvent(name: "sessionStatus", value: "stopped", displayed: false, isStateChange: true)
    sendEvent(name: "switch", value: "off", displayed: false, isStateChange: true)
	sendEvent(name: "lock", value: "unlocked", displayed: false, isStateChange: true)
    
    resumeProcessing()
}
def void initialize() {
    trace "Entered <initialize>"
    debug "Initialize called"// settings: $settings"
    try {
        if (!state.init) {
            state.init = true
        }
        state.Started = now()
        state.Intent=""
        state.SessionId=""
        state.VechicleId=""
    } catch (e) {
        warn "initialize() threw $e"
    }
}
def void updated() {
    trace "Entered <updated>"
    endRunning()
    unschedule()
    //debug "Update called settings: $settings"
    try {
        if (!state.init) {
            state.init = true
        }
        resumeProcessing()
    } catch (e) {
        warn "updated() threw $e"
    }
}
private errorsInSettings(){
	trace "Entered <errorsInSettings>"
    if(Username == "" || Username == null) {
        processError "Username cannot be empty"
        return true
    }
    if(Password == "" || Password == null) {
        processError "Password cannot be empty"
        return true
    }
    if(VehicleName == "" || VehicleName == null) {
        processError "VehicleName cannot be empty"
        return true
    }
    //if(location == "" || location == null) {
    //    processError "location cannot be empty"
    //    return true
    //}
    //if(location.timeZone == "" || location.timeZone == null) {
    //    processError "location.timeZone cannot be empty"
    //    return true
    //}	
}
private void send(String verb, String friendlyVerb=null){
    trace "Entered <Send>"
	
    updateTiles(["arm","disarm","remote","panic"])
    updateTile(verb,"sending")
    updateTile("command",verb,true,true)
    
    if(Trunk && verb !="trunk") updateTile("trunk")
    if(GPS && verb != "locate") updateTile("locate")
    
    if(errorsInSettings()) return
    trace "passed error check"
    
    initialize()
	
    ///
    if(friendlyVerb == null) friendlyVerb = verb
    updateStatus(friendlyVerb)

    ///
    state.Intent= verb

    trace "Logging in"    
    login()        
}

private void login(){
    trace "Entered <login>"
    debug "Building login URL"
    def url = getLoginUrl(Username,Password)
    // debug "the url is: "+ url

    def params = 
        [
            uri: url,
            contentType: 'application/json'
        ]

    webRequestInit(params,'loginResponse')
}
private void loginResponse(response, data){
    trace "Entered <loginResponse>"
    debug "Async Login Reply Received"

    if (response.hasError()) {
        if(response.status==401)
        processError("Authentication Failure, check SmartStart credentials")
        else	
            processError(response.errorMessage)
    } else {

        def results
        try {
            results = response.json
        } catch (e) {
            processError( "error parsing json from response: $e")
            return
        }

        if (results) {
            state.SessionId=results.Return.Results.SessionID
            debug "Received Session ID: "+state.SessionId
        } else {
            processError( "did not get json results from response body: $response.data")
            return
        }

        // Next
        getVehicleID()
    }
}

private void getVehicleID() {
    trace "Entered <getVehicleID>"
    debug "Building Get Vehicle URL"
    def Vehicles
    def VehicleId = ""
    def url = getVehicleIdUrl()
    debug "Get Vehicle URL: "+ url
    def params = 
        [
            uri: url,
            contentType: 'application/json'
        ]

    webRequestInit(params,'getVehicleIDResponse')
}
private void getVehicleIDResponse(response, data){
    trace "Entered <getVehicleIDResponse>"
    debug "Async VehicleID Reply Recived"
    if (response.hasError()) {
        processError( "response has error: $response.errorMessage")
    } else {
        def results
        try {
            results = response.json
        } catch (e) {
            processError( "error parsing json from response: $e")
            return
        }

        if (results) {
            //debug results.Return.Results.Devices
            def Vehicles=results.Return.Results.Devices
            def id= Vehicles.findIndexOf{ it-> it.Name.equals(VehicleName)}

            if(id == -1) {
                processError("Failed to find vehicle {"+VehicleName+"}. Check SmartStart vehicle name.")
                return
            }

            assert id != -1
            state.VechicleId = Vehicles[id].DeviceId

            //def actions
            //Vehicles[id].AvailActions.each{ it ->
            //	actions = actions+ it?.Name+", "
            //}
            //debug actions

            debug "Found Vehicle Id: " + state.VehicleId
            // return VehicleId
        } else {
            processError( "did not get json results from response body: $response.data")
            return
        }

        // Next
        sendIntent()
    }
}

private void sendIntent(){
    trace "Entered <sendIntent>"
    def url = getSendCommandUrl()
    debug "Send Action Url is ${url}"

    def params = 
        [
            uri: url,
            contentType: 'application/json'
        ]

    webRequestInit(params,'sendIntentResponse')
}
private void sendIntentResponse(response,data){
    trace "Entered <sendIntentResponse>"
    debug "Async Intent Reply Recived"
    if (response.hasError()) {
        processError( "response has error: $response.errorMessage")
    } else {

        def results
        try {
            results = response.json
        } catch (e) {
            processError( "error parsing json from response: $e")
            return
        }

        //debug results
        if(state.Intent == "locate")
        processLocateResponse(results)

        // Next
        intentComplete()
    }
}
private void processLocateResponse(results){
    trace "Entered <processLocateResponse>"
    debug results
    //state.LastLocateLat=results.Return.Results.Device.Latitude
    //state.LastLocateLon=results.Return.Results.Device.Longitude
    state.LastLocateAddress =results.Return.Results.Device.Address
}

private void webRequestInit(params, responseHandlerMethod){
    trace "Entered <webRequestInit>"
    try 
    {
        debug "Starting async httpGet"
        asynchttp_v1.get(responseHandlerMethod, params)
    }
    catch(java.lang.SecurityException e){
        processError("Endpoint is blacklisted or not responding,try again later")
    }
    catch (e) 
    {
        processError(e.message)
    }
}
private void processError(Error){
    trace "Entered <processError>"
    error Error
    state.Intent = "error"

    sendEvent(name:"command", value:"error",displayed:false, isStateChange: true)
    sendEvent(name: "info", value: "Error: $Error", displayed: true)

    runIn(5,intentComplete)
}

def void test(verb="locate"){
    //trace "Entered <test>"
    //info verb
    //locate
    //READ_ACTIVE
    //READ_CURRENT
    //send(verb)

    sendEvent(name: "info", value:"error: dd")
}
def void intentComplete(){
    trace "Entered <intentComplete>"
    info "Finalizing"

    sendEvent(name:"command", value:"default",displayed:false, isStateChange: true)

    switch (state.Intent) {
        case "arm":
        updateStatus("Last Action: Lock")
        sendEvent(name: "lock", value: "locked", displayed: false, isStateChange: true)
        break
        case "disarm":
        updateStatus("Last Action: Unlock")
        sendEvent(name: "lock", value: "unlocked", displayed: false, isStateChange: true)
        break
        case "remote":
        updateStatus("Last Action: Start/Stop")
        endRunning()
        unschedule()

        if(state.remoteType=="Start"){
            sendEvent(name: "sessionStatus", value: "running", displayed: false, isStateChange: true)
            sendEvent(name: "switch", value: "on", displayed: false, isStateChange: true)
            runIn(60*StartMinutes,endRunning)
        }

        state.remoteType = null
        break
        case "panic":
        updateStatus("Last Action: Panic")
        break
        case "trunk":
        updateStatus("Last Action: Trunk")
        break
        case "locate":
        updateStatus("Last Action: Locate "+state.LastLocateAddress)
        break
        case "error":
        break
        default:
            warn "no handling for intent with status $state.Intent"
        break
    }

    state.Started=0
    state.Intent=""
    state.SessionId=""
    state.VechicleId=""

    resumeProcessing()
}
def void resumeProcessing(){
    trace "Entered <resumeProcessing>"
    debug "resuming processing"
    updateTiles(["locate","trunk"],"NA")
    updateTiles(["remote","arm","disarm","panic"],"active")

    if(Trunk) updateTile("trunk", "active")
    if(GPS) updateTile("locate", "active")
}

def void endRunning(){
    trace "Entered <endRunning>"
    debug "endRunning"
    sendEvent(name: "sessionStatus", value: "stopped", displayed: false, isStateChange: true)
    sendEvent(name: "switch", value: "off", displayed: false, isStateChange: true)
}
private void updateStatus(String status){
    debug "Updating status"
    sendEvent(name: "info", value: status, displayed: false, isStateChange: true)
    sendEvent(name: "lastUpdate", value: formatLocalTime(), displayed: false, isStateChange: true)
}
private void updateTile(String name, String stateString ="inactive", boolean display = false, boolean stateChange = true){
    sendEvent(name:name, value:stateString,displayed:display, isStateChange: stateChange)
}
private void updateTiles(names, String stateString ="inactive", boolean display = false, boolean stateChange = true){
    names.each{name->
        updateTile(name,stateString,display,stateChange)
    }
}
private String formatLocalTime(time=now(), format = "EEE, MMM d yyyy @ h:mm a z") {
    trace "Entered <formatLocalTime>"
    
    if (time instanceof Long) {
        time = new Date(time)
    }
    if (time instanceof String) {
        //get UTC time
        time = timeToday(time, location.timeZone)
    }   
    if (!(time instanceof Date)) {
        return null
    }
    
    if(location == null || location.timeZone == null) {
    	warn "Location for hub not set"
        return time
    }
    
    def formatter = new java.text.SimpleDateFormat(format)
    formatter.setTimeZone(location.timeZone)
    return formatter.format(time)
}
private String encode(String value, String encoding ="UTF-8"){
    return URLEncoder.encode(value, encoding)
}

def void trace(message,Throwable e= null) {log(message,"trace",e)}
def void debug(message,Throwable e= null) {log(message,"debug",e)}
def void info(message,Throwable e= null) {log(message,"info",e)}
def void warn(message,Throwable e= null) {log(message,"warn",e)}
def void error(message,Throwable e= null) {log(message,"error",e)}
def void log(message, level = "trace",Throwable e= null) {
    if(LoggingLevel == debug && level == "trace") return
    if(LoggingLevel == info && (level == "trace" || level == "debug")) return
    if(LoggingLevel == warn && (level == "trace" || level == "debug" || level == "info")) return
    if(LoggingLevel == error && level != "error") return

    switch (level) {
        case "trace":
        if(e == null)
        log.trace(message)
        else
            log.trace(message,e)
        break;

        case "debug":
        if(e == null)
        log.debug(message)
        else
            log.debug(message,e)
        break;

        case "info":
        if(e == null)
        log.info (message)
        else
            log.info (message,e)
        break;

        case "warn":
        if(e == null)
        log.warn (message)
        else
            log.warn (message,e)
        break;

        case "error":
        if(e == null)
        log.error (message)
        else
            log.error (message,e)
        break;

        default:
            log.trace (message)
        break;
    }            
}