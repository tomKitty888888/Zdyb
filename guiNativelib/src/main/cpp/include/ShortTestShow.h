// ShortTestShow.h: interface for the CShortTestShow class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_SHORTTESTSHOW_H__CDF8DA24_5BF3_46CA_A6BA_BC332F0374F5__INCLUDED_)
#define AFX_SHORTTESTSHOW_H__CDF8DA24_5BF3_46CA_A6BA_BC332F0374F5__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Gui.h"
#include "adsStd.h"
#include "Binary.h"

class CShortTestShow  
{
public:
	CShortTestShow();
	virtual ~CShortTestShow();

public:
	enum
	{
		DT_SHORT_TEST           = 100
	};

	//added by johnnyling
	W_INT16	m_nEnterIndex;
	W_INT16 GetEnterIndex(void);

	void Init (const char* pTitle = NULL);
	void Init (CBinary idTitle);
	void Init (string strTitle);	

	W_INT16 AddItem (const char* pContain);
	W_INT16 AddItem (string strContain);
	W_INT16 AddItem (CBinary idContain);


	W_INT16 ChangeValue (CBinary idValue);
	W_INT16 ChangeValue (const char* pValue);
	W_INT16 ChangeValue (string strValue);

	W_INT16 Show ();

};

#endif // !defined(AFX_SHORTTESTSHOW_H__CDF8DA24_5BF3_46CA_A6BA_BC332F0374F5__INCLUDED_)
