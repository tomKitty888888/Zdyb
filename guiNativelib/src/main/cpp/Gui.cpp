// Gui.cpp: implementation of the CGui class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Gui.h"
#include "adsStd.h"
#include "Display.h"
#include "Debug.h"
#include "Com.h"
#include "unistd.h"

#include "MyHttp.h"
#include "Base64.h"
#include "CJsonObject.h"
#include <android/log.h>
//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

string g_strPath="";
//CDatabase g_DatabaseAcces;

BYTE g_bMyFlag = 0; //加载数据流项目时(CdsSelectInit)赋值为1;  退出实时数据流时(CdsShow)赋值为2
volatile unsigned  char guiBuf[0xffff]={0,};
volatile unsigned char  comBuf[1024]={0,};

extern DWORD g_dwProduct;

extern CCom g_Com;

CGui::CGui()
{
	m_MapGuiHandle = NULL;
	m_pMapGuiBuff  = NULL;

	m_pfData = NULL;
	m_dwCurrentAddr = 0x00000000;


	//g_strPath="";

	memset(m_szInputText,0,255);
	memset(m_szFileDialog,0,255);
	m_pVdiData = NULL;
	m_dwVdiIndex = 0;
}

CGui::~CGui()
{
    if (m_pVdiData != NULL)
    {
        delete [] m_pVdiData;
    }

}

bool CGui::GuiOpen()
{
	if (!DataFileOpen())
	{
		OutputDebugString("Data File open faild!\r\n");
		return false;
	}
	if (!ShareBufferCreate())
	{
		OutputDebugString("ShareBufferCreate faild!\r\n");
		return false;
	}
	if (!GetMainFormHandle())return false;


	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_GUI_OPEN;
	m_pMapGuiBuff[3] = '\0';
	if (!SendMessage2MainForm_SendMessage())
		return false;

	return true;
}

bool CGui::GuiOpenNoDataVdi()
{
	if (!GetMainFormHandle())
	{
		OutputDebugString("Display has been lost!\r\n");
		return false;
	}

	if (!ShareBufferCreate())
	{
		OutputDebugString("main form has been lost!\r\n");
		return false;
	}

	return true;
}

bool CGui::GuiClose()
{

	//诊断程序被杀死了, 不会执行到这里来.   直接被杀死了,串口也关掉了.
	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_GUI_CLOSE;
	m_pMapGuiBuff[3] = '\0';
	if (!SendMessage2MainForm_SendMessage())
		return false;

	if (!DataFileClose())return false;
	if (!ShareBufferDestroy())return false;

	return true;
}

bool CGui::ShareBufferCreate(void)
{
	m_pMapGuiBuff=(char*)guiBuf;
	m_pMapComBuff=(char*)comBuf;
//	//1.创建GUI共享内存
//	m_MapGuiHandle = CreateFileMapping((HANDLE)0xFFFFFFFF,NULL,PAGE_READWRITE,0,0xFFFF,"EPA2015GuiShareFile");
//	if (m_MapGuiHandle==NULL || GetLastError()!=ERROR_ALREADY_EXISTS) //()
//	{
//		CloseHandle(m_MapGuiHandle);
//		m_MapGuiHandle = NULL;
//		return false;
//	}
//	//2.指针指向该共享内存
//	m_pMapGuiBuff = (char *)MapViewOfFile(m_MapGuiHandle,FILE_MAP_READ|FILE_MAP_WRITE,0,0,0);
//	if (m_pMapGuiBuff == NULL)
//	{
//		CloseHandle(m_MapGuiHandle);
//		m_MapGuiHandle = NULL;
//		return false;
//	}
//
//	//1.创建COM共享内存
//	m_MapComHandle = CreateFileMapping((HANDLE)0xFFFFFFFF,NULL,PAGE_READWRITE,0,1024,"EPA2015ComShareFile");
//	if (m_MapComHandle==NULL || GetLastError()!=ERROR_ALREADY_EXISTS) //创建共享内存成功，且原先经存在(显示程序创建)
//	{
//		CloseHandle(m_MapComHandle);
//		m_MapComHandle = NULL;
//		return false;
//	}
//	//2.指针指向该共享内存
//	m_pMapComBuff = (char *)MapViewOfFile(m_MapComHandle,FILE_MAP_READ|FILE_MAP_WRITE,0,0,0);
//	if (m_pMapComBuff == NULL)
//	{
//		CloseHandle(m_MapComHandle);
//		m_MapComHandle = NULL;
//		return false;
//	}

	return true;
}
bool CGui::ShareBufferDestroy(void)
{
	if (m_MapGuiHandle)
	{
		if (CloseHandle(m_MapGuiHandle) == FALSE)return false;
	}
 	if (m_pMapGuiBuff != NULL)
 	{
 		m_pMapGuiBuff = NULL;
 	}

    if (m_pMapComBuff != NULL)
    {
        m_pMapComBuff = NULL;
    }

	return true;
}

bool CGui::GetMainFormHandle()
{
	m_hMainWnd = ::FindWindow(NULL,_T("EPA 2015"));
	if (m_hMainWnd == NULL)return false;
	return true;
}

bool CGui::SendMessage2MainForm_PostMessage()
{
	if (m_hMainWnd == NULL)return false;
	HRESULT hr = ::PostMessage(m_hMainWnd,MSG_MAIN_EPA2015,PARAM_GUI,0);
	if (hr == 0)return false;
	return true;
}
bool CGui::SendMessage2MainForm_SendMessage()
{
	if (m_hMainWnd == NULL)return false;

//	adsSleep(10);
	adsSleep(1);
	HRESULT hr = ::SendMessage(m_hMainWnd,MSG_MAIN_EPA2015,PARAM_GUI,0);
	adsSleep(1);
//	adsSleep(10);

//用SendMessageTimeout发消息给有子窗口的的主窗口消息时会出现阻塞导致超时返回失败
//	HRESULT hr = ::SendMessageTimeout(m_hMainWnd,MSG_MAIN_EPA2015,PARAM_GUI,0,SMTO_BLOCK,15000,0);

	//用PostMessage太快了，有时候消息多了的时候跟不上，部分消息会丢失
//	HRESULT hr = ::PostMessage(m_hMainWnd,MSG_MAIN_EPA2015,PARAM_GUI,0);
	
	//if (hr == 0)return false;//Q vs2010的显示程序为什么收不到消息?> 奇怪

	DWORD dwErr = GetLastError();
	if (dwErr != 0)
	{
		char szTempdel[100];
		sprintf(szTempdel,"SendMessage2MainForm_SendMessage()->GetLastError()==%d\r\n",dwErr);
		OutputDebugString(szTempdel);
	}
	return true;
}
bool CGui::SendMessage2MainForm_SendMessage_COM()
{
	if (m_hMainWnd == NULL)return false;
	HRESULT hr = ::SendMessage(m_hMainWnd,MSG_MAIN_EPA2015,PARAM_COM,0);
	return true;
}

DWORD CGui::StrToHex(char *str)
{
	DWORD dw=0;
	BYTE i=0;
	while(*(str+i)){
		if(*(str+i)>='0' && *(str+i)<='9')dw=(dw<<4)+(*(str+i)-'0');
		else if(*(str+i)>='a' && *(str+i)<='f')dw=(dw<<4)+(*(str+i)-'a'+10);
		else if(*(str+i)>='A' && *(str+i)<='F')dw=(dw<<4)+(*(str+i)-'A'+10);
		i++;
	}
	return dw;
}
BYTE CGui::ByteToAscii(BYTE nSource)
{
	BYTE nTarget = 0x00;
	if (nSource <= 0x09)
	{
		nTarget = nSource + 0x30;
	}
	else
	{
		nTarget = nSource + 55;
	}
	return nTarget;
}
bool CGui::IdTranslate(BYTE *pbId,BYTE bLen,BYTE *pbTarget)
{
	if (bLen > 100)return false;

	BYTE bTemp = 0x00;
	for (BYTE i=0; i<bLen; i++)
	{
		bTemp = (pbId[i]>>4)&0x0F;
		*pbTarget++ = ByteToAscii(bTemp);
		bTemp = pbId[i]&0x0F;
		*pbTarget++ = ByteToAscii(bTemp);
	}
	return true;
}
bool CGui::GetText(BYTE *pbId,WORD wLen,char *pszText,BYTE bIndex)
{
	char szText[1024] = "";
	if (!GetTextEx(TEXT_TXT,pbId,wLen,szText,bIndex))
	{
		if (!GetTextEx(TEXT_OTHER,pbId,wLen,szText,bIndex))
		{
			for (WORD i=0; i<wLen; i++) //读不到则显示ID值
			{
				sprintf(szText+i*2,"%02X",pbId[i]);
			}
		}
	}
	strcpy(pszText,szText);
	return true;
}


bool CGui::ReadDataFile(BYTE *pnBuff,WORD wLen)
{
	if (fread(pnBuff,sizeof(char),wLen,m_pfData) != wLen)
	{
		return false;
	}
	m_dwCurrentAddr += wLen;

	return true;
}

string CGui::LoadText(CBinary binID,BYTE bDB,BYTE nIndex)
{
	string str = "";
	char sz[4096] = "";//gxf
	if (GetTextEx(bDB,(BYTE *)binID.GetBuffer(),6,sz,nIndex))
	{
		str = sz;
	}
	else
	{
		if (GetTextEx(bDB,(BYTE *)CBinary("\x10\x08\x00\x00\x00\x16",6).GetBuffer(),6,sz,nIndex))
    	{
    		str = sz;
    	}
    	else
    	{
		    str = "#Load DB Error#";//gxf #数据库加载错误
		}
	}
	return str;
}

bool CGui::GetTextEx(BYTE bHead,BYTE *pbId,WORD wLen,char *pszText,BYTE bIndex)
{

	string strFileName = CInformation::GetVehiclesSystemName();
	if (strFileName == "SHVW")
	{
		CBinary bin;
		bin.WriteBuffer(pbId,wLen);

		string strFileName = "";
		switch (bHead)
		{
		case TEXT_TXT:
			strFileName = "CN_TXT";
			break;
		case TEXT_DTC:
			strFileName = "CN_DTC";
			break;
		case TEXT_CDS:
			strFileName = "CN_DS";
			break;
		default:
			OutputDebugString("\r\nGetTextEx Error!!\r\n");
			return false;
		}
//		vector<CBinary> vecbinText = g_DatabaseAcces.SearchIdAccessDatabase(bin,strFileName);
//		int itest = vecbinText.size();//del
//		if (vecbinText.size() < bIndex)return false;
//		char sz[1000] = "";
//		vecbinText[bIndex-1].ReadBuffer(sz);
//		strcpy(pszText,sz);
		return true;
	}


	DWORD dwBeginAddr = 0;
	DWORD dwEndAddr = 0;
	BYTE bAddrStartPos = 0x00;

	switch (bHead)
	{
	case TEXT_TXT:
		bAddrStartPos = 32;
		break;
	case TEXT_DTC:
		bAddrStartPos = 32+4+4;
		break;
	case TEXT_CDS:
		bAddrStartPos = 32+4+4+4+4;
		break;
	case TEXT_OTHER:
		bAddrStartPos = 32+4+4+4+4+4+4;
		break;
	default:
		return false;
	}

/*	fseek(m_pfData,bAddrStartPos,SEEK_SET); //指针偏移到要读取的地址开始
	fread(&dwBeginAddr,sizeof(DWORD),1,m_pfData);
	fread(&dwEndAddr,sizeof(DWORD),1,m_pfData);
	fseek(m_pfData,dwBeginAddr,SEEK_SET);
	m_dwCurrentAddr = dwBeginAddr;*/


    m_dwVdiIndex = bAddrStartPos;
    dwBeginAddr = m_pVdiData[m_dwVdiIndex+3]<<24|m_pVdiData[m_dwVdiIndex+2]<<16|m_pVdiData[m_dwVdiIndex+1]<<8|m_pVdiData[m_dwVdiIndex];
    m_dwVdiIndex += 4;
    dwEndAddr = m_pVdiData[m_dwVdiIndex+3]<<24|m_pVdiData[m_dwVdiIndex+2]<<16|m_pVdiData[m_dwVdiIndex+1]<<8|m_pVdiData[m_dwVdiIndex];
    m_dwVdiIndex += 4;
    m_dwVdiIndex = dwBeginAddr;
    m_dwCurrentAddr = dwBeginAddr;
  /*  m_dwCurrentAddr = bAddrStartPos;
    dwBeginAddr = m_pVdiData[m_dwVdiIndex+3]<<24|m_pVdiData[+2]<<16|
                    m_pVdiData[m_dwVdiIndex+1]<<8|m_pVdiData[m_dwVdiIndex];
    m_dwVdiIndex +=4;
    dwEndAddr = m_pVdiData[m_dwVdiIndex + 3] << 24 | m_pVdiData[m_dwVdiIndex + 2] << 16 |
                m_pVdiData[m_dwVdiIndex + 1] << 8 | m_pVdiData[m_dwVdiIndex];
    m_dwVdiIndex +=4;

    m_dwVdiIndex = dwBeginAddr;
    m_dwCurrentAddr = dwBeginAddr;*/

	WORD wSize = 0;
	BYTE nBuff[4096] = {0,};
	bool bIdFound = false;
	int i = 0,j=0;
	BYTE b = 0;

	while (1)
	{

//		if (fread(&wSize,sizeof(WORD),1,m_pfData) != 1)break;
        wSize = m_pVdiData[m_dwVdiIndex + 1] << 8 | m_pVdiData[m_dwVdiIndex];
		m_dwVdiIndex += 2;
		m_dwCurrentAddr += 2;

		if (wLen != wSize)
		{
//			fseek(m_pfData,wSize,SEEK_CUR);
            m_dwVdiIndex += wSize;
			m_dwCurrentAddr += wSize;
		}
		else
		{
			//if (fread(nBuff,sizeof(char),wLen,m_pfData) != wLen)break;
            for (j = 0; j < wLen; j++)
			 {
                nBuff[j] = m_pVdiData[m_dwVdiIndex++];
            }
			m_dwCurrentAddr += wLen;
			if (memcmp(pbId,nBuff,wLen) == 0)
			{

				WORD wAllLen = 0;
				WORD wOneLen = 0;
				WORD wCheckLen = 0; //еOneLen(2), wAllOne
//				if (fread(&wAllLen,sizeof(WORD),1,m_pfData) != 1)break;
                wAllLen = m_pVdiData[m_dwVdiIndex + 1] << 8 | m_pVdiData[m_dwVdiIndex];
				m_dwVdiIndex += 2;
				m_dwCurrentAddr += 2;
//				if (fread(&wOneLen,sizeof(WORD),1,m_pfData) != 1)break;
                wOneLen = m_pVdiData[m_dwVdiIndex + 1] << 8 | m_pVdiData[m_dwVdiIndex];
				m_dwVdiIndex += 2;
				m_dwCurrentAddr += 2;

				wCheckLen += wOneLen;
				wCheckLen += 2;

				for (BYTE i=1; i<bIndex; i++)
				{

//					fseek(m_pfData,wOneLen,SEEK_CUR);
                    m_dwVdiIndex += wOneLen;
                    m_dwCurrentAddr += wOneLen;
//					if (fread(&wOneLen,sizeof(WORD),1,m_pfData) != 1)break;
                    wOneLen = m_pVdiData[m_dwVdiIndex + 1] << 8 | m_pVdiData[m_dwVdiIndex];
					m_dwVdiIndex += 2;
					m_dwCurrentAddr += 2;

					wCheckLen += wOneLen;
					wCheckLen += 2;
					if (wCheckLen > wAllLen) //实际上一般是等于为最后一个
					{
						return false;
					}
				}

				memset(nBuff,0,4096);
//				if (fread(nBuff,sizeof(char),wOneLen,m_pfData) != wOneLen)break;
				//memcmp(pszText,nBuff,wOneLen+1);  //+1
                for (j=0;j<wOneLen;j++) 
				{
                    nBuff[j] = m_pVdiData[m_dwVdiIndex++];
                }
					//解密Begin
				BYTE nDecryptIndex = 0x00;
				for (WORD ww=0; ww<wLen; ww++)
				{
					nDecryptIndex += pbId[ww];
				}
                Decrypt2(nBuff,wOneLen);
				Decrypt(nDecryptIndex,nBuff,wOneLen);
				//解密End
//                OutputDebugString("pszText size:%d",wOneLen);
				nBuff[4095]=0;//gxf
				strcpy(pszText,(char *)nBuff);
				//lgh 处理文件里面有\r\n的串
				string strText = pszText;
				if (strText.find("\\\\r\\\\n") != -1)
				    ReplaceStr(pszText,"\\\\r\\\\n","\r\n");
				if (strText.find("\\r\\n") != -1)
                    ReplaceStr(pszText,"\\r\\n","\r\n");
//				OutputDebugString("pszText len:%d",strlen((char*)nBuff));
//				strncpy(pszText,(char *)nBuff,sizeof(nBuff));
				return true;
			}
			else
			{
				//if (fread(&wSize,sizeof(WORD),1,m_pfData) != 1)break;
                wSize = m_pVdiData[m_dwVdiIndex + 1] << 8 | m_pVdiData[m_dwVdiIndex];
				m_dwVdiIndex += 2;
				m_dwCurrentAddr += 2;
				//fseek(m_pfData,wSize,SEEK_CUR);
				m_dwVdiIndex += wSize;
				m_dwCurrentAddr += wSize;
			}
		}

		if (m_dwCurrentAddr >= dwEndAddr)break;	//超过要读取的地址

	}

	return false;
}
bool CGui::Decrypt2(BYTE *pBuf, WORD wLen)
{
	BYTE nByte = 0x00;
	BYTE bKey[4] = {0,};
	bKey[0] = (BYTE)(m_dwKey>>0x18);
	bKey[1] = (BYTE)(m_dwKey>>0x10);
	bKey[2] = (BYTE)(m_dwKey>>0x08);
	bKey[3] = (BYTE)(m_dwKey>>0x00);
	for (WORD w=0; w<wLen; w++)
	{
		nByte = pBuf[w];

		nByte = nByte ^ bKey[w%4];

		pBuf[w] = nByte;
	}	return true;
}
bool CGui::Decrypt(BYTE nIndex, BYTE *pBuf, WORD wLen)
{
	BYTE nByte = 0x00;
	for (WORD w=0; w<wLen; w++)
	{
		nByte = pBuf[w];

		if (w <= 255)
		{
			nByte = nByte ^ w;
		}
		else
		{
			nByte = nByte ^ ((BYTE)(w>>8));
			nByte = nByte ^ ((BYTE)(w>>0));
		}
		nByte = nByte ^ nIndex;
		BYTE tmp = ((nByte & 0x03) << 6);
		nByte = ((nByte >> 2) | tmp);

		pBuf[w] = nByte;
	}
	return true;
}

