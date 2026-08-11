import {
  HttpClient,
  HttpErrorResponse
} from '@angular/common/http';

import {
  computed,
  inject,
  Injectable,
  signal
} from '@angular/core';

import {
  catchError,
  firstValueFrom,
  map,
  Observable,
  of,
  switchMap,
  tap
} from 'rxjs';

import {
  AuthUser,
  CsrfResponse,
  LoginRequest
} from '../types/auth.types';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http =
    inject(HttpClient);

  private readonly currentUserState =
    signal<AuthUser | null>(null);

  private readonly initializedState =
    signal(false);

  private initializationPromise:
    Promise<AuthUser | null> | null = null;

  readonly currentUser =
    this.currentUserState.asReadonly();

  readonly initialized =
    this.initializedState.asReadonly();

  readonly authenticated =
    computed(
      () => this.currentUserState() !== null
    );

  readonly isAdmin =
    computed(
      () =>
        this.currentUserState()?.role ===
        'ADMIN'
    );

  ensureInitialized():
      Promise<AuthUser | null> {

    if (this.initializedState()) {
      return Promise.resolve(
        this.currentUserState()
      );
    }

    if (this.initializationPromise) {
      return this.initializationPromise;
    }

    this.initializationPromise =
      firstValueFrom(
        this.http
          .get<AuthUser>('/api/auth/me')
          .pipe(
            switchMap(user =>
              this.fetchCsrf().pipe(
                map(() => user)
              )
            ),

            tap(user =>
              this.currentUserState.set(user)
            ),

            catchError(
              (error: unknown) => {

                if (
                  !(
                    error instanceof
                    HttpErrorResponse
                  ) ||
                  error.status !== 401
                ) {
                  console.error(
                    'Unable to restore authentication session.',
                    error
                  );
                }

                this.currentUserState.set(
                  null
                );

                return of(null);
              }
            )
          )
      )
      .finally(() => {
        this.initializedState.set(true);
      });

    return this.initializationPromise;
  }

  login(
      request: LoginRequest):
      Observable<AuthUser> {

    return this.fetchCsrf().pipe(

      switchMap(() =>
        this.http.post<AuthUser>(
          '/api/auth/login',
          request
        )
      ),

      switchMap(user =>
        this.fetchCsrf().pipe(
          map(() => user)
        )
      ),

      tap(user => {
        this.currentUserState.set(user);
        this.initializedState.set(true);

        this.initializationPromise =
          Promise.resolve(user);
      })
    );
  }

  logout(): Observable<void> {

    return this.fetchCsrf().pipe(

      switchMap(() =>
        this.http.post<void>(
          '/api/auth/logout',
          {}
        )
      ),

      tap(() => {
        this.clearSession();
      })
    );
  }

  clearSession(): void {

    this.currentUserState.set(null);
    this.initializedState.set(true);

    this.initializationPromise =
      Promise.resolve(null);
  }

  private fetchCsrf():
      Observable<CsrfResponse> {

    return this.http.get<CsrfResponse>(
      '/api/auth/csrf'
    );
  }
}