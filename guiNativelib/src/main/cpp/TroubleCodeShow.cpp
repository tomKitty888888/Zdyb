// TroubleCodeShow.cpp: implementation of the CTroubleCodeShow class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "TroubleCodeShow.h"
#include "Display.h"		//注意:这个只能定义到这里cpp, 如果定义到.h里面则会出现错误!! 

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

extern CGui g_Gui;

CTroubleCodeShow::CTroubleCodeShow()
{
	m_pfnGetTroubleCodeString = NULL;
}

CTroubleCodeShow::~CTroubleCodeShow()
{

}

void CTroubleCodeShow::Init(const char *pTitle)
{
	g_Gui.DtcInit();
}
void CTroubleCodeShow::Init(string strTitle)
{
	g_Gui.DtcInit();
}
void CTroubleCodeShow::Init(CBinary idTitle)
{
	g_Gui.DtcInit();
}
void CTroubleCodeShow::Init(INT nItem, INT nColumnWidth)
{
	g_Gui.DtcInit(nItem, nColumnWidth);
}


bool CTroubleCodeShow::Add(CBinary idTroubleCode, string strTroubleStatus, int nShowByte, int nShowSymbol)
{
	//

	BYTE *pbTroubleCode = new BYTE [idTroubleCode.GetSize()+1];
	WORD wLen = 0;
	CDisplay dis;
	if (!dis.BinaryToBytePoint(idTroubleCode,pbTroubleCode,wLen))
	{
		delete [] pbTroubleCode;
		return false;
	}

	char szText[1024] = "";
	string str;
	g_Gui.GetTextEx(TEXT_DTC,pbTroubleCode,wLen,szText,2);
	str = szText;
	str += " ";
	str += strTroubleStatus;

	char szDtcId[50] = "";
	g_Gui.GetTextEx(TEXT_DTC,pbTroubleCode,wLen,szDtcId,1);
	if (strlen(szDtcId) <= 0)
	{
		char szTemp[20] = "";
		if (nShowSymbol == 1)
		{
			//PCUB码转换
			BYTE *chCodeBuffer = new BYTE[wLen + 1];
			char chCode;

			if (!StrToPCBU(pbTroubleCode, wLen, nShowByte, chCode, chCodeBuffer))
			{
				return false;
			}

			if (nShowByte == 3)
			{
				sprintf(szTemp, "%C%06X", chCode, chCodeBuffer[3] << 16 | chCodeBuffer[4] << 8 | chCodeBuffer[5]);
			}
			else if (nShowByte == 4)
			{
				sprintf(szTemp, "%C%08X", chCode, chCodeBuffer[2] << 24 | chCodeBuffer[3] << 16 | chCodeBuffer[4] << 8 | chCodeBuffer[5]);
			}
			else
			{
				sprintf(szTemp, "%C%04X", chCode, chCodeBuffer[4] << 8 | chCodeBuffer[5]);
			}

			if (chCodeBuffer)
			{
				delete[] chCodeBuffer;
				chCodeBuffer = NULL;
			}
		}
		else
		{
			if (nShowByte == 3)
			{
				sprintf(szTemp, "%06X", pbTroubleCode[3] << 16 | pbTroubleCode[4] << 8 | pbTroubleCode[5]);
			}
			else if (nShowByte == 4)
			{
				sprintf(szTemp, "%08X", pbTroubleCode[2] << 24 | pbTroubleCode[3] << 16 | pbTroubleCode[4] << 8 | pbTroubleCode[5]);
			}
			else   //除了输入第三个参数输入3或4显示3或4个字节，其他情况都只显示2个字节
			{
				sprintf(szTemp, "%04X", pbTroubleCode[4] << 8 | pbTroubleCode[5]);
			}
		}

		if (pbTroubleCode)
		{
			delete[] pbTroubleCode;
			pbTroubleCode = NULL;
		}

		strcpy(szDtcId,szTemp);
	}

	return g_Gui.DtcAdd(szDtcId,(char *)str.c_str());
}
bool CTroubleCodeShow::Add(CBinary idTroubleCode, CBinary idTroubleStatus, int nShowByte, int nShowSymbol)
{
	return Add(idTroubleCode,(string)"", nShowByte, nShowSymbol);
}
bool CTroubleCodeShow::Add(CBinary idTroubleCode, string strTroubleStatus,string strTroubleContent, int nShowByte, int nShowSymbol)
{
	BYTE *pbTroubleCode = new BYTE [idTroubleCode.GetSize()+1];
	WORD wLen = 0;
	CDisplay dis;
	if (!dis.BinaryToBytePoint(idTroubleCode,pbTroubleCode,wLen))
	{
		delete [] pbTroubleCode;
		return false;
	}

	char szText[1024] = "";
	string str;
	g_Gui.GetTextEx(TEXT_DTC,pbTroubleCode,wLen,szText,2);
	str = szText;
	str += " ";
	str += strTroubleStatus;
	
	char szDtcId[50] = "";
	g_Gui.GetTextEx(TEXT_DTC,pbTroubleCode,wLen,szDtcId,1);
	if (strlen(szDtcId) <= 0)
	{
		char szTemp[20] = "";
		
		if (nShowSymbol == 1)
		{
			//PCUB码转换
			BYTE *chCodeBuffer = new BYTE[wLen + 1];
			char chCode;

			if (!StrToPCBU(pbTroubleCode, wLen, nShowByte, chCode, chCodeBuffer))
			{
				return false;
			}

			if (nShowByte == 3)
			{
				sprintf(szTemp, "%C%06X", chCode, chCodeBuffer[3] << 16 | chCodeBuffer[4] << 8 | chCodeBuffer[5]);
			}
			else if (nShowByte == 4)
			{
				sprintf(szTemp, "%C%08X", chCode, chCodeBuffer[2] << 24 | chCodeBuffer[3] << 16 | chCodeBuffer[4] << 8 | chCodeBuffer[5]);
			}
			else
			{
				sprintf(szTemp, "%C%04X", chCode, chCodeBuffer[4] << 8 | chCodeBuffer[5]);
			}

			if (chCodeBuffer)
			{
				delete[] chCodeBuffer;
				chCodeBuffer = NULL;
			}
		}
		else
		{
			if (nShowByte == 3)
			{
				sprintf(szTemp, "%06X", pbTroubleCode[3] << 16 | pbTroubleCode[4] << 8 | pbTroubleCode[5]);
			}
			else if (nShowByte == 4)
			{
				sprintf(szTemp, "%08X", pbTroubleCode[2] << 24 | pbTroubleCode[3] << 16 | pbTroubleCode[4] << 8 | pbTroubleCode[5]);
			}
			else   //除了输入第三个参数输入3或4显示3或4个字节，其他情况都只显示2个字节
			{
				sprintf(szTemp, "%04X", pbTroubleCode[4] << 8 | pbTroubleCode[5]);
			}
		}

		if (pbTroubleCode)
		{
			delete[] pbTroubleCode;
			pbTroubleCode = NULL;
		}

		strcpy(szDtcId,szTemp);
	}
	
	return g_Gui.DtcAdd(szDtcId,(char *)str.c_str(), (char *)strTroubleContent.c_str());
}
bool CTroubleCodeShow::Add(CBinary idTroubleCode, CBinary idTroubleStatus,string strTroubleContent, int nShowByte, int nShowSymbol)
{
	return Add(idTroubleCode,(string)"", strTroubleContent, nShowByte, nShowSymbol);
}

