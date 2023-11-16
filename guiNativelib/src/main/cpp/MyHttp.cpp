#include "MyHttp.h"
#include <android/log.h>
#include "Debug.h"


#ifdef _WIN32_
	#include <winsock2.h>
	#include  <ws2tcpip.h>
	
	#pragma comment(lib, "ws2_32.lib")

#else
	#include <stdio.h>
	#include <stdlib.h>
	#include <netdb.h>		//�ṩ���ü���ȡ�����ĺ���
	#include <netinet/in.h> //�������ݽṹsockaddr_in
	#include <sys/ioctl.h>  //�ṩ��I/O���Ƶĺ���
	#include <sys/socket.h> //�ṩsocket���������ݽṹ
	#include <arpa/inet.h>  //�ṩIP��ַת������
	#include <unistd.h>
#include <errno.h>
#include <resolv.h>
#endif




CMyHttp::CMyHttp()
{
#ifdef _WIN32_
	//�˴�һ��Ҫ��ʼ��һ�£�����gethostbyname����һֱΪ��
	WSADATA wsa = {0};//һ�����ݽṹ������ṹ�������洢��WSAStartup�������ú󷵻ص�Windows Sockets����
	WSAStartup(MAKEWORD(2,2), &wsa);

#endif // WIN32

}

CMyHttp::~CMyHttp()
{
}

int CMyHttp::SocketHttp(string host, string request, string &response)
{
	__android_log_print(ANDROID_LOG_INFO,"MyHttp","SocketHttp=%d",::GetTickCount());
	//�����׽���
#ifdef _WIN32_
	SOCKET sockfd = socket(AF_INET, SOCK_STREAM, 0); //����TCP�׽���
	if (sockfd == INVALID_SOCKET)
	{
		int nError = WSAGetLastError();

		//���������
		WSACleanup();

		return -1;
	}
#else
	__android_log_print(ANDROID_LOG_INFO,"MyHttp","socket ");
	int sockfd = socket(AF_INET, SOCK_STREAM, 0); //����TCP�׽���
	if (sockfd == -1)
	{
		__android_log_print(ANDROID_LOG_INFO,"MyHttp","socket fail %d",errno);
		char fail[]="socket %d";
		sprintf(fail,"socket %d",-1);
		::lognet(true,fail,sizeof(fail));
		return -1;
	}
#endif

	//���÷��ͺͽ��ճ�ʱ
	int nRet = -1;
#ifdef _WIN32_
	int nNetSendTimeout = 30 * 1000; //30��
	int nNetRecvTimeout = 60 * 1000; //60�� code����500���24s

	//����ʱ��
	nRet = setsockopt(sockfd, SOL_SOCKET, SO_SNDTIMEO, (char*)&nNetSendTimeout, sizeof(int));
	if (nRet == -1)
	{
		closesocket(sockfd);
		WSACleanup();

		return -2;
	}

	//����ʱ��
	nRet = setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, (char*)&nNetRecvTimeout, sizeof(int));
	if (nRet == -1)
	{
		closesocket(sockfd);
		WSACleanup();
		return -2;
	}
#else
	struct timeval sendTimeout = { 30,0 }; //30s
	struct timeval recvTimeout = { 60,0 }; //60s
	//���÷��ͳ�ʱ
	__android_log_print(ANDROID_LOG_INFO,"MyHttp","setsockopt send ");
	nRet = setsockopt(sockfd, SOL_SOCKET, SO_SNDTIMEO, (char *)&sendTimeout, sizeof(struct timeval));
	if (nRet == -1)
	{
		__android_log_print(ANDROID_LOG_INFO,"MyHttp","setsockopt fail %d ",errno);
		char fail[]="setsockopt %d";
		sprintf(fail,"setsockopt %d",-2);
		::lognet(true,fail,sizeof(fail));
		return -2;
	}
	
	//���ý��ճ�ʱ
	__android_log_print(ANDROID_LOG_INFO,"MyHttp","setsockopt recv ");
	nRet = setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, (char *)&recvTimeout, sizeof(struct timeval));
	if (nRet == -1)
	{
		char fail[]="setsockopt %d";
		sprintf(fail,"setsockopt %d",-2);
		::lognet(true,fail,sizeof(fail));
		return -2;
	}