/*bool CGui::Decrypt(BYTE nIndex, BYTE *pBuf, WORD wLen)
{
	BYTE nByte = 0x00;
	for (WORD w=0; w<wLen; w++)
	{
		nByte = pBuf[w];
		if (w <= 255)
		{
			nByte = nByte ^ w;
		}
		else
		{
			nByte = nByte ^ ((BYTE)(w>>8));
			nByte = nByte ^ ((BYTE)(w>>0));
		}
		nByte = nByte ^ nIndex;
		BYTE tmp = ((nByte & 0x03) << 6);
		nByte = ((nByte >> 2) | tmp);

		pBuf[w] = nByte;
	}

	return true;
}


bool CGui::Decrypt2(BYTE *pBuf, WORD wLen)
{
	return true;
}*/

/*
bool CGui::GetTextEx(BYTE bHead,BYTE *pbId,BYTE bLen,char *pszText,BYTE bIndex)
{
	BYTE bIdBuffer[50] = {0,};
	if (!IdTranslate(pbId,bLen,bIdBuffer))return false;
	bLen *= 2;

	fseek(m_pfData,0,SEEK_SET); //从文件头开始读起
	m_dwCurrentAddr = 0;

	bool bIdFound = false;
	int i = 0;
	BYTE b = 0;
	while (1)
	{
		i = fgetc(m_pfData);
		if (i == EOF)return false;
		b = (BYTE)i;
		m_dwCurrentAddr++;

		if (b != bHead)continue;		//读到头标记位为开始(05表示TXT,06表示DTC,07表示DS,08表示其它)

		break;
	}

	while (1)
	{
		i = fgetc(m_pfData);
		if (i == EOF)return false;
		b = (BYTE)i;
		m_dwCurrentAddr++;

		if (b == 0x01) //(01) ID 01
		{
			for (BYTE j=0; j<bLen; j++)
			{
				i = fgetc(m_pfData);
				if (i == EOF)return false;
				b = (BYTE)i;
				m_dwCurrentAddr++;
				if (b != bIdBuffer[j])break;
			}
			if (j != bLen)continue;
			i = fgetc(m_pfData);
			if (i == EOF)return false;
			b = (BYTE)i;
			m_dwCurrentAddr++;
			if (b == 0x01) //01 ID (01)
			{
				bIdFound = true;
				break;
			}
		}
	}

	if (!bIdFound)return false;

	memset(pszText,0,strlen(pszText));
	i = fgetc(m_pfData);
	if (i == EOF)return false;
	b = (BYTE)i;
	m_dwCurrentAddr++;
	if (b != 0x02)return false; //(02) contents 02 02 contents 02 ... 01

	BYTE nContentCount = 0;
	const WORD LENGTH_CONTENT = 2048; //如果内容超过这个长度，则需要改大一点
	char szTemp[LENGTH_CONTENT] = "";
	WORD wLen = 0;
	while (1)
	{
		i = fgetc(m_pfData);
		if (i == EOF)return false;
		b = (BYTE)i;
		m_dwCurrentAddr++;

//08其它会有好几个(以后再说)
//		if (b == bHead)return false;		//读到头标记位结束字节(05表示TXT,06表示DTC,07表示DS,08表示其它)

		if (b == 0x02) //02 contents (02) 02 contents 02 ... 01
		{
			nContentCount++;
			if (nContentCount == bIndex)break;
			else
			{
				memset(szTemp,0,strlen(szTemp));
				wLen = 0;
				i = fgetc(m_pfData);
				if (i == EOF)return false;
				b = (BYTE)i;
				if (b != 0x02)return false; //02 contents 02 (02) contents 02 ... 01
				m_dwCurrentAddr++;
				continue;
			}
			i = fgetc(m_pfData);
			if (i == EOF)return false;
			b = (BYTE)i;
			m_dwCurrentAddr++;
			if (b != 0x02)return false; //02 contents 02 (02) contents 02 ... 01
			continue;
		}
		if (wLen >= LENGTH_CONTENT)return false;
		szTemp[wLen++] = (char)b;
	}
	szTemp[wLen] = '\0';
	memcpy(pszText,szTemp,wLen);

	return true;
}
*/

//////////////////////////////////////////////////////////////////////////
// 打开数据文件
// 注意:只能是当前文件夹下数据文件或者上一层目录文件夹下的数据文件(两者只能二选一)
//

bool CGui::DataFileOpen()
{

	//获取本地路径
	char szPath[1024];
	DWORD dwLen = GetModuleFileName(NULL,szPath,1024);
	if (dwLen == 0){
	    LoadVdiStatusCallBack(false);
	    return false;
	}
	//查找最后一个\符号,找到路径(不含文件名字的路径)
	int i;
	for (i=dwLen-1; i>0; i--)
	{
		if (szPath[i] ==SPLIT)
		{
			break;
		}
	}
	if (i <= 0){
	    LoadVdiStatusCallBack(false);
	    return false;
	}
	szPath[i+1] = '\0';

	g_strPath = szPath; //记录下目录

	//文件夹向上一层(当前文件夹下没有数据文件时候查询上一层文件夹,目的是共用数据文件)
	char szPathEx[1024];
	strcpy(szPathEx,szPath);
	DWORD dwLenEx = strlen(szPath);
	int j;
	for (j=dwLenEx-2; j>0; j--)
	{
		if (szPathEx[j] == SPLIT)
		{
			break;
		}
	}
	if (j <= 0){
	    LoadVdiStatusCallBack(false);
	    return false;
	}
	szPathEx[j+1] = '\0';
	//添加数据文件

	bool bCurrent = true;			//数据文件在当前文件还是上一层文件夹,true为当前文件夹有数据文件

	strcat(szPath,_T("data.vdi"));
	if (_access(szPath,4) == -1)
	{
		bCurrent = false;
		//如果当前文件夹下找不到数据文件,则向上一层文件夹查找
		strcat(szPathEx,_T("data.vdi"));
		if (_access(szPathEx,4) == -1)
		{
		    LoadVdiStatusCallBack(false);
			return false;
		}
	}
	else
	{
		bCurrent = true;
	}
	//打开数据文件
	string strFileName = CInformation::GetVehiclesSystemName();
//	if (strFileName == "SHVW")
//	{
//		if (!g_DatabaseAcces.OpenAccessDatabase(""))
//		{
//			return false;
//		}
//		return true;
//	}


	if (bCurrent)
	{
		m_pfData = fopen(szPath,"rb");
	}
	else
	{
		m_pfData = fopen(szPathEx,"rb");
	}
	
	if (m_pfData == NULL){
	    LoadVdiStatusCallBack(false);
	    return false;
	}
    OutputDebugString("Loading resource......");
    fseek(m_pfData,0,SEEK_END);
    DWORD dwFileLen = ftell(m_pfData);
    fseek(m_pfData,0,SEEK_SET);
    m_pVdiData = new BYTE [dwFileLen];
    if (m_pVdiData == NULL){
        LoadVdiStatusCallBack(false);
        return false;
    }
    if (fread(m_pVdiData,sizeof(char),dwFileLen,m_pfData) != dwFileLen)
    {
        delete [] m_pVdiData;
        fclose(m_pfData);
        OutputDebugString("Failed\r\n");
        LoadVdiStatusCallBack(false);
        return false;
    }
    fclose(m_pfData);

    DWORD dwSeed = (DWORD)m_pVdiData[2]<<24|m_pVdiData[3]<<16|m_pVdiData[4]<<8|m_pVdiData[5];

    BYTE bSendBuf[20],bRecvBuf[20];
    WORD bSendLen,bRecvLen;
    BYTE bTemp[20];

    if (!g_Com.MySecurityAccess())
	{
		OutputDebugString("MySecurityAccess Failed\r\n");
		LoadVdiStatusCallBack(false);
		return false;
	}
    //	A5 A5 00 06 FE 03 12 34 56 78 55
    //	A5 A5 00 06 FE 03 CC CC CC CC 55
    memcpy(bSendBuf,(BYTE *)"\xA5\xA5\x00\x06\xFE\x03\xCC\xCC\xCC\xCC\x55",bSendLen=11);
    bSendBuf[6] = (BYTE)(dwSeed>>0x18);
    bSendBuf[7] = (BYTE)(dwSeed>>0x10);
    bSendBuf[8] = (BYTE)(dwSeed>>0x08);
    bSendBuf[9] = (BYTE)(dwSeed>>0x00);
    if (!g_Com.SendCmd(bSendBuf,bSendLen)){
        OutputDebugString("Failed\r\n");
        LoadVdiStatusCallBack(false);
        return false;
    }
    if (!g_Com.RecvCmdEx(bRecvBuf,bRecvLen=11)){
        OutputDebugString("Failed\r\n");
        LoadVdiStatusCallBack(false);
        return false;
    }
    memcpy(bTemp,bRecvBuf,6);
    if (memcmp(bTemp,(BYTE *)"\xA5\xA5\x00\x06\xFE\x03",6) != 0){
        OutputDebugString("Failed\r\n");
        LoadVdiStatusCallBack(false);
        return false;
    }
    if (bRecvBuf[10] != 0x55){
        OutputDebugString("Failed\r\n");
        LoadVdiStatusCallBack(false);
        return false;
    }
    m_dwKey = (DWORD)bRecvBuf[6]<<24|bRecvBuf[7]<<16|bRecvBuf[8]<<8|bRecvBuf[9];
    OutputDebugString("OK\r\n");
    LoadVdiStatusCallBack(true);
	return true;
}
bool CGui::DataFileClose()
{
	string strFileName = CInformation::GetVehiclesSystemName();
//	if (strFileName == "SHVW")
//	{
//		if (!g_DatabaseAcces.CloseAccessDatabase())
//		{
//			return false;
//		}
//		return true;
//	}
    if (m_pVdiData != NULL)
    {
        delete [] m_pVdiData;
        m_pVdiData = NULL;
    }

/*	if (m_pfData != NULL)
	{
		if (fclose(m_pfData) != 0)return false;
	}*/
	return true;
}




//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////
// MENU

bool CGui::MenuInit()
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_MENU;
	m_pMapGuiBuff[3] = (BYTE)0x01;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x00;
	m_pMapGuiBuff[6] = '\0';
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}
bool CGui::MenuAdd(BYTE *pbId,BYTE bLen)
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	char szBuff[1024] = "";
	if (!GetTextEx(TEXT_TXT,pbId,bLen,szBuff))
	{
		for (BYTE i=0; i<bLen; i++) //读不到则显示ID值
		{
			sprintf(szBuff+i*2,"%02X",pbId[i]);
		}
	}

	return MenuAdd(szBuff);
}
bool CGui::MenuAdd(char *pszMenu)
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	char szBuff[1024] = "";
	strcpy(szBuff,pszMenu);

	WORD wLen = strlen(szBuff);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_MENU;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+6,szBuff,wLen);
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}
BYTE CGui::MenuShow()
{
	CloseDialogWait();

	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_MENU;
	m_pMapGuiBuff[3] = (BYTE)0x03;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x01;
	m_pMapGuiBuff[6] = (BYTE)0xFE;
	m_pMapGuiBuff[7] = '\0';
	if (!SendMessage2MainForm_SendMessage())
		return false;
	BYTE bKeyIndex = m_pMapGuiBuff[6];
	while (1)
	{
		Sleep(1);
		bKeyIndex = m_pMapGuiBuff[6];
		if (bKeyIndex != 0xFE)
		{
			break;
		}
	}
	return bKeyIndex;
}

bool CGui::MenuExInit(UINT nMenuNum)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);

	char szMenuNumBuff[3] = "";
	sprintf(szMenuNumBuff, "%d", nMenuNum);
	WORD wMenuNumLen = 0;
	wMenuNumLen = (WORD)strlen(szMenuNumBuff);

	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_MENU_EX;
	m_pMapGuiBuff[3] = (BYTE)0x01;
	m_pMapGuiBuff[4] = (BYTE)(wMenuNumLen >> 8);
	m_pMapGuiBuff[5] = (BYTE)wMenuNumLen;
	memcpy(m_pMapGuiBuff + 6, szMenuNumBuff, wMenuNumLen);

	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	return true;
}

bool CGui::MenuExAdd(char * pszMenu, UINT nCtrlFlag)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);

	char szFlagBuff[10] = "";
	sprintf(szFlagBuff, "%d", nCtrlFlag);
	WORD wFlagLen = (WORD)strlen(szFlagBuff);

	char szBuff[1024] = "";
	strcpy(szBuff, pszMenu);
	WORD wLen = (WORD)strlen(szBuff);

	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_MENU_EX;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)(wFlagLen >> 8);
	m_pMapGuiBuff[5] = (BYTE)wFlagLen;
	m_pMapGuiBuff[6] = (BYTE)(wLen >> 8);
	m_pMapGuiBuff[7] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff + 8, szFlagBuff, wFlagLen);
	memcpy(m_pMapGuiBuff + 8 + wFlagLen, szBuff, wLen);

	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	return true;
}

bool CGui::MenuExAdd(vector<string> vecpszMenu, UINT nCtrlFlag)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);

	char szFlagBuff[10] = "";
	sprintf(szFlagBuff, "%d", nCtrlFlag);
	WORD wFlagLen = (WORD)strlen(szFlagBuff);

	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_MENU_EX;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)(wFlagLen >> 8);
	m_pMapGuiBuff[5] = (BYTE)wFlagLen;

	string strMenu;
	char szBuff[8192] = "";
	for (UINT i = 0; i < vecpszMenu.size(); i++)
	{
		strMenu += vecpszMenu[i] + _T("@*@");
	}

	strcpy(szBuff, strMenu.c_str());
	WORD wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[6] = (BYTE)(wLen >> 8);
	m_pMapGuiBuff[7] = (BYTE)wLen;

	memcpy(m_pMapGuiBuff + 8, szFlagBuff, wFlagLen);
	memcpy(m_pMapGuiBuff + 8 + wFlagLen, szBuff, wLen);

	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	return true;
}

BYTE CGui::MenuExShow(vector<UINT>& vecIndex)
{
	CloseDialogWait();

	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_MENU_EX;
	m_pMapGuiBuff[3] = (BYTE)0x03;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x01;
	m_pMapGuiBuff[6] = (BYTE)0xFE;
	m_pMapGuiBuff[7] = '\0';
	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	BYTE bKeyIndex = m_pMapGuiBuff[6];   
	while (1)
	{
		Sleep(1);
		bKeyIndex = m_pMapGuiBuff[6];

		if (bKeyIndex == 0xFF)
		{
			return bKeyIndex;
		}

		if (bKeyIndex != 0xFE)
		{
			UINT nIndexNum = 0;
			nIndexNum = (UINT)m_pMapGuiBuff[0];
			if (nIndexNum == 0)
			{
				bKeyIndex = 0x7E;  
				break;
			}

			for (UINT i = 0; i < nIndexNum; i++)
			{
				vecIndex.push_back((UINT)m_pMapGuiBuff[i + 7]);
			}

			break;
		}
	}
	return bKeyIndex;
}

