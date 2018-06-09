/**
 *  Join Notifier
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
 */
include 'asynchttp_v1'
import groovy.json.JsonSlurper

metadata {
	definition (name: "Join Notifier 2", namespace: "rleonard55", author: "Rob Leonard", iconUrl: "https://joaoapps.com/wp-content/uploads/2015/11/com.joaomgcd.join_-270x250.png") {
        command "pushMap",[]
        command "pushText",["String"]	
        command "pushUrl", ["string"]
		command "pushClipboard", ["string"]
        command "pushJsonString", ["string"]
        command "pushNotification", ["string","string","string","string"]
        
        command "sendFile",["string"]
        command "sendText",["string","string","string"] 
        
        command "callNumber", ["string"]
	}

	simulator {
		// TODO: define status and reply messages here
	}

	preferences {
		input "deviceId", "string", title: "Device Id", displayDuringSetup: true, required: true
		input "apiKey", "string", title: "API Key",displayDuringSetup: true, required: true 
		input(name: "LoggingLevel", type: "enum", title: "Logging Level", options: ["trace","debug","info","warn","error"], defaultValue: info, required: true, displayDuringSetup: true)
	}
	
	tiles {
        standardTile("speakTile", "device.speech", inactiveLabel: true, canChangeIcon: true) {
            state "inactive", label:'Speak', action:"sendText", icon:"st.Electronics.electronics16", nextState:"active"
            state "active", label:'Sending', icon:"st.Electronics.electronics16", backgroundColor: "#59ab46", nextState:"inactive"
        }
	}
}

private getPossibleValues(){
	def myList = ["deviceNames","text","title","icon","smallicon","url","image","sound","group","clipboard","file","callnumber","smsnumber","smstext","mmsfile","wallpaper","lockWallpaper","interruptionFilter","mediaVolume","ringVolume","alarmVolume"]
    return myList
}

private getApiKey() {	
	return getDevicePreferenceByName(device, "apiKey") 
}
private getDeviceId() {	
	return getDevicePreferenceByName(device, "deviceId") 
}
private getLoglevel() { getDevicePreferenceByName(device,"LoggingLevel") }
private getServerUrl() { return "https://joinjoaomgcd.appspot.com/_ah/api/messaging/v1/sendPush?" }

def pushMap(Map JoinMap = null){
    trace "Entered <pushMap>"
    debug "Executing 'pushMap'"
    if(JoinMap == null)
   		sendMessage(testMap())
    else
    	sendMessage(JoinMap)
}
def pushJsonString(String data='{"url":"http://yahoo.com"}'){
    trace "Entered <pushJsonString>"
    debug "Executing 'pushJsonString' with data "+data
    
    if(data == null) {
    	ProcessError("Null string passed in")
        return
    }
    
    try{
    	def jsonData = new JsonSlurper().parseText(data)
        debug "Successfully processed string as json"
        pushMap(jsonData)
    }
    catch(e){
        ProcessError("Couldn't parse string as JSON. "+e)
        return
    }
    
}

def pushText(String data="test"){
	trace "Entered <pushMessage>"
	debug "Executing 'pushClipboard' with clipboard data:"+data
    
    def JoinMap = [:]
    JoinMap.text = data
    
    sendMessage(JoinMap)
}
def pushUrl(String url= "https://joaoapps.com/join/") {
	trace "Entered <pushUrl>"
    debug "Executing 'pushUrl'"
    
	def JoinMap = [:]
	JoinMap.url=url

    sendMessage(JoinMap)
}
def pushClipboard(String data="https://joaoapps.com/join/") {
	trace "Entered <pushClipboard>"
	debug "Executing 'pushClipboard' with clipboard data:"+data
    
    def JoinMap = [:]
    JoinMap.clipboard = data
    
    sendMessage(JoinMap)
}
def pushNotification(String title="Title", String text="Text",String icon="https://joaoapps.com/wp-content/uploads/2015/11/com.joaomgcd.join_-270x250.png", String smallicon ="https://joaoapps.com/wp-content/uploads/2015/11/com.joaomgcd.join_-270x250.png"){
    trace "Entered <pushNotification>"
    debug "Executing 'pushNotification'"

    def JoinMap = [:]
    JoinMap.title = title
    JoinMap.text = text
	JoinMap.icon = icon
    JoinMap.smallicon = smallicon
    sendMessage(JoinMap)
}

