import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { ProfileGuard } from './guards/profile.guard';
import { RoleGuard } from './guards/role.guard';
import { LoginGuard } from './guards/login.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent), canActivate: [LoginGuard] },
  { path: 'register-login', loadComponent: () => import('./components/register-login/register-login.component').then(m => m.RegisterLoginComponent) },
  { path: 'forgot-password', loadComponent: () => import('./components/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent) },
  { path: 'profile', loadComponent: () => import('./components/profile/profile.component').then(m => m.ProfileComponent), canActivate: [AuthGuard] },
  { path: 'profile/form', loadComponent: () => import('./components/profile-form/profile-form.component').then(m => m.ProfileFormComponent), canActivate: [AuthGuard] },
  { path: 'users', loadComponent: () => import('./components/users-list/users-list.component').then(m => m.UsersListComponent), canActivate: [AuthGuard, ProfileGuard] },
  { path: 'user/dashboard', loadComponent: () => import('./components/user-dashboard/user-dashboard.component').then(m => m.DashboardComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'USER' } },
  { path: 'admin/dashboard', loadComponent: () => import('./components/user-dashboard/user-dashboard.component').then(m => m.DashboardComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'ADMIN' } },
  { path: 'admin/users', loadComponent: () => import('./components/admin-users/admin-users.component').then(m => m.AdminUsersComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'ADMIN' } },
  { path: 'admin/auth-users', loadComponent: () => import('./components/admin-auth-users/admin-auth-users.component').then(m => m.AdminAuthUsersComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'ADMIN' } },
  { path: 'admin/login-audit', loadComponent: () => import('./components/admin-login-audit/admin-login-audit.component').then(m => m.AdminLoginAuditComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'ADMIN' } },
];
