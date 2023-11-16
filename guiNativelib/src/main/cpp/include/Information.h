// Information.h: interface for the CInformation class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_INFORMATION_H__A6875046_67DD_4F80_A951_234040F20881__INCLUDED_)
#define AFX_INFORMATION_H__A6875046_67DD_4F80_A951_234040F20881__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "adsStd.h"

#include <string>
using namespace std;

class CInformation  
{
public:
	CInformation();
	virtual ~CInformation();

//protected:
public:
		static string m_strVehicleSystemName;
		static W_FLOAT m_fVersion;
		static string m_strEquipmentName;
public:
		// 设置车系名称
		static void SetVehiclesSystemName(char *pVehicleSystemName);

		// 获取车系名称
		static string GetVehiclesSystemName();

		// 设置版本信息
		static void SetVersion(W_FLOAT fVersion);

		// 获取版本信息
		static W_FLOAT GetVersion();

		// 获取设备名称
		static string GetEquipmentName();
};

#endif // !defined(AFX_INFORMATION_H__A6875046_67DD_4F80_A951_234040F20881__INCLUDED_)
