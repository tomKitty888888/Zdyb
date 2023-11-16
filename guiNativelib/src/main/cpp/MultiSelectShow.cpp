// MultiSelectShow.cpp: implementation of the CMultiSelectShow class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "MultiSelectShow.h"
#include "Display.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

//保存着所有的数据流 (没有选中之前的所有数据流)
vector<CBinary> g_vecbinAllCDS;	

//定义一个全局变量,用来保存所有选中的数据流ID(CBinary) , 来自g_vecbinAllCDS选中部分.(可以是一部分,也可以是全部)
vector<CBinary> g_vecbinAllSelectedCDS;



extern CGui g_Gui;

CMultiSelectShow::CMultiSelectShow()
{
//	g_vecbinAllCDS.clear();	//不能在这里初始化这两个全局变量,否在没Add()一次就执行这里一次..(因为CDisplay每一次都附带执行)
//	g_vecbinAllSelectedCDS.clear();
}

CMultiSelectShow::~CMultiSelectShow()
{

}
CMultiSelectShow::CSelectedItemData::CSelectedItemData()
{
	CSelectedItemData::m_i16ScreenFirstLineItemSequence = 0;
	CSelectedItemData::m_i16ItemNumber = 0;
	CSelectedItemData::m_uiMaxSelectedNumber = 0xFFFF;	// 最多可被选中项数量

	BYTE ucValue = 0x00;
	CSelectedItemData::m_binMaskCode += ucValue;
}

//最多被选中数量
void CMultiSelectShow::CSelectedItemData::SetMaxSelectedNumber(W_UINT16 uiMaxSelectedNumber)
{
	CSelectedItemData::m_uiMaxSelectedNumber = uiMaxSelectedNumber;
}

//某一项目是否被选中true,否则false
bool CMultiSelectShow::CSelectedItemData::IsSelected (W_INT16 iSequence)
{
/* 这种方式只能选择当前页
	BYTE bSelectedItem[255] = {0,};
	BYTE bSelectedNum = g_Gui.CdsSelectGetItem(bSelectedItem);
	
	for (WORD w=0; w<bSelectedNum; w++)
	{
		if (iSequence == bSelectedItem[w])
		{
			//从所有数据流中记录下 被选中的数据流
			g_vecbinAllSelectedCDS.push_back(g_vecbinAllCDS[iSequence]);

			return true;
		}
	}
*/

	/*这种方式居然跑了很一段时间,程序OK, 奇怪, 暂不分析, 略过
	for (WORD w=0; w<g_vecbinAllCDS.size(); w++)
	{
		if (iSequence == w)
		{
			//从所有数据流中记录下 被选中的数据流
			g_vecbinAllSelectedCDS.push_back(g_vecbinAllCDS[iSequence]);

			return true;
		}
	}
	*/

	for (WORD w=0; w<g_vecbinAllCDS.size(); w++)
	{
		if (iSequence == w)
		{
			//从所有数据流中记录下 被选中的数据流
			g_vecbinAllSelectedCDS.push_back(g_vecbinAllCDS[iSequence]);

			return true;
		}
	}


	return false;
}

//取得多选项目数量
W_INT16 CMultiSelectShow::CSelectedItemData::GetItemNumber ()
{
	return CSelectedItemData::m_i16ItemNumber;
}

/***************************************************************************************
功    能：设定多选项目数量
参数说明：W_INT16 nNumber 多选项数量
返 回 值：错误代码
说    明：该函数在CMultiSelectShow.Show()中被自动调用，修正成实际的项目数量
**************************************************************************************/
W_INT16 CMultiSelectShow::CSelectedItemData::SetItemNumber (W_INT16 nNumber)
{
	W_INT16 iByteNum = (nNumber-1)/8 + 1;  // 实际需要的掩码长度
	W_INT16 iMaskLenth = CSelectedItemData::m_binMaskCode.GetSize();

	CSelectedItemData::m_i16ItemNumber = nNumber;

	// 实际需要的掩码长度不够
	if(iMaskLenth < iByteNum)
	{
		iMaskLenth = (iMaskLenth<0)?0:iMaskLenth;

		BYTE ucValue = 0x00;
		for(W_INT16 iIndex = iMaskLenth; iIndex < iByteNum; iIndex++)
		{					
			CSelectedItemData::m_binMaskCode += ucValue;
		}
	}
	else if(iMaskLenth > iByteNum)
	{
		// 保存旧的掩码
		CBinary binTemp;
		for(W_INT16 iIndex = 0; iIndex < iByteNum; iIndex++)
		{
			binTemp += CSelectedItemData::m_binMaskCode[iIndex];
		}

		CSelectedItemData::m_binMaskCode = binTemp;
	}

	return true;
}

