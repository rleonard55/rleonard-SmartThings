/**
 *  SHM To Button Linker
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
    name: "Sync SHM and Switch",
    namespace: "rleonard55",
    author: "Rob Leonard",
    description: "Links the status of the SHM to a switch.",
    category: "",
    iconUrl: "https://cdn4.iconfinder.com/data/icons/materia-security-vol-3/24/022_110_crypto_switch_commutator_security_lock_arrows-256.png",
    iconX2Url: "https://cdn4.iconfinder.com/data/icons/materia-security-vol-3/24/022_110_crypto_switch_commutator_security_lock_arrows-256.png",
    iconX3Url: "https://cdn4.iconfinder.com/data/icons/materia-security-vol-3/24/022_110_crypto_switch_commutator_security_lock_arrows-256.png")

preferences {
	section("Title") {
		input("MySwitch", "capability.switch", title: "Switches to Sync", multiple: false, required: true)
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	 subscribe(location,"alarmSystemStatus",alarmStatusHandler)
     subscribe(MySwitch, "switch.on", switchOnHandler)
     subscribe(MySwitch, "switch.off", switchOffHandler)
}

def alarmStatusHandler(event) {
	log.debug "Caught alarm status change: "+event.value
    if (event.value == "off") MySwitch.off()
    else if (event.value == "away")  MySwitch.on()
    else if (event.value == "stay") MySwitch.on()
}
def switchOnHandler (evt) {
	log.debug "Switch on handler raised: ${evt}"
    def armMode = 'stay'
	sendSHMEvent(armMode)
	execRoutine(armMode)
}
def switchOffHandler (evt) {
	log.debug "Switch off handler raised: ${evt}"
    def armMode = 'off'
	sendSHMEvent(armMode)
	execRoutine(armMode)
}
private sendSHMEvent(String shmState){
	def event = [name:"alarmSystemStatus", value: shmState, 
    			displayed: true, description: "System Status is ${shmState}"]
    sendLocationEvent(event)
}

private execRoutine(armMode) {
	if (armMode == 'away') location.helloHome?.execute(settings.armRoutine)
    else if (armMode == 'stay') location.helloHome?.execute(settings.stayRoutine)
    else if (armMode == 'off') location.helloHome?.execute(settings.disarmRoutine)    
}