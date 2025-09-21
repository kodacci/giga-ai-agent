package pro.ra_tech.giga_ai_agent.domain.impl;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.val;
import pro.ra_tech.giga_ai_agent.database.repos.api.TagRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.TagData;
import pro.ra_tech.giga_ai_agent.domain.api.TagService;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagRepository repo;

    @Override
    public Either<AppFailure, List<TagData>> mergeAndSave(List<String> tags) {
        val all = new ArrayList<TagData>();

        return repo.findByNames(tags)
                .peek(all::addAll)
                .map(
                        foundTags -> tags.stream()
                                .filter(tag -> foundTags.stream()
                                        .noneMatch(foundTag -> tag.equals(foundTag.name()))
                                )
                                .toList()
                )
                .flatMap(repo::create)
                .peek(all::addAll)
                .map(res -> all);
    }
}