bool CGui::MenuCtrlInit()
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_MENU_CTRL;
	m_pMapGuiBuff[3] = (BYTE)0x01;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x00;
	m_pMapGuiBuff[6] = '\0';
	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}
		
	return true;
}

bool CGui::MenuCtrlAdd(char * pszMenu, UINT nCtrlFlag)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);

	char szCtrlFlagBuff[10] = "";
	sprintf(szCtrlFlagBuff, "%d", nCtrlFlag);
	WORD nCtrlFlagLen = (WORD)strlen(szCtrlFlagBuff);

	const int needlen=strlen(pszMenu);
	char *pBuff = new char [needlen + 1];
	if (pBuff==NULL)return false;
	memset(pBuff,0,needlen+1);
	//char pBuff[2048] = "";
	strcpy(pBuff, pszMenu);
	WORD nLen = (WORD)strlen(pBuff);

	WORD nTotalLen = 0;
	nTotalLen = nCtrlFlagLen + nLen;

	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_MENU_CTRL;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)(nTotalLen >> 8);
	m_pMapGuiBuff[5] = (BYTE)nTotalLen;

	m_pMapGuiBuff[6] = (BYTE)(nCtrlFlagLen >> 8);
	m_pMapGuiBuff[7] = (BYTE)nCtrlFlagLen;

	m_pMapGuiBuff[8] = (BYTE)(nLen >> 8);
	m_pMapGuiBuff[9] = (BYTE)nLen;

	memcpy(m_pMapGuiBuff + 18, szCtrlFlagBuff, nCtrlFlagLen);
	memcpy(m_pMapGuiBuff + 18 + nCtrlFlagLen, pBuff, nLen);

	if (pBuff)
	{
		delete pBuff;
		pBuff=NULL;
	}

	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	return true;
}

bool CGui::MenuCtrlAdd(UINT nRow, UINT nColumn, UINT nCtrlType, vector<UINT> vecColumnWidthRatio, vector<string> vecShowText, UINT nCtrlFlag)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);

	char szCtrlFlagBuff[10] = "";              
	char szRowBuff[10] = "";
	char szColumnBuff[10] = "";
	char szCtrlType[10] = "";
	char *pColumnWidthRadioBuff = 0;
	char *pShowTextBuff = 0;

	WORD nCtrlFlagLen = 0;
	WORD nRowLen = 0;
	WORD nColumnLen = 0;
	WORD nCtrlTypeLen = 0;
	WORD nColumnWidthRadioLen = 0;
	WORD nShowTextLen = 0;

	sprintf(szCtrlFlagBuff, "%d", nCtrlFlag);
	sprintf(szRowBuff, "%d", nRow);
	sprintf(szColumnBuff, "%d", nColumn);
	sprintf(szCtrlType, "%d", nCtrlType);

	//////////////////////////////////////////////////////////////
	if (vecColumnWidthRatio.size() == 0)
	{
		//sprintf(pColumnWidthRadioBuff, "%s", "");
		return false;
	}
	else
	{
		string strTmp = "";
		for (UINT i = 0; i < vecColumnWidthRatio.size(); i++)
		{
			string str;
			char szBuff[100];
			sprintf(szBuff, "%d", vecColumnWidthRatio[i]);
			str = szBuff;
			strTmp += str + "@*@";
		}
		pColumnWidthRadioBuff=new char [strTmp.length()+1];
		if (pColumnWidthRadioBuff==NULL)return false;
		memset(pColumnWidthRadioBuff,0,strTmp.length()+1);
		sprintf(pColumnWidthRadioBuff, "%s", strTmp.c_str());
	}
	
	/////////////////////////////////////////////////////////////////
	if (vecShowText.size() == 0)
	{
		//sprintf(pShowTextBuff, "%s", "");
		return false;
	}
	else
	{
		string strTmp = "";
		for (UINT i = 0; i < vecShowText.size(); i++)
		{
			strTmp += vecShowText[i] + "@*@";
		}
		pShowTextBuff=new char [strTmp.length()+1];
		if (pShowTextBuff==NULL)return false;
		memset(pShowTextBuff,0,strTmp.length()+1);
		sprintf(pShowTextBuff, "%s", strTmp.c_str());
	}

	nCtrlFlagLen = (WORD)strlen(szCtrlFlagBuff);
	nRowLen = (WORD)strlen(szRowBuff);
	nColumnLen = (WORD)strlen(szColumnBuff);
	nCtrlTypeLen = (WORD)strlen(szCtrlType);
	nColumnWidthRadioLen = (WORD)strlen(pColumnWidthRadioBuff);
	nShowTextLen = (WORD)strlen(pShowTextBuff);

	WORD nTotalLen = 0;
	nTotalLen = nCtrlFlagLen + nRowLen + nColumnLen + nCtrlTypeLen + nColumnWidthRadioLen + nShowTextLen;

	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_MENU_CTRL;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)(nTotalLen >> 8);
	m_pMapGuiBuff[5] = (BYTE)nTotalLen;

	m_pMapGuiBuff[6] = (BYTE)(nCtrlFlagLen >> 8);
	m_pMapGuiBuff[7] = (BYTE)nCtrlFlagLen;

	m_pMapGuiBuff[8] = (BYTE)(nRowLen >> 8);
	m_pMapGuiBuff[9] = (BYTE)nRowLen;

	m_pMapGuiBuff[10] = (BYTE)(nColumnLen >> 8);
	m_pMapGuiBuff[11] = (BYTE)nColumnLen;

	m_pMapGuiBuff[12] = (BYTE)(nCtrlTypeLen >> 8);
	m_pMapGuiBuff[13] = (BYTE)nCtrlTypeLen;

	m_pMapGuiBuff[14] = (BYTE)(nColumnWidthRadioLen >> 8);
	m_pMapGuiBuff[15] = (BYTE)nColumnWidthRadioLen;

	m_pMapGuiBuff[16] = (BYTE)(nShowTextLen >> 8);
	m_pMapGuiBuff[17] = (BYTE)nShowTextLen;

	memcpy(m_pMapGuiBuff + 18, szCtrlFlagBuff, nCtrlFlagLen);
	memcpy(m_pMapGuiBuff + 18 + nCtrlFlagLen, szRowBuff, nRowLen);
	memcpy(m_pMapGuiBuff + 18 + nCtrlFlagLen + nRowLen, szColumnBuff, nColumnLen);
	memcpy(m_pMapGuiBuff + 18 + nCtrlFlagLen + nRowLen + nColumnLen, szCtrlType, nCtrlTypeLen);
	memcpy(m_pMapGuiBuff + 18 + nCtrlFlagLen + nRowLen + nColumnLen + nCtrlTypeLen, pColumnWidthRadioBuff, nColumnWidthRadioLen);
	memcpy(m_pMapGuiBuff + 18 + nCtrlFlagLen + nRowLen + nColumnLen + nCtrlTypeLen + nColumnWidthRadioLen, pShowTextBuff, nShowTextLen);

	if (pColumnWidthRadioBuff){
		delete pColumnWidthRadioBuff;
		pColumnWidthRadioBuff=NULL;
	}
	if (pShowTextBuff){
		delete pShowTextBuff;
		pShowTextBuff=NULL;
	}
	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	return true;
}

bool CGui::MenuCtrlAdd(UINT nRow, UINT nColumn, vector<string> vecShowText, UINT nCtrlFlag)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);

	char szCtrlFlagBuff[10] = "";               
	char szRowBuff[10] = "";
	char szColumnBuff[10] = "";
	char *pShowTextBuff = 0;

	WORD nCtrlFlagLen = 0;
	WORD nRowLen = 0;
	WORD nColumnLen = 0;
	WORD nShowTextLen = 0;

	sprintf(szCtrlFlagBuff, "%d", nCtrlFlag);
	sprintf(szRowBuff, "%d", nRow);
	sprintf(szColumnBuff, "%d", nColumn);

	/////////////////////////////////////////////////////////////////
	if (vecShowText.size() == 0)
	{
		return false;
	}
	else
	{
		string strTmp = "";
		for (UINT i = 0; i < vecShowText.size(); i++)
		{
			strTmp += vecShowText[i] + "@*@";
		}
		pShowTextBuff=new char [strTmp.length()+1];
		if (pShowTextBuff==NULL)return false;
		memset(pShowTextBuff,0,strTmp.length()+1);
		sprintf(pShowTextBuff, "%s", strTmp.c_str());
	}

	nCtrlFlagLen = (WORD)strlen(szCtrlFlagBuff);
	nRowLen = (WORD)strlen(szRowBuff);
	nColumnLen = (WORD)strlen(szColumnBuff);
	nShowTextLen = (WORD)strlen(pShowTextBuff);

	WORD nTotalLen = 0;
	nTotalLen = nCtrlFlagLen + nRowLen + nColumnLen + nShowTextLen;

	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_MENU_CTRL;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)(nTotalLen >> 8);
	m_pMapGuiBuff[5] = (BYTE)nTotalLen;

	m_pMapGuiBuff[6] = (BYTE)(nCtrlFlagLen >> 8);
	m_pMapGuiBuff[7] = (BYTE)nCtrlFlagLen;

	m_pMapGuiBuff[8] = (BYTE)(nRowLen >> 8);
	m_pMapGuiBuff[9] = (BYTE)nRowLen;

	m_pMapGuiBuff[10] = (BYTE)(nColumnLen >> 8);
	m_pMapGuiBuff[11] = (BYTE)nColumnLen;

	m_pMapGuiBuff[12] = (BYTE)(nShowTextLen >> 8);
	m_pMapGuiBuff[13] = (BYTE)nShowTextLen;

	memcpy(m_pMapGuiBuff + 18, szCtrlFlagBuff, nCtrlFlagLen);
	memcpy(m_pMapGuiBuff + 18 + nCtrlFlagLen, szRowBuff, nRowLen);
	memcpy(m_pMapGuiBuff + 18 + nCtrlFlagLen + nRowLen, szColumnBuff, nColumnLen);
	memcpy(m_pMapGuiBuff + 18 + nCtrlFlagLen + nRowLen + nColumnLen, pShowTextBuff, nShowTextLen);

	if (pShowTextBuff){
	delete	pShowTextBuff;
		pShowTextBuff=NULL;
	}
	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	return true;
}

BYTE CGui::MenuCtrlShow(vector<string> &vecStrDefText)
{
	CloseDialogWait();

	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_MENU_CTRL;
	m_pMapGuiBuff[3] = (BYTE)0x03;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x01;
	m_pMapGuiBuff[6] = (BYTE)0xFE;
	m_pMapGuiBuff[7] = '\0';
	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}
		
volatile	BYTE bKeyIndex = m_pMapGuiBuff[6];
	while (1)
	{
        Sleep(1);
		bKeyIndex = m_pMapGuiBuff[6];
		if ( bKeyIndex!= 0xFE)
		{
			UINT nRows = 0;
			nRows = (UINT)m_pMapGuiBuff[0];
			if (nRows == 0)
			{
                m_pMapGuiBuff[6] = 0x7E;
				__android_log_print(ANDROID_LOG_INFO,"MenuCtrlShow","m_pMapGuiBuff[6]=0x7e");
				break;
			}

			int nTotalLen = 0;	
			nTotalLen = (int)(m_pMapGuiBuff[8] << 8 | m_pMapGuiBuff[9]);

		
			int nIndex = 0;
			nIndex = 10 + (nRows - 1) * 2 + 2;

			int nTotalLenTmp = 0;

			DWORD dwLen = 0;
	
			vecStrDefText.clear();
			for (UINT i = 0; i < nRows; i++)
			{
				char szBuff[40960] = { 0 };//gxf
				dwLen = (DWORD)((BYTE)m_pMapGuiBuff[10 + i * 2] << 8 | (BYTE)m_pMapGuiBuff[10 + i * 2 + 1]);
				
				memcpy(szBuff, m_pMapGuiBuff + nIndex + nTotalLenTmp, dwLen);

				nTotalLenTmp += dwLen;

				string strSource(szBuff);

				if (strSource.find("@&@") == string::npos) 
				{
					string strGap = "@*@";

					string strTarget;
					ExtractStrToStr(strGap, strSource, strTarget);

					strSource = strTarget;
				}
			
				vecStrDefText.push_back(strSource);
			}
			__android_log_print(ANDROID_LOG_INFO,"MenuCtrlShow2","m_pMapGuiBuff[6]=0x7e");
			break;
		}
	}
    __android_log_print(ANDROID_LOG_INFO,"MenuCtrlShow3","bKeyIndex=%x",bKeyIndex);
    return  bKeyIndex;
}

void CGui::ExtractStrToStr(string strGap, string strSource, string & strTarget)
{
	string strSourceTmp;
	strSourceTmp = strSource;

	string strGapTemp = strGap; 
	int nPos = -1;
	nPos = strSourceTmp.find(strGapTemp);

	if(nPos != string::npos) 
	{
		string str;
		str = strSourceTmp.substr(0, nPos);
		strTarget = str;
	}
}

void CGui::ExtractStrToStr(string strGap, string strSource, vector<string>& vecStrTarget)
{
	string strSourceTmp;
	strSourceTmp = strSource;

	int nPos = -1;
	nPos = strSourceTmp.find(strGap);

	while (nPos != string::npos)  
	{
		string str;
		str = strSourceTmp.substr(0, nPos);
		vecStrTarget.push_back(str);

		strSourceTmp = strSourceTmp.substr(nPos + strGap.length());

		nPos = strSourceTmp.find(strGap);
	}
}

//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////
// DTC

bool CGui::DtcInit()
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_DTC;
	m_pMapGuiBuff[3] = (BYTE)0x01;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x00;
	m_pMapGuiBuff[6] = '\0';
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}
bool CGui::DtcInit(INT nItem, INT nColumnWidth)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_DTC;
	m_pMapGuiBuff[3] = (BYTE)0x01;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x06;
	m_pMapGuiBuff[6] = (BYTE)nItem;
	m_pMapGuiBuff[7] = (BYTE)(nColumnWidth >> 8);
	m_pMapGuiBuff[8] = (BYTE)nColumnWidth;
	m_pMapGuiBuff[9] = '\0';

	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	return true;
}
bool CGui::DtcAdd(BYTE *pbId,BYTE bLen)
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	//1.ID
	char szBuff[100] = "";
	if (!GetTextEx(TEXT_DTC,pbId,bLen,szBuff))
	{
//OK ,但不以这种方式显示
//		for (BYTE i=0; i<bLen; i++) //读不到则显示ID值
//		{
//			sprintf(szBuff+i*2,"%02X",pbId[i]);
//		}
		//以下这种类型显示
		sprintf(szBuff,"%04X",pbId[4]<<8|pbId[5]);
	}
	WORD wLen = strlen(szBuff);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_DTC;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+6,szBuff,wLen);
	//2.contents
