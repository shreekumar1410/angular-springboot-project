// ng g component components/editor-auth-users
// EDITOR can view auth users and create/edit profiles only

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AdminService } from '../../services/admin.service';
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
  selector: 'app-editor-auth-users',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './editor-auth-users.component.html',
  styleUrls: ['./editor-auth-users.component.css']
})
export class EditorAuthUsersComponent implements OnInit {
  authUsers: AuthUser[] = [];
  errorMessage: string = '';
  loading: boolean = false;

  constructor(
    private adminService: AdminService,
    private router: Router
  ) {}

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
        this.errorMessage = error?.error?.message || 'Failed to load auth users';
        this.loading = false;
        console.error('Failed to load auth users', error);
      }
    });
  }

  // Navigate to create profile page
  createProfile(authId: number): void {
    this.router.navigate(['/editor/profile-create', authId]);
  }

  // Navigate to edit profile page
  editProfile(userId: number): void {
    this.router.navigate(['/editor/profile-edit', userId]);
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
    return profileCreated ? 'badge-success' : 'badge-warning';
  }

  // Get profile status text
  getProfileStatusText(profileCreated: boolean): string {
    return profileCreated ? 'Created' : 'Not Created';
  }
}
