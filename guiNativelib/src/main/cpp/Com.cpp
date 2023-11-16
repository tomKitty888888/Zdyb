// Com.cpp: implementation of the CCom class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Debug.h"
#include "Com.h"
#include <android/log.h>
//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////
//产品索引 m_ProductIndex
//#define YTOPower	1
//#define EPS918		2
//BYTE m_ProductIndex = YTOPower;

//change to below
DWORD g_dwProduct = PRODUCT_EPS918_PRO;

#pragma message("\r\n------------------------------------------ 当前编译版本: EPS918 旗舰版\r\n")
//#pragma message("\r\n------------------------------------------ 当前编译版本: EPS918 标准版\r\n")
//#pragma message("\r\n------------------------------------------ 当前编译版本: EPS918 大陈版\r\n")
//#pragma message("\r\n------------------------------------------ 当前编译版本: 一拖专用版\r\n")
//#pragma message("\r\n------------------------------------------ 当前编译版本: EPS916\r\n")

//定义一个全局CCom提供给其它类使用
CCom g_Com;

extern CGui g_Gui;


extern BYTE g_bProtocol; //来自CCommWithEcu类的全局.

extern FILE *g_pFile;
extern void printfhex(unsigned char * buf,int len);

CCom::CCom()
{
	m_handle = INVALID_HANDLE_VALUE;
	m_bProtocol = PROTOCOL_M_KWP;
	m_bErrorCode = ERR_OK;

	//InitializeCriticalSection(&m_CriticalSection);  //未尝试过, 可以不要这个看看是不是稳定一些.. 有时间再试!

	m_bStandardCan = false;
}

CCom::~CCom()
{
	//DeleteCriticalSection(&m_CriticalSection);
}

//////////////////////////////////////////////////////////////////////////
// 打开串口
bool CCom::ComOpen() 
{
	string strPath = CRunEnvironment::GetDisplayDirectory();
	if (strPath.length() < 3)
	{
		return false;
	}
	

	string strPathFile = strPath + "Config\\com.txt";
	FILE *fp = NULL;
	fp = fopen(strPathFile.c_str(), "r");
	if (fp == NULL)
	{
		return false;
	}
	


	char szLine[255];
	fgets(szLine,255,fp);
	int ch = fgetc(fp);
	BYTE bCom = (BYTE)(ch - 0x30);
	fclose(fp);

	if (!ComOpen(bCom))
	{
		return false;
		//如果打开文件记录中的串口号失败,则开始遍历所有串口
		BYTE i;
		for (i=1; i<20; i++)
		{
			if (!ComOpen(i))
			{
				Sleep(100);
				continue;
			}
			break;
		}
		if (i >= 20)
		{
				return false;
		}
	

		//保存已经打开的串口号,以便下一次优先使用
		FILE *fp = NULL;
		fp = fopen(strPathFile.c_str(),"w");
		if (fp == NULL)
		{
			return false;
		}
		
		char sz[100] = "";
		sprintf(sz,"%d\r\n",i);
		fwrite(sz,sizeof(char),strlen(sz),fp);
		fclose(fp);
	}

	return true;
}
bool CCom::ComOpen(BYTE nPort)
{
	char szBuff[20];
#ifdef _ANDROID
	sprintf(szBuff,"/dev/ttyUSB%d/x00",nPort);
#else
	sprintf(szBuff,"\\\\.\\COM%d",nPort);
#endif
	m_handle = CreateFile(szBuff,
		GENERIC_READ | GENERIC_WRITE,
		0, 
		NULL,
		OPEN_EXISTING,
		FILE_ATTRIBUTE_NORMAL,
		NULL
		);
	if(m_handle == INVALID_HANDLE_VALUE)
	{
		//OutputDebugString("OpenPort Failed");
		return false;
	}
	
    SetupComm(m_handle, 40960, 40960);//gxf
	
	COMMTIMEOUTS timeouts;
	GetCommTimeouts(m_handle, &timeouts);
	timeouts.ReadIntervalTimeout=MAXDWORD;
	timeouts.ReadTotalTimeoutConstant=0;
	timeouts.ReadTotalTimeoutMultiplier=0;
	timeouts.WriteTotalTimeoutConstant=1000;
	timeouts.WriteTotalTimeoutMultiplier=1000;
	SetCommTimeouts(m_handle, &timeouts);
	
//	COMMTIMEOUTS timeouts;
//	GetCommTimeouts(m_handle, &timeouts);
//	timeouts.ReadIntervalTimeout=50;
//	timeouts.ReadTotalTimeoutConstant=5;
//	timeouts.ReadTotalTimeoutMultiplier=50;
//	timeouts.WriteTotalTimeoutConstant=0;
//	timeouts.WriteTotalTimeoutMultiplier=0;
//	SetCommTimeouts(m_handle, &timeouts);
	
	DCB dcb;
	GetCommState(m_handle, &dcb);
	dcb.BaudRate = 115200;//500000;//115200;
	dcb.ByteSize = 8;
	dcb.StopBits = ONESTOPBIT;//ONE5STOPBITS; //TWOSTOPBITS;
	dcb.Parity = NOPARITY;
	dcb.fRtsControl = 0;
	SetCommState(m_handle, &dcb);
//	DCB dcb;
//	GetCommState(m_handle, &dcb);
//	dcb.BaudRate = 115200;//500000;//115200;
//	dcb.ByteSize = 8;
//	dcb.StopBits = ONESTOPBIT;//ONE5STOPBITS; //TWOSTOPBITS;
//	dcb.Parity = NOPARITY;
//	dcb.fRtsControl = 0;
//	dcb.fOutxCtsFlow=FALSE;
//	dcb.fOutxDsrFlow=FALSE;
//	dcb.fDtrControl=DTR_CONTROL_DISABLE;
//	dcb.fDsrSensitivity = FALSE;
//	dcb.fOutX = FALSE;
//	dcb.fInX = FALSE;
//	dcb.fErrorChar = TRUE;
//	dcb.fNull = FALSE;
//	dcb.fRtsControl=RTS_CONTROL_DISABLE;
//	dcb.ErrorChar = '?';
//	SetCommState(m_handle, &dcb);
	
    PurgeComm(m_handle, PURGE_TXABORT | PURGE_RXABORT | PURGE_TXCLEAR | PURGE_RXCLEAR);

//	BYTE nSend[7] = {0xA5,0xA5,0x00,0x02,0xFF,0x00,0xFE};
	BYTE nSend[7] = {0xA5,0xA5,0x00,0x02,0xFF,0x00,0x55};
	BYTE nRecv[7] = {0,};
	WORD wRecvLen = 0;
//	if (!SendCmd(nSend,7))return false;
//	if (!RecvCmd(nRecv,7))return false;


	adsSleep(100);
	if (!SendRecvCmd2Mcu(nSend,7,nRecv,wRecvLen))return false;
	if ((nRecv[0]!=0xFF) || (nRecv[1]!=0x01))return false;

	return true;
}
bool CCom::SetBluetoothLedStatus(BYTE bLedStatus)
{
	BYTE nSend[8] = {0xA5,0xA5,0x00,0x03,0xFC,0x04,0xCC,0x55};
	BYTE nRecv[8] = {0,};
	WORD wRecvLen = 0;
	if (bLedStatus > 1)return false;
	nSend[6] = bLedStatus;
	if (!SendRecvCmd2Mcu(nSend,8,nRecv,wRecvLen))return false;
	if ((nRecv[0]!=0xFC) && (nRecv[1]!=0x04))return false;
	return true;
}




WORD CCom::CalculateSeed2Key(WORD wSeed)
{
	WORD TOPBIT = 0x8000;
	WORD POLYNOM_1 = 0x8408;
	WORD POLYNOM_2 = 0x8025;
	WORD BITMASK = 0x0080;
	WORD INITIAL_REMINDER = 0xFFFE;
	WORD MSG_LEN = 2; /* seed length in bytes */

	BYTE bSeed[2];
	WORD remainder;
	BYTE n;
	BYTE i;

	bSeed[0] = (BYTE)(wSeed >> 8); /* MSB */
	bSeed[1] = (BYTE)wSeed; /* LSB */
	remainder = INITIAL_REMINDER;
	for (n = 0; n < MSG_LEN; n++)
	{
		/* Bring the next byte into the remainder. */
		remainder ^= ((bSeed[n]) << 8);
		/* Perform modulo-2 division, a bit at a time. */
		for (i = 0; i < 8; i++)
		{
			/* Try to divide the current data bit. */
			if (remainder & TOPBIT)
			{
				if(remainder & BITMASK)
				{
					remainder = (remainder << 1) ^ POLYNOM_1;
				}
				else
				{
					remainder = (remainder << 1) ^ POLYNOM_2;
				}
			}
			else
			{
				remainder = (remainder << 1);
			}
		}
	}
	/* The final remainder is the key */
	return remainder;
}


//////////////////////////////////////////////////////////////////////////
// 关闭串口
bool CCom::ComClose()
{
	if (m_handle == NULL)return true;
    PurgeComm(m_handle, PURGE_TXABORT | PURGE_RXABORT | PURGE_TXCLEAR | PURGE_RXCLEAR);
    CloseHandle(m_handle);
    m_handle = NULL;

	g_Gui.SendDiagnoseQuitMsg2Display();

//	if (!ShareBufferDestroy())return false;

	return true;
}

//////////////////////////////////////////////////////////////////////////
// 发送数据
bool CCom::SendCmd(BYTE *nBuff, DWORD nLen)
{
	if (g_Gui.m_MapComHandle != NULL)
	{
		g_Gui.m_pMapComBuff[0] = (BYTE)0xAA;
		g_Gui.m_pMapComBuff[1] = (BYTE)0x01;
		g_Gui.m_pMapComBuff[2] = (BYTE)0x01;
		g_Gui.m_pMapComBuff[3] = (BYTE)(nLen>>8);
		g_Gui.m_pMapComBuff[4] = (BYTE)(nLen>>0);
		memcpy(g_Gui.m_pMapComBuff+5,nBuff,nLen);
		g_Gui.SendMessage2MainForm_SendMessage_COM();
	}
	
	PurgeComm(m_handle, PURGE_TXABORT | PURGE_RXABORT | PURGE_TXCLEAR | PURGE_RXCLEAR);
//	FlushFileBuffers(m_handle);

	int iBytesSend = 0;
//	EnterCriticalSection(&m_CriticalSection);
	WriteFile(m_handle, nBuff, nLen, (LPDWORD)&iBytesSend, NULL);
//	LeaveCriticalSection(&m_CriticalSection);
	return (iBytesSend==(int)nLen)?true:false;

	
	/* 2
	int iBytesSend = 0;
	DWORD dwError;
	if (ClearCommError(m_handle,&dwError,NULL) && dwError>0)
		PurgeComm(m_handle,PURGE_TXABORT|PURGE_TXCLEAR);
	WriteFile(m_handle, nBuff, nLen, (LPDWORD)&iBytesSend, NULL);
	return (iBytesSend==(int)nLen)?true:false;
*/
/*
	DWORD dwError;
	OVERLAPPED _overlapped;
	unsigned long iBytesSend = 0;

	if (ClearCommError(m_handle,&dwError,NULL) && dwError>0)
		PurgeComm(m_handle,PURGE_TXABORT|PURGE_TXCLEAR);
	
	memset(&_overlapped,0,sizeof(_overlapped));
	_overlapped.hEvent = CreateEvent(NULL,TRUE,FALSE,NULL);

	if (!WriteFile(m_handle, nBuff, nLen, (LPDWORD)&iBytesSend, &_overlapped))
	{
		dwError = GetLastError();
		if (dwError == ERROR_IO_PENDING)
			iBytesSend = 0;
		else
		{
			GetOverlappedResult(m_handle,&_overlapped,&iBytesSend,TRUE);
		}
	}

	CloseHandle(_overlapped.hEvent);

	return (iBytesSend==(int)nLen)?true:false;
*/
}

//////////////////////////////////////////////////////////////////////////
// 接收数据
bool CCom::RecvCmd(BYTE *nBuff, DWORD nLen)
{
    return RecvCmdEx(nBuff, nLen,6000);
}

