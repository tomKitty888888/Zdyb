//
// Created by kouten on 17-1-2.
//


#include <stdarg.h>
#include "string.h"
#include<stdlib.h>
#include<unistd.h>
#include <time.h>
#include "Debug.h"
#include <android/log.h>
#include <dirent.h>

unsigned long GetTickCount() {


    struct timespec ts;

    clock_gettime(CLOCK_MONOTONIC, &ts);

    return (ts.tv_sec * 1000 + ts.tv_nsec / 1000000);

}

void Sleep(long ms) {
    usleep(1000 * ms);
}


int _access(const char *path, int mode) {
    return access(path, mode);
}

char *strupr(char *str) {
    char *ptr = str;

    while (*ptr != '\0') {
        if (islower(*ptr))
            *ptr = toupper(*ptr);
        ptr++;
    }

    return str;
}


DWORD GetPrivateProfileString(
        LPCSTR lpAppName,
        LPCSTR lpKeyName,
        LPCSTR lpDefault,
        LPSTR lpReturnedString,
        DWORD nSize,
        LPCSTR lpFileName
) {
    DWORD ret = 0;
    FILE *file = NULL;
    try {
        ret = strlen(lpDefault);
        if (ret < nSize)
            strcpy(lpReturnedString, lpDefault);
        else {
            int i;
            for (i = 0; i < nSize - 1; i++)
                lpReturnedString[i] = lpDefault[i];
            lpReturnedString[i] = '\0';
            ret = nSize - 1;
        }

        file = fopen(lpFileName, "r");
        if (file == NULL)
            return ret;

        if (lpAppName == 0x0) {
            char buf[256];
            int size = 256;
            char *_buf = buf;
            char *lpret = lpReturnedString;
            DWORD count = 0;

            while (fgets(_buf, size, file) != NULL && (count < nSize - 2)) {
                if (_buf[0] != '[') continue;
                for (int i = 1;
                     count < nSize - 2 &&
                     _buf[i] != ']' &&
                     _buf[i] != '\r' &&
                     _buf[i] != '\n';
                     i++) {
                    *lpret = _buf[i];
                    lpret++;
                    count++;
                }
                *lpret = '\0';
                lpret++;
                count++;
            }
            *lpret = '\0';
            if (count > 0) count--;
            fclose(file);
            file = NULL;
            return ret = count;
        }//   end   of   lpAppName   ==   ""

        if (lpKeyName == 0x0) {
            char srcbuf[256];
            char desbuf[256];
            int size = 256;
            char *_srcbuf = srcbuf;
            char *_desbuf = desbuf;
            char target = 'S';   //'S'   find   section   name,'K'   find   key   name
            char *lpret = lpReturnedString;
            DWORD count = 0;
            while (fgets(_srcbuf, size, file) != NULL && (count < nSize - 2)) {
                BOOL flag = TRUE;
                if (target == 'S') {
                    if (_srcbuf[0] != '[') continue;
                    strcpy(_desbuf, lpAppName);
                    int len = strlen(_desbuf);
                    int i;
                    for (i = 0; i < len; i++) {
                        if (_desbuf[i] != _srcbuf[i + 1]) {
                            flag = FALSE;
                            break;
                        }
                    }
                    if (flag == FALSE) continue;
                    if (_srcbuf[i + 1] != ']') continue;
                    target = 'K';
                } else {
                    if (_srcbuf[0] == '[') break;
                    for (int i = 0;
                         count < nSize - 2 &&
                         _srcbuf[i] != ' ' &&
                         _srcbuf[i] != '=' &&
                         _srcbuf[i] != '\r' &&
                         _srcbuf[i] != '\n';
                         i++) {
                        *lpret = _srcbuf[i];
                        lpret++;
                        count++;

                    }
                    *lpret = '\0';
                    lpret++;
                    count++;
                }//   end   of   if
            }//   end   of   while
            *lpret = '\0';
            if (count > 0) count--;
            fclose(file);
            file = NULL;
            return ret = count;

        }//   end   of   lpKeyName   ==   ""

        char srcbuf[256];
        char desbuf[256];
        int size = 256;
        char *_srcbuf = srcbuf;
        char *_desbuf = desbuf;
        char target = 'S';   //'S'   find   section   name,'K'   find   key   name
        while (fgets(_srcbuf, size, file) != NULL) {
            BOOL flag = TRUE;
            if (target == 'S') {
                if (_srcbuf[0] != '[') continue;
                strcpy(_desbuf, lpAppName);
                int len = strlen(_desbuf);
                int i;
                for (i = 0; i < len; i++) {
                    if (_desbuf[i] != _srcbuf[i + 1]) {
                        flag = FALSE;
                        break;
                    }
                }
                if (flag == FALSE) continue;
                if (_srcbuf[i + 1] != ']') continue;
                target = 'K';
            } else {
                if (_srcbuf[0] == '[') break;
                strcpy(_desbuf, lpKeyName);
                int len = strlen(_desbuf);
                int i;
                for (i = 0; i < len; i++) {
                    if (_desbuf[i] != _srcbuf[i]) {
                        flag = FALSE;
                        break;
                    }
                }
                if (flag == FALSE) continue;

                BOOL _flag = FALSE;
                while (_srcbuf[i] != '\0') {
                    if (_srcbuf[i] == ' ') {
                        i++;
                        continue;
                    }
                    if (_srcbuf[i] == '=') {
                        _flag = TRUE;
                        i++;
                        continue;
                    }
                    if (_flag == FALSE) break;
                    char *lpresult = &_srcbuf[i];
                    char *lpret = lpReturnedString;
                    int count = 0;
                   //过滤换车和换行符
                    while (*lpresult != '\r' &&
                           *lpresult != '\n' &&
                           *lpresult != '\0'
                            ) {
                        *lpret = *lpresult;
                        lpret++;
                        lpresult++;
                        count++;
                    }
                    *lpret = '\0';
                    fclose(file);
                    file = NULL;
                    return ret = count;
                }//   end   of   while
            }//   end   of   if
        }//   end   of   while
        fclose(file);
        file = NULL;
        return ret;

    }//end   of   try
    catch (...) {
        if (file != NULL) {
            fclose(file);
            file = NULL;
        }
        ret = strlen(lpDefault);
        if (ret < nSize)
            strcpy(lpReturnedString, lpDefault);
        else {
            int i;
            for (i = 0; i < nSize - 1; i++)
                lpReturnedString[i] = lpDefault[i];
            lpReturnedString[i] = '\0';
            ret = nSize - 1;
        }
        return ret;
    }

}//end   of   GetPrivateProfileString