#endif

	//�������(�ض���IP�Ͷ˿�)��������
	struct sockaddr_in address;		//Internet����Ľṹ��
	struct hostent* server = NULL;

	address.sin_family = AF_INET;
	address.sin_port = htons(80);	//�˿ں�
	if(0!=res_init()){
		return -7;
	}
	server = gethostbyname(host.c_str());

	if (server == NULL || server->h_addr == NULL)
	{
#ifdef _WIN32_
		closesocket(sockfd);
		WSACleanup();
#endif
		{
			char fail[]="gethostbyname fail %d";
			sprintf(fail,"gethostbyname fail %d",errno);
			::lognet(true,fail,sizeof(fail));
		}
		return -3;
	}
	{

		::lognet(true,server->h_name,strlen(server->h_name));
	}
	__android_log_print(ANDROID_LOG_INFO,"MyHttp","gethostbyname %s %s",server->h_name,server->h_addr);


	memcpy((char*)&address.sin_addr.s_addr, (char*)server->h_addr, server->h_length);

	__android_log_print(ANDROID_LOG_INFO,"MyHttp","Aconnect=%d",::GetTickCount());
	if (-1 == connect(sockfd, (struct sockaddr*)&address, sizeof(address)))
	{
		int a=errno;
		a=a+1;
		a=a-1;
#ifdef _WIN32_
		closesocket(sockfd);
		WSACleanup();
#else
		close(sockfd);
#endif // _WIN32_

		char fail[]="connect %d %d";
		sprintf(fail,"connect %d %d",-4,errno);
		::lognet(true,fail,sizeof(fail));
		return -4;  //����ʧ��
	}
	__android_log_print(ANDROID_LOG_INFO,"MyHttp","Bconnect=%d",::GetTickCount());
	//��������
	long lSendLen = 0;
	long lSendOffset = 0;
	lSendLen = request.size();

	char *pSendBuf = new char[lSendLen + 1];
	memset(pSendBuf, 0, lSendLen + 1);
	memcpy(pSendBuf, request.c_str(), lSendLen);

	long lSendSize = 0;

	unsigned int nSize = 1024 * 2; //һ�η���2KB

	unsigned int nCount = 0;
	nCount = lSendLen / nSize;

	unsigned int nLetf = 0;
	nLetf = lSendLen % nSize;
	__android_log_print(ANDROID_LOG_INFO,"MyHttp","Asend=%d",::GetTickCount());
	for (unsigned int i = 0; i < nCount; i++)
	{
#ifdef _WIN32_
		lSendSize = send(sockfd, pSendBuf + nSize * i, nSize, 0);
#else	
		lSendSize = write(sockfd, pSendBuf + nSize * i, nSize);
#endif
		if (lSendSize == -1)
		{
#ifdef _WIN32_
			closesocket(sockfd);
			WSACleanup();
#endif
			char fail[]="write %d %d";
			sprintf(fail,"write %d %d",-5,errno);
			::lognet(true,fail,sizeof(fail));
			return -5;
		}
	}

	if (nLetf > 0)
	{
#ifdef _WIN32_
		lSendSize = send(sockfd, pSendBuf + nSize * nCount, nLetf, 0);
#else	
		lSendSize = write(sockfd, pSendBuf + nSize * nCount, nLetf);
#endif
		if (lSendSize == -1)
		{
#ifdef _WIN32_
			closesocket(sockfd);
			WSACleanup();
#endif
			char fail[]="write2 %d %d";
			sprintf(fail,"write2 %d %d",-5,errno);
			::lognet(true,fail,sizeof(fail));
			return -5;
		}
	}
	__android_log_print(ANDROID_LOG_INFO,"MyHttp","Bsend=%d",::GetTickCount());
	//��������

	char *pBuf = new char[lSendLen + 1024 * 2];
	memset(pBuf, 0x00, lSendLen + 1024 * 2);

	int offset = 0;
	int rc = 0;

#ifdef _WIN32_
	while (rc = recv(sockfd, pBuf + offset, nSize, 0))
#else
	__android_log_print(ANDROID_LOG_INFO,"MyHttp","Read=%d",::GetTickCount());
	while (rc = read(sockfd, pBuf + offset, nSize))
