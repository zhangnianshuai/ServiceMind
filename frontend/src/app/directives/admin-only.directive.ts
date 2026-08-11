import {
  Directive,
  effect,
  inject,
  TemplateRef,
  ViewContainerRef
} from '@angular/core';

import {
  AuthService
} from '../services/auth.service';

@Directive({
  selector: '[appAdminOnly]',
  standalone: true
})
export class AdminOnlyDirective {

  private readonly auth =
    inject(AuthService);

  private readonly templateRef =
    inject(TemplateRef<unknown>);

  private readonly viewContainer =
    inject(ViewContainerRef);

  private rendered = false;

  constructor() {
    effect(() => {
      const isAdmin =
        this.auth.isAdmin();

      if (
        isAdmin &&
        !this.rendered
      ) {
        this.viewContainer.createEmbeddedView(
          this.templateRef
        );

        this.rendered = true;
        return;
      }

      if (
        !isAdmin &&
        this.rendered
      ) {
        this.viewContainer.clear();
        this.rendered = false;
      }
    });
  }
}