// Display.cpp: implementation of the CDisplay class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Display.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

CGui g_Gui;


//CDisplay::CDisplay()
//{
//
//}
CDisplay::~CDisplay()
{

}

W_UINT8 CDisplay::Init()
{
	if (!g_Gui.GuiOpen())
	{
		return 0;
	}
	
	return 1;
}
int CDisplay::IsHaveDataVdi()
{
	if (!g_Gui.ShareBufferCreate())
	{
		OutputDebugString("main form has been lost!\r\n");
		return -1;
	}

	if (g_Gui.m_pMapGuiBuff[0] == 0x01)
	{
		return 1;
	}
	else if (g_Gui.m_pMapGuiBuff[0] == 0x00)
	{
		return 0;
	}

	return 1;
}
W_UINT8 CDisplay::InitNoDataVdi()
{
	if (!g_Gui.GetMainFormHandle())
	{
		OutputDebugString("Display has been lost!\r\n");
		return false;
	}

	return 1;
}
void CDisplay::Destroy()
{
	g_Gui.GuiClose();
}

bool CDisplay::BinaryToBytePoint(CBinary bin,BYTE *pb,WORD &wLen)
{
	if (bin.GetSize() <= 0)
	{
		return false;
	}
	
	BYTE *p = new BYTE [bin.GetSize()+1];
	for (WORD w=0; w<bin.GetSize(); w++)
	{
		p[w] = bin.GetAt(w);
	}
	wLen = bin.GetSize();
	memcpy(pb,p,wLen);
	delete [] p;
	return true;
}


//////////////////////////////////////////////////////////////////////////
//说明:原来这里是有const 在前两个参数上的,为了传参方便去掉了.
//


W_UINT16 CDisplay::MessageBox(char *pContain, char *pTitle, W_UINT16 nFlag, DWORD dwColor, W_UINT16 nView)
{
	BYTE bKey = g_Gui.MsgShowMessage((char*)pContain,nFlag, dwColor);
	return (W_UINT16)bKey;
}

W_UINT16 CDisplay::MessageBox(string strContain,  string strTitle,  W_UINT16 nFlag, W_UINT16 nView)
{
	BYTE bKey = g_Gui.MsgShowMessage((char *)strContain.c_str(),nFlag);
	return (W_UINT16)bKey;
}

W_UINT16 CDisplay::MessageBox(CBinary idContain,  CBinary idTitle,  W_UINT16 nFlag, W_UINT16 nView)
{
	//idTitle和nView 参数这里没有用到

	BYTE *pbCondition = new BYTE [idContain.GetSize()+1];
	WORD wLen = 0;
	if (!BinaryToBytePoint(idContain,pbCondition,wLen))
	{
		delete [] pbCondition;
		return 0;
	}

	BYTE bKey = g_Gui.MsgShowMessage(pbCondition,(BYTE)wLen,nFlag);

	delete [] pbCondition;

	return (W_UINT16)bKey;
}

W_UINT16 CDisplay::SysMessageBox(W_UINT16 nMsgID, W_UINT16 nFlag)
{
	return 0;//not use
}


//////////////////////////////////////////////////////////////////////////
// CDisplay::CInputValue  begin
//
CDisplay::CInputValue::~CInputValue()
{

}


CDisplay::CInputValue::CInputValue()
{
	m_iUserKey = 0;
	m_strUserInput = "";
}
CDisplay::CInputValue::CInputValue(W_INT16 iKey, char * strInput)
{
	m_iUserKey = iKey;
    if(NULL==strInput)
    {
        m_strUserInput = "";
    } 
	else
	{
        m_strUserInput = strInput;
        //m_strUserInput = "1234567";
    }
}
W_INT16 CDisplay::CInputValue::GetUserKey()
{
	return m_iUserKey;
}

string  CDisplay::CInputValue::GetInput()
{
	return m_strUserInput;
}

string  CDisplay::CInputValue::GetString()
{
	return m_strUserInput;
}

W_INT32 CDisplay::CInputValue::GetInteger32()
{
	return atoi(m_strUserInput.c_str());
}

/**************************************************************
    
功    能：构造函数
参数说明：CInputValue& Input-源二进制数据
返 回 值：无
说    明：以一个二进制数据初始化变量；

**************************************************************/

CDisplay::CInputValue::CInputValue(const CInputValue& Input)
{
    m_iUserKey = Input.m_iUserKey;
    m_strUserInput = Input.m_strUserInput;
}

/*****************************************************
    
功    能：复制类
参数说明：CDisplay::CInputValue，
返 回 值：无
说    明：结构指针指向类Input的地址；
	注意自身赋值的处理

*****************************************************/
void CDisplay::CInputValue::operator = (const CInputValue Input)
{
    m_iUserKey = Input.m_iUserKey;
    m_strUserInput = Input.m_strUserInput;

}

