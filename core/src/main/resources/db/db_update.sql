alter table 
 cbioportal.mutation
 add
  UNIQUE KEY `UQ_MUTATION_EVENT_ID_GENETIC_PROFILE_ID_SAMPLE_ID` (`MUTATION_EVENT_ID`,`GENETIC_PROFILE_ID`,`SAMPLE_ID`) COMMENT 'Constraint to block duplicated mutation entries.';
  
alter table
 cbioportal.sample_profile
 add
  UNIQUE KEY `UQ_SAMPLE_ID_GENETIC_PROFILE_ID` (`SAMPLE_ID`,`GENETIC_PROFILE_ID`) COMMENT 'Constraint to allow each sample only once in each profile.';

