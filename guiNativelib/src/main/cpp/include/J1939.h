// J1939.h: interface for the CJ1939 class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_J1939_H__F725E92D_E92D_4356_A24D_0DC844A0D57A__INCLUDED_)
#define AFX_J1939_H__F725E92D_E92D_4356_A24D_0DC844A0D57A__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

//#include <io.h>
#include <stdio.h>
//#include <tchar.h>
//#include <windows.h>

#include "Binary.h"
#include "Com.h"
#include "ReceiveFrame.h"

#define J1939_DM01	RequestsPGN(65226) //Active Diagnostic Trouble Codes
#define J1939_DM02	RequestsPGN(65227) //Previously Active Diagnostic Trouble Codes
#define J1939_DM03	RequestsPGN(65228) //Diagnostics Data Clear/Reset for Previously Active DTCs
#define J1939_DM04	RequestsPGN(65229) //Freeze Frame Parameters
#define J1939_DM05	RequestsPGN(65230) //Diagnostic Readiness 1
#define J1939_DM06	RequestsPGN(65231) //Pending DTCs
#define J1939_DM07	RequestsPGN(58112) //Command Non-continuously Monitored Test
#define J1939_DM08	RequestsPGN(65232) //Test Results for Non-continuously Monitored Systems
#define J1939_DM09	RequestsPGN(65233) //Oxygen Sensor Test Results
#define J1939_DM10	RequestsPGN(65234) //Non-continuously Monitored System Test Identifiers Support
#define J1939_DM11	RequestsPGN(65235) //Diagnostic Data Clear/Reset for Active DTCs
#define J1939_DM12	RequestsPGN(65236) //Emissions Related Active DTCs
#define J1939_DM13	RequestsPGN(57088) //Stop Start Broadcast
#define J1939_DM14	RequestsPGN(55552) //Memory Access Request
#define J1939_DM15	RequestsPGN(55296) //Memory Access Response
#define J1939_DM16	RequestsPGN(55040) //Binary Data Transfer
#define J1939_DM17	RequestsPGN(54784) //Boot Load Data
#define J1939_DM18	RequestsPGN(54272) //Data Security
#define J1939_DM19	RequestsPGN(54016) //Calibration Information
#define J1939_DM20	RequestsPGN(49664) //Monitor Performance Ratio
#define J1939_DM21	RequestsPGN(49408) //Diagnostic Readiness 2
#define J1939_DM22	RequestsPGN(49920) //Individual Clear/Reset Of Active And Previously Active DTC
#define J1939_DM23	RequestsPGN(64949) //Previously Active Emission Related Faults
#define J1939_DM24	RequestsPGN(64950) //SPN Support
#define J1939_DM25	RequestsPGN(64951) //Expanded Freeze Frame
#define J1939_DM26	RequestsPGN(64952) //Diagnostic Readiness 3
#define J1939_DM27	RequestsPGN(64898) //All Pending DTCs
#define J1939_DM28	RequestsPGN(64896) //Permanent DTCs
#define J1939_DM29	RequestsPGN(40448) //DTC Counts
#define J1939_DM30	RequestsPGN(41984) //Scaled Test Results
#define J1939_DM31	RequestsPGN(41728) //DTC To Lamp Association
#define J1939_DM32	RequestsPGN(41472) //Regulated Exhaust Emission Level Exceedance
#define J1939_DM33	RequestsPGN(41216) //Emission Increasing Auxiliary Emission Control Device Active Time
#define J1939_DM34	RequestsPGN(40960) //NTE Status
#define J1939_DM35	RequestsPGN(40704) //Immediate Fault Status
#define J1939_DM36	RequestsPGN(64868) //Harmonized Roadworthiness - Vehicle (HRWV)
#define J1939_DM37	RequestsPGN(64867) //Harmonized Roadworthiness - System (HRWS)
#define J1939_DM38	RequestsPGN(64866) //Harmonized Global Regulation Description (HGRD)
#define J1939_DM39	RequestsPGN(64865) //Cumulative Continuous MI ?C System (HCMI) 
#define J1939_DM40	RequestsPGN(64864) //Harmonized B1 Failure Counts (HB1C)
#define J1939_DM41	RequestsPGN(64863) //DTCs - A, Pending
#define J1939_DM42	RequestsPGN(64862) //DTCs - A, Confirmed and Active
#define J1939_DM43	RequestsPGN(64861) //DTCs - A, Previously Active
#define J1939_DM44	RequestsPGN(64860) //DTCs - B1, Pending
#define J1939_DM45	RequestsPGN(64859) //DTCs - B1, Confirmed and Active
#define J1939_DM46	RequestsPGN(64858) //DTCs - B1, Previously Active
#define J1939_DM47	RequestsPGN(64857) //DTCs - B2, Pending
#define J1939_DM48	RequestsPGN(64856) //DTCs - B2, Confirmed and Active
#define J1939_DM49	RequestsPGN(64855) //DTCs - B2, Previously Active
#define J1939_DM50	RequestsPGN(64854) //DTCs - C, Pending
#define J1939_DM51	RequestsPGN(64853) //DTCs - C, Confirmed and Active
#define J1939_DM52	RequestsPGN(64852) //DTCs - C, Previously Active
#define J1939_DM53	RequestsPGN(64721) //Active Service Only DTCs
#define J1939_DM54	RequestsPGN(64722) //Previously Active Service Only DTCs
#define J1939_DM55	RequestsPGN(64723) //Diagnostic Data Clear/Reset for All Service Only DTCs
#define J1939_DM56	RequestsPGN(64711) //Engine Emissions Certification Information
#define J1939_DM57	RequestsPGN(64710) //OBD Information

