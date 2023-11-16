// CommWithEcu.cpp: implementation of the CCommWithEcu class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "CommWithEcu.h"
#include "Debug.h"


//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

extern CCom g_Com;
extern CGui g_Gui;


//设置CAN过滤模式,其实can已经在MCU中处理了多帧,这里只设置一个全局标记位g_bCanFilterMode
//为true表示使用MCU自己处理多帧,即处理原来的(3,0x10,4)硬件过滤, 否则为收发单帧(多帧要自己去处理)
//在SetCanContinueFrame()中置为true,  在SetProtocol()为置为false
//所以执行顺序是先SetProtocol(),后SetCanContinueFrame();  这样可以避免有些系统用硬件过滤,但有些自己处理,切换时错误.
bool g_bCanFilterMode = false;


//SetProtocol()或ProtocolSet()之后保存下协议类型,提供给其它地方使用.如选线时.
BYTE g_bProtocol = PROTOCOL_M_KWP;

CCommWithEcu::CCommWithEcu()
{
	m_bBeginIsRunning = false;
	m_SendFrame.Clear();
	m_RecvFrame.clear();
}

CCommWithEcu::~CCommWithEcu()
{

}

void CCommWithEcu::Init (void)
{

/*
	bool b = g_Com.ComOpen(4); //(YTOPower)
	if (b)
		OutputDebugString("COM4打开成功\r\n");
	else
		OutputDebugString("COM4打开失败\r\n");
*/


	//
	char exeFullPath[MAX_PATH];
	DWORD dw = GetModuleFileName(NULL,exeFullPath,MAX_PATH);
	int i = 0;
	for (i=strlen(exeFullPath); i>0; i--){if (exeFullPath[i] ==SPLIT){exeFullPath[i] = '\0';break;}}
	for (i=strlen(exeFullPath); i>0; i--){if (exeFullPath[i] ==SPLIT){exeFullPath[i] = '\0';break;}}
	for (i=strlen(exeFullPath); i>0; i--){if (exeFullPath[i] ==SPLIT){exeFullPath[i] = '\0';break;}}
	for (i=strlen(exeFullPath); i>0; i--){if (exeFullPath[i] ==SPLIT){exeFullPath[i] = '\0';break;}}
	char szPath[1024];
	char szPathComm[1024];
#ifdef _DEBUG
	//for (i=strlen(exeFullPath); i>0; i--){if (exeFullPath[i] == SPLIT){exeFullPath[i] = '\0';break;}}
	strcpy(szPath,exeFullPath);
	strcpy(szPathComm,exeFullPath);
	strcat(szPath,"/Debug/config/wireless.txt");
	strcat(szPathComm,"/Debug/config/com.txt");
#else
	strcpy(szPath,exeFullPath);
	strcpy(szPathComm,exeFullPath);
	strcat(szPath,"/Release/config/wireless.txt");
	strcat(szPathComm,"/Release/config/com.txt");
#endif
	
	string strPathFile = szPath;

	char szLine[255];
	FILE *fp = NULL;
	fp = fopen(strPathFile.c_str(),"r");
	if (fp == NULL)return ;
	fgets(szLine,255,fp);
	BYTE bWireless = atoi(szLine);
	fclose(fp);
	
	fp = fopen(szPathComm,"r");
	if (fp == NULL)return ;
	fgets(szLine,255,fp);
	BYTE bCom = atoi(szLine);
	fclose(fp);

	if (bWireless==0 || bWireless==1)
	{
		g_Gui.MsgShowMessage("正在加载诊断程序,请稍后...",MSG_MB_NOBUTTON);
		if (g_Com.ComOpen(bCom))
		{
			if (bWireless == 0)
				OutputDebugString("打开USB OK\r\n");
			else
				OutputDebugString("打开蓝牙 OK\r\n");

//不用设置灯了
//			if (!g_Com.SetBluetoothLedStatus(bWireless))
//				if (!g_Com.SetBluetoothLedStatus(bWireless))
//					if (!g_Com.SetBluetoothLedStatus(bWireless))
//					{
//						OutputDebugString("设置VCI灯失败\r\n");
//					}
		}
	}
	else
	{
		g_Gui.CloseDialogWait();
		g_Gui.CloseDialogWait();
		g_Gui.CloseDialogWait();
		g_Gui.CloseDialogWait();
		g_Gui.CloseDialogWait();
		g_Gui.CloseDialogWait();
	}
	g_Gui.CloseDialogWait();


/* del
	bool bMark = false;
	if (bWireless == 0)
	{
		g_Gui.MsgShowMessage("正在加载诊断程序,请稍后...",MSG_MB_NOBUTTON);
		bMark = g_Com.ComOpen(5);//4
		if (bMark)
			OutputDebugString("打开USB OK\r\n");
		if (!g_Com.SetBluetoothLedStatus(0))
			if (!g_Com.SetBluetoothLedStatus(0))
				if (!g_Com.SetBluetoothLedStatus(0))
				{
					//
				}
	}
	else if (bWireless == 1)
	{
		g_Gui.MsgShowMessage("正在加载诊断程序,请稍后...",MSG_MB_NOBUTTON);
		bMark = g_Com.ComOpen(9);//7
		if (bMark)
			OutputDebugString("打开蓝牙 OK\r\n");
		if (!g_Com.SetBluetoothLedStatus(1))
			if (!g_Com.SetBluetoothLedStatus(1))
				if (!g_Com.SetBluetoothLedStatus(1))
				{
					//
				}
	}
	else
	{
		g_Gui.CloseDialogWait();
		g_Gui.CloseDialogWait();
		g_Gui.CloseDialogWait();
		g_Gui.CloseDialogWait();
		g_Gui.CloseDialogWait();
		g_Gui.CloseDialogWait();
	}
	g_Gui.CloseDialogWait();
*/
	

}
void CCommWithEcu::Destroy (void)
{
	g_Com.ComClose();

	//
}