VOID GetLocalTime(
        LPSYSTEMTIME lpSystemTime
) {
    struct timeval tv;
    struct timezone tz;

    struct tm *p;

    gettimeofday(&tv, &tz);
//    printf("tv_sec:%ld\n",tv.tv_sec);
//    printf("tv_usec:%ld\n",tv.tv_usec);
//    printf("tz_minuteswest:%d\n",tz.tz_minuteswest);
//    printf("tz_dsttime:%d\n",tz.tz_dsttime);
    p = localtime(&tv.tv_sec);
//    printf("time_now:%d /%d /%d %d :%d :%d.%3ld\n", 1900+p->tm_year, 1+p->tm_mon, p->tm_mday, p->tm_hour, p->tm_min, p->tm_sec, tv.tv_usec);
    lpSystemTime->wYear = 1900 + p->tm_year;
    lpSystemTime->wMonth = 1 + p->tm_mon;
    lpSystemTime->wDay = p->tm_mday;
    lpSystemTime->wHour = p->tm_hour;
    lpSystemTime->wMinute = p->tm_min;
    lpSystemTime->wSecond = p->tm_sec;
    lpSystemTime->wMilliseconds = tv.tv_usec / 1000;

}

#define PBSZ 1024
//int printf(const char *fmtString, ...)
//{
//    char printBuffer[PBSZ];
//    va_list args;
//    va_start(args, fmtString);
//    vsprintf(printBuffer, fmtString, args);
//    va_end(args);
//    return  LOGI(printBuffer,"\n");
//}

int OutputDebugString(const char *format, ...) {
    if (strlen(format) >= 1024) {
        return 0;
    }
    char printBuffer[PBSZ];
    va_list args;
    va_start(args, format);
    vsprintf(printBuffer, format, args);
    va_end(args);
    LOGD(printBuffer);
   return 0;
}


HANDLE CreateFileMapping(
        HANDLE hFile,
        LPSECURITY_ATTRIBUTES lpFileMappingAttributes,
        DWORD flProtect,
        DWORD dwMaximumSizeHigh,
        DWORD dwMaximumSizeLow,
        LPCSTR lpName
) {
    return 0;
}

DWORD
GetLastError(
        VOID
) {
    return 0;
}


BOOL
CloseHandle(
        HANDLE hObject
) {
    return true;

}

LPVOID
MapViewOfFile(
        HANDLE hFileMappingObject,
        DWORD dwDesiredAccess,
        DWORD dwFileOffsetHigh,
        DWORD dwFileOffsetLow,
        DWORD dwNumberOfBytesToMap
) {
    return 0;

}


void adsSleep(W_INT32 iMilliseconds) {
    Sleep(iMilliseconds);
}


char *strlwr(char *s) {
    char *str;
    str = s;
    while (*str != '\0') {
        if (*str >= 'A' && *str <= 'Z') {
            *str += 'a' - 'A';
        }
        str++;
    }
    return s;
}

int min(int a, int b) {
    if (a > b) return b;
    return a;
}


extern void *thread_fun(void *arg);


