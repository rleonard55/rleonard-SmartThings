// This is just a start, but I hope others will take interest and help make it better.
// It would be nice to add Locate and Current Status
// TODO: Needs state check implemented sense start/stop calls the same action on 
include 'asynchttp_v1'
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
        command "test"
        }
	
	preferences {
		input("Username", "string", title:"SmartStart Username", description: "Please enter your SmartStart Username", defaultValue: "user" , required: true, displayDuringSetup: true)
		input("Password", "password", title:"SmartStart Password", description: "Please enter your SmartStart Password", defaultValue: "password" , required: true, displayDuringSetup: true)
		input("VehicleName", "string", title:"SmartStart Vehicle Name", description: "Please enter your SmartStart Vehicle Name", defaultValue: "My Car" , required: true, displayDuringSetup: true)
        input("Debuging","bool", title: "Debugging Enabled?", description: "Enable Debuging?", defaultValue: true, required: true, displayDuringSetup: true)
    	input(name: "LoggingLevel", type: "enum", title: "Logging Level", options: ["trace","debug","info","warn","error"], defaultValue: info, required: true, displayDuringSetup: true)
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
        standardTile("lock", "device.lock", width:3, height:2){
            state "active", label: 'Lock', icon: "st.bmw.doors-locked", backgroundColor: "#ffffff", action: "lock", nextState:"sending"
            state "sending", label: 'Sending', backgroundColor: "#000000"
            state "inactive", label: 'Lock',icon: "st.bmw.doors-locked", backgroundColor: "#d3d3d3"
        }
        standardTile("unlock", "device.unlock", width:3, height:2){
            state "active", label: 'Unlock', icon: "st.bmw.doors-unlocked", backgroundColor: "#ffffff", action: "unlock", nextState:"sending"
            state "sending", label: 'Sending', backgroundColor: "#000000" 
            state "inactive", label: 'Unlock', icon: "st.bmw.doors-unlocked", backgroundColor: "#d3d3d3"
        }
        standardTile("start", "timedSession.start", width:3, height:2){
            state "active", label: 'Start', icon: "st.samsung.da.RC_ic_power", backgroundColor: "#d5fdd5", action: "start", nextState:"sending"
            state "sending", label: 'Sending', backgroundColor: "#000000"
            state "inactive", label: 'Start', icon: "st.samsung.da.RC_ic_power", backgroundColor: "#d3d3d3"
        }
        standardTile("trunk", "trunk", width:3, height:2){
            state "active", label: 'Trunk', icon: "st.bmw.trunk_open", backgroundColor: "#ffffff", action: "trunk", nextState:"sending"
            state "sending", label: 'Sending', backgroundColor: "#000000"
            state "inactive", label: 'Trunk', icon: "st.bmw.trunk_open", backgroundColor: "#d3d3d3"
        }        
        standardTile("panic", "panic", width:3, height: 2){
            state "active", label: 'Panic', icon: "st.Office.office6", backgroundColor: "#ff9999", action: "panic", nextState:"sending"
            state "sending", label: 'Sending', backgroundColor: "#000000"
            state "inactive", label: 'Panic', icon: "st.Office.office6", backgroundColor: "#d3d3d3"
        }
        standardTile("Test","Test", width:3, height:2){
        	state "active", label: 'Test', icon: "st.Office.office6", backgroundColor: "#ff9999", action: "test"
        }
		main (["lock"])
	}
}

def getServerUrl() { return "https://colt.calamp-ts.com" }
def getLoginUrl(user, pass) { return getServerUrl()+"/auth/login/${user}/${pass}"}
def getVehicleIdUrl() { return getServerUrl()+"/device/advancedsearch?sessid=${state.SessionId}"}
def getSendCommandUrl(){return getServerUrl()+"/device/sendcommand/${state.VechicleId}/${state.Intent}?sessid=${state.SessionId}"}

private getUsername() {	getDevicePreferenceByName(device, "Username") }
private getPassword() {	getDevicePreferenceByName(device, "Password") }
private getVechicleName() { getDevicePreferenceByName(device, "VehicleName") }	
//	private getDebugingEnabled() {	getDevicePreferenceByName(device, "Debuging") }
private getLoglevel() { getDevicePreferenceByName(device,"LoggingLevel") }

