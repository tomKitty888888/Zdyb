#include <jni.h>
#include <string>
#include "Debug.h"
#include <dlfcn.h>
#include "Gui.h"
#include "Com.h"
#include "Display.h"
#include "CommWithEcu.h"
#include <android/log.h>

extern unsigned char guiBuf[];
extern unsigned char comBuf[];


// 定义全局变量
static JavaVM *m_jvm = NULL;
static jobject m_object;

//static JNIEnv* env;
//jclass m_class;
//jmethodID m_sendData, m_revcData,m_MessageBox,m_SendMessage,m_PostMessage,m_test,m_getExePath,m_initCom;
void exitMethodID() {
    //   m_jvm->DetachCurrentThread();
}

jmethodID getMethodID(JNIEnv **env, const char *name, const char *sig) {
    if (JNI_OK != m_jvm->AttachCurrentThread(env, NULL)) {
        return NULL;
    }
    jclass cls = (*env)->GetObjectClass(m_object);
    if (cls == NULL) {
        exitMethodID();
        return NULL;
    }
    jmethodID mid = (*env)->GetMethodID(cls, name, sig);
    if (mid == NULL) {
        (*env)->DeleteLocalRef(cls);
        exitMethodID();
        return NULL;
    }
    (*env)->DeleteLocalRef(cls);
    return mid;
}




int jstringTostring(JNIEnv *env, jstring jstr, char *str, int len) {
//    if(len==0) return 0;
//    if(jstr==NULL) return 0;
//    const char *str0 = env->GetStringUTFChars(jstr, JNI_FALSE);
//    strncpy(str,str0,len);
//    str[len-1]=0;
//    env->ReleaseStringUTFChars(jstr, str0);
//    return  strlen(str);
    char *rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);
    int ret = 0;
    if (alen > 0) {
        if (alen < len) {
            memcpy(str, ba, alen);
            str[alen] = 0;
            ret = alen + 1;
        }
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    env->DeleteLocalRef(barr);
    env->DeleteLocalRef(strencode);
    env->DeleteLocalRef(clsstring);
    return ret;
}

/***********************************GUI********************************************/
int MessageBox(HWND hWnd, LPCSTR lpText, LPCSTR lpCaption, UINT uType) {
    JNIEnv *env;
//    m_jvm->AttachCurrentThread(&env,NULL);
//    jclass m_class=env->GetObjectClass(m_object);
//    jmethodID m_MessageBox=env->GetMethodID(m_class,"MessageBox","(Ljava/lang/String;Ljava/lang/String;J)I");
    jmethodID m_MessageBox = getMethodID(&env, "MessageBox",
                                         "(Ljava/lang/String;Ljava/lang/String;J)I");
    if (m_MessageBox == NULL) {
        return -1;
    }
    jstring text = env->NewStringUTF(lpText);
    jstring cap = env->NewStringUTF(lpCaption);
    jlong type = uType;
    int ret = env->CallIntMethod(m_object, m_MessageBox, text, cap, type);
    env->DeleteLocalRef(cap);
    env->DeleteLocalRef(text);
    exitMethodID();

    return ret;
}

void LoadVdiStatusCallBack(bool flag)
{
    JNIEnv *env;
    jmethodID m_SendMessage = getMethodID(&env, "LoadVdiStatusCallBack", "(Z)V");
    if (m_SendMessage == NULL) {
        return ;
    }

    env->CallVoidMethod(m_object, m_SendMessage, flag);
}


LRESULT SendMessage(
        HWND hWnd,
        UINT Msg,
        WPARAM wParam,
        LPARAM lParam) {
    JNIEnv *env;
//    m_jvm->AttachCurrentThread(&env,NULL);
//    jclass m_class=env->GetObjectClass(m_object);
//    jmethodID m_SendMessage=env->GetMethodID(m_class,"SendMessage","(JJJ)J");
    jmethodID m_SendMessage = getMethodID(&env, "SendMessage", "(JJJ)J");
    if (m_SendMessage == NULL) {
        return -1;
    }
    jlong msg = Msg;
    jlong wp = wParam;
    jlong lp = lParam;
    jlong ret = env->CallLongMethod(m_object, m_SendMessage, msg, wp, lp);
    exitMethodID();
    return (LRESULT) ret;
}

