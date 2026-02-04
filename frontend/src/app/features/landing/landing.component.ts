import { Component, signal, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { AccordionModule } from 'primeng/accordion';
import { RippleModule } from 'primeng/ripple';

@Component({
    selector: 'app-landing',
    standalone: true,
    imports: [CommonModule, RouterLink, ButtonModule, AccordionModule, RippleModule],
    templateUrl: './landing.component.html',
    styleUrl: './landing.component.scss'
})
export class LandingComponent {
    isScrolled = signal(false);
    mobileMenuOpen = signal(false);

    // Supported GST Rules - the core value prop
    gstRules = [
        {
            image: 'assets/images/landing/icons/rule-37.png',
            rule: 'Rule 37',
            title: '180-Day ITC Reversal',
            description: 'Auto-calculate ITC reversals for unpaid invoices exceeding 180 days.'
        },
        {
            image: 'assets/images/landing/icons/rule-36-4.png',
            rule: 'Rule 36(4)',
            title: 'ITC Matching',
            description: 'Match claimed ITC with GSTR-2A/2B and identify mismatches instantly.'
        },
        {
            image: 'assets/images/landing/icons/rule-86b.png',
            rule: 'Rule 86B',
            title: 'Credit Restriction',
            description: 'Monitor the 99% credit utilization threshold automatically.'
        },
        {
            image: 'assets/images/landing/icons/rule-16-4.png',
            rule: 'Rule 16(4)',
            title: 'ITC Time Limit',
            description: 'Track ITC claims against due dates to avoid lapses.'
        },
        {
            image: 'assets/images/landing/icons/gstr-9.png',
            rule: 'GSTR-9',
            title: 'Annual Return',
            description: 'Reconcile data for annual return filing with precision.'
        },
        {
            image: 'assets/images/landing/icons/gstr-3b.png',
            rule: 'GSTR-3B',
            title: 'Non-Filer Detection',
            description: 'Identify vendors who haven\'t filed returns — protect your ITC.'
        }
    ];

    // Features focusing on platform benefits
    features = [
        {
            image: 'assets/images/landing/features/one-upload.png',
            title: 'One Upload, All Rules',
            description: 'Upload your ledger once. Get compliance status for all GST rules in seconds — no manual hopping between tools.'
        },
        {
            image: 'assets/images/landing/features/peace-of-mind.png',
            title: 'Peace of Mind Dashboard',
            description: '"All Clear" or "Action Required" — a single glance tells you everything. No more spreadsheet nightmares.'
        },
        {
            image: 'assets/images/landing/features/always-updated.png',
            title: 'Always Up-to-Date',
            description: 'New CBIC notifications? We update the rules automatically. You just focus on your business.'
        },
        {
            image: 'assets/images/landing/features/security.png',
            title: 'Bank-Grade Security',
            description: 'End-to-end encryption. AWS infrastructure. Your financial data never leaves secure servers.'
        }
    ];

    // How it works steps
    steps = [
        {
            number: 1,
            icon: 'pi-download',
            title: 'Export Your Data',
            description: 'Export your party ledger or GSTR data from Tally, Busy, or GST Portal.'
        },
        {
            number: 2,
            icon: 'pi-cloud-upload',
            title: 'Upload to Buddy',
            description: 'Drop your file. Our AI scans it against all applicable GST rules instantly.'
        },
        {
            number: 3,
            icon: 'pi-chart-line',
            title: 'Get Your Report',
            description: 'See flagged issues, reversal amounts, interest due — and export professional reports.'
        }
    ];

    // Comparison with manual/other tools
    comparisons = [
        { feature: 'Single upload for all rules', us: true, others: false },
        { feature: 'Automatic rule updates', us: true, others: false },
        { feature: 'Peace of Mind Dashboard', us: true, others: false },
        { feature: 'No CAPTCHA hassles', us: true, others: false },
        { feature: 'Excel report export', us: true, others: true },
        { feature: 'Works with Tally & Busy', us: true, others: true }
    ];

    faqs = [
        {
            question: 'What GST rules does GST Buddy support?',
            answer: 'We support all major compliance rules including Rule 37 (180-day reversal), Rule 36(4) (ITC matching), Rule 86B (credit restriction), Rule 16(4) (ITC time limit), GSTR-9 reconciliation, and GSTR-3B vendor filing checks. New rules are added as CBIC releases notifications.'
        },
        {
            question: 'Is GST Buddy free to try?',
            answer: 'Yes! Sign up for free and get 5 full compliance checks at no cost — no credit card required.'
        },
        {
            question: 'What file formats do you support?',
            answer: 'Excel exports from Tally, Busy, or any party ledger in .xlsx/.xls format. We also support JSON exports from the GST Portal.'
        },
        {
            question: 'How is this different from GST Doctor or other tools?',
            answer: 'Unlike fragmented tools that make you run separate checks for each rule, GST Buddy provides a unified dashboard. One upload, all rules checked. Plus our premium UI makes compliance actually pleasant.'
        },
        {
            question: 'Is my data secure?',
            answer: 'Absolutely. We use bank-grade encryption, AWS infrastructure, and never share your data. Your files are processed and deleted — we don\'t store sensitive financial information.'
        },
        {
            question: 'Can I export reports for my clients?',
            answer: 'Yes! Export professional, white-labeled reports in Excel or PDF format — perfect for CA firms and tax consultants.'
        }
    ];

    @HostListener('window:scroll')
    onScroll(): void {
        this.isScrolled.set(window.scrollY > 50);
    }

    toggleMobileMenu(): void {
        this.mobileMenuOpen.update(v => !v);
    }

    scrollTo(sectionId: string): void {
        this.mobileMenuOpen.set(false);
        const element = document.getElementById(sectionId);
        if (element) {
            element.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }
}
