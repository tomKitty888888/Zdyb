#pragma once

#include <string>

using namespace std;

class CBase64;
extern CBase64 g_Base64;

class CBase64 
{

public:
	//base64±àÂë
	int Base64_Encode(const unsigned char* inputBuffer, int inputCount, char* outputBuffer);

	//base64½âÂë
	int Base4_Decode(const char * inputBuffer, int inputCount, unsigned char* outputBuffer);

	//url±àÂë
	string UrlEncode(const string& str, string &dst);

	//url½âÂë
	string UrlDecode(const string& str, string &dst);

	unsigned char ToHex(unsigned char x);

	unsigned char FromHex(unsigned char x);
};





