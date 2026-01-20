// ng g guard guards/role

import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const requiredRole = route.data['role'];
    const userRole = this.authService.getRole();
    
    if (!userRole) {
      this.router.navigate(['/login']);
      return false;
    }

    // Check if user has the required role
    if (requiredRole === 'USER' && this.authService.isUser()) {
      return true;
    } else if (requiredRole === 'EDITOR' && this.authService.isEditor()) {
      return true;
    } else if (requiredRole === 'SUPPORT' && this.authService.isSupport()) {
      return true;
    } else if (requiredRole === 'ADMIN' && this.authService.isAdmin()) {
      return true;
    } else if (requiredRole === 'SUPER_ADMIN' && this.authService.isSuperAdmin()) {
      return true;
    } else if (Array.isArray(requiredRole)) {
      // Support multiple roles
      return requiredRole.includes(userRole);
    } else {
      this.router.navigate(['/login']);
      return false;
    }
  }
}