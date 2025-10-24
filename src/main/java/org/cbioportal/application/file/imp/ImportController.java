package org.cbioportal.application.file.imp;

import org.cbioportal.application.file.imp.readers.KeyValueMetadataReader;
import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.SequencedMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
public class ImportController {

    //TODO Add Authentication and autorization check
    @PostMapping(value = "/import/study/",
        consumes = {MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/zip"})
    public ResponseEntity<String> uploadStudyData(InputStream bodyStream, @RequestParam(defaultValue = "false") boolean incremental) throws IOException {
        if (!incremental) {
            //TODO has to drop the study and all related data?
        }
        try (ZipInputStreamReaderIterator zipInputStreamReaderIterator = new ZipInputStreamReaderIterator(bodyStream)) {
            HashMap<String, SequencedMap<String, String>> metadataByDataFilename = new HashMap<>();
            zipInputStreamReaderIterator.forEachRemaining(reader -> {
                if (reader.getFilename().startsWith("meta_")) {
                    try {
                        var metadata = new KeyValueMetadataReader(reader).readMetadata();
                        if (metadata.containsKey("data_filename")) {
                            metadataByDataFilename.put(metadata.get("data_filename"), metadata);
                        } else {
                            ingestMetadata(metadata);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (reader.getFilename().startsWith("data_")) {
                    if (metadataByDataFilename.containsKey(reader.getFilename())) {
                        ingestMetadataAndData(metadataByDataFilename.get(reader.getFilename()), reader);
                    } else {
                        throw new IllegalStateException("Data file " + reader.getFilename() + " does not have corresponding meta file sent before in the stream.");
                    }
                }
            });
            return ResponseEntity.ok("Archive processed successfully");
        }
    }

    private void ingestMetadataAndData(SequencedMap<String, String> stringStringSequencedMap, ZipInputStreamReaderIterator.ZipEntryInputStreamReader reader) {
    }

    private void ingestMetadata(SequencedMap<String, String> metadata) {
        //TODO does not have to appear in incremental upload
        if (metadata.containsKey("type_of_cancer")) { //detect as study metadata
            CancerStudyMetadata cancerStudyMetadata = composeCancerStudyMetadata(metadata);
        }
    }

    private static CancerStudyMetadata composeCancerStudyMetadata(SequencedMap<String, String> metadata) {
        CancerStudyMetadata cancerStudyMetadata = new CancerStudyMetadata();
        cancerStudyMetadata.setCancerStudyIdentifier(metadata.get("cancer_study_identifier"));
        cancerStudyMetadata.setTypeOfCancer(metadata.get("type_of_cancer"));
        cancerStudyMetadata.setName(metadata.get("name"));
        cancerStudyMetadata.setDescription(metadata.get("description"));
        cancerStudyMetadata.setCitation(metadata.get("citation"));
        cancerStudyMetadata.setPmid(metadata.get("pmid"));
        cancerStudyMetadata.setGroups(metadata.get("groups"));
        cancerStudyMetadata.setAddGlobalCaseList(Boolean.parseBoolean(metadata.get("add_global_case_list")));
        cancerStudyMetadata.setReferenceGenome(metadata.get("reference_genome"));
        return cancerStudyMetadata;
    }
}