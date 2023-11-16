// AutoLog.h: interface for the CAutoLog class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_AUTOLOG_H__051B3749_09DC_4C7E_9A1B_C11E86BF2AFD__INCLUDED_)
#define AFX_AUTOLOG_H__051B3749_09DC_4C7E_9A1B_C11E86BF2AFD__INCLUDED_


#include "adsStd.h"
#include "Binary.h"
#include "adsStd.h"
#include "SendFrame.h"
#include "ReceiveFrame.h"

#include <vector>
#include <string>
using namespace std;




#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

class CAutoLog  
{
public:
	CAutoLog();
	virtual ~CAutoLog();

public:
	long RecordFlag; //BOOL
	void MakeSendReceiveFrame(CSendFrame sf,CReceiveFrame rf);
	void LogPrint(void);
	void PrintString(string str);
	void InitAutoLog(DWORD iTaskId);
	void SetBufSize(DWORD size);
	void MakeReceiveFrame(CReceiveFrame rf);

};

#endif // !defined(AFX_AUTOLOG_H__051B3749_09DC_4C7E_9A1B_C11E86BF2AFD__INCLUDED_)
