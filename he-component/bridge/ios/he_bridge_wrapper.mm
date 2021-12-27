//
//  he_bridge_wrapper.m
//  Runner
//
//  Created by Andru Stefanescu on 17.10.2021.
//

#import "he_bridge_wrapper.h"
#import "he_bridge.hpp"

@implementation HeBridgeWrapper
- (NSString *) hello: (NSString *) name {
    string myString = string([name UTF8String]);
    static HeBridge bridge;
    string helloWorldMessage = bridge.hello(myString);
    return [NSString
            stringWithCString:helloWorldMessage.c_str()
            encoding:NSUTF8StringEncoding];
}

- (NSArray<NSString *> *)encrypt:(double)latitudeCos latSin:(double)latitudeSin longCos:(double)longitudeCos longSin:(double)longitudeSin alt:(double)altitude pubKey:(NSString*)publicKey {
    
    static HeBridge bridge;
    
    string publicKeyString = [self getStringFromNSString:publicKey];
    bridge.setPublic(publicKeyString);
    vector<string> resultedCiphertexts = bridge.encrypt(latitudeCos, latitudeSin, longitudeCos, longitudeSin, altitude);
    NSMutableArray<NSString*>* resultingArray = [[NSMutableArray alloc] init];
    for (auto &currentCipher: resultedCiphertexts) {
        [resultingArray addObject:[self getNSStringFromString:currentCipher]];
    }
    
    return resultingArray;
}

- (double)decrypt:(NSString *)givenCiphertext privateKey:(NSString *)givenPrivateKey {
    string ciphertextString = [self getStringFromNSString:givenCiphertext];
    string privateKeyString = [self getStringFromNSString:givenPrivateKey];
    static HeBridge bridge;
    bridge.setPrivate(privateKeyString);
    
    return bridge.decrypt(ciphertextString);
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

@end
