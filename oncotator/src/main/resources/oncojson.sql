-- sql table for oncotator json cache
-- a simple cache with key and the raw json value pair

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

drop table if EXISTS onco_json_cache;

--
-- Database: `oncotator`
--

--
-- Table structure for oncotator JSON cache
--
CREATE TABLE IF NOT EXISTS `onco_json_cache` (
  `CACHE_KEY` varchar(255) NOT NULL,
  `RAW_JSON` text,
  UNIQUE KEY `CACHE_KEY` (`CACHE_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;