def lock() {
    info "Received Lock Request"
    sendEvent(name:"lock", value:"sending",displayed:true, isStateChange: true)
    sendEvent(name:"unlock", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"start", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"trunk", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"panic", value:"inactive",displayed:true, isStateChange: true)
    send("arm")
}
def unlock() {
	info "Received Unlock Request"
    sendEvent(name:"lock", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"unlock", value:"sending",displayed:true, isStateChange: true)
    sendEvent(name:"start", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"trunk", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"panic", value:"inactive",displayed:true, isStateChange: true)
	send("disarm")
}

def on() {
	start()
}
def off() {
	stop()
}

def start() {
	info "Received Start Request"
    sendEvent(name:"lock", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"unlock", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"start", value:"sending",displayed:true, isStateChange: true)
    sendEvent(name:"trunk", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"panic", value:"inactive",displayed:true, isStateChange: true)
	send("remote")
}
def stop() {
	info "Received Stop Request"
    sendEvent(name:"lock", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"unlock", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"start", value:"sending",displayed:true, isStateChange: true)
    sendEvent(name:"trunk", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"panic", value:"inactive",displayed:true, isStateChange: true)
	send("remote")
}

def panic(){
	info "Received Panic Request"
    sendEvent(name:"lock", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"unlock", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"start", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"trunk", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"panic", value:"sending",displayed:true, isStateChange: true)
	send("panic")
}
def trunk(){
	info "Received Trunk Open Request"
    sendEvent(name:"lock", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"unlock", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"start", value:"inactive",displayed:true, isStateChange: true)
    sendEvent(name:"trunk", value:"sending",displayed:true, isStateChange: true)
    sendEvent(name:"panic", value:"inactive",displayed:true, isStateChange: true)
	send("trunk")
}

private send(Action){
    try{
    	initialize()
    	state.Intent=Action
        debug "Logging in"
        login()        
    }
     catch(e){
    	log.error "$e"
    	sendEvent(name: "info", value: "$e", displayed: true)
        return
    }
}

private login(){
        debug "Building login URL"
        def url = getLoginUrl(getUsername(),getPassword())
        debug "the url is: "+ url
        
        def params = 
        [
        	uri: url,
        	contentType: 'application/json'
    	]
        
        WebRequestInit(params,'LoginResponse')
}
private LoginResponse(response, data){
    debug "Async Login Reply Received"
    if (response.hasError()) {
        //log.error "response has error: $response.errorMessage"
        if(response.status==401)
        	ProcessError("Authentication Failure, check credentials")
        else	
            ProcessError(response.errorMessage)
    } else {
        
        def results
        try {
            results = response.json
        } catch (e) {
            error "error parsing json from response: $e"
            return
        }
        
        if (results) {
        	state.SessionId=results.Return.Results.SessionID
        	debug "Received Session ID: "+state.SessionId
        } else {
            error "did not get json results from response body: $response.data"
            return
        }
        
        // Next
        GetVehicleID()
    }
}

private GetVehicleID() {
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

    WebRequestInit(params,'GetVehicleIDResponse')
}
private GetVehicleIDResponse(response, data){
    debug "Async VehicleID Reply Recived"
    if (response.hasError()) {
        error "response has error: $response.errorMessage"
    } else {
        def results
        try {
            results = response.json
        } catch (e) {
            error "error parsing json from response: $e"
            return
        }

        if (results) {
            //log.debug results.Return.Results.Devices
            def Vehicles=results.Return.Results.Devices
            def id= Vehicles.findIndexOf{ it-> it.Name.equals(getVechicleName())}

            if(id == -1) {
                error("Failed to find vehicle")
                return
            }

            assert id != -1
            state.VechicleId = Vehicles[id].DeviceId
            
            //def actions
            //Vehicles[id].AvailActions.each{ it ->
            //	actions = actions+ it?.Name+", "
            //}
            //log.debug actions
            
            debug "Returning Vehicle Id: " + state.VehicleId
            // return VehicleId
        } else {
            error "did not get json results from response body: $response.data"
            return
        }
        
        // Next
        SendIntent()
    }
}

