// J1939.cpp: implementation of the CJ1939 class.
//
//////////////////////////////////////////////////////////////////////

//#include "StdAfx.h"
#include "J1939.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

CJ1939 g_J1939;
extern CCom g_Com;

CJ1939::CJ1939()
{
	m_nEcuAddr  = 0xFF; //init to be a global address
	m_nToolAddr = 0xFE; //init to be a off board diagnostic-service tool 1
	m_bBroadcastRecvEnable = true;
	m_dwTickCount = GetTickCount();
}

CJ1939::CJ1939(BYTE nEcuAddr,BYTE nToolAddr)
{
	m_nEcuAddr  = nEcuAddr;
	m_nToolAddr = nToolAddr;
	m_bBroadcastRecvEnable = true;
	m_dwTickCount = GetTickCount();
}

CJ1939::~CJ1939()
{

}

void CJ1939::OutputDebugStringEx(CBinary binCmd)
{
	char sz[2048];
	int i=0;
	for ( i=0; i<binCmd.GetSize(); i++)
	{
		sprintf(sz+3*i,"%02X ",binCmd[i]);
	}
	OutputDebugString(sz);
	OutputDebugString("\r\n");
}
bool CJ1939::SendCmd(CBinary binSend)
{
	BYTE i = 0;
	BYTE nSend[18] = {0xA5,0xA5,0x00,0x0D,0x30,0x98,0xEF,0x00,0xF9,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x55};
	nSend[5] = binSend[0];
	nSend[6] = binSend[1];
	nSend[7] = binSend[2];
	nSend[8] = binSend[3];
	for (i=0; i<8; i++)nSend[9+i] = binSend[i+4];
	if (!g_Com.SendCmd(nSend,18))
	{
		return false;
	}
	return true;
}
CBinary CJ1939::RecvOneFrame()
{
	CBinary binRecv;
	BYTE nBuf[200] = {0,};
	if (!g_Com.RecvCmdEx(nBuf,2))
		return binRecv;
	if (nBuf[0] != 0xA5)
		return binRecv;
	if (nBuf[1] != 0xA5)
		return binRecv;
	if (!g_Com.RecvCmdEx(nBuf,2))
		return binRecv;
	WORD wLen = (WORD)nBuf[0]<<8|nBuf[1];
	if (wLen > 100)return binRecv;
	if (!g_Com.RecvCmdEx(nBuf,wLen+1))
		return binRecv;
	if (nBuf[0] != 0x30)
	{
		if (nBuf[0] == 0x10)
		{
			if (nBuf[wLen] != 0x55)
				return binRecv;
			binRecv.WriteBuffer(nBuf,1);
			return binRecv;
		}
	}
	if (nBuf[wLen] != 0x55)
		return binRecv;
	binRecv.WriteBuffer(nBuf+1,wLen-1);
	return binRecv;
}
CBinary CJ1939::RequestsPGN(DWORD dwPGN)
{
	if (GetTickCount() - m_dwTickCount < 400)
	{
		adsSleep(400);
	}

	CBinary binSend = 0;
	CBinary binRecv = 0;
	if (m_bBroadcastRecvEnable)
	{
		binRecv = RequestsPGNEx(dwPGN);
	}
	if (binRecv.GetSize() == 0)
	{
		BYTE nCmd[13] = {0xA5,0xA5,0x00,0x08,0x30,0xC1,0xC2,0xC3,0xC4,0xD1,0xD2,0xD3,0x55};
		nCmd[5] = 0x98;
		nCmd[6] = 0xEA;
		nCmd[7] = m_nEcuAddr;
		nCmd[8] = m_nToolAddr;
		nCmd[9]  = (BYTE)(dwPGN>>0x00);
		nCmd[10] = (BYTE)(dwPGN>>0x08);
		nCmd[11] = (BYTE)(dwPGN>>0x10);
		if (!g_Com.SendCmd(nCmd,13))return 0;
		binSend.WriteBuffer(nCmd+5,7);
		OutputDebugStringEx(binSend);
		binRecv = RequestsPGNEx(dwPGN);
	}
	m_dwTickCount = GetTickCount();
	return binRecv;
}
CBinary CJ1939::RequestsPGNEx(DWORD dwPGN)
{
	DWORD t = 0;
	CBinary binSend = 0;
	CBinary binRecv = 0;
	WORD wNum = 0;
	BYTE bFrm = 0;
	BYTE nCount = 0x01;
	CBinaryGroup bgRecv;
	
	t = GetTickCount();
	while (1)
	{
		if (GetTickCount() - t > 2*1000)
			return 0;
		binRecv = RecvOneFrame();
		if (binRecv.GetSize() != 12)
			continue;
		if (binRecv[1] == 0xE8)
		{
			OutputDebugStringEx(binRecv);
			return binRecv;
		}
		else if (binRecv[1] == 0xEC)
		{
			if (((DWORD)(binRecv[11]&0x01)<<16|binRecv[10]<<8|binRecv[9]) == dwPGN)
			{
				if (binRecv[4] == 0x20)
				{
					OutputDebugStringEx(binRecv);
					bgRecv.push_back(binRecv);
					wNum = (WORD)binRecv[6]<<8|binRecv[5];
					bFrm = binRecv[7];
					t = GetTickCount();
					while (1)
					{
						if (GetTickCount() - t > 2*1000)
							return 0;
						binRecv = RecvOneFrame();
						if (binRecv.GetSize() != 12)
							continue;
						if (binRecv[1] != 0xEB)
							continue;
						if (binRecv[4] == nCount)
						{
							OutputDebugStringEx(binRecv);
							bgRecv.push_back(binRecv);
							nCount++;
						}
						if (binRecv[4] == bFrm)
						{
							OutputDebugString("\r\n");
							binRecv = BinaryGroup2Binary(bgRecv);
							return binRecv;
						}
					}
				}
				else if (binRecv[4] == 0x10)
				{
					adsSleep(100);
					OutputDebugStringEx(binRecv);
					bgRecv.push_back(binRecv);
					wNum = (WORD)binRecv[6]<<8|binRecv[5];
					bFrm = binRecv[7];
					binSend.WriteBuffer("\x9C\xEC\x00\xF9\x11\xCC\x01\xFF\xFF\xDD\xDD\xDD",12);
					binSend.SetAt(2,m_nEcuAddr);
					binSend.SetAt(3,m_nToolAddr);
					binSend.SetAt(5,bFrm);
					binSend.SetAt(9 ,(BYTE)(dwPGN>>0x00));
					binSend.SetAt(10,(BYTE)(dwPGN>>0x08));
					binSend.SetAt(11,(BYTE)(dwPGN>>0x10));
					g_J1939.SendCmd(binSend);
					OutputDebugStringEx(binSend);
					t = GetTickCount();
					while (1)
					{
						if (GetTickCount() - t > 2*1000)
							return 0;
						binRecv = RecvOneFrame();
						if (binRecv.GetSize() != 12)
							continue;
						if (binRecv[1] != 0xEB)
							continue;
						if (binRecv[4] == nCount)
						{
							OutputDebugStringEx(binRecv);
							bgRecv.push_back(binRecv);
							nCount++;
						}
						if (binRecv[4] == bFrm)
						{
							binSend.WriteBuffer("\x9C\xEC\x00\xF9\x13\xDD\xDD\xCC\xFF\xDD\xDD\xDD",12);
							binSend.SetAt(2,m_nEcuAddr);
							binSend.SetAt(3,m_nToolAddr);
							binSend.SetAt(5,(BYTE)(wNum>>0x00));
							binSend.SetAt(6,(BYTE)(wNum>>0x08));
							binSend.SetAt(7,bFrm);
							binSend.SetAt(9 ,(BYTE)(dwPGN>>0x00));
							binSend.SetAt(10,(BYTE)(dwPGN>>0x08));
							binSend.SetAt(11,(BYTE)(dwPGN>>0x10));
							g_J1939.SendCmd(binSend);
							OutputDebugStringEx(binSend);
							OutputDebugString("\r\n");
							binRecv = BinaryGroup2Binary(bgRecv);
							return binRecv;
						}
					}
				}
			}
		}
		else if (((DWORD)(binRecv[0]&0x01)<<16|binRecv[1]<<8|binRecv[2]) == dwPGN)
		{
			OutputDebugStringEx(binRecv);
			OutputDebugString("\r\n");
			return binRecv;
		}
	}
	return binRecv;
}
CBinary CJ1939::BinaryGroup2Binary(CBinaryGroup bgRecv)
{
	int i = 0;
	CBinary binRecv = 0;
	CBinary binTemp = 0;
	WORD wNum = 0;
	BYTE nFrm = 0;
	if (bgRecv.size() < 2)return 0;
	for (i=0; i<(int)bgRecv.size(); i++){if (bgRecv[i].GetSize()!=12)return 0;}
	if (bgRecv[0][4]==0x20 || bgRecv[0][4]==0x10)
	{
		wNum = (WORD)bgRecv[0][6]<<8|bgRecv[0][5];
		nFrm = (BYTE)bgRecv[0][7];
		for (i=1; i<(int)bgRecv.size(); i++)
		{
			binTemp = bgRecv[i];
			if (binTemp[4] != (BYTE)i)return 0;
			if (i == (int)bgRecv.size()-1)
			{
				binRecv.Append((BYTE *)binTemp.GetBuffer()+5,(wNum%7)?(wNum%7):7);
			}
			else
			{
				binRecv.Append((BYTE *)binTemp.GetBuffer()+5,7);
			}
		}
	}
	else return 0;
	return binRecv;
}