#endif // _WIN32_
	{
        __android_log_print(ANDROID_LOG_INFO,"MyHttp","Reading=%s",pBuf + offset);
		if (pBuf[offset + rc - 4] == '\r' && pBuf[offset + rc - 3] == '\n' &&
			pBuf[offset + rc - 2] == '\r' &&pBuf[offset + rc - 1] == '\n')
		{
			offset += rc;
			break;	// �������
		}
		if (rc == -1)
		{
			if (pBuf)
			{
				delete[]pBuf;
				pBuf = NULL;
			}

#ifdef _WIN32_
			closesocket(sockfd);
			WSACleanup();
#endif
			__android_log_print(ANDROID_LOG_INFO,"MyHttp","ReadError=%d",::GetTickCount());
			char fail[]="read %d";
			sprintf(fail,"read %d",errno);
			::lognet(true,fail,sizeof(fail));
			return -6;
		}

		offset += rc;
	}
	__android_log_print(ANDROID_LOG_INFO,"MyHttp","ReadOk=%d",::GetTickCount());
#ifdef _WIN32_
	closesocket(sockfd);
	WSACleanup();
#else
	close(sockfd);
#endif // _WIN32_

	pBuf[offset] = 0;

	string strResponse;
	strResponse = pBuf;
	if (pBuf)
	{
		delete[]pBuf;
		pBuf = NULL;
	}

	//����ȡ�����ݽ�����ȡ,ȥ����Ӧ�����ֶΡ�ͷ��  ֻ����json����
	int nRet1 = 0;
	
	nRet1 = ExtractStr(strResponse, response);

	if (nRet1 != 1)
	{
		return nRet1;
	}

	return 1;
}

int CMyHttp::PostData(string host, string path, string post_content, string &response)
{
	//POST����ʽ
	long nContentLen = 0;
	nContentLen = post_content.length();
	char szContenLen[100] = {0};
	string strContenLen;
	sprintf(szContenLen, "%ld", nContentLen);
	strContenLen = szContenLen;

	string strRequest;
	strRequest = "POST ";
	strRequest += path;
	strRequest += " HTTP/1.0\r\n";
	strRequest += "User-Agent: Mozilla/5.0\r\n";
	strRequest += "Accept: */*\r\n";
	strRequest += "Host: ";
	strRequest += host;
	strRequest += "\r\n";
	strRequest += "Connection: keep-alive\r\n";
	strRequest += "Content-Type: multipart/form-data; boundary=--------------------------381906398464664695032150\r\n";
	strRequest += "Content-Length: ";
	strRequest += strContenLen;
	strRequest += "\r\n";
	strRequest += "\r\n";
	strRequest += post_content;

	return SocketHttp(host, strRequest, response);
}

int CMyHttp::GetData(string host, string path, string get_content, string &response)
{
	//GET����ʽ
//	stringstream stream;
//	stream << "GET " << path << "?" << get_content;
//	stream << " HTTP/1.0\r\n";
//	stream << "Host: " << host << "\r\n";
//	stream << "User-Agent: Mozilla/5.0\r\n";
//	stream << "Connection:close\r\n\r\n";

//	return SocketHttp(host, stream.str(), response);
	return 1;
}

int CMyHttp::ExtractStr(string strResponse, string & strResponseResult)
{
	unsigned int nPos1 = -1, nPos2 = -1;
	nPos1 = strResponse.find(" ");
	if (nPos1 == -1)  //û���ҵ�
	{
		return -7;
	}

	string strRight;
	strRight = strResponse.substr(nPos1 + 1);

	nPos2 = strRight.find(" ");
	if (nPos2 == -1)
	{
		return -7;
	}

	string strCode;
	strCode = strRight.substr(0, nPos2);
	int nCode = 0;
	sscanf(strCode.c_str(), "%d", &nCode);
	if (nCode != _STATUS_OK_CODE_)
	{
		return nCode;
	}

	nPos1 = strResponse.find("{");
	if (nPos1 == -1)
	{
		return -8;
	}

	long nLen = 0;
	nLen = strResponse.length();

	string strJson;
	strJson = strResponse.substr(nPos1, nLen - nPos1);

	strResponseResult = strJson;

	return 1;
}
