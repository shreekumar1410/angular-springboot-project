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

  getAuthUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/admin/auth-users`);
  }

  changeUserRole(authId: number, role: string) {
  return this.http.put(`${this.apiUrl}/admin/users/${authId}/role`,{ role });
}

  changeStatus(authId: string, active: boolean): Observable<any> {
    return this.http.put(`${this.apiUrl}/admin/users/${authId}/status`, { active });
  }

  deleteUser(userId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/admin/users/${userId}`);
  }

  getLoginAudit(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/admin/login-audit`);
  }
}