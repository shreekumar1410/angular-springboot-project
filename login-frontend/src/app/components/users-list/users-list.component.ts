// ng g component components/users-list

import { Component, OnInit } from '@angular/core';
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
  isAdmin: boolean = false;
  loggedInEmail = localStorage.getItem('email');

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private adminService: AdminService
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.getUsers().subscribe({
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