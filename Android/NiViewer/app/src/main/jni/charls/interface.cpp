//
// (C) Jan de Vaan 2007-2010, all rights reserved. See the accompanying "License.txt" for licensed use.
//


//implement correct linkage for win32 dlls
#if defined(_WIN32)
#define CHARLS_IMEXPORT(returntype) __declspec(dllexport) returntype __stdcall
#endif

#include "config.h"
#include "util.h"
#include "interface.h"
#include "header.h"

JNIEXPORT void JNICALL Java_de_windowsfreak_testjni_codec_CharLSCodec_hello(JNIEnv * env, jobject obj){

    return ;
}

JNIEXPORT jint JNICALL Java_de_windowsfreak_testjni_codec_CharLSCodec_encode(JNIEnv * env, jobject obj, jobject compressedData, jint compressedLength, jobject uncompressedData, jint uncompressedLength, jint x, jint y, jint allowedlossyerror, jint MAXVAL, jint T1, jint T2, jint T3, jint RESET){
    JlsParameters params;
    memset(&params,0,sizeof(JlsParameters));
    params.width = x;
    params.height = y;
    params.bitspersample =16;
    params.bytesperline = 2*x;
    params.components =1;
    params.allowedlossyerror = allowedlossyerror;
    params.ilv=ILV_NONE;
    params.colorTransform=0;
    params.outputBgr=false;
    params.custom.MAXVAL = MAXVAL;
    params.custom.T1 = T1;
    params.custom.T2 = T2;
    params.custom.T3 = T3;
    params.custom.RESET = RESET;

    jbyte * comp_bytes;
    jbyte * uncomp_bytes;
    int bytesWritten;

    comp_bytes = (jbyte*)env->GetDirectBufferAddress((jobject)compressedData);
    uncomp_bytes = (jbyte*)env->GetDirectBufferAddress((jobject)uncompressedData);

    int err = JpegLsEncode(comp_bytes, compressedLength, (size_t*)&bytesWritten,uncomp_bytes, uncompressedLength, &params);

    if(err == 0)
        return bytesWritten;
    return -err;
}
JNIEXPORT jint JNICALL Java_de_windowsfreak_testjni_codec_CharLSCodec_decode(JNIEnv * env, jobject obj, jobject compressedData, jint compressedLength, jobject uncompressedData, jint uncompressedLength, jint x, jint y, jint allowedlossyerror, jint MAXVAL, jint T1, jint T2, jint T3, jint RESET){
    JlsParameters params;
    memset(&params,0,sizeof(JlsParameters));
    params.width = x;
    params.height = y;
    params.bitspersample =16;
    params.bytesperline = 2*x;
    params.components =1;
    params.allowedlossyerror = allowedlossyerror;
    params.ilv=ILV_NONE;
    params.colorTransform=0;
    params.outputBgr=false;
    params.custom.MAXVAL = MAXVAL;
    params.custom.T1 = T1;
    params.custom.T2 = T2;
    params.custom.T3 = T3;
    params.custom.RESET = RESET;

    jbyte * comp_bytes;
    jbyte * uncomp_bytes;

    comp_bytes = (jbyte*)env->GetDirectBufferAddress((jobject)compressedData);
    uncomp_bytes = (jbyte*)env->GetDirectBufferAddress((jobject)uncompressedData);

    int err = JpegLsDecode(uncomp_bytes, uncompressedLength, comp_bytes, compressedLength, &params);
    if(err==0)
        return x*y*2;
    return -err;
}
JLS_ERROR CheckInput(const void* compressedData, size_t compressedLength, const void* uncompressedData, size_t uncompressedLength, const JlsParameters* pparams)
{
	if (pparams == NULL)
		return InvalidJlsParameters;

	if (compressedLength == 0)
		return InvalidJlsParameters;

	if (compressedData == NULL)
		return InvalidJlsParameters;

	if (uncompressedData == NULL)
		return InvalidJlsParameters;

	if (pparams->width < 1 || pparams->width > 65535)
		return ParameterValueNotSupported;

	if (pparams->height < 1 || pparams->height > 65535)
		return ParameterValueNotSupported;

	int bytesperline = pparams->bytesperline < 0 ? -pparams->bytesperline : pparams->bytesperline;

	if (uncompressedLength < size_t(bytesperline * pparams->height))
		return InvalidJlsParameters;

	return CheckParameterCoherent(pparams);
}



