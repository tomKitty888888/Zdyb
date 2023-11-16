#ifndef _RunDate_
#define _RunDate_

//#include "DiagnosisUnit.h"
#include "adsStd.h"	// Added by ClassView
#include "sysstr.h"
#include "Binary.h"
#include "BaseDataStream.h"
#include <map>

#define Text_Title								CBinary()
#define Text_NoSupport							CBinary("\x00\x00\x00\x00\x00\x30",6)
#define Text_NoStream							CBinary("\x00\x00\x00\x00\x00\x31",6)
#define Text_NoVersion							CBinary("\x00\x00\x00\x00\x00\x32",6)
#define Text_Lost_EnterSysDate					CBinary("\x00\x00\x00\x00\x00\x33",6)
#define Text_Lost_menuDate						CBinary("\x00\x00\x00\x00\x00\x34",6)
#define Text_Lost_AddressDate					CBinary("\x00\x00\x00\x00\x00\x35",6)
#define Text_Load_Stream						CBinary("\x00\x00\x00\x00\x00\x36",6)
#define Text_Loading_Actuator					CBinary("\x00\x00\x00\x00\x00\x37",6)
#define Text_ErrorBit							CBinary("\x00\x00\x00\x00\x00\x38",6)
#define Text_ErrorDate							CBinary("\x00\x00\x00\x00\x00\x39",6)
#define Text_ErrorInput							CBinary("\x00\x00\x00\x00\x00\x3a",6)
#define Text_ErrorActuator						CBinary("\x00\x00\x00\x00\x00\x3b",6)
#define Text_Errorprotocol						CBinary("\x00\x00\x00\x00\x00\x3C",6)
#define Text_Undefine_state						CBinary("\x00\x00\x00\x00\x00\x3d",6)
#define Text_Undefine							CBinary("\x00\x00\x00\x00\x00\x3e",6)
#define Text_Succsess_Write						CBinary("\x00\x00\x00\x00\x00\x3f",6)
#define Text_Succsess_Execute					CBinary("\x00\x00\x00\x00\x00\x40",6)
#define Text_Fail_Write							CBinary("\x00\x00\x00\x00\x00\x41",6)
#define Text_Fail_Execute						CBinary("\x00\x00\x00\x00\x00\x42",6)
#define Text_Fail_ConnectVCI					CBinary("\x00\x00\x00\x00\x00\x43",6)
#define Text_Fail_ConnectECU					CBinary("\x00\x00\x00\x00\x00\x44",6)
#define Text_Fail_EnterActuator					CBinary("\x00\x00\x00\x00\x00\x45",6)
#define Text_Ready_Test							CBinary("\x00\x00\x00\x00\x00\x46",6)
#define Text_Writing							CBinary("\x00\x00\x00\x00\x00\x47",6)
#define Text_NoD								CBinary("\x00\x00\x00\x00\x00\x48",6)
#define Text_EnterActuator						CBinary("\x00\x00\x00\x00\x00\x49",6)
#define Text_TestConditions						CBinary("\x00\x00\x00\x00\x00\x4a",6)
#define Text_Out_Actuator						CBinary("\x00\x00\x00\x00\x00\x4b",6)
#define Text_CanNotNULL							CBinary("\x00\x00\x00\x00\x00\x4c",6)




enum Date{
	e_EnterSys,
	e_Menu,
	e_Ver,
	e_ReadCode,
	e_ClearRode,
	e_ReadStream,
	e_Actuator,
	e_SpecialFunction,
	e_calibration,
	e_WriteBrush
};



struct EnterData
{
	BYTE bProtocl;
	int dwBaud[5];
	int bTargeAdress[5];
	int bSourseAdress[5];
	int bPin[5];
	BYTE bPinNum;
	BYTE bTIdNum;
	BYTE bSIdNum;
	BYTE dwBaudNum;
	vector<CBinary> EnterCmd;
	BYTE SetVci[70];
	BYTE VciNum;
};

struct MenuData
{
	BYTE bMenu;
};

struct VerData
{
	vector<string> bVerName;
	vector<CBinary> bVerCmd;
	vector<BYTE> bMode;
	vector<string> bVerType;
	vector<string> bFormula;
	BYTE  VerNumber;
};

struct CodeData
{
	vector<string> binDtcId;
	vector<CBinary> bCodeCmd;
	vector<CBinary> bClearCmd;
	vector<BYTE> bCodePos;
	vector<BYTE> bCodeLen;
	vector<BYTE> bCodeBit;
	vector<BYTE> bCodeCheck;
	BYTE bMode;
};


struct OneButton
{
	vector<CBinary> ActCmd;
	vector<BYTE> SendNum;


};

struct ActData
{	
	string ActName;
	BYTE ButtonNum;
	string bStream;
	vector<string> groupName;
	BYTE groupNum;
	vector<int> ButtonName;
	CBinary transformCmd;
	vector<OneButton> Button;
	string ActCondition;
};

struct WriteBoxDate
{
	string OnName;
	string Hintinformation;
	CBinary Cmd;
	int Max;
	int Min;
	int Len;
	int BegPos;
	int WriteLen;
	float Ratio;
	string limit;
	char Offsettimes;
	char Offset1[50];
	CBinary TranSend;
};


#endif




