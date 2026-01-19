// ng g service services/admin

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8080'; // Adjust as needed

  constructor(private http: HttpClient) {}

  // Get all users (full details)
  getUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/users`);
  }

  // Get auth users
  getAuthUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/admin/auth-users`);
  }

  // Change role (USER <-> SUPPORT for ADMIN, can change ADMIN roles for SUPER_ADMIN)
  changeUserRole(authId: number, role: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/admin/change-role/${authId}`, { role });
  }

  // Change status (activate/deactivate)
  changeStatus(authId: number, active: boolean): Observable<any> {
    return this.http.put(`${this.apiUrl}/admin/change-status/${authId}`, { active });
  }

  // Delete user profile
  deleteUser(userId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/admin/users/${userId}`);
  }

  // Get password reset audit (read-only)
  getPasswordResetAudit(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/admin/password-reset-audit`);
  }

  // Get login audit (for SUPER_ADMIN only)
  getLoginAudit(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/support/login-audit`);
  }
}