//////////////////////////////////////////////////////////////////////////
// Begin和End 在这里KWP进入系统的时候不能分开,否则无法进入系统(KWP的要求低25ms高25ms之后要立即发命令)
// 所以在这里解决.但不是打包的形式.
// 如果还有其它地方用到Begin和End(不是KWP进入系统的情况),则要单独处理
//

void CCommWithEcu::Begin (void)
{
	m_bBeginIsRunning = true;
}
CReceiveFrame CCommWithEcu::End (void)
{
	m_bBeginIsRunning = false;

	CReceiveFrame rf;
	if (m_SendFrame.GetFrameNumber() == 1) //目前只处理KWP进入系统的情况
	{
		BYTE nSendBuf[50] = {0,};
		BYTE nRecvBuf[200] = {0,};
		WORD wRecvLen = 0;
		m_SendFrame.PutCmdDataToBuffer(nSendBuf,50);  //05 81 11 F1 81 04 01
		wRecvLen = g_Com.KwpFlashInit(nSendBuf+1,nSendBuf[0],nRecvBuf);
		if (wRecvLen > 0)
		{
			CBinary binRecv = 0;
			vector<CBinary> vecbinRecv;
			binRecv = Byte2Binary(nRecvBuf,wRecvLen);
			vecbinRecv.push_back(binRecv);
			if (vecbinRecv.size() > 0)
			{
				rf += vecbinRecv;
			}
		}

		return rf;
	}

	return rf;
}



void CCommWithEcu::WaitCommPacketMaxTime (W_UINT32 iMaxTime)
{
	//g_Com中没有最大时间参数,都在SetTime()中
}


W_INT16 CCommWithEcu::SetIoPort (	W_UINT8 iIoOutputPort,
									W_UINT8 iIoInputPort, 
									W_INT8 iOutputVoltage,
									W_UINT16 ui16IoSelectItem)
{
	if (iIoOutputPort==DB15_CAN_PIN6_14 && iIoInputPort==DB15_CAN_PIN6_14)
	{
		iIoOutputPort  = PORT_06;
		iIoInputPort = PORT_14; 
	}

	if (!g_Com.SetChannel(iIoOutputPort,iIoInputPort))
	{
		OutputDebugString("\r\n Setting Channel Failed! \r\n");
		return -1;
	}

	Sleep(200);

	return 0;
}

W_INT16 CCommWithEcu::EnableOutputIoLine (W_UINT8 iIoOutputPort, bool bIsConnect)
{
	//暂不需要此功能
	return 0;
}

W_INT16 CCommWithEcu::SetBps (W_FLOAT fBps, W_INT8 iParityBit, W_INT8 iDataBit)
{

	if (iDataBit != 8)
	{
		MessageBox(NULL,"9位的数据尚未开发","请下载最新版本",MB_OK);
	}

		//校验位
	if (!g_Com.SetParity_UART2(iParityBit)) //他吗的! 代码已经完成, 可是TAKATA的6回路SRS ECU不给力!!没测通
	{
		return -1;
	}
	



	if (g_Com.m_bProtocol == PROTOCOL_M_CAN)
	{
//定义16M的晶体下，CAN总线波特率  == 这里的定义同步MCU.
#define ByteRate_20k	0x00		//波特率20kbps
#define ByteRate_40k	0x01		//波特率40kbps
#define ByteRate_50k	0x02		//波特率50kbps
#define ByteRate_80k	0x03		//波特率80kbps
#define ByteRate_100k	0x04		//波特率100kbps
#define ByteRate_125k	0x05		//波特率125kbps
#define ByteRate_200k	0x06		//波特率200kbps
#define ByteRate_250k	0x07		//波特率250kbps
#define ByteRate_400k	0x08		//波特率400kbps
#define ByteRate_500k	0x09		//波特率500kbps
#define ByteRate_666k	0x0a		//波特率666kbps
#define ByteRate_800k	0x0b		//波特率800kbps
#define ByteRate_1000k	0x0c		//波特率1000kbps
		BYTE bBps = 0xFF;
		DWORD dwBps = (DWORD)fBps;
		switch (dwBps)
		{
		case 20000:
			bBps = ByteRate_20k;
			break;
		case 40000:
			bBps = ByteRate_40k;
			break;
		case 50000:
			bBps = ByteRate_50k;
			break;
		case 80000:
			bBps = ByteRate_80k;
			break;
		case 100000:
			bBps = ByteRate_100k;
			break;
		case 125000:
			bBps = ByteRate_125k;
			break;
		case 250000:
			bBps = ByteRate_250k;
			break;
		case 400000:
			bBps = ByteRate_400k;
			break;
		case 500000:
			bBps = ByteRate_500k;
			break;
		case 660000:
			bBps = ByteRate_666k;
			break;
		case 800000:
			bBps = ByteRate_800k;
			break;
		case 1000000:
			bBps = ByteRate_1000k;
			break;
		default:
			return -1;
		}
		if (!g_Com.SetBaudRate((DWORD)bBps))
		{
			return -1;
		}
	}
	else
	{
		if (!g_Com.SetBaudRate((DWORD)fBps))
		{
			return -1;
		}
	}

	return 0;
}
//W_INT16 CCommWithEcu::SetBRs (W_UINT8 BR1,W_UINT8 BR2,W_INT8 iParityBit, W_INT8 iDataBit)
//{
//	//好像没有什么诊断程序调用这个接口,等有用到再说
//	return 0;
//}


