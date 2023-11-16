// DataFile.h: interface for the CDataFile class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_DATAFILE_H__00F15985_4A91_4F15_AC01_4AD05717EF2C__INCLUDED_)
#define AFX_DATAFILE_H__00F15985_4A91_4F15_AC01_4AD05717EF2C__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "adsStd.h"
#include "Com.h"
#include "Binary.h"
#include "Display.h"

#include <io.h>
#include <tchar.h>

//same as GUI.lib
//#define TEXT_TXT	5
//#define TEXT_DTC	6
//#define TEXT_CDS	7
//#define TEXT_OTHER	8

class CDataFile  
{
public:
	CDataFile();
	virtual ~CDataFile();

	DWORD StrToHex(char *str);

	FILE *m_pfData;
	DWORD m_dwCurrentAddr;
	bool DataFileOpen();
	bool DataFileClose();
	bool GetTextCommand(BYTE bHead,BYTE *pbId,WORD wLen,vector<CBinary> &vecbinCmd);

	void String2Binary(string str,CBinary &binary);
};

#endif // !defined(AFX_DATAFILE_H__00F15985_4A91_4F15_AC01_4AD05717EF2C__INCLUDED_)
