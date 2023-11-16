# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

# Declares and names the project.
project("diag-lib")
project("gui-lib")

#收集c/c++文件并赋值给变量SRC_LIST_CPP  ${PROJECT_SOURCE_DIR}代表区当前项目录
file(GLOB SRC_LIST_CPP ${PROJECT_SOURCE_DIR}/*.cpp)
file(GLOB SRC_LIST_C ${PROJECT_SOURCE_DIR}/*.c)
#指定头文件目录
include_directories(${PROJECT_SOURCE_DIR}/include)

#指定诊断的cpp与头文件
file(GLOB SRC_LIST_CPP ${PROJECT_SOURCE_DIR}/*.cpp)
include_directories(${PROJECT_SOURCE_DIR}/include)
#include_directories(${PROJECT_SOURCE_DIR}/src/main/inc)


SET(LIBRARY_OUTPUT_PATH ${PROJECT_SOURCE_DIR}/../../../../lib_common/libs/armeabi-v7a)
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../../../../lib_common/libs/armeabi-v7a)

#SET(LIBRARY_OUTPUT_PATH ${PROJECT_SOURCE_DIR}/libs/armeabi-v7a)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
ADD_LIBRARY(diag-lib SHARED ${SRC_LIST_CPP} )
#ADD_LIBRARY(gui-lib SHARED IMPORTED)
#ADD_LIBRARY(commom-lib SHARED ${PROJECT_SOURCE_DIR}/main.cpp)

add_library(gui-lib STATIC ${SRC_LIST_CPP} ${SRC_LIST_C})

set_target_properties(gui-lib  PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/../../../../lib_common/libs/${ANDROID_ABI}/libgui-lib.a)


#set_target_properties(diag-lib	PROPERTIES
#        LIBRARY_OUTPUT_DIRECTORY
#        "${CMAKE_CURRENT_SOURCE_DIR}")

set_target_properties(diag-lib	PROPERTIES
        LIBRARY_OUTPUT_DIRECTORY
        "${PROJECT_SOURCE_DIR}/../../../../lib_common/libs/armeabi-v7a")

#add_library( # Sets the name of the library.
#        gui-lib
#
#        # Sets the library as a shared library.
#        SHARED
#
#        # Provides a relative path to your source file(s).
#        gui-lib.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)
#target_include_directories(
#        gui-lib # 库名
#        PRIVATE # 库属性
#        #        src/main/cpp # 头文件目录
#        src/main/cpp/include
#)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        diag-lib
        gui-lib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})