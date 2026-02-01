import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { LedgerResult } from '../../../shared/models/rule37.model';

@Component({
  selector: 'app-compliance-view',
  standalone: true,
  imports: [CommonModule, ButtonModule, TableModule],
  template: `
    <div class="space-y-6">
      <div class="grid">
        <div class="col-12 md:col-6">
          <div class="buddy-card-amber p-4">
            <h3 class="text-lg font-semibold text-800 mb-2 flex align-items-center gap-2">
              <i class="pi pi-exclamation-triangle"></i>
              Total ITC Reversal (All Ledgers)
            </h3>
            <p class="text-3xl font-bold" style="color: var(--buddy-amber-dark);">
              {{ formatCurrency(totalItcReversal) }}
            </p>
          </div>
        </div>
        <div class="col-12 md:col-6">
          <div class="buddy-card-amber p-4">
            <h3 class="text-lg font-semibold text-800 mb-2 flex align-items-center gap-2">
              <i class="pi pi-percentage"></i>
              Total Interest Payable (All Ledgers)
            </h3>
            <p class="text-3xl font-bold" style="color: var(--buddy-amber-dark);">
              {{ formatCurrency(totalInterest) }}
            </p>
          </div>
        </div>
      </div>

      @if (results().length > 1 && runId() && showExportAll()) {
        <div class="flex justify-content-end">
          <button
            pButton
            label="Export All Ledgers to Excel"
            icon="pi pi-download"
            class="p-button-success"
            (click)="exportAll.emit()"
          ></button>
        </div>
      }

      @for (lr of results(); track lr.ledgerName) {
        <div class="surface-0 border-1 surface-border border-round overflow-hidden shadow-2">
          <div class="surface-50 border-bottom-1 surface-border px-4 py-3">
            <div class="flex justify-content-between align-items-center flex-wrap gap-2">
              <div>
                <h3 class="text-xl font-semibold text-900 m-0">{{ lr.ledgerName }}</h3>
                <p class="text-sm text-600 mt-1 m-0">
                  ITC Reversal: {{ formatCurrency(lr.summary.totalItcReversal) }} |
                  Interest: {{ formatCurrency(lr.summary.totalInterest) }}
                </p>
              </div>
              <button
                pButton
                label="Export"
                icon="pi pi-download"
                class="p-button-outlined"
                (click)="exportLedger.emit({ ledgerName: lr.ledgerName, summary: lr.summary })"
              ></button>
            </div>
          </div>

          <div class="overflow-x-auto">
            @for (group of getGroupedBySupplier(lr); track group.supplier) {
              <div class="border-bottom-1 surface-border last:border-bottom-0">
                <div class="buddy-supplier-header px-4 py-2">
                  <h4 class="font-semibold text-900 m-0">{{ group.supplier }}</h4>
                  <p class="text-sm text-600 m-0 mt-1">
                    {{ group.rows.length }} transaction(s) with delays beyond 180 days
                  </p>
                </div>
                <p-table [value]="group.rows" styleClass="p-datatable-sm p-datatable-striped">
                  <ng-template pTemplate="header">
                    <tr>
                      <th>Purchase Date</th>
                      <th>Payment Date</th>
                      <th class="text-right">Principal</th>
                      <th class="text-right">Delay (Days)</th>
                      <th class="text-right">ITC Amount</th>
                      <th class="text-right">Interest</th>
                      <th class="text-center">Status</th>
                    </tr>
                  </ng-template>
                  <ng-template pTemplate="body" let-row>
                    <tr [ngClass]="row.status === 'UNPAID' ? 'buddy-row-unpaid' : ''">
                      <td>{{ formatDate(row.purchaseDate) }}</td>
                      <td>{{ formatDate(row.paymentDate) }}</td>
                      <td class="text-right font-medium">{{ formatCurrency(row.principal) }}</td>
                      <td class="text-right">{{ row.delayDays }}</td>
                      <td class="text-right font-medium">{{ formatCurrency(row.itcAmount) }}</td>
                      <td class="text-right font-medium">{{ formatCurrency(row.interest) }}</td>
                      <td class="text-center">
                        <span
                          [class]="row.status === 'UNPAID' ? 'buddy-badge-unpaid' : 'buddy-badge-late'"
                        >
                          {{ row.status === 'UNPAID' ? 'Unpaid' : 'Paid Late' }}
                        </span>
                      </td>
                    </tr>
                  </ng-template>
                </p-table>
              </div>
            }
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .buddy-card-amber {
      background: var(--buddy-amber-light);
      border: 2px solid var(--buddy-amber);
      border-radius: 12px;
    }
    .buddy-supplier-header {
      background: var(--buddy-teal-light);
      border-bottom: 1px solid var(--buddy-teal);
    }
    .buddy-row-unpaid {
      background: var(--buddy-amber-light) !important;
    }
    .buddy-badge-unpaid {
      display: inline-block;
      padding: 0.25rem 0.5rem;
      border-radius: 9999px;
      font-size: 0.75rem;
      font-weight: 500;
      background: var(--buddy-amber-light);
      color: var(--buddy-amber-dark);
    }
    .buddy-badge-late {
      display: inline-block;
      padding: 0.25rem 0.5rem;
      border-radius: 9999px;
      font-size: 0.75rem;
      font-weight: 500;
      background: var(--buddy-teal-light);
      color: var(--buddy-teal-dark);
    }
  `],
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
