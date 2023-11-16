// MenuTree.h: interface for the CMenuTree class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_MENUTREE_H__3FB0F313_D135_4EFF_9018_220711A2BAA0__INCLUDED_)
#define AFX_MENUTREE_H__3FB0F313_D135_4EFF_9018_220711A2BAA0__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Gui.h"
#include "adsStd.h"
#include "Binary.h"

#include <stack>
#include "MenuShow.h"

extern WORD g_wTaskIdValue;

class CMenuTree  
{
protected:
	class CMenuLevel
	{
	public:
		CBinary idMenu;
		CMenuShow::CMenuStruct structMenu;
	};

	stack <CMenuLevel> stackMenuLevel;
#ifndef _TASKIDTYPE
	W_INT16 (*m_pfnTask) (W_INT16 iTaskId, CBinary idSelectedText);
#else
	W_UINT (*m_pfnTask) (W_UINT iTaskId, CBinary idSelectedText);
#endif

	
public:
	CMenuTree();
	CMenuTree(string strMenuDbFileName);
	virtual ~CMenuTree();

public:
	int ShowMenu (CBinary binIDMenu);

#ifndef _TASKIDTYPE
	void* SetTaskCallBackFunction(W_INT16 (*pfnTask) (W_INT16 iTaskId, CBinary idSelectedText));
#else
	void* SetTaskCallBackFunction(W_UINT (*pfnTask) (W_UINT iTaskId, CBinary idSelectedText));
#endif

protected:
	string m_strFileMenuDb;


//Self添加,处理菜单
private:
	//菜单层。如[1,4]表示TAB=0的第一个，TAB=1的第四个  的下一层菜单显示出来。 为空表示第一层菜单
	vector<WORD> m_vecMenuLayer;
};

#endif // !defined(AFX_MENUTREE_H__3FB0F313_D135_4EFF_9018_220711A2BAA0__INCLUDED_)
