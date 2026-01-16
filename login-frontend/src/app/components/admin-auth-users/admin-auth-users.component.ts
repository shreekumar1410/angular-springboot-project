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

  constructor(private adminService: AdminService) {}

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

  this.adminService.changeUserRole(authId, role).subscribe({
    next: () => {
        alert('Role updated successfully');
      this.loadAuthUsers();
      console.log('Role updated successfully');
    },
    error: (err) => {
      console.error('Failed to change role', err);
    }
  });
}



  changeStatus(authId: string, active: boolean): void {
    this.adminService.changeStatus(authId, active).subscribe({
      next: () => {
        alert('Status updated successfully');
        this.loadAuthUsers();
        console.log('Status updated successfully');
      },
      error: (error) => {
        console.error('Failed to change status', error);
      }
    });
  }
}