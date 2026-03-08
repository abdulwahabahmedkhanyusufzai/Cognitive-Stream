import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { NavbarComponent } from '../../components/navbar/navbar.component';
import { LucideAngularModule, Trash, History, LUCIDE_ICONS, LucideIconProvider } from 'lucide-angular';
import { SMALL_IMG_BASE_URL } from '../../constants';

@Component({
    selector: 'app-search-history-page',
    standalone: true,
    imports: [CommonModule, RouterLink, RouterLinkActive, NavbarComponent, LucideAngularModule],
    providers: [{ provide: LUCIDE_ICONS, useValue: new LucideIconProvider({ Trash, History }) }],
    templateUrl: './search-history-page.component.html'
})
export class SearchHistoryPageComponent implements OnInit {
    searchHistory = signal<any[]>([]);
    http = inject(HttpClient);
    toastr = inject(ToastrService);

    SMALL_IMG_BASE_URL = SMALL_IMG_BASE_URL;

    ngOnInit() {
        this.getSearchHistory();
    }

    getSearchHistory() {
        this.http.get<{ content: any[] }>('/api/v1/search/history')
            .subscribe({
                next: (res: { content: any[] }) => this.searchHistory.set(res.content),
                error: () => this.searchHistory.set([])
            });
    }

    handleDelete(entry: any) {
        this.http.delete(`/api/v1/search/history/${entry.id}`)
            .subscribe({
                next: () => {
                    this.searchHistory.update(history => history.filter(item => item.id !== entry.id));
                    this.toastr.success('Search item deleted');
                },
                error: () => this.toastr.error('Failed to delete search item')
            });
    }
}
