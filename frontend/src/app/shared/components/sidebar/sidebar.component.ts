import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

interface MenuItem {
  label: string;
  icon: string;
  route?: string;
  children?: MenuItem[];
  roles?: string[];
  badge?: string;
  badgeClass?: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <aside class="sidebar" [class.collapsed]="collapsed">
      <div class="sidebar-content">
        <nav class="sidebar-nav">
          <ul class="nav-list">
            <li *ngFor="let item of menuItems" class="nav-item">
              <div *ngIf="hasPermission(item.roles)">
                <!-- Single menu item -->
                <a *ngIf="!item.children" 
                   [routerLink]="item.route" 
                   routerLinkActive="active"
                   class="nav-link"
                   [title]="collapsed ? item.label : ''">
                  <i [class]="item.icon"></i>
                  <span class="nav-text">{{ item.label }}</span>
                  <span *ngIf="item.badge" [class]="'badge ' + (item.badgeClass || 'badge-primary')">
                    {{ item.badge }}
                  </span>
                </a>

                <!-- Menu item with children -->
                <div *ngIf="item.children" class="nav-group">
                  <button class="nav-link nav-toggle" 
                          (click)="toggleSubmenu(item)"
                          [class.expanded]="item.expanded">
                    <i [class]="item.icon"></i>
                    <span class="nav-text">{{ item.label }}</span>
                    <i class="fas fa-chevron-down nav-arrow"></i>
                  </button>
                  <ul class="nav-submenu" [class.show]="item.expanded">
                    <li *ngFor="let child of item.children" class="nav-subitem">
                      <a *ngIf="hasPermission(child.roles)"
                         [routerLink]="child.route" 
                         routerLinkActive="active"
                         class="nav-sublink">
                        <i [class]="child.icon"></i>
                        <span>{{ child.label }}</span>
                        <span *ngIf="child.badge" [class]="'badge ' + (child.badgeClass || 'badge-primary')">
                          {{ child.badge }}
                        </span>
                      </a>
                    </li>
                  </ul>
                </div>
              </div>
            </li>
          </ul>
        </nav>
      </div>

      <!-- Sidebar Footer -->
      <div class="sidebar-footer">
        <div class="system-status">
          <div class="status-indicator online"></div>
          <span class="status-text">System Online</span>
        </div>
      </div>
    </aside>
  `,
  styles: [`
    .sidebar {
      position: fixed;
      left: 0;
      top: 70px;
      width: 250px;
      height: calc(100vh - 70px);
      background: #2c3e50;
      color: white;
      transition: all 0.3s ease;
      z-index: 999;
      display: flex;
      flex-direction: column;
    }

    .sidebar.collapsed {
      width: 70px;
    }

    .sidebar-content {
      flex: 1;
      overflow-y: auto;
      overflow-x: hidden;
    }

    .sidebar-nav {
      padding: 1rem 0;
    }

    .nav-list {
      list-style: none;
      padding: 0;
      margin: 0;
    }

    .nav-item {
      margin-bottom: 0.25rem;
    }

    .nav-link {
      display: flex;
      align-items: center;
      padding: 0.875rem 1.5rem;
      color: #bdc3c7;
      text-decoration: none;
      transition: all 0.3s ease;
      position: relative;
      border: none;
      background: none;
      width: 100%;
      text-align: left;
      cursor: pointer;
    }

    .nav-link:hover {
      background: rgba(255, 255, 255, 0.1);
      color: white;
    }

    .nav-link.active {
      background: #667eea;
      color: white;
    }

