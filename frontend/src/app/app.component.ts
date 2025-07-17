import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { HeaderComponent } from './shared/components/header/header.component';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { FooterComponent } from './shared/components/footer/footer.component';
import { AuthService } from './core/services/auth.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    HeaderComponent,
    SidebarComponent,
    FooterComponent
  ],
  template: `
    <div class="app-container" [class.authenticated]="isAuthenticated">
      <!-- Login/Public Layout -->
      <div *ngIf="!isAuthenticated" class="public-layout">
        <router-outlet></router-outlet>
      </div>
      
      <!-- Authenticated Layout -->
      <div *ngIf="isAuthenticated" class="authenticated-layout">
        <app-header 
          (toggleSidebar)="toggleSidebar()"
          [sidebarCollapsed]="sidebarCollapsed">
        </app-header>
        
        <div class="main-content-wrapper">
          <app-sidebar 
            [collapsed]="sidebarCollapsed"
            (collapsedChange)="onSidebarCollapsedChange($event)">
          </app-sidebar>
          
          <main class="main-content" [class.sidebar-collapsed]="sidebarCollapsed">
            <div class="content-container">
              <router-outlet></router-outlet>
            </div>
          </main>
        </div>
        
        <app-footer></app-footer>
      </div>
    </div>
  `,
  styles: [`
    .app-container {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    
    .public-layout {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    
    .authenticated-layout {
      flex: 1;
      display: flex;
      flex-direction: column;
    }
    
    .main-content-wrapper {
      flex: 1;
      display: flex;
      position: relative;
    }
    
    .main-content {
      flex: 1;
      margin-left: 250px;
      transition: margin-left 0.3s ease;
      background-color: #f8f9fa;
      min-height: calc(100vh - 120px);
    }
    
    .main-content.sidebar-collapsed {
      margin-left: 70px;
    }
    
    .content-container {
      padding: 2rem;
      max-width: 100%;
      overflow-x: auto;
    }
    
    @media (max-width: 768px) {
      .main-content {
        margin-left: 0;
      }
      
      .main-content.sidebar-collapsed {
        margin-left: 0;
      }
      
      .content-container {
        padding: 1rem;
      }
    }
  `]
})
export class AppComponent implements OnInit {
  title = 'SAMAP - Secure Access Management & Audit Platform';
  isAuthenticated = false;
  sidebarCollapsed = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Subscribe to authentication status
    this.authService.isAuthenticated$.subscribe(
      isAuth => {
        this.isAuthenticated = isAuth;
        if (!isAuth) {
          this.router.navigate(['/login']);
        }
      }
    );

    // Handle route changes
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      // Close sidebar on mobile after navigation
      if (window.innerWidth <= 768) {
        this.sidebarCollapsed = true;
      }
    });

    // Handle window resize
    window.addEventListener('resize', () => {
      if (window.innerWidth <= 768) {
        this.sidebarCollapsed = true;
      } else {
        this.sidebarCollapsed = false;
      }
    });
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  onSidebarCollapsedChange(collapsed: boolean): void {
    this.sidebarCollapsed = collapsed;
  }
}
