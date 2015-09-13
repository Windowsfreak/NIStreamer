//
// (C) Jan de Vaan 2007-2010, all rights reserved. See the accompanying "License.txt" for licensed use.
//

#include <jni.h>
#ifndef JLS_INTERFACE
#define JLS_INTERFACE

#include "publictypes.h"

#if defined(_WIN32)
#ifndef CHARLS_IMEXPORT
#define CHARLS_IMEXPORT(returntype) __declspec(dllimport) returntype __stdcall
#endif
#else
#ifndef CHARLS_IMEXPORT
#define CHARLS_IMEXPORT(returntype) returntype
#endif
#endif /* _WIN32 */


#ifdef __cplusplus
extern "C"
{
#endif

  CHARLS_IMEXPORT(enum JLS_ERROR) JpegLsEncode(void* compressedData, size_t compressedLength, size_t* pcbyteWritten,
	    const void* uncompressedData, size_t uncompressedLength, struct JlsParameters* pparams);
/*
 * Class:     de_windowsfreak_testjni_codec_CharLSCodec
 * Method:    hello
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_windowsfreak_testjni_codec_CharLSCodec_hello
  (JNIEnv *, jobject);

/*
 * Class:     de_windowsfreak_testjni_codec_CharLSCodec
 * Method:    encode
 * Signature: (Ljava/nio/ByteBuffer;ILjava/nio/ByteBuffer;IIIIIIIII)I
 */
JNIEXPORT jint JNICALL Java_de_windowsfreak_testjni_codec_CharLSCodec_encode
  (JNIEnv *, jobject, jobject, jint, jobject, jint, jint, jint, jint, jint, jint, jint, jint, jint);

/*
 * Class:     de_windowsfreak_testjni_codec_CharLSCodec
 * Method:    decode
 * Signature: (Ljava/nio/ByteBuffer;ILjava/nio/ByteBuffer;IIIIIIIII)I
 */
JNIEXPORT jint JNICALL Java_de_windowsfreak_testjni_codec_CharLSCodec_decode
  (JNIEnv *, jobject, jobject, jint, jobject, jint, jint, jint, jint, jint, jint, jint, jint, jint);

  CHARLS_IMEXPORT(enum JLS_ERROR) JpegLsDecode(void* uncompressedData, size_t uncompressedLength,
		const void* compressedData, size_t compressedLength,
		struct JlsParameters* info);


  CHARLS_IMEXPORT(enum JLS_ERROR) JpegLsDecodeRect(void* uncompressedData, size_t uncompressedLength,
		const void* compressedData, size_t compressedLength,
		struct JlsRect rect, struct JlsParameters* info);

  CHARLS_IMEXPORT(enum JLS_ERROR) JpegLsReadHeader(const void* compressedData, size_t compressedLength,
		struct JlsParameters* pparams);

  CHARLS_IMEXPORT(enum JLS_ERROR) JpegLsVerifyEncode(const void* uncompressedData, size_t uncompressedLength,
		const void* compressedData, size_t compressedLength);

#ifdef __cplusplus
}
#endif

#endif
