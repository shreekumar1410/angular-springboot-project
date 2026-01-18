// Super Admin Login Audit Component

import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../services/admin.service';
import { CommonModule } from '@angular/common';

interface LoginAuditEntry {
  email: string;
  role: string;
  loginType: string;
  eventTime: string;
  jwtExpiresAt: string;
}

@Component({
  selector: 'app-super-admin-login-audit',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './super-admin-login-audit.component.html',
  styleUrls: ['./super-admin-login-audit.component.css']
})
export class SuperAdminLoginAuditComponent implements OnInit {
  auditEntries: LoginAuditEntry[] = [];
  filteredEntries: LoginAuditEntry[] = [];
  errorMessage: string = '';
  loading: boolean = false;

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
        this.auditEntries = data;
        this.filteredEntries = [...this.auditEntries];
        this.updatePagination();
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load login audit data';
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

  formatDate(dateString: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString();
  }

  getLoginTypeClass(loginType: string): string {
    switch (loginType) {
      case 'LOGIN':
        return 'badge bg-success';
      case 'LOGOUT':
        return 'badge bg-info';
      case 'FAILED':
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
}
