// ShortTestShow.cpp: implementation of the CShortTestShow class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "ShortTestShow.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

extern CGui g_Gui;

//////////////////////////////////////////////////////////////////////////
//
//
//	说明, 快速测试目前没有使用到, 所以这里都留着空函数
//
//


CShortTestShow::CShortTestShow()
{

}

CShortTestShow::~CShortTestShow()
{

}


W_INT16 CShortTestShow::GetEnterIndex(void)
{
	return m_nEnterIndex;
}

void CShortTestShow::Init (const char* pTitle)
{
}

void CShortTestShow::Init (string strTitle)
{
}
void CShortTestShow::Init (CBinary idTitle)
{
}

W_INT16 CShortTestShow::AddItem (const char* pContain)
{
	return 0;
}
W_INT16 CShortTestShow::AddItem (string strContain)
{
	return 0;
}
W_INT16 CShortTestShow::AddItem (CBinary idContain)
{
	return 0;
}
W_INT16 CShortTestShow::ChangeValue (CBinary idValue)
{
	return 0;
}
W_INT16 CShortTestShow::ChangeValue (string strValue)
{
	return 0;
}
W_INT16 CShortTestShow::ChangeValue (const char* pValue)
{
	return 0;
}

W_INT16 CShortTestShow::Show ()
{
	return 0;
}