LRESULT MsgReflashProgressAdaptee(
        BYTE *pszMessage,BYTE bMsgLen) {
    JNIEnv *env;
    jmethodID m_PostMessage = getMethodID(&env, "MsgReflashProgress", "([B)V");
    if (m_PostMessage == NULL) return -1;
    jbyteArray data = env->NewByteArray(bMsgLen);
    env->SetByteArrayRegion(data, 0, bMsgLen, (jbyte *) pszMessage);
     env->CallVoidMethod(m_object, m_PostMessage, data);
    env->DeleteLocalRef(data);
    exitMethodID();
    return (LRESULT) 0;
}

LRESULT PostMessage(
        HWND hWnd,
        UINT Msg,
        WPARAM wParam,
        LPARAM lParam) {
    JNIEnv *env;
    jmethodID m_PostMessage = getMethodID(&env, "PostMessage", "(JJJ)J");
    if (m_PostMessage == NULL) return -1;
    jlong msg = Msg;
    jlong wp = wParam;
    jlong lp = lParam;
    jlong ret = env->CallLongMethod(m_object, m_PostMessage, msg, wp, lp);
    exitMethodID();
    return (LRESULT) ret;
}
/*    0	没错
    1	警告（非致命错误）。例如，一个或多个文件被某些其他应用程序锁定，因此它们未被压缩。
    2	致命错误
    7	命令行错误
    8	操作内存不足
    255	用户停止了该过程*/
extern "C"
int executeCommand(char *command) {
    JNIEnv *env;
    jmethodID m_command = getMethodID(&env, "executeCommand", "(Ljava/lang/String;)I");
    if (m_command==NULL||m_object==NULL) {
        return -1;
    }
    jstring result = env->NewStringUTF(command);
    jlong ret = env->CallIntMethod(m_object, m_command, result);

  //  LOGI("executeCommand return:" + ret);

    exitMethodID();
    return ret;

}

extern "C"
JNIEXPORT jint JNICALL
Java_com_zdeps_gui_MainActivity_nExactFile(JNIEnv *env, jobject instance, jstring command_) {
    const char *command = env->GetStringUTFChars(command_, 0);


    jclass jimagec = env->FindClass("com/hzy/libp7zip/P7ZipApi");


    jmethodID m_nativeZipCommand = env->GetStaticMethodID(jimagec, "executeCommand",
                                                          "(Ljava/lang/String;)I");
    int ret = env->CallStaticIntMethod(jimagec, m_nativeZipCommand, command_);

    LOGI("executeCommand return:" + ret);
    exitMethodID();
    return ret;

}

HWND
FindWindow(
        LPCSTR lpClassName,
        LPCSTR lpWindowName) {
    return (void *) 1;
}




//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////
/******************COMM functions**********************************************************************/
//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////
int sendData(unsigned char *dat, int len) {
    JNIEnv *env;
    jmethodID m_sendData = getMethodID(&env, "sendData", "([BI)I");
    if (m_sendData == NULL) return -1;

    jbyteArray data = env->NewByteArray(len);
    env->SetByteArrayRegion(data, 0, len, (jbyte *) dat);
    int ret = env->CallIntMethod(m_object, m_sendData, data, len);
    env->DeleteLocalRef(data);
    exitMethodID();
    return ret;
}

int recvData(unsigned char *dat, int len) {
    JNIEnv *env;
    jmethodID m_recvData = getMethodID(&env, "recvData", "(I)[B");
    if (m_recvData == NULL) return 0;

    int rlen = 0;
    jbyteArray rdat = (jbyteArray) env->CallObjectMethod(m_object, m_recvData, len);
    if (rdat != NULL) {
        jbyte *olddata = (jbyte *) env->GetByteArrayElements(rdat, 0);
        jsize oldsize = env->GetArrayLength(rdat);
        env->GetByteArrayRegion(rdat,0,oldsize,(jbyte*)dat);
        __android_log_print(ANDROID_LOG_INFO,"recvData","oldsize=%d",oldsize);//gxf 2023
        env->ReleaseByteArrayElements(rdat, olddata, 0);
        env->DeleteLocalRef(rdat);
        rlen = oldsize;
    }
    exitMethodID();
    return rlen;
}

