// ActiveTestShow.h: interface for the CActiveTestShow class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_ACTIVETESTSHOW_H__6ED03408_92A1_44DD_B374_1D597DBDFB1E__INCLUDED_)
#define AFX_ACTIVETESTSHOW_H__6ED03408_92A1_44DD_B374_1D597DBDFB1E__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Gui.h"
#include "adsStd.h"
#include "Binary.h"

class CActiveTestShow  
{
public:
	CActiveTestShow();
	virtual ~CActiveTestShow();

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

	W_UINT8 AcceptMsg ();

	void Init (const char *pTitle = NULL);	
	void Init (string strTitle);
	void Init (CBinary idTitle);

	W_INT16 AddButton (CBinary idButtonText, char byStatus);
	W_INT16 AddPrompt (CBinary idPromptText);
	W_INT16 AddPrompt (char *pszText);
	W_INT16 AddPrompt(const char *pszText);

	//add by scf 2008\2\29
	W_INT16 SetFlag (W_INT16 iFlag);
	W_INT16 AddMsg(string strMsg);
	W_INT16 AddHeaderText(string strHeaderMsg);
	//end add
	W_INT16 Add (CBinary idDataStream, string strDataStreamValue);
	W_INT16 Add (CBinary idDataStream, string strDataStreamValue, CBinary idUnit);
	W_INT16 Add (CBinary idDataStream, string strValue1, string strValue2);

	W_INT16 Show ();
	W_INT16 Show (W_INT16 &iSelNum);
};

#endif // !defined(AFX_ACTIVETESTSHOW_H__6ED03408_92A1_44DD_B374_1D597DBDFB1E__INCLUDED_)
