/**
 *  Aqualink Switch Child Device
 *
 *  Author: Thomas Moore
 *
 *  Copyright 2019
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */

metadata {
    definition (name: "Aqualink Switch Child", namespace: "tcm0116/aqualink", author: "Thomas Moore") {
        capability "Actuator"
        capability "Switch"

        attribute "status", "enum", ["off", "on", "delay"]
    }

    simulator {}

    tiles(scale: 2) {
        standardTile("switch", "device.status", width: 6, height: 1) {
            state "default", label:'unavailable', icon:"st.samsung.da.RC_ic_power", backgroundColor:"#cccccc", defaultState: true
            state "on", action:"off", label:'${name}', icon:"st.samsung.da.RC_ic_power", backgroundColor:"#00A0DC", nextState:"delay"
            state "off", action:"on", label:'${name}', icon:"st.samsung.da.RC_ic_power", backgroundColor:"#ffffff", nextState:"delay"
            state "delay", action:"off", label:'wait', icon:"st.samsung.da.RC_ic_power", backgroundColor:"#f0b823", nextState: "delay"
        }
        main(["switch"])
    }
}

def installed() {
    initialize()
}

def updated() {
    log.debug "${device.componentLabel}: updated()"
    initialize()
}

def initialize() {
    parent.subscribe(this, ["aqualinkd/${device.componentName}", "aqualinkd/${device.componentName}/delay"])
}

// Parse events from the parent device
def process(topic, value) {
    log.debug "topic = ${topic}, value = ${value}"

    if (topic == "aqualinkd/${device.componentName}/delay") {
        if (value == "1") {
            updateStatus("delay")
        }
        else if (value == "0") {
            updateStatus(device.currentValue('switch'))
        }
        else {
            log.debug "${device.componentLabel}: unexpected value '${value}' for topic '${topic}'"
        }
    }
    else if (topic == "aqualinkd/${device.componentName}") {
        if (value == "1") {
            sendEvent(name:"switch", value:"on")
            if (device.currentValue('status') != "delay")
                updateStatus("on")
        }
        else if (value == "0") {
            sendEvent(name:"switch", value:"off")
            if (device.currentValue('status') != "delay")
                updateStatus("off")
        }
        else {
            log.debug "${device.componentLabel}: unexpected value '${value}' for topic '${topic}'"
        }
    }
}

private updateStatus(status) {
    if (status != device.currentValue('status')) {
        sendEvent(name:"status", value:status)
        parent.switchStatus(device.componentLabel, status)
    }
}

def on() {
    parent.push("aqualinkd/${device.componentName}/set", "1")
}

def off() {
    parent.push("aqualinkd/${device.componentName}/set", "0")
}