W_INT16 CCommWithEcu::TimeIntervalOver (	W_INT32 iMaxWaitReceivePacket,
										  W_INT32 iSendPacketMinWaitTime,
										  W_FLOAT fSendByteMinWaitTime)
{
	if (!g_Com.SetTime((DWORD)iMaxWaitReceivePacket,(DWORD)iSendPacketMinWaitTime,(DWORD)fSendByteMinWaitTime))
	{
		OutputDebugString("\r\n Setting Timer Failed! \r\n");
		return -1;
	}

	Sleep(200);
	OutputDebugString("\r\n Setting Timer OK! \r\n");

	return 0;
}


W_INT16 CCommWithEcu::KeepLink (W_INT16 iKeepTime,  CSendFrame *pKeepLinkCommand)
{
	//就目前的下位机版本,链路保持发的过快偶尔会第一次失败
	//延迟100ms也偶有失败,200ms就好多了,但还是有失败的.这里用延迟500ms以确保不失败
	Sleep(500);
	

	//目前只实现单帧发送和单帧接收(如需要多帧则改这里)
	if (iKeepTime == 0)
	{
		g_Com.SetKeepLink();
	}
	else
	{
		BYTE i;
		for (i=0; i<3; i++)
		{
			BYTE bCmd[255] = {0,};
			WORD wLen = 0;
			pKeepLinkCommand->PutCmdDataToBuffer(bCmd,wLen);
			if (!g_Com.SetKeepLink((DWORD)iKeepTime,bCmd+1,bCmd[0]))
			{
				//test
				char sz[50];
				sprintf(sz,"Test 链路设置失败!,失败次数 %d\r\n",i);
				OutputDebugString(sz);
				//test

				continue;
			}
			break;
		}
		if (i >= 3)return -1;
	}

	return 0;
}

W_INT16 CCommWithEcu::BoschKeepLink (bool bEnableKeep)
{
	Sleep(500);
	if (bEnableKeep)
	{
		BYTE i;
		for ( i=0; i<5; i++)
		{
			if (!g_Com.SetKeepLink(2*1000,(BYTE *)"\x03\xCC\x09\x03",4))
			{
				Sleep(100);
				continue;
			}
			break;
		}
		if (i >= 5)return -1;
	}
	else
	{
		g_Com.SetKeepLink();
	}
	
	
	return 0;
}

W_INT16 CCommWithEcu::VoltageHighLowTime (W_INT16 iHighLowTimes, ...)
{
//	if (!g_Com.SetVoltage((WORD)iHighLowTimes))
//	{
//		return -1;
//	}
	//好像上面这种方法不能传参数过去. 
	
	//以下拷贝来自CCom::SetVoltage(); 不能在这里改代码

	WORD wNumber = iHighLowTimes;

	BYTE nSend[200] = {0xFC,0xCF,0x55,0x06,0xC1,0xC2,};
	BYTE nLen = 0;
	BYTE nRecv[20] = {0,};
	BYTE nTemp = 0x00;

	nSend[4] = (BYTE)((wNumber*2)>>8);
	nSend[5] = (BYTE)((wNumber*2));
	nLen = 6;

	va_list ap;
	va_start(ap,iHighLowTimes);
	for (WORD w=0; w<wNumber; w++)
	{
		W_INT16 wVoltage = va_arg(ap,W_INT16);
		nSend[nLen++] = (BYTE)(wVoltage>>8);
		nSend[nLen++] = (BYTE)(wVoltage);
	}
	va_end(ap);
	nSend[nLen++] = 0xCF;
	nSend[nLen++] = 0xFC;

	if (!g_Com.SendCmd(nSend,nLen))return -1;

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
		if (!g_Com.RecvCmd(&nTemp,1))continue;
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
		if (!g_Com.RecvCmd(&nTemp,1))continue;
		nRecv[nCount++] = nTemp;
		if (nCount >= 20)
			return false; //避免收错数据导致溢出
		wFlag = wFlag<<8|nTemp;
		if (wFlag == 0xCFFC)
			break;
	}

	if (nRecv[0]!=0x80 || nRecv[1]!=0x01)return -1;
	if (nRecv[2] != 0x00)
	{
		return -1;
	}

	return 0;
}

W_INT16 CCommWithEcu::VoltageHighLowTenthTime (W_INT16 iHighLowTimes, ...)
{
	return VoltageHighLowTime(iHighLowTimes);
}


W_INT16 CCommWithEcu::SetHighSpeed(BYTE pParameter)
{
	//不知道哪里用到了,用到再增加

	return 0;
}

W_INT16 CCommWithEcu::SetBMWSpecial(BYTE pParamerer)
{
	//目前无实现. 等到做宝马的时候再考虑要不要这个接口

	return 0;
}


 //等到做地址码再改这里
