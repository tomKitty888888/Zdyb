// TroubleCodeShow.h: interface for the CTroubleCodeShow class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_TROUBLECODESHOW_H__5933C2CA_57C0_4919_AB95_53EB769B0356__INCLUDED_)
#define AFX_TROUBLECODESHOW_H__5933C2CA_57C0_4919_AB95_53EB769B0356__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Gui.h"
#include "adsStd.h"
#include "Binary.h"

class CTroubleCodeShow  
{
public:
	CTroubleCodeShow();
	virtual ~CTroubleCodeShow();

	void Init(const char *pTitle = NULL);
	void Init(string strTitle);
	void Init(CBinary idTitle);
	void Init(INT nItem, INT nColumnWidth = 100);        	//2019.10.22进行修改

	void *SetTroubleCodeCallBackFunction(string (*pfnCallBack) (CBinary idStroubleCode));  // 带函数指针参数的函数

	//第三个参数:要显示的字节个数; 第四个参数:未定义时是否显示字符(PCUB等)0：不显示 1：显示
	bool Add(CBinary idTroubleCode, string strTroubleStatus, int nShowByte=2, int nShowSymbol=0);
	bool Add(CBinary idTroubleCode, CBinary idTroubleStatus, int nShowByte=2, int nShowSymbol = 0);

	//2019.10.22进行修改，增加一个参数，第三个参数为故障码第三列要显示的内容
	bool Add(CBinary idTroubleCode, string strTroubleStatus, string strTroubleContent, int nShowByte=2, int nShowSymbol = 0);
	bool Add(CBinary idTroubleCode, CBinary idTroubleStatus, string strTroubleContent, int nShowByte=2, int nShowSymbol = 0);

	void Show();
	W_INT16 Show (W_INT16 &iSelNum);
	//故障码显示多列+带表头+可拖动
	//参数:列数。列宽为平均宽度
	void InitDtcMulti(INT nColumns);
	//第一个参数:列数；第二个参数:每一列的列宽比列
	void InitDtcMulti(INT nColumns, vector<INT> vecColumnWidthRatio);  

	//第一个参数:索引的ID; 第二个参数:当前是第几列; 第三个参数:故障码号要显示的字节数; 第四个参数:是否显示PCUB码
	//通过ID去data.vdi中查找要显示的字符串
	bool AddDtcMulti(CBinary idTroubleCode, INT nCurColumn, INT nShowByte = 2, INT nShowSymbol = 0);
	//第一个参数:要显示的整数类型
	bool AddDtcMulti(INT nContent, INT nCurColumn);
	//第一个参数:要显示的字符串类型
	bool AddDtcMulti(string strContent, INT nCurColumn);

	void ShowDtcMulti();

	BOOL StrToPCBU(BYTE* pCode, int nLen, int nShowByte, char &chCode, BYTE chCodeBuf[]);
	bool SetFlag(W_INT16 iFlag);
	string DefaultStroubleCodeCallBack(CBinary idTroubleCode);

	string GetSelectedItemText(W_INT16 iCol);

protected:
	string (*m_pfnGetTroubleCodeString) (CBinary idStroubleCode);  // 函数指针
private:
	string TanslateToPCBU(char chHigh, char chLow);

};

#endif // !defined(AFX_TROUBLECODESHOW_H__5933C2CA_57C0_4919_AB95_53EB769B0356__INCLUDED_)
