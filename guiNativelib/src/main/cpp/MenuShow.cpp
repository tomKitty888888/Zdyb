// MenuShow.cpp: implementation of the CMenuShow class.
//
//////////////////////////////////////////////////////////////////////

#include "MenuShow.h"
#include "Display.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

extern CGui g_Gui;

CMenuShow::CMenuShow()
{

}

CMenuShow::~CMenuShow()
{

}

void CMenuShow::Init(const char *pTitle)
{
	g_Gui.MenuInit();
}

void CMenuShow::Init(string strTitle)
{
	g_Gui.MenuInit();
}

void CMenuShow::Init(CBinary idTitle)
{
	g_Gui.MenuInit();
}

void CMenuShow::Init(UINT nMenuNum)
{
	g_Gui.MenuExInit(nMenuNum);
}

bool CMenuShow::Add(string strMenuItem, UINT nCtrlFlag)
{
	g_Gui.MenuExAdd((char *)strMenuItem.c_str(), nCtrlFlag);

	return true;
}

bool CMenuShow::Add(CBinary idMenuItem, UINT nCtrlFlag)
{
	BYTE *pbMenuItem = new BYTE[idMenuItem.GetSize() + 1];
	WORD wLen = 0;
	CDisplay dis;
	if (!dis.BinaryToBytePoint(idMenuItem, pbMenuItem, wLen))
	{
		delete[] pbMenuItem;
		return false;
	}

	char szMenuItem[255];
	g_Gui.GetText(pbMenuItem, wLen, szMenuItem);
	string strMenuItem = szMenuItem;

	delete[] pbMenuItem;

	return Add(strMenuItem, nCtrlFlag);
}
bool CMenuShow::Add(CBinary idMenuItem)
{
	BYTE *pbMenuItem = new BYTE [idMenuItem.GetSize()+1];
	WORD wLen = 0;
	CDisplay dis;
	if (!dis.BinaryToBytePoint(idMenuItem,pbMenuItem,wLen))
	{
		delete [] pbMenuItem;
		return false;
	}

	char szMenuItem[255];
	g_Gui.GetText(pbMenuItem,wLen,szMenuItem);
	string strMenuItem = szMenuItem;

	delete [] pbMenuItem;

	return Add(strMenuItem);
}
bool CMenuShow::Add(vector<CBinary> vecIdMenuItem, UINT nCtrlFlag)
{
	if (vecIdMenuItem.size() == 0)
	{
		return false;
	}

	vector<string> vecStrMenuItem;
	vecStrMenuItem.clear();

	for (UINT i = 0; i < vecIdMenuItem.size(); i++)
	{
		BYTE *pbMenuItem = new BYTE[vecIdMenuItem[i].GetSize() + 1];
		if (pbMenuItem == NULL)
		{
			return false;
		}

		WORD wLen = 0;
		CDisplay dis;
		if (!dis.BinaryToBytePoint(vecIdMenuItem[i], pbMenuItem, wLen))
		{
			if (pbMenuItem)
			{
				delete[] pbMenuItem;
				pbMenuItem = NULL;
				return false;
			}
		}

		char szMenuItem[255];
		g_Gui.GetText(pbMenuItem, wLen, szMenuItem);

		string strMenuItem = szMenuItem;

		vecStrMenuItem.push_back(strMenuItem);

		if (pbMenuItem)
		{
			delete[] pbMenuItem;
			pbMenuItem = NULL;
		}
	}

	return Add(vecStrMenuItem, nCtrlFlag);
}

bool CMenuShow::Add(vector<string> vecStrMenuItem, UINT nCtrlFlag)
{
	g_Gui.MenuExAdd(vecStrMenuItem, nCtrlFlag);

	return true;
}

W_INT16 CMenuShow::Show(vector<UINT>& vecIndex)
{
	BYTE bKey = g_Gui.MenuExShow(vecIndex);

	return bKey;
}


bool CMenuShow::Add(string strMenuItem)
{
	g_Gui.MenuAdd((char *)strMenuItem.c_str());

	return true;
}

W_INT16 CMenuShow::Show(CMenuStruct &MenuParameter)
{
	BYTE bKey = g_Gui.MenuShow();
	if (bKey == ID_MENU_BACK)
	{
		MenuParameter.m_i16MenuSelected = -1;
	}
	else
	{
		MenuParameter.m_i16MenuSelected = bKey;
	}

	return MenuParameter.m_i16MenuSelected;
}

void CMenuShow::GetComboDropIndex(vector<UINT>& vecComboDropInex)
{
	UINT nComboDropIndex = 0, nComboCount = 0;
	nComboCount = (UINT)g_Gui.m_pMapGuiBuff[0];
	for (UINT i = 0; i < nComboCount; i++)
	{
		nComboDropIndex = (UINT)g_Gui.m_pMapGuiBuff[i+1];
		vecComboDropInex.push_back(nComboDropIndex);
	}	
}
//////////////////////////////////////////////////////////////////////////
// 原先是用来分屏用,这里不需要处理
W_INT16 CMenuShow::SetFlag (W_INT16 iFlag)
{
	return 0;
}