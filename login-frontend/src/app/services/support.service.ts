// ng g service services/support

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SupportService {
  private apiUrl = 'http://localhost:8080'; // Adjust as needed

  constructor(private http: HttpClient) {}

  // Get all password reset requests
  getPasswordResetRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/support/password-reset/requests`);
  }

  // Approve password reset request (generate password)
  approvePasswordReset(requestId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/support/password-reset/approve/${requestId}`, {});
  }

  // Get login audit (read-only)
  getLoginAudit(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/login-audit`);
  }
}