CDisplay::CInputValue  CDisplay::Input(CBinary idPrompt, 
	                          CBinary idTitle, 
                              string strDefaultString, 							 
                              string strFormat, 
                              string strMinString, 
                              string strMaxString)
{
	CInputValue input(0,"");

	BYTE *pbPrompt = new BYTE [idPrompt.GetSize()+1];
	WORD wPromptLen = 0;
	if (!BinaryToBytePoint(idPrompt,pbPrompt,wPromptLen))
	{
		delete [] pbPrompt;
		return input;
	}

	BYTE *pbTitle = new BYTE [idTitle.GetSize()+1];
	WORD wTitleLen = 0;
	if (!BinaryToBytePoint(idTitle,pbTitle,wTitleLen))
	{
		delete [] pbTitle;
		return input;
	}

	char szMsg[1024]={0,};
	char szTip[1024]={0,};
	if (!g_Gui.GetText(pbPrompt,wPromptLen,szMsg))
	{
		delete [] pbTitle;
		delete [] pbPrompt;
		return input;
	}
	if (!g_Gui.GetText(pbTitle,wTitleLen,szTip))
	{
		delete [] pbTitle;
		delete [] pbPrompt;
		return input;
	}

	delete [] pbTitle;
	delete [] pbPrompt;

	string strPrompt = szMsg;
	string strTitle  = szTip;

	return CDisplay::Input(strPrompt,strTitle,strDefaultString,strFormat,strMinString,strMaxString);
}
CDisplay::CInputValue CDisplay::Input(string strPrompt, 
                             string strTitle, 
                             string strDefaultString, 
                             string strFormat, 
                             string strMinString, 
                             string strMaxString)
{
	CInputValue input(0,"");

//#define INPUT_MODE_DEC		0x01	//限制只能输入10进制数字
//#define INPUT_MODE_HEX		0x02	//限制只能输入16进制数字
//#define INPUT_MODE_VIN		0x03	//限制只能输入作为汽车VIN使用的字符和数字
//#define INPUT_MODE_ALL		0x10	//无限制

	BYTE bMode = INPUT_MODE_ALL;
	if (strFormat == "9")			//"9"表示"0"~"9"之间；
	{
bMode = INPUT_MODE_DEC;
	}
	else if (strFormat == "X")		//"X"表示"0"~"F"之间
	{
		bMode = INPUT_MODE_HEX;
	}
	else if (strFormat == "A")		//"A"表示"A"~"Z"之间
	{
		return input;//目前没有,碰到后再增加
	}
	else if (strFormat == "S")		//"S"表示全部字符集。
	{
		bMode = INPUT_MODE_ALL;
	}
	else 
	{
		return input;
	}
	

	BYTE bKey = g_Gui.MsgInputBox((char *)strPrompt.c_str(),(char *)strTitle.c_str(),\
		bMode,(char *)strMinString.c_str(),(char *)strMaxString.c_str());

	char szInput[1024]={0,};
	g_Gui.MsgInputGetText(szInput);

//    string strtemp=szInput;
	CInputValue inputResult(bKey,szInput);
	return inputResult;
}
//
// CDisplay::CInputValue  end
//////////////////////////////////////////////////////////////////////////



W_UINT16 CDisplay::ProgressBar(CBinary idPrompt, CBinary idTitle, W_UINT16 uiPercen)
{
	BYTE *pbPrompt = new BYTE [idPrompt.GetSize()+1];
	WORD wPromptLen = 0;
	if (!BinaryToBytePoint(idPrompt,pbPrompt,wPromptLen))
	{
		delete [] pbPrompt;
		return 0;
	}

	BYTE *pbTitle = new BYTE [idTitle.GetSize()+1];
	WORD wTitleLen = 0;
	if (!BinaryToBytePoint(idTitle,pbTitle,wTitleLen))
	{
		delete [] pbTitle;
		return 0;
	}

	char szMsg[1024]={0};
	char szTip[1024]={0};
	if (!g_Gui.GetText(pbPrompt,wPromptLen,szMsg))
	{
		delete [] pbTitle;
		delete [] pbPrompt;
		return 0;
	}
	if (!g_Gui.GetText(pbTitle,wTitleLen,szTip))
	{
		delete [] pbTitle;
		delete [] pbPrompt;
		return 0;
	}

	delete [] pbTitle;
	delete [] pbPrompt;

	string strPrompt = szMsg;
	string strTitle  = szTip;

	return ProgressBar(strPrompt,strTitle,uiPercen);
}
W_UINT16 CDisplay::ProgressBar(string strPrompt, string strTitle, W_UINT16 uiPercen)
{
	//目前没有进度条窗口,暂时用无按钮的等待窗口代替 ==============有时间再完善
	g_Gui.MsgShowMessage((char *)strPrompt.c_str(),adsMB_NoButton);

	return uiPercen;
}







//////////////////////////////////////////////////////////////////////////
// 奇瑞和吉利车型 用来返回故障码给显示程序的, 这里略,不做处理
//

int CDisplay::SendDTCId (vector<CBinary>)
{
	return 0;
}
//////////////////////////////////////////////////////////////////////////
// 奇瑞和吉利 用来返回车型ID给显示显示程序的, 这里略,不做处理
//

int CDisplay::SendVsfeType (W_UINT16 vt)
{
	return 0;
}
//
int CDisplay::SendVehicleInfo (vector<CBinary>)
{
	return 0;
}
