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
  constructor(private authService: AuthService, private router: Router) {}

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

  getUsersRoute(): string {
    const role = this.authService.getRole();
    switch (role) {
      case 'USER':
        return '/users';
      case 'SUPPORT':
        return '/users'; // Support can also view users list
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
}