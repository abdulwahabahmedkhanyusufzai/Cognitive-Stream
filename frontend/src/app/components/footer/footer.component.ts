import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  template: `
    <footer class="bg-nebula-dark border-t border-white/5 pt-24 pb-12 overflow-hidden relative">
      <!-- Background Bloom -->
      <div class="absolute -bottom-24 -left-24 w-96 h-96 bg-nebula-violet/10 blur-[120px] rounded-full pointer-events-none"></div>

      <div class="max-w-7xl mx-auto px-8 relative z-10">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-16 mb-24">
          <!-- Column 1: Brand -->
          <div class="space-y-8">
            <div class="flex items-center space-x-4">
              <div class="w-10 h-10 bg-gradient-premium rounded-xl flex items-center justify-center shadow-lg shadow-nebula-violet/20">
                <span class="text-white font-black text-lg">C</span>
              </div>
              <div class="flex flex-col">
                <span class="text-white font-black text-sm tracking-widest uppercase font-heading">Cognitive</span>
                <span class="text-nebula-cyan font-bold text-[10px] tracking-[0.3em] uppercase">Stream</span>
              </div>
            </div>
            <p class="text-gray-400 text-sm leading-relaxed font-medium">
              Architecting the next generation of neural content distribution. Synced across all nodes in the decentralized universe.
            </p>
          </div>

          <!-- Column 2: Navigation -->
          <div class="space-y-6">
            <h4 class="text-white text-[11px] font-black uppercase tracking-[0.4em] font-heading">Sectors</h4>
            <nav class="flex flex-col space-y-4 text-[11px] font-bold uppercase tracking-widest text-gray-500">
              <a routerLink="/" class="hover:text-nebula-violet transition-colors">Movies Cluster</a>
              <a routerLink="/" class="hover:text-nebula-violet transition-colors">Universe Nodes</a>
              <a routerLink="/history" class="hover:text-nebula-violet transition-colors">Data Trajectory</a>
              <a routerLink="/search" class="hover:text-nebula-violet transition-colors">Global Search</a>
            </nav>
          </div>

          <!-- Column 3: Platform -->
          <div class="space-y-6">
            <h4 class="text-white text-[11px] font-black uppercase tracking-[0.4em]">Protocol</h4>
            <nav class="flex flex-col space-y-4 text-[11px] font-bold uppercase tracking-widest text-gray-500">
              <span class="cursor-not-allowed opacity-50">API Endpoints</span>
              <span class="cursor-not-allowed opacity-50">Neural Link Docs</span>
              <span class="cursor-not-allowed opacity-50">Node Status</span>
              <span class="cursor-not-allowed opacity-50">Whitepaper</span>
            </nav>
          </div>

          <!-- Column 4: Contact -->
          <div class="space-y-6">
            <h4 class="text-white text-[11px] font-black uppercase tracking-[0.4em]">Uplink</h4>
            <div class="space-y-4">
              <p class="text-gray-500 text-[11px] font-bold uppercase tracking-widest">Global Headquarters</p>
              <p class="text-white text-[11px] font-bold uppercase tracking-widest">sector-7, NEBULA-9</p>
              <div class="pt-4 flex items-center space-x-6 text-gray-500">
                  <span class="hover:text-white transition-colors cursor-pointer">X</span>
                  <span class="hover:text-white transition-colors cursor-pointer">GH</span>
                  <span class="hover:text-white transition-colors cursor-pointer">LD</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Bottom Bar -->
        <div class="pt-12 border-t border-white/5 flex flex-col md:flex-row justify-between items-center gap-6">
          <p class="text-[9px] font-black uppercase tracking-[0.4em] text-gray-600">
            &copy; 2026 Cognitive Stream Protocol. All Rights Synchronized.
          </p>
          
          <p class="text-[9px] font-black uppercase tracking-[0.4em] text-gray-600">
            Engineered by 
            <a href="https://github.com/abdulwahabahmedkhanyusufzai" target="_blank" class="text-white hover:text-nebula-cyan transition-all border-b border-transparent hover:border-nebula-cyan pb-0.5">YUSUFZAI</a>
          </p>
        </div>
      </div>
    </footer>
  `,
  styles: []
})
export class FooterComponent { }
