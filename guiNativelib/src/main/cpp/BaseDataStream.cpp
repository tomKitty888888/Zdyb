// BaseDataStream.cpp: implementation of the CBaseDataStream class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "BaseDataStream.h"
#include "Display.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

extern CGui g_Gui;

CBaseDataStream::CBaseDataStream()
{
	m_bShowDataStreamSelectWindow = true;
}

CBaseDataStream::~CBaseDataStream()
{

}

void CBaseDataStream::EnableShowMultiSelected (bool bEnable = true)
{
	m_bShowDataStreamSelectWindow = bEnable;
}
