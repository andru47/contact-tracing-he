//
//  he_bridge_wrapper.m
//  Runner
//
//  Created by Andru Stefanescu on 17.10.2021.
//

#import "he_bridge_wrapper.h"
#import "he_bridge.hpp"

ClientHelper* helper = nil;
HeBridge* bridge = nil;
bool init = false;
mutex helperInitMtx;

@implementation HeBridgeWrapper
- (NSString *) hello: (NSString *) name {
    string myString = string([name UTF8String]);
    static ClientHelper* helper = getHelper();
    static HeBridge bridge(&helper);
    string helloWorldMessage = bridge.hello(myString);
    return [NSString
            stringWithCString:helloWorldMessage.c_str()
            encoding:NSUTF8StringEncoding];
}

- (NSArray<NSString *> *)encrypt:(double)latitudeCos latSin:(double)latitudeSin longCos:(double)longitudeCos longSin:(double)longitudeSin alt:(double)altitude pubKey:(NSString*)publicKey {
    helperInitMtx.lock();
    if (!init) {
        init = true;
        helper = getHelper();
        bridge = new HeBridge(&helper);
    }
    helperInitMtx.unlock();
    
    string publicKeyString = [self getStringFromNSString:publicKey];
    bridge -> setPublic(publicKeyString);
    vector<string> resultedCiphertexts = bridge -> encrypt(latitudeCos, latitudeSin, longitudeCos, longitudeSin, altitude);
    NSMutableArray<NSString*>* resultingArray = [[NSMutableArray alloc] init];
    for (auto &currentCipher: resultedCiphertexts) {
        [resultingArray addObject:[self getNSStringFromString:currentCipher]];
    }
    
    return resultingArray;
}

- (double)decrypt:(NSString *)givenCiphertext privateKey:(NSString *)givenPrivateKey {
    string ciphertextString = [self getStringFromNSString:givenCiphertext];
    string privateKeyString = [self getStringFromNSString:givenPrivateKey];
    helperInitMtx.lock();
    if (!init) {
        init = true;
        helper = getHelper();
        bridge = new HeBridge(&helper);
    }
    helperInitMtx.unlock();
    
    bridge -> setPrivate(privateKeyString);
    
    return bridge -> decrypt(ciphertextString);
}

- (NSString*) getNSStringFromString: (string) givenString {
    size_t sz = givenString.size();
    unichar* pointers = (unichar*) malloc(sizeof(unichar) * (sz + 1));
    for (int i = 0; i < sz; ++i) {
        pointers[i] = unichar((int)givenString[i]);
    }
    NSString* ret = [NSString stringWithCharacters:pointers length:givenString.size()];
    
    free(pointers);
    return ret;
}

- (string) getStringFromNSString: (NSString*) givenNSString {
    NSUInteger len = [givenNSString length];
    
    string ret = "";
    for (int i = 0; i < len; ++i) {
        char chr = char((int) [givenNSString characterAtIndex:i]);
        ret += chr;
    }
    
    return ret;
}

- (NSObject *) decryptMulti: (NSString*) givenCiphertext partialCipher: (NSString*) givenPartialCipher privateKey: (NSString*) givenPrivateKey isFinal: (bool) finalDecryption {
    helperInitMtx.lock();
    if (!init) {
        init = true;
        helper = getHelper();
        bridge = new HeBridge(&helper);
    }
    helperInitMtx.unlock();
    string ciphertextString = [self getStringFromNSString:givenCiphertext];
    string privateKeyString = [self getStringFromNSString:givenPrivateKey];
    string partialString = [self getStringFromNSString:givenPartialCipher];
    
    bridge -> setPrivate(privateKeyString);
    
    MKResult nativeResult = bridge -> decryptMulti(ciphertextString, partialString);
    if (finalDecryption) {
        NSNumber* retValue = @(nativeResult.result);
        return retValue;
    }
    
    NSString* retString = [self getNSStringFromString:nativeResult.halfCipher];
    return retString;
}

- (void) generateKeys {
    helperInitMtx.lock();
    if (!init) {
        init = true;
        helper = getHelper();
        bridge = new HeBridge(&helper);
    }
    helperInitMtx.unlock();
    helper -> generateKeys();
}

- (NSString*) getPublicKey {
    helperInitMtx.lock();
    if (!init) {
        init = true;
        helper = getHelper();
        bridge = new HeBridge(&helper);
    }
    helperInitMtx.unlock();
    
    return [self getNSStringFromString: bridge -> getPublicKey()];
}

- (NSString*) getPrivateKey {
    helperInitMtx.lock();
    if (!init) {
        init = true;
        helper = getHelper();
        bridge = new HeBridge(&helper);
    }
    helperInitMtx.unlock();
    
    return [self getNSStringFromString: bridge -> getPrivateKey()];
}

- (NSString*) getRelinKey {
    helperInitMtx.lock();
    if (!init) {
        init = true;
        helper = getHelper();
        bridge = new HeBridge(&helper);
    }
    helperInitMtx.unlock();
    
    return [self getNSStringFromString: bridge -> getRelinKeys()];
}

- (NSString*) getMKPublicKey {
    helperInitMtx.lock();
    if (!init) {
        init = true;
        helper = getHelper();
        bridge = new HeBridge(&helper);
    }
    helperInitMtx.unlock();
    
    return [self getNSStringFromString: bridge -> getMKPubKey()];
}

@end
