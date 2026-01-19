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

  // Step 1: Accept password reset request (generates password, stores hash, status -> ACCEPTED)
  acceptPasswordReset(requestId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/support/password-reset/accept/${requestId}`, {});
  }

  // Step 2: Send password (updates user password, status -> PASSWORD_SENT)
  sendPassword(requestId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/support/password-reset/send/${requestId}`, {});
  }

  // Get login audit (read-only)
  getLoginAudit(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/support/login-audit`);
  }
}
