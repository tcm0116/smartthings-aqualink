/**
 *  Aqualink Pool Filter Pump Child Device
 *
 *  Author: Thomas Moore
 *
 *  Copyright 2018
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
    definition (name: "Aqualink Pool Filter Pump Child", namespace: "tcm0116/aqualink", author: "Thomas Moore") {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"

        attribute "speed", "number"
        attribute "power", "number"
    }

    simulator {}

    tiles(scale: 2) {
        standardTile("filterPump", "device.switch", width: 6, height: 1) {
            state "default", label:'OFFLINE', icon:"st.samsung.da.RC_ic_power", backgroundColor:"#cccccc", defaultState: true
            state "on", action:"switch.off", label:'${name}', icon:"st.samsung.da.RC_ic_power", backgroundColor:"#00A0DC", nextState:"turningOff"
            state "off", action:"switch.on", label:'${name}', icon:"st.samsung.da.RC_ic_power", backgroundColor:"#ffffff", nextState:"turningOn"
            state "turningOn", label:'${name}', icon:"st.samsung.da.RC_ic_power", backgroundColor:"#f0b823", nextState: "turningOn"
            state "turningOff", label:'${name}', icon:"st.samsung.da.RC_ic_power", backgroundColor:"#f0b823", nextState: "turningOff"
        }
        valueTile("filterPumpSpeed", "device.speed", width: 3, height: 1) {
            state "default", label:'${currentValue} ${unit}', unit:'RPM'
        }
        valueTile("filterPumpPower", "device.power", width: 3, height: 1) {
            state "default", label:'${currentValue} ${unit}', unit:'Watts'
        }
        /*
        multiAttributeTile(name:"filterPump", type:"generic", width: 2, height: 2, canChangeIcon: true) {
            tileAttribute ("device.switch", key:"PRIMARY_CONTROL", defaultValue:"off") {    
                attributeState "on", action:"switch.off", label:'${name}', icon:"st.Health & Wellness.health2", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", action:"switch.on", label:'${name}', icon:"st.Health & Wellness.health2", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', icon:"st.Health & Wellness.health2", backgroundColor:"#f0b823", nextState: "turningOn"
                attributeState "turningOff", label:'${name}', icon:"st.Health & Wellness.health2", backgroundColor:"#f0b823", nextState: "turningOff"
            }
            tileAttribute ("device.speed", key:"SECONDARY_CONTROL") {
                attributeState "speed", label:'Speed: ${currentValue} RPM'
            }
            tileAttribute ("device.power", key:"SECONDARY_CONTROL") {
                attributeState "power", label:'Power: ${currentValue} Watts'
            }
        }
        */
        main(["filterPump"])
        details(["filterPump", "filterPumpSpeed", "filterPumpPower"])
    }
}

def installed() {
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    parent.subscribe(this, ["aqualinkd/Filter_Pump", "aqualinkd/Filter_Pump/speed", "aqualinkd/Filter_Pump/power"])
}

// Parse events from the parent device
def process(topic, value) {
    if (topic == "aqualinkd/Filter_Pump") {
        if (value == "1")
            sendEvent(name:"switch", value:"on")
        else if (value == "0") {
            sendEvent(name:"switch", value:"off")
            sendEvent(name:"speed", value:null)
            sendEvent(name:"power", value:null)
        }
    }
    else if (topic == "aqualinkd/Filter_Pump/speed") {
        sendEvent(name:"speed", value:value)
    }
    else if (topic == "aqualinkd/Filter_Pump/power") {
        sendEvent(name:"power", value:value)
    }
}

def on() {
    parent.push("aqualinkd/Filter_Pump/set", "1")
}

def off() {
    parent.push("aqualinkd/Filter_Pump/set", "0")
}