bool CCom::RecvCmdEx(BYTE *nBuff, DWORD nLen,DWORD dwTimeout)
{

    dwTimeout = 6 * 1000;  //固定6s,让超时不起作用。与918接口功能相同
//	if(nLen>2){
//		dwTimeout = 6 * 1000*10;
//	}
__android_log_print(ANDROID_LOG_INFO,"RecvCmdEx","total nLen=%d",nLen);//gxf 2023
//	PurgeComm(m_handle, PURGE_TXABORT | PURGE_TXCLEAR);
	int len=0, cnt= 0;
	DWORD clk=::GetTickCount();
	DWORD nowt=0;

	COMSTAT ComStat;//gxf
	DWORD dwErrorFlags;
	int Current =nLen;//nLen;// 1024*3;
	while(1){
        Current = (Current<(nLen-len))?Current:(nLen-len);//取更小的长度
        ReadFile(m_handle, nBuff+len, Current, (LPDWORD)&cnt, NULL);
        __android_log_print(ANDROID_LOG_INFO,"RecvCmdEx","onetime len=%d Current=%d left=%d cnt=%d",len,Current,nLen-len,cnt);//gxf 2023
        len+=cnt;
        if(len==nLen){
            break;
        }else{
            Sleep(10);
        }
        nowt=::GetTickCount();
		if(nowt-clk>dwTimeout) {
			__android_log_print(ANDROID_LOG_INFO,"RecvCmdEx","timeout len=%d nLen=%d cnt=%d",len,nLen,cnt);//gxf 2023
			break;
		}
		//ClearCommError(m_handle,&dwErrorFlags,&ComStat);
		//if(ComStat.cbInQue==0)continue;
		//int times=nlen/512;

	}
/*
    int need=512;
	int times=nLen/need;
	int left=nLen%need;
	nowt=::GetTickCount();
	for(int i=0;i<times;i++){
		ReadFile(m_handle, nBuff+i*need, need, (LPDWORD)&cnt, NULL);
		if(cnt<=0){
			if(::GetTickCount()-nowt>6000){
				__android_log_print(ANDROID_LOG_INFO,"RecvCmdEx","timeout1 nowt=%d,clk=%d cnt=%d len=%d",nowt,clk,cnt,len);
				break;
			}
			i--;
			//Sleep(1000);
			continue;
		}
		len+=cnt;

		if(cnt!=need){//连48都没有获取到
				int times2=(need-cnt+left)/need;
				if(times2>0){
					times+=times2;
					left=(need-cnt+left)%need;
				}else{
					left+=need-cnt;
				}
		}
		__android_log_print(ANDROID_LOG_INFO,"RecvCmdEx","onetime cnt=%d len=%d",cnt,len);
	}
	nowt=::GetTickCount();
	while(left>0){
		ReadFile(m_handle, nBuff+len, left, (LPDWORD)&cnt, NULL);
		if(cnt<=0){
			if(::GetTickCount()-nowt>6000){
				__android_log_print(ANDROID_LOG_INFO,"RecvCmdEx","timeout2 nLen=%d nowt=%d,clk=%d cnt=%d len=%d",nLen,nowt,clk,cnt,len);
				break;
			}
			//Sleep(1000);
			continue;
		}
		left-=cnt;
		len+=cnt;
		__android_log_print(ANDROID_LOG_INFO,"RecvCmdEx","onetime2 cnt=%d len=%d",cnt,len);
	}
*/

//	if(len>22&&nBuff[len-1]==0x55){
//	    //发送完毕 发送测试
//	    char test[7]={0xa5,0xa5,0x00,0x02,0x11,0x11,0x55};
//	    SendCmd((BYTE*)test,7);
//	}

	//return (len==(int)nLen)?true:false;
	if (len == (int)nLen)
	{
		if (g_Gui.m_MapComHandle != NULL)
		{
			g_Gui.m_pMapComBuff[0] = (BYTE)0xAA;
			g_Gui.m_pMapComBuff[1] = (BYTE)0x01;
			g_Gui.m_pMapComBuff[2] = (BYTE)0x02;
			g_Gui.m_pMapComBuff[3] = (BYTE)(nLen>>8);
			g_Gui.m_pMapComBuff[4] = (BYTE)(nLen>>0);
			memcpy(g_Gui.m_pMapComBuff+5,nBuff,nLen);
			g_Gui.SendMessage2MainForm_SendMessage_COM();
		}
		//printfhex(nBuff,512);
		return true;
	}
	return false;
}
bool CCom::RecvCmdExTimeout(BYTE * nBuff, DWORD nLen, DWORD dwTimeout)
{
	int len = 0, cnt = 0;
	DWORD clk = ::GetTickCount();
	COMSTAT ComStat;
	DWORD dwErrorFlags;
	__android_log_print(ANDROID_LOG_INFO,"RecvCmdExTimeout","total nLen=%d",nLen);//gxf 2023
	while (1)
	{
		if (::GetTickCount() - clk > dwTimeout)break;
		ClearCommError(m_handle, &dwErrorFlags, &ComStat);
		if (ComStat.cbInQue == 0)continue;
		ReadFile(m_handle, nBuff + len, nLen - len, (LPDWORD)&cnt, NULL);
		len += cnt;
		if (len == nLen)break;
		else{
			Sleep(10);
		}
	}
	return (len==(int)nLen)?true:false;
}

DWORD CCom::RecvCmdTime(BYTE *nBuff,DWORD dwRecvCount/*=100*/)
{
//	DWORD iBytesReceive = 0;
//	int len=0, cnt= 0;
//	DWORD clk=::GetTickCount();
//	COMSTAT ComStat;
//	DWORD dwErrorFlags;
//	while(1){
//		if(::GetTickCount()-clk>2*1000)break;
//		ClearCommError(m_handle,&dwErrorFlags,&ComStat);
//		if(ComStat.cbInQue==0)continue;
// 		ReadFile(m_handle, nBuff+len, dwRecvCount-len, (LPDWORD)&cnt, NULL);
//		len+=cnt;
//		break;
//	}
//	return iBytesReceive = len;

//	DWORD iBytesReceive = 0;
//	ReadFile(m_handle, nBuff, dwRecvCount, (LPDWORD)&iBytesReceive, NULL);
//
//	if (g_pFile != NULL)
//	{
//		if (g_Gui.m_MapComHandle != NULL)
//		{
//			g_Gui.m_pMapComBuff[0] = (BYTE)0xAA;
//			g_Gui.m_pMapComBuff[1] = (BYTE)0x01;
//			g_Gui.m_pMapComBuff[2] = (BYTE)0x02;
//			g_Gui.m_pMapComBuff[3] = (BYTE)(iBytesReceive>>8);
//			g_Gui.m_pMapComBuff[4] = (BYTE)(iBytesReceive>>0);
//			memcpy(g_Gui.m_pMapComBuff+5,nBuff,iBytesReceive);
//			g_Gui.SendMessage2MainForm_SendMessage_COM();
//		}
//	}
//
//	return iBytesReceive;
	DWORD iBytesReceive = 0;
	int len=0, cnt= 0;
	DWORD clk=::GetTickCount();
	char ch[50]={0};
	DWORD dwTimeE=0;
//	sprintf(ch,"while Start:%08x",clk);//gxf
//	OutputDebugString(ch);//gxf
	COMSTAT ComStat;
	DWORD dwErrorFlags;
	//	while(1){
//		if(::GetTickCount()-clk>2*1000)break;
	ClearCommError(m_handle,&dwErrorFlags,&ComStat);
	if(ComStat.cbInQue==0)return 0;
	ReadFile(m_handle, nBuff+len, dwRecvCount, (LPDWORD)&cnt, NULL);
//		len+=cnt;
//		if(len==dwRecvCount) break;
//	}
/*	while(1){
		if(::GetTickCount()-clk>2*1000)break;
		ClearCommError(m_handle,&dwErrorFlags,&ComStat);
		if(ComStat.cbInQue==0)continue;
		ReadFile(m_handle, nBuff+len, dwRecvCount-len, (LPDWORD)&cnt, NULL);
		len+=cnt;
		if(len==dwRecvCount) break;
	}*/
	iBytesReceive = cnt;
	dwTimeE=::GetTickCount();
	sprintf(ch,"while end:%08x",dwTimeE);
	//OutputDebugString(ch);
//	return iBytesReceive = len;

//	DWORD iBytesReceive = 0;
//	ReadFile(m_handle, nBuff, dwRecvCount, (LPDWORD)&iBytesReceive, NULL);
	if (g_pFile != NULL)
	{
		if (g_Gui.m_MapComHandle != NULL)
		{
			g_Gui.m_pMapComBuff[0] = (BYTE)0xAA;
			g_Gui.m_pMapComBuff[1] = (BYTE)0x01;
			g_Gui.m_pMapComBuff[2] = (BYTE)0x02;
			g_Gui.m_pMapComBuff[3] = (BYTE)(iBytesReceive>>8);
			g_Gui.m_pMapComBuff[4] = (BYTE)(iBytesReceive>>0);
			memcpy(g_Gui.m_pMapComBuff+5,nBuff,iBytesReceive);


			g_Gui.SendMessage2MainForm_SendMessage_COM();
		}
	}

	return iBytesReceive;

}
DWORD CCom::RecvCmdTimeout(BYTE *nBuff,DWORD dwCount,DWORD dwTimeout)
{
#ifdef _NO_COM_
	DWORD dwRecvNum = 0;
	if (g_Gui.m_MapComHandle != NULL)
	{
		g_Gui.m_pMapComBuff[0] = (BYTE)0xAA;
		g_Gui.m_pMapComBuff[1] = (BYTE)0x01;
		g_Gui.m_pMapComBuff[2] = (BYTE)0x22;  //22-收到个数或超时返回
		g_Gui.m_pMapComBuff[3] = (BYTE)(dwCount>>8);
		g_Gui.m_pMapComBuff[4] = (BYTE)(dwCount>>0);
		g_Gui.m_pMapComBuff[5] = (BYTE)(dwTimeout>>0x18);
		g_Gui.m_pMapComBuff[6] = (BYTE)(dwTimeout>>0x10);
		g_Gui.m_pMapComBuff[7] = (BYTE)(dwTimeout>>0x08);
		g_Gui.m_pMapComBuff[8] = (BYTE)(dwTimeout>>0x00);
		memcpy(g_Gui.m_pMapComBuff+9,nBuff,0);
		g_Gui.SendMessage2MainForm_SendMessage_COM();
	}
	return dwRecvNum;
#endif
}
void CCom::PurgeCommEx()
{
	PurgeComm(m_handle, PURGE_TXABORT | PURGE_RXABORT | PURGE_TXCLEAR | PURGE_RXCLEAR);
}
bool CCom::RecvCmd_OneBytOne(BYTE *nBuff, DWORD nLen)
{
	DWORD dwCount = 0;
	BYTE nByte = 0x00;
	DWORD t = GetTickCount();
	while (1)
	{
		if (GetTickCount() - t > 2*1000)
		{
			return false;
		}
		if (!RecvCmd(&nByte,1))continue;
//		//test
//		char sz[10];
//		sprintf(sz,"[%02X]",nByte);
//		OutputDebugString(sz);
//		//test
		nBuff[dwCount] = nByte;
		dwCount++;
		if (dwCount == nLen)
		{
			break;
		}
	}
	return true;
}





static void OutputDebug(BYTE *nBuf,DWORD dwLen)
{
	char sz[2048];
	for (DWORD dw=0; dw<dwLen; dw++)
	{
		sprintf(sz+dw*3,"%02X ",nBuf[dw]);
	}
	OutputDebugString(sz);
	OutputDebugString("\r\n");
}


