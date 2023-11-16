// EcuCommException.h: interface for the CEcuCommException class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_ECUCOMMEXCEPTION_H__6FC4B870_5D6C_4F11_9ABD_72558A770EB9__INCLUDED_)
#define AFX_ECUCOMMEXCEPTION_H__6FC4B870_5D6C_4F11_9ABD_72558A770EB9__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Gui.h"
#include "adsStd.h"
#include "Binary.h"

class CEcuCommException  
{
protected:
	W_INT16 m_iErrorCode;
	string m_strOtherMessage;
	string m_strFunctionName;
//	static string m_astrException[-ECX_MAX_ERROR_CODE_NUMBER];
	 
public:
	CEcuCommException();
	CEcuCommException(W_INT16 iErrorCode, string strOtherMessageString, string strFunctionName);
	virtual ~CEcuCommException();

	virtual void SetExceptionMessage (W_INT16 iErrorCode, string strOtherMessageString, string strFunctionName);
	virtual W_INT16 GetErrorCode ();
	virtual W_UINT16 ReportError(W_UINT16 nType = 0x02);
};

#endif // !defined(AFX_ECUCOMMEXCEPTION_H__6FC4B870_5D6C_4F11_9ABD_72558A770EB9__INCLUDED_)