extern "C"
{

CHARLS_IMEXPORT(JLS_ERROR) JpegLsEncode(void* compressedData, size_t compressedLength, size_t* pcbyteWritten, const void* uncompressedData, size_t uncompressedLength, struct JlsParameters* pparams)
{
	JlsParameters info = *pparams;
	if(info.bytesperline == 0)
	{
		info.bytesperline = info.width * ((info.bitspersample + 7)/8);
		if (info.ilv != ILV_NONE)
		{
			info.bytesperline *= info.components;
		}
	}

	JLS_ERROR parameterError = CheckInput(compressedData, compressedLength, uncompressedData, uncompressedLength, &info);

	if (parameterError != OK)
		return parameterError;

	if (pcbyteWritten == NULL)
		return InvalidJlsParameters;

	Size size = Size(info.width, info.height);
	JLSOutputStream stream;

	stream.Init(size, info.bitspersample, info.components);

	if (info.colorTransform != 0)
	{
		stream.AddColorTransform(info.colorTransform);
	}

	if (info.ilv == ILV_NONE)
	{
		LONG cbyteComp = size.cx*size.cy*((info.bitspersample +7)/8);
		for (LONG component = 0; component < info.components; ++component)
		{
			const BYTE* compareData = static_cast<const BYTE*>(uncompressedData) + component*cbyteComp;
			stream.AddScan(compareData, &info);
		}
	}
	else
	{
		stream.AddScan(uncompressedData, &info);
	}


	stream.Write((BYTE*)compressedData, compressedLength);

	*pcbyteWritten = stream.GetBytesWritten();
	return OK;
}

CHARLS_IMEXPORT(JLS_ERROR) JpegLsDecode(void* uncompressedData, size_t uncompressedLength, const void* compressedData, size_t compressedLength, JlsParameters* info)
{
	JLSInputStream reader((BYTE*)compressedData, compressedLength);

	if(info != NULL)
	{
	 	reader.SetInfo(info);
	}

	try
	{
		reader.Read(uncompressedData, uncompressedLength);
		return OK;
	}
	catch (JlsException& e)
	{
		return e._error;
	}
}


CHARLS_IMEXPORT(JLS_ERROR) JpegLsDecodeRect(void* uncompressedData, size_t uncompressedLength, const void* compressedData, size_t compressedLength, JlsRect roi, JlsParameters* info)
{
	JLSInputStream reader((BYTE*)compressedData, compressedLength);

	if(info != NULL)
	{
	 	reader.SetInfo(info);
	}

	reader.SetRect(roi);

	try
	{
		reader.Read(uncompressedData, uncompressedLength);
		return OK;
	}
	catch (JlsException& e)
	{
		return e._error;
	}
}


CHARLS_IMEXPORT(JLS_ERROR) JpegLsVerifyEncode(const void* uncompressedData, size_t uncompressedLength, const void* compressedData, size_t compressedLength)
{
	JlsParameters info = JlsParameters();

	JLS_ERROR error = JpegLsReadHeader(compressedData, compressedLength, &info);
	if (error != OK)
		return error;

	error = CheckInput(compressedData, compressedLength, uncompressedData, uncompressedLength, &info);

	if (error != OK)
		return error;

	Size size = Size(info.width, info.height);

	JLSOutputStream stream;

	stream.Init(size, info.bitspersample, info.components);

	if (info.ilv == ILV_NONE)
	{
		LONG cbyteComp = size.cx*size.cy*((info.bitspersample +7)/8);
		for (LONG component = 0; component < info.components; ++component)
		{
			const BYTE* compareData = static_cast<const BYTE*>(uncompressedData) + component*cbyteComp;
			stream.AddScan(compareData, &info);
		}
	}
	else
	{
		stream.AddScan(uncompressedData, &info);
	}

	std::vector<BYTE> rgbyteCompressed(compressedLength + 16);

	memcpy(&rgbyteCompressed[0], compressedData, compressedLength);

	stream.EnableCompare(true);
	stream.Write(&rgbyteCompressed[0], compressedLength);

	return OK;
}


CHARLS_IMEXPORT(JLS_ERROR) JpegLsReadHeader(const void* compressedData, size_t compressedLength, JlsParameters* pparams)
{
	try
	{
		JLSInputStream reader((BYTE*)compressedData, compressedLength);
		reader.ReadHeader();
		JlsParameters info = reader.GetMetadata();
		*pparams = info;
		return OK;
	}
	catch (JlsException& e)
	{
		return e._error;
	}

}

}
