import {
  Routes
} from '@angular/router';

import {
  KnowledgeDocumentsComponent
} from './components/knowledge-documents/knowledge-documents.component';

import {
  OrdersComponent
} from './components/orders/orders.component';

import {
  AfterSalesComponent
} from './components/after-sales/after-sales.component';

import {
  TicketsComponent
} from './components/tickets/tickets.component';

import {
  LoginComponent
} from './components/login/login.component';

import {
  ShellComponent
} from './components/shell/shell.component';

import {
  authChildGuard,
  authGuard,
  guestGuard
} from './guards/auth.guard';

export const routes: Routes = [

  {
    path: 'login',
    component: LoginComponent,
    canActivate: [
      guestGuard
    ],
    title:
      '登录 - ServiceMind'
  },

  {
    path: '',
    component: ShellComponent,
    canActivate: [
      authGuard
    ],
    canActivateChild: [
      authChildGuard
    ],

    children: [

      {
        path: 'knowledge',
        component:
          KnowledgeDocumentsComponent,
        title:
          '知识库 - ServiceMind'
      },

      {
        path: 'orders',
        component:
          OrdersComponent,
        title:
          '订单中心 - ServiceMind'
      },

      {
        path: 'after-sales',
        component:
          AfterSalesComponent,
        title:
          '售后中心 - ServiceMind'
      },

      {
        path: 'tickets',
        component:
          TicketsComponent,
        title:
          '客服工单 - ServiceMind'
      },

      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'knowledge'
      }
    ]
  },

  {
    path: '**',
    redirectTo: 'knowledge'
  }
];