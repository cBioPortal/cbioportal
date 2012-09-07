-- sql table for oncotator cache

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

drop table if EXISTS mutation_assessor_cache;

--
-- Database: `mutation_assessor_cache`
--

--
-- Table structure for mutation assessor cache
--
CREATE TABLE IF NOT EXISTS `mutation_assessor_cache` (
  `CACHE_KEY` varchar(255) NOT NULL,
  `PREDICTED_IMPACT` varchar(10),
  `PROTEIN_CHANGE` varchar(50),
  `STRUCTURE_LINK` varchar(80),
  `ALIGNMENT_LINK` varchar(80),
  UNIQUE KEY `CACHE_KEY` (`CACHE_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;