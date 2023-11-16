/******************************************************************************

	Copyright (c) 2005, AUTOBOSS Inc.
	All rights reserved.

	文件名称：Binary.cpp
	文档标识：007汽车诊断平台详细设计说明书
	摘    要：本类用于存放二进制数据。实现二进制数据的保存、更改及读取使用引
				用计数，提高效率

	历史记录：
	----------------------------------------------------------------------------
	时     间   作者      版本号       操    作    内    容
	----------------------------------------------------------------------------
	2005.11.25  zhangsh   1.0AX        创建此类。
	2006.07.11  wcx       0.2001       GetSize()在本类未分配内存时，由原来返
									   回-1改为返回0
	2006.11.08  wcx       0.6000       改正操作符+的参数为内容为空时，返回值错
									   误
	2006.11.08  wcx       0.6000       缺省分配长度由1000改为100
	2006.11.08  wcx       0.6000       更正了操作符+错误，包括：
									   1、本身为空时返回值错；
									   2、参数为空时，返回值非法操作；
									   3、累加后内容长度小于4时内存异常。
	2006.11.08  wcx       0.6001       更正了操作符>=数据为等于时判断错误。								   
******************************************************************************/

#include <memory.h>
#include <assert.h>
#include "Binary.h"

/**************************************************************
功    能：缺省构造函数
参数说明：无
返 回 值：无
说    明：初始化成员变量m_pbinData = NULL；
**************************************************************/
CBinary::CBinary(void)
{
	m_pbinData = NULL;
}


/**************************************************************
功    能：构造函数
参数说明：CBinary& binData-源二进制数据
返 回 值：无
说    明：以一个二进制数据初始化变量；
**************************************************************/
CBinary::CBinary(const CBinary& binData)
{
	if(binData.m_pbinData == NULL)
	{
		m_pbinData = NULL;
		return;
	}
    W_INT iBufferLength=binData.m_pbinData->m_iDataLength ;
    BYTE *pBuffer=binData.m_pbinData->m_pucBinary;

    m_pbinData = (tagBinaryData*)new BYTE[sizeof(tagBinaryData)];
    assert(m_pbinData != NULL);

    BYTE* pDataBuf = NULL;
    if( iBufferLength < BN_COMMON_ALLOCLENGTH)
    {
        pDataBuf = new BYTE[BN_COMMON_ALLOCLENGTH];
        assert(pDataBuf != NULL);
    }
    else
    {
        pDataBuf = new BYTE[iBufferLength];
        assert(pDataBuf != NULL);
    }

    memset(m_pbinData, 0, sizeof(tagBinaryData));
    memset(pDataBuf, 0, iBufferLength);

    memcpy(pDataBuf, pBuffer, iBufferLength);

    this->m_pbinData->m_iDataLength = iBufferLength;
    this->m_pbinData->m_pucBinary   = pDataBuf;

	// 按照大的数值分配内存
    if(iBufferLength < BN_COMMON_ALLOCLENGTH)
    {
        this->m_pbinData->m_iAllocLength = BN_COMMON_ALLOCLENGTH;
    }
    else
    {
        this->m_pbinData->m_iAllocLength = iBufferLength;
    }

    this->m_pbinData->m_iCitationCount         = 1;
    this->m_pbinData->m_iDefaultAddAllocLength = BN_DEFALT_GROW_LENGTH;



//    W_INT i=binData.m_pbinData->m_iDataLength ;
//    BYTE *p=binData.m_pbinData->m_pucBinary;
//    CBinary(p,i);
//    W_INT i=binData.GetBufSize();

//    CBinary(binData.GetBuffer(),i);
//	if(binData.m_pbinData == NULL)
//	{
//		this->m_pbinData = NULL;
//	}
//	else
//	{
//		this->m_pbinData = (tagBinaryData*)new BYTE[sizeof(tagBinaryData)];
//		assert(this->m_pbinData != NULL);
//
//		if(binData.m_pbinData->m_pucBinary == NULL)
//		{
//			return;
//		}
//        this->m_pbinData = binData.m_pbinData;
//        this->m_pbinData->m_iCitationCount=1;
//
//		this->m_pbinData = binData.m_pbinData;
//		this->m_pbinData->m_iCitationCount++;//结构引用次数增加一次
//	}
}