CReceiveFrame CCommWithEcu::AddressCodeEnter (BYTE ucAddressCode, 									
											W_UINT16 ui16Parameter/* = ACE_BPS_AUTO_RECOGNIZE | ACE_KW2_REVERSE_TO_ECU | ACE_GET_ADDRESS_REVERSE_FROM_ECU*/,
											W_FLOAT fBps/* = 5*/,
											W_INT16 i16ReverseKw2TimeInterval/* = 30*/)
{
	//等找个ECU来测试通过后再说. 这里

	CReceiveFrame rf;
	return rf;
}


// 等到做bosch地址码再改这里
CReceiveFrame CCommWithEcu::BoschEnter (BYTE ucAddressCode,	W_INT16 i16ReverseKw2TimeInterval/* = 30*/)
{
	CReceiveFrame rf;
	BYTE bRecvBuff[1024] = {0,};

	WORD wRecvLen = g_Com.AddressCodeEnter_Bosch(bRecvBuff,ucAddressCode,
	ADDR_AUTO_BPS_RECOGNIZE|ADDR_KW2_INVERSE_TO_ECU|ADDR_RECV_MUTI_FRAME);
//	WORD wRecvLen = g_Com.AddressCodeEnter_Bosch(bRecvBuff,ucAddressCode,
//		ADDR_AUTO_BPS_RECOGNIZE|ADDR_KW2_INVERSE_TO_ECU|ADDR_RECV_ONE_FRAME,2);


	//bRecvBuff接收存放格式
	//帧数[1] 长度1[1] 数据1[n] 长度2[1] 数据2[n] ...
	//补充说明:KeyWord也是一帧数据.一般为第一帧,存放在长度1和数据1中.
	if (wRecvLen < 3)
	{
		return rf;
	}

	BYTE nFrameNum = 0;
	BYTE nLen = 0;
	WORD wIndex = 0;
	CBinary binRecv = 0;
	vector<CBinary> vecbinRecv;

	nFrameNum = bRecvBuff[wIndex++];
	for (BYTE i=0; i<nFrameNum; i++)
	{
		binRecv = 0;
		nLen = bRecvBuff[wIndex++];
		for (BYTE j=0; j<nLen; j++)
		{
			binRecv.Add(bRecvBuff[wIndex++]);
		}
		vecbinRecv.push_back(binRecv);
	}

	if (vecbinRecv.size() > 0)
	{
		rf += vecbinRecv;
	}

	return rf;
}


W_INT16 CCommWithEcu::GetStatus(W_UINT8 ui8StatusWay, BYTE ucIoInputPort /* = DB15_NO_SWITCH_CHANNEL */)
{
	//取得下位机状态, 仅用于非组合发送  =======目前不知道要做什么,暂时没有做
	//目前benz有用到.  暂不管
	return 0;
}




// 构造函数
CCommWithEcu::CFilterOrLengthRecognise::CFilterOrLengthRecognise()
{	
	Empty();
}
// 类嵌套函数 Empty
void CCommWithEcu::CFilterOrLengthRecognise::Empty()
{
	memset(m_chBuffer, 0, sizeof(m_chBuffer));
	m_iLenth = 0;
	m_iNum = 0;
}
// 类嵌套函数 SetNormalFilterMode ====================== Audi中有用到,但这里还没做!!!,没改动!!!!!!!!!!!!!还没处理
// 参数:  
// iStartPosition		位置
// iFilterSizeLength	长度
// chFilterContain		过滤
// 解析:从位置开始过滤长度个过滤
// 举例:flrFilterCondition.SetNormalFilterMode(3,1,"\xA3");
// 说明:在接收帧的位置3上过滤1个字节, 即==0xA3才接收该帧  (是等于还是位过滤?预留,目前先理解为等于)
//
void CCommWithEcu::CFilterOrLengthRecognise::SetNormalFilterMode(W_INT16 iStartPosition, 
																 W_INT16 iFilterSizeLength, 
																 char *chFilterContain)
{
	m_iLenth = 0;
	m_iNum = 1;

	m_chBuffer[m_iLenth++] = 0x01;
	m_chBuffer[m_iLenth++] = (char)(BYTE)(1+1+iFilterSizeLength);
	m_chBuffer[m_iLenth++] = iStartPosition;
	m_chBuffer[m_iLenth++] = iFilterSizeLength;
	memcpy(m_chBuffer+m_iLenth, chFilterContain, iFilterSizeLength);
	m_iLenth += iFilterSizeLength;

}

void CCommWithEcu::CFilterOrLengthRecognise::SetCanFilterMode(W_INT16 iFilterPosition, 
															  W_INT16 iFilterMask, 
															  W_INT16 iLengthPosition)
{
	//设置can过滤的(3,0x10,4), MCU已经处理,故这里什么也不需要做.
/*	
	m_iLenth = 0;
	m_iNum = 1;

	m_chBuffer[m_iLenth++] = 0x03;
	m_chBuffer[m_iLenth++] = (char)(BYTE)(1+1+2);
	m_chBuffer[m_iLenth++] = iFilterPosition;
	m_chBuffer[m_iLenth++] = 2;
	m_chBuffer[m_iLenth++] = iFilterMask;
	m_chBuffer[m_iLenth++] = iLengthPosition;
*/
}

