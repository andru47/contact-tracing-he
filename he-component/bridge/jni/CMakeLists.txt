cmake_minimum_required(VERSION 3.10)
project(HE-Lib-Java)

set(CMAKE_CXX_STANDARD 17)

set(SEAL_IMPL_DIR "../../src/server/seal")
set(BASE_IMPL_DIR "../../src/server")
set(UTIL_DIR "../../src/util/")

find_package(JNI REQUIRED)
find_package(SEAL REQUIRED)

include_directories(${JNI_INCLUDE_DIRS} ${SEAL_INCLUDE_DIRS} ${SEAL_IMPL_DIR} ${UTIL_DIR} ${BASE_IMPL_DIR} ${BASE_IMPL_DIR}/base)

add_library(HELib SHARED ./dissertation_backend_JNIBridge.cpp ${SEAL_IMPL_DIR}/seal_he_server.cpp ${BASE_IMPL_DIR}/server_selector.cpp ${BASE_IMPL_DIR}/base/server_base.cpp ${UTIL_DIR}/util.cpp)
target_link_libraries(HELib SEAL::seal)
