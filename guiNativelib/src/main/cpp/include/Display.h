// Display.h: interface for the CDisplay class.
//
// 说明:Display的类名不是显示类,不能顾名思义,这里只是为了兼容接口 
//

//////////////////////////////////////////////////////////////////////

#if !defined(AFX_DISPLAY_H__CA6D9FB4_3190_4D5D_8AF8_D95EB9E0F836__INCLUDED_)
#define AFX_DISPLAY_H__CA6D9FB4_3190_4D5D_8AF8_D95EB9E0F836__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "adsStd.h"
#include "Binary.h"
#include "Gui.h"
#include "Database.h"
#include "stdlib.h"

#include "MenuShow.h"
#include "TroubleCodeShow.h"
#include "MultiSelectShow.h"
#include "DataStreamShow.h"
#include "ActiveTestShow.h"
#include "VehicleInfo.h"
#include "ShortTestShow.h"
#include "SpecialFuncTestShow.h"


#define adsMB_NoButton	MSG_MB_NOBUTTON
#define adsMB_OK		MSG_MB_OK
#define adsMB_YesNo		MSG_MB_YESNO

#define	adsMB_Cancel			MB_NO
#define	adsMB_OKCancel			MSG_MB_YESNO

#define adsIDYES		MB_YES
#define	adsIDNO			MB_NO
#define	adsIDCANCEL		MB_NO

#define	adsIDBACK		ID_MENU_BACK
#define	adsIDOK			MB_YES

#define adsIDSHORTTESTENTER	0x0D	//快速测试使用到,这里暂不使用

//用户按键键值
//#define	adsIDNOCLICK	0x00
//#define	adsIDOK			0x01
//#define	adsIDCANCEL		0x02
//#define adsIDYES		0x03
//#define	adsIDNO			0x04
//#define adsIDRETRY		0x05
//#define	adsIDIGNORE		0x06
//#define	adsIDABORT		0x07
#define	adsIDPREV		0x08
#define	adsIDNEXT		0x09
//#define	adsIDFINISH		0x0A
//#define	adsIDBACK		0x0B
#define	adsIDCHANNEL	0x0C //AUDI 
//#define	adsIDSHORTTESTENTER	0x0D //SHORTTESTENTER


class CDisplay  
{
public:
	CDisplay() : 
	  Menu(m_Menu),
	  TroubleCode(m_TroubleCode),
	  DataStream(m_DataStream),
	  ActiveTest(m_ActiveTest),
	  MultiSelect(m_MultiSelect),
	  VehicleInfo(m_VehicleInfo),
	  ShortTest(m_ShortTest),
	  SpecialFuncTest(m_SpecialFuncTest)
	{
	}
	virtual ~CDisplay();
public:
	W_UINT8 Init();
	int IsHaveDataVdi();		//1:用到data.vdi 0:不用data.vdi -1:创建共享内存失败
	W_UINT8 InitNoDataVdi();
	void Destroy();


//	W_UINT16 MessageBox(char const *pContain, char const *pTitle, W_UINT16 nFlag = adsMB_OK, W_UINT16 nView = 0);
	W_UINT16 MessageBox(char *pContain, char *pTitle, W_UINT16 nFlag = adsMB_OK, DWORD dwColor = RGB(112, 112, 112), W_UINT16 nView = 0);
	W_UINT16 MessageBox(string strContain,  string strTitle,  W_UINT16 nFlag = adsMB_OK, W_UINT16 nView = 0);	
	W_UINT16 MessageBox(CBinary idContain,  CBinary idTitle,  W_UINT16 nFlag = adsMB_OK, W_UINT16 nView = 0);
	W_UINT16 SysMessageBox(W_UINT16 nMsgID, W_UINT16 nFlag = adsMB_OK);

public:
	class CInputValue 
    {
	public:
		CInputValue();
		CInputValue(W_INT16 iKey, char * strInput);
        // 构造函数
        CInputValue(const CInputValue& Input);

		virtual ~CInputValue();
	public:
		W_INT32 GetInteger32();
		W_INT16 GetUserKey();
		string GetString();
		string GetInput();
		// 复制类
		void operator = (const CInputValue Input);

