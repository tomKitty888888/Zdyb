// MenuShow.h: interface for the CMenuShow class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_MENUSHOW_H__7EF14762_2101_4494_A4D0_D53524F5CA52__INCLUDED_)
#define AFX_MENUSHOW_H__7EF14762_2101_4494_A4D0_D53524F5CA52__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Gui.h"
#include "adsStd.h"
#include "Binary.h"

class CMenuShow  
{
public:
	CMenuShow();
	virtual ~CMenuShow();

public:
	class CMenuStruct 
	{
	public:
		W_INT16  m_i16MenuSelected;
		W_INT16  m_i16MenuScreenFirstLineItem;

		CMenuStruct() 
		{ 
			m_i16MenuSelected            = -1; 
			m_i16MenuScreenFirstLineItem = 0; 
		}

		virtual ~CMenuStruct() 
		{ 

		}
	};

public:
	void Init(const char *pTitle = NULL);
	void Init(string strTitle);
	void Init(CBinary idTitle);

	bool Add(string strMenuItem);
	bool Add(CBinary idMenuItem);

	W_INT16 Show(CMenuStruct &MenuParameter);


	///2021.11.18增加，Menu菜单项上增加Combo控件
	void Init(UINT nMenuNum);
	bool Add(string strMenuItem, UINT nCtrlFlag);//nCtrlFlag:1 ListCtrl控件上显示内容
	bool Add(CBinary idMenuItem, UINT nCtrlFlag);
	bool Add(vector<CBinary> vecIdMenuItem, UINT nCtrlFlag);//nCtrlFlag:2 Combo控件上显示内容
	bool Add(vector<string> vecStrMenuItem, UINT nCtrlFlag);
	W_INT16 Show(vector<UINT> &vecIndex); //vecIndex为Combo被选中的下标

	//2021.11.19进行添加,获取点击执行按钮后的内容，也就是每个Combo控件选中后对应的下拉框的索引
	void GetComboDropIndex(vector<UINT> &vecComboDropInex);

	W_INT16 SetFlag (W_INT16 iFlag);
};

#endif // !defined(AFX_MENUSHOW_H__7EF14762_2101_4494_A4D0_D53524F5CA52__INCLUDED_)