HANDLE
CreateFile(
        LPCSTR lpFileName,
        DWORD dwDesiredAccess,
        DWORD dwShareMode,
        LPSECURITY_ATTRIBUTES lpSecurityAttributes,
        DWORD dwCreationDisposition,
        DWORD dwFlagsAndAttributes,
        HANDLE hTemplateFile
) {
    JNIEnv *env;
//    m_jvm->AttachCurrentThread(&env,NULL);
//    jclass m_class= env->GetObjectClass(m_object);
//    jmethodID m_initCom  = env->GetMethodID(m_class,"initCom","(Ljava/lang/String;)Z");
    jmethodID m_initCom = getMethodID(&env, "initCom", "(Ljava/lang/String;)Z");
    if (m_initCom == NULL) return 0;

    jstring name = env->NewStringUTF(lpFileName);
    jboolean ret = env->CallBooleanMethod(m_object, m_initCom, name);
    env->DeleteLocalRef(name);
    exitMethodID();
    if (!ret) return 0;
    return (void *) 1;
}

BOOL
PurgeComm(
        HANDLE hFile,
        DWORD dwFlags
) {
    JNIEnv *env;
//    m_jvm->AttachCurrentThread(&env,NULL);
//    jclass m_class= env->GetObjectClass(m_object);
//    jmethodID m_initCom  = env->GetMethodID(m_class,"initCom","(Ljava/lang/String;)Z");
    jmethodID m_PurgeComm = getMethodID(&env, "PurgeComm", "()V");
    if (m_PurgeComm == NULL) return 0;
    env->CallVoidMethod(m_object, m_PurgeComm);
    exitMethodID();
    return 1;
}

BOOL
SetupComm(
        HANDLE hFile,
        DWORD dwInQueue,
        DWORD dwOutQueue
) {
    return 0;
}

BOOL
GetCommTimeouts(
        HANDLE hFile,
        LPCOMMTIMEOUTS lpCommTimeouts
) {
    return 0;
}

BOOL
SetCommTimeouts(
        HANDLE hFile,
        LPCOMMTIMEOUTS lpCommTimeouts
) {
    return 0;
}

BOOL
GetCommState(
        HANDLE hFile,
        LPDCB lpDCB
) {
    return 0;
}

BOOL
SetCommState(
        HANDLE hFile,
        LPDCB lpDCB
) {
    return 0;
}

void printfhex(unsigned char * buf,int len)
{
    char temp[200];
    int i=len;
    while(i>0){
        int len=12;
        if(len>i) len=i;
        for(int k=0;k<len;k++)
            sprintf(&temp[3*k],"%.2x ",*buf++);
        LOGD(temp);
        i-=len;
    }
}


BOOL
ReadFile(
        HANDLE hFile,
        LPVOID lpBuffer,
        DWORD nNumberOfBytesToRead,
        LPDWORD lpNumberOfBytesRead,
        LPOVERLAPPED lpOverlapped
) {
    queue_init(GetRingBufferStruct());//gxf 2023-02-01
    if (!IsRingBufferEmpty(GetRingBufferStruct()))
    {//C队列不为空,从队列读取数据
        *lpNumberOfBytesRead = ReadRingBuffer((unsigned char *) lpBuffer,GetRingBufferStruct(),nNumberOfBytesToRead);
        if (-1==(*lpNumberOfBytesRead))
        {
            *lpNumberOfBytesRead = 0;//
            g_queue_init = 0;//设置C队列需要重新初始化
        }
    }
    else
    {//C队列为空，从USB队列读取数据到C队列
        *lpNumberOfBytesRead = recvData((unsigned char *) GetRingBufferStruct()->buffer, RING_BUFF_SIZE);//一次性读取USB全部数据到g_stRingBuffer队列起始位置，RING_BUFF_SIZE认为可以全读取完
        //WriteOneparagraphToRingBuffer(GetRingBufferStruct(),(unsigned char *) g_queue_usb_buffer,*lpNumberOfBytesRead);//将g_queue_usb_buffer数据全部写入C队列
        GetRingBufferStruct()->out = 0;//读取开始下标
        GetRingBufferStruct()->in = *lpNumberOfBytesRead;//直接修改队列写入位置
        *lpNumberOfBytesRead = 0;//C队列为空，本次读取返回0，用户需要下次调用从C队列读取数据。
    }
    //printfhex(( unsigned char *)lpBuffer,*lpNumberOfBytesRead);//gxf 屏蔽打印
    return 0;
}