void CJ1939::SetAddr(BYTE nEcuAddr,BYTE nToolAddr)
{
	m_nEcuAddr  = nEcuAddr;
	m_nToolAddr = nToolAddr;
}
void CJ1939::SetBroadcastRecvEnable(bool bEnable)
{
	m_bBroadcastRecvEnable = bEnable;
}

BYTE CJ1939::Connect(DWORD dwBaud,BYTE nPin)
{
	int i = 0;
	CBinary binRecv = 0;
	BYTE nCmd[52] = {0xA5,0xA5,0x00,0x2F,0x10,0x01,0x03,0xD0,0x90,0x02,0x81,0x03,0x10,0x04,0x00,0x00,0x00,0x00,0x05,0x00,0x00,0x00,0x3D,0x06,0xFF,0xFF,0xFF,0x00,0x07,0x00,0x00,0x00,0x00,0x08,0x00,0x00,0x00,0x00,0x09,0x00,0x00,0x0A,0x00,0x00,0x0B,0x00,0x00,0x0C,0xE6,0x0F,0x12,0x55};
	nCmd[6] = (BYTE)(dwBaud>>0x10);
	nCmd[7] = (BYTE)(dwBaud>>0x08);
	nCmd[8] = (BYTE)(dwBaud>>0x00);
	nCmd[22] = m_nEcuAddr;
	nCmd[48] = nPin;
	for (i=0; i<2; i++)
	{
		if (!g_Com.SendCmd(nCmd,52)){adsSleep(200);continue;}
		binRecv = RecvOneFrame();
		if (binRecv.GetSize()<=0 || binRecv[0]!=0x10){adsSleep(200);continue;}
		break;
	}
	if (i >= 2)
	{
		return 1; //connect VCI failed
	}
	adsSleep(100);

	binRecv = RecvOneFrame();
	if (binRecv.GetSize()!=12)
	{
		return 2; //connect ECU failed
	}

	return 0;
}
//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////


