// Com.h: interface for the CCom class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_COM_H__7C33D3F0_12E5_4FE8_9DBE_A1D2FF221BCD__INCLUDED_)
#define AFX_COM_H__7C33D3F0_12E5_4FE8_9DBE_A1D2FF221BCD__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000


#include <stdio.h>
//#include <windows.h>

#include <string>
#include <map>

using namespace std;

#include "RunEnvironment.h"

#include "Gui.h"
#include "Binary.h"

typedef unsigned char		BYTE;
typedef unsigned short		WORD;
typedef unsigned long		DWORD;


//DWORD g_dwProduct
#define PRODUCT_EPS918_PRO	0x0001
#define PRODUCT_EPS918_STD	0x0002
#define PRODUCT_EPS918_DC	0x0004
#define PRODUCT_YTOPOWER	0x0008
#define PRODUCT_EPS916	0x0010


#define CAN_STD	0
#define CAN_EXT 1


class CCom  
{
public:
	bool SetParity_UART2(BYTE bParity);
	bool SetCanContinueFrame(BYTE *pbContinueFrame,BYTE nContinueFrameLen);
	bool SetCanFilter(WORD wCanID);
	WORD KwpFlashInit(BYTE *pbSend,BYTE bLen,BYTE *pbRecv);
	WORD AddressCodeEnter(BYTE bAddr,BYTE bParam=0x19,BYTE bKey2Time=30);
	WORD AddressCodeEnter_Bosch(BYTE *bRecv,BYTE bAddr,BYTE bParam=0x19,BYTE bKey2Time=30);
	bool SetChannel(BYTE bPortOne,BYTE nPortTwo);
	bool SetKeepLink(DWORD dwTime=0,BYTE *pbSend=NULL,BYTE bSendLen=0);
	WORD RecvCmdOnly(BYTE *pbRecv);
	WORD SendCmdOnly(BYTE *pbSend, WORD wSendLen);
	WORD SendRecvCmd(BYTE *pbSend,WORD wSendLen,BYTE *pbRecv);
	WORD SendRecvMultiCmd(BYTE *pbSend,WORD wSendLen,BYTE *pbRecv);
	bool SetVoltage(WORD wNumber,...);
	bool SetTime(DWORD dwMaxRecv,DWORD dwSendSpace,DWORD dwSendB2B);
	bool SetBaudRate(DWORD dwBps);
	bool SetProtocol(BYTE bProtocol, BYTE *pParam=NULL);

	void SetErrorCode(BYTE bErrorCode);
	BYTE GetErrorCode(void);

	bool SetParity(BYTE bParity);
	bool SetBaud(DWORD dwBaud);
	
	DWORD m_dwCanSendID;
	bool m_bStandardCan;  //是不是标准CAN(是则为前面带长度字节的)
	void SetCanSendID(DWORD dwCanID,bool bCan=false){m_dwCanSendID=dwCanID;m_bStandardCan=bCan;}
	BYTE SendRecvCanData(BYTE *nSendBuf,BYTE nSendLen,BYTE *nRecvBuff);
//	BYTE SendRecvKwpData(BYTE *nSendBuf,BYTE nSendLen,BYTE *nRecvBuff,BYTE n4=0x33);
	WORD SendRecvKwpData(BYTE *nSendBuf,BYTE nSendLen,BYTE *nRecvBuff,BYTE n4=0x33);
	bool ResetMcu();
	void SetMcuChecksum(BYTE *nBuf,WORD nLen);
//	bool SetKwp2Mcu(DWORD dwBaud,DWORD dwPort,BYTE bLogicV1=0xC1,BYTE bLogicV2=0xC1,\
//		CBinary binTimes=CBinary("\x10\x19\x50\x05\x04\x00\x32",7));
	bool SetKwp2Mcu(DWORD dwBaud,DWORD dwPort,BYTE bLogicV1=0xC6,BYTE bLogicV2=0xC1,\
		CBinary binTimes=CBinary((BYTE *)"\x15\xF0\x00\x00\x04\x00\x19",7));
	bool SetCan2Mcu(DWORD dwBaud,DWORD dwSendCanID,DWORD dwRecvCanID,CBinary bin30=0,BYTE bMode=CAN_STD,BYTE bPin=0xE6);
	bool RecvKwpOneFrame(BYTE *nRecvBuf,WORD &nRecvLen);
	bool RecvCanOneFrame(BYTE *nRecvBuf,WORD &nRecvLen);
	
	bool SendRecvCmd2Mcu_KWP(BYTE *nSendBuf,WORD nSendLen,BYTE *nRecvBuff,WORD &nRecvLen);
	bool SendRecvCmd2Mcu(BYTE *nSendBuf,WORD nSendLen,BYTE *nRecvBuff,WORD &nRecvLen);
	bool RecvCmd(BYTE *nBuff, DWORD nLen);
	bool RecvCmdEx(BYTE *nBuff, DWORD nLen,DWORD dwTimeout=6*1000);
	DWORD RecvCmdTime(BYTE *nBuff,DWORD dwRecvCount=100);
	DWORD RecvCmdTimeout(BYTE *nBuff, DWORD dwCount, DWORD dwTimeout); //收到指定个数或超时返回(ms)
	void PurgeCommEx();
	bool RecvCmd_OneBytOne(BYTE *nBuff, DWORD nLen);

	bool SetBluetoothLedStatus(BYTE bLedStatus);

