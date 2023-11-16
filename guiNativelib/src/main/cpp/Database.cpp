// Database.cpp: implementation of the CDatabase class.
//
//////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Database.h"
#include "Display.h"

#include "Information.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

extern CGui g_Gui;

extern string g_strPath;

CDatabase::CDatabase()
{
	m_DataBaseType = 0xFF;
	m_strFileName = "";

//	m_DatabaseOpened = false;
}

CDatabase::~CDatabase()
{

}


//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////
/*
bool CDatabase::OpenAccessDatabase(string strFile)
{
	CoInitialize(NULL);

	HRESULT hr = S_OK;

	string strSQL = "";
	strSQL += _T("Provider=Microsoft.Jet.OLEDB.4.0;Data Source=");
	strSQL += g_strPath;
	strSQL += "data.mdb";
	strSQL += _T(";Jet OLEDB:Database Password=kj&^(fd8i9698#@xcvz");
	
//	string strSQLEx = "select * from "; //可以不用CreateInstance和Open
//	strSQLEx += strFile;

	try
	{
		if (SUCCEEDED(hr))
		{
			hr = m_AccessConnect.CreateInstance(__uuidof(Connection));
			if (hr != S_OK)return false;
			hr = m_AccessConnect->Open(strSQL.c_str(),"","",adModeUnknown);
			if (hr != S_OK)return false;

//			hr = m_AccessRecordSet.CreateInstance(__uuidof(Recordset));
//			if (hr != S_OK)return false;
//			hr = m_AccessRecordSet->Open(strSQLEx.c_str(),m_AccessConnect.GetInterfacePtr(),\
//				adOpenDynamic,adLockOptimistic,adCmdText);
//			if (hr != S_OK)return false;
		}
	}
	catch(...)
	{
		return false;
	}

	m_DatabaseOpened = true;
	
	return true;
}
bool CDatabase::CloseAccessDatabase()
{
	if (m_AccessRecordSet)
	{
		m_AccessRecordSet->Close();
		m_AccessRecordSet = NULL;
	}
	if (m_AccessConnect)
	{
		m_AccessConnect->Close();
		m_AccessConnect = NULL;
	}

	m_DatabaseOpened = false;

	::CoUninitialize();

	return true;
}

static string Binary2String(CBinary bin)
{
	char sz[10] = "";
	string str = "";
	for (WORD w=0; w<bin.GetSize(); w++)
	{
		sprintf(sz,"%02x",bin[w]);
		str += sz;
	}
	return str;
}
const vector<CBinary> CDatabase::SearchIdAccessDatabase(CBinary& Id,string strFileNameEx)
{
	vector<CBinary> binVector;

	string strId = Binary2String(Id); //有writeBuff和ReadBuffer呀

	string strFileName = m_strFileName.substr(0,m_strFileName.size()-3);
	//"select * from [CN_DS] where SID='[14a0403700a3]'";
	string strSQL = "select * from ";
	if (strFileNameEx.size() == 0)
	{
		strSQL += strFileName;
	}
	else
	{
		strSQL += strFileNameEx;
	}
	strSQL += " where SID='";
	strSQL += strId;
	strSQL += "'";

	string str = "";
	CBinary bin = 0;
	_variant_t RecordsAffected;
	_variant_t m_vt;
	try
	{
		m_AccessRecordSet = m_AccessConnect->Execute(strSQL.c_str() , &RecordsAffected, 1);
		m_vt = m_AccessRecordSet->GetCollect(_variant_t("SID"));
		if (m_vt.vt != VT_NULL)
		{
			str = (char *)(_bstr_t)(m_vt);

			char szTemp[100] = "";
			for (BYTE k=0; k<strId.size(); k++)
			{
				szTemp[k] = toupper(strId[k]);
			}
			szTemp[k] = '\0';
			strId = szTemp;
			
			if (str != strId)
			{
				return binVector;
			}
			char szItem[10] = "";
			for (BYTE i=1; i<=200; i++)
			{
				sprintf(szItem,"ITEM%03d",i);
				m_vt = m_AccessRecordSet->GetCollect(_variant_t(szItem));
				if (m_vt.vt != VT_NULL)
				{
					str = (char *)(_bstr_t)(m_vt);
					if (str.size() == 0)
					{
						return binVector;
					}

					//解密:Begin
					BYTE nDecryptIndex = 0x00;
					for (int m=0; m<Id.GetSize(); m++)
					{
						nDecryptIndex += Id[m];
					}
					Decrypt(nDecryptIndex,(BYTE *)str.c_str(),str.length());
					//解密:End

					bin.WriteBuffer(str.c_str(),str.size());
					binVector.push_back(bin);
				}
				else
				{
					return binVector;
				}
			}
		}
	}
	catch (...)
	{
		return binVector;
	}


	return binVector;
}
*/
bool CDatabase::Decrypt(BYTE nIndex, BYTE *pBuf, WORD wLen)
{
	BYTE nByte = 0x00;
	for (WORD w=0; w<wLen; w++)
	{
		nByte = pBuf[w];
		nByte = nByte ^ 0x3C;
//		if (w <= 255)
//		{
//			nByte = nByte ^ w;
//		}
//		else
//		{
//			nByte = nByte ^ ((BYTE)(w>>8));
//			nByte = nByte ^ ((BYTE)(w>>0));
//		}
////		nByte = nByte ^ nIndex;
//		BYTE tmp = ((nByte & 0x07) << 5);
//		nByte = ((nByte >> 3) | tmp);

		pBuf[w] = nByte;
	}

	return true;
}

