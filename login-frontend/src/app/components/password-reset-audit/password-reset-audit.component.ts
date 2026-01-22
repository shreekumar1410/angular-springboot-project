// Unified Password Reset Audit Component
// Handles ADMIN and SUPER_ADMIN roles with role-based features

import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

interface PasswordResetAuditEntry {
  id: number;
  email: string;
  requestedAt: string;
  approvedAt?: string;
  approvedBy?: string;
  status: string;
}

@Component({
  selector: 'app-password-reset-audit',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './password-reset-audit.component.html',
  styleUrls: ['./password-reset-audit.component.css']
})
export class PasswordResetAuditComponent implements OnInit {
  auditEntries: PasswordResetAuditEntry[] = [];
  filteredEntries: PasswordResetAuditEntry[] = [];
  errorMessage: string = '';
  loading: boolean = false;
  currentRole: string | null = null;

  // Role flags
  isAdmin: boolean = false;
  isSuperAdmin: boolean = false;

  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 10;
  totalPages: number = 1;

  constructor(
    private adminService: AdminService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentRole = this.authService.getRole();
    this.setRoleFlags();
    this.loadAuditData();
  }

  setRoleFlags(): void {
    this.isAdmin = this.authService.isAdmin();
    this.isSuperAdmin = this.authService.isSuperAdmin();
  }

  loadAuditData(): void {
    this.loading = true;
    this.errorMessage = '';
    this.adminService.getPasswordResetAudit().subscribe({
      next: (data) => {
        // Sort by latest first (requestedAt descending) for ADMIN and SUPER_ADMIN
        this.auditEntries = data.sort((a, b) => {
          const timeA = new Date(a.requestedAt).getTime();
          const timeB = new Date(b.requestedAt).getTime();
          return timeB - timeA;
        });
        this.filteredEntries = [...this.auditEntries];
        this.updatePagination();
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Failed to load password reset audit data';
        this.loading = false;
        console.error('Error loading audit data:', error);
      }
    });
  }

  updatePagination(): void {
    this.totalPages = Math.ceil(this.filteredEntries.length / this.itemsPerPage);
    if (this.currentPage > this.totalPages && this.totalPages > 0) {
      this.currentPage = this.totalPages;
    }
  }

  get paginatedEntries(): PasswordResetAuditEntry[] {
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

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'PENDING':
        return 'badge bg-warning';
      case 'APPROVED':
        return 'badge bg-success';
      case 'REJECTED':
        return 'badge bg-danger';
      default:
        return 'badge bg-secondary';
    }
  }

  getMin(a: number, b: number): number {
    return Math.min(a, b);
  }

  getPageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  // Get page title based on role
  getPageTitle(): string {
    return 'Password Reset Audit (Read-Only)';
  }
}