/**************************************************************
功    能：构造函数，分配内存
参数说明：W_INT16 iLength，分配的内存字节数
返 回 值：无
说    明：分配成员变量m_pbinData内存，并将并分配内存给结构的
	缓冲指针；
**************************************************************/
CBinary::CBinary(W_INT iLength)
{
	this->m_pbinData = (tagBinaryData*)new BYTE[sizeof(tagBinaryData)];
	assert(m_pbinData != NULL);

    BYTE* pDataBuf = new BYTE[iLength];
	assert(pDataBuf != NULL);

	memset(this->m_pbinData, 0, sizeof(tagBinaryData));
	memset(pDataBuf, 0, iLength);

	this->m_pbinData->m_pucBinary              = pDataBuf;
	this->m_pbinData->m_iAllocLength           = iLength;
	this->m_pbinData->m_iCitationCount         = 1;
	this->m_pbinData->m_iDefaultAddAllocLength = BN_DEFALT_GROW_LENGTH;
}


/**************************************************************
功    能：构造函数，建立长度为iBufferLength的缓冲区，并将pBuffer
	的内容复制到缓冲区
参数说明：iBufferLength：分配的内存长度；pBuffer内容缓冲区指针
返 回 值：无
说    明：无
**************************************************************/
CBinary::CBinary(BYTE *pBuffer, W_INT iBufferLength)
{
	m_pbinData = (tagBinaryData*)new BYTE[sizeof(tagBinaryData)];
	assert(m_pbinData != NULL);

	BYTE* pDataBuf = NULL;
	if(iBufferLength < BN_COMMON_ALLOCLENGTH)
	{
		pDataBuf = new BYTE[BN_COMMON_ALLOCLENGTH];
		assert(pDataBuf != NULL);
	}
	else
	{
		pDataBuf = new BYTE[iBufferLength];
		assert(pDataBuf != NULL);
	}	

	memset(m_pbinData, 0, sizeof(tagBinaryData));
	memset(pDataBuf, 0, iBufferLength);

	memcpy(pDataBuf, pBuffer, iBufferLength);
	
	this->m_pbinData->m_iDataLength = iBufferLength;
	this->m_pbinData->m_pucBinary   = pDataBuf;

	// 按照大的数值分配内存
	if(iBufferLength < BN_COMMON_ALLOCLENGTH)
	{
		this->m_pbinData->m_iAllocLength = BN_COMMON_ALLOCLENGTH;
	}
	else
	{
		this->m_pbinData->m_iAllocLength = iBufferLength;
	}

	this->m_pbinData->m_iCitationCount         = 1;
	this->m_pbinData->m_iDefaultAddAllocLength = BN_DEFALT_GROW_LENGTH;
}

CBinary::CBinary( const char *pBuffer, W_INT iBufferLength)
{
	m_pbinData = (tagBinaryData*)new BYTE[sizeof(tagBinaryData)];
	assert(m_pbinData != NULL);

	BYTE* pDataBuf = NULL;
	if(iBufferLength < BN_COMMON_ALLOCLENGTH)
	{
		pDataBuf = new BYTE[BN_COMMON_ALLOCLENGTH];
		assert(pDataBuf != NULL);
	}
	else
	{
		pDataBuf = new BYTE[iBufferLength];
		assert(pDataBuf != NULL);
	}	

	memset(m_pbinData, 0, sizeof(tagBinaryData));
	memset(pDataBuf, 0, iBufferLength);

	memcpy(pDataBuf, pBuffer, iBufferLength);
	
	this->m_pbinData->m_iDataLength = iBufferLength;
	this->m_pbinData->m_pucBinary   = pDataBuf;

	// 按照大的数值分配内存
	if(iBufferLength < BN_COMMON_ALLOCLENGTH)
	{
		this->m_pbinData->m_iAllocLength = BN_COMMON_ALLOCLENGTH;
	}
	else
	{
		this->m_pbinData->m_iAllocLength = iBufferLength;
	}

	this->m_pbinData->m_iCitationCount         = 1;
	this->m_pbinData->m_iDefaultAddAllocLength = BN_DEFALT_GROW_LENGTH;

}

