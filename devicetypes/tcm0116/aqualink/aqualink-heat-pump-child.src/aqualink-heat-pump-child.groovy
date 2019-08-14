/**
 *  Aqualink Heat Pump Child Device
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
    definition (name: "Aqualink Heat Pump Child", namespace: "tcm0116/aqualink", author: "Thomas Moore") {
        capability "Actuator"
        capability "Sensor"
        //capability "Temperature Measurement"
        capability "Thermostat Mode"
        capability "Thermostat Operating State"
        capability "Thermostat Heating Setpoint"
        capability "Thermostat"
        
        attribute "heatingSetpointMin", "number"
		attribute "heatingSetpointMax", "number"

        command "heatingSetpointUp"
        command "heatingSetpointDown"
        command "thermostatModeOff"
        command "thermostatModeHeat"
    }

    simulator {}

    tiles(scale: 2) {
        //valueTile("temperature", "device.temperature", width: 1, height: 1) {
        //    state "temperature", label:'${currentValue}°', icon:"st.alarm.temperature.normal"
        //}
        /*valueTile("temperature", "device.temperature", width: 1, height: 1, decoration: "flat") {
			state("temperature", label:'${currentValue}°', icon:"st.alarm.temperature.normal",
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
			)
		}*/
        standardTile("heatingSetpointDown", "device.heatingSetpoint",  width: 1, height: 1, canChangeIcon: false, decoration: "flat") {
            state "default", label:'', action:"heatingSetpointDown", icon:"st.thermostat.thermostat-left"
        }
        valueTile("heatingSetpoint", "device.heatingSetpoint", width: 3, height: 1) {
            state "heatingSetpoint", label:'Setpoint: ${currentValue}°', unit: "Heat"
        }
        standardTile("heatingSetpointUp", "device.heatingSetpoint", width: 1, height: 1, canChangeIcon: false, decoration: "flat") {
            state "default", label:'', action:"heatingSetpointUp", icon:"st.thermostat.thermostat-right"
        }
        standardTile("thermostatOperatingState", "device.thermostatOperatingState", width: 6, height: 1, decoration: "flat") {
            state "default", label:'unavailable', icon:"st.thermostat.heat", backgroundColor:"#cccccc", defaultState: true
            state "idle", label:'off', action:"thermostatModeHeat", icon:"st.thermostat.heat", backgroundColor:"#ffffff", nextState:"enabling"
            state "pending heat", label:'enabled', action:"thermostatModeOff", icon:"st.thermostat.heat", backgroundColor:"#00a0dc", nextState:"disabling"
            state "heating", label:'heating', action:"thermostatModeOff", icon:"st.thermostat.heat", backgroundColor:"#e86d13", nextState:"disabling"
            state "enabling", label:'${name}', icon:"st.thermostat.heat", backgroundColor:"#f0b823", nextState: "enabling"
            state "disabling", label:'${name}', icon:"st.thermostat.heat", backgroundColor:"#f0b823", nextState: "disabling"
        }
    }
}

def installed() {
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    //def topics = ["aqualinkd/Filter_Pump", "aqualinkd/Spa_Mode"]
    def topics = []

    if (device.componentName == "poolHeater")
        topics += [
            //"aqualinkd/Temperature/Pool",
            "aqualinkd/Pool_Heater",
            "aqualinkd/Pool_Heater/enabled",
            "aqualinkd/Pool_Heater/setpoint"
        ]
    else if (device.componentName == "spaHeater")
        topics += [
            //"aqualinkd/Temperature/Spa",
            "aqualinkd/Spa_Heater",
            "aqualinkd/Spa_Heater/enabled",
            "aqualinkd/Spa_Heater/setpoint"
        ]
    else {
        log.error "Invalid componentName: ${device.componentName}"
        return
    }

    parent.subscribe(this, topics)
    
    def supportedThermostatModes = ["heat", "off"]
    sendEvent(name:"supportedThermostatModes", value:supportedThermostatModes)
    sendEvent(name:"heatingSetpointMin", value:(getTemperatureScale() == "F") ? 34 : fahrenheitToCelsius(34), unit:getTemperatureScale())
    sendEvent(name:"heatingSetpointMax", value:(getTemperatureScale() == "F") ? 104 : fahrenheitToCelsius(104), unit:getTemperatureScale())
    
    //state.receivedTemperature = null
    state.filterPump = false
    state.spaMode = false
}

