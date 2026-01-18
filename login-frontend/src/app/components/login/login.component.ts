// ng g component components/login

import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage: string = '';
  showPassword: boolean = false;
  showError: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.authService.login(this.loginForm.value).subscribe({
        next: (response) => {
          localStorage.setItem('token', response.token);
          localStorage.setItem('role', response.role);
          localStorage.setItem('profileCreated', response.profileCreated.toString());
          localStorage.setItem('email', this.loginForm.value.email);

          // Store login alert message in sessionStorage if present
          if (response.loginAlert && response.loginAlert.message) {
            sessionStorage.setItem('loginAlertMessage', response.loginAlert.message);
          }

          if (!response.profileCreated) {
            this.router.navigate(['/profile/form']);
          } else {
            // Role-based redirection
            const role = response.role;
            switch (role) {
              case 'USER':
                this.router.navigate(['/user/dashboard']);
                break;
              case 'SUPPORT':
                this.router.navigate(['/support/dashboard']);
                break;
              case 'ADMIN':
                this.router.navigate(['/admin/dashboard']);
                break;
              case 'SUPER_ADMIN':
                this.router.navigate(['/super-admin/dashboard']);
                break;
              default:
                this.router.navigate(['/login']);
            }
          }
        },
        error: (err) => {
          this.errorMessage = err?.error?.message || 'Something went wrong';
          this.showError = false;
          setTimeout(() => this.showError = true, 100);
        }
      });
    }
  }
}