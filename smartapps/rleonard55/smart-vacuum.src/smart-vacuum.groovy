/**
*  Smart Vacuum
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
definition(
    name: "Smart Vacuum",
    namespace: "rleonard55",
    author: "Rob Leonard",
    description: "Vacuums the floor while you are away.",
    category: "",
    iconUrl: "http://cdn.device-icons.smartthings.com/samsung/da/RC_ic_rc.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/samsung/da/RC_ic_rc@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/samsung/da/RC_ic_rc@2x.png")

preferences {
	page(name: "settings")
}

def settings() {
    dynamicPage(name: "settings", title: "Turn switches off after some minutes", uninstall: true, install: true) {
        section("When everyone departs..."){
            input "MyPresenceSensors", "capability.presenceSensor", title: "Presence Sensor(s)", multiple: true, required: true
        }
        section("Start the Vacuum(s)") {
        	input "MyVacuums", "capability.timedSession", title: "Vacuum(s)", multiple: true, required: true
            //input "MyVacuums", "capability.switch", title: "Vacuum(s)", multiple: true, required: true
        }
        section("Between These Times...") {
        	input "startingX", "enum", title: "Only between these times", options: ["A specific time", "Sunrise", "Sunset"], submitOnChange: true, required: false
            if(startingX == "A specific time") 
            	input "starting", "time", title: "Start time", required: true
            else if(startingX == "Sunrise") 
                input "startSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: true, defaultValue: 0
            else if(startingX == "Sunset") 
            	input "startSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: true, defaultValue: 0
        	if(startingX != null) {
            	input "endingX", "enum", title: "Ending at", options: ["A specific time", "Sunrise", "Sunset"], submitOnChange: true, required: true
           		if(endingX == "A specific time") 
            		input "ending", "time", title: "End time", required: true
            	else if(endingX == "Sunrise") 
            		input "endSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: true, defaultValue: 0
            	else if (endingX == "Sunset") 
            		input "endSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: true, defaultValue: 0 
        	}
		}
        section("On These Day(s)...") {
        	input "Days", "enum", title: "Only on these days", options:["Sunday","Monday","Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], multiple:true, required: false
        }
        section("Additional Settings") {
        	input "dustbinReminder", "bool", title: "Remind me to empty the vacuum's dustbin", defaultValue: true
            input "oncePerDay", "bool", title: "Only once per day", defaultValue: true
            input "stopOnArrive", "bool", title: "Stop vacuum(s) on arrival", defaultValue: false
        	input "modes", "mode", title: "Only when mode is", multiple: true, required: false
            input "waitMin", "number", title: "Minutes to wait after departure",range: "0..60", required: true, defaultValue: 15
        }
	}
}
def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}
def updated() {

	log.debug "Updated with settings: ${settings}"
	unsubscribe()
    unschedule()
    
	initialize()
    precheck()
}
def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(MyVacuums, "switch.on", OnSwitchedOn)
    subscribe(MyPresenceSensors, "presence.present", OnPresenceArrive)
    subscribe(MyPresenceSensors, "presence.not present", OnPresenceDepart)
}

def OnPresenceDepart(evt) {
	log.debug "Running 'OnPresenceDepart'"
    precheck()
}
def OnPresenceArrive(evt) {
	log.debug "Running 'OnPresenceArrive'"
    
    unschedule()

    def result = MyVacuums.any {v -> v.currentValue("operationState")=="cleaning" }
    if(stopOnArrive && result)
    	MyVacuums.cancel()

	log.debug "result: ${result}"
    log.debug "dustbinReminder: ${dustbinReminder}"
    log.debug "todayOk(): ${todayOk()}"
    
   if(!result && dustbinReminder && todayOk())
		sendPush("Remember to empty the vacuum's dustbin!")
}
def OnSwitchedOn(evt) {	
    log.debug "Vacuum changed to 'On'"
	state.LastRan = now()
}

private midnight() {
	return timeToday("00:00", location.timeZone).time
}
private precheck() {
	log.debug "Running 'precheck'"
    
	if(!timeOk())
    	return
    if(!dayOk())
    	return
    if(!modeOk())
    	return
	if(!todayOk())
     	return
        
	log.debug "All OK, Scheduleing a vacuum in ${waitMin} minutes"
    runIn(waitMin*60, runIt)
}

private modeOk() {
	log.debug "Running 'modeOk'"
	if(modes == null) return true
	def result = !modes || modes.contains(location.mode)
	return result
}
private timeOk() {
	log.debug "Running timeOk"
    def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)
	def start = null
	def stop = null
    
    if(startingX =="A specific time" && starting!= null)
    	start = timeToday(starting,location.timeZone)
    if(endingX == "A specific time" && ending!= null)
        stop = timeToday(ending,location.timeZone)
        
    if(startingX == "Sunrise")
    	start = s.sunrise
     if(startingX == "Sunset")
    	start = s.sunset
     if(endingX == "Sunrise")  
      	stop = s.sunrise
     if(endingX == "Sunset")
     	stop = s.sunset
	
    log.debug "1) start: ${start} | stop: ${stop}"
    if(start == null || stop == null)
    	return true
    
     if(stop < start) 
     	stop = stop + 1
    
    log.debug "2) start: ${start} | stop: ${stop}"
    return timeOfDayIsBetween(start, stop, (new Date()), location.timeZone)
}
private dayOk() {
	if(Days == null) return true;
	return Days.contains(new Date().format("EEEE"))
}
private todayOk() {
	 if(oncePerDay) 
    	return (!(state.LastRan > midnight()))
    else return true
}
private runIt() {
	log.debug "Running 'Run'"
    
    if(!modeOk())
    	return
    
    def result = MyPresenceSensors.any {p -> p.currentValue("presence") == "present" }
    
    if(!result) {
    	MyVacuums.on()
    	//MyVacuums.each{v-> v.on() }
    }
}