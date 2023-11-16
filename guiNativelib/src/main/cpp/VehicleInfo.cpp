// VehicleInfo.cpp: implementation of the CVehicleInfo class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "VehicleInfo.h"
#include "Display.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

extern CGui g_Gui;


CVehicleInfo::CVehicleInfo()
{

}

CVehicleInfo::~CVehicleInfo()
{

}

void CVehicleInfo::Init (const char *pTitle)
{
	g_Gui.VerInit();
}
void CVehicleInfo::Init (string strTitle)
{
	Init();
}
void CVehicleInfo::Init (CBinary idTitle)
{
	Init();
}


W_INT16 CVehicleInfo::Add (string strExplain, string strContain)
{
	g_Gui.VerAdd((char *)strExplain.c_str(),(char *)strContain.c_str());

	return 0;
}
W_INT16 CVehicleInfo::Add (CBinary idExplain, string strContain)
{
	BYTE *pbExplain = new BYTE [idExplain.GetSize()+1];
	WORD wLen = 0;
	CDisplay dis;
	if (!dis.BinaryToBytePoint(idExplain,pbExplain,wLen))
	{
		delete [] pbExplain;
		return -1;
	}

	char szExPlain[1024];
	g_Gui.GetText(pbExplain,(BYTE)wLen,szExPlain);

	string strExplain = szExPlain;

	delete [] pbExplain;

	return Add(strExplain,strContain);
}


W_INT16 CVehicleInfo::Show ()
{
	BYTE bKey = g_Gui.VerShow();
	if (bKey == ID_MENU_BACK)
	{
		return adsIDBACK;
	}

	return 0;
}
