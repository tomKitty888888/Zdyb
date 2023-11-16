// StdDataStream.cpp: implementation of the CStdDataStream class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "StdDataStream.h"
#include "Display.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

//extern CDisplay adsDisplay;

CStdDataStream::CStdDataStream()
{

}

CStdDataStream::~CStdDataStream()
{

}

W_INT16 CStdDataStream::ReadDataStream (vector<CBinary> *paidDataStream)
{

	//test
	//adsDisplay.MessageBox("ReadDataStream NULL","ReadDataStream NULL");

	return 0; //有没有用到??
}

//第一次查库的时候已经记住了每项数据流后面的内容
void CStdDataStream::DsIdToSendFrame (void)
{
	//
}

//原来的函数就是空的
void CStdDataStream::OnBeforeSendDsCmd ()
{

}