BOOL
WriteFile(
        HANDLE hFile,
        LPCVOID lpBuffer,
        DWORD nNumberOfBytesToWrite,
        LPDWORD lpNumberOfBytesWritten,
        LPOVERLAPPED lpOverlapped
) {
    g_queue_init = 0;//设置C队列需要重新初始化
    queue_init(GetRingBufferStruct());//gxf 2023-03-17 发送数据前清空一次接收C++缓冲区

    *lpNumberOfBytesWritten = sendData((unsigned char *) lpBuffer, nNumberOfBytesToWrite);
    return (*lpNumberOfBytesWritten == nNumberOfBytesToWrite);
}

BOOL
ClearCommError(
        HANDLE hFile,
        LPDWORD lpErrors,
        LPCOMSTAT lpStat
) {
    lpStat->cbInQue = 1;
    return 0;
}


int GetModuleFileName(char *sModuleName, char *sFileName, int nSize) {
//    static char* soPath="/storage/sdcard0/zdeps/Diagnosis/Electronic/01Bosch/Diagnose.exe";
//    strncpy(sFileName,soPath,nSize);
//    return strlen(sFileName);
    JNIEnv *env;
    jmethodID m_getExePath = getMethodID(&env, "getExePath", "()[B");
    if (m_getExePath == NULL) return 0;

    int rlen = 0;
    jbyteArray rdat = (jbyteArray) env->CallObjectMethod(m_object, m_getExePath);
    if (rdat != NULL) {
        jbyte *olddata = (jbyte *) env->GetByteArrayElements(rdat, 0);
        jsize oldsize = env->GetArrayLength(rdat);
        if (nSize > oldsize) nSize = oldsize;
        memcpy(sFileName, olddata, nSize);
        env->ReleaseByteArrayElements(rdat, olddata, 0);
        env->DeleteLocalRef(rdat);
        rlen = nSize;
    }
    exitMethodID();
    return rlen;

}

char* int2str(int i, char *s) {
    sprintf(s,"%d",i);
    return s;
}
int guiGetCurrentPath(char *sFileName, int nSize){

    JNIEnv *env;
    jmethodID m_getExePath = getMethodID(&env, "getCurrentPath", "()[B");
    if (m_getExePath == NULL) return 0;

    int rlen = 0;
    jbyteArray rdat = (jbyteArray) env->CallObjectMethod(m_object, m_getExePath);
    if (rdat != NULL) {
        jbyte *olddata = (jbyte *) env->GetByteArrayElements(rdat, 0);
        jsize oldsize = env->GetArrayLength(rdat);
        char a[64];
        LOGD(int2str(nSize,a));
        char b[64];
        LOGD(int2str(oldsize,b));
       if (nSize > oldsize) nSize = oldsize;
        memcpy(sFileName, olddata, oldsize);

        env->ReleaseByteArrayElements(rdat, olddata, 0);
        env->DeleteLocalRef(rdat);
        rlen = nSize;
    }
    exitMethodID();
    return rlen;
}