// 类嵌套函数 AddCanFilterId
void CCommWithEcu::CFilterOrLengthRecognise::AddCanFilterId(CBinary binCanFilterId)
{
	//这里只设置2个字节ID(11位CAN),如有用到29位CAN再改.
	WORD wCanID = binCanFilterId[0]<<8|binCanFilterId[1];
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			break;
		}
		if (g_Com.SetCanFilter(wCanID))
		{
			break;
		}
		Sleep(1);
	}

/*
	switch(binCanFilterId.GetSize()){
	case 2:
	case 4:
		if (0x00==m_chBuffer[0]){
			m_chBuffer[0] = 0x02;
			m_chBuffer[1] = 0x00;
			m_iLenth = 2;

			m_iNum = 1;
		}
		if (0x02==m_chBuffer[0]) {
			m_chBuffer[m_iLenth++] = binCanFilterId.GetSize();
			binCanFilterId.ReadBuffer(m_chBuffer+m_iLenth);
			m_iLenth += binCanFilterId.GetSize();

			m_chBuffer[1] = m_iLenth-2;
		}
		break;
	default:
		break;
	}
*/
}

// 类嵌套函数 SetNormalLengthRecogniseMode
void CCommWithEcu::CFilterOrLengthRecognise::SetNormalLengthRecogniseMode(W_INT16 iBytePosition, 
																		  BYTE ucMark, 
																		  W_INT16 iAddValue)
{
//	m_gBytePosition = 0x00;
//	m_gMask = 0x00;
//	m_gAddValue = 0x00;
//
//	//把参数记录下来, 到ProtocolSet(...)中去设置normal格式的协议
//	m_gBytePosition = (BYTE)iBytePosition;
//	m_gMask = (BYTE)ucMark;
//	//m_bAddValue = (BYTE)iAddValue;
//	if (iAddValue < 0) //负偏移要转成最高位为1的格式.
//	{
//		m_gAddValue = 0x80;
//		iAddValue *= -1;
//	}
//	m_gAddValue |= (BYTE)iAddValue;

	BYTE bParam[3] = {0,};
	bParam[0] = (BYTE)iBytePosition;
	bParam[1] = (BYTE)ucMark;
	if (iAddValue < 0) //负偏移要转成最高位为1的格式.
	{
		bParam[2] = 0x80;
		iAddValue *= -1;
	}
	bParam[2] |= (BYTE)iAddValue;
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
		t1 = GetTickCount();
		if (t1 - t0 > 1*1000)
		{
			break;
		}
		if (g_Com.SetProtocol(PROTOCOL_NORMAL,bParam)) //是不是所有调用这个函数都是Normal类型协议, 待考察
		{
			break;
		}
		Sleep(1);
	}
	


/*
	m_iLenth = 0;
	m_iNum = 1;

	m_chBuffer[m_iLenth++] = (BYTE)0x81;
	m_chBuffer[m_iLenth++] = 3;
	m_chBuffer[m_iLenth++] = iBytePosition;
	m_chBuffer[m_iLenth++] = ucMark;
	m_chBuffer[m_iLenth++] = iAddValue;
*/
 }

// 类嵌套函数 SetSpecifiedLengthMode  ===== 没改动, 有用到再说
void CCommWithEcu::CFilterOrLengthRecognise::SetSpecifiedLengthMode()
{
	m_iLenth = 0;
	m_iNum = 1;

	m_chBuffer[m_iLenth++] = (BYTE)0x82;
	m_chBuffer[m_iLenth++] = 0;
}


W_INT16 CCommWithEcu::CFilterOrLengthRecognise::GetLenth()
{
	return m_iLenth;
}

W_INT16 CCommWithEcu::CFilterOrLengthRecognise::GetNum()
{	
	return m_iNum;
}

char *CCommWithEcu::CFilterOrLengthRecognise::GetBuffer()
{
	return m_chBuffer;
}
W_INT16 CCommWithEcu::SetAutoSendContinuedFrame (CFilterOrLengthRecognise flrFilterCondition, CSendFrame sfSendContinuedFrame)
{

	g_bCanFilterMode = true;

	//flrFilterCondition在类嵌套中已经处理, 这里不再处理

	//sfSendContinuedFrame
	//处理续发帧(这里只处理只有1帧的情况,如果有多帧,需要改MCU和这里=可以参照sendrencv函数)
	if (sfSendContinuedFrame.GetFrameNumber() == 0)
	{
		//常有人写变态程序传一个空进来,这里处理掉
		return 0;
	}
	else if (sfSendContinuedFrame.GetFrameNumber() != 1)
	{
		MessageBox(NULL,"出现了多帧情况","出现了多帧情况",MB_OK);
		return 0;
	}

	

	BYTE nFrameNum = sfSendContinuedFrame.GetFrameNumber();
	BYTE nBuf[0xFF] = {0,};
	BYTE nLen = 0;
	WORD wLen = sfSendContinuedFrame.PutCmdDataToBuffer(nBuf,0x1FF); //{ nLen[1] + Data[n] + RecvLen[1] } ...
	if (wLen < 3)return -1;
	if (!g_Com.SetCanContinueFrame(nBuf+1,nBuf[0]))
	{
		return -1;
	}
	return 0;
}







