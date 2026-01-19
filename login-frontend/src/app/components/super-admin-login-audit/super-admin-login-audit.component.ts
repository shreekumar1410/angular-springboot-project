// Super Admin Login Audit Component
// Extended to include PASSWORD_CHANGED audit records

import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../services/admin.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface LoginAuditEntry {
  email: string;
  role: string;
  loginType: string; // LOGIN | LOGOUT | FAILED | PASSWORD_CHANGED
  reason?: string; // PASSWORD_CHANGED_SUCCESS | INVALID_CURRENT_PASSWORD | SAME_PASSWORD_REUSE
  eventTime: string;
  jwtExpiresAt?: string;
}

@Component({
  selector: 'app-super-admin-login-audit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './super-admin-login-audit.component.html',
  styleUrls: ['./super-admin-login-audit.component.css']
})
export class SuperAdminLoginAuditComponent implements OnInit {
  auditEntries: LoginAuditEntry[] = [];
  filteredEntries: LoginAuditEntry[] = [];
  errorMessage: string = '';
  loading: boolean = false;

  // Filters
  filterEmail: string = '';
  filterAction: string = 'ALL';
  filterStatus: string = 'ALL';

  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 10;
  totalPages: number = 1;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadAuditData();
  }

  loadAuditData(): void {
    this.loading = true;
    this.errorMessage = '';
    this.adminService.getLoginAudit().subscribe({
      next: (data) => {
        // Sort by latest first (eventTime descending)
        this.auditEntries = data.sort((a, b) => {
          const timeA = new Date(a.eventTime).getTime();
          const timeB = new Date(b.eventTime).getTime();
          return timeB - timeA;
        });
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Failed to load login audit data';
        this.loading = false;
        console.error('Error loading audit data:', error);
      }
    });
  }

  applyFilters(): void {
    this.filteredEntries = this.auditEntries.filter(entry => {
      // Email filter
      if (this.filterEmail && !entry.email.toLowerCase().includes(this.filterEmail.toLowerCase())) {
        return false;
      }

      // Action filter
      if (this.filterAction !== 'ALL') {
        if (this.filterAction === 'LOGIN' && entry.loginType !== 'LOGIN') return false;
        if (this.filterAction === 'LOGOUT' && entry.loginType !== 'LOGOUT') return false;
        if (this.filterAction === 'PASSWORD_CHANGED' && entry.loginType !== 'PASSWORD_CHANGED') return false;
        if (this.filterAction === 'FAILED' && entry.loginType !== 'FAILED') return false;
      }

      // Status filter
      if (this.filterStatus !== 'ALL') {
        const status = this.getStatus(entry);
        if (this.filterStatus === 'SUCCESS' && status !== 'SUCCESS') return false;
        if (this.filterStatus === 'FAILED' && status !== 'FAILED') return false;
      }

      return true;
    });

    this.updatePagination();
    this.currentPage = 1; // Reset to first page when filtering
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  clearFilters(): void {
    this.filterEmail = '';
    this.filterAction = 'ALL';
    this.filterStatus = 'ALL';
    this.applyFilters();
  }

  // Map loginType to Action display text
  getAction(loginType: string): string {
    switch (loginType) {
      case 'LOGIN':
        return 'Login';
      case 'LOGOUT':
        return 'Logout';
      case 'FAILED':
        return 'Login Failed';
      case 'PASSWORD_CHANGED':
        return 'Password Changed';
      default:
        return loginType;
    }
  }

  // Map loginType and reason to Status
  getStatus(entry: LoginAuditEntry): string {
    const loginType = entry.loginType?.toUpperCase();
    const reason = entry.reason?.toUpperCase();

    if (loginType === 'PASSWORD_CHANGED') {
      if (reason === 'PASSWORD_CHANGED_SUCCESS') {
        return 'SUCCESS';
      } else if (reason === 'INVALID_CURRENT_PASSWORD' || reason === 'SAME_PASSWORD_REUSE') {
        return 'FAILED';
      }
      // Default for PASSWORD_CHANGED without clear reason
      return 'SUCCESS';
    }

    if (loginType === 'LOGIN' || loginType === 'LOGOUT') {
      return 'SUCCESS';
    }

    if (loginType === 'FAILED') {
      return 'FAILED';
    }

    return 'UNKNOWN';
  }

  // Get status badge class
  getStatusClass(entry: LoginAuditEntry): string {
    const status = this.getStatus(entry);
    switch (status) {
      case 'SUCCESS':
        return 'badge-custom badge-success';
      case 'FAILED':
        return 'badge-custom badge-danger';
      default:
        return 'badge-custom badge-secondary';
    }
  }

  // Get action badge class
  getActionClass(loginType: string): string {
    switch (loginType) {
      case 'LOGIN':
        return 'badge-custom badge-success';
      case 'LOGOUT':
        return 'badge-custom badge-info';
      case 'FAILED':
        return 'badge-custom badge-danger';
      case 'PASSWORD_CHANGED':
        return 'badge-custom badge-warning';
      default:
        return 'badge-custom badge-secondary';
    }
  }

  // Get action icon
  getActionIcon(loginType: string): string {
    switch (loginType) {
      case 'LOGIN':
        return 'bi-box-arrow-in-right';
      case 'LOGOUT':
        return 'bi-box-arrow-right';
      case 'FAILED':
        return 'bi-x-circle';
      case 'PASSWORD_CHANGED':
        return 'bi-key';
      default:
        return 'bi-question-circle';
    }
  }

  updatePagination(): void {
    this.totalPages = Math.ceil(this.filteredEntries.length / this.itemsPerPage);
    if (this.currentPage > this.totalPages && this.totalPages > 0) {
      this.currentPage = this.totalPages;
    }
  }

  get paginatedEntries(): LoginAuditEntry[] {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.filteredEntries.slice(start, end);
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString();
  }

  getMin(a: number, b: number): number {
    return Math.min(a, b);
  }

  getPageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }
}