bool CCom::RecvCanOneFrame(BYTE *nRecvBuf,WORD &nRecvLen)
{
	BYTE nTempBuf[255];
	WORD nTempLen=0;
	if (!RecvCmdEx(nTempBuf,2))
		return false;
	if (nTempBuf[0] != 0xA5)
		return false;
	if (nTempBuf[1] != 0xA5)
		return false;
	if (!RecvCmdEx(nTempBuf,2))
		return false;
	nTempLen = nRecvLen = nTempBuf[0]<<8|nTempBuf[1];
	if (!RecvCmdEx(nRecvBuf,nRecvLen))
		return false;
	if (!RecvCmdEx(nTempBuf,1))
		return false;
	if (nTempBuf[0] != 0x55)
		return false;
	if (nRecvLen > 5)
	{
		memcpy(nRecvBuf,nRecvBuf+5,nRecvLen-5);
		nRecvLen = nRecvLen - 5;
	}
	return true;
}
/*OK
bool CCom::RecvKwpOneFrame(BYTE *nRecvBuf,WORD &nRecvLen)
{
	DWORD t = GetTickCount();
	while (1)
	{
		if (GetTickCount() - t > 3*1000)
		{
			OutputDebugString("---------接收A5 A5超时失败\r\n");
			return false;
		}
		if (!RecvCmd_OneBytOne(nRecvBuf,2))continue; //1.先收A5 A5两个字节
		break;
	}
	if ((nRecvBuf[0]<<8|nRecvBuf[1]) != 0xA5A5)
	{
		OutputDebugString("---------接收A5 A5但字节不等于0xA5A5\r\n");
		return false;
	}

	t = GetTickCount();
	while (1)
	{
		if (GetTickCount() - t > 5*1000)
		{
			OutputDebugString("---------接收XX XX长度字节超时失败\r\n");
			return false;
		}
		if (!RecvCmd_OneBytOne(nRecvBuf,2))continue; //2.再收长度的两个字节
		break;
	}
	WORD wLen = nRecvBuf[0]<<8|nRecvBuf[1];

	t = GetTickCount();
	while (1)
	{
		if (GetTickCount() - t > 5*1000)
		{
			OutputDebugString("---------接收长度字节之后的数据 超时失败\r\n");
			return false;
		}
		if (!RecvCmd_OneBytOne(nRecvBuf,wLen))continue;
		break;
	}

	BYTE bCS = 0;
	if (!RecvCmd_OneBytOne(&bCS,1))
	{
		OutputDebugString("---------接收校验字节55超时失败\r\n");
		return false;
	}
	if (bCS != 0x55)
	{
		OutputDebugString("---------接收校验字节 !=55 失败\r\n");
		return false;
	}

	memcpy(nRecvBuf,nRecvBuf+1,wLen-1);
	nRecvLen = wLen-1;

	return true;
}
*/
bool CCom::RecvKwpOneFrame(BYTE *nRecvBuf,WORD &nRecvLen)
{
	BYTE nTempBuf[500];
	WORD nTempLen=0;
	if (!RecvCmdEx(nTempBuf,2,5*1000))
		return false;
	if (nTempBuf[0] != 0xA5)
		return false;
	if (nTempBuf[1] != 0xA5)
		return false;
	if (!RecvCmdEx(nTempBuf,2))
		return false;
	nTempLen = nTempBuf[0]<<8|nTempBuf[1];
	if (nTempLen > 200)
	{
		int kkkk = 0;
	}
	if (!RecvCmdEx(nTempBuf,nTempLen))
		return false;

	memcpy(nRecvBuf,nTempBuf+1,nRecvLen=nTempLen-1);

	if (!RecvCmdEx(nTempBuf,1))
		return false;
	if (nTempBuf[0] != 0x55)
		return false;
	return true;
}

bool CCom::SendRecvCmd2Mcu_KWP(BYTE *nSendBuf,WORD nSendLen,BYTE *nRecvBuff,WORD &nRecvLen)
{
	DWORD t = 0;
	BYTE nTmp[500];
	WORD wLen;

	if (nSendLen != 0) //发送不为0才清除COM数据
	{
		if (PurgeComm(m_handle, PURGE_TXABORT | PURGE_RXABORT | PURGE_TXCLEAR | PURGE_RXCLEAR) != TRUE)
		{
			OutputDebugString("SendRecvCmd2Mcu_KWP()中的PurgeComm()错误!!!\r\n");
		}
	//	FlushFileBuffers(m_handle);
	}

$_KWP_SEND_RECV_AGAIN:
	if (nSendLen != 0) //如果发送为0,则表示只收不发
	{
		if (!SendCmd(nSendBuf,nSendLen))
		{
			return false;
		}
	}

$_KWP_RECV_AGAIN:
	if (!RecvKwpOneFrame(nTmp,wLen))return false;
	memcpy(nRecvBuff,nTmp,wLen);
	nRecvLen = wLen;

	if (wLen >= 5)
	{
		BYTE nIndex = 0;
		if (nRecvBuff[0] == 0x80)nIndex = 4;
		else if (nRecvBuff[0] > 0x80)nIndex = 3;
		else if (nRecvBuff[0] == 0x00)nIndex = 2;
		else nIndex = 1;
		if (nRecvBuff[nIndex]==0x7F && nRecvBuff[nIndex+2]==0x78)goto $_KWP_RECV_AGAIN;
		if (nRecvBuff[nIndex]==0x7F && nRecvBuff[nIndex+2]==0x21)goto $_KWP_SEND_RECV_AGAIN;
		if (nRecvBuff[nIndex]==0x7F && nRecvBuff[nIndex+2]==0x23)goto $_KWP_SEND_RECV_AGAIN;
		if (nRecvBuff[nIndex]==0x7F && nRecvBuff[nIndex+2]==0xFB)goto $_KWP_SEND_RECV_AGAIN;
	}

	return true;
}
bool CCom::ResetMcu()
{
	BYTE nSendBuf[10],nSendLen;
	memcpy(nSendBuf,(BYTE *)"\xA5\xA5\x00\x02\xF9\x00\x55",nSendLen=7);
	SendCmd(nSendBuf,nSendLen);
	adsSleep(3*1000);

	BYTE nSend[7] = {0xA5,0xA5,0x00,0x02,0xFF,0x00,0x55};
	BYTE nRecv[7] = {0,};
	WORD wRecvLen = 0;
	if (!SendRecvCmd2Mcu(nSend,7,nRecv,wRecvLen))
	{
		if (!SendRecvCmd2Mcu(nSend,7,nRecv,wRecvLen))
		{
			if (!SendRecvCmd2Mcu(nSend,7,nRecv,wRecvLen))
			{
				return false;
			}
		}
	}
	if ((nRecv[0]!=0xFF) || (nRecv[1]!=0x01))return false;
	adsSleep(500);
	return true;
}
bool CCom::SendRecvCmd2Mcu(BYTE *nSendBuf,WORD nSendLen,BYTE *nRecvBuff,WORD &nRecvLen)
{
	DWORD t = 0;
	BYTE nTmp[100];
	WORD wLen;

//	FlushFileBuffers(m_handle);
	if (PurgeComm(m_handle, PURGE_TXABORT | PURGE_RXABORT | PURGE_TXCLEAR | PURGE_RXCLEAR) != TRUE)
	{
		OutputDebugString("SendRecvCmd2Mcu()-------------------PurgeComm() Failed\r\n");
	}
	Sleep(1);

	
	if (nSendLen != 0) //如果发送为0,则表示只收不发
	{
		if (!SendCmd(nSendBuf,nSendLen))
		{
			return false;
		}
	}
	else
	{
		OutputDebugString("---warning : nSendLen == 0\r\n");
	}

	if (!RecvCanOneFrame(nTmp,wLen))return false;
	memcpy(nRecvBuff,nTmp,wLen);
	nRecvLen = wLen;



	if (wLen > 5)
	{
		if (!m_bStandardCan)return true;

		if (nTmp[0] & 0x10)
		{
			//先把第一个字节10去掉
			memcpy(nRecvBuff,nRecvBuff+1,nRecvLen-1);
			nRecvLen--;
			//接收10之后的所有帧
			WORD nNum = ((nTmp[0]&0x0F)<<8) + nTmp[1];
			nNum -= 6;
			BYTE bLeftFrm = nNum/7 + ((nNum%7)?1:0);
			for (BYTE i=0; i<bLeftFrm; i++)
			{
				if (!RecvCanOneFrame(nTmp,wLen))return false;
				memcpy(nRecvBuff+nRecvLen,nTmp+1,wLen-1);
				nRecvLen += wLen-1;
			}
		}
	}
	
	return true;
}
void CCom::SetMcuChecksum(BYTE *nBuf,WORD nLen)
{
//第一版MCU使用的校验如下:(已经不用,包括YTOPower也适用固定的55)
//	BYTE cs = 0;
//	for (WORD w=2; w<nLen-1; w++)cs += nBuf[w];
//	nBuf[w] = ~cs;

//第二版MCU更改使用固定校验0x55
	nBuf[nLen-1] = 0x55;
}
bool CCom::SetKwp2Mcu(DWORD dwBaud,DWORD dwPort,BYTE bLogicV1,BYTE bLogicV2,CBinary binTimes)
{
	BYTE nCmd[49] = {
		0xA5,0xA5,0x00,0x2C,0x13,
		0x01,0x00,0x28,0xA0,
		0x02,0x00,
		0x03,0xC0,
		0x04,0x77,0x88,0xC1,0xC1,
//		0x05,0x15,0x19,0x24,0x04,0x04,0x00,0x19, //普通K线时序OK
//		0x05,0x15,0xF0,0x00,0x00,0x04,0x00,0x19, //刷写K线时序OK,也适用普通K线通讯时序(未完整测试)
//		0x05,0x15,0x20,0x20,0x0A,0x06,0x00,0x00, //test
		0x05,0x10,0x19,0x50,0x05,0x04,0x00,0x32,
		0x06,0x03,
		0x07,0x02,0x00,0x19,0x80,0x19,
		0x08,0x70,
		0x09,0x00,
		0x0A,0x00,0x00,0x00,
		0x0B,0x00,
		0x0C,0x01,0x00,0x73,
		0x19
	};
	BYTE nRecvBuf[100];
	WORD nRecvLen;

	nCmd[6] = (BYTE)(dwBaud>>0x10);
	nCmd[7] = (BYTE)(dwBaud>>0x08);
	nCmd[8] = (BYTE)(dwBaud>>0x00);
	nCmd[14] = (BYTE)(dwPort>>0x08);
	nCmd[15] = (BYTE)(dwPort>>0x00);
	nCmd[16] = bLogicV1;
	nCmd[17] = bLogicV2;
	for (BYTE i=0; i<7; i++)nCmd[19+i] = binTimes[i];
	SetMcuChecksum(nCmd,49);
	if (!SendRecvCmd2Mcu(nCmd,49,nRecvBuf,nRecvLen))
	{
		adsSleep(500);//发现,不延时会出现两次失败对话框.待查 //一般200ms就够了.
		return false;
	}
//	if (m_ProductIndex == YTOPower)
//	{
//		if ((nRecvLen!=1) || (nRecvBuf[0]!=0x00))return false;//YTOPower
//	}
//	else
	{
		if ((nRecvLen!=1) || (nRecvBuf[0]!=0x13))return false;//第一版MCU返回值(EPS918)
	}

	return true;
}
bool CCom::SetCan2Mcu(DWORD dwBaud,DWORD dwSendCanID,DWORD dwRecvCanID,CBinary bin30,BYTE bMode,BYTE bPin)
{
	BYTE nCmd[69] = {
		0xA5,0xA5,0x00,0x40,0x10,
		0x01,0x07,0xa1,0x20,
		0x02,0x81, //7F模式(发送接收没有CANID参与),81模式(发送接收有CANID参与)
		0x03,0x09,
		0x04,0x00,0x00,0x07,0xE8,
		0x05,0x8c,0xF0,0x04,0x00,
		0x06,0x00,0x00,0x00,0x00,
		0x07,0x00,0x00,0x00,0x00, 
		0x08,0x00,0x00,0x00,0x00,
		0x09,0x00,0x00,
		0x0A,0x00,0x00,
		0x0B,0x00,0x00,
		0x0C,0xE6,
//		0x0D,0x01,
		0x0E,0x01,0x00,0x0D,0x30,0x00,0x00,0x07,0xE0,0x30,0x00,0x0A,0x00,0x00,0x00,0x00,0x00,
		0x0F,0x05,
		0x55
	};
	BYTE nRecvBuf[100];
	WORD nRecvLen;

//	if (m_ProductIndex == YTOPower)
	{
		nCmd[3] = 0x40;//临时
	}

	nCmd[6] = (BYTE)(dwBaud>>0x10);
	nCmd[7] = (BYTE)(dwBaud>>0x08);
	nCmd[8] = (BYTE)(dwBaud>>0x00);
	if (bMode==CAN_EXT)nCmd[12] = 0x10;
	nCmd[14] = (BYTE)(dwSendCanID>>0x18);
	nCmd[15] = (BYTE)(dwSendCanID>>0x10);
	nCmd[16] = (BYTE)(dwSendCanID>>0x08);
	nCmd[17] = (BYTE)(dwSendCanID>>0x00);
	nCmd[19] = (BYTE)(dwRecvCanID>>0x18);
	nCmd[20] = (BYTE)(dwRecvCanID>>0x10);
	nCmd[21] = (BYTE)(dwRecvCanID>>0x08);
	nCmd[22] = (BYTE)(dwRecvCanID>>0x00);
	nCmd[48] = bPin;
	if (bin30.GetSize() == 12)
	{
		for (BYTE i=0; i<12; i++)
		{
			nCmd[54+i] = bin30.GetAt(i);
		}
	}
	SetMcuChecksum(nCmd,69);
	if (!SendRecvCmd2Mcu(nCmd,69,nRecvBuf,nRecvLen))
	{
		adsSleep(1*1000);
		return false;
	}
//	if (m_ProductIndex == YTOPower)
//	{
//		if ((nRecvLen!=1) || (nRecvBuf[0]!=0x00))return false;
//	}
//	else
	{
		if ((nRecvLen!=1) || (nRecvBuf[0]!=0x10))
		{
			adsSleep(1*1000);
			return false;//第一版MCU返回值(EPS918)
		}
	}
	return true;
}
static void UnpackCmd(BYTE *nBuf,BYTE &nLen)
{
	BYTE nTmp[20];
	BYTE len = nBuf[9];
	if (nBuf[9] & 0x10)
	{
		len = nBuf[10];
		memcpy(nTmp,nBuf+9,8);
		memcpy(nBuf,nTmp,8);
		nLen = 8;
		return ;
		
	}
	memcpy(nTmp,nBuf+9,len+1);
	memcpy(nBuf,nTmp,len+1);
	nLen = len + 1;
}
BYTE CCom::SendRecvCanData(BYTE *nSendBuf,BYTE nSendLen,BYTE *nRecvBuff)
{
	if (nSendLen)
		OutputDebug(nSendBuf,nSendLen);
//	BYTE nSend[14] = {0xA5,0xA5,0x00,0x09,0x30,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x8B}; //7F模式
	BYTE nSend[18] = {0xA5,0xA5,0x00,0x0D,0x30,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x8B};//81模式
	nSend[5] = (BYTE)(m_dwCanSendID>>0x18);
	nSend[6] = (BYTE)(m_dwCanSendID>>0x10);
	nSend[7] = (BYTE)(m_dwCanSendID>>0x08);
	nSend[8] = (BYTE)(m_dwCanSendID>>0x00);
	for (BYTE i=0; i<nSendLen; i++)
	{
		nSend[9+i] = nSendBuf[i];
	}
	SetMcuChecksum(nSend,18);
	WORD nRecvLen = 0;
	if (!SendRecvCmd2Mcu(nSend,nSendLen==0?0:18,nRecvBuff,nRecvLen))return 0;
//	UnpackCmd(nRecvBuff,nRecvLen);
	OutputDebug(nRecvBuff,nRecvLen);
	return nRecvLen;
}
WORD CCom::SendRecvKwpData(BYTE *nSendBuf,BYTE nSendLen,BYTE *nRecvBuff,BYTE n4)
{
	if (nSendLen)OutputDebug(nSendBuf,nSendLen);
	WORD nRecvLen = 0;
	BYTE nSend[500] = {0xA5,0xA5,0x00,0x06,0x23,0x81,0x11,0xF1,0x81,0x04,0x55};
	nSend[2] = 0x00;
	nSend[3] = nSendLen+1;
	nSend[4] = n4;
	BYTE i;
	for (i=0; i<nSendLen; i++)
	{
		nSend[5+i] = nSendBuf[i];
	}
	nSend[i+5] = 0x55;
	WORD nLen = nSendLen+6;
	if (!SendRecvCmd2Mcu_KWP(nSend,nLen,nRecvBuff,nRecvLen))return 0;
	if (nRecvLen)OutputDebug(nRecvBuff,nRecvLen);
	OutputDebugString("\r\n");
	return nRecvLen;
}
















