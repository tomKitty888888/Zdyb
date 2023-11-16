#pragma once

#include <string>

using namespace std;

class CBase64;
extern CBase64 g_Base64;

class CBase64 
{

public:
	//base64����
	int Base64_Encode(const unsigned char* inputBuffer, int inputCount, char* outputBuffer);

	//base64����
	int Base4_Decode(const char * inputBuffer, int inputCount, unsigned char* outputBuffer);

	//url����
	string UrlEncode(const string& str, string &dst);

	//url����
	string UrlDecode(const string& str, string &dst);

	unsigned char ToHex(unsigned char x);

	unsigned char FromHex(unsigned char x);
};