CBinaryGroup CJ1939::GetBroadcastCanId(DWORD dwBaud,BYTE bCanChannel)
{
	BYTE nCmd[52] =
			{
					0xA5,0xA5,0x00,0x2F,0x10,
					0x01,0x03,0xD0,0x90,
					0x02,0x80,
					0x03,0x11,
					0x04,0x00,0x00,0x00,0x00,
					0x05,0x00,0x00,0x00,0x00,
					0x06,0xFF,0xFF,0xFF,0xFF,
					0x07,0x00,0x00,0x00,0x00,
					0x08,0xFF,0xFF,0xFF,0xFF,
					0x09,0x00,0x00,
					0x0A,0x00,0x00,
					0x0B,0x00,0x00,
					0x0C,0xE6,
					0x0F,0x12,
					0x55
			};
	CBinaryGroup bgCanId;
	BYTE nRecvBuf[1000];
	WORD nRecvLen;
	//先屏蔽CAN以避免受到广播帧干扰
	nCmd[24] = 0x00;
	nCmd[25] = 0x00;
	nCmd[26] = 0x00;
	nCmd[27] = 0x00;
	nCmd[34] = 0x00;
	nCmd[35] = 0x00;
	nCmd[36] = 0x00;
	nCmd[37] = 0x00;
	g_Com.SendCmd(nCmd,52);
	nRecvLen = g_Com.RecvCmdTime(nRecvBuf,500);
	adsSleep(200);

	//再打开CAN
	nCmd[24] = 0xFF;
	nCmd[25] = 0xFF;
	nCmd[26] = 0xFF;
	nCmd[27] = 0xFF;
	nCmd[34] = 0xFF;
	nCmd[35] = 0xFF;
	nCmd[36] = 0xFF;
	nCmd[37] = 0xFF;
	nCmd[6] = (BYTE)(dwBaud>>0x10);
	nCmd[7] = (BYTE)(dwBaud>>0x08);
	nCmd[8] = (BYTE)(dwBaud>>0x00);
	nCmd[48] = bCanChannel;

	g_Com.SendCmd(nCmd,52);
//	nRecvLen = g_Com.RecvCmdTime(nRecvBuf,1000); //这种方式会接收不全,该用下面的方式
//	if (nRecvLen < 5)
//	{
//		adsSleep(1000);
//		nRecvLen = g_Com.RecvCmdTime(nRecvBuf,1000);
//		if (nRecvLen < 5)return bgCanId;
//	}
	BYTE nByte = 0;
	WORD wLen = 0;
	DWORD t = GetTickCount();
	while (1)
	{
		adsSleep(1);
		if (GetTickCount() - t > 2*1000)break;
//		if (g_Com.RecvCmd(&nByte,1))
		if (g_Com.RecvCmdTime(&nByte,1))
		{
			nRecvBuf[wLen++] = nByte;
		}
	}
	nRecvLen = wLen;
	if (nRecvLen < 5)return bgCanId;

	BYTE nOneFrameLen = 0;
	DWORD dwCount = 0;
	CBinary binTemp;
	int i = 0;
	while (1)
	{
		if (nRecvBuf[dwCount++] != 0xA5)return bgCanId;
		if (nRecvBuf[dwCount++] != 0xA5)return bgCanId;
		nOneFrameLen = nRecvBuf[dwCount]<<8|nRecvBuf[dwCount+1];
		if (nOneFrameLen != 5) //不是CANID(这里有MCU对设置命令的回复,如:A5 A5 00 01 10 55)
		{
			dwCount+=(2+nOneFrameLen+1);
			continue;
		}
		dwCount+=2;
		if (nRecvBuf[dwCount++] != 0x30)return bgCanId;
		binTemp = 0;
		for (i=0; i<nOneFrameLen-1; i++)
		{
			binTemp.Add(nRecvBuf[dwCount++]);
		}
		if (nRecvBuf[dwCount++] != 0x55)return bgCanId;
		if (binTemp.GetSize() == 4) //CAN ID
		{
			bgCanId.push_back(binTemp);
		}
		if (dwCount >= nRecvLen)
		{
			break;
		}
	}
	for (i=0; i<bgCanId.size(); i++)OutputDebugStringEx(bgCanId[i]);//
	return bgCanId;
}


