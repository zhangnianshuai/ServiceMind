import {
  inject
} from '@angular/core';

import {
  CanActivateChildFn,
  CanActivateFn,
  Router
} from '@angular/router';

import {
  AuthService
} from '../services/auth.service';

function requireAuthentication(
    targetUrl: string) {

  const auth =
    inject(AuthService);

  const router =
    inject(Router);

  return auth
    .ensureInitialized()
    .then(user => {

      if (user) {
        return true;
      }

      return router.createUrlTree(
        ['/login'],
        {
          queryParams: {
            returnUrl: targetUrl
          }
        }
      );
    });
}

export const authGuard:
    CanActivateFn =
  (_route, state) =>
    requireAuthentication(
      state.url
    );

export const authChildGuard:
    CanActivateChildFn =
  (_route, state) =>
    requireAuthentication(
      state.url
    );

export const guestGuard:
    CanActivateFn =
  () => {

    const auth =
      inject(AuthService);

    const router =
      inject(Router);

    return auth
      .ensureInitialized()
      .then(user => {

        if (!user) {
          return true;
        }

        return router.createUrlTree(
          ['/knowledge']
        );
      });
  };