import { Component, ElementRef, Input, OnChanges, OnDestroy, ViewChild, AfterViewInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import Hls from 'hls.js';
import { LucideAngularModule, Play, Pause, Volume2, VolumeX, Maximize, Settings, SkipForward, SkipBack } from 'lucide-angular';

@Component({
    selector: 'app-video-player',
    standalone: true,
    imports: [CommonModule, LucideAngularModule],
    template: `
    <div class="relative group aspect-video bg-black rounded-[32px] overflow-hidden border border-white/5 shadow-2xl" #containerRef>
      <video
        #videoRef
        (play)="isPlaying.set(true)"
        (pause)="isPlaying.set(false)"
        (waiting)="isBuffering.set(true)"
        (playing)="isBuffering.set(false)"
        class="w-full h-full object-contain cursor-pointer"
        (click)="togglePlay()"
        (timeupdate)="onTimeUpdate()"
        (loadedmetadata)="onMetadataLoaded()"
      ></video>

      <!-- Custom Controls Overlay -->
      <div 
        class="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent transition-opacity duration-300 flex flex-col justify-end p-8"
        [class.opacity-0]="!showControls && isPlaying()"
        [class.opacity-100]="showControls || !isPlaying()"
        (mouseenter)="showControls = true"
        (mouseleave)="showControls = false"
      >
        <!-- Progress Bar -->
        <div class="relative w-full h-1.5 bg-white/10 rounded-full mb-8 cursor-pointer overflow-hidden group/progress" (click)="seek($event)">
          <div 
            class="absolute top-0 left-0 h-full bg-nebula-violet shadow-[0_0_12px_rgba(124,58,237,0.5)] transition-all duration-100"
            [style.width.%]="progress()"
          ></div>
          <div 
            class="absolute h-3 w-3 bg-white rounded-full -top-1 shadow-lg opacity-0 group-hover/progress:opacity-100 transition-opacity"
            [style.left.%]="progress()"
          ></div>
        </div>

        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-6">
            <button (click)="togglePlay()" class="hover:scale-110 transition-transform">
              <lucide-icon [name]="isPlaying() ? 'pause' : 'play'" class="text-white size-6 fill-white" />
            </button>
            <div class="flex items-center space-x-4 group/volume">
              <button (click)="toggleMute()">
                <lucide-icon [name]="isMuted() ? 'volume-x' : 'volume-2'" class="text-white size-5" />
              </button>
              <input 
                type="range" 
                min="0" max="1" step="0.1" 
                [value]="volume()"
                (input)="onVolumeChange($event)"
                class="w-0 group-hover/volume:w-20 transition-all duration-300 accent-nebula-violet h-1"
              />
            </div>
            <span class="text-[10px] font-black tracking-widest text-gray-400 tabular-nums">
              {{ currentTime() }} <span class="mx-2 opacity-30">/</span> {{ duration() }}
            </span>
          </div>

          <div class="flex items-center space-x-6">
            <div class="relative group/settings">
              <button class="hover:rotate-45 transition-transform">
                <lucide-icon name="settings" class="text-white size-5" />
              </button>
              <!-- Quality Menu -->
              <div class="absolute bottom-full right-0 mb-4 bg-black/90 backdrop-blur-xl border border-white/10 rounded-2xl p-4 min-w-[160px] opacity-0 group-hover/settings:opacity-100 pointer-events-none group-hover/settings:pointer-events-auto transition-all translate-y-2 group-hover/settings:translate-y-0 shadow-2xl">
                <p class="text-[9px] font-black uppercase tracking-widest text-gray-500 mb-4 px-2">Adaptive Stream</p>
                <div class="space-y-1">
                  <button 
                    *ngFor="let level of levels(); let i = index"
                    (click)="setQuality(i)"
                    class="w-full text-left px-3 py-2 rounded-xl text-[10px] font-bold uppercase tracking-wider hover:bg-white/10 transition-colors flex items-center justify-between"
                    [class.text-nebula-violet]="currentLevel() === i"
                  >
                    <span>{{ level }}P</span>
                    <span *ngIf="currentLevel() === i" class="w-1 h-1 bg-current rounded-full"></span>
                  </button>
                  <button 
                    (click)="setQuality(-1)"
                    class="w-full text-left px-3 py-2 rounded-xl text-[10px] font-bold uppercase tracking-wider hover:bg-white/10 transition-colors flex items-center justify-between"
                    [class.text-nebula-violet]="currentLevel() === -1"
                  >
                    <span>Auto</span>
                    <span *ngIf="currentLevel() === -1" class="w-1 h-1 bg-current rounded-full"></span>
                  </button>
                </div>
              </div>
            </div>
            <button (click)="toggleFullscreen()">
              <lucide-icon name="maximize" class="text-white size-5" />
            </button>
          </div>
        </div>
      </div>

      <!-- Buffering/Loading State -->
      <div *ngIf="isBuffering()" class="absolute inset-0 flex items-center justify-center bg-black/20 backdrop-blur-[2px]">
        <div class="w-12 h-12 border-2 border-white/5 border-t-nebula-violet rounded-full animate-spin"></div>
      </div>
    </div>
  `,
    styles: [`
    :host { display: block; }
    input[type='range'] {
      @apply bg-white/20 rounded-full cursor-pointer;
    }
    input[type='range']::-webkit-slider-thumb {
      @apply appearance-none w-3 h-3 bg-white rounded-full border-2 border-nebula-violet shadow-lg;
    }
  `]
})
export class VideoPlayerComponent implements OnChanges, OnDestroy, AfterViewInit {
    @Input({ required: true }) src: string = '';
    @ViewChild('videoRef') videoRef!: ElementRef<HTMLVideoElement>;
    @ViewChild('containerRef') containerRef!: ElementRef<HTMLDivElement>;
    
    
    private hls?: Hls;

    // State Signals
    isPlaying = signal(false);
    isMuted = signal(false);
    volume = signal(1);
    progress = signal(0);
    duration = signal('00:00');
    currentTime = signal('00:00');
    isBuffering = signal(false);
    levels = signal<string[]>([]);
    currentLevel = signal(-1);
    showControls = false;

    ngAfterViewInit() {
        this.initPlayer();
    }

    ngOnChanges() {
        if (this.videoRef?.nativeElement) {
            this.initPlayer();
        }
    }

    ngOnDestroy() {
        this.destroyHls();
    }

    private initPlayer() {
        this.destroyHls();
        const video = this.videoRef.nativeElement;

        if (Hls.isSupported()) {
            this.hls = new Hls({
                capLevelToPlayerSize: true,
                autoStartLoad: true
            });
            this.hls.loadSource(this.src);
            this.hls.attachMedia(video);

            this.hls.on(Hls.Events.MANIFEST_PARSED, (_: any, data: { levels: any[] }) => {
                this.levels.set(data.levels.map((l: any) => l.height.toString()));
            });

            this.hls.on(Hls.Events.LEVEL_SWITCHED, (_: any, data: { level: number }) => {
                this.currentLevel.set(this.hls?.autoLevelEnabled ? -1 : data.level);
            });

        } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
            video.src = this.src;
        }
    }

    private destroyHls() {
        if (this.hls) {
            this.hls.destroy();
            this.hls = undefined;
        }
    }

    togglePlay() {
        const video = this.videoRef.nativeElement;
        video.paused ? video.play() : video.pause();
    }

    toggleMute() {
        const video = this.videoRef.nativeElement;
        video.muted = !video.muted;
        this.isMuted.set(video.muted);
    }

    onVolumeChange(event: any) {
        const val = event.target.value;
        this.volume.set(val);
        this.videoRef.nativeElement.volume = val;
        this.isMuted.set(val === 0);
    }

    onTimeUpdate() {
        const video = this.videoRef.nativeElement;
        this.progress.set((video.currentTime / video.duration) * 100);
        this.currentTime.set(this.formatTime(video.currentTime));
        this.isBuffering.set(video.readyState < 3);
    }

    onMetadataLoaded() {
        this.duration.set(this.formatTime(this.videoRef.nativeElement.duration));
    }

    seek(event: MouseEvent) {
        const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
        const pos = (event.clientX - rect.left) / rect.width;
        this.videoRef.nativeElement.currentTime = pos * this.videoRef.nativeElement.duration;
    }

    setQuality(index: number) {
        if (this.hls) {
            this.hls.currentLevel = index;
            this.currentLevel.set(index);
        }
    }

    toggleFullscreen() {
        const el = this.containerRef.nativeElement as any;
        if (document.fullscreenElement) {
            document.exitFullscreen();
        } else {
            if (el.requestFullscreen) el.requestFullscreen();
            else if (el.webkitRequestFullscreen) el.webkitRequestFullscreen();
        }
    }

    private formatTime(time: number): string {
        const mins = Math.floor(time / 60);
        const secs = Math.floor(time % 60);
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }
}
