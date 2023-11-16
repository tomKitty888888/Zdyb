// Gui.h: interface for the CGui class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_GUI_H__FC28D3F7_6C55_47C4_9CB8_77CD6D237C0A__INCLUDED_)
#define AFX_GUI_H__FC28D3F7_6C55_47C4_9CB8_77CD6D237C0A__INCLUDED_


#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "adsStd.h"

//#include <io.h>
#include <stdio.h>
//#include <tchar.h>

//#include <windows.h>
#include "Binary.h"
#include "Information.h"
#include "Database.h"

#include <vector>
#include <string>

#include "Debug.h"

using namespace std;


extern WORD g_wTaskIdValue;

//消息框的三种模式(同步VDI)  bMode
#define MSG_MB_OK			0x01		//只有一个[确定]按钮的消息框
#define MSG_MB_YESNO		0x02		//有[确定]和[取消]两个按钮的消息框
#define MSG_MB_NOBUTTON		0x03		//没有按钮的消息框(用来提示正在操作中，请稍候...之类的窗口)

//MSG_MB_YESNO这种模式下返回确定还是取消(同步VDI) --inputBox窗口共用此参数返回值
#define MB_YES		0x7E
#define MB_NO		0x7F

//定义GetText的标识字节
#define TEXT_TXT		0x05
#define TEXT_DTC		0x06
#define TEXT_CDS		0x07
#define TEXT_OTHER		0x08

#define CREATE_BUTTON				0x01
#define CREATE_RADIO_BUTTON			0x02
#define CREATE_EDIT					0x03
#define CREATE_STATIC				0x04
#define CREATE_COMBO_BOX			0x05
#define CREATE_EDIT_STATIC			0x06
#define CREATE_STATIC_EDIT_STATIC	0x07
#define CREATE_STATIC_EDIT			0x08

class CGui  
{
public:
	bool GuiOpen(void);
	bool GuiOpenNoDataVdi();
	bool GuiClose(void);
	
	bool GetText(BYTE *pbId,WORD wLen,char *pszText,BYTE bIndex=1);
	bool GetTextEx(BYTE bHead,BYTE *pbId,WORD wLen,char *pszText,BYTE bIndex=1);
	string LoadText(CBinary binID,BYTE bDB=TEXT_TXT,BYTE nIndex=1);

	bool MenuInit(void);
	bool MenuAdd(BYTE *pbId, BYTE bLen);
	bool MenuAdd(char *pszMenu);
	BYTE MenuShow(void);

	//2021.11.24进行添加，Menu菜单项上显示Combo控件
	bool MenuExInit(UINT nMenuNum=255);  //菜单项个数，如果不传参数，默然为255，显示程序初始化时，变量分配空间就是255
	bool MenuExAdd(char *pszMenu, UINT nCtrlFlag);  //使用时nCtrlFlag传入1，显示程序用来保存ListCtrl控件上的内容
	bool MenuExAdd(vector<string> vecpszMenu, UINT nCtrlFlag);   //使用时nCtrlFlag传入2，显示程序用来保存Combo控件上的内容
	BYTE MenuExShow(vector<UINT>&vecIndex);

	//2022.01.55进行添加，Menu菜单项上增加Button RadioButton Edit Static ComboBox控件
	bool MenuCtrlInit();
	//第一个参数:ListCtrl上显示的内容; 第二个参数:表示内容是添加到ListCtrl上
	bool MenuCtrlAdd(char *pszMenu, UINT nCtrlFlag);  //使用时nCtrlFlag传入1，显示程序用来保存ListCtrl控件上的内容
	//第一个参数:行; 第二个参数:列; 第三个参数: 控件类型(例如是Button,还是Edit等控件，见头文件定义的宏); 
	//第四个参数:控件所占的宽度; 第五个参数:控件上显示的文字; 第六个:表示内容是添加创建的控件上
	bool MenuCtrlAdd(UINT nRow, UINT nColumn, UINT nCtrlType, vector<UINT> vecColumnWidthRatio, vector<string> vecShowText, UINT nCtrlFlag); //使用时nCtrlFlag传入2，显示程序用来保存各个需要创建的控件上的内容
	//第一个参数:行; 第二个参数:列; 第三个参数:控件上默认要显示的内容; 第四个参数:表示内容添加的是默认显示内容
	bool MenuCtrlAdd(UINT nRow, UINT nColumn, vector<string> vecShowText, UINT nCtrlFlag);  //使用时nCtrlFlag传入3，控件上默认显示的参数
	//返回标定选择后的内容
	BYTE MenuCtrlShow(vector<string> &vecStrDefText);
	//第一个参数:分隔符; 第个参数:要分割的字符串; 第三个参数:分割后的字符串
	void ExtractStrToStr(string strGap, string strSource, string &strTarget);
	void ExtractStrToStr(string strGap, string strSource, vector<string>& vecStrTarget);