BOOL CTroubleCodeShow::StrToPCBU(BYTE * pCode, int nLen, int nShowByte, char &chCode, BYTE chCodeBuf[])
{
	if (pCode)
	{
		BYTE *pCodeTmp = pCode;
		if (!pCodeTmp)
		{
			return 0;
		}

		//获取需要处理的字符
		BYTE chShowSymbol, chShowSymbolHighTwo, chShowSymbolLowSix;
		chShowSymbol = pCode[nLen - nShowByte];

		//保留字节位的高两位，其他位为0
		chShowSymbolHighTwo = chShowSymbol & 0xC0;
		//高两位为0，其他位保留原本的值
		chShowSymbolLowSix = chShowSymbol & 0x3F;

		pCodeTmp[nLen - nShowByte] = chShowSymbolLowSix;  //转换后的6个字节
		for (int i = 0; i < nLen + 1; i++)
		{
			chCodeBuf[i] = pCodeTmp[i];
		}

		DWORD dwCode = (DWORD)chShowSymbolHighTwo;
		if (dwCode == 0x00)  //P
		{
			chCode = 'P';
		}
		else if (dwCode == 0x40)//C
		{
			chCode = 'C';
		}
		else if (dwCode == 0x80) //B
		{
			chCode = 'B';
		}
		else if (dwCode == 0xC0) //U
		{
			chCode = 'U';
		}
	}
	else
	{
		return 0;
	}

	return 1;
}


void CTroubleCodeShow::Show()
{
	g_Gui.DtcShow();
}
W_INT16 CTroubleCodeShow::Show (W_INT16 &iSelNum)
{
	//用来做维修帮助的. 目前没有,如以后需要再增加  (这里直接显示即可)

	Show();
	return 0;
}
void CTroubleCodeShow::InitDtcMulti(INT nColumns)
{
	g_Gui.DtcMultiInit(nColumns);
}

void CTroubleCodeShow::InitDtcMulti(INT nColumns, vector<INT> vecColumnWidthRatio)
{
	g_Gui.DtcMultiInit(nColumns, vecColumnWidthRatio);
}

