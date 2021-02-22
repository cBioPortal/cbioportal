package org.cbioportal.model;

public enum MutationEventType {

    missense_mutation("missense_mutation"),
    missense("missense"),
    missense_variant("missense_variant"),
    frame_shift_ins("frame_shift_ins"),
    frame_shift_del("frame_shift_del"),
    frameshift("frameshift"),
    frameshift_deletion("frameshift_deletion"),
    frameshift_insertion("frameshift_insertion"),
    de_novo_start_outofframe("de_novo_start_outofframe"),
    frameshift_variant("frameshift_variant"),
    nonsense_mutation("nonsense_mutation"),
    nonsense("nonsense"),
    stopgain_snv("stopgain_snv"),
    stop_gained("stop_gained"),
    splice_site("splice_site"),
    splice("splice"),
    splicing("splicing"),
    splice_site_snp("splice_site_snp"),
    splice_site_del("splice_site_del"),
    splice_site_indel("splice_site_indel"),
    splice_region_variant("splice_region_variant"),
    splice_region("splice_region"),
    translation_start_site("translation_start_site"),
    initiator_codon_variant("initiator_codon_variant"),
    start_codon_snp("start_codon_snp"),
    start_codon_del("start_codon_del"),
    nonstop_mutation("nonstop_mutation"),
    stop_lost("stop_lost"),
    inframe_del("inframe_del"),
    inframe_deletion("inframe_deletion"),
    in_frame_del("in_frame_del"),
    in_frame_deletion("in_frame_deletion"),
    inframe_ins("inframe_ins"),
    inframe_insertion("inframe_insertion"),
    in_frame_ins("in_frame_ins"),
    in_frame_insertion("in_frame_insertion"),
    indel("indel"),
    nonframeshift_deletion("nonframeshift_deletion"),
    nonframeshift("nonframeshift"),
    nonframeshift_insertion("nonframeshift_insertion"),
    targeted_region("targeted_region"),
    inframe("inframe"),
    truncating("truncating"),
    feature_truncation("feature_truncation"),
    fusion("fusion"),
    silent("silent"),
    synonymous_variant("synonymous_variant"),
    any("any"),
    other("other");

    private final String mutationType;

    MutationEventType(String mutationType) {
        this.mutationType = mutationType;
    }

    public String getMutationType() {
        return mutationType;
    }

}