/**************************************************************
功    能：析构函数
参数说明：无
返 回 值：无
说    明：引用数减1，如结果为0释放缓冲区及结构内存
**************************************************************/
CBinary::~CBinary(void)
{
	if(NULL == m_pbinData)
	{
		return;
	}

	m_pbinData->m_iCitationCount--;
	if(0 == m_pbinData->m_iCitationCount)
	{
		if(m_pbinData->m_pucBinary != NULL)
		{
			delete [] m_pbinData->m_pucBinary;
			m_pbinData->m_pucBinary = NULL;
		}

		if(m_pbinData != NULL)
		{
			delete [] m_pbinData;
			m_pbinData = NULL;
		}
	}
}


/************************************************************
功    能：将二进制数据类合并，返回新类
参数说明：CBinary binData，被合并的类
返 回 值：无
说    明：注意加自身的处理
************************************************************/
CBinary CBinary::operator + (CBinary& binData)
{
	CBinary binTemp;
	
	if(this->m_pbinData == NULL)
		return binData;
	if(binData.m_pbinData == NULL)
		return *this;

	binTemp.m_pbinData = (tagBinaryData*)new BYTE[sizeof(tagBinaryData)];
	assert(binTemp.m_pbinData != NULL);
	memset(binTemp.m_pbinData, 0, sizeof(tagBinaryData));

	DWORD dataLength = this->m_pbinData->m_iDataLength + binData.m_pbinData->m_iDataLength;
	BYTE* pBuf = new BYTE[dataLength];
	assert(pBuf != NULL);

	memcpy(pBuf, this->m_pbinData->m_pucBinary,  this->m_pbinData->m_iDataLength);
	memcpy(pBuf+this->m_pbinData->m_iDataLength, binData.m_pbinData->m_pucBinary, binData.m_pbinData->m_iDataLength);

	binTemp.m_pbinData->m_pucBinary              = pBuf;
	binTemp.m_pbinData->m_iDataLength            = dataLength;
	binTemp.m_pbinData->m_iAllocLength           = dataLength;
	binTemp.m_pbinData->m_iCitationCount         = 1;
	binTemp.m_pbinData->m_iDefaultAddAllocLength = BN_DEFALT_GROW_LENGTH;

	return binTemp;
}


/****************************************************************************
功    能：将二进制数据合并
参数说明：无
返 回 值：无
说    明：
****************************************************************************/
CBinary& CBinary::operator += (CBinary& binData)
{
	if(binData.m_pbinData == NULL)
	{
		return *this;
	}

	this->Append(binData.m_pbinData->m_pucBinary, binData.m_pbinData->m_iDataLength);

	return *this;
}

/*************************************************************
功    能：将二进制数据缓冲区尾部加一个字节数据
参数说明：被追加的字符
返 回 值：CBinary&
说    明：无
*************************************************************/

CBinary& CBinary::operator += (BYTE ucValue)
{
	Append(&ucValue, 1); 

	return *this;
}


/***********************************************************
功    能：读取指定位置的字符数据
参数说明：数据位置
返 回 值：指定位置的字节数据
说    明：
***********************************************************/
const BYTE CBinary::operator [] (DWORD nIndex)const
{
	if(this->m_pbinData == NULL)
		return 0xfd;//windows中当没有赋值的CBinary取值会返回值0xfd;为了不让进程退出

	if( nIndex >= this->m_pbinData->m_iDataLength)
		return 0xfd;//windows中当没有赋值的CBinary取值会返回值0xfd;为了不让进程退出

	assert(this->m_pbinData != NULL);
	assert(nIndex >= 0 && nIndex < this->m_pbinData->m_iDataLength) ;

	return (BYTE)this->m_pbinData->m_pucBinary[nIndex];
}

