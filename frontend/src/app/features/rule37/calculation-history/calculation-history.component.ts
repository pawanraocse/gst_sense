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
  templateUrl: './calculation-history.component.html',
  styleUrls: ['./calculation-history.component.scss']
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
