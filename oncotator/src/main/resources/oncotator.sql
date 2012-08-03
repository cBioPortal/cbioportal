-- sql table for oncotator cache

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

drop table if EXISTS oncotator_cache;

--
-- Database: `oncotator`
--

--
-- Table structure for oncotator cache
--
CREATE TABLE IF NOT EXISTS `oncotator_cache` (
  `CACHE_KEY` varchar(255) NOT NULL,
  `GENE_SYMBOL` varchar(25),
  `GENOME_CHANGE` varchar(128),
  `PROTEIN_CHANGE` varchar(128),
  `VARIANT_CLASSIFICATION` varchar(25),
  `EXON_AFFECTED` int(11),
  `COSMIC_OVERLAP` varchar(1024),
  `DB_SNP_RS` varchar(25),
  UNIQUE KEY `CACHE_KEY` (`CACHE_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;