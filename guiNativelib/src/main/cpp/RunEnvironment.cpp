// RunEnvironment.cpp: implementation of the CRunEnvironment class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "RunEnvironment.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

extern CGui g_Gui;

CRunEnvironment::CRunEnvironment()
{

}

CRunEnvironment::~CRunEnvironment()
{

}

bool CRunEnvironment::GetDemoMode()
{
	string strPath = CRunEnvironment::GetDisplayDirectory();
	if (strPath.length() > 3)
	{
		strPath += "Config\\demo.txt";
		FILE *fp = NULL;
		fp = fopen(strPath.c_str(),"r");
		if (fp == NULL)return false;
		int ch = fgetc(fp); //0x00000031
		BYTE bDemo = (BYTE)(ch - 0x30);
		if (bDemo == 0)
		{
			fclose(fp);
			return false; //0演示模式,其它值都是实车测试模式
		}
		fclose(fp);
	}
	return true;
}

string CRunEnvironment::GetLanguage()
{
	string str = "CN"; //目前固定回CN , 只做中文版 , 如需要做其它语言版本需要改这里
	return str;
}

string CRunEnvironment::GetDisplayDirectory()
{
	return g_Gui.GetRootPath();
}


unsigned char CRunEnvironment::GetScreenType()
{
	//获取屏幕类型, 暂不管.  Benz中有用到.
	return 0;
}