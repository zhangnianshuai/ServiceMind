import { Routes } from '@angular/router';

import {
  KnowledgeDocumentsComponent
} from './components/knowledge-documents/knowledge-documents.component';
import {
  OrdersComponent
} from './components/orders/orders.component';

export const routes: Routes = [
  {
    path: 'knowledge',
    component: KnowledgeDocumentsComponent,
    title: '知识库 - ServiceMind'
  },
  {
    path: 'orders',
    component: OrdersComponent,
    title: '订单中心 - ServiceMind'
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'knowledge'
  },
  {
    path: '**',
    redirectTo: 'knowledge'
  }
];