private SendIntent(){
    def url = getSendCommandUrl()
    debug "Send Action Url is ${url}"

    def params = 
        [
            uri: url,
            contentType: 'application/json'
        ]

    WebRequestInit(params,'SendIntentResponse')
}
private SendIntentResponse(response,data){
    debug "Async Intent Reply Recived"
    if (response.hasError()) {
        error "response has error: $response.errorMessage"
    } else {

        def results
        try {
            results = response.json
        } catch (e) {
            error "error parsing json from response: $e"
            return
        }
        
        debug results
        debug "Sending "+state.Intent+ " to vehicle"
        // Next
        IntentComplete()
    }
}
private IntentComplete(){

    info "Finalizing"
    sendEvent(name:"info", value: "default",displayed:true, isStateChange: true)
    
    switch (state.Intent) {
        case "arm":
        	sendEvent(name:"lock", value:"active",displayed:true, isStateChange: true)
            sendEvent(name: "info", value: "Last Action: Lock", displayed: false)
        	break
        case "disarm":
        	sendEvent(name:"unlock", value:"active",displayed:true, isStateChange: true)
            sendEvent(name: "info", value: "Last Action: Unlock", displayed: false)
            break
        case "remote":
        	sendEvent(name:"start",value:"active",displayed:true, isStateChange: true)
            sendEvent(name: "info", value: "Last Action: Start/Stop", displayed: false)
            break
        case "panic":
        	 sendEvent(name:"panic",value:"active",displayed:true, isStateChange: true)
             sendEvent(name: "info", value: "Last Action: Panic", displayed: false)
        break
        case "trunk":
        	sendEvent(name:"trunk",value:"active",displayed:true, isStateChange: true)
            sendEvent(name: "info", value: "Last Action: Trunk", displayed: false)
        	break
        default:
            warn "no handling for intent with status $state.Intent"
        break
    }
    state.Started=0
    state.Intent=""
    state.SessionId=""
    state.VechicleId=""
    sendEvent(name: "lastUpdate", value: formatLocalTime(now()), displayed: false)
    resumeProcessing()
}

def test(verb="locate"){
	info verb
	//locate
    //READ_ACTIVE
    //READ_CURRENT
	send(verb)
}
def resumeProcessing(){
	debug "resuming processing"
    sendEvent(name:"start", value:"active",displayed:true, isStateChange: true)
	sendEvent(name:"lock", value:"active",displayed:true, isStateChange: true)
    sendEvent(name:"unlock", value:"active",displayed:true, isStateChange: true)
    sendEvent(name:"trunk", value:"active",displayed:true, isStateChange: true)
    sendEvent(name:"panic", value:"active",displayed:true, isStateChange: true)
}

private ProcessResponseStatus(response) {
	def status = response.status
    switch (status) {
        case 200:
            debug "200 returned"
            break
        case 304:
            debug "304 returned"
            break
        case 401:
        	error "Login failed, check credentials"
            break
        default:
            warn "no handling for response with status $status"
            break
    }
}
private WebRequestInit(params, responseHandlerMethod){

    try 
    {
        debug "Starting async httpGet"
        asynchttp_v1.get(responseHandlerMethod, params)
    } catch (e) 
    {
        ProcessError(e.message)
    }
}
private formatLocalTime(time, format = "EEE, MMM d yyyy @ h:mm a z") {
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
	def formatter = new java.text.SimpleDateFormat(format)
	formatter.setTimeZone(location.timeZone)
	return formatter.format(time)
}
private ProcessError(Error){
	error Error
    IntentComplete()
    sendEvent(name: "info", value: "Error: $Error", displayed: true)
}
//private DebugLog(message){
//
//log.debug getDebugingEnabled()
//log.debug message
//log.debug "**********************************"
//    if (getDebugingEnabled()=='true'){
//        debug message
//    }
//}

def initialize() {
    trace "Initialize called"// settings: $settings"
    try {
        if (!state.init) {
            state.init = true
        }
        state.Started = now()
        state.Intent=""
        state.SessionId=""
        state.VechicleId=""
        //response(refresh()) // Get the updates
    } catch (e) {
        warn "updated() threw $e"
    }
}
def updated() {
    debug "Update called"// settings: $settings"
    try {
        if (!state.init) {
            state.init = true
        }
        //response(refresh()) // Get the updates
    } catch (e) {
        warn "updated() threw $e"
    }
}

def void trace(String message,Throwable e= null) {log(message,"trace",e)}
def void debug(String message,Throwable e= null) {log(message,"debug",e)}
def void info(String message,Throwable e= null) {log(message,"info",e)}
def void warn(String message,Throwable e= null) {log(message,"warn",e)}
def void error(String message,Throwable e= null) {log(message,"error",e)}
def void log(String message, level = "trace",Throwable e= null) {
	def logLevel = getLoglevel()
    if(loglevel == "debug" && level == "trace") return
	if(loglevel == "info" && (level == "trace" || level == "debug")) return
    if(loglevel == "warn" && (level == "trace" || level == "debug" || level == "info")) return
    if(loglevel == "error" && level != "error") return
    
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