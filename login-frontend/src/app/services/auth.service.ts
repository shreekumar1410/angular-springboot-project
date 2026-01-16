// ng g service services/auth

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080'; // Adjust as needed

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: { email: string; password: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/login`, credentials);
  }

  register(credentials: { email: string; password: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/register`, credentials);
  }

  forgotPassword(email: { email: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/forgot-password`, email);
  }

  resetPassword(data: { token: string; newPassword: string }): Observable<any> {
    return this.http.put(`${this.apiUrl}/auth/reset-password`, data);
  }

  logout(): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/logout`, {});
  }

  clearLocalStorage(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('profileCreated');
    localStorage.removeItem('email');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getRole(): string | null {
    return localStorage.getItem('role');
  }

  isProfileCreated(): boolean {
    return localStorage.getItem('profileCreated') === 'true';
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  isUser(): boolean {
    return this.getRole() === 'USER';
  }
}