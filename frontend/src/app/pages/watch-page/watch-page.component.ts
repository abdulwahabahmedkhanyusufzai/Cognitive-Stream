import { Component, ElementRef, OnInit, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink, RouterLinkActive } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { NavbarComponent } from '../../components/navbar/navbar.component';
import { VideoPlayerComponent } from '../../components/video-player/video-player.component';
import { ContentService } from '../../services/content.service';
import { LucideAngularModule, ChevronLeft, ChevronRight, AlertCircle, LUCIDE_ICONS, LucideIconProvider } from 'lucide-angular';
import { ORIGINAL_IMG_BASE_URL, SMALL_IMG_BASE_URL } from '../../constants';

@Component({
    selector: 'app-watch-page',
    standalone: true,
    imports: [CommonModule, RouterLink, RouterLinkActive, NavbarComponent, LucideAngularModule, VideoPlayerComponent],
    providers: [{ provide: LUCIDE_ICONS, useValue: new LucideIconProvider({ ChevronLeft, ChevronRight, AlertCircle }) }],
    templateUrl: './watch-page.component.html'
})
export class WatchPageComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private http = inject(HttpClient);
    public contentService = inject(ContentService);
    private sanitizer = inject(DomSanitizer);

    @ViewChild('sliderRef') sliderRef!: ElementRef<HTMLDivElement>;

    id = signal<string | null>(null);
    trailers = signal<any[]>([]);
    currentTrailerIdx = signal(0);
    loading = signal(true);
    content = signal<any>(null);
    similarContent = signal<any[]>([]);
    streamUrl = signal<string | null>(null);
    useTrailer = signal(false);

    ORIGINAL_IMG_BASE_URL = ORIGINAL_IMG_BASE_URL;
    SMALL_IMG_BASE_URL = SMALL_IMG_BASE_URL;

    ngOnInit() {
        this.route.paramMap.subscribe((params: any) => {
            const id = params.get('id');
            this.id.set(id);
            if (id) {
                this.loadData(id);
            }
        });
    }

    loadData(id: string) {
        const type = this.contentService.contentType();
        this.loading.set(true);

        // Trailers
        this.http.get<{ trailers: any[] }>(`/api/v1/${type}/${id}/trailers`)
            .subscribe({
                next: (res: { trailers: any[] }) => this.trailers.set(res.trailers),
                error: () => this.trailers.set([])
            });

        // Similar
        this.http.get<{ similar: any[] }>(`/api/v1/${type}/${id}/similar`)
            .subscribe({
                next: (res: { similar: any[] }) => this.similarContent.set(res.similar),
                error: () => this.similarContent.set([])
            });

        // Details
        this.http.get<{ content: any }>(`/api/v1/${type}/${id}/details`)
            .subscribe({
                next: (res: { content: any }) => {
                    this.content.set(res.content);
                    this.loading.set(false);
                },
                error: (err: Error) => {
                    console.error("Failed to fetch details", err);
                    this.content.set(null);
                    this.loading.set(false);
                }
            });

        // HLS Stream from CDN
        this.http.get<{ url: string }>(`/api/v1/stream/${type}/${id}`)
            .subscribe({
                next: (res: { url: string }) => this.streamUrl.set(res.url),
                error: () => this.streamUrl.set(null)
            });
    }

    toggleSource() {
        this.useTrailer.update((v: boolean) => !v);
    }

    getSafeUrl(key: string): SafeResourceUrl {
        return this.sanitizer.bypassSecurityTrustResourceUrl(`https://www.youtube.com/embed/${key}?autoplay=0`);
    }

    handleNext() {
        if (this.currentTrailerIdx() < this.trailers().length - 1) {
            this.currentTrailerIdx.update((v: number) => v + 1);
        }
    }

    handlePrev() {
        if (this.currentTrailerIdx() > 0) {
            this.currentTrailerIdx.update((v: number) => v - 1);
        }
    }

    scrollLeft() {
        if (this.sliderRef?.nativeElement) {
            this.sliderRef.nativeElement.scrollBy({ left: -this.sliderRef.nativeElement.offsetWidth, behavior: "smooth" });
        }
    }

    scrollRight() {
        if (this.sliderRef?.nativeElement) {
            this.sliderRef.nativeElement.scrollBy({ left: this.sliderRef.nativeElement.offsetWidth, behavior: "smooth" });
        }
    }
}