//////////////////////////////////////////////////////////////////////////
// 设置波特率
bool CCom::SetBaud(DWORD dwBaud)
{
	DCB dcb;
	if (GetCommState(m_handle,&dcb) != TRUE)return false;
	dcb.BaudRate = dwBaud;
	if (SetCommState(m_handle,&dcb) != TRUE)return false;
	return true;
}

//////////////////////////////////////////////////////////////////////////
// 设置校验位
bool CCom::SetParity(BYTE bParity)
{
	DCB dcb;
	if (GetCommState(m_handle,&dcb) != TRUE)return false;
	dcb.Parity = bParity;
	if (SetCommState(m_handle,&dcb) != TRUE)return false;
	return true;
}

//////////////////////////////////////////////////////////////////////////




//////////////////////////////////////////////////////////////////////////




void CCom::SetErrorCode(BYTE bErrorCode)
{
	m_bErrorCode = bErrorCode;
}
BYTE CCom::GetErrorCode(void)
{
	return m_bErrorCode;
}

//////////////////////////////////////////////////////////////////////////
// 设置协议类型
bool CCom::SetProtocol(BYTE bProtocol, BYTE *pParam)
{
	BYTE nSend[20] = {0xFC,0xCF,0x55,0x04,0xC1,0xC2,0xCC,};
	BYTE nLen = 0;
	BYTE nRecv[20] = {0,};
	BYTE nTemp = 0;

	switch (bProtocol)
	{
	case PROTOCOL_M_KWP:
	case PROTOCOL_M_ISO:
	case PROTOCOL_M_BOSCH:
	case PROTOCOL_M_CAN:
		nSend[4] = 0x00;
		nSend[5] = 0x01;
		nSend[6] = bProtocol;
		nSend[7] = 0xCF;
		nSend[8] = 0xFC;
		nLen = 9;
		break;
	case PROTOCOL_M_NORMAL:
		nSend[4] = 0x00;
		nSend[5] = 0x04;
		nSend[6] = bProtocol;
		nSend[7] = pParam[0]; //Position
		nSend[8] = pParam[1]; //Mask
		nSend[9] = pParam[2]; //Offset
		nSend[10] = 0xCF;
		nSend[11] = 0xFC;
		nLen = 12;
		break;
	default:
		return false;
	}
	if (!SendCmd(nSend,nLen))
		return false;

	//RECV: FC CF 80 01 [00] CF FC ;[n]==0x00:OK else ERR_ID
	WORD wFlag = 0x0000;
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 >= 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xFCCF)
			break;
	}
	BYTE nCount = 0;
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		nRecv[nCount++] = nTemp;
		if (nCount >= 20)
			return false; //避免收错数据导致溢出
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xCFFC)
			break;
	}

	if (nRecv[0]!=0x80 || nRecv[1]!=0x01)return false;
	if (nRecv[2] != 0x00)
	{
		SetErrorCode(nRecv[2]);
		return false;
	}
	m_bProtocol = bProtocol;
	return true;
}

void CCom::Test_OutPut(BYTE *pByte, BYTE nLen) //TEST
{
	char szBuff[1024] = "";
	for (BYTE i=0; i<nLen; i++)
	{
		sprintf(szBuff+i*3,"%02X ",pByte[i]);
	}
	OutputDebugString(szBuff);
}

BYTE CCom::SetBaudRateEx(DWORD dwBaudRate)
{
	memset(g_Gui.m_pMapGuiBuff, 0, 0xFFF0);
	g_Gui.m_pMapGuiBuff[0] = (BYTE)0x55;
	g_Gui.m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	g_Gui.m_pMapGuiBuff[2] = (BYTE)FORM_BAUD_RATE_CHANGE;
	g_Gui.m_pMapGuiBuff[3] = (BYTE)0x00;  //[3]-[5]位暂时不用
	g_Gui.m_pMapGuiBuff[4] = (BYTE)0x00;
	g_Gui.m_pMapGuiBuff[5] = (BYTE)0x00;
	g_Gui.m_pMapGuiBuff[6] = (BYTE)0xFE;   //返回值的变化

	g_Gui.m_pMapGuiBuff[7] = (BYTE)(dwBaudRate >> 0x18);   //类型的长度
	g_Gui.m_pMapGuiBuff[8] = (BYTE)(dwBaudRate >> 0x10);
	g_Gui.m_pMapGuiBuff[9] = (BYTE)(dwBaudRate >> 0x08);
	g_Gui.m_pMapGuiBuff[10] = (BYTE)(dwBaudRate >> 0x00);

	if (!g_Gui.SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	BYTE bKeyIndex = g_Gui.m_pMapGuiBuff[6];
	while (1)
	{
		Sleep(1);
		bKeyIndex = g_Gui.m_pMapGuiBuff[6];    //显示程序处理完消息后,给共享内存赋值

		if (bKeyIndex != 0xFE)
		{
			break;
		}
	}

	return bKeyIndex;
}
//////////////////////////////////////////////////////////////////////////
// 设置波特率
bool CCom::SetBaudRate(DWORD dwBps)
{
	BYTE nSend[20] = {0xFC,0xCF,0x55,0x02,};
	BYTE nLen = 0;
	BYTE nRecv[20] = {0,};
	BYTE nTemp = 0x00;

	switch (m_bProtocol)
	{
	case PROTOCOL_M_KWP:
	case PROTOCOL_M_ISO:
	case PROTOCOL_M_BOSCH:
	case PROTOCOL_M_NORMAL:
		nSend[4] = 0x00;
		nSend[5] = 0x04;
		nSend[6] = (BYTE)(dwBps>>24);
		nSend[7] = (BYTE)(dwBps>>16);
		nSend[8] = (BYTE)(dwBps>>8);
		nSend[9] = (BYTE)(dwBps);
		nSend[10] = 0xCF;
		nSend[11] = 0xFC;
		nLen = 12;
		break;
	case PROTOCOL_M_CAN:
		nSend[4] = 0x00;
		nSend[5] = 0x01;
		nSend[6] = (BYTE)dwBps;
		nSend[7] = 0xCF;
		nSend[8] = 0xFC;
		nLen = 9;
		break;
	default:
		return false;
	}
	if (!SendCmd(nSend,nLen))return false;

	//NO CAN:FC CF 55 02 00 04 00 00 28 B0 CF FC
	//   CAN:FC CF 55 02 00 01 09 CF FC
	BYTE nCount = 0;
	WORD wFlag = 0x0000;
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 >= 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xFCCF)
			break;
	}
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		nRecv[nCount++] = nTemp;
		if (nCount >= 20)
			return false; //避免收错数据导致溢出
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xCFFC)
			break;
	}

	if (nRecv[0]!=0x80 || nRecv[1]!=0x01)return false;
	if (nRecv[2] != 0x00)
	{
		SetErrorCode(nRecv[2]);
		return false;
	}
	return true;
}

