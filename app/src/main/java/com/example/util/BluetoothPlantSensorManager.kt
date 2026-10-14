package com.example.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

data class BluetoothPlantSensor(
    val macAddress: String,
    val name: String,
    val soilMoisturePercent: Int, // 0..100%
    val temperatureCelsius: Float, // e.g. 21.5
    val sunlightLux: Int, // e.g. 3500
    val batteryPercent: Int = 92,
    val isConnected: Boolean = true
)

object BluetoothPlantSensorManager {
    fun scanNearbySensors(): Flow<List<BluetoothPlantSensor>> = flow {
        // Mock scanning for Bluetooth LE sensors
        delay(1200)
        val mockSensors = listOf(
            BluetoothPlantSensor(
                macAddress = "C4:7F:51:8B:2A:01",
                name = "FlowerCare-2A01",
                soilMoisturePercent = 42,
                temperatureCelsius = 22.4f,
                sunlightLux = 4200,
                batteryPercent = 95
            ),
            BluetoothPlantSensor(
                macAddress = "C4:7F:51:32:89:FE",
                name = "SoilPulse-Monster1",
                soilMoisturePercent = 18, // Dry alert!
                temperatureCelsius = 24.1f,
                sunlightLux = 6800,
                batteryPercent = 88
            ),
            BluetoothPlantSensor(
                macAddress = "C4:7F:51:99:C3:02",
                name = "VerdantSense-Succulent",
                soilMoisturePercent = 75,
                temperatureCelsius = 20.8f,
                sunlightLux = 2100,
                batteryPercent = 91
            )
        )
        emit(mockSensors)
    }

    fun readSensorData(macAddress: String): BluetoothPlantSensor {
        val baseMoisture = Random.nextInt(15, 85)
        val temp = 18.0f + Random.nextFloat() * 10f
        val lux = Random.nextInt(1000, 10000)
        return BluetoothPlantSensor(
            macAddress = macAddress,
            name = "Sensor $macAddress",
            soilMoisturePercent = baseMoisture,
            temperatureCelsius = (temp * 10).toInt() / 10.0f,
            sunlightLux = lux
        )
    }
}