HANDLE CreateThread(
        LPSECURITY_ATTRIBUTES lpThreadAttributes,//线程安全属性
        DWORD dwStackSize,//堆栈大小
        LPTHREAD_START_ROUTINE lpStartAddress,//线程函数
        LPVOID lpParameter,//线程参数
        DWORD dwCreationFlags,//线程创建属性
        LPDWORD lpThreadId//线程ID
) {
    OutputDebugString("CreateThread");
    static LPVOID para[2] = {(LPVOID) lpStartAddress, lpParameter};
    // 创建线程，并指明调用的函数，注意只接收一个参数i作为thread_fun的参数，后面会介绍怎么传多个参数
    //if(0!=pthread_create((pthread_t *) lpThreadId, NULL, (void *(*)(void*))lpStartAddress, lpParameter))
    if (0 != pthread_create((pthread_t *) lpThreadId, NULL, (void *(*)(void *)) thread_fun,
                            (LPVOID) para)) {
        OutputDebugString("CreateThread failed");
        return 0;
    }
    OutputDebugString("CreateThread OK");
    return lpThreadId;
}

extern bool thread_flag;

DWORD MsgWaitForMultipleObjects(
        DWORD nCount,          // 表示pHandles所指的handles数组的元素个数，最大容量是MAXIMUM_WAIT_OBJECTS
        LPHANDLE pHandles,     // 指向一个由对象handles组成的数组，这些handles的类型不需要相同
        BOOL fWaitAll,         // 是否等待所有的handles被激发才返回
        DWORD dwMilliseconds,  // 超时时间
        DWORD dwWakeMask       // 欲观察的用户输入消息类型
) {
    OutputDebugString("MsgWaitForMultipleObjects");
    if (nCount != 1) {
        OutputDebugString("MsgWaitForMultipleObjects 1");
        return 1;
    }
    if (pHandles == NULL) {
        OutputDebugString("MsgWaitForMultipleObjects 2");
        return 2;
    }
    void *ret;
    pthread_t *pth = (pthread_t *) pHandles[0];
    if (pth == NULL) {
        OutputDebugString("MsgWaitForMultipleObjects 3");
        return WAIT_FAILED;
    }

    while (thread_flag) {
        sleep(1);
    }
//    pthread_join(*pth,&ret);
    OutputDebugString("MsgWaitForMultipleObjects OK");
    return WAIT_OBJECT_0;
}

BOOL PeekMessage(
        LPMSG lpMsg,
        HWND hWnd,
        UINT wMsgFilterMin,
        UINT wMsgFilterMax,
        UINT wRemoveMsg) {
    OutputDebugString("PeekMessage");
    return false;
}

LONG DispatchMessage(
        CONST MSG *lpMsg) {
    OutputDebugString("DispatchMessage");
    return 0;
}


char *itoa(int value, char *string, int radix) {
    char zm[37] = "0123456789abcdefghijklmnopqrstuvwxyz";
    char aa[100] = {0};

    int sum = value;
    char *cp = string;
    int i = 0;

    if (radix < 2 || radix > 36)//增加了对错误的检测
    {
        //cout<<"error data!"<<endl;
        return string;
    }

    if (value < 0) {
        //cout<<"error data!"<<endl;
        return string;
    }


    while (sum > 0) {
        aa[i++] = zm[sum % radix];
        sum /= radix;
    }

    for (int j = i - 1; j >= 0; j--) {
        *cp++ = aa[j];
    }
    *cp = '\0';
    return string;
}

char *_strupr(char *src) {
    while (*src != '\0') {
        if (*src >= 'a' && *src <= 'z')
            //*p -= 0x20; 
            *src -= 32;
        src++;
    }
    return src;
}

void ZeroMemory(char value[], int size) {
    memset(value, 0, size);
}
void ZeroMemory(void *value, int size) {
    memset(value,0, size);
}

void ultoa(BYTE value, char buffer[], int radix) {
    itoa(value, buffer, 16);
}

void ultoa(int value, char buffer[], int radix) {
    itoa(value, buffer, 16);
}

void ultoa(DWORD value, char buffer[], int radix) {
    itoa(value, buffer, 16);
}

time_t GetCurrentTime(){

    return time(NULL);
}



void searchdir(const char *path,vector<string> &fileList)
{
    DIR *dp;
    struct dirent *dmsg;
    int i=0;
    char addpath[1024] = {'\0'}, *tmpstr;
    if ((dp = opendir(path)) != NULL)
    {

        while ((dmsg = readdir(dp)) != NULL)
        {
            if (!strcmp(dmsg->d_name, ".") || !strcmp(dmsg->d_name, ".."))
                continue;

            strcpy(addpath, path);
            strcat(addpath, "/");
            strcat(addpath, dmsg->d_name);
            fileList.push_back(addpath);

            if (dmsg->d_type == DT_DIR )
            {
               /* char *temp;
                temp=dmsg->d_name;
                if(strchr(dmsg->d_name, '.'))
                {
                    if((strcmp(strchr(dmsg->d_name, '.'), dmsg->d_name)==0))
                    {
                        continue;
                    }
                }
                fileList.push_back(addpath);
                searchdir(addpath,fileList);*/
                continue;
            }
        }
    }
    closedir(dp);
}
int WinExec(string command,int aaa){

    return executeCommand((char*)command.c_str());
}