//////////////////////////////////////////////////////////////////////////
// 设置通讯时序
bool CCom::SetTime(DWORD dwMaxRecv, DWORD dwSendSpace, DWORD dwSendB2B)
{
	BYTE nSend[20] = {0xFC,0xCF,0x55,0x03,0x00,0x0C,};
	BYTE nLen = 0;
	BYTE nRecv[20] = {0,};
	BYTE nTemp = 0x00;

	nSend[6] = (BYTE)(dwMaxRecv>>24);
	nSend[7] = (BYTE)(dwMaxRecv>>16);
	nSend[8] = (BYTE)(dwMaxRecv>>8);
	nSend[9] = (BYTE)(dwMaxRecv);

	nSend[10] = (BYTE)(dwSendSpace>>24);
	nSend[11] = (BYTE)(dwSendSpace>>16);
	nSend[12] = (BYTE)(dwSendSpace>>8);
	nSend[13] = (BYTE)(dwSendSpace);

	nSend[14] = (BYTE)(dwSendB2B>>24);
	nSend[15] = (BYTE)(dwSendB2B>>16);
	nSend[16] = (BYTE)(dwSendB2B>>8);
	nSend[17] = (BYTE)(dwSendB2B);

	nSend[18] = 0xCF;
	nSend[19] = 0xFC;
	nLen = 20;

	if (!SendCmd(nSend,nLen))return false;

	//FC CF 55 03 00 0C 00 00 03 E8 00 00 00 00 00 00 00 02 CF FC
	BYTE nCount = 0;
	WORD wFlag = 0x0000;
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 >= 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xFCCF)
			break;
	}
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		nRecv[nCount++] = nTemp;
		if (nCount >= 20)
			return false; //避免收错数据导致溢出
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xCFFC)
			break;
	}

	if (nRecv[0]!=0x80 || nRecv[1]!=0x01)return false;
	if (nRecv[2] != 0x00)
	{
		SetErrorCode(nRecv[2]);
		return false;
	}

	return true;
}

//////////////////////////////////////////////////////////////////////////
// 设置电平
bool CCom::SetVoltage(WORD wNumber,...)
{
	BYTE nSend[200] = {0xFC,0xCF,0x55,0x06,0xC1,0xC2,};
	BYTE nLen = 0;
	BYTE nRecv[20] = {0,};
	BYTE nTemp = 0x00;

	nSend[4] = (BYTE)((wNumber*2)>>8);
	nSend[5] = (BYTE)((wNumber*2));
	nLen = 6;

	va_list ap;
	va_start(ap,wNumber);
	for (WORD w=0; w<wNumber; w++)
	{
		WORD wVoltage = va_arg(ap,WORD);
		nSend[nLen++] = (BYTE)(wVoltage>>8);
		nSend[nLen++] = (BYTE)(wVoltage);
	}
	va_end(ap);
	nSend[nLen++] = 0xCF;
	nSend[nLen++] = 0xFC;

	if (!SendCmd(nSend,nLen))return false;

	//FC CF 55 06 00 06 01 2C 00 19 00 19 CF FC
	BYTE nCount = 0;
	WORD wFlag = 0x0000;
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 >= 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xFCCF)
			break;
	}
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		nRecv[nCount++] = nTemp;
		if (nCount >= 20)
			return false; //避免收错数据导致溢出
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xCFFC)
			break;
	}

	if (nRecv[0]!=0x80 || nRecv[1]!=0x01)return false;
	if (nRecv[2] != 0x00)
	{
		SetErrorCode(nRecv[2]);
		return false;
	}

	return true;
}


////////////////////////////////////////////////////////////////////////// ==这个函数未经测试,目前使用的是发一帧,多次接收

//////////////////////////////////////////////////////////////////////////
// 发送接收函数 - 发一帧收n帧
// 返回:
// pbRecv [0]=帧数(1) [[1][2]=第一帧长度(2) 第一帧(n)] [...] ...
WORD CCom::SendRecvMultiCmd(BYTE *pbSend, WORD wSendLen,BYTE *pbRecv)
{
	BYTE *pbCmd = new BYTE [wSendLen + 10];
	WORD wLen = 0;
	pbCmd[0] = 0xFC;
	pbCmd[1] = 0xCF;
	pbCmd[2] = 0x55;
	pbCmd[3] = 0x08;
	pbCmd[4] = (BYTE)(wSendLen>>8);
	pbCmd[5] = (BYTE)(wSendLen);
	memcpy(pbCmd+6,pbSend,wSendLen);
	pbCmd[6+wSendLen] = 0xCF;
	pbCmd[7+wSendLen] = 0xFC;
	wLen = 8+wSendLen;

	if (!SendCmd(pbCmd,wLen))
	{
			return 0;
	}


	delete [] pbCmd;

	//example for two frames:  LEN(00 18 = 00 07 + 00 05)
	//FC CF 00 18 83 F1 11 C1 EF 8F C4 CF FC FC CF 00 05 81 F1 11 C2 45 CF FC 
	BYTE bRecv[2048] = {0,};
	WORD nLen = 0;
	BYTE nTemp = 0x00;
	WORD wFlag = 0x0000;
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
//		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 >= 1*1000)
		{
			return 0;
		}
		if (!RecvCmd(&nTemp,1))
		{
			continue;
		}
		
		Test_OutPut(&nTemp,1);//test
		bRecv[nLen++] = nTemp;
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xFCCF)
		{
			break;
		}
	}

	WORD wRecvLen = 0x0000;
	BYTE nCount = 0;
	t0 = GetTickCount();
	while (1)
	{
//		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			return 0;
		}
		if (!RecvCmd(&nTemp,1))
		{
				continue;
		}
	
		Test_OutPut(&nTemp,1);//test
		bRecv[nLen++] = nTemp;
		wRecvLen = wRecvLen<<8|nTemp;
		nCount++;
		if (nCount == 2)
		{
			break;
		}
	
	}
	if (wRecvLen == 0)
	{
		return 0;
	}
	

	WORD w = 0;
	for (w=0; w<wRecvLen-4; w++)
	{
		t0 = GetTickCount();
		while (1)
		{
//			Sleep(1);
			t1 = GetTickCount();
			if (t1 - t0 > 1*1000)
			{
				return 0;
			}
			if (!RecvCmd(&nTemp,1))
			{
					continue;
			}
		
			Test_OutPut(&nTemp,1);//test
			bRecv[nLen++] = nTemp;
			break;
		}
	}
	if ((bRecv[nLen-2]<<8|bRecv[nLen-1]) != 0xCFFC)
	{
		return 0;
	}
	

	//假设命令中没有FC CF CF FC字节,如果有,则需要改代码换另外一种算法
	BYTE nFrame = 1;
	DWORD dwFlag = 0x00000000;
	for (w=0; w<nLen; w++)
	{
		dwFlag = dwFlag<<24|dwFlag<<16|dwFlag<<8|bRecv[w];
		if (dwFlag == 0xFCCFCFFC)
		{
			nFrame++;
		}
	}
	*pbRecv++ = nFrame;
	

	return 0;
}

//////////////////////////////////////////////////////////////////////////
// 发送接收函数 - 发一帧收一帧
WORD CCom::SendRecvCmd(BYTE *pbSend, WORD wSendLen, BYTE *pbRecv)
{
	BYTE *pbCmd = new BYTE [wSendLen + 10];
	WORD wLen = 0;
	pbCmd[0] = 0xFC;
	pbCmd[1] = 0xCF;
	pbCmd[2] = 0x55;
	pbCmd[3] = 0x09;
	pbCmd[4] = (BYTE)(wSendLen>>8);
	pbCmd[5] = (BYTE)(wSendLen);
	memcpy(pbCmd+6,pbSend,wSendLen);
	pbCmd[6+wSendLen] = 0xCF;
	pbCmd[7+wSendLen] = 0xFC;
	wLen = 8+wSendLen;

	if (!SendCmd(pbCmd,wLen)){delete [] pbCmd;;return 0;}

	//output
	Test_OutPut(pbSend,(BYTE)wSendLen);//test
	OutputDebugString("\r\n");

	delete [] pbCmd;

	//FC CF 55 09 00 06 82 11 f1 21 01 A6 CF FC
	//FC CF 00 06 82 F1 11 61 01 CC CF FC
	BYTE nTemp = 0x00;
	WORD wFlag = 0x0000;
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
//		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 >= 1*1000)
		{
			return 0;
		}
		if (!RecvCmd(&nTemp,1))continue;
//		Test_OutPut(&nTemp,1);//test
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xFCCF)
			break;
	}

	WORD wRecvLen = 0x0000;
	BYTE nCount = 0;
	t0 = GetTickCount();
	while (1)
	{
//		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			return 0;
		}
		if (!RecvCmd(&nTemp,1))continue;
//		Test_OutPut(&nTemp,1);//test
		wRecvLen = wRecvLen<<8|nTemp;
		nCount++;
		if (nCount == 2)break;
	}
	if (wRecvLen == 0)return 0;

	for (WORD w=0; w<wRecvLen; w++)
	{
		t0 = GetTickCount();
		while (1)
		{
//			Sleep(1);
			t1 = GetTickCount();
			if (t1 - t0 > 1*1000)
			{
				return 0;
			}
			if (!RecvCmd(&nTemp,1))continue;
//			Test_OutPut(&nTemp,1);//test
			//*pbRecv++ = nTemp;
			pbRecv[w] = nTemp;
			break;
		}
	}

	t0 = GetTickCount();
	while (1)
	{
//		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			return 0;
		}
		if (!RecvCmd(&nTemp,1))continue;
//		Test_OutPut(&nTemp,1);//test
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xCFFC)
			break;
	}

//	BYTE nTempBuf[255] = {0,};
//	memcpy(nTempBuf,pbRecv,(BYTE)wRecvLen);
	Test_OutPut(pbRecv,(BYTE)wRecvLen);
	OutputDebugString("\r\n\r\n");//test

	Sleep(1);//用Simulator的话需要延迟一下,ECU偶尔也会
	return wRecvLen;
}

//////////////////////////////////////////////////////////////////////////
// 只 发送函数
WORD CCom::SendCmdOnly(BYTE *pbSend, WORD wSendLen)
{
	BYTE *pbCmd = new BYTE [wSendLen + 10];
	WORD wLen = 0;
	pbCmd[0] = 0xFC;
	pbCmd[1] = 0xCF;
	pbCmd[2] = 0x55;
	pbCmd[3] = 0x0B;
	pbCmd[4] = (BYTE)(wSendLen>>8);
	pbCmd[5] = (BYTE)(wSendLen);
	memcpy(pbCmd+6,pbSend,wSendLen);
	pbCmd[6+wSendLen] = 0xCF;
	pbCmd[7+wSendLen] = 0xFC;
	wLen = 8+wSendLen;

	if (!SendCmd(pbCmd,wLen))
		return 0;

	delete [] pbCmd;

	return 1;
}

//////////////////////////////////////////////////////////////////////////
// 只 接收函数
WORD CCom::RecvCmdOnly(BYTE *pbRecv)
{
	BYTE nSend[] = {0xFC,0xCF,0x55,0x0C,0x00,0x00,0xCF,0xFC};
	WORD wLen = 8;

	if (!SendCmd(nSend,wLen))
		return 0;


	BYTE nTemp = 0x00;
	WORD wFlag = 0x0000;
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
//		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 >= 1*1000)
		{
			return 0;
		}
		if (!RecvCmd(&nTemp,1))continue;
//		Test_OutPut(&nTemp,1);//test
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xFCCF)
			break;
	}

	WORD wRecvLen = 0x0000;
	BYTE nCount = 0;
	t0 = GetTickCount();
	while (1)
	{
//		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			return 0;
		}
		if (!RecvCmd(&nTemp,1))continue;
//		Test_OutPut(&nTemp,1);//test
		wRecvLen = wRecvLen<<8|nTemp;
		nCount++;
		if (nCount == 2)break;
	}
	if (wRecvLen == 0)return 0;

	for (WORD w=0; w<wRecvLen; w++)
	{
		t0 = GetTickCount();
		while (1)
		{
//			Sleep(1);
			t1 = GetTickCount();
			if (t1 - t0 > 1*1000)
			{
				return 0;
			}
			if (!RecvCmd(&nTemp,1))continue;
//			Test_OutPut(&nTemp,1);//test
			//*pbRecv++ = nTemp;
			pbRecv[w] = nTemp;
			break;
		}
	}

	t0 = GetTickCount();
	while (1)
	{
//		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			return 0;
		}
		if (!RecvCmd(&nTemp,1))continue;
//		Test_OutPut(&nTemp,1);//test
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xCFFC)
			break;
	}

	Test_OutPut(pbRecv,(BYTE)wRecvLen);
	OutputDebugString("\r\n\r\n");//test

	return wRecvLen;
}

