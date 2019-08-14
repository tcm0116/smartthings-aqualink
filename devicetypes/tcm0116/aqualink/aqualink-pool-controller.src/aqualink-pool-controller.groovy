/**
 *  Aqualink Pool Controller Device
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

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

metadata {
    definition (name: "Aqualink Pool Controller", namespace: "tcm0116/aqualink", author: "Thomas Moore") {
        capability "Actuator"
		capability "Refresh"
        
        attribute "mode", "enum", ["offline", "off", "pool", "spa", "clean", "service"]
        //attribute "status", "enum", ["offline", "online"]
    }

    preferences {
        input("ip", "string",
            title: "MQTT Proxy IP Address",
            description: "MQTT Proxy IP Address",
            required: true,
            displayDuringSetup: true
        )
        input("port", "string",
            title: "MQTT Proxy Port",
            description: "MQTT Proxy Port",
            required: true,
            displayDuringSetup: true
        )
        input("mac", "string",
            title: "MQTT Proxy MAC Address",
            description: "MQTT Proxy MAC Address",
            required: true,
            displayDuringSetup: true
        )
    }

    simulator {}

    tiles (scale: 2) {
        standardTile("mode", "device.mode", width: 6, height: 1, decoration: "flat") {
            state "offline", label:'${name}', icon:"st.Health & Wellness.health2", backgroundColor:"#cccccc", defaultState: true
            state "off", label:'${name}', icon:"st.Health & Wellness.health2", backgroundColor:"#ffffff"
            state "pool", label:'${name}', icon:"st.Health & Wellness.health2", backgroundColor:"#00A0DC"
            state "spa", label:'${name}', icon:"st.Health & Wellness.health2", backgroundColor:"#00A0DC"
            state "clean", label:'${name}', icon:"st.Health & Wellness.health2", backgroundColor:"#00A0DC"
            state "service", label:'${name}', icon:"st.Health & Wellness.health2", backgroundColor:"#cccccc"
        }
        childDeviceTile("airTemperature", "Air", width: 6, height: 1, childTileName: "temperature")
        //childDeviceTile("filterPump", "filterPump", width: 6, height: 1, childTileName: "filterPump")
        //childDeviceTile("filterPumpSpeed", "filterPump", width: 3, height: 1, childTileName: "filterPumpSpeed")
        //childDeviceTile("filterPumpPower", "filterPump", width: 3, height: 1, childTileName: "filterPumpPower")
        childDeviceTile("filterPump", "Filter_Pump", width: 6, height: 1, childTileName: "switch")
        childDeviceTile("spaMode", "Spa_Mode", width: 6, height: 1, childTileName: "switch")
        childDeviceTile("spaBlower", "Aux_2", width: 6, height: 1, childTileName: "switch")
        childDeviceTile("spillover", "Aux_3", width: 6, height: 1, childTileName: "switch")
        childDeviceTile("poolThermostatOperatingState", "poolHeater", width: 6, height: 1, childTileName: "thermostatOperatingState")
        //childDeviceTile("poolTemperature", "poolHeater", width: 2, height: 1, childTileName: "temperature")
        childDeviceTile("poolTemperature", "Pool", width: 2, height: 1, childTileName: "temperature")
        childDeviceTile("poolHeatingSetpointDown", "poolHeater", width: 1, height: 1, childTileName: "heatingSetpointDown")
        childDeviceTile("poolHeatingSetpoint", "poolHeater", width: 2, height: 1, childTileName: "heatingSetpoint")
        childDeviceTile("poolHeatingSetpointUp", "poolHeater", width: 1, height: 1, childTileName: "heatingSetpointUp")
        childDeviceTile("spaThermostatOperatingState", "spaHeater", width: 6, height: 1, childTileName: "thermostatOperatingState")
        //childDeviceTile("spaTemperature", "spaHeater", width: 2, height: 1, childTileName: "temperature")
        childDeviceTile("spaTemperature", "Spa", width: 2, height: 1, childTileName: "temperature")
        childDeviceTile("spaHeatingSetpointDown", "spaHeater", width: 1, height: 1, childTileName: "heatingSetpointDown")
        childDeviceTile("spaHeatingSetpoint", "spaHeater", width: 2, height: 1, childTileName: "heatingSetpoint")
        childDeviceTile("spaHeatingSetpointUp", "spaHeater", width: 1, height: 1, childTileName: "heatingSetpointUp")
        childDeviceTile("cleaner", "Aux_1", width: 6, height: 1, childTileName: "switch")
        standardTile("refresh", "device.refresh", width: 6, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        main "mode"
    }
}

def installed() {
    log.debug "installed()"
    state.subscriptions = [:]
    state.offline = true
    state.filterPump = false
    state.spaMode = false
    state.cleaner = false
    addChildDevices()
}

def addChildDevices() {
    if (state.childDevicesAdded)
        return
/*
    addChildDevice(
        "Aqualink Pool Filter Pump Child",
        "${device.deviceNetworkId}.1",
        null,
        [completedSetup: true, label: "${device.displayName} Filter Pump", componentName: "filterPump", componentLabel: "Filter Pump"])
*/
    addChildDevice(
        "Aqualink Switch Child",
        "${device.deviceNetworkId}.1",
        null,
        [completedSetup: true, label: "${device.displayName} Filter Pump", componentName: "Filter_Pump", componentLabel: "Filter Pump"])

    addChildDevice(
        "Aqualink Switch Child",
        "${device.deviceNetworkId}.2",
        null,
        [completedSetup: true, label: "${device.displayName} Spa Mode", componentName: "Spa_Mode", componentLabel: "Spa Mode"])

    addChildDevice(
        "Aqualink Switch Child",
        "${device.deviceNetworkId}.3",
        null,
        [completedSetup: true, label: "${device.displayName} Spa Blower", componentName: "Aux_2", componentLabel: "Spa Blower"])

    addChildDevice(
        "Aqualink Switch Child",
        "${device.deviceNetworkId}.4",
        null,
        [completedSetup: true, label: "${device.displayName} Spillover", componentName: "Aux_3", componentLabel: "Spillover"])

    addChildDevice(
        "Aqualink Heat Pump Child",
        "${device.deviceNetworkId}.5",
        null,
        [completedSetup: true, label: "${device.displayName} Heater", componentName: "poolHeater", componentLabel: "Pool Heater"])

    addChildDevice(
        "Aqualink Heat Pump Child",
        "${device.deviceNetworkId}.6",
        null,
        [completedSetup: true, label: "${device.displayName} Spa Heater", componentName: "spaHeater", componentLabel: "Spa Heater"])

    addChildDevice(
        "Aqualink Switch Child",
        "${device.deviceNetworkId}.7",
        null,
        [completedSetup: true, label: "${device.displayName} Cleaner", componentName: "Aux_1", componentLabel: "Cleaner"])

    addChildDevice(
        "Aqualink Temperature Sensor Child",
        "${device.deviceNetworkId}.8",
        null,
        [completedSetup: true, label: "${device.displayName} Air Temperature", componentName: "Air", componentLabel: "Air Temperature"])

    addChildDevice(
        "Aqualink Temperature Sensor Child",
        "${device.deviceNetworkId}.9",
        null,
        [completedSetup: true, label: "${device.displayName} Pool Temperature", componentName: "Pool", componentLabel: "Pool Temperature"])

    addChildDevice(
        "Aqualink Temperature Sensor Child",
        "${device.deviceNetworkId}.10",
        null,
        [completedSetup: true, label: "${device.displayName} Spa Temperature", componentName: "Spa", componentLabel: "Spa Temperature"])
    
    state.childDevicesAdded = true
}