//	fseek(m_pfData,-100L,SEEK_CUR); //指针退回100个字节，再读ID(减少读文件时间)
//	m_dwCurrentAddr -= 100;
	char szBuffEx[2048] = "";
	if (!GetTextEx(TEXT_DTC,pbId,bLen,szBuffEx,2))
	{
//		for (BYTE i=0; i<bLen; i++) //读不到则显示ID值
//		{
//			sprintf(szBuffEx+i*2,"%02X",pbId[i]);
//		}
		if (g_dwProduct == PRODUCT_EPS918_PRO || 
			g_dwProduct == PRODUCT_EPS918_STD ||
			g_dwProduct == PRODUCT_EPS916)
		{
            if (!GetTextEx(TEXT_TXT,(BYTE *)CBinary("\x10\x08\x00\x00\x00\x16",6).GetBuffer(),6,szBuffEx,1))
            {
                strcpy(szBuffEx,"#Load DB Error#");//gxf "#Load DB Error#";//"未定义,请联系厂家售后"
            }
		}
		else if (g_dwProduct == PRODUCT_EPS918_DC)
		{
            if (!GetTextEx(TEXT_TXT,(BYTE *)CBinary("\x10\x08\x00\x00\x00\x16",6).GetBuffer(),6,szBuffEx,1))
            {
                strcpy(szBuffEx,"#Load DB Error#");//gxf "#Load DB Error#";//"未定义,请联系厂家售后"
            }
		}
		
	}
	WORD wLenEx = strlen(szBuffEx);
	m_pMapGuiBuff[wLen+7] = (BYTE)(wLenEx>>8);
	m_pMapGuiBuff[wLen+8] = (BYTE)wLenEx;
	memcpy(m_pMapGuiBuff+wLen+9,szBuffEx,wLenEx);
	
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}
bool CGui::DtcAdd(char *pszId,char *pszDtc)
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	//1.ID
	char szBuff[100] = "";
	strcpy(szBuff,pszId);
	WORD wLen = strlen(szBuff);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_DTC;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+6,szBuff,wLen);
	//2.contents
	char szBuffEx[2048] = "";
	strcpy(szBuffEx,pszDtc);
	WORD wLenEx = strlen(szBuffEx);
	if (wLenEx < 2) //没有故障码内容的时候传进来的是一个空格,长度为1
	{
		if (g_dwProduct == PRODUCT_EPS918_PRO || 
			g_dwProduct == PRODUCT_EPS918_STD ||
			g_dwProduct == PRODUCT_EPS916)
		{
            if (!GetTextEx(TEXT_TXT,(BYTE *)CBinary("\x10\x08\x00\x00\x00\x16",6).GetBuffer(),6,szBuffEx,1))
            {
                strcpy(szBuffEx,"#Load DB Error#");//gxf "#Load DB Error#";//"未定义,请联系正德友邦售后"
            }
		}
		else if (g_dwProduct == PRODUCT_EPS918_DC)
		{
            if (!GetTextEx(TEXT_TXT,(BYTE *)CBinary("\x10\x08\x00\x00\x00\x16",6).GetBuffer(),6,szBuffEx,1))
            {
                strcpy(szBuffEx,"#Load DB Error#");//gxf "#Load DB Error#";//"未定义,请联系正德友邦售后"
            }
		}

		wLenEx = strlen(szBuffEx);
	}
	m_pMapGuiBuff[wLen+7] = (BYTE)(wLenEx>>8);
	m_pMapGuiBuff[wLen+8] = (BYTE)wLenEx;
	memcpy(m_pMapGuiBuff+wLen+9,szBuffEx,wLenEx);
	
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}

bool CGui::DtcAdd(char *pszId, char *pszDtc, char *pszContent)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);


	//1.ID
	char szBuff[100] = "";
	strcpy(szBuff, pszId);
	WORD wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_DTC;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)(wLen >> 8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff + 6, szBuff, wLen);

	//2.contents
	char szBuffEx[2048] = "";
	strcpy(szBuffEx, pszDtc);
	WORD wLenEx = (WORD)strlen(szBuffEx);
	if (wLenEx < 2) //没有故障码内容的时候传进来的是一个空格,长度为1
	{
        if (!GetTextEx(TEXT_TXT,(BYTE *)CBinary("\x10\x08\x00\x00\x00\x16",6).GetBuffer(),6,szBuffEx,1))
        {
            strcpy(szBuffEx,"#Load DB Error#");//gxf "#Load DB Error#";//"未定义,请联系厂家售后"
        }
		wLenEx = (WORD)strlen(szBuffEx);
	}

	m_pMapGuiBuff[wLen + 7] = (BYTE)(wLenEx >> 8);
	m_pMapGuiBuff[wLen + 8] = (BYTE)wLenEx;
	memcpy(m_pMapGuiBuff + wLen + 9, szBuffEx, wLenEx);

	//3、显示程序故障码第三列要显示的内容
	char szBuffContent[1024] = "";
	strcpy(szBuffContent, pszContent);
	WORD wLenContentLength = (WORD)strlen(szBuffContent);

	m_pMapGuiBuff[wLen + 10 + wLenEx] = (BYTE)(wLenContentLength >> 8);
	m_pMapGuiBuff[wLen + 11 + wLenEx] = (BYTE)wLenContentLength;
	memcpy(m_pMapGuiBuff + wLen + 12 + wLenEx, szBuffContent, wLenContentLength);

	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	return true;
}

BYTE CGui::DtcShow()
{
	CloseDialogWait();

	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_DTC;
	m_pMapGuiBuff[3] = (BYTE)0x03;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x01;
	m_pMapGuiBuff[6] = (BYTE)0xFE;
	m_pMapGuiBuff[7] = '\0';
	if (!SendMessage2MainForm_SendMessage())
		return false;
	BYTE bKeyIndex = m_pMapGuiBuff[6];
	while (1)
	{
		Sleep(1);
		bKeyIndex = m_pMapGuiBuff[6];
		if (bKeyIndex != 0xFE)
		{
			break;
		}
	}
	return bKeyIndex;
}

bool CGui::DtcMultiInit(INT nColumns)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);

	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_DTC_MULTI;
	m_pMapGuiBuff[3] = (BYTE)0x01;
	m_pMapGuiBuff[4] = (BYTE)0x00;		//列宽是否按着比列进行分配
	m_pMapGuiBuff[5] = (BYTE)nColumns;	//列数，最多255

	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	return true;
}

bool CGui::DtcMultiInit(INT nColumns, vector<INT> vecColumnWidthRatio)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);

	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_DTC_MULTI;
	m_pMapGuiBuff[3] = (BYTE)0x01;
	m_pMapGuiBuff[4] = (BYTE)0x01;		//列宽是否按着比列进行分配
	m_pMapGuiBuff[5] = (BYTE)nColumns;	//列数，最多255

	if (vecColumnWidthRatio.size() != nColumns)
	{
		return false;
	}

	for (INT i = 0; i < nColumns; i++)
	{
		m_pMapGuiBuff[6 + i] = (BYTE)vecColumnWidthRatio[i];
	}

	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	return true;
}

bool CGui::DtcMultiAdd(INT pszContent, INT nCurColumn)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);

	char szBuf[100] = "";
	sprintf(szBuf, "%d", pszContent);
	WORD wDtcLen = (WORD)strlen(szBuf);

	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_DTC_MULTI;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)nCurColumn;

	m_pMapGuiBuff[6] = (BYTE)(wDtcLen >> 8);
	m_pMapGuiBuff[7] = (BYTE)wDtcLen;
	memcpy(m_pMapGuiBuff + 8, szBuf, wDtcLen);

//	strcpy(m_pMapGuiBuff + 6, szBuf);

	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	return true;
}

bool CGui::DtcMultiAdd(char * pszContent, INT nCurColumn)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);

	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_DTC_MULTI;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)0x00|0x1;
	m_pMapGuiBuff[5] = (BYTE)nCurColumn;

//	strcpy(m_pMapGuiBuff + 6, pszContent);
	WORD wDtcLen = (WORD)strlen(pszContent);
	m_pMapGuiBuff[6] = (BYTE)(wDtcLen >> 8);
	m_pMapGuiBuff[7] = (BYTE)wDtcLen;
	memcpy(m_pMapGuiBuff + 8, pszContent, wDtcLen);

	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	return true;
}

bool CGui::DtcMultiAdd(char * pszId, char * pszDtc, INT nCurColumn)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);

	char szIdBuff[100] = "";
	strcpy(szIdBuff, pszId);
	WORD wIdLen = 0;
	wIdLen = (WORD)strlen(szIdBuff);

	char szDtcBuff[1024] = "";
	strcpy(szDtcBuff, pszDtc);
	WORD wDtcLen = 0;
	wDtcLen = (WORD)strlen(szDtcBuff);

	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_DTC_MULTI;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)0x00|0x2;
	m_pMapGuiBuff[5] = (BYTE)nCurColumn;

	m_pMapGuiBuff[6] = (BYTE)(wIdLen >> 8);
	m_pMapGuiBuff[7] = (BYTE)wIdLen;
	memcpy(m_pMapGuiBuff + 8, szIdBuff, wIdLen);

	m_pMapGuiBuff[wIdLen + 9] = (BYTE)(wDtcLen >> 8);
	m_pMapGuiBuff[wIdLen + 10] = (BYTE)wDtcLen;
	memcpy(m_pMapGuiBuff + 11 + wIdLen, szDtcBuff, wDtcLen);

	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	return true;
}

BYTE CGui::DtcMulitShow(void)
{
	CloseDialogWait();

	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_DTC_MULTI;
	m_pMapGuiBuff[3] = (BYTE)0x03;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x01;
	m_pMapGuiBuff[6] = (BYTE)0xFE;
	m_pMapGuiBuff[7] = '\0';

	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}
		
	BYTE bKeyIndex = m_pMapGuiBuff[6];
	while (1)
	{
		Sleep(1);
		bKeyIndex = m_pMapGuiBuff[6];
		if (bKeyIndex != 0xFE)
		{
			break;
		}
	}
	return bKeyIndex;
}



//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////
// VER

bool CGui::VerInit()
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_VER;
	m_pMapGuiBuff[3] = (BYTE)0x01;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x00;
	m_pMapGuiBuff[6] = '\0';
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}

bool CGui::VerAdd(BYTE *pbId,BYTE bLen,char *pszText)
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	char szBuff[1024] = "";
	if (!GetTextEx(TEXT_TXT,pbId,bLen,szBuff))
	{
		for (BYTE i=0; i<bLen; i++)  //读不到则显示ID值
		{
			sprintf(szBuff+i*2,"%02X",pbId[i]);
		}
	}
	WORD wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_VER;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+6,szBuff,wLen);

	WORD wLenEx = (WORD)strlen(pszText);
	m_pMapGuiBuff[wLen+7] = (BYTE)(wLenEx>>8);
	m_pMapGuiBuff[wLen+8] = (BYTE)wLenEx;
	memcpy(m_pMapGuiBuff+wLen+9,pszText,wLenEx);

	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}
bool CGui::VerAdd(char *pszName,char *pszText)
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	char szBuff[1024] = "";
	strcpy(szBuff,pszName);
	WORD wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_VER;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+6,szBuff,wLen);

	WORD wLenEx = (WORD)strlen(pszText);
	m_pMapGuiBuff[wLen+7] = (BYTE)(wLenEx>>8);
	m_pMapGuiBuff[wLen+8] = (BYTE)wLenEx;
	memcpy(m_pMapGuiBuff+wLen+9,pszText,wLenEx);

	if (!SendMessage2MainForm_SendMessage())
		return false;
	
	return true;
}
BYTE CGui::VerShow()
{
	CloseDialogWait();
	
	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_VER;
	m_pMapGuiBuff[3] = (BYTE)0x03;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x01;
	m_pMapGuiBuff[6] = (BYTE)0xFE;
	m_pMapGuiBuff[7] = '\0';
	if (!SendMessage2MainForm_SendMessage())
		return false;
	BYTE bKeyIndex = m_pMapGuiBuff[6];
	while (1)
	{
		Sleep(1);
		bKeyIndex = m_pMapGuiBuff[6];
		if (bKeyIndex != 0xFE)
		{
			break;
		}
	}
	return bKeyIndex;
}

//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////
// List
bool CGui::ListInit(char *pszTitle)
{
	WORD wLen = (WORD)strlen(pszTitle);

	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_LIST;
	m_pMapGuiBuff[3] = (BYTE)0x01;
	m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[5] = (BYTE)(wLen>>0);
	memcpy(m_pMapGuiBuff+6,pszTitle,wLen);
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}
bool CGui::ListInit(BYTE *pbId,BYTE bLen)
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	char szBuff[1024] = "";
	if (!GetTextEx(TEXT_TXT,pbId,bLen,szBuff))
	{
		for (BYTE i=0; i<bLen; i++)
		{
			sprintf(szBuff+i*2,"%02X",pbId[i]);
		}
	}
	return ListInit(szBuff);
}
bool CGui::ListAdd(char *pszName,char *pszValue,DWORD dwColor)
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	char szBuff[1024] = "";
	strcpy(szBuff,pszName);
	WORD wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_LIST;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+6,szBuff,wLen);

	WORD wLenEx = (WORD)strlen(pszValue);
	m_pMapGuiBuff[wLen+7] = (BYTE)(wLenEx>>8);
	m_pMapGuiBuff[wLen+8] = (BYTE)wLenEx;
	memcpy(m_pMapGuiBuff+wLen+9,pszValue,wLenEx);

	m_pMapGuiBuff[wLen+9+wLenEx+1 + 0] = (BYTE)(dwColor>>0x18);
	m_pMapGuiBuff[wLen+9+wLenEx+1 + 1] = (BYTE)(dwColor>>0x10);
	m_pMapGuiBuff[wLen+9+wLenEx+1 + 2] = (BYTE)(dwColor>>0x08);
	m_pMapGuiBuff[wLen+9+wLenEx+1 + 3] = (BYTE)(dwColor>>0x00);

	if (!SendMessage2MainForm_SendMessage())
		return false;

	return true;
}
bool CGui::ListAdd(BYTE *pbId,BYTE bLen,BYTE *pbId1,BYTE bLen1,DWORD dwColor)
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	char szBuff[1024] = "";
	if (!GetTextEx(TEXT_TXT,pbId,bLen,szBuff))
	{
		for (BYTE i=0; i<bLen; i++)
		{
			sprintf(szBuff+i*2,"%02X",pbId[i]);
		}
	}
	char szBuff1[1024] = "";
	if (!GetTextEx(TEXT_TXT,pbId1,bLen1,szBuff1))
	{
		for (BYTE i=0; i<bLen; i++)
		{
			sprintf(szBuff1+i*2,"%02X",pbId1[i]);
		}
	}

	return ListAdd(szBuff,szBuff1,dwColor);
}
BYTE CGui::ListShow()
{
	CloseDialogWait();

	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_LIST;
	m_pMapGuiBuff[3] = (BYTE)0x03;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x01;
	m_pMapGuiBuff[6] = (BYTE)0xFE;
	m_pMapGuiBuff[7] = '\0';
	if (!SendMessage2MainForm_SendMessage())
		return false;
	BYTE bKeyIndex = (BYTE)m_pMapGuiBuff[3];
	while (1)
	{
		Sleep(1);
		bKeyIndex = (BYTE)m_pMapGuiBuff[3];
		if (bKeyIndex==MB_NO)
		{
			break;
		}
		if (bKeyIndex==MB_YES)
		{
			break;
		}
	}
	return bKeyIndex;
}



/*
bool CGui::ListInit(BYTE *pbId,BYTE bLen)
{
	return true;
}
bool CGui::ListInit(char *pszTitle)
{
	return true;
}
bool CGui::ListAdd(BYTE *pbId,BYTE bLen,BYTE *pbId1,BYTE bLen1,DWORD dwColor)
{
	return true;
}
bool CGui::ListAdd(char *pszName,char *pszValue,DWORD dwColor)
{
	return true;
}
BYTE CGui::ListShow(void)
{
	return MB_YES;
}
*/

//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////
// CDS SELECT

