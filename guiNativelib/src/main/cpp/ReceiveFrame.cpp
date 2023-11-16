/******************************************************************************

	Copyright (c) 2012, XXX Inc. 
	All rights reserved.

******************************************************************************/
#include <assert.h>
#include "ReceiveFrame.h"


CReceiveFrame::CReceiveFrame(void)
{
	m_ReceiveFrame.clear();
}

CReceiveFrame::~CReceiveFrame(void)
{
}

/*****************************************************************
功    能：复制接收帧
参数说明：const CReceiveFrame& ReceiveFrame 接收帧类
返 回 值：无
说    明：无
*****************************************************************/
void CReceiveFrame::operator = (CReceiveFrame ReceiveFrame )
{
	CBinaryGroup groupBin;
	vector< CBinaryGroup >::iterator vIter;

	m_ReceiveFrame.clear();
	for(vIter = ReceiveFrame.m_ReceiveFrame.begin(); vIter != ReceiveFrame.m_ReceiveFrame.end(); vIter++)
	{
		groupBin.assign(vIter->begin(), vIter->end());
		m_ReceiveFrame.push_back(groupBin);
	}
}


/*****************************************************************
功    能：加入新发送帧数据
参数说明：CBinaryGroup& ReceiveGroup 接收组数据
返 回 值：无
说    明：无
*****************************************************************/
void CReceiveFrame::operator += ( CBinaryGroup& ReceiveGroup )
{
	m_ReceiveFrame.push_back(ReceiveGroup);
}


/*****************************************************************
功    能：取得某一发送帧的应答帧
参数说明：发送帧序号
返 回 值：该发送帧对应的接收帧数组的引用
说    明：无
*****************************************************************/
CBinaryGroup& CReceiveFrame::operator[] (const W_INT16 iFrameOrder)
{
	assert( (0 <= iFrameOrder) && (iFrameOrder < (W_INT16)m_ReceiveFrame.size()) );

	return m_ReceiveFrame[iFrameOrder];
}


/*****************************************************************
功    能：取得发送帧的数量
参数说明：无
返 回 值：发送帧的数量
说    明：无
*****************************************************************/
W_INT16 CReceiveFrame::GetSendFrameNumber (void)
{
	W_INT16 iSize = m_ReceiveFrame.size();
	return iSize;
}


///*****************************************************************
//功    能：设置发送帧的数量
//参数说明：W_INT16 iSendFrameNumber发送帧的数量
//返 回 值：错误代码
//说    明：无
//*****************************************************************/
//W_INT16 CReceiveFrame::SetSendFrameNumber (W_INT16 iSendFrameNumber)
//{
//	W_INT16 iRet = 0;
//
//	return iRet;
//}
//

/*****************************************************************
功    能：取得第一发送帧的应答帧
参数说明：
返 回 值：
说    明：无
*****************************************************************/
CBinary CReceiveFrame::GetFirstAnswerFrame()
{
	CBinary binReceive;

	if(0 == m_ReceiveFrame.size())
	{
		return binReceive;
	}

	if(0 == m_ReceiveFrame[0].size())
	{
		return binReceive;
	}

	return m_ReceiveFrame[0][0];
}


/*****************************************************************
功    能：取得某一发送帧的应答帧
参数说明：发送帧序号
返 回 值：该发送帧对应的接收帧数组的引用
说    明：无
*****************************************************************/
CBinaryGroup& CReceiveFrame::GetOneFrameAnswer (const W_INT16 iFrameOrder)
{
	assert( (0<=iFrameOrder) && (iFrameOrder < (W_INT16)m_ReceiveFrame.size()) );
	return m_ReceiveFrame[iFrameOrder];
}


/*****************************************************************
功    能：返回接收帧的数组尺寸
参数说明：无
返 回 值：接收帧数组的尺寸
说    明：无
*****************************************************************/
W_INT16 CReceiveFrame::size()
{
	W_INT16 iSize = m_ReceiveFrame.size();
	return iSize;
}


/*****************************************************************
功    能：清空接收帧数据
参数说明：无
返 回 值：无
说    明：无
*****************************************************************/
void CReceiveFrame::clear()
{
	m_ReceiveFrame.clear();
}