//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////











bool CDatabase::IsOpen()
{
	return true;//
}
bool CDatabase::Open(W_UINT16 uiFileCodeName)
{
	m_DataBaseType = uiFileCodeName;
	return true;
}
bool CDatabase::Open(string strFile)
{
	m_strFileName = strFile;

	string strVehicleSystemName = CInformation::GetVehiclesSystemName();
//	if (strVehicleSystemName == "SHVW") //SHVW单独处理(数据量过大,采用数据库方式处理)
//	{
//		if (!OpenAccessDatabase(strFile))
//		{
//			return false;
//		}
//	}

	return true;
}
void CDatabase::Close()
{
	m_DataBaseType = 0xFF;
	m_strFileName = "";

	string strVehicleSystemName = CInformation::GetVehiclesSystemName();
//	if (strVehicleSystemName == "SHVW")
//	{
//		if (!CloseAccessDatabase())
//		{
//			return ;
//		}
//	}

}



//目前使用数据流文本的格式如下:
//数据流ID + TAB + 数据流内容 + TAB/，+ 单位 + TAB/，+ 显示格式 + TAB/，+ 算法 + TAB/，+ 读数命ID
//如:LIFAN,Airbag,0x00,0x01,0x00,0x02	"电源电压"	"V"	""	0	0x21,0xf0
//如有变动,需更改这里
//
//故障码(OBD2为例子)	ID 内容1 内容2
//
const vector<CBinary> CDatabase::SearchId(CBinary& Id)
{
	string strVehicleSystemName = CInformation::GetVehiclesSystemName();
//	if (strVehicleSystemName == "SHVW")
//	{
//		return SearchIdAccessDatabase(Id);
//	}

	
	vector<CBinary> binVector;
/*	
	if (m_DataBaseType == DB_DATA_STREAM)
	{
		for (BYTE i=1; i<=5; i++)
		{
			char szIdString[255] = "";
			BYTE nID[255];
			memcpy(nID, Id.GetBuffer(), Id.GetSize());
			if (!g_Gui.GetTextEx(TEXT_CDS,nID,Id.GetSize(),szIdString,i))return binVector;
			CBinary binCmd = 0;
			if (i == 1)
			{
				binVector.push_back(Id);
			}
			else if (i == 5)
			{
				g_Gui.String2Binary(szIdString,binCmd);
				binVector.push_back(binCmd);
			}
			else //目前只读出i==1是ID(直接push_back),i==5是对应的命令(比如:0x21,0x10), 其它的就push_back一个空进去暂时不用
			{
				binVector.push_back(binCmd);
			}
		}
		
	}
	else
	{
		//MessageBox(NULL,"CDatabase::SearchId其它方式还没写","CDatabase::SearchId其它方式还没写",MB_OK);

		//OBD2有故障码是两列内容的, 根据条件选择一列.
		//所以这里要写一个通用的函数去实现
	}
	*/

	strupr((char *)m_strFileName.c_str());
	BYTE bDataBaseType = 0x00;
	if (m_DataBaseType==DB_COMMAND || m_strFileName.find("CMD.DB")!=string::npos)
	{
		bDataBaseType = TEXT_OTHER;
	}
	else if (m_DataBaseType==DB_TEXT || m_strFileName.find("TXT.DB")!=string::npos)
	{
		bDataBaseType = TEXT_TXT;
	}
	else if (m_DataBaseType==DB_TROUBLE_CODE || m_strFileName.find("DTC.DB")!=string::npos)
	{
		bDataBaseType = TEXT_DTC;
	}
	else if (m_DataBaseType==DB_DATA_STREAM || m_strFileName.find("DS.DB")!=string::npos)
	{
		bDataBaseType = TEXT_CDS;
	}
	else
	{
		bDataBaseType = TEXT_OTHER;
	}

	for (BYTE i=1; i<20; i++)  //因为不知道有多少个,先读1-20个,读不到的表示没了
	{
		char szString[1024] = "";
		BYTE nID[255];
		memcpy(nID, Id.GetBuffer(), Id.GetSize());
		if (!g_Gui.GetTextEx(bDataBaseType,nID,Id.GetSize(),szString,i))return binVector;

		bool bIsCmd = false;
		CBinary binTemp = 0;
//		for (int j=0; j<strlen(szString)-1; j++)  //VW中有"1J0920xx017"这样串,但不是命令
//		{
//			if (szString[j]=='0' && szString[j+1]=='x' || szString[j+1]=='X')
//			{
//				g_Gui.String2Binary(szString,binTemp);
//				bIsCmd = true;
//				break;
//			}
//		}
		if (szString[0]=='0' && (szString[1]=='x' || szString[1]=='X') && szString[2]==',' )
		{
			bIsCmd = true;
			g_Gui.String2Binary(szString,binTemp);
		}
		if (bIsCmd)
		{
			binVector.push_back(binTemp);
		}
		else
		{
			binTemp.WriteBuffer((char *)szString,strlen(szString));
			binVector.push_back(binTemp);
		}
	}
	
	return binVector;
}








//这两个静态变量是为读取文件的时候的缓冲使用,以防止读取大量文本的时候耗时过长.
//第一次读取会保存到变量中, 之后再次读取,按ID对比,之前读过此ID的直接返回缓冲中的值.
static vector<CBinary> g_vecBinary;
static vector<string> g_vecString;
string adsGetTextString(CBinary binTextID)
{
	WORD w = 0;
	for (w=0; w<g_vecBinary.size(); w++)
	{
		if (binTextID == g_vecBinary[w])
		{
			return g_vecString[w];
		}
	}

	string str;
	CDisplay dis;
	BYTE nTextID[255] = {0,};
	WORD wLen = 0;
	if (!dis.BinaryToBytePoint(binTextID,nTextID,wLen))
	{
		return str;
	}
	
	
	char szText[255] = "";
	if (!g_Gui.GetText(nTextID,(BYTE)wLen,szText))
	{
		return str;
	}
	
	
	str = szText;

	if (w >= g_vecBinary.size())
	{
		g_vecBinary.push_back(binTextID);
		g_vecString.push_back(str);
	}

	return str;
}


