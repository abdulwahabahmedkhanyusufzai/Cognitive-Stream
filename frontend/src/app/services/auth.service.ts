import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { catchError, map, tap } from 'rxjs/operators';
import { Observable, of, throwError } from 'rxjs';

export interface User {
  _id: string;
  email: string;
  username: string;
  image?: string;
  searchHistory?: any[];
  // Add other user properties as needed based on backend response
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private toastr = inject(ToastrService);

  private _user = signal<User | null>(null);
  private _isSigningUp = signal<boolean>(false);
  private _isLoggingIn = signal<boolean>(false);
  private _isLoggingOut = signal<boolean>(false);
  private _isCheckingAuth = signal<boolean>(true);

  public user = this._user.asReadonly();
  public isSigningUp = this._isSigningUp.asReadonly();
  public isLoggingIn = this._isLoggingIn.asReadonly();
  public isLoggingOut = this._isLoggingOut.asReadonly();
  public isCheckingAuth = this._isCheckingAuth.asReadonly();

  constructor() { }

  signup(credentials: any): Observable<any> {
    this._isSigningUp.set(true);
    return this.http.post<any>('/api/v1/auth/signup', credentials,{withCredentials: true}).pipe(
      tap((response) => {
        this._user.set(response.user);
        this._isSigningUp.set(false);
        this.toastr.success('Account created successfully');
      }),
      catchError((error) => {
        this._isSigningUp.set(false);
        this._user.set(null);
        this.toastr.error(error.error?.message || 'Signup failed');
        return throwError(() => error);
      })
    );
  }

  login(credentials: any): Observable<any> {
    this._isLoggingIn.set(true);
    return this.http.post<any>('/api/v1/auth/login', credentials,{withCredentials: true}).pipe(
      tap((response) => {
        this._user.set(response.user);
        this._isLoggingIn.set(false);
        this.toastr.success('Logged in successfully');
      }),
      catchError((error) => {
        this._isLoggingIn.set(false);
        this._user.set(null);
        this.toastr.error(error.error?.message || 'Login failed');
        return throwError(() => error);
      })
    );
  }

  logout(): Observable<any> {
    this._isLoggingOut.set(true);
    return this.http.post<any>('/api/v1/auth/logout', {}).pipe(
      tap(() => {
        this._user.set(null);
        this._isLoggingOut.set(false);
        this.toastr.success('Logged out successfully');
      }),
      catchError((error) => {
        this._isLoggingOut.set(false);
        this.toastr.error(error.error?.message || 'Logout failed');
        return throwError(() => error);
      })
    );
  }

  authCheck(): Observable<any> {
    this._isCheckingAuth.set(true);
    return this.http.get<any>('/api/v1/auth/authCheck',{
      withCredentials: true
    }).pipe(
      tap((response) => {
        this._user.set(response.user);
        this._isCheckingAuth.set(false);
      }),
      catchError((error) => {
        this._isCheckingAuth.set(false);
        this._user.set(null);
        // Silent fail for auth check usually, or toast
        // this.toastr.error(error.error?.message || 'An error occurred');
        return of(null);
      })
    );
  }
}