// Parse events from the parent device
def process(topic, value) {
    /*if (topic == "aqualinkd/Filter_Pump") {
        if (value == "1") {
            state.filterPump = true;
            if (state.receivedTemperature != null) {
                if ((device.componentName == "spaHeater" && state.spaMode) ||
                    (device.componentName == "poolHeater" && !state.spaMode)) {
                    sendEvent(name:"temperature", value:state.receivedTemperature, unit:getTemperatureScale())
                }
            }
        }
        else if (value == "0") {
            state.filterPump = false;
            sendEvent(name:"temperature", value:null, unit:getTemperatureScale())
        }
    }
    else if (topic == "aqualinkd/Spa_Mode") {
        if (value == "1") {
            state.spaMode = true;
            if (state.receivedTemperature != null && device.componentName == "spaHeater" && state.filterPump)
                sendEvent(name:"temperature", value:state.receivedTemperature, unit:getTemperatureScale())
            else if (device.componentName == "poolHeater")
                sendEvent(name:"temperature", value:null, unit:getTemperatureScale())
        }
        else if (value == "0") {
            state.spaMode = false;
            if (device.componentName == "spaHeater")
                sendEvent(name:"temperature", value:null, unit:getTemperatureScale())
        }
    }
    else if (topic == "aqualinkd/Temperature/Pool" || topic == "aqualinkd/Temperature/Spa") {
        def temperature = value.toBigDecimal();
        if (getTemperatureScale() == "C")
            temperature = fahrenheitToCelsius(temperature)
        state.receivedTemperature = temperature.setScale(0, BigDecimal.ROUND_HALF_UP);
        if (state.filterPump && ((state.spaMode && device.componentName == "spaHeater") || (!state.spaMode && device.componentName == "poolHeater")))
            sendEvent(name:"temperature", value:state.receivedTemperature, unit:getTemperatureScale())
    }
    else*/ if (topic == "aqualinkd/Pool_Heater/setpoint" || topic == "aqualinkd/Spa_Heater/setpoint") {
        def setpoint = value.toBigDecimal();
        if (getTemperatureScale() == "C")
            setpoint = fahrenheitToCelsius(setpoint)
        setpoint = setpoint.setScale(0, BigDecimal.ROUND_HALF_UP);
        sendEvent(name:"heatingSetpoint", value:setpoint, unit:getTemperatureScale())
    }
    else if (topic == "aqualinkd/Pool_Heater/enabled" || topic == "aqualinkd/Spa_Heater/enabled") {
        if (value == "1") {
            sendEvent(name:"thermostatMode", value:"heat")
            if (device.currentValue("thermostatOperatingState") == "idle")
                sendEvent(name:"thermostatOperatingState", value:"pending heat")
        }
        else if (value == "0") {
            sendEvent(name:"thermostatMode", value:"off")
            sendEvent(name:"thermostatOperatingState", value:"idle")
        }
    }
    else if (topic == "aqualinkd/Pool_Heater" || topic == "aqualinkd/Spa_Heater") {
        if (value == "1") {
            sendEvent(name:"thermostatMode", value:"heat")
            sendEvent(name:"thermostatOperatingState", value:"heating")
        }
        else if (value == "0") {
            if (device.currentValue("thermostatMode") == "heat")
                sendEvent(name:"thermostatOperatingState", value:"pending heat")
            else
                sendEvent(name:"thermostatOperatingState", value:"idle")
        }
    }
}

def setHeatingSetpoint(setpoint) {
    if (setpoint < device.currentValue("heatingSetpointMin"))
        setpoint = device.currentValue("heatingSetpointMin")
    else if (setpoint > device.currentValue("heatingSetpointMax"))
        setpoint = device.currentValue("heatingSetpointMax")

    // Setting the setpoint is a slow operation, so go ahead and update the local state
    // and send the command to the controller after 5 seconds. If the user updates the
    // setpoint again within the 5 second window, then the 5 second timer will start over.
    // This allows the UI to react quickly to the user without sending a bunch of commands
    // to the controller.
    sendEvent(name:"heatingSetpoint", value:setpoint, unit:getTemperatureScale())
    runIn(5, pushSetpoint)
}

def pushSetpoint() {
    def setpoint = device.currentValue("heatingSetpoint");
    if (getTemperatureScale() == "C")
        setpoint = fahrenheitToCelsius(setpoint)
        
    setpoint = setpoint.setScale(2, BigDecimal.ROUND_HALF_UP);
    
    if (device.componentName == "poolHeater")
        parent.push("aqualinkd/Pool_Heater/setpoint/set", setpoint.toString())
    else if (device.componentName == "spaHeater")
        parent.push("aqualinkd/Spa_Heater/setpoint/set", setpoint.toString())
}

void heatingSetpointUp() {
    setHeatingSetpoint(device.currentValue("heatingSetpoint") + 1)
}

void heatingSetpointDown() {
    setHeatingSetpoint(device.currentValue("heatingSetpoint") - 1)
}

def setThermostatMode(mode) {
    def value
    if (mode == "heat")
        value = "1"
    else if (mode == "off")
        value = "0"
    else
        return
    
    if (device.componentName == "poolHeater")
        parent.push("aqualinkd/Pool_Heater/set", value)
    else if (device.componentName == "spaHeater")
        parent.push("aqualinkd/Spa_Heater/set", value)
}

void thermostatModeOff() {
    setThermostatMode("off")
}

void thermostatModeHeat() {
    setThermostatMode("heat")
}