//////////////////////////////////////////////////////////////////////////
// 设置链路保持
bool CCom::SetKeepLink(DWORD dwTime, BYTE *pbSend, BYTE bSendLen)
{
	BYTE nSend[50] = {0xFC,0xCF,0x55,0x05,0xC1,0xC2};
	BYTE nLen = 0;
	BYTE nRecv[50] = {0,};
	BYTE nTemp = 0x00;

	nSend[4] = 0x00;
	nSend[5] = bSendLen+4;
	
	nSend[6] = (BYTE)(dwTime>>24);
	nSend[7] = (BYTE)(dwTime>>16);
	nSend[8] = (BYTE)(dwTime>>8);
	nSend[9] = (BYTE)(dwTime);

	for (BYTE i=0; i<bSendLen; i++)
	{
		nSend[10+i] = pbSend[i];
	}
	nSend[10+bSendLen] = 0xCF;
	nSend[11+bSendLen] = 0xFC;
	nLen = 12+bSendLen;

	if (!SendCmd(nSend,nLen))return false;

	//FC CF 55 05 00 09 00 00 07 D0 81 11 f1 3E C1 CF FC
	BYTE nCount = 0;
	WORD wFlag = 0x0000;
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 >= 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xFCCF)
			break;
	}
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		nRecv[nCount++] = nTemp;
		if (nCount >= 50)
			return false; //避免收错数据导致溢出
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xCFFC)
			break;
	}

	if (nRecv[0]!=0x80 || nRecv[1]!=0x01)
		return false;
	if (nRecv[2] != 0x00)
	{
		SetErrorCode(nRecv[2]);
		return false;
	}

	return true;
}


//////////////////////////////////////////////////////////////////////////
// 设置选线
bool CCom::SetChannel(BYTE nPortOne, BYTE nPortTwo)
{
//	BYTE nSend[20] = {0xFC,0xCF,0x55,0x01,0x00,0x01,0xCC,0xCF,0xFC};
	BYTE nSend[20] = {0xFC,0xCF,0x55,0x01,0x00,0x02,0x01,0xCC,0xCF,0xFC};
//	BYTE nLen = 9;
	BYTE nLen = 10;
	BYTE nRecv[20] = {0,};
	BYTE nTemp = 0x00;

	if (nPortOne>=16 || nPortTwo>=16)return false;
	if (nPortOne==4||nPortOne==5 || nPortTwo==4||nPortTwo==5)return false;

//	nSend[6] = 0x00;
	nSend[7] = 0x00;

	if (g_bProtocol == PROTOCOL_M_CAN)  //如果SetChannel()在 SetProtocol()/ProtocolSet()之前执行 将会出现错误.
	{
		nSend[6] = 0x02;
		if (nPortOne==PORT_06 && nPortTwo==PORT_14)
		{
			nSend[6] |= 0x10;
			nSend[7] = 0x18 | 0x40;
		}
		else if (nPortOne==PORT_03 && nPortTwo==PORT_08)
		{
			nSend[6] |= 0x20;
			nSend[7] = 0x04 | 0x60;
		}
		else if (nPortOne==PORT_03 && nPortTwo==PORT_11)
		{
			nSend[6] |= 0x30;
			nSend[7] = 0x04 | 0xC0;
		}
		else if (nPortOne==PORT_12 && nPortTwo==PORT_13)
		{
			nSend[6] |= 0x40;
			nSend[7] = 0x08 | 0x80;
		}
		else if (nPortOne==PORT_01 && nPortTwo==PORT_09)
		{
			nSend[6] |= 0x50;
			nSend[7] = 0x0C | 0xB0;
		}
		else
		{
			return false;
		}
	}
	else
	{
		nSend[6] = 0x01;
		switch (nPortOne)
		{
		case PORT_01:	nSend[7] = 0x0C;	break;
		case PORT_02:	nSend[7] = 0x14;	break;
		case PORT_03:	nSend[7] = 0x04;	break;
		case PORT_06:	nSend[7] = 0x18;	break;
		case PORT_07:	nSend[7] = 0x10;	break;
		case PORT_08:	nSend[7] = 0x60;	break;
		case PORT_09:	nSend[7] = 0xB0;	break;
		case PORT_10:	nSend[7] = 0x00;	break;
		case PORT_11:	nSend[7] = 0xC0;	break;
		case PORT_12:	nSend[7] = 0x08;	break;
		case PORT_13:	nSend[7] = 0x80;	break;
		case PORT_14:	nSend[7] = 0x40;	break;
		case PORT_15:	nSend[7] = 0x20;	break;
		default: return false;
		}
		switch (nPortTwo)
		{
		case PORT_01:	nSend[7] |= 0x0C;	break;
		case PORT_02:	nSend[7] |= 0x14;	break;
		case PORT_03:	nSend[7] |= 0x04;	break;
		case PORT_06:	nSend[7] |= 0x18;	break;
		case PORT_07:	nSend[7] |= 0x10;	break;
		case PORT_08:	nSend[7] |= 0x60;	break;
		case PORT_09:	nSend[7] |= 0xB0;	break;
		case PORT_10:	nSend[7] |= 0x00;	break;
		case PORT_11:	nSend[7] |= 0xC0;	break;
		case PORT_12:	nSend[7] |= 0x08;	break;
		case PORT_13:	nSend[7] |= 0x80;	break;
		case PORT_14:	nSend[7] |= 0x40;	break;
		case PORT_15:	nSend[7] |= 0x20;	break;
		default: return false;
		}
	}


	if (!SendCmd(nSend,nLen))return false;
	
	//FC CF 55 01 00 01 50 CF FC
	BYTE nCount = 0;
	WORD wFlag = 0x0000;
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 >= 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xFCCF)
			break;
	}
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		nRecv[nCount++] = nTemp;
		if (nCount >= 20)
			return false; //避免收错数据导致溢出
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xCFFC)
			break;
	}

	if (nRecv[0]!=0x80 || nRecv[1]!=0x01)return false;
	if (nRecv[2] != 0x00)
	{
		SetErrorCode(nRecv[2]);
		return false;
	}
	
	return true;
}

//////////////////////////////////////////////////////////////////////////
// 5波特率地址码进入系统方式
WORD CCom::AddressCodeEnter(BYTE bAddr,BYTE bParam,BYTE bKey2Time)
{
	BYTE nSend[20] = {0xFC,0xCF,0x55,0x0D,0x00,0x03,0xCC,0xCC,0xCC,0xCF,0xFC};
	BYTE nLen = 11;
	BYTE nRecv[200] = {0,};
	BYTE *pbRecv = nRecv;
	WORD wRecvLenTotal = 0x0000;
	BYTE nTemp = 0;

	//FC CF 55 0A 00 03 83 29 1E CF FC
	nSend[6] = bAddr;
	nSend[7] = bParam;
	nSend[8] = bKey2Time;

	if (!SendCmd(nSend,nLen))return 0;	

	BYTE nKeyNum = 0;
	if (bParam & ADDR_RECV_KW5BYTE)nKeyNum = 5;
	else nKeyNum = 2;

	BYTE nCount = 0;
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 >= 3*1000) //地址码需要3秒
		{
			return 0;
		}
		if (!RecvCmd(&nTemp,1))continue;
		Test_OutPut(&nTemp,1);//test
		nRecv[nCount++] = nTemp;
		if (nCount == nKeyNum)
		{
			wRecvLenTotal += nKeyNum;
			break; //接收完KeyWord
		}
	}

	if (bParam & ADDR_INVERSE_FROM_RCU)
	{
		nCount = 0;
		t0 = GetTickCount();
		while (1)
		{
			Sleep(1);
			t1 = GetTickCount();
			if (t1 - t0 >= 1*1000)
			{
				return 0;
			}
			if (!RecvCmd(&nTemp,1))continue;
			Test_OutPut(&nTemp,1);//test
			nRecv[nCount++] = nTemp;
			wRecvLenTotal++;
			if (nTemp == ~bAddr)
			{
				break; //已经接受到ECU发回来的地址码取反
			}
		}
	}

	if (bParam & ADDR_RECV_ONE_FRAME)
	{
		WORD wFlag = 0x0000;
		DWORD t0,t1;
		t0 = GetTickCount();
		while (1)
		{
//			Sleep(1);
			t1 = GetTickCount();
			if (t1 - t0 >= 1*1000)
			{
				return 0;
			}
			if (!RecvCmd(&nTemp,1))continue;
			Test_OutPut(&nTemp,1);//test
			wFlag = wFlag<<8|nTemp;
			if (wFlag == 0xFCCF)
				break;
		}

		WORD wRecvLen = 0x0000;
		BYTE nCount = 0;
		t0 = GetTickCount();
		while (1)
		{
//			Sleep(1);
			t1 = GetTickCount();
			if (t1 - t0 > 1*1000)
			{
				return 0;
			}
			if (!RecvCmd(&nTemp,1))continue;
			Test_OutPut(&nTemp,1);//test
			wRecvLen = wRecvLen<<8|nTemp;
			nCount++;
			if (nCount == 2)break;
		}
		if (wRecvLen == 0)return 0;

		for (WORD w=0; w<wRecvLen; w++)
		{
			t0 = GetTickCount();
			while (1)
			{
//				Sleep(1);
				t1 = GetTickCount();
				if (t1 - t0 > 1*1000)
				{
					return 0;
				}
				if (!RecvCmd(&nTemp,1))continue;
				Test_OutPut(&nTemp,1);//test
				*pbRecv++ = nTemp;
				break;
			}
		}

		t0 = GetTickCount();
		while (1)
		{
//			Sleep(1);
			t1 = GetTickCount();
			if (t1 - t0 > 1*1000)
			{
				return 0;
			}
			if (!RecvCmd(&nTemp,1))continue;
			Test_OutPut(&nTemp,1);//test
			wFlag = wFlag<<8|nTemp;
			if (wFlag == 0xCFFC)
				break;
		}
		wRecvLenTotal += wRecvLen;
	}
	
	if (bParam & ADDR_RECV_MUTI_FRAME)
	{
		//接收多帧，和接收单帧一样，循环几次而已。但要判断最后一帧接收完成标记(Len_H,Len_L,80,00,01(ERR_OK))
	}

	return wRecvLenTotal;
}

