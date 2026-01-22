// Unified Users List Component
// Handles USER, SUPPORT, ADMIN, and SUPER_ADMIN roles with role-based visibility

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { AdminService } from '../../services/admin.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-users-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './users-list.component.html',
  styleUrls: ['./users-list.component.css']
})
export class UsersListComponent implements OnInit {
  users: any[] = [];
  loggedInEmail = localStorage.getItem('email');
  currentRole: string | null = null;

  // Role flags
  isUser: boolean = false;
  isEditor: boolean = false;
  isSupport: boolean = false;
  isAdmin: boolean = false;
  isSuperAdmin: boolean = false;

  // Permission flags
  canViewFullDetails: boolean = false;
  canDelete: boolean = false;
  canEditProfile: boolean = false; // EDITOR can edit profiles
  // Note: canChangeRole and canActivateDeactivate require auth user data
  // These are handled in auth-users component

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private adminService: AdminService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeRole();
    this.loadUsers();
  }

  initializeRole(): void {
    this.currentRole = this.authService.getRole();
    
    // Set role flags
    this.isUser = this.authService.isUser();
    this.isEditor = this.authService.isEditor();
    this.isSupport = this.authService.isSupport();
    this.isAdmin = this.authService.isAdmin();
    this.isSuperAdmin = this.authService.isSuperAdmin();

    // Set permission flags based on role
    this.canViewFullDetails = this.isEditor || this.isSupport || this.isAdmin || this.isSuperAdmin;
    this.canDelete = this.isAdmin || this.isSuperAdmin;
    this.canEditProfile = this.isEditor; // EDITOR can edit user profiles
  }

  loadUsers(): void {
    // Use appropriate service based on role
    // EDITOR, ADMIN, and SUPER_ADMIN use admin service for full details
    const service = (this.isEditor || this.isAdmin || this.isSuperAdmin) 
      ? this.adminService.getUsers() 
      : this.userService.getUsers();

    service.subscribe({
      next: (data) => {
        this.users = data;
      },
      error: (error) => {
        console.error('Failed to load users', error);
      }
    });
  }

  // Phone masking for USER role
  maskPhone(phone: string | null | undefined): string {
    if (!phone || phone.length < 4) return 'N/A';
    // Backend already masks, but we'll handle it here too for safety
    if (phone.startsWith('XXXXXX')) {
      return phone; // Already masked by backend
    }
    return 'XXXXXX' + phone.substring(phone.length - 4);
  }

  // Get display phone based on role
  getDisplayPhone(phone: string | null | undefined): string {
    if (!phone) return 'N/A';
    if (this.isUser) {
      return this.maskPhone(phone);
    }
    return phone;
  }

  // Delete user (ADMIN can delete USER, SUPER_ADMIN can delete ADMIN and USER)
  // Note: Backend handles role-based deletion permissions
  deleteUser(userId: number, userEmail: string): void {
    // Check if trying to delete own profile
    if (userEmail === this.loggedInEmail) {
      alert('You cannot delete your own profile');
      return;
    }

    if (confirm('Are you sure you want to delete this user?')) {
      this.adminService.deleteUser(userId).subscribe({
        next: () => {
          alert('User deleted successfully');
          this.loadUsers();
        },
        error: (err) => {
          if (err.status === 403) {
            alert(err.error || 'You do not have permission to delete this user');
          } else {
            alert('Something went wrong');
          }
        }
      });
    }
  }


  // Get page title based on role
  getPageTitle(): string {
    if (this.isUser) return 'Users List';
    if (this.isEditor) return 'Manage Users';
    if (this.isSupport) return 'Users List';
    if (this.isAdmin) return 'Manage Users';
    if (this.isSuperAdmin) return 'Manage Users';
    return 'Users List';
  }

  // Check if EDITOR can edit this user's profile
  // EDITOR cannot edit ADMIN or SUPER_ADMIN profiles
  canEditUserProfile(user: any): boolean {
    if (!this.canEditProfile) return false;
    // EDITOR cannot edit ADMIN or SUPER_ADMIN profiles
    // Note: user object may not have role field, backend will validate
    return true; // Frontend allows, backend enforces
  }

  // Navigate to edit profile page
  editUserProfile(userId: number): void {
    // Navigate to editor profile edit route using Router
    this.router.navigate(['/editor/profile-edit', userId]);
  }
}
