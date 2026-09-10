package org.greenstone.backend.notification;
import org.greenstone.backend.persistence.entity.*;
public interface JobNotifier { void send(PaintingJob job, AdminUser recipient, NotificationType type); }
