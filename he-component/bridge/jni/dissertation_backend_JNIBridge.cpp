#include "dissertation_backend_JNIBridge.h"
#include <string>

using namespace std;

string getStringFromJString(JNIEnv *env, jstring givenJString) {
  return string(env->GetStringUTFChars(givenJString, NULL));
}

JNIEXPORT jstring JNICALL Java_dissertation_backend_JNIBridge_getNativeGreetingMessage
  (JNIEnv *env, jobject obj, jstring givenName) {
    string str("Hello, " + getStringFromJString(env, givenName) + " from C++!");
    
    return env -> NewStringUTF(str.c_str());
}
