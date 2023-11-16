#include "queue_read_usb.h"



static stRingBuff g_stRingBuffer ;
int g_queue_init = 0;//0：表示未初始、1：表示完成初始化


void queue_init(stRingBuff *ringBuf)
{
	if (!g_queue_init)
		{
			memset(ringBuf->buffer,0,sizeof(ringBuf->buffer));//清空队列
			ringBuf->in = 0;//存入开始下标
			ringBuf->out = 0;//读取开始下标
			g_queue_init = 1;//设置1，表示完成初始化
		}

}



//获取数据队列结构体
stRingBuff *GetRingBufferStruct(void)
{
	return &g_stRingBuffer;
	
}



//获取队列中当前数据长度
int GetRingBufferLength(stRingBuff *ringBuf)
{
    if (ringBuf == NULL)
    {
        //printf("pointer is null\r\n");
        return -1;
    }

    return (ringBuf->in - ringBuf->out + RING_BUFF_SIZE) % RING_BUFF_SIZE;
}




//写一个字节到队列
char WriteOneByteToRingBuffer(stRingBuff *ringBuf,char data)
{
	if (ringBuf == NULL)
    {
        //printf("pointer is null\r\n");
        return -1;
    }
    
    if(IsRingBufferFull(ringBuf))    //写入队列之前，先判断队列是否满
    {
        return FALSE;
    }

    ringBuf->buffer[ringBuf->in] = data;
    ringBuf->in = (++ringBuf->in) % RING_BUFF_SIZE;   //放置越界
	return TRUE;
}
//写len个字节到队列中
char WriteOneparagraphToRingBuffer(stRingBuff *ringBuf,unsigned char *data,unsigned int len)
{
	unsigned int i = 0;
	char Sign = FALSE;

//    if(IsRingBufferEmpty(ringBuf))    //判断队列是否为空
//    {//队列空时，直接写入一段
//        memcpy(ringBuf->buffer,data,len);//mecpy写入数据比一次一字节快
//    }
//    else
    {
        for(i = 0 ; i < len ;i++)
        {
            if (FALSE==WriteOneByteToRingBuffer(ringBuf,data[i]))
            {
                break;
            }
        }
    }

	return	i;
}
//判断队列是否满
char IsRingBufferFull(stRingBuff *ringBuf)
{
	 if (ringBuf == NULL)
    {
        //printf("pointer is null\r\n");
        return FALSE;
    }
    
    if(((ringBuf->in+1) % RING_BUFF_SIZE) == ringBuf->out)
    {
//		printf("Ring buffer is Full\r\n");
        return TRUE;
    }
    return FALSE;
}

//******************************从队列读取数据************************************

//从队列中读取一个字节的数据
char ReadOneByteFromRingBuffer(stRingBuff *ringBuf,unsigned char *data)
{
	if (ringBuf == NULL)
    {
        //printf("pointer is null\r\n");
        return FALSE;
    }
    
    if(IsRingBufferEmpty(ringBuf))    //判断队列是否为空
    {
        return FALSE;
    }

    *data = ringBuf->buffer[ringBuf->out];
    ringBuf->out = (++ringBuf->out) % RING_BUFF_SIZE;   //下标循环到0开始

    return TRUE;
} 


//判断队列是否为空
char IsRingBufferEmpty(stRingBuff *ringBuf)
{ 
	if (ringBuf == NULL)
    {
        //printf("pointer is null\r\n");
        return FALSE; 
    }
    
    if(ringBuf->in == ringBuf->out)   //如果写入下标等于读出下标表示队列是空
    {
//		printf("Ring buffer is Empty\r\n");
        return TRUE;
    }
    return FALSE;
}


//从队列中读取len个字节的数据
int ReadRingBuffer(unsigned char *readBuf,stRingBuff *ringBuf,unsigned int len)
{
    unsigned int rlen;
    
	if (ringBuf == NULL)
    {
        // printf("pointer is null\r\n");
        return -1;
    }

    rlen = GetRingBufferLength(ringBuf);
    if(rlen>len)
    {
        memcpy(readBuf,ringBuf->buffer+ringBuf->out,len);//拷贝数据
        rlen = len;
        ringBuf->out = (ringBuf->out+len) % RING_BUFF_SIZE;   //下标循环到未取数据下标开始
    }
    else
    {
        memcpy(readBuf,ringBuf->buffer+ringBuf->out,rlen);//拷贝数据
        ringBuf->out = (ringBuf->out+rlen) % RING_BUFF_SIZE;   //下标循环到未取数据下标开始
    }

	return rlen;
}








