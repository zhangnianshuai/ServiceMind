import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';

import {
  inject
} from '@angular/core';

import {
  Router
} from '@angular/router';

import {
  catchError,
  throwError
} from 'rxjs';

import {
  AuthService
} from '../services/auth.service';

export const authInterceptor:
    HttpInterceptorFn =
  (request, next) => {

    const auth =
      inject(AuthService);

    const router =
      inject(Router);

    return next(request).pipe(

      catchError(
        (error: unknown) => {

          if (
            error instanceof HttpErrorResponse &&
            error.status === 401 &&
            !request.url.includes(
              '/api/auth/login'
            ) &&
            !request.url.includes(
              '/api/auth/me'
            ) &&
            !request.url.includes(
              '/api/auth/csrf'
            )
          ) {
            auth.clearSession();

            void router.navigate(
              ['/login'],
              {
                queryParams: {
                  returnUrl:
                    router.url
                }
              }
            );
          }

          return throwError(
            () => error
          );
        }
      )
    );
  };