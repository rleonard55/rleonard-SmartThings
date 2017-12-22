/*
 *  Copyright 2017 Rob Leonard
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy 
 *  of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 *  License for the specific language governing permissions and limitations 
 *  under the License.
 */

metadata {
    definition (name: "Simulated Presence Sensor -RL", namespace: "rleonard55", author: "Rob Leonard") {
        capability "Switch"
        capability "Refresh"
        capability "Presence Sensor"
		capability "Sensor"
        
		command "arrived"
		command "departed"
        command "setStateMessage"
         
        attribute "stateMessage", "string"
    }

	simulator {
		status "present": "presence: 1"
		status "not present": "presence: 0"
	}
    
    preferences {
        //input "apiKey", "text", title: "IFTTT API Key", required: false
        //input "makerOnEvent","text",title: "IFTTT On Event Name", required: false
        //input "makerOffEvent","text",title: "IFTTT Off Event Name", required: false
        input("DebugMsg", "bool", title: "Enable Debug Messages", defaultValue: false)
    }

	tiles(scale: 2) {
		standardTile("button", "device.switch", canChangeIcon: false,  canChangeBackground: false, width: 2, height: 2) {
			state "off", label: "Away", action: "switch.on", icon: "st.presence.tile.not-present", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: "Present", action: "switch.off", icon: "st.presence.tile.present", backgroundColor: "#00a0dc", nextState: "off"
		}
		standardTile("presence", "device.presence", width: 4, height: 4, canChangeIcon: false, canChangeBackground: true) {
			state("present", labelIcon:"st.presence.tile.mobile-present", action: "switch.off", nextState:"not present", backgroundColor:"#00a0dc")
			state("not present", labelIcon:"st.presence.tile.mobile-not-present", action: "switch.on", nextState:"present", backgroundColor:"#ffffff")
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat",width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("stateTitle", "device.stateTitle", inactiveLable: true, width:2, height:1, decoration: "flat", wordWrap: true) {
        	state "default", label: "Last Known Location:"
        }
		valueTile("stateMessage", "device.stateMessage", inactiveLabel: true, width: 4, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'${currentValue}'
        }
		main (["presence"])
		details(["presence","button", "refresh","stateTitle","stateMessage"])
	}
}

// def parse(String description) {
// def pair = description.split(":")
//	createEvent(name: pair[0].trim(), value: pair[1].trim())
//}

def arrived() {
	on()
}

def departed() {
    off()
}

def on() {
	debug("Inital value is : ${device.currentValue("presence")}")
    if(device.currentValue("presence") != "present") {
    	debug("Turning ON")
        sendEvent(name: "switch", value: "on", displayed: false)
        sendEvent(name: "presence", value: "present")
        setStateMessage("Home")
    }	
    debug("Final value is : ${device.currentValue("presence")}")
}

def off() {
    debug ("Inital value is : ${device.currentValue("presence")}")
	if(device.currentValue("presence") != "not present") {
    	debug ("Turning OFF")
        sendEvent(name: "switch", value: "off", displayed: false)
        sendEvent(name: "presence", value: "not present")
        setStateMessage("Away")
    }
    
     debug ("Final value is : ${device.currentValue("presence")}")
}


def debug(String message) {
	if(DebugMsg)
    	log.debug message
}

def parse(String description) {
	def name = parseName(description)
	def value = parseValue(description)
	def linkText = getLinkText(device)
	def descriptionText = parseDescriptionText(linkText, value, description)
	def handlerName = getState(value)
	def isStateChange = isStateChange(device, name, value)

	def results = [
    	translatable: true,
		name: name,
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: handlerName,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
	debug("Parse returned $results.descriptionText")
	return results

}

def setStateMessage(String message) {
    if(device.currentValue("stateMessage") != message) {
        sendEvent(name:"stateMessage", value: message, isStateChange: true)
    }
    //send(name: "stateMessage", value: message, isStateChange: true)//, descriptionText: "${device.displayName} has no current weather alerts")
}

private String parseName(String description) {
	if (description?.startsWith("presence: ")) {
		return "presence"
	}
	null
}

private String parseValue(String description) {
	switch(description) {
		case "presence: 1": return "present"
		case "presence: 0": return "not present"
		default: return description
	}
}

private parseDescriptionText(String linkText, String value, String description) {
	switch(value) {
		case "present": return "{{ linkText }} has arrived"
		case "not present": return "{{ linkText }} has left"
		default: return value
	}
}

private getState(String value) {
	switch(value) {
		case "present": return "arrived"
		case "not present": return "left"
		default: return value
	}
}

private sendIt(command) {
	def apiURL = "https://maker.ifttt.com/trigger/${command}/with/key/${apiKey}"
   // log.debug apiURL
    try {
         httpGet(apiURL) { resp ->
             log.debug "response data: ${resp.data}"
             log.debug "response contentType: ${resp.contentType}"
         }
        // log.debug("done")
	} catch (e) {
    	log.error "something went wrong: $e"
	}
}