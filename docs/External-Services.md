# External Services
cBioPortal uses the APIs from several external services to provide more
information about a variant:

- [OncoKB](#OncoKB)
- [CIVIC](#CIVIC)
- [Genome Nexus](#Genome-Nexus)
- [G2S](#G2S)

For privacy concerns see the section: [A note on privacy](#A-note-on-privacy).

## OncoKB
[OncoKB](https://www.oncokb.org) is a precision oncology knowledge base that
contains information about the effects and treatment implications of specific
cancer gene alterations. For information on how to deploy this service yourself
see: https://github.com/oncokb/oncokb.

## CIVIC
[CIVIC](https://civicdb.org) is a community-edited forum for discussion and
interpretation of peer-reviewed publications pertaining to the clinical
relevance of variants (or biomarker alterations) in cancer. For information on
how to deploy this service yourself see:
https://github.com/griffithlab/civic-server. It is also possible to disable
showing CIVIC in cBioPortal by setting `show.civic=false` in the
`portal.properties` (See [portal.properties reference](portal.properties-Reference.md).

## Genome Nexus
[Genome Nexus](https://www.genomenexus.org) is a comprehensive one-stop
resource for fast, automated and high-throughput annotation and interpretation
of genetic variants in cancer. For information on how to deploy this service
yourself see: https://github.com/genome-nexus/genome-nexus. For more
information on the various annotation sources and versions provided by Genome
Nexus see: https://docs.genomenexus.org/annotation-sources.

## G2S
[G2S (Genome to Structure)](https://g2s.genomenexus.org) maps genomic variants
to 3D structures. cBioPortal uses it on the mutations tab to show the variants
on a 3D structure. For information on how to deploy this service yourself see:
https://github.com/genome-nexus/g2s.


## A note on privacy

cBioPortal calls these services with variant information from the cBioPortal
database. It however does not send over information that links a variant to a
particular sample or patient. If this is still a concern for you we recommmend
to deploy your own versions of these services. See the sections above to
linkouts for instructions on how to do this.
