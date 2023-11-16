#ifndef _QUEUE_READ_USB_H_
#define _QUEUE_READ_USB_H_
#include <stddef.h>
#include <string.h>

#define   TRUE       1
#define   FALSE      0
#define   RING_BUFF_SIZE  20480

extern int g_queue_init;

typedef struct ringBuff{
    unsigned int in;               //写入的下标
    unsigned int out;              //读出的下标
    unsigned char buffer[RING_BUFF_SIZE];     //保存数据的数组
}stRingBuff;

void queue_init(stRingBuff *ringBuf);//初始化队列

stRingBuff *GetRingBufferStruct(void);//获取数据队列结构体
int GetRingBufferLength(stRingBuff *ringBuf);//获取队列中当前数据长度

char WriteOneparagraphToRingBuffer(stRingBuff *ringBuf,unsigned char *data,unsigned int len);//写len个字节的数据到队列
char WriteOneByteToRingBuffer(stRingBuff *ringBuf,char data);//写一个字节到队列
char IsRingBufferFull(stRingBuff *ringBuf);//判断队列是否满


char ReadOneByteFromRingBuffer(stRingBuff *ringBuf,unsigned char *data);//从队列中读取一个字节的数据
char IsRingBufferEmpty(stRingBuff *ringBuf);//判断队列是否为空
int ReadRingBuffer(unsigned char *readBuf,stRingBuff *ringBuf,unsigned int len);//从队列中读取len个字节的数据

#endif