bool CGui::CdsSelectInit(bool bInitVar)
{
	if(g_bMyFlag != 2)
	{
		//MsgShowMessage("正在加载数据流,请稍后...",MSG_MB_NOBUTTON);
	}
	//Sleep(100);

	g_bMyFlag = 1;

	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_CDS_SELECT;
	m_pMapGuiBuff[3] = (BYTE)0x01;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x00;
	m_pMapGuiBuff[6] = bInitVar?0x01:0x02;
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}
bool CGui::CdsSelectInit(bool bInitVar, INT nColumnCdsSelect)
{
	if (g_bMyFlag != 2)
	{
		//		MsgShowMessage("正在加载数据流,请稍后...",MSG_MB_NOBUTTON);
	}
	//	Sleep(100);
	g_bMyFlag = 1;

	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_CDS_SELECT;
	m_pMapGuiBuff[3] = (BYTE)0x01;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x02;
	m_pMapGuiBuff[6] = bInitVar ? 0x01 : 0x02;
	m_pMapGuiBuff[7] = (BYTE)nColumnCdsSelect;
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}
bool CGui::CdsSelectInit(bool bInitVar, INT nColumnCdsSelect, INT nColumnCdsShow)
{
	if (g_bMyFlag != 2)
	{
		//		MsgShowMessage("正在加载数据流,请稍后...",MSG_MB_NOBUTTON);
	}
	//	Sleep(100);
	g_bMyFlag = 1;

	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_CDS_SELECT;
	m_pMapGuiBuff[3] = (BYTE)0x01;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x03;
	m_pMapGuiBuff[6] = bInitVar ? 0x01 : 0x02;
	m_pMapGuiBuff[7] = (BYTE)nColumnCdsSelect;
	m_pMapGuiBuff[8] = (BYTE)nColumnCdsShow;
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}
bool CGui::CdsSelectAdd(BYTE *pbId,BYTE bLen, BYTE *pUnit/*=NULL*/,BYTE bUnitLen/*=0*/)
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	//1.name
	char szBuff[1024] = "";
	if (!GetTextEx(TEXT_CDS,pbId,bLen,szBuff))
	{
		for (BYTE i=0; i<bLen; i++) //读不到则显示ID值
		{
			sprintf(szBuff+i*2,"%02X",pbId[i]);
		}
	}
	WORD wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_CDS_SELECT;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+6,szBuff,wLen);
	//2.Unit
	char szBuffEx[100] = "";
	if (pUnit == NULL && bUnitLen == 0)		//单位 放在CN_CDS.txt的第3列的情况
	{
		if (!GetTextEx(TEXT_CDS,pbId,bLen,szBuffEx,2))
		{
			//读不到ID不要把ID显示出来, 只显示空格(看不到).   --因为有变态程序单位是后加上去的(见数据流显示3个参数那种)
			strcpy(szBuffEx," ");
		}
		WORD wLenEx = (WORD)strlen(szBuffEx);
		m_pMapGuiBuff[wLen+7] = (BYTE)(wLenEx>>8);
		m_pMapGuiBuff[wLen+8] = (BYTE)wLenEx;
		memcpy(m_pMapGuiBuff+wLen+9,szBuffEx,wLenEx);
	}
	else									//单位 放在CN_TXT.txt的情况
	{
		if (!GetTextEx(TEXT_TXT,pUnit,bUnitLen,szBuffEx))
		{
			//读不到ID不要把ID显示出来, 只显示空格(看不到).   --因为有变态程序单位是后加上去的(见数据流显示3个参数那种)
			strcpy(szBuffEx," ");
		}
		WORD wLenEx = (WORD)strlen(szBuffEx);
		m_pMapGuiBuff[wLen+7] = (BYTE)(wLenEx>>8);
		m_pMapGuiBuff[wLen+8] = (BYTE)wLenEx;
		memcpy(m_pMapGuiBuff+wLen+9,szBuffEx,wLenEx);
	}
	
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}
bool CGui::CdsSelectAdd(char *pszName,char *pszUnit)
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	//1.name
	char szBuff[1024] = "";
	strcpy(szBuff,pszName);
	WORD wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_CDS_SELECT;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+6,szBuff,wLen);
	//2.Unit
	char szBuffEx[100] = "";
	strcpy(szBuffEx,pszUnit);
	WORD wLenEx = (WORD)strlen(szBuffEx);
	m_pMapGuiBuff[wLen+7] = (BYTE)(wLenEx>>8);
	m_pMapGuiBuff[wLen+8] = (BYTE)wLenEx;
	memcpy(m_pMapGuiBuff+wLen+9,szBuffEx,wLenEx);
	
	if (!SendMessage2MainForm_SendMessage())
		return false;

	return true;
}
BYTE CGui::CdsSelectShow()
{
	CloseDialogWait();

	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_CDS_SELECT;
	m_pMapGuiBuff[3] = (BYTE)0x03;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x01;
	m_pMapGuiBuff[6] = (BYTE)0xFE;
	m_pMapGuiBuff[7] = '\0';
	if (!SendMessage2MainForm_SendMessage())
		return false;
	BYTE bKeyIndex = (BYTE)m_pMapGuiBuff[6];
	while (1)
	{
		Sleep(1);
		bKeyIndex = (BYTE)m_pMapGuiBuff[6];
		if (bKeyIndex != 0xFE)
		{
			if (bKeyIndex == ID_CDS_VIEW)
			{
				m_pMapGuiBuff[6] = (BYTE)0x00;
			}
			break;
		}
	}
	return bKeyIndex;
}
BYTE CGui::CdsSelectGetItem(BYTE *pbSelectedItem) //BYTE最多只能选255个数据流
{
	CloseDialogWait();

	BYTE bSelectedNum = 0;
	BYTE bSelectedItem[300] = {0,};

	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_CDS_SELECT;
	m_pMapGuiBuff[3] = (BYTE)0x04;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x00;
	m_pMapGuiBuff[6] = '\0';
	if (!SendMessage2MainForm_SendMessage())
		return false;

	bSelectedNum = m_pMapGuiBuff[0];
	for (BYTE i=0; i<bSelectedNum; i++)
	{
		bSelectedItem[i] = m_pMapGuiBuff[i+1];
	}
	memcpy(pbSelectedItem,bSelectedItem,bSelectedNum);

	return bSelectedNum;
}
//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////
// CDS SHOW

bool CGui::CdsInit()
{
	//MsgShowMessage("正在读取数据流,请稍后...",MSG_MB_NOBUTTON);
	//Sleep(100);

	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_CDS_SHOW;
	m_pMapGuiBuff[3] = (BYTE)0x01;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x00;
	m_pMapGuiBuff[6] = '\0';
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}

bool CGui::CdsInit(INT nColumnCdsShow)
{
	//	MsgShowMessage("正在读取数据流,请稍后...",MSG_MB_NOBUTTON);
	//	Sleep(100);

	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_CDS_SHOW;
	m_pMapGuiBuff[3] = (BYTE)0x01;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x01;
	m_pMapGuiBuff[6] = (BYTE)nColumnCdsShow;
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}
//注:调用该函数必须是经过了选择数据流界面,否则不知道wIndex是什么.
//注:一般是不需要第三个参数(单位的),因为单位已经在数据流文本的第三列中了,但有些部分代码是把单位放到数据流显示的
//时候才加进来的单位(是从文本加进去的.), 所以这里pszUnit不为空则属于从CN_TXT.txt中加入单位的.
bool CGui::CdsAdd(WORD wIndex,char *pszValue, char *pszUnit/*=NULL*/)
{
	if ((BYTE)m_pMapGuiBuff[0xFFFF-1] == 0xFF) //这一步很关键,否则还没运行到CdsShow时共享内存又被清掉了,造成无法返回
	{
		return true;
	}

	//1.name
	memset(m_pMapGuiBuff,0,0xFFF0);
	WORD wLen = (WORD)strlen(pszValue);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_CDS_SHOW;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)wIndex;    //数据流索引
	m_pMapGuiBuff[5] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[6] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+7,pszValue,wLen);

	//2.unit
	if (pszUnit != NULL)
	{
		char szBuffEx[100] = "";
		strcpy(szBuffEx,pszUnit);
		WORD wLenEx = (WORD)strlen(szBuffEx);
		m_pMapGuiBuff[wLen+7] = (BYTE)(wLenEx>>8);
		m_pMapGuiBuff[wLen+8] = (BYTE)wLenEx;
		memcpy(m_pMapGuiBuff+wLen+9,szBuffEx,wLenEx);
	}

	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}
bool CGui::CdsAdd(BYTE *pbId, BYTE nLen, char *pszValue, char *pszUnit) //调用该函数可以不经过选择数据流界面, 显示程序自动判断重复ID
{
	char szCdsName[255] = "";
	if (!GetTextEx(TEXT_CDS,pbId,(WORD)nLen,szCdsName))return false;
	
	memset(m_pMapGuiBuff,0,0xFFF0);
	WORD wLen = (WORD)strlen(szCdsName);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_CDS_SHOW;
	m_pMapGuiBuff[3] = (BYTE)0x04 ;
	m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+6,szCdsName,wLen);

	WORD wLenEx = (WORD)strlen(pszValue);
	m_pMapGuiBuff[wLen+7] = (BYTE)(wLenEx>>8);
	m_pMapGuiBuff[wLen+8] = (BYTE)wLenEx;
	memcpy(m_pMapGuiBuff+wLen+9,pszValue,wLenEx);

	char szUnit[50] = "";
	if (pszUnit == NULL)
	{
		if (!GetTextEx(TEXT_CDS,pbId,(WORD)nLen,szUnit,2))return false;
	}
	else
	{
		strcpy(szUnit,pszUnit);
	}
	WORD wLenExEx = (WORD)strlen(szUnit);
	m_pMapGuiBuff[wLen+wLenEx+10] = (BYTE)(wLenExEx>>8);
	m_pMapGuiBuff[wLen+wLenEx+11] = (BYTE)wLenExEx;
	memcpy(m_pMapGuiBuff+wLen+wLenEx+12,szUnit,wLenExEx);

	if (!SendMessage2MainForm_SendMessage())
		return false;

	return true;
}
BYTE CGui::CdsShow()
{
	CloseDialogWait();

	if ((BYTE)m_pMapGuiBuff[0xFFFF-1] == 0xFF) //用最后一个值来当返回
	{
		m_pMapGuiBuff[0xFFFF-1] = 0x00; //将该值复位为0,等待下一次读数据流(显示程序点View按钮后会将该值设置为FF)

		//Sleep(200);
		//MsgShowMessage("正在退出数据流,请稍后...",MSG_MB_NOBUTTON);//MSG_MB_OK);
		//Sleep(500);
		g_bMyFlag = 2;

		return ID_MENU_BACK;
	}

	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_CDS_SHOW;
	m_pMapGuiBuff[3] = (BYTE)0x03;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x01;
	m_pMapGuiBuff[6] = (BYTE)0xFE;
	m_pMapGuiBuff[7] = '\0';
	if (!SendMessage2MainForm_SendMessage())
		return false;

	BYTE bKeyIndex = (BYTE)m_pMapGuiBuff[0xFFFF-1];
	return bKeyIndex;

//	BYTE bKeyIndex = m_pMapGuiBuff[6];
//	while (1)
//	{
//		Sleep(1);
//		bKeyIndex = m_pMapGuiBuff[6];
//		if (bKeyIndex != 0xFE)
//		{
//			break;
//		}
//	}
//	return bKeyIndex;

}
//////////////////////////////////////////////////////////////////////////
//检查数据流是否更新(主要是检查是否按了上一页或下一页的按钮)
//
bool CGui::CdsCheckUpdate()
{
	bool bUpdate = false;
	
	if ((BYTE)m_pMapGuiBuff[0xFFFF-2] == 0xFC) //用倒数第二个字节是否==0xFC来判断是否有上下页数据更新
	{
		m_pMapGuiBuff[0xFFFF-2] = 0x00; //复位该字节,等待下一次更新
		bUpdate = true;
	}

	return bUpdate;
}


//////////////////////////////////////////////////////////////////////////
// ACT
//
bool CGui::ActInit(void)
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_ACT;
	m_pMapGuiBuff[3] = (BYTE)0x01;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x00;
	m_pMapGuiBuff[6] = '\0';
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}

bool CGui::ActAddButton(BYTE *pbId,BYTE bLen,char *pszText,char bStatus)
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	//1.name
	char szBuff[1024] = "";
	if (!GetTextEx(TEXT_TXT,pbId,bLen,szBuff))
	{
		for (BYTE i=0; i<bLen; i++) //读不到则显示ID值
		{
			sprintf(szBuff+i*2,"%02X",pbId[i]);
		}
	}
	WORD wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_ACT;
	m_pMapGuiBuff[3] = (BYTE)0x04;			//01:Init 02:Add 03:Show 04:AddButton
	m_pMapGuiBuff[4] = (BYTE)bStatus;
	m_pMapGuiBuff[5] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[6] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+7,szBuff,wLen);

	if (!SendMessage2MainForm_SendMessage())
		return false;

	return true;
}
bool CGui::ActAddPrompt(BYTE *pbId,BYTE bLen)
{
	memset(m_pMapGuiBuff,0,0xFFF0);

	char szBuff[2048] = "";
	if (!GetTextEx(TEXT_TXT,pbId,bLen,szBuff))
	{
		for (BYTE i=0; i<bLen; i++) //读不到则显示ID值
		{
			sprintf(szBuff+i*2,"%02X",pbId[i]);
		}
	}
	
	return ActAddPrompt(szBuff);
}
bool CGui::ActAddPrompt(char *pszText)
{
	WORD wLen = (WORD)strlen(pszText);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_ACT;
	m_pMapGuiBuff[3] = (BYTE)0x05;			//01:Init 02:Add 03:Show 04:AddButton 05:ActAddPrompt
	m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+6,pszText,wLen);
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}

bool CGui::ActAddPrompt(const char *pszText)
{
	WORD wLen = (WORD)strlen(pszText);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_ACT;
	m_pMapGuiBuff[3] = (BYTE)0x05;			//01:Init 02:Add 03:Show 04:AddButton 05:ActAddPrompt
	m_pMapGuiBuff[4] = (BYTE)(wLen >> 8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff + 6, pszText, wLen);
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}

bool CGui::ActAdd(char *pszName,char *pszValue,char *pszUnit)
{
	memset(m_pMapGuiBuff,0,0xFFF0);
	//1.name
	char szBuff[500] = "";
	strcpy(szBuff,pszName);
	WORD wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_ACT;
	m_pMapGuiBuff[3] = (BYTE)0x02;
	m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+6,szBuff,wLen);
	//2.Value
	char szBuffEx[200] = "";
	strcpy(szBuffEx,pszValue);
	WORD wLenEx = (WORD)strlen(szBuffEx);
	m_pMapGuiBuff[wLen+7] = (BYTE)(wLenEx>>8);
	m_pMapGuiBuff[wLen+8] = (BYTE)wLenEx;
	memcpy(m_pMapGuiBuff+wLen+9,szBuffEx,wLenEx);
	//3.Unit
	char szBuffExEx[50] = "";
	strcpy(szBuffExEx,pszUnit);
	WORD wLenExEx = (WORD)strlen(szBuffExEx);
	m_pMapGuiBuff[wLen+wLenEx+10] = (BYTE)(wLenExEx>>8);
	m_pMapGuiBuff[wLen+wLenEx+11] = (BYTE)wLenExEx;
	memcpy(m_pMapGuiBuff+wLen+wLenEx+12,szBuffExEx,wLenExEx);
	
	if (!SendMessage2MainForm_SendMessage())
		return false;

	return true;
}

BYTE CGui::ActShow(void)
{ 
	CloseDialogWait();

	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_ACT;
	m_pMapGuiBuff[3] = (BYTE)0x03;
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x01;
	m_pMapGuiBuff[6] = '\0';
	if (!SendMessage2MainForm_SendMessage())
		//return false;
		return 0xFE;
	BYTE bKeyIndex = (BYTE)m_pMapGuiBuff[0xFFFF-4]; //用倒数第4个来当返回值,0-254按钮索引 255:默认返回

	if (bKeyIndex >= 10) //最多容纳10个按钮
	{
		return -1; //-1
	}

	m_pMapGuiBuff[0xFFFF-4] = (char)0xFE; //复位,后等待下次按键
	
	Sleep(1); //这里有必要延迟一下,因为整个动作测试都运行在循环中,一直占着资源
	return bKeyIndex;
}









//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////
// MessageBox SHOW
BYTE CGui::MsgShowMessage(BYTE *pbMessageId,BYTE bMsgLen,BYTE bMode)
{
	if (bMode != MSG_MB_NOBUTTON)
	{
		//把这里屏蔽掉是为了NO_BUTTON状态时候用来多次频繁显示,如果百分比. 目的是减少闪烁
		CloseDialogWait();
	}
	
	memset(m_pMapGuiBuff,0,0xFFF0);
	char szBuff[1024] = "";
	if (!GetTextEx(TEXT_TXT,pbMessageId,bMsgLen,szBuff))
	{
		for (BYTE i=0; i<bMsgLen; i++) //读不到则显示ID值
		{
			sprintf(szBuff+i*2,"%02X",pbMessageId[i]);
		}
	}

	return MsgShowMessage(szBuff,bMode);
}

BYTE CGui::MsgShowMessage(char *pszMessage,BYTE bMode, DWORD dwColor)  //这个函数是可以和其同名函数合并的(尚未合并)
{
	if (bMode != MSG_MB_NOBUTTON)
	{
		//把这里屏蔽掉是为了NO_BUTTON状态时候用来多次频繁显示,如果百分比. 目的是减少闪烁
		CloseDialogWait();
	}
	
	memset(m_pMapGuiBuff,0,0xFFF0);
	char szBuff[1024] = "";
	strcpy(szBuff,pszMessage);
	//WORD bMsgLen = strlen(szBuff);

	WORD wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_MSG;
	m_pMapGuiBuff[3] = (BYTE)bMode;		//bMode==MSG_MB_OK|MSG_MB_YESNO|MSG_MB_NOBUTTON
	m_pMapGuiBuff[4] = (BYTE)(wLen>>8);		//MessageID的长度
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+6,szBuff,wLen);

	m_pMapGuiBuff[wLen + 6 + 1] = (BYTE)(dwColor >> 0x18);
	m_pMapGuiBuff[wLen + 6 + 2] = (BYTE)(dwColor >> 0x10);
	m_pMapGuiBuff[wLen + 6 + 3] = (BYTE)(dwColor >> 0x08);
	m_pMapGuiBuff[wLen + 6 + 4] = (BYTE)(dwColor >> 0x00);

//	if (!SendMessage2MainForm_SendMessage())  //这里不能使用SendMessage否则造成阻塞！
//		return false;
	if (!SendMessage2MainForm_PostMessage())
		return false;
	
	BYTE bKeyIndex = (BYTE)m_pMapGuiBuff[3];
	if (bMode==MSG_MB_OK || bMode==MSG_MB_YESNO)
	{
		while (1)
		{
			Sleep(1);
			bKeyIndex = (BYTE)m_pMapGuiBuff[3];
			if (bKeyIndex==MB_YES || bKeyIndex==MB_NO)
			{
				break;
			}
		}
	}
	return bKeyIndex;
}

