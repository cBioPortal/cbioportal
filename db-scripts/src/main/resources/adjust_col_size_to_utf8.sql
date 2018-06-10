DROP PROCEDURE IF EXISTS adjust_col_size_to_utf8;
DELIMITER $$
CREATE PROCEDURE adjust_col_size_to_utf8() BEGIN 
 IF ((SELECT MAX(LENGTH(TUMOR_SEQ_ALLELE)) FROM mutation_event) < 256 
    AND (SELECT  MAX(LENGTH(REFERENCE_ALLELE)) FROM mutation_event) < 256)
 THEN 
    ALTER TABLE mutation_event 
        MODIFY REFERENCE_ALLELE varchar(255),
        MODIFY TUMOR_SEQ_ALLELE varchar(255),
        MODIFY MUTATION_TYPE varchar(64),
        MODIFY LINK_XVAR varchar(255),
        MODIFY LINK_PDB varchar(255),
        MODIFY LINK_MSA varchar(255);
 END IF;
END$$
DELIMITER ;