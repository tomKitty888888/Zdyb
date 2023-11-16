// VehicleInfo.h: interface for the CVehicleInfo class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_VEHICLEINFO_H__F854BEAC_718F_4C36_AD63_4C4663114F6F__INCLUDED_)
#define AFX_VEHICLEINFO_H__F854BEAC_718F_4C36_AD63_4C4663114F6F__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Gui.h"
#include "adsStd.h"
#include "Binary.h"

class CVehicleInfo  
{
public:
	CVehicleInfo();
	virtual ~CVehicleInfo();

public:
	void Init (const char *pTitle = NULL);	
	void Init (string strTitle);
	void Init (CBinary idTitle);
	
	W_INT16 Add (string strExplain, string strContain);
	W_INT16 Add (CBinary idExplain, string strContain);

	W_INT16 Show ();
};

#endif // !defined(AFX_VEHICLEINFO_H__F854BEAC_718F_4C36_AD63_4C4663114F6F__INCLUDED_)
