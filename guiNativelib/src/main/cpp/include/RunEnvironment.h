// RunEnvironment.h: interface for the CRunEnvironment class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_RUNENVIRONMENT_H__BA067A91_38A4_4057_AF39_592176C09561__INCLUDED_)
#define AFX_RUNENVIRONMENT_H__BA067A91_38A4_4057_AF39_592176C09561__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Gui.h"
#include "adsStd.h"
#include "Binary.h"

class CRunEnvironment  
{
public:
	CRunEnvironment();
	virtual ~CRunEnvironment();

protected:
	static string m_strDisplayDirectory;    // 显示程序工作目录
	static string m_strDiagnosisDirectory;  // 诊断程序工作目录
	static string m_strLanguage;            // 语言代码
	//add by scf 2007.2.13
	static string m_strSerialNumber;         //机器序列号
	static bool      m_bDemoMode;
	static  unsigned char    m_cScreenType;		//屏幕类型
public:
	// 设置显示程序工作目录
	static void SetDisplayDirectory(char* pDirectory);

	// 取得显示程序工作目录
	static string GetDisplayDirectory();

	// 设置诊断程序工作目录
	static void SetDiagnosisDirectory(char* pDirectory);

	// 取得诊断程序工作目录
	static string GetDiagnosisDirectory();

	// 设置语言代码
	static void SetLanguage(char* pLanguage);

	// 取得语言代码
	static string GetLanguage();

	//add by scf 2007.2.13
	// 设置序列号
	static void SetSerialNumber(char* pSerailNumber);

	//add by scf 2007.2.13
	// 设置序列号
	static void SetDemoMode(bool bDemoMode=false);


	// 取得序列号
	static string GetSerialNumber();
	
	// 取得演示模式
	static bool GetDemoMode();
	
	static unsigned char GetScreenType();

	static void SetScreenType(unsigned char uiType);


};

#endif // !defined(AFX_RUNENVIRONMENT_H__BA067A91_38A4_4057_AF39_592176C09561__INCLUDED_)