//////////////////////////////////////////////////////////////////////////
// 5波特率地址码进入系统方式
WORD CCom::AddressCodeEnter_Bosch(BYTE *bRecv,BYTE bAddr,BYTE bParam,BYTE bKey2Time)
{
	BYTE nSend[20] = {0xFC,0xCF,0x55,0x10,0x00,0x03,0xCC,0xCC,0xCC,0xCF,0xFC};
	BYTE nLen = 11;
	BYTE nRecv[200] = {0,};
	BYTE *pbRecv = nRecv;
	WORD wRecvLenTotal = 0x0000;
	BYTE nTemp = 0;

	//FC CF 55 10 00 03 83 29 1E CF FC
	nSend[6] = bAddr;
	nSend[7] = bParam;
	nSend[8] = bKey2Time;

	if (!SendCmd(nSend,nLen))return 0;

	//bRecv接收存放格式
	//帧数[1] 长度1[1] 数据1[n] 长度2[1] 数据2[n] ...
	//补充说明:KeyWord也是一帧数据.一般为第一帧,存放在长度1和数据1中.
	bRecv[0] = 0x00;
	WORD wIndex = 0;

	BYTE nKeyNum = 0;
	if (bParam & ADDR_RECV_KW5BYTE)nKeyNum = 5;
	else nKeyNum = 2;

	BYTE nCount = 0;
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 >= 5*1000) //地址码需要3秒
		{
			return 0;
		}
		if (!RecvCmd(&nTemp,1))continue;
		Test_OutPut(&nTemp,1);//test
		nRecv[nCount++] = nTemp;
		if (nCount == nKeyNum)
		{
			wRecvLenTotal += nKeyNum;
			bRecv[0]++;
			bRecv[1] = nCount;
			memcpy(bRecv+2,nRecv,nCount);
			wIndex += 2;
			wIndex += nCount;
			break; //接收完KeyWord
		}
	}

	if (bParam & ADDR_INVERSE_FROM_RCU)
	{
		nCount = 0;
		t0 = GetTickCount();
		while (1)
		{
			Sleep(1);
			t1 = GetTickCount();
			if (t1 - t0 >= 1*1000)
			{
				return 0;
			}
			if (!RecvCmd(&nTemp,1))continue;
			Test_OutPut(&nTemp,1);//test
			nRecv[nCount++] = nTemp;
			wRecvLenTotal++;
			if (nTemp == ~bAddr)
			{
				break; //已经接受到ECU发回来的地址码取反
			}
		}
	}

	if (bParam & ADDR_RECV_ONE_FRAME)
	{
		WORD wFlag = 0x0000;
		DWORD t0,t1;
		t0 = GetTickCount();
		while (1)
		{
//			Sleep(1);
			t1 = GetTickCount();
			if (t1 - t0 >= 1*1000)
			{
				return 0;
			}
			if (!RecvCmd(&nTemp,1))continue;
			Test_OutPut(&nTemp,1);//test
			wFlag = wFlag<<8|nTemp;
			if (wFlag == 0xFCCF)
				break;
		}

		WORD wRecvLen = 0x0000;
		BYTE nCount = 0;
		t0 = GetTickCount();
		while (1)
		{
//			Sleep(1);
			t1 = GetTickCount();
			if (t1 - t0 > 1*1000)
			{
				return 0;
			}
			if (!RecvCmd(&nTemp,1))continue;
			Test_OutPut(&nTemp,1);//test
			wRecvLen = wRecvLen<<8|nTemp;
			nCount++;
			if (nCount == 2)break;
		}
		if (wRecvLen == 0)return 0;

		nCount = 0;
		for (WORD w=0; w<wRecvLen; w++)
		{
			t0 = GetTickCount();
			while (1)
			{
//				Sleep(1);
				t1 = GetTickCount();
				if (t1 - t0 > 1*1000)
				{
					return 0;
				}
				if (!RecvCmd(&nTemp,1))continue;
				Test_OutPut(&nTemp,1);//test
				//*pbRecv++ = nTemp;
				nRecv[nCount++] = nTemp;
				break;
			}
		}
		bRecv[0]++;
		bRecv[wIndex++] = (BYTE)wRecvLen;
		memcpy(bRecv+wIndex,nRecv,nCount);
		wIndex += nCount;

		t0 = GetTickCount();
		while (1)
		{
//			Sleep(1);
			t1 = GetTickCount();
			if (t1 - t0 > 1*1000)
			{
				return 0;
			}
			if (!RecvCmd(&nTemp,1))continue;
			Test_OutPut(&nTemp,1);//test
			wFlag = wFlag<<8|nTemp;
			if (wFlag == 0xCFFC)
				break;
		}
		wRecvLenTotal += wRecvLen;
	}
	
	if (bParam & ADDR_RECV_MUTI_FRAME)
	{
		//接收多帧，和接收单帧一样，循环几次而已。但要判断最后一帧接收完成标记(收不到了)
		while (1)
		{
			WORD wFlag = 0x0000;
			DWORD t0,t1;
			t0 = GetTickCount();
			while (1)
			{
	//			Sleep(1);
				t1 = GetTickCount();
				if (t1 - t0 >= 1*1000)
				{
				//	return 0;
					return wIndex;  //收不到的时候为收完了.
				}
				if (!RecvCmd(&nTemp,1))continue;
				Test_OutPut(&nTemp,1);//test
				wFlag = wFlag<<8|nTemp;
				if (wFlag == 0xFCCF)
					break;
			}

			WORD wRecvLen = 0x0000;
			BYTE nCount = 0;
			t0 = GetTickCount();
			while (1)
			{
	//			Sleep(1);
				t1 = GetTickCount();
				if (t1 - t0 > 1*1000)
				{
					return 0;
				}
				if (!RecvCmd(&nTemp,1))continue;
				Test_OutPut(&nTemp,1);//test
				wRecvLen = wRecvLen<<8|nTemp;
				nCount++;
				if (nCount == 2)break;
			}
			if (wRecvLen == 0)return 0;

			nCount = 0;
			for (WORD w=0; w<wRecvLen; w++)
			{
				t0 = GetTickCount();
				while (1)
				{
	//				Sleep(1);
					t1 = GetTickCount();
					if (t1 - t0 > 1*1000)
					{
						return 0;
					}
					if (!RecvCmd(&nTemp,1))continue;
					Test_OutPut(&nTemp,1);//test
					//*pbRecv++ = nTemp;
					nRecv[nCount++] = nTemp;
					break;
				}
			}
			bRecv[0]++;
			bRecv[wIndex++] = (BYTE)wRecvLen;
			memcpy(bRecv+wIndex,nRecv,nCount);
			wIndex += nCount;

			t0 = GetTickCount();
			while (1)
			{
	//			Sleep(1);
				t1 = GetTickCount();
				if (t1 - t0 > 1*1000)
				{
					return 0;
				}
				if (!RecvCmd(&nTemp,1))continue;
				Test_OutPut(&nTemp,1);//test
				wFlag = wFlag<<8|nTemp;
				if (wFlag == 0xCFFC)
					break;
			}
			wRecvLenTotal += wRecvLen;
		}
	}

//	return wRecvLenTotal;
	return wIndex;
}


//////////////////////////////////////////////////////////////////////////
// KWP2000快速初始化
WORD CCom::KwpFlashInit(BYTE *pbSend,BYTE bLen,BYTE *pbRecv)
{
	BYTE nSend[50] = {0xFC,0xCF,0x55,0x0E,0x05,0x06,0x01,0x2C,0x00,0x19,0x00,0x19,};
	BYTE nLen = 0;

	if (bLen >= 50)return 0;
	//nLen0[1] nLen1[1] Voltage[n] Command[n] //nLen1==len(Voltage); nLen0==len(Command)
	//FC CF 55 0B 05 06 01 2C 00 19 00 19 81 11 F1 81 04 CF FC
	
	nSend[4] = bLen;
	for (BYTE i=0; i<bLen; i++)
	{
		nSend[12+i] = pbSend[i];
	}
	nSend[12+bLen] = 0xCF;
	nSend[12+bLen+1] = 0xFC;
	nLen = 12+bLen+2;

	if (!SendCmd(nSend,nLen))
		return 0;

	return RecvCmdOnly(pbRecv);
}

bool CCom::SetCanFilter(WORD wCanID)
{
	BYTE nSend[200] = {0xFC,0xCF,0x55,0x07,0x00,0x02,0xC1,0xC2,0xCF,0xFC};
	BYTE nLen = 10;
	BYTE nRecv[20] = {0,};
	BYTE nTemp = 0x00;
	
	nSend[6] = (BYTE)(wCanID>>8);
	nSend[7] = (BYTE)wCanID;

	if (!SendCmd(nSend,nLen))return false;

	//FC CF 55 07 00 02 07 09 CF FC
	BYTE nCount = 0;
	WORD wFlag = 0x0000;
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 >= 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xFCCF)
			break;
	}
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		nRecv[nCount++] = nTemp;
		if (nCount >= 20)
			return false; //避免收错数据导致溢出
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xCFFC)
			break;
	}

	if (nRecv[0]!=0x80 || nRecv[1]!=0x01)return false;
	if (nRecv[2] != 0x00)
	{
		SetErrorCode(nRecv[2]);
		return false;
	}

	return true;
}

bool CCom::SetCanContinueFrame(BYTE *pbContinueFrame, BYTE nContinueFrameLen)
{
	BYTE nSend[200] = {0xFC,0xCF,0x55,0x08,0xC1,0xC2,};
	BYTE nLen = 0;
	BYTE nRecv[20] = {0,};
	BYTE nTemp = 0x00;
	
	nSend[4] = 0x00;
	nSend[5] = nContinueFrameLen;
	for (BYTE i=0; i<nContinueFrameLen; i++)
	{
		nSend[6+i] = pbContinueFrame[i];
	}
	nSend[6+nContinueFrameLen] = 0xCF;
	nSend[6+nContinueFrameLen+1] = 0xFC;
	nLen = 6+nContinueFrameLen+2;

	if (!SendCmd(nSend,nLen))return false;

	//FC CF 55 08 00 0B 08 07 19 30 00 00 00 00 00 00 00 CF FC 
	BYTE nCount = 0;
	WORD wFlag = 0x0000;
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 >= 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xFCCF)
			break;
	}
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		nRecv[nCount++] = nTemp;
		if (nCount >= 20)
			return false; //避免收错数据导致溢出
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xCFFC)
			break;
	}

	if (nRecv[0]!=0x80 || nRecv[1]!=0x01)return false;
	if (nRecv[2] != 0x00)
	{
		SetErrorCode(nRecv[2]);
		return false;
	}

	return true;
}



bool CCom::SetParity_UART2(BYTE bParity)
{
	BYTE nSend[20] = {0xFC,0xCF,0x55,0x0F,0xC1,0xC2,0xCC,0xCF,0xFC};
	BYTE nLen = 9;
	BYTE nRecv[20] = {0,};
	BYTE nTemp = 0x00;
	
	nSend[4] = 0x00;
	nSend[5] = 0x01;
	nSend[6] = bParity;

	if (!SendCmd(nSend,nLen))return false;

	//FC CF 55 0F 00 01 XX CF FC 
	BYTE nCount = 0;
	WORD wFlag = 0x0000;
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 >= 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xFCCF)
			break;
	}
	t0 = GetTickCount();
	while (1)
	{
		Sleep(1);
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			return false;
		}
		if (!RecvCmd(&nTemp,1))continue;
		nRecv[nCount++] = nTemp;
		if (nCount >= 20)
			return false; //避免收错数据导致溢出
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xCFFC)
			break;
	}

	if (nRecv[0]!=0x80 || nRecv[1]!=0x01)return false;
	if (nRecv[2] != 0x00)
	{
		SetErrorCode(nRecv[2]);
		return false;
	}

	return true;
}






//////////////////////////////////////////////////////////////////////////
//
//返回FF为错误, 0为不加密(此情况时dwSeed无效), 返回其它为加密模式ID值
//
BYTE CCom::CheckEncrypt(char *szFile,DWORD &dwSeed)
{
	FILE *fp  = fopen(szFile,"rb");
	if (fp == NULL)return 0xFF;
	BYTE bByte;
	int i = 0;
	i = fgetc(fp);	bByte = (BYTE)i;	if (bByte != 'Z')return 0;
	i = fgetc(fp);	bByte = (BYTE)i;	if (bByte != 'D')return 0;
	i = fgetc(fp);	bByte = (BYTE)i;	if (bByte != 'Y')return 0;
	i = fgetc(fp);	bByte = (BYTE)i;	if (bByte != 'B')return 0;

	BYTE bSeed[4] = {0,};
	fseek(fp,0x0d,0);
	i = fgetc(fp);	bByte = (BYTE)i;	bSeed[0] = bByte;
	i = fgetc(fp);	bByte = (BYTE)i;	bSeed[1] = bByte;
	i = fgetc(fp);	bByte = (BYTE)i;	bSeed[2] = bByte;
	i = fgetc(fp);	bByte = (BYTE)i;	bSeed[3] = bByte;
	dwSeed = (DWORD)(bSeed[0]<<24|bSeed[1]<<16|bSeed[2]<<8|bSeed[3]);

	fclose(fp);
	return 1;
}
BYTE CCom::SeedToKey(BYTE *bSeed,BYTE *bKey)
{
	BYTE nRecvBuf[100] = {0,};
	WORD wRecvLen = 0;
	BYTE nCmd[10] = {0xA5,0xA5,0x00,0x05,0xC0,0x11,0x22,0x33,0x44,0x55};
	nCmd[5] = bSeed[0];
	nCmd[6] = bSeed[1];
	nCmd[7] = bSeed[2];
	nCmd[8] = bSeed[3];
	SendCmd(nCmd,10);
	if (!RecvKwpOneFrame(nRecvBuf,wRecvLen))return 0;
	if (wRecvLen != 4)return 0;
	memcpy(bKey,nRecvBuf,4);
	
	return 1;
}
BYTE CCom::DecryptData(BYTE *bBuf,DWORD dwLen,BYTE *bKey)
{
	BYTE *pData = new BYTE [dwLen];
	if (pData == NULL)return 0;
	for (DWORD dw=0; dw<dwLen; dw++)
	{
		pData[dw] = bBuf[dw] ^ bKey[0];
	}
	memcpy(bBuf,pData,dwLen);
	delete [] pData;
	return 1;
}








