import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router,RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule,RouterLink],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  emailForm: FormGroup;
  resetForm: FormGroup;
  resetToken: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;
  showResetForm: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.emailForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });

    this.resetForm = this.fb.group({
      newPassword: ['', Validators.required],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('newPassword');
    const confirmPassword = form.get('confirmPassword');
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword?.setErrors({ ...confirmPassword.errors, mismatch: true });
    } else {
      const errors = confirmPassword?.errors;
      if (errors && errors['mismatch']) {
        delete errors['mismatch'];
        confirmPassword?.setErrors(Object.keys(errors).length ? errors : null);
      }
    }
  }

  onEmailSubmit(): void {
    if (this.emailForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
  
      this.authService.forgotPassword(this.emailForm.value).subscribe({
        next: () => {
          // ✅ Do NOT expect any response body
          this.isLoading = false;
  
          // show success message instead of reset form
          this.successMessage =
            'Password reset request submitted successfully. Please contact support.';
  
          // optional: disable form or redirect
          this.emailForm.reset();
        },
        error: (error) => {
          this.errorMessage =
            error?.error?.message || 'Failed to send reset email';
          this.isLoading = false;
        }
      });
    }
  }
  


  onResetSubmit(): void {
    if (this.resetForm.valid && this.resetToken) {
      this.isLoading = true;
      this.errorMessage = '';
      const data = {
        token: this.resetToken,
        newPassword: this.resetForm.value.newPassword
      };
      this.authService.resetPassword(data).subscribe({
        next: (response) => {
          this.successMessage = response.message;
          setTimeout(() => this.router.navigate(['/login']), 2000);
          this.isLoading = false;
        },
        error: (error) => {
          this.errorMessage = error?.error?.message || 'Failed to reset password';
          this.isLoading = false;
        }
      });
    }
  }

  clearError(): void {
    this.errorMessage = '';
  }
}
