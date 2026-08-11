import {
  Component,
  inject,
  signal
} from '@angular/core';

import {
  HttpErrorResponse
} from '@angular/common/http';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  finalize
} from 'rxjs';

import {
  MatFormFieldModule
} from '@angular/material/form-field';

import {
  MatInputModule
} from '@angular/material/input';

import {
  MatIconModule
} from '@angular/material/icon';

import {
  MatButtonModule
} from '@angular/material/button';

import {
  AuthService
} from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl:
    './login.component.html',
  styleUrl:
    './login.component.scss'
})
export class LoginComponent {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly auth =
    inject(AuthService);

  private readonly router =
    inject(Router);

  private readonly route =
    inject(ActivatedRoute);

  readonly submitting =
    signal(false);

  readonly errorMessage =
    signal<string | null>(null);

  readonly showPassword =
    signal(false);

  readonly form =
    this.formBuilder.nonNullable.group({
      username: [
        '',
        [
          Validators.required,
          Validators.maxLength(64)
        ]
      ],

      password: [
        '',
        [
          Validators.required,
          Validators.maxLength(200)
        ]
      ]
    });

  togglePassword(): void {
    this.showPassword.update(
      current => !current
    );
  }

  submit(): void {

    if (
      this.submitting() ||
      this.form.invalid
    ) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.auth
      .login(
        this.form.getRawValue()
      )
      .pipe(
        finalize(() =>
          this.submitting.set(false)
        )
      )
      .subscribe({

        next: () => {
          this.router.navigateByUrl(
            this.resolveReturnUrl()
          );
        },

        error: (
            error:
              HttpErrorResponse
        ) => {

          if (error.status === 401) {
            this.errorMessage.set(
              '用户名或密码错误，请重新输入。'
            );
            return;
          }

          this.errorMessage.set(
            '登录失败，请确认后端服务正常后重试。'
          );
        }
      });
  }

  private resolveReturnUrl():
      string {

    const returnUrl =
      this.route.snapshot
        .queryParamMap
        .get('returnUrl');

    if (
      returnUrl &&
      returnUrl.startsWith('/') &&
      !returnUrl.startsWith('//') &&
      returnUrl !== '/login'
    ) {
      return returnUrl;
    }

    return '/knowledge';
  }
}