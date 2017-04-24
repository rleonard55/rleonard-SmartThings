/**
 *  Join Notifier
 *
 *  Copyright 2017 Rob Leonard
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
 
metadata {
	definition (name: "Join Notifier", namespace: "rleonard55", author: "Rob Leonard", iconUrl: "https://joaoapps.com/wp-content/uploads/2015/11/com.joaomgcd.join_-270x250.png") {
		command "push3", ["string", "string", "string"]
        command "push2", ["string", "string"]
        command "push1", ["string"]
        command "pushUrl", ["string"]
        command "pushClipboard", ["string"]
        command "pushFile", ["string"]
        command "doorbell"
        command "lock"
        command "unlock"
        command "playSound", ["string"]
        command "test"
        command "test2"
        
        capability "Tone"
        capability "Notification"
        capability "Speech Synthesis"
	}

	simulator {
		// TODO: define status and reply messages here
	}
    
	preferences {
        input "deviceId", "string", title: "Device Id", displayDuringSetup: true, required: true
        input "apiKey", "string", title: "API Key",displayDuringSetup: true, required: true 
        input "beepSound", "string", title:"Beep Sound", displayDuringSetup:false, required:false, defaultValue: "Door_Chime"
        input "doorBellSound", "string", title:"Doorbell Sound", displayDuringSetup:false, required:false, defaultValue: "Doorbell"
        input "testText", "string", title:"Test Text", displayDuringSetup:false, defaultValue: "Hello, how are you?"
	}

	tiles {
    	standardTile("speakTile", "device.speech", inactiveLabel: true, canChangeIcon: true) {
            state "inactive", label:'Speak', action:"Speech Synthesis.speak", icon:"st.Electronics.electronics16", nextState:"active"
            state "active", label:'Sending', icon:"st.Electronics.electronics16", backgroundColor: "#59ab46", nextState:"inactive"
        }
        standardTile("name", "device.name", inactiveLabel: false, decoration: "flat") {
        	state "inactive", label: "Tasker", action: "test", icon:"st.Seasonal Winter.seasonal-winter-014", nextState:"active" //, backgroundColor: "#59ab46"
			state "active", label: "Sending", icon:"st.Seasonal Winter.seasonal-winter-014", backgroundColor: "#59ab46", nextState:"inactive"
       }
        standardTile("toast", "device.notification", inactiveLabel: false, decoration: "flat") {
            state "inactive", label: "Notify", action: "test2",icon: "st.Office.office19", nextState:"active"
            state "active", label: "Sending",icon: "st.Office.office19",backgroundColor: "#59ab46", nextState:"inactive"
        }
        standardTile("beep", "device.tone", inactiveLabel: false, decoration: "flat") {
            state "inactive", label:'Tone', action:"tone.beep", icon:"st.Entertainment.entertainment2", nextState:"active"
            state "active", label: "Sending",icon:"st.Entertainment.entertainment2", backgroundColor: "#59ab46", nextState:"inactive"
        }
		standardTile("doorbell", "tone2", inactiveLabel: false, decoration: "flat") {
            state "inactive", label:'Doorbell', action:"doorbell", icon:"st.Electronics.electronics13", nextState:"active"
            state "active", label: "Sending",icon:"st.Electronics.electronics13", backgroundColor: "#59ab46", nextState:"inactive"
        }
        main "speakTile"
	}
}

def beep() {
    log.debug "Executing 'beep'"
    playSound(beepSound)
    sendEvent(name: "tone", value: "inactive",displayed: false,isStateChange: true)
}
def doorbell() {
    log.debug "Executing 'doorbell'"
    playSound(doorBellSound)
	sendEvent(name: "tone2", value: "inactive",displayed: false,isStateChange: true)
}
def lock() {
    log.debug "Executing 'doorbell'"
    playSound("Car_Lock")
}
def unlock() {
    log.debug "Executing 'doorbell'"
    playSound("Car_Unlock")
}

def playSound(track) {
 log.debug "Executing 'play'"
    push1("Play=:="+track)
}
def speak(String text) {
    log.debug "Executing 'speak'"
    
	//sendEvent(name: "speech", value: "speaking",displayed: false,isStateChange: true)

    def command="Say=:="+testText
    if(text!=null)
    	command="Say=:="+text
    push1(command)
    

	sendEvent(name: "speech", value: "inactive",displayed: false,isStateChange: true)
}

def deviceNotification() {
	log.debug "Executing 'deviceNotification'"
	// TODO: handle 'deviceNotification' command
}
def parse(String description) {
	log.debug "Parsing '${description}'"
    push1(description)
}
def deviceNotification(String text) {
	push2(text,"Notification")
}

def push() {
	test()
}
def push1(text) {
	def Map = [:]
	Map.Text=text
    join_push(getRequestUrl(Map))
}
def push2(text, title) {
	def Map = [:]
    Map.Text=text
    Map.Title=title
    
    join_push(getRequestUrl(Map))
}
def push3(text, title, iconUrl) {
	def Map = [:]
    Map.Text=text
    Map.Title=title
    Map.Icon = iconUrl
    join_push(getRequestUrl(Map))
}

def pushUrl(url) {
	def Map = [:]
	Map.URL=url

    join_push(getRequestUrl(Map))
}
def pushFile(Url) { 
	def Map = [:]
	Map.File=Url

    join_push(getRequestUrl(Map))
}
def pushClipboard(text){
	def Map = [:]
	Map.Clipboard=text.toString()

    join_push(getRequestUrl(Map))
}

private getDeviceId() {	
	return getDevicePreferenceByName(device, "deviceId") 
}
private getApiKey() {	
	return getDevicePreferenceByName(device, "apiKey") 
}
private getServerUrl() { 
return "https://joinjoaomgcd.appspot.com/_ah/api/messaging/v1/sendPush?" }
private getPresentRequire() {
	return getDevicePreferenceByName(device, "presentRequire") 
}

private getRequestUrl(JoinMsg) {
	def ReturnUrl="https://joinjoaomgcd.appspot.com/_ah/api/messaging/v1/sendPush?"
	def encoding = "UTF-8"
    
    if(JoinMsg.Text!=null)
    	ReturnUrl+="text="+URLEncoder.encode(JoinMsg.Text.toString(),encoding)+"&"
    
    if(JoinMsg.Title!=null)
    	ReturnUrl+="title=" + URLEncoder.encode(JoinMsg.Title.toString(), encoding)+"&"    
    
    if(JoinMsg.Icon!=null)
    	ReturnUrl+="icon="+URLEncoder.encode(JoinMsg.Icon.toString(), encoding)+"&" 
        
    if(JoinMsg.URL!=null)
    	ReturnUrl+="url="+URLEncoder.encode(JoinMsg.URL.toString(), encoding)+"&" 
    
    if(JoinMsg.Clipboard!=null)
    	ReturnUrl+="clipboard="+URLEncoder.encode(JoinMsg.Clipboard.toString(), encoding)+"&" 
    
    if(JoinMsg.File!=null)
    	ReturnUrl+="file="+JoinMsg.File+"&" 

    ReturnUrl+="deviceId="+getDeviceId().toString()+"&"
    ReturnUrl+="apikey="+getApiKey().toString()
    
    log.debug "URL: "+ReturnUrl
    return ReturnUrl
}
private join_push(url) {
	
 //   def p = presentRequire
 //   log.info p.events()
 //   if(presentRequire!= null)
 //   	if(presentRequire != "present")
 //   		return
            
    def Response
    httpGet(url) { resp -> Response = resp.data}

    if(Response.success == true)
		log.info "Success"
    else
        log.error ("Failed to send "+Response)
}

def installed() {
    log.debug "installed with settings: $settings"
    // subscribe to events, create scheduled jobs.
}
def uninstalled() {
    log.debug "uninstalled with settings: $settings"
    // external cleanup. No need to unsubscribe or remove scheduled jobs
}
def updated() {
   // log.debug "updated with settings: $settings"
   // unsubscribe()
    // resubscribe to device events, create scheduled jobs
}

private test() {
	log.debug "Executing 'test'"
    push1("Hello $location")
    
	sendEvent(name: "name", value: "inactive", displayed: false, isStateChange: true)
}
private test2() {
	log.debug "Executing 'test2'"
    push2("Hello", "$location")
    
    sendEvent(name: "notification", value: "inactive",displayed: false,isStateChange: true)
}