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
    if (requiredRole === 'ADMIN' && this.authService.isAdmin()) {
      return true;
    } else if (requiredRole === 'USER' && this.authService.isUser()) {
      return true;
    } else {
      this.router.navigate(['/login']);
      return false;
    }
  }
}