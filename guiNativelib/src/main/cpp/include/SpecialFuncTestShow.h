// SpecialFuncTestShow.h: interface for the CSpecialFuncTestShow class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_SPECIALFUNCTESTSHOW_H__33C39359_BAF5_4B7E_843E_E1D7CFE8860E__INCLUDED_)
#define AFX_SPECIALFUNCTESTSHOW_H__33C39359_BAF5_4B7E_843E_E1D7CFE8860E__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Gui.h"
#include "adsStd.h"
#include "Binary.h"

#include "ActiveTestShow.h"

class CSpecialFuncTestShow  
{
public:
	CSpecialFuncTestShow();
	virtual ~CSpecialFuncTestShow();

	enum 
	{
		DT_SPECIAL_TEST          = 110
    };

	// BS：BUTTON STATUS
	enum 
	{
		BS_MASK          = 0x01,	// 按钮状态掩码位，表示按钮选中状态
		BS_LOCK          = 0x02     // 按钮状态锁定位，表示按钮锁定状态
    };

public:
	enum 
	{
		//BDF_ BUTTON SHOW FORMAT
		BDF_NOMAL = 0,
		BDF_VW = 1
	};

	void Init (const char *pTitle = NULL);	
	void Init (string strTitle);
	void Init (CBinary idTitle);

	W_INT16 AddButton (CBinary idButtonText, char byStatus);

	W_INT16 Add (CBinary idDataStream, string strDataStreamValue);
	W_INT16 Add (CBinary idDataStream, string strDataStreamValue, CBinary idUnit);
	W_INT16 Add (string strMsg);
	W_INT16 AddPrerequisites (const char* strMsg);
	W_INT16 AddProcessCtrl (W_INT16 nMin,W_INT16 nMax,W_INT16 nSteps);

	W_INT16 SetFlag (W_INT16 iFlag); //新版的lib库中加入此函数

	W_INT16 Show ();

private:
	W_INT16 m_iTitleLenth;        // 动作测试标题长度
	W_INT16 m_iButtonNum;
	W_INT16 m_iDataStreamFormat;  // 数据流格式
	bool m_bAddDataStream;        // 是否已经添加数据流内容








private: //self:全部调用ActiveTestShow类的东西
	CActiveTestShow m_Act;


};

#endif // !defined(AFX_SPECIALFUNCTESTSHOW_H__33C39359_BAF5_4B7E_843E_E1D7CFE8860E__INCLUDED_)