W_INT16 CCommWithEcu::ProtocolSet (W_INT16 iProtocolWay, CFilterOrLengthRecognise *pFilterOrLengthRecognise)
{
	g_bCanFilterMode = false;

	if (iProtocolWay == PROTOCOL_NORMAL)
	{
		//在 SetNormalLengthRecogniseMode 已经设置过了, 这里不再处理
	}
	else
	{
		if (SetProtocol(iProtocolWay) != 0)
		{
			OutputDebugString("\r\n Setting Protocol Failed! \r\n");
			return -1;
		}
	}

	Sleep(200);
	g_bProtocol = (BYTE)iProtocolWay;
	
	return 0;
}
W_INT16 CCommWithEcu::SetProtocol (W_INT16 iProtocolWay)
{
	g_bCanFilterMode = false;

	BYTE i;
	for (i=0; i<5; i++)
	{
		Sleep(1);
		if (!g_Com.SetProtocol(iProtocolWay))
		{
			//test
			char sz[50];
			sprintf(sz,"SetProtocol() 失败啦,失败次数 %d\r\n",i);
			OutputDebugString(sz);
			//test

			continue;
		}
		break;
	}
	if (i >= 5)return -1;

	g_bProtocol = (BYTE)iProtocolWay;

	return 0;
}

//暂时没有用到该函数
//W_INT16 CCommWithEcu::ControlKLine (W_INT16 iEnable)
//{
//}
W_INT16 CCommWithEcu::SetVWCanSpecial (W_INT16 iEnable)
{
	//////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	//
	// 如何实现?   功能是怎么样的?  待询问
	//

	return 0;
}
W_INT16 CCommWithEcu::SetBenzSpecial (W_INT16 iEnable)
{
	//////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	//
	// 如何实现?   功能是怎么样的?  待询问
	//
	return 0;
}
//暂时没有用到该函数
//W_INT16 CCommWithEcu::SetHoldonSpecial(W_INT16 iFilterByte)
//{
//}