    .nav-link.active::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: #fff;
    }

    .nav-link i {
      width: 20px;
      text-align: center;
      margin-right: 0.75rem;
      font-size: 1.1rem;
    }

    .nav-text {
      flex: 1;
      white-space: nowrap;
      opacity: 1;
      transition: opacity 0.3s ease;
    }

    .collapsed .nav-text {
      opacity: 0;
    }

    .nav-arrow {
      font-size: 0.8rem;
      transition: transform 0.3s ease;
    }

    .nav-toggle.expanded .nav-arrow {
      transform: rotate(180deg);
    }

    .nav-submenu {
      list-style: none;
      padding: 0;
      margin: 0;
      max-height: 0;
      overflow: hidden;
      transition: max-height 0.3s ease;
      background: rgba(0, 0, 0, 0.2);
    }

    .nav-submenu.show {
      max-height: 300px;
    }

    .nav-sublink {
      display: flex;
      align-items: center;
      padding: 0.75rem 1.5rem 0.75rem 3rem;
      color: #bdc3c7;
      text-decoration: none;
      transition: all 0.3s ease;
      font-size: 0.9rem;
    }

    .nav-sublink:hover {
      background: rgba(255, 255, 255, 0.1);
      color: white;
    }

    .nav-sublink.active {
      background: #667eea;
      color: white;
    }

    .nav-sublink i {
      width: 16px;
      text-align: center;
      margin-right: 0.5rem;
      font-size: 0.9rem;
    }

    .badge {
      font-size: 0.7rem;
      padding: 0.2rem 0.4rem;
      border-radius: 10px;
      margin-left: auto;
    }

    .badge-primary {
      background: #667eea;
      color: white;
    }

    .badge-danger {
      background: #dc3545;
      color: white;
    }

    .badge-warning {
      background: #ffc107;
      color: #212529;
    }

    .sidebar-footer {
      padding: 1rem 1.5rem;
      border-top: 1px solid rgba(255, 255, 255, 0.1);
    }

    .system-status {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .status-indicator {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #28a745;
      animation: pulse 2s infinite;
    }

    .status-indicator.online {
      background: #28a745;
    }

    .status-text {
      font-size: 0.8rem;
      color: #bdc3c7;
      opacity: 1;
      transition: opacity 0.3s ease;
    }

    .collapsed .status-text {
      opacity: 0;
    }

    @keyframes pulse {
      0% { opacity: 1; }
      50% { opacity: 0.5; }
      100% { opacity: 1; }
    }

    @media (max-width: 768px) {
      .sidebar {
        transform: translateX(-100%);
      }

      .sidebar:not(.collapsed) {
        transform: translateX(0);
      }
    }
  `]
})
export class SidebarComponent {
  @Input() collapsed = false;
  @Output() collapsedChange = new EventEmitter<boolean>();

  menuItems: MenuItem[] = [
    {
      label: 'Dashboard',
      icon: 'fas fa-tachometer-alt',
      route: '/dashboard'
    },
    {
      label: 'User Management',
      icon: 'fas fa-users',
      children: [
        { label: 'All Users', icon: 'fas fa-list', route: '/users' },
        { label: 'Add User', icon: 'fas fa-user-plus', route: '/users/add' },
        { label: 'User Roles', icon: 'fas fa-user-tag', route: '/users/roles' }
      ],
      roles: ['ROLE_ADMIN', 'ROLE_USER_MANAGER']
    },
    {
      label: 'Role Management',
      icon: 'fas fa-user-shield',
      children: [
        { label: 'All Roles', icon: 'fas fa-list', route: '/roles' },
        { label: 'Permissions', icon: 'fas fa-key', route: '/roles/permissions' }
      ],
      roles: ['ROLE_ADMIN']
    },
    {
      label: 'Security Monitoring',
      icon: 'fas fa-shield-alt',
      children: [
        { label: 'Threat Dashboard', icon: 'fas fa-chart-line', route: '/security/dashboard' },
        { label: 'Security Alerts', icon: 'fas fa-exclamation-triangle', route: '/security/alerts', badge: '3', badgeClass: 'badge-danger' },
        { label: 'Risk Assessment', icon: 'fas fa-chart-pie', route: '/security/risk' }
      ],
      roles: ['ROLE_ADMIN', 'ROLE_SECURITY_OFFICER']
    },
    {
      label: 'Audit Logs',
      icon: 'fas fa-clipboard-list',
      children: [
        { label: 'All Logs', icon: 'fas fa-list', route: '/audit' },
        { label: 'Failed Logins', icon: 'fas fa-ban', route: '/audit/failed-logins' },
        { label: 'User Activities', icon: 'fas fa-user-clock', route: '/audit/activities' }
      ],
      roles: ['ROLE_ADMIN', 'ROLE_AUDITOR']
    },
    {
      label: 'Reports',
      icon: 'fas fa-chart-bar',
      children: [
        { label: 'Security Reports', icon: 'fas fa-file-alt', route: '/reports/security' },
        { label: 'Compliance Reports', icon: 'fas fa-clipboard-check', route: '/reports/compliance' },
        { label: 'Custom Reports', icon: 'fas fa-cogs', route: '/reports/custom' }
      ],
      roles: ['ROLE_ADMIN', 'ROLE_AUDITOR']
    },
    {
      label: 'Settings',
      icon: 'fas fa-cog',
      route: '/settings',
      roles: ['ROLE_ADMIN']
    }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  toggleSubmenu(item: MenuItem): void {
    if (this.collapsed) return;
    
    item.expanded = !item.expanded;
    
    // Close other submenus
    this.menuItems.forEach(menuItem => {
      if (menuItem !== item) {
        menuItem.expanded = false;
      }
    });
  }

  hasPermission(roles?: string[]): boolean {
    if (!roles || roles.length === 0) return true;
    return this.authService.hasAnyRole(roles);
  }
}
