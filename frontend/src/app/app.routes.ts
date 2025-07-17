import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { LoginGuard } from './core/guards/login.guard';

export const routes: Routes = [
  // Public routes
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent),
    canActivate: [LoginGuard]
  },
  {
    path: 'forgot-password',
    loadComponent: () => import('./features/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent),
    canActivate: [LoginGuard]
  },
  
  // Protected routes
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [AuthGuard],
    data: { title: 'Dashboard', breadcrumb: 'Dashboard' }
  },
  {
    path: 'users',
    loadChildren: () => import('./features/users/users.routes').then(m => m.USERS_ROUTES),
    canActivate: [AuthGuard],
    data: { title: 'User Management', breadcrumb: 'Users' }
  },
  {
    path: 'roles',
    loadChildren: () => import('./features/roles/roles.routes').then(m => m.ROLES_ROUTES),
    canActivate: [AuthGuard],
    data: { title: 'Role Management', breadcrumb: 'Roles' }
  },
  {
    path: 'audit',
    loadChildren: () => import('./features/audit/audit.routes').then(m => m.AUDIT_ROUTES),
    canActivate: [AuthGuard],
    data: { title: 'Audit Logs', breadcrumb: 'Audit' }
  },
  {
    path: 'security',
    loadChildren: () => import('./features/security/security.routes').then(m => m.SECURITY_ROUTES),
    canActivate: [AuthGuard],
    data: { title: 'Security Monitoring', breadcrumb: 'Security' }
  },
  {
    path: 'reports',
    loadChildren: () => import('./features/reports/reports.routes').then(m => m.REPORTS_ROUTES),
    canActivate: [AuthGuard],
    data: { title: 'Reports', breadcrumb: 'Reports' }
  },
  {
    path: 'profile',
    loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent),
    canActivate: [AuthGuard],
    data: { title: 'Profile', breadcrumb: 'Profile' }
  },
  {
    path: 'settings',
    loadComponent: () => import('./features/settings/settings.component').then(m => m.SettingsComponent),
    canActivate: [AuthGuard],
    data: { title: 'Settings', breadcrumb: 'Settings' }
  },
  
  // Default redirects
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: '**',
    loadComponent: () => import('./shared/components/not-found/not-found.component').then(m => m.NotFoundComponent),
    data: { title: 'Page Not Found' }
  }
];
