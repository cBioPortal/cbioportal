package org.cbioportal.persistence.mybatis.util;

import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

@Component
public class MolecularProfileCaseIdentifierUtil {

    public Map<String, Set<String>> getGroupedCasesByMolecularProfileId(List<String> molecularProfileIds, List<String> caseIds) {

        if (CollectionUtils.isEmpty(caseIds)) {
            return molecularProfileIds.stream().collect(Collectors.toMap(Function.identity(), e -> new HashSet<>()));
        }

        return IntStream.range(0, molecularProfileIds.size())
            .mapToObj(i -> new Pair<>(molecularProfileIds.get(i), caseIds.get(i)))
            .collect(groupingBy(
                Pair<String, String>::getFirst,
                mapping(Pair<String, String>::getSecond, toSet()))
            );
    }
}
