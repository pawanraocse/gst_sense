import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { LedgerResult } from '../../../shared/models/rule37.model';

@Component({
  selector: 'app-compliance-view',
  standalone: true,
  imports: [CommonModule, ButtonModule, TableModule],
  templateUrl: './compliance-view.component.html',
  styleUrls: ['./compliance-view.component.scss']
})
export class ComplianceViewComponent {
  results = input.required<LedgerResult[]>();
  runId = input<number | null>(null);
  showExportAll = input<boolean>(true);
  exportAll = output<void>();
  exportLedger = output<{ ledgerName: string; summary: LedgerResult['summary'] }>();

  get totalItcReversal(): number {
    return this.results().reduce((s, r) => s + r.summary.totalItcReversal, 0);
  }
  get totalInterest(): number {
    return this.results().reduce((s, r) => s + r.summary.totalInterest, 0);
  }

  getGroupedBySupplier(lr: LedgerResult): { supplier: string; rows: typeof lr.summary.details }[] {
    const map = new Map<string, typeof lr.summary.details>();
    for (const row of lr.summary.details) {
      const list = map.get(row.supplier) ?? [];
      list.push(row);
      map.set(row.supplier, list);
    }
    return Array.from(map.entries()).map(([supplier, rows]) => ({ supplier, rows }));
  }

  formatDate(d: string | null): string {
    if (!d) return 'Unpaid';
    return new Date(d).toLocaleDateString('en-IN');
  }
  formatCurrency(n: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 2,
    }).format(n);
  }
}
