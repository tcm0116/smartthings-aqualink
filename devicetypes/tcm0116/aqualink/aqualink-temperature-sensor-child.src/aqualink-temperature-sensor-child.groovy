/**
 *  Aqualink Temperature Sensor Child Device
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
    definition (name: "Aqualink Temperature Sensor Child", namespace: "tcm0116/aqualink", author: "Thomas Moore") {
        capability "Sensor"
        capability "Temperature Measurement"
    }

    simulator {}

    tiles(scale: 2) {
        valueTile("temperature", "device.temperature", width: 1, height: 1, decoration: "flat") {
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
    parent.subscribe(this, ["aqualinkd/Temperature/${device.componentName}"])
}

// Parse events from the parent device
def process(topic, value) {
    def temperature = value.toBigDecimal();
    if (temperature == 0)
        sendEvent(name:"temperature", value:null)
    else {
        if (getTemperatureScale() == "C")
            temperature = fahrenheitToCelsius(temperature)
        temperature = temperature.setScale(0, BigDecimal.ROUND_HALF_UP);
        sendEvent(name:"temperature", value:temperature, unit:getTemperatureScale())
    }
}