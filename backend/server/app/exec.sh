#!/bin/bash
source ~/tensorflow/bin/activate
cd /home/ana/Documents/PI/Tensorflow/tensorflow-for-poets-2
python -m scripts.label_image \
    --graph=tf_files/retrained_graph.pb  \
    --image=$1 > temp.txt