/***************************************************************************************
功    能：设置被选中项
参数说明：W_INT16 iSequence 被测试项序号，如为SID_ALL_ITEM则针对所有项有效
         bool IsSelected 是否被选中标志，被选中-true 不被选中-false 
返 回 值：错误代码 
说    明：无
**************************************************************************************/
W_INT16 CMultiSelectShow::CSelectedItemData::SetSelectedItem (W_INT16 iSequence, bool IsSelected)
{
	if(iSequence > CSelectedItemData::m_i16ItemNumber-1)
	{
		return EC_OVER_ITEM_NUM;
	}

	// 掩码所能表达的位数
	W_INT16 iBitSum = 8*m_binMaskCode.GetSize();  

	// 如果长度不够
	if(iSequence > iBitSum-1 )
	{
		return EC_OVER_MASK_LEN;
	}

	// 如果iSequence参数为SID_ALL_ITEM，设置所有为被选中
//	if(SID_ALL_ITEM == iSequence)
	if(-1 == iSequence)
	{
		for(W_INT16 iIndex = 0;  iIndex < m_binMaskCode.GetSize(); iIndex++)
		{
			m_binMaskCode.SetAt(iIndex, IsSelected?0xFF:0x00);
		}
	}
	else 
	{	
		W_INT16 iIndexByte = iSequence/8;         // 字节序号
		char chCode = CSelectedItemData::m_binMaskCode.GetAt(iIndexByte);

		W_INT16 iIndexBit = (iSequence)%8;	        // 位序号
		char chTemp = 0x01<<(7-iIndexBit);

		m_binMaskCode.SetAt(iIndexByte, IsSelected ? (chCode|chTemp) : (chCode&~chTemp));
	}


	return true;
}

/***************************************************************************************
功    能：取得退出界面时用户按键键值
参数说明：无
返 回 值：用户按键键值 
说    明：该函数返回值与CMultiSelectShow.Show()的返回值相同，建议诊断程序员直接使用CMultiSelectShow.Show()的返回值
**************************************************************************************/
W_INT16 CMultiSelectShow::CSelectedItemData::UserClickButtonKeyValue ()
{
	return CSelectedItemData::m_iUserClickButtonKeyValue;
}

void CMultiSelectShow::Init(const char* pTitle)
{
	g_vecbinAllCDS.clear();	//清除所有数据流容器

	g_vecbinAllSelectedCDS.clear();  //清除所有选中数据流的容器

	g_Gui.CdsSelectInit();
}
void CMultiSelectShow::Init (string strTitle)
{
	Init();
}
void CMultiSelectShow::Init(CBinary idTitle)
{
	Init();
}


W_INT16 CMultiSelectShow::Add(CBinary idDataStream)
{
	BYTE *pbDataStream = new BYTE [idDataStream.GetSize()+1];
	WORD wLen = 0;
	CDisplay dis;
	if (!dis.BinaryToBytePoint(idDataStream,pbDataStream,wLen))
	{
		delete [] pbDataStream;
		return 0;
	}

	g_Gui.CdsSelectAdd(pbDataStream,(BYTE)wLen);

	delete [] pbDataStream;

	g_vecbinAllCDS.push_back(idDataStream);		//记录下数据流,加到所有数据流的容器中

	int iTest = g_vecbinAllCDS.size();

	return 0;
}
W_INT16 CMultiSelectShow::Add(string strMutiSelectedItem)
{
	g_Gui.CdsSelectAdd((char *)strMutiSelectedItem.c_str(),(char *)"");  //单位没了.

	//g_vecbinAllCDS.push_back(idDataStream);		//记录下数据流,加到所有数据流的容器中

	return 0;
}


/***************************************************************************************
功    能：添加多选项的说明
参数说明：内容文本
返 回 值：错误代码，成功为0
说    明：无
**************************************************************************************/
W_INT16 CMultiSelectShow::AddMsg(string strMsg)
{
	//m_pSendAndReceive->Add(strMsg);
	m_strMsg = strMsg;
	return 0;
}


bool CMultiSelectShow::Show(CSelectedItemData& SelectedData)
{
	BYTE nKey = g_Gui.CdsSelectShow();
	if (nKey == ID_CDS_VIEW)
	{
		return true;
	}

	return false;
}
bool CMultiSelectShow::Show(CSelectedItemData& SelectedData,W_INT16 &iSelectedIndex,BYTE iFlag)
{
	return Show(SelectedData);
}



