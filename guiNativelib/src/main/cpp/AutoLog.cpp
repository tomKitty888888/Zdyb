// AutoLog.cpp: implementation of the CAutoLog class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "AutoLog.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

//自动记录交互命令的log类,这里不实现
//有些函数并未知道其定义,这里暂时只为了编译能通过,如有需要则改之

CAutoLog::CAutoLog()
{
	RecordFlag = FALSE;
}

CAutoLog::~CAutoLog()
{

}

void CAutoLog::MakeSendReceiveFrame(CSendFrame sf,CReceiveFrame rf)
{
}

void CAutoLog::LogPrint(void)
{
}

void CAutoLog::PrintString(string str)
{
}

void CAutoLog::InitAutoLog(DWORD iTaskId)
{
}
void CAutoLog::SetBufSize(DWORD size)
{
}
void CAutoLog::MakeReceiveFrame(CReceiveFrame rf)
{
}
