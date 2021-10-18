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
    HeBridge helloWorld;
    string myString = string([name UTF8String]);
    string helloWorldMessage = helloWorld.hello(myString);
    return [NSString
            stringWithCString:helloWorldMessage.c_str()
            encoding:NSUTF8StringEncoding];
}
@end
