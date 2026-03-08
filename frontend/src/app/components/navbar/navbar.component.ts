import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { LucideAngularModule, LogOut, Menu, Search, LUCIDE_ICONS, LucideIconProvider } from 'lucide-angular';
import { AuthService } from '../../services/auth.service';
import { ContentService } from '../../services/content.service';

@Component({
    selector: 'app-navbar',
    standalone: true,
    imports: [CommonModule, RouterLink, RouterLinkActive, LucideAngularModule],
    providers: [{ provide: LUCIDE_ICONS, useValue: new LucideIconProvider({ Search, LogOut, Menu }) }],
    templateUrl: './navbar.component.html'
})
export class NavbarComponent {
    authService = inject(AuthService);
    contentService = inject(ContentService);
    isMobileMenuOpen = signal(false);

    readonly Icons = { LogOut, Menu, Search };

    toggleMobileMenu() {
        this.isMobileMenuOpen.update(v => !v);
    }

    logout() {
        this.authService.logout().subscribe();
    }

    setContentType(type: string) {
        this.contentService.setContentType(type);
        this.isMobileMenuOpen.set(false);
    }
}
