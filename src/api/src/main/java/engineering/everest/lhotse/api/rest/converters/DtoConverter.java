package engineering.everest.lhotse.api.rest.converters;

import engineering.everest.lhotse.api.rest.responses.CompetitionEntryFragment;
import engineering.everest.lhotse.api.rest.responses.CompetitionSummaryResponse;
import engineering.everest.lhotse.api.rest.responses.CompetitionWithEntriesResponse;
import engineering.everest.lhotse.api.rest.responses.PhotoResponse;
import engineering.everest.lhotse.competitions.domain.Competition;
import engineering.everest.lhotse.competitions.domain.CompetitionWithEntries;
import engineering.everest.lhotse.photos.Photo;
import org.springframework.stereotype.Service;

@Service
public class DtoConverter {

    public PhotoResponse convert(Photo photo) {
        return new PhotoResponse(photo.getId(), photo.getFilename(), photo.getUploadTimestamp());
    }

    public CompetitionSummaryResponse convert(Competition competition) {
        return new CompetitionSummaryResponse(competition.getId(), competition.getDescription(), competition.getSubmissionsOpenTimestamp(),
            competition.getSubmissionsCloseTimestamp(), competition.getVotingEndsTimestamp(), competition.getMaxEntriesPerUser());
    }

    public CompetitionWithEntriesResponse convert(CompetitionWithEntries competitionWithEntries) {
        var entries = competitionWithEntries.getEntries().stream()
            .map(x -> new CompetitionEntryFragment(x.getPhotoId(), x.getSubmittedByUserId(), x.getEntryTimestamp(),
                x.getNumVotesReceived(), x.isWinner()))
            .toList();

        return new CompetitionWithEntriesResponse(
            competitionWithEntries.getId(),
            competitionWithEntries.getDescription(),
            competitionWithEntries.getSubmissionsOpenTimestamp(),
            competitionWithEntries.getSubmissionsCloseTimestamp(),
            competitionWithEntries.getVotingEndsTimestamp(),
            competitionWithEntries.getMaxEntriesPerUser(),
            entries);
    }
}
