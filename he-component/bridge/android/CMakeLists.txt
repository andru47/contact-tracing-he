cmake_minimum_required(VERSION 3.4.1)

project(HELib-Android)

set(SEAL_SRC_DIR "$ENV{HOME}/SEAL")
set(SEAL_BUILD_DIR "$ENV{HOME}/SEAL/android-build")
set(IMPL_DIR "../../src/client")
set(UTIL_DIR "../../src/util")
file(MAKE_DIRECTORY ${SEAL_BUILD_DIR})

add_library( jni-android-he
             SHARED
             com_example_phone_app_JNIBridge.cpp ${IMPL_DIR}/simple_he.cpp ${UTIL_DIR}/util.cpp)

add_subdirectory(${SEAL_SRC_DIR} ${SEAL_BUILD_DIR})
add_library(lib_seal STATIC IMPORTED)
set_target_properties(lib_seal PROPERTIES IMPORTED_LOCATION ${SEAL_BUILD_DIR}/lib/x86_64/libsealc.so)
include_directories(${SEAL_SRC_DIR}/native/src ${SEAL_BUILD_DIR}/native/src ${IMPL_DIR} ${UTIL_DIR})
target_link_libraries(jni-android-he lib_seal)
