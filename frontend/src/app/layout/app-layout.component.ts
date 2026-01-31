import { Component, computed, inject, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { MenubarModule } from 'primeng/menubar';
import { ButtonModule } from 'primeng/button';
import { AvatarModule } from 'primeng/avatar';
import { MenuItem } from 'primeng/api';
import { ShareDialogComponent } from '../shared/components/share-dialog/share-dialog.component';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, MenubarModule, ButtonModule, AvatarModule, ShareDialogComponent],
  templateUrl: './app-layout.component.html',
  styleUrls: ['./app-layout.component.scss']
})
export class AppLayoutComponent {
  authService = inject(AuthService);

  @ViewChild('shareDialog') shareDialog!: ShareDialogComponent;

  openShareDialog() {
    this.shareDialog.show();
  }

  items = computed<MenuItem[]>(() => {
    return [
      { label: 'Dashboard', icon: 'pi pi-home', routerLink: '/app/dashboard' },
      {
        label: 'Settings',
        icon: 'pi pi-cog',
        items: [
          { label: 'Account', icon: 'pi pi-user', routerLink: '/app/settings/account' }
        ]
      }
    ];
  });

  logout() {
    this.authService.logout();
  }
}