#define J1939_ERC1		RequestsPGN(61440) //Electronic Retarder Controller 1
#define J1939_EEC2		RequestsPGN(61443) //Electronic Engine Controller 2
#define J1939_EEC3		RequestsPGN(65247) //Electronic Engine Controller 3
#define J1939_SOFT		RequestsPGN(65242) //Software Identification
#define J1939_RC		RequestsPGN(65249) //Retarder Configuration
#define J1939_CI		RequestsPGN(65259) //Component Identification
#define J1939_CCVS1		RequestsPGN(65265) //Cruise Control/Vehicle Speed 1
#define J1939_LFE1		RequestsPGN(65266) //Fuel Economy (Liquid)
#define J1939_AMB		RequestsPGN(65269) //Ambient Conditions
#define J1939_IC1		RequestsPGN(65270) //Intake/Exhaust Conditions 1
#define J1939_VEP1		RequestsPGN(65271) //Vehicle Electrical Power 1
#define J1939_A1SCREGT1	RequestsPGN(64830) //Aftertreatment 1 SCR Exhaust Gas Temperature 1
#define J1939_AT1T1I	RequestsPGN(65110) //Aftertreatment 1 Diesel Exhaust Fluid Tank 1 Information
#define J1939_PropB_00	RequestsPGN(65280) //Proprietary B (first entry)

class CJ1939  
{
public:
	CJ1939();
	CJ1939(BYTE nEcuAddr,BYTE nToolAddr);
	virtual ~CJ1939();

private:
	BYTE m_nEcuAddr;  //初始化为全局地址 0xFF
	BYTE m_nToolAddr; //初始化为车载诊断地址1 0xFE
	bool m_bBroadcastRecvEnable;

public:
	void OutputDebugStringEx(CBinary binCmd);
private:
	CBinary BinaryGroup2Binary(CBinaryGroup bgRecv);
	CBinary RequestsPGNEx(DWORD dwPGN);
	DWORD m_dwTickCount;
public:
	bool SendCmd(CBinary binSend);
	CBinary RecvOneFrame();

public:
	void SetAddr(BYTE nEcuAddr,BYTE nToolAddr); //设置ECU和TOOL的地址字节
	void SetBroadcastRecvEnable(bool bEnable);  //设置是否优先接收广播帧

	BYTE Connect(DWORD dwBaud,BYTE nPin); //连接ECU(波特率,通讯引脚),如:(250000,0xE6)表示250K,6-14引脚
	                                      //返回0-OK,1-连接VCI失败,2-连接ECU失败

	CBinary RequestsPGN(DWORD dwPGN);

public:
	CBinaryGroup GetBroadcastCanId(DWORD dwBaud,BYTE bCanChannel);
	CBinaryGroup GetClaimedAddressAndName(DWORD dwBaud, BYTE bChannel ,BYTE bAddr);

};

#endif // !defined(AFX_J1939_H__F725E92D_E92D_4356_A24D_0DC844A0D57A__INCLUDED_)
