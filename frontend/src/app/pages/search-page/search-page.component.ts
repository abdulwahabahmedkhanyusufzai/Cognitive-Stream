import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { NavbarComponent } from '../../components/navbar/navbar.component';
import { ContentService } from '../../services/content.service';
import { LucideAngularModule, Search, LUCIDE_ICONS, LucideIconProvider } from 'lucide-angular';
import { ORIGINAL_IMG_BASE_URL } from '../../constants';

@Component({
    selector: 'app-search-page',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive, NavbarComponent, LucideAngularModule],
    providers: [{ provide: LUCIDE_ICONS, useValue: new LucideIconProvider({ Search }) }],
    templateUrl: './search-page.component.html'
})
export class SearchPageComponent {
    activeTab = signal<'movie' | 'tv' | 'person'>('movie');
    searchTerm = signal('');
    results = signal<any[]>([]);

    contentService = inject(ContentService);
    http = inject(HttpClient);
    toastr = inject(ToastrService);

    ORIGINAL_IMG_BASE_URL = ORIGINAL_IMG_BASE_URL;

    handleTabClick(tab: 'movie' | 'tv' | 'person') {
        this.activeTab.set(tab);
        if (tab === 'movie' || tab === 'tv') {
            this.contentService.setContentType(tab);
        }
        this.results.set([]);
    }

    handleSearch(event: Event) {
        event.preventDefault();
        this.http.get<{ content: any[] }>(`/api/v1/search/${this.activeTab()}/${this.searchTerm()}`)
            .subscribe({
                next: (res: { content: any[] }) => {
                    this.results.set(res.content);
                },
                error: (error: HttpErrorResponse) => {
                    if (error.status === 404) {
                        this.toastr.error('Nothing found, make sure you are searching under the right category');
                    } else {
                        this.toastr.error('An error occurred, please try again later');
                    }
                }
            });
    }
}
