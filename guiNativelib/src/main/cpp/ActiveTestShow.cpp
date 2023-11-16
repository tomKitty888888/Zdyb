// ActiveTestShow.cpp: implementation of the CActiveTestShow class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "ActiveTestShow.h"
#include "Display.h"
#include "Debug.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

extern CGui g_Gui;

CActiveTestShow::CActiveTestShow()
{

}

CActiveTestShow::~CActiveTestShow()
{

}


W_UINT8 CActiveTestShow::AcceptMsg ()  //还没写
{
	return 0;
}


void CActiveTestShow::Init (const char* pTitle)
{
	g_Gui.ActInit();
}
void CActiveTestShow::Init (string strTitle)
{
	Init();
}
void CActiveTestShow::Init (CBinary idTitle)
{
	Init();
}

W_INT16 CActiveTestShow::AddButton (CBinary idButtonText, char byStatus)
{
	WORD wLen = idButtonText.GetSize();
	BYTE *pButtonText = new BYTE [wLen+1];
	CDisplay dis;
	dis.BinaryToBytePoint(idButtonText,pButtonText,wLen);
	char szButtonText[255];
	g_Gui.ActAddButton(pButtonText,(BYTE)idButtonText.GetSize(),szButtonText,byStatus);

	delete [] pButtonText;

	return 1;
}
W_INT16 CActiveTestShow::AddPrompt (CBinary idPromptText)
{
	WORD wLen = idPromptText.GetSize();
	BYTE *pPromptText = new BYTE [wLen+1];
	CDisplay dis;
	dis.BinaryToBytePoint(idPromptText,pPromptText,wLen);
	g_Gui.ActAddPrompt(pPromptText,(BYTE)wLen);

	return 1;
}
W_INT16 CActiveTestShow::AddPrompt (char *pszText)
{
	g_Gui.ActAddPrompt(pszText);

	return 1;
}

W_INT16 CActiveTestShow::AddPrompt(const char *pszText)
{
	g_Gui.ActAddPrompt(pszText);

	return 1;
}


W_INT16 CActiveTestShow::Add (CBinary idDataStream, string strDataStreamValue)
{
	return Add(idDataStream,strDataStreamValue,""); //这种方式单位为空
}
W_INT16 CActiveTestShow::Add (CBinary idDataStream, string strDataStreamValue, CBinary idUnit)
{
	string strUnit = adsGetTextString(idUnit);

	return Add(idDataStream,strDataStreamValue,strUnit);
}
W_INT16 CActiveTestShow::Add (CBinary idDataStream, string strValue1, string strValue2)
{
	CDisplay dis;
	BYTE nTextID[255] = {0,};
	WORD wLen = 0;
	if (!dis.BinaryToBytePoint(idDataStream,nTextID,wLen))return 0;

	char szText[255] = "";
	if (!g_Gui.GetTextEx(TEXT_CDS,nTextID,(BYTE)wLen,szText))return 0;
	string strDataStream = szText;

	g_Gui.ActAdd((char *)strDataStream.c_str(), (char *)strValue1.c_str(), (char *)strValue2.c_str());

	return 1;
}


W_INT16 CActiveTestShow::Show ()
{
	BYTE bKey = g_Gui.ActShow();
	if (bKey == ID_MENU_BACK)  //FF:返回
	{
		return -1;//adsIDBACK;
	}
	return bKey; //按钮的索引
}
W_INT16 CActiveTestShow::Show (W_INT16 &iSelNum)
{
	return Show(); //暂时调用Show();   可扩展用户选择项目
}







W_INT16 CActiveTestShow::AddMsg(string strMsg)
{
	MessageBox(NULL,"开瑞的 CTIGGO3_MGH25_ABS 中有调用到adsDisplay.ActiveTest.AddMsg(...)","未开发",MB_OK);

	return 0;
}

W_INT16 CActiveTestShow::SetFlag (W_INT16 iFlag)
{
	//开瑞的 CTIGGO3_MGH25_ABS 中有调用到, 本来是PC做分页菜单处理的, 这里忽略.
	return 0;
}
