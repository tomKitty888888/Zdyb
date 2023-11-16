// MultiSelectShow.h: interface for the CMultiSelectShow class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_MULTISELECTSHOW_H__1318D73C_5082_4F6E_9353_2DB3EB80C570__INCLUDED_)
#define AFX_MULTISELECTSHOW_H__1318D73C_5082_4F6E_9353_2DB3EB80C570__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Gui.h"
#include "adsStd.h"
#include "Binary.h"

class CMultiSelectShow  
{
public:
	CMultiSelectShow();
	virtual ~CMultiSelectShow();

public:
	class CSelectedItemData 
	{
	friend class CMultiSelectShow;
	public:
		CSelectedItemData();

		void SetMaxSelectedNumber(W_UINT16 uiMaxSelectedNumber = 0xFFFF) ;
		bool IsSelected(W_INT16 iSequence);

		W_INT16 GetItemNumber();
		W_INT16 SetItemNumber(W_INT16 nNumber);
		W_INT16 SetSelectedItem(W_INT16 iSequence, bool IsSelected);
		W_INT16 UserClickButtonKeyValue();

	protected: 
		CBinary m_binMaskCode;
		W_INT16 m_i16ScreenFirstLineItemSequence;
		W_INT16 m_i16ItemNumber;
		W_INT16 m_iUserClickButtonKeyValue;
		W_UINT16 m_uiMaxSelectedNumber;
	};

public:
	void Init(const char* pTitle = NULL);
	void Init(string strTitle);
	void Init(CBinary idTitle);

	W_INT16 Add(CBinary idDataStream);
	W_INT16 Add(string strMutiSelectedItem);
	W_INT16 AddMsg(string strMsg);
	bool Show(CSelectedItemData& SelectedData,W_INT16 &iSelectedIndex,BYTE iFlag=0);
	bool Show(CSelectedItemData& SelectedData);

private:
	//CDatabase *m_pDataBase;
	W_INT16 m_iItemNumber;  // 实际的多选项目数量
	string	m_strMsg;
};

#endif // !defined(AFX_MULTISELECTSHOW_H__1318D73C_5082_4F6E_9353_2DB3EB80C570__INCLUDED_)