/********************************jni***********************************************/
//CCommWithEcu adsCommEcu;            // 全局通信接口
//CDisplay adsDisplay;            // 全局显示接口
//void test() {
//
//
//    W_UINT8 nRet = adsDisplay.Init();
//    adsCommEcu.Init();
//
////    if (adsDisplay.MessageBox(GT(CBinary("\x94\x08\x00\x00\x01\x74",6)),"",MSG_MB_YESNO) != MB_YES)
////    {}
//    if (adsDisplay.MessageBox(
//            "注意事项:\r\n1.发动机转速小于1600 RPM\r\n2.车速小于3 km/h\r\n3.本次测试为短期调整,关闭点火后将回到原状态,如要长期调整(永久写入),请在ECU标定中进行标定",
//            "", \
//        MSG_MB_YESNO) != MB_YES) {
//
//    }
////    SendMessage(0,1,2,3);
////    MessageBox(0,"提示","标题",MB_OK);
////    JNIEnv *env;
////    m_jvm->AttachCurrentThread(&env,NULL);
////    unsigned char data[]={0xa5,0xa5,0x00,0x02,0xFF,0x00,0x55};
////    jbyteArray dat=env->NewByteArray(sizeof(data));
////    env->SetByteArrayRegion(dat,0,sizeof(data),(jbyte*)data);
////    sendData(data,sizeof(data));
////    Sleep(3000);
////    int ret=recvData(data,sizeof(data));
//}


// 初始化函数
extern "C"
void
Java_com_zdeps_gui_ComJni_nativeSetup(
        JNIEnv *jniEnv,
        jobject thiz) {
    // 初始化中存储相应变量
    JNIEnv *env = jniEnv;
    env->GetJavaVM(&m_jvm);
//    jclass clazz = env->GetObjectClass(thiz); //获取当前对象的类信息
    //m_class = (jclass)env->NewGlobalRef(clazz); //将类型信息存储到m_class中
    m_object = (jobject) env->NewGlobalRef(thiz); // 将对象信息存储到m_object中

//    MessageBox(0,"提示","标题",MB_OK);


//    m_test=env->GetMethodID(m_class,"test","(Ljava/lang/String;)I");





//    jstring text=env->NewStringUTF("lpText");
//    env->CallIntMethod(m_object,m_test,text);
//     test();
}

extern "C"
jstring
Java_com_zdeps_gui_ComJni_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
//    test();
    return env->NewStringUTF(hello.c_str());
}

extern "C"
jboolean
Java_com_zdeps_gui_ComJni_getGUIBuf(
        JNIEnv *env,
        jobject thiz, jint offset, jint len) {
    if (offset + len > BUF_GUI_LEN) return false;
    jclass cls = env->GetObjectClass(thiz);
    if (cls == NULL) {
        return false;
    }
    jmethodID mid = env->GetMethodID(cls, "genGUIBuf", "([B)Z");
    jboolean ret = false;
    if (mid != NULL) {
        jbyteArray data = env->NewByteArray(len);
        env->SetByteArrayRegion(data, 0, len, (jbyte *) &guiBuf[offset]);
        ret = env->CallBooleanMethod(thiz, mid, data);
        env->DeleteLocalRef(data);
    }
    env->DeleteLocalRef(cls);

    return ret;
}

extern "C"
jboolean
Java_com_zdeps_gui_ComJni_getCOMBuf(
        JNIEnv *env,
        jobject thiz, jint offset, jint len) {
    if (offset + len > BUF_COM_LEN) return false;

    jclass cls = env->GetObjectClass(thiz);
    if (cls == NULL) {
        return false;
    }
    jmethodID mid = env->GetMethodID(cls, "genCOMBuf", "([B)Z");
    jboolean ret = false;
    if (mid != NULL) {
        jbyteArray data = env->NewByteArray(len);
        env->SetByteArrayRegion(data, 0, len, (jbyte *) &comBuf[offset]);
        ret = env->CallBooleanMethod(thiz, mid, data);
        env->DeleteLocalRef(data);
    }
    env->DeleteLocalRef(cls);
    return ret;
}

extern "C"
jboolean
Java_com_zdeps_gui_ComJni_setCOMBuf(
        JNIEnv *env,
        jobject, jint offset, jbyteArray dat, jint len) {
    if (offset + len > BUF_COM_LEN) return false;
    env->GetByteArrayRegion(dat, 0, len, (jbyte *) &comBuf[offset]);
    return true;
}