CBinary CCommWithEcu::Byte2Binary(BYTE *pbBuf, WORD wLen)
{
	CBinary bin;
	for (WORD w=0; w<wLen; w++)
	{
		bin.Add(pbBuf[w]);
	}
	return bin;
}
//把	ID[2] nLen1[1] Frame1[1..8] nLen2[1] Frame2[1..8] ...
//转换成一帧帧的CAN
void CCommWithEcu::CanConversion(CBinary binCmd,vector<CBinary> &vecbinCmd)
{
	WORD wLen = binCmd.GetSize();
	if (wLen < 5)return ;
	vecbinCmd.clear();

	WORD wID  = binCmd[0]<<8|binCmd[1];
	BYTE nBuf[13] = {0,};
	for (WORD w=2; w<wLen-1; w++)
	{
		BYTE nLen = binCmd[w];
		CBinary binOneFrame;
		binOneFrame.Add(nLen);
		binOneFrame.Add((BYTE)(wID>>8));
		binOneFrame.Add((BYTE)(wID));

		CBinary binTemp;
		binTemp.WriteBuffer(binCmd.GetBuffer()+w+1,nLen);
		binOneFrame += binTemp;

		vecbinCmd.push_back(binOneFrame);
		w += nLen;
	}
}
CReceiveFrame CCommWithEcu::SendReceive_CanBus (CSendFrame& SendFrame, bool bRepeat)
{
	CReceiveFrame rf;

	//////////////////////////////////////////////////////////////////////////
	// 处理不用硬件过滤,而自己去处理多帧的情况(不排除上位机处理慢导致丢帧情况) (建议最好用硬件过滤)
	// Begin:
	//
	if (!g_bCanFilterMode)
	{
		BYTE nFrameNum = SendFrame.GetFrameNumber();
		BYTE nBuf[0x1FF] = {0,};
		BYTE nLen = 0;
		WORD wLen = SendFrame.PutCmdDataToBuffer(nBuf,0x1FF); //{ nLen[1] + Data[n] + RecvLen[1] } ...
		if (wLen < 3)return rf;
		if (nFrameNum != 1)//TMD不好处理,这里只处理单帧方式,如过出现组包,让诊断程序改成硬件过滤方式好了!!
		{
			MessageBox(NULL,"目前不支持多帧打包又不硬件过滤","建议用硬件过滤方式处理",MB_OK);
			return rf;
		}

		BYTE nSendLen = 0;
		BYTE nSendBuf[13] = {0,};
		BYTE nRecvFrameNum = 0;
		WORD wSendBuffIndex = 0;

		nSendLen = nBuf[wSendBuffIndex++];
		memcpy(nSendBuf,nBuf+wSendBuffIndex,nSendLen);
		wSendBuffIndex += nSendLen;
		nRecvFrameNum = nBuf[wSendBuffIndex++];

		//
		vector<CBinary> vecbinRecv;
		if (nRecvFrameNum == 0xFF)
		{
			BYTE nRecvBuf[0xFF] = {0,};
			WORD nRecvLen = 0;
			nRecvLen = g_Com.SendRecvCmd(&nBuf[1],nSendLen,nRecvBuf);
			while (1)
			{
				if (nRecvLen <= 0)
				{
					break;
				}
				CBinary binRecv = Byte2Binary(nRecvBuf,nRecvLen);
				vecbinRecv.push_back(binRecv);
				nRecvLen = g_Com.RecvCmdOnly(nRecvBuf);
			}
		}
//		else if (nRecvFrameNum == 0xFE)
//		{
//		}
		else
		{
			BYTE nRecvBuf[0xFF] = {0,};
			WORD nRecvLen = 0;
			nRecvLen = g_Com.SendRecvCmd(&nBuf[1],nSendLen,nRecvBuf);
			if (nRecvLen > 0)
			{
				CBinary binRecv = Byte2Binary(nRecvBuf,nRecvLen);
				vecbinRecv.push_back(binRecv);
			}
		}

		if (vecbinRecv.size() > 0)
			rf += vecbinRecv;
		return rf;
	}
	//
	// End.
	//////////////////////////////////////////////////////////////////////////
	
	

	BYTE nFrameNum = SendFrame.GetFrameNumber();
	BYTE nBuf[0x1FF] = {0,};
	BYTE nLen = 0;
	WORD wLen = SendFrame.PutCmdDataToBuffer(nBuf,0x1FF); //{ nLen[1] + Data[n] + RecvLen[1] } ...
	if (wLen < 3)return rf;

	WORD wSendBuffIndex = 0;	//指向nBuf的下标索引
	WORD wSendBuffIndex2 = 0;	//指向pbCanSend的下标索引
	BYTE *pbCanSend = new BYTE [wLen-nFrameNum*2 + 10];		//组发送帧放到这里,格式见MCU接收格式
	pbCanSend[0] = 0xCC;
	pbCanSend[1] = 0xCC;
	wSendBuffIndex2 = 2;
	for (BYTE i=0; i<nFrameNum; i++)
	{
		BYTE nSendLen = 0;
		BYTE nSendBuf[13] = {0,};
		BYTE nRecvFrameNum = 0;

		if (wSendBuffIndex >= wLen)
		{
			break;
		}

		nSendLen = nBuf[wSendBuffIndex++];
		memcpy(nSendBuf,nBuf+wSendBuffIndex,nSendLen);
		wSendBuffIndex += nSendLen;
		nRecvFrameNum = nBuf[wSendBuffIndex++];

		//单帧传入: /*00 0B */08 07 E1 02 10 81 00 00 00 00 00
		//多帧传入: /*00 1D */08 07 E1 10 13 61 85 32 33 33 33 08 21 33 33 33 33 33 33 34 08 22 34 34 34 34 34 34 00

		if (i == 0) //第一帧的组包与其它帧不同,第一帧带ID,且其长度不包含ID
		{
			pbCanSend[wSendBuffIndex2++] = nSendBuf[0];//nSendLen;
			pbCanSend[wSendBuffIndex2++] = nSendBuf[1];
			pbCanSend[wSendBuffIndex2++] = nSendBuf[2];
			memcpy(pbCanSend+wSendBuffIndex2, nSendBuf+3, nSendBuf[0]);
			wSendBuffIndex2 += nSendBuf[0];
		}
		else
		{
			pbCanSend[wSendBuffIndex2++] = nSendBuf[0];
			memcpy(pbCanSend+wSendBuffIndex2, nSendBuf+3, nSendBuf[0]);
			wSendBuffIndex2 += nSendBuf[0];
		}

	}
	wSendBuffIndex2 -= 2; //减去它自己本身占有的长度
	pbCanSend[0] = (BYTE)(wSendBuffIndex2>>8);
	pbCanSend[1] = (BYTE)(wSendBuffIndex2);

	BYTE nRecvBuf[0x1FF] = {0,};
	WORD nRecvLen = g_Com.SendRecvCmd(&pbCanSend[2],wSendBuffIndex2,nRecvBuf);
	if (nRecvLen <= 0)
	{
		delete [] pbCanSend;
		return rf;
	}
	delete [] pbCanSend;

	CBinary binRecv = Byte2Binary(nRecvBuf,nRecvLen);
	vector<CBinary> vecbinRecv;

	if (nFrameNum > 1) //如果是发多帧(打包型),则有ECU回复的30续发帧在下位机已经处理,这里随便加一条回去
	{
		vecbinRecv.push_back(CBinary((char*)"\x08\x07\xE1\x30\x00\x00\x00\x00\x00\x00\x00",11));
		rf += vecbinRecv;
		vecbinRecv.clear();
	}

	CanConversion(binRecv,vecbinRecv);
	if (vecbinRecv.size() > 0)
	{
		rf += vecbinRecv;
	}

	return rf;
}
CReceiveFrame CCommWithEcu::SendReceiveEx (CSendFrame& SendFrame, bool bRepeat)
{
	//CAN单独处理 (因为Can都在MCU去处理了,也不需要FF,FE,1帧处理,这里单独出来处理)
	if (g_Com.m_bProtocol == PROTOCOL_CANBUS)
	{
		return SendReceive_CanBus(SendFrame,bRepeat);
	}

	CReceiveFrame rf;

	if (m_bBeginIsRunning) //如果Begin()已经启动. -这里把命令保存起来,不发,等End()那边发出去
	{
		m_SendFrame = SendFrame;
		return rf; //这里的返回是没有用处的.仅仅函数返回而已.
	}



	BYTE nFrameNum = SendFrame.GetFrameNumber();
	BYTE nBuf[0x1FF] = {0,};
	BYTE nLen = 0;
	WORD wLen = SendFrame.PutCmdDataToBuffer(nBuf,0x1FF); //{ nLen[1] + Data[n] + RecvLen[1] } ...
	if (wLen < 3)return rf;

	CBinary binRecv = 0;			//每一帧接收都放在这里 (每一帧放到组里,每一组再放到CReceiveFrame,再返回)
	vector<CBinary> vecbinRecv;		//每一组接收的帧都放在这里
	WORD wSendBuffIndex = 0;
	for (BYTE i=0; i<nFrameNum; i++)
	{
		BYTE nSendLen = 0;
		BYTE nSendBuf[255] = {0,};
		BYTE nRecvFrameNum = 0;

		if (wSendBuffIndex >= wLen)
		{
			break;
		}

		nSendLen = nBuf[wSendBuffIndex++];
		memcpy(nSendBuf,nBuf+wSendBuffIndex,nSendLen);
		wSendBuffIndex += nSendLen;
		nRecvFrameNum = nBuf[wSendBuffIndex++];

		binRecv = 0;
		vecbinRecv.clear();
		rf.clear();

		BYTE nRecvBuf[255] = {0,};
		BYTE nRecvLen = 0;
		if (nRecvFrameNum == 0xFF)			//自动接收(收到时间结束)
		{
			nRecvLen = g_Com.SendRecvCmd(nSendBuf,nSendLen,nRecvBuf);
			if (nRecvLen <= 0)
			{
				return rf;
			}
			binRecv = Byte2Binary(nRecvBuf,nRecvLen);
			if (g_Com.m_bProtocol == PROTOCOL_CANBUS)CanConversion(binRecv,vecbinRecv);
			else vecbinRecv.push_back(binRecv);
			rf += vecbinRecv;
			while (1)
			{
				binRecv = 0;
				nRecvLen = g_Com.RecvCmdOnly(nRecvBuf);
				if (nRecvLen <= 0)
				{
					return rf;
				}
				binRecv = Byte2Binary(nRecvBuf,nRecvLen);
				if (g_Com.m_bProtocol == PROTOCOL_CANBUS)CanConversion(binRecv,vecbinRecv);
				else vecbinRecv.push_back(binRecv);

				if (vecbinRecv.size() > 0)
				{
					rf += vecbinRecv;
				}
			}
		}
		else if (nRecvFrameNum == 0xFE)		//多帧,先收一帧,剩下的帧数由CCommWithEcu::ReceiveOnly()来接收
		{
			nRecvLen = g_Com.SendRecvCmd(nSendBuf,nSendLen,nRecvBuf);
			if (nRecvLen <= 0)
			{
				return rf;
			}
			binRecv = Byte2Binary(nRecvBuf,nRecvLen);
			if (g_Com.m_bProtocol == PROTOCOL_CANBUS)CanConversion(binRecv,vecbinRecv);
			else vecbinRecv.push_back(binRecv);
		}
		else if (nRecvFrameNum == 0)		//只发不收
		{
			g_Com.SendCmdOnly(nSendBuf,nSendLen);
		}
		else								//指定收nRecvFrameNum帧
		{
			nRecvLen = g_Com.SendRecvCmd(nSendBuf,nSendLen,nRecvBuf);
			if (nRecvLen <= 0)
			{
				return rf;
			}
			binRecv = Byte2Binary(nRecvBuf,nRecvLen);
			if (g_Com.m_bProtocol == PROTOCOL_CANBUS)CanConversion(binRecv,vecbinRecv);
			else vecbinRecv.push_back(binRecv);
			for (BYTE j=0; j<nRecvFrameNum-1; j++)
			{
				binRecv = 0;
				nRecvLen = g_Com.RecvCmdOnly(nRecvBuf);
				if (nRecvLen <= 0)
				{
					return rf;
				}
				binRecv = Byte2Binary(nRecvBuf,nRecvLen);
				if (g_Com.m_bProtocol == PROTOCOL_CANBUS)CanConversion(binRecv,vecbinRecv);
				else vecbinRecv.push_back(binRecv);
			}
		}

		if (vecbinRecv.size() > 0)
		{
			rf += vecbinRecv;
		}

	}

	m_RecvFrame = rf; //把收到的包保存起来(1.目前End()函数的返回值)

	return rf;
}
CReceiveFrame CCommWithEcu::SendReceive (CSendFrame& SendFrame, bool bRepeat)
{
	CReceiveFrame rf;
	for (BYTE i=0; i<3; i++)
	{
		rf = SendReceiveEx(SendFrame,bRepeat);
		if (rf.size() > 0)
		{
			break;
		}
	}
	return rf;
}

