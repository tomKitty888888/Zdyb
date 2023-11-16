// Information.cpp: implementation of the CInformation class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Information.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

//该类目前有部分诊断仪包含到头文件, 但都很少用,  这里只是拷贝过来,并未改动


string CInformation::m_strVehicleSystemName = "OBD2";

// 诊断程序版本号
W_FLOAT CInformation::m_fVersion = (W_FLOAT)1.01;

// 设备名，要求诊断程序员设置
string CInformation::m_strEquipmentName = "007";


CInformation::CInformation()
{

}

CInformation::~CInformation()
{

}

void CInformation::SetVehiclesSystemName(char *pVehicleSystemName)
{
	m_strVehicleSystemName = pVehicleSystemName;
}
string CInformation::GetVehiclesSystemName()
{
	return m_strVehicleSystemName;
}
void CInformation::SetVersion(W_FLOAT fVersion)
{
	m_fVersion = fVersion;
}
W_FLOAT CInformation::GetVersion()
{
	return m_fVersion;
}
string CInformation::GetEquipmentName()
{
	return m_strEquipmentName;
}
