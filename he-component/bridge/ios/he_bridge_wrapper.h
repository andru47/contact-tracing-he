//
//  he_bridge_wrapper.h
//  Runner
//
//  Created by Andru Stefanescu on 17.10.2021.
//

#import <Foundation/Foundation.h>

@interface HeBridgeWrapper : NSObject
- (NSString *) hello: (NSString*) name;
- (NSArray<NSString *> *) encrypt: (double) latitudeCos latSin: (double) latitudeSin longCos: (double) longitudeCos longSin: (double) longitudeSin alt: (double) altitude pubKey: (NSString* ) publicKey;
- (double) decrypt: (NSString*) givenCiphertext privateKey: (NSString*) givenPrivateKey;
@end
