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

  createProfile(profile: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/api/users`, profile);
  }

  getCurrentUser(): Observable<any> {
    return this.http.get(`${this.apiUrl}/api/users/me`);
  }

  getOwnProfile(id: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/api/users/${id}`);
  }

  updateOwnProfile(id: string, profile: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/api/users/${id}`, profile);
  }

  getUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/api/users`);
  }

  getUserLoginHistory(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/api/users/me/login-history`);
  }
}