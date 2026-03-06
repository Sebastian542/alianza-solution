import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'clientes', pathMatch: 'full' },
  {
    path: 'clientes',
    loadComponent: () =>
      import('./clients/components/client-list/client-list.component')
        .then(m => m.ClientListComponent)
  }
];
