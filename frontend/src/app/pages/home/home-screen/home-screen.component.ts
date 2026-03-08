import { Component, OnInit, inject, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { NavbarComponent } from '../../../components/navbar/navbar.component';
import { MovieSliderComponent } from '../../../components/movie-slider/movie-slider.component';
import { ContentService } from '../../../services/content.service';
import { LucideAngularModule, Play, Info, Home, Compass, Clock, History, LUCIDE_ICONS, LucideIconProvider } from 'lucide-angular';
import { MOVIE_CATEGORIES, TV_CATEGORIES, ORIGINAL_IMG_BASE_URL } from '../../../constants';

@Component({
    selector: 'app-home-screen',
    standalone: true,
    imports: [CommonModule, RouterLink, RouterLinkActive, NavbarComponent, MovieSliderComponent, LucideAngularModule],
    providers: [{ provide: LUCIDE_ICONS, useValue: new LucideIconProvider({ Play, Info, Home, Compass, Clock, History }) }],
    templateUrl: './home-screen.component.html'
})
export class HomeScreenComponent implements OnInit {
    contentService = inject(ContentService);
    private http = inject(HttpClient);

    trendingContent = signal<any>(null);
    imgLoading = signal(true);

    MOVIE_CATEGORIES = MOVIE_CATEGORIES;
    TV_CATEGORIES = TV_CATEGORIES;
    ORIGINAL_IMG_BASE_URL = ORIGINAL_IMG_BASE_URL;

    ngOnInit() {
    }

    constructor() {
        effect(() => {
            const type = this.contentService.contentType();
            this.getTrendingContent(type);
        });
    }

    getTrendingContent(type: string) {
        this.http.get<{ content: any }>(`/api/v1/${type}/trending`)
            .subscribe({
                next: (res: { content: any }) => {
                    this.trendingContent.set(res.content);
                    this.imgLoading.set(true); // Reset loading for new image
                },
                error: (err: Error) => {
                    console.error("Failed to fetch trending content", err);
                    this.trendingContent.set(null); // Clear content or show error state
                }
            });
    }

    setContentType(type: string) {
        this.contentService.setContentType(type);
    }
}
