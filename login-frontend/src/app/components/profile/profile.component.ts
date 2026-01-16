// ng g component components/profile

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: any = null;
  errorMessage: string = '';
  lastLoginTime: string | null = null;
  lastLogoutTime: string | null = null;

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (this.authService.isProfileCreated()) {
      this.loadProfile();
      this.loadLoginHistory();
    } else {
      this.router.navigate(['/profile/form']);
    }
  }

  loadProfile(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load profile';
      }
    });
  }

  loadLoginHistory(): void {
    this.userService.getUserLoginHistory().subscribe({
      next: (history) => {
        // Find last LOGIN event
        const loginEvents = history.filter((entry: any) => entry.loginType === 'LOGIN');
        if (loginEvents.length > 0) {
          this.lastLoginTime = loginEvents[0].eventTime;
        }

        // Find last LOGOUT event
        const logoutEvents = history.filter((entry: any) => entry.loginType === 'LOGOUT');
        if (logoutEvents.length > 0) {
          this.lastLogoutTime = logoutEvents[0].eventTime;
        }
      },
      error: (error) => {
        // Silently fail - login history is optional
        console.error('Failed to load login history:', error);
      }
    });
  }

  formatDate(dateString: string | null): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString();
  }

  editProfile(): void {
    this.router.navigate(['/profile/form']);
  }
}