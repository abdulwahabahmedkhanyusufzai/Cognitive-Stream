#!/bin/bash
# Adaptive Bitrate HLS Transcoding Script for CognitiveStream
# Usage: ./transcode.sh input.mp4 storage/hls-content/movie-123

INPUT=$1
OUTPUT=$2

mkdir -p "$OUTPUT"

ffmpeg -i "$INPUT" \
  -filter_complex "[0:v]split=3[v1,v2,v3]; [v1]scale=w=640:h=360[v1out]; [v2]scale=w=1280:h=720[v2out]; [v3]scale=w=1920:h=1080[v3out]" \
  -map "[v1out]" -c:v:0 libx264 -b:v:0 800k -maxrate:v:0 856k -bufsize:v:0 1200k \
  -map "[v2out]" -c:v:1 libx264 -b:v:1 2800k -maxrate:v:1 2996k -bufsize:v:1 4200k \
  -map "[v3out]" -c:v:2 libx264 -b:v:2 5000k -maxrate:v:2 5350k -bufsize:v:2 7500k \
  -map a:0 -c:a:0 aac -b:a:0 128k -ac 2 \
  -map a:0 -c:a:1 aac -b:a:1 128k -ac 2 \
  -map a:0 -c:a:2 aac -b:a:2 128k -ac 2 \
  -f hls \
  -hls_time 10 \
  -hls_playlist_type vod \
  -hls_flags independent_segments \
  -master_pl_name master.m3u8 \
  -var_stream_map "v:0,a:0 v:1,a:1 v:2,a:2" \
  -hls_segment_filename "$OUTPUT/p%v_%03d.ts" \
  "$OUTPUT/p%v.m3u8"

echo "Transcoding complete. Master manifest at: $OUTPUT/master.m3u8"
