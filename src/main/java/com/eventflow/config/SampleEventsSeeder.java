package com.eventflow.config;

import com.eventflow.model.Event;
import com.eventflow.model.EventScheduleItem;
import com.eventflow.model.EventTicketType;
import com.eventflow.model.User;
import com.eventflow.repository.EventRepository;
import com.eventflow.repository.EventScheduleRepository;
import com.eventflow.repository.EventTicketTypeRepository;
import com.eventflow.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Optional sample content for a brand-new database (OCCAVIE_SAMPLE_EVENTS=true). Runs only when there are no events at all,
 * so it never touches real data. The events belong to a non-login "Occavie Events" organizer account.
 */
@Component
public class SampleEventsSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SampleEventsSeeder.class);
    private static final String ORGANIZER_EMAIL = "events@occavie.local";

    record Row(String title, String category, String venue, int days, int hour, int minute, int capacity, int price) { }

    private static final List<Row> ROWS = List.of(
        new Row("AI & Cloud Innovation Summit", "Technology", "BICC, Dhaka", 5, 9, 30, 350, 800),
        new Row("Morning Yoga by the Lake", "Health", "Dhanmondi Lake", 6, 6, 30, 80, 0),
        new Row("Flutter Mobile Dev Day", "Technology", "Bangladesh Startup Hub", 7, 10, 0, 160, 400),
        new Row("Full Stack Web Bootcamp", "Technology", "SEU Computer Lab", 8, 10, 0, 100, 500),
        new Row("Inter-Department Cricket Cup", "Sports", "SEU Sports Ground", 9, 14, 0, 600, 100),
        new Row("Resume & LinkedIn Clinic", "Career", "SEU Seminar Hall", 10, 11, 0, 140, 0),
        new Row("Cloud Native Meetup", "Technology", "BICC, Dhaka", 11, 17, 0, 220, 0),
        new Row("Campus Music Fiesta", "Music", "Southeast University Auditorium", 12, 18, 0, 500, 300),
        new Row("Tree Plantation Drive", "Community", "Hatirjheel, Dhaka", 12, 8, 0, 200, 0),
        new Row("Intro to Machine Learning", "Education", "SEU CSE Lab 2", 13, 10, 30, 90, 300),
        new Row("Women in Leadership Summit", "Business", "Gulshan Innovation Hub", 14, 9, 30, 300, 900),
        new Row("Entrepreneur Networking Night", "Business", "Gulshan Innovation Hub", 15, 17, 30, 150, 600),
        new Row("Acoustic Evening", "Music", "Banani Creative Studio", 16, 18, 30, 150, 350),
        new Row("Small Business Finance Clinic", "Business", "Motijheel Training Centre", 17, 11, 0, 110, 250),
        new Row("UX Product Design Workshop", "Design", "Banani Creative Studio", 18, 11, 0, 90, 450),
        new Row("Nutrition & Fitness Workshop", "Health", "SEU Community Hall", 19, 10, 0, 120, 150),
        new Row("Career & Internship Expo", "Career", "Southeast University, Dhaka", 21, 10, 0, 650, 0),
        new Row("Poster & Brand Design Jam", "Design", "Banani Creative Studio", 21, 11, 0, 70, 300),
        new Row("5K Fun Run for Clean Air", "Sports", "Hatirjheel Running Track", 23, 6, 45, 700, 200),
        new Row("Robotics & IoT Showcase", "Technology", "Dhaka Innovation Center", 24, 14, 0, 240, 0),
        new Row("Neighbourhood Book Swap", "Community", "Dhanmondi Lake", 25, 16, 0, 120, 0),
        new Row("Health & Wellness Awareness Camp", "Health", "SEU Community Hall", 26, 9, 0, 180, 0),
        new Row("Scholarship & Study Abroad Fair", "Education", "SEU Central Auditorium", 27, 10, 0, 800, 0),
        new Row("Open Source Sprint", "Technology", "SEU Computer Lab", 28, 9, 30, 85, 0),
        new Row("Inter-University Football Tournament", "Sports", "Dhaka University Sports Ground", 29, 15, 0, 800, 150),
        new Row("Battle of the Bands", "Music", "SEU Open Stage", 30, 17, 0, 900, 250),
        new Row("Data Science for Beginners", "Education", "SEU CSE Lab 2", 32, 10, 30, 100, 300),
        new Row("Figma for Beginners", "Design", "SEU Seminar Hall", 33, 14, 0, 100, 0),
        new Row("Startup Pitch Day", "Business", "Bangladesh Startup Hub", 35, 11, 0, 200, 500),
        new Row("Tech Hiring Day", "Career", "International Convention City", 36, 10, 0, 500, 0),
        new Row("Photography & Storytelling Meetup", "Community", "Hatirjheel, Dhaka", 38, 8, 0, 100, 0),
        new Row("Cybersecurity CTF Night", "Technology", "Dhaka Innovation Center", 40, 18, 0, 180, 200),
        new Row("Cybersecurity Conference", "Technology", "International Convention City", 42, 9, 30, 450, 1000),
        new Row("Investor Speed Meetings", "Business", "Gulshan Business Centre", 44, 13, 0, 90, 600),
        new Row("Digital Marketing Masterclass", "Business", "Dhanmondi Training Centre", 45, 11, 0, 120, 350),
        new Row("Mental Wellbeing Circle", "Health", "SEU Community Hall", 47, 15, 0, 60, 0),
        new Row("UI Design Portfolio Review", "Design", "Banani Creative Studio", 48, 14, 0, 80, 400),
        new Row("Table Tennis Open", "Sports", "SEU Indoor Hall", 50, 15, 30, 128, 80),
        new Row("University Job Fair", "Career", "SEU Central Auditorium", 52, 10, 0, 750, 0),
        new Row("Research Paper Writing Workshop", "Education", "SEU Library Hall", 53, 10, 0, 100, 0),
        new Row("Marathon for a Cause", "Sports", "Hatirjheel Running Track", 55, 7, 0, 600, 250),
        new Row("Folk Music Mehfil", "Music", "University Auditorium", 56, 18, 0, 400, 150),
        new Row("Blockchain Technology Forum", "Technology", "BICC, Dhaka", 58, 10, 0, 250, 700),
        new Row("Motion Graphics Workshop", "Design", "Banani Creative Studio", 59, 11, 0, 75, 450),
        new Row("Music & Cultural Festival", "Music", "SEU Open Stage", 61, 17, 0, 900, 200),
        new Row("Internship Showcase", "Career", "SEU Central Auditorium", 62, 11, 0, 450, 0),
        new Row("Women in Technology Panel", "Education", "Southeast University Auditorium", 64, 15, 0, 230, 0),
        new Row("Charity Food Fair", "Community", "Southeast University Ground", 66, 12, 0, 1000, 0),
        new Row("Mobile App Hackathon", "Technology", "SEU Innovation Lab", 68, 9, 0, 140, 0),
        new Row("DevOps Practice Day", "Technology", "BICC, Dhaka", 70, 9, 30, 150, 500),
        new Row("Public Speaking Workshop", "Education", "SEU Seminar Hall", 72, 11, 0, 90, 200),
        new Row("Startup Weekend Dhaka", "Business", "Bangladesh Startup Hub", 74, 9, 0, 250, 700),
        new Row("Creative Entrepreneurship Fest", "Business", "Gulshan Business Centre", 75, 12, 0, 320, 500),
        new Row("Community Volunteer Meetup", "Community", "Dhanmondi Lake", 80, 9, 0, 120, 0));

    private final boolean enabled;
    private final EventRepository events;
    private final EventTicketTypeRepository tickets;
    private final EventScheduleRepository schedule;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public SampleEventsSeeder(@Value("${occavie.sample-events.enabled:false}") boolean enabled, EventRepository events,
                              EventTicketTypeRepository tickets, EventScheduleRepository schedule, UserRepository users,
                              PasswordEncoder encoder) {
        this.enabled = enabled; this.events = events; this.tickets = tickets; this.schedule = schedule;
        this.users = users; this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled || events.count() > 0) return;
        User organizer = users.findByEmailIgnoreCase(ORGANIZER_EMAIL).orElseGet(() -> users.save(User.builder()
                .fullName("Occavie Events").email(ORGANIZER_EMAIL).role("ORGANIZER").enabled(true)
                .password(encoder.encode(UUID.randomUUID() + "Aa1!")).build()));   // random password: nobody can log in
        LocalDate today = LocalDate.now();
        List<EventTicketType> ticketRows = new ArrayList<>();
        List<EventScheduleItem> scheduleRows = new ArrayList<>();
        int index = 0;
        for (Row r : ROWS) {
            LocalTime start = LocalTime.of(r.hour(), r.minute());
            boolean paid = r.price() > 0;
            Event event = events.save(Event.builder().title(r.title()).category(r.category()).location(r.venue())
                    .date(today.plusDays(r.days())).time(start)
                    .description(r.title() + " brings people together at " + r.venue() + " for talks, hands-on sessions and networking.")
                    .capacity(r.capacity()).status("UPCOMING").featured(index++ < 6)
                    .organizerId(organizer.getId()).organizerName(organizer.getFullName())
                    .pricingType(paid ? "PAID" : "FREE").basePrice(BigDecimal.valueOf(r.price())).build());
            int standard = paid ? (int) Math.floor(r.capacity() * 0.8) : r.capacity();
            ticketRows.add(EventTicketType.builder().eventId(event.getId()).name(paid ? "Standard" : "General Admission")
                    .description(paid ? "Regular event entry" : "Free admission").price(BigDecimal.valueOf(r.price()))
                    .capacity(standard).active(true).sortOrder(1).build());
            if (paid) ticketRows.add(EventTicketType.builder().eventId(event.getId()).name("VIP")
                    .description("Priority seating and enhanced access").price(BigDecimal.valueOf(r.price() * 2L))
                    .capacity(r.capacity() - standard).active(true).sortOrder(2).build());
            int[][] parts = {{0, 25}, {30, 100}, {110, 155}};
            String[] names = {"Registration & Welcome", "Main Session: " + r.title(), "Questions & Networking"};
            for (int i = 0; i < 3; i++) {
                int from = r.hour() * 60 + r.minute() + parts[i][0], to = r.hour() * 60 + r.minute() + parts[i][1];
                scheduleRows.add(EventScheduleItem.builder().eventId(event.getId()).title(names[i])
                        .startTime(LocalTime.of((from / 60) % 24, from % 60)).endTime(LocalTime.of((to / 60) % 24, to % 60))
                        .location(r.venue()).description("").build());
            }
        }
        tickets.saveAll(ticketRows);
        schedule.saveAll(scheduleRows);
        log.info("Loaded {} sample events (OCCAVIE_SAMPLE_EVENTS=true).", ROWS.size());
    }
}
