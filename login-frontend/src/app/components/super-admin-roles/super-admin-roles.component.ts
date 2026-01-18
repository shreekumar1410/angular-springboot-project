// Super Admin Roles Component

import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-super-admin-roles',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './super-admin-roles.component.html',
  styleUrls: ['./super-admin-roles.component.css']
})
export class SuperAdminRolesComponent implements OnInit {
  authUsers: any[] = [];
  loggedInEmail = localStorage.getItem('email');
  errorMessage: string = '';
  successMessage: string = '';
  loading: boolean = false;

  // Available roles for SUPER_ADMIN (can change ADMIN roles)
  availableRoles = ['USER', 'SUPPORT', 'ADMIN', 'SUPER_ADMIN'];

  constructor(
    private adminService: AdminService,
    private authService: AuthService
  ) {}

  // Get roles to display in dropdown: current role first, then all other available roles
  getRolesForDropdown(currentRole: string): string[] {
    // Start with current role
    const roles = [currentRole];
    
    // Add all other roles except the current one
    this.availableRoles.forEach(role => {
      if (role !== currentRole) {
        roles.push(role);
      }
    });
    
    return roles;
  }

  ngOnInit(): void {
    this.loadAuthUsers();
  }

  loadAuthUsers(): void {
    this.loading = true;
    this.errorMessage = '';
    this.adminService.getAuthUsers().subscribe({
      next: (data) => {
        this.authUsers = data;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load auth users';
        this.loading = false;
        console.error('Failed to load auth users', error);
      }
    });
  }

  onRoleChange(authId: number, event: Event): void {
    const targetUser = this.authUsers.find(a => a.id === authId);
    
    if (this.loggedInEmail === targetUser?.email) {
      alert('You cannot change your own role');
      return;
    }

    const selectElement = event.target as HTMLSelectElement | null;
    if (!selectElement) {
      console.error('Select element not found');
      return;
    }

    const role = selectElement.value;
    if (!authId) {
      console.error('authId is undefined');
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.adminService.changeUserRole(authId, role).subscribe({
      next: () => {
        this.successMessage = 'Role updated successfully';
        this.loadAuthUsers();
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Failed to change role';
        this.loading = false;
        console.error('Failed to change role', err);
      }
    });
  }

  changeStatus(authId: number, active: boolean): void {
    const targetUser = this.authUsers.find(a => a.id === authId);
    
    if (this.loggedInEmail === targetUser?.email) {
      alert('You cannot change your own status');
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.adminService.changeStatus(authId, active).subscribe({
      next: () => {
        this.successMessage = 'Status updated successfully';
        this.loadAuthUsers();
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Failed to change status';
        this.loading = false;
        console.error('Failed to change status', error);
      }
    });
  }

  canModify(user: any): boolean {
    // SUPER_ADMIN can modify ADMIN, SUPPORT, and USER roles
    // Cannot modify own account
    return user.email !== this.loggedInEmail;
  }
}