	bool DtcInit(void);
	bool DtcInit(INT nItem, INT nColumnWidth = 100);               //2019.10.22进行添加
	bool DtcAdd(BYTE *pbId,BYTE bLen);
	bool DtcAdd(char *pszId,char *pszDtc);
    bool DtcAdd(char *pszId,char *pszDtc, char *pszContent);  
	BYTE DtcShow(void);

	//2021.12.15进行添加，DTC带表头可拖动多列
	bool DtcMultiInit(INT nColumns);
	bool DtcMultiInit(INT nColumns, vector<INT> vecColumnWidthRatio);
	bool DtcMultiAdd(INT pszContent, INT nCurColumn);
	bool DtcMultiAdd(char *pszContent, INT nCurColumn);
	bool DtcMultiAdd(char *pszId, char *pszDtc, INT nCurColumn);
	BYTE DtcMulitShow(void);

	bool VerInit(void);
	bool VerAdd(BYTE *pbId,BYTE bLen,char *pszText);
	bool VerAdd(char *pszName,char *pszText);
	BYTE VerShow(void);

	bool ListInit(BYTE *pbId,BYTE bLen);
	bool ListInit(char *pszTitle);
	bool ListAdd(BYTE *pbId,BYTE bLen,BYTE *pbId1,BYTE bLen1,DWORD dwColor=RGB(0,0,0));
	bool ListAdd(char *pszName,char *pszValue,DWORD dwColor=RGB(0,0,0));
	BYTE ListShow(void);

	bool CdsSelectInit(bool bInitVar=true);
	bool CdsSelectInit(bool bInitVar,INT nColumnCdsSelect);           //2019.11.14进行添加
	bool CdsSelectInit(bool bInitVar,INT nColumnCdsSelect, INT nColumnCdsShow);           //2019.11.14进行添加
	bool CdsSelectAdd(BYTE *pbId,BYTE bLen, BYTE *pUnit=NULL,BYTE bUnitLen=0);
	bool CdsSelectAdd(char *pszName,char *pszUnit);
	BYTE CdsSelectShow(void);
	BYTE CdsSelectGetItem(BYTE *pbSelectedItem); //BYTE最多只能选255个数据流

	bool CdsInit(void);
	bool CdsInit(INT nColumnCdsShow);        //2019.11.14进行添加
	bool CdsAdd(WORD wIndex,char *pszValue, char *pszUnit=NULL);
	bool CdsAdd(BYTE *pbId, BYTE nLen, char *pszValue, char *pszUnit);
	BYTE CdsShow(void);
	bool CdsCheckUpdate(void);

	bool ActInit(void);
	bool ActAddButton(BYTE *pbId,BYTE bLen,char *pszText,char bStatus);
	bool ActAddPrompt(BYTE *pbId,BYTE bLen);
	bool ActAddPrompt(char *pszText);
	bool ActAddPrompt(const char *pszText);
	bool ActAdd(char *pszName,char *pszValue,char *pszUnit);
	BYTE ActShow(void);

	BYTE MsgShowMessage(BYTE *pbMessageId,BYTE bMsgLen,BYTE bMode=MSG_MB_OK);
//	BYTE MsgShowMessage(char *pszMessage,BYTE bMode=MSG_MB_OK);
	BYTE MsgShowMessage(char *pszMessage,BYTE bMode=MSG_MB_OK, DWORD dwColor = RGB(112, 112, 112));
	BYTE MsgShowMessage(char *pszMessage,char *pszBmpPath,BYTE bMode=MSG_MB_OK);
	void MsgReflashProgress(BYTE *pszMessage,BYTE bMsgLen);
	BYTE MsgInputBox(BYTE *pbMsg,BYTE bMsgLen,BYTE *pbTip,BYTE bTipLen,BYTE bMode,char *pszMin,char *pszMax);
	BYTE MsgInputBox(char *pszMsg,char *pszTip,BYTE bMode,char *pszMin,char *pszMax);
	void MsgInputGetText(char *pszInputText);
	void MsgTitleText(char *pszTitleText);
	void MsgTitleText(DWORD dwTaskId);
    void MsgTitleText(DWORD dwTaskId, string &strMenuText);

