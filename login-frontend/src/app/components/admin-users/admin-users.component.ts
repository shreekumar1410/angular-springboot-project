// ng g component components/admin-users

import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { AdminService } from '../../services/admin.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.css']
})
export class AdminUsersComponent implements OnInit {
  users: any[] = [];
  loggedInEmail = localStorage.getItem('email');

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private adminService: AdminService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.adminService.getUsers().subscribe({
      next: (data) => {
        this.users = data;
      },
      error: (error) => {
        console.error('Failed to load users', error);
      }
    });
  }

  deleteUser(userId: number) {
    if (confirm('Are you sure you want to delete this user?')){
      this.adminService.deleteUser(userId).subscribe({
        next: () => {
          alert('User deleted successfully');
          this.loadUsers();
          console.log('User deleted successfully');
          console.log('Logged in email during delete:', this.loggedInEmail);
        },
        error: (err) => {
          if (err.status === 403) {
            alert(err.error); // "You cannot delete your own profile"
          } else {
            alert('Something went wrong');
          }
        }
      });
  }
} 
}