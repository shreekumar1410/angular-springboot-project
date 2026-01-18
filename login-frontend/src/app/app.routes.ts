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
  
  // Profile routes (accessible to all authenticated users)
  { path: 'profile', loadComponent: () => import('./components/profile/profile.component').then(m => m.ProfileComponent), canActivate: [AuthGuard] },
  { path: 'profile/form', loadComponent: () => import('./components/profile-form/profile-form.component').then(m => m.ProfileFormComponent), canActivate: [AuthGuard] },
  
  // USER role routes
  { path: 'user/dashboard', loadComponent: () => import('./components/user-dashboard/user-dashboard.component').then(m => m.UserDashboardComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'USER' } },
  { path: 'user/profile', loadComponent: () => import('./components/profile/profile.component').then(m => m.ProfileComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'USER' } },
  { path: 'users', loadComponent: () => import('./components/users-list/users-list.component').then(m => m.UsersListComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'USER' } },
  
  // SUPPORT role routes
  { path: 'support/dashboard', loadComponent: () => import('./components/support-dashboard/support-dashboard.component').then(m => m.SupportDashboardComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'SUPPORT' } },
  { path: 'support/password-reset-requests', loadComponent: () => import('./components/support-password-reset-requests/support-password-reset-requests.component').then(m => m.SupportPasswordResetRequestsComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'SUPPORT' } },
  { path: 'support/login-audit', loadComponent: () => import('./components/support-login-audit/support-login-audit.component').then(m => m.SupportLoginAuditComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'SUPPORT' } },
  
  // ADMIN role routes
  { path: 'admin/dashboard', loadComponent: () => import('./components/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'ADMIN' } },
  { path: 'admin/users', loadComponent: () => import('./components/admin-users/admin-users.component').then(m => m.AdminUsersComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'ADMIN' } },
  { path: 'admin/auth-users', loadComponent: () => import('./components/admin-auth-users/admin-auth-users.component').then(m => m.AdminAuthUsersComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'ADMIN' } },
  { path: 'admin/password-reset-audit', loadComponent: () => import('./components/admin-password-reset-audit/admin-password-reset-audit.component').then(m => m.AdminPasswordResetAuditComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'ADMIN' } },
  
  // SUPER_ADMIN role routes
  { path: 'super-admin/dashboard', loadComponent: () => import('./components/super-admin-dashboard/super-admin-dashboard.component').then(m => m.SuperAdminDashboardComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'SUPER_ADMIN' } },
  { path: 'super-admin/users', loadComponent: () => import('./components/admin-users/admin-users.component').then(m => m.AdminUsersComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'SUPER_ADMIN' } },
  { path: 'super-admin/roles', loadComponent: () => import('./components/super-admin-roles/super-admin-roles.component').then(m => m.SuperAdminRolesComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'SUPER_ADMIN' } },
  { path: 'super-admin/password-reset-audit', loadComponent: () => import('./components/admin-password-reset-audit/admin-password-reset-audit.component').then(m => m.AdminPasswordResetAuditComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'SUPER_ADMIN' } },
  { path: 'super-admin/login-audit', loadComponent: () => import('./components/super-admin-login-audit/super-admin-login-audit.component').then(m => m.SuperAdminLoginAuditComponent), canActivate: [AuthGuard, ProfileGuard, RoleGuard], data: { role: 'SUPER_ADMIN' } },
];