	void CloseDialogWait(void); //可以自己调用,如不调用则遇到到任何Show窗口都会内部调用

	void FileParam(char *szP,char *szV,char *szHP,char *szMemo);
	BYTE FileDialog(bool bOpen,char *szFilter,char *szPath);
	//读取刷写完成通知显示程序，这样做的目的是告知显示程序将刷写、读取数据上传服务器
	bool FileHandleFinish(int nFinishFlag, char* szPath);

	//2020.9.3进行添加，指定路径创建文件
	//第一参数：文件路径；
	bool CreateFileByPath(char *szPath);

	bool SendDiagnoseQuitMsg2Display();
public:
	CGui();
	virtual ~CGui();

public: //共享内存
	HANDLE m_MapGuiHandle;
	HANDLE m_MapComHandle;
     char *m_pMapGuiBuff;
	 char *m_pMapComBuff;
	bool ShareBufferCreate(void);
	bool ShareBufferDestroy(void);

private: //数据文件
	FILE *m_pfData;
	DWORD m_dwCurrentAddr;
	bool DataFileOpen(void);
	bool DataFileClose(void);
	bool ReadDataFile(BYTE *pnBuff,WORD wLen);
	BYTE *m_pVdiData;
	DWORD m_dwVdiIndex;

public:
	HWND m_hMainWnd; //显示程序窗口句柄
	bool GetMainFormHandle(void);
	bool SendMessage2MainForm_SendMessage(void);
	bool SendMessage2MainForm_PostMessage(void);
	bool SendMessage2MainForm_SendMessage_COM(void);

public:
	DWORD StrToHex(char *str);									//字符串转16进制
private:
	BYTE ByteToAscii(BYTE nSource);								//Byte转Ascii码
	bool IdTranslate(BYTE *pbId,BYTE bLen,BYTE *pbTarget);		//ID转换

	/////////////////////////////////////安全算法需要用到///////////////////////////////////////
public:
	//获取下位机的接收命令（与安全算法相关，2020.3.06添加）
	BYTE GetLowerMachineInformationInit(int nType);  //1:安全算法相关的命令 2:下位机硬件相关的命令

	//获取下位机返回的license
	int GetLowerMachineLicense(int nType, char *pLicenseBuff);

	//加密算法接口
	//参数一：加密算法类别；参数二：掩码数量；参数三：掩码数组指针；
	//参数四：Seed字节数量；参数五：Seed指针；参数六：返回Key数量；参数七：返回Key指针

	//1018服务器上获取安全算法返回值说明
	//-1:创建套接字失败
	//-2:设置发送和接收超时失败
	//-3:域名解析出错
	//-4:Socket连接失败
	//-5:发送数据失败
	//-6:接收数据失败
	//-7:查找服务器返回的状态码失败
	//-8:查找服务器返回的json数组的第一个"{"失败 (提取服务器返回的json包失败)
	//500:等等服务器返回失败的状态码
	//-9:json解析出错
	//-10:当前算法不在算法库
	//-11:传递给服务器的json包存在问题(由于客户端用代码封装json包，可能性低)
	//-12://程序超时(没有等到服务器程序及时处理)
	//-20://base64加密失败
	//-21://算法封装json失败
	//-22://Base64解密出错
	//-200 网络不通或网络达不到要求 返回-200
	//1:成功
	int MemLoadEncryDll(int nType, int nMaskS, int* pMask, int nInputS, char* pInputChar, int* pOutputS, char* pOutputChar, int nFlag = -1);
	int MemLoadEncryDll(char *pLicenseBuff, int nType, int nMaskS, int* pMask, int nInputS, char* pInputChar, int* pOutputS, char* pOutputChar);

	////////////////////////////////从服务器获取Count.dll需要用到///////////////////////
	//CSocketClient m_socketClient;

	/////////////////////////////////////////////////////////////////////////////////////
	
	//下位机硬件信息接口
	struct _HARD_INFO_
	{
		WORD wHardVer;    //硬件版本号
		DWORD dwDate;     //硬件生产日期
		BYTE bBTH;        //无线的蓝牙通讯
		BYTE bUSB;        //有线的串口通讯
		BYTE bChip;       //01:主芯片为STM32
		BYTE bSRAM;       //外部SRAM类型，01：一代 02：三代
	} hard;

	int GetLowerMachineHardware(char *strBuf);

