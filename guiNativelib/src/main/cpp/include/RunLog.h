// RunLog.h: interface for the CRunLog class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_RUNLOG_H__BD6DA2AF_31A4_4DB7_8342_B94276F1990E__INCLUDED_)
#define AFX_RUNLOG_H__BD6DA2AF_31A4_4DB7_8342_B94276F1990E__INCLUDED_

#include "adsStd.h"	// Added by ClassView
#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

class CRunLog  
{
public:
	CRunLog();
	virtual ~CRunLog();

	W_INT16 WriteMenu(void);
	void WriteContent(string str,bool b);
	void SetBufSize(DWORD size);
};

#endif // !defined(AFX_RUNLOG_H__BD6DA2AF_31A4_4DB7_8342_B94276F1990E__INCLUDED_)
