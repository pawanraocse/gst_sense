import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { Rule37ApiService } from '../../../core/services/rule37-api.service';
import {
  Rule37RunResponse,
  LedgerResult,
} from '../../../shared/models/rule37.model';
import { ComplianceViewComponent } from '../compliance-view/compliance-view.component';

@Component({
  selector: 'app-calculation-history',
  standalone: true,
  imports: [CommonModule, ButtonModule, TooltipModule, ComplianceViewComponent],
  template: `
    <div class="space-y-6">
      <div class="flex justify-content-between align-items-center flex-wrap gap-2">
        <h2 class="text-2xl font-bold text-900 m-0 flex align-items-center gap-2">
          <i class="pi pi-history"></i>
          Saved Calculations
        </h2>
        <button
          pButton
          label="Refresh"
          icon="pi pi-refresh"
          [text]="true"
          (click)="loadRuns()"
          [loading]="loading()"
        ></button>
      </div>

      @if (loading()) {
        <div class="flex justify-content-center py-8">
          <i class="pi pi-spin pi-spinner" style="font-size: 3rem;"></i>
        </div>
      }

      @if (error()) {
        <div class="buddy-error-card p-4 flex align-items-start gap-3">
          <i class="pi pi-exclamation-triangle text-2xl"></i>
          <p class="m-0">{{ error() }}</p>
        </div>
      }

      @if (!loading() && runs().length === 0) {
        <div class="surface-50 border-1 surface-border border-round p-8 text-center">
          <i class="pi pi-folder-open text-6xl text-400 mb-4"></i>
          <h3 class="text-lg font-semibold text-900 mb-2">No saved calculations</h3>
          <p class="text-600 m-0">
            Your calculation results will appear here and be stored for 7 days.
          </p>
        </div>
      }

      @if (viewingRun(); as run) {
        <div class="space-y-6">
          <div class="flex justify-content-between align-items-center">
            <h2 class="text-2xl font-bold text-900 m-0 flex align-items-center gap-2">
              <i class="pi pi-eye"></i>
              Viewing: {{ run.filename }}
            </h2>
            <button
              pButton
              label="Back to List"
              (click)="viewingRun.set(null)"
            ></button>
          </div>
          <app-compliance-view
            [results]="normalizeResults(run.calculationData)"
            [runId]="run.id"
            [showExportAll]="true"
            (exportAll)="downloadExport(run.id, run.filename)"
            (exportLedger)="onExportLedger($event, run.id, run.filename)"
          />
        </div>
      } @else if (!loading() && runs().length > 0) {
        <div class="grid gap-4">
          @for (run of runs(); track run.id) {
            <div
              class="surface-0 border-1 surface-border border-round p-4 hover:shadow-2 transition-shadow cursor-pointer"
              (click)="viewingRun.set(run)"
            >
              <div class="flex justify-content-between align-items-start gap-4">
                <div class="flex-1">
                  <h3 class="font-semibold text-900 text-lg mb-2 m-0">{{ run.filename }}</h3>
                  <div class="grid">
                    <div class="col-6 md:col-3">
                      <span class="text-500 text-sm">Total Interest:</span>
                      <p class="font-semibold text-900 m-0">
                        {{ formatCurrency(run.totalInterest ?? 0) }}
                      </p>
                    </div>
                    <div class="col-6 md:col-3">
                      <span class="text-500 text-sm">ITC Reversal:</span>
                      <p class="font-semibold text-900 m-0">
                        {{ formatCurrency(run.totalItcReversal ?? 0) }}
                      </p>
                    </div>
                    <div class="col-6 md:col-3">
                      <span class="text-500 text-sm">Created:</span>
                      <p class="font-semibold text-900 m-0 text-sm">
                        {{ formatDate(run.createdAt) }}
                      </p>
                    </div>
                    <div class="col-6 md:col-3">
                      <span class="text-500 text-sm">Expires:</span>
                      <p
                        class="font-semibold m-0 text-sm"
                        [ngClass]="getDaysRemaining(run.expiresAt) <= 2 ? 'text-amber-600' : 'text-900'"
                      >
                        {{ getDaysRemaining(run.expiresAt) > 0
                          ? getDaysRemaining(run.expiresAt) + ' day(s) left'
                          : 'Expired' }}
                      </p>
                    </div>
                  </div>
                </div>
                <div class="flex gap-2">
                  <button
                    pButton
                    icon="pi pi-eye"
                    class="p-button-rounded p-button-text p-button-secondary"
                    pTooltip="View details"
                    (click)="viewingRun.set(run); $event.stopPropagation()"
                  ></button>
                  <button
                    pButton
                    icon="pi pi-download"
                    class="p-button-rounded p-button-text p-button-secondary"
                    pTooltip="Export"
                    (click)="downloadExport(run.id, run.filename); $event.stopPropagation()"
                  ></button>
                  <button
                    pButton
                    icon="pi pi-trash"
                    class="p-button-rounded p-button-text p-button-danger"
                    pTooltip="Delete"
                    (click)="deleteRun(run.id); $event.stopPropagation()"
                  ></button>
                </div>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .buddy-error-card {
      background: var(--buddy-amber-light);
      border: 1px solid var(--buddy-amber);
      border-radius: 12px;
    }
  `],
})
export class CalculationHistoryComponent {
  private api = inject(Rule37ApiService);

  runs = signal<Rule37RunResponse[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  viewingRun = signal<Rule37RunResponse | null>(null);

  constructor() {
    this.loadRuns();
  }

  loadRuns() {
    this.loading.set(true);
    this.error.set(null);
    this.api.listRuns(0, 20).subscribe({
      next: (page) => {
        this.runs.set(page.content);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.message || 'Failed to load calculations');
        this.loading.set(false);
      },
    });
  }

  deleteRun(id: number) {
    if (!confirm('Are you sure you want to delete this calculation?')) return;
    this.api.deleteRun(id).subscribe({
      next: () => {
        this.runs.update((list) => list.filter((r) => r.id !== id));
        if (this.viewingRun()?.id === id) {
          this.viewingRun.set(null);
        }
      },
      error: (err) => {
        alert('Failed to delete: ' + (err?.message || 'Unknown error'));
      },
    });
  }

  downloadExport(id: number, filename: string) {
    this.api.exportRun(id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = (filename || 'export') + '_Interest_Calculation.xlsx';
        a.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => {
        alert('Failed to export: ' + (err?.message || 'Unknown error'));
      },
    });
  }

  onExportLedger(
    _ev: { ledgerName: string; summary: LedgerResult['summary'] },
    runId: number,
    filename: string
  ) {
    this.downloadExport(runId, filename);
  }

  normalizeResults(data: LedgerResult[]): LedgerResult[] {
    if (!data) return [];
    return data.map((r) => ({
      ...r,
      summary: {
        ...r.summary,
        details: r.summary.details.map((d) => ({
          ...d,
          purchaseDate: typeof d.purchaseDate === 'string' ? d.purchaseDate : d.purchaseDate,
          paymentDate: d.paymentDate,
        })),
      },
    }));
  }

  formatDate(s: string): string {
    return new Date(s).toLocaleDateString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
  formatCurrency(n: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 2,
    }).format(n);
  }
  getDaysRemaining(expiresAt: string): number {
    const diff = new Date(expiresAt).getTime() - Date.now();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }
}