	/////////////////////////////////////获取引脚电压///////////////////////////////
public:
	//参数:要测试的引脚组合 例如:0xE6(14和6引脚)
	//返回值 1:引脚有电压  -1:引脚没有电压 -2:引脚电压获取失败 0:给显示程序发送消息失败
	int GetPinVoltage(BYTE bPin);  //该接口还未使用到，等待下位机优化后再优化接口，可以先不管

	//测试所有组合引脚的电压(6和14、3和11、1和9、11和12、12和13)
	//返回值 1:引脚有电压 -2:引脚电压获取失败 0:给显示程序发送消息失败
	//参数一[out]:哪些引脚对(例如6和14是一对，值0xe6)存在电压  参数二[out]:引脚对的个数
	int GetPinVoltage(char *pPinBuff, int &nPinNum); //该接口还未使用到，等待下位机优化后再优化接口，可以先不管
	
private:
	char m_szInputText[255];
public:
	char m_szFileDialog[255];


//////////////////////////////////////////////////////////////////////////
public:
	bool SendMessage_TaskIdRunning(bool bRunning,WORD iTaskID);

public:
	void String2Binary(string str,CBinary &binary);
	bool GetTextCommand(BYTE bHead,BYTE *pbId,WORD wLen,vector<CBinary> &vecbinCmd);

//public:
//	static string g_strPath;	//记录下当前目录(menu.txt,data.vdi等那一层,不包含文件本身)

	string GetFileName(WORD iTaskID);
	string GetFilePathName(WORD iTaskID);

	string GetRootPath();     //获取根目录(显示程序的路径,含debug或Release文件夹)
	string GetRootPathEx();   //获取根目录(显示程序的路径,不含debug或Release文件夹)
	string GetAbsolutePath(); //获取绝对路径(诊断程序的路径)
	string GetCurrentPath();
	string GetRelativePath(string strAbsolutePath,string strRootPath); //获取相对路径(绝对路径-根目录)
	string ReplaceChar(string str,char a,char b); //str中含有a的都替换成b

	string GetLocalDateTime();  //获取本地当前日期时间
	string GetSidFromIniFile(); //获取SID序列号(从ini文件中读取)

private:
//解密
	bool Decrypt(BYTE nIndex, BYTE *pBuf, WORD wLen);
	bool Decrypt2(BYTE *pBuf, WORD wLen);
	DWORD m_dwKey;

public:
	DWORD GetPinAD(BYTE bPin);

public:
	BYTE SetMultiTaskBegin(DWORD nCalcType,DWORD nSystemName);
	BYTE SetMultiTaskEnd();

public: //SRAM
	WORD CRC16CCITT(BYTE * pszBuf, WORD unLength);
	bool WriteSram(BYTE *pBuf,DWORD dwLen);

public:
	//2021.1.25进行添加，刷写数据进行下载接口
	//第一个参数：数据类型，例如：字符1代表康明斯
	//第二个参数：数据类型长度
	//第三个参数：文件名称
	//第四个参数：文件名称长度
	//第五个参数：返回的下载数据本地所在路径
	//返回值：成功非0值 失败：false
	BYTE ReflashDataDownload(char * pType, DWORD nTypeLen, char * pFileName, DWORD nFileNameLen, char * szLocalPath);

	//2021.2.19进行添加，刷写数据删除本地对应的数据
	//参数：要删除的刷写数据所在路径
	//返回值：成功 true  失败：false
	bool ReflashDataDelete(char *szLocalPath);


	//获取显示程序中config文件夹中的配置文件suffixName.ini
	//参数一(传入):ini文件中键的名称；参数二(传出):ini文件中键对应的值
	//返回值 0:参数指针为空或者suffixName.ini文件不存在 1:suffixName.ini文件存在
	int GetSuffixNameIniFile(const char* pKeyName, char *pValue);


	//判断是蓝牙还是有线连接
	//返回值 -1:wireless.txt文件不存在 -2:打开文件失败 -3:读取失败 0:有线连接 1:蓝牙连接 2:未知连接
	int IsWiredOrBluetooth();

	//发送指令获取下位机的软件版本号
	//参数[out]：版本号
	//返回值: 成功1 失败0
	int ReadVciBoxSoftwareVersion(int &nSoftwareVersion);  

	//发送指令获取下位机的序列号
	//参数[out]：序列号
	//返回值: 成功1 失败0
	int ReadVciSid(string &strVciSid);

