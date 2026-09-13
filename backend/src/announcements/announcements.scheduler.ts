/*
 * Scheduled announcements are not part of schema.sql.
 *
 * The annonce table contains no:
 *
 * - date_planifiee
 * - publiee
 *
 * Announcements are therefore published immediately.
 *
 * This file is intentionally left without a scheduler.
 *
 * The AnnouncementsModule no longer registers
 * AnnouncementsScheduler.
 */