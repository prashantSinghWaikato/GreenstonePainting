package org.greenstone.backend.notification;
import jakarta.mail.MessagingException;
import org.greenstone.backend.persistence.entity.*;
import org.greenstone.backend.web.NotificationDeliveryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
public class EmailJobNotifier implements JobNotifier {
 private final JavaMailSender mail; private final String from; private final String frontend;
 public EmailJobNotifier(JavaMailSender m,@Value("${app.notification.from}")String f,@Value("${app.frontend-base-url}")String u){mail=m;from=f;frontend=u.replaceAll("/+$","");}
 public void send(PaintingJob job,AdminUser recipient,NotificationType type){var heading=switch(type){case JOB_ASSIGNMENT->"Painting job assigned";case JOB_SCHEDULE->"Job schedule updated";case JOB_STATUS->"Job status changed";case JOB_REMINDER->"Painting job starts tomorrow";default->"Painting job update";};var url=frontend+"/admin/?section=jobs&job="+job.getId();var schedule=(job.getScheduledStartDate()==null?"Not scheduled":job.getScheduledStartDate().format(DateTimeFormatter.ofPattern("d MMM yyyy")))+(job.getScheduledEndDate()==null?"":" to "+job.getScheduledEndDate().format(DateTimeFormatter.ofPattern("d MMM yyyy")));var plain="Hi "+recipient.getDisplayName()+",\n\n"+heading+" for "+job.getCustomerName()+".\nJob: "+job.getJobNumber()+"\nProject: "+job.getTitle()+"\nSchedule: "+schedule+"\nStatus: "+job.getStatus()+"\n\nOpen job: "+url;var html="<h1>"+esc(heading)+"</h1><p><strong>"+esc(job.getJobNumber())+" · "+esc(job.getCustomerName())+"</strong></p><p>"+esc(job.getTitle())+"<br>"+esc(schedule)+"<br>Status: "+esc(job.getStatus().name().replace('_',' '))+"</p><a href=\""+esc(url)+"\" style=\"display:inline-block;padding:12px 18px;background:#0b2a3e;color:#fff;text-decoration:none\">Open job</a>";try{var msg=mail.createMimeMessage();var h=new MimeMessageHelper(msg,true,StandardCharsets.UTF_8.name());h.setTo(recipient.getEmail());h.setFrom(from);h.setSubject(heading+" · "+job.getJobNumber());h.setText(plain,"<!doctype html><html><body style=\"font-family:Arial;background:#f1eee8;padding:28px\"><div style=\"max-width:680px;margin:auto;background:#fff;padding:28px\">"+html+"</div></body></html>");mail.send(msg);}catch(MessagingException|MailException e){throw new NotificationDeliveryException("The job notification could not be sent.",e);}}
 private String esc(String v){return HtmlUtils.htmlEscape(v==null?"":v);}
}
