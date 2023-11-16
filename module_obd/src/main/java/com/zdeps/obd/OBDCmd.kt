package com.zdeps.obd

import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.module_obd.R

object OBDCmd {



    fun description(key:String):String{
        when(key){
            "VIN" ->{
                return getValue(R.string.obd_key_VIN)
            }
            "LampState" ->{
                return getValue(R.string.obd_key_LampState)
            }
            "Mileage" ->{
                return getValue(R.string.obd_key_Mileage)
            }
            "Current" ->{
                return getValue(R.string.obd_key_Current)
            }
            "SolarTermDoorPosition" ->{
                return getValue(R.string.obd_key_SolarTermDoorPosition)
            }
            "CalculationLoad" ->{
                return getValue(R.string.obd_key_CalculationLoad)
            }
            "Velocity" ->{
                return getValue(R.string.obd_key_Velocity)
            }
            "EngineRev" ->{
                return getValue(R.string.obd_key_EngineRev)
            }
            "IntakeAirPressure" ->{
                return getValue(R.string.obd_key_IntakeAirPressure)
            }
            "CoolantTemp" ->{
                return getValue(R.string.obd_key_CoolantTemp)
            }
            "MAF" ->{
                return getValue(R.string.obd_key_MAF)
            }
            "FuelDeliveryPressure" ->{
                return getValue(R.string.obd_key_FuelDeliveryPressure)
            }
            "EGRPosition" ->{
                return getValue(R.string.obd_key_EGRPosition)
            }
            "EGT" ->{
                return getValue(R.string.obd_key_EGT)
            }
            "ThrottlePosition" ->{
                return getValue(R.string.obd_key_ThrottlePosition)
            }
            "EngineOilT" ->{
                return getValue(R.string.obd_key_EngineOilT)
            }
            "NOXConcentration" ->{
                return getValue(R.string.obd_key_NOXConcentration)
            }
            "EngineOutputPower" ->{
                return getValue(R.string.obd_key_EngineOutputPower)
            }
            "UreaInjectionVolume" ->{
                return getValue(R.string.obd_key_UreaInjectionVolume)
            }
            "FuelConsumption" ->{
                return getValue(R.string.obd_key_FuelConsumption)
            }
            "DPFDifferentialPressure" ->{
                return getValue(R.string.obd_key_DPFDifferentialPressure)
            }
            "ChargeAirPressure" ->{
                return getValue(R.string.obd_key_ChargeAirPressure)
            }
            "ActualEngineTorque" ->{
                return getValue(R.string.obd_key_ActualEngineTorque)
            }
            "EngineFrictionTorque" ->{
                return getValue(R.string.obd_key_EngineFrictionTorque)
            }
            "DTC" ->{
                return getValue(R.string.obd_key_DTC)
            }
            "DTCMsg" ->{
                return getValue(R.string.obd_key_DTCMsg)
            }
            "FaultMileage" ->{
                return getValue(R.string.obd_key_FaultMileage)
            }
            "MILTime" ->{
                return getValue(R.string.obd_key_MILTime)
            }

            "EGR" ->{
                return getValue(R.string.obd_key_EGR)
            }
            "DOC" ->{
                return getValue(R.string.obd_key_DOC)
            }
            "SCR" ->{
                return getValue(R.string.obd_key_SCR)
            }
            "DPF" ->{
                return getValue(R.string.obd_key_DPF)
            }
            "POC" ->{
                return getValue(R.string.obd_key_POC)
            }
            "CC" ->{
                return getValue(R.string.obd_key_CC)
            }
            "CCH" ->{
                return getValue(R.string.obd_key_CCH)
            }
            "OS" ->{
                return getValue(R.string.obd_key_OS)
            }
            "OSH" ->{
                return getValue(R.string.obd_key_OSH)
            }

//            "CurDTC" ->{
//                return getValue(R.string.obd_key_CurDTC)
//            }
//            "CurDetail" ->{
//                return getValue(R.string.obd_key_CurDetail)
//            }
            "Unsettled" ->{
                return getValue(R.string.obd_key_Unsettled)
            }
            "UnsetDTC" ->{
                return getValue(R.string.obd_key_UnsetDTC)
            }
            "UnsetDetail" ->{
                return getValue(R.string.obd_key_UnsetDetail)
            }
            "Permanent" ->{
                return getValue(R.string.obd_key_Permanent)
            }
            "ODO" ->{
                return getValue(R.string.obd_key_ODO)
            }
            "OBDType" ->{
                return getValue(R.string.obd_key_OBDType)
            }


        }

        val CurDTC = "CurDTC"
        if (key.contains(CurDTC)) {
            val sumIndex = key.substring(CurDTC.length, key.length)
            return getValue(R.string.obd_key_CurDTC) + sumIndex
        }
        val CurDetail = "CurDetail"
        if (key.contains(CurDetail)) {
            val sumIndex = key.substring(CurDetail.length, key.length)
            return getValue(R.string.obd_key_CurDetail) + sumIndex //+ getValue(R.string.obd_key_Chinese_instructions)
        }
        val UnsetDTC = "UnsetDTC"
        if (key.contains("UnsetDTC")) {
            val sumIndex = key.substring(UnsetDTC.length, key.length)
            return getValue(R.string.obd_key_UnsetDTC) + sumIndex
        }
        val UnsetDetail = "UnsetDetail"
        if (key.contains(UnsetDetail)) {
            val sumIndex = key.substring(UnsetDetail.length, key.length)
            return getValue(R.string.obd_key_UnsetDetail) + sumIndex //+ getValue(R.string.obd_key_Chinese_instructions)
        }
        val PDTC = "PDTC"
        if (key.contains(PDTC)) {
            val sumIndex = key.substring(PDTC.length, key.length)
            return getValue(R.string.obd_key_PDTC) + sumIndex
        }
        val PDetail = "PDetail"
        if (key.contains(PDetail)) {
            val sumIndex = key.substring(PDetail.length, key.length)
            return getValue(R.string.obd_key_PDetail) + sumIndex
        }

        return "未标注"
    }