def removeChildDevices() {
    childDevices.each {
        try {
            deleteChildDevice(it.deviceNetworkId)
        }
        catch (e) {
            log.debug "Error deleting ${it.deviceNetworkId}: ${e}"
        }
    }
}

def getChildDevice(label) {
    return childDevices.find { it.componentLabel == label }
}

def update() {    
    subscribe(null, ["aqualinkd/Alive", "aqualinkd/Service_Mode"/*, "aqualinkd/Filter_Pump", "aqualinkd/Spa_Mode"*/])
    
    childDevices.each {child ->
        child.updated()
    }
}

def updated() {
    state.failedTries = 0
    unschedule()
    runEvery1Minute(checkAlive)
    update()
}

def refresh() {
    state.manualPress = true
    
    //state.childDevicesAdded = false
    //addChildDevices()
    
    update()
}

// Store the MAC address as the device ID so that it can talk to SmartThings
def setNetworkAddress() {
    // Setting Network Device Id
    def hex = "$settings.mac".toUpperCase().replaceAll(':', '')
    if (device.deviceNetworkId != "$hex") {
        device.deviceNetworkId = "$hex"
        log.debug "Device Network Id set to ${device.deviceNetworkId}"
    }
}

// Parse events from the proxy
def parse(String description) {
    setNetworkAddress()

    def msg = parseLanMessage(description)
	def topic = msg.data.topic
    def value = msg.data.value
    
    if (topic != null && value != null)
    {
        log.debug "Received event from proxy: topic = ${topic}, value = ${value}"
        
        if (topic == "aqualinkd/Alive") {
            //Map myMap = [name: "status", isStateChange: true, displayed: true]
            if (value == "0") {
                state.offline = true
                //myMap.value = 'offline'
                //myMap.descriptionText = "$device.displayName is offline"
            }
            else if (value == "1") {
                state.offline = false
                //myMap.value = 'online'
                //myMap.descriptionText = "$device.displayName is online"
            }
            /*if (device.currentValue('status') != myMap.value || state.manualPress) {
                log.info "${myMap.descriptionText}"
                sendEvent(myMap)
                if (myMap.isStateChange) {
                    if (myMap.value == 'offline')
                        offline()
                    else if (myMap.value == 'online' && !state.manualPress)
                        update()
                }
            }*/
            state.failedTries = 0
            //state.manualPress = false
            
            updateMode()
        }
        else if (topic == "aqualinkd/Service_Mode") {
            if (value == "0") {
                state.service_mode = false
            }
            else if (value == "1") {
                state.service_mode = true
            }
            
            updateMode()
        }/*
        else if (topic == "aqualinkd/Filter_Pump") {
            log.debug "${topic} = ${value}"
            if (value == "0")
                state.filterPump = false
            else if (value == "1")
                state.filterPump = true
            
            updateMode()
        }
        else if (topic == "aqualinkd/Spa_Mode") {
            log.debug "${topic} = ${value}"
            if (value == "0")
                state.spaMode = false
            else if (value == "1")
                state.spaMode = true
            
            updateMode()
        }*/

        state.subscriptions[topic].each {childId ->
            childDevices.findAll( { it.device.deviceNetworkId == childId} ).each {
                //log.debug "Sending event '${topic}' to '${childId}'"
                it.process(topic, value)
            }
        }
    }
}