DWORD CCom::MySeed2Key(DWORD dwSeed,BYTE bLoopTimes)
{
	BYTE i = 0;
	DWORD dwKey = dwSeed;
	BYTE bSeed[4] = {0,};
	BYTE bKey[4] = {0,};
	bSeed[0] = (BYTE)(dwSeed>>0x18);
	bSeed[1] = (BYTE)(dwSeed>>0x10);
	bSeed[2] = (BYTE)(dwSeed>>0x08);
	bSeed[3] = (BYTE)(dwSeed>>0x00);
	if (bSeed[1] || bSeed[2])
	{
		for (i=0; i<0x23*bLoopTimes; i++)
		{
			if (dwKey & 0x80000000)
			{
				dwKey = 2 *dwKey ^ 0xE4C5C784;
			}
			else
			{
				dwKey *= 2;
			}
		}
	}
	else
	{
		dwKey = dwSeed;
	}
	return dwKey;
}
bool CCom::MySecurityAccess()
{
	BYTE bSendBuf[20],bRecvBuf[20];
	WORD bSendLen,bRecvLen;
	BYTE bTemp[20];
	DWORD dwSeed,dwKey;

	if (m_handle == NULL)return false;

	//A5 A5 00 04 FE 01 27 01 55
	//A5 A5 00 08 FE 01 67 01 CC CC CC CC 55
	memcpy(bSendBuf,(BYTE *)"\xA5\xA5\x00\x04\xFE\x01\x27\x01\x55",bSendLen=9);
	if (!SendCmd(bSendBuf,bSendLen))
	{
		return false;
	}

	if (!RecvCmdEx(bRecvBuf,bRecvLen=13,3000))
	{
		return false;
	}
	
	memcpy(bTemp,bRecvBuf,8);
	if (memcmp(bTemp,(BYTE *)"\xA5\xA5\x00\x08\xFE\x01\x67\x01",8) != 0)
	{
		return false;
	}
	
	if (bRecvBuf[12] != 0x55)
	{
		return false;
	}
	
	dwSeed = (DWORD)bRecvBuf[8]<<24|bRecvBuf[9]<<16|bRecvBuf[10]<<8|bRecvBuf[11];

	dwKey = MySeed2Key(dwSeed,1);

	//A5 A5 00 08 FE 02 27 02 CC CC CC CC 55
	//A5 A5 00 05 FE 02 67 02 [34] 55 //OK
	memcpy(bSendBuf,(BYTE *)"\xA5\xA5\x00\x08\xFE\x02\x27\x02\xCC\xCC\xCC\xCC\x55",bSendLen=13);
	bSendBuf[8]  = (BYTE)(dwKey>>0x18);
	bSendBuf[9]  = (BYTE)(dwKey>>0x10);
	bSendBuf[10] = (BYTE)(dwKey>>0x08);
	bSendBuf[11] = (BYTE)(dwKey>>0x00);
	if (!SendCmd(bSendBuf,bSendLen))
	{
			return false;
	}

	if (!RecvCmdEx(bRecvBuf,bRecvLen=10))
	{
		return false;
	}
	
	memcpy(bTemp,bRecvBuf,8);
	if (memcmp(bTemp,(BYTE *)"\xA5\xA5\x00\x05\xFE\x02\x67\x02",8) != 0)
	{
		return false;
	}

	if (bRecvBuf[9] != 0x55)
	{
		return false;
	}
	
	if (bRecvBuf[8] != 0x34)
	{
		return false;
	}
	

	return true;
}
//解密pFile文件到pBuf中
bool CCom::MyDeCryptDataFile(char *pFile,BYTE *pBuf,DWORD &dwLen)
{
	FILE *fp = fopen(pFile,"rb");
	if (fp == NULL)return false;
	if (fread(pBuf,sizeof(char),dwLen,fp) != dwLen){fclose(fp);return false;}
	fclose(fp);
	BYTE nEnCryptedTypeIndex = pBuf[0x0D];
	BYTE bSP = (pBuf[0x0F] % (256-4)) + 0x10; //seed position
	DWORD dwSeed = (DWORD)pBuf[bSP]<<24|pBuf[bSP+1]<<16|pBuf[bSP+2]<<8|pBuf[bSP+3];

//	A5 A5 00 06 FE 03 12 34 56 78 55
//	A5 A5 00 06 FE 03 CC CC CC CC 55
	BYTE bSendBuf[20],bRecvBuf[20];
	WORD bSendLen,bRecvLen;
	BYTE bTemp[20];
	memcpy(bSendBuf,(BYTE *)"\xA5\xA5\x00\x06\xFE\x03\x12\x34\x56\x78\x55",bSendLen=11);
	bSendBuf[6] = (BYTE)(dwSeed>>0x18);
	bSendBuf[7] = (BYTE)(dwSeed>>0x10);
	bSendBuf[8] = (BYTE)(dwSeed>>0x08);
	bSendBuf[9] = (BYTE)(dwSeed>>0x00);
	if (!SendCmd(bSendBuf,bSendLen))return false;
	if (!RecvCmdEx(bRecvBuf,bRecvLen=11))return false;
	memcpy(bTemp,bRecvBuf,6);
	if (memcmp(bTemp,(BYTE *)"\xA5\xA5\x00\x06\xFE\x03",6) != 0)return false;
	if (bRecvBuf[10] != 0x55)return false;
	DWORD dwKey = (DWORD)bRecvBuf[6]<<24|bRecvBuf[7]<<16|bRecvBuf[8]<<8|bRecvBuf[9];
	BYTE bKey[4] = {(BYTE)(dwKey>>24),(BYTE)(dwKey>>16),(BYTE)(dwKey>>8),(BYTE)dwKey};
	const DWORD const_addr = 0x00000800;
	for (DWORD i=0; i<dwLen-const_addr; i++)
	{
		pBuf[i+const_addr] = pBuf[i+const_addr] ^ (i%256);
		pBuf[i+const_addr] = pBuf[i+const_addr] ^ bKey[i%4];
	}
//	fclose(fp); del
	dwLen -= const_addr;
	return true;
}

int CCom::GetEcuPinBaudRate(vector<BYTE> vecbPin, map<BYTE, DWORD> &mapPinBaud)
{
	int nPinSize = 0;
	nPinSize = vecbPin.size();

	if (nPinSize == 0)
	{
		return -1;  //传入引脚个数为空
	}

	int  nSendCmdLen = 0;
	int nSendDataLen = 0;
	nSendDataLen = 1 + nPinSize;
	nSendCmdLen = 4 + nSendDataLen + 1;

	int  nRecvCmdLen = 0;
	int nRecvDataLen = 0;
	nRecvDataLen = 1 + 4 * nPinSize;
	nRecvCmdLen = 4 + nRecvDataLen + 1;

	BYTE *pSendCmd = new BYTE[nSendCmdLen + 1];
	BYTE *pRecvCmd = new BYTE[nRecvCmdLen + 1];

	memset(pSendCmd, 0 , nSendCmdLen + 1);
	memset(pRecvCmd, 0, nRecvCmdLen + 1);

	pSendCmd[0] = (BYTE)0xA5;
	pSendCmd[1] = (BYTE)0xA5;
	pSendCmd[2] = (BYTE)(nSendDataLen >> 8);
	pSendCmd[3] = (BYTE)nSendDataLen;
	pSendCmd[4] = (BYTE)0xEB;
	for (int i = 0; i < nPinSize; i++)
	{
		pSendCmd[5 + i] = vecbPin[i];
	}
	pSendCmd[nSendCmdLen - 1] = (BYTE)0x55;

	//发送命令
	SendCmd(pSendCmd, nSendCmdLen);

	//接收命令
	if (!RecvCmd(pRecvCmd, 2)) //接收A5 A5
	{
		if (!RecvCmd(pRecvCmd, 2))
		{
			if (pSendCmd)
			{
				delete[]pSendCmd;
				pSendCmd = NULL;
			}

			if (pRecvCmd)
			{
				delete[]pRecvCmd;
				pRecvCmd = NULL;
			}
			return -2;
		}
	}

	if (pRecvCmd[0] != 0xA5 &&
		pRecvCmd[1] != 0xA5)
	{
		if (pSendCmd)
		{
			delete[]pSendCmd;
			pSendCmd = NULL;
		}

		if (pRecvCmd)
		{
			delete[]pRecvCmd;
			pRecvCmd = NULL;
		}
		return -2;
	}

	if (!RecvCmd(pRecvCmd + 2, 2)) //接收长度
	{
		if (pSendCmd)
		{
			delete[]pSendCmd;
			pSendCmd = NULL;
		}

		if (pRecvCmd)
		{
			delete[]pRecvCmd;
			pRecvCmd = NULL;
		}
		return -2;
	}

	int nRecvLen = 0;

	nRecvLen = pRecvCmd[2] << 8 | pRecvCmd[3];

	if (nRecvDataLen != nRecvLen)
	{
		if (pSendCmd)
		{
			delete[]pSendCmd;
			pSendCmd = NULL;
		}

		if (pRecvCmd)
		{
			delete[]pRecvCmd;
			pRecvCmd = NULL;
		}
		return -2;  //实际接收的数据长度存在问题
	}

	if (!RecvCmd(pRecvCmd + 4, nRecvLen)) //接收实际数据
	{
		if (pSendCmd)
		{
			delete[]pSendCmd;
			pSendCmd = NULL;
		}

		if (pRecvCmd)
		{
			delete[]pRecvCmd;
			pRecvCmd = NULL;
		}
		return -2;
	}

	if (pRecvCmd[4] != 0xEB)
	{
		if (pSendCmd)
		{
			delete[]pSendCmd;
			pSendCmd = NULL;
		}

		if (pRecvCmd)
		{
			delete[]pRecvCmd;
			pRecvCmd = NULL;
		}
		return -2;
	}

	if (!RecvCmd(pRecvCmd + 4 + nRecvLen, 1)) //接收55
	{
		if (pSendCmd)
		{
			delete[]pSendCmd;
			pSendCmd = NULL;
		}

		if (pRecvCmd)
		{
			delete[]pRecvCmd;
			pRecvCmd = NULL;
		}
		return -2;
	}

	if (pRecvCmd[nRecvCmdLen - 1] != 0x55)
	{
		if (pSendCmd)
		{
			delete[]pSendCmd;
			pSendCmd = NULL;
		}

		if (pRecvCmd)
		{
			delete[]pRecvCmd;
			pRecvCmd = NULL;
		}
		return -2;
	}

	//接收正常，进行解析处理
	BYTE *pRecvCmdTmp = new BYTE[nRecvCmdLen + 1];
	memset(pRecvCmdTmp, 0, nRecvCmdLen + 1);
	memcpy(pRecvCmdTmp, pRecvCmd + 5, nRecvLen - 1);

	mapPinBaud.clear();

	for (int j = 0; j < nPinSize; j++)
	{
		BYTE bPin = 0;
		DWORD dwBaud = 0;
		bPin = (BYTE)pRecvCmdTmp[j*4];
		dwBaud = pRecvCmdTmp[j * 4 + 1] << 16 | pRecvCmdTmp[j * 4 + 2] << 8 | pRecvCmdTmp[j * 4 + 3];
		//mapPinBaud.insert(pair<BYTE, DWORD>(bPin, dwBaud));  //使用该方式，一旦key重复， 就插入失败
		mapPinBaud[bPin] = dwBaud;  //key重复内容进行覆盖
	}

	if (pSendCmd)
	{
		delete[]pSendCmd;
		pSendCmd = NULL;
	}

	if (pRecvCmd)
	{
		delete[]pRecvCmd;
		pRecvCmd = NULL;
	}

	if (pRecvCmdTmp)
	{
		delete[]pRecvCmdTmp;
		pRecvCmdTmp = NULL;
	}

	return 1;
}