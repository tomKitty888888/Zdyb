#ifndef  _MY_HTTP_H
#define  _MY_HTTP_H

#include <string>
using namespace std;

//#define _WIN32_

#ifdef  _WIN32_
#define _WINSOCK_DEPRECATED_NO_WARNINGS
#endif //  WIN32

#define _STATUS_OK_CODE_ 200

class  CMyHttp
{
public:
	 CMyHttp();
	~ CMyHttp();

public:
	int SocketHttp(string host, string request, string &response);

	int PostData(string host, string path, string post_content, string &response);

	int GetData(string host, string path, string get_content, string &response);

	int ExtractStr(string strResponse, string &strResponseResult);
};

#endif // ! _MY_HTTP_H
