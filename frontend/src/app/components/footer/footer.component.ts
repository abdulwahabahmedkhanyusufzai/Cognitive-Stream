import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  template: `
    <footer class="py-12 border-t border-white/5 bg-nebula-dark text-gray-500">
      <div class="max-w-7xl mx-auto px-8 flex flex-col md:flex-row justify-between items-center gap-6">
        <div class="flex items-center space-x-3 group cursor-default">
            <div class="w-6 h-6 bg-nebula-violet rounded flex items-center justify-center transition-transform group-hover:scale-110">
                <span class="text-white font-bold text-[10px]">C</span>
            </div>
            <span class="text-white font-bold text-xs tracking-widest uppercase opacity-50 group-hover:opacity-100 transition-opacity">Cognitive Stream</span>
        </div>
        
        <p class="text-[10px] font-bold uppercase tracking-[0.2em]">
          &copy; 2026 Developed by 
          <a href="https://github.com/abdulwahabahmedkhanyusufzai" target="_blank" class="text-white hover:text-nebula-cyan transition-colors">YUSUFZAI</a>
        </p>
      </div>
    </footer>
  `,
  styles: []
})
export class FooterComponent { }
