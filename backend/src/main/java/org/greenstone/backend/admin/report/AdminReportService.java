package org.greenstone.backend.admin.report;
import org.greenstone.backend.persistence.entity.*;
import org.greenstone.backend.persistence.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class AdminReportService {
 private final EnquiryRepository enquiries; private final QuoteRepository quotes; private final PaintingJobRepository jobs; private final InvoiceRepository invoices;
 public AdminReportService(EnquiryRepository e,QuoteRepository q,PaintingJobRepository j,InvoiceRepository i){enquiries=e;quotes=q;jobs=j;invoices=i;}
 @Transactional(readOnly=true) public AdminReportSummaryResponse summary(){var es=enquiries.findAll();var qs=quotes.findAll();var js=jobs.findAll();var is=invoices.findAll();var active=es.stream().filter(e->switch(e.getStatus()){case NEW,IN_REVIEW,CONTACTED,QUOTED->true;default->false;}).count();var accepted=qs.stream().filter(q->q.getStatus()==QuoteStatus.ACCEPTED).toList();var today=LocalDate.now(ZoneId.of("Pacific/Auckland"));var overdue=is.stream().filter(i->i.getDueDate().isBefore(today)&&i.getStatus()!=InvoiceStatus.PAID&&i.getStatus()!=InvoiceStatus.VOID).count();var acceptedValue=accepted.stream().map(Quote::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);var invoiced=is.stream().filter(i->i.getStatus()!=InvoiceStatus.VOID).map(Invoice::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);var paid=is.stream().filter(i->i.getStatus()!=InvoiceStatus.VOID).map(Invoice::getAmountPaid).reduce(BigDecimal.ZERO,BigDecimal::add);return new AdminReportSummaryResponse(active,accepted.size(),count(js,JobStatus.PLANNED),count(js,JobStatus.SCHEDULED),count(js,JobStatus.IN_PROGRESS),count(js,JobStatus.COMPLETED),overdue,acceptedValue,invoiced,paid,invoiced.subtract(paid));}
 private long count(java.util.List<PaintingJob> jobs,JobStatus s){return jobs.stream().filter(j->j.getStatus()==s).count();}
}
