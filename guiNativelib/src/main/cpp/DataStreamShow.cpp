// DataStreamShow.cpp: implementation of the CDataStreamShow class.
//
// 这个类有大改动!  不用原来那一套算法.  只是函数名字相同而已,内部算法已经全部改变了.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "DataStreamShow.h"
#include "Display.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////


//来自CMultiSelectShow定义的全局变量,保存着所有的数据流 (没有选中之前的所有数据流)
extern vector<CBinary> g_vecbinAllCDS;	

//来自CMultiSelectShow定义的全局变量,保存着所有选中的数据流ID(CBinary)
extern vector<CBinary> g_vecbinAllSelectedCDS;



extern CGui g_Gui;

CDataStreamShow::CDataStreamShow()
{
	//把当前要显示的数据流的索引保存到m_vecSelectedItem中(只有当前页)  ==不能在这里初始化,  DEL
//	m_vecSelectedItem.clear();
//	BYTE bSelectedItem[255] = {0,};
//	BYTE bSelectedNum = 0;
//	memset(bSelectedItem,0,255);
//	bSelectedNum = g_Gui.CdsSelectGetItem(bSelectedItem);
//	for (BYTE i=0; i<bSelectedNum; i++)
//	{
//		m_vecSelectedItem.push_back(bSelectedItem[i]);
//	}

}

CDataStreamShow::~CDataStreamShow()
{

}

W_UINT8 CDataStreamShow::AcceptMsg()
{
	//按键 接收信息在这里处理  上一页,下一页,  这里是处理 !=0 , 即有更新

	if (g_Gui.CdsCheckUpdate()) //更新当前页数据流的索引
	{
		m_vecSelectedItem.clear();

		BYTE bSelectedItem[255] = {0,};
		BYTE bSelectedNum = 0;
		memset(bSelectedItem,0,255);
		bSelectedNum = g_Gui.CdsSelectGetItem(bSelectedItem);
		for (BYTE i=0; i<bSelectedNum; i++)
		{
			m_vecSelectedItem.push_back(bSelectedItem[i]);
		}
		return 1;
	}

	return 0;
}

void CDataStreamShow::Init(CBinary idTitle, string strStdValueLibName, W_UINT16 uiType)
{
	Init();
}
void CDataStreamShow::Init(CBinary idTitle, CBinary idStdValueLibName, W_UINT16 uiType)
{
	Init();
}
void CDataStreamShow::Init(string strTitle, string strStdValueLibName, W_UINT16 uiType)
{
	g_Gui.CdsInit();

//原来代码OK,但移植的时候出了问题, 暂时没时间去管, 暂时先屏蔽 , 利用下面方式先解决
//	m_vecSelectedItem.clear();
//	BYTE bSelectedItem[255] = {0,};
//	BYTE bSelectedNum = 0;
//	memset(bSelectedItem,0,255);
//	bSelectedNum = g_Gui.CdsSelectGetItem(bSelectedItem);
//	for (BYTE i=0; i<bSelectedNum; i++)
//	{
//		m_vecSelectedItem.push_back(bSelectedItem[i]);
//	}

	//替代方式
	m_vecSelectedItem = m_vecMySelected;

}
void CDataStreamShow::Init(W_UINT16 uiTopLine,W_UINT16 uiAllCount,string strTitle, string strStdValueLibName, W_UINT16 uiType)
{
	//

	Init();
}
void CDataStreamShow::AddSelectIndex(BYTE *nIndex,BYTE nNum)
{
	BYTE i = 0;
	m_vecMySelected.clear();
	for (i=0; i<nNum; i++)
	{
		m_vecMySelected.push_back(nIndex[i]);
	}
}