void CGui::MsgReflashProgress(BYTE *pszMessage,BYTE bMsgLen){
	::MsgReflashProgressAdaptee(pszMessage,bMsgLen);
}

BYTE CGui::MsgShowMessage(char *pszMessage,char *pszBmpPath,BYTE bMode)
{
    CloseDialogWait();

    memset(m_pMapGuiBuff,0,0xFFF0);
    m_pMapGuiBuff[0] = (BYTE)0x55;
    m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
    m_pMapGuiBuff[2] = (BYTE)FORM_MSG;
    m_pMapGuiBuff[3] = (BYTE)bMode + 0x10;

    char szBuff[1024] = "";
    strcpy(szBuff,pszMessage);
    WORD wLen = strlen(szBuff);
    m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
    m_pMapGuiBuff[5] = (BYTE)wLen;
    memcpy(m_pMapGuiBuff+6,szBuff,wLen);

    memset(szBuff,0,1024);
    strcpy(szBuff,pszBmpPath);
    wLen = strlen(szBuff);
    m_pMapGuiBuff[2000] = (BYTE)(wLen>>8);
    m_pMapGuiBuff[2001] = (BYTE)wLen;
    memcpy(m_pMapGuiBuff+2002,szBuff,wLen);

    if (!SendMessage2MainForm_PostMessage())
        return false;

    BYTE bKeyIndex = (BYTE)m_pMapGuiBuff[3];
    if (bMode==MSG_MB_OK || bMode==MSG_MB_YESNO)
    {
        while (1)
        {
            Sleep(1);
            bKeyIndex = (BYTE)m_pMapGuiBuff[3];
            if (bKeyIndex==MB_YES || bKeyIndex==MB_NO)
            {
                break;
            }
        }
    }
    return bKeyIndex;
}
void CGui::MsgTitleText(char *pszTitleText)
{
    memset(m_pMapGuiBuff,0,0xFFF0);
    m_pMapGuiBuff[0] = (BYTE)0x55;
    m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
    m_pMapGuiBuff[2] = (BYTE)FORM_TITLE;
    m_pMapGuiBuff[3] = (BYTE)0x01; //传输的是字符串
    char szBuff[1024] = "";
    strcpy(szBuff,pszTitleText);
    WORD wLen = strlen(szBuff);
    m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
    m_pMapGuiBuff[5] = (BYTE)wLen;
    memcpy(m_pMapGuiBuff+6,szBuff,wLen);

    if (!SendMessage2MainForm_PostMessage())
        return ;
}

//根据id把诊断目录下的menu.txt 对应的名称展示在标题栏上
void CGui::MsgTitleText(DWORD dwTaskId)
{
    memset(m_pMapGuiBuff,0,0xFFF0);
    m_pMapGuiBuff[0] = (BYTE)0x55;
    m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
    m_pMapGuiBuff[2] = (BYTE)FORM_TITLE;
    m_pMapGuiBuff[3] = (BYTE)0x02; //传输的是TASK ID
    m_pMapGuiBuff[4] = (BYTE)(dwTaskId>>0x18);
    m_pMapGuiBuff[5] = (BYTE)(dwTaskId>>0x10);
    m_pMapGuiBuff[6] = (BYTE)(dwTaskId>>0x08);
    m_pMapGuiBuff[7] = (BYTE)(dwTaskId>>0x00);

    if (!SendMessage2MainForm_PostMessage())
        return ;
}

//根据id把诊断目录下的menu.txt 对应的名称传到此处
void CGui::MsgTitleText(DWORD dwTaskId, string & strMenuText)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_TITLE;
	m_pMapGuiBuff[3] = (BYTE)0x03; //传输的是TASK ID,并且要求显示程序把菜单名称返回给GUI接口
	m_pMapGuiBuff[4] = (BYTE)(dwTaskId >> 0x18);
	m_pMapGuiBuff[5] = (BYTE)(dwTaskId >> 0x10);
	m_pMapGuiBuff[6] = (BYTE)(dwTaskId >> 0x08);
	m_pMapGuiBuff[7] = (BYTE)(dwTaskId >> 0x00);
	m_pMapGuiBuff[8] = 0x01;
	if (!SendMessage2MainForm_PostMessage())
	{
		return;
	}

	while (1)
	{
		Sleep(1);

		if (m_pMapGuiBuff[8] == 0)
		{

			char szBuff[4028] = {0};
			if (szBuff)
			{
				strcpy(szBuff, m_pMapGuiBuff + 11);

				strMenuText = szBuff;

				break;
			}
		}
	}
}


//////////////////////////////////////////////////////////////////////////
// 发消息给等待窗口执行关闭窗口指令
//
// 【在需要的地方都要调用此函数，即使等待窗口没有启动也可以调用】

void CGui::CloseDialogWait(void)
{
	DWORD t0,t1;
	t0 = GetTickCount();

	while (1)
	{
		t1 = GetTickCount();
		if (t1 - t0 > 3*1000)
		{
			break;
		}
		HWND hWnd = NULL;
		hWnd = ::FindWindow(NULL,_T("Dialog_Wait"));  //如果为NULL即等待窗口没有启动则什么也不做直接返回

		if (hWnd != NULL)
		{
			//这里不能使用SendMessage否则造成阻塞！
			::PostMessage(hWnd,MSG_WAIT_EPA2015,MSG_WAIT_WINDOW_CLOSE,0);
			break;
		}
		else
		{

			break;
		}
		Sleep(1);
	}
}



BYTE CGui::MsgInputBox(BYTE *pbMsg,BYTE bMsgLen,BYTE *pbTip,BYTE bTipLen,BYTE bMode,char *pszMin,char *pszMax)
{
	char szMsg[1024],szTip[1024];
	if (!GetText(pbMsg,bMsgLen,szMsg))return 0;
	if (!GetText(pbTip,bTipLen,szTip))return 0;

	return MsgInputBox(szMsg,szTip,bMode,pszMin,pszMax);
}

BYTE CGui::MsgInputBox(char *pszMsg,char *pszTip,BYTE bMode,char *pszMin,char *pszMax)
{
	CloseDialogWait();

	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_INPUT;
	m_pMapGuiBuff[3] = bMode;
	

	char szBuff[1024] = {0,};
	strcpy(szBuff,pszMsg);
	WORD wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+6,szBuff,wLen);

	memset(szBuff,0,1024);
	strcpy(szBuff,pszTip);
	wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[2000] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[2001] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+2002,szBuff,wLen);

	memset(szBuff,0,1024);
	strcpy(szBuff,pszMin);
	wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[4000] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[4001] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+4002,szBuff,wLen);

	memset(szBuff,0,1024);
	strcpy(szBuff,pszMax);
	wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[6000] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[6001] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+6002,szBuff,wLen);


	if (!SendMessage2MainForm_PostMessage())
		return false;
	
	//发送完毕,等待返VDI的inputbox窗口返回
	
	BYTE bKeyIndex = (BYTE)m_pMapGuiBuff[3];
//	if (bMode==MSG_MB_OK || bMode==MSG_MB_YESNO)
	{
		while (1)
		{
			Sleep(1);
			bKeyIndex = (BYTE)m_pMapGuiBuff[3];
			if (bKeyIndex==MB_NO)
			{
				break;
			}
			if (bKeyIndex==MB_YES)
			{
				strcpy(m_szInputText,m_pMapGuiBuff+6);
				break;
			}
		}
	}
	return bKeyIndex;
}

void CGui::MsgInputGetText(char *pszInputText)
{
	strcpy(pszInputText,m_szInputText);
}

void CGui::FileParam(char *szP, char *szV, char *szHP, char *szMemo)
{
	CloseDialogWait();

	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_FILEPARAM;
	m_pMapGuiBuff[3] = (BYTE)0; //reserve

	char szBuff[1024] = "";
	strcpy(szBuff, szP);
	WORD wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[4] = (BYTE)(wLen >> 8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff + 6, szBuff, wLen);

	memset(szBuff, 0, 1024);
	strcpy(szBuff, szV);
	wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[100] = (BYTE)(wLen >> 8);
	m_pMapGuiBuff[101] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff + 102, szBuff, wLen);

	memset(szBuff, 0, 1024);
	strcpy(szBuff, szHP);
	wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[200] = (BYTE)(wLen >> 8);
	m_pMapGuiBuff[201] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff + 202, szBuff, wLen);

	memset(szBuff, 0, 1024);
	strcpy(szBuff, szMemo);
	wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[300] = (BYTE)(wLen >> 8);
	m_pMapGuiBuff[301] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff + 302, szBuff, wLen);

	if (!SendMessage2MainForm_PostMessage())
		return;
	//发过去就可以了，不要求回执。
}
BYTE CGui::FileDialog(bool bOpen,char *szFilter,char *szPath)
{
	CloseDialogWait();

    LOGD(szPath);

    string strPath = szPath;
    string strRootPath;
    strRootPath = GetRootPath();  // "/storage/emulated/0/zdeps/"

    string strRootPathEx;
    strRootPathEx = GetRootPathEx(); // "/storage/emulated/0/"

    int nPos = -1;
    nPos = strPath.find(strRootPath);

    string strAbsolutePath;
    if (nPos == -1)   //找不到字符串
    {
        nPos = strPath.find(strRootPathEx);
        if(nPos == -1) //找不到字符串
        {
            strAbsolutePath = strRootPath + strPath;
        }
        else //找到字符串
        {
            strPath.replace(0, strRootPathEx.length(), strRootPath);

            strAbsolutePath = strPath;
        }
    }
    else //找到字符串
    {
        strAbsolutePath = strPath;
    }

	memset(m_pMapGuiBuff,0,0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_FILEDIALOG;
	m_pMapGuiBuff[3] = (BYTE)bOpen;

	char szBuff[1024] = "";
	strcpy(szBuff,szFilter);
	WORD wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[4] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+6,szBuff,wLen);

	memset(szBuff,0,1024);
	strcpy(szBuff, strAbsolutePath.c_str());
	wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[1000] = (BYTE)(wLen>>8);
	m_pMapGuiBuff[1001] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff+1002,szBuff,wLen);

	if (!SendMessage2MainForm_PostMessage())
		return false;

	BYTE bKeyIndex = (BYTE)m_pMapGuiBuff[3];
	while (1) 
	{
		Sleep(1);
		bKeyIndex = (BYTE)m_pMapGuiBuff[3];
		if (bKeyIndex==MB_NO)
		{
			break;
		}
		if (bKeyIndex==MB_YES)
		{
			strcpy(m_szFileDialog,m_pMapGuiBuff+2000);
			break;
		}
	}
	return bKeyIndex;
}

bool CGui::FileHandleFinish(int nFinishFlag, char * szPath)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_FILE_FINISH;
	m_pMapGuiBuff[3] = (BYTE)nFinishFlag;

	char szBuff[1024] = "";
	strcpy(szBuff, szPath);
	WORD wLen = (WORD)strlen(szPath);
	m_pMapGuiBuff[4] = (BYTE)(wLen >> 8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	m_pMapGuiBuff[6] = (BYTE)0xFE;

	memcpy(m_pMapGuiBuff + 7, szBuff, wLen);

	if (!SendMessage2MainForm_PostMessage())
	{
		return false;
	}

	//其实可以不用等待的，现在测试没什么问题，等到后面再处理。
	BYTE bKeyIndex = m_pMapGuiBuff[6];
	while (1)
	{
		Sleep(1);
		bKeyIndex = m_pMapGuiBuff[6];    //显示程序处理完消息后,给共享内存赋值

		if (bKeyIndex != 0xFE)
		{
			break;
		}
	}

	return true;
}






//////////////////////////////////////////////////////////////////////////
// 发送消息给VDI,告知任务ID 已启动 或 已关闭
// 说明:主要是让VDI知道什么时候处理menu.txt的菜单,什么时候处理诊断程序的菜单
//

bool CGui::SendMessage_TaskIdRunning(bool bRunning,WORD iTaskID)
{
	//最后退出系统没地方处理关闭,这里特别处理关闭
	DWORD t0,t1;
	t0 = GetTickCount();
	while (1)
	{
		t1 = GetTickCount();
		if (t1 - t0 > 500)
		{
			break;
		}
		HWND hWnd = NULL;
		hWnd = ::FindWindow(NULL,"Dialog_Wait");
		if (hWnd != NULL)
		{
			::PostMessage(hWnd,MSG_WAIT_EPA2015,MSG_WAIT_WINDOW_CLOSE,0);
			break;
		}
		else
		{
			break;
		}
	}

	
	
	if (m_hMainWnd == NULL)return false;
	HRESULT hr = 0;
	if (bRunning)
	{
		hr = ::PostMessage(m_hMainWnd,MSG_MAIN_EPA2015,PARAM_GUI_TASK_ID_BEGIN,iTaskID);
	}
	else
	{
		hr = ::PostMessage(m_hMainWnd,MSG_MAIN_EPA2015,PARAM_GUI_TASK_ID_END,iTaskID);
	}
	if (hr == 0)return false;
	return true;
}











//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////




bool CGui::GetTextCommand(BYTE bHead,BYTE *pbId,WORD wLen,vector<CBinary> &vecbinCmd)
{
	DWORD dwBeginAddr = 0;
	DWORD dwEndAddr = 0;
	BYTE bAddrStartPos = 0x00;

	vecbinCmd.clear();

	switch (bHead)
	{
	case TEXT_TXT:
		bAddrStartPos = 32;
		break;
	case TEXT_DTC:
		bAddrStartPos = 32+4+4;
		break;
	case TEXT_CDS:
		bAddrStartPos = 32+4+4+4+4;
		break;
	case TEXT_OTHER:
		bAddrStartPos = 32+4+4+4+4+4+4;
		break;
	default:
		return false;
	}
	fseek(m_pfData,bAddrStartPos,SEEK_SET); //指针偏移到要读取的地址开始
	fread(&dwBeginAddr,sizeof(DWORD),1,m_pfData);
	fread(&dwEndAddr,sizeof(DWORD),1,m_pfData);
	fseek(m_pfData,dwBeginAddr,SEEK_SET);
	m_dwCurrentAddr = dwBeginAddr;

	WORD wSize = 0;
	BYTE nBuff[1024] = {0,};
	bool bIdFound = false;
	int i = 0;
	BYTE b = 0;

	while (1)
	{
		if (fread(&wSize,sizeof(WORD),1,m_pfData) != 1)break;
		m_dwCurrentAddr += 2;
		if (wLen != wSize)
		{
			fseek(m_pfData,wSize,SEEK_CUR);
			m_dwCurrentAddr += wSize;
			if (fread(&wSize,sizeof(WORD),1,m_pfData) != 1)break;
			m_dwCurrentAddr += 2;
			fseek(m_pfData,wSize,SEEK_CUR);
			m_dwCurrentAddr += wSize;
		}
		else
		{
			if (fread(nBuff,sizeof(char),wLen,m_pfData) != wLen)break;
			m_dwCurrentAddr += wLen;
			if (memcmp(pbId,nBuff,wLen) == 0) //找到了对应的ID
			{
				WORD wAllLen = 0;
				WORD wOneLen = 0;
				WORD wLen = wOneLen;
				if (fread(&wAllLen,sizeof(WORD),1,m_pfData) != 1)break;
				m_dwCurrentAddr += 2;
				if (fread(&wOneLen,sizeof(WORD),1,m_pfData) != 1)break;
				m_dwCurrentAddr += 2;

				wLen = wOneLen+2;

				CBinary binCmd = 0;
				//第一个内容
				memset(nBuff,0,1024);
				if (fread(nBuff,sizeof(char),wOneLen,m_pfData) != wOneLen)break;
				m_dwCurrentAddr += wOneLen;
//				vecString.push_back((char *)nBuff);
				String2Binary((char *)nBuff,binCmd);
				vecbinCmd.push_back(binCmd);

				//剩下的内容
				while (wLen < wAllLen)
				{
					if (fread(&wOneLen,sizeof(WORD),1,m_pfData) != 1)break;
					m_dwCurrentAddr += 2;

					memset(nBuff,0,1024);
					if (fread(nBuff,sizeof(char),wOneLen,m_pfData) != wOneLen)break;
					m_dwCurrentAddr += wOneLen;
//					vecString.push_back((char *)nBuff);
					String2Binary((char *)nBuff,binCmd);
					vecbinCmd.push_back(binCmd);

					wLen += 2;
					wLen += wOneLen;
				}
				return true;
			}
			else
			{
				if (fread(&wSize,sizeof(WORD),1,m_pfData) != 1)break;
				m_dwCurrentAddr += 2;
				fseek(m_pfData,wSize,SEEK_CUR);
				m_dwCurrentAddr += wSize;
			}
		}
		
		if (m_dwCurrentAddr >= dwEndAddr)	//超过要读取的地址
			break;

	}

	return false;
}

