// Cummins_Communication.h: interface for the CCummins_Communication class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_CUMMINS_COMMUNICATION_H__91ACE3FA_15A7_452F_A2CA_BB3D1301973E__INCLUDED_)
#define AFX_CUMMINS_COMMUNICATION_H__91ACE3FA_15A7_452F_A2CA_BB3D1301973E__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <time.h>

//#include "DiagnosisUnit.h"
#include "adsStd.h"
#include "sysstr.h"
#include "Binary.h"
//#include "BaseDataStream.h"
#include "ReceiveFrame.h"

class CCummins_Communication  
{
public:
	CCummins_Communication();
	virtual ~CCummins_Communication();

private:
	CBinary GetRecvCommand(CBinaryGroup bgRecv);
	CBinary RecvOneFrame(void);
	void DebugStringEx(CBinary binCmd);

public:
	BYTE m_nEcuAddr;
	CBinary m_binRecv;				//"y浽(Ч)
	vector<DWORD> m_vecdwPid;		//ЧPID(ЧPID,ЧPID)
	
	bool m_bCount;
	BYTE m_bCount0x20;
	BYTE m_bCount0x01;

	WORD m_nRecvNum;
	BYTE m_nRecvFrm;

	bool m_bDelay;

public:
	bool RecvCmdResult(DWORD dwCanId,DWORD dwTimeout=5*1000);
	bool SendRecvCmd(BYTE nSid,DWORD dwAddr,CBinary binData,int nOffset=0,bool bRecvEnable=false);
	bool SendRecvCmd(CBinary binSend,DWORD dwRecvCanID,CBinary &binRecv);
	bool SendRecvCmd(CBinaryGroup &bgRecv,CBinary binData,DWORD dwParam=0x00000111,bool bRecvEnable=true);
	bool SendRecvCmd(CBinaryGroup &bgRecv,vector<DWORD> vecdwPid,DWORD dwParam=0x00000110,bool bRecvEnable=true);
	bool SendRecvCmd(CBinaryGroup &bgRecv,DWORD dwPid,DWORD dwParam=0x00000110,bool bRecvEnable=true);
	bool SendRecvCmd(CBinaryGroup bgSend,CBinaryGroup &bgRecv,bool bRecvEnable=true);
	bool SendCmd_ClaimedName();
	CBinaryGroup SendRecvCmd_ClaimedName(CBinary binSend);
	void SetEcuAddr(BYTE nEcuAddr);
	bool SendCmd(CBinary binSend);
	CBinary RecvCmd(DWORD dwCanID=0);
	bool SendRecvCmd(BYTE nSid,DWORD dwAddr,DWORD dwSize=4);

};

#endif // !defined(AFX_CUMMINS_COMMUNICATION_H__91ACE3FA_15A7_452F_A2CA_BB3D1301973E__INCLUDED_)