//所有静态变量都是为处理第一部分用的
#include "MultiSelectShow.h"
static CBinary s_binFirstidDataStream = 0;
static bool s_bFirstGroupEnd = false;
static vector<CBinary> s_vecAllCds;
static bool bMultiCdsBack = false;
W_INT16 CDataStreamShow::Add(CBinary idDataStream, string strDataStreamValue, string strUnit/*=NULL*/)
{
	//第一部分: 这一部分是为了有些数据没有经过选择数据流的对话框,直接就调用数据流显示了.
	//所以这里自动加上数据流选择对话框
	//参数说明:
	//s_binFirstidDataStream : 记录下第一条数据的ID, 循环一次再返回到第一条数据流ID为一轮.
	//                         根据此可用一轮的时间自动加载数据流选择对话框
	//                         在show()之后在置为false,以等待下一次(返回后再次执行的情况)
	//s_bFirstGroupEnd       : 第一轮标记位,一轮执行完后置为true, 同时置g_vecbinAllCDS和g_vecbinAllSelectedCDS的值.
	//                         在show()之后在置为false,以等待下一次(返回后再次执行的情况)
	//s_vecAllCds            : 所有数据记录在这里, 以一轮为量,即Init,Add...Show,Init为一轮
	//bMultiCdsBack          : 此静态变量是为选择数据流界面返回(不点查看按钮而直接返回)处理的标记位
	//                       : 初始为false,点了返回置为true, 在show()之后又置为false,等待下一次读取数据流处理
	//原理:
	//如果全局变量保存的数据为了0(即没经过选择数据流对话框),则利用以上参数执行一轮这里第一部分(只执行一次)
	//在执行了一轮第一部分后,全局变量已经保存的数据流,接下来就和普通读取一样执行第二部分(不再执行第一部分)
	int iCdsAll = g_vecbinAllCDS.size();
	int iCdsSelected = g_vecbinAllSelectedCDS.size();
	//这里不用iCdsAll来做条件是因为如果上一次从读数据流处(经过有选择对话框的数据流)后再来执行此功能,则
	//iCdsAll一定不为0, 但iCdsSelected一定为0.  故屏蔽此条件.OK
	if (/*iCdsAll == 0 && */iCdsSelected == 0 && !s_bFirstGroupEnd)
	{
		if (s_binFirstidDataStream == idDataStream)
		{
			s_bFirstGroupEnd = true;
			g_Gui.CdsSelectInit(false);
			if (g_Gui.CdsSelectShow() == adsIDBACK)
			{
				bMultiCdsBack = true;
				return 0;
			}
			g_vecbinAllCDS = s_vecAllCds;
			g_vecbinAllSelectedCDS  = s_vecAllCds;
		}
		if (s_binFirstidDataStream.GetSize() == 0)
		{
			s_vecAllCds.clear();
			g_Gui.CdsSelectInit();
			s_binFirstidDataStream = idDataStream;
		}

		if (!s_bFirstGroupEnd)
		{
			BYTE nID[255] = "";
			BYTE nLen = idDataStream.GetSize();
			memcpy(nID,idDataStream.GetBuffer(),nLen);
			g_Gui.CdsSelectAdd(nID,nLen);
			s_vecAllCds.push_back(idDataStream);
			
			return 0;  //利用此返回以确保第一轮不会执行到第二部分去.
		}
		else
		{
			Init();
		}
	}


	//第二部分:
	//如果已经经过了数据流选择对话框的选择,则不执行第一部分,直接从这里开始执行

	AcceptMsg();

	for (BYTE i=0; i<g_vecbinAllCDS.size(); i++)	//遍历所有被选中的数据流
	{
		for (BYTE j=0; j<m_vecSelectedItem.size(); j++)
		{
			if (m_vecSelectedItem[j] == i)
			{
				if (idDataStream == g_vecbinAllCDS[i])		//如果要显示的值的数据流ID是被选中数据流中的一条
				{
					if (strUnit == "")
					{
						g_Gui.CdsAdd(i,(char *)strDataStreamValue.c_str());
					}
					else
					{
						g_Gui.CdsAdd(i,(char *)strDataStreamValue.c_str(),(char *)strUnit.c_str());
					}

					return 1;
				}
			}
		}

	}


	return 0;
}
W_INT16 CDataStreamShow::Add(CBinary idDataStream, string strDataStreamValue, CBinary idUnit)
{
	//用这种方式还没在GUI中实现, 有时间加一个接口在GUI中....DEL

	//暂时用这种原来方式处理, 这样处理就是把单位放弃掉了(除非文本里面有单位)
	//临时处理

	string strUnit = adsGetTextString(idUnit);
	
	return Add(idDataStream,strDataStreamValue,strUnit);

}


W_INT16 CDataStreamShow::Show (void)
{
	if (bMultiCdsBack)  //只处理第一部分的情况,即用户不点查看数据流,而直接点返回按钮,则清除变量.
	{
		bMultiCdsBack = false;

		g_vecbinAllCDS.clear();
		g_vecbinAllSelectedCDS.clear();
		s_vecAllCds.clear();
		s_binFirstidDataStream = 0;
		s_bFirstGroupEnd = false;
		return adsIDBACK;
	}

	BYTE bKey = g_Gui.CdsShow();
	if (bKey == ID_MENU_BACK)
	{
		g_vecbinAllCDS.clear();
		g_vecbinAllSelectedCDS.clear();
		s_vecAllCds.clear();
		s_binFirstidDataStream = 0;
		s_bFirstGroupEnd = false;
		return adsIDBACK;
	}

	return 0;
}
W_INT16 CDataStreamShow::Show (W_INT16 &iTop,W_INT16 &iNum)
{
	iTop = m_vecSelectedItem[0];
	iNum = m_vecSelectedItem.size();

	return Show();
}
