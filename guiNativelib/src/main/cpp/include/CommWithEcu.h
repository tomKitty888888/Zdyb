// CommWithEcu.h: interface for the CCommWithEcu class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_COMMWITHECU_H__9B95D037_4E68_42F9_9052_82985640D907__INCLUDED_)
#define AFX_COMMWITHECU_H__9B95D037_4E68_42F9_9052_82985640D907__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "adsStd.h"
#include "Com.h"
#include "Binary.h"
#include "SendFrame.h"
#include "ReceiveFrame.h"
#include "stdlib.h"

#include "RunLog.h"

#include "Gui.h"
#include "J1939.h"

// 设置输入输出端口及通信参数  //选线只改了几个引脚,如PORT_XX, 如其它引脚有用到,再改
//#define DB15_PIN0_INPUT_GROUND											  0x00
#define DB15_PIN1														  PORT_01//0x01
#define DB15_PIN2_INPUT													  PORT_02//0x02
#define DB15_PIN3														  PORT_03//0x03
#define DB15_PIN4_OUTPUT_INVALIDATION									  PORT_04//0x04
#define DB15_PIN4_INPUT_UNCONNECT										  PORT_04//0x04
#define DB15_PIN5														  PORT_15//0x05  //注意5对应15
#define DB15_PIN6														  PORT_06//0x06
#define DB15_PIN7														  PORT_07//0x07
#define DB15_PIN8														  PORT_08//0x08
#define DB15_PIN9														  PORT_09//0x09
#define DB15_PIN10_INPUT												  PORT_10//0x0A
#define DB15_PIN11														  PORT_11//0x0B
#define DB15_PIN12														  PORT_12//0x0C
#define DB15_PIN13														  PORT_13//0x0D
#define DB15_PIN14														  PORT_14//0x0E
#define DB15_PIN15_INPUT_WORK_VOLTAGE									  PORT_16//0x0F /注意15对应16
//#define DB15_PIN14_153_6kHz												  0x1E
//#define DB15_PIN1_HIGH_ELECTRICITY										  0x11
//#define DB15_PIN5_HIGH_ELECTRICITY										  0x15
//#define DB15_VPW														  0x22
//#define DB15_PWM														  0x42
#define DB15_CAN_PIN6_14												  0x86
//#define DB15_CAN_PIN3_11												  0x83
#define DB15_CANBUS														  0x86	//为兼容而保留，建议用DB15_CAN_PIN6_14来替代
#define DB15_NO_SWITCH_CHANNEL											  0xFF//用于且仅用于GetAdcStatus(),GetChannelVoltage()函数参数
#define IOS_INPUT_POSITIVE_LOGIC         0x01
//#define IOS_INPUT_NEGATIVE_LOGIC         0x00
#define IOS_OUTPUT_POSITIVE_LOGIC        0x02
//#define IOS_OUTPUT_NEGATIVE_LOGIC        0x00
//#define IOS_PULL						 0x00 //仅为与前一版本兼容，已无实际意义
//#define IOS_INPUT_PULL_UP				 0x40 //日产加入输入上拉。2006.7.19
//#define IOS_OUTPUT_REVERSE				 0x80


class CCommWithEcu  
{
public:
	CCommWithEcu();
	virtual ~CCommWithEcu();

public:
	void Init (void);
	void Destroy (void);

	void Begin (void);
	CReceiveFrame End (void);
	//self
	bool m_bBeginIsRunning;
	CSendFrame m_SendFrame;
	CReceiveFrame m_RecvFrame;
	CBinary Byte2Binary(BYTE *pbBuf, WORD wLen);
	void CanConversion(CBinary binCmd,vector<CBinary> &vecbinCmd);
	CReceiveFrame SendReceive_CanBus (CSendFrame& SendFrame, bool bRepeat=false);





	void WaitCommPacketMaxTime (W_UINT32 iMaxTime);

