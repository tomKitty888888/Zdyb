#pragma once

#include "cJSON.h"
#include <string>

using namespace std;

class CJsonObject
{
public:
	//��ȫ�㷨�ַ������
	int CreateSecurityAlgorithm(int nType, int nMaskS, int * pMask, int nInputS, char * pInputChar, int nFlag, string &strRequest);

	//��ȫ�㷨�ַ�������
	int ParseJsonString(char * pString, int & nInputLen, string &strInputChar, int & nOutputLen, string &strOutputChar, string &strMessage);
};