	private:
		W_INT16 m_iUserKey;     // 用户按键键值
		string m_strUserInput;  // 用户输入字符串
	};

public:
	CInputValue Input(string strPrompt, string strTitle, string strDefaultString, string strFormat = "" , string strMinString = "", string strMaxString = "");
	CInputValue Input(CBinary idPrompt, CBinary idTitle, string strDefaultString, string strFormat = "" , string strMinString = "", string strMaxString = "");

public:
	W_UINT16 ProgressBar(string strPrompt, string strTitle, W_UINT16 uiPercen = 0);
	W_UINT16 ProgressBar(CBinary idPrompt, CBinary idTitle, W_UINT16 uiPercen = 0);
 
//目前没有调用到这三个函数,如有调用到,再增加
//	W_UINT16 Picture(string strPictureFileName, string strTitle, W_UINT16 nFlag = adsMB_OK, vector< XYPOINT > *point = NULL);
//	W_UINT16 Picture(CBinary idPictureFileName, CBinary idTitle, W_UINT16 nFlag = adsMB_OK, vector< XYPOINT > *point = NULL);
//	char *LoadString(W_UINT16 strID);

	
public:
    CMenuShow			&Menu		;
	CTroubleCodeShow	&TroubleCode;
	CDataStreamShow 	&DataStream ;
	CActiveTestShow 	&ActiveTest ;
	CMultiSelectShow	&MultiSelect;
	CVehicleInfo    	&VehicleInfo;
	CShortTestShow  	&ShortTest  ;
	CSpecialFuncTestShow 	&SpecialFuncTest ;
protected:
	CMenuShow                     m_Menu; 
	CTroubleCodeShow              m_TroubleCode; 
	CDataStreamShow               m_DataStream; 
	CActiveTestShow               m_ActiveTest; 
	CMultiSelectShow              m_MultiSelect; 
	CVehicleInfo                  m_VehicleInfo;
	CShortTestShow                m_ShortTest; 
	CSpecialFuncTestShow           m_SpecialFuncTest; 





public:
	int SendDTCId (vector<CBinary>);
	int SendVsfeType (W_UINT16 vt);
	int SendVehicleInfo (vector<CBinary>);

public:
	bool BinaryToBytePoint(CBinary bin,BYTE *pb,WORD &wLen);	//Binary转BYTE指针


};


//定义公共使用ID
#ifndef STRID_INFORMATION
#define STRID_INFORMATION					(CBinary("\x00\x00\x00\x00\x00\x01",6))
#endif

#ifndef STRID_ERROR
#define STRID_ERROR							(CBinary("\x00\x00\x00\x00\x00\x02",6))
#endif

#ifndef STRID_COMMUNICATING
#define STRID_COMMUNICATING					(CBinary("\x00\x00\x00\x00\x00\x03",6))
#endif

#ifndef STRID_COMMUNICATION_FAILED
#define STRID_COMMUNICATION_FAILED			(CBinary("\x00\x00\x00\x00\x00\x04",6))
#endif

#ifndef STRID_CONNECTING_ECU
#define STRID_CONNECTING_ECU				(CBinary("\x00\x00\x00\x00\x00\x05",6))
#endif

#ifndef STRID_CONNECT_ECU_FAILED
#define STRID_CONNECT_ECU_FAILED			(CBinary("\x00\x00\x00\x00\x00\x06",6))
#endif

#ifndef STRID_READINGCONTROLUNITVERSION
#define STRID_READINGCONTROLUNITVERSION		(CBinary("\x00\x00\x00\x00\x00\x07",6))
#endif

#ifndef STRID_READCONTROLUNITVERSION_FAILED
#define STRID_READCONTROLUNITVERSION_FAILED	(CBinary("\x00\x00\x00\x00\x00\x08",6))
#endif

#ifndef STRID_READINGFAULTCODE
#define STRID_READINGFAULTCODE				(CBinary("\x00\x00\x00\x00\x00\x09",6))
#endif

#ifndef STRID_READFAULTCODE_FAILED
#define STRID_READFAULTCODE_FAILED			(CBinary("\x00\x00\x00\x00\x00\x0A",6))
#endif

#ifndef STRID_NOFAULTCODEPRESENT
#define STRID_NOFAULTCODEPRESENT			(CBinary("\x00\x00\x00\x00\x00\x0B",6))
#endif

#ifndef STRID_ASK_CLEARFAULTCODE
#define STRID_ASK_CLEARFAULTCODE			(CBinary("\x00\x00\x00\x00\x00\x0C",6))
#endif

#ifndef STRID_CLEARINGFAULTCODE
#define STRID_CLEARINGFAULTCODE				(CBinary("\x00\x00\x00\x00\x00\x0D",6))
#endif

#ifndef STRID_CLEARFAULTCODE_FAILED
#define STRID_CLEARFAULTCODE_FAILED			(CBinary("\x00\x00\x00\x00\x00\x0E",6))
#endif

#ifndef STRID_CLEARFAULTCODE_OK
#define STRID_CLEARFAULTCODE_OK				(CBinary("\x00\x00\x00\x00\x00\x0F",6))
#endif

#ifndef STRID_ASK_END_DIAGNOSIS
#define STRID_ASK_END_DIAGNOSIS				(CBinary("\x00\x00\x00\x00\x00\x10",6))
#endif

#ifndef STRID_QUITING_SYSTEM
#define STRID_QUITING_SYSTEM				(CBinary("\x00\x00\x00\x00\x00\x11",6))
#endif

#ifndef STRID_ASK_START_SHORTTEST
#define STRID_ASK_START_SHORTTEST			(CBinary("\x00\x00\x00\x00\x00\x12",6))
#endif

#ifndef STRID_ASK_STOP_SHORTTEST
#define STRID_ASK_STOP_SHORTTEST			(CBinary("\x00\x00\x00\x00\x00\x13",6))
#endif

#endif // !defined(AFX_DISPLAY_H__CA6D9FB4_3190_4D5D_8AF8_D95EB9E0F836__INCLUDED_)
