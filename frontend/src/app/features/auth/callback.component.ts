import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { fetchAuthSession } from 'aws-amplify/auth';
import { Hub } from 'aws-amplify/utils';
import { AuthService } from '../../core/auth.service';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-auth-callback',
  standalone: true,
  imports: [CommonModule, ProgressSpinnerModule, MessageModule, RouterModule],
  template: `
    <div class="callback-container">
      <div class="callback-content">
        @if (loading) {
          <p-progressSpinner styleClass="w-4rem h-4rem" strokeWidth="4"></p-progressSpinner>
          <h3 class="mt-4 text-900">Completing Sign In...</h3>
          <p class="text-600">Please wait while we verify your credentials.</p>
        }
        @if (error) {
          <p-message severity="error" [text]="error" styleClass="w-full mb-4"></p-message>
          <a routerLink="/auth/login" class="text-primary cursor-pointer">Back to Login</a>
        }
      </div>
    </div>
  `,
  styles: [`
    .callback-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--surface-ground);
    }
    .callback-content {
      text-align: center;
      padding: 2rem;
    }
  `]
})
export class AuthCallbackComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private hubListenerCancel: (() => void) | null = null;

  loading = true;
  error: string | null = null;

  ngOnInit() {
    // Listen for Hub auth events
    this.hubListenerCancel = Hub.listen('auth', async ({ payload }) => {
      if (payload.event === 'signInWithRedirect') {
        await this.completeSignIn();
      }
    });

    // Also verify session immediately
    this.completeSignIn();
  }

  ngOnDestroy() {
    if (this.hubListenerCancel) this.hubListenerCancel();
  }

  private async completeSignIn() {
    try {
      const session = await fetchAuthSession();
      if (session.tokens?.idToken) {
        const success = await this.authService.checkAuth();
        if (success) {
          this.router.navigate(['/app']);
        } else {
          this.error = 'Verification failed.';
          this.loading = false;
        }
      }
    } catch (e) {
      // Retry logic loops could go here, but kept simple
      // If it's just loading, we wait.
    }
  }
}