private updateMode() {
    if (state.offline == true) {
        offline()
        sendEvent(name:"mode", value:"offline")
        log.debug "Setting mode to 'offline'"
    }
    else if (state.service_mode == true) {
        offline()
        sendEvent(name:"mode", value:"service")
        log.debug "Setting mode to 'service'"
    }
    else {
        if (device.currentValue('mode').matches("offline|service"))
            update()
            
        def filterPump = getChildDevice("Filter Pump").currentValue("status")
        def cleaner = getChildDevice("Cleaner").currentValue("status")
        def spaMode = getChildDevice("Spa Mode").currentValue("status")
        
        log.debug "updateMode() -> filterPump = ${filterPump}, spaMode = ${spaMode}, cleaner = ${cleaner}"

        if (filterPump != null && cleaner != null && spaMode != null) {
            if (filterPump == "off") {
                sendEvent(name:"mode", value:"off")
                log.debug "Setting mode to 'off'"
            }
            else if (cleaner != "off") {
                sendEvent(name:"mode", value:"clean")
                log.debug "Setting mode to 'clean'"
            }
            else if (spaMode != "off") {
                sendEvent(name:"mode", value:"spa")
                log.debug "Setting mode to 'spa'"
            }
            else {
                sendEvent(name:"mode", value:"pool")
                log.debug "Setting mode to 'pool'"
            }
        }
    }
}

def subscribe(child, topics) {
    if (device.hub == null) {
        log.error "Hub is null, must set the hub in the device settings so we can get local hub IP and port"
        return
    }
    else if (!ip || !port || !mac) {
        log.error "Must set the ip, port, and mac of proxy in the device settings"
        return
    }

    def json = [
        topics: topics,
        callback: device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
    ]

    postMessage("/subscribe", json)

    if (child != null) {
        topics.each {topic ->
            if (state.subscriptions[topic] == null)
                state.subscriptions[topic] = []

            def childId = child.device.deviceNetworkId
            if (!state.subscriptions[topic].contains(childId))
                state.subscriptions[topic].add(childId)
        }
    }
}

def push(topic, value) {
    def json = [
        topic: topic,
        value: value
    ]

    postMessage("/push", json)
}

// Send message to the Bridge
def postMessage(path, message) {
    log.debug "Posting '${message}' to device at '${path}'"
    setNetworkAddress()

    def headers = [:]
    headers.put("HOST", "$ip:$port")
    headers.put("Content-Type", "application/json")

    def hubAction = new physicalgraph.device.HubAction(
        method: "POST",
        path: path,
        headers: headers,
        body: message
    )
    sendHubCommand(hubAction)
}

def switchStatus(component, value) {
    log.debug "switchStatus(${component}, ${value})"
    
    if (component == "Filter Pump") {
        if (value == "on" || value == "delay")
            state.filterPump = true
        else if (value == "off")
            state.filterPump = false

        updateMode()
    }
    else if (component == "Spa Mode") {
        if (value == "on" || value == "delay")
            state.spaMode = true
        else if (value == "off")
            state.spaMode = false

        updateMode()
    }
    else if (component == "Cleaner") {
        if (value == "on" || value == "delay")
            state.cleaner = true
        else if (value == "off")
            state.cleaner = false

        updateMode()
    }
}

def checkAlive() {
    log.debug "checkAlive()"
    //Map myMap = [name: "status", isStateChange: true, displayed: true, value: 'offline', descriptionText: "$device.displayName is offline" ]
    if (state.failedTries >= 2) {
        state.offline = true
        updateMode()
        /*
        if (device.currentValue('status') != 'offline' || state.manualPress) {
            sendEvent(myMap)
            state.manualPress = false
            log.info "${myMap.descriptionText}"
            offline()
        }*/
    }
    state.failedTries = state.failedTries + 1 
	subscribe(null, ["aqualinkd/Alive"])
}

private offline() {
    //sendEvent(name:"mode", value:null)

    childDevices.each {
        def theAtts = it.supportedAttributes
        theAtts.each {att ->
            Map myMap = [name:att.name, displayed: true, value:null, descriptionText: "$it.displayName $att.name is unavailable"]
            it.sendEvent(myMap)
        }
    }
}