    /**
     * 单位
     */
    fun getUnit(key:String):String{
        when(key){
            "VIN" ->{
            }
            "LampState" ->{
            }
            "Mileage" ->{
                return "km"
            }
            "Current" ->{
            }
            "SolarTermDoorPosition" ->{
                return "%"
            }
            "CalculationLoad" ->{
                return "%"
            }
            "Velocity" ->{
                return "km/h"
            }
            "EngineRev" ->{
                return "rpm"
            }
            "IntakeAirPressure" ->{
                return "kpa"
            }
            "CoolantTemp" ->{
                return "℃"
            }
            "MAF" ->{
                return "g/s"
            }
            "FuelDeliveryPressure" ->{
                return "bar"
            }
            "EGRPosition" ->{
                return "%"
            }
            "EGT" ->{
                return "℃"
            }
            "ThrottlePosition" ->{
                return "%"
            }
            "EngineOilT" ->{
                return return "℃"
            }
            "NOXConcentration" ->{
                return "ppm"
            }
            "EngineOutputPower" ->{
                return "kw"
            }
            "UreaInjectionVolume" ->{
                return "mL/s"
            }
            "FuelConsumption" ->{
                return "L/100km"
            }
            "DPFDifferentialPressure" ->{
                return "Kpa"
            }
            "ChargeAirPressure" ->{
                return "Kpa"
            }
            "ActualEngineTorque" ->{
                return "%"
            }
            "EngineFrictionTorque" ->{
                return "%"
            }
            "DTC" ->{

            }
            "DTCMsg" ->{

            }
            "FaultMileage" ->{
                return "km"
            }
            "MILTime" ->{
                return "min"
            }
        }
        return ""
    }
    fun getValue(id:Int):String{
        return BaseApplication.getInstance().getString(id)
    }
}