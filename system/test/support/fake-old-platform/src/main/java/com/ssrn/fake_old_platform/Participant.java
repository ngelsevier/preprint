package com.ssrn.fake_old_platform;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class Participant {

    private final String username;
    private final int accountId;
    private String name;
    private final EventLog authorEventLog;
    private final PaperRepository paperRepository;
    private int authorVersion = 1;

    public Participant(String username, int accountId, String name, EventLog authorEventLog, PaperRepository paperRepository) {
        this.username = username;
        this.accountId = accountId;
        this.name = name;
        this.authorEventLog = authorEventLog;
        this.paperRepository = paperRepository;
    }

    void addToPaper(int abstractId, DateTime overiddenEventTime, boolean isHistoricAssociation) {
        boolean notAnAuthor = !isAnAuthor(Optional.of(abstractId));

        forPaper(abstractId, p -> p.addAuthor(Integer.toString(accountId), overiddenEventTime, isHistoricAssociation));
        if (notAnAuthor && !isHistoricAssociation) {
            emitAuthorEvent("REGISTERED", overiddenEventTime);
        }
    }

    void removeFromPaper(int abstractId) {
        boolean wasAnAuthor = isAnAuthor(Optional.empty());

        forPaper(abstractId, paper -> paper.removeAuthor(Integer.toString(accountId)));

        if (wasAnAuthor && !isAnAuthor(Optional.empty())) {
            emitAuthorEvent("UNREGISTERED", null);
        }
    }

    public int getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public void setName(String firstName, String lastName, DateTime overriddenEventTime) {
        name = String.format("%s %s", firstName, lastName);

        if (isAnAuthor(Optional.empty())) {
            emitAuthorEvent("NAME CHANGED", overriddenEventTime);
        }
    }

    public boolean isAnAuthor(Optional<Integer> abstractIdToExclude) {
        return abstractIdToExclude
                .map(abstractId -> Arrays.stream(paperRepository.getPapersWrittenBy(this)).filter(paper -> !abstractId.equals(paper.getId())).anyMatch(Paper::hasSearchableSubmissionStage))
                .orElseGet(() -> Arrays.stream(paperRepository.getPapersWrittenBy(this)).anyMatch(Paper::hasSearchableSubmissionStage));
    }

    public int getAuthorVersion() {
        return authorVersion;
    }

    @Override
    public String toString() {
        return "Participant{" +
                "username='" + username + '\'' +
                ", accountId=" + accountId +
                ", name='" + name + '\'' +
                ", authorVersion=" + authorVersion +
                '}';
    }

    void emitAuthorEvent(String type, DateTime overriddenEventTime) {
        authorEventLog.append(new AuthorEvent(
                UUID.randomUUID().toString(),
                type,
                Integer.toString(accountId),
                overriddenEventTime == null ? new DateTime(DateTimeZone.UTC) : overriddenEventTime,
                ++authorVersion,
                new AuthorEventData(name)
        ));
    }

    private void forPaper(int abstractId, Consumer<Paper> c) {
        Paper paper = paperRepository.getById(abstractId);

        if (paper == null) {
            throw new RuntimeException(String.format("Could not find paper %d", abstractId));
        }

        c.accept(paper);
        paperRepository.save(paper);
    }

    public boolean hasSearchablePaper() {
        return Arrays.stream(paperRepository.getPapersWrittenBy(this)).anyMatch(Paper::hasSearchableSubmissionStage);
    }
}
