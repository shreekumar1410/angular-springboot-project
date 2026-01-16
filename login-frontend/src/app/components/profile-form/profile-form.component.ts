// ng g component components/profile-form

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-profile-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile-form.component.html',
  styleUrls: ['./profile-form.component.css']
})
export class ProfileFormComponent implements OnInit {
  profileForm: FormGroup;
  isEdit: boolean = false;
  userId: string = '';
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private router: Router
  ) {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.pattern(/^[a-zA-Z\s]+$/)]],
      email: [{value: ''}, {disabled: true}],
      age: [''],
      address: ['', Validators.required],
      phone: ['', [Validators.required, Validators.pattern(/^[0-9+\-\s]+$/)]],
      gender: [''],
      qualification: [''],
      dob: ['', Validators.required],
      languages: ['']
    });
  }

  ngOnInit(): void {
    this.profileForm.get('email')!.setValue(localStorage.getItem('email'));
    this.isEdit = this.authService.isProfileCreated();
    if (this.isEdit) {
      this.userService.getCurrentUser().subscribe({
        next: (user) => {
          this.userId = user.id;
          this.profileForm.patchValue(user);
        },
        error: (error) => {
          this.errorMessage = 'Failed to load profile';
        }
      });
    }
  }

  onSubmit(): void {
    console.log(this.profileForm.value);
    if (this.profileForm.valid) {
      if (this.isEdit) {
        this.userService.updateOwnProfile(this.userId, this.profileForm.value).subscribe({
          next: () => {
            this.successMessage = 'Profile updated successfully';
            setTimeout(() => this.router.navigate(['/profile']), 2000);
          },
          error: (error) => {
            this.errorMessage = 'Profile update failed';
          }
        });
      } else {
        this.userService.createProfile(this.profileForm.value).subscribe({
          next: () => {
            localStorage.setItem('profileCreated', 'true');
            this.successMessage = 'Profile created successfully';
            setTimeout(() => this.router.navigate(['/profile']), 2000);
          },
          error: (error) => {
            this.errorMessage = 'Profile creation failed';
          }
        });
      }
    }
  }
}