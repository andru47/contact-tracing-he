#!/bin/sh

cd $HOME
git clone https://github.com/microsoft/SEAL.git
cd SEAL/
cmake -S . -B build/ -DSEAL_USE_MSGSL=OFF -DSEAL_USE_ZLIB=OFF -DSEAL_USE_ZSTD=OFF
cmake --build build
sudo cmake --install build