extern "C"
jboolean
Java_com_zdeps_gui_ComJni_setGUIBuf(
        JNIEnv *env,
        jobject, jint offset, jbyteArray dat, jint len) {
    if (offset + len > BUF_GUI_LEN) return false;
    env->GetByteArrayRegion(dat, 0, len, (jbyte *) &guiBuf[offset]);
    return true;
}


extern "C"
jboolean
Java_com_zdeps_gui_ComJni_loadLib(
        JNIEnv *env,
        jobject thiz, jbyteArray dat) {
    static void *pHandle = NULL;
    if (pHandle != NULL) {
        dlclose(pHandle);
        pHandle = NULL;
    }
    char *path = (char *) env->GetByteArrayElements(dat, 0);
    pHandle = dlopen(path, RTLD_LAZY);
    if (pHandle == NULL) return false;

    jint (*pMytest)(JNIEnv *, jobject);//,然后使用dlsym函数将函数指针pMytest指向mytest函数，
    pMytest = (jint (*)(JNIEnv *, jobject)) dlsym(pHandle, "Java_com_zdeps_diag_DiagJni_run");
    if (pMytest == NULL) return false;
    pMytest(env, thiz);
    return true;
}

bool thread_flag = false;

void *thread_fun(void *arg) {
    JNIEnv *env;
    jclass cls;
    jmethodID mid;



    //Attach主线程
    if (m_jvm->AttachCurrentThread(&env, NULL) != JNI_OK) {
        OutputDebugString("%s: AttachCurrentThread() failed", __FUNCTION__);
        return NULL;
    }
    if (thread_flag) {
        OutputDebugString("thread is running");
        return NULL;
    }
    thread_flag = true;
    OutputDebugString("Enter Thread");
    void **para = (void **) arg;
    ((void *(*)(void *)) para[0])(para[1]);
    OutputDebugString("Finish Thread");
    thread_flag = false;
//    //找到对应的类
//    cls = (*env)->GetObjectClass(env,g_obj);
//    if(cls == NULL)
//    {
//        LOGE("FindClass() Error.....");
//        goto error;
//    }
//    //再获得类中的方法
//    mid = (*env)->GetStaticMethodID(env, cls, "fromJNI", "(I)V");
//    if (mid == NULL)
//    {
//        LOGE("GetMethodID() Error.....");
//        goto error;
//    }
//    //最后调用java中的静态方法
//    (*env)->CallStaticVoidMethod(env, cls, mid ,(int)arg);


  //  error:   //2020.4.30进行注释
    //Detach主线程
    if (m_jvm->DetachCurrentThread() != JNI_OK) {
        OutputDebugString("%s: DetachCurrentThread() failed", __FUNCTION__);
    }

    OutputDebugString("Exit Thread");
    pthread_exit(0);
}


unsigned int androidGetProductType(){
    JNIEnv *env;
    jmethodID getProductType = getMethodID(&env, "getProductType", "()I");
    if (getProductType == NULL) return 0;
    jint productType=env->CallIntMethod(m_object,getProductType);
    return productType;
}

void lognet(bool  isRequest, const char *info, int len){
    JNIEnv *env;
    jmethodID m_PostMessage = getMethodID(&env, "logNet", "(Z[BI)V");
    if (m_PostMessage == NULL) return ;
    jbyteArray data = env->NewByteArray(len);
    env->SetByteArrayRegion(data, 0, len, (jbyte *) info);
    env->CallVoidMethod(m_object, m_PostMessage, isRequest,data,len);
    env->DeleteLocalRef(data);
    exitMethodID();
    return ;
}
 int IsWiredOrBluetooth(){
     JNIEnv *env;
     jmethodID m_PostMessage = getMethodID(&env, "IsWiredOrBluetooth", "()I");
     if (m_PostMessage == NULL) return 2;

     jint type=env->CallIntMethod(m_object, m_PostMessage);
     exitMethodID();
     return type;
}