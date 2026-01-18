// ng g component components/admin-auth-users

import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../services/admin.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-auth-users',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-auth-users.component.html',
  styleUrls: ['./admin-auth-users.component.css']
})
export class AdminAuthUsersComponent implements OnInit {
  authUsers: any[] = [];

  loggedInEmail = localStorage.getItem('email');

  // ADMIN can only change roles between USER and SUPPORT
  availableRoles = ['USER', 'SUPPORT'];

  constructor(private adminService: AdminService) {}

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
    
    console.log('Logged in email:', this.loggedInEmail);
  }

  loadAuthUsers(): void {
    this.adminService.getAuthUsers().subscribe({
      next: (data) => {
        this.authUsers = data;
      },
      error: (error) => {
        console.error('Failed to load auth users', error);
      }
    });
  }

  onRoleChange(authId: number, event: Event): void {
    if (this.loggedInEmail === this.authUsers.find(a => a.id === authId)?.email) {
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

    // ADMIN can only change USER <-> SUPPORT
    if (role !== 'USER' && role !== 'SUPPORT') {
      alert('ADMIN can only change roles between USER and SUPPORT');
      return;
    }

    this.adminService.changeUserRole(authId, role).subscribe({
      next: () => {
        alert('Role updated successfully');
        this.loadAuthUsers();
        console.log('Role updated successfully');
      },
      error: (err) => {
        alert(err?.error?.message || 'Failed to change role');
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

    // ADMIN can only activate/deactivate USER or SUPPORT
    if (targetUser?.role !== 'USER' && targetUser?.role !== 'SUPPORT') {
      alert('ADMIN can only activate/deactivate USER or SUPPORT accounts');
      return;
    }

    this.adminService.changeStatus(authId, active).subscribe({
      next: () => {
        alert('Status updated successfully');
        this.loadAuthUsers();
        console.log('Status updated successfully');
      },
      error: (error) => {
        alert(error?.error?.message || 'Failed to change status');
        console.error('Failed to change status', error);
      }
    });
  }
}