	W_INT16 SetIoPort (	W_UINT8 iIoOutputPort,
						W_UINT8 iIoInputPort, 
						W_INT8 iOutputVoltage = 12,
						W_UINT16 ui16IoSelectItem = IOS_INPUT_POSITIVE_LOGIC | IOS_OUTPUT_POSITIVE_LOGIC);

	W_INT16 EnableOutputIoLine (W_UINT8 iIoOutputPort, bool bIsConnect = TRUE);

//PB_  party bit //===这个还没用到,用到再改
#define PB_SPACE														4 //0校验
#define PB_MARK															3 //1校验
#define PB_EVEN															2 //偶校验
#define PB_ODD															1 //奇校验
#define PB_NONE															0 //无校验

	W_INT16 SetBps (W_FLOAT fBps, W_INT8 iParityBit = PB_NONE, W_INT8 iDataBit = 8);
	W_INT16 SetBRs (W_UINT8 BR1,W_UINT8 BR2,W_INT8 iParityBit = PB_NONE, W_INT8 iDataBit = 8);

	W_INT16 TimeIntervalOver (W_INT32 iMaxWaitReceivePacket,
							  W_INT32 iSendPacketMinWaitTime,
							  W_FLOAT fSendByteMinWaitTime);

	W_INT16 KeepLink (W_INT16 iKeepTime = 0,  CSendFrame *pKeepLinkCommand = NULL);

	W_INT16 BoschKeepLink (bool bEnableKeep = false);

	W_INT16 VoltageHighLowTime (W_INT16 iHighLowTimes, ...);
	W_INT16 VoltageHighLowTenthTime (W_INT16 iHighLowTimes, ...);

	//设置高速通信方式 0 低速  1 高速 注：在退出高速的诊断方式时一定要还原为低速设置SetHighSpeed(0);
	W_INT16 SetHighSpeed(BYTE pParameter);

	//设置BMW特殊方式(发命令之前先高低电平触发)通信方式命令字 0 无效  1 有效 注：在退出时一定要还原为无效设置SetBMWSpecial(0);
	W_INT16 SetBMWSpecial(BYTE pParamerer);


// 地址码进入系统，省略的参数必须为W_INT16类型
#ifndef ACE_BPS_AUTO_RECOGNIZE
#define ACE_BPS_AUTO_RECOGNIZE           ADDR_AUTO_BPS_RECOGNIZE//0x01
#endif
#ifndef ACE_KW2_REVERSE_TO_ECU
#define ACE_KW2_REVERSE_TO_ECU           ADDR_KW2_INVERSE_TO_ECU//0x02
#endif
#ifndef ACE_GET_ADDRESS_REVERSE_FROM_ECU
#define ACE_GET_ADDRESS_REVERSE_FROM_ECU ADDR_INVERSE_FROM_RCU//0x04
#endif
#ifndef ACE_RECEIVE_KW5
#define ACE_RECEIVE_KW5					 ADDR_RECV_KW5BYTE//0x08
#endif
#ifndef ACE_RECEIVE_ONE_FRAME
#define ACE_RECEIVE_ONE_FRAME			 ADDR_RECV_ONE_FRAME//0x10
#endif
#ifndef ACE_KW1_REVERSE_TO_ECU
#define ACE_KW1_REVERSE_TO_ECU			 ADDR_KW1_INVERSE_TO_ECU//0x20
#endif
#ifndef ACE_RECEIVE_N_FRAME
#define ACE_RECEIVE_N_FRAME				 ADDR_RECV_MUTI_FRAME//0x40
#endif
#ifndef ACE_CLOSE_TRIGGER_CHANNEL
#define ACE_CLOSE_TRIGGER_CHANNEL		 ADDR_CLOSE_TIGGER//0x80
#endif

