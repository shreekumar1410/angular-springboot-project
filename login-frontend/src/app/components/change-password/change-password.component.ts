// Change Password Component
// Allows authenticated users to change their own password

import { Component, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent implements OnDestroy {
  changePasswordForm: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';
  isSubmitting: boolean = false;

  // Password visibility toggles
  showCurrentPassword: boolean = false;
  showNewPassword: boolean = false;
  showConfirmPassword: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.changePasswordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  // Custom validator to ensure new password matches confirm password
  passwordMatchValidator(form: AbstractControl) {
    const newPassword = form.get('newPassword');
    const confirmPassword = form.get('confirmPassword');
    
    if (newPassword && confirmPassword) {
      if (newPassword.value !== confirmPassword.value) {
        confirmPassword.setErrors({ ...confirmPassword.errors, mismatch: true });
        return { mismatch: true };
      } else {
        const errors = confirmPassword.errors;
        if (errors && errors['mismatch']) {
          delete errors['mismatch'];
          confirmPassword.setErrors(Object.keys(errors).length ? errors : null);
        }
      }
    }
    return null;
  }

  onSubmit(): void {
    // Clear previous messages
    this.errorMessage = '';
    this.successMessage = '';

    // Mark all fields as touched to show validation errors
    if (this.changePasswordForm.invalid) {
      Object.keys(this.changePasswordForm.controls).forEach(key => {
        this.changePasswordForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isSubmitting = true;

    const formValue = this.changePasswordForm.value;
    const changePasswordData = {
      currentPassword: formValue.currentPassword,
      newPassword: formValue.newPassword
    };

    // Security: Do NOT log passwords
    this.authService.changePassword(changePasswordData).subscribe({
      next: () => {
        this.successMessage = 'Password changed successfully';
        this.isSubmitting = false;
        
        // Clear form fields (security: remove sensitive data)
        this.changePasswordForm.reset();
        Object.keys(this.changePasswordForm.controls).forEach(key => {
          this.changePasswordForm.get(key)?.setErrors(null);
          this.changePasswordForm.get(key)?.markAsUntouched();
        });

        // Optionally redirect to profile after 2 seconds
        setTimeout(() => {
          this.router.navigate(['/profile']);
        }, 2000);
      },
      error: (error) => {
        this.isSubmitting = false;
        // Display backend error message exactly as received
        this.errorMessage = error?.error?.message || error?.error || 'Failed to change password';
        console.error('Password change error:', error);
      }
    });
  }

  togglePasswordVisibility(field: 'current' | 'new' | 'confirm'): void {
    switch (field) {
      case 'current':
        this.showCurrentPassword = !this.showCurrentPassword;
        break;
      case 'new':
        this.showNewPassword = !this.showNewPassword;
        break;
      case 'confirm':
        this.showConfirmPassword = !this.showConfirmPassword;
        break;
    }
  }

  clearError(): void {
    this.errorMessage = '';
  }

  // Security: Clear sensitive data on component destroy
  ngOnDestroy(): void {
    if (this.changePasswordForm) {
      this.changePasswordForm.reset();
    }
  }

  // Helper method to get role-based profile route
  getProfileRoute(): string {
    const role = this.authService.getRole();
    switch (role) {
      case 'USER':
        return '/user/profile';
      case 'SUPPORT':
        return '/support/profile';
      case 'ADMIN':
        return '/admin/profile';
      case 'SUPER_ADMIN':
        return '/super-admin/profile';
      default:
        return '/profile';
    }
  }
}
