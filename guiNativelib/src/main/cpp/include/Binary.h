#ifndef		__BINARY_H__
#define		__BINARY_H__

#include "adsStd.h"

class CBinary
{
protected:
	struct tagBinaryData 
	{
		BYTE *m_pucBinary;
		DWORD m_iDataLength;
		DWORD m_iAllocLength;
		DWORD m_iDefaultAddAllocLength;
		DWORD m_iCitationCount;
	}*m_pbinData;

	enum
	{
		BN_ALL_DATA           = -1,
		BN_COMMON_ALLOCLENGTH = 6,
		BN_DEFALT_GROW_LENGTH = 100
	};

public:
	// 缺省构造函数
	CBinary(void);

	// 构造函数
	CBinary(const CBinary& binData);

	// 构造函数，分配内存
	CBinary(W_INT iLength);

	// 构造函数，建立长度为iLength的缓冲区，并将pBuffer的内容复制到缓冲区
	CBinary(const char *pBuffer, W_INT iBufferLength);
	CBinary(BYTE *pucBuffer, W_INT iBufferLength);

	// 析构函数
	~CBinary(void);

public:
	// 复制类
	void operator = (const CBinary binData);

	// 将二进制数据合并
	CBinary& operator += (CBinary& binData);

	// 将二进制数据缓冲区尾部加一个字节数据
	CBinary& operator += (BYTE ucValue);
//	CBinary& operator += (char cValue);

	// 将二进制数据类合并，返回新类
	CBinary operator + (CBinary& binData);

	// 取指定位置的字符数据
	const BYTE operator [] (DWORD nIndex)const; 

	// 比较二个二进制串的大小
	bool operator < (CBinary& binData);
	bool operator <= (CBinary& binData);
	bool operator >= (CBinary& binData);
	bool operator > (CBinary& binData);

	// 比较二个二进制串的大小
	bool operator == (const CBinary& binData);

	// 取指定位置的字符数据
	BYTE GetAt(DWORD nIndex);

	// 改变指定位置的字符数据
	bool SetAt(DWORD nIndex, BYTE ucNewElement);

	// 将二进制数据缓冲区尾部加一个字节数据
//	bool Add(char cNewElement);
	bool Add(BYTE ucNewElement);

	// 设定缓冲区分配的尺寸
	bool SetAllocLength(DWORD iAllocLength, DWORD iGrowBy = BN_DEFALT_GROW_LENGTH);

	// 取数据长度
	DWORD GetSize(void);

	// 取数据缓冲区指针
	const BYTE *GetBuffer(void);//const;

	// 读类中数据到目标缓冲区
	DWORD ReadBuffer(BYTE *pucSrc, DWORD iStart=0, W_INT32 iLength = BN_ALL_DATA);
	DWORD ReadBuffer(char *pSrc, DWORD iStart=0, W_INT32 iLength = BN_ALL_DATA);
	
	// 从指定缓冲区数据写入对象内部
	DWORD WriteBuffer(const BYTE * pucSrc, DWORD iWriteLength);
	DWORD WriteBuffer(const char* pSrc, DWORD iWriteLength);

   // 将数据追加到类中数据的结尾
//	DWORD Append(char *pBuffer, DWORD iWriteLength);
	DWORD Append(BYTE *pucBuffer, DWORD iWriteLength);

	// 类数据是否为空
	bool IsEmpty(void);

	// 释放未使用的内存
	void FreeExtra(void);

	//以下用于调试目的
	DWORD GetBufSize(void);
	DWORD GetCitationCount(void);
	DWORD GetDefaultAddAllocLength(void);

private:
	void CopySelf(CBinary binData);
};

#endif		//__BINARY_H_