void CGui::String2Binary(string str,CBinary &binary)
{
	CBinary binCmd;
	char szBuff[5] = "";
	BYTE nLen = 0;
	for (WORD w=0; w<str.size(); w++)
	{
		if (str[w] == ',')
		{
			if (szBuff[0]=='0' && (szBuff[1]=='x' || szBuff[1]=='X'))
			{
				binCmd.Add((BYTE)StrToHex(szBuff+2));
			}
			else
			{
				binCmd.Add((BYTE)StrToHex(szBuff));
			}

			memset(szBuff,0,5);
			nLen = 0;
			continue;
		}
		szBuff[nLen++] = str[w];
	}
	if (szBuff[0]=='0' && (szBuff[1]=='x' || szBuff[1]=='X'))
	{
		binCmd.Add((BYTE)StrToHex(szBuff+2));
	}
	else
	{
		binCmd.Add((BYTE)StrToHex(szBuff));
	}
	binary = binCmd;
}






string CGui::GetFileName(WORD iTaskID)
{
	string strRootPath,strAbsolutePath,strName,strTemp;
	char szTemp[100];
	strRootPath = GetRootPathEx();
	strAbsolutePath = GetAbsolutePath();;
	strName = GetRelativePath(strAbsolutePath,strRootPath);
	strName = ReplaceChar(strName,SPLIT,'_');
	sprintf(szTemp,"%04X_",iTaskID);
	strName += szTemp;
	strTemp = GetLocalDateTime() + "_";
	strName += strTemp;
	strTemp = GetSidFromIniFile();
	strName += strTemp;
	strName += ".log";
	return strName;
}
string CGui::GetFilePathName(WORD iTaskID)
{
	string strPath = GetRootPathEx() + "log/";
	strPath += GetFileName(iTaskID);
	return strPath;
}


string CGui::GetSidFromIniFile()
{
	string strSid;
	char szTemp[100];
	string strPath = GetRootPathEx() + "main.ini";
	GetPrivateProfileString("SYSTEM","SID","102601010018",szTemp,100,(char *)strPath.c_str());
	strSid = szTemp;
	return strSid;
}
string CGui::GetLocalDateTime()
{
	string strDateTime;
	char szTemp[255];
	SYSTEMTIME st;
	GetLocalTime(&st);
	sprintf(szTemp,"%04d%02d%02d%02d%02d%02d%03d",st.wYear,st.wMonth,st.wDay,st.wHour,st.wMinute,st.wSecond,st.wMilliseconds);
	strDateTime = szTemp;
	return strDateTime;
}
string CGui::ReplaceChar(string str,char a,char b) //str中含有a的都替换成b
{
	string strResult;
	char szTemp[MAX_PATH] = "";
	int i = 0;
	strcpy(szTemp,(char *)str.c_str());
	for (i=0; i<strlen(szTemp); i++)
	{
		if (szTemp[i] == a)
		{
			szTemp[i] = b;
		}
	}
	strResult = szTemp;
	return strResult;
}

string CGui::GetRelativePath(string strAbsolutePath,string strRootPath) //diagnose.exe的路径 [减去-] VDI路径
{
	string strPath = "";
	char szPath[MAX_PATH] = "";
	int nLen = 0;
	int i = 0;
	int nIndex = strAbsolutePath.find((char *)strRootPath.c_str());
	if (nIndex == -1)return strPath;
	for (i=strlen((char *)strRootPath.c_str()); i<strlen((char *)strAbsolutePath.c_str()); i++)
	{
		szPath[nLen++] = ((char *)strAbsolutePath.c_str())[i];
	}
	szPath[nLen] = '\0';
	strPath = szPath;
	return strPath;
}
string CGui::GetAbsolutePath() //diagnose.exe的路径
{
	string strPath = "";
	char szPath[MAX_PATH]={0};
	int i = 0;
	GetModuleFileName(NULL,szPath,MAX_PATH);
	for (i=strlen(szPath)-1; i>0; i--)
	{
		if (szPath[i] == SPLIT)
		{
			szPath[i] = '\0';
			break;
		}
	}
	if (i < 0)return strPath;
	
	strPath = szPath;
	
	return strPath;
}

string CGui::GetCurrentPath() 
{

    string strPath = "";
    char szPath[MAX_PATH] ={0,};

    guiGetCurrentPath(szPath,MAX_PATH);

    strPath = szPath;
    strPath.append("/");
    return strPath;
}

string CGui::GetRootPathEx()
{
	string strPath;
	int i = 0;
	string strRootPath = GetRootPath();
	char szPath[MAX_PATH] = "";
	strcpy(szPath,(char *)strRootPath.c_str());
	for (i=strlen(szPath)-1-1; i>0; i--)
	{
		if (szPath[i] == SPLIT)
		{
			szPath[i+1] = '\0';
			break;
		}
	}
	if (i < 0)return strPath;
	strPath = szPath;
	return strPath;
}

string CGui::GetRootPath()
{
	string strPath = "";
	if (m_hMainWnd == NULL)	return strPath;
	
	memset(m_pMapGuiBuff,0,0xFFF0);  //后面16个低字节用作特殊用途
	::SendMessage(m_hMainWnd,MSG_MAIN_EPA2015,PARAM_GUI_ROOT_PATH,0);
	WORD w;
	for (w=0; w<30000; w++)
	{
		Sleep(1);
		if (strlen(m_pMapGuiBuff) > 3)
		{
			break;
		}
	}
	if (w >= 30000)return "";
	strPath = m_pMapGuiBuff;

	return strPath;
}

bool CGui::CreateFileByPath(char * szPath)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_CREATEFILE;
	m_pMapGuiBuff[3] = (BYTE)0xFE;

	char szBuff[1024] = "";
	strcpy(szBuff, szPath);
	WORD wLen = (WORD)strlen(szBuff);
	m_pMapGuiBuff[4] = (BYTE)(wLen >> 8);
	m_pMapGuiBuff[5] = (BYTE)wLen;
	memcpy(m_pMapGuiBuff + 6, szBuff, wLen);

	if (!SendMessage2MainForm_PostMessage())
	{
		return false;
	}
		
	BYTE bKeyIndex = m_pMapGuiBuff[3];
	while (1)
	{
		Sleep(1);
		bKeyIndex = m_pMapGuiBuff[3];
		if (bKeyIndex != 0xFE)
		{
			break;
		}
	}

	if (m_pMapGuiBuff[3] == 0x7F)
	{
		return false;
	}
	else if (m_pMapGuiBuff[3] == 0x7E)
	{
		return true;
	}
	
	return true;
}

bool CGui::SendDiagnoseQuitMsg2Display()
{
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_QUIT;
	m_pMapGuiBuff[3] = (BYTE)0x01; //01表示退出
	if (!SendMessage2MainForm_SendMessage())
		return false;
	return true;
}

DWORD CGui::GetPinAD(BYTE bPin)
{
	DWORD dwAD = 0x7FFFFFFF;
	BYTE nCmd[7] = {0xA5,0xA5,0x00,0x02,0xF4,0xCC,0x55};
	BYTE nRecv[10] = {0,};
	nCmd[5] = bPin;
	g_Com.SendCmd(nCmd,7);
	if (!g_Com.RecvCmdEx(nRecv,10))return dwAD;
	if (nRecv[0] != 0xA5)return dwAD;
	if (nRecv[1] != 0xA5)return dwAD;
	if (nRecv[2] != 0x00)return dwAD;
	if (nRecv[3] != 0x05)return dwAD;
	if (nRecv[4] != 0xF4)return dwAD;
	if (nRecv[5] != 0x00)return dwAD;
	if (nRecv[9] != 0x55)return dwAD;
	dwAD = (DWORD)nRecv[5]<<24|nRecv[6]<<16|nRecv[7]<<8|nRecv[8];
	return dwAD;
}
BYTE CGui::SetMultiTaskBegin(DWORD nCalcType, DWORD nSystemName)
{
	BYTE nSend[16] = { 0xA5,0xA5,0x00,0x0B,0xFE,0xA0,0x01,0xC1,0xC2,0xC3,0xC4,0xD1,0xD2,0xD3,0xD4,0x55 };
	BYTE nRecv[9] = { 0, };
	nSend[7] = (BYTE)(nCalcType >> 0x18);
	nSend[8] = (BYTE)(nCalcType >> 0x10);
	nSend[9] = (BYTE)(nCalcType >> 0x08);
	nSend[10] = (BYTE)(nCalcType >> 0x00);
	nSend[11] = (BYTE)(nSystemName >> 0x18);
	nSend[12] = (BYTE)(nSystemName >> 0x10);
	nSend[13] = (BYTE)(nSystemName >> 0x08);
	nSend[14] = (BYTE)(nSystemName >> 0x00);
	g_Com.SendCmd(nSend, 16);
	if (!g_Com.RecvCmdEx(nRecv, 9))return 1;
	if (nRecv[0] != 0xA5)return 2;
	if (nRecv[1] != 0xA5)return 3;
	if (nRecv[2] != 0x00)return 4;
	if (nRecv[3] != 0x03)return 5;
	if (nRecv[4] != 0xFE)return 6;
	if (nRecv[5] != 0xA0)return 7;
	if (nRecv[6] != 0x01)return 8;
	if (nRecv[7] != 0x34)return 9;
	if (nRecv[8] != 0x55)return 10;
	return 0;
}
BYTE CGui::SetMultiTaskEnd()
{
	BYTE nSend[8] = { 0xA5,0xA5,0x00,0x03,0xFE,0xA0,0x00,0x55 };
	BYTE nRecv[9] = { 0, };
	g_Com.SendCmd(nSend, 8);
	if (!g_Com.RecvCmdEx(nRecv, 9))return 1;
	if (nRecv[0] != 0xA5)return 2;
	if (nRecv[1] != 0xA5)return 3;
	if (nRecv[2] != 0x00)return 4;
	if (nRecv[3] != 0x03)return 5;
	if (nRecv[4] != 0xFE)return 6;
	if (nRecv[5] != 0xA0)return 7;
	if (nRecv[6] != 0x00)return 8;
	if (nRecv[7] != 0x34)return 9;
	if (nRecv[8] != 0x55)return 10;
	return 0;
}
WORD CGui::CRC16CCITT(BYTE * pszBuf, WORD unLength)
{

	DWORD i, j;
	WORD CrcReg = 0xFFFF;
	WORD CurVal;

	for (i = 0; i < unLength; i++)
	{
		CurVal = pszBuf[i] << 8;

		for (j = 0; j < 8; j++)
		{
			if ((short)(CrcReg ^ CurVal) < 0)
				CrcReg = (CrcReg << 1) ^ 0x1021;
			else
				CrcReg <<= 1;
			CurVal <<= 1;
		}
	}

	return CrcReg;
}
bool CGui::WriteSram(BYTE *pBuf, DWORD dwLen)
{
	BYTE nSendBuf[10000 + 100] = { 0, };
	WORD nSendLen = 0;
	BYTE nRecvBuf[100] = { 0, };
	WORD crc = 0;

	//A5 A5 00 08 FC 01 00 00 00 06 F2 5B 55
	//A5 A5 00 02 FC 07 55
	memcpy(nSendBuf, (BYTE *)"\xA5\xA5\x00\x08\xFC\x01\xC1\xC2\xC3\xC4\xD1\xD2\x55", 13);
	nSendLen = 13;
	nSendBuf[6] = (BYTE)(dwLen >> 0x18);
	nSendBuf[7] = (BYTE)(dwLen >> 0x10);
	nSendBuf[8] = (BYTE)(dwLen >> 0x08);
	nSendBuf[9] = (BYTE)(dwLen >> 0x00);
	crc = (WORD)CRC16CCITT(nSendBuf + 2, nSendLen - 5);
	nSendBuf[10] = (BYTE)(crc >> 0x08);
	nSendBuf[11] = (BYTE)(crc >> 0x00);
	g_Com.SendCmd(nSendBuf, nSendLen);
	if ((!g_Com.RecvCmdEx(nRecvBuf, 7)) || (nRecvBuf[5] != 0x07))
	{
		return false;
	}

	//trasfer
	DWORD dwEachCount = 10000;
	DWORD dwFrame = dwLen / dwEachCount;
	DWORD dwLeft = dwLen % dwEachCount;
	DWORD i;
	DWORD pBufIndex = 0;
	for (i = 0; i < dwFrame; i++)
	{
		nSendBuf[0] = 0xA5;
		nSendBuf[1] = 0xA5;
		nSendBuf[2] = (BYTE)((dwEachCount + 4) >> 8);
		nSendBuf[3] = (BYTE)((dwEachCount + 4) >> 0);
		nSendBuf[4] = 0xFD;
		nSendBuf[5] = 0x03;
		memcpy(nSendBuf + 6, pBuf + pBufIndex, dwEachCount);
		crc = (WORD)CRC16CCITT(nSendBuf + 2, ((WORD)dwEachCount + 9) - 5);
		nSendBuf[6 + dwEachCount] = (BYTE)(crc >> 8);
		nSendBuf[7 + dwEachCount] = (BYTE)(crc >> 0);
		nSendBuf[8 + dwEachCount] = 0x55;
		nSendLen = dwEachCount + 9;
		g_Com.SendCmd(nSendBuf, nSendLen);
		if ((!g_Com.RecvCmdEx(nRecvBuf, 7)) || (nRecvBuf[5] != 0x07))
		{
			return false;
		}
	}
	if (dwLeft)
	{
		nSendBuf[2] = (BYTE)((dwLeft + 4) >> 8);
		nSendBuf[3] = (BYTE)((dwLeft + 4) >> 0);
		nSendBuf[4] = 0xFD;
		nSendBuf[5] = 0x03;
		memcpy(nSendBuf + 6, pBuf + pBufIndex, dwLeft);
		crc = (WORD)CRC16CCITT(nSendBuf + 2, ((WORD)dwLeft + 9) - 5);
		nSendBuf[6 + dwLeft] = (BYTE)(crc >> 8);
		nSendBuf[7 + dwLeft] = (BYTE)(crc >> 0);
		nSendBuf[8 + dwLeft] = 0x55;
		nSendLen = dwLeft + 9;
		g_Com.SendCmd(nSendBuf, nSendLen);
		if ((!g_Com.RecvCmdEx(nRecvBuf, 7)) || (nRecvBuf[5] != 0x07))
		{
			return false;
		}
	}

	return true;
}
BYTE CGui::ReflashDataDownload(char * pType, DWORD nTypeLen, char * pFileName, DWORD nFileNameLen, char * szLocalPath)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_REFALSE_DATA_DOWNLOAD;
	m_pMapGuiBuff[3] = (BYTE)0x00;  //[3]-[5]位暂时不用
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x00;
	m_pMapGuiBuff[6] = (BYTE)0xFE;   //返回值的变化

	m_pMapGuiBuff[7] = (BYTE)(nTypeLen >> 0x18);   //类型的长度
	m_pMapGuiBuff[8] = (BYTE)(nTypeLen >> 0x10);
	m_pMapGuiBuff[9] = (BYTE)(nTypeLen >> 0x08);
	m_pMapGuiBuff[10] = (BYTE)(nTypeLen >> 0x00);
	if (pType == NULL)
	{
		return 0x77;
	}
	memcpy(m_pMapGuiBuff + 11, pType, nTypeLen);

	m_pMapGuiBuff[11 + nTypeLen + 0] = (BYTE)(nFileNameLen >> 0x18);   //文件名称的长度
	m_pMapGuiBuff[11 + nTypeLen + 1] = (BYTE)(nFileNameLen >> 0x10);
	m_pMapGuiBuff[11 + nTypeLen + 2] = (BYTE)(nFileNameLen >> 0x08);
	m_pMapGuiBuff[11 + nTypeLen + 3] = (BYTE)(nFileNameLen >> 0x00);
	if (pFileName == NULL)
	{
		return 0x76;
	}
	memcpy(m_pMapGuiBuff + 11 + nTypeLen + 4, pFileName, nFileNameLen);

	m_pMapGuiBuff[11 + nTypeLen + 4 + nFileNameLen] = '\0';

	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	BYTE bKeyIndex = m_pMapGuiBuff[6];
	while (1)
	{
		Sleep(1);
		bKeyIndex = m_pMapGuiBuff[6];    //显示程序处理完消息后,给共享内存赋值

		if (bKeyIndex != 0xFE)
		{
			break;
		}
	}
	
	if (bKeyIndex == 0x7F)  //显示程序正确获取到路径
	{
		if (szLocalPath)
		{
			strcpy(szLocalPath, m_pMapGuiBuff + 7);
		}
	}

	return bKeyIndex;
}

