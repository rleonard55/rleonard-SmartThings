// This is just a start, but I hope others will take interest and help make it better.
// It would be nice to add a Trunk, Locate and Current Status
// TODO: Needs state check implemented sense start/stop calls the same action on 
metadata {
	definition(
        name: "Smart Start",
        namespace: "rleonard55",
        author: "Rob Leonard",	
        description: "Start/stop/arm/disarm/panic vehicle",
        singleInstance: false)
        {
		capability "timedSession"
		capability "lock"
		
        command "panic"
        command "trunk"
        command "test"
        }
	
	preferences {
		input("Username", "string", title:"SmartStart Username", description: "Please enter your SmartStart Username", defaultValue: "user" , required: true, displayDuringSetup: true)
		input("Password", "password", title:"SmartStart Password", description: "Please enter your SmartStart Password", defaultValue: "password" , required: true, displayDuringSetup: true)
		input("VehicleName", "string", title:"SmartStart Vehicle Name", description: "Please enter your SmartStart Vehicle Name", defaultValue: "My Car" , required: true, displayDuringSetup: true)
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
            state "active", label: 'Lock', icon: "st.bmw.doors-locked", backgroundColor: "#ffffff", action: "lock", nextState:"inactive"
            state "inactive", label: 'Sending',icon: "st.bmw.doors-locked", backgroundColor: "#d3d3d3", nextState:"active"
        }
        standardTile("unlock", "device.unlock", width:3, height:2){
            state "active", label: 'Unlock', icon: "st.bmw.doors-unlocked", backgroundColor: "#ffffff", action: "unlock", nextState:"inactive"
            state "inactive", label: 'Sending',icon: "st.bmw.doors-unlocked", backgroundColor: "#d3d3d3", nextState:"active"
        }
        standardTile("start", "timedSession.start", width:3, height:2){
            state "active", label: 'Start', icon: "st.samsung.da.RC_ic_power", backgroundColor: "#d5fdd5", action: "start", nextState:"inactive"
            state "inactive", label: 'Sending',icon: "st.samsung.da.RC_ic_power", backgroundColor: "#d3d3d3", nextState:"active"
        }
        standardTile("trunk", "trunk", width:3, height:2){
            state "active", label: 'Trunk', icon: "st.bmw.trunk_open", backgroundColor: "#ffffff", action: "lock", nextState:"inactive"
            state "inactive", label: 'Sending',icon: "st.bmw.trunk_open", backgroundColor: "#d3d3d3", nextState:"active"
        }
        standardTile("panic", "panic", width:3, height: 2){
            state "active", label: 'Panic', icon: "st.Office.office6", backgroundColor: "#ff9999", action: "panic", nextState:"inactive"
            state "inactive", label: 'Sending',icon: "st.Office.office6", backgroundColor: "#d3d3d3", nextState:"active"
        }

		main (["lock"])
	}
}

def getServerUrl() { return "https://colt.calamp-ts.com" }
def getLoginUrl(user, pass) { return getServerUrl()+"/auth/login/${user}/${pass}"}
def getVehicleIdUrl(sessionId) { return getServerUrl()+"/device/advancedsearch?sessid=${sessionId}"}
def getSendCommandUrl(vehicleId,action,sessionId){return getServerUrl()+"/device/sendcommand/${vehicleId}/${action}?sessid=${sessionId}"}

private getUsername() {	getDevicePreferenceByName(device, "Username") }
private getPassword() {	getDevicePreferenceByName(device, "Password") }
private getVechicleName() { getDevicePreferenceByName(device, "VehicleName") }	

def lock() {
    log.info "Received Locking Request"
    send("arm")

    sendEvent(name:"lock", value:"active",displayed:true, isStateChange: true)
}

def unlock() {
	log.info "Received Unlocking Request"
	send("disarm")
    
    sendEvent(name:"unlock", value:"active",displayed:true, isStateChange: true)
}
def start() {
	log.info "Received Starting Request"
	send("remote")
    
    sendEvent(name:"start",value:"active",displayed:true, isStateChange: true)
}
def stop() {
	log.info "Received Stoping Request"
	send("remote")
    
    sendEvent(name:"start",value:"active",displayed:true, isStateChange: true)
}
def panic(){
	log.info "Received Panic Request"
	send("panic")
    
    sendEvent(name:"panic",value:"active",displayed:true, isStateChange: true)
}
def trunk(){
	log.info "Received Trunk Open Request"
	send("trunk")
    
    sendEvent(name:"trunk",value:"active",displayed:true, isStateChange: true)
}

def test(verb="READ_CURRENT"){
	//locate
    //READ_ACTIVE
    //READ_CURRENT
	send(verb)
}

private login() {
        log.debug "Building login URL"
        def SessionKey = ""
        def url = getLoginUrl(getUsername(),getPassword())
        log.debug "the url is: "+ url

        httpGet(url) { resp -> SessionKey = resp.data.Return.Results.SessionID }

        log.debug "Received Session Key: " + SessionKey
        return SessionKey
}
private GetVehicleID(SessionId) {
    log.debug "Building Get Vehicle URL"
    def Vehicles
    def VehicleId = ""
    def url = getVehicleIdUrl(SessionId)
    log.debug "Get Vehicle URL: "+ url

    httpGet(url) { resp -> Vehicles = resp.data.Return.Results.Devices }
    def id= Vehicles.findIndexOf{ it-> it.Name.equals(getVechicleName())}

    if(id == -1)
    	log.error("Failed to find vehicle")
    assert id != -1

    VehicleId = Vehicles[id].DeviceId
    log.debug "Returning Vehicle Id: " + VehicleId
    return VehicleId
}
private send (Action) {	
	def SessionKey = ""
	def VechicleId = ""
    try {	
        log.debug "Logging in"
        SessionKey = login()
        log.debug "Success! => Logged in successfully!"

        log.debug "Finding vehicle "+ getVechicleName() 
        VechicleId = GetVehicleID(SessionKey)
        log.debug "Success! => Vechicle Id found successfully!"

        log.debug "Sending "+ Action+ " to "+ VechicleId + " with session key " + SessionKey
        SendAction(VechicleId, Action, SessionKey)

        sendEvent(name: "info", value: "Last Action: $Action", displayed: false)
    }
    catch(e){
    	log.error "$e"
    	sendEvent(name: "info", value: "$e", displayed: true)	
    }
    
	sendEvent(name: "lastUpdate", value: formatLocalTime(now()), displayed: false)
}
private SendAction(VechicleId, Action, SessionId) {
	def url = getSendCommandUrl(VechicleId, Action, SessionId)
	log.debug "Send Action Url is ${url}"
    
	httpGet(url) { resp -> log.debug resp.data }
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
