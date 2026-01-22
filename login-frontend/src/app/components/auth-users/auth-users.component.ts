// Unified Auth Users Component
// Handles EDITOR, ADMIN, and SUPER_ADMIN roles with role-based permissions

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

interface AuthUser {
  id: number;
  email: string;
  role: string;
  active: boolean;
  profileCreated: boolean;
  userId?: number;
}

@Component({
  selector: 'app-auth-users',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './auth-users.component.html',
  styleUrls: ['./auth-users.component.css']
})
export class AuthUsersComponent implements OnInit {
  authUsers: AuthUser[] = [];
  loggedInEmail = localStorage.getItem('email');
  currentRole: string | null = null;
  errorMessage: string = '';
  successMessage: string = '';
  loading: boolean = false;

  // Role flags
  isEditor: boolean = false;
  isAdmin: boolean = false;
  isSuperAdmin: boolean = false;

  // Available roles based on current user role
  availableRoles: string[] = [];

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentRole = this.authService.getRole();
    this.setRoleFlags();
    this.setAvailableRoles();
    this.loadAuthUsers();
  }

  setRoleFlags(): void {
    this.isEditor = this.currentRole === 'EDITOR';
    this.isAdmin = this.currentRole === 'ADMIN';
    this.isSuperAdmin = this.currentRole === 'SUPER_ADMIN';
  }

  setAvailableRoles(): void {
    if (this.isAdmin) {
      // ADMIN can only change roles between USER, SUPPORT, and EDITOR
      this.availableRoles = ['USER', 'SUPPORT', 'EDITOR'];
    } else if (this.isSuperAdmin) {
      // SUPER_ADMIN can change all roles
      this.availableRoles = ['USER', 'SUPPORT', 'ADMIN', 'SUPER_ADMIN'];
    }
    // EDITOR has no available roles (read-only for role management)
  }

  // Get roles to display in dropdown: current role first, then all other available roles
  getRolesForDropdown(currentRole: string): string[] {
    if (this.isEditor) {
      return [currentRole]; // EDITOR cannot change roles
    }
    
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

  loadAuthUsers(): void {
    this.loading = true;
    this.errorMessage = '';
    this.adminService.getAuthUsers().subscribe({
      next: (data) => {
        this.authUsers = data;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Failed to load auth users';
        this.loading = false;
        console.error('Failed to load auth users', error);
      }
    });
  }

  // EDITOR: Navigate to create profile page
  createProfile(authId: number): void {
    this.router.navigate(['/editor/profile-create', authId]);
  }

  // EDITOR: Navigate to edit profile page
  editProfile(userId: number): void {
    this.router.navigate(['/editor/profile-edit', userId]);
  }

  // ADMIN & SUPER_ADMIN: Handle role change
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

    // ADMIN can only change USER <-> SUPPORT <-> EDITOR
    if (this.isAdmin && (role === 'ADMIN' || role === 'SUPER_ADMIN')) {
      alert('ADMIN can only change roles between USER, SUPPORT and EDITOR');
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

  // ADMIN & SUPER_ADMIN: Handle status change
  changeStatus(authId: number, active: boolean): void {
    const targetUser = this.authUsers.find(a => a.id === authId);
    
    if (this.loggedInEmail === targetUser?.email) {
      alert('You cannot change your own status');
      return;
    }

    // ADMIN can only activate/deactivate USER or SUPPORT
    if (this.isAdmin && targetUser?.role !== 'USER' && targetUser?.role !== 'SUPPORT' && targetUser?.role !== 'EDITOR') {
      alert('ADMIN can only activate/deactivate USER, SUPPORT, or EDITOR accounts');
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

  // Check if user can modify role/status
  canModifyRole(user: AuthUser): boolean {
    if (this.isEditor) {
      return false; // EDITOR cannot modify roles
    }
    if (user.email === this.loggedInEmail) {
      return false; // Cannot modify own account
    }
    if (this.isAdmin) {
      // ADMIN cannot modify ADMIN or SUPER_ADMIN
      return user.role !== 'ADMIN' && user.role !== 'SUPER_ADMIN';
    }
    // SUPER_ADMIN can modify all except themselves
    return true;
  }

  // Check if user can modify status
  canModifyStatus(user: AuthUser): boolean {
    if (this.isEditor) {
      return false; // EDITOR cannot modify status
    }
    if (user.email === this.loggedInEmail) {
      return false; // Cannot modify own account
    }
    if (this.isAdmin) {
      // ADMIN can only modify USER, SUPPORT, EDITOR
      return user.role === 'USER' || user.role === 'SUPPORT' || user.role === 'EDITOR';
    }
    // SUPER_ADMIN can modify all except themselves
    return true;
  }

  // Get role badge class for styling
  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'USER':
        return 'badge-user';
      case 'EDITOR':
        return 'badge-editor';
      case 'SUPPORT':
        return 'badge-support';
      case 'ADMIN':
        return 'badge-admin';
      case 'SUPER_ADMIN':
        return 'badge-super-admin';
      default:
        return 'badge-secondary';
    }
  }

  // Get profile status badge class
  getProfileStatusClass(profileCreated: boolean): string {
    return profileCreated ? 'badge-yes' : 'badge-no';
  }

  // Get profile status text
  getProfileStatusText(profileCreated: boolean): string {
    return profileCreated ? 'Yes' : 'No';
  }

  // Get page title based on role
  getPageTitle(): string {
    if (this.isEditor) {
      return 'Auth Users - Profile Management';
    } else if (this.isAdmin) {
      return 'Auth Users';
    } else if (this.isSuperAdmin) {
      return 'Roles Management';
    }
    return 'Auth Users';
  }

  // Get page subtitle based on role
  getPageSubtitle(): string | null {
    if (this.isEditor) {
      return 'Create and edit user profiles';
    }
    return null;
  }
}
