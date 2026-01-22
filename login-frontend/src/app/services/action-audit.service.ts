// ng g service services/action-audit

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ActionAuditEntry {
  actorEmail: string;
  actorRole: string;
  targetUserEmail: string;
  actionType: string; // ROLE_CHANGE | PROFILE_CREATE | PROFILE_UPDATE | PASSWORD_RESET | USER_DELETE | ACCOUNT_ACTIVATE | ACCOUNT_DEACTIVATE
  actionStatus: string; // SUCCESS | FAILED
  actionReason?: string;
  beforeState?: string;
  afterState?: string;
  performedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class ActionAuditService {
  private apiUrl = 'http://localhost:8080/super-admin/action-audit';

  constructor(private http: HttpClient) {}

  // Get all action audit records
  getAllActionAudit(): Observable<ActionAuditEntry[]> {
    return this.http.get<ActionAuditEntry[]>(this.apiUrl);
  }

  // Get action audit by type
  getActionAuditByType(type: string): Observable<ActionAuditEntry[]> {
    return this.http.get<ActionAuditEntry[]>(`${this.apiUrl}/type/${type}`);
  }

  // Get action audit by status
  getActionAuditByStatus(status: string): Observable<ActionAuditEntry[]> {
    return this.http.get<ActionAuditEntry[]>(`${this.apiUrl}/status/${status}`);
  }

  // Get action audit by type and status
  getActionAuditByFilter(type: string, status: string): Observable<ActionAuditEntry[]> {
    const params = new HttpParams()
      .set('type', type)
      .set('status', status);
    return this.http.get<ActionAuditEntry[]>(`${this.apiUrl}/filter`, { params });
  }

  // Get action audit by date range
  getActionAuditByDateRange(from: string, to: string): Observable<ActionAuditEntry[]> {
    const params = new HttpParams()
      .set('from', from)
      .set('to', to);
    return this.http.get<ActionAuditEntry[]>(`${this.apiUrl}/date-range`, { params });
  }
}
