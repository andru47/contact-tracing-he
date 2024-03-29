cmake_minimum_required(VERSION 3.4.1)
project(LibLattigo)

set(TARGET liblattigoserver_go)

set(SRCS main.go)
set(LIB liblattigo.so)

add_custom_command(OUTPUT ${CMAKE_CURRENT_BINARY_DIR}/${LIB}
  DEPENDS ${SRCS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
  COMMAND env GOPATH=/Users/andru/go go build -buildmode=c-archive
  -o "${CMAKE_CURRENT_BINARY_DIR}/${LIB}"
  ${CMAKE_GO_FLAGS} ./...
  COMMENT "Building Go library")

add_custom_target(${TARGET} DEPENDS ${LIB} ${HEADER})
add_library(liblattigoserver STATIC IMPORTED GLOBAL)
add_dependencies(liblattigoserver ${TARGET})
set_target_properties(liblattigoserver
  PROPERTIES
  IMPORTED_LOCATION ${CMAKE_CURRENT_BINARY_DIR}/${LIB}
  INTERFACE_INCLUDE_DIRECTORIES ${CMAKE_CURRENT_BINARY_DIR})
