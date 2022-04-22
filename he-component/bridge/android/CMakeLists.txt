cmake_minimum_required(VERSION 3.4.1)

project(HELib-Android)

set(SEAL_SRC_DIR "$ENV{HOME}/SEAL")
set(SEAL_BUILD_DIR "$ENV{HOME}/SEAL/android/app/.cxx/cmake/release/x86_64")
set(SEAL_IMPL_DIR "../../src/client/seal")
set(SMKHE_BUILD_DIR "$ENV{HOME}/Desktop/smkhe/build-android")
set(PROTOBUF_BUILD_DIR "$ENV{HOME}/Desktop")
set(SMKHE_INCLUDE "$ENV{HOME}/Desktop/smkhe/include")
set(BASE_CLIENT_DIR "../../src/client")
set(UTIL_DIR "../../src/util")
set(LATTIGO_IMPL_DIR "../../src/client/lattigo")
set(SMKHE_IMPL_DIR "../../src/client/smkhe")
file(MAKE_DIRECTORY ${SEAL_BUILD_DIR})

add_library( jni-android-he
             SHARED
             com_example_phone_app_JNIBridge.cpp ${SEAL_IMPL_DIR}/seal_he_client.cpp ${UTIL_DIR}/util.cpp
            ${BASE_CLIENT_DIR}/client_selector.cpp ${BASE_CLIENT_DIR}/base/client_base.cpp
            ${LATTIGO_IMPL_DIR}/cpp/lattigo_he_client.cpp ${SMKHE_IMPL_DIR}/smkhe_he_client.cpp)

add_library(lib_seal STATIC IMPORTED)
set_target_properties(lib_seal PROPERTIES IMPORTED_LOCATION ${SEAL_BUILD_DIR}/lib/x86_64/libsealc.so)
find_library(log-lib log)
add_library(lib_smkhe STATIC IMPORTED)
set_target_properties(lib_smkhe PROPERTIES IMPORTED_LOCATION ${SMKHE_BUILD_DIR}/libsmkhe.a)
include_directories(${SMKHE_INCLUDE})

add_library(lib_protobuf STATIC IMPORTED)
set_target_properties(lib_protobuf PROPERTIES IMPORTED_LOCATION ${PROTOBUF_BUILD_DIR}/libprotobuf-android-3.a)

add_library(lib_lattigo SHARED IMPORTED)
set_property(TARGET lib_lattigo PROPERTY IMPORTED_NO_SONAME 1)
set_target_properties(lib_lattigo PROPERTIES IMPORTED_LOCATION ../../../../src/main/jniLibs/${CMAKE_ANDROID_ARCH_ABI}/lib_lattigo.so)
include_directories(${LATTIGO_IMPL_DIR}/cpp ../../../phone-app/android/app/src/main/jniLibs/${CMAKE_ANDROID_ARCH_ABI})

include_directories(${SEAL_SRC_DIR}/native/src ${SEAL_BUILD_DIR}/native/src ${SEAL_IMPL_DIR} ${UTIL_DIR} ${SMKHE_IMPL_DIR})
include_directories(${BASE_CLIENT_DIR} ${BASE_CLIENT_DIR}/base)

target_link_libraries(jni-android-he ${log-lib} lib_seal lib_lattigo lib_smkhe lib_protobuf)
