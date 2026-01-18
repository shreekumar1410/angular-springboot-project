// Admin Dashboard Component

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  loginAlertMessage: string | null = null;
  showAlert: boolean = false;
  private autoHideTimer: any = null;
  private readonly AUTO_HIDE_DELAY = 5000; // 5 seconds

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // Check for login alert message in sessionStorage
    const alertMessage = sessionStorage.getItem('loginAlertMessage');
    if (alertMessage) {
      this.loginAlertMessage = alertMessage;
      this.showAlert = true;
      sessionStorage.removeItem('loginAlertMessage');
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
