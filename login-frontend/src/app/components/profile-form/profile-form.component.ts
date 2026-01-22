// ng g component components/profile-form

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
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
  authId: string = ''; // For EDITOR creating profile for another user
  editingOtherUser: boolean = false; // True when EDITOR is editing another user's profile
  creatingForOtherUser: boolean = false; // True when EDITOR is creating profile for another user
  errorMessage: string = '';
  successMessage: string = '';
  isSubmitting: boolean = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, this.nameValidator]],
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

  // Custom validator for name: allows letters, spaces, underscore, and up to 3 digits
  nameValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // Let required validator handle empty values
    }

    const value = control.value as string;
    
    // Check if value contains only allowed characters: letters, spaces, underscore, and digits
    const allowedPattern = /^[a-zA-Z\s_0-9]*$/;
    if (!allowedPattern.test(value)) {
      return { pattern: true };
    }

    // Count digits in the value
    const digitCount = (value.match(/\d/g) || []).length;
    
    // Check if digit count exceeds 3
    if (digitCount > 3) {
      return { maxDigits: true };
    }

    return null; // Valid
  }

  ngOnInit(): void {
    const routeUserId = this.route.snapshot.paramMap.get('userId');
    const routeAuthId = this.route.snapshot.paramMap.get('authId');
    const isEditor = this.authService.isEditor();

    if (routeAuthId && isEditor) {
      // EDITOR creating profile for another user
      this.creatingForOtherUser = true;
      this.isEdit = false;
      this.authId = routeAuthId;
      
      // Need to fetch email for the auth user
      this.loadAuthUserEmail(routeAuthId);
    } else if (routeUserId && isEditor) {
      // EDITOR editing another user's profile
      this.editingOtherUser = true;
      this.isEdit = true;
      this.userId = routeUserId;
      this.loadUserProfile(routeUserId);
    } else {
      // Normal flow: own profile creation/edit
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
  }

  loadAuthUserEmail(authId: string): void {
    // Fetch auth user email from admin service
    // We'll assume the email is passed or we need to fetch it
    // For now, we'll just enable the form without pre-filling email
    // The backend should handle the email association
  }

  loadUserProfile(userId: string): void {
    this.userService.getOwnProfile(userId).subscribe({
      next: (user) => {
        this.profileForm.patchValue({
          name: user.name,
          email: user.emailId || user.email,
          age: user.age,
          address: user.address,
          phone: user.phone,
          gender: user.gender,
          qualification: user.qualification,
          dob: user.dob,
          languages: user.languages
        });
        // Set email field (disabled)
        this.profileForm.get('email')!.setValue(user.emailId || user.email);
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Failed to load user profile';
        console.error('Error loading user profile:', error);
      }
    });
  }

  onSubmit(): void {
    // Prevent double submission
    if (this.isSubmitting) {
      return;
    }

    if (this.profileForm.valid) {
      this.isSubmitting = true;
      this.errorMessage = '';
      this.successMessage = '';

      if (this.isEdit) {
        this.userService.updateOwnProfile(this.userId, this.profileForm.value).subscribe({
          next: () => {
            console.log('Profile updated successfully');
            console.log(this.profileForm.value);
            this.successMessage = 'Profile updated successfully';
            setTimeout(() => {
              // Navigate based on context
              if (this.editingOtherUser) {
                this.router.navigate(['/editor/auth-users']);
              } else {
                this.router.navigate(['/profile']);
              }
              this.isSubmitting = false;
            }, 2000);
          },
          error: (error) => {
            this.errorMessage = error?.error?.message || 'Profile update failed';
            this.isSubmitting = false;
            console.error('Profile update error:', error);
          }
        });
      } else {
        // Check if creating for another user (EDITOR)
        if (this.creatingForOtherUser && this.authId) {
          this.userService.createProfileForUser(this.authId, this.profileForm.value).subscribe({
            next: () => {
              console.log('Profile created successfully for user');
              console.log(this.profileForm.value);
              this.successMessage = 'Profile created successfully';
              setTimeout(() => {
                this.router.navigate(['/editor/auth-users']);
                this.isSubmitting = false;
              }, 2000);
            },
            error: (error) => {
              this.errorMessage = error?.error?.message || 'Profile creation failed';
              this.isSubmitting = false;
              console.error('Profile creation error:', error);
            }
          });
        } else {
          // Self profile creation
          this.userService.createProfile(this.profileForm.value).subscribe({
            next: () => {
              localStorage.setItem('profileCreated', 'true');
              console.log('Profile created successfully');
              console.log(this.profileForm.value);
              this.successMessage = 'Profile created successfully';
              setTimeout(() => {
                this.router.navigate(['/profile']);
                this.isSubmitting = false;
              }, 2000);
            },
            error: (error) => {
              this.errorMessage = 'Profile creation failed';
              this.isSubmitting = false;
            }
          });
        }
      }
    }
  }
}