CBinaryGroup CJ1939::GetClaimedAddressAndName(DWORD dwBaud, BYTE bChannel ,BYTE bAddr)
{
	BYTE nCmd[52] =
			{
					0xA5,0xA5,0x00,0x2F,0x10,
					0x01,0x03,0xD0,0x90,
					0x02,0x81,
					0x03,0x11,
					0x04,0x00,0x00,0x00,0x00,
					0x05,0x00,0xEE,0x00,0x00,
					0x06,0xFF,0x00,0xFF,0xFF,
					0x07,0x00,0xE8,0x00,0x00,
					0x08,0xFF,0x00,0xFF,0xFF,
					0x09,0x00,0x00,
					0x0A,0x00,0x00,
					0x0B,0x00,0x00,
					0x0C,0xE6,
					0x0F,0x12,
					0x55
			};
	nCmd[6] = (BYTE)(dwBaud>>0x10);
	nCmd[7] = (BYTE)(dwBaud>>0x08);
	nCmd[8] = (BYTE)(dwBaud>>0x00);
	nCmd[48] = bChannel;

	CBinaryGroup bgClaimedAddressAndName;
	BYTE nRecvBuf[1000];
	WORD nRecvLen;
	if (!g_Com.SendRecvCmd2Mcu(nCmd,52,nRecvBuf,nRecvLen) ||
		(nRecvLen != 1) ||
		(nRecvBuf[0] != 0x10)
			)
	{
		return bgClaimedAddressAndName;
	}

	CBinary binTemp;
	BYTE nSendLen = 13;
	BYTE nSendBuf[13] = {0xA5,0xA5,0x00,0x08,0x30,0x98,0xEA,0x00,0xFF,0x00,0xEE,0x00,0x55};
	nSendBuf[7] = bAddr;
	binTemp.WriteBuffer(nSendBuf+5,7);
	OutputDebugStringEx(binTemp);
	g_Com.SendCmd(nSendBuf,nSendLen);
	nRecvLen = g_Com.RecvCmdTime(nRecvBuf,1000);

	BYTE nByte = 0;
	WORD wLen = 0;
	DWORD t = GetTickCount();
	while (1)
	{
		adsSleep(1);
		if (GetTickCount() - t > 2*1000)break;
		if (wLen > 200)break;
		if (g_Com.RecvCmdTime(&nByte,1))
		{
			nRecvBuf[wLen++] = nByte;
		}
	}
	nRecvLen = wLen;
	if (nRecvLen < 5)return bgClaimedAddressAndName;
	BYTE nOneFrameLen = 0;
	DWORD dwCount = 0;
	int i=0,j=0;
	while (1)
	{
		if (nRecvBuf[dwCount++] != 0xA5)return bgClaimedAddressAndName;
		if (nRecvBuf[dwCount++] != 0xA5)return bgClaimedAddressAndName;
		nOneFrameLen = nRecvBuf[dwCount]<<8|nRecvBuf[dwCount+1];
		if (nOneFrameLen != 0x000D)
		{
			dwCount+=(2+nOneFrameLen+1);
			continue;
		}
		dwCount+=2;
		if (nRecvBuf[dwCount++] != 0x30)return bgClaimedAddressAndName;
		binTemp = 0;
		for (i=0; i<nOneFrameLen-1; i++)
		{
			binTemp.Add(nRecvBuf[dwCount++]);
		}
		if (nRecvBuf[dwCount++] != 0x55)return bgClaimedAddressAndName;
		if (binTemp.GetSize() == 0x0C) //canid(included address) & Name
		{
			bgClaimedAddressAndName.push_back(binTemp);
		}
		if (dwCount >= nRecvLen)
		{
			break;
		}
	}
	if (bgClaimedAddressAndName.size() <= 0)
	{
		return bgClaimedAddressAndName;
	}

	//去掉重复(声明总线冲突时候经常会有重声明)
	CBinaryGroup bgTemp;
	bgTemp.push_back(bgClaimedAddressAndName[0]);
	for (i=0; i<bgClaimedAddressAndName.size(); i++)
	{
		for (j=0; j<bgTemp.size(); j++)
		{
			if (bgTemp[j] == bgClaimedAddressAndName[i])
			{
				break;
			}
		}
		if (j >= bgTemp.size())
		{
			bgTemp.push_back(bgClaimedAddressAndName[i]);
			OutputDebugStringEx(binTemp);
		}
	}
	bgClaimedAddressAndName = bgTemp;
	for (i=0; i<bgTemp.size(); i++)
	{
		OutputDebugStringEx(bgTemp[i]);
	}

	return bgClaimedAddressAndName;
}