CReceiveFrame CCommWithEcu::CanSendReceive (CSendFrame& SendFrame, bool bRepeat)
{
	return SendReceive_CanBus(SendFrame,bRepeat);
}

//康明斯柴油车的发送接收函数,目前没有用到
//CReceiveFrame CCommWithEcu::CumSendReceive (CSendFrame& SendFrame, bool bRepeat)
//{
//}


CReceiveFrame CCommWithEcu::ReceiveOnly (bool bClearBuffer)
{
	CReceiveFrame rf;

	BYTE nRecvBuf[255] = {0,};
	WORD wRecvLen = 0;
	wRecvLen = g_Com.RecvCmdOnly(nRecvBuf);
	if (wRecvLen <= 0)
	{
		return rf;
	}

	CBinary binRecv = Byte2Binary(nRecvBuf,wRecvLen);
	vector<CBinary> vecbinRecv;
	vecbinRecv.push_back(binRecv);
	rf += vecbinRecv;

	return rf;
}







//不实现其功能
void CCommWithEcu::SetDemoFrame(CBinaryGroup bgRecvData)
{
}
CRunLog *CCommWithEcu::GetRunLog(void)
{
	CRunLog *runlog = NULL;
	return runlog;
}
void CCommWithEcu::CommBlockStart()
{
}
void CCommWithEcu::CommBlockEnd()
{
}
