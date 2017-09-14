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
    }

	simulator {
		status "present": "presence: 1"
		status "not present": "presence: 0"
	}
    
    preferences {
    	input("DebugMsg", "Bool", title: "Enable Debug Messages", defaultValue: false)
		// input("presenceSensors", "capability.presenceSensor", title: "Presence sensor(s) to sync with", multiple: true, required: false)
    }

	tiles {
		standardTile("button", "device.switch", canChangeIcon: false,  canChangeBackground: true) {
			state "off", label: 'Away', action: "switch.on", icon: "st.Kids.kid10", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Present', action: "switch.off", icon: "st.Kids.kid10", backgroundColor: "#00a0dc", nextState: "off"
		}
		standardTile("presence", "device.presence", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
			state("present", labelIcon:"st.presence.tile.mobile-present", action: "switch.off", nextState:"not present", backgroundColor:"#00a0dc")
			state("not present", labelIcon:"st.presence.tile.mobile-not-present", action: "switch.on", nextState:"present", backgroundColor:"#ffffff")
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["presence"])
		details(["presence","button", "refresh"])
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
    }	
    debug("Final value is : ${device.currentValue("presence")}")
}

def off() {
    debug ("Inital value is : ${device.currentValue("presence")}")
	if(device.currentValue("presence") != "not present") {
    	debug ("Turning OFF")
        sendEvent(name: "switch", value: "off", displayed: false)
        sendEvent(name: "presence", value: "not present")
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