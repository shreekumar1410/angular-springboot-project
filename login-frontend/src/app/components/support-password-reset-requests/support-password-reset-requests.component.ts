// Support Password Reset Requests Component

import { Component, OnInit } from '@angular/core';
import { SupportService } from '../../services/support.service';
import { CommonModule } from '@angular/common';

interface PasswordResetRequest {
  id: number;
  email: string;
  requestedAt: string;
  status: string;
  approvedAt?: string;
  approvedBy?: string;
}

@Component({
  selector: 'app-support-password-reset-requests',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './support-password-reset-requests.component.html',
  styleUrls: ['./support-password-reset-requests.component.css']
})
export class SupportPasswordResetRequestsComponent implements OnInit {
  requests: PasswordResetRequest[] = [];
  filteredRequests: PasswordResetRequest[] = [];
  errorMessage: string = '';
  successMessage: string = '';
  loading: boolean = false;

  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 10;
  totalPages: number = 1;

  constructor(private supportService: SupportService) {}

  ngOnInit(): void {
    this.loadRequests();
  }

  loadRequests(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.supportService.getPasswordResetRequests().subscribe({
      next: (data) => {
        this.requests = data;
        this.filteredRequests = [...this.requests];
        this.updatePagination();
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load password reset requests';
        this.loading = false;
        console.error('Error loading requests:', error);
      }
    });
  }

  approveRequest(requestId: number): void {
    if (!confirm('Are you sure you want to approve this password reset request? A new password will be generated.')) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    
    this.supportService.approvePasswordReset(requestId).subscribe({
      next: (response) => {
        this.successMessage = `Password reset approved successfully. New password: ${response.password || 'Generated'}`;
        this.loadRequests(); // Reload to refresh the list
        setTimeout(() => {
          this.successMessage = '';
        }, 5000);
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Failed to approve password reset request';
        this.loading = false;
        console.error('Error approving request:', error);
      }
    });
  }

  updatePagination(): void {
    this.totalPages = Math.ceil(this.filteredRequests.length / this.itemsPerPage);
    if (this.currentPage > this.totalPages && this.totalPages > 0) {
      this.currentPage = this.totalPages;
    }
  }

  get paginatedRequests(): PasswordResetRequest[] {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.filteredRequests.slice(start, end);
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

  canApprove(request: PasswordResetRequest): boolean {
    return request.status?.toUpperCase() === 'PENDING';
  }
}
