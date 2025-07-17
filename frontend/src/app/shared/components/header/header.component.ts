import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService, User } from '../../../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <header class="main-header">
      <div class="header-left">
        <button class="sidebar-toggle" (click)="toggleSidebar.emit()">
          <i class="fas fa-bars"></i>
        </button>
        <div class="logo">
          <i class="fas fa-shield-alt"></i>
          <span>SAMAP</span>
        </div>
      </div>

      <div class="header-center">
        <div class="search-box">
          <i class="fas fa-search"></i>
          <input type="text" placeholder="Search users, logs, alerts...">
        </div>
      </div>

      <div class="header-right">
        <div class="header-actions">
          <button class="action-btn" title="Notifications">
            <i class="fas fa-bell"></i>
            <span class="badge">3</span>
          </button>
          <button class="action-btn" title="Security Alerts">
            <i class="fas fa-shield-alt"></i>
            <span class="badge alert">2</span>
          </button>
        </div>

        <div class="user-menu" (click)="toggleUserMenu()">
          <div class="user-avatar">
            <i class="fas fa-user"></i>
          </div>
          <div class="user-info">
            <span class="user-name">{{ currentUser?.firstName }} {{ currentUser?.lastName }}</span>
            <span class="user-role">{{ getUserRoles() }}</span>
          </div>
          <i class="fas fa-chevron-down"></i>

          <div class="dropdown-menu" [class.show]="showUserMenu">
            <a routerLink="/profile" class="dropdown-item">
              <i class="fas fa-user"></i> Profile
            </a>
            <a routerLink="/settings" class="dropdown-item">
              <i class="fas fa-cog"></i> Settings
            </a>
            <div class="dropdown-divider"></div>
            <button class="dropdown-item" (click)="logout()">
              <i class="fas fa-sign-out-alt"></i> Logout
            </button>
          </div>
        </div>
      </div>
    </header>
  `,
  styles: [`
    .main-header {
      height: 70px;
      background: white;
      border-bottom: 1px solid #e9ecef;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 1.5rem;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      position: sticky;
      top: 0;
      z-index: 1000;
    }

    .header-left {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .sidebar-toggle {
      background: none;
      border: none;
      font-size: 1.2rem;
      color: #6c757d;
      cursor: pointer;
      padding: 0.5rem;
      border-radius: 5px;
      transition: all 0.3s ease;
    }

    .sidebar-toggle:hover {
      background: #f8f9fa;
      color: #667eea;
    }

    .logo {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 1.5rem;
      font-weight: 700;
      color: #2c3e50;
    }

    .logo i {
      color: #667eea;
    }

    .header-center {
      flex: 1;
      max-width: 500px;
      margin: 0 2rem;
    }

    .search-box {
      position: relative;
      width: 100%;
    }

    .search-box i {
      position: absolute;
      left: 1rem;
      top: 50%;
      transform: translateY(-50%);
      color: #6c757d;
    }

    .search-box input {
      width: 100%;
      padding: 0.75rem 1rem 0.75rem 2.5rem;
      border: 1px solid #e9ecef;
      border-radius: 25px;
      background: #f8f9fa;
      transition: all 0.3s ease;
    }

    .search-box input:focus {
      outline: none;
      border-color: #667eea;
      background: white;
      box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .header-actions {
      display: flex;
      gap: 0.5rem;
    }

    .action-btn {
      position: relative;
      background: none;
      border: none;
      padding: 0.75rem;
      border-radius: 50%;
      color: #6c757d;
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .action-btn:hover {
      background: #f8f9fa;
      color: #667eea;
    }

    .action-btn .badge {
      position: absolute;
      top: 0.25rem;
      right: 0.25rem;
      background: #dc3545;
      color: white;
      font-size: 0.7rem;
      padding: 0.2rem 0.4rem;
      border-radius: 10px;
      min-width: 1.2rem;
      text-align: center;
    }

    .action-btn .badge.alert {
      background: #ffc107;
      color: #212529;
    }

    .user-menu {
      position: relative;
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.5rem 1rem;
      border-radius: 25px;
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .user-menu:hover {
      background: #f8f9fa;
    }

    .user-avatar {
      width: 40px;
      height: 40px;
      background: linear-gradient(135deg, #667eea, #764ba2);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
    }

    .user-info {
      display: flex;
      flex-direction: column;
    }

    .user-name {
      font-weight: 600;
      color: #2c3e50;
      font-size: 0.9rem;
    }

    .user-role {
      font-size: 0.75rem;
      color: #6c757d;
    }

    .dropdown-menu {
      position: absolute;
      top: 100%;
      right: 0;
      background: white;
      border: 1px solid #e9ecef;
      border-radius: 10px;
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
      min-width: 200px;
      opacity: 0;
      visibility: hidden;
      transform: translateY(-10px);
      transition: all 0.3s ease;
      z-index: 1000;
    }

    .dropdown-menu.show {
      opacity: 1;
      visibility: visible;
      transform: translateY(0);
    }

    .dropdown-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem 1rem;
      color: #2c3e50;
      text-decoration: none;
      border: none;
      background: none;
      width: 100%;
      text-align: left;
      transition: all 0.3s ease;
    }

    .dropdown-item:hover {
      background: #f8f9fa;
      color: #667eea;
    }

    .dropdown-divider {
      height: 1px;
      background: #e9ecef;
      margin: 0.5rem 0;
    }

    @media (max-width: 768px) {
      .header-center {
        display: none;
      }

      .user-info {
        display: none;
      }

      .header-actions {
        gap: 0.25rem;
      }
    }
  `]
})
export class HeaderComponent {
  @Input() sidebarCollapsed = false;
  @Output() toggleSidebar = new EventEmitter<void>();

  currentUser: User | null = null;
  showUserMenu = false;

  constructor(private authService: AuthService) {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
  }

  getUserRoles(): string {
    if (!this.currentUser?.roles) return '';
    return this.currentUser.roles
      .map(role => role.replace('ROLE_', ''))
      .join(', ');
  }

  logout(): void {
    this.authService.logout();
  }
}
