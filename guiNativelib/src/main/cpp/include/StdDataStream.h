// StdDataStream.h: interface for the CStdDataStream class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_STDDATASTREAM_H__0AF29180_1BD7_4F11_90E2_53C879D32788__INCLUDED_)
#define AFX_STDDATASTREAM_H__0AF29180_1BD7_4F11_90E2_53C879D32788__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Gui.h"
#include "adsStd.h"
#include "Binary.h"

#include "BaseDataStream.h"

//#define MAXLINE 7
#define MAXLINE 20

class CStdDataStream : public CBaseDataStream  
{
public:
	CStdDataStream();
	virtual ~CStdDataStream();

public:
	// 完成读数据流并显示
	virtual W_INT16 ReadDataStream (vector<CBinary> *paidDataStream);


	virtual void DsIdToSendFrame (void);

	virtual void OnBeforeSendDsCmd () ;
};

#endif // !defined(AFX_STDDATASTREAM_H__0AF29180_1BD7_4F11_90E2_53C879D32788__INCLUDED_)
