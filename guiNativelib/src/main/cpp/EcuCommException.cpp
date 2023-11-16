// EcuCommException.cpp: implementation of the CEcuCommException class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "EcuCommException.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

CEcuCommException::CEcuCommException()
{
	m_iErrorCode = 0;
	m_strOtherMessage = "";
	m_strFunctionName = "";
}

CEcuCommException::CEcuCommException(W_INT16 iErrorCode, string strOtherMessageString, string strFunctionName)
{
}

CEcuCommException::~CEcuCommException()
{

}
void CEcuCommException::SetExceptionMessage (W_INT16 iErrorCode, 
											 string strOtherMessage, 
											 string strFunctionName)
{
	m_iErrorCode      = iErrorCode;
	m_strOtherMessage = strOtherMessage;
	m_strFunctionName = strFunctionName;
}
W_INT16 CEcuCommException::GetErrorCode ()
{
	return m_iErrorCode;
}
W_UINT16 CEcuCommException::ReportError(W_UINT16 nType)
{
	OutputDebugString("CEcuCommException::ReportError() ERROR!!!\r\n");
	return 0;
}