/**
 *  Synced Simulated Presence Sensor
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
    name: "Synced Simulated Presence",
    namespace: "rleonard55",
    author: "Rob Leonard",
    description: "syncs a simulated presence sensor with mobile presence sensors",
    category: "Convenience",
    iconUrl: "http://icons.iconarchive.com/icons/ampeross/qetto-2/128/sync-icon.png",
    iconX2Url: "http://icons.iconarchive.com/icons/ampeross/qetto-2/128/sync-icon.png",
    iconX3Url: "http://icons.iconarchive.com/icons/ampeross/qetto-2/128/sync-icon.png")


preferences {
		input("presenceSensors", "capability.presenceSensor", title: "Presence sensor(s) to sync with", multiple: true, required: false)
        input("SimulatedPresenceSensor", "device.SimulatedPresenceSensor", title: "Slave (Simulated) Device", multiple: false, required: true)
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
}
def initialize() {
	log.debug "Initializing with settings: ${settings}"
    runEvery10Minutes(OnPresenceDepart)
    subscribe(presenceSensors,"presence.present",OnPresenceArrive)
    subscribe(presenceSensors,"presence.not present",OnPresenceDepart)
    
    OnPresenceDepart(null)
}

def OnPresenceArrive(evt) {
	log.debug "Running 'OnPresenceArrive'"
	
    if(SimulatedPresenceSensor.currentValue("presence") != "present")
    	SimulatedPresenceSensor.arrived()
}
def OnPresenceDepart(evt) {
	log.debug "Running 'OnPresenceDepart'"

    def result = presenceSensors.any {p -> 
    	p.currentValue("presence") == "present" }
	
    if(result && SimulatedPresenceSensor.currentValue("presence") != "present")
    	SimulatedPresenceSensor.arrived()
    else if(!result && SimulatedPresenceSensor.currentValue("presence") == "present")
    	SimulatedPresenceSensor.departed()
}