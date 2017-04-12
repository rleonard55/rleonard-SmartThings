// This is just a start, but I hope others will take interest and help make it better.
// It would be nice to add a Trunk, Locate and Current Status
// TODO: Needs state check implemented sense start/stop calls the same action on 
metadata 
{
	definition(
        name: "Smart Start",
        namespace: "rleonard55",
        author: "Rob Leonard",	
        description: "Start/stop/arm/disarm vehicle",
        iconUrl:"http://dl.myket.ir/imageresizing/icon/small/com.directed.android.viper.png",
        iconX2Url:"http://www.fgcaraudio.com/viper_smart_start.png",
        singleInstance: true)
        {
		capability "timedSession"
		capability "lock"
        }
	
	preferences 
	{
		input("Username", "string", title:"SmartStart Username", description: "Please enter your SmartStart Username", defaultValue: "user" , required: true, displayDuringSetup: true)
		input("Password", "password", title:"SmartStart Password", description: "Please enter your SmartStart Password", defaultValue: "password" , required: true, displayDuringSetup: true)
		input("VehicleName", "string", title:"SmartStart Vehicle Name", description: "Please enter your SmartStart Vehicle Name", defaultValue: "My Car" , required: true, displayDuringSetup: true)
    }
    
	simulator { }

	tiles 
	{
		standardTile("button2", "device.timedSession", width: 2, height: 2, canChangeIcon: true) 
		{
			state "on", label: 'Start', action: "timedSession.start", icon: "st.Kids.kid10", backgroundColor: "#ffffff", nextState: "off"
			state "off", label: 'Stop', action: "timedSession.stop", icon: "st.Kids.kid10", backgroundColor: "#79b821", nextState: "on"
		}
        	standardTile("button", "device.lock", width: 2, height: 2, canChangeIcon: true) 
		{
			state "on", label: 'Lock', action: "lock.lock", icon: "st.Kids.kid10", backgroundColor: "#ffffff", nextState: "off"
			state "off", label: 'Unlock', action: "lock.unlock", icon: "st.Kids.kid10", backgroundColor: "#79b821", nextState: "on"
		}
	}
}

def getServerUrl() { return "https://colt.calamp-ts.com" }
def getLoginUrl(user, pass) { return getServerUrl()+"/auth/login/${user}/${pass}"}
def getVehicleIdUrl(sessionId) { return getServerUrl()+"/device/advancedsearch?sessid=${sessionId}"}
def getSendCommandUrl(vehicleId,action,sessionId){return getServerUrl()+"/device/sendcommand/${vehicleId}/${action}?sessid=${sessionId}"}

private getUsername() {	getDevicePreferenceByName(device, "Username") }
private getPassword() {	getDevicePreferenceByName(device, "Password") }
private getVechicleName() { getDevicePreferenceByName(device, "VehicleName") }	

def lock() 
{
	log.info "Received Locking Request"
	send("arm")
}

def unlock() 
{
	log.info "Received Unlocking Request"
	send("disarm")
}

def start()
{
	log.info "Received Starting Request"
	send("remote")
}

def stop()
{
	log.info "Received Stoping Request"
	send("remote")
}

private send (Action)
{	
	def SessionKey = ""
	def VechicleId = ""
    
	log.debug "Logging in"
	SessionKey = login()
	log.debug "Success! => Logged in successfully!"

	log.debug "Finding vehicle "+ getVechicleName() 
	VechicleId = GetVehicleID(SessionKey)
	log.debug "Success! => Vechicle Id found successfully!"
    
	log.debug "Sending "+ Action+ " to "+ VechicleId + " with session key " + SessionKey
	SendAction(VechicleId, Action, SessionKey)
}

private SendAction(VechicleId, Action, SessionId)
{
	def url = getSendCommandUrl(VechicleId, Action, SessionId)
	log.debug "Send Action Url is "+ url
    
	httpGet(url) { resp -> log.debug resp.data }
}

private GetVehicleID(SessionId)
{
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

private login()
{
	log.debug "Building login URL"
	def SessionKey = ""
	def url = getLoginUrl(getUsername(),getPassword())
	log.debug "the url is: "+ url
    
	httpGet(url) { resp -> SessionKey = resp.data.Return.Results.SessionID }
	log.debug "Received Session Key: " + SessionKey
	return SessionKey
}
