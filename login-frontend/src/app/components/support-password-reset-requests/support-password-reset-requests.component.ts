// Support Password Reset Requests Component
// Two-Step Approval Model: Accept Request -> Send Password

import { Component, OnInit } from '@angular/core';
import { SupportService } from '../../services/support.service';
import { CommonModule } from '@angular/common';

interface PasswordResetRequest {
  id: number;
  userEmail: string;
  requestedAt: string;
  status: string; // REQUESTED | ACCEPTED | PASSWORD_SENT
  approvedBy?: string;
  approvedAt?: string;
  passwordSentAt?: string;
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
  processingRequestId: number | null = null; // Track which request is being processed

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
        console.log(data);
        this.requests = data;
        this.filteredRequests = [...this.requests];
        this.updatePagination();
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Failed to load password reset requests';
        this.loading = false;
        console.error('Error loading requests:', error);
      }
    });
  }

  // Step 1: Accept Request (generates password, stores hash, status -> ACCEPTED)
  acceptRequest(requestId: number): void {
    this.processingRequestId = requestId;
    this.errorMessage = '';
    this.successMessage = '';
    
    this.supportService.acceptPasswordReset(requestId).subscribe({
      next: () => {
        this.successMessage = 'Request accepted successfully. Password generated and stored.';
        this.processingRequestId = null;
        this.loadRequests(); // Refresh to show updated status
        setTimeout(() => {
          this.successMessage = '';
        }, 5000);
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Failed to accept password reset request';
        this.processingRequestId = null;
        console.error('Error accepting request:', error);
      }
    });
  }

  // Step 2: Send Password (updates user password, status -> PASSWORD_SENT)
  sendPassword(requestId: number): void {
    if (!confirm('This will reset the user\'s password. Continue?')) {
      return;
    }

    this.processingRequestId = requestId;
    this.errorMessage = '';
    this.successMessage = '';
    
    this.supportService.sendPassword(requestId).subscribe({
      next: () => {
        this.successMessage = 'Password sent successfully. User password has been reset.';
        this.processingRequestId = null;
        this.loadRequests(); // Refresh to show updated status
        setTimeout(() => {
          this.successMessage = '';
        }, 5000);
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Failed to send password';
        this.processingRequestId = null;
        console.error('Error sending password:', error);
      }
    });
  }

  // Check if request can be accepted (status is REQUESTED)
  canAccept(request: PasswordResetRequest): boolean {
    return request.status?.toUpperCase() === 'REQUESTED';
  }

  // Check if password can be sent (status is ACCEPTED)
  canSendPassword(request: PasswordResetRequest): boolean {
    return request.status?.toUpperCase() === 'ACCEPTED';
  }

  // Check if request is completed (status is PASSWORD_SENT)
  isCompleted(request: PasswordResetRequest): boolean {
    return request.status?.toUpperCase() === 'PASSWORD_SENT';
  }

  // Check if request is being processed
  isProcessing(requestId: number): boolean {
    return this.processingRequestId === requestId;
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
    const statusUpper = status?.toUpperCase();
    switch (statusUpper) {
      case 'REQUESTED':
        return 'badge-custom badge-warning';
      case 'ACCEPTED':
        return 'badge-custom badge-info';
      case 'PASSWORD_SENT':
        return 'badge-custom badge-success';
      default:
        return 'badge-custom badge-secondary';
    }
  }

  getStatusDisplay(status: string): string {
    const statusUpper = status?.toUpperCase();
    switch (statusUpper) {
      case 'REQUESTED':
        return 'Requested';
      case 'ACCEPTED':
        return 'Accepted';
      case 'PASSWORD_SENT':
        return 'Completed';
      default:
        return status;
    }
  }

  getMin(a: number, b: number): number {
    return Math.min(a, b);
  }

  getPageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }
}
