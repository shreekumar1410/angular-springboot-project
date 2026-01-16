// Unified Dashboard Component for both User and Admin

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './user-dashboard.component.html',
  styleUrls: ['./user-dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  loginAlertMessage: string | null = null;
  showAlert: boolean = false;
  isAdmin: boolean = false;
  isUser: boolean = false;
  private autoHideTimer: any = null;
  private readonly AUTO_HIDE_DELAY = 5000; // 5 seconds

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // Check user role
    this.isAdmin = this.authService.isAdmin();
    this.isUser = this.authService.isUser();

    // Check for login alert message in sessionStorage
    const alertMessage = sessionStorage.getItem('loginAlertMessage');
    if (alertMessage) {
      this.loginAlertMessage = alertMessage;
      this.showAlert = true;
      // Remove from sessionStorage after displaying
      sessionStorage.removeItem('loginAlertMessage');
      // Auto-hide after delay
      this.startAutoHideTimer();
    }
  }

  ngOnDestroy(): void {
    this.clearAutoHideTimer();
  }

  startAutoHideTimer(): void {
    this.clearAutoHideTimer();
    this.autoHideTimer = setTimeout(() => {
      this.dismissAlert();
    }, this.AUTO_HIDE_DELAY);
  }

  clearAutoHideTimer(): void {
    if (this.autoHideTimer) {
      clearTimeout(this.autoHideTimer);
      this.autoHideTimer = null;
    }
  }

  dismissAlert(): void {
    this.clearAutoHideTimer();
    this.showAlert = false;
    this.loginAlertMessage = null;
  }
}