/*****************************************************
功    能：复制类
参数说明：CBinary binData，
返 回 值：无
说    明：结构指针指向类binData的地址，其引用数加1；
	注意自身赋值的处理
*****************************************************/
void CBinary::operator = (const CBinary binData)
{
	if(binData.m_pbinData == NULL)//若右值为空
	{
		if(this->m_pbinData != NULL)
		{			
			if(this->m_pbinData->m_iCitationCount > 1)
			{
				this->m_pbinData->m_iCitationCount--;
			}
			else
			{	
				// 在没有别的引用条件下先释放自己
				delete [] this->m_pbinData->m_pucBinary;
				delete [] this->m_pbinData;
			}
		}

		this->m_pbinData = NULL;
		return;
	}

	// 若左值为空
	if(this->m_pbinData == NULL) 
	{
		this->m_pbinData = binData.m_pbinData;

		// 结构引用次数增加一次
		this->m_pbinData->m_iCitationCount++;

		return;
	}

	//内容非空，但是‘A＝A’
	if(this->m_pbinData == binData.m_pbinData)
	{
		return;
	}

	// 左右值均为非空
	if(this->m_pbinData->m_iCitationCount > 1 )
	{
		this->m_pbinData->m_iCitationCount--;
	}
	else
	{
		// 在没有别的引用条件下先释放自己
		delete [] this->m_pbinData->m_pucBinary;
		delete [] this->m_pbinData;
	}

	this->m_pbinData = binData.m_pbinData;

	// 结构引用次数增加一次
	this->m_pbinData->m_iCitationCount++;
}

bool CBinary::operator <= (CBinary& binData)
{
	if(*this < binData)
		return true;
	if(*this == binData)
		return true;

	return false;
}

bool CBinary::operator >= (CBinary& binData)
{
//	return binData < *this;
	
	return !(*this < binData);
}

bool CBinary::operator > (CBinary& binData)
{
	return binData <= *this;
}

/******************************************************
功    能：比较二个二进制串的大小
参数说明：被比较的类
返 回 值：比较结果
说    明：从前向后逐个字节比较，
		  如果bin1的字节内容值小于bin2则返回真，
		  如果长度不等，则长度大的大。
******************************************************/
bool CBinary::operator < (CBinary& binData)
{
	if(binData.m_pbinData == NULL)
	{
		return false;
	}
	else if(this->m_pbinData == NULL)
	{
		return true;
	}


	DWORD lengMin = min(this->m_pbinData->m_iDataLength, binData.m_pbinData->m_iDataLength);
	int result = memcmp(this->m_pbinData->m_pucBinary, binData.m_pbinData->m_pucBinary, lengMin);

	if(result < 0)
	{
		return true;
	}
	else if(result == 0)
	{
		bool bResult = (this->m_pbinData->m_iDataLength < binData.m_pbinData->m_iDataLength) ? true : false;
		return bResult;
	}
	else
	{
		return false;
	}
}