	bool SendCmd(BYTE *nBuff, DWORD nLen);
	bool ComClose(void);
	bool ComOpen(BYTE nPort);
	bool ComOpen(void);
	CCom();
	virtual ~CCom();



public:
	BYTE CheckEncrypt(char *szFile,DWORD &dwSeed);
	BYTE SeedToKey(BYTE *bSeed,BYTE *bKey);
	BYTE DecryptData(BYTE *bBuf,DWORD dwLen,BYTE *bKey);


private:
	WORD CalculateSeed2Key(WORD wSeed);


private:
	HANDLE m_handle;

private:
//	CRITICAL_SECTION m_CriticalSection;

public:
	BYTE m_bProtocol;

	BYTE m_bErrorCode;


public: //Security
	DWORD MySeed2Key(DWORD dwSeed,BYTE bLoopTimes);
	bool MySecurityAccess();
	bool MyDeCryptDataFile(char *pFile,BYTE *pBuf,DWORD &dwLen);


private:
	void Test_OutPut(BYTE *pByte, BYTE nLen);

public:
	BYTE SetBaudRateEx(DWORD dwBaudRate);

	bool RecvCmdExTimeout(BYTE *nBuff, DWORD nLen, DWORD dwTimeout = 3 * 1000);

		//2022.9.8添加，自识别获取引脚对应的波特率
    	/*
    		发送:A5 A5 00 05 EB E6 B3 91 CB 55
    			00 05：数据长度
    			EB:检测波特率命令号
    			E6 B3 91 CB:下位机要检测的引脚
    			E6 B3 91 CB这些都是诊断传给gui

    		回复：A5 A5 00 11 EB E6 03 D0 90 B3 07 A1 20 91 FF FF FF CB EE EE EE 55
    		00 11:数据长度
    		E6 03 D0 90:E6引脚检测到的波特率为250K
    		B3 07 A1 20:B3引脚检测到的波特率为500K
    		91 FF FF FF:91引脚未检测到波特率，但是有can的电压(诊断可以发送任意的波特率)
    		CB EE EE EE：CB引脚没有can的电压（诊断不需要发送这个引脚的任何数据，节省时间）
    	*/
    int GetEcuPinBaudRate(vector<BYTE> vecbPin, map<BYTE, DWORD> &mapPinBaud);
};


//SetProtocol 设置协议类型，同步MCU和VDI(如有改动需同步改动)
#define PROTOCOL_M_KWP		0x01
#define PROTOCOL_M_CAN		0x02
#define PROTOCOL_M_NORMAL	0x03
#define PROTOCOL_M_ISO		0x04
#define PROTOCOL_M_BOSCH	0x05

//错误处理,同步MCU(如有改动需同步改动)
#define ERR_OK							0x00	//OK
#define ERR_Uart1RecvOneByte_TIMEOUT	0x01	//串口1接收一个字节超时错误（帧错误也在其中）
#define ERR_Uart1SendOneByte_TIMEOUT	0x02	//串口1发送一个字节超时错误（帧错误也在其中）
#define ERR_Uart2RecvOneByte_TIMEOUT	0x03	//串口2接收一个字节超时错误（帧错误也在其中）
#define ERR_Uart2SendOneByte_TIMEOUT	0x04	//串口2发送一个字节超时错误（帧错误也在其中）
#define ERR_Uart2SendRecvOwn_TIMEOUT	0x05	//串口2自发自收一个字节超时错误
#define ERR_BPS_KWP						0x06	//设置K  线波特率错误  
#define ERR_BPS_CAN						0x07	//设置CAN线波特率错误
#define ERR_TIME						0x08	//设置时序错误
#define ERR_PROTOCOL					0x09	//设置协议类型错误
#define ERR_PROTOCOL_KWP				0x0A	//设置KWP协议类型错误
#define ERR_PROTOCOL_CAN				0x0B	//设置CAN协议类型错误
#define ERR_PROTOCOL_NORMAL				0x0C	//设置NORMAL协议类型错误
#define ERR_PROTOCOL_ISO				0x0D	//设置NORMAL协议类型错误
#define ERR_PROTOCOL_BOSCH				0x0E	//设置BOSCH协议类型错误
#define ERR_VOLTAGE						0x0F	//设置高低电平错误
#define ERR_ISO_RECV_TIMEOUT			0x10	//ISO协议格式接收超时错误（没长度，只能按时间接收）

//选线
#define PORT_01		1
#define PORT_02		2
#define PORT_03		3
#define PORT_04		4 //GND
#define PORT_05		5 //GND
#define PORT_06		6 //CAN H
#define PORT_07		7
#define PORT_08		8
#define PORT_09		9
#define PORT_10		10
#define PORT_11		11
#define PORT_12		12
#define PORT_13		13
#define PORT_14		14 //CAN L
#define PORT_15		15
#define PORT_16		16 //POWER


//地址码进入系统用到的参数字节的位定义,同步MCU(如有改动需同步改动)
#define ADDR_AUTO_BPS_RECOGNIZE		0x01	//自动识别波特率
#define ADDR_RECV_KW5BYTE			0x02	//接收5个KW，未选中则表示接收2个KW
#define ADDR_KW1_INVERSE_TO_ECU		0x04	//KW1取反发回
#define ADDR_KW2_INVERSE_TO_ECU		0x08	//KW2取反发回
#define ADDR_INVERSE_FROM_RCU		0x10	//接收来自ECU地址码取反
#define ADDR_RECV_ONE_FRAME			0x20	//接收一帧
#define ADDR_RECV_MUTI_FRAME		0x40	//接收多帧
#define ADDR_CLOSE_TIGGER			0x80	//关闭触发线


#endif // !defined(AFX_COM_H__7C33D3F0_12E5_4FE8_9DBE_A1D2FF221BCD__INCLUDED_)
