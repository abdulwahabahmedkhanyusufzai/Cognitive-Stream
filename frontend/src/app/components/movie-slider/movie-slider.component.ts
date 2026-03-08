import { Component, ElementRef, Input, ViewChild, inject, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ContentService } from '../../services/content.service';
import { LucideAngularModule, Play, ChevronLeft, ChevronRight, LUCIDE_ICONS, LucideIconProvider } from 'lucide-angular';
import { SMALL_IMG_BASE_URL, ORIGINAL_IMG_BASE_URL } from '../../constants';

@Component({
    selector: 'app-movie-slider',
    standalone: true,
    imports: [CommonModule, RouterLink, RouterLinkActive, LucideAngularModule],
    providers: [{ provide: LUCIDE_ICONS, useValue: new LucideIconProvider({ Play, ChevronLeft, ChevronRight }) }],
    templateUrl: './movie-slider.component.html'
})
export class MovieSliderComponent {
    @Input() category!: string;
    @ViewChild('sliderRef') sliderRef!: ElementRef<HTMLDivElement>;

    contentService = inject(ContentService);
    http = inject(HttpClient);

    content = signal<any[]>([]);
    showArrows = signal(false);

    SMALL_IMG_BASE_URL = SMALL_IMG_BASE_URL;
    ORIGINAL_IMG_BASE_URL = ORIGINAL_IMG_BASE_URL;

    constructor() {
        effect(() => {
            const contentType = this.contentService.contentType();
            if (this.category) {
                this.getContent(contentType, this.category);
            }
        });
    }

    getContent(contentType: string, category: string) {
        this.http.get<{ content: any[] }>(`/api/v1/${contentType}/${category}`)
            .subscribe((res: { content: any[] }) => {
                this.content.set(res.content);
            });
    }

    get categoryName() {
        return this.category.replaceAll("_", " ")[0].toUpperCase() + this.category.replaceAll("_", " ").slice(1);
    }

    scrollLeft() {
        if (this.sliderRef && this.sliderRef.nativeElement) {
            this.sliderRef.nativeElement.scrollBy({ left: -this.sliderRef.nativeElement.offsetWidth, behavior: "smooth" });
        }
    }

    scrollRight() {
        if (this.sliderRef && this.sliderRef.nativeElement) {
            this.sliderRef.nativeElement.scrollBy({ left: this.sliderRef.nativeElement.offsetWidth, behavior: "smooth" });
        }
    }
}