/******************************************************
功    能：比较二个二进制数据的串内容是否相等
参数说明：被比较的类
返 回 值：比较结果
说    明：从前向后逐个字节比较，
		  如果bin1的字节内容值等于bin2则返回真，否则返回假。
******************************************************/
bool CBinary::operator == (const CBinary& binData)
{
	// 如果有空值
	if(this->m_pbinData == NULL || binData.m_pbinData == NULL)
	{
		if(this->m_pbinData == NULL && binData.m_pbinData == NULL)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	if(this->m_pbinData == binData.m_pbinData)
	{
		return true;
	}

	if(this->m_pbinData->m_iDataLength != binData.m_pbinData->m_iDataLength)
	{
		return false;
	}

	int result = memcmp(this->m_pbinData->m_pucBinary, binData.m_pbinData->m_pucBinary, binData.m_pbinData->m_iDataLength);
	return (result == 0)?true:false;
}


/******************************************************
功    能：取指定位置的字符数据
参数说明：数据位置
返 回 值：指定位置的字节数据
说    明：无
******************************************************/
BYTE CBinary::GetAt(DWORD nIndex)
{
	assert(this->m_pbinData != NULL);
	assert((0 <= nIndex) && (nIndex < this->m_pbinData->m_iDataLength)) ;

	return this->m_pbinData->m_pucBinary[nIndex];
}

/******************************************************
功    能：改变指定位置的字符数据
参数说明：数据位置
返 回 值：成功—false  失败—true
说    明：如果新元素与原元素相同则无操作，否则修改引用
	计数，重新分配内存，复制原数据内容，更改元素内容。
******************************************************/
bool CBinary::SetAt(DWORD nIndex, BYTE ucNewElement)
{
	if(this->m_pbinData == NULL)
	{
		return false;
	}

	if((nIndex >= this->m_pbinData->m_iDataLength) || (nIndex < 0))
	{
		return false;
	}

	if(this->m_pbinData->m_iCitationCount > 1)
	{
		CopySelf(*this);
	}

	this->m_pbinData->m_pucBinary[nIndex] = ucNewElement;
	return true;
}


/******************************************************
功    能：将二进制数据缓冲区尾部加一个字节数据
参数说明：被追加的字符
返 回 值：成功—ture  失败—false
说    明：无
******************************************************/
bool CBinary::Add(BYTE cNewElement)
/*{
	return Add((char) ucNewElement);
}

bool CBinary::Add(char cNewElement)
*/
{
	DWORD iLenth = Append(&cNewElement, 1);

	if(1 != iLenth)
	{
		return false;
	}

	return true;
}


/******************************************************************
功    能：	设定缓冲区分配的尺寸
参数说明：	iAllocLength—新缓冲区尺寸,
			iGrowBy—缺省一次增加的内存单元数量
返 回 值：	成功—false  失败—true
说    明：
******************************************************************/
bool CBinary::SetAllocLength(DWORD iAllocLength, DWORD iGrowBy)
{
	if(this->m_pbinData == NULL)
	{
		this->m_pbinData = (tagBinaryData*)new BYTE[sizeof(tagBinaryData)];
		assert(this->m_pbinData != NULL);

		BYTE* pDataBuf = new BYTE[iAllocLength];
		assert(pDataBuf != NULL);

		memset(m_pbinData, 0, sizeof(tagBinaryData));
		memset(pDataBuf,   0, iAllocLength);

		this->m_pbinData->m_pucBinary              = pDataBuf;
		this->m_pbinData->m_iAllocLength           = iAllocLength;
		this->m_pbinData->m_iCitationCount         = 1;
		this->m_pbinData->m_iDefaultAddAllocLength = iGrowBy;
	}
	else
	{
		if(iAllocLength < m_pbinData->m_iDataLength)
		{
			return false;
		}

		if(this->m_pbinData->m_iCitationCount> 1)
		{
			CopySelf(*this);
		}

		BYTE* pDataBuf = new BYTE[iAllocLength];
		assert(pDataBuf != NULL);

		memcpy(pDataBuf, this->m_pbinData->m_pucBinary, this->m_pbinData->m_iDataLength);
	
		delete [] this->m_pbinData->m_pucBinary;

		this->m_pbinData->m_pucBinary              = pDataBuf;
		this->m_pbinData->m_iAllocLength           = iAllocLength;
		this->m_pbinData->m_iDefaultAddAllocLength = iGrowBy;
	}

	return true;
}


/*****************************************************************
功    能：取数据长度
参数说明：无
返 回 值：数据长度
说    明：无
*****************************************************************/
DWORD CBinary::GetSize(void)
{
	if(this->m_pbinData != NULL)
	{
		return this->m_pbinData->m_iDataLength;
	}
	else
	{
		return 0 ;
	}
}


/*****************************************************************
功    能：取缓冲区长度
参数说明：无
返 回 值：数据长度
说    明：调试接口
*****************************************************************/
DWORD CBinary::GetBufSize(void)
{
	if(this->m_pbinData != NULL)
	{
		return this->m_pbinData->m_iAllocLength;
	}
	else
	{
		return -1 ;
	}
}


/*****************************************************************
功    能：取数据的引用次数
参数说明：无
返 回 值：数据长度
说    明：调试接口
*****************************************************************/
DWORD CBinary::GetCitationCount(void)
{
	if(this->m_pbinData != NULL)
	{
		return this->m_pbinData->m_iCitationCount;
	}
	else
	{
		return -1;
	}
}


/*****************************************************************
功    能：取数据的缓冲区默认增长值
参数说明：无
返 回 值：数据长度
说    明：调试接口
*****************************************************************/
DWORD CBinary::GetDefaultAddAllocLength(void)
{
	if(this->m_pbinData != NULL)
	{
		return this->m_pbinData->m_iDefaultAddAllocLength;
	}
	else
	{
		return -1 ;
	}
}


/****************************************************************
功    能：取数据缓冲区指针
参数说明：无
返 回 值：数据缓冲区指针
说    明：无
****************************************************************/
const BYTE* CBinary::GetBuffer(void)//const
{
	if(this->m_pbinData != NULL && this->m_pbinData->m_pucBinary != NULL)
	{
		return	this->m_pbinData->m_pucBinary;
	}
	else
	{
		return NULL;
	}
}

/***************************************************************
功    能：	从类读取数据到数据缓冲区
参数说明：	char* pSrc 数据缓冲区指针；
			W_INT16 iStart读取起始位置，默认值为0，从头读；
			W_INT16 iLength读取长度，默认值为-1，从起始位置读到结尾
返 回 值：	实际读取的字节数
说    明：	类的数据长度设定为读入的长度
***************************************************************/
DWORD CBinary::ReadBuffer(BYTE *pucSrc, DWORD iStart, W_INT32 iLength)
{
	return ReadBuffer((char *)pucSrc, iStart, iLength);
}

DWORD CBinary::ReadBuffer(char *pSrc, DWORD iStart, W_INT32 iLength)
{
	if(iStart < 0)
	{
		iStart = 0;
	}

	if(this->m_pbinData == NULL)
	{
		return 0;
	}

	// 超出拷贝范围(未分配数据时必然会超出范围)
	if(iStart > this->m_pbinData->m_iDataLength-1)
	{
		return 0;
	}

	if(iLength <= BN_ALL_DATA)
	{
		iLength = this->m_pbinData->m_iDataLength-iStart;
		memcpy(pSrc, this->m_pbinData->m_pucBinary+iStart, iLength);
	}
	else
	{
		if((iStart+iLength) > (this->m_pbinData->m_iDataLength))
		{
			iLength = this->m_pbinData->m_iDataLength - iStart;
		}
		memcpy(pSrc, this->m_pbinData->m_pucBinary+iStart, iLength);
	}

	return iLength;
}


/**************************************************************
功    能：写数据到缓冲区
参数说明：目标缓冲区指针
返 回 值：实际写入的字节数
说    明：写入的长度为类的数据长度
**************************************************************/
DWORD CBinary::WriteBuffer(const BYTE* pucSrc, DWORD iWriteLength)
{
	return WriteBuffer((char*)pucSrc, iWriteLength);
}
DWORD CBinary::WriteBuffer(const char* pSrc, DWORD iWriteLength)
{
	assert(pSrc != NULL);

	if(this->m_pbinData == NULL)
	{
		this->m_pbinData = (tagBinaryData*)new BYTE[sizeof(tagBinaryData)];
		assert(this->m_pbinData != NULL);

		BYTE* pBuf = NULL;
		if(iWriteLength < BN_COMMON_ALLOCLENGTH)
		{
			pBuf = new BYTE[BN_COMMON_ALLOCLENGTH];
			assert(pBuf != NULL);
		}
		else
		{
			pBuf = new BYTE[iWriteLength];
			assert(pBuf != NULL);
		}

		memcpy(pBuf, pSrc, iWriteLength);
		this->m_pbinData->m_pucBinary = pBuf;
		this->m_pbinData->m_iDataLength = iWriteLength;

		if(iWriteLength < BN_COMMON_ALLOCLENGTH)
		{
			this->m_pbinData->m_iAllocLength = BN_COMMON_ALLOCLENGTH;
		}
		else
		{
			this->m_pbinData->m_iAllocLength = iWriteLength;
		}

		this->m_pbinData->m_iCitationCount         = 1;
		this->m_pbinData->m_iDefaultAddAllocLength = BN_DEFALT_GROW_LENGTH;

		return iWriteLength;
	}

	if(this->m_pbinData->m_iCitationCount>1)
	{
		CopySelf(*this);
	}

	if(this->m_pbinData->m_iAllocLength >= iWriteLength)
	{
		memset(this->m_pbinData->m_pucBinary, 0,    this->m_pbinData->m_iAllocLength);
		memcpy(this->m_pbinData->m_pucBinary, pSrc, iWriteLength);
		this->m_pbinData->m_iDataLength = iWriteLength;
	}
	else
	{
		BYTE* pBuf = new BYTE[iWriteLength];
		assert(pBuf != NULL);

		memcpy(pBuf, pSrc, iWriteLength);
		
		delete [] this->m_pbinData->m_pucBinary;
		this->m_pbinData->m_pucBinary = pBuf;
		this->m_pbinData->m_iAllocLength = iWriteLength;
		this->m_pbinData->m_iDataLength = iWriteLength;
	}

	return iWriteLength;
}


/**************************************************************
功    能：将数据追加到类中数据的结尾
参数说明：char*pBuffer     数据缓冲区指针；
		  W_INT16 iLength  读入长度
返 回 值：实际读入的字节数
说    明：
**************************************************************/
DWORD CBinary::Append(BYTE *pBuffer, DWORD iWriteLength)
/*
{
	return Append((char *)pucBuffer, iWriteLength);
}

DWORD CBinary::Append(char *pBuffer, DWORD iWriteLength)
*/
{
	assert(pBuffer != NULL);

	if(iWriteLength <= 0)
	{
		return 0;
	}

	if(this->m_pbinData == NULL)
	{
		m_pbinData = (tagBinaryData*)new BYTE[sizeof(tagBinaryData)];
		assert(m_pbinData != NULL);

		BYTE* pBuf = NULL;
		if(iWriteLength < BN_COMMON_ALLOCLENGTH)
		{
			pBuf = new BYTE[BN_COMMON_ALLOCLENGTH];
			assert(pBuf != NULL);
		}
		else
		{
			pBuf = new BYTE[iWriteLength];
			assert(pBuf != NULL);
		}

		memcpy(pBuf, pBuffer, iWriteLength);
		this->m_pbinData->m_pucBinary = pBuf;
		this->m_pbinData->m_iDataLength = iWriteLength;

		if(iWriteLength < BN_COMMON_ALLOCLENGTH)
		{
			this->m_pbinData->m_iAllocLength = BN_COMMON_ALLOCLENGTH;
		}
		else
		{
			this->m_pbinData->m_iAllocLength = iWriteLength;
		}

		this->m_pbinData->m_iCitationCount         = 1;
		this->m_pbinData->m_iDefaultAddAllocLength = BN_DEFALT_GROW_LENGTH;

		return iWriteLength;
	}


	if(this->m_pbinData->m_iCitationCount > 1)
	{
		CopySelf(*this);
	}

	if(this->m_pbinData->m_iAllocLength >= 	this->m_pbinData->m_iDataLength+iWriteLength)
	{
		memcpy((this->m_pbinData->m_pucBinary+this->m_pbinData->m_iDataLength),
				pBuffer,
				iWriteLength);
		this->m_pbinData->m_iDataLength += iWriteLength;
	}
	else
	{
		DWORD iBufLength;
		if(iWriteLength < BN_DEFALT_GROW_LENGTH)
		{
			iBufLength = this->m_pbinData->m_iDataLength + BN_DEFALT_GROW_LENGTH;
		}
		else
		{
			iBufLength = this->m_pbinData->m_iDataLength + iWriteLength;
		}

		BYTE* pBuf = new BYTE[iBufLength];
		assert(pBuf != NULL);

		memcpy(pBuf, this->m_pbinData->m_pucBinary, this->m_pbinData->m_iDataLength);
		memcpy(pBuf+this->m_pbinData->m_iDataLength, pBuffer, iWriteLength);

		delete [] this->m_pbinData->m_pucBinary;
		this->m_pbinData->m_pucBinary = pBuf;
		this->m_pbinData->m_iDataLength = this->m_pbinData->m_iDataLength + iWriteLength;
		this->m_pbinData->m_iAllocLength = iBufLength;		
	}

	return iWriteLength;
}


/**************************************************************
功    能：判断类数据是否为空
参数说明：无
返 回 值：为空—true; 不为空—false
说    明：调试接口
**************************************************************/
bool CBinary::IsEmpty(void)
{
	if(this->m_pbinData == NULL)
	{
		return true;
	}

	if(this->m_pbinData->m_iDataLength > 0)
	{
		return false;
	}
	else
	{
		return true;
	}
}

/**************************************************************
功    能：释放未使用的内存
参数说明：无
返 回 值：无
说    明：无
**************************************************************/
void CBinary::FreeExtra(void)
{
	if(this->m_pbinData == NULL)
	{
		return;
	}

	if(this->m_pbinData->m_iAllocLength > this->m_pbinData->m_iDataLength)
	{
		if(this->m_pbinData->m_iCitationCount > 1)
		{
			CopySelf(*this);
		}

		DWORD iBufLength;
		iBufLength = this->m_pbinData->m_iDataLength;
		BYTE* pBuf = new BYTE[iBufLength];
		assert(pBuf != NULL);

		memcpy(pBuf, this->m_pbinData->m_pucBinary, this->m_pbinData->m_iDataLength);

		delete [] this->m_pbinData->m_pucBinary;
		this->m_pbinData->m_pucBinary = pBuf;
		this->m_pbinData->m_iAllocLength = iBufLength;
		this->m_pbinData->m_iDataLength = iBufLength;
	}
}


/**************************************************************
功    能：复制自身，返回复制类
参数说明：CBinary binData－需要复制的类变量
返 回 值：无
说    明：因复制，故原结构的引用次数需要减一，私有接口
**************************************************************/
void CBinary::CopySelf(CBinary binData)
{
	if(binData.m_pbinData == NULL)
	{
		return;
	}	 

	this->m_pbinData = (tagBinaryData*)new BYTE[sizeof(tagBinaryData)];
	assert(this->m_pbinData != NULL);

	DWORD iBufLength = binData.m_pbinData->m_iAllocLength;
	BYTE* pBuf = new BYTE[iBufLength];
	assert(pBuf != NULL);

	memcpy(pBuf, binData.m_pbinData->m_pucBinary, iBufLength);

	this->m_pbinData->m_pucBinary              = pBuf;	
	this->m_pbinData->m_iDataLength            = binData.m_pbinData->m_iDataLength;
	this->m_pbinData->m_iAllocLength           = binData.m_pbinData->m_iAllocLength;
	this->m_pbinData->m_iCitationCount         = 1;
	this->m_pbinData->m_iDefaultAddAllocLength = BN_DEFALT_GROW_LENGTH;

	// 原结构的引用次数减一
	binData.m_pbinData->m_iCitationCount--; 
}
