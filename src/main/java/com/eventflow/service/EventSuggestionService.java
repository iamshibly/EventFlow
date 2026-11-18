package com.eventflow.service;

import com.eventflow.model.Event;
import com.eventflow.model.Registration;
import com.eventflow.model.SavedEvent;
import com.eventflow.model.User;
import com.eventflow.repository.EventRepository;
import com.eventflow.repository.RegistrationRepository;
import com.eventflow.repository.SavedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Related events using plain filters: same category, categories the participant already likes, then soonest. */
@Service
@RequiredArgsConstructor
public class EventSuggestionService {
    private static final String PUBLISHED = "UPCOMING";

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final SavedEventRepository savedEventRepository;

    /** "You might also like" for an event page. */
    public List<Event> relatedTo(Event event, User viewer, int limit) {
        Set<String> exclude = new LinkedHashSet<>(excludedIds(viewer));
        exclude.add(event.getId());
        Map<String, Event> picks = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        if (event.getCategory() != null) add(picks, eventRepository.findTop12ByCategoryAndStatusAndDateGreaterThanEqualOrderByDateAsc(event.getCategory(), PUBLISHED, today), exclude, limit);
        for (String category : interests(viewer)) {
            if (picks.size() >= limit) break;
            add(picks, eventRepository.findTop12ByCategoryAndStatusAndDateGreaterThanEqualOrderByDateAsc(category, PUBLISHED, today), exclude, limit);
        }
        if (picks.size() < limit) add(picks, eventRepository.findTop12ByStatusAndDateGreaterThanEqualOrderByDateAsc(PUBLISHED, today), exclude, limit);
        return new ArrayList<>(picks.values());
    }

    /** "Suggested for you" on the participant dashboard. */
    public List<Event> forParticipant(User participant, int limit) {
        Set<String> exclude = new LinkedHashSet<>(excludedIds(participant));
        Map<String, Event> picks = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (String category : interests(participant)) {
            if (picks.size() >= limit) break;
            add(picks, eventRepository.findTop12ByCategoryAndStatusAndDateGreaterThanEqualOrderByDateAsc(category, PUBLISHED, today), exclude, limit);
        }
        if (picks.size() < limit) add(picks, eventRepository.findTop12ByStatusAndDateGreaterThanEqualOrderByDateAsc(PUBLISHED, today), exclude, limit);
        return new ArrayList<>(picks.values());
    }

    private void add(Map<String, Event> picks, List<Event> candidates, Set<String> exclude, int limit) {
        for (Event candidate : candidates) {
            if (picks.size() >= limit) return;
            if (!exclude.contains(candidate.getId()) && EventRules.isPublished(candidate) && EventRules.bookable(candidate)) {
                picks.putIfAbsent(candidate.getId(), candidate);
            }
        }
    }

    /** Events a participant already registered for or saved are not suggested again. */
    private Set<String> excludedIds(User user) {
        Set<String> ids = new LinkedHashSet<>();
        if (user == null || !"PARTICIPANT".equals(user.getRole())) return ids;
        registrationRepository.findByParticipantIdOrderByRegisteredAtDesc(user.getId()).forEach(r -> ids.add(r.getEventId()));
        return ids;
    }

    /** Categories of events the participant registered for or saved, most recent first. */
    private List<String> interests(User user) {
        if (user == null || !"PARTICIPANT".equals(user.getRole())) return List.of();
        List<String> ids = new ArrayList<>();
        registrationRepository.findByParticipantIdOrderByRegisteredAtDesc(user.getId()).stream().map(Registration::getEventId).forEach(ids::add);
        savedEventRepository.findByParticipantIdOrderBySavedAtDesc(user.getId()).stream().map(SavedEvent::getEventId).forEach(ids::add);
        Set<String> categories = new LinkedHashSet<>();
        for (String id : ids) {
            eventRepository.findById(id).map(Event::getCategory).filter(c -> c != null && !c.isBlank()).ifPresent(categories::add);
            if (categories.size() >= 3) break;
        }
        return new ArrayList<>(categories);
    }
}