	CReceiveFrame AddressCodeEnter (BYTE ucAddressCode, 									
									W_UINT16 ui16Parameter = ACE_BPS_AUTO_RECOGNIZE | ACE_KW2_REVERSE_TO_ECU | ACE_GET_ADDRESS_REVERSE_FROM_ECU,
									W_FLOAT fBps = 5,
									W_INT16 i16ReverseKw2TimeInterval = 30);
	CReceiveFrame BoschEnter (BYTE ucAddressCode,	W_INT16 i16ReverseKw2TimeInterval = 30);

// 取得下位机状态, 仅用于非组合发送  =======目前不知道要做什么,暂时没有改动
//GS_ Get Status
#define GS_CHANNEL_SIGNAL											  0x00
#define GS_CHANNEL_VOLTAGE											  0x01
W_INT16 GetStatus(W_UINT8 ui8StatusWay, BYTE ucIoInputPort = DB15_NO_SWITCH_CHANNEL);


//这里只把用到的宏重定义了一下
#define PROTOCOL_NORMAL					PROTOCOL_M_NORMAL//0x00
#define	PROTOCOL_KWP					PROTOCOL_M_KWP//0x01
#define	PROTOCOL_ISO					PROTOCOL_M_ISO//0x02
//#define	PROTOCOL_VPW					0x03
//#define	PROTOCOL_PWM					0x04
#define	PROTOCOL_CANBUS					PROTOCOL_M_CAN//0x05
//#define	PROTOCOL_CAN_SINGLE_LINE		0x85
#define	PROTOCOL_BOSCH					PROTOCOL_M_BOSCH//0x06



	class CFilterOrLengthRecognise
	{
	public:
		CFilterOrLengthRecognise();

	public:
		void Empty();
		void SetNormalFilterMode(W_INT16 iStartPosition, W_INT16 iFilterSizeLength, char *chFilterContain);
		void SetCanFilterMode(W_INT16 iFilterPosition,W_INT16 iFilterMask,W_INT16 iLengthPosition);
		void AddCanFilterId(CBinary binCanFilterId);
		void SetNormalLengthRecogniseMode(W_INT16 iBytePosition, BYTE ucMark, W_INT16 iAddValue);
		void SetSpecifiedLengthMode();

	private:
		W_INT16 m_iLenth;
		W_INT16 m_iNum;
		char m_chBuffer[300];

	public:
		W_INT16 GetLenth();
		W_INT16 GetNum();
		char *GetBuffer();
	};


	W_INT16 ProtocolSet (W_INT16 iProtocolWay, CFilterOrLengthRecognise *pFilterOrLengthRecognise = NULL);
	W_INT16 SetProtocol (W_INT16 iProtocolWay);//for K-CAN
	W_INT16 ControlKLine (W_INT16 iEnable);//for K-CAN
	W_INT16 SetVWCanSpecial(W_INT16 iEnable);//for VW CAN
	W_INT16 SetBenzSpecial(W_INT16 iEnable);
	W_INT16 SetHoldonSpecial(W_INT16 iFilterByte);


	// 发送数据给ECU接收应答
	CReceiveFrame SendReceive (CSendFrame& SendFrame, bool bRepeat = false);
	CReceiveFrame SendReceiveEx (CSendFrame& SendFrame, bool bRepeat=false);
	CReceiveFrame CanSendReceive (CSendFrame& SendFrame, bool bRepeat = false);
	CReceiveFrame CumSendReceive (CSendFrame& SendFrame, bool bRepeat = false);

	CReceiveFrame ReceiveOnly (bool bClearBuffer=false);

	W_INT16 SetAutoSendContinuedFrame (CFilterOrLengthRecognise flrFilterCondition, CSendFrame sfSendContinuedFrame);








	void SetDemoFrame(CBinaryGroup bgRecvData);
	CRunLog *GetRunLog(void);
	void CommBlockStart();
	void CommBlockEnd();
	
};

#endif // !defined(AFX_COMMWITHECU_H__9B95D037_4E68_42F9_9052_82985640D907__INCLUDED_)
