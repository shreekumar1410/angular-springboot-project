// Unified Dashboard Component

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  loginAlertMessage: string | null = null;
  showAlert: boolean = false;
  private autoHideTimer: any = null;
  private readonly AUTO_HIDE_DELAY = 5000; // 5 seconds
  currentRole: string | null = null;
  dashboardTitle: string = 'Dashboard';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // Get current role
    this.currentRole = this.authService.getRole();
    this.setDashboardTitle();

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

  setDashboardTitle(): void {
    switch (this.currentRole) {
      case 'USER':
        this.dashboardTitle = 'User Dashboard';
        break;
      case 'EDITOR':
        this.dashboardTitle = 'Editor Dashboard';
        break;
      case 'SUPPORT':
        this.dashboardTitle = 'Support Dashboard';
        break;
      case 'ADMIN':
        this.dashboardTitle = 'Admin Dashboard';
        break;
      case 'SUPER_ADMIN':
        this.dashboardTitle = 'Super Admin Dashboard';
        break;
      default:
        this.dashboardTitle = 'Dashboard';
    }
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
