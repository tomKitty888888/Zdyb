#include "CJsonObject.h"

int CJsonObject::CreateSecurityAlgorithm(int nType, int nMaskS, int * pMask, int nInputS, char * pInputChar, int nFlag, string &strRequest)
{
	char *req_string = NULL;  //返回值
	cJSON *jsonAlgorithm = NULL;
	cJSON *jsonMask = NULL;
	cJSON *jsonMaskLength = NULL;
	cJSON *jsonInputLength = NULL;
	cJSON *jsonInput = NULL;

	//创建一个json对象，{}括起来
	cJSON* obj = cJSON_CreateObject();
	if (obj == NULL)
	{
		cJSON_Delete(obj);
		return 0;
	}

	//创建"Algorithm":  键值对
	jsonAlgorithm = cJSON_CreateNumber(nType);
	if (jsonAlgorithm == NULL)
	{
		cJSON_Delete(obj);
		return 0;
	}
	cJSON_AddItemToObject(obj, "Algorithm", jsonAlgorithm);

	//创建"InputLength":键值对
	jsonInputLength = cJSON_CreateNumber(nInputS);
	if (jsonInputLength == NULL)
	{
		cJSON_Delete(obj);
		return 0;
	}
	cJSON_AddItemToObject(obj, "InputLength", jsonInputLength);

	//创建"Input"键值对
	jsonInput = cJSON_CreateString(pInputChar);
	if (jsonInput == NULL)
	{
		cJSON_Delete(obj);
		return 0;
	}
	cJSON_AddItemToObject(obj, "Input", jsonInput);
	
	//创建Mask数组,数组内容为十六进制字符串
	jsonMask = cJSON_CreateArray();
	if (jsonMask == NULL)
	{
		cJSON_Delete(obj);
		return 0;
	}

	for (int i = 0; i < nMaskS; i++)
	{
		char szBuffer[1024] = { 0 };
		sprintf(szBuffer, "0X%x", pMask[i]);	

		cJSON_AddItemToArray(jsonMask, cJSON_CreateString(szBuffer));
	}

	cJSON_AddItemToObject(obj, "Mask", jsonMask);

	if (nFlag != -1)
	{
		nMaskS = nFlag;
	}

	//创建 "MaskLength": 键值对
	jsonMaskLength = cJSON_CreateNumber(nMaskS);
	if (jsonMaskLength == NULL)
	{
		cJSON_Delete(obj);
		return 0;
	}
	cJSON_AddItemToObject(obj, "MaskLength", jsonMaskLength);

	req_string = cJSON_PrintUnformatted(obj);

	strRequest = req_string;

	cJSON_Delete(obj);
	return 1;
}

int CJsonObject::ParseJsonString(char * pString, int & nInputLen, string &strInputChar, int & nOutputLen, string &strOutputChar, string &strMessage)
{
	cJSON *json = NULL;

	cJSON *jsonCode = NULL;
	cJSON *jsonMessage = NULL;
	cJSON *jsonInputLen = NULL;
	cJSON *jsonInputChar = NULL;
	cJSON *jsonOutputLen = NULL;
	cJSON *jsonOutputChar = NULL;
	cJSON *jsonResult = NULL;

	json = cJSON_Parse(pString);
	if (json == NULL)
	{
		cJSON_Delete(json);
		return -9;
	}

	jsonCode = cJSON_GetObjectItem(json, "code");
	string strCode;
	if (jsonCode != NULL && jsonCode->type == cJSON_String)
	{
		strCode = jsonCode->valuestring;
	}
	else
	{
		cJSON_Delete(json);
		return -9;
	}

	if (strCode == "0")  //当前算法不在算法库
	{
		cJSON_Delete(json);
		return -10;
	}
	else if (strCode == "-1") //传递给服务器的json包存在问题(由于客户端用代码封装json包，可能性低)
	{
		cJSON_Delete(json);
		return -11;
	}
	else if (strCode == "-2") //程序超时(没有等到服务器程序及时处理)
	{
		cJSON_Delete(json);
		return -12;
	}

	jsonMessage = cJSON_GetObjectItem(json, "message");
	string strMessageTmp;
	if (jsonMessage != NULL && jsonMessage->type == cJSON_String)
	{
		strMessageTmp = jsonMessage->valuestring;

		//将utf-8转换为ANSI(暂时没有显示message的信息，先不处理)

		strMessage = strMessageTmp;
	}
	else
	{
		cJSON_Delete(json);
		return -9;
	}

	jsonResult = cJSON_GetObjectItem(json, "result");
	if (jsonResult != NULL && jsonResult->type == cJSON_Object)
	{
		jsonInputLen = cJSON_GetObjectItem(jsonResult, "inputLength");
		if (jsonInputLen != NULL && jsonInputLen->type == cJSON_String)
		{
			//nInputLen = atoi(jsonInputLen->valuestring);

			string strInputLen = jsonInputLen->valuestring;
			sscanf(strInputLen.c_str(), "%d", &nInputLen);
		}
		else
		{
			cJSON_Delete(json);
			return -9;
		}

		jsonInputChar = cJSON_GetObjectItem(jsonResult, "input");
		if (jsonInputChar != NULL && jsonInputChar->type == cJSON_String)
		{
			strInputChar = jsonInputChar->valuestring;
		}
		else
		{
			cJSON_Delete(json);
			return -9;
		}

		jsonOutputLen = cJSON_GetObjectItem(jsonResult, "outputS");
		if (jsonInputLen != NULL && jsonInputLen->type == cJSON_String)
		{
			//nOutputLen = atoi(jsonOutputLen->valuestring);
			
			string strOutputLen = jsonOutputLen->valuestring;
			sscanf(strOutputLen.c_str(), "%d", &nOutputLen);
		}
		else
		{
			cJSON_Delete(json);
			return -9;
		}

		jsonOutputChar = cJSON_GetObjectItem(jsonResult, "outputChar");
		if (jsonOutputChar != NULL && jsonOutputChar->type == cJSON_String)
		{
			strOutputChar = jsonOutputChar->valuestring;
		}
		else
		{
			cJSON_Delete(json);
			return -9;
		}
	}
	else
	{
		cJSON_Delete(json);
		return -9;
	}


	cJSON_Delete(json); //释放cjson结构体内存

	return 1;
}
