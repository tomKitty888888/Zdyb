// Database.h: interface for the CDatabase class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_DATABASE_H__AB801482_C7C4_4A3B_AA0F_D073825F77B9__INCLUDED_)
#define AFX_DATABASE_H__AB801482_C7C4_4A3B_AA0F_D073825F77B9__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Gui.h"
#include "adsStd.h"
#include "Binary.h"
#include "StdAfx.h"

class CDatabase  
{
public:
	CDatabase();
	virtual ~CDatabase();
	
	enum 
	{	// DB_ DATA BASE
		DB_COMMAND           = 0x00,
		DB_TEXT              = 0x01,
		DB_TROUBLE_CODE      = 0x02,
        DB_DATA_STREAM       = 0x03,
		DB_TROUBLE_CODE_HELP = 0x04
	}; 

public:
	bool IsOpen();
	// 打开文件
	bool Open(W_UINT16 uiFileCodeName);

	// 打开文件
	bool Open(string strFile);

	// 关闭文件
	void Close();

	// 检索数据库，通过ID找到对应的内容
	const vector<CBinary> SearchId(CBinary& Id);
	



private:
	BYTE m_DataBaseType;	//enum: DB_DATA_BASE
	string m_strFileName;	//enum: DB_DATA_BASE



//public:
//	_ConnectionPtr m_AccessConnect;
//	_RecordsetPtr  m_AccessRecordSet;
//
//	bool m_DatabaseOpened;
//
//	bool OpenAccessDatabase(string strFile);
//	bool CloseAccessDatabase();
//	const vector<CBinary> SearchIdAccessDatabase(CBinary& Id,string strFileNameEx="");


private:
	bool Decrypt(BYTE nIndex, BYTE *pBuf, WORD wLen);



};

string adsGetTextString (CBinary binTextID);

#endif // !defined(AFX_DATABASE_H__AB801482_C7C4_4A3B_AA0F_D073825F77B9__INCLUDED_)
