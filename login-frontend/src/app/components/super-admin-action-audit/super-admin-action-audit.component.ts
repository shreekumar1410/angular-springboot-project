// Super Admin Action Audit Component
// Shows all privileged actions in the system (read-only)

import { Component, OnInit } from '@angular/core';
import { ActionAuditService, ActionAuditEntry } from '../../services/action-audit.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-super-admin-action-audit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './super-admin-action-audit.component.html',
  styleUrls: ['./super-admin-action-audit.component.css']
})
export class SuperAdminActionAuditComponent implements OnInit {
  auditEntries: ActionAuditEntry[] = [];
  filteredEntries: ActionAuditEntry[] = [];
  errorMessage: string = '';
  loading: boolean = false;

  // Filters
  filterActionType: string = 'ALL';
  filterStatus: string = 'ALL';
  filterDateFrom: string = '';
  filterDateTo: string = '';

  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 10;
  totalPages: number = 1;

  // Expanded rows
  expandedRows: Set<number> = new Set();

  // Action type options
  actionTypes: string[] = [
    'ALL',
    'ROLE_CHANGE',
    'PROFILE_CREATE',
    'PROFILE_UPDATE',
    'PASSWORD_RESET',
    'USER_DELETE',
    'ACCOUNT_ACTIVATE',
    'ACCOUNT_DEACTIVATE'
  ];

  constructor(private actionAuditService: ActionAuditService) {}

  ngOnInit(): void {
    this.loadAuditData();
  }

  loadAuditData(): void {
    this.loading = true;
    this.errorMessage = '';
    this.actionAuditService.getAllActionAudit().subscribe({
      next: (data) => {
        // Sort by latest first (performedAt descending)
        this.auditEntries = data.sort((a, b) => {
          const timeA = new Date(a.performedAt).getTime();
          const timeB = new Date(b.performedAt).getTime();
          return timeB - timeA;
        });
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Failed to load action audit data';
        this.loading = false;
        console.error('Error loading audit data:', error);
      }
    });
  }

  applyFilters(): void {
    this.filteredEntries = this.auditEntries.filter(entry => {
      // Action type filter
      if (this.filterActionType !== 'ALL' && entry.actionType !== this.filterActionType) {
        return false;
      }

      // Status filter
      if (this.filterStatus !== 'ALL' && entry.actionStatus !== this.filterStatus) {
        return false;
      }

      // Date range filter
      if (this.filterDateFrom || this.filterDateTo) {
        const entryDate = new Date(entry.performedAt);
        if (this.filterDateFrom) {
          const fromDate = new Date(this.filterDateFrom);
          if (entryDate < fromDate) return false;
        }
        if (this.filterDateTo) {
          const toDate = new Date(this.filterDateTo);
          toDate.setHours(23, 59, 59, 999); // Include the entire end date
          if (entryDate > toDate) return false;
        }
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
    this.filterActionType = 'ALL';
    this.filterStatus = 'ALL';
    this.filterDateFrom = '';
    this.filterDateTo = '';
    this.applyFilters();
  }

  // Toggle row expansion for viewing before/after states
  toggleRowExpansion(index: number): void {
    if (this.expandedRows.has(index)) {
      this.expandedRows.delete(index);
    } else {
      this.expandedRows.add(index);
    }
  }

  isRowExpanded(index: number): boolean {
    return this.expandedRows.has(index);
  }

  // Get status badge class
  getStatusClass(status: string): string {
    switch (status) {
      case 'SUCCESS':
        return 'badge-custom badge-success';
      case 'FAILED':
        return 'badge-custom badge-danger';
      default:
        return 'badge-custom badge-secondary';
    }
  }

  // Get action type badge class
  getActionTypeClass(actionType: string): string {
    switch (actionType) {
      case 'ROLE_CHANGE':
        return 'badge-custom badge-warning';
      case 'PROFILE_CREATE':
        return 'badge-custom badge-success';
      case 'PROFILE_UPDATE':
        return 'badge-custom badge-info';
      case 'PASSWORD_RESET':
        return 'badge-custom badge-primary';
      case 'USER_DELETE':
        return 'badge-custom badge-danger';
      case 'ACCOUNT_ACTIVATE':
        return 'badge-custom badge-success';
      case 'ACCOUNT_DEACTIVATE':
        return 'badge-custom badge-secondary';
      default:
        return 'badge-custom badge-secondary';
    }
  }

  // Get action type icon
  getActionTypeIcon(actionType: string): string {
    switch (actionType) {
      case 'ROLE_CHANGE':
        return 'bi-shield-lock';
      case 'PROFILE_CREATE':
        return 'bi-person-plus';
      case 'PROFILE_UPDATE':
        return 'bi-pencil-square';
      case 'PASSWORD_RESET':
        return 'bi-key';
      case 'USER_DELETE':
        return 'bi-trash';
      case 'ACCOUNT_ACTIVATE':
        return 'bi-check-circle';
      case 'ACCOUNT_DEACTIVATE':
        return 'bi-x-circle';
      default:
        return 'bi-question-circle';
    }
  }

  // Format action type for display
  formatActionType(actionType: string): string {
    return actionType.replace(/_/g, ' ');
  }

  updatePagination(): void {
    this.totalPages = Math.ceil(this.filteredEntries.length / this.itemsPerPage);
    if (this.currentPage > this.totalPages && this.totalPages > 0) {
      this.currentPage = this.totalPages;
    }
  }

  get paginatedEntries(): ActionAuditEntry[] {
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

  // Format JSON state for display
  formatState(state: string | undefined): string {
    if (!state) return 'N/A';
    try {
      const parsed = JSON.parse(state);
      return JSON.stringify(parsed, null, 2);
    } catch {
      return state;
    }
  }
}
