import {
  Component,
  computed,
  inject,
  signal
} from '@angular/core';

import {
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';

import {
  MatIconModule
} from '@angular/material/icon';

import {
  MatTooltipModule
} from '@angular/material/tooltip';

import {
  finalize
} from 'rxjs';

import {
  ChatComponent
} from '../chat/chat.component';

import {
  AuthService
} from '../../services/auth.service';

@Component({
  selector: 'app-shell',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatIconModule,
    MatTooltipModule,
    ChatComponent
  ],
  templateUrl:
    './shell.component.html',
  styleUrl:
    './shell.component.scss'
})
export class ShellComponent {

  readonly auth =
    inject(AuthService);

  private readonly router =
    inject(Router);

  readonly loggingOut =
    signal(false);

  readonly roleLabel =
    computed(() => {

      const role =
        this.auth.currentUser()?.role;

      return role === 'ADMIN'
        ? '系统管理员 · ADMIN'
        : '客服坐席 · AGENT';
    });

  logout(): void {

    if (this.loggingOut()) {
      return;
    }

    this.loggingOut.set(true);

    this.auth
      .logout()
      .pipe(
        finalize(() =>
          this.loggingOut.set(false)
        )
      )
      .subscribe({

        next: () => {
          this.router.navigateByUrl(
            '/login'
          );
        },

        error: () => {
          this.auth.clearSession();

          this.router.navigateByUrl(
            '/login'
          );
        }
      });
  }
}