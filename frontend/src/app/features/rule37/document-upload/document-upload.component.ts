import { Component, output, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-document-upload',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule],
  template: `
    <div class="w-full space-y-4">
      <div class="buddy-info-card">
        <h3 class="font-semibold text-800 mb-3 flex align-items-center gap-2">
          <i class="pi pi-file-edit"></i>
          Ledger Format Requirements
        </h3>
        <div class="text-sm text-700 space-y-2">
          <p class="font-medium">Your Excel file must be a continuous ledger with these columns:</p>
          <ul class="list-disc list-inside space-y-1 ml-2">
            <li><span class="font-semibold">Date</span> - Transaction date</li>
            <li><span class="font-semibold">Debit</span> - Debit amount (output GST)</li>
            <li><span class="font-semibold">Credit</span> - Credit amount (input GST)</li>
            <li><span class="font-semibold">Name</span> - Supplier/Customer name</li>
          </ul>
          <p class="mt-3 text-600">
            The ledger should be continuous with no gaps or empty rows between entries.
          </p>
        </div>
        <button
          type="button"
          class="mt-4 text-sm font-medium text-primary cursor-pointer border-none bg-transparent underline"
          (click)="showSample.set(!showSample())"
        >
          {{ showSample() ? 'Hide' : 'View' }} Sample Ledger Format
        </button>
      </div>

      @if (showSample()) {
        <div class="surface-0 border-1 surface-border border-round p-4 overflow-x-auto">
          <h4 class="font-semibold text-900 mb-3">Sample Ledger</h4>
          <table class="w-full text-sm">
            <thead>
              <tr class="surface-100">
                <th class="border-1 surface-border px-4 py-2 text-left">Date</th>
                <th class="border-1 surface-border px-4 py-2 text-right">Debit</th>
                <th class="border-1 surface-border px-4 py-2 text-right">Credit</th>
                <th class="border-1 surface-border px-4 py-2 text-left">Name</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td class="border-1 surface-border px-4 py-2">01/01/2024</td>
                <td class="border-1 surface-border px-4 py-2 text-right">1800.00</td>
                <td class="border-1 surface-border px-4 py-2 text-right"></td>
                <td class="border-1 surface-border px-4 py-2">ABC Suppliers Ltd</td>
              </tr>
              <tr>
                <td class="border-1 surface-border px-4 py-2">02/01/2024</td>
                <td class="border-1 surface-border px-4 py-2 text-right"></td>
                <td class="border-1 surface-border px-4 py-2 text-right">900.00</td>
                <td class="border-1 surface-border px-4 py-2">XYZ Materials Co</td>
              </tr>
            </tbody>
          </table>
        </div>
      }

      <div>
        <input
          #fileInput
          type="file"
          accept=".xlsx,.xls"
          multiple
          class="hidden"
          [disabled]="disabled()"
          (change)="onFileChange($event)"
        />
        <button
          type="button"
          pButton
          label="Upload Supplier Ledger(s) (Excel)"
          icon="pi pi-upload"
          class="w-full"
          [disabled]="disabled()"
          (click)="fileInput.click()"
        ></button>
        <p class="mt-2 text-sm text-500 text-center">
          Accepted formats: .xlsx, .xls | You can upload multiple files
        </p>
      </div>
    </div>
  `,
  styles: [`
    .buddy-info-card {
      background: var(--buddy-teal-light);
      border: 1px solid var(--buddy-teal);
      border-radius: 12px;
      padding: 1.25rem;
    }
  `],
})
export class DocumentUploadComponent {
  filesSelected = output<File[]>();
  disabled = input<boolean>(false);
  showSample = signal(false);

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    const files = input.files;
    if (files && files.length > 0) {
      this.filesSelected.emit(Array.from(files));
    }
    input.value = '';
  }
}