bool CTroubleCodeShow::AddDtcMulti(CBinary idTroubleCode, INT nCurColumn, INT nShowByte, INT nShowSymbol)
{
	if (nCurColumn == 1 || nCurColumn == 2)
	{
		BYTE *pbTroubleCode = new BYTE[idTroubleCode.GetSize() + 1];
		WORD wLen = 0;
		CDisplay dis;

		if (!dis.BinaryToBytePoint(idTroubleCode, pbTroubleCode, wLen))
		{
			delete[] pbTroubleCode;
			return false;
		}

		//故障码描述
		char szText[1024] = "";
		g_Gui.GetTextEx(TEXT_DTC, pbTroubleCode, wLen, szText, 2);
		WORD dwLen = 0;
		dwLen = (WORD)strlen(szText);
		if (dwLen < 2)   //没有故障码
		{
			memset(szText, 0x00, 1024);
			strcpy(szText, "未定义，请联系厂家售后");
		}

		//故障码ID
		char szDtcId[50] = "";
		g_Gui.GetTextEx(TEXT_DTC, pbTroubleCode, wLen, szDtcId, 1);
		if (strlen(szDtcId) <= 0)
		{
			char szTemp[20] = "";

			if (nShowSymbol == 1)
			{
				//PCUB码转换
				BYTE *chCodeBuffer = new BYTE[wLen + 1];
				char chCode;

				if (!StrToPCBU(pbTroubleCode, wLen, nShowByte, chCode, chCodeBuffer))
				{
					return false;
				}

				if (nShowByte == 3)
				{
					sprintf(szTemp, "%C%06X", chCode, chCodeBuffer[3] << 16 | chCodeBuffer[4] << 8 | chCodeBuffer[5]);
				}
				else if (nShowByte == 4)
				{
					sprintf(szTemp, "%C%08X", chCode, chCodeBuffer[2] << 24 | chCodeBuffer[3] << 16 | chCodeBuffer[4] << 8 | chCodeBuffer[5]);
				}
				else
				{
					sprintf(szTemp, "%C%04X", chCode, chCodeBuffer[4] << 8 | chCodeBuffer[5]);
				}

				if (chCodeBuffer)
				{
					delete[] chCodeBuffer;
					chCodeBuffer = NULL;
				}
			}
			else
			{
				if (nShowByte == 3)
				{
					sprintf(szTemp, "%06X", pbTroubleCode[3] << 16 | pbTroubleCode[4] << 8 | pbTroubleCode[5]);
				}
				else if (nShowByte == 4)
				{
					sprintf(szTemp, "%08X", pbTroubleCode[2] << 24 | pbTroubleCode[3] << 16 | pbTroubleCode[4] << 8 | pbTroubleCode[5]);
				}
				else   //除了输入第三个参数输入3或4显示3或4个字节，其他情况都只显示2个字节
				{
					sprintf(szTemp, "%04X", pbTroubleCode[4] << 8 | pbTroubleCode[5]);
				}
			}

			if (pbTroubleCode)
			{
				delete[] pbTroubleCode;
				pbTroubleCode = NULL;
			}

			strcpy(szDtcId, szTemp);
		}

		return g_Gui.DtcMultiAdd(szDtcId, szText, nCurColumn);
	}
	else
	{
		BYTE *pbTroubleCode = new BYTE[idTroubleCode.GetSize() + 1];
		WORD wLen = 0;
		CDisplay dis;

		if (!dis.BinaryToBytePoint(idTroubleCode, pbTroubleCode, wLen))
		{
			delete[] pbTroubleCode;
			return false;
		}

		char szContent[255];
		g_Gui.GetText(pbTroubleCode, wLen, szContent);
		delete[] pbTroubleCode;

		return g_Gui.DtcMultiAdd(szContent, nCurColumn);
	}

	return false;
}

bool CTroubleCodeShow::AddDtcMulti(INT nContent, INT nCurColumn)
{
	return g_Gui.DtcMultiAdd(nContent, nCurColumn);
}

bool CTroubleCodeShow::AddDtcMulti(string strContent, INT nCurColumn)
{
	return g_Gui.DtcMultiAdd((char *)strContent.c_str(), nCurColumn);
}

void CTroubleCodeShow::ShowDtcMulti()
{
	g_Gui.DtcMulitShow();
}


bool CTroubleCodeShow::SetFlag(W_INT16 iFlag)
{
	//这个函数是用来显示分页的,目前没什么用. 如以后需要再增加

	return true;
}

string  CTroubleCodeShow::GetSelectedItemText(W_INT16 iCol)
{
	//这个函数是用来检查选择了哪个故障码行(维修帮助用),目前没什么用. 如以后需要再增加

	return "";
}

void *CTroubleCodeShow::SetTroubleCodeCallBackFunction (string (*pfnCallBack) (CBinary idStroubleCode))
{
	m_pfnGetTroubleCodeString = pfnCallBack;

	return (void*)m_pfnGetTroubleCodeString;
}

string CTroubleCodeShow::DefaultStroubleCodeCallBack(CBinary idTroubleCode)
{
	//这个函数是用来回调找不到故障码时候显示的内容,目前没什么用. 如以后需要再增加

	return "";
}

string CTroubleCodeShow::TanslateToPCBU(char chHigh, char chLow)
{
	//这个函数是用来把故障码转成PCBU的格式,目前没什么用. 如以后需要再增加

	return "";
}

