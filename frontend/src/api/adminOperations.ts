import { getAdminMutationHeaders } from './adminAuth'
const API=(import.meta.env.VITE_API_BASE_URL??'http://localhost:8080').replace(/\/$/,'')
export type InvoiceStatus='DRAFT'|'SENT'|'PART_PAID'|'PAID'|'VOID'
export type Invoice={id:string;jobId:string;jobNumber:string;invoiceNumber:string;version:number;status:InvoiceStatus;customerName:string;customerEmail:string;propertyAddress:string|null;subtotal:number;gstAmount:number;total:number;amountPaid:number;balanceDue:number;dueDate:string;sentAt:string|null;paidAt:string|null;paymentReference:string|null;notes:string|null;createdAt:string}
export type ReportSummary={activeEnquiries:number;acceptedQuotes:number;plannedJobs:number;scheduledJobs:number;inProgressJobs:number;completedJobs:number;overdueInvoices:number;acceptedQuoteValue:number;invoicedValue:number;collectedRevenue:number;outstandingRevenue:number}
export class OperationsApiError extends Error{status:number;constructor(message:string,status:number){super(message);this.name='OperationsApiError';this.status=status}}
async function err(r:Response,f:string){try{const b=await r.json() as{message?:string};return new OperationsApiError(b.message||f,r.status)}catch{return new OperationsApiError(f,r.status)}}
async function json<T>(path:string,init?:RequestInit,f='The request could not be completed.'){const r=await fetch(`${API}${path}`,{credentials:'include',...init});if(!r.ok)throw await err(r,f);return r.json() as Promise<T>}
export function listInvoices(){return json<Invoice[]>('/api/admin/invoices',undefined,'Invoices could not be loaded.')}
export function createInvoice(jobId:string){return getAdminMutationHeaders().then(headers=>json<Invoice>(`/api/admin/jobs/${encodeURIComponent(jobId)}/invoice`,{method:'POST',headers},'The invoice could not be created.'))}
export function updateInvoice(id:string,body:{status:InvoiceStatus;amountPaid:number;dueDate:string;paymentReference:string|null;notes:string|null;version:number}){return getAdminMutationHeaders().then(headers=>json<Invoice>(`/api/admin/invoices/${encodeURIComponent(id)}`,{method:'PATCH',headers,body:JSON.stringify(body)},'The invoice could not be saved.'))}
export async function downloadInvoicePdf(id:string){const r=await fetch(`${API}/api/admin/invoices/${encodeURIComponent(id)}/pdf`,{credentials:'include'});if(!r.ok)throw await err(r,'The invoice PDF could not be generated.');return r.blob()}
export function getReportSummary(){return json<ReportSummary>('/api/admin/reports/summary',undefined,'Reports could not be loaded.')}