def callNumber(String phoneNumber){
	trace "Entered <callNumber>"
	debug "Executing 'callNumber' with phone number:"+phoneNumber
    
    if(phoneNumber == null) {
    	ProcessError("Phone number cannot be null")
        return
    }
    
    def JoinMap = [:]
    JoinMap.callnumber = phoneNumber
    
    sendMessage(JoinMap)
}
def sendFile(String fileUrl="https://joinjoaomgcd.appspot.com/images/join.png"){
    trace "Entered <sendFile>"
	debug "Executing 'sendFile' with file:"+fileUrl
    
    def JoinMap = [:]
    JoinMap.file = fileUrl
    
    sendMessage(JoinMap)
}
def sendText(String phoneNumber, String text="Test", String fileUrl="https://joaoapps.com/wp-content/uploads/2015/11/com.joaomgcd.join_-270x250.png"){
    trace "Entered <sendText>"
	debug "Executing 'sendText' \'"+text+" to "+phoneNumber+" and file ="+fileUrl
    
    if(phoneNumber == null) {
        ProcessError("Phone number cannot be null")
        return
    }
    
    def JoinMap = [:]
    JoinMap.smsnumber = phoneNumber
    JoinMap.smstext = text
    JoinMap.mmsfile = fileUrl
    sendMessage(JoinMap)
}

private sendMessage(Map JoinMap){
    trace "Entered <send>"
    debug "Building URLwith Map: "+JoinMap
    def url = getRequestUrl(JoinMap)
    debug "the url is: "+ url

    def params = 
        [
            uri: url,
            contentType: 'application/json'
        ]

    WebRequestInit(params,'processResponse')
}
private getRequestUrl(Map JoinMap) {
    trace "Entered <getRequestUrl> with Map: "+JoinMap
    def encoding = "UTF-8"
    def ReturnUrl = getServerUrl()   
    
    def values=getPossibleValues()
    debug "values: "+values
    values.each{
        //trace "Checking if $it was provided"
        def x = JoinMap["$it"]?.value
        //debug "$it value is "+x
        if(x!=null) {
            debug "adding $it"
            ReturnUrl+="$it="+URLEncoder.encode("$x", encoding)+"&"
        }
    }

    ReturnUrl+="deviceId="+getDeviceId().toString()+"&"
    ReturnUrl+="apikey="+getApiKey().toString()
    debug "URL: "+ReturnUrl
    return ReturnUrl
}
private WebRequestInit(params, responseHandlerMethod){
    trace "Entered <WebRequestInit>"
    debug "Parms: "+params+" Method: "+responseHandlerMethod
    try 
    {
        debug "Starting async httpGet"
        asynchttp_v1.get(responseHandlerMethod, params)
    } 
    catch (e) 
    {
        ProcessError(e.message)
    }
}
private processResponse(response, data) {
	trace "Entered <loginResponse>"
    debug "Async Login Reply Received"
    if (response.hasError()) {
    	ProcessError("response error: "+response.status)
	}
	else 
    {
		debug "Sent Successfully"
	}
}

private testMap(){
	def JoinMap = [:]
	JoinMap.url="https://google.com"
    return JoinMap
}
private ProcessError(Error){
	trace "Entered <ProcessError>"
	error Error
    //IntentComplete()
    //sendEvent(name: "info", value: "Error: $Error", displayed: true)
}

def void trace(message,Throwable e= null) {log(message,"trace",e)}
def void debug(message,Throwable e= null) {log(message,"debug",e)}
def void info(message,Throwable e= null) {log(message,"info",e)}
def void warn(message,Throwable e= null) {log(message,"warn",e)}
def void error(message,Throwable e= null) {log(message,"error",e)}
def void log(message, level = "trace",Throwable e= null) {
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

def updated() {
    debug "updated with settings: $settings"

   // unsubscribe()
   // resubscribe to device events, create scheduled jobs
}
def installed() {
	debug "installed with settings: $settings"
	// subscribe to events, create scheduled jobs.
}
def uninstalled() {
    debug "uninstalled with settings: $settings"
    // external cleanup. No need to unsubscribe or remove scheduled jobs
}
def parse(String description) {
	debug "Parsing '${description}'"
    pushJsonString(description)
}