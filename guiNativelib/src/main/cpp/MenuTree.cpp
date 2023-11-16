// MenuTree.cpp: implementation of the CMenuTree class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "MenuTree.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

extern CGui g_Gui;

//定义两个全局变量,保存TaskID是否在运行和TaskID的值; 供其它地方调用
bool g_bTaskIdRunning = false;
WORD g_wTaskIdValue = 0x0000;


FILE *g_pFile = NULL;

CMenuTree::CMenuTree()
{
	m_vecMenuLayer.clear();
}

CMenuTree::~CMenuTree()
{

}


#ifndef _TASKIDTYPE
void* CMenuTree::SetTaskCallBackFunction(W_INT16 (*pfnTask) (W_INT16 iTaskId,  CBinary idSelectedText))
{
	void* PreTaskFunc = (void*)(*m_pfnTask);
	m_pfnTask = pfnTask;
	return PreTaskFunc;
}
#else
void* CMenuTree::SetTaskCallBackFunction(W_UINT  (*pfnTask) (W_UINT  iTaskId,  CBinary idSelectedText))
{
	void* PreTaskFunc = (void*)(*m_pfnTask);
	m_pfnTask = pfnTask;
	return PreTaskFunc;
}
#endif

int CMenuTree::ShowMenu (CBinary binIDMenu)
{
#ifndef _TASKIDTYPE
			unsigned short iTaskID = 0;
#else
			unsigned int iTaskID = 0;
#endif

	W_INT16 iTaskMarkNum = 0;

	while (1)
	{
		//检测任务m_pMapGuiBuff是否是任务ID,是则执行callback函数.---诊断程序会去new一个类
		//然后清除m_pMapGuiBuff继续等待是否是任务ID

		Sleep(1);
		if (g_Gui.m_pMapGuiBuff[0] != 0x55)continue;				//55
		if (g_Gui.m_pMapGuiBuff[1] != DIRECTION_VDI2GUI)continue;	//02
		if (g_Gui.m_pMapGuiBuff[2] != FORM_MENU)continue;			//01
		if (g_Gui.m_pMapGuiBuff[3] != 0x10)continue;				//10

//		iTaskID = DWORD(g_Gui.m_pMapGuiBuff[4]<<24 | g_Gui.m_pMapGuiBuff[5]<<16 | g_Gui.m_pMapGuiBuff[6]<<8 | g_Gui.m_pMapGuiBuff[7]);

		BYTE b1 = (BYTE)g_Gui.m_pMapGuiBuff[4];
		BYTE b2 = (BYTE)g_Gui.m_pMapGuiBuff[5];
		BYTE b3 = (BYTE)g_Gui.m_pMapGuiBuff[6];
		BYTE b4 = (BYTE)g_Gui.m_pMapGuiBuff[7];
		iTaskID = b1<<24|b2<<16|b3<<8|b4;

		g_bTaskIdRunning = true;
		g_wTaskIdValue = iTaskID;
		
		//获取路径,整理成要保存的文件名字   (已经在VDI中实现了)
//		string strPathName = g_Gui.GetFilePathName(iTaskID);
//		g_pFile = fopen((char *)strPathName.c_str(),"wb");
		
		memset(g_Gui.m_pMapGuiBuff,0,0xFFF0); //清除共享内存等待下一个TaskID

		g_Gui.SendMessage_TaskIdRunning(true,iTaskID); //1.发消息给VDI,告知TaskID在运行

		//test output
//		char szTemp[50];
//		sprintf(szTemp,"\r\nTask ID == %08X\r\n",iTaskID);
//		OutputDebugString(szTemp);
		//test end


		//处理回调函数
		if(m_pfnTask != NULL)
		{
			iTaskMarkNum = (*m_pfnTask)(iTaskID, CBinary("\x00",1)); //这里参数2没用到,只随便传了个00回去
		}
		else
		{
			iTaskMarkNum = -1;
		}
		
		g_Gui.SendMessage_TaskIdRunning(false,iTaskID); //2.发消息给VDI,告知TaskID已经运行完毕

		g_bTaskIdRunning = false;
		g_wTaskIdValue = 0x0000;

//		if (g_pFile != NULL) //已经在VDI中实现了
//		{
//			fclose(g_pFile);
//		}
//		g_pFile = NULL;
	}

	return iTaskMarkNum;
}



