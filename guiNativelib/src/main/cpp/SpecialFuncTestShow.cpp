// SpecialFuncTestShow.cpp: implementation of the CSpecialFuncTestShow class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "SpecialFuncTestShow.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

CSpecialFuncTestShow::CSpecialFuncTestShow()
{

}

CSpecialFuncTestShow::~CSpecialFuncTestShow()
{

}




void CSpecialFuncTestShow::Init(const char *pTitle /* = NULL */)
{
	m_Act.Init(pTitle);
}
void CSpecialFuncTestShow::Init(string strTitle)
{
	Init();
}
void CSpecialFuncTestShow::Init(CBinary idTitle)
{
	Init();
}

W_INT16 CSpecialFuncTestShow::AddButton(CBinary idButtonText, char byStatus)
{
	return m_Act.AddButton(idButtonText,byStatus);
}

W_INT16 CSpecialFuncTestShow::Add(CBinary idDataStream, string strDataStreamValue)
{
	return m_Act.Add(idDataStream,strDataStreamValue);
}
W_INT16 CSpecialFuncTestShow::Add(CBinary idDataStream, string strDataStreamValue, CBinary idUnit)
{
	return m_Act.Add(idDataStream,strDataStreamValue,idUnit);
}
//W_INT16 CSpecialFuncTestShow::Add(string strMsg)
//{
//	//不知道哪里有用到,暂不实现.
//	return 0;
//}

W_INT16 CSpecialFuncTestShow::AddPrerequisites(const char* strMsg)
{
	//是添加测试条件用的提示消息.  Benz有调用到, 暂不实现
	return 0;
}

W_INT16 CSpecialFuncTestShow::AddProcessCtrl(W_INT16 nMin,W_INT16 nMax,W_INT16 nSteps)
{
	//用来显示执行的进度, 暂不实现
	return 0;
}

W_INT16 CSpecialFuncTestShow::SetFlag(W_INT16 iFlag)
{
	return m_Act.SetFlag(iFlag);
}


W_INT16 CSpecialFuncTestShow::Show()
{
	return m_Act.Show();
}
