// ng g component components/navbar

import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  // role: string | null;

  constructor(private authService: AuthService, private router: Router) {
    // this.role = this.authService.getRole();
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  isUser(): boolean {
    return this.authService.isUser();
  }

  isSupport(): boolean {
    return this.authService.isSupport();
  }

  isSuperAdmin(): boolean {
    return this.authService.isSuperAdmin();
  }

  isEditor(): boolean {
    return this.authService.isEditor();
  }

  getUsersRoute(): string {
    const role = this.authService.getRole();
    switch (role) {
      case 'USER':
        return '/users';
      case 'EDITOR':
        return '/editor/users';
      case 'SUPPORT':
        return '/support/users'; // Support can also view users list
      case 'ADMIN':
        return '/admin/users';
      case 'SUPER_ADMIN':
        return '/super-admin/users';
      default:
        return '/users';
    }
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.authService.clearLocalStorage();
        this.router.navigate(['/login']);
      },
      error: (err) => {
        // Even if logout API fails, clear local storage and redirect
        console.error('Logout API error:', err);
        this.authService.clearLocalStorage();
        this.router.navigate(['/login']);
      }
    });
  }

  // Get formatted role display name
  getRoleDisplayName(): string {
    const role = this.authService.getRole();
    if (!role) return '';
    switch (role) {
      case 'USER':
        return 'USER';
      case 'EDITOR':
        return 'EDITOR';
      case 'SUPPORT':
        return 'SUPPORT';
      case 'ADMIN':
        return 'ADMIN';
      case 'SUPER_ADMIN':
        return 'SUPER_ADMIN';
      default:
        return role;
    }
  }

  // Get role badge class for styling
  getRoleBadgeClass(): string {
    const role = this.authService.getRole();
    if (!role) return 'role-badge-default';
    switch (role) {
      case 'USER':
        return 'role-badge-user';
      case 'EDITOR':
        return 'role-badge-editor';
      case 'SUPPORT':
        return 'role-badge-support';
      case 'ADMIN':
        return 'role-badge-admin';
      case 'SUPER_ADMIN':
        return 'role-badge-super-admin';
      default:
        return 'role-badge-default';
    }
  }

  // Get role icon
  getRoleIcon(): string {
    const role = this.authService.getRole();
    if (!role) return 'bi-person';
    switch (role) {
      case 'USER':
        return 'bi-person me-1';
      case 'EDITOR':
        return 'bi-pencil-square me-1';
      case 'SUPPORT':
        return 'bi-headset me-1';
      case 'ADMIN':
        return 'bi-shield-check me-1';
      case 'SUPER_ADMIN':
        return 'bi-shield-lock me-1';
      default:
        return 'bi-person me-1';
    }
  }
}