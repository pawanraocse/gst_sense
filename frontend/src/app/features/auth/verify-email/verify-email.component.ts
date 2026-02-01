import { Component, ElementRef, OnDestroy, OnInit, QueryList, ViewChildren } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { environment } from '../../../../environments/environment';

/**
 * Premium Email Verification Component.
 * Modern glassmorphism design with individual digit inputs and smooth animations.
 */
@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, ButtonModule, MessageModule],
  templateUrl: './verify-email.component.html',
  styleUrls: ['./verify-email.component.scss']
})
export class VerifyEmailComponent implements OnInit, OnDestroy {
  @ViewChildren('digitInput') digitInputs!: QueryList<ElementRef>;

  email: string = '';
  tenantId: string = '';
  codeDigits: string[] = ['', '', '', '', '', ''];
  verifying: boolean = false;
  resending: boolean = false;
  verified: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';
  cooldownRemaining: number = 0;
  redirectCountdown: number = 3;
  private cooldownInterval: any;
  private redirectInterval: any;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient
  ) { }

  ngOnInit(): void {
    // Get email and tenantId from router state
    this.email = history.state?.email || this.route.snapshot.queryParams['email'];
    this.tenantId = history.state?.tenantId || '';

    console.log('VerifyEmailComponent initialized:', { email: this.email, tenantId: this.tenantId });

    // If no email provided, redirect to signup
    if (!this.email) {
      console.log('No email found, redirecting to signup');
      this.router.navigate(['/auth/signup/personal']);
    }
  }

  isCodeComplete(): boolean {
    return this.codeDigits.every(digit => digit !== '');
  }

  getVerificationCode(): string {
    return this.codeDigits.join('');
  }

  onDigitInput(index: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = input.value;

    // Only allow digits
    if (!/^\d*$/.test(value)) {
      this.codeDigits[index] = '';
      return;
    }

    // Take only the last character if multiple
    this.codeDigits[index] = value.slice(-1);

    // Clear error on input
    this.errorMessage = '';

    // Move to next input
    if (value && index < 5) {
      this.focusInput(index + 1);
    }
  }

  onKeyDown(index: number, event: KeyboardEvent): void {
    // Handle backspace
    if (event.key === 'Backspace') {
      if (!this.codeDigits[index] && index > 0) {
        this.focusInput(index - 1);
        this.codeDigits[index - 1] = '';
      }
    }

    // Handle arrow keys
    if (event.key === 'ArrowLeft' && index > 0) {
      this.focusInput(index - 1);
    }
    if (event.key === 'ArrowRight' && index < 5) {
      this.focusInput(index + 1);
    }
  }

  onPaste(event: ClipboardEvent): void {
    event.preventDefault();
    const pastedData = event.clipboardData?.getData('text') || '';
    const digits = pastedData.replace(/\D/g, '').slice(0, 6);

    for (let i = 0; i < 6; i++) {
      this.codeDigits[i] = digits[i] || '';
    }

    // Focus the last filled or first empty input
    const lastFilledIndex = digits.length - 1;
    if (lastFilledIndex >= 0 && lastFilledIndex < 5) {
      this.focusInput(lastFilledIndex + 1);
    }
  }

  private focusInput(index: number): void {
    setTimeout(() => {
      const inputs = this.digitInputs.toArray();
      if (inputs[index]) {
        inputs[index].nativeElement.focus();
        inputs[index].nativeElement.select();
      }
    }, 0);
  }

  verifyEmail(): void {
    if (!this.isCodeComplete()) return;

    this.verifying = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload = {
      email: this.email,
      code: this.getVerificationCode(),
      tenantId: this.tenantId
    };

    this.http.post(`${environment.apiUrl}/auth/api/v1/auth/signup/verify`, payload)
      .subscribe({
        next: (response: any) => {
          this.verifying = false;
          this.verified = true;
          this.startRedirectCountdown();
        },
        error: (err) => {
          this.verifying = false;
          this.errorMessage = err.error?.message || 'Verification failed. Please try again.';
        }
      });
  }

  private startRedirectCountdown(): void {
    this.redirectCountdown = 3;
    this.redirectInterval = setInterval(() => {
      this.redirectCountdown--;
      if (this.redirectCountdown <= 0) {
        clearInterval(this.redirectInterval);
        this.router.navigate(['/auth/login'], {
          queryParams: { verified: 'true', email: this.email }
        });
      }
    }, 1000);
  }

  resendCode(): void {
    if (this.cooldownRemaining > 0) return;

    this.resending = true;
    this.errorMessage = '';

    this.http.post(`${environment.apiUrl}/auth/api/v1/auth/resend-verification`, { email: this.email })
      .subscribe({
        next: () => {
          this.resending = false;
          this.successMessage = 'Verification code sent!';
          this.startCooldown(60);
          // Clear success message after 3 seconds
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          this.resending = false;
          this.errorMessage = err.error?.message || 'Failed to resend code.';
        }
      });
  }

  private startCooldown(seconds: number): void {
    this.cooldownRemaining = seconds;

    if (this.cooldownInterval) {
      clearInterval(this.cooldownInterval);
    }

    this.cooldownInterval = setInterval(() => {
      this.cooldownRemaining--;
      if (this.cooldownRemaining <= 0) {
        clearInterval(this.cooldownInterval);
      }
    }, 1000);
  }

  ngOnDestroy(): void {
    if (this.cooldownInterval) {
      clearInterval(this.cooldownInterval);
    }
    if (this.redirectInterval) {
      clearInterval(this.redirectInterval);
    }
  }
}
