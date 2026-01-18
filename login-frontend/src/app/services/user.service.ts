// ng g service services/user

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'http://localhost:8080'; // Adjust as needed

  constructor(private http: HttpClient) {}

  // Profile management
  createProfile(profile: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/users/profile`, profile);
  }

  getCurrentUser(): Observable<any> {
    return this.http.get(`${this.apiUrl}/users/me`);
  }

  getOwnProfile(id: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/users/${id}`);
  }

  updateOwnProfile(id: string, profile: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/users/${id}`, profile);
  }

  // User list (short view for USER role)
  getUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/users`);
  }

  // Password reset request
  requestPasswordReset(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/user/password-reset-request?email=${email}`, {});
  }

  getUserLoginHistory(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/users/me/login-history`);
  }
}