	//读取配置文件获取显示程序的软件版本号
	//参数[out]：版本号
	//返回值: 成功1 失败0
	int GetAppSoftwareVersion(int &nSoftwareVersion);

    //替换字符串中特征字符串为指定字符串
    char * ReplaceStr(char *sSrc, char *sMatchStr, char *sReplaceStr);

    //获取产品类型
	//前面的0x1 代表的是安卓平台  后面的0001是产品类型
	//0x0 未知产品类型
	//0x10001 安卓- 卡修精灵-柴油版
	//0x10002 安卓-卡修精灵-天然气版本
	//0x10003 安卓-1018
    unsigned int getProductType();


};

#define  WM_USER				0x400

//自定义消息,发消息给VDI(有改动需同步VDI)
#define MSG_MAIN_EPA2015		(WM_USER+200)
#define PARAM_GUI				0x10000001
#define PARAM_COM				0x10000002

//定义消息传送的方向，占用共享内存索引[1]下标(有改动需同步VDI)
#define DIRECTION_GUI2VDI		0x01
#define DIRECTION_VDI2GUI		0x02

//定义消息指定的窗口，占用共享内存索引[2]下标

#define FORM_MENU			0x01
#define FORM_DTC			0x02
#define FORM_CDS_SELECT		0x03
#define FORM_CDS_SHOW		0x04
#define FORM_VER			0x05
#define FORM_ACT			0x06
#define FORM_MAIN			0x07
#define FORM_DIESEL			0x08
#define FORM_BMP			0x09
#define FORM_LIST			0x0D
#define FORM_MENU_EX        0x0F
#define FORM_DTC_MULTI	    0x33
#define FORM_MENU_CTRL		0x34
#define FORM_SOCKET         0x35 //从显示程序中获取Socket
#define FORM_FILE_FINISH    0x36
#define FORM_PIN_VOLTAGE	0x37 //从显示程序中获取引脚电压

#define FORM_GUI_OPEN		0x10
#define FORM_GUI_CLOSE		0x11

#define FORM_COMMAND        0x20 //与下位机进行通信，2020.03.06添加

#define FORM_MSG			0x40 //注：该消息可以指定三种窗口(一个确定按钮，是否按钮，无按钮)
#define FORM_INPUT			0x41 //输入框窗口消息
#define FORM_TITLE			0x42

#define FORM_WEB			0x50 //WebBrowser窗口

#define FORM_FILEDIALOG		0x60 //打开或保存窗口
#define FORM_FILEPARAM		0x61 //设置FORM_FILEDIALOG的相关参数

#define FORM_QUIT			0x70 //退出诊断程序

#define FORM_CREATEFILE     0x30 //创建文件，2020.09.03添加
#define FORM_REFALSE_DATA_DOWNLOAD	0x31		//刷写数据在线下载相关
#define FORM_BAUD_RATE_CHANGE       0x32		//2021.10.22诊断系统设置波特率

//定义特定消息
#define ID_MENU_BACK	0xFF //所有菜单返回均指定为该ID
#define ID_CDS_VIEW		0xFD //数据流窗口查看的时候指定的ID


//发消息给等待窗口(同步VDI)
#define MSG_WAIT_EPA2015			(WM_USER+300)
#define MSG_WAIT_SET_MESSAGE		0x10000001
#define MSG_WAIT_WINDOW_CLOSE		0x10000002

//input输入框模式参数,使用限制输入(不包括删除键Backspace和DEL键)(同步VDI)
#define INPUT_MODE_DEC		0x01	//限制只能输入10进制数字
#define INPUT_MODE_HEX		0x02	//限制只能输入16进制数字
#define INPUT_MODE_VIN		0x03	//限制只能输入作为汽车VIN使用的字符和数字
#define INPUT_MODE_ALL		0x10	//无限制


//定义任务ID是否已经启动(发给VDI用来判断执行是menu.txt的菜单还是由诊断程序建立的菜单)
#define PARAM_GUI_TASK_ID_BEGIN		0xFFFF0001
#define PARAM_GUI_TASK_ID_END		0xFFFF0002

//消息来自GUI.LIB发过来的 获取VDI程序的根目录(同步)
#define PARAM_GUI_ROOT_PATH			0xFFFF0010
#define PARAM_GUI_ABSOLUTE_PATH			0xFFFF0011

#endif // !defined(AFX_GUI_H__FC28D3F7_6C55_47C4_9CB8_77CD6D237C0A__INCLUDED_)
