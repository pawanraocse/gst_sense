import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../core/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ToggleSwitchModule } from 'primeng/toggleswitch';

@Component({
  selector: 'app-account-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, ButtonModule, DialogModule, InputTextModule, ToastModule, ToggleSwitchModule],
  providers: [MessageService],
  templateUrl: './account-settings.component.html',
  styleUrls: ['./account-settings.component.scss']
})
export class AccountSettingsComponent {
  authService = inject(AuthService);
  private http = inject(HttpClient);
  private router = inject(Router);
  private messageService = inject(MessageService);

  deleting = signal(false);
  showDeleteDialog = false;
  confirmationText = '';

  // Mock preferences for UI
  preferences = {
    email_notifications: true,
    beta_features: false
  };

  deleteAccount(): void {
    if (this.confirmationText !== 'DELETE') return;

    this.deleting.set(true);
    // Assuming backend endpoint exists or will be updated. Using a generic one for now.
    // If backend endpoint for account deletion was platform-service based, this might need check.
    // But typically auth/account deletion is common.
    // Let's assume auth-service has it or we can stub it.
    // In simplified backend, maybe it's auth-service/api/v1/auth/user (DELETE)?
    // The previous code called `auth/api/v1/account/delete`. I'll keep it.

    this.http.post(`${environment.apiUrl}/auth/api/v1/account/delete`, {
      confirmation: this.confirmationText
    }).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Account Deleted', detail: 'Redirecting...' });
        setTimeout(() => this.authService.logout(), 2000);
      },
      error: (err) => {
        this.deleting.set(false);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to delete account.' });
      }
    });
  }
}
