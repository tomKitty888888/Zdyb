#ifndef __SENDFRAME_H_
#define __SENDFRAME_H_

#include <vector>
using namespace std;
#include "adsStd.h"
#include "Binary.h"


class CSendFrame
{
public:
//RFN_ RECEIVE FRAME NUMBER
	enum {
		RFN_AUTO_DISTINGUISH = 0xFF,
		RFN_COUNTLESS = 0xFE
	};

public:
	class COneSendFrame 
	{
	public:
		CBinary m_binSendCmd;
		W_INT16 m_iReceiveFrameNumber;
	};

protected:
	static int m_iDefaultReceiveFrameNumber;
	vector<COneSendFrame> m_SendFrame;

public:
	CSendFrame(void);
	~CSendFrame(void);

public:
	void operator = (CSendFrame SendFrame);

	void operator = (vector<CBinary>& binCmd);
	void operator += (vector<CBinary>& binCmd);

	void operator = (const CBinary binData);
	void operator += (CBinary binData);

	void operator += (CSendFrame& SendFrame);
	void operator += (COneSendFrame& oneSendFrame);
	CSendFrame::COneSendFrame operator [] (W_INT16 nIndex);

	// 设置缺省接收帧帧数
	W_INT16	SetDefaultReceiveFrameNumber (W_INT16 iDefaultReceiveFrame = 1);

	// 加入命令库ID对应的发送帧
	bool AddFromCmdLib(CBinary idCmd);

	// 追加发送帧
	W_INT16	AddSendFrame(CSendFrame& SendFrame);

	// 清空发送帧
	void Clear(void);

	// 取得发送帧数量
	W_INT16	GetFrameNumber(void);

	// 得到发送帧的返回帧数
	W_INT16 GetAllReceiveFrameNumber(void);

	// 得到发送帧中单帧的返回帧数
	W_INT16 GetOneReceiveFrameNumber(int iOrder);

	// 将发送帧命令内容按“[长度＋内容]...”的格式输出到指定的缓冲区,返回填充长度
	W_INT PutCmdDataToBuffer(unsigned char* dataBuf, W_INT bufLength);
};

#endif	// __SENDFRAME_H_
