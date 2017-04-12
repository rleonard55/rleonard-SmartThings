/**
 *  Copyright 2017 SmartThings
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
    definition (name: "IFTTT Maker Switch", namespace: "rleonard55", author: "Rob Leonard") {
		capability "Switch"
        preferences {
            //You must set up the Maker channel on IFTTT https://ifttt.com/maker
            //The channel has a single API Key per account
            //Each recipe requires a distinct event name    
            section("Send IFTTT alert"){
                input "apiKey", "text", title: "IFTTT API Key", required: true
                input "makerOnEvent","text",title: "IFTTT On Event Name", required: true
                input "makerOffEvent","text",title: "IFTTT Off Event Name", required: true
            } 
		} 
	}
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
		}
        main "switch"
        details("switch")
	}
}
def parse(description) {
}

def on() {
	log.debug "on()"
    SendIt(makerOnEvent)
    sendEvent(name: "switch", value: "on")
}
def off() {
	log.debug "off()"
	SendIt(makerOffEvent)
    sendEvent(name: "switch", value: "off")
}

def SendIt(command) {
	def apiURL = "https://maker.ifttt.com/trigger/${command}/with/key/${apiKey}"
   // log.debug apiURL
    try {
         httpGet(apiURL) { resp ->
             log.debug "response data: ${resp.data}"
             log.debug "response contentType: ${resp.contentType}"
         }
        // log.debug("done")
	} catch (e) {
    	log.debug "something went wrong: $e"
	}
}