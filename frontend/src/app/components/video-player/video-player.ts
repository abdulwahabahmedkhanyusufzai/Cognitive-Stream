import { Component, ElementRef, Input, OnChanges, OnDestroy, SimpleChanges, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import Hls from 'hls.js'; // You will need to run: npm install hls.js

@Component({
  selector: 'app-video-player',
  standalone: true, // Fixes your 'TS-992010' build error
  imports: [CommonModule],
  template: `
    <div class="video-container">
      <video #videoElement 
             controls 
             class="netflix-style-player"
             poster="assets/images/loading-spinner.gif">
      </video>
    </div>
  `,
  styles: [`
    .video-container { width: 100%; background: #000; border-radius: 8px; overflow: hidden; }
    .netflix-style-player { width: 100%; height: auto; outline: none; }
  `]
})
export class VideoPlayer implements AfterViewInit, OnChanges, OnDestroy {
  @ViewChild('videoElement') videoElement!: ElementRef<HTMLVideoElement>;
  
  // This input matches the "url" key from your Java VideoController response
  @Input() streamUrl: string = ''; 
  
  private hls?: Hls;

  ngAfterViewInit() {
    this.loadStream();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['streamUrl'] && !changes['streamUrl'].firstChange) {
      this.loadStream();
    }
  }

  private loadStream() {
    if (!this.streamUrl) return;

    const video = this.videoElement.nativeElement;

    // 1. Check for Native HLS support (Safari/iOS)
    if (video.canPlayType('application/vnd.apple.mpegurl')) {
      video.src = this.streamUrl;
    } 
    // 2. Use HLS.js for Chrome/Firefox/Edge (Standard MAANG approach)
    else if (Hls.isSupported()) {
      if (this.hls) {
        this.hls.destroy();
      }

      this.hls = new Hls({
        capLevelToPlayerSize: true, // Auto-resolution based on player size
        debug: false
      });

      this.hls.loadSource(this.streamUrl);
      this.hls.attachMedia(video);
      
      this.hls.on(Hls.Events.MANIFEST_PARSED, () => {
        // video.play(); // Optional: Auto-play
      });
    }
  }

  ngOnDestroy() {
    if (this.hls) {
      this.hls.destroy();
    }
  }
}