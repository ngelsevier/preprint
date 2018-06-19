package com.ssrn.fake_old_platform;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

class ParticipantRepository {
    private final List<Participant> participants = new CopyOnWriteArrayList<>();

    public Stream<Participant> getAll() {
        return participants.stream();
    }

    Optional<Participant> getByUsername(String username) {
        return participants.stream()
                .filter(participant -> username.equals(participant.getUsername()))
                .findFirst();
    }

    public Optional<Participant> getById(int id) {
        return participants.stream()
                .filter(p -> id == p.getAccountId())
                .findFirst();
    }

    public void save(Participant participant) {
        participants.removeIf(p -> p.getAccountId() == participant.getAccountId());
        participants.add(participant);
    }
}