bool CGui::ReflashDataDelete(char * szLocalPath)
{
	
	if (*szLocalPath == '\0')
	{
		return false;
	}

	//if (DeleteFile(szLocalPath) == 0) //删除文件在生成.so文件时编译不通过
	if(!remove(szLocalPath))
	{
		return false;
	}


	return true;
}
int CGui::GetSuffixNameIniFile(const char * pKeyName, char * pValue)
{
	if (pKeyName == NULL || pValue == NULL)
	{
		return 0;
	}

	//获取suffixName.ini文件路径
	string strRootPath, strConfigPath;
	strRootPath = GetRootPathEx();
	strConfigPath = strRootPath + _T("config\\suffixName.ini");

	const char *pIniPath = strConfigPath.c_str();   //suffixName.ini所在路径
	if (pIniPath == NULL)
	{
		return 0;
	}

	//文件不存在
	if (access(pIniPath, 0) == -1)
	{
		return 0;
	}
	else
	{
		//读取INI文件
		GetPrivateProfileString("EXTENSION", pKeyName, "", pValue, 256, pIniPath);
	}


	return 1;
}

//0:有线连接 1:蓝牙连接 2:未知连接
int CGui::IsWiredOrBluetooth()
{
	return ::IsWiredOrBluetooth();
}

int CGui::ReadVciBoxSoftwareVersion(int & nSoftwareVersion)
{
	BYTE nSendCmd[7] = { 0xA5,0xA5,0x00,0x02,0xF0,0x00,0x55 };
	BYTE nRecvCmd[30] = { 0 };

	g_Com.SendCmd(nSendCmd, 7);

	memset(nRecvCmd, 0, 30);
	g_Com.RecvCmd(nRecvCmd, 2);
	if (nRecvCmd[0] != 0xA5 &&
		nRecvCmd[1] != 0xA5)
	{
		return 0;
	}

	memset(nRecvCmd, 0, 30);
	g_Com.RecvCmd(nRecvCmd, 2);
	if (nRecvCmd[0] != 0x00 &&
		nRecvCmd[1] != 0x06)
	{
		return 0;
	}

	memset(nRecvCmd, 0, 30);
	g_Com.RecvCmd(nRecvCmd, 6);
	g_Com.RecvCmd(nRecvCmd + 6, 1);
	if (nRecvCmd[6] != 0x55)
	{
		return 0;
	}

	//例如:56 32 2E 30 39 ===> V2.09
	if (nRecvCmd[1] != 0x56) //"V"
	{
		return 0;
	}

	char szBuf[10] = { 0 };
	szBuf[0] = nRecvCmd[2];
	szBuf[1] = nRecvCmd[4];
	szBuf[2] = nRecvCmd[5];

	nSoftwareVersion = atoi(szBuf);

	return 1;
}
int CGui::ReadVciSid(string &strVciSid)
{
	BYTE nSendCmd[7] = { 0xA5,0xA5,0x00,0x02,0xF2,0x00,0x55 };
	BYTE nRecvCmd[30] = {0};

	g_Com.SendCmd(nSendCmd, 7);

	memset(nRecvCmd, 0, 30);
	g_Com.RecvCmd(nRecvCmd, 2);
	if (nRecvCmd[0] != 0xA5 &&
		nRecvCmd[1] != 0xA5)
	{
		return 0;
	}

	memset(nRecvCmd, 0, 30);
	g_Com.RecvCmd(nRecvCmd, 2);
	if (nRecvCmd[0] != 0x00 &&
		nRecvCmd[1] != 0x11)
	{
		return 0;
	}

	memset(nRecvCmd, 0, 30);
	g_Com.RecvCmd(nRecvCmd, 17);
	g_Com.RecvCmd(nRecvCmd + 17, 1);
	if (nRecvCmd[17] != 0x55)
	{
		return 0;
	}

	for (int i = 1; i < 13; i++)
	{
		//strVciSid.push_back(nRecvCmd[i]);
		strVciSid += nRecvCmd[i];
		
	}
	
	if (strVciSid.length() != 12)
	{
		return 0;
	}
	
	return 1;
}
int CGui::GetAppSoftwareVersion(int & nSoftwareVersion)
{
	//获取main.ini文件路径
	string strRootPath, strConfigPath;
	strRootPath = GetRootPathEx();
	strConfigPath = strRootPath + _T("main.ini");

	const char *pIniPath = strConfigPath.c_str();   //suffixName.ini所在路径
	if (pIniPath == NULL)
	{
		return 0;
	}

	//文件不存在
	if (access(pIniPath, 0) == -1)
	{
		return 0;
	}
	else
	{
		//读取INI文件
		char pValue[256] = {0};
		GetPrivateProfileString("SYSTEM", "APP_VERSION", "", pValue, 256, pIniPath);
	
		if (pValue == "")
		{
			return 0;
		}
	
		char szBuf[10] = { 0 };
	
		szBuf[0] = pValue[1];
		szBuf[1] = pValue[3];
		szBuf[2] = pValue[4];
		szBuf[3] = pValue[5];

		nSoftwareVersion = atoi(szBuf);
	}

	return 1;
}

//1018目前没有区分硬件版本 等1018出现二代版本的时候再实现
BYTE CGui::GetLowerMachineInformationInit(int nType)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_COMMAND;
	if (nType == 1)
	{
		m_pMapGuiBuff[3] = (BYTE)0x01;   //加密算法相关命令
	}
	else if (nType == 2)
	{
		m_pMapGuiBuff[3] = (BYTE)0x02;  //获取下位机硬件相关命令
	}

	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x01;
	m_pMapGuiBuff[6] = (BYTE)0xFE;
	m_pMapGuiBuff[7] = '\0';

	if (!SendMessage2MainForm_SendMessage())
	{
		return false;
	}

	BYTE bKeyIndex = m_pMapGuiBuff[6];
	while (1)
	{
		Sleep(1);
		bKeyIndex = m_pMapGuiBuff[6];    //显示程序处理完消息后,给共享内存赋值

		if (bKeyIndex != 0xFE)
		{
			break;
		}
	}
	if (bKeyIndex == 0x7E)  //获取下位机命令失败
	{
		return false;
	}

	return bKeyIndex;
}
//安卓平台无需实现  已经由网络安全算法实现
int CGui::GetLowerMachineLicense(int nType, char * pLicenseBuff)
{
	if (GetLowerMachineInformationInit(1) != 0)  //获取到了命令
	{
		strcpy(pLicenseBuff, m_pMapGuiBuff + 7);
	}
	else
	{
		return -1;
	}
	return 0;
}
int CGui::MemLoadEncryDll(int nType, int nMaskS, int* pMask, int nInputS, char* pInputChar, int* pOutputS, char* pOutputChar, int nFlag)
{

	__android_log_print(ANDROID_LOG_INFO,"MemLoadEncryDll","start=%d nInputS=%d",::GetTickCount(),nInputS);
	CMyHttp http;


	string host = "algorithm1018.zdeps.com";
	string path = "/v";

//	string host = "USER-20191211SI:8000";
//	string path = "/v";

//	string host = "192.168.0.110:8000";
//	string path = "/v";

	//输入转换为base64的字符串
	CBase64 base64;
	char *pInputCharBase64 = new char[nInputS * 4];
	memset(pInputCharBase64, 0, nInputS * 4);

	//如果文件比较大，是否考虑分段base64。目前验证8M数据没有问题
	int nRetBase = 0;
	nRetBase = base64.Base64_Encode((const unsigned char*)pInputChar, nInputS, pInputCharBase64);
	if (nRetBase < 0)
	{
		if (pInputCharBase64)
		{
			delete[] pInputCharBase64;
			pInputCharBase64 = NULL;
		}

		return -20;  //base64加密失败
	}

	CJsonObject cJson;

	int nRet = 0;
	string strAlgorithmJson;
	nRet = cJson.CreateSecurityAlgorithm(nType, nMaskS, pMask, nInputS, pInputCharBase64, nFlag, strAlgorithmJson);

	if (pInputCharBase64)
	{
		delete[]pInputCharBase64;
		pInputCharBase64 = NULL;
	}

	if (!nRet)
	{
		return -21;   //算法封装json失败
	}


	//获取序列号
	string strSerialNum = GetSidFromIniFile();

	//获取任务id
	DWORD dwTaskId = g_wTaskIdValue;
	string strTaskId;
	char szBuf[1024] = { 0 };
	sprintf(szBuf, "%ul", dwTaskId);
	strTaskId = szBuf;

	//请求体
	string strPostContent;
	strPostContent = "----------------------------381906398464664695032150\r\n";
	strPostContent += "Content-Disposition: form-data; name=\"src\"\r\n\r\n";
	strPostContent += strAlgorithmJson;
	strPostContent += "\r\n";

	strPostContent += "----------------------------381906398464664695032150\r\n";
	strPostContent += "Content-Disposition: form-data; name=\"serialNum\"\r\n\r\n";
	strPostContent += strSerialNum;
	strPostContent += "\r\n";

	strPostContent += "----------------------------381906398464664695032150\r\n";
	strPostContent += "Content-Disposition: form-data; name=\"taskId\"\r\n\r\n";
	strPostContent += strTaskId;
	strPostContent += "\r\n";

	strPostContent += "----------------------------381906398464664695032150--\r\n";
	__android_log_print(ANDROID_LOG_INFO,"MemLoadEncryDll","midle=%d",::GetTickCount());
	::lognet(true,strAlgorithmJson.c_str(),strAlgorithmJson.length());
	//向服务器发起请求
	int nPostRet = 0;
	string strResponse;
	nPostRet = http.PostData(host, path, strPostContent, strResponse);
	__android_log_print(ANDROID_LOG_INFO,"MemLoadEncryDll","end=%d",::GetTickCount());
	if (nPostRet != 1)  //1:正确获取到服务器返回的json包
	{
		if (nPostRet<=-1&&nPostRet>=-6){
			return -200;
		}
		return nPostRet;
		//-1:创建套接字失败 socket   网络失败
		//-2:设置发送和接收超时失败  setsockopt 网络失败
		//-3:域名解析出错   gethostbyname 网络失败
		//-4:Socket连接失败  connect 网络失败
		//-5:发送数据失败      write 网络失败
		//-6:接收数据失败      read  网络失败
		//-7:查找服务器返回的状态码失败
		//-8:查找服务器返回的json数组的第一个"{"失败 (提取服务器返回的json包失败)
		//500:等等服务器返回失败的状态码
	}
	::lognet(false,strResponse.c_str(),strResponse.length());

	//获取的json字符串进行解析
	int nInputLen = 0;
	string strInputChar;
	int nOutputLen = 0;
	string strOutputChar;
	string strMessage;

	nRet = cJson.ParseJsonString((char *)strResponse.c_str(), nInputLen, strInputChar, nOutputLen, strOutputChar, strMessage);
	if (nRet != 1)
	{
		return nRet;

		//-9:json解析出错
		//-10:当前算法不在算法库
		//-11:传递给服务器的json包存在问题(由于客户端用代码封装json包，可能性低)
		//-12://程序超时(没有等到服务器程序及时处理)
	}

	nInputS = nInputLen;
	*pOutputS = nOutputLen;

	int nRetBase1 = 0, nRetBase2 = 0;
	nRetBase1 = base64.Base4_Decode(strInputChar.c_str(), strInputChar.length(), (unsigned char*)pInputChar);
	if (nRetBase1 < 0)
	{
		return -22;  //Base64解密出错
	}

	nRetBase2 = base64.Base4_Decode(strOutputChar.c_str(), strOutputChar.length(), (unsigned char*)pOutputChar);
	if (nRetBase2 < 0)
	{
		return -22;  //Base64解密出错
	}

	__android_log_print(ANDROID_LOG_INFO,"MemLoadEncryDll","return 1=%d",::GetTickCount());
	return 1;
}
int CGui::MemLoadEncryDll(char * pLicenseBuff, int nType, int nMaskS, int * pMask, int nInputS, char * pInputChar, int * pOutputS, char * pOutputChar)
{
	return 1;
}
int CGui::GetLowerMachineHardware(char *strBuf)
{
	//获取下位机硬件相关信息
	//if (GetLowerMachineInformationInit(2) != 0)  //获取到了命令
	//{
	//	memcpy(strBuf, m_pMapGuiBuff + 7, 10);

	//	hard.wHardVer = (WORD)strBuf[0] * 0x100 + strBuf[1];
	//	hard.dwDate = (DWORD)((WORD)strBuf[2] * 0x100 + strBuf[3]) * 0x10000 + (WORD)strBuf[4] * 0x100 + (BYTE)strBuf[5];
	//	hard.bBTH = strBuf[6];
	//	hard.bUSB = strBuf[7];
	//	hard.bChip = strBuf[8];
	//	hard.bSRAM = strBuf[9];
	//}
	//else
	//{
	//	return -1;    //获取下位机硬件相关信息失败
	//}

	hard.bSRAM = 2;  //只表示3代下位机

	return 0;
}

int CGui::GetPinVoltage(BYTE bPin)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_PIN_VOLTAGE;
	m_pMapGuiBuff[3] = (BYTE)0x01;  //测试指定引脚的电压
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x01;
	m_pMapGuiBuff[6] = (BYTE)0xFE;
	m_pMapGuiBuff[7] = '\0';
	m_pMapGuiBuff[8] = bPin;

	if (!SendMessage2MainForm_SendMessage())
	{
		return 0;
	}

	BYTE bKeyIndex = m_pMapGuiBuff[6];
	while (1)
	{
		Sleep(1);
		bKeyIndex = m_pMapGuiBuff[6]; //显示程序处理完消息后，给共享内存赋值

		if (bKeyIndex != 0xFE)
		{
			break;
		}
	}


	if (bKeyIndex == 0x7F)  //引脚有电压
	{
		return 1;
	}
	else if (bKeyIndex == 0x7E)  // 引脚没有电压
	{
		return -1;
	}
	else if (bKeyIndex == 0x7D) //引脚电压获取失败
	{
		return -2;
	}

	return 0;
}

int CGui::GetPinVoltage(char * pPinBuff, int &nPinNum)
{
	memset(m_pMapGuiBuff, 0, 0xFFF0);
	m_pMapGuiBuff[0] = (BYTE)0x55;
	m_pMapGuiBuff[1] = (BYTE)DIRECTION_GUI2VDI;
	m_pMapGuiBuff[2] = (BYTE)FORM_PIN_VOLTAGE;
	m_pMapGuiBuff[3] = (BYTE)0x02;  //测试指定组合引脚的电压
	m_pMapGuiBuff[4] = (BYTE)0x00;
	m_pMapGuiBuff[5] = (BYTE)0x01;
	m_pMapGuiBuff[6] = (BYTE)0xFE;

	UINT nLen = 5;
	//6和14、3和11、1和9、11和12、12和13
	BYTE szBuff[6] = { 0xE6, 0xB3, 0x91, 0xCB, 0xDC };

	m_pMapGuiBuff[7] = (BYTE)nLen;  //引脚的对数
	memcpy(m_pMapGuiBuff + 8, szBuff, nLen);
	
	if (!SendMessage2MainForm_SendMessage())
	{
		return 0;
	}

	BYTE bKeyIndex = m_pMapGuiBuff[6];
	while (1)
	{
		Sleep(1);
		bKeyIndex = m_pMapGuiBuff[6]; //显示程序处理完消息后，给共享内存赋值

		if (bKeyIndex != 0xFE)
		{
			break;
		}
	}


	if (bKeyIndex == 0x7F)  //引脚有电压
	{
		char szResultBuff[20] = {0};

		nPinNum = (int)m_pMapGuiBuff[7];

		if (pPinBuff)
		{
			memcpy(pPinBuff, m_pMapGuiBuff+8, nPinNum);
		}

		return 1;
	}
	else if (bKeyIndex == 0x7D) //引脚电压获取失败
	{
		return -2;
	}

	return 0;
}

char * CGui::ReplaceStr(char *sSrc, char *sMatchStr, char *sReplaceStr)
{
	int  StringLen;
	char caNewString[4096]={0};
	char *FindPos = strstr(sSrc, sMatchStr);
	if( (!FindPos) || (!sMatchStr) )
		  return sSrc;
   while(FindPos)
   {
		memset(caNewString, 0, sizeof(caNewString));
		StringLen = FindPos - sSrc;
		strncpy(caNewString, sSrc, StringLen);
		strcat(caNewString, sReplaceStr);
		strcat(caNewString, FindPos + strlen(sMatchStr));
		sSrc= strcpy(sSrc, caNewString);
		FindPos = strstr(sSrc, sMatchStr);
	}
   getProductType();
	return sSrc;
}

unsigned int CGui::getProductType(){
    return ::androidGetProductType();
}


