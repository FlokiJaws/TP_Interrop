cmake_minimum_required(VERSION 3.8)

project(main)

add_executable(main hello.cpp main.cpp)
target_compile_features(main PUBLIC cxx_std_11)
