#!/bin/bash
source ~/tensorflow/bin/activate
#source activate tensorflow

cd ../../../../tensorflow-for-poets-2
python -m scripts.label_image \
    --graph=tf_files/retrained_graph.pb  \
    --image=$1 > temp.txt
