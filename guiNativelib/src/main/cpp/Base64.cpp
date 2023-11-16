//#include "pch.h"
#include "Base64.h"
#include <iostream> 

CBase64 g_Base64;

static const char * DATA_BIN2ASCII = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

int CBase64::Base64_Encode(const unsigned char * inputBuffer, int inputCount, char * outputBuffer)
{
	int i;
	unsigned char b0, b1, b2;

	if ((inputBuffer == NULL) || (inputCount < 0))
	{
		return -1;
	}

	if (outputBuffer != NULL)
	{
		for (i = inputCount; i > 0; i -= 3)
		{
			if (i >= 3)
			{
				b0 = *inputBuffer++;
				b1 = *inputBuffer++;
				b2 = *inputBuffer++;

				*outputBuffer++ = DATA_BIN2ASCII[b0 >> 2];
				*outputBuffer++ = DATA_BIN2ASCII[((b0 << 4) | (b1 >> 4)) & 0x3F];
				*outputBuffer++ = DATA_BIN2ASCII[((b1 << 2) | (b2 >> 6)) & 0x3F];
				*outputBuffer++ = DATA_BIN2ASCII[b2 & 0x3F];
			}
			else
			{
				b0 = *inputBuffer++;
				if (i == 2)b1 = *inputBuffer++; else b1 = 0;

				*outputBuffer++ = DATA_BIN2ASCII[b0 >> 2];
				*outputBuffer++ = DATA_BIN2ASCII[((b0 << 4) | (b1 >> 4)) & 0x3F];
				*outputBuffer++ = (i == 1) ? '=' : DATA_BIN2ASCII[(b1 << 2) & 0x3F];
				*outputBuffer++ = '=';
			}
		}

		*outputBuffer++ = '\0';
	}

	return ((inputCount + 2) / 3) * 4;
}

#define B64_EOLN			0xF0	
#define B64_CR				0xF1	
#define B64_EOF				0xF2	
#define B64_WS				0xE0	
#define B64_ERROR       	0xFF	
#define B64_NOT_BASE64(a)	(((a)|0x13) == 0xF3)

static const unsigned char DATA_ASCII2BIN[128] = {
	0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xE0,0xF0,0xFF,0xFF,0xF1,0xFF,0xFF,
	0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,
	0xE0,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0x3E,0xFF,0xF2,0xFF,0x3F,
	0x34,0x35,0x36,0x37,0x38,0x39,0x3A,0x3B,0x3C,0x3D,0xFF,0xFF,0xFF,0x00,0xFF,0xFF,
	0xFF,0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,
	0x0F,0x10,0x11,0x12,0x13,0x14,0x15,0x16,0x17,0x18,0x19,0xFF,0xFF,0xFF,0xFF,0xFF,
	0xFF,0x1A,0x1B,0x1C,0x1D,0x1E,0x1F,0x20,0x21,0x22,0x23,0x24,0x25,0x26,0x27,0x28,
	0x29,0x2A,0x2B,0x2C,0x2D,0x2E,0x2F,0x30,0x31,0x32,0x33,0xFF,0xFF,0xFF,0xFF,0xFF
};

int CBase64::Base4_Decode(const char * inputBuffer, int inputCount, unsigned char * outputBuffer)
{
	int i, j;
	unsigned char b[4];
	char ch;

	if ((inputBuffer == NULL) || (inputCount < 0))
	{
		return -1;
	}

	while (inputCount > 0)
	{
		ch = *inputBuffer;
		if ((ch < 0) || (ch >= 0x80))
		{
			return -2;
		}
		else
		{
			if (DATA_ASCII2BIN[ch] == B64_WS)
			{
				inputBuffer++;
				inputCount--;
			}
			else
			{
				break;
			}
		}
	}


	while (inputCount >= 4)
	{
		ch = inputBuffer[inputCount - 1];
		if ((ch < 0) || (ch >= 0x80))
		{
			return -2;
		}
		else
		{
			if (B64_NOT_BASE64(DATA_ASCII2BIN[ch]))
			{
				inputCount--;
			}
			else
			{
				break;
			}
		}
	}

	if ((inputCount % 4) != 0)
	{
		return -2;
	}

	if (outputBuffer != NULL)
	{
		for (i = 0; i < inputCount; i += 4)
		{
			for (j = 0; j < 4; j++)
			{
				ch = *inputBuffer++;
				if ((ch < 0) || (ch >= 0x80))
				{
					return -2;
				}
				else
				{
					if (ch == '=')
					{
						break;
					}
					else
					{
						b[j] = DATA_ASCII2BIN[ch];
						if (b[j] & 0x80)
						{
							return -2;
						}
					}
				}
			} // End for j

			if (j == 4)
			{
				*outputBuffer++ = (b[0] << 2) | (b[1] >> 4);
				*outputBuffer++ = (b[1] << 4) | (b[2] >> 2);
				*outputBuffer++ = (b[2] << 6) | b[3];
			}
			else if (j == 3)
			{
				*outputBuffer++ = (b[0] << 2) | (b[1] >> 4);
				*outputBuffer++ = (b[1] << 4) | (b[2] >> 2);

				return (i >> 2) * 3 + 2;
			}
			else if (j == 2)
			{
				*outputBuffer++ = (b[0] << 2) | (b[1] >> 4);

				return (i >> 2) * 3 + 1;
			}
			else
			{
				return -2;
			}
		}	
	}

	return (inputCount >> 2) * 3;
}

string CBase64::UrlEncode(const string & str, string & dst)
{
	dst = "";
	size_t length = str.length();

	for (size_t i = 0; i < length; i++)
	{
		if (isalnum((unsigned char)str[i]) ||
			(str[i] == '-') ||
			(str[i] == '_') ||
			(str[i] == '.') ||
			(str[i] == '~'))
		{
			dst += str[i];
		}
		else if (str[i] == ' ')
		{
			dst += "+";
		}
		else
		{
			dst += '%';
			dst += ToHex((unsigned char)str[i] >> 4);
			dst += ToHex((unsigned char)str[i] % 16);
		}
	}

	return dst;
}

string CBase64::UrlDecode(const string & str, string & dst)
{
	dst = "";
	size_t length = str.length();

	for (size_t i = 0; i < length; i++) 
	{
		if (str[i] == '+') 
		{
			dst += ' ';
		}
		else if (i + 2 < length && str[i] == '%') 
		{
			unsigned char high = FromHex((unsigned char)str[++i]);
			unsigned char low = FromHex((unsigned char)str[++i]);
			dst += high * 16 + low;
		}
		else 
		{
			dst += str[i];
		}
	}
	return dst;


}

unsigned char CBase64::ToHex(unsigned char x)
{
	return x > 9 ? x + 55 :x + 48;
}

unsigned char CBase64::FromHex(unsigned char x)
{
	unsigned char y;
	if (x >= 'A' && x <= 'Z')
	{
		y = x - 'A' + 10;
	}
	else if (x >= 'a' && x <= 'z')
	{
		y = x - 'a' + 10;
	}
	else if (x >= '0' && x <= '9')
	{
		y = x - '0';
	}
	else
	{
		return 0;
	}

	return y;
}
