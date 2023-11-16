// DataStreamShow.h: interface for the CDataStreamShow class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_DATASTREAMSHOW_H__B1AEDC2C_331E_4035_BDE0_E71E3E254DB5__INCLUDED_)
#define AFX_DATASTREAMSHOW_H__B1AEDC2C_331E_4035_BDE0_E71E3E254DB5__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Gui.h"
#include "adsStd.h"
#include "Binary.h"

#define	DST_NORMAL	0
#define DST_VW		1

class CDataStreamShow  
{
public:
	CDataStreamShow();
	virtual ~CDataStreamShow();

private:
	W_INT16 m_iDataStreamFormat;	// 数据流格式
	W_INT16 m_iDataStreamType;		// 数据流类型

public:
	W_UINT8 AcceptMsg();

	void Init(CBinary idTitle, string strStdValueLibName = "", W_UINT16 uiType = DST_NORMAL);
	void Init(CBinary idTitle, CBinary idStdValueLibName, W_UINT16 uiType = DST_NORMAL);
	void Init(string strTitle="", string strStdValueLibName = "", W_UINT16 uiType = DST_NORMAL);	
	void Init(W_UINT16 uiTopLine,W_UINT16 uiAllCount,string strTitle="", string strStdValueLibName = "", W_UINT16 uiType = DST_NORMAL);		

	void AddSelectIndex(BYTE *nIndex,BYTE nNum);
	vector<BYTE> m_vecMySelected;

//	W_INT16 Add(CBinary idDataStream, string strDataStreamValue); //更改了此条接口(兼容原来的接口)
	W_INT16 Add(CBinary idDataStream, string strDataStreamValue, string strUnit="");
	W_INT16 Add(CBinary idDataStream, string strDataStreamValue, CBinary idUnit);

	W_INT16 Show (void);
	W_INT16 Show (W_INT16 &iTop,W_INT16 &iNum);


private:
	vector<BYTE> m_vecSelectedItem;		//保存当前要显示的数据流的索引
};

#endif // !defined(AFX_DATASTREAMSHOW_H__B1AEDC2C_331E_4035_BDE0_E71E3E254DB5__INCLUDED_)
