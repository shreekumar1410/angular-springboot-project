import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class LoginGuard implements CanActivate {

  constructor(private router: Router) {}

  canActivate(): boolean {

    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    const profileCreated = localStorage.getItem('profileCreated') === 'true';

    if (token) {
      // Already logged in → redirect
      if (!profileCreated) {
        this.router.navigate(['/profile/create']);
      } else if (role === 'ADMIN') {
        this.router.navigate(['/admin/dashboard']);
      } else {
        this.router.navigate(['/user/dashboard']);
      }
      return false;
    }

    return true; // Allow login page
  }
}
