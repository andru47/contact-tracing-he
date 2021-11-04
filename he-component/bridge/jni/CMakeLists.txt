cmake_minimum_required(VERSION 3.10)
project(HE-Lib-Java)

set(CMAKE_CXX_STANDARD 17)

set(IMPL_DIR "../../src/server/")
set(UTIL_DIR "../../src/util/")

find_package(JNI REQUIRED)
find_package(SEAL REQUIRED)

include_directories(${JNI_INCLUDE_DIRS} ${SEAL_INCLUDE_DIRS} ${IMPL_DIR} ${UTIL_DIR})

add_library(HELib SHARED ./dissertation_backend_JNIBridge.cpp ${IMPL_DIR}/ckks_he_server.cpp ${UTIL_DIR}/util.cpp)
target_link_libraries(HELib SEAL::seal)
