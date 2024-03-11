cmake_minimum_required(VERSION 3.8)

project(mylibrary)

find_package(JNI REQUIRED)

add_library(mylibrary SHARED libmain.cpp)
target_compile_features(mylibrary PUBLIC cxx_std_11)
target_link_libraries(mylibrary ${JNI_LIBRARIES})
target_include_directories(mylibrary PRIVATE ${JNI_INCLUDE_DIRS})
