
#ifndef	__RECEIVEFRAME_H_
#define	__RECEIVEFRAME_H_

#include "adsStd.h"
#include <vector>
using namespace std;
#include "Binary.h"

#ifndef CBinaryGroup
#define CBinaryGroup vector<CBinary>
#endif


class CReceiveFrame
{
public:
	CReceiveFrame(void);
	~CReceiveFrame(void);

protected:
	vector< CBinaryGroup > m_ReceiveFrame;

public:
	// 复制接收帧
	void operator = (CReceiveFrame ReceiveFrame );

	// 加入新发送帧数据
	void operator += ( CBinaryGroup& ReceiveGroup );

	// 取得某一发送帧的应答帧
	CBinaryGroup& operator[] (const W_INT16 iFrameOrder);

	// 取得发送帧的数量
	W_INT16 GetSendFrameNumber ();

	//W_INT16 SetSendFrameNumber (W_INT16 iSendFrameNumber);

	// 取得第一发送帧的应答帧
	CBinary GetFirstAnswerFrame();

	// 取得某一发送帧的应答帧
	CBinaryGroup& GetOneFrameAnswer (const W_INT16 iFrameOrder);

	// 取得接收帧的数组大小
	W_INT16 size();

	// 清空接收帧数据
	void clear();
};

#endif	//__RECEIVEFRAME_H_

