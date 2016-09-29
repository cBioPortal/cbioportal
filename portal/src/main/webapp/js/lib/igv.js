/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 University of California San Diego
 * Author: Jim Robinson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * NOTICE OF MODIFICATION
 * This copy of the igv.js code has been modified for use with the cbioportal project (http://cbioportal.org)
 * Modified on: 2016-09-21
 * For an unmodified version please see https://github.com/igvteam/igv.js
 */

var igv = (function (igv) {
    

    function canBePaired(alignment) {
        return alignment.isPaired() &&
            alignment.mate &&
            alignment.isMateMapped() &&
            alignment.chr === alignment.mate.chr &&
            (alignment.isFirstOfPair() || alignment.isSecondOfPair()) && !(alignment.isSecondary() || alignment.isSupplementary());
    }


    igv.AlignmentContainer = function (chr, start, end, samplingWindowSize, samplingDepth, pairsSupported) {

        this.chr = chr;
        this.start = start;
        this.end = end;
        this.length = (end - start);

        this.coverageMap = new CoverageMap(chr, start, end);
        this.alignments = [];
        this.downsampledIntervals = [];

        this.samplingWindowSize = samplingWindowSize === undefined ? 100 : samplingWindowSize;
        this.samplingDepth = samplingDepth === undefined ? 50 : samplingDepth;

        this.pairsSupported = pairsSupported;
        this.paired = false;  // false until proven otherwise
        this.pairsCache = {};  // working cache of paired alignments by read name

        this.downsampledReads = new Set();

        this.currentBucket = new DownsampleBucket(this.start, this.start + this.samplingWindowSize, this);

        this.filter = function filter(alignment) {         // TODO -- pass this in
            return alignment.isMapped() && !alignment.isFailsVendorQualityCheck();
        }

    }

    igv.AlignmentContainer.prototype.push = function (alignment) {

        if (this.filter(alignment) === false) return;

        this.coverageMap.incCounts(alignment);   // Count coverage before any downsampling

        if (this.pairsSupported && this.downsampledReads.has(alignment.readName)) {
            return;   // Mate already downsampled -- pairs are treated as a single alignment for downsampling
        }

        if (alignment.start >= this.currentBucket.end) {
            finishBucket.call(this);
            this.currentBucket = new DownsampleBucket(alignment.start, alignment.start + this.samplingWindowSize, this);
        }

        this.currentBucket.addAlignment(alignment);

    }

    igv.AlignmentContainer.prototype.forEach = function (callback) {
        this.alignments.forEach(callback);
    }

    igv.AlignmentContainer.prototype.finish = function () {

        if (this.currentBucket !== undefined) {
            finishBucket.call(this);
        }

        // Need to remove partial pairs whose mate was downsampled
        if(this.pairsSupported) {
            var tmp = [], ds = this.downsampledReads;

            this.alignments.forEach(function (a) {
                if (!ds.has(a.readName)) {
                    tmp.push(a);
                }
            })
            this.alignments = tmp;
        }

        this.alignments.sort(function (a, b) {
            return a.start - b.start
        });

        this.pairsCache = undefined;
        this.downsampledReads = undefined;
    }

    igv.AlignmentContainer.prototype.contains = function (chr, start, end) {
        return this.chr == chr &&
            this.start <= start &&
            this.end >= end;
    }

    igv.AlignmentContainer.prototype.hasDownsampledIntervals = function () {
        return this.downsampledIntervals && this.downsampledIntervals.length > 0;
    }

    function finishBucket() {
        this.alignments = this.alignments.concat(this.currentBucket.alignments);
        if (this.currentBucket.downsampledCount > 0) {
            this.downsampledIntervals.push(new DownsampledInterval(
                this.currentBucket.start,
                this.currentBucket.end,
                this.currentBucket.downsampledCount));
        }
        this.paired = this.paired || this.currentBucket.paired;
    }

    function DownsampleBucket(start, end, alignmentContainer) {

        this.start = start;
        this.end = end;
        this.alignments = [];
        this.downsampledCount = 0;
        this.samplingDepth = alignmentContainer.samplingDepth;
        this.pairsSupported = alignmentContainer.pairsSupported;
        this.downsampledReads = alignmentContainer.downsampledReads;
        this.pairsCache = alignmentContainer.pairsCache;
    }

    DownsampleBucket.prototype.addAlignment = function (alignment) {

        var samplingProb, idx, replacedAlignment, pairedAlignment;

        if (this.alignments.length < this.samplingDepth) {

            if (this.pairsSupported && canBePaired(alignment)) {
                pairedAlignment = this.pairsCache[alignment.readName];
                if (pairedAlignment) {
                    //Not subject to downsampling, just update the existing alignment
                    pairedAlignment.setSecondAlignment(alignment);
                    this.pairsCache[alignment.readName] = undefined;   // Don't need to track this anymore. NOTE: Don't "delete", causes runtime performance issues
                }
                else {
                    // First alignment in a pair
                    pairedAlignment = new igv.PairedAlignment(alignment);
                    this.paired = true;
                    this.pairsCache[alignment.readName] = pairedAlignment;
                    this.alignments.push(pairedAlignment);
                }
            }
            else {
                this.alignments.push(alignment);
            }

        } else {

            samplingProb = this.samplingDepth / (this.samplingDepth + this.downsampledCount + 1);

            if (Math.random() < samplingProb) {

                idx = Math.floor(Math.random() * (this.alignments.length - 1));
                replacedAlignment = this.alignments[idx];   // To be replaced

                if (this.pairsSupported && canBePaired(alignment)) {

                    if(this.pairsCache[replacedAlignment.readName] !== undefined) {
                        this.pairsCache[replacedAlignment.readName] = undefined;
                    }

                    pairedAlignment = new igv.PairedAlignment(alignment);
                    this.paired = true;
                    this.pairsCache[alignment.readName] = pairedAlignment;
                    this.alignments[idx] = pairedAlignment;

                }
                else {
                    this.alignments[idx] = alignment;
                }
                this.downsampledReads.add(replacedAlignment.readName);

            }
            else {
                this.downsampledReads.add(alignment.readName);
            }

            this.downsampledCount++;
        }

    }


    // TODO -- refactor this to use an object, rather than an array,  if end-start is > some threshold
    function CoverageMap(chr, start, end) {

        this.chr = chr;
        this.bpStart = start;
        this.length = (end - start);

        this.coverage = new Array(this.length);

        this.maximum = 0;

        this.threshold = 0.2;
        this.qualityWeight = true;
    }

    CoverageMap.prototype.incCounts = function (alignment) {

        var self = this;

        if (alignment.blocks === undefined) {

            incBlockCount(alignment);
        }
        else {
            alignment.blocks.forEach(function (block) {
                incBlockCount(block);
            });
        }

        function incBlockCount(block) {

            var key,
                base,
                i,
                j,
                q;

            for (i = block.start - self.bpStart, j = 0; j < block.len; i++, j++) {

                if (!self.coverage[i]) {
                    self.coverage[i] = new Coverage();
                }

                base = block.seq.charAt(j);
                key = (alignment.strand) ? "pos" + base : "neg" + base;
                q = block.qual[j];

                self.coverage[i][key] += 1;
                self.coverage[i]["qual" + base] += q;

                self.coverage[i].total += 1;
                self.coverage[i].qual += q;

                self.maximum = Math.max(self.coverage[i].total, self.maximum);

            }
        }
    }

    function Coverage() {
        this.posA = 0;
        this.negA = 0;

        this.posT = 0;
        this.negT = 0;

        this.posC = 0;
        this.negC = 0;
        this.posG = 0;

        this.negG = 0;

        this.posN = 0;
        this.negN = 0;

        this.pos = 0;
        this.neg = 0;

        this.qualA = 0;
        this.qualT = 0;
        this.qualC = 0;
        this.qualG = 0;
        this.qualN = 0;

        this.qual = 0;

        this.total = 0;
    }

    Coverage.prototype.isMismatch = function (refBase) {

        var myself = this,
            mismatchQualitySum,
            threshold = igv.CoverageMap.threshold * ((igv.CoverageMap.qualityWeight && this.qual) ? this.qual : this.total);

        mismatchQualitySum = 0;
        ["A", "T", "C", "G"].forEach(function (base) {

            if (base !== refBase) {
                mismatchQualitySum += ((igv.CoverageMap.qualityWeight && myself.qual) ? myself["qual" + base] : (myself["pos" + base] + myself["neg" + base]));
            }
        });

        return mismatchQualitySum >= threshold;

    };

    DownsampledInterval = function (start, end, counts) {
        this.start = start;
        this.end = end;
        this.counts = counts;
    }

    DownsampledInterval.prototype.popupData = function (genomicLocation) {
        return [
            {name: "start", value: this.start + 1},
            {name: "end", value: this.end},
            {name: "# downsampled:", value: this.counts}]
    }


    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    var BAM_MAGIC = 21840194;
    var BAI_MAGIC = 21578050;
    var SECRET_DECODER = ['=', 'A', 'C', 'x', 'G', 'x', 'x', 'x', 'T', 'x', 'x', 'x', 'x', 'x', 'x', 'N'];
    var CIGAR_DECODER = ['M', 'I', 'D', 'N', 'S', 'H', 'P', '=', 'X', '?', '?', '?', '?', '?', '?', '?'];
    var READ_PAIRED_FLAG = 0x1;
    var PROPER_PAIR_FLAG = 0x2;
    var READ_UNMAPPED_FLAG = 0x4;
    var MATE_UNMAPPED_FLAG = 0x8;
    var READ_STRAND_FLAG = 0x10;
    var MATE_STRAND_FLAG = 0x20;
    var FIRST_OF_PAIR_FLAG = 0x40;
    var SECOND_OF_PAIR_FLAG = 0x80;
    var SECONDARY_ALIGNMNET_FLAG = 0x100;
    var READ_FAILS_VENDOR_QUALITY_CHECK_FLAG = 0x200;
    var DUPLICATE_READ_FLAG = 0x400;
    var SUPPLEMENTARY_ALIGNMENT_FLAG = 0x800;

    /**
     * readName
     * chr
     * cigar
     * lengthOnRef
     * start
     * seq
     * qual
     * mq
     * strand
     * blocks
     */

    igv.BamAlignment = function () {
        this.hidden = false;
    }

    igv.BamAlignment.prototype.isMapped = function () {
        return (this.flags & READ_UNMAPPED_FLAG) == 0;
    }

    igv.BamAlignment.prototype.isPaired = function () {
        return (this.flags & READ_PAIRED_FLAG) != 0;
    }

    igv.BamAlignment.prototype.isProperPair = function () {
        return (this.flags & PROPER_PAIR_FLAG) != 0;
    }

    igv.BamAlignment.prototype.isFirstOfPair = function () {
        return (this.flags & FIRST_OF_PAIR_FLAG) != 0;
    }

    igv.BamAlignment.prototype.isSecondOfPair = function () {
        return (this.flags & SECOND_OF_PAIR_FLAG) != 0;
    }

    igv.BamAlignment.prototype.isSecondary = function () {
        return (this.flags & SECONDARY_ALIGNMNET_FLAG) != 0;
    }

    igv.BamAlignment.prototype.isSupplementary = function () {
        return (this.flags & SUPPLEMENTARY_ALIGNMENT_FLAG) != 0;
    }

    igv.BamAlignment.prototype.isFailsVendorQualityCheck = function () {
        return (this.flags & READ_FAILS_VENDOR_QUALITY_CHECK_FLAG) != 0;
    }

    igv.BamAlignment.prototype.isDuplicate = function () {
        return (this.flags & DUPLICATE_READ_FLAG) != 0;
    }

    igv.BamAlignment.prototype.isMateMapped = function () {
        return (this.flags & MATE_UNMAPPED_FLAG) == 0;
    }

    igv.BamAlignment.prototype.isNegativeStrand = function () {
        return (this.flags & READ_STRAND_FLAG) != 0;
    }

    igv.BamAlignment.prototype.isMateNegativeStrand = function () {
        return (this.flags & MATE_STRAND_FLAG) != 0;
    }

    igv.BamAlignment.prototype.tags = function () {

        function decodeTags(ba) {

            var p = 0,
                len = ba.length,
                tags = {};

            while (p < len) {
                var tag = String.fromCharCode(ba[p]) + String.fromCharCode(ba[p + 1]);
                var type = String.fromCharCode(ba[p + 2]);
                var value;

                if (type == 'A') {
                    value = String.fromCharCode(ba[p + 3]);
                    p += 4;
                } else if (type === 'i' || type === 'I') {
                    value = readInt(ba, p + 3);
                    p += 7;
                } else if (type === 'c' || type === 'C') {
                    value = ba[p + 3];
                    p += 4;
                } else if (type === 's' || type === 'S') {
                    value = readShort(ba, p + 3);
                    p += 5;
                } else if (type === 'f') {
                    // TODO 'FIXME need floats';
                    value = readFloat(ba, p + 3);
                    p += 7;
                } else if (type === 'Z') {
                    p += 3;
                    value = '';
                    for (; ;) {
                        var cc = ba[p++];
                        if (cc === 0) {
                            break;
                        } else {
                            value += String.fromCharCode(cc);
                        }
                    }
                } else {
                    //'Unknown type ' + type;
                    value = 'Error unknown type: ' + type;
                    tags[tag] = value;
                    break;
                }
                tags[tag] = value;
            }
            return tags;
        }

        if (!this.tagDict) {
            if (this.tagBA) {
                this.tagDict = decodeTags(this.tagBA);
                this.tagBA = undefined;
            } else {
                this.tagDict = {};  // Mark so we don't try again.  The record has not tags
            }
        }
        return this.tagDict;

    }

    igv.BamAlignment.prototype.popupData = function (genomicLocation) {

        // if the user clicks on a base next to an insertion, show just the
        // inserted bases in a popup (like in desktop IGV).
        var nameValues = [], isFirst, tagDict;

        if(this.insertions) {
            for(var i = 0; i < this.insertions.length; i += 1) {
                var ins_start = this.insertions[i].start;
                if(genomicLocation == ins_start || genomicLocation == ins_start - 1) {
                    nameValues.push({name: 'Insertion', value: this.insertions[i].seq });
                    nameValues.push({name: 'Location', value: ins_start });
                    return nameValues;
                }
            }
        }

        nameValues.push({ name: 'Read Name', value: this.readName });

        // Sample
        // Read group
        nameValues.push("<hr>");

        // Add 1 to genomic location to map from 0-based computer units to user-based units
        nameValues.push({ name: 'Alignment Start', value: igv.numberFormatter(1 + this.start), borderTop: true });

        nameValues.push({ name: 'Read Strand', value: (true === this.strand ? '(+)' : '(-)'), borderTop: true });
        nameValues.push({ name: 'Cigar', value: this.cigar });
        nameValues.push({ name: 'Mapped', value: yesNo(this.isMapped()) });
        nameValues.push({ name: 'Mapping Quality', value: this.mq });
        nameValues.push({ name: 'Secondary', value: yesNo(this.isSecondary()) });
        nameValues.push({ name: 'Supplementary', value: yesNo(this.isSupplementary()) });
        nameValues.push({ name: 'Duplicate', value: yesNo(this.isDuplicate()) });
        nameValues.push({ name: 'Failed QC', value: yesNo(this.isFailsVendorQualityCheck()) });

        if (this.isPaired()) {
            nameValues.push("<hr>");
            nameValues.push({ name: 'First in Pair', value: !this.isSecondOfPair(), borderTop: true });
            nameValues.push({ name: 'Mate is Mapped', value: yesNo(this.isMateMapped()) });
            if (this.isMapped()) {
                nameValues.push({ name: 'Mate Chromosome', value: this.mate.chr });
                nameValues.push({ name: 'Mate Start', value: (this.mate.position + 1)});
                nameValues.push({ name: 'Mate Strand', value: (true === this.mate.strand ? '(+)' : '(-)')});
                nameValues.push({ name: 'Insert Size', value: this.fragmentLength });
                // Mate Start
                // Mate Strand
                // Insert Size
            }
            // First in Pair
            // Pair Orientation

        }

        nameValues.push("<hr>");
        tagDict = this.tags();
        isFirst = true;
        for (var key in tagDict) {

            if (tagDict.hasOwnProperty(key)) {

                if (isFirst) {
                    nameValues.push({ name: key, value: tagDict[key], borderTop: true });
                    isFirst = false;
                } else {
                    nameValues.push({ name: key, value: tagDict[key] });
                }

            }
        }

        return nameValues;


        function yesNo(bool) {
            return bool ? 'Yes' : 'No';
        }
    }


    function readInt(ba, offset) {
        return (ba[offset + 3] << 24) | (ba[offset + 2] << 16) | (ba[offset + 1] << 8) | (ba[offset]);
    }

    function readShort(ba, offset) {
        return (ba[offset + 1] << 8) | (ba[offset]);
    }

    function readFloat(ba, offset) {

        var dataView = new DataView(ba.buffer),
            littleEndian = true;

        return dataView.getFloat32(offset, littleEndian);

    }




    igv.BamFilter = function (options) {
        if (!options) options = {};
        this.vendorFailed = options.vendorFailed === undefined ? true : options.vendorFailed;
        this.duplicates = options.duplicates === undefined ? true : options.duplicates;
        this.secondary = options.secondary || false;
        this.supplementary = options.supplementary || false;
        this.mqThreshold = options.mqThreshold === undefined ? 0 : options.mqThreshold;
    }

    igv.BamFilter.prototype.pass = function (alignment) {

        if (this.vendorFailed && alignment.isFailsVendorQualityCheck()) return false;
        if (this.duplicates && alignment.isDuplicate()) return false;
        if (this.secondary && alignment.isSecondary()) return false;
        if (this.supplementary && alignment.isSupplementary()) return false;
        if (alignment.mq < this.mqThreshold) return false;

        return true;


    }

    return igv;

})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by turner on 2/10/15.
 */
var igv = (function (igv) {

    igv.BamAlignmentRow = function () {

        this.alignments = [];
        this.score = undefined;
    };

    igv.BamAlignmentRow.prototype.findCenterAlignment = function (bpStart, bpEnd) {

        var centerAlignment = undefined;

        // find single alignment that overlaps sort location
        this.alignments.forEach(function(a){

            if (undefined === centerAlignment) {

                if ((a.start + a.lengthOnRef) < bpStart || a.start > bpEnd) {
                    // do nothing
                } else {
                    centerAlignment = a;
                }

            }

        });

        return centerAlignment;
    };

    igv.BamAlignmentRow.prototype.updateScore = function (genomicLocation, genomicInterval, sortOption) {

        this.score = this.calculateScore(genomicLocation, (1 + genomicLocation), genomicInterval, sortOption);

    };

    igv.BamAlignmentRow.prototype.calculateScore = function (bpStart, bpEnd, interval, sortOption) {

        var baseScore,
            baseScoreFirst,
            baseScoreSecond,
            alignment,
            blockFirst,
            blockSecond,
            block;

        alignment = this.findCenterAlignment(bpStart, bpEnd);
        if (undefined === alignment) {
            return Number.MAX_VALUE;
        }

        baseScoreFirst = baseScoreSecond = undefined;

        if ("NUCLEOTIDE" === sortOption.sort) {

            if (alignment.blocks && alignment.blocks.length > 0) {
                blockFirst = blockAtGenomicLocation(alignment.blocks, bpStart, interval.start);
                if (blockFirst) {
                    baseScoreFirst = blockScoreWithObject(blockFirst, interval);
                }
                // baseScoreFirst = nucleotideBlockScores(alignment.blocks);
            }

            if (alignment.firstAlignment && alignment.firstAlignment.blocks && alignment.firstAlignment.blocks.length > 0) {
                blockFirst = blockAtGenomicLocation(alignment.firstAlignment.blocks, bpStart, interval.start);
                if (blockFirst) {
                    baseScoreFirst = blockScoreWithObject(blockFirst, interval);
                }
                // baseScoreFirst = nucleotideBlockScores(alignment.firstAlignment.blocks);
            }

            if (alignment.secondAlignment && alignment.secondAlignment.blocks && alignment.secondAlignment.blocks.length > 0) {
                blockSecond = blockAtGenomicLocation(alignment.secondAlignment.blocks, bpStart, interval.start);
                if (blockSecond) {
                    baseScoreSecond = blockScoreWithObject(blockSecond, interval);
                }
                // baseScoreSecond = nucleotideBlockScores(alignment.secondAlignment.blocks);
            }

            baseScore = (undefined === baseScoreFirst) ? baseScoreSecond : baseScoreFirst;

            return (undefined === baseScore) ? Number.MAX_VALUE : baseScore;
        } else if ("STRAND" === sortOption.sort) {

            return alignment.strand ? 1 : -1;
        } else if ("START" === sortOption.sort) {

            return alignment.start;
        }

        return Number.MAX_VALUE;

        function blockAtGenomicLocation(blocks, genomicLocation, genomicIntervalStart) {

            var result = undefined;

            blocks.forEach(function (block) {

                for (var i = 0, genomicOffset = block.start - genomicIntervalStart, blockLocation = block.start, blockSequenceLength = block.seq.length;
                     i < blockSequenceLength;
                     i++, genomicOffset++, blockLocation++) {

                    if (genomicLocation === blockLocation) {
                        result = { block: block, blockSeqIndex: i, referenceSequenceIndex: genomicOffset, location: genomicLocation };
                    }

                }

            });

            return result;
        }

        function blockScoreWithObject(obj, interval) {

            var reference,
                base,
                coverage,
                count,
                phred;

            if ("*" === obj.block.seq) {
                return 3;
            }

            reference = interval.sequence.charAt(obj.referenceSequenceIndex);
            base = obj.block.seq.charAt(obj.blockSeqIndex);

            if ("=" === base) {
                base = reference;
            }

            if ('N' === base) {
                return 2;
            } else if (reference === base) {
                return 3;
            } else if ("X" === base|| reference !== base) {

                coverage = interval.coverageMap.coverage[ (obj.location - interval.coverageMap.bpStart) ];

                count = coverage[ "pos" + base ] + coverage[ "neg" + base ];
                phred = (coverage.qual) ? coverage.qual : 0;

                return -(count + (phred / 1000.0));
            }

            return undefined;
        }

        function nucleotideBlockScores(blocks) {

            var result = undefined;

            blocks.forEach(function (block) {

                var sequence = interval.sequence,
                    coverageMap = interval.coverageMap,
                    reference,
                    base,
                    coverage,
                    count,
                    phred;

                if ("*" === block.seq) {
                    result = 3;
                }

                for (var i = 0, indexReferenceSequence = block.start - interval.start, bpBlockSequence = block.start, lengthBlockSequence = block.seq.length;
                     i < lengthBlockSequence;
                     i++, indexReferenceSequence++, bpBlockSequence++) {

                    if (bpStart !== bpBlockSequence) {
                        continue;
                    }

                    reference = sequence.charAt(indexReferenceSequence);
                    base = block.seq.charAt(i);

                    if (base === "=") {
                        base = reference;
                    }

                    if (base === 'N') {
                        result = 2;
                    }
                    else if (base === reference) {
                        result = 3;
                    }
                    else if (base === "X" || base !== reference){

                        coverage = coverageMap.coverage[ (bpBlockSequence - coverageMap.bpStart) ];
                        count = coverage[ "pos" + base ] + coverage[ "neg" + base ];
                        phred = (coverage.qual) ? coverage.qual : 0;
                        result = -(count + (phred / 1000.0));
                    } else {
                        console.log("BamAlignmentRow.caculateScore - huh?");
                    }

                } // for (i < lengthBlockSequence)

            });

            return result;
        }
    };

    return igv;

})(igv || {});

// Represents a BAM index.
// Code is based heavily on bam.js, part of the Dalliance Genome Explorer,  (c) Thomas Down 2006-2001.

var igv = (function (igv) {


    const BAI_MAGIC = 21578050;
    const TABIX_MAGIC = 21578324;
    const MAX_HEADER_SIZE = 100000000;   // IF the header is larger than this we can't read it !
    const MAX_GZIP_BLOCK_SIZE = (1 << 16);


    /**
     * @param indexURL
     * @param config
     * @param tabix
     *
     * @returns a Promised for the bam or tabix index.  The fulfill function takes the index as an argument.
     */
    igv.loadBamIndex = function (indexURL, config, tabix) {

        return new Promise(function (fulfill, reject) {

            var genome = igv.browser ? igv.browser.genome : null;

            igvxhr.loadArrayBuffer(indexURL,
                {
                    headers: config.headers,
                    withCredentials: config.withCredentials
                }).then(function (arrayBuffer) {

                var indices = [],
                    magic, nbin, nintv, nref, parser,
                    blockMin = Number.MAX_VALUE,
                    blockMax = 0,
                    binIndex, linearIndex, binNumber, cs, ce, b, i, ref, sequenceIndexMap;

                if (!arrayBuffer) {
                    fulfill(null);
                    return;
                }

                if (tabix) {
                    var inflate = new Zlib.Gunzip(new Uint8Array(arrayBuffer));
                    arrayBuffer = inflate.decompress().buffer;
                }

                parser = new igv.BinaryParser(new DataView(arrayBuffer));

                magic = parser.getInt();

                if (magic === BAI_MAGIC || (tabix && magic === TABIX_MAGIC)) {

                    nref = parser.getInt();


                    if (tabix) {
                        // Tabix header parameters aren't used, but they must be read to advance the pointer
                        var format = parser.getInt();
                        var col_seq = parser.getInt();
                        var col_beg = parser.getInt();
                        var col_end = parser.getInt();
                        var meta = parser.getInt();
                        var skip = parser.getInt();
                        var l_nm = parser.getInt();

                        sequenceIndexMap = {};
                        for (i = 0; i < nref; i++) {
                            var seq_name = parser.getString();

                            // Translate to "official" chr name.
                            if (genome) seq_name = genome.getChromosomeName(seq_name);

                            sequenceIndexMap[seq_name] = i;
                        }
                    }

                    for (ref = 0; ref < nref; ++ref) {

                        binIndex = {};
                        linearIndex = [];

                        nbin = parser.getInt();

                        for (b = 0; b < nbin; ++b) {

                            binNumber = parser.getInt();

                            if (binNumber === 37450) {
                                // This is a psuedo bin, not used but we have to consume the bytes
                                nchnk = parser.getInt(); // # of chunks for this bin
                                cs = parser.getVPointer();   // unmapped beg
                                ce = parser.getVPointer();   // unmapped end
                                var n_maped = parser.getLong();
                                var nUnmapped = parser.getLong();

                            }
                            else {

                                binIndex[binNumber] = [];
                                var nchnk = parser.getInt(); // # of chunks for this bin

                                for (i = 0; i < nchnk; i++) {
                                    cs = parser.getVPointer();    //chunk_beg
                                    ce = parser.getVPointer();    //chunk_end
                                    if (cs && ce) {
                                        if (cs.block < blockMin) {
                                            blockMin = cs.block;    // Block containing first alignment
                                        }
                                        if (ce.block > blockMax) {
                                            blockMax = ce.block;
                                        }
                                        binIndex[binNumber].push([cs, ce]);
                                    }
                                }
                            }
                        }


                        nintv = parser.getInt();
                        for (i = 0; i < nintv; i++) {
                            cs = parser.getVPointer();
                            linearIndex.push(cs);   // Might be null
                        }

                        if (nbin > 0) {
                            indices[ref] = {
                                binIndex: binIndex,
                                linearIndex: linearIndex
                            }
                        }
                    }

                } else {
                    throw new Error(indexURL + " is not a " + (tabix ? "tabix" : "bai") + " file");
                }
                fulfill(new igv.BamIndex(indices, blockMin, blockMax, sequenceIndexMap, tabix));
            }).catch(reject);
        })
    }


    igv.BamIndex = function (indices, blockMin, blockMax, sequenceIndexMap, tabix) {
        this.firstAlignmentBlock = blockMin;
        this.lastAlignmentBlock = blockMax;
        this.indices = indices;
        this.sequenceIndexMap = sequenceIndexMap;
        this.tabix = tabix;

    }

    /**
     * Fetch blocks for a particular genomic range.  This method is public so it can be unit-tested.
     *
     * @param refId  the sequence dictionary index of the chromosome
     * @param min  genomic start position
     * @param max  genomic end position
     * @param return an array of {minv: {filePointer, offset}, {maxv: {filePointer, offset}}
     */
    igv.BamIndex.prototype.blocksForRange = function (refId, min, max) {

        var bam = this,
            ba = bam.indices[refId],
            overlappingBins,
            chunks,
            nintv,
            lowest,
            minLin,
            maxLin,
            vp,
            i;


        if (!ba) {
            return [];
        }
        else {

            overlappingBins = reg2bins(min, max);        // List of bin #s that overlap min, max
            chunks = [];

            // Find chunks in overlapping bins.  Leaf bins (< 4681) are not pruned
            overlappingBins.forEach(function (bin) {
                if (ba.binIndex[bin]) {
                    var binChunks = ba.binIndex[bin],
                        nchnk = binChunks.length;
                    for (var c = 0; c < nchnk; ++c) {
                        var cs = binChunks[c][0];
                        var ce = binChunks[c][1];
                        chunks.push({minv: cs, maxv: ce, bin: bin});
                    }
                }
            });

            // Use the linear index to find minimum file position of chunks that could contain alignments in the region
            nintv = ba.linearIndex.length;
            lowest = null;
            minLin = Math.min(min >> 14, nintv - 1);
            maxLin = Math.min(max >> 14, nintv - 1);
            for (i = minLin; i <= maxLin; ++i) {
                vp = ba.linearIndex[i];
                if (vp) {
                    // todo -- I think, but am not sure, that the values in the linear index have to be in increasing order.  So the first non-null should be minimum
                    if (!lowest || vp.isLessThan(lowest)) {
                        lowest = vp;
                    }
                }
            }
            
            return optimizeChunks(chunks, lowest);
        }

    };


    function optimizeChunks(chunks, lowest) {

        var mergedChunks = [],
            lastChunk = null;

        if (chunks.length === 0) return chunks;

        chunks.sort(function (c0, c1) {
            var dif = c0.minv.block - c1.minv.block;
            if (dif != 0) {
                return dif;
            } else {
                return c0.minv.offset - c1.minv.offset;
            }
        });

        chunks.forEach(function (chunk) {

            if(chunk.maxv.isGreaterThan(lowest)) {
                if (lastChunk === null) {
                    mergedChunks.push(chunk);
                    lastChunk = chunk;
                }
                else {
                    if ((chunk.minv.block - lastChunk.maxv.block) < 65000) { // Merge chunks that are withing 65k of each other
                        if (chunk.maxv.isGreaterThan(lastChunk.maxv)) {
                            lastChunk.maxv = chunk.maxv;
                        }
                    }
                    else {
                        mergedChunks.push(chunk);
                        lastChunk = chunk;
                    }
                }
            }
        });

        return mergedChunks;
    }

    /**
     * Calculate the list of bins that overlap with region [beg, end]
     *
     */
    function reg2bins(beg, end) {
        var i = 0, k, list = [];
        if (end >= 1 << 29)   end = 1 << 29;
        --end;
        list.push(0);
        for (k = 1 + (beg >> 26); k <= 1 + (end >> 26); ++k) list.push(k);
        for (k = 9 + (beg >> 23); k <= 9 + (end >> 23); ++k) list.push(k);
        for (k = 73 + (beg >> 20); k <= 73 + (end >> 20); ++k) list.push(k);
        for (k = 585 + (beg >> 17); k <= 585 + (end >> 17); ++k) list.push(k);
        for (k = 4681 + (beg >> 14); k <= 4681 + (end >> 14); ++k) list.push(k);
        return list;
    }


    return igv;

})(igv || {});
// Represents a BAM file.
// Code is based heavily on bam.js, part of the Dalliance Genome Explorer,  (c) Thomas Down 2006-2001.

var igv = (function (igv) {

    var BAM_MAGIC = 21840194;
    var BAI_MAGIC = 21578050;
    var SECRET_DECODER = ['=', 'A', 'C', 'x', 'G', 'x', 'x', 'x', 'T', 'x', 'x', 'x', 'x', 'x', 'x', 'N'];
    var CIGAR_DECODER = ['M', 'I', 'D', 'N', 'S', 'H', 'P', '=', 'X', '?', '?', '?', '?', '?', '?', '?'];
    var READ_STRAND_FLAG = 0x10;
    var MATE_STRAND_FLAG = 0x20;
    var FIRST_OF_PAIR_FLAG = 0x40;
    var SECOND_OF_PAIR_FLAG = 0x80;
    var NOT_PRIMARY_ALIGNMENT_FLAG = 0x100;
    var READ_FAILS_VENDOR_QUALITY_CHECK_FLAG = 0x200;
    var DUPLICATE_READ_FLAG = 0x400;
    var SUPPLEMENTARY_FLAG = 0x800;

    const MAX_GZIP_BLOCK_SIZE = 65536;   //  APPARENTLY.  Where is this documented???
    const DEFAULT_SAMPLING_WINDOW_SIZE = 100;
    const DEFAULT_SAMPLING_DEPTH = 50;
    const MAXIMUM_SAMPLING_DEPTH = 2500;

    /**
     * Class for reading a bam file
     *
     * @param config
     * @constructor
     */
    igv.BamReader = function (config) {

        this.config = config;

        this.filter = config.filter || new igv.BamFilter();

        this.bamPath = 'gcs' === config.sourceType ?
            igv.translateGoogleCloudURL(config.url) :
            config.url;
        this.baiPath = 'gcs' === config.sourceType ?
            igv.translateGoogleCloudURL(config.url + ".bai") :
        config.url + ".bai"; // Todo - deal with Picard convention.  WHY DOES THERE HAVE TO BE 2?
        this.baiPath = config.indexURL || this.baiPath; // If there is an indexURL provided, use it!
        this.headPath = config.headURL || this.bamPath;


        this.samplingWindowSize = config.samplingWindowSize === undefined ? DEFAULT_SAMPLING_WINDOW_SIZE : config.samplingWindowSize;
        this.samplingDepth = config.samplingDepth === undefined ? DEFAULT_SAMPLING_DEPTH : config.samplingDepth;
        if(this.samplingDepth > MAXIMUM_SAMPLING_DEPTH) {
            igv.log("Warning: attempt to set sampling depth > maximum value of 2500");
            this.samplingDepth = MAXIMUM_SAMPLING_DEPTH;
        }

        if (config.viewAsPairs) {
            this.pairsSupported = true;
        }
        else {
            this.pairsSupported = config.pairsSupported === undefined ? true : config.pairsSupported;
        }

    };

    igv.BamReader.prototype.readAlignments = function (chr, bpStart, bpEnd) {

        var self = this;

        return new Promise(function (fulfill, reject) {


            getChrIndex(self).then(function (chrToIndex) {

                var chrId = chrToIndex[chr],

                    alignmentContainer = new igv.AlignmentContainer(chr, bpStart, bpEnd, self.samplingWindowSize, self.samplingDepth, self.pairsSupported);

                if (chrId === undefined) {
                    fulfill(alignmentContainer);
                } else {

                    getIndex(self).then(function (bamIndex) {

                        var chunks = bamIndex.blocksForRange(chrId, bpStart, bpEnd),
                            promises = [];


                        if (!chunks) {
                            fulfill(null);
                            reject("Error reading bam index");
                            return;
                        }
                        if (chunks.length === 0) {
                            fulfill(alignmentContainer);
                            return;
                        }

                        chunks.forEach(function (c) {

                            promises.push(new Promise(function (fulfill, reject) {

                                var fetchMin = c.minv.block,
                                    fetchMax = c.maxv.block + 65000,   // Make sure we get the whole block.
                                    range = {start: fetchMin, size: fetchMax - fetchMin + 1};

                                igvxhr.loadArrayBuffer(self.bamPath,
                                    {
                                        headers: self.config.headers,
                                        range: range,
                                        withCredentials: self.config.withCredentials
                                    }).then(function (compressed) {

                                    var ba = new Uint8Array(igv.unbgzf(compressed)); //new Uint8Array(igv.unbgzf(compressed)); //, c.maxv.block - c.minv.block + 1));
                                    decodeBamRecords(ba, c.minv.offset, alignmentContainer, bpStart, bpEnd, chrId, self.filter);

                                    fulfill(alignmentContainer);

                                }).catch(function (obj) {
                                    reject(obj);
                                });

                            }))
                        });


                        Promise.all(promises).then(function (ignored) {
                            alignmentContainer.finish();
                            fulfill(alignmentContainer);
                        }).catch(function (obj) {
                            reject(obj);
                        });
                    }).catch(reject);
                }
            }).catch(reject);
        });


        function decodeBamRecords(ba, offset, alignments, min, max, chrId, filter) {

            var blockSize,
                blockEnd,
                alignment,
                blocks,
                refID,
                pos,
                bmn,
                bin,
                mq,
                nl,
                flag_nc,
                flag,
                nc,
                lseq,
                mateRefID,
                matePos,
                readName,
                j,
                p,
                lengthOnRef,
                cigar,
                c,
                cigarArray,
                seq,
                seqBytes;

            while (true) {

                blockSize = readInt(ba, offset);
                blockEnd = offset + blockSize + 4;

                if (blockEnd > ba.length) {
                    return;
                }

                alignment = new igv.BamAlignment();

                refID = readInt(ba, offset + 4);
                pos = readInt(ba, offset + 8);

                if (refID > chrId || pos > max) return;  // We've gone off the right edge => we're done
                else if (refID < chrId) continue;    // Not sure this is possible

                bmn = readInt(ba, offset + 12);
                bin = (bmn & 0xffff0000) >> 16;
                mq = (bmn & 0xff00) >> 8;
                nl = bmn & 0xff;

                flag_nc = readInt(ba, offset + 16);
                flag = (flag_nc & 0xffff0000) >> 16;
                nc = flag_nc & 0xffff;

                alignment.flags = flag;
                alignment.strand = !(flag & READ_STRAND_FLAG);

                lseq = readInt(ba, offset + 20);

                mateRefID = readInt(ba, offset + 24);
                matePos = readInt(ba, offset + 28);
                alignment.fragmentLength = readInt(ba, offset + 32);

                readName = '';
                for (j = 0; j < nl - 1; ++j) {
                    readName += String.fromCharCode(ba[offset + 36 + j]);
                }

                p = offset + 36 + nl;

                lengthOnRef = 0;
                cigar = '';


                cigarArray = [];
                for (c = 0; c < nc; ++c) {
                    var cigop = readInt(ba, p);
                    var opLen = (cigop >> 4);
                    var opLtr = CIGAR_DECODER[cigop & 0xf];
                    if (opLtr == 'M' || opLtr == 'EQ' || opLtr == 'X' || opLtr == 'D' || opLtr == 'N' || opLtr == '=')
                        lengthOnRef += opLen;
                    cigar = cigar + opLen + opLtr;
                    p += 4;

                    cigarArray.push({len: opLen, ltr: opLtr});
                }
                alignment.cigar = cigar;
                alignment.lengthOnRef = lengthOnRef;

                if (alignment.start + alignment.lengthOnRef < min) continue;  // Record out-of-range "to the left", skip to next one


                seq = '';
                seqBytes = (lseq + 1) >> 1;
                for (j = 0; j < seqBytes; ++j) {
                    var sb = ba[p + j];
                    seq += SECRET_DECODER[(sb & 0xf0) >> 4];
                    seq += SECRET_DECODER[(sb & 0x0f)];
                }
                seq = seq.substring(0, lseq);  // seq might have one extra character (if lseq is an odd number)

                p += seqBytes;
                alignment.seq = seq;


                if (lseq === 1 && String.fromCharCode(ba[p + j] + 33) === "*") {
                    // TODO == how to represent this?
                }
                else {
                    alignment.qual = [];
                    for (j = 0; j < lseq; ++j) {
                        alignment.qual.push(ba[p + j]);
                    }
                }
                p += lseq;


                alignment.start = pos;
                alignment.mq = mq;
                alignment.readName = readName;
                alignment.chr = self.indexToChr[refID];

                if (mateRefID >= 0) {
                    alignment.mate = {
                        chr: self.indexToChr[mateRefID],
                        position: matePos,
                        strand: !(flag & MATE_STRAND_FLAG)
                    };
                }


                alignment.tagBA = new Uint8Array(ba.buffer.slice(p, blockEnd));  // decode thiese on demand
                p += blockEnd;

                if (!min || alignment.start <= max &&
                    alignment.start + alignment.lengthOnRef >= min &&
                    filter.pass(alignment)) {
                    if (chrId === undefined || refID == chrId) {
                        blocks = makeBlocks(alignment, cigarArray);
                        alignment.blocks = blocks.blocks;
                        alignment.insertions = blocks.insertions;
                        alignments.push(alignment);
                    }
                }
                offset = blockEnd;
            }
            // Exits via top of loop.
        }

        /**
         * Split the alignment record into blocks as specified in the cigarArray.  Each aligned block contains
         * its portion of the read sequence and base quality strings.  A read sequence or base quality string
         * of "*" indicates the value is not recorded.  In all other cases the length of the block sequence (block.seq)
         * and quality string (block.qual) must == the block length.
         *
         * NOTE: Insertions are not yet treated // TODO
         *
         * @param record
         * @param cigarArray
         * @returns array of blocks
         */
        function makeBlocks(record, cigarArray) {

            var blocks = [],
                insertions,
                seqOffset = 0,
                pos = record.start,
                len = cigarArray.length,
                blockSeq,
                blockQuals,
                gapType,
                minQ = 5,  //prefs.getAsInt(PreferenceManager.SAM_BASE_QUALITY_MIN)
                maxQ = 20; //prefs.getAsInt(PreferenceManager.SAM_BASE_QUALITY_MAX)

            for (var i = 0; i < len; i++) {

                var c = cigarArray[i];

                switch (c.ltr) {
                    case 'H' :
                        break; // ignore hard clips
                    case 'P' :
                        break; // ignore pads
                    case 'S' :
                        seqOffset += c.len;
                        gapType = 'S';
                        break; // soft clip read bases
                    case 'N' :
                        pos += c.len;
                        gapType = 'N';
                        break;  // reference skip
                    case 'D' :
                        pos += c.len;
                        gapType = 'D';
                        break;
                    case 'I' :
                        blockSeq = record.seq === "*" ? "*" : record.seq.substr(seqOffset, c.len);
                        blockQuals = record.qual ? record.qual.slice(seqOffset, c.len) : undefined;
                        if (insertions === undefined) insertions = [];
                        insertions.push({start: pos, len: c.len, seq: blockSeq, qual: blockQuals});
                        seqOffset += c.len;
                        break;
                    case 'M' :
                    case 'EQ' :
                    case '=' :
                    case 'X' :

                        blockSeq = record.seq === "*" ? "*" : record.seq.substr(seqOffset, c.len);
                        blockQuals = record.qual ? record.qual.slice(seqOffset, c.len) : undefined;
                        blocks.push({start: pos, len: c.len, seq: blockSeq, qual: blockQuals, gapType: gapType});
                        seqOffset += c.len;
                        pos += c.len;

                        break;

                    default :
                        console.log("Error processing cigar element: " + c.len + c.ltr);
                }
            }

            return {blocks: blocks, insertions: insertions};

        }
    }

    igv.BamReader.prototype.readHeader = function () {

        var self = this;

        return new Promise(function (fulfill, reject) {

            getIndex(self).then(function (index) {

                var len = index.firstAlignmentBlock + MAX_GZIP_BLOCK_SIZE;   // Insure we get the complete compressed block containing the header

                igvxhr.loadArrayBuffer(self.bamPath,
                    {
                        headers: self.config.headers,

                        range: {start: 0, size: len},

                        withCredentials: self.config.withCredentials
                    }).then(function (compressedBuffer) {

                    var unc = igv.unbgzf(compressedBuffer, len),
                        uncba = new Uint8Array(unc),
                        magic = readInt(uncba, 0),
                        samHeaderLen = readInt(uncba, 4),
                        samHeader = '',
                        genome = igv.browser ? igv.browser.genome : null;

                    for (var i = 0; i < samHeaderLen; ++i) {
                        samHeader += String.fromCharCode(uncba[i + 8]);
                    }

                    var nRef = readInt(uncba, samHeaderLen + 8);
                    var p = samHeaderLen + 12;

                    self.chrToIndex = {};
                    self.indexToChr = [];
                    for (var i = 0; i < nRef; ++i) {
                        var lName = readInt(uncba, p);
                        var name = '';
                        for (var j = 0; j < lName - 1; ++j) {
                            name += String.fromCharCode(uncba[p + 4 + j]);
                        }
                        var lRef = readInt(uncba, p + lName + 4);
                        //dlog(name + ': ' + lRef);

                        if (genome && genome.getChromosomeName) {
                            name = genome.getChromosomeName(name);
                        }

                        self.chrToIndex[name] = i;
                        self.indexToChr.push(name);

                        p = p + 8 + lName;
                    }

                    fulfill();

                }).catch(reject);
            }).catch(reject);
        });
    }

//
    function getIndex(bam) {

        return new Promise(function (fulfill, reject) {

            if (bam.index) {
                fulfill(bam.index);
            }
            else {
                igv.loadBamIndex(bam.baiPath, bam.config).then(function (index) {
                    bam.index = index;

                    fulfill(bam.index);
                }).catch(reject);
            }
        });
    }


    function getChrIndex(bam) {

        return new Promise(function (fulfill, reject) {

            if (bam.chrToIndex) {
                fulfill(bam.chrToIndex);
            }
            else {
                bam.readHeader().then(function () {
                    fulfill(bam.chrToIndex);
                }).catch(reject);
            }
        });
    }

    function readInt(ba, offset) {
        return (ba[offset + 3] << 24) | (ba[offset + 2] << 16) | (ba[offset + 1] << 8) | (ba[offset]);
    }

    function readShort(ba, offset) {
        return (ba[offset + 1] << 8) | (ba[offset]);
    }

    return igv;

})
(igv || {});



/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {


    igv.BamSource = function (config) {

        this.config = config;
        this.alignmentContainer = undefined;
        this.maxRows = config.maxRows || 1000;

        if (config.sourceType === "ga4gh") {
            this.bamReader = new igv.Ga4ghAlignmentReader(config);
        }
        else {
            this.bamReader = new igv.BamReader(config);
        }

       this.viewAsPairs = config.viewAsPairs;
    };

    igv.BamSource.prototype.setViewAsPairs = function (bool) {
        var self = this;

        if (this.viewAsPairs !== bool) {
            this.viewAsPairs = bool;
            // TODO -- repair alignments
            if (this.alignmentContainer) {
                var alignmentContainer = this.alignmentContainer,
                    alignments;

                if (bool) {
                    alignments = pairAlignments(alignmentContainer.packedAlignmentRows);
                }
                else {
                    alignments = unpairAlignments(alignmentContainer.packedAlignmentRows);
                }
                alignmentContainer.packedAlignmentRows = packAlignmentRows(alignments, alignmentContainer.start, alignmentContainer.end, self.maxRows);

            }
        }
    }

    igv.BamSource.prototype.getAlignments = function (chr, bpStart, bpEnd) {

        var self = this;
        return new Promise(function (fulfill, reject) {

            if (self.alignmentContainer && self.alignmentContainer.contains(chr, bpStart, bpEnd)) {
                fulfill(self.alignmentContainer);
            } else {

                self.bamReader.readAlignments(chr, bpStart, bpEnd).then(function (alignmentContainer) {

                    var maxRows = self.config.maxRows || 500,
                        alignments = alignmentContainer.alignments;

                    if (!self.viewAsPairs) {
                        alignments = unpairAlignments([{alignments: alignments}]);
                    }

                    alignmentContainer.packedAlignmentRows = packAlignmentRows(alignments, alignmentContainer.start, alignmentContainer.end, maxRows);


                    alignmentContainer.alignments = undefined;  // Don't need to hold onto these anymore
                    self.alignmentContainer = alignmentContainer;

                    igv.browser.genome.sequence.getSequence(alignmentContainer.chr, alignmentContainer.start, alignmentContainer.end).then(
                        function (sequence) {


                            if (sequence) {

                                alignmentContainer.coverageMap.refSeq = sequence;    // TODO -- fix this
                                alignmentContainer.sequence = sequence;           // TODO -- fix this


                                fulfill(alignmentContainer);
                            }
                        }).catch(reject);

                }).catch(reject);
            }
        });
    }

    function pairAlignments(rows) {

        var pairCache = {},
            result = [];

        rows.forEach(function (row) {

            row.alignments.forEach(function (alignment) {

                var pairedAlignment;

                if (canBePaired(alignment)) {

                    pairedAlignment = pairCache[alignment.readName];
                    if (pairedAlignment) {
                        pairedAlignment.setSecondAlignment(alignment);
                        pairCache[alignment.readName] = undefined;   // Don't need to track this anymore.
                    }
                    else {
                        pairedAlignment = new igv.PairedAlignment(alignment);
                        pairCache[alignment.readName] = pairedAlignment;
                        result.push(pairedAlignment);
                    }
                }

                else {
                    result.push(alignment);
                }
            });
        });
        return result;
    }

    function unpairAlignments(rows) {
        var result = [];
        rows.forEach(function (row) {
            row.alignments.forEach(function (alignment) {
                if (alignment instanceof igv.PairedAlignment) {
                    if (alignment.firstAlignment) result.push(alignment.firstAlignment);  // shouldn't need the null test
                    if (alignment.secondAlignment) result.push(alignment.secondAlignment);

                }
                else {
                    result.push(alignment);
                }
            });
        });
        return result;
    }

    function canBePaired(alignment) {
        return alignment.isPaired() &&
            alignment.isMateMapped() &&
            alignment.chr === alignment.mate.chr &&
            (alignment.isFirstOfPair() || alignment.isSecondOfPair()) && !(alignment.isSecondary() || alignment.isSupplementary());
    }


    function packAlignmentRows(alignments, start, end, maxRows) {

        if (!alignments) return;

        alignments.sort(function (a, b) {
            return a.start - b.start;
        });

        if (alignments.length === 0) {

            return [];

        } else {

            var bucketList = [],
                allocatedCount = 0,
                lastAllocatedCount = 0,
                nextStart = start,
                alignmentRow,
                index,
                bucket,
                alignment,
                alignmentSpace = 4 * 2,
                packedAlignmentRows = [],
                bucketStart = Math.max(start, alignments[0].start);

            alignments.forEach(function (alignment) {

                var buckListIndex = Math.max(0, alignment.start - bucketStart);
                if (bucketList[buckListIndex] === undefined) {
                    bucketList[buckListIndex] = [];
                }
                bucketList[buckListIndex].push(alignment);
            });


            while (allocatedCount < alignments.length && packedAlignmentRows.length < maxRows) {

                alignmentRow = new igv.BamAlignmentRow();

                while (nextStart <= end) {

                    bucket = undefined;

                    while (!bucket && nextStart <= end) {

                        index = nextStart - bucketStart;
                        if (bucketList[index] === undefined) {
                            ++nextStart;                     // No alignments at this index
                        } else {
                            bucket = bucketList[index];
                        }

                    } // while (bucket)

                    if (!bucket) {
                        break;
                    }
                    alignment = bucket.pop();
                    if (0 === bucket.length) {
                        bucketList[index] = undefined;
                    }

                    alignmentRow.alignments.push(alignment);
                    nextStart = alignment.start + alignment.lengthOnRef + alignmentSpace;
                    ++allocatedCount;

                } // while (nextStart)

                if (alignmentRow.alignments.length > 0) {
                    packedAlignmentRows.push(alignmentRow);
                }

                nextStart = bucketStart;

                if (allocatedCount === lastAllocatedCount) break;   // Protect from infinite loops

                lastAllocatedCount = allocatedCount;

            } // while (allocatedCount)

            return packedAlignmentRows;
        }
    }


    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


var igv = (function (igv) {

    var alignmentRowYInset = 0;
    var alignmentStartGap = 5;
    var downsampleRowHeight = 5;
    const DEFAULT_COVERAGE_TRACK_HEIGHT = 50;

    igv.BAMTrack = function (config) {

        this.featureSource = new igv.BamSource(config);

        igv.configTrack(this, config);

        if(config.coverageTrackHeight === undefined) {
            config.coverageTrackHeight = DEFAULT_COVERAGE_TRACK_HEIGHT;
        }

        this.coverageTrack = new CoverageTrack(config, this);

        this.alignmentTrack = new AlignmentTrack(config, this);

        this.visibilityWindow = config.visibilityWindow || 30000;     // 30kb default

        this.viewAsPairs = config.viewAsPairs;

        this.pairsSupported = config.pairsSupported === undefined ? true : false;

        this.color = config.color || "rgb(185, 185, 185)";

        // sort alignment rows
        this.sortOption = config.sortOption || {sort: "NUCLEOTIDE"};
        this.sortDirection = true;

        // filter alignments
        this.filterOption = config.filterOption || {name: "mappingQuality", params: [30, undefined]};

    };

    igv.BAMTrack.prototype.getFeatures = function (chr, bpStart, bpEnd) {
        return this.featureSource.getAlignments(chr, bpStart, bpEnd);
    };

    igv.BAMTrack.filters = {

        noop: function () {
            return function (alignment) {
                return false;
            };
        },

        strand: function (strand) {
            return function (alignment) {
                return alignment.strand === strand;
            };
        },

        mappingQuality: function (lower, upper) {
            return function (alignment) {

                if (lower && alignment.mq < lower) {
                    return true;
                }

                if (upper && alignment.mq > upper) {
                    return true;
                }

                return false;
            }
        }
    };

    // Alt - Click to Sort alignment rows
    igv.BAMTrack.prototype.altClick = function (genomicLocation, event) {

        this.alignmentTrack.sortAlignmentRows(genomicLocation, this.sortOption);

        this.trackView.redrawTile(this.featureSource.alignmentContainer);
        $(this.trackView.viewportDiv).scrollTop(0);

        this.sortDirection = !this.sortDirection;
    };

    /**
     * Optional method to compute pixel height to accomodate the list of features.  The implementation below
     * has side effects (modifiying the samples hash).  This is unfortunate, but harmless.
     *
     * @param alignmentContainer
     * @returns {number}
     */
    igv.BAMTrack.prototype.computePixelHeight = function (alignmentContainer) {

        return this.coverageTrack.computePixelHeight(alignmentContainer) +
            this.alignmentTrack.computePixelHeight(alignmentContainer);

    };

    igv.BAMTrack.prototype.draw = function (options) {

        if(this.coverageTrack.height > 0) {
            this.coverageTrack.draw(options);
        }

        this.alignmentTrack.draw(options);
    };

    igv.BAMTrack.prototype.paintAxis = function (ctx, pixelWidth, pixelHeight) {

        this.coverageTrack.paintAxis(ctx, pixelWidth, this.coverageTrackHeight);

    };

    igv.BAMTrack.prototype.popupData = function (genomicLocation, xOffset, yOffset) {

        if (yOffset >= this.coverageTrack.top && yOffset < this.coverageTrack.height) {
            return this.coverageTrack.popupData(genomicLocation, xOffset, this.coverageTrack.top);
        }
        else {
            return this.alignmentTrack.popupData(genomicLocation, xOffset, yOffset - this.alignmentTrack.top);
        }

    };

    igv.BAMTrack.prototype.popupMenuItems = function (popover) {

        var self = this,
            html,
            menuItems = [],
            colorByMenuItems = [],
            tagLabel = 'tag' + (self.alignmentTrack.colorByTag ? ' (' + self.alignmentTrack.colorByTag + ')' : ''),
            selected;

        menuItems.push(igv.colorPickerMenuItem(popover, this.trackView));

        menuItems.push(sortMenuItem(popover));

        colorByMenuItems.push({key: 'none', label: 'track color'});

        if(!self.viewAsPairs) {
            colorByMenuItems.push({key: 'strand', label: 'read strand'});
        }
        if (self.pairsSupported && self.alignmentTrack.hasPairs) {
            colorByMenuItems.push({key: 'firstOfPairStrand', label: 'first-of-pair strand'});
        }
        colorByMenuItems.push({key: 'tag', label: tagLabel});

        menuItems.push('<div class="igv-track-menu-category igv-track-menu-border-top">Color by</div>');

        colorByMenuItems.forEach(function (item) {
            selected = self.alignmentTrack.colorBy === item.key;
            menuItems.push(colorByMarkup(item, selected));
        });

        html = [];
        if (self.pairsSupported && self.alignmentTrack.hasPairs) {
            html.push('<div class="igv-track-menu-item igv-track-menu-border-top">');
            html.push(true === self.viewAsPairs ? '<i class="fa fa-check fa-check-shim">' : '<i class="fa fa-check fa-check-shim fa-check-hidden">');
            html.push('</i>');
            html.push('View as pairs');
            html.push('</div>');
            menuItems.push({
                object: $(html.join('')),
                click: function () {
                    var $fa = $(this).find('i');

                    popover.hide();

                    self.viewAsPairs = !self.viewAsPairs;

                    if (true === self.viewAsPairs) {
                        $fa.removeClass('fa-check-hidden');
                    } else {
                        $fa.addClass('fa-check-hidden');
                    }

                    self.featureSource.setViewAsPairs(self.viewAsPairs);
                    self.trackView.update();
                }
            });
        }

        return menuItems;

        function colorByMarkup(menuItem, showCheck, index) {

            var parts = [],
                item = {};


            //parts.push((0 === index) ? '<div class=\"igv-track-menu-item igv-track-menu-border-top\">' : '<div class="igv-track-menu-item">');
            parts.push('<div class="igv-track-menu-item">');

            parts.push(showCheck ? '<i class="fa fa-check fa-check-shim"></i>' : '<i class="fa fa-check fa-check-shim fa-check-hidden"></i>');

            //parts.push('<span>');
            //parts.push('Color by: ');
            //parts.push('</span>');

            if (menuItem.key === 'tag') {
                parts.push('<span id="color-by-tag">');
            } else {
                parts.push('<span>');
            }
            parts.push(menuItem.label);
            parts.push('</span>');

            parts.push('</div>');

            item.object = $(parts.join(''));

            item.click = function () {

                igv.popover.hide();

                if ('tag' === menuItem.key) {

                    igv.dialog.configure(function () {
                            return "Tag Name"
                        },

                        self.alignmentTrack.colorByTag ? self.alignmentTrack.colorByTag : '',

                        function () {
                            var tag = igv.dialog.$dialogInput.val().trim();
                            self.alignmentTrack.colorBy = 'tag';

                            if(tag !== self.alignmentTrack.colorByTag) {
                                self.alignmentTrack.colorByTag = igv.dialog.$dialogInput.val().trim();
                                self.alignmentTrack.tagColors = new igv.PaletteColorTable("Set1");
                                $('#color-by-tag').text(self.alignmentTrack.colorByTag);
                            }

                            self.trackView.update();
                        });

                    igv.dialog.show($(self.trackView.trackDiv));

                } else {
                    self.alignmentTrack.colorBy = menuItem.key;
                    self.trackView.update();
                }
            };

            return item;
        }

        function sortMenuItem(popover) {

            return {
                object: $('<div class="igv-track-menu-item">' + "Sort by base" + '</div>'),
                click: function () {
                    var genomicLocationViaTrackViewportHalfWidth,
                        trackViewportHalfWidth;

                    popover.hide();

                    trackViewportHalfWidth = Math.floor(igv.browser.trackViewportWidth()/2);
                    genomicLocationViaTrackViewportHalfWidth = Math.floor((igv.browser.referenceFrame.start) + igv.browser.referenceFrame.toBP(trackViewportHalfWidth));

                    // console.log('bamTrack - sort - trackViewportHalfWidth ' + igv.numberFormatter(genomicLocationViaTrackViewportHalfWidth));

                    self.altClick(genomicLocationViaTrackViewportHalfWidth, undefined);

                    if ("show center guide" === igv.browser.centerGuide.$centerGuideToggle.text()) {
                        igv.browser.centerGuide.$centerGuideToggle.trigger( "click" );
                    }

                }
            }
        }

    };

    function shadedBaseColor(qual, nucleotide, genomicLocation) {

        var color,
            alpha,
            minQ = 5,   //prefs.getAsInt(PreferenceManager.SAM_BASE_QUALITY_MIN),
            maxQ = 20,  //prefs.getAsInt(PreferenceManager.SAM_BASE_QUALITY_MAX);
            foregroundColor = igv.nucleotideColorComponents[nucleotide],
            backgroundColor = [255, 255, 255];   // White


        //if (171167156 === genomicLocation) {
        //    // NOTE: Add 1 when presenting genomic location
        //    console.log("shadedBaseColor - locus " + igv.numberFormatter(1 + genomicLocation) + " qual " + qual);
        //}

        if (!foregroundColor) return;

        if (qual < minQ) {
            alpha = 0.1;
        } else {
            alpha = Math.max(0.1, Math.min(1.0, 0.1 + 0.9 * (qual - minQ) / (maxQ - minQ)));
        }
        // Round alpha to nearest 0.1
        alpha = Math.round(alpha * 10) / 10.0;

        if (alpha >= 1) {
            color = igv.nucleotideColors[nucleotide];
        }
        else {
            color = "rgba(" + foregroundColor[0] + "," + foregroundColor[1] + "," + foregroundColor[2] + "," + alpha + ")";    //igv.getCompositeColor(backgroundColor, foregroundColor, alpha);
        }
        return color;
    }

    CoverageTrack = function (config, parent) {

        this.parent = parent;
        this.featureSource = parent.featureSource;
        this.top = 0;


        this.height = config.coverageTrackHeight;
        this.dataRange = {min: 0};   // Leav max undefined
        this.paintAxis = igv.paintAxis;
    };

    CoverageTrack.prototype.computePixelHeight = function (alignmentContainer) {
        return this.height;
    };

    CoverageTrack.prototype.draw = function (options) {

        var self = this,
            alignmentContainer = options.features,
            ctx = options.context,
            bpPerPixel = options.bpPerPixel,
            bpStart = options.bpStart,
            pixelWidth = options.pixelWidth,
            bpEnd = bpStart + pixelWidth * bpPerPixel + 1,
            coverageMap = alignmentContainer.coverageMap,
            bp,
            x,
            y,
            w,
            h,
            refBase,
            i,
            len,
            item,
            accumulatedHeight,
            sequence;

        if (this.top) ctx.translate(0, top);

        if (coverageMap.refSeq) sequence = coverageMap.refSeq.toUpperCase();

        this.dataRange.max = coverageMap.maximum;

        // paint backdrop color for all coverage buckets
        w = Math.max(1, Math.ceil(1.0 / bpPerPixel));
        for (i = 0, len = coverageMap.coverage.length; i < len; i++) {

            bp = (coverageMap.bpStart + i);
            if (bp < bpStart) continue;
            if (bp > bpEnd) break;

            item = coverageMap.coverage[i];
            if (!item) continue;

            h = Math.round((item.total / this.dataRange.max) * this.height);
            y = this.height - h;
            x = Math.floor((bp - bpStart) / bpPerPixel);


            igv.graphics.setProperties(ctx, {fillStyle: this.parent.color, strokeStyle: this.color});
            // igv.graphics.setProperties(ctx, {fillStyle: "rgba(0, 200, 0, 0.25)", strokeStyle: "rgba(0, 200, 0, 0.25)" });
            igv.graphics.fillRect(ctx, x, y, w, h);
        }

        // coverage mismatch coloring -- don't try to do this in above loop, color bar will be overwritten when w<1
        if (sequence) {
            for (i = 0, len = coverageMap.coverage.length; i < len; i++) {

                bp = (coverageMap.bpStart + i);
                if (bp < bpStart) continue;
                if (bp > bpEnd) break;

                item = coverageMap.coverage[i];
                if (!item) continue;

                h = (item.total / this.dataRange.max) * this.height;
                y = this.height - h;
                x = Math.floor((bp - bpStart) / bpPerPixel);

                refBase = sequence[i];
                if (item.isMismatch(refBase)) {

                    igv.graphics.setProperties(ctx, {fillStyle: igv.nucleotideColors[refBase]});
                    igv.graphics.fillRect(ctx, x, y, w, h);

                    accumulatedHeight = 0.0;
                    ["A", "C", "T", "G"].forEach(function (nucleotide) {

                        var count,
                            hh;

                        count = item["pos" + nucleotide] + item["neg" + nucleotide];


                        // non-logoritmic
                        hh = (count / self.dataRange.max) * self.height;

                        y = (self.height - hh) - accumulatedHeight;
                        accumulatedHeight += hh;

                        igv.graphics.setProperties(ctx, {fillStyle: igv.nucleotideColors[nucleotide]});
                        igv.graphics.fillRect(ctx, x, y, w, hh);
                    });
                }
            }
        }

    };

    CoverageTrack.prototype.popupData = function (genomicLocation, xOffset, yOffset) {

        var coverageMap = this.featureSource.alignmentContainer.coverageMap,
            coverageMapIndex,
            coverage,
            nameValues = [];


        coverageMapIndex = genomicLocation - coverageMap.bpStart;
        coverage = coverageMap.coverage[coverageMapIndex];

        if (coverage) {


            nameValues.push(igv.browser.referenceFrame.chr + ":" + igv.numberFormatter(1 + genomicLocation));

            nameValues.push({name: 'Total Count', value: coverage.total});

            // A
            tmp = coverage.posA + coverage.negA;
            if (tmp > 0)  tmp = tmp.toString() + " (" + Math.floor(((coverage.posA + coverage.negA) / coverage.total) * 100.0) + "%)";
            nameValues.push({name: 'A', value: tmp});


            // C
            tmp = coverage.posC + coverage.negC;
            if (tmp > 0)  tmp = tmp.toString() + " (" + Math.floor((tmp / coverage.total) * 100.0) + "%)";
            nameValues.push({name: 'C', value: tmp});

            // G
            tmp = coverage.posG + coverage.negG;
            if (tmp > 0)  tmp = tmp.toString() + " (" + Math.floor((tmp / coverage.total) * 100.0) + "%)";
            nameValues.push({name: 'G', value: tmp});

            // T
            tmp = coverage.posT + coverage.negT;
            if (tmp > 0)  tmp = tmp.toString() + " (" + Math.floor((tmp / coverage.total) * 100.0) + "%)";
            nameValues.push({name: 'T', value: tmp});

            // N
            tmp = coverage.posN + coverage.negN;
            if (tmp > 0)  tmp = tmp.toString() + " (" + Math.floor((tmp / coverage.total) * 100.0) + "%)";
            nameValues.push({name: 'N', value: tmp});

        }


        return nameValues;

    };

    AlignmentTrack = function (config, parent) {

        this.parent = parent;
        this.featureSource = parent.featureSource;
        this.top = config.coverageTrackHeight == 0 ? 0 : config.coverageTrackHeight  + 5;
        this.alignmentRowHeight = config.alignmentRowHeight || 14;

        this.negStrandColor = config.negStrandColor || "rgba(150, 150, 230, 0.75)";
        this.posStrandColor = config.posStrandColor || "rgba(230, 150, 150, 0.75)";
        this.insertionColor = config.insertionColor || "rgb(138, 94, 161)";
        this.deletionColor = config.deletionColor || "black";
        this.skippedColor = config.skippedColor || "rgb(150, 170, 170)";

        this.colorBy = config.colorBy || "none";
        this.colorByTag = config.colorByTag;
        this.bamColorTag = config.bamColorTag === undefined ? "YC" : config.bamColorTag;

        // sort alignment rows
        this.sortOption = config.sortOption || {sort: "NUCLEOTIDE"};

        this.sortDirection = true;

        this.hasPairs = false;   // Until proven otherwise

    };

    AlignmentTrack.prototype.computePixelHeight = function (alignmentContainer) {

        if (alignmentContainer.packedAlignmentRows) {
            var h = 0;
            if (alignmentContainer.hasDownsampledIntervals()) {
                h += downsampleRowHeight + alignmentStartGap;
            }
            return h + (this.alignmentRowHeight * alignmentContainer.packedAlignmentRows.length) + 5;
        }
        else {
            return this.height;
        }

    };

    AlignmentTrack.prototype.draw = function (options) {

        var self = this,
            alignmentContainer = options.features,
            ctx = options.context,
            bpPerPixel = options.bpPerPixel,
            bpStart = options.bpStart,
            pixelWidth = options.pixelWidth,
            bpEnd = bpStart + pixelWidth * bpPerPixel + 1,
            packedAlignmentRows = alignmentContainer.packedAlignmentRows,
            sequence = alignmentContainer.sequence;

        if (this.top) ctx.translate(0, this.top);

        if (sequence) {
            sequence = sequence.toUpperCase();
        }

        if (alignmentContainer.hasDownsampledIntervals()) {
            alignmentRowYInset = downsampleRowHeight + alignmentStartGap;

            alignmentContainer.downsampledIntervals.forEach(function (interval) {
                var xBlockStart = (interval.start - bpStart) / bpPerPixel,
                    xBlockEnd = (interval.end - bpStart) / bpPerPixel;

                if (xBlockEnd - xBlockStart > 5) {
                    xBlockStart += 1;
                    xBlockEnd -= 1;
                }
                igv.graphics.fillRect(ctx, xBlockStart, 2, (xBlockEnd - xBlockStart), downsampleRowHeight - 2, {fillStyle: "black"});
            })

        }
        else {
            alignmentRowYInset = 0;
        }

        if (packedAlignmentRows) {

            packedAlignmentRows.forEach(function renderAlignmentRow(alignmentRow, i) {

                var yRect = alignmentRowYInset + (self.alignmentRowHeight * i),
                    alignmentHeight = self.alignmentRowHeight - 2,
                    i,
                    b,
                    alignment;

                for (i = 0; i < alignmentRow.alignments.length; i++) {

                    alignment = alignmentRow.alignments[i];

                    self.hasPairs = self.hasPairs || alignment.isPaired();

                    if ((alignment.start + alignment.lengthOnRef) < bpStart) continue;
                    if (alignment.start > bpEnd) break;


                    if (true === alignment.hidden) {
                        continue;
                    }

                    if (alignment instanceof igv.PairedAlignment) {

                        drawPairConnector(alignment, yRect, alignmentHeight);

                        drawSingleAlignment(alignment.firstAlignment, yRect, alignmentHeight);

                        if (alignment.secondAlignment) {
                            drawSingleAlignment(alignment.secondAlignment, yRect, alignmentHeight);
                        }

                    }
                    else {
                        drawSingleAlignment(alignment, yRect, alignmentHeight);
                    }

                }
            });
        }


        // alignment is a PairedAlignment
        function drawPairConnector(alignment, yRect, alignmentHeight) {

            var alignmentColor = getAlignmentColor.call(self, alignment.firstAlignment),
                outlineColor = alignmentColor,
                xBlockStart = (alignment.connectingStart - bpStart) / bpPerPixel,
                xBlockEnd = (alignment.connectingEnd - bpStart) / bpPerPixel,
                yStrokedLine = yRect + alignmentHeight / 2;

            if ((alignment.connectingEnd) < bpStart || alignment.connectingStart > bpEnd) return;

            if (alignment.mq <= 0) {
                alignmentColor = igv.addAlphaToRGB(alignmentColor, "0.15");
            }

            igv.graphics.setProperties(ctx, {fillStyle: alignmentColor, strokeStyle: outlineColor});

            igv.graphics.strokeLine(ctx, xBlockStart, yStrokedLine, xBlockEnd, yStrokedLine);

        }


        function drawSingleAlignment(alignment, yRect, alignmentHeight) {

            var alignmentColor = getAlignmentColor.call(self, alignment),
                outlineColor = alignmentColor,
                lastBlockEnd,
                blocks = alignment.blocks,
                block,
                b;

            if ((alignment.start + alignment.lengthOnRef) < bpStart || alignment.start > bpEnd) return;

            if (alignment.mq <= 0) {
                alignmentColor = igv.addAlphaToRGB(alignmentColor, "0.15");
            }

            igv.graphics.setProperties(ctx, {fillStyle: alignmentColor, strokeStyle: outlineColor});

            for (b = 0; b < blocks.length; b++) {   // Can't use forEach here -- we need ability to break

                block = blocks[b];

                if ((block.start + block.len) < bpStart) continue;

                drawBlock(block);

                if ((block.start + block.len) > bpEnd) break;  // Do this after drawBlock to insure gaps are drawn


                if (alignment.insertions) {
                    alignment.insertions.forEach(function (block) {
                        var refOffset = block.start - bpStart,
                            xBlockStart = refOffset / bpPerPixel - 1,
                            widthBlock = 3;
                        igv.graphics.fillRect(ctx, xBlockStart, yRect - 1, widthBlock, alignmentHeight + 2, {fillStyle: self.insertionColor});
                    });
                }

            }

            function drawBlock(block) {
                var seqOffset = block.start - alignmentContainer.start,
                    xBlockStart = (block.start - bpStart) / bpPerPixel,
                    xBlockEnd = ((block.start + block.len) - bpStart) / bpPerPixel,
                    widthBlock = Math.max(1, xBlockEnd - xBlockStart),
                    widthArrowHead = self.alignmentRowHeight / 2.0,
                    blockSeq = block.seq.toUpperCase(),
                    skippedColor = self.skippedColor,
                    deletionColor = self.deletionColor,
                    refChar,
                    readChar,
                    readQual,
                    xBase,
                    widthBase,
                    colorBase,
                    x,
                    y,
                    i,
                    yStrokedLine = yRect + alignmentHeight / 2;

                if (block.gapType !== undefined && xBlockEnd !== undefined && lastBlockEnd !== undefined) {
                    if ("D" === block.gapType) {
                        igv.graphics.strokeLine(ctx, lastBlockEnd, yStrokedLine, xBlockStart, yStrokedLine, {strokeStyle: deletionColor});
                    }
                    else {
                        igv.graphics.strokeLine(ctx, lastBlockEnd, yStrokedLine, xBlockStart, yStrokedLine, {strokeStyle: skippedColor});
                    }
                }
                lastBlockEnd = xBlockEnd;

                if (true === alignment.strand && b === blocks.length - 1) {
                    // Last block on + strand
                    x = [
                        xBlockStart,
                        xBlockEnd,
                        xBlockEnd + widthArrowHead,
                        xBlockEnd,
                        xBlockStart,
                        xBlockStart];
                    y = [
                        yRect,
                        yRect,
                        yRect + (alignmentHeight / 2.0),
                        yRect + alignmentHeight,
                        yRect + alignmentHeight,
                        yRect];
                    igv.graphics.fillPolygon(ctx, x, y, {fillStyle: alignmentColor});
                    if (alignment.mq <= 0) {
                        igv.graphics.strokePolygon(ctx, x, y, {strokeStyle: outlineColor});
                    }
                }
                else if (false === alignment.strand && b === 0) {
                    // First block on - strand
                    x = [
                        xBlockEnd,
                        xBlockStart,
                        xBlockStart - widthArrowHead,
                        xBlockStart,
                        xBlockEnd,
                        xBlockEnd];
                    y = [
                        yRect,
                        yRect,
                        yRect + (alignmentHeight / 2.0),
                        yRect + alignmentHeight,
                        yRect + alignmentHeight,
                        yRect];
                    igv.graphics.fillPolygon(ctx, x, y, {fillStyle: alignmentColor});
                    if (alignment.mq <= 0) {
                        igv.graphics.strokePolygon(ctx, x, y, {strokeStyle: outlineColor});
                    }
                }
                else {
                    //      igv.graphics.fillRect(ctx, xBlockStart, yRect, widthBlock, height, {fillStyle: "white"});
                    igv.graphics.fillRect(ctx, xBlockStart, yRect, widthBlock, alignmentHeight, {fillStyle: alignmentColor});
                    if (alignment.mq <= 0) {
                        ctx.save();
                        ctx.strokeStyle = outlineColor;
                        ctx.strokeRect(xBlockStart, yRect, widthBlock, alignmentHeight);
                        ctx.restore();
                    }
                }
                // Only do mismatch coloring if a refseq exists to do the comparison
                if (sequence && blockSeq !== "*") {
                    for (i = 0, len = blockSeq.length; i < len; i++) {
                        readChar = blockSeq.charAt(i);
                        refChar = sequence.charAt(seqOffset + i);
                        if (readChar === "=") {
                            readChar = refChar;
                        }
                        if (readChar === "X" || refChar !== readChar) {
                            if (block.qual && block.qual.length > i) {
                                readQual = block.qual[i];
                                colorBase = shadedBaseColor(readQual, readChar, i + block.start);
                            }
                            else {
                                colorBase = igv.nucleotideColors[readChar];
                            }
                            if (colorBase) {
                                xBase = ((block.start + i) - bpStart) / bpPerPixel;
                                widthBase = Math.max(1, 1 / bpPerPixel);
                                igv.graphics.fillRect(ctx, xBase, yRect, widthBase, alignmentHeight, {fillStyle: colorBase});
                            }
                        }
                    }
                }
            }
        }

    };

    AlignmentTrack.prototype.sortAlignmentRows = function (genomicLocation, sortOption) {

        var self = this;

        // console.log('bamtrack - sortAlignmentRows - location ' + igv.numberFormatter(genomicLocation));

        this.featureSource.alignmentContainer.packedAlignmentRows.forEach(function (row) {
            row.updateScore(genomicLocation, self.featureSource.alignmentContainer, sortOption);
        });

        this.featureSource.alignmentContainer.packedAlignmentRows.sort(function (rowA, rowB) {
            // return rowA.score - rowB.score;
            return true === self.sortDirection ? rowA.score - rowB.score : rowB.score - rowA.score;
        });

        // this.featureSource.alignmentContainer.packedAlignmentRows.forEach(function (row) {
        //     console.log('score ' + row.score);
        // });

    };

    AlignmentTrack.prototype.popupData = function (genomicLocation, xOffset, yOffset) {

        var packedAlignmentRows = this.featureSource.alignmentContainer.packedAlignmentRows,
            downsampledIntervals = this.featureSource.alignmentContainer.downsampledIntervals,
            packedAlignmentsIndex,
            alignmentRow,
            clickedObject,
            i, len, tmp;

        packedAlignmentsIndex = Math.floor((yOffset - (alignmentRowYInset)) / this.alignmentRowHeight);

        if (packedAlignmentsIndex < 0) {

            for (i = 0, len = downsampledIntervals.length; i < len; i++) {


                if (downsampledIntervals[i].start <= genomicLocation && (downsampledIntervals[i].end >= genomicLocation)) {
                    clickedObject = downsampledIntervals[i];
                    break;
                }

            }
        }
        else if (packedAlignmentsIndex < packedAlignmentRows.length) {

            alignmentRow = packedAlignmentRows[packedAlignmentsIndex];

            clickedObject = undefined;

            for (i = 0, len = alignmentRow.alignments.length, tmp; i < len; i++) {

                tmp = alignmentRow.alignments[i];

                if (tmp.start <= genomicLocation && (tmp.start + tmp.lengthOnRef >= genomicLocation)) {
                    clickedObject = tmp;
                    break;
                }

            }
        }

        if (clickedObject) {
            return clickedObject.popupData(genomicLocation);
        }
        else {
            return [];
        }

    };

    function getAlignmentColor(alignment) {

        var alignmentTrack = this,
            option = alignmentTrack.colorBy,
            tagValue, color,
            strand;

        color = alignmentTrack.parent.color; // default

        switch (option) {

            case "strand":
                color = alignment.strand ? alignmentTrack.posStrandColor : alignmentTrack.negStrandColor;
                break;
            case "firstOfPairStrand":

                if(alignment instanceof igv.PairedAlignment) {
                    color = alignment.firstOfPairStrand() ? alignmentTrack.posStrandColor : alignmentTrack.negStrandColor;
                }
                else if (alignment.isPaired()) {

                    if (alignment.isFirstOfPair()) {
                        color = alignment.strand ? alignmentTrack.posStrandColor : alignmentTrack.negStrandColor;
                    }
                    else if (alignment.isSecondOfPair()) {
                        color = alignment.strand ? alignmentTrack.negStrandColor : alignmentTrack.posStrandColor;
                    }
                    else {
                        console.log("ERROR. Paired alignments are either first or second.")
                    }
                }
                break;

            case "tag":

                tagValue = alignment.tags()[alignmentTrack.colorByTag];
                if (tagValue !== undefined) {

                    if (alignmentTrack.bamColorTag === alignmentTrack.colorByTag) {
                        // UCSC style color option
                        color = "rgb(" + tagValue + ")";
                    }
                    else {
                        color = alignmentTrack.tagColors.getColor(tagValue);
                    }
                }
                break;
            default:
                color = alignmentTrack.parent.color;
        }
        return color;

    }

    return igv;

})
(igv || {});

var igv = (function (igv) {

    var BLOCK_HEADER_LENGTH = 18;
    var BLOCK_LENGTH_OFFSET = 16;  // Location in the gzip block of the total block size (actually total block size - 1)
    var BLOCK_FOOTER_LENGTH = 8; // Number of bytes that follow the deflated data
    var MAX_COMPRESSED_BLOCK_SIZE = 64 * 1024; // We require that a compressed block (including header and footer, be <= this)
    var GZIP_OVERHEAD = BLOCK_HEADER_LENGTH + BLOCK_FOOTER_LENGTH + 2; // Gzip overhead is the header, the footer, and the block size (encoded as a short).
    var GZIP_ID1 = 31;   // Magic number
    var GZIP_ID2 = 139;  // Magic number
    var GZIP_FLG = 4; // FEXTRA flag means there are optional fields


    // Uncompress data,  assumed to be series of bgzipped blocks
    // Code is based heavily on bam.js, part of the Dalliance Genome Explorer,  (c) Thomas Down 2006-2001.
    igv.unbgzf = function (data, lim) {

        var oBlockList = [],
            ptr = [0],
            totalSize = 0;

        lim = lim || data.byteLength - 18;

        while (ptr[0] < lim) {

            var ba = new Uint8Array(data, ptr[0], 18);

            var xlen = (ba[11] << 8) | (ba[10]);
            var si1 = ba[12];
            var si2 = ba[13];
            var slen = (ba[15] << 8) | (ba[14]);
            var bsize = (ba[17] << 8) | (ba[16]) + 1;

            var start = 12 + xlen + ptr[0];    // Start of CDATA
            var length = data.byteLength - start;

            if (length < (bsize + 8)) break;

            var unc = jszlib_inflate_buffer(data, start, length, ptr);

            ptr[0] += 8;    // Skipping CRC-32 and size of uncompressed data

            totalSize += unc.byteLength;
            oBlockList.push(unc);
        }

        // Concatenate decompressed blocks
        if (oBlockList.length == 1) {
            return oBlockList[0];
        } else {
            var out = new Uint8Array(totalSize);
            var cursor = 0;
            for (var i = 0; i < oBlockList.length; ++i) {
                var b = new Uint8Array(oBlockList[i]);
                arrayCopy(b, 0, out, cursor, b.length);
                cursor += b.length;
            }
            return out.buffer;
        }
    }



    igv.BGZFile = function (config) {
        this.filePosition = 0;
        this.config = config;
    }

    igv.BGZFile.prototype.nextBlock = function () {

        var self = this;

        return new Promise(function (fulfill, reject) {

            igvxhr.loadArrayBuffer(self.path,
                {
                    headers: self.config.headers,
                    range: {start: self.filePosition, size: BLOCK_HEADER_LENGTH},
                    withCredentials: self.config.withCredentials

                }).then(function (arrayBuffer) {

                var ba = new Uint8Array(arrayBuffer);
                var xlen = (ba[11] << 8) | (ba[10]);
                var si1 = ba[12];
                var si2 = ba[13];
                var slen = (ba[15] << 8) | (ba[14]);
                var bsize = (ba[17] << 8) | (ba[16]) + 1;

                self.filePosition += BLOCK_HEADER_LENGTH;

                igvxhr.loadArrayBuffer(self.path, {
                    headers: self.config.headers,
                    range: {start: self.filePosition, size: bsize},
                    withCredentials: self.config.withCredentials

                }).then(function (arrayBuffer) {

                    var unc = jszlib_inflate_buffer(arrayBuffer);

                    self.filePosition += (bsize + 8);  // "8" for CRC-32 and size of uncompressed data

                    fulfill(unc);

                }).catch(reject)
            }).catch(reject);
        })

    }




    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by turner on 3/21/14.
 */
var igv = (function (igv) {

    igv.CoverageMap = function (chr, start, end, alignments, refSeq) {

        var myself = this;

        this.refSeq = refSeq;
        this.chr = chr;
        this.bpStart = start;
        this.length = (end - start);

        this.coverage = new Array(this.length);

        this.maximum = 0;

        alignments.forEach(function (alignment) {

            alignment.blocks.forEach(function (block) {

                var key,
                    base,
                    i,
                    j,
                    q;

                for (i = block.start - myself.bpStart, j = 0; j < block.len; i++, j++) {

                    if (!myself.coverage[ i ]) {
                        myself.coverage[ i ] = new Coverage();
                    }

                    base = block.seq.charAt(j);
                    key = (alignment.strand) ? "pos" + base : "neg" + base;
                    q = block.qual[j];

                    myself.coverage[ i ][ key ] += 1;
                    myself.coverage[ i ][ "qual" + base ] += q;

                    myself.coverage[ i ].total += 1;
                    myself.coverage[ i ].qual += q;

                    myself.maximum = Math.max(myself.coverage[ i ].total, myself.maximum);

                    //if (171168321 === (j + block.start)) {
                    //    // NOTE: Add 1 when presenting genomic location
                    //    console.log("locus " + igv.numberFormatter(1 + 171168321) + " base " + base + " qual " + q);
                    //}
                }

            });
        });

    };

    igv.CoverageMap.threshold = 0.2;
    igv.CoverageMap.qualityWeight = true;

    function Coverage() {
        this.posA = 0;
        this.negA = 0;

        this.posT = 0;
        this.negT = 0;

        this.posC = 0;
        this.negC = 0;
        this.posG = 0;

        this.negG = 0;

        this.posN = 0;
        this.negN = 0;

        this.pos = 0;
        this.neg = 0;

        this.qualA = 0;
        this.qualT = 0;
        this.qualC = 0;
        this.qualG = 0;
        this.qualN = 0;

        this.qual = 0;

        this.total = 0;
    }

    Coverage.prototype.isMismatch = function (refBase) {

        var myself = this,
            mismatchQualitySum,
            threshold = igv.CoverageMap.threshold * ((igv.CoverageMap.qualityWeight && this.qual) ? this.qual : this.total);

        mismatchQualitySum = 0;
        [ "A", "T", "C", "G" ].forEach(function (base) {

            if (base !== refBase) {
                mismatchQualitySum += ((igv.CoverageMap.qualityWeight && myself.qual) ? myself[ "qual" + base] : (myself[ "pos" + base ] + myself[ "neg" + base ]));
            }
        });

        return mismatchQualitySum >= threshold;

    };

    return igv;

})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 University of California San Diego
 * Author: Jim Robinson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


var igv = (function (igv) {


    igv.PairedAlignment = function (firstAlignment) {

        this.firstAlignment = firstAlignment;
        this.chr = firstAlignment.chr;
        this.readName = firstAlignment.readName;

        if (firstAlignment.start < firstAlignment.mate.position) {
            this.start = firstAlignment.start;
            this.end = Math.max(firstAlignment.mate.position, firstAlignment.start + firstAlignment.lengthOnRef);  // Approximate
            this.connectingStart = firstAlignment.start + firstAlignment.lengthOnRef;
            this.connectingEnd = firstAlignment.mate.position;
        }
        else {
            this.start = firstAlignment.mate.position;
            this.end = firstAlignment.start + firstAlignment.lengthOnRef;
            this.connectingStart = firstAlignment.mate.position;
            this.connectingEnd = firstAlignment.start;
        }
        this.lengthOnRef = this.end - this.start;

    }

    igv.PairedAlignment.prototype.setSecondAlignment = function (alignment) {

        // TODO -- check the chrs are equal,  error otherwise
        this.secondAlignment = alignment;

        if (alignment.start > this.firstAlignment.start) {
            this.end = alignment.start + alignment.lengthOnRef;
            this.connectingEnd = alignment.start;
        }
        else {
            this.start = alignment.start;
            this.connectingStart = alignment.start + alignment.lengthOnRef;
        }
        this.lengthOnRef = this.end - this.start;


    }

    igv.PairedAlignment.prototype.popupData = function (genomicLocation) {

        var nameValues = [];

        nameValues = nameValues.concat(this.firstAlignment.popupData(genomicLocation));

        if (this.secondAlignment) {
            nameValues.push("-------------------------------");
            nameValues = nameValues.concat(this.secondAlignment.popupData(genomicLocation));
        }
        return nameValues;
    }

    igv.PairedAlignment.prototype.isPaired = function () {
        return true; // By definition
    }

    igv.PairedAlignment.prototype.firstOfPairStrand = function () {
        if (this.firstAlignment.isFirstOfPair()) {
            return this.firstAlignment.strand;
        }
        else if (this.secondAlignment) {
            return this.secondAlignment.strand;
        }
        else {
            return this.firstAlignment.strand;          // This assumes inward pointing pairs
        }
    }


    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 University of California San Diego
 * Author: Jim Robinson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


var igv = (function (igv) {


    igv.isb = {

        querySegByStudy: function (study, limit) {
            var q = "SELECT * FROM [isb-cgc:tcga_201510_alpha.Copy_Number_segments] " +
                "WHERE ParticipantBarcode IN " +
                "(SELECT ParticipantBarcode FROM [isb-cgc:tcga_201510_alpha.Clinical_data] WHERE Study = \"" + study + "\")";

            if(limit) q += (" limit " + limit);

            return q;
        },


        decodeSeg: function (row) {

            var seg = {};
            seg["ParticipantBarcode"] = row.f[0].v;
            seg["Study"] = row.f[4].v;
            seg["Chromosome"] = row.f[6].v;
            seg["Start"] = row.f[7].v;
            seg["End"] = row.f[8].v;
            seg["Num_Probes"] = row.f[9].v;
            seg["Segment_mean"] = row.f[10].v;
            return seg;
        }


    }

    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by jrobinso on 4/7/14.
 */


var igv = (function (igv) {


    igv.BufferedReader = function (config, contentLength, bufferSize) {
        this.path = config.url;
        this.contentLength = contentLength;
        this.bufferSize = bufferSize ? bufferSize : 512000;
        this.range = {start: -1, size: -1};
        this.config = config;
    }

    /**
     *
     * @param requestedRange - byte rangeas {start, size}
     * @param fulfill - function to receive result
     * @param asUint8 - optional flag to return result as an UInt8Array
     */
    igv.BufferedReader.prototype.dataViewForRange = function (requestedRange, asUint8) {

        var self = this;

        return new Promise(function (fulfill, reject) {
            var hasData = (self.data && (self.range.start <= requestedRange.start) &&
                ((self.range.start + self.range.size) >= (requestedRange.start + requestedRange.size))),
                bufferSize,
                loadRange;

            if (hasData) {
                subbuffer(self, requestedRange, asUint8);
            }
            else {
                // Expand buffer size if needed, but not beyond content length
                bufferSize = Math.max(self.bufferSize, requestedRange.size);

                if (self.contentLength > 0 && requestedRange.start + bufferSize > self.contentLength) {
                    loadRange = {start: requestedRange.start};
                }
                else {
                    loadRange = {start: requestedRange.start, size: bufferSize};
                }

                igvxhr.loadArrayBuffer(self.path,
                    {
                        headers: self.config.headers,
                        range: loadRange,
                        withCredentials: self.config.withCredentials
                    }).then(function (arrayBuffer) {
                    self.data = arrayBuffer;
                    self.range = loadRange;
                    subbuffer(self, requestedRange, asUint8);
                }).catch(reject);

            }


            function subbuffer(bufferedReader, requestedRange, asUint8) {

                var len = bufferedReader.data.byteLength,
                    bufferStart = requestedRange.start - bufferedReader.range.start,
                    result = asUint8 ?
                        new Uint8Array(bufferedReader.data, bufferStart, len - bufferStart) :
                        new DataView(bufferedReader.data, bufferStart, len - bufferStart);
                fulfill(result);
            }
        });

    }


    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by jrobinso on 4/7/14.
 */


var igv = (function (igv) {

    var BPTREE_MAGIC_LTH = 0x78CA8C91;
    var BPTREE_MAGIC_HTL = 0x918CCA78;
    var BPTREE_HEADER_SIZE = 32;


    igv.BPTree = function (binaryParser, treeOffset) {

        var genome = igv.browser ? igv.browser.genome : null;

        this.treeOffset = treeOffset; // File offset to beginning of tree
        this.header = {};
        this.header.magic = binaryParser.getInt();
        this.header.blockSize = binaryParser.getInt();
        this.header.keySize = binaryParser.getInt();
        this.header.valSize = binaryParser.getInt();
        this.header.itemCount = binaryParser.getLong();
        this.header.reserved = binaryParser.getLong();

        this.dictionary = {};

        // Recursively walk tree to populate dictionary
        readTreeNode(binaryParser, -1, this.header.keySize, this.dictionary);


        function readTreeNode(byteBuffer, offset, keySize, dictionary) {

            if (offset >= 0) byteBuffer.position = offset;

            var type = byteBuffer.getByte(),
                reserved = byteBuffer.getByte(),
                count = byteBuffer.getShort(),
                i,
                key,
                chromId,
                chromSize,
                childOffset,
                bufferOffset;


            if (type == 1) {
                for (i = 0; i < count; i++) {
                    key = byteBuffer.getFixedLengthString(keySize).trim();

                    if(genome) key = genome.getChromosomeName(key);  // Translate to canonical chr name

                    chromId = byteBuffer.getInt();
                    chromSize = byteBuffer.getInt();
                    dictionary[key] = chromId;

                }
            }
            else { // non-leaf
                for (i = 0; i < count; i++) {
                    childOffset = byteBuffer.nextLong();
                    bufferOffset = childOffset - self.treeOffset;
                    readTreeNode(byteBuffer, offset, keySize, dictionary);
                }
            }
        }
    }


    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by jrobinso on 4/7/14.
 */


var igv = (function (igv) {

    var RPTREE_MAGIC_LTH = 0x2468ACE0;
    var RPTREE_MAGIC_HTL = 0xE0AC6824;
    var RPTREE_HEADER_SIZE = 48;
    var RPTREE_NODE_LEAF_ITEM_SIZE = 32;   // leaf item size
    RPTREE_NODE_CHILD_ITEM_SIZE = 24;  // child item size
    var BUFFER_SIZE = 512000;     //  buffer

    igv.RPTree = function (fileOffset, contentLength, config, littleEndian) {

        this.config = config;
        this.filesize = contentLength;
        this.fileOffset = fileOffset; // File offset to beginning of tree
        this.path = config.url;
        this.littleEndian = littleEndian;
    }


    igv.RPTree.prototype.load = function () {

        var self = this;

        return new Promise(function (fulfill, reject) {
            var rootNodeOffset = self.fileOffset + RPTREE_HEADER_SIZE,
                bufferedReader = new igv.BufferedReader(self.config, self.filesize, BUFFER_SIZE);

            self.readNode(rootNodeOffset, bufferedReader).then(function (node) {
                self.rootNode = node;
                fulfill(self);
            }).catch(reject);
        });
    }


    igv.RPTree.prototype.readNode = function (filePosition, bufferedReader) {

        var self = this;

        return new Promise(function (fulfill, reject) {

            bufferedReader.dataViewForRange({start: filePosition, size: 4}, false).then(function (dataView) {
                var binaryParser = new igv.BinaryParser(dataView, self.littleEndian);

                var type = binaryParser.getByte();
                var isLeaf = (type === 1) ? true : false;
                var reserved = binaryParser.getByte();
                var count = binaryParser.getShort();

                filePosition += 4;

                var bytesRequired = count * (isLeaf ? RPTREE_NODE_LEAF_ITEM_SIZE : RPTREE_NODE_CHILD_ITEM_SIZE);
                var range2 = {start: filePosition, size: bytesRequired};

                bufferedReader.dataViewForRange(range2, false).then(function (dataView) {

                    var i,
                        items = new Array(count),
                        binaryParser = new igv.BinaryParser(dataView);

                    if (isLeaf) {
                        for (i = 0; i < count; i++) {
                            var item = {
                                isLeaf: true,
                                startChrom: binaryParser.getInt(),
                                startBase: binaryParser.getInt(),
                                endChrom: binaryParser.getInt(),
                                endBase: binaryParser.getInt(),
                                dataOffset: binaryParser.getLong(),
                                dataSize: binaryParser.getLong()
                            };
                            items[i] = item;

                        }
                        fulfill(new RPTreeNode(items));
                    }
                    else { // non-leaf
                        for (i = 0; i < count; i++) {

                            var item = {
                                isLeaf: false,
                                startChrom: binaryParser.getInt(),
                                startBase: binaryParser.getInt(),
                                endChrom: binaryParser.getInt(),
                                endBase: binaryParser.getInt(),
                                childOffset: binaryParser.getLong()
                            };
                            items[i] = item;

                        }

                        fulfill(new RPTreeNode(items));
                    }
                }).catch(reject);
            }).catch(reject);
        });
    }


    igv.RPTree.prototype.findLeafItemsOverlapping = function (chrIdx, startBase, endBase) {

        var self = this;

        return new Promise(function (fulfill, reject) {

            var leafItems = [],
                processing = new Set(),
                bufferedReader = new igv.BufferedReader(self.config, self.filesize, BUFFER_SIZE);

            processing.add(0);  // Zero represents the root node
            findLeafItems(self.rootNode, 0);

            function findLeafItems(node, nodeId) {

                if (overlaps(node, chrIdx, startBase, endBase)) {

                    var items = node.items;

                    items.forEach(function (item) {

                        if (overlaps(item, chrIdx, startBase, endBase)) {

                            if (item.isLeaf) {
                                leafItems.push(item);
                            }

                            else {
                                if (item.childNode) {
                                    findLeafItems(item.childNode);
                                }
                                else {
                                    processing.add(item.childOffset);  // Represent node to-be-loaded by its file position
                                    self.readNode(item.childOffset, bufferedReader).then(function (node) {
                                        item.childNode = node;
                                        findLeafItems(node, item.childOffset);
                                    }).catch(reject);
                                }
                            }
                        }
                    });

                }

                if (nodeId != undefined) processing.delete(nodeId);

                // Wait until all nodes are processed
                if (processing.isEmpty()) {
                    fulfill(leafItems);
                }
            }
        });
    }


    function RPTreeNode(items) {


        this.items = items;

        var minChromId = Number.MAX_VALUE,
            maxChromId = 0,
            minStartBase = Number.MAX_VALUE,
            maxEndBase = 0,
            i,
            item;

        for (i = 0; i < items.length; i++) {
            item = items[i];
            minChromId = Math.min(minChromId, item.startChrom);
            maxChromId = Math.max(maxChromId, item.endChrom);
            minStartBase = Math.min(minStartBase, item.startBase);
            maxEndBase = Math.max(maxEndBase, item.endBase);
        }

        this.startChrom = minChromId;
        this.endChrom = maxChromId;
        this.startBase = minStartBase;
        this.endBase = maxEndBase;

    }

    /**
     * Return true if {chrIdx:startBase-endBase} overlaps item's interval
     * @returns {boolean}
     */
    function overlaps(item, chrIdx, startBase, endBase) {

        //  if (chrIdx > item.endChrom || chrIdx < item.startChrom) return false;

        if (!item) {
            console.log("null item");
            return false;
        }

        return ((chrIdx > item.startChrom) || (chrIdx == item.startChrom && endBase >= item.startBase)) &&
            ((chrIdx < item.endChrom) || (chrIdx == item.endChrom && startBase < item.endBase));


    }


    return igv;


})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by jrobinso on 4/7/14.
 */


var igv = (function (igv) {

    var BIGWIG_MAGIC_LTH = 0x888FFC26; // BigWig Magic Low to High
    var BIGWIG_MAGIC_HTL = 0x26FC8F66; // BigWig Magic High to Low
    var BIGBED_MAGIC_LTH = 0x8789F2EB; // BigBed Magic Low to High
    var BIGBED_MAGIC_HTL = 0xEBF28987; // BigBed Magic High to Low
    var BBFILE_HEADER_SIZE = 64;


    igv.BWReader = function (config) {
        this.path = config.url;
        this.headPath = config.headURL || this.path;
        this.rpTreeCache = {};
        this.config = config;
    };

    igv.BWReader.prototype.getZoomHeaders = function () {

        var self = this;

        return new Promise(function (fulfill, reject) {
            if (self.zoomLevelHeaders) {
                fulfill(self.zoomLevelHeaders);
            }
            else {
                self.loadHeader().then(function () {
                    fulfill(self.zoomLevelHeaders);
                }).catch(function (error) {
                    reject(error);
                });
            }
        });
    }

    igv.BWReader.prototype.loadHeader = function () {

        var self = this;

        return new Promise(function (fulfill, reject) {
            igvxhr.loadArrayBuffer(self.path,
                {
                    headers: self.config.headers,
                    range: {start: 0, size: BBFILE_HEADER_SIZE},
                    withCredentials: self.config.withCredentials
                }).then(function (data) {

                if (!data) return;

                // Assume low-to-high unless proven otherwise
                self.littleEndian = true;

                var binaryParser = new igv.BinaryParser(new DataView(data));

                var magic = binaryParser.getUInt();

                if (magic === BIGWIG_MAGIC_LTH) {
                    self.type = "BigWig";
                }
                else if (magic == BIGBED_MAGIC_LTH) {
                    self.type = "BigBed";
                }
                else {
                    //Try big endian order
                    self.littleEndian = false;

                    binaryParser.littleEndian = false;
                    binaryParser.position = 0;
                    var magic = binaryParser.getUInt();

                    if (magic === BIGWIG_MAGIC_HTL) {
                        self.type = "BigWig";
                    }
                    else if (magic == BIGBED_MAGIC_HTL) {
                        self.type = "BigBed";
                    }
                    else {
                        // TODO -- error, unknown file type  or BE
                    }

                }
                // Table 5  "Common header for BigWig and BigBed files"
                self.header = {};
                self.header.bwVersion = binaryParser.getShort();
                self.header.nZoomLevels = binaryParser.getShort();
                self.header.chromTreeOffset = binaryParser.getLong();
                self.header.fullDataOffset = binaryParser.getLong();
                self.header.fullIndexOffset = binaryParser.getLong();
                self.header.fieldCount = binaryParser.getShort();
                self.header.definedFieldCount = binaryParser.getShort();
                self.header.autoSqlOffset = binaryParser.getLong();
                self.header.totalSummaryOffset = binaryParser.getLong();
                self.header.uncompressBuffSize = binaryParser.getInt();
                self.header.reserved = binaryParser.getLong();

                loadZoomHeadersAndChrTree.call(self).then(fulfill).catch(reject);
            }).catch(function (error) {
                    reject(error);
                });

        });
    }


    function loadZoomHeadersAndChrTree() {


        var startOffset = BBFILE_HEADER_SIZE,
            self = this;

        return new Promise(function (fulfill, reject) {

            igvxhr.loadArrayBuffer(self.path,
                {
                    headers: self.config.headers,
                    range: {start: startOffset, size: (self.header.fullDataOffset - startOffset + 5)},
                    withCredentials: self.config.withCredentials
                }).then(function (data) {

                var nZooms = self.header.nZoomLevels,
                    binaryParser = new igv.BinaryParser(new DataView(data)),
                    i,
                    len,
                    zoomNumber,
                    zlh;

                self.zoomLevelHeaders = [];

                self.firstZoomDataOffset = Number.MAX_VALUE;
                for (i = 0; i < nZooms; i++) {
                    zoomNumber = nZooms - i;
                    zlh = new ZoomLevelHeader(zoomNumber, binaryParser);
                    self.firstZoomDataOffset = Math.min(zlh.dataOffset, self.firstZoomDataOffset);
                    self.zoomLevelHeaders.push(zlh);
                }

                // Autosql
                if (self.header.autoSqlOffset > 0) {
                    binaryParser.position = self.header.autoSqlOffset - startOffset;
                    self.autoSql = binaryParser.getString();
                }

                // Total summary
                if (self.header.totalSummaryOffset > 0) {
                    binaryParser.position = self.header.totalSummaryOffset - startOffset;
                    self.totalSummary = new igv.BWTotalSummary(binaryParser);
                }

                // Chrom data index
                if (self.header.chromTreeOffset > 0) {
                    binaryParser.position = self.header.chromTreeOffset - startOffset;
                    self.chromTree = new igv.BPTree(binaryParser, 0);
                }
                else {
                    // TODO -- this is an error, not expected
                }

                //Finally total data count
                binaryParser.position = self.header.fullDataOffset - startOffset;
                self.dataCount = binaryParser.getInt();

                fulfill();

            }).catch(reject);
        });
    }

    igv.BWReader.prototype.loadRPTree = function (offset) {

        var self = this;

        return new Promise(function (fulfill, reject) {
            var rpTree = self.rpTreeCache[offset];
            if (rpTree) {
                fulfill(rpTree);
            }
            else {
                rpTree = new igv.RPTree(offset, self.contentLength, self.config, self.littleEndian);
                self.rpTreeCache[offset] = rpTree;
                rpTree.load().then(function () {
                    fulfill(rpTree);
                }).catch(reject);
            }
        });
    }


    var ZoomLevelHeader = function (index, byteBuffer) {
        this.index = index;
        this.reductionLevel = byteBuffer.getInt();
        this.reserved = byteBuffer.getInt();
        this.dataOffset = byteBuffer.getLong();
        this.indexOffset = byteBuffer.getLong();

    }


    return igv;

})
(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by jrobinso on 4/7/14.
 */


var igv = (function (igv) {

    igv.BWSource = function (config) {

        this.reader = new igv.BWReader(config);
        this.bufferedReader = new igv.BufferedReader(config);
    };

    igv.BWSource.prototype.getFeatures = function (chr, bpStart, bpEnd) {

        var self = this;
        return new Promise(function (fulfill, reject) {

            self.reader.getZoomHeaders().then(function (zoomLevelHeaders) {

                // Select a biwig "zoom level" appropriate for the current resolution
                var bwReader = self.reader,
                    bufferedReader = self.bufferedReader,
                    bpPerPixel = igv.browser.referenceFrame.bpPerPixel,
                    zoomLevelHeader = zoomLevelForScale(bpPerPixel, zoomLevelHeaders),
                    treeOffset,
                    decodeFunction;

                if (zoomLevelHeader) {
                    treeOffset = zoomLevelHeader.indexOffset;
                    decodeFunction = decodeZoomData;
                } else {
                    treeOffset = bwReader.header.fullIndexOffset;
                    if (bwReader.type === "BigWig") {
                        decodeFunction = decodeWigData;
                    }
                    else {
                        decodeFunction = decodeBedData;
                    }
                }

                bwReader.loadRPTree(treeOffset).then(function (rpTree) {

                    var chrIdx = self.reader.chromTree.dictionary[chr];
                    if (chrIdx === undefined) {
                        fulfill(null);
                    }
                    else {

                        rpTree.findLeafItemsOverlapping(chrIdx, bpStart, bpEnd).then(function (leafItems) {

                            var promises = [];

                            if (!leafItems || leafItems.length == 0) fulfill([]);

                            leafItems.forEach(function (item) {

                                promises.push(new Promise(function (fulfill, reject) {
                                    var features = [];

                                    bufferedReader.dataViewForRange({
                                        start: item.dataOffset,
                                        size: item.dataSize
                                    }, true).then(function (uint8Array) {

                                        var inflate = new Zlib.Inflate(uint8Array);
                                        var plain = inflate.decompress();
                                        decodeFunction(new DataView(plain.buffer), chr, chrIdx, bpStart, bpEnd, features);

                                        fulfill(features);

                                    }).catch(reject);
                                }));
                            });


                            Promise.all(promises).then(function (featureArrays) {

                                var i, allFeatures = featureArrays[0];
                                if(featureArrays.length > 1) {
                                   for(i=0; i<featureArrays.length; i++) {
                                       allFeatures = allFeatures.concat(featureArrays[i]);
                                   }
                                    allFeatures.sort(function (a, b) {
                                        return a.start - b.start;
                                    })
                                }

                                fulfill(allFeatures)
                            }).catch(reject);

                        }).catch(reject);
                    }
                }).catch(reject);
            }).catch(reject);


        });
    }


    function zoomLevelForScale(bpPerPixel, zoomLevelHeaders) {

        var level = null, i, zl;

        for (i = 0; i < zoomLevelHeaders.length; i++) {

            zl = zoomLevelHeaders[i];

            if (zl.reductionLevel > bpPerPixel) {
                level = zl;
                break;
            }
        }

        if (null == level) {
            level = zoomLevelHeaders[zoomLevelHeaders.length - 1];
        }

        return (level && level.reductionLevel < 4 * bpPerPixel) ? level : null;
    }


    function decodeWigData(data, chr, chrIdx, bpStart, bpEnd, featureArray) {

        var binaryParser = new igv.BinaryParser(data),
            chromId = binaryParser.getInt(),
            chromStart = binaryParser.getInt(),
            chromEnd = binaryParser.getInt(),
            itemStep = binaryParser.getInt(),
            itemSpan = binaryParser.getInt(),
            type = binaryParser.getByte(),
            reserved = binaryParser.getByte(),
            itemCount = binaryParser.getShort(),
            value;

        if (chromId === chrIdx) {

            while (itemCount-- > 0) {

                switch (type) {
                    case 1:
                        chromStart = binaryParser.getInt();
                        chromEnd = binaryParser.getInt();
                        value = binaryParser.getFloat();
                        break;
                    case 2:

                        chromStart = binaryParser.getInt();
                        value = binaryParser.getFloat();
                        chromEnd = chromStart + itemSpan;
                        break;
                    case 3:  // Fixed step
                        value = binaryParser.getFloat();
                        chromEnd = chromStart + itemSpan;
                        chromStart += itemStep;
                        break;

                }

                if (chromStart >= bpEnd) {
                    break; // Out of interval
                } else if (chromEnd > bpStart) {
                    featureArray.push({chr: chr, start: chromStart, end: chromEnd, value: value});
                }


            }
        }

    }

    function decodeZoomData(data, chr, chrIdx, bpStart, bpEnd, featureArray) {

        var binaryParser = new igv.BinaryParser(data),
            minSize = 8 * 4,   // Minimum # of bytes required for a zoom record
            chromId,
            chromStart,
            chromEnd,
            validCount,
            minVal,
            maxVal,
            sumData,
            sumSquares,
            value;

        while (binaryParser.remLength() >= minSize) {
            chromId = binaryParser.getInt();
            if (chromId === chrIdx) {

                chromStart = binaryParser.getInt();
                chromEnd = binaryParser.getInt();
                validCount = binaryParser.getInt();
                minVal = binaryParser.getFloat();
                maxVal = binaryParser.getFloat();
                sumData = binaryParser.getFloat();
                sumSquares = binaryParser.getFloat();
                value = validCount == 0 ? 0 : sumData / validCount;

                if (chromStart >= bpEnd) {
                    break; // Out of interval

                } else if (chromEnd > bpStart) {
                    featureArray.push({chr: chr, start: chromStart, end: chromEnd, value: value});
                }

            }
        }

    }


    function decodeBedData(data, chr, chrIdx, bpStart, bpEnd, featureArray) {

        var binaryParser = new igv.BinaryParser(data),
            minSize = 3 * 4 + 1,   // Minimum # of bytes required for a bed record
            chromId,
            chromStart,
            chromEnd,
            rest,
            tokens,
            feature,
            exonCount, exonSizes, exonStarts, exons, eStart, eEnd;


        while (binaryParser.remLength() >= minSize) {

            chromId = binaryParser.getInt();
            if (chromId != chrIdx) continue;

            chromStart = binaryParser.getInt();
            chromEnd = binaryParser.getInt();
            rest = binaryParser.getString();

            feature = {chr: chr, start: chromStart, end: chromEnd};

            if (chromStart < bpEnd && chromEnd >= bpStart) {
                featureArray.push(feature);

                tokens = rest.split("\t");

                if (tokens.length > 0) {
                    feature.name = tokens[0];
                }

                if (tokens.length > 1) {
                    feature.score = parseFloat(tokens[1]);
                }
                if (tokens.length > 2) {
                    feature.strand = tokens[2];
                }
                if (tokens.length > 3) {
                    feature.cdStart = parseInt(tokens[3]);
                }
                if (tokens.length > 4) {
                    feature.cdEnd = parseInt(tokens[4]);
                }
                if (tokens.length > 5) {
                    if (tokens[5] !== "." && tokens[5] !== "0")
                        feature.color = igv.createColorString(tokens[5]);
                }
                if (tokens.length > 8) {
                    exonCount = parseInt(tokens[6]);
                    exonSizes = tokens[7].split(',');
                    exonStarts = tokens[8].split(',');
                    exons = [];

                    for (var i = 0; i < exonCount; i++) {
                        eStart = start + parseInt(exonStarts[i]);
                        eEnd = eStart + parseInt(exonSizes[i]);
                        exons.push({start: eStart, end: eEnd});
                    }

                    feature.exons = exons;
                }
            }
        }

    }


    return igv;


})
(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by jrobinso on 4/7/14.
 */


var igv = (function (igv) {


    igv.BWTotalSummary = function (byteBuffer) {

        if (byteBuffer) {

            this.basesCovered = byteBuffer.getLong();
            this.minVal = byteBuffer.getDouble();
            this.maxVal = byteBuffer.getDouble();
            this.sumData = byteBuffer.getDouble();
            this.sumSquares = byteBuffer.getDouble();

            computeStats.call(this);
        }
        else {
            this.basesCovered = 0;
            this.minVal = 0;
            this.maxVal = 0;
            this.sumData = 0;
            this.sumSquares = 0;
            this.mean = 0;
            this.stddev = 0;
        }
    }


    function computeStats() {
        var n = this.basesCovered;
        if (n > 0) {
            this.mean = this.sumData / n;
            this.stddev = Math.sqrt((this.sumSquares - (this.sumData / n) * this.sumData) / (n - 1));
        }
    }

    igv.BWTotalSummary.prototype.updateStats = function (stats) {

        this.basesCovered += stats.count;
        this.sumData += status.sumData;
        this.sumSquares += sumSquares;
        this.minVal = MIN(_minVal, min);
        this.maxVal = MAX(_maxVal, max);

        computeStats.call(this);

    }


    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    // TODO -- big endian

    igv.BinaryParser = function (dataView, littleEndian) {

        this.littleEndian = (littleEndian ? littleEndian : true);
        this.position = 0;
        this.view = dataView;
        this.length = dataView.byteLength;
    }

    igv.BinaryParser.prototype.remLength = function() {
        return this.length - this.position;
    }

    igv.BinaryParser.prototype.hasNext = function () {
        return this.position < this.length - 1;
    }

    igv.BinaryParser.prototype.getByte = function () {
        var retValue = this.view.getUint8(this.position, this.littleEndian);
        this.position++;
        return retValue;
    }

    igv.BinaryParser.prototype.getShort = function () {

        var retValue = this.view.getInt16(this.position, this.littleEndian);
        this.position += 2
        return retValue;
    }


    igv.BinaryParser.prototype.getInt = function () {

        var retValue = this.view.getInt32(this.position, this.littleEndian);
        this.position += 4;
        return retValue;
    }


    igv.BinaryParser.prototype.getUInt = function () {
        var retValue = this.view.getUint32(this.position, this.littleEndian);
        this.position += 4;
        return retValue;
    }

    igv.BinaryParser.prototype.getLong = function () {

        // js doesn't support long.  Let's hope this fits an int, but advance the buffer 8 bytes

        var integer = this.view.getInt32(this.position, this.littleEndian);
        this.position += 8;
        return integer;
    }

    igv.BinaryParser.prototype.getString = function (len) {

        var s = "";
        var c;
        while ((c = this.view.getUint8(this.position++)) != 0) {
            s += String.fromCharCode(c);
            if (len && s.length == len) break;
        }
        return s;
    }

    igv.BinaryParser.prototype.getFixedLengthString = function (len) {

        var s = "";
        var i;
        var c;
        for (i=0; i<len; i++) {
            c = this.view.getUint8(this.position++);
            if(c > 0) {
                s += String.fromCharCode(c);
            }
        }
        return s;
    }


    igv.BinaryParser.prototype.getFloat = function () {

        var retValue = this.view.getFloat32(this.position, this.littleEndian);
        this.position += 4;
        return retValue;


    }

    igv.BinaryParser.prototype.getDouble = function () {

        var retValue = this.view.getFloat64(this.position, this.littleEndian);
        this.position += 8;
        return retValue;
    }

    igv.BinaryParser.prototype.skip = function (n) {

        this.position += n;
        return this.position;
    }


    /**
     * Return a bgzip (bam and tabix) virtual pointer
     * TODO -- why isn't 8th byte used ?
     * @returns {*}
     */
    igv.BinaryParser.prototype.getVPointer = function() {

        var position = this.position,
            offset = (this.view.getUint8(position + 1) << 8) | (this.view.getUint8(position)),
            byte6 = ((this.view.getUint8(position + 6) & 0xff) * 0x100000000),
            byte5 = ((this.view.getUint8(position + 5) & 0xff) * 0x1000000),
            byte4 = ((this.view.getUint8(position + 4) & 0xff) * 0x10000),
            byte3 = ((this.view.getUint8(position + 3) & 0xff) * 0x100),
            byte2 = ((this.view.getUint8(position + 2) & 0xff)),
            block = byte6 + byte5 + byte4 + byte3 + byte2;
        this.position += 8;

 //       if (block == 0 && offset == 0) {
 //           return null;
 //       } else {
            return new VPointer(block, offset);
 //       }
    }


    function VPointer(block, offset) {
        this.block = block;
        this.offset = offset;
    }

    VPointer.prototype.isLessThan = function (vp) {
        return this.block < vp.block ||
            (this.block === vp.block && this.offset < vp.offset);
    }

    VPointer.prototype.isGreaterThan = function (vp) {
        return this.block > vp.block ||
            (this.block === vp.block && this.offset > vp.offset);
    }

    VPointer.prototype.print = function() {
        return "" + this.block + ":" + this.offset;
    }


    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    var knownFileTypes = new Set(["narrowpeak", "broadpeak", "peaks", "bedgraph", "wig", "gff3", "gff",
        "gtf", "aneu", "fusionjuncspan", "refflat", "seg", "bed", "vcf", "bb", "bigbed", "bw", "bigwig", "bam"]);

    igv.Browser = function (options, trackContainer) {

        igv.browser = this;   // Make globally visible (for use in html markup).

        this.config = options;

        this.div = $('<div id="igvRootDiv" class="igv-root-div">')[0];

        initialize.call(this, options);

        $("input[id='trackHeightInput']").val(this.trackHeight);

        this.trackContainerDiv = trackContainer;

        addTrackContainerHandlers(trackContainer);

        this.trackViews = [];

        this.trackLabelsVisible = true;

        this.featureDB = {};   // Hash of name -> feature, used for search function.

        this.constants = {
            dragThreshold: 3,
            defaultColor: "rgb(0,0,150)",
            doubleClickDelay: options.doubleClickDelay || 500
        };

        // Map of event name -> [ handlerFn, ... ]
        this.eventHandlers = {};

        window.onresize = igv.throttle(function () {
            igv.browser.resize();
        }, 10);

    };

    function initialize(options) {
        var genomeId;

        this.flanking = options.flanking;
        this.type = options.type || "IGV";
        this.crossDomainProxy = options.crossDomainProxy;
        this.formats = options.formats;
        this.trackDefaults = options.trackDefaults;

        if (options.search) {
            this.searchConfig = {
                type: "json",
                url: options.search.url,
                coords: options.search.coords === undefined ? 1 : options.search.coords,
                chromosomeField: options.search.chromosomeField || "chromosome",
                startField: options.search.startField || "start",
                endField: options.search.endField || "end",
                resultsField: options.search.resultsField
            }
        }
        else {

            if (options.reference && options.reference.id) {
                genomeId = options.reference.id;
            }
            else if (options.genome) {
                genomeId = options.genome;
            }
            else {
                genomeId = "hg19";
            }

            this.searchConfig = {
                // Legacy support -- deprecated
                type: "plain",
                url: "https://portals.broadinstitute.org/webservices/igv/locus?genome=" + genomeId + "&name=$FEATURE$",
                coords: 0,
                chromosomeField: "chromosome",
                startField: "start",
                endField: "end"

            }
        }
    }

    igv.Browser.prototype.getFormat = function (name) {
        if (this.formats === undefined) return undefined;
        return this.formats[name];
    };

    igv.Browser.prototype.loadTracksWithConfigList = function (configList, sortBy) {

        var self = this;

        configList.forEach(function (config) {
            self.loadTrack(config, sortBy);
        });

        // Really we should just resize the new trackViews, but currently there is no way to get a handle on those
        this.trackViews.forEach(function (trackView) {
            trackView.resize();
        })

    };

    igv.Browser.prototype.loadTrack = function (config, sortBy) {

        var self = this,
            settings,
            property,
            newTrack,
            featureSource,
            nm;

        inferTypes(config);

        // Set defaults if specified
        if (this.trackDefaults && config.type) {
            settings = this.trackDefaults[config.type];
            if (settings) {
                for (property in settings) {
                    if (settings.hasOwnProperty(property) && config[property] === undefined) {
                        config[property] = settings[property];
                    }
                }
            }
        }

        switch (config.type.toLowerCase()) {
            case "gwas":
                newTrack = new igv.GWASTrack(config);
                break;
            case "annotation":
            case "genes":
            case "fusionjuncspan":
                newTrack = new igv.FeatureTrack(config);
                break;
            case "variant":
                newTrack = new igv.VariantTrack(config);
                break;

            case "alignment":

                newTrack = new igv.BAMTrack(config, featureSource);
                break;

            case "data":  // deprecated
            case "wig":
                newTrack = new igv.WIGTrack(config);
                break;
            case "sequence":
                newTrack = new igv.SequenceTrack(config);
                break;
            case "eqtl":
                newTrack = new igv.EqtlTrack(config);
                break;
            case "seg":
                newTrack = new igv.SegTrack(config);
                break;
            case "aneu":
                newTrack = new igv.AneuTrack(config);
                break;
            default:

                //alert("Unknown file type: " + config.url);
                igv.presentAlert("Unknown file type: " + config.url);

                return null;
        }

        // Set order field of track here.  Otherwise track order might get shuffled during asynchronous load
        if (undefined === newTrack.order) {
            newTrack.order = this.trackViews.length;
        }


        // If defined, attempt to load the file header before adding the track.  This will catch some errors early
        if (typeof newTrack.getFileHeader === "function") {
            newTrack.getFileHeader().then(function (header) {
                self.addTrack(newTrack);
            }).catch(function (error) {
                //alert(error);
                igv.presentAlert(error);
            });
        }
        else {
            var tracks = igv.browser.findTracks("type", "seg");
            if(tracks.length > 0){
                setTimeout(function(){
                    tracks.forEach(function (track) {
                        track.sortSamples(sortBy[0], sortBy[1], sortBy[2],sortBy[3]);
                    })
                    setTimeout(function(){
                        self.addTrack(newTrack);
                    }, 500);
                }, 500);
            }else{
                self.addTrack(newTrack);
            }

        }

    };

    /**
     * Add a new track.  Each track is associated with the following DOM elements
     *
     *      leftHandGutter  - div on the left for track controls and legend
     *      contentDiv  - a div element wrapping all the track content.  Height can be > viewportDiv height
     *      viewportDiv - a div element through which the track is viewed.  This might have a vertical scrollbar
     *      canvas     - canvas element upon which the track is drawn.  Child of contentDiv
     *
     * The width of all elements should be equal.  Height of the viewportDiv is controlled by the user, but never
     * greater than the contentDiv height.   Height of contentDiv and canvas are equal, and governed by the data
     * loaded.
     *
     * @param track
     */
    igv.Browser.prototype.addTrack = function (track) {

        var trackView = new igv.TrackView(track, this);

        if (typeof igv.popover !== "undefined") {
            igv.popover.hide();
        }

        // Register view with track.  This backpointer is unfortunate, but is needed to support "resize" events.
        track.trackView = trackView;


        this.trackViews.push(trackView);

        this.reorderTracks();

        trackView.resize();
    };

    igv.Browser.prototype.reorderTracks = function () {

        var myself = this;

        this.trackViews.sort(function (a, b) {
            var aOrder = a.track.order || 0;
            var bOrder = b.track.order || 0;
            return aOrder - bOrder;
        });

        // Reattach the divs to the dom in the correct order
        $(this.trackContainerDiv).children("igv-track-div").detach();

        this.trackViews.forEach(function (trackView, index, trackViews) {

            myself.trackContainerDiv.appendChild(trackView.trackDiv);

        });

    };

    igv.Browser.prototype.removeTrack = function (track) {

        // Find track panel
        var trackPanelRemoved;
        for (var i = 0; i < this.trackViews.length; i++) {
            if (track === this.trackViews[i].track) {
                trackPanelRemoved = this.trackViews[i];
                break;
            }
        }

        if (trackPanelRemoved) {
            this.trackViews.splice(i, 1);
            this.trackContainerDiv.removeChild(trackPanelRemoved.trackDiv);
            this.fireEvent('trackremoved', [trackPanelRemoved.track]);
        }

    };

    /**
     *
     * @param property
     * @param value
     * @returns {Array}  tracks with given property value.  e.g. findTracks("type", "annotation")
     */
    igv.Browser.prototype.findTracks = function (property, value) {
        var tracks = [];
        this.trackViews.forEach(function (trackView) {
            if (value === trackView.track[property]) {
                tracks.push(trackView.track)
            }
        })
        return tracks;
    };

    igv.Browser.prototype.reduceTrackOrder = function (trackView) {

        var indices = [],
            raisable,
            raiseableOrder;

        if (1 === this.trackViews.length) {
            return;
        }

        this.trackViews.forEach(function (tv, i, tvs) {

            indices.push({trackView: tv, index: i});

            if (trackView === tv) {
                raisable = indices[i];
            }

        });

        if (0 === raisable.index) {
            return;
        }

        raiseableOrder = raisable.trackView.track.order;
        raisable.trackView.track.order = indices[raisable.index - 1].trackView.track.order;
        indices[raisable.index - 1].trackView.track.order = raiseableOrder;

        this.reorderTracks();

    };

    igv.Browser.prototype.increaseTrackOrder = function (trackView) {

        var j,
            indices = [],
            raisable,
            raiseableOrder;

        if (1 === this.trackViews.length) {
            return;
        }

        this.trackViews.forEach(function (tv, i, tvs) {

            indices.push({trackView: tv, index: i});

            if (trackView === tv) {
                raisable = indices[i];
            }

        });

        if ((this.trackViews.length - 1) === raisable.index) {
            return;
        }

        raiseableOrder = raisable.trackView.track.order;
        raisable.trackView.track.order = indices[1 + raisable.index].trackView.track.order;
        indices[1 + raisable.index].trackView.track.order = raiseableOrder;

        this.reorderTracks();

    };

    igv.Browser.prototype.setTrackHeight = function (newHeight) {

        this.trackHeight = newHeight;

        this.trackViews.forEach(function (panel) {
            panel.setTrackHeight(newHeight);
        });

    };

    igv.Browser.prototype.resize = function () {

        if (this.ideoPanel) this.ideoPanel.resize();
        if (this.karyoPanel) this.karyoPanel.resize();

        this.trackViews.forEach(function (panel) {
            panel.resize();
        });

        this.centerGuide.repaint();

    };

    igv.Browser.prototype.repaint = function () {

        if (this.ideoPanel) {
            this.ideoPanel.repaint();
        }

        if (this.karyoPanel) {
            this.karyoPanel.repaint();
        }
        this.trackViews.forEach(function (trackView) {
            trackView.repaint();
        });

    };

    igv.Browser.prototype.update = function () {

        this.updateLocusSearch(this.referenceFrame);

        if (this.centerGuide) {
            this.centerGuide.repaint();
        }

        if (this.ideoPanel) {
            this.ideoPanel.repaint();
        }

        if (this.karyoPanel) {
            this.karyoPanel.repaint();
        }

        this.trackViews.forEach(function (trackPanel) {
            trackPanel.update();
        });

    };

    igv.Browser.prototype.loadInProgress = function () {
        var i;
        for (i = 0; i < this.trackViews.length; i++) {
            if (this.trackViews[i].loading) {
                return true;
            }
        }
        return false;
    };

    igv.Browser.prototype.updateLocusSearch = function (referenceFrame) {

        var chr,
            ss,
            ee,
            str,
            end,
            chromosome;


        if (this.$searchInput) {

            chr = referenceFrame.chr;
            ss = igv.numberFormatter(Math.floor(referenceFrame.start + 1));

            end = referenceFrame.start + this.trackViewportWidthBP();
            if (this.genome) {
                chromosome = this.genome.getChromosome(chr);
                if (chromosome) end = Math.min(end, chromosome.bpLength);
            }

            ee = igv.numberFormatter(Math.floor(end));

            str = chr + ":" + ss + "-" + ee;
            this.$searchInput.val(str);

            this.windowSizePanel.update(Math.floor(end - referenceFrame.start));
        }

        this.fireEvent('locuschange', [referenceFrame, str]);
    };

    /**
     * Return the visible width of a track.  All tracks should have the same width.
     */
    igv.Browser.prototype.trackViewportWidth = function () {

        var width;

        if (this.trackViews && this.trackViews.length > 0) {
            width = this.trackViews[0].viewportDiv.clientWidth;
        }
        else {
            width = this.trackContainerDiv.clientWidth - 100;   // Should never get here
        }

        return width;

    };

    igv.Browser.prototype.trackViewportWidthBP = function () {
        return this.referenceFrame.bpPerPixel * this.trackViewportWidth();
    };

    igv.Browser.prototype.trackViewportCenterLineBP = function () {
        var centerLineBP = (0.5 * this.referenceFrame.bpPerPixel * this.trackViewportWidth());

        // return Math.floor(centerLineBP);
        return centerLineBP;
    };

    igv.Browser.prototype.minimumBasesExtent = function () {
        return 40;
    };

    igv.Browser.prototype.removeAllTracks = function () {
        var tracks = this.trackViews;

        for (var i = 0; i < tracks.length; i++) {
            var track = this.trackViews[i].track;
            this.removeTrack(track);
        }
    };

    igv.Browser.prototype.setGotoCallback = function (gotocallback) {
        this.gotocallback = gotocallback;
    };

    igv.Browser.prototype.goto = function (chr, start, end) {

        if (typeof this.gotocallback != "undefined") {
            //console.log("Got chr="+chr+", start="+start+", end="+end+", also using callback "+this.gotocallback);
            this.gotocallback(chr, start, end);
        }

        var w,
            chromosome,
            viewportWidth = this.trackViewportWidth();

        if (igv.popover) {
            igv.popover.hide();
        }

        // Translate chr to official name
        if (this.genome) {
            chr = this.genome.getChromosomeName(chr);
        }

        this.referenceFrame.chr = chr;

        // If end is undefined,  interpret start as the new center, otherwise compute scale.
        if (!end) {
            w = Math.round(viewportWidth * this.referenceFrame.bpPerPixel / 2);
            start = Math.max(0, start - w);
        }
        else {
            this.referenceFrame.bpPerPixel = (end - start) / (viewportWidth);
        }

        if (this.genome) {
            chromosome = this.genome.getChromosome(this.referenceFrame.chr);
            if (!chromosome) {
                if (console && console.log) console.log("Could not find chromsome " + this.referenceFrame.chr);
            }
            else {
                if (!chromosome.bpLength) chromosome.bpLength = 1;

                var maxBpPerPixel = chromosome.bpLength / viewportWidth;
                if (this.referenceFrame.bpPerPixel > maxBpPerPixel) this.referenceFrame.bpPerPixel = maxBpPerPixel;

                if (!end) {
                    end = start + viewportWidth * this.referenceFrame.bpPerPixel;
                }

                if (chromosome && end > chromosome.bpLength) {
                    start -= (end - chromosome.bpLength);
                }
            }
        }

        this.referenceFrame.start = start;

        this.update();

    };

    // Zoom in by a factor of 2, keeping the same center location
    igv.Browser.prototype.zoomIn = function () {

        if (this.loadInProgress()) {
            // ignore
            return;
        }

        var centerBP;

        console.log('browser.zoomIn - src extent ' + basesExtent(this.trackViewportWidth(), this.referenceFrame.bpPerPixel));

        // Have we reached the zoom-in threshold yet? If so, bail.
        if (this.minimumBasesExtent() > basesExtent(this.trackViewportWidth(), this.referenceFrame.bpPerPixel/2.0)) {
            console.log('browser.zoomIn - dst extent ' + basesExtent(this.trackViewportWidth(), this.referenceFrame.bpPerPixel/2.0) + ' bailing ...');
            return;
        } else {
            console.log('browser.zoomIn - dst extent ' + basesExtent(this.trackViewportWidth(), this.referenceFrame.bpPerPixel/2.0));
        }

        // window center (base-pair units)
        centerBP = this.referenceFrame.start + this.referenceFrame.bpPerPixel * (this.trackViewportWidth()/2);

        // derive scaled (zoomed in) start location (base-pair units) by multiplying half-width by halve'd bases-per-pixel
        // which results in base-pair units
        this.referenceFrame.start = centerBP - (this.trackViewportWidth()/2) * (this.referenceFrame.bpPerPixel/2.0);

        // halve the bases-per-pixel
        this.referenceFrame.bpPerPixel /= 2.0;

        this.update();

        function basesExtent(width, bpp) {
            return Math.floor(width * bpp);
        }
    };

    // Zoom out by a factor of 2, keeping the same center location if possible
    igv.Browser.prototype.zoomOut = function () {

        if (this.loadInProgress()) {
            // ignore
            return;
        }

        var newScale, maxScale, center, chrLength, widthBP, viewportWidth;
        viewportWidth = this.trackViewportWidth();

        newScale = this.referenceFrame.bpPerPixel * 2;
        chrLength = 250000000;
        if (this.genome) {
            var chromosome = this.genome.getChromosome(this.referenceFrame.chr);
            if (chromosome) {
                chrLength = chromosome.bpLength;
            }
        }
        maxScale = chrLength / viewportWidth;
        if (newScale > maxScale) newScale = maxScale;

        center = this.referenceFrame.start + this.referenceFrame.bpPerPixel * viewportWidth / 2;
        widthBP = newScale * viewportWidth;

        this.referenceFrame.start = Math.round(center - widthBP / 2);

        if (this.referenceFrame.start < 0) this.referenceFrame.start = 0;
        else if (this.referenceFrame.start > chrLength - widthBP) this.referenceFrame.start = chrLength - widthBP;

        this.referenceFrame.bpPerPixel = newScale;
        this.update();
    };

    /**
     *
     * @param feature
     * @param callback - function to call
     */
    igv.Browser.prototype.search = function (feature, callback, force, config) {
        var type,
            chr,
            start,
            end,
            searchConfig,
            url,
            result;

        // See if we're ready to respond to a search, if not just queue it up and return
        if (igv.browser === undefined || igv.browser.genome === undefined) {
            igv.browser.initialLocus = feature;
            if (callback) {
                callback();
            }
            return;
        }
        var div = $("#igvDiv")[0],
            options = {
                showNavigation: true,
                showRuler: true,
                genome: "hg19",
                locus: feature,
                tracks: [
                    {
                        url: "api-legacy/copynumbersegments",
                        indexed: false,
                        name: "Segmented CN",
                        type:"seg",
                        json: true,
                        method: "POST"
                    },
                    {
                        name: "Genes",
                        url: "https://s3.amazonaws.com/igv.broadinstitute.org/annotations/hg19/genes/gencode.v18.collapsed.bed",
                        order: Number.MAX_VALUE,
                        displayMode: "EXPANDED"

                    }
                ]
            };

        if (isLocusFeature(feature, this.genome, force)) {
            var chr = feature.substring(3, feature.indexOf(":")).trim(), success = true;
            if(config !== undefined && config.sortBy !== undefined && chr !== config.sortBy[0]){
                options.tracks[0].sampleIds = config.tracks[0].sampleIds;
                options.tracks[0].chromosome = chr;
                options.tracks[0].cancerStudyId = config.tracks[0].cancerStudyId;
                igv.createBrowser(div, options);
            }else{
                success =  gotoLocusFeature(feature, this.genome, this);
            }

            if ((force || true === success) && callback) {
                callback();
            }

        } else {

            // Try local feature cache first
            result = this.featureDB[feature.toUpperCase()];
            if (result) {

                handleSearchResult(result.name, result.chr, result.start, result.end, "");

            } else if (this.searchConfig) {
                url = this.searchConfig.url.replace("$FEATURE$", feature);
                searchConfig = this.searchConfig;

                if (url.indexOf("$GENOME$") > -1) {
                    var genomeId = this.genome.id ? this.genome.id : "hg19";
                    url.replace("$GENOME$", genomeId);
                }

                // var loader = new igv.DataLoader(url);
                // if (range)  loader.range = range;
                // loader.loadBinaryString(callback);

                igvxhr.loadString(url).then(function (data) {
                    var results = ("plain" === searchConfig.type) ? parseSearchResults(data) : JSON.parse(data);

                    if (searchConfig.resultsField) {
                        results = results[searchConfig.resultsField];
                    }

                    if (results.length == 0) {
                        //alert('No feature found with name "' + feature + '"');
                        igv.presentAlert('No feature found with name "' + feature + '"');
                    }
                    else{
                    //else if (results.length == 1) {

                        // Just take the first result for now
                        // TODO - merge results, or ask user to choose
                        for(var i = 0;i < results.length;i++){
                            if(results[i].chromosome.length < 6){
                                r = results[i];
                                break;
                            }
                        }
                        chr = r[searchConfig.chromosomeField];
                        start = r[searchConfig.startField] - searchConfig.coords;
                        end = r[searchConfig.endField];
                        type = r["featureType"] || r["type"];
                        if(config !== undefined && config.sortBy !== undefined && chr !== config.sortBy[0]){
                            options.tracks[0].sampleIds = config.tracks[0].sampleIds;
                            options.tracks[0].chromosome = chr.substring(3);
                            options.tracks[0].cancerStudyId = config.tracks[0].cancerStudyId;
                            igv.createBrowser(div, options);
                        }else{
                            handleSearchResult(feature, chr, start, end, type);
                        }

                    }
                    //else {
                    //    presentSearchResults(results, searchConfig, feature);
                    //}

                    if (callback) callback();
                });
            }
        }

        function isLocusFeature(f, genome) {

            if (2 === f.split(':').length) {
                return true;
            }

            if (genome.getChromosome(f)) {
                return true;
            }

            return false;
        }
    };

    function gotoLocusFeature(locusFeature, genome, browser) {

        var type,
            tokens,
            chr,
            start,
            end,
            chrName,
            startEnd,
            center,
            obj;


        type = 'locus';
        tokens = locusFeature.split(":");
        chrName = genome.getChromosomeName(tokens[ 0 ]);
        if (chrName) {
            chr = genome.getChromosome(chrName);
        }

        if (chr) {

            // returning undefined indicates locus is a chromosome name.
            start = end = undefined;
            if (1 === tokens.length) {
                start = 0;
                end = chr.bpLength;
            } else {
                startEnd = tokens[ 1 ].split("-");
                start = Math.max(0, parseInt(startEnd[ 0 ].replace(/,/g, "")) - 1);
                if (2 === startEnd.length) {
                    end = Math.min(chr.bpLength, parseInt(startEnd[ 1 ].replace(/,/g, "")));
                    if (end < 0) {
                        // This can happen from integer overflow
                        end = chr.bpLength;
                    }
                }
            }

            obj = { start: start, end: end };
            validateLocusExtent(igv.browser, chr, obj);
            start = obj.start;
            end = obj.end;

        }

        if (undefined === chr || isNaN(start) || (start > end)) {
            igv.presentAlert("Unrecognized feature or locus: " + locusFeature);
            return false;
        }

        browser.goto(chrName, start, end);
        fireOnsearch.call(igv.browser, locusFeature, type);

        function validateLocusExtent(browser, chromosome, extent) {

            var ss = extent.start,
                ee = extent.end,
                locusExtent = ee - ss;

            if (undefined === ee) {

                ss -= igv.browser.minimumBasesExtent()/2;
                ee = ss + igv.browser.minimumBasesExtent();

                if (ee > chromosome.bpLength) {
                    ee = chromosome.bpLength;
                    ss = ee - igv.browser.minimumBasesExtent();
                } else if (ss < 0) {
                    ss = 0;
                    ee = igv.browser.minimumBasesExtent();
                }

            } else if (ee - ss < igv.browser.minimumBasesExtent()) {

                center = (ee + ss)/2;
                if (center - igv.browser.minimumBasesExtent()/2 < 0) {
                    ss = 0;
                    ee = ss + igv.browser.minimumBasesExtent();
                } else if (center + igv.browser.minimumBasesExtent()/2 > chromosome.bpLength) {
                    ee = chromosome.bpLength;
                    ss = ee - igv.browser.minimumBasesExtent();
                } else {
                    ss = center - igv.browser.minimumBasesExtent()/2;
                    ee = ss + igv.browser.minimumBasesExtent();
                }
            }

            extent.start = Math.ceil(ss);
            extent.end = Math.floor(ee);
        }

        return true;
    }

    function presentSearchResults(loci, config, feature) {

        igv.browser.$searchResultsTable.empty();
        igv.browser.$searchResults.show();

        loci.forEach(function (locus) {

            var row = $('<tr class="igvNavigationSearchResultsTableRow">');
            row.text(locus.locusString);

            row.click(function () {

                igv.browser.$searchResults.hide();

                handleSearchResult(
                    feature,
                    locus[config.chromosomeField],
                    locus[config.startField] - config.coords,
                    locus[config.endField],
                    (locus["featureType"] || locus["type"]));

            });

            igv.browser.$searchResultsTable.append(row);

        });

    }

    /**
     * Parse the igv line-oriented (non json) search results.
     * Example
     *    EGFR    chr7:55,086,724-55,275,031    refseq
     *
     * @param data
     */
    function parseSearchResults(data) {

        var lines = data.splitLines(),
            linesTrimmed = [],
            results = [];

        lines.forEach(function (item) {
            if ("" === item) {
                // do nothing
            } else {
                linesTrimmed.push(item);
            }
        });

        linesTrimmed.forEach(function (line) {

            var tokens = line.split("\t"),
                source,
                locusTokens,
                rangeTokens;

            if (tokens.length >= 3) {

                locusTokens = tokens[1].split(":");
                rangeTokens = locusTokens[1].split("-");
                source = tokens[2].trim();

                results.push({
                    chromosome: igv.browser.genome.getChromosomeName(locusTokens[0].trim()),
                    start: parseInt(rangeTokens[0].replace(/,/g, '')),
                    end: parseInt(rangeTokens[1].replace(/,/g, '')),
                    type: ("gtex" === source ? "snp" : "gene")
                });

            }

        });

        return results;

    }

    function handleSearchResult(name, chr, start, end, type) {

        igv.browser.selection = new igv.GtexSelection('gtex' === type || 'snp' === type ? {snp: name} : {gene: name});

        if (end === undefined) {
            end = start + 1;
        }
        if (igv.browser.flanking) {
            start = Math.max(0, start - igv.browser.flanking);
            end += igv.browser.flanking;    // TODO -- set max to chromosome length
        }

        igv.browser.goto(chr, start, end);

        // Notify tracks (important for gtex).   TODO -- replace this with some sort of event model ?
        fireOnsearch.call(igv.browser, name, type);
    }

    function fireOnsearch(feature, type) {
        // Notify tracks (important for gtex).   TODO -- replace this with some sort of event model ?
        this.trackViews.forEach(function (tp) {
            var track = tp.track;
            if (track.onsearch) {
                track.onsearch(feature, type);
            }
        });
    }

    function addTrackContainerHandlers(trackContainerDiv) {

        var isRulerTrack = false,
            isMouseDown = false,
            isDragging = false,
            lastMouseX = undefined,
            mouseDownX = undefined;

        $(trackContainerDiv).mousedown(function (e) {

            var coords = igv.translateMouseCoordinates(e, trackContainerDiv);

            if (igv.popover) {
                igv.popover.hide();
            }

            isRulerTrack = ($(e.target).parent().parent().parent()[0].dataset.rulerTrack) ? true : false;

            if (isRulerTrack) {
                return;
            }

            isMouseDown = true;
            lastMouseX = coords.x;
            mouseDownX = lastMouseX;
        });

        // Guide line is bound within track area, and offset by 5 pixels so as not to interfere mouse clicks.
        $(trackContainerDiv).mousemove(function (e) {
            var xy,
                _left,
                $element = igv.browser.$cursorTrackingGuide;

            xy = igv.translateMouseCoordinates(e, trackContainerDiv);
            _left = Math.max(50, xy.x - 5);

            _left = Math.min(igv.browser.trackContainerDiv.clientWidth - 65, _left);
            $element.css({ left: _left + 'px' });
        });


        $(trackContainerDiv).mousemove(igv.throttle(function (e) {

            var coords = igv.translateMouseCoordinates(e, trackContainerDiv),
                maxEnd,
                maxStart,
                referenceFrame = igv.browser.referenceFrame;

            if (isRulerTrack) {
                return;
            }

            if (!referenceFrame) {
                return;
            }

            if (isMouseDown) { // Possibly dragging

                if (mouseDownX && Math.abs(coords.x - mouseDownX) > igv.browser.constants.dragThreshold) {

                    if (igv.browser.loadInProgress()) {
                        // ignore
                        return;
                    }

                    isDragging = true;

                    referenceFrame.shiftPixels(lastMouseX - coords.x);

                    // clamp left
                    referenceFrame.start = Math.max(0, referenceFrame.start);

                    // clamp right
                    var chromosome = igv.browser.genome.getChromosome(referenceFrame.chr);
                    maxEnd = chromosome.bpLength;
                    maxStart = maxEnd - igv.browser.trackViewportWidth() * referenceFrame.bpPerPixel;


                    if (referenceFrame.start > maxStart) referenceFrame.start = maxStart;

                    igv.browser.updateLocusSearch(referenceFrame);


                    igv.browser.repaint();
                    igv.browser.fireEvent('trackdrag');
                }

                lastMouseX = coords.x;

            }

        }, 10));

        $(trackContainerDiv).mouseup(mouseUpOrOut);

        $(trackContainerDiv).mouseleave(mouseUpOrOut);

        function mouseUpOrOut(e) {

            var element = igv.browser.$cursorTrackingGuide.get(0);

            if (isRulerTrack) {
                return;
            }

            // Don't let vertical line interfere with dragging
            if (igv.browser.$cursorTrackingGuide && e.toElement === igv.browser.$cursorTrackingGuide.get(0) && e.type === 'mouseleave') {
                return;
            }

            if (isDragging) {
                igv.browser.fireEvent('trackdragend');
                isDragging = false;
            }

            mouseDownX = undefined;
            isMouseDown = false;
            lastMouseX = undefined;
        }

    }


    /**
     * Infer properties format and track type from legacy "config.type" property
     *
     * @param config
     */


    function inferTypes(config) {

        function translateDeprecatedTypes(config) {

            if (config.featureType) {  // Translate deprecated "feature" type
                config.type = config.type || config.featureType;
                config.featureType = undefined;
            }

            if ("bed" === config.type) {
                config.type = "annotation";
                config.format = config.format || "bed";

            }

            else if ("bam" === config.type) {
                config.type = "alignment";
                config.format = "bam"
            }

            else if ("vcf" === config.type) {
                config.type = "variant";
                config.format = "vcf"
            }

            else if ("t2d" === config.type) {
                config.type = "gwas";
            }

            else if ("FusionJuncSpan" === config.type) {
                config.format = "fusionjuncspan";
            }
        }

        function inferFileFormat(config) {

            if (config.format) {
                config.format = config.format.toLowerCase();
                return;
            }

            var path = config.url || config.localFile.name,
                fn = path.toLowerCase(),
                idx,
                ext;

            //Strip parameters -- handle local files later
            idx = fn.indexOf("?");
            if (idx > 0) {
                fn = fn.substr(0, idx);
            }

            //Strip aux extensions .gz, .tab, and .txt
            if (fn.endsWith(".gz")) {
                fn = fn.substr(0, fn.length - 3);
            } else if (fn.endsWith(".txt") || fn.endsWith(".tab")) {
                fn = fn.substr(0, fn.length - 4);
            }


            idx = fn.lastIndexOf(".");
            ext = idx < 0 ? fn : fn.substr(idx + 1);

            switch (ext.toLowerCase()) {

                case "bw":
                    config.format = "bigwig";
                    break;
                case "bb":
                    config.format = "bigbed";

                default:
                    if (knownFileTypes.has(ext)) {
                        config.format = ext;
                    }
            }
        }

        function inferTrackType(config) {

            if (config.type) return;

            if (config.format !== undefined) {
                switch (config.format.toLowerCase()) {
                    case "bw":
                    case "bigwig":
                    case "wig":
                    case "bedgraph":
                        config.type = "wig";
                        break;
                    case "vcf":
                        config.type = "variant";
                        break;
                    case "seg":
                        config.type = "seg";
                        break;
                    case "bam":
                        config.type = "alignment";
                        break;
                    default:
                        config.type = "annotation";
                }
            }
        }

        translateDeprecatedTypes(config);

        if (undefined === config.sourceType && (config.url || config.localFile)) {
            config.sourceType = "file";
        }

        if ("file" === config.sourceType) {
            if (undefined === config.format) {
                inferFileFormat(config);
            }
        }

        if (undefined === config.type) {
            inferTrackType(config);
        }


    };

    igv.Browser.prototype.on = function (eventName, fn) {
        if (!this.eventHandlers[eventName]) {
            this.eventHandlers[eventName] = [];
        }
        this.eventHandlers[eventName].push(fn);
    };

    igv.Browser.prototype.un = function (eventName, fn) {
        if (!this.eventHandlers[eventName]) {
            return;
        }

        var callbackIndex = this.eventHandlers[eventName].indexOf(fn);
        if (callbackIndex !== -1) {
            this.eventHandlers[eventName].splice(callbackIndex, 1);
        }
    };

    igv.Browser.prototype.fireEvent = function (eventName, args, thisObj) {
        if (!this.eventHandlers[eventName]) {
            return;
        }

        var scope = thisObj || window;
        for (var i = 0, l = this.eventHandlers[eventName].length; i < l; i++) {
            var item = this.eventHandlers[eventName][i];
            var result = item.apply(scope, args);

            // If any of the handlers return any value, then return it
            if (result !== undefined) {
                return result;
            }
        }
    };

    return igv;
})
(igv || {});



/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Utilities for loading encode files
 *
 * Created by jrobinso on 3/19/14.
 */

var igv = (function (igv) {

    var antibodyColors =
    {
        H3K27AC: "rgb(200, 0, 0)",
        H3K27ME3: "rgb(130, 0, 4)",
        H3K36ME3: "rgb(0, 0, 150)",
        H3K4ME1: "rgb(0, 150, 0)",
        H3K4ME2: "rgb(0, 150, 0)",
        H3K4ME3: "rgb(0, 150, 0)",
        H3K9AC: "rgb(100, 0, 0)",
        H3K9ME1: "rgb(100, 0, 0)"
    },
        defaultColor ="rgb(3, 116, 178)";


    igv.EncodeTable = function (parentModalBodyObject, continuation) {

        var self = this,
            spinnerFA;

        this.encodeModalTableObject = $('<table id="encodeModalTable" cellpadding="0" cellspacing="0" border="0" class="display"></table>');
        parentModalBodyObject.append(this.encodeModalTableObject[ 0 ]);

        this.initialized = false;

        spinnerFA = $('<i class="fa fa-lg fa-spinner fa-spin"></i>');
        this.spinner = $('<div class="igv-encode-spinner-container"></div>');
        this.spinner.append(spinnerFA[ 0 ]);

        $('#encodeModalTable').append(this.spinner[ 0 ]);
        $('#igvEncodeModal').on('shown.bs.modal', function (e) {

            if (true === self.initialized) {
                return;
            }

            self.initialized = true;

            continuation();
        });

        $('#encodeModalTopCloseButton').on('click', function () {
            $('tr.selected').removeClass('selected');
        });

        $('#encodeModalBottomCloseButton').on('click', function () {
            $('tr.selected').removeClass('selected');
        });

        $('#encodeModalGoButton').on('click', function () {

            var tableRows,
                dataSourceJSONRow,
                configList = [],
                encodeModalTable = $('#encodeModalTable'),
                dataTableAPIInstance = encodeModalTable.DataTable();

            tableRows = self.dataTablesObject.$('tr.selected');
            if (tableRows) {

                tableRows.removeClass('selected');

                tableRows.each(function() {

                    var index,
                        data = dataTableAPIInstance.row( this ).data();

                    index = data[ 0 ];

                    dataSourceJSONRow = self.dataSource.jSON.rows[ index ];

                    configList.push({
                        type: dataSourceJSONRow[ "Format" ],
                        url: dataSourceJSONRow[ "url" ],
                        color: encodeAntibodyColor(dataSourceJSONRow[ "Target" ]),
                        format: dataSourceJSONRow["Format"],
                        name: dataSourceJSONRow["Name"]
                    });

                });

                if (undefined === igv.browser.designatedTrack) {
                    configList[ 0 ].designatedTrack = true;
                }

                igv.browser.loadTracksWithConfigList(configList);

            } // if (tableRows)

        });

    };

    igv.EncodeTable.prototype.loadWithDataSource = function (dataSource) {

        var self = this,
            dataSet = dataSource.dataTablesData(),
            columns = dataSource.columnHeadings();

        this.dataSource = dataSource;

        this.dataTablesObject = self.encodeModalTableObject.dataTable({

            "data": dataSet,
            "scrollX": true,
            "scrollY": "400px",
            "scrollCollapse": true,
            "paging": false,
            "columnDefs": [ { "targets": 0, "visible": false } ],
            "autoWidth": true,
            "columns": columns
        });

        self.encodeModalTableObject.find('tbody').on('click', 'tr', function () {

            if ($(this).hasClass('selected')) {
                $(this).removeClass('selected');
            } else {
                $(this).addClass('selected');
            }

        });

    };

    igv.EncodeTable.prototype.encodeTrackLabel = function (record) {

        return (record.antibody) ? record.antibody + " " + record.cell + " " + record.replicate : record.cell + record.dataType + " " + record.view + " " + record.replicate;

    };

    function encodeAntibodyColor (antibody) {

        var key;

        if (!antibody || "" === antibody) {
            return defaultColor;
        }

        key = antibody.toUpperCase();
        return (antibodyColors[ key ]) ? antibodyColors[ key ] : defaultColor;

    }

    igv.EncodeDataSource = function (config) {
        this.config = config;
    };

    igv.EncodeDataSource.prototype.loadJSON = function (continuation) {

        this.jSON = {};
        if (this.config.filePath) {
            this.ingestFile(this.config.filePath, continuation);
        } else if (this.config.jSON) {
            this.ingestJSON(this.config.jSON, continuation);
        }

    };

    igv.EncodeDataSource.prototype.ingestJSON = function (json, continuation) {

        var self = this;

        self.jSON = json;

        json.rows.forEach(function(row, i){

            Object.keys(row).forEach(function(key){
                var item = row[ key ];
                self.jSON.rows[ i ][ key ] = (undefined === item || "" === item) ? "-" : item;
            });

        });

        continuation();

    };

    igv.EncodeDataSource.prototype.ingestFile = function (file, continuation) {

        var self = this;

        igvxhr.loadString(file).then(function (data) {

            var lines = data.splitLines(),
                item;

            // Raw data items order:
            // path | cell | dataType | antibody | view | replicate | type | lab | hub
            //
            // Reorder to match desired order. Discard hub item.
            //
            self.jSON.columns = lines[0].split("\t");
            self.jSON.columns.pop();
            item = self.jSON.columns.shift();
            self.jSON.columns.push(item);

            self.jSON.rows = [];

            lines.slice(1, lines.length - 1).forEach(function (line) {

                var tokens,
                    row;

                tokens = line.split("\t");
                tokens.pop();
                item = tokens.shift();
                tokens.push(item);

                row = {};
                tokens.forEach(function (t, i, ts) {
                    var key = self.jSON.columns[ i ];
                    row[ key ] = (undefined === t || "" === t) ? "-" : t;
                });

                self.jSON.rows.push(row);

            });

            continuation();
        });

    };

    igv.EncodeDataSource.prototype.dataTablesData = function () {

        var self = this,
            result = [];

        this.jSON.rows.forEach(function(row, index){

            var rr = [];

            rr.push( index );
            self.jSON.columns.forEach(function(key){
                rr.push( row[ key ] );
            });

            result.push( rr );
        });

        return result;
    };

    igv.EncodeDataSource.prototype.columnHeadings = function () {

        var columnWidths = this.jSON.columnWidths,
            columnHeadings = [ ];

        columnHeadings.push({ "title": "index" });
        this.jSON.columns.forEach(function(heading, i){
            //columnHeadings.push({ "title": heading, width: (columnWidths[ i ].toString() + "%") });
            columnHeadings.push({ "title": heading });
        });

        return columnHeadings;

    };

    return igv;

})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by jrobinso on 5/27/15.
 */

/**
 * Support functions for the Encode rest api  https://www.encodeproject.org/help/rest-api/
 */

var igv = (function (igv) {

    var query1 = "https://www.encodeproject.org/search/?" +
        "type=experiment&" +
        "files.file_format=bed&" +
        "format=json&" +
        "limit=all&" +
        "field=replicates.library.biosample.donor.organism.name&" +
        "field=lab.title&field=biosample_term_name&" +
        "field=assay_term_name&" +
        "field=target.label&" +
        "field=files.file_format&" +
        "field=files.output_type&" +
        "field=files.href&" +
        "field=files.replicate.technical_replicate_number&" +
        "field=files.replicate.biological_replicate_number";

    var query2 = "https://www.encodeproject.org/search/?" +
        "type=experiment&" +
            // "assembly=hg19&" +
        "files.output_type=peaks&" +
        "files.file_format=bed&" +
        "format=json&" +
        "field=lab.title&" +
        "field=biosample_term_name&" +
        "field=assay_term_name&" +
        "field=target.label&" +
        "field=files.file_format&" +
        "field=files.output_type&" +
        "field=files.href&" +
        "field=files.replicate.technical_replicate_number&" +
        "field=files.replicate.biological_replicate_number&" +
        "field=files.assembly&" +
        "limit=all";

    igv.encodeSearch = function (continuation) {

        igvxhr.loadJson(query2, {}).then(function (json) {

            var columns = ["Assembly", "Cell Type", "Target", "Assay Type", "Bio Rep", "Tech Rep", "Lab"],
                columnWidths = [8, 20, 10, 10, 8, 8, 40],
                rows = [];

            json["@graph"].forEach(function (record) {

                var assayType = record.assay_term_name,
                    experimentId = record["@id"],
                    cellType = record["biosample_term_name"] || "",
                    target = record.target ? record.target.label : "",
                    lab = record.lab ? record.lab.title : "";


                record.files.forEach(function (file) {

                    if (file.file_format === "bed") {

                        var format = file.file_format,
                            type = file.output_type,
                            bioRep = file.replicate ? file.replicate.bioligcal_replicate_number : undefined,
                            techRep = file.replicate ? file.replicate.technical_replicate_number : undefined,
                            name = cellType + " " + target,
                            assembly = file.assembly;
                        if (bioRep) name += " " + bioRep;
                        if (techRep) name += (bioRep ? ":" : "0:") + techRep;

                        rows.push({
                            "Assembly": assembly,
                            "ExperimentID": experimentId,
                            "Cell Type": cellType,
                            "Assay Type": assayType,
                            "Target": target,
                            "Lab": lab,
                            "Format": format,
                            "Type": type,
                            "url": "https://www.encodeproject.org" + file.href,
                            "Bio Rep": bioRep,
                            "Tech Rep": techRep,
                            "Name": name
                        });
                    }
                });

            });

            rows.sort(function (a, b) {
                var a1 = a["Assembly"],
                    a2 = b["Assembly"],
                    ct1 = a["Cell Type"],
                    ct2 = b["Cell Type"],
                    t1 = a["Target"],
                    t2 = b["Target"];

                if (a1 === a2) {
                    if (ct1 === ct2) {
                        if (t1 === t2) {
                            return 0;
                        }
                        else if (t1 < t2) {
                            return -1;
                        }
                        else {
                            return 1;
                        }
                    }
                    else if (ct1 < ct2) {
                        return -1;
                    }
                    else {
                        return 1;
                    }
                }
                else {
                    if (a1 < a2) {
                        return -1;
                    }
                    else {
                        return 1;
                    }
                }
            });

            continuation({
                columns: columns,
                columnWidths: columnWidths,
                rows: rows
            });

        });

    }


    return igv;
})
(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// Indexed fasta files

var igv = (function (igv) {

    igv.FastaSequence = function (reference) {

        this.file = reference.fastaURL;
        this.indexed = reference.indexed !== false;   // Indexed unless it explicitly is not
        if (this.indexed) {
            this.indexFile = reference.indexURL || reference.indexFile || this.file + ".fai";
        }
        this.withCredentials = reference.withCredentials;

    };

    igv.FastaSequence.prototype.init = function () {

        var self = this;

        if (self.indexed) {

            return new Promise(function (fulfill, reject) {

                self.getIndex().then(function (index) {
                    var order = 0;
                    self.chromosomes = {};
                    self.chromosomeNames.forEach(function (chrName) {
                        var bpLength = self.index[chrName].size;
                        self.chromosomes[chrName] = new igv.Chromosome(chrName, order++, bpLength);
                    });


                    // Ignore index, getting chr names as a side effect.  Really bad practice
                    fulfill();
                }).catch(reject);
            });
        }
        else {
            return self.loadAll();
        }

    }

    igv.FastaSequence.prototype.getSequence = function (chr, start, end) {

        if (this.indexed) {
            return getSequenceIndexed.call(this, chr, start, end);
        }
        else {
            return getSequenceNonIndexed.call(this, chr, start, end);

        }

    }

    function getSequenceIndexed(chr, start, end) {

        var self = this;

        return new Promise(function (fulfill, reject) {
            var interval = self.interval;

            if (interval && interval.contains(chr, start, end)) {

                fulfill(getSequenceFromInterval(interval, start, end));
            }
            else {

                //console.log("Cache miss: " + (interval === undefined ? "nil" : interval.chr + ":" + interval.start + "-" + interval.end));

                // Expand query, to minimum of 100kb
                var qstart = start;
                var qend = end;
                if ((end - start) < 100000) {
                    var w = (end - start);
                    var center = Math.round(start + w / 2);
                    qstart = Math.max(0, center - 50000);
                    qend = center + 50000;
                }


                self.readSequence(chr, qstart, qend).then(function (seqBytes) {
                    self.interval = new igv.GenomicInterval(chr, qstart, qend, seqBytes);
                    fulfill(getSequenceFromInterval(self.interval, start, end));
                }).catch(reject);
            }

            function getSequenceFromInterval(interval, start, end) {
                var offset = start - interval.start;
                var n = end - start;
                var seq = interval.features ? interval.features.substr(offset, n) : null;
                return seq;
            }
        });
    }


    function getSequenceNonIndexed(chr, start, end) {

        var self = this;

        return new Promise(function (fulfill, reject) {
            var seq = self.sequences[chr];
            if (seq && seq.length > end) {
                fulfill(seq.substring(start, end));
            }
        });

    }

    igv.FastaSequence.prototype.getIndex = function () {

        var self = this;

        return new Promise(function (fulfill, reject) {

            if (self.index) {
                fulfill(self.index);
            } else {
                igvxhr.load(self.indexFile, {
                    withCredentials: self.withCredentials
                }).then(function (data) {
                    var lines = data.splitLines();
                    var len = lines.length;
                    var lineNo = 0;

                    self.chromosomeNames = [];     // TODO -- eliminate this side effect !!!!
                    self.index = {};               // TODO -- ditto
                    while (lineNo < len) {

                        var tokens = lines[lineNo++].split("\t");
                        var nTokens = tokens.length;
                        if (nTokens == 5) {
                            // Parse the index line.
                            var chr = tokens[0];
                            var size = parseInt(tokens[1]);
                            var position = parseInt(tokens[2]);
                            var basesPerLine = parseInt(tokens[3]);
                            var bytesPerLine = parseInt(tokens[4]);

                            var indexEntry = {
                                size: size, position: position, basesPerLine: basesPerLine, bytesPerLine: bytesPerLine
                            };

                            self.chromosomeNames.push(chr);
                            self.index[chr] = indexEntry;
                        }
                    }

                    if (fulfill) {
                        fulfill(self.index);
                    }
                }).catch(reject);
            }
        });
    }

    igv.FastaSequence.prototype.loadAll = function () {

        var self = this;

        return new Promise(function (fulfill, reject) {
            self.chromosomeNames = [];
            self.chromosomes = {};
            self.sequences = {};

            igvxhr.load(self.file, {
                withCredentials: self.withCredentials

            }).then(function (data) {

                var lines = data.splitLines(),
                    len = lines.length,
                    lineNo = 0,
                    nextLine,
                    currentSeq = "",
                    currentChr,
                    order = 0;


                while (lineNo < len) {
                    nextLine = lines[lineNo++].trim();
                    if (nextLine.startsWith("#") || nextLine.length === 0) {
                        continue;
                    }
                    else if (nextLine.startsWith(">")) {
                        if (currentSeq) {
                            self.chromosomeNames.push(currentChr);
                            self.sequences[currentChr] = currentSeq;
                            self.chromosomes[currentChr] = new igv.Chromosome(currentChr, order++, currentSeq.length);
                        }
                        currentChr = nextLine.substr(1).split("\\s+")[0];
                        currentSeq = "";
                    }
                    else {
                        currentSeq += nextLine;
                    }
                }

                fulfill();

            });
        });
    }

    igv.FastaSequence.prototype.readSequence = function (chr, qstart, qend) {

        //console.log("Read sequence " + chr + ":" + qstart + "-" + qend);
        var self = this;

        return new Promise(function (fulfill, reject) {
            self.getIndex().then(function () {

                var idxEntry = self.index[chr];
                if (!idxEntry) {
                    console.log("No index entry for chr: " + chr);

                    // Tag interval with null so we don't try again
                    self.interval = new igv.GenomicInterval(chr, qstart, qend, null);
                    fulfill(null);

                } else {

                    var start = Math.max(0, qstart);    // qstart should never be < 0
                    var end = Math.min(idxEntry.size, qend);
                    var bytesPerLine = idxEntry.bytesPerLine;
                    var basesPerLine = idxEntry.basesPerLine;
                    var position = idxEntry.position;
                    var nEndBytes = bytesPerLine - basesPerLine;

                    var startLine = Math.floor(start / basesPerLine);
                    var endLine = Math.floor(end / basesPerLine);

                    var base0 = startLine * basesPerLine;   // Base at beginning of start line

                    var offset = start - base0;

                    var startByte = position + startLine * bytesPerLine + offset;

                    var base1 = endLine * basesPerLine;
                    var offset1 = end - base1;
                    var endByte = position + endLine * bytesPerLine + offset1 - 1;
                    var byteCount = endByte - startByte + 1;
                    if (byteCount <= 0) {
                        fulfill(null);
                    }

                    igvxhr.load(self.file, {
                        range: {start: startByte, size: byteCount}
                    }).then(function (allBytes) {

                        var nBases,
                            seqBytes = "",
                            srcPos = 0,
                            desPos = 0,
                            allBytesLength = allBytes.length;

                        if (offset > 0) {
                            nBases = Math.min(end - start, basesPerLine - offset);
                            seqBytes += allBytes.substr(srcPos, nBases);
                            srcPos += (nBases + nEndBytes);
                            desPos += nBases;
                        }

                        while (srcPos < allBytesLength) {
                            nBases = Math.min(basesPerLine, allBytesLength - srcPos);
                            seqBytes += allBytes.substr(srcPos, nBases);
                            srcPos += (nBases + nEndBytes);
                            desPos += nBases;
                        }

                        fulfill(seqBytes);
                    }).catch(reject)
                }
            }).catch(reject)
        });
    }


    return igv;

})(igv || {});


/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    /**
     * feature source for "bed like" files (tab delimited files with 1 feature per line: bed, gff, vcf, etc)
     *
     * @param config
     * @constructor
     */
    igv.AneuFeatureSource = function (config, thefilename) {

        this.config = config || {};
        // check if type is redline or diff
        //console.log("AneuFeatureSource:  filename="+thefilename+", config="+JSON.stringify(config));
        // need function to cut off last part of file and add redline or diff file

        var getPath = function (urlorfile) {
            var last = urlorfile.lastIndexOf("/");
            var path = urlorfile.substring(0, last + 1);
            // console.log("Getting path of file or url "+urlorfile+"="+path);
            return path;
        }

        if (config.localFile) {

            var path = getPath(config.localFile.name);
            this.localFile = config.localFile;
            this.filename = path + thefilename;
            //  console.log("Got localfile: "+JSON.stringify(config)+", this.filename="+this.filename);
        }
        else {

            var path = getPath(config.url);

            this.url = path + thefilename;
            this.filename = thefilename;
            this.headURL = config.headURL || this.filename;
            //   console.log("Got URL: "+config.url+"-> url="+this.url);
        }


        this.parser = getParser("aneu");
    };


    function getParser(format) {
        return new igv.FeatureParser(format);
    }

    /**
     * Required function fo all data source objects.  Fetches features for the
     * range requested and passes them on to the success function.  Usually this is
     * a function that renders the features on the canvas
     *
     * @param chr
     * @param bpStart
     * @param bpEnd
     * @param success -- function that takes an array of features as an argument
     */
    igv.AneuFeatureSource.prototype.getFeatures = function (chr, bpStart, bpEnd, success) {

        var myself = this,
            range = new igv.GenomicInterval(chr, bpStart, bpEnd),
            featureCache = this.featureCache;

        if (featureCache && (featureCache.range === undefined || featureCache.range.containsRange(range))) {//}   featureCache.range.contains(queryChr, bpStart, bpEnd))) {
            var features = this.featureCache.queryFeatures(chr, bpStart, bpEnd);
            // console.log("getFeatures: got "+features.length+" cached features on chr "+chr);
            success(features);

        }
        else {
            //  console.log("getFeatures: calling loadFeatures");
            this.loadFeatures(function (featureList) {
                    //  console.log("Creating featureCache with "+featureList.length+ " features");
                    myself.featureCache = new igv.FeatureCache(featureList);   // Note - replacing previous cache with new one                    
                    // Finally pass features for query interval to continuation

                    var features = myself.featureCache.queryFeatures(chr, bpStart, bpEnd);
                    //  console.log("calling success "+success);
                    //  console.log("features from queryCache "+features);
                    success(features);

                },
                range);   // Currently loading at granularity of chromosome
        }

    };


    /**
     * Get the feature cache.  This method is exposed for use by cursor.  Loads all features (no index).
     * @param success
     */
    igv.AneuFeatureSource.prototype.getFeatureCache = function (success) {

        var myself = this;

        if (this.featureCache) {
            success(this.featureCache);
        }
        else {
            this.loadFeatures(function (featureList) {
                //myself.featureMap = featureMap;
                myself.featureCache = new igv.FeatureCache(featureList);
                // Finally pass features for query interval to continuation
                success(myself.featureCache);

            });
        }
    }

    /**
     *
     * @param success
     * @param range -- genomic range to load.
     */
    igv.AneuFeatureSource.prototype.loadFeatures = function (continuation, range) {

        var self = this;
        var parser = self.parser;
        var options = {
                headers: self.config.headers,           // http headers, not file header
                tokens: self.config.tokens,           // http headers, not file header
                withCredentials: self.config.withCredentials
            },
            success = function (data) {
                // console.log("Loaded data, calling parser.parseFeatures: parser="+parser);
                self.header = parser.parseHeader(data);
                var features = parser.parseFeatures(data);
                //console.log("Calling success "+success);
                //console.log("nr features in argument "+features.length);
                continuation(features);   // <= PARSING DONE HERE
            };

        //  console.log("=================== load features. File is: "+myself.localFile+"/"+myself.url);
        if (self.localFile) {
            //    console.log("Loading local file: "+JSON.stringify(localFile));
            igvxhr.loadStringFromFile(self.localFile, options).then(success);
        }
        else {
            //console.log("Loading URL "+myself.url);
            igvxhr.loadString(self.url, options).then(success);
        }


    }

    return igv;
})
(igv || {});
/*R
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    var debug = false;

    var log = function (msg) {
        if (debug) {
            var d = new Date();
            var time = d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds();
            if (typeof copy != "undefined") {
                copy(msg);
            }
            if (typeof console != "undefined") {
                console.log("AneuTrack: " + time + " " + msg);
            }

        }
    };
    var sortDirection = "ASC";

    igv.AneuTrack = function (config) {

        igv.configTrack(this, config);

        this.maxHeight = config.maxHeight - 2 || 500;
        this.sampleSquishHeight = config.sampleSquishHeight || 20;
        this.sampleExpandHeight = config.sampleExpandHeight || 125;

        this.sampleHeight = this.sampleExpandHeight;

        this.highColor = config.highColor || 'rgb(30,30,255)';
        this.lowColor = config.lowColor || 'rgb(220,0,0)';
        this.midColor = config.midColor || 'rgb(150,150,150)';
        this.posColorScale = config.posColorScale || new igv.GradientColorScale({
                low: 0.1,
                lowR: 255,
                lowG: 255,
                lowB: 255,
                high: 1.5,
                highR: 255,
                highG: 0,
                highB: 0
            });
        this.negColorScale = config.negColorScale || new igv.GradientColorScale({
                low: -1.5,
                lowR: 0,
                lowG: 0,
                lowB: 255,
                high: -0.1,
                highR: 255,
                highG: 255,
                highB: 255
            });

        this.sampleCount = 0;
        this.samples = {};
        this.sampleNames = [];

        log("AneuTrack: config: " + JSON.stringify(config));
        this.config = config;

    };

    igv.AneuTrack.prototype.popupMenuItems = function (popover) {

        var myself = this;

        return [];

    };

    igv.AneuTrack.prototype.getSummary = function (chr, bpStart, bpEnd, continuation) {
        var me = this;
        var filtersummary = function (redlinedata) {
            var summarydata = [];
            //log("AneuTrack: getSummary for: " + JSON.stringify(me.featureSourceRed.url));
            for (i = 0, len = redlinedata.length; i < len; i++) {
                var feature = redlinedata[i];
                if (Math.abs(feature.score - 2) > 0.5 && (feature.end - feature.start > 5000000)) {
                    //log("adding summary: "+JSON.stringify(feature));
                    summarydata.push(feature);
                }
            }
            continuation(summarydata);
        };
        if (this.featureSourceRed) {
            this.featureSourceRed.getFeatures(chr, bpStart, bpEnd, filtersummary);
        }
        else {
            log("Aneu track has no summary data yet");
            continuation(null);
        }
    }

    igv.AneuTrack.prototype.loadSummary = function (chr, bpStart, bpEnd, continuation) {
        var self = this;
        if (this.featureSourceRed) {
            this.featureSourceRed.getFeatures(chr, bpStart, bpEnd, continuation);
        }
        else {
            //log("Data is not loaded yet. Loading json first. tokens are "+me.config.tokens);

            var afterJsonLoaded = function (json) {
                if (json) {
                    json = JSON.parse(json);
//        		log("Got json: " + JSON.stringify(json));
                    self.featureSourceRed = new igv.AneuFeatureSource(config, json.redline);
                    self.getSummary(chr, bpStart, bpEnd, continuation);
                }
                else {
                    //log("afterJsonLoaded: got no json result for "+config.url);
                }
            };

            afterload = {
                headers: self.config.headers, // http headers, not file header
                tokens: self.config.tokens, // http headers, not file header
                success: afterJsonLoaded,
                withCredentials: self.config.withCredentials
            };
            var config = self.config;
            if (config.localFile) {
                igvxhr.loadStringFromFile(config.localFile, afterload);
            } else {
                igvxhr.loadString(config.url, afterload);
            }
            return null;
        }
    }

    igv.AneuTrack.prototype.getFeatures = function (chr, bpStart, bpEnd) {

        var self = this;

        return new Promise(function (fulfill, reject) {

            loadJson.call(self).then(function () {
                // first load diff file, then load redline file, THEN call
                // continuation
                var loadsecondfile = function (redlinedata) {
                    // console.log("loadsecondfile: argument redlinedata:
                    // "+JSON.stringify(redlinedata));
                    self.redlinedata = redlinedata;
                    // console.log("Now loading diff data, using original
                    // continuation");
                    self.featureSource.getFeatures(chr, bpStart, bpEnd, fulfill);
                };
                // console.log("About to load redline file");
                self.featureSourceRed.getFeatures(chr, bpStart, bpEnd, loadsecondfile);


            });
        });
    }

    function loadJson() {

        var self = this;

        return new Promise(function (fulfill, reject) {

            if (self.featureSourceRed) {
                fulfill();
            }
            else {
                var afterJsonLoaded = function (json) {
                        json = JSON.parse(json);
                        log("Got json: " + json + ", diff :" + json.diff);
                        self.featureSource = new igv.AneuFeatureSource(config, json.diff);
                        self.featureSourceRed = new igv.AneuFeatureSource(config, json.redline);
                        fulfill();
                    },

                    afterload = {
                        headers: self.config.headers, // http headers, not file header
                        tokens: self.config.tokens, // http headers, not file header
                        withCredentials: self.config.withCredentials
                    };

                var config = self.config;
                if (config.localFile) {
                    igvxhr.loadStringFromFile(config.localFile, afterload).then(afterJsonLoaded);
                } else {
                    igvxhr.loadString(config.url, afterload).then(afterJsonLoaded);
                }
            }
        });
    }


    igv.AneuTrack.prototype.getColor = function (value) {
        var expected = 2;
        if (value < expected) {
            color = this.lowColor;
        } else if (value > expected) {
            color = this.highColor;
        }
        else color = this.midColor;
        return color;
    };
    igv.AneuTrack.prototype.paintAxis = function (ctx, pixelWidth, pixelHeight) {

        var track = this,
            yScale = (track.maxLogP - track.minLogP) / pixelHeight;

        var font = {
            'font': 'normal 10px Arial',
            'textAlign': 'right',
            'strokeStyle': "black"
        };

        igv.graphics.fillRect(ctx, 0, 0, pixelWidth, pixelHeight, {'fillStyle': "rgb(255, 255, 255)"});

        function computeH(min, max, value, maxpixels) {
            return maxpixels - Math.round((value - min) / max * maxpixels);
        }

        var max = track.max;
        if (!max) max = 8;
        var min = 0;
        var x = 49;

        igv.graphics.strokeLine(ctx, x, computeH(min, max, 0, track.maxheight), x, computeH(min, max, max, track.maxheight), font); // Offset

        x = x - 5;
        for (var p = 0; p <= max; p += 1) {
            var h = computeH(min, max, p, track.maxheight);
            igv.graphics.strokeLine(ctx, x, h, x + 5, h, font); // Offset dashes up by 2							// pixel
            if (p > 0 && p < max) igv.graphics.fillText(ctx, p, x - 4, h + 3, font); // Offset
        }

        font['textAlign'] = 'center';
        igv.graphics.fillText(ctx, "ploidy", x - 15, pixelHeight / 2, font, {rotate: {angle: -90}});


    };

    igv.AneuTrack.prototype.draw = function (options) {

        var myself = this,
            ctx,
            bpPerPixel,
            bpStart,
            pixelWidth,
            pixelHeight,
            bpEnd,
            segment,
            len,
            sample,
            i,
            y,
            color,
            value,
            px,
            px1,
            pw,
            xScale;

        ctx = options.context;
        pixelWidth = options.pixelWidth;
        pixelHeight = options.pixelHeight;
//	
        var max = 4;
        var min = 0;

        var PLOIDYMAX = 10;
        // deubugging
        igv.graphics.fillRect(ctx, 0, 0, pixelWidth, pixelHeight, {'fillStyle': "rgb(255, 255, 255)"});

        var track = this;
        window.track = track;
        var computeMinMax = function (featureList) {
            for (i = 0, len = featureList.length; i < len; i++) {
                sample = featureList[i].sample;
                var value = featureList[i].value;
                if (value > max) max = value;
                if (value < min) min = value;
            }
            if (max > PLOIDYMAX) max = PLOIDYMAX;
            min = Math.max(min, 0);
            track.max = max;
        };
        var drawFeatureList = function (ctx, featureList, debug) {
            bpPerPixel = options.bpPerPixel;
            bpStart = options.bpStart;
            bpEnd = bpStart + pixelWidth * bpPerPixel + 1;
            xScale = bpPerPixel;

            for (i = 0, len = featureList.length; i < len; i++) {
                sample = featureList[i].sample;
                if (sample && this.samples && this.samples.hasOwnProperty(sample)) {
                    this.samples[sample] = myself.sampleCount;
                    this.sampleNames.push(sample);
                    this.sampleCount++;
                }
            }

            checkForLog(featureList);
            var expected = 2;
            if (myself.isLog) {
                min = 0;
                expected = 0;
            }
            var maxheight = myself.height - 4;
            myself.maxheight = maxheight;


            var len = featureList.length;
            //  log("AneuTrack: Drawing "+len+" features between "+bpStart+"-"+bpEnd+", maxheight="+maxheight);
            // console.log("AneuTrack: Drawing: min ="+min+", max="+max);

            for (i = 0; i < len; i++) {


                segment = featureList[i];
                if (segment.end < bpStart) continue;
                if (segment.start > bpEnd) break;

                if (segment.sample) {
                    y = myself.samples[segment.sample] * myself.sampleHeight;
                    log("Got sample y=" + y);
                } else y = 0;

                value = segment.score;
                color = myself.midColor;
                if (myself.isLog) {
                    value = Math.log2(value / 2);
                    if (value < expected - 0.1) {
                        color = myself.negColorScale.getColor(value);
                    } else if (value > expected + 0.1) {
                        color = myself.posColorScale.getColor(value);
                    }
                } else {
                    if (value < expected - 0.2) {
                        color = myself.lowColor;
                    } else if (value > expected + 0.2) {
                        color = myself.highColor;
                    }
                }

                //debug = i < 5 && value == 0;
                //if (debug == true) log("Feature: " + JSON.stringify(segment));

                px = Math.round((segment.start - bpStart) / xScale);
                px1 = Math.round((segment.end - bpStart) / xScale);
                pw = Math.max(2, px1 - px);

                // the value determines the height
                if (value <= max) {
                    var h = computeH(min, max, value, maxheight);
                    if (debug == true)
                        log("       Got value " + value + ", h=" + h + ", y+h=" + (y + h) + ", px=" + px
                            + ", px1=" + px1 + ", pw=" + pw + ", color=" + color + ", maxh=" + maxheight);
                    // use different plot types
                    igv.graphics.fillRect(ctx, px, y + h, pw, 2, {
                        fillStyle: color
                    });
                }
                //else log("Value is too large: "+value);

            }
        };
        var maxheight = myself.height - 4;
        var font = {
            'font': 'normal 10px Arial',
            'textAlign': 'right',
            'strokeStyle': 'rgb(150,150,150)',
            'fillStyle': 'rgb(150,150,150)'
        };
        if (options.features) {
            computeMinMax(options.features);
        }
        if (this.redlinedata) {
            // console.log("Drawing redline data on top");
            computeMinMax(this.redlinedata);
        }
        //log("Got min/max: "+min+"-"+max);
        if (min < 2 && 2 < max) {

            var mid = computeH(min, max, 2.0, maxheight);
            console.log("drawing dashed line and solid line at " + mid + " to " + pixelWidth);
            igv.graphics.dashedLine(ctx, 20, mid, pixelWidth, mid, 4, font);
            var zero = computeH(min, max, 0, maxheight);
            igv.graphics.strokeLine(ctx, 20, zero, pixelWidth, zero, font);
        }
        else log("NOT drawing line at 2");
        if (options.features) {

            // console.log("Drawing diff data first");
            drawFeatureList(ctx, options.features, false);
        } else {
            console.log("No diff feature list. options=" + JSON.stringify(options));
        }
        if (this.redlinedata) {
            // console.log("Drawing redline data on top");
            drawFeatureList(ctx, this.redlinedata, false);
        } else {
            console.log("No redline feature list");
        }
        // draw axis is in paitnControl

        function computeH(min, max, value, maxpixels) {
            // console.log("comptuteH. min/max="+min+"/"+max+",
            // maxpixels="+maxpixels);
            return maxpixels - Math.round((value - min) / max * maxpixels);
        }

        function checkForLog(featureList) {
            var i;
            if (myself.isLog === undefined) {
                myself.isLog = false;
                for (i = 0; i < featureList.length; i++) {
                    if (featureList[i].value < 0) {
                        myself.isLog = true;
                        return;
                    }
                }
            }
        }
    };

    /**
     * Optional method to compute pixel height to accomodate the list of
     * features. The implementation below has side effects (modifiying the
     * samples hash). This is unfortunate, but harmless.
     *
     * @param features
     * @returns {number}
     */
    igv.AneuTrack.prototype.computePixelHeight = function (features) {
        // console.log("computePixelHeight");
        for (i = 0, len = features.length; i < len; i++) {
            sample = features[i].sample;
            if (this.samples && !this.samples.hasOwnProperty(sample)) {
                this.samples[sample] = this.sampleCount;
                this.sampleNames.push(sample);
                this.sampleCount++;
            }
        }
        this.sampleCount = Math.max(1, this.sampleCount);
        var h = Math.max(30, this.sampleCount * this.sampleHeight);
        this.height = h;
//	console.log("Computed height for " + features.length + " features, samplecount " + this.sampleCount
//		+ " and height " + this.sampleHeight + ": " + h);
        return h;
    };

    /**
     * Sort samples by the average value over the genomic range in the direction
     * indicated (1 = ascending, -1 descending)
     */
    igv.AneuTrack.prototype.sortSamples = function (chr, bpStart, bpEnd, direction, callback) {

        var self = this, segment, min, max, f, i, s, sampleNames, len = bpEnd - bpStart, scores = {};

        this.featureSource.getFeatures(chr, bpStart, bpEnd, function (featureList) {

            // Compute weighted average score for each sample
            for (i = 0, len = featureList.length; i < len; i++) {

                segment = featureList[i];

                if (segment.end < bpStart) continue;
                if (segment.start > bpEnd) break;

                min = Math.max(bpStart, segment.start);
                max = Math.min(bpEnd, segment.end);
                f = (max - min) / len;

                s = scores[segment.sample];
                if (!s) s = 0;
                scores[segment.sample] = s + f * segment.value;

            }

            // Now sort sample names by score
            sampleNames = Object.keys(self.samples);
            sampleNames.sort(function (a, b) {

                var s1 = scores[a];
                var s2 = scores[b];
                if (!s1) s1 = Number.MAX_VALUE;
                if (!s2) s2 = Number.MAX_VALUE;

                if (s1 == s2)
                    return 0;
                else if (s1 > s2)
                    return direction;
                else return direction * -1;

            });

            // Finally update sample hash
            for (i = 0; i < sampleNames.length; i++) {
                self.samples[sampleNames[i]] = i;
            }
            self.sampleNames = sampleNames;
            
            

            callback();

        });
    };

    /**
     * Handle an alt-click. TODO perhaps generalize this for all tracks
     * (optional).
     *
     * @param genomicLocation
     * @param event
     */
    igv.AneuTrack.prototype.altClick = function (genomicLocation, event) {

        // Define a region 5 "pixels" wide in genomic coordinates
        var refFrame = igv.browser.referenceFrame, bpWidth = refFrame.toBP(2.5), bpStart = genomicLocation - bpWidth, bpEnd = genomicLocation
            + bpWidth, chr = refFrame.chr, track = this;

        this.sortSamples(chr, bpStart, bpEnd, sortDirection);

        sortDirection = (sortDirection === "ASC" ? "DESC" : "ASC");
    };

    igv.AneuTrack.prototype.popupData = function (genomicLocation, xOffset, yOffset) {

        var sampleName, row = Math.floor(yOffset / this.sampleHeight), items;

        log("popupData for row " + row + ", sampleNames=" + JSON.stringify(this.sampleNames));
        if (row < this.sampleNames.length) {

            sampleName = this.sampleNames[row];

            if (sampleName) {
                items = [{
                    name: "Sample",
                    value: sampleName
                }];

            } else {
                items = [];
            }
            // We use the featureCache property rather than method to avoid
            // async load. If the
            // feature is not already loaded this won't work, but the user
            // wouldn't be mousing over it either.
            if (this.featureSource.featureCache) {
                var chr = igv.browser.referenceFrame.chr; // TODO -- this
                // should be passed
                // in
                var featureList = this.featureSource.featureCache.queryFeatures(chr, genomicLocation, genomicLocation);
                featureList.forEach(function (f) {
                    if (f.sample === sampleName) {
                        items.push({
                            name: "Value",
                            value: f.value
                        });
                        items.push({
                            name: "Start",
                            value: f.start
                        });
                        items.push({
                            name: "End",
                            value: f.end
                        });
                    }
                });
            }
            if (this.featureSourceRed.featureCache) {
                var chr = igv.browser.referenceFrame.chr; // TODO -- this
                // should be passed
                // in
                var featureList = this.featureSourceRed.featureCache.queryFeatures(chr, genomicLocation,
                    genomicLocation);
                featureList.forEach(function (f) {
                    if (f.sample === sampleName) {
                        items.push({
                            name: "Value",
                            value: f.value
                        });
                        items.push({
                            name: "Start",
                            value: f.start
                        });
                        items.push({
                            name: "End",
                            value: f.end
                        });
                    }
                });
            }

            return items;
        }

        return null;
    }

    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    /**
     * Object for caching lists of features.  Supports effecient queries for sub-range  (chr, start, end)
     *
     * @param featureList
     * @param The genomic range spanned by featureList (optional)
     * @constructor
     */

    igv.FeatureCache = function (featureList, range) {
        this.treeMap = buildTreeMap(featureList);
        this.range = range;
    }

    igv.FeatureCache.prototype.queryFeatures = function (chr, start, end) {

        var featureList, intervalFeatures, feature, len, i, tree, intervals;

        tree = this.treeMap[chr];

        if (!tree) return [];

        intervals = tree.findOverlapping(start, end);

        if (intervals.length == 0) {
            return [];
        }
        else {
            // Trim the list of features in the intervals to those
            // overlapping the requested range.
            // Assumption: features are sorted by start position

            featureList = [];

            intervals.forEach(function (interval) {
                intervalFeatures = interval.value;
                len = intervalFeatures.length;
                for (i = 0; i < len; i++) {
                    feature = intervalFeatures[i];
                    if (feature.start > end) break;
                    else if (feature.end >= start) {
                        featureList.push(feature);
                    }
                }
            });

            return featureList;
        }

    };

    igv.FeatureCache.prototype.allFeatures = function () {

        var allFeatures = [];
        var treeMap = this.treeMap;
        if (treeMap) {
            for (var key in treeMap) {
                if (treeMap.hasOwnProperty(key)) {

                    var tree = treeMap[key];
                    tree.mapIntervals(function (interval) {
                        allFeatures = allFeatures.concat(interval.value);
                    });
                }
            }
        }
        return allFeatures;

    }

    function buildTreeMap(featureList) {

        var featureCache = {},
            chromosomes = [],
            treeMap = {},
            genome = igv.browser ? igv.browser.genome : null;

        if (featureList) {

            featureList.forEach(function (feature) {

                var chr = feature.chr,
                    geneList;

                // Translate to "official" name
                if(genome) chr = genome.getChromosomeName(chr);

                geneList = featureCache[chr];

                if (!geneList) {
                    chromosomes.push(chr);
                    geneList = [];
                    featureCache[chr] = geneList;
                }

                geneList.push(feature);

            });


            // Now build interval tree for each chromosome

            for (i = 0; i < chromosomes.length; i++) {
                chr = chromosomes[i];
                treeMap[chr] = buildIntervalTree(featureCache[chr]);
            }
        }

        return treeMap;
    };

    /**
     * Build an interval tree from the feature list for fast interval based queries.   We lump features in groups
     * of 10, or total size / 100,   to reduce size of the tree.
     *
     * @param featureList
     */
    function buildIntervalTree(featureList) {

        var i, e, iStart, iEnd, tree, chunkSize, len, subArray;

        tree = new igv.IntervalTree();
        len = featureList.length;

        chunkSize = Math.max(10, Math.round(len / 100));

        featureList.sort(function (f1, f2) {
            return (f1.start === f2.start ? 0 : (f1.start > f2.start ? 1 : -1));
        });

        for (i = 0; i < len; i += chunkSize) {
            e = Math.min(len, i + chunkSize);
            subArray = featureList.slice(i, e);
            iStart = subArray[0].start;
            //
            iEnd = iStart;
            subArray.forEach(function (feature) {
                iEnd = Math.max(iEnd, feature.end);
            });
            tree.insert(iStart, iEnd, subArray);
        }

        return tree;
    }


    return igv;
})
(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    const MAX_GZIP_BLOCK_SIZE = (1 << 16);

    /**
     * Reader for "bed like" files (tab delimited files with 1 feature per line: bed, gff, vcf, etc)
     *
     * @param config
     * @constructor
     */
    igv.FeatureFileReader = function (config) {

        this.config = config || {};

        if (config.localFile) {
            this.localFile = config.localFile;
            this.filename = config.localFile.name;
        }
        else {
            this.url = config.url;
            this.indexURL = config.indexURL;
            this.headURL = config.headURL || this.filename;

            var uriParts = igv.parseUri(config.url);
            this.filename = uriParts.file;
            this.path = uriParts.path;
        }

        this.format = config.format;

        this.parser = getParser.call(this, this.format, config.decode);
    };


    function getParser(format, decode) {
        switch (format) {
            case "vcf":
                return new igv.VcfParser();
            case "seg" :
                return new igv.SegParser();
            default:
                return new igv.FeatureParser(format, decode, this.config);
        }

    }

    // seg files don't have an index
    function isIndexable() {
        var configIndexURL = this.config.indexURL,
            type = this.type,
            configIndexed = this.config.indexed;

        return configIndexURL || (type != "wig" && configIndexed != false);
    }


    /**
     * Return a Promise for the async loaded index
     */
    function loadIndex() {
        var idxFile = this.indexURL;
        if (this.filename.endsWith(".gz")) {
            if (!idxFile) idxFile = this.url + ".tbi";
            return igv.loadBamIndex(idxFile, this.config, true);
        }
        else {
            if (!idxFile) idxFile = this.url + ".idx";
            return igv.loadTribbleIndex(idxFile, this.config);
        }
    }

    function loadFeaturesNoIndex() {

        var self = this;

        return new Promise(function (fulfill, reject) {
            var options = {
                headers: self.config.headers,           // http headers, not file header
                withCredentials: self.config.withCredentials,
                method: self.config.method,
                cancerStudyId: self.config.cancerStudyId,
                hugoSymbol: self.config.hugoSymbol,
                sampleIds: self.config.sampleIds
            };
 
            function parseData(data) {
                if(self.config.json){
                    self.header = {"headings":["Sample","Chromosome","Start","End","Num_Probes","Segment_Mean"],"lineCount":1};
                    fulfill(data);
                }else{
                    self.header = self.parser.parseHeader(data);
                    if (self.header instanceof String && self.header.startsWith("##gff-version 3")) {
                        self.format = 'gff3';
                    }
                    fulfill(self.parser.parseFeatures(data));   // <= PARSING DONE HERE
                }
            };


            if (self.localFile) {
                igvxhr.loadStringFromFile(self.localFile, options).then(parseData).catch(reject);
            }
            else if(self.config.json){
                //this is customized for cBioPortal use case
                $.when($.ajax({
                    method : options.method,
                    url : self.url,
                    data : {
                        cancerStudyId: options.cancerStudyId,
                        chromosomes: options.chromosome,
                        sampleIds: options.sampleIds
                    }
                })).then(
                    function(response) {
                        parseData(response);
                    });
            }
            else {
                igvxhr.loadString(self.url, options).then(parseData).catch(reject);
            }


        });
    }


    function loadFeaturesWithIndex(chr, start, end) {

        //console.log("Using index");
        var self = this;

        return new Promise(function (fulfill, reject) {

            var blocks,
                index = self.index,
                tabix = index && index.tabix,
                refId = tabix ? index.sequenceIndexMap[chr] : chr,
                promises = [];

            blocks = index.blocksForRange(refId, start, end);

            if (!blocks || blocks.length === 0) {
                fulfill(null);       // TODO -- is this correct?  Should it return an empty array?
            }
            else {

                blocks.forEach(function (block) {

                    promises.push(new Promise(function (fulfill, reject) {

                        var startPos = block.minv.block,
                            startOffset = block.minv.offset,
                            endPos = endPos = block.maxv.block + MAX_GZIP_BLOCK_SIZE,
                            options = {
                                headers: self.config.headers, // http headers, not file header
                                range: {start: startPos, size: endPos - startPos + 1},
                                withCredentials: self.config.withCredentials
                            },
                            success;

                        success = function (data) {

                            var inflated, slicedData;

                            if (index.tabix) {

                                inflated = igvxhr.arrayBufferToString(igv.unbgzf(data));
                                // need to decompress data
                            }
                            else {
                                inflated = data;
                            }

                            slicedData = startOffset ? inflated.slice(startOffset) : inflated;
                            var f = self.parser.parseFeatures(slicedData);
                            fulfill(f);
                        };


                        // Async load
                        if (self.localFile) {
                            igvxhr.loadStringFromFile(self.localFile, options).then(success).catch(reject);
                        }
                        else {
                            if (index.tabix) {
                                igvxhr.loadArrayBuffer(self.url, options).then(success).catch(reject);
                            }
                            else {
                                igvxhr.loadString(self.url, options).then(success).catch(reject);
                            }
                        }
                    }))
                });

                Promise.all(promises).then(function (featureArrays) {

                    var i, allFeatures;

                    if (featureArrays.length === 1) {
                        allFeatures = featureArrays[0];
                    } else {
                        allFeatures = featureArrays[0];

                        for (i = 1; i < featureArrays.length; i++) {
                            allFeatures = allFeatures.concat(featureArrays[i]);
                        }

                        allFeatures.sort(function (a, b) {
                            return a.start - b.start;
                        });
                    }

                    fulfill(allFeatures)
                }).catch(reject);
            }
        });

    }


    function getIndex() {

        var self = this,
            isIndeedIndexible = isIndexable.call(this);
        return new Promise(function (fulfill, reject) {

            if (self.indexed === undefined && isIndeedIndexible) {
                loadIndex.call(self).then(function (index) {
                    if (index) {
                        self.index = index;
                        self.indexed = true;
                    }
                    else {
                        self.indexed = false;
                    }
                    fulfill(self.index);
                });
            }
            else {
                fulfill(self.index);   // Is either already loaded, or there isn't one
            }

        });
    }

    igv.FeatureFileReader.prototype.readHeader = function () {

        var self = this;

        return new Promise(function (fulfill, reject) {


            if (self.header) {
                fulfill(self.header);
            }

            else {

                // We force a load of the index first

                getIndex.call(self).then(function (index) {

                    if (index) {
                        // Load the file header (not HTTP header) for an indexed file.
                        // TODO -- note this will fail if the file header is > 65kb in size
                        var options = {
                                headers: self.config.headers,           // http headers, not file header
                                bgz: index.tabix,
                                range: {start: 0, size: 65000},
                                withCredentials: self.config.withCredentials
                            },
                            success = function (data) {
                                self.header = self.parser.parseHeader(data);
                                fulfill(self.header);
                            };

                        if (self.localFile) {
                            igvxhr.loadStringFromFile(self.localFile, options).then(success);
                        }
                        else {
                            igvxhr.loadString(self.url, options).then(success).catch(reject);
                        }
                    }
                    else {
                        loadFeaturesNoIndex.call(self, undefined).then(function (features) {
                            var header = self.header || {};
                            header.features = features;
                            fulfill(header);
                        }).catch(reject);
                    }
                });
            }
        });

    }

    /**
     *
     * @param fulfill
     * @param range -- genomic range to load.  For use with indexed source (optional)
     */
    igv.FeatureFileReader.prototype.readFeatures = function (chr, start, end) {

        var self = this;

        return new Promise(function (fulfill, reject) {

            if (self.index) {
                loadFeaturesWithIndex.call(self, chr, start, end).then(packFeatures).catch(reject);
            }
            else {
                loadFeaturesNoIndex.call(self).then(packFeatures).catch(reject);
            }

            function packFeatures(features) {
                // TODO pack
                fulfill(features);
            }

        });
    }


    return igv;
})
(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 *  Define parsers for bed-like files  (.bed, .gff, .vcf, etc).  A parser should implement 2 methods
 *
 *     parseHeader(data) - return an object representing a header or metadata.  Details are format specific
 *
 *     parseFeatures(data) - return an array of features
 *
 */


var igv = (function (igv) {

    var maxFeatureCount = Number.MAX_VALUE;    // For future use,  controls downsampling

    var gffNameFields = ["Name", "gene_name", "gene", "gene_id", "alias", "locus"];

    /**
     * A factory function.  Return a parser for the given file format.
     */
    igv.FeatureParser = function (format, decode, config) {

        var customFormat;

        this.format = format;
        this.nameField = config ? config.nameField : undefined;
        this.skipRows = 0;   // The number of fixed header rows to skip.  Override for specific types as needed

        if (decode) {
            this.decode = decode;
        }


        switch (format) {
            case "narrowpeak":
            case "broadpeak":
            case "peaks":
                this.decode = decodePeak;
                this.delimiter = /\s+/;
                break;
            case "bedgraph":
                this.decode = decodeBedGraph;
                this.delimiter = /\s+/;
                break;
            case "wig":
                this.decode = decodeWig;
                this.delimiter = /\s+/;
                break;
            case "gff3" :
            case "gff" :
            case "gtf" :
                this.decode = decodeGFF;
                this.delimiter = "\t";
                break;
            case "aneu":
                this.decode = decodeAneu;
                this.delimiter = "\t";
                break;
            case "fusionjuncspan":
                // bhaas, needed for FusionInspector view
                this.decode = decodeFusionJuncSpan;
                this.delimiter = /\s+/;
                break;
            case "gtexgwas":
                this.skipRows = 1;
                this.decode = decodeGtexGWAS;
                this.delimiter = "\t";
                break;
            case "refflat":
                this.decode = decodeRefflat;
                this.delimiter = "\t";
                break;
            default:

                customFormat = igv.browser.getFormat(format);
                if (customFormat !== undefined) {
                    this.decode = decodeCustom;
                    this.format = customFormat;
                    this.delimiter = customFormat.delimiter || "\t";
                }

                else {
                    this.decode = decodeBed;
                    this.delimiter = /\s+/;
                }

        }

    };

    igv.FeatureParser.prototype.parseHeader = function (data) {

        var lines = data.splitLines(),
            len = lines.length,
            line,
            i,
            header;

        for (i = 0; i < len; i++) {
            line = lines[i];
            if (line.startsWith("track") || line.startsWith("#") || line.startsWith("browser")) {
                if (line.startsWith("track")) {
                    header = parseTrackLine(line);
                }
                else if (line.startsWith("##gff-version 3")) {
                    this.format = "gff3";
                    if (!header) header = {};
                    header["format"] = "gff3";
                }
            }
            else {
                break;
            }
        }
        return header;
    };

    igv.FeatureParser.prototype.parseFeatures = function (data) {

        if (!data) return null;

        var wig,
            feature,
            lines = data.splitLines(),
            len = lines.length,
            tokens,
            allFeatures = [],
            line,
            i,
            cnt = 0,
            j,
            decode = this.decode,
            format = this.format,
            delimiter = this.delimiter || "\t";


        for (i = this.skipRows; i < len; i++) {
            line = lines[i];
            if (line.startsWith("track") || line.startsWith("#") || line.startsWith("browser")) {
                continue;
            }
            else if (format === "wig" && line.startsWith("fixedStep")) {
                wig = parseFixedStep(line);
                continue;
            }
            else if (format === "wig" && line.startsWith("variableStep")) {
                wig = parseVariableStep(line);
                continue;
            }

            tokens = lines[i].split(delimiter);
            if (tokens.length < 1) continue;

            feature = decode.call(this, tokens, wig);

            if (feature) {
                if (allFeatures.length < maxFeatureCount) {
                    allFeatures.push(feature);
                }
                else {
                    // Reservoir sampling,  conditionally replace existing feature with new one.
                    j = Math.floor(Math.random() * cnt);
                    if (j < maxFeatureCount) {
                        allFeatures[j] = feature;
                    }
                }
                cnt++;
            }
        }

        return allFeatures;
    };


    function parseFixedStep(line) {

        var tokens = line.split(/\s+/),
            cc = tokens[1].split("=")[1],
            ss = parseInt(tokens[2].split("=")[1], 10),
            step = parseInt(tokens[3].split("=")[1], 10),
            span = (tokens.length > 4) ? parseInt(tokens[4].split("=")[1], 10) : 1;

        return {format: "fixedStep", chrom: cc, start: ss, step: step, span: span, index: 0};

    }

    function parseVariableStep(line) {

        var tokens = line.split(/\s+/),
            cc = tokens[1].split("=")[1],
            span = tokens.length > 2 ? parseInt(tokens[2].split("=")[1], 10) : 1;
        return {format: "variableStep", chrom: cc, span: span}

    }

    function parseTrackLine(line) {
        var properties = {},
            tokens = line.split(/(?:")([^"]+)(?:")|([^\s"]+)(?=\s+|$)/g),
            tmp = [],
            i, tk, curr;

        // Clean up tokens array
        for (i = 1; i < tokens.length; i++) {
            if (!tokens[i] || tokens[i].trim().length === 0) continue;

            tk = tokens[i].trim();

            if (tk.endsWith("=") > 0) {
                curr = tk;
            }
            else if (curr) {
                tmp.push(curr + tk);
                curr = undefined;
            }
            else {
                tmp.push(tk);
            }

        }


        tmp.forEach(function (str) {
            if (!str) return;
            var kv = str.split('=', 2);
            if (kv.length == 2) {
                properties[kv[0]] = kv[1];
            }

        });

        return properties;
    }

    /**
     * Decode the "standard" UCSC bed format
     * @param tokens
     * @param ignore
     * @returns decoded feature, or null if this is not a valid record
     */
    function decodeBed(tokens, ignore) {

        var chr, start, end, id, name, tmp, idName, exonCount, exonSizes, exonStarts, exons, exon, feature,
            eStart, eEnd;

        if (tokens.length < 3) return null;

        chr = tokens[0];
        start = parseInt(tokens[1]);
        end = tokens.length > 2 ? parseInt(tokens[2]) : start + 1;

        feature = {chr: chr, start: start, end: end, score: 1000};

        if (tokens.length > 3) {
            // Note: these are very special rules for the gencode gene files.
            tmp = tokens[3].replace(/"/g, '');
            idName = tmp.split(';');
            for (var i = 0; i < idName.length; i++) {
                var kv = idName[i].split('=');
                if (kv[0] == "gene_id") {
                    id = kv[1];
                }
                if (kv[0] == "gene_name") {
                    name = kv[1];
                }
            }
            feature.id = id ? id : tmp;
            feature.name = name ? name : tmp;
        }

        if (tokens.length > 4) {
            feature.score = parseFloat(tokens[4]);
        }
        if (tokens.length > 5) {
            feature.strand = tokens[5];
        }
        if (tokens.length > 6) {
            feature.cdStart = parseInt(tokens[6]);
        }
        if (tokens.length > 7) {
            feature.cdEnd = parseInt(tokens[7]);
        }
        if (tokens.length > 8) {
            if (tokens[8] !== "." && tokens[8] !== "0")
                feature.color = igv.createColorString(tokens[8]);
        }
        if (tokens.length > 11) {
            exonCount = parseInt(tokens[9]);
            exonSizes = tokens[10].split(',');
            exonStarts = tokens[11].split(',');
            exons = [];

            for (var i = 0; i < exonCount; i++) {
                eStart = start + parseInt(exonStarts[i]);
                eEnd = eStart + parseInt(exonSizes[i]);
                var exon = {start: eStart, end: eEnd};

                if (feature.cdStart > eEnd || feature.cdEnd < feature.cdStart) exon.utr = true;   // Entire exon is UTR
                if (feature.cdStart >= eStart && feature.cdStart <= eEnd) exon.cdStart = feature.cdStart;
                if (feature.cdEnd >= eStart && feature.cdEnd <= eEnd) exon.cdEnd = feature.cdEnd;

                exons.push(exon);
            }

            feature.exons = exons;
        }

        feature.popupData = function () {
            var data = [];
            if (feature.name) data.push({name: "Name", value: feature.name});
            if ("+" === feature.strand || "-" === feature.strand) data.push({name: "Strand", value: feature.strand});
            return data;
        };

        return feature;

    }

    /**
     * Decode a UCSC "refflat" record
     * @param tokens
     * @param ignore
     * @returns {*}
     */
    function decodeRefflat(tokens, ignore) {

        if (tokens.length < 10) return null;

        var feature = {
                chr: tokens[2],
                start: parseInt(tokens[4]),
                end: parseInt(tokens[5]),
                id: tokens[0],
                name: tokens[1],
                strand: tokens[3],
                cdStart: parseInt(tokens[6]),
                cdEnd: parseInt(tokens[7])
            },
            exonCount = parseInt(tokens[8]),
            exonStarts = tokens[9].split(','),
            exonEnds = tokens[10].split(','),
            exons = [];

        for (var i = 0; i < exonCount; i++) {
            exons.push({start: parseInt(exonStarts[i]), end: parseInt(exonEnds[i])});
        }

        feature.exons = exons;

        feature.popupData = function () {
            return [{name: "Name", value: feature.name}];
        };

        return feature;

    }

    function decodePeak(tokens, ignore) {

        var tokenCount, chr, start, end, strand, name, score, qValue, signal, pValue;

        tokenCount = tokens.length;
        if (tokenCount < 9) {
            return null;
        }

        chr = tokens[0];
        start = parseInt(tokens[1]);
        end = parseInt(tokens[2]);
        name = tokens[3];
        score = parseFloat(tokens[4]);
        strand = tokens[5].trim();
        signal = parseFloat(tokens[6]);
        pValue = parseFloat(tokens[7]);
        qValue = parseFloat(tokens[8]);

        if (score === 0) score = signal;

        return {
            chr: chr, start: start, end: end, name: name, score: score, strand: strand, signal: signal,
            pValue: pValue, qValue: qValue
        };
    }

    function decodeBedGraph(tokens, ignore) {

        var chr, start, end, value;

        if (tokens.length < 3) return null;

        chr = tokens[0];
        start = parseInt(tokens[1]);
        end = parseInt(tokens[2]);

        value = parseFloat(tokens[3]);

        return {chr: chr, start: start, end: end, value: value};
    }

    function decodeWig(tokens, wig) {

        var ss,
            ee,
            value;

        if (wig.format === "fixedStep") {

            ss = (wig.index * wig.step) + wig.start;
            ee = ss + wig.span;
            value = parseFloat(tokens[0]);
            ++(wig.index);
            return isNaN(value) ? null : {chr: wig.chrom, start: ss, end: ee, value: value};
        }
        else if (wig.format === "variableStep") {

            if (tokens.length < 2) return null;

            ss = parseInt(tokens[0], 10);
            ee = ss + wig.span;
            value = parseFloat(tokens[1]);
            return isNaN(value) ? null : {chr: wig.chrom, start: ss, end: ee, value: value};

        }
        else {
            return decodeBedGraph(tokens);
        }
    }

    function decodeAneu(tokens, ignore) {

        var chr, start, end, feature;


        if (tokens.length < 4) return null;

        // console.log("Decoding aneu.tokens="+JSON.stringify(tokens));
        chr = tokens[1];
        start = parseInt(tokens[2]);
        end = tokens.length > 3 ? parseInt(tokens[3]) : start + 1;

        feature = {chr: chr, start: start, end: end};

        if (tokens.length > 4) {
            feature.score = parseFloat(tokens[4]);
            feature.value = feature.score;
        }


        feature.popupData = function () {
            return [{name: "Name", value: feature.name}];
        };

        return feature;

    }

    function decodeFusionJuncSpan(tokens, ignore) {

        /*
         Format:

         0       #scaffold
         1       fusion_break_name
         2       break_left
         3       break_right
         4       num_junction_reads
         5       num_spanning_frags
         6       spanning_frag_coords

         0       B3GNT1--NPSR1
         1       B3GNT1--NPSR1|2203-10182
         2       2203
         3       10182
         4       189
         5       1138
         6       1860-13757,1798-13819,1391-18127,1443-17174,...

         */


        //console.log("decoding fusion junc spans");

        var chr = tokens[0];
        var fusion_name = tokens[1];
        var junction_left = parseInt(tokens[2]);
        var junction_right = parseInt(tokens[3]);
        var num_junction_reads = parseInt(tokens[4]);
        var num_spanning_frags = parseInt(tokens[5]);

        var spanning_frag_coords_text = tokens[6];

        var feature = {
            chr: chr,
            name: fusion_name,
            junction_left: junction_left,
            junction_right: junction_right,
            num_junction_reads: num_junction_reads,
            num_spanning_frags: num_spanning_frags,
            spanning_frag_coords: [],

            start: -1,
            end: -1
        }; // set start and end later based on min/max of span coords

        var min_coord = junction_left;
        var max_coord = junction_right;

        if (num_spanning_frags > 0) {

            var coord_pairs = spanning_frag_coords_text.split(',');

            for (var i = 0; i < coord_pairs.length; i++) {
                var split_coords = coord_pairs[i].split('-');

                var span_left = split_coords[0];
                var span_right = split_coords[1];

                if (span_left < min_coord) {
                    min_coord = span_left;
                }
                if (span_right > max_coord) {
                    max_coord = span_right;
                }
                feature.spanning_frag_coords.push({left: span_left, right: span_right});

            }
        }

        feature.start = min_coord;
        feature.end = max_coord;


        feature.popupData = function () {
            return [{name: "Name", value: feature.name}];
        };

        return feature;

    }

    function decodeGtexGWAS(tokens, ignore) {


        var tokenCount, chr, start, end, strand, name, score, qValue, signal, pValue;

        tokenCount = tokens.length;
        if (tokenCount < 8) {
            return null;
        }

        chr = tokens[0];
        start = parseInt(tokens[1]) - 1;
        end = parseInt(tokens[3].split(':')[1]);
        //name = tokens[3];
        //score = parseFloat(tokens[4]);
        //strand = tokens[5].trim();
        //signal = parseFloat(tokens[6]);
        pValue = parseFloat(tokens[5]);
        //qValue = parseFloat(tokens[8]);

        //return {chr: chr, start: start, end: end, name: name, score: score, strand: strand, signal: signal,
        //    pValue: pValue, qValue: qValue};
        return {chr: chr, start: start, end: end, pvalue: pValue};
    }

    /**
     * Decode a single gff record (1 line in file).  Aggregations such as gene models are constructed at a higher level.
     *      ctg123 . mRNA            1050  9000  .  +  .  ID=mRNA00001;Parent=gene00001
     * @param tokens
     * @param ignore
     * @returns {*}
     */
    function decodeGFF(tokens, ignore) {

        var tokenCount, chr, start, end, strand, type, score, phase, attributeString, id, parent, color, name,
            transcript_id, i,
            format = this.format;

        tokenCount = tokens.length;
        if (tokenCount < 9) {
            return null;      // Not a valid gff record
        }

        chr = tokens[0];
        type = tokens[2];
        start = parseInt(tokens[3]) - 1;
        end = parseInt(tokens[4]);
        score = "." === tokens[5] ? 0 : parseFloat(tokens[5]);
        strand = tokens[6];
        phase = "." === tokens[7] ? 0 : parseInt(tokens[7]);
        attributeString = tokens[8];

        // Find ID and Parent, or transcript_id
        var delim = ('gff3' === format) ? '=' : /\s+/;
        var attributes = {};
        attributeString.split(';').forEach(function (kv) {
            var t = kv.trim().split(delim, 2), key, value;
            if (t.length == 2) {
                key = t[0].trim();
                value = t[1].trim();
                //Strip off quotes, if any
                if (value.startsWith('"') && value.endsWith('"')) {
                    value = value.substr(1, value.length - 2);
                }
                if ("ID" === t[0]) id = t[1];
                else if ("Parent" === t[0]) parent = t[1];
                else if ("color" === t[0].toLowerCase()) color = igv.createColorString(t[1]);
                else if ("transcript_id" === t[0]) id = t[1];     // gtf format
                attributes[key] = value;
            }
        });

        // Find name (label) property
        if (this.nameField) {
            name = attributes[this.nameField];
        }
        else {
            for (i = 0; i < gffNameFields.length; i++) {
                if (attributes.hasOwnProperty(gffNameFields[i])) {
                    this.nameField = gffNameFields[i];
                    name = attributes[this.nameField];


                    break;
                }
            }
        }


        return {
            id: id,
            parent: parent,
            name: name,
            type: type,
            chr: chr,
            start: start,
            end: end,
            score: score,
            strand: strand,
            color: color,
            attributeString: attributeString,
            popupData: function () {
                var kvs = this.attributeString.split(';'),
                    pd = [],
                    key, value;
                kvs.forEach(function (kv) {
                    var t = kv.trim().split(delim, 2);
                    if (t.length === 2 && t[1] !== undefined) {
                        key = t[0].trim();
                        value = t[1].trim();
                        //Strip off quotes, if any
                        if (value.startsWith('"') && value.endsWith('"')) {
                            value = value.substr(1, value.length - 2);
                        }
                        pd.push({name: key, value: value});
                    }
                });
                return pd;
            }

        };
    }

    /**
     * Decode the "standard" UCSC bed format
     * @param tokens
     * @param ignore
     * @returns decoded feature, or null if this is not a valid record
     */
    function decodeCustom(tokens, ignore) {

        var feature,
            chr, start, end,
            format = this.format,         // "this" refers to FeatureParser instance
            coords = format.coords || 0;

        if (tokens.length < 3) return null;

        chr = tokens[format.chr];
        start = parseInt(tokens[format.start]) - coords;
        end = format.end !== undefined ? parseInt(tokens[format.end]) : start + 1;

        feature = {chr: chr, start: start, end: end};

        if (format.fields) {
            format.fields.forEach(function (field, index) {
                if (index != format.chr && index != format.start && index != format.end) {
                    feature[field] = tokens[index];
                }
            });
        }

        return feature;

    }


    return igv;
})
(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    const MAX_GZIP_BLOCK_SIZE = (1 << 16);

    /**
     * feature source for "bed like" files (tab delimited files with 1 feature per line: bed, gff, vcf, etc)
     *
     * @param config
     * @constructor
     */
    igv.FeatureSource = function (config) {

        this.config = config || {};

        this.sourceType = (config.sourceType === undefined ? "file" : config.sourceType);

        if (config.sourceType === "ga4gh") {
            this.reader = new igv.Ga4ghVariantReader(config);
        } else if (config.sourceType === "immvar") {
            this.reader = new igv.ImmVarReader(config);
        } else if (config.type === "eqtl") {
            if (config.sourceType === "gtex-ws") {
                this.reader = new igv.GtexReader(config);
            }
            else {
                this.reader = new igv.GtexFileReader(config);
            }
        } else if (config.sourceType === "bigquery") {
            this.reader = new igv.BigQueryFeatureReader(config);
        }
        else {
            // Default for all sorts of ascii tab-delimited file formts
            this.reader = new igv.FeatureFileReader(config);
        }
        this.visibilityWindow = config.visibilityWindow;

    };

    igv.FeatureSource.prototype.getFileHeader = function () {

        var self = this,
            maxRows = this.config.maxRows || 500;

        return new Promise(function (fulfill, reject) {

            if (self.header) {
                fulfill(self.header);
            } else {
                if (typeof self.reader.readHeader === "function") {

                    self.reader.readHeader().then(function (header) {
                        // Non-indexed readers will return features as a side effect.  This is an important,
                        // if unfortunate, performance hack
                        if(header) {
                            var features = header.features;
                            if (features) {

                                if ("gtf" === self.config.format || "gff3" === self.config.format || "gff" === self.config.format) {
                                    features = (new igv.GFFHelper(self.config.format)).combineFeatures(features);
                                }

                                // Assign overlapping features to rows

                                packFeatures(features, maxRows);
                                self.featureCache = new igv.FeatureCache(features);

                                // If track is marked "searchable"< cache features by name -- use this with caution, memory intensive
                                if (self.config.searchable) {
                                    addFeaturesToDB(features);
                                }
                            }
                        }

                        if (header && header.format) {
                            self.config.format = header.format;
                        }

                        fulfill(header);
                    }).catch(reject);
                }
                else {
                    fulfill(null);
                }
            }
        });
    }

    function addFeaturesToDB(featureList) {
        featureList.forEach(function (feature) {
            if (feature.name) {
                igv.browser.featureDB[feature.name.toUpperCase()] = feature;
            }
        })
    }


    /**
     * Required function fo all data source objects.  Fetches features for the
     * range requested and passes them on to the success function.  Usually this is
     * a function that renders the features on the canvas
     *
     * @param chr
     * @param bpStart
     * @param bpEnd
     */

    igv.FeatureSource.prototype.getFeatures = function (chr, bpStart, bpEnd) {

        var self = this;
        return new Promise(function (fulfill, reject) {

            var genomicInterval = new igv.GenomicInterval(chr, bpStart, bpEnd),
                featureCache = self.featureCache,
                maxRows = self.config.maxRows || 500;

            if (featureCache && (featureCache.range === undefined || featureCache.range.containsRange(genomicInterval))) {
                fulfill(self.featureCache.queryFeatures(chr, bpStart, bpEnd));

            }
            else {
                // TODO -- reuse cached features that overelap new region

                if (self.sourceType === 'file' && (self.visibilityWindow === undefined || self.visibilityWindow <= 0)) {
                    // Expand genomic interval to grab entire chromosome
                    genomicInterval.start = 0;
                    var chromosome = igv.browser.genome.getChromosome(chr);
                    genomicInterval.end = (chromosome === undefined ?  Number.MAX_VALUE : chromosome.bpLength);
                }

                self.reader.readFeatures(chr, genomicInterval.start, genomicInterval.end).then(
                    function (featureList) {

                        if (featureList && typeof featureList.forEach === 'function') {  // Have result AND its an array type

                            var isIndexed =
                                self.reader.indexed ||
                                self.config.sourceType === "ga4gh" ||
                                self.config.sourceType === "immvar" ||
                                self.config.sourceType === "gtex" ||
                                self.config.sourceType === "bigquery";

                            // TODO -- COMBINE GFF FEATURES HERE
                            // if(self.isGFF) featureList = combineFeatures(featureList);
                            if ("gtf" === self.config.format || "gff3" === self.config.format || "gff" === self.config.format) {
                                featureList = (new igv.GFFHelper(self.config.format)).combineFeatures(featureList);
                            }

                            self.featureCache = isIndexed ?
                                new igv.FeatureCache(featureList, genomicInterval) :
                                new igv.FeatureCache(featureList);   // Note - replacing previous cache with new one


                            // Assign overlapping features to rows
                            packFeatures(featureList, maxRows);

                            // If track is marked "searchable"< cache features by name -- use this with caution, memory intensive
                            if (self.config.searchable) {
                                addFeaturesToDB(featureList);
                            }

                            // Finally pass features for query interval to continuation
                            fulfill(self.featureCache.queryFeatures(chr, bpStart, bpEnd));
                        }
                        else {
                            fulfill(null);
                        }

                    }).catch(reject);
            }
        });
    }


    function packFeatures(features, maxRows) {

        if (features == null || features.length === 0) {
            return;
        }

        // Segregate by chromosome

        var chrFeatureMap = {},
            chrs = [];
        features.forEach(function (feature) {

            var chr = feature.chr,
                flist = chrFeatureMap[chr];

            if (!flist) {
                flist = [];
                chrFeatureMap[chr] = flist;
                chrs.push(chr);
            }

            flist.push(feature);
        });

        // Loop through chrosomosomes and pack features;

        chrs.forEach(function (chr) {

            pack(chrFeatureMap[chr], maxRows);
        });


        // Assigns a row # to each feature.  If the feature does not fit in any row and #rows == maxRows no
        // row number is assigned.
        function pack(featureList, maxRows) {

            var rows = [];

            featureList.sort(function (a, b) {
                return a.start - b.start;
            })


            rows.push(-1000);
            featureList.forEach(function (feature) {

                var i,
                    r,
                    len = Math.min(rows.length, maxRows),
                    start = feature.start;

                for (r = 0; r < len; r++) {
                    if (start >= rows[r]) {
                        feature.row = r;
                        rows[r] = feature.end;
                        return;
                    }
                }
                feature.row = r;
                rows[r] = feature.end;


            });
        }
    }

    return igv;
})
(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    igv.FeatureTrack = function (config) {

        igv.configTrack(this, config);

        this.displayMode = config.displayMode || "COLLAPSED";    // COLLAPSED | EXPANDED | SQUISHED
        this.labelDisplayMode = config.labelDisplayMode;

        this.variantHeight = config.variantHeight || this.height;
        this.squishedCallHeight = config.squishedCallHeight || 30;
        this.expandedCallHeight = config.expandedCallHeight || 15;

        this.featureHeight = config.featureHeight || 14;

        // Set maxRows -- protects against pathological feature packing cases (# of rows of overlapping feaures)
        if (config.maxRows === undefined) {
            config.maxRows = 500;
        }
        this.maxRows = config.maxRows;


        if (config.url && (config.url.toLowerCase().endsWith(".bigbed") || config.url.toLowerCase().endsWith(".bb"))) {
            this.featureSource = new igv.BWSource(config);
        }
        else {
            this.featureSource = new igv.FeatureSource(config);
        }

        // Set the render function.  This can optionally be passed in the config
        if (config.render) {
            this.render = config.render;
        } else if ("variant" === config.type) {
            this.render = renderVariant;
            this.homvarColor = "rgb(17,248,254)";
            this.hetvarColor = "rgb(34,12,253)";
        }
        else if ("FusionJuncSpan" === config.type) {
            this.render = renderFusionJuncSpan;
            this.height = config.height || 50;
            this.autoHeight = false;
        }
        else {
            this.render = renderFeature;
            this.arrowSpacing = 30;

            // adjust label positions to make sure they're always visible
            monitorTrackDrag(this);
        }
    };

    igv.FeatureTrack.prototype.getFileHeader = function () {
        var self = this;
        return new Promise(function (fulfill, reject) {
            if (typeof self.featureSource.getFileHeader === "function") {
                self.featureSource.getFileHeader().then(function (header) {

                    if (header) {
                        // Header (from track line).  Set properties,unless set in the config (config takes precedence)
                        if (header.name && !self.config.name) {
                            self.name = header.name;
                        }
                        if (header.color && !self.config.color) {
                            self.color = "rgb(" + header.color + ")";
                        }
                    }
                    fulfill(header);

                }).catch(reject);
            }
            else {
                fulfill(null);
            }
        });
    };

    igv.FeatureTrack.prototype.getFeatures = function (chr, bpStart, bpEnd) {

        var self = this;

        return new Promise(function (fulfill, reject) {

            self.featureSource.getFeatures(chr, bpStart, bpEnd).then(fulfill).catch(reject);

        });
    };


    /**
     * The required height in pixels required for the track content.   This is not the visible track height, which
     * can be smaller (with a scrollbar) or larger.
     *
     * @param features
     * @returns {*}
     */
    igv.FeatureTrack.prototype.computePixelHeight = function (features) {

        if (this.displayMode === "COLLAPSED") {
            return this.variantHeight;
        }
        else {
            var maxRow = 0;
            if (features && (typeof features.forEach === "function")) {
                features.forEach(function (feature) {

                    if (feature.row && feature.row > maxRow) maxRow = feature.row;

                });
            }
            return Math.max(this.variantHeight, (maxRow + 1) * (this.displayMode === "SQUISHED" ? this.expandedCallHeight : this.squishedCallHeight));

        }

    };

    igv.FeatureTrack.prototype.draw = function (options) {

        var track = this,
            featureList = options.features,
            ctx = options.context,
            bpPerPixel = options.bpPerPixel,
            bpStart = options.bpStart,
            pixelWidth = options.pixelWidth,
            pixelHeight = options.pixelHeight,
            bpEnd = bpStart + pixelWidth * bpPerPixel + 1;

        igv.graphics.fillRect(ctx, 0, 0, pixelWidth, pixelHeight, {'fillStyle': "rgb(255, 255, 255)"});


        if (featureList) {

            for (var gene, i = 0, len = featureList.length; i < len; i++) {
                gene = featureList[i];
                if (gene.end < bpStart) continue;
                if (gene.start > bpEnd) break;
                track.render.call(this, gene, bpStart, bpPerPixel, pixelHeight, ctx);
            }
        }
        else {
            console.log("No feature list");
        }

    };

    /**
     * Return "popup data" for feature @ genomic location.  Data is an array of key-value pairs
     */
    igv.FeatureTrack.prototype.popupData = function (genomicLocation, xOffset, yOffset) {

        // We use the featureCache property rather than method to avoid async load.  If the
        // feature is not already loaded this won't work,  but the user wouldn't be mousing over it either.
        if (this.featureSource.featureCache) {

            var chr = igv.browser.referenceFrame.chr,  // TODO -- this should be passed in
                tolerance = 2 * igv.browser.referenceFrame.bpPerPixel,  // We need some tolerance around genomicLocation, start with +/- 2 pixels
                featureList = this.featureSource.featureCache.queryFeatures(chr, genomicLocation - tolerance, genomicLocation + tolerance),
                row;

            if (this.displayMode != "COLLAPSED") {
                row = (Math.floor)(this.displayMode === "SQUISHED" ? yOffset / this.expandedCallHeight : yOffset / this.squishedCallHeight);
            }

            if (featureList && featureList.length > 0) {


                var popupData = [];
                featureList.forEach(function (feature) {
                    if (feature.end >= genomicLocation - tolerance &&
                        feature.start <= genomicLocation + tolerance) {

                        // If row number is specified use it
                        if (row === undefined || feature.row === undefined || row === feature.row) {
                            var featureData
                            if (feature.popupData) {
                                featureData = feature.popupData(genomicLocation);
                            }
                            else {
                                featureData = extractPopupData(feature);
                            }
                            if (featureData) {
                                if (popupData.length > 0) {
                                    popupData.push("<HR>");
                                }
                                Array.prototype.push.apply(popupData, featureData);
                            }

                        }
                    }
                });

                return popupData;
            }

        }

        return null;
    };

    /**
     * Default popup text function -- just extracts string and number properties in random order.
     * @param feature
     * @returns {Array}
     */
    function extractPopupData(feature) {
        var data = [];
        for (var property in feature) {
            if (feature.hasOwnProperty(property) &&
                "chr" !== property && "start" !== property && "end" !== property && "row" !== property &&
                igv.isStringOrNumber(feature[property])) {
                data.push({name: property, value: feature[property]});
            }
        }
        return data;
    }

    igv.FeatureTrack.prototype.popupMenuItems = function (popover) {

        var myself = this,
            menuItems = [],
            lut = {"COLLAPSED": "Collapse", "SQUISHED": "Squish", "EXPANDED": "Expand"},
            checkMark = '<i class="fa fa-check fa-check-shim"></i>',
            checkMarkNone = '<i class="fa fa-check fa-check-shim fa-check-hidden"></i>',
            trackMenuItem = '<div class=\"igv-track-menu-item\">',
            trackMenuItemFirst = '<div class=\"igv-track-menu-item igv-track-menu-border-top\">';

        menuItems.push(igv.colorPickerMenuItem(popover, this.trackView));

        ["COLLAPSED", "SQUISHED", "EXPANDED"].forEach(function (displayMode, index) {

            var chosen,
                str;

            chosen = (0 === index) ? trackMenuItemFirst : trackMenuItem;
            str = (displayMode === myself.displayMode) ? chosen + checkMark + lut[displayMode] + '</div>' : chosen + checkMarkNone + lut[displayMode] + '</div>';

            menuItems.push({
                object: $(str),
                click: function () {
                    popover.hide();
                    myself.displayMode = displayMode;
                    myself.trackView.update();
                }
            });

        });

        return menuItems;

    };

    /**
     * @param feature
     * @param bpStart  genomic location of the left edge of the current canvas
     * @param xScale  scale in base-pairs per pixel
     * @returns {{px: number, px1: number, pw: number, h: number, py: number}}
     */
    function calculateFeatureCoordinates(feature, bpStart, xScale) {
        var px = Math.round((feature.start - bpStart) / xScale),
            px1 = Math.round((feature.end - bpStart) / xScale),
            pw = px1 - px;

        if (pw < 3) {
            pw = 3;
            px -= 1;
        }

        return {
            px: px,
            px1: px1,
            pw: pw
        };
    }

    /**
     *
     * @param feature
     * @param bpStart  genomic location of the left edge of the current canvas
     * @param xScale  scale in base-pairs per pixel
     * @param pixelHeight  pixel height of the current canvas
     * @param ctx  the canvas 2d context
     */
    function renderFeature(feature, bpStart, xScale, pixelHeight, ctx) {

        var x, e, exonCount, cy, direction, exon, ePx, ePx1, ePxU, ePw, py2, h2, py,
            windowX, windowX1,
            coord = calculateFeatureCoordinates(feature, bpStart, xScale),
            h = this.featureHeight,
            step = this.arrowSpacing,
            color = this.color;


        if (this.config.colorBy) {
            var colorByValue = feature[this.config.colorBy.field];
            if (colorByValue) {
                color = this.config.colorBy.pallete[colorByValue];
            }
        }
        else if (feature.color) {
            color = feature.color;
        }


        ctx.fillStyle = color;
        ctx.strokeStyle = color;

        if (this.displayMode === "SQUISHED" && feature.row != undefined) {
            h = this.featureHeight / 2;
            py = this.expandedCallHeight * feature.row + 2;
        } else if (this.displayMode === "EXPANDED" && feature.row != undefined) {
            py = this.squishedCallHeight * feature.row + 5;
        } else {  // collapsed
            py = 5;
        }

        cy = py + h / 2;
        h2 = h / 2;
        py2 = cy - h2 / 2;

        exonCount = feature.exons ? feature.exons.length : 0;

        if (exonCount == 0) {
            // single-exon transcript
            ctx.fillRect(coord.px, py, coord.pw, h);

        }
        else {
            // multi-exon transcript
            igv.graphics.strokeLine(ctx, coord.px + 1, cy, coord.px1 - 1, cy); // center line for introns

            direction = feature.strand == '+' ? 1 : -1;
            for (x = coord.px + step / 2; x < coord.px1; x += step) {
                // draw arrowheads along central line indicating transcribed orientation
                igv.graphics.strokeLine(ctx, x - direction * 2, cy - 2, x, cy);
                igv.graphics.strokeLine(ctx, x - direction * 2, cy + 2, x, cy);
            }
            for (e = 0; e < exonCount; e++) {
                // draw the exons
                exon = feature.exons[e];
                ePx = Math.round((exon.start - bpStart) / xScale);
                ePx1 = Math.round((exon.end - bpStart) / xScale);
                ePw = Math.max(1, ePx1 - ePx);

                if (exon.utr) {
                    ctx.fillRect(ePx, py2, ePw, h2); // Entire exon is UTR
                }
                else {
                    if (exon.cdStart) {
                        ePxU = Math.round((exon.cdStart - bpStart) / xScale);
                        ctx.fillRect(ePx, py2, ePxU - ePx, h2); // start is UTR
                        ePw -= (ePxU - ePx);
                        ePx = ePxU;

                    }
                    if (exon.cdEnd) {
                        ePxU = Math.round((exon.cdEnd - bpStart) / xScale);
                        ctx.fillRect(ePxU, py2, ePx1 - ePxU, h2); // start is UTR
                        ePw -= (ePx1 - ePxU);
                        ePx1 = ePxU;
                    }

                    ctx.fillRect(ePx, py, ePw, h);

                    // Arrows
                    if (ePw > step + 5) {
                        ctx.fillStyle = "white";
                        ctx.strokeStyle = "white";
                        for (x = ePx + step / 2; x < ePx1; x += step) {
                            // draw arrowheads along central line indicating transcribed orientation
                            igv.graphics.strokeLine(ctx, x - direction * 2, cy - 2, x, cy);
                            igv.graphics.strokeLine(ctx, x - direction * 2, cy + 2, x, cy);
                        }
                        ctx.fillStyle = color;
                        ctx.strokeStyle = color;

                    }
                }
            }
        }
        windowX = Math.round(igv.browser.referenceFrame.toPixels(igv.browser.referenceFrame.start - bpStart));
        windowX1 = windowX + igv.browser.trackViewportWidth();

        renderFeatureLabels.call(this, ctx, feature, coord.px, coord.px1, py, windowX, windowX1);
    }

    /**
     * @param ctx       the canvas 2d context
     * @param feature
     * @param featureX  feature start x-coordinate
     * @param featureX1 feature end x-coordinate
     * @param featureY  feature y-coordinate
     * @param windowX   visible window start x-coordinate
     * @param windowX1  visible window end x-coordinate
     */
    function renderFeatureLabels(ctx, feature, featureX, featureX1, featureY, windowX, windowX1) {
        var geneColor, geneFontStyle, transform,
            boxX, boxX1,    // label should be centered between these two x-coordinates
            labelX, labelY,
            textFitsInBox;

        // feature outside of viewable window
        if (featureX1 < windowX || featureX > windowX1) {
            boxX = featureX;
            boxX1 = featureX1;
        } else {
            // center label within visible portion of the feature
            boxX = Math.max(featureX, windowX);
            boxX1 = Math.min(featureX1, windowX1);
        }

        if (igv.browser.selection && "genes" === this.config.type && feature.name !== undefined) {
            // TODO -- for gtex, figure out a better way to do this
            geneColor = igv.browser.selection.colorForGene(feature.name);
        }

        textFitsInBox = (boxX1 - boxX) > ctx.measureText(feature.name).width;

        if ((textFitsInBox || geneColor) && this.displayMode != "SQUISHED" && feature.name !== undefined) {
            geneFontStyle = {
                font: '10px PT Sans',
                textAlign: 'center',
                fillStyle: geneColor || this.color,
                strokeStyle: geneColor || this.color
            };

            if (this.displayMode === "COLLAPSED" && this.labelDisplayMode === "SLANT") {
                transform = {rotate: {angle: 45}};
                delete geneFontStyle.textAlign;
            }

            labelX = boxX + ((boxX1 - boxX) / 2);
            labelY = getFeatureLabelY(featureY, transform);

            igv.graphics.fillText(ctx, feature.name, labelX, labelY, geneFontStyle, transform);
        }
    }

    function getFeatureLabelY(featureY, transform) {
        return transform ? featureY + 20 : featureY + 25;
    }

    /**
     * Monitors track drag events, updates label position to ensure that they're always visible.
     * @param track
     */
    function monitorTrackDrag(track) {
        var onDragEnd = function () {
            if (!track.trackView || !track.trackView.tile || track.displayMode === "SQUISHED") {
                return;
            }
            track.trackView.update();
        }

        var unSubscribe = function (removedTrack) {
            if (track === removedTrack) {
                igv.browser.un('trackdrag', onDragEnd);
                igv.browser.un('trackremoved', unSubscribe);
            }
        };

        igv.browser.on('trackdragend', onDragEnd);
        igv.browser.on('trackremoved', unSubscribe);
    }

    /**
     *
     * @param variant
     * @param bpStart  genomic location of the left edge of the current canvas
     * @param xScale  scale in base-pairs per pixel
     * @param pixelHeight  pixel height of the current canvas
     * @param ctx  the canvas 2d context
     */
    function renderVariant(variant, bpStart, xScale, pixelHeight, ctx) {

        var coord = calculateFeatureCoordinates(variant, bpStart, xScale),
            py = 20,
            h = 10,
            style;

        switch (variant.genotype) {
            case "HOMVAR":
                style = this.homvarColor;
                break;
            case "HETVAR":
                style = this.hetvarColor;
                break;
            default:
                style = this.color;
        }

        ctx.fillStyle = style;
        ctx.fillRect(coord.px, py, coord.pw, h);
    }


    /**
     *
     * @param feature
     * @param bpStart  genomic location of the left edge of the current canvas
     * @param xScale  scale in base-pairs per pixel
     * @param pixelHeight  pixel height of the current canvas
     * @param ctx  the canvas 2d context
     */
    function renderFusionJuncSpan(feature, bpStart, xScale, pixelHeight, ctx) {

        var coord = calculateFeatureCoordinates(feature, bpStart, xScale),
            py = 5, h = 10; // defaults borrowed from renderFeature above

        var rowHeight = (this.displayMode === "EXPANDED") ? this.squishedCallHeight : this.expandedCallHeight;

        // console.log("row height = " + rowHeight);

        if (this.displayMode === "SQUISHED" && feature.row != undefined) {
            py = rowHeight * feature.row;
        }
        else if (this.displayMode === "EXPANDED" && feature.row != undefined) {
            py = rowHeight * feature.row;
        }

        var cy = py + 0.5 * rowHeight;
        var top_y = cy - 0.5 * rowHeight;
        var bottom_y = cy + 0.5 * rowHeight;

        //igv.Canvas.strokeLine.call(ctx, coord.px, cy, coord.px1, cy); // center line for introns

        // draw the junction arc
        var junction_left_px = Math.round((feature.junction_left - bpStart) / xScale);
        var junction_right_px = Math.round((feature.junction_right - bpStart) / xScale);


        ctx.beginPath();
        ctx.moveTo(junction_left_px, cy);
        ctx.bezierCurveTo(junction_left_px, top_y, junction_right_px, top_y, junction_right_px, cy);

        ctx.lineWidth = 1 + Math.log(feature.num_junction_reads) / Math.log(2);
        ctx.strokeStyle = 'blue';
        ctx.stroke();

        // draw the spanning arcs
        var spanning_coords = feature.spanning_frag_coords;
        for (var i = 0; i < spanning_coords.length; i++) {
            var spanning_info = spanning_coords[i];

            var span_left_px = Math.round((spanning_info.left - bpStart) / xScale);
            var span_right_px = Math.round((spanning_info.right - bpStart) / xScale);


            ctx.beginPath();
            ctx.moveTo(span_left_px, cy);
            ctx.bezierCurveTo(span_left_px, bottom_y, span_right_px, bottom_y, span_right_px, cy);

            ctx.lineWidth = 1;
            ctx.strokeStyle = 'purple';
            ctx.stroke();
        }


    }


    return igv;

})
(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 University of California San Diego
 * Author: Jim Robinson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by jrobinson on 4/7/16.
 */

var igv = (function (igv) {

    var transcriptTypes;
    var cdsTypes;
    var utrTypes;
    var exonTypes;

    function setTypes() {
        transcriptTypes = new Set();
        cdsTypes = new Set();
        utrTypes = new Set();
        exonTypes = new Set();
        transcriptTypes.addAll(['transcript', 'primary_transcript', 'processed_transcript', 'mRNA', 'mrna']);
        cdsTypes.addAll(['CDS', 'cds', 'start_codon', 'stop_codon']);
        utrTypes.addAll(['5UTR', '3UTR', 'UTR', 'five_prime_UTR', 'three_prime_UTR', "3'-UTR", "5'-UTR"]);
        exonTypes.addAll(['exon', 'coding-exon']);


    }

    igv.GFFHelper = function (format) {
        this.format = format;
    }

    igv.GFFHelper.prototype.combineFeatures = function (features) {

        if (transcriptTypes === undefined) setTypes();

        if ("gff3" === this.format) {
            return combineFeaturesGFF.call(this, features);
        }
        else {
            return combineFeaturesGTF.call(this, features);
        }
    }

    function combineFeaturesGTF(features) {

        var transcripts = {},
            combinedFeatures = [];


        // 1. Build dictionary of transcripts  -- transcript records are not required in gtf / gff v2
        features.forEach(function (f) {
            var transcriptId, gffTranscript;
            if (transcriptTypes.has(f.type)) {
                transcriptId = f.id; // getAttribute(f.attributeString, "transcript_id", /\s+/);
                if (transcriptId) {
                    gffTranscript = new GFFTranscript(f);
                    transcripts[transcriptId] = gffTranscript;
                    combinedFeatures.push(gffTranscript);
                }
                else {
                    combinedFeatures.push(f);
                }
            }
        });


        // Add exons
        features.forEach(function (f) {
            var id, transcript;
            if (exonTypes.has(f.type)) {
                id = f.id;
                if (id) {
                    transcript = transcripts[id];
                    if (transcript === undefined) {
                        transcript = new GFFTranscript(f);
                        transcripts[id] = transcript;
                        combinedFeatures.push(transcript);
                    }
                    transcript.addExon(f);

                }
            }
        });


        // Apply CDS and UTR
        features.forEach(function (f) {
            var id, transcript;
            if (cdsTypes.has(f.type) || utrTypes.has(f.type)) {
                id = f.id;
                if (id) {
                    transcript = transcripts[id];
                    if (transcript === undefined) {
                        transcript = new GFFTranscript(f);
                        transcripts[id] = transcript;
                        combinedFeatures.push(transcript);
                    }

                    if (utrTypes.has(f.type)) {
                        transcript.addUTR(f);
                    }
                    else {
                        transcript.addCDS(f);
                    }
                }
            }
        });

        // Finish transcripts
        combinedFeatures.forEach(function (f) {
            if (f instanceof GFFTranscript) f.finish();
        })

        combinedFeatures.sort(function (a, b) {
            return a.start - b.start;
        })

        return combinedFeatures;

    }

    function combineFeaturesGFF(features) {


        var transcripts = {},
            combinedFeatures = [],
            parents,
            isoforms;

        function getParents(f) {
            if (f.parent && f.parent.trim() !== "") {
                return f.parent.trim().split(",");
            }
            else {
                return null;
            }
        }

        // 1. Build dictionary of transcripts  -- transcript records are not required in gtf / gff v2
        features.forEach(function (f) {
            var transcriptId, gffTranscript;
            if (transcriptTypes.has(f.type)) {
                transcriptId = f.id; // getAttribute(f.attributeString, "transcript_id", /\s+/);
                if (transcriptId) {
                    gffTranscript = new GFFTranscript(f);
                    transcripts[transcriptId] = gffTranscript;
                    combinedFeatures.push(gffTranscript);
                }
                else {
                    combinedFeatures.push(f);
                }
            }
        });


        // Add exons
        features.forEach(function (f) {
            var id, transcript;
            if (exonTypes.has(f.type)) {
                parents = getParents(f);
                if (parents) {
                    parents.forEach(function (id) {
                        transcript = transcripts[id];
                        if (transcript === undefined) {
                            transcript = new GFFTranscript(f);
                            transcripts[id] = transcript;
                            combinedFeatures.push(transcript);
                        }
                        transcript.addExon(f);
                    });
                } else {
                    combinedFeatures.push(f);   // parent-less exon
                }
            }
        });

        // Apply CDS and UTR
        features.forEach(function (f) {
            var id, transcript;
            if (cdsTypes.has(f.type) || utrTypes.has(f.type)) {
                parents = getParents(f);
                if (parents) {
                    parents.forEach(function (id) {
                        transcript = transcripts[id];
                        if (transcript === undefined) {
                            transcript = new GFFTranscript(f);
                            transcripts[id] = transcript;
                            combinedFeatures.push(transcript);
                        }

                        if (utrTypes.has(f.type)) {
                            transcript.addUTR(f);
                        }
                        else {
                            transcript.addCDS(f);
                        }

                    });
                }
                else {
                    combinedFeatures.push(f);
                }
            }
        });

        // Finish transcripts
        combinedFeatures.forEach(function (f) {
            if (f instanceof GFFTranscript) f.finish();
        })

        combinedFeatures.sort(function (a, b) {
            return a.start - b.start;
        })

        return combinedFeatures;

    }

    GFFTranscript = function (feature) {
        Object.assign(this, feature);
        this.exons = [];
        this.attributeString = feature.attributeString;
    }

    GFFTranscript.prototype.addExon = function (feature) {
        this.exons.push({
            start: feature.start,
            end: feature.end
        });
        // Expand feature --  for transcripts not explicitly represented in the file
        this.start = Math.min(this.start, feature.start);
        this.end = Math.max(this.end, feature.end);
    }

    GFFTranscript.prototype.addCDS = function (cds) {

        var i, exon,
            exons = this.exons;

        // Find exon containing CDS
        for (i = 0; i < exons.length; i++) {
            if (exons[i].start <= cds.start && exons[i].end >= cds.end) {
                exon = exons[i];
                break;
            }
        }

        if (exon) {
            exon.cdStart = exon.cdStart ? Math.min(cds.start, exon.cdStart) : cds.start;
            exon.cdEnd = exon.cdEnd ? Math.max(cds.end, exon.cdEnd) : cds.end;

        } else {
            exons.push({start: cds.start, end: cds.end, cdStart: cds.start, cdEnd: cds.end});  // Create new exon
        }

        // Expand feature --  for transcripts not explicitly represented in the file
        this.start = Math.min(this.start, cds.start);
        this.end = Math.max(this.end, cds.end);

        this.cdStart = this.cdStart ? Math.min(cds.start, this.cdStart) : cds.start;
        this.cdEnd = this.cdEnd ? Math.max(cds.end, this.cdEnd) : cds.end;

    }

    GFFTranscript.prototype.addUTR = function (utr) {

        var i, exon,
            exons = this.exons;

        // Find exon containing CDS
        for (i = 0; i < exons.length; i++) {
            if (exons[i].start <= utr.start && exons[i].end >= utr.end) {
                exon = exons[i];
                break;
            }
        }

        if (exon) {
            if (utr.start === exon.start && utr.end === exon.end) {
                exon.utr = true;
            }

        } else {
            exons.push({start: utr.start, end: utr.end, utr: true});  // Create new exon
        }

        // Expand feature --  for transcripts not explicitly represented in the file
        this.start = Math.min(this.start, utr.start);
        this.end = Math.max(this.end, utr.end);
    }

    GFFTranscript.prototype.finish = function () {

        var cdStart = this.cdStart;
        var cdEnd = this.cdEnd;

        this.exons.sort(function (a, b) {
            return a.start - b.start;
        })

        // Search for UTR exons that were not explicitly tagged
        if (cdStart) {
            this.exons.forEach(function (exon) {
                if (exon.end < cdStart || exon.start > cdEnd) exon.utr = true;
            });
        }

    }

    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 *  Define parser for seg files  (.bed, .gff, .vcf, etc).  A parser should implement 2 methods
 *
 *     parseHeader(data) - return an object representing a header.  Details are format specific
 *
 *     parseFeatures(data) - return a list of features
 *
 */


var igv = (function (igv) {

    var maxFeatureCount = Number.MAX_VALUE,    // For future use,  controls downsampling
        sampleColumn = 0,
        chrColumn = 1,
        startColumn = 2,
        endColumn = 3;


    igv.SegParser = function () {
   }

    igv.SegParser.prototype.parseHeader = function (data) {

        var lines = data.splitLines(),
            len = lines.length,
            line,
            i,
            tokens;

        for (i = 0; i < len; i++) {
            line = lines[i];
            if (line.startsWith("#")) {
                continue;
            }
            else {
                tokens = line.split("\t");
                this.header = {headings: tokens, lineCount: i + 1};
                return this.header;
                break;
            }
        }

        return this.header;
    }


    igv.SegParser.prototype.parseFeatures = function (data) {

        var lines = data ? data.splitLines() : [] ,
            len = lines.length,
            tokens, allFeatures = [], line, i, dataColumn;

        if (!this.header) {
            this.header = this.parseHeader(data);
        }
        dataColumn = this.header.headings.length - 1;


        for (i = this.header.lineCount; i < len; i++) {

            line = lines[i];

            tokens = lines[i].split("\t");

            if (tokens.length > dataColumn) {

                allFeatures.push({
                    sample: tokens[sampleColumn],
                    chr: tokens[chrColumn],
                    start: parseInt(tokens[startColumn]),
                    end: parseInt(tokens[endColumn]),
                    value: parseFloat(tokens[dataColumn])
                });
            }
        }

        return allFeatures;

    }


    return igv;
})
(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    var sortDirection = "DESC";

    igv.SegTrack = function (config) {

        igv.configTrack(this, config);

        this.displayMode = config.displayMode || "SQUISHED"; // EXPANDED | SQUISHED

        this.maxHeight = config.maxHeight || 500;
        this.sampleSquishHeight = config.sampleSquishHeight || 2;
        this.sampleExpandHeight = config.sampleExpandHeight || 12;

        this.posColorScale = config.posColorScale ||
            new igv.GradientColorScale(
                {
                    low: 0.1,
                    lowR: 255,
                    lowG: 255,
                    lowB: 255,
                    high: 1.5,
                    highR: 255,
                    highG: 0,
                    highB: 0
                }
            );
        this.negColorScale = config.negColorScale ||
            new igv.GradientColorScale(
                {
                    low: -1.5,
                    lowR: 0,
                    lowG: 0,
                    lowB: 255,
                    high: -0.1,
                    highR: 255,
                    highG: 255,
                    highB: 255
                }
            );

        this.sampleCount = 0;
        this.samples = {};
        this.sampleNames = [];

        //   this.featureSource = config.sourceType === "bigquery" ?
        //       new igv.BigQueryFeatureSource(this.config) :
        this.featureSource = new igv.FeatureSource(this.config);


    };

    igv.SegTrack.prototype.popupMenuItems = function (popover) {

        var myself = this;

        return [
            {
                name: ("SQUISHED" === this.displayMode) ? "Expand sample hgt" : "Squish sample hgt",
                click: function () {
                    popover.hide();
                    myself.toggleSampleHeight();
                }
            }
        ];

    };

    igv.SegTrack.prototype.toggleSampleHeight = function () {

        this.displayMode = ("SQUISHED" === this.displayMode) ? "EXPANDED" : "SQUISHED";

        this.trackView.update();
    };


    igv.SegTrack.prototype.getFeatures = function (chr, bpStart, bpEnd) {

        var self = this;
        return new Promise(function (fulfill, reject) {
            // If no samples are defined, optionally query feature source.  This step was added to support the TCGA BigQuery
            if (self.sampleCount === 0 && (typeof self.featureSource.reader.allSamples == "function")) {
                self.featureSource.reader.allSamples().then(function (samples) {
                    samples.forEach(function (sample) {
                        self.samples[sample] = self.sampleCount;
                        self.sampleNames.push(sample);
                        self.sampleCount++;
                    })
                    self.featureSource.getFeatures(chr, bpStart, bpEnd).then(fulfill).catch(reject);
                }).catch(reject);
            }
            else {
                self.featureSource.getFeatures(chr, bpStart, bpEnd).then(fulfill).catch(reject);
            }
        });
    }


    igv.SegTrack.prototype.draw = function (options) {

        var myself = this,
            featureList,
            ctx,
            bpPerPixel,
            bpStart,
            pixelWidth,
            pixelHeight,
            bpEnd,
            segment,
            len,
            sample,
            i,
            y,
            color,
            value,
            px,
            px1,
            pw,
            xScale,
            sampleHeight,
            border;

        sampleHeight = ("SQUISHED" === this.displayMode) ? this.sampleSquishHeight : this.sampleExpandHeight;
        border = ("SQUISHED" === this.displayMode) ? 0 : 1;

        ctx = options.context;
        pixelWidth = options.pixelWidth;
        pixelHeight = options.pixelHeight;
        igv.graphics.fillRect(ctx, 0, 0, pixelWidth, pixelHeight, {'fillStyle': "rgb(255, 255, 255)"});

        featureList = options.features;
        if (featureList) {

            bpPerPixel = options.bpPerPixel;
            bpStart = options.bpStart;
            bpEnd = bpStart + pixelWidth * bpPerPixel + 1;
            xScale = bpPerPixel;

            for (i = 0, len = featureList.length; i < len; i++) {
                sample = featureList[i].sample;
                if (!this.samples.hasOwnProperty(sample)) {
                    this.samples[sample] = myself.sampleCount;
                    this.sampleNames.push(sample);
                    this.sampleCount++;
                }
            }

            checkForLog(featureList);

            for (i = 0, len = featureList.length; i < len; i++) {

                segment = featureList[i];

                if (segment.end < bpStart) continue;
                if (segment.start > bpEnd) break;

                y = myself.samples[segment.sample] * sampleHeight + border;

                value = segment.value;
                if (!myself.isLog) {
                    value = Math.log2(value / 2);
                }

                if (value < -0.1) {
                    color = myself.negColorScale.getColor(value);
                }
                else if (value > 0.1) {
                    color = myself.posColorScale.getColor(value);
                }
                else {
                    color = "white";
                }

                px = Math.round((segment.start - bpStart) / xScale);
                px1 = Math.round((segment.end - bpStart) / xScale);
                pw = Math.max(1, px1 - px);

                igv.graphics.fillRect(ctx, px, y, pw, sampleHeight - 2 * border, {fillStyle: color});

            }
        }
        else {
            console.log("No feature list");
        }


        function checkForLog(featureList) {
            var i;
            if (myself.isLog === undefined) {
                myself.isLog = false;
                for (i = 0; i < featureList.length; i++) {
                    if (featureList[i].value < 0) {
                        myself.isLog = true;
                        return;
                    }
                }
            }
        }
    };

    /**
     * Optional method to compute pixel height to accomodate the list of features.  The implementation below
     * has side effects (modifiying the samples hash).  This is unfortunate, but harmless.
     *
     * @param features
     * @returns {number}
     */
    igv.SegTrack.prototype.computePixelHeight = function (features) {

        var sampleHeight = ("SQUISHED" === this.displayMode) ? this.sampleSquishHeight : this.sampleExpandHeight;

        for (i = 0, len = features.length; i < len; i++) {
            sample = features[i].sample;
            if (!this.samples.hasOwnProperty(sample)) {
                this.samples[sample] = this.sampleCount;
                this.sampleNames.push(sample);
                this.sampleCount++;
            }
        }

        return this.sampleCount * sampleHeight;
    };

    /**
     * Sort samples by the average value over the genomic range in the direction indicated (1 = ascending, -1 descending)
     */
    igv.SegTrack.prototype.sortSamples = function (chr, bpStart, bpEnd, direction) {

        var self = this,
            d2 = (direction === "ASC" ? 1 : -1);

        this.featureSource.getFeatures(chr, bpStart, bpEnd).then(function (featureList) {

            var segment,
                min,
                max,
                f,
                i,
                s,
                sampleNames,
                scores = {},
                bpLength = bpEnd - bpStart + 1;

            // Compute weighted average score for each sample
            for (i = 0; i < featureList.length; i++) {

                segment = featureList[i];

                if (segment.end < bpStart) continue;
                if (segment.start > bpEnd) break;

                min = Math.max(bpStart, segment.start);
                max = Math.min(bpEnd, segment.end);
                f = (max - min) / bpLength;

                s = scores[segment.sample];
                if (!s) s = 0;
                scores[segment.sample] = s + f * segment.value;

            }

            // Now sort sample names by score
            sampleNames = Object.keys(self.samples);
            sampleNames.sort(function (a, b) {

                var s1 = scores[a];
                var s2 = scores[b];
                if (!s1) s1 = Number.MAX_VALUE;
                if (!s2) s2 = Number.MAX_VALUE;

                if (s1 == s2) return 0;
                else if (s1 > s2) return d2;
                else return d2 * -1;

            });

            // Finally update sample hash
            for (i = 0; i < sampleNames.length; i++) {
                self.samples[sampleNames[i]] = i;
            }
            self.sampleNames = sampleNames;

            self.trackView.update();
            $(self.trackView.viewportDiv).scrollTop(0);


        }).catch(function(error) {
            console.log(error);
        });
    };

    /**
     * Handle an alt-click.   TODO perhaps generalize this for all tracks (optional).
     *
     * @param genomicLocation
     * @param event
     */
    igv.SegTrack.prototype.altClick = function (genomicLocation, event) {

        // Define a region 5 "pixels" wide in genomic coordinates
        var refFrame = igv.browser.referenceFrame,
            bpWidth = refFrame.toBP(2.5),
            bpStart = genomicLocation - bpWidth,
            bpEnd = genomicLocation + bpWidth,
            chr = refFrame.chr,
            myself = this;

        this.sortSamples(chr, bpStart, bpEnd, sortDirection);

        sortDirection = (sortDirection === "ASC" ? "DESC" : "ASC");
    };

    igv.SegTrack.prototype.popupData = function (genomicLocation, xOffset, yOffset) {

        var sampleHeight = ("SQUISHED" === this.displayMode) ? this.sampleSquishHeight : this.sampleExpandHeight,
            sampleName,
            row,
            items;

        row = Math.floor(yOffset / sampleHeight);

        if (row < this.sampleNames.length) {

            sampleName = this.sampleNames[row];

            items = [
                {name: "Sample", value: sampleName}
            ];

            // We use the featureCache property rather than method to avoid async load.  If the
            // feature is not already loaded this won't work,  but the user wouldn't be mousing over it either.
            if (this.featureSource.featureCache) {
                var chr = igv.browser.referenceFrame.chr;  // TODO -- this should be passed in
                var featureList = this.featureSource.featureCache.queryFeatures(chr, genomicLocation, genomicLocation);
                featureList.forEach(function (f) {
                    if (f.sample === sampleName) {
                        items.push({name: "Value", value: f.value});
                    }
                });
            }

            return items;
        }

        return null;
    };


    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    /**
     *
     * @param indexFile
     * @param config
     * @returns a Promise for the tribble-style (.idx) index.  The fulfill function takes the index as an argument
     */
    igv.loadTribbleIndex = function (indexFile, config) {

        var genome = igv.browser ? igv.browser.genome : null;

        //console.log("Loading " + indexFile);
        return new Promise(function (fulfill, reject) {

            igvxhr.loadArrayBuffer(indexFile,
                {
                    headers: config.headers,
                    withCredentials: config.withCredentials
                }).then(function (arrayBuffer) {

                    if (arrayBuffer) {

                        var index = {};

                        var parser = new igv.BinaryParser(new DataView(arrayBuffer));

                        readHeader(parser);  // <= nothing in the header is actually used

                        var nChrs = parser.getInt();
                        while (nChrs-- > 0) {
                            // todo -- support interval tree index, we're assuming its a linear index
                            var chrIdx = readLinear(parser);
                            index[chrIdx.chr] = chrIdx;
                        }

                        fulfill(new igv.TribbleIndex(index));
                    }
                    else {
                        fulfill(null);
                    }

                }).catch(function (error) {
                    console.log(error);
                    fulfill(null);
                });


            function readHeader(parser) {

                //var magicString = view.getString(4);
                var magicNumber = parser.getInt();     //   view._getInt32(offset += 32, true);
                var type = parser.getInt();
                var version = parser.getInt();

                var indexedFile = parser.getString();

                var indexedFileSize = parser.getLong();

                var indexedFileTS = parser.getLong();
                var indexedFileMD5 = parser.getString();
                flags = parser.getInt();
                if (version < 3 && (flags & SEQUENCE_DICTIONARY_FLAG) == SEQUENCE_DICTIONARY_FLAG) {
                    // readSequenceDictionary(dis);
                }

                if (version >= 3) {
                    var nProperties = parser.getInt();
                    while (nProperties-- > 0) {
                        var key = parser.getString();
                        var value = parser.getString();
                    }
                }
            }

            function readLinear(parser) {

                var chr = parser.getString(),
                    blockMax = 0;

                // Translate to canonical name
                if (genome) chr = genome.getChromosomeName(chr);

                var binWidth = parser.getInt();
                var nBins = parser.getInt();
                var longestFeature = parser.getInt();
                //largestBlockSize = parser.getInt();
                // largestBlockSize and totalBlockSize are old V3 index values.  largest block size should be 0 for
                // all newer V3 block.  This is a nasty hack that should be removed when we go to V4 (XML!) indices
                var OLD_V3_INDEX = parser.getInt() > 0;
                var nFeatures = parser.getInt();

                // note the code below accounts for > 60% of the total time to read an index
                var blocks = new Array();
                var pos = parser.getLong();
                var chrBegPos = pos;

                var blocks = new Array();
                for (var binNumber = 0; binNumber < nBins; binNumber++) {
                    var nextPos = parser.getLong();
                    var size = nextPos - pos;
                    blocks.push({min: pos, max: nextPos}); //        {position: pos, size: size});
                    pos = nextPos;

                    if (nextPos > blockMax) blockMax = nextPos;
                }

                return {chr: chr, blocks: blocks};

            }


        });
    }


    igv.TribbleIndex = function (chrIndexTable) {
        this.chrIndex = chrIndexTable;      // Dictionary of chr -> tribble index
    }

    /**
     * Fetch blocks for a particular genomic range.
     *
     * @param refId  the sequence dictionary index of the chromosome
     * @param min  genomic start position
     * @param max  genomic end position
     * @param return an array of {minv: {block: filePointer, offset: 0}, {maxv: {block: filePointer, offset: 0}}
     */
    igv.TribbleIndex.prototype.blocksForRange = function (queryChr, min, max) { //function (refId, min, max) {

        var chrIdx = this.chrIndex[queryChr];

        if (chrIdx) {
            var blocks = chrIdx.blocks,
                lastBlock = blocks[blocks.length - 1],
                mergedBlock = {minv: {block: blocks[0].min, offset: 0}, maxv: {block: lastBlock.max, offset: 0}};

            return [mergedBlock];
        }
        else {
            return null;
        }


    }


    return igv;
})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by turner on 2/11/14.
 */
var igv = (function (igv) {

    igv.WIGTrack = function (config) {

        this.config = config;
        this.url = config.url;

        if (config.color === undefined) config.color = "rgb(150,150,150)";   // Hack -- should set a default color per track type
        
        igv.configTrack(this, config);

        if ("bigwig" === config.format) {
            this.featureSource = new igv.BWSource(config);
        }
        else {
            this.featureSource = new igv.FeatureSource(config);
        }

        // Min and max values.  No defaults for these, if they aren't set track will autoscale.
        this.dataRange = {
            min: config.min,
            max: config.max
        };

        this.paintAxis = igv.paintAxis;

    };

    igv.WIGTrack.prototype.getFeatures = function (chr, bpStart, bpEnd) {

        var self = this;
        return new Promise(function (fulfill, reject) {
            self.featureSource.getFeatures(chr, bpStart, bpEnd).then(fulfill).catch(reject);
        });
    };

    igv.WIGTrack.prototype.popupMenuItems = function (popover) {

        var self = this,
            menuItems = [],
            html = [];

        menuItems.push(igv.colorPickerMenuItem(popover, this.trackView));
        menuItems.push(igv.dataRangeMenuItem(popover, this.trackView));

        //html.push('<div class="igv-track-menu-item igv-track-menu-border-top">');
        html.push('<div class="igv-track-menu-item">');
        html.push(true === self.autoScale ? '<i class="fa fa-check fa-check-shim">' : '<i class="fa fa-check fa-check-shim fa-check-hidden">');
        html.push('</i>');
        html.push('Autoscale');
        html.push('</div>');

        menuItems.push({
            object: $(html.join('')),
            click: function () {
                var $fa = $(this).find('i');

                popover.hide();

                self.autoScale = !self.autoScale;

                if (true === self.autoScale) {
                    $fa.removeClass('fa-check-hidden');
                } else {
                    $fa.addClass('fa-check-hidden');
                }

                // do stuff

                self.trackView.update();
            }
        });

        return menuItems;

    };

    igv.WIGTrack.prototype.draw = function (options) {

        var self = this,
            features = options.features,
            ctx = options.context,
            bpPerPixel = options.bpPerPixel,
            bpStart = options.bpStart,
            pixelWidth = options.pixelWidth,
            pixelHeight = options.pixelHeight,
            bpEnd = bpStart + pixelWidth * bpPerPixel + 1,
            featureValueMinimum,
            featureValueMaximum,
            featureValueRange,
            $dataRangeTrackLabel,
            str,
            min,
            max;


        if (features && features.length > 0) {
            if (self.autoScale || self.dataRange.max === undefined) {
                var s = autoscale(features);
                featureValueMinimum = s.min;
                featureValueMaximum = s.max;
            }
            else {
                featureValueMinimum = self.dataRange.min === undefined ? 0 : self.dataRange.min;
                featureValueMaximum = self.dataRange.max;
            }
            self.dataRange.min = featureValueMinimum;  // Record for disply, menu, etc
            self.dataRange.max = featureValueMaximum;

            featureValueRange = featureValueMaximum - featureValueMinimum;

            //$dataRangeTrackLabel = $(this.trackView.trackDiv).find('.igv-data-range-track-label');
            //
            //min = (Math.floor(track.dataRange.min) === track.dataRange.min) ? track.dataRange.min : track.dataRange.min.toFixed(2);
            //max = (Math.floor(track.dataRange.max) === track.dataRange.max) ? track.dataRange.max : track.dataRange.max.toFixed(2);
            //str = '[' + min + ' - ' + max + ']';
            //
            //$dataRangeTrackLabel.text(str);

            features.forEach(renderFeature);
        }


        function renderFeature(feature, index, featureList) {

            var yUnitless,
                heightUnitLess,
                x,
                y,
                width,
                height,
                rectEnd,
                rectBaseline;

            if (feature.end < bpStart) return;
            if (feature.start > bpEnd) return;

            x = Math.floor((feature.start - bpStart) / bpPerPixel);
            rectEnd = Math.ceil((feature.end - bpStart) / bpPerPixel);
            width = Math.max(1, rectEnd - x);

            //height = ((feature.value - featureValueMinimum) / featureValueRange) * pixelHeight;
            //rectBaseline = pixelHeight - height;
            //canvas.fillRect(rectOrigin, rectBaseline, rectWidth, rectHeight, {fillStyle: track.color});

            if (signsDiffer(featureValueMinimum, featureValueMaximum)) {

                if (feature.value < 0) {
                    yUnitless = featureValueMaximum / featureValueRange;
                    heightUnitLess = -feature.value / featureValueRange;
                } else {
                    yUnitless = ((featureValueMaximum - feature.value) / featureValueRange);
                    heightUnitLess = feature.value / featureValueRange;
                }

            }
            else if (featureValueMinimum < 0) {
                yUnitless = 0;
                heightUnitLess = -feature.value / featureValueRange;
            }
            else {
                yUnitless = 1.0 - feature.value / featureValueRange;
                heightUnitLess = feature.value / featureValueRange;
            }

            //canvas.fillRect(x, yUnitless * pixelHeight, width, heightUnitLess * pixelHeight, { fillStyle: igv.randomRGB(64, 255) });
            igv.graphics.fillRect(ctx, x, yUnitless * pixelHeight, width, heightUnitLess * pixelHeight, {fillStyle: self.color});

        }

    };

    function autoscale(features) {
        var min = 0,
            max = -Number.MAX_VALUE;

        features.forEach(function (f) {
            min = Math.min(min, f.value);
            max = Math.max(max, f.value);
        });

        return {min: min, max: max};

    }

    function signsDiffer(a, b) {
        return (a > 0 && b < 0 || a < 0 && b > 0);
    }


    return igv;

})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {


    var BAM_MAGIC = 21840194;
    var BAI_MAGIC = 21578050;
    var SECRET_DECODER = ['=', 'A', 'C', 'x', 'G', 'x', 'x', 'x', 'T', 'x', 'x', 'x', 'x', 'x', 'x', 'N'];
    var CIGAR_DECODER = ['M', 'I', 'D', 'N', 'S', 'H', 'P', '=', 'X', '?', '?', '?', '?', '?', '?', '?'];
    var READ_PAIRED_FLAG = 0x1;
    var PROPER_PAIR_FLAG = 0x2;
    var READ_UNMAPPED_FLAG = 0x4;
    var MATE_UNMAPPED_FLAG = 0x8;
    var READ_STRAND_FLAG = 0x10;
    var MATE_STRAND_FLAG = 0x20;
    var FIRST_OF_PAIR_FLAG = 0x40;
    var SECOND_OF_PAIR_FLAG = 0x80;
    var NOT_PRIMARY_ALIGNMENT_FLAG = 0x100;
    var READ_FAILS_VENDOR_QUALITY_CHECK_FLAG = 0x200;
    var DUPLICATE_READ_FLAG = 0x400;
    var SUPPLEMENTARY_ALIGNMENT_FLAG = 0x800;


    var CigarOperationTable = {
        ALIGNMENT_MATCH: "M",
        INSERT: "I",
        DELETE: "D",
        SKIP: "N",
        CLIP_SOFT: "S",
        CLIP_HARD: "H",
        PAD: "P",
        SEQUENCE_MATCH: "=",
        SEQUENCE_MISMATCH: "X"
    }


    igv.Ga4ghAlignment = function (json, genome) {

        this.readName = json.fragmentName;
        this.properPlacement = json.properPlacement;
        this.duplicateFragment = json.duplicateFragment;
        this.numberReads = json.numberReads;
        this.fragmentLength = json.fragmentLength;
        this.readNumber = json.readNumber;
        this.failedVendorQualityChecks = json.failedVendorQualityChecks;
        this.secondaryAlignment = json.secondaryAlignment;
        this.supplementaryAlignment = json.supplementaryAlignment;

        this.seq = json.alignedSequence;
        this.qual = json.alignedQuality;
        this.tagDict = json.info;

        //this.flags = encodeFlags(json);


        alignment = json.alignment;
        if (alignment) {
            this.mapped = true;

            this.chr = json.alignment.position.referenceName;
            if (genome) this.chr = genome.getChromosomeName(this.chr);

            this.start = parseInt(json.alignment.position.position);
            this.strand = !(json.alignment.position.reverseStrand);
            this.mq = json.alignment.mappingQuality;
            //this.cigar = encodeCigar(json.alignment.cigar);
            cigarDecoded = translateCigar(json.alignment.cigar);

            this.lengthOnRef = cigarDecoded.lengthOnRef;

            this.blocks = makeBlocks(this, cigarDecoded.array);
        }
        else {
            this.mapped = false;
        }

        if (json.nextMatePosition) {
            this.mate = {
                chr: json.nextMatePosition.referenceFrame,
                position: parseInt(json.nextMatePosition.position),
                strand: !json.nextMatePosition.reverseStrand
            };

            this.info = json.info;
        }

    }


    igv.Ga4ghAlignment.prototype.isMapped = function () {
        return this.mapped;
    }

    igv.Ga4ghAlignment.prototype.isPaired = function () {
        return this.numberReads && this.numberReads > 1;
    }

    igv.Ga4ghAlignment.prototype.isProperPair = function () {
        return this.properPlacement === undefined || this.properPlacement;       // Assume true
    }

    igv.Ga4ghAlignment.prototype.isFirstOfPair = function () {
        return this.readNumber && this.readNumber === 0;
    }

    igv.Ga4ghAlignment.prototype.isSecondOfPair = function () {
        return this.readNumber && this.readNumber === 1;
    }

    igv.Ga4ghAlignment.prototype.isSecondary = function () {
        return this.secondaryAlignment;
    }

    igv.Ga4ghAlignment.prototype.isSupplementary = function () {
        return this.supplementaryAlignment;
    }

    igv.Ga4ghAlignment.prototype.isFailsVendorQualityCheck = function () {
        return this.failedVendorQualityChecks;
    }

    igv.Ga4ghAlignment.prototype.isDuplicate = function () {
        return this.duplicateFragment;
    }

    igv.Ga4ghAlignment.prototype.isMateMapped = function () {
        return this.mate;
    }

    igv.Ga4ghAlignment.prototype.mateStrand = function () {
        return this.mate && this.mate.strand;
    }

    igv.Ga4ghAlignment.prototype.tags = function () {
        return this.info;
    }

    igv.Ga4ghAlignment.prototype.popupData = function (genomicLocation) {

        var isFirst;

        nameValues = [];

        nameValues.push({name: 'Read Name', value: this.readName});
        // Sample
        // Read group
        nameValues.push("<hr>");

        // Add 1 to genomic location to map from 0-based computer units to user-based units
        nameValues.push({name: 'Alignment Start', value: igv.numberFormatter(1 + this.start), borderTop: true});

        nameValues.push({name: 'Read Strand', value: (true === this.strand ? '(+)' : '(-)'), borderTop: true});
        nameValues.push({name: 'Cigar', value: this.cigar});
        nameValues.push({name: 'Mapped', value: yesNo(this.isMapped())});
        nameValues.push({name: 'Mapping Quality', value: this.mq});
        nameValues.push({name: 'Secondary', value: yesNo(this.isSecondary())});
        nameValues.push({name: 'Supplementary', value: yesNo(this.isSupplementary())});
        nameValues.push({name: 'Duplicate', value: yesNo(this.isDuplicate())});
        nameValues.push({name: 'Failed QC', value: yesNo(this.isFailsVendorQualityCheck())});


        if (this.isPaired()) {
            nameValues.push("<hr>");
            nameValues.push({name: 'First in Pair', value: !this.isSecondOfPair(), borderTop: true});
            nameValues.push({name: 'Mate is Mapped', value: yesNo(this.isMateMapped())});
            if (this.isMapped()) {
                nameValues.push({name: 'Mate Start', value: this.matePos});
                nameValues.push({name: 'Mate Strand', value: (this.mateStrand() ? '(-)' : '(+)')});
                nameValues.push({name: 'Insert Size', value: this.fragmentLength});
                // Mate Start
                // Mate Strand
                // Insert Size
            }
            // First in Pair
            // Pair Orientation

        }

        nameValues.push("<hr>");
        this.tags();
        isFirst = true;
        for (var key in this.tagDict) {

            if (this.tagDict.hasOwnProperty(key)) {

                if (isFirst) {
                    nameValues.push({name: key, value: this.tagDict[key], borderTop: true});
                    isFirst = false;
                } else {
                    nameValues.push({name: key, value: this.tagDict[key]});
                }

            }
        }

        return nameValues;


        function yesNo(bool) {
            return bool ? 'Yes' : 'No';
        }
    }


    function translateCigar(cigar) {

        var cigarUnit, opLen, opLtr,
            lengthOnRef = 0,
            cigarArray = [];

        for (i = 0; i < cigar.length; i++) {

            cigarUnit = cigar[i];

            opLtr = CigarOperationTable[cigarUnit.operation];
            opLen = parseInt(cigarUnit.operationLength);    // TODO -- this should be a long by the spec

            if (opLtr == 'M' || opLtr == 'EQ' || opLtr == 'X' || opLtr == 'D' || opLtr == 'N' || opLtr == '=')
                lengthOnRef += opLen;

            cigarArray.push({len: opLen, ltr: opLtr});

        }

        return {lengthOnRef: lengthOnRef, array: cigarArray};
    }



    function translateCigar(cigar) {

        var cigarUnit, opLen, opLtr,
            lengthOnRef = 0,
            cigarArray = [];

        for (i = 0; i < cigar.length; i++) {

            cigarUnit = cigar[i];

            opLtr = CigarOperationTable[cigarUnit.operation];
            opLen = parseInt(cigarUnit.operationLength);    // TODO -- this should be a long by the spec

            if (opLtr == 'M' || opLtr == 'EQ' || opLtr == 'X' || opLtr == 'D' || opLtr == 'N' || opLtr == '=')
                lengthOnRef += opLen;

            cigarArray.push({len: opLen, ltr: opLtr});

        }

        return {lengthOnRef: lengthOnRef, array: cigarArray};
    }


    /**
     * Split the alignment record into blocks as specified in the cigarArray.  Each aligned block contains
     * its portion of the read sequence and base quality strings.  A read sequence or base quality string
     * of "*" indicates the value is not recorded.  In all other cases the length of the block sequence (block.seq)
     * and quality string (block.qual) must == the block length.
     *
     * NOTE: Insertions are not yet treated // TODO
     *
     * @param record
     * @param cigarArray
     * @returns array of blocks
     */
    function makeBlocks(record, cigarArray) {


        var blocks = [],
            seqOffset = 0,
            pos = record.start,
            len = cigarArray.length,
            blockSeq,
            blockQuals,
            gapType;

        for (var i = 0; i < len; i++) {

            var c = cigarArray[i];

            switch (c.ltr) {
                case 'H' :
                    break; // ignore hard clips
                case 'P' :
                    break; // ignore pads
                case 'S' :
                    seqOffset += c.len;
                    gapType = 'S';
                    break; // soft clip read bases
                case 'N' :
                    pos += c.len;
                    gapType = 'N';
                    break;  // reference skip
                case 'D' :
                    pos += c.len;
                    gapType = 'D';
                    break;
                case 'I' :
                    seqOffset += c.len;
                    break;
                case 'M' :
                case 'EQ' :
                case '=' :
                case 'X' :
                    blockSeq = record.seq === "*" ? "*" : record.seq.substr(seqOffset, c.len);
                    blockQuals = record.qual === "*" ? "*" : record.qual.slice(seqOffset, c.len);
                    blocks.push({start: pos, len: c.len, seq: blockSeq, qual: blockQuals, gapType: gapType});
                    seqOffset += c.len;
                    pos += c.len;
                    break;
                default :
                    console.log("Error processing cigar element: " + c.len + c.ltr);
            }
        }

        return blocks;

    }

    return igv;


})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {


    var BAM_MAGIC = 21840194;
    var BAI_MAGIC = 21578050;
    var SECRET_DECODER = ['=', 'A', 'C', 'x', 'G', 'x', 'x', 'x', 'T', 'x', 'x', 'x', 'x', 'x', 'x', 'N'];
    var CIGAR_DECODER = ['M', 'I', 'D', 'N', 'S', 'H', 'P', '=', 'X', '?', '?', '?', '?', '?', '?', '?'];
    var READ_PAIRED_FLAG = 0x1;
    var PROPER_PAIR_FLAG = 0x2;
    var READ_UNMAPPED_FLAG = 0x4;
    var MATE_UNMAPPED_FLAG = 0x8;
    var READ_STRAND_FLAG = 0x10;
    var MATE_STRAND_FLAG = 0x20;
    var FIRST_OF_PAIR_FLAG = 0x40;
    var SECOND_OF_PAIR_FLAG = 0x80;
    var NOT_PRIMARY_ALIGNMENT_FLAG = 0x100;
    var READ_FAILS_VENDOR_QUALITY_CHECK_FLAG = 0x200;
    var DUPLICATE_READ_FLAG = 0x400;
    var SUPPLEMENTARY_ALIGNMENT_FLAG = 0x800;

    var CigarOperationTable = {
        "ALIGNMENT_MATCH": "M",
        "INSERT": "I",
        "DELETE": "D",
        "SKIP": "N",
        "CLIP_SOFT": "S",
        "CLIP_HARD": "H",
        "PAD": "P",
        "SEQUENCE_MATCH": "=",
        "SEQUENCE_MISMATCH": "X"
    }

    igv.Ga4ghAlignmentReader = function (config) {

        this.config = config;
        this.url = config.url;
        this.filter = config.filter || new igv.BamFilter();
        this.readGroupSetIds = config.readGroupSetIds;
        this.authKey = config.authKey;   // Might be undefined or nill

        this.samplingWindowSize = config.samplingWindowSize === undefined ? 100 : config.samplingWindowSize;
        this.samplingDepth = config.samplingDepth === undefined ? 100 : config.samplingDepth;
        if (config.viewAsPairs) {
            this.pairsSupported = true;
        }
        else {
            this.pairsSupported = config.pairsSupported === undefined ? true : config.pairsSupported;
        }
    }


    igv.Ga4ghAlignmentReader.prototype.readAlignments = function (chr, bpStart, bpEnd) {


        //ALIGNMENT_MATCH, INSERT, DELETE, SKIP, CLIP_SOFT, CLIP_HARD, PAD, SEQUENCE_MATCH, SEQUENCE_MISMATCH
        var self = this,
            cigarMap = {
                "ALIGNMENT_MATCH": "M",
                "INSERT": "I",
                "DELETE": "D",
                "SKIP": "N",
                "CLIP_SOFT": "S",
                "CLIP_HARD": "H",
                "PAD": "P",
                "SEQUENCE_MATCH": "=",
                "SEQUENCE_MISMATCH": "X"
            };

        return new Promise(function (fulfill, reject) {

            getChrNameMap().then(function (chrNameMap) {

                var queryChr = chrNameMap.hasOwnProperty(chr) ? chrNameMap[chr] : chr,
                    readURL = self.url + "/reads/search";

                igv.ga4ghSearch({
                    url: readURL,
                    body: {
                        "readGroupSetIds": [self.readGroupSetIds],
                        "referenceName": queryChr,
                        "start": bpStart,
                        "end": bpEnd,
                        "pageSize": "10000"
                    },
                    decode: decodeGa4ghReads,
                    results: new igv.AlignmentContainer(chr, bpStart, bpEnd, self.samplingWindowSize, self.samplingDepth, self.pairsSupported)
                }).then(fulfill)
                    .catch(reject);

            }).catch(reject);

            function getChrNameMap() {


                return new Promise(function (fulfill, reject) {
                    if (self.chrNameMap) {
                        fulfill(self.chrNameMap);
                    }

                    else {
                        self.readMetadata().then(function (json) {

                            self.chrNameMap = {};

                            if (igv.browser && json.readGroups && json.readGroups.length > 0) {

                                var referenceSetId = json.readGroups[0].referenceSetId;

                                console.log("No reference set specified");

                                if (referenceSetId) {

                                    // Query for reference names to build an alias table (map of genome ref names -> dataset ref names)
                                    var readURL = self.url + "/references/search";

                                    igv.ga4ghSearch({
                                        url: readURL,
                                        body: {
                                            "referenceSetId": referenceSetId
                                        },
                                        decode: function (j) {
                                            return j.references;
                                        }
                                    }).then(function (references) {
                                        references.forEach(function (ref) {
                                            var refName = ref.name,
                                                alias = igv.browser.genome.getChromosomeName(refName);
                                            self.chrNameMap[alias] = refName;
                                        });
                                        fulfill(self.chrNameMap);

                                    }).catch(reject);
                                }
                                else {

                                    // Try hardcoded constants -- workaround for non-compliant data at Google
                                    populateChrNameMap(self.chrNameMap, self.config.datasetId);

                                    fulfill(self.chrNameMap);
                                }
                            }

                            else {
                                // No browser object, can't build map.  This can occur when run from unit tests
                                fulfill(self.chrNameMap);
                            }
                        }).catch(reject);
                    }

                });
            }


            /**
             * Decode an array of ga4gh read records
             *

             */
            function decodeGa4ghReads(json) {

                var i,
                    jsonRecords = json.alignments,
                    len = jsonRecords.length,
                    json,
                    alignment,
                    jsonAlignment,
                    cigarDecoded,
                    alignments = [],
                    genome = igv.browser.genome,
                    mate;

                for (i = 0; i < len; i++) {

                    json = jsonRecords[i];

                    alignment = new igv.BamAlignment();

                    alignment.readName = json.fragmentName;
                    alignment.properPlacement = json.properPlacement;
                    alignment.duplicateFragment = json.duplicateFragment;
                    alignment.numberReads = json.numberReads;
                    alignment.fragmentLength = json.fragmentLength;
                    alignment.readNumber = json.readNumber;
                    alignment.failedVendorQualityChecks = json.failedVendorQualityChecks;
                    alignment.secondaryAlignment = json.secondaryAlignment;
                    alignment.supplementaryAlignment = json.supplementaryAlignment;
                    alignment.seq = json.alignedSequence;
                    alignment.qual = json.alignedQuality;
                    alignment.matePos = json.nextMatePosition;
                    alignment.tagDict = json.info;
                    alignment.flags = encodeFlags(json);


                    jsonAlignment = json.alignment;
                    if (jsonAlignment) {
                        alignment.mapped = true;

                        alignment.chr = json.alignment.position.referenceName;
                        if (genome) alignment.chr = genome.getChromosomeName(alignment.chr);

                        alignment.start = parseInt(json.alignment.position.position);
                        alignment.strand = !(json.alignment.position.reverseStrand);
                        alignment.mq = json.alignment.mappingQuality;
                        alignment.cigar = encodeCigar(json.alignment.cigar);
                        cigarDecoded = translateCigar(json.alignment.cigar);

                        alignment.lengthOnRef = cigarDecoded.lengthOnRef;

                        blocks = makeBlocks(alignment, cigarDecoded.array);
                        alignment.blocks = blocks.blocks;
                        alignment.insertions = blocks.insertions;

                    }
                    else {
                        alignment.mapped = false;
                    }

                    mate = json.nextMatePosition;
                    if (mate) {
                        alignment.mate = {
                            chr: mate.referenceFrame,
                            position: parseInt(mate.position),
                            strand: !mate.reverseStrand
                        };
                    }

                    if (self.filter.pass(alignment)) {
                        alignments.push(alignment);
                    }


                }

                return alignments;

                // Encode a cigar string -- used for popup text
                function encodeCigar(cigarArray) {

                    var cigarString = "";
                    cigarArray.forEach(function (cigarUnit) {
                        var op = CigarOperationTable[cigarUnit.operation],
                            len = cigarUnit.operationLength;
                        cigarString = cigarString + (len + op);
                    });

                    return cigarString;
                }

                // TODO -- implement me
                function encodeFlags(json) {
                    return 0;
                }

                function translateCigar(cigar) {

                    var cigarUnit, opLen, opLtr,
                        lengthOnRef = 0,
                        cigarArray = [],
                        i;

                    for (i = 0; i < cigar.length; i++) {

                        cigarUnit = cigar[i];

                        opLtr = CigarOperationTable[cigarUnit.operation];
                        opLen = parseInt(cigarUnit.operationLength);    // Google represents long as a String

                        if (opLtr === 'M' || opLtr === 'EQ' || opLtr === 'X' || opLtr === 'D' || opLtr === 'N' || opLtr === '=')
                            lengthOnRef += opLen;

                        cigarArray.push({len: opLen, ltr: opLtr});

                    }

                    return {lengthOnRef: lengthOnRef, array: cigarArray};
                }


                /**
                 * Split the alignment record into blocks as specified in the cigarArray.  Each aligned block contains
                 * its portion of the read sequence and base quality strings.  A read sequence or base quality string
                 * of "*" indicates the value is not recorded.  In all other cases the length of the block sequence (block.seq)
                 * and quality string (block.qual) must == the block length.
                 *
                 * NOTE: Insertions are not yet treated // TODO
                 *
                 * @param record
                 * @param cigarArray
                 * @returns array of blocks
                 */
                function makeBlocks(record, cigarArray) {


                    var blocks = [],
                        insertions,
                        seqOffset = 0,
                        pos = record.start,
                        len = cigarArray.length,
                        blockSeq,
                        gapType,
                        blockQuals;

                    for (var i = 0; i < len; i++) {

                        var c = cigarArray[i];

                        switch (c.ltr) {
                            case 'H' :
                                break; // ignore hard clips
                            case 'P' :
                                break; // ignore pads
                            case 'S' :
                                seqOffset += c.len;
                                gapType = 'S';
                                break; // soft clip read bases
                            case 'N' :
                                pos += c.len;
                                gapType = 'N';
                                break;  // reference skip
                            case 'D' :
                                pos += c.len;
                                gapType = 'D';
                                break;
                            case 'I' :
                                blockSeq = record.seq === "*" ? "*" : record.seq.substr(seqOffset, c.len);
                                blockQuals = record.qual ? record.qual.slice(seqOffset, c.len) : undefined;
                                if (insertions === undefined) insertions = [];
                                insertions.push({start: pos, len: c.len, seq: blockSeq, qual: blockQuals});
                                seqOffset += c.len;
                                break;
                            case 'M' :
                            case 'EQ' :
                            case '=' :
                            case 'X' :

                                blockSeq = record.seq === "*" ? "*" : record.seq.substr(seqOffset, c.len);
                                blockQuals = record.qual ? record.qual.slice(seqOffset, c.len) : undefined;
                                blocks.push({start: pos, len: c.len, seq: blockSeq, qual: blockQuals, gapType: gapType});
                                seqOffset += c.len;
                                pos += c.len;

                                break;

                            default :
                                console.log("Error processing cigar element: " + c.len + c.ltr);
                        }
                    }

                    return {blocks: blocks, insertions: insertions};
                }
            }


        });
    }


    igv.Ga4ghAlignmentReader.prototype.readMetadata = function () {

        return igv.ga4ghGet({
            url: this.url,
            entity: "readgroupsets",
            entityId: this.readGroupSetIds
        });
    }

    igv.decodeGa4ghReadset = function (json) {

        var sequenceNames = [],
            fileData = json["fileData"];

        fileData.forEach(function (fileObject) {

            var refSequences = fileObject["refSequences"];

            refSequences.forEach(function (refSequence) {
                sequenceNames.push(refSequence["name"]);
            });
        });

        return sequenceNames;
    }


    /**
     * Hardcoded hack to work around some non-compliant google datasets
     *
     * @param chrNameMap
     * @param datasetId
     */
    function populateChrNameMap(chrNameMap, datasetId) {
        var i;
        if ("461916304629" === datasetId || "337315832689" === datasetId) {
            for (i = 1; i < 23; i++) {
                chrNameMap["chr" + i] = i;
            }
            chrNameMap["chrX"] = "X";
            chrNameMap["chrY"] = "Y";
            chrNameMap["chrM"] = "MT";
        }
    }


    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    /**
     *
     * @param options
     */
    igv.ga4ghGet = function (options) {

        var url = options.url + "/" + options.entity + "/" + options.entityId,
            apiKey = oauth.google.apiKey,
            paramSeparator = "?";

        if (apiKey) {
            url = url + paramSeparator + "key=" + apiKey;
        }

        options.headers = ga4ghHeaders();

        return igvxhr.loadJson(url, options);      // Returns a promise
    }

    igv.ga4ghSearch = function (options) {

        return new Promise(function (fulfill, reject) {
            var results = options.results ? options.results : [],
                url = options.url,
                body = options.body,
                decode = options.decode,
                apiKey = oauth.google.apiKey,
                paramSeparator = "?",
                fields = options.fields;  // Partial response

            if (apiKey) {
                url = url + paramSeparator + "key=" + apiKey;
                paramSeparator = "&";
            }

            if (fields) {
                url = url + paramSeparator + "fields=" + fields;
            }


            // Start the recursive load cycle.  Data is fetched in chunks, if more data is available a "nextPageToken" is returned.
            loadChunk();

            function loadChunk(pageToken) {

                if (pageToken) {
                    body.pageToken = pageToken;
                }
                else {
                    if (body.pageToken != undefined) delete body.pageToken;    // Remove previous page token, if any
                }

                var sendData = JSON.stringify(body);

                igvxhr.loadJson(url,
                    {
                        sendData: sendData,
                        contentType: "application/json",
                        headers: ga4ghHeaders()
                    }).then(function (json) {
                    var nextPageToken, tmp;

                    if (json) {

                        tmp = decode ? decode(json) : json;

                        if (tmp) {

                            tmp.forEach(function (a) {
                                var keep = true;           // TODO -- conditionally keep (downsample)
                                if (keep) {
                                    results.push(a);
                                }
                            });
                        }


                        nextPageToken = json["nextPageToken"];

                        if (nextPageToken) {
                            loadChunk(nextPageToken);
                        }
                        else {
                            fulfill(results);
                        }
                    }
                    else {
                        fulfill(results);
                    }

                }).catch(function (error) {
                    reject(error);
                });
            }

        });


    }

    igv.ga4ghSearchReadGroupSets = function (options) {

        igv.ga4ghSearch({
            url: options.url + "/readgroupsets/search",
            body: {
                "datasetIds": [options.datasetId],

                "pageSize": "10000"
            },
            decode: function (json) {
                return json.readGroupSets;
            }
        }).then(function (results) {
            options.success(results);
        }).catch(function (error) {
            console.log(error);
        });
    }

    igv.ga4ghSearchVariantSets = function (options) {

        igv.ga4ghSearch({
            url: options.url + "/variantsets/search",
            body: {
                "datasetIds": [options.datasetId],
                "pageSize": "10000"
            },
            decode: function (json) {
                return json.variantSets;
            }
        }).then(function (results) {
            options.success(results);
        }).catch(function (error) {
            console.log(error);
        });
    }

    igv.ga4ghSearchCallSets = function (options) {

        // When searching by dataset id, first must get variant sets.
        if (options.datasetId) {

            igv.ga4ghSearchVariantSets({

                url: options.url,
                datasetId: options.datasetId,
                success: function (results) {

                    var variantSetIds = [];
                    results.forEach(function (vs) {
                        variantSetIds.push(vs.id);
                    });

                    // Substitute variantSetIds for datasetId
                    options.datasetId = undefined;
                    options.variantSetIds = variantSetIds;
                    igv.ga4ghSearchCallSets(options);


                }
            });

        }

        else {

            igv.ga4ghSearch({
                url: options.url + "/callsets/search",
                body: {
                    "variantSetIds": options.variantSetIds,
                    "pageSize": "10000"
                },
                decode: function (json) {

                    if (json.callSets) json.callSets.forEach(function (cs) {
                        cs.variantSetIds = options.variantSetIds;
                    });

                    return json.callSets;
                }
            }).then(function (results) {
                options.success(results);
            }).catch(function (error) {
                console.log(error);
            });
        }
    }


    /**
     * Method to support ga4gh application
     *
     * @param options
     */
    igv.ga4ghSearchReadAndCallSets = function (options) {

        igv.ga4ghSearchReadGroupSets({
            url: options.url,
            datasetId: options.datasetId,
            success: function (readGroupSets) {
                igv.ga4ghSearchCallSets({
                    url: options.url,
                    datasetId: options.datasetId,
                    success: function (callSets) {

                        // Merge call sets and read group sets

                        var csHash = {};
                        callSets.forEach(function (cs) {
                            csHash[cs.name] = cs;
                        });

                        var mergedResults = [];
                        readGroupSets.forEach(function (rg) {
                            var m = {readGroupSetId: rg.id, name: rg.name, datasetId: options.datasetId},
                                cs = csHash[rg.name];
                            if (cs) {
                                m.callSetId = cs.id;
                                m.variantSetIds = cs.variantSetIds;
                            }
                            mergedResults.push(m);
                        });

                        options.success(mergedResults);

                    }
                });
            }
        });

    }


    function ga4ghHeaders() {

        var headers = {},
            acToken = oauth.google.access_token;

        headers["Cache-Control"] = "no-cache";
        if (acToken) {
            headers["Authorization"] = "Bearer " + acToken;
        }
        return headers;

    }

    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {


    igv.Ga4ghVariantReader = function (config) {

        this.config = config;
        this.url = config.url;
        this.variantSetId = config.variantSetId;
        this.callSetIds = config.callSetIds;
        this.includeCalls = (config.includeCalls === undefined ? true : config.includeCalls);

    }

    // Simulate a VCF file header
    igv.Ga4ghVariantReader.prototype.readHeader = function () {

        var self = this;

        return new Promise(function (fulfill, reject) {


            if (self.header) {
                fulfill(self.header);
            }

            else {

                self.header = {};

                if (self.includeCalls === false) {
                    fulfill(self.header);
                }
                else {

                    var readURL = self.url + "/callsets/search";

                    igv.ga4ghSearch({
                        url: readURL,
                        fields: "nextPageToken,callSets(id,name)",
                        body: {
                            "variantSetIds": (Array.isArray(self.variantSetId) ? self.variantSetId : [self.variantSetId]),
                            "pageSize": "10000"
                        },
                        decode: function (json) {
                            // If specific callSetIds are specified filter to those
                            if (self.callSetIds) {
                                var filteredCallSets = [],
                                    csIdSet = new Set();

                                csIdSet.addAll(self.callSetIds);
                                json.callSets.forEach(function (cs) {
                                    if (csIdSet.has(cs.id)) {
                                        filteredCallSets.push(cs);
                                    }
                                });
                                return filteredCallSets;
                            }
                            else {
                                return json.callSets;
                            }
                        }
                    }).then(function (callSets) {
                        self.header.callSets = callSets;
                        fulfill(self.header);
                    }).catch(reject);
                }
            }

        });

    }


    igv.Ga4ghVariantReader.prototype.readFeatures = function (chr, bpStart, bpEnd) {

        var self = this;

        return new Promise(function (fulfill, reject) {

            self.readHeader().then(function (header) {

                getChrNameMap().then(function (chrNameMap) {

                    var queryChr = chrNameMap.hasOwnProperty(chr) ? chrNameMap[chr] : chr,
                        readURL = self.url + "/variants/search";

                    igv.ga4ghSearch({
                        url: readURL,
                        fields: (self.includeCalls ? undefined : "nextPageToken,variants(id,variantSetId,names,referenceName,start,end,referenceBases,alternateBases,quality, filter, info)"),
                        body: {
                            "variantSetIds": (Array.isArray(self.variantSetId) ? self.variantSetId : [self.variantSetId]),
                            "callSetIds": (self.callSetIds ? self.callSetIds : undefined),
                            "referenceName": queryChr,
                            "start": bpStart.toString(),
                            "end": bpEnd.toString(),
                            "pageSize": "10000"
                        },
                        decode: function (json) {
                            var variants = [];

                            json.variants.forEach(function (json) {
                                variants.push(igv.createGAVariant(json));
                            });

                            return variants;
                        }
                    }).then(fulfill).catch(reject);
                }).catch(reject);  // chr name map
            }).catch(reject);  // callsets
        });


        function getChrNameMap() {

            return new Promise(function (fulfill, reject) {

                if (self.chrNameMap) {
                    fulfill(self.chrNameMap);
                }

                else {
                    self.readMetadata().then(function (json) {

                        self.metadata = json.metadata;
                        self.chrNameMap = {};
                        if (json.referenceBounds && igv.browser) {
                            json.referenceBounds.forEach(function (rb) {
                                var refName = rb.referenceName,
                                    alias = igv.browser.genome.getChromosomeName(refName);
                                self.chrNameMap[alias] = refName;

                            });
                        }
                        fulfill(self.chrNameMap);

                    })
                }

            });
        }

    }


    igv.Ga4ghVariantReader.prototype.readMetadata = function () {

        return igv.ga4ghGet({
            url: this.url,
            entity: "variantsets",
            entityId: this.variantSetId
        });
    }


    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {


    igv.translateGoogleCloudURL = function(gsUrl) {

        var i = gsUrl.indexOf('/', 5);
        if (i < 0) {
            console.log("Invalid gs url: " + gsUrl);
            return gsUrl;
        }

        var bucket = gsUrl.substring(5, i);
        var object = encodeURIComponent(gsUrl.substring(i + 1));

        return "https://www.googleapis.com/storage/v1/b/" + bucket + "/o/" + object + "?alt=media";



    }


    return igv;

})(igv || {});


/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    igv.Genome = function (sequence, ideograms, aliases) {

        this.sequence = sequence;
        this.chromosomeNames = sequence.chromosomeNames;
        this.chromosomes = sequence.chromosomes;  // An object (functions as a dictionary)
        this.ideograms = ideograms;

        /**
         * Return the official chromosome name for the (possibly) alias.  Deals with
         * 1 <-> chr1,  chrM <-> MT,  IV <-> chr4, etc.
         * @param str
         */
        var chrAliasTable = {},
            self = this;

        // The standard mappings
        this.chromosomeNames.forEach(function (name) {
            var alias = name.startsWith("chr") ? name.substring(3) : "chr" + name;
            chrAliasTable[alias] = name;
            if (name === "chrM") chrAliasTable["MT"] = "chrM";
            if (name === "MT") chrAliasTable["chrmM"] = "MT";
        });

        // Custom mappings
        if (aliases) {
            aliases.forEach(function (array) {
                // Find the official chr name
                var defName;
                for (i = 0; i < array.length; i++) {
                    if (self.chromosomes[array[i]]) {
                        defName = array[i];
                        break;
                    }
                }

                if (defName) {
                    array.forEach(function (alias) {
                        if (alias !== defName) {
                            chrAliasTable[alias] = defName;
                        }
                    });
                }

            });
        }

        this.chrAliasTable = chrAliasTable;

    }

    igv.Genome.prototype.getChromosomeName = function (str) {
        var chr = this.chrAliasTable[str];
        return chr ? chr : str;
    }

    igv.Genome.prototype.getChromosome = function (chr) {
        chr = this.getChromosomeName(chr);
        return this.chromosomes[chr];
    }

    igv.Genome.prototype.getCytobands = function (chr) {
        return this.ideograms ? this.ideograms[chr] : null;
    }

    igv.Genome.prototype.getLongestChromosome = function () {

        var longestChr,
            key,
            chromosomes = this.chromosomes;
        for (key in chromosomes) {
            if (chromosomes.hasOwnProperty(key)) {
                var chr = chromosomes[key];
                if (longestChr === undefined || chr.bpLength > longestChr.bpLength) {
                    longestChr = chr;
                }
            }
            return longestChr;
        }
    }

    igv.Genome.prototype.getChromosomes = function () {
        return this.chromosomes;
    }

    igv.Chromosome = function (name, order, bpLength) {
        this.name = name;
        this.order = order;
        this.bpLength = bpLength;
    }

    igv.Cytoband = function (start, end, name, typestain) {
        this.start = start;
        this.end = end;
        this.name = name;
        this.stain = 0;

        // Set the type, either p, n, or c
        if (typestain == 'acen') {
            this.type = 'c';
        } else {
            this.type = typestain.charAt(1);
            if (this.type == 'p') {
                this.stain = parseInt(typestain.substring(4));
            }
        }
    }

    igv.GenomicInterval = function (chr, start, end, features) {
        this.chr = chr;
        this.start = start;
        this.end = end;
        this.features = features;
    }

    igv.GenomicInterval.prototype.contains = function (chr, start, end) {
        return this.chr == chr &&
            this.start <= start &&
            this.end >= end;
    }

    igv.GenomicInterval.prototype.containsRange = function (range) {
        return this.chr === range.chr &&
            this.start <= range.start &&
            this.end >= range.end;
    }

    igv.loadGenome = function (reference) {

        return new Promise(function (fulfill, reject) {

            var cytobandUrl = reference.cytobandURL,
                cytobands,
                aliasURL = reference.aliasURL,
                aliases,
                chrNames,
                chromosomes = {},
                sequence;

            sequence = new igv.FastaSequence(reference);

            sequence.init().then(function () {

                var order = 0;

                chrNames = sequence.chromosomeNames;
                chromosomes = sequence.chromosomes;

                if (cytobandUrl) {
                    loadCytobands(cytobandUrl, reference.withCredentials, function (result) {
                        cytobands = result;
                        checkReady();
                    });
                }

                if (aliasURL) {
                    loadAliases(aliasURL, reference.withCredentials, function (result) {
                        aliases = result;
                        checkReady();
                    });
                }

                checkReady();

            }).catch(function(err) {
                reject(err);
            });

            function checkReady() {

                var isReady = (cytobandUrl === undefined || cytobands !== undefined) &&
                    (aliasURL === undefined || aliases !== undefined);
                if (isReady) {
                    fulfill(new igv.Genome(sequence, cytobands, aliases));
                }

            }
        });
    }

    function loadCytobands(cytobandUrl, withCredentials, continuation) {

        igvxhr.loadString(cytobandUrl, {
            withCredentials: withCredentials
        }).then(function (data) {

            var bands = [],
                lastChr,
                n = 0,
                c = 1,
                lines = data.splitLines(),
                len = lines.length,
                cytobands = {};

            for (var i = 0; i < len; i++) {
                var tokens = lines[i].split("\t");
                var chr = tokens[0];
                if (!lastChr) lastChr = chr;

                if (chr != lastChr) {

                    cytobands[lastChr] = bands;
                    bands = [];
                    lastChr = chr;
                    n = 0;
                    c++;
                }

                if (tokens.length == 5) {
                    //10	0	3000000	p15.3	gneg
                    var chr = tokens[0];
                    var start = parseInt(tokens[1]);
                    var end = parseInt(tokens[2]);
                    var name = tokens[3];
                    var stain = tokens[4];
                    bands[n++] = new igv.Cytoband(start, end, name, stain);
                }
            }

            continuation(cytobands);
        });
    }

    function loadAliases(aliasURL, withCredentials, continuation) {
        igvxhr.loadString(aliasURL, {
            withCredentials: withCredentials
        }).then(function (data) {

            var lines = data.splitLines(),
                aliases = [];

            lines.forEach(function (line) {
                if (!line.startsWith("#") & line.length > 0) aliases.push(line.split("\t"));
            });

            continuation(aliases);
        });

    }

    return igv;

})
(igv || {});


/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {


    igv.EqtlTrack = function (config) {


        var url = config.url,
            label = config.name;

        this.config = config;
        this.url = url;
        this.name = label;
        this.pValueField = config.pValueField || "pValue";
        this.geneField = config.geneField || "geneName";
        this.minLogP = config.minLogP || 3.5;
        this.maxLogP = config.maxLogP || 25;
        this.background = config.background;    // No default
        this.divider = config.divider || "rgb(225,225,225)";
        this.dotSize = config.dotSize || 2;
        this.height = config.height || 100;
        this.autoHeight = false;
        this.disableButtons = config.disableButtons;

        this.featureSource = new igv.FeatureSource(config);


        this.onsearch = function (feature, source) {
            selectedFeature.call(this, feature, source);
        }
    }

    igv.EqtlTrack.prototype.paintAxis = function (ctx, pixelWidth, pixelHeight) {

        var track = this,
            yScale = (track.maxLogP - track.minLogP) / pixelHeight;

        var font = {
            'font': 'normal 10px Arial',
            'textAlign': 'right',
            'strokeStyle': "black"
        };

        igv.graphics.fillRect(ctx, 0, 0, pixelWidth, pixelHeight, {'fillStyle': "rgb(255, 255, 255)"});

        for (var p = 4; p <= track.maxLogP; p += 2) {

            var x1,
                x2,
                y1,
                y2,
                ref;

            // TODO: Dashes may not actually line up with correct scale. Ask Jim about this

            ref = 0.85 * pixelWidth;
            x1 = ref - 5;
            x2 = ref;

            y1 = y2 = pixelHeight - Math.round((p - track.minLogP) / yScale);

            igv.graphics.strokeLine(ctx, x1, y1, x2, y2, font); // Offset dashes up by 2 pixel

            igv.graphics.fillText(ctx, p, x1 - 1, y1 + 2, font); // Offset numbers down by 2 pixels; TODO: error
        }

        font['textAlign'] = 'center';

        igv.graphics.fillText(ctx, "-log10(pvalue)", pixelWidth/4, pixelHeight/2, font, {rotate: {angle: -90}});

    };

    igv.EqtlTrack.prototype.getFeatures = function (chr, bpStart, bpEnd) {
        return this.featureSource.getFeatures(chr, bpStart, bpEnd);
    }

    igv.EqtlTrack.prototype.draw = function (options) {

        var track = this,
            featureList = options.features,
            ctx = options.context,
            bpPerPixel = options.bpPerPixel,
            bpStart = options.bpStart,
            pixelWidth = options.pixelWidth,
            pixelHeight = options.pixelHeight,
            bpEnd = bpStart + pixelWidth * bpPerPixel + 1,
            yScale = (track.maxLogP - track.minLogP) / pixelHeight;

        // Background
        if (this.background) igv.graphics.fillRect(ctx, 0, 0, pixelWidth, pixelHeight, {'fillStyle': this.background});
        igv.graphics.strokeLine(ctx, 0, pixelHeight - 1, pixelWidth, pixelHeight - 1, {'strokeStyle': this.divider});

        if (ctx) {

            var len = featureList.length;


            ctx.save();


            // Draw in two passes, with "selected" eqtls drawn last
            drawEqtls(false);
            drawEqtls(true);


            ctx.restore();

        }

        function drawEqtls(drawSelected) {

            var radius = drawSelected ? 2 * track.dotSize : track.dotSize,
                eqtl, i, px, py, color, isSelected, snp, geneName, selection;


            //ctx.fillStyle = igv.selection.colorForGene(eqtl.geneName);
            igv.graphics.setProperties(ctx, {
                fillStyle: "rgb(180, 180, 180)",
                strokeStyle: "rgb(180, 180, 180)"
            });

            for (i = 0; i < len; i++) {

                eqtl = featureList[i];
                snp = eqtl.snp.toUpperCase();
                geneName = eqtl[track.geneField].toUpperCase();
                selection = igv.browser.selection;
                isSelected = selection &&
                (selection.snp === snp || selection.gene === geneName);

                if (drawSelected && !isSelected) continue;

                // Add eqtl's gene to the selection if this is the selected snp.
                // TODO -- this should not be done here in the rendering code.
                if (selection && selection.snp === snp) {
                    selection.addGene(geneName);
                }

                if (drawSelected && selection) {
                    color = selection.colorForGene(geneName);
                }

                if (drawSelected && color === undefined) continue;   // This should be impossible


                px = (Math.round(eqtl.position  - bpStart + 0.5)) / bpPerPixel;
                if (px < 0) continue;
                else if (px > pixelWidth) break;

                var mLogP = -Math.log(eqtl[track.pValueField]) / Math.LN10;
                if (mLogP < track.minLogP) continue;

                py = Math.max(0 + radius, pixelHeight - Math.round((mLogP - track.minLogP) / yScale));
                eqtl.px = px;
                eqtl.py = py;

                if (color) igv.graphics.setProperties(ctx, {fillStyle: color, strokeStyle: "black"});
                igv.graphics.fillCircle(ctx, px, py, radius);
                igv.graphics.strokeCircle(ctx, px, py, radius);
            }
        }

    }

    function selectedFeature(feature, source) {
        console.log(feature + " " + source);

        // TODO -- temporary hack, determine type from the source
        var type = source === "gtex" ? "snp" : "gene";

        this.selection = new GtexSelection(type == 'gene' ? {gene: feature} : {snp: feature});
        igv.browser.update();
    }

    /**
     * Return "popup data" for feature @ genomic location.  Data is an array of key-value pairs
     */
    igv.EqtlTrack.prototype.popupData = function (genomicLocation, xOffset, yOffset) {

        // We use the featureCache property rather than method to avoid async load.  If the
        // feature is not already loaded this won't work,  but the user wouldn't be mousing over it either.
        if (this.featureSource.featureCache) {

            var chr = igv.browser.referenceFrame.chr,  // TODO -- this should be passed in
                tolerance = 2 * this.dotSize * igv.browser.referenceFrame.bpPerPixel,
                featureList = this.featureSource.featureCache.queryFeatures(chr, genomicLocation - tolerance, genomicLocation + tolerance),
                dotSize = this.dotSize,
                tissue = this.name;

            if (featureList && featureList.length > 0) {


                var popupData = [];
                featureList.forEach(function (feature) {
                    if (feature.end >= genomicLocation - tolerance &&
                        feature.start <= genomicLocation + tolerance &&
                        feature.py - yOffset < 2 * dotSize) {

                        if(popupData.length > 0) {
                            popupData.push("<hr>");
                        }

                        popupData.push(
                            {name: "snp id", value: feature.snp},
                            {name: "gene id", value: feature.geneId},
                            {name: "gene name", value: feature.geneName},
                            {name: "p value", value: feature.pValue},
                            {name: "tissue", value: tissue});

                    }
                });
                return popupData;
            }
        }
    }

    GtexSelection = function (selection) {

        this.geneColors = {};
        this.gene = null;
        this.snp = null;
        this.genesCount = 0;

        if (selection.gene) {
            this.gene = selection.gene.toUpperCase();
            this.geneColors[this.gene] = brewer[this.genesCount++];

        }
        if (selection.snp) {
            this.snp = selection.snp.toUpperCase();
        }

    }

    GtexSelection.prototype.addGene = function (geneName) {
        if (!this.geneColors[geneName.toUpperCase()]) {
            this.geneColors[geneName.toUpperCase()] = brewer[this.genesCount++];
        }
    }

    GtexSelection.prototype.colorForGene = function (geneName) {
        return this.geneColors[geneName.toUpperCase()];
    }

    var brewer = new Array();
// Set +!
    brewer.push("rgb(228,26,28)");
    brewer.push("rgb(55,126,184)");
    brewer.push("rgb(77,175,74)");
    brewer.push("rgb(166,86,40)");
    brewer.push("rgb(152,78,163)");
    brewer.push("rgb(255,127,0)");
    brewer.push("rgb(247,129,191)");
    brewer.push("rgb(153,153,153)");
    brewer.push("rgb(255,255,51)");

// #Set 2
    brewer.push("rgb(102, 194, 165");
    brewer.push("rgb(252, 141, 98");
    brewer.push("rgb(141, 160, 203");
    brewer.push("rgb(231, 138, 195");
    brewer.push("rgb(166, 216, 84");
    brewer.push("rgb(255, 217, 47");
    brewer.push("rgb(229, 196, 148");
    brewer.push("rgb(179, 179, 179");

//#Set 3
    brewer.push("rgb( 141, 211, 199");
    brewer.push("rgb(255, 255, 179");
    brewer.push("rgb(190, 186, 218");
    brewer.push("rgb(251, 128, 114");
    brewer.push("rgb(128, 177, 211");
    brewer.push("rgb(253, 180, 98");
    brewer.push("rgb(179, 222, 105");
    brewer.push("rgb(252, 205, 229");
    brewer.push("rgb(217, 217, 217");
    brewer.push("rgb(188, 128, 189");
    brewer.push("rgb(204, 235, 197");
    brewer.push("rgb(255, 237, 111");

    return igv;

})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {


    igv.GtexSelection = function (selection) {

        this.geneColors = {};
        this.gene = null;
        this.snp = null;
        this.genesCount = 0;

        if (selection.gene) {
            this.gene = selection.gene.toUpperCase();
            this.geneColors[this.gene] = brewer[this.genesCount++];

        }
        if (selection.snp) {
            this.snp = selection.snp.toUpperCase();
        }

    }

    igv.GtexSelection.prototype.addGene = function (geneName) {
        if (!this.geneColors[geneName.toUpperCase()]) {
            this.geneColors[geneName.toUpperCase()] = brewer[this.genesCount++];
        }
    }

    igv.GtexSelection.prototype.colorForGene = function (geneName) {
        return this.geneColors[geneName.toUpperCase()];
    }

    var brewer = new Array();
// Set +!
    brewer.push("rgb(228,26,28)");
    brewer.push("rgb(55,126,184)");
    brewer.push("rgb(77,175,74)");
    brewer.push("rgb(166,86,40)");
    brewer.push("rgb(152,78,163)");
    brewer.push("rgb(255,127,0)");
    brewer.push("rgb(247,129,191)");
    brewer.push("rgb(153,153,153)");
    brewer.push("rgb(255,255,51)");

// #Set 2
    brewer.push("rgb(102, 194, 165");
    brewer.push("rgb(252, 141, 98");
    brewer.push("rgb(141, 160, 203");
    brewer.push("rgb(231, 138, 195");
    brewer.push("rgb(166, 216, 84");
    brewer.push("rgb(255, 217, 47");
    brewer.push("rgb(229, 196, 148");
    brewer.push("rgb(179, 179, 179");

//#Set 3
    brewer.push("rgb( 141, 211, 199");
    brewer.push("rgb(255, 255, 179");
    brewer.push("rgb(190, 186, 218");
    brewer.push("rgb(251, 128, 114");
    brewer.push("rgb(128, 177, 211");
    brewer.push("rgb(253, 180, 98");
    brewer.push("rgb(179, 222, 105");
    brewer.push("rgb(252, 205, 229");
    brewer.push("rgb(217, 217, 217");
    brewer.push("rgb(188, 128, 189");
    brewer.push("rgb(204, 235, 197");
    brewer.push("rgb(255, 237, 111");


    return igv;

})
(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    igv.GtexFileReader = function (config) {

        this.config = config;
        this.file = config.url;
        this.codec = this.file.endsWith(".bin") ? createEqtlBinary : createEQTL,
            this.cache = {};
        this.binary = this.file.endsWith(".bin");
        this.compressed = this.file.endsWith(".compressed.bin");

    };

    igv.GtexFileReader.prototype.readFeatures = function (chr, bpStart, bpEnd) {

        var self = this;

        return new Promise(function (fulfill, reject) {
            var file = self.file,
                index = self.index;

            if (index) {
                loadWithIndex(index, chr, fulfill);
            }
            else {
                loadIndex(self.file, function (index) {
                    self.index = index;
                    loadWithIndex(index, chr, fulfill);

                });

            }

            function loadWithIndex(index, chr, fulfill) {

                var chrIdx = index[chr];
                if (chrIdx) {
                    var blocks = chrIdx.blocks,
                        lastBlock = blocks[blocks.length - 1],
                        endPos = lastBlock.startPos + lastBlock.size,
                        len = endPos - blocks[0].startPos,
                        range = {start: blocks[0].startPos, size: len};


                    igvxhr.loadArrayBuffer(file,
                        {
                            range: range,
                            withCredentials: self.config.withCredentials
                        }).then(function (arrayBuffer) {

                            if (arrayBuffer) {

                                var data = new DataView(arrayBuffer);
                                var parser = new igv.BinaryParser(data);

                                var featureList = [];
                                var lastOffset = parser.offset;
                                while (parser.hasNext()) {
                                    var feature = createEqtlBinary(parser);
                                    featureList.push(feature);
                                }

                                fulfill(featureList);
                            }
                            else {
                                fulfill(null);
                            }

                        }).catch(reject);


                }
                else {
                    fulfill([]); // Mark with empy array, so we don't try again
                }


                var createEqtlBinary = function (parser) {
                    var snp = parser.getString();
                    var chr = parser.getString();
                    var position = parser.getInt();
                    var geneId = parser.getString();
                    var geneName = parser.getString();
                    //var genePosition = -1;
                    //var fStat = parser.getFloat();
                    var pValue = parser.getFloat();
                    //var qValue = parser.getFloat();
                    return new Eqtl(snp, chr, position, geneId, geneName, pValue);
                }

            }


            /**
             * Load the index
             *
             * @param fulfill function to receive the result
             */
            function loadIndex(url, fulfill) {

                var genome = igv.browser ? igv.browser.genome : null;

                igvxhr.loadArrayBuffer(url,
                    {
                        range: {start: 0, size: 200},
                        withCredentials: self.config.withCredentials

                    }).then(function (arrayBuffer) {

                        var data = new DataView(arrayBuffer),
                            parser = new igv.BinaryParser(data),
                            magicNumber = parser.getInt(),
                            version = parser.getInt(),
                            indexPosition = parser.getLong(),
                            indexSize = parser.getInt();

                        igvxhr.loadArrayBuffer(url, {

                            range: {start: indexPosition, size: indexSize},
                            withCredentials: self.config.withCredentials

                        }).then(function (arrayBuffer2) {

                            var data2 = new DataView(arrayBuffer2);
                            var index = null;


                            var parser = new igv.BinaryParser(data2);
                            var index = {};
                            var nChrs = parser.getInt();
                            while (nChrs-- > 0) {

                                var chr = parser.getString();
                                if (genome) chr = genome.getChromosomeName(chr);

                                var position = parser.getLong();
                                var size = parser.getInt();
                                var blocks = new Array();
                                blocks.push(new Block(position, size));
                                index[chr] = new ChrIdx(chr, blocks);
                            }

                            fulfill(index)
                        });
                    });
            }


            //function Eqtl(snp, chr, position, geneId, geneName, genePosition, fStat, pValue) {
            function Eqtl(snp, chr, position, geneId, geneName, pValue) {

                this.snp = snp;
                this.chr = chr;
                this.position = position;
                this.start = position;
                this.end = position + 1;
                this.geneId = geneId;
                this.geneName = geneName;
                //this.genePosition = genePosition;
                //this.fStat = fStat;
                this.pValue = pValue;

            }


            Eqtl.prototype.description = function () {
                return "<b>snp</b>:&nbsp" + this.snp +
                    "<br/><b>location</b>:&nbsp" + this.chr + ":" + formatNumber(this.position + 1) +
                    "<br/><b>gene</b>:&nbsp" + this.geneName +
                        //"<br/><b>fStat</b>:&nbsp" + this.fStat +
                    "<br/><b>pValue</b>:&nbsp" + this.pValue +
                    "<br/><b>mLogP</b>:&nbsp" + this.mLogP;
            }


            Block = function (startPos, size) {
                this.startPos = startPos;
                this.size = size;
            }

            ChrIdx = function (chr, blocks) {
                this.chr = chr;
                this.blocks = blocks;
            }

        });
    }

    var createEQTL = function (tokens) {
        var snp = tokens[0];
        var chr = tokens[1];
        var position = parseInt(tokens[2]) - 1;
        var geneId = tokens[3]
        var geneName = tokens[4];
        var genePosition = tokens[5];
        var fStat = parseFloat(tokens[6]);
        var pValue = parseFloat(tokens[7]);
        return new Eqtl(snp, chr, position, geneId, geneName, genePosition, fStat, pValue);
    };

    var createEqtlBinary = function (parser) {

        var snp = parser.getString();
        var chr = parser.getString();
        var position = parser.getInt();
        var geneId = parser.getString();
        var geneName = parser.getString();
        //var genePosition = -1;
        //var fStat = parser.getFloat();
        var pValue = parser.getFloat();
        //var qValue = parser.getFloat();
        //return new Eqtl(snp, chr, position, geneId, geneName, genePosition, fStat, pValue);
        return new Eqtl(snp, chr, position, geneId, geneName, pValue);
    };


    return igv;

})(igv || {});


/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 UC San Diego
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by jrobinso on 10/8/15.
 */

var igv = (function (igv) {


    /**
     * @param url - url to the webservice
     * @constructor
     */
    igv.GtexReader = function (config) {

        this.config = config;
        this.url = config.url;
        this.tissueName = config.tissueName;
        this.indexed = true;
    };

    //{
    //    "release": "v6",
    //    "singleTissueEqtl": [
    //    {
    //        "beta": -0.171944779728988,
    //        "chromosome": "3",
    //        "gencodeId": "ENSG00000168827.10",
    //        "geneSymbol": "GFM1",
    //        "pValue": 1.22963421134407e-09,
    //        "snpId": "rs3765025",
    //        "start": 158310846,
    //        "tissueName": "Thyroid"
    //    },
    //
    // http://vgtxportaltest.broadinstitute.org:9000/v6/singleTissueEqtlByLocation?tissueName=Thyroid&chromosome=3&start=158310650&end=158311650

        igv.GtexReader.prototype.readFeatures = function (chr, bpStart, bpEnd) {

            var self=this,
                queryChr = chr.startsWith("chr") ? chr.substr(3) : chr,
                queryStart = bpStart,
                queryEnd = bpEnd,
                queryURL = this.url + "?chromosome=" + queryChr + "&start=" + queryStart + "&end=" + queryEnd +
                    "&tissueName=" + this.tissueName;

            return new Promise(function (fulfill, reject) {

                igvxhr.loadJson(queryURL, {
                    withCredentials: self.config.withCredentials
                }).then(function (json) {

                    var variants;

                    if (json && json.singleTissueEqtl) {
                        //variants = json.variants;
                        //variants.sort(function (a, b) {
                        //    return a.POS - b.POS;
                        //});
                        //source.cache = new FeatureCache(chr, queryStart, queryEnd, variants);

                        json.singleTissueEqtl.forEach(function (eqtl) {
                            eqtl.chr = "chr" + eqtl.chromosome;
                            eqtl.position = eqtl.start;
                            eqtl.start = eqtl.start - 1;
                            eqtl.snp = eqtl.snpId;
                            eqtl.geneName = eqtl.geneSymbol;
                            eqtl.geneId = eqtl.gencodeId;
                            eqtl.end = eqtl.start;
                        });

                        fulfill(json.singleTissueEqtl);
                    }
                    else {
                        fulfill(null);
                    }

                }).catch(reject);

            });
        }


    return igv;
})
(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// Experimental class for fetching features from an mpg webservice.
// http://immvar.broadinstitute.org:3000/load_data?chromosome=&start=&end=&categories=

var igv = (function (igv) {

    /**
     * @param url - url to the webservice
     * @constructor
     */
    igv.ImmVarReader = function (config) {

        this.config = config;
        this.url = config.url;
        this.cellConditionId = config.cellConditionId;
        this.valueThreshold = config.valueThreshold ? config.valueThreshold : 5E-2;

    };

    igv.ImmVarReader.prototype.readFeatures = function (queryChr, queryStart, queryEnd) {

        var self = this,
            queryURL = this.url + "?chromosome=" + queryChr + "&start=" + queryStart + "&end=" + queryEnd +
                "&cell_condition_id=" + this.cellConditionId;

        return new Promise(function (fulfill, reject) {
            igvxhr.loadJson(queryURL, {
                withCredentials: self.config.withCredentials
            }).then(function (json) {

                if (json) {
                    //variants = json.variants;
                    //variants.sort(function (a, b) {
                    //    return a.POS - b.POS;
                    //});
                    //source.cache = new FeatureCache(chr, queryStart, queryEnd, variants);

                    json.eqtls.forEach(function (eqtl) {
                        eqtl.chr = eqtl.chromosome;
                        eqtl.start = eqtl.position;
                        eqtl.end = eqtl.position + 1;
                    });

                    fulfill(json.eqtls);
                }
                else {
                    fulfill(null);
                }

            }).catch(function (error) {
                reject(error);
            });

        });
    }


    return igv;
})
(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// Simple variant track for mpg prototype


var igv = (function (igv) {

    const DEFAULT_POPOVER_WINDOW = 100000000;

    igv.GWASTrack = function (config) {
        this.config = config;
        this.url = config.url;
        this.name = config.name;
        this.trait = config.trait;
        this.height = config.height || 100;   // The preferred height
        this.minLogP = config.minLogP || 0;
        this.maxLogP = config.maxLogP || 15;
        this.background = config.background;    // No default
        this.divider = config.divider || "rgb(225,225,225)";
        this.dotSize = config.dotSize || 4;
        this.popoverWindow = (config.popoverWindow === undefined ? DEFAULT_POPOVER_WINDOW : config.popoverWindow);

        this.description = config.description;  // might be null
        this.proxy = config.proxy;   // might be null

        this.portalURL = config.portalURL ? config.portalURL : window.location.origin;
        this.variantURL = config.variantURL || "http://www.type2diabetesgenetics.org/variant/variantInfo/";
        this.traitURL = config.traitURL || "http://www.type2diabetesgenetics.org/trait/traitInfo/";

        var cs = config.colorScale || {
                thresholds: [5e-8, 5e-4, 0.5],
                colors: ["rgb(255,50,50)", "rgb(251,100,100)", "rgb(251,170,170)", "rgb(227,238,249)"]
            };

        this.pvalue = config.pvalue ? config.pvalue : "PVALUE";

        this.colorScale = new igv.BinnedColorScale(cs);

        // An obvious hack -- the source should be passed in as an arbument
        if (config.format && ("gtexGWAS" === config.format)) {
            this.featureSource = new igv.FeatureSource(config);
        } else {
            this.featureSource = new igv.T2DVariantSource(config);
        }

    }


    igv.GWASTrack.prototype.getFeatures = function (chr, bpStart, bpEnd) {
       return this.featureSource.getFeatures(chr, bpStart, bpEnd);
    }


    igv.GWASTrack.prototype.draw = function (options) {

        var track = this,
            featureList = options.features,
            ctx = options.context,
            bpPerPixel = options.bpPerPixel,
            bpStart = options.bpStart,
            pixelWidth = options.pixelWidth,
            pixelHeight = options.pixelHeight,
            bpEnd = bpStart + pixelWidth * bpPerPixel + 1,
            yScale = (track.maxLogP - track.minLogP) / pixelHeight,
            enablePopover = (bpEnd - bpStart) < DEFAULT_POPOVER_WINDOW;

        if (enablePopover) {
            this.po = [];
        }
        else {
            this.po = undefined;
        }

        if (this.background) igv.graphics.fillRect(ctx, 0, 0, pixelWidth, pixelHeight, {'fillStyle': this.background});
        igv.graphics.strokeLine(ctx, 0, pixelHeight - 1, pixelWidth, pixelHeight - 1, {'strokeStyle': this.divider});

        var variant, pos, len, xScale, px, px1, pw, py, color, pvalue, val;

        if (featureList) {
            len = featureList.length;

            for (var i = 0; i < len; i++) {

                variant = featureList[i];

                pos = variant.start;     // TODO fixme

                if (pos < bpStart) continue;
                if (pos > bpEnd) break;

                pvalue = variant.pvalue || variant[track.pvalue];
                if (!pvalue) continue;

                color = track.colorScale.getColor(pvalue);
                val = -Math.log(pvalue) / 2.302585092994046;

                xScale = bpPerPixel;

                px = Math.round((pos - bpStart) / xScale);

                py = Math.max(track.dotSize, pixelHeight - Math.round((val - track.minLogP) / yScale));

                if (color) igv.graphics.setProperties(ctx, {fillStyle: color, strokeStyle: "black"});

                igv.graphics.fillCircle(ctx, px, py, track.dotSize);
                //canvas.strokeCircle(px, py, radius);

                if (enablePopover) track.po.push({x: px, y: py, feature: variant});

            }
        }

    };


    igv.GWASTrack.prototype.paintAxis = function (ctx, pixelWidth, pixelHeight) {

        var track = this,
            yScale = (track.maxLogP - track.minLogP) / pixelHeight;

        var font = {
            'font': 'normal 10px Arial',
            'textAlign': 'right',
            'strokeStyle': "black"
        };

        igv.graphics.fillRect(ctx, 0, 0, pixelWidth, pixelHeight, {'fillStyle': "rgb(255, 255, 255)"});

        for (var p = 2; p < track.maxLogP; p += 2) {
            var yp = pixelHeight - Math.round((p - track.minLogP) / yScale);
            // TODO: Dashes may not actually line up with correct scale. Ask Jim about this
            igv.graphics.strokeLine(ctx, 45, yp - 2, 50, yp - 2, font); // Offset dashes up by 2 pixel
            igv.graphics.fillText(ctx, p, 44, yp + 2, font); // Offset numbers down by 2 pixels;
        }


        font['textAlign'] = 'center';


        igv.graphics.fillText(ctx, "-log10(pvalue)", pixelWidth / 2, pixelHeight / 2, font, {rotate: {angle: -90}});


    };


    igv.GWASTrack.prototype.popupData = function (genomicLocation, xOffset, yOffset) {

        var i,
            len,
            p,
            dbSnp,
            url,
            data = [],
            chr,
            pos,
            pvalue;

        if (this.po) {
            for (i = 0, len = this.po.length; i < len; i++) {
                p = this.po[i];

                if (Math.abs(xOffset - p.x) < this.dotSize && Math.abs(yOffset - p.y) <= this.dotSize) {

                    chr = p.feature.CHROM || p.feature.chr;   // TODO fixme
                    pos = p.feature.POS || p.feature.start;   // TODO fixme
                    pvalue = p.feature[this.pvalue] || p.feature.pvalue;
                    dbSnp = p.feature.DBSNP_ID;


                    if (dbSnp) {
                        url = this.variantURL.startsWith("http") ? this.variantURL : this.portalURL + "/" + this.variantURL;
                        data.push("<a target='_blank' href='" + url + (url.endsWith("/") ? "" : "/") + dbSnp + "' >" + dbSnp + "</a>");
                    }
                    data.push(chr + ":" + pos.toString());
                    data.push({name: 'p-value', value: pvalue});

                    if (p.feature.ZSCORE) {
                        data.push({name: 'z-score', value: p.feature.ZSCORE});
                    }

                    if (dbSnp) {
                        url = this.traitURL.startsWith("http") ? this.traitURL : this.portalURL + "/" + this.traitURL;
                        data.push("<a target='_blank' href='" + url + (url.endsWith("/") ? "" : "/") + dbSnp + "'>" +
                        "see all available statistics for this variant</a>");
                    }

                    if (i < len - 1) {
                        data.push("<p/>");
                    }
                }
            }
        } else {
            data.push("Popover not available at this resolution.")

        }
        return data;
    };


    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// Experimental class for fetching features from an mpg webservice.


var igv = (function (igv) {


    const VARIANT = "VARIANT";
    const TRAIT = "TRAIT";
    /**
     * @param url - url to the webservice
     * @constructor
     */
    igv.T2DVariantSource = function (config) {

        this.config = config;
        this.url = config.url;
        this.trait = config.trait;
        this.dataset = config.dataset;
        this.pvalue = config.pvalue;

        // Hack for old service that is missing CORS headers
        // if (config.dataset === undefined && config.proxy === undefined) {
        //     config.proxy = "//data.broadinstitute.org/igvdata/t2d/postJson.php";
        // }

        if (config.valueThreshold === undefined) {
            config.valueThreshold = 5E-2;
        }

        if (config.dataset === undefined) {
            this.queryJson = config.queryJson || queryJsonV1;
            this.jsonToVariants = config.jsonToVariants || jsonToVariantsV1;
        } else {
            this.queryJson = config.queryJson || queryJsonV2;
            this.jsonToVariants = config.jsonToVariants || jsonToVariantsV2;
        }

    };

    /**
     * Required function fo all data source objects.  Fetches features for the
     * range requested and passes them on to the success function.  Usually this is
     * a function that renders the features on the canvas
     *
     * @param queryChr
     * @param bpStart
     * @param bpEnd
     */

    igv.T2DVariantSource.prototype.getFeatures = function (chr, bpStart, bpEnd) {

        var self = this;
        return new Promise(function (fulfill, reject) {

            if (self.cache && self.cache.chr === chr && self.cache.end > bpEnd && self.cache.start < bpStart) {
                fulfill(self.cache.featuresBetween(bpStart, bpEnd));
            }

            else {

                // Get a minimum 10mb window around the requested locus
                var window = Math.max(bpEnd - bpStart, 10000000) / 2,
                    center = (bpEnd + bpStart) / 2,
                    queryChr = (chr.startsWith("chr") ? chr.substring(3) : chr), // Webservice uses "1,2,3..." convention
                    queryStart = Math.max(0, center - window),
                    queryEnd = center + window,
                    queryURL = self.config.proxy ? self.config.proxy : self.url,
                    body = self.queryJson(queryChr, queryStart, queryEnd, self.config);

                igvxhr.loadJson(queryURL, {
                    sendData: body,
                    withCredentials: self.config.withCredentials

                }).then(function (json) {
                    var variants;

                    if (json) {

                        if (json.error_code) {
                            //alert("Error querying trait " + self.trait + "  (error_code=" + json.error_code + ")");
                            igv.presentAlert("Error querying trait " + self.trait + "  (error_code=" + json.error_code + ")");
                            fulfill(null);
                        }
                        else {
                            variants = self.jsonToVariants(json, self.config);

                            variants.sort(function (a, b) {
                                return a.POS - b.POS;
                            });

                            // TODO -- extract pvalue

                            self.cache = new FeatureCache(chr, queryStart, queryEnd, variants);

                            fulfill(variants);
                        }
                    }
                    else {
                        fulfill(null);
                    }
                }).catch(reject);

            }

        });
    }


    // Experimental linear index feature cache.
    var FeatureCache = function (chr, start, end, features) {

        var i, bin, lastBin;

        this.chr = chr;
        this.start = start;
        this.end = end;
        this.binSize = (end - start) / 100;
        this.binIndeces = [0];
        this.features = features;

        lastBin = 0;
        for (i = 0; i < features.length; i++) {
            bin = Math.max(0, Math.floor((features[i].POS - this.start) / this.binSize));
            if (bin > lastBin) {
                this.binIndeces.push(i);
                lastBin = bin;
            }
        }
    }

    FeatureCache.prototype.featuresBetween = function (start, end) {


        var startBin = Math.max(0, Math.min(Math.floor((start - this.start) / this.binSize) - 1, this.binIndeces.length - 1)),
            endBin = Math.max(0, Math.min(Math.floor((end - this.start) / this.binSize), this.binIndeces.length - 1)),
            startIdx = this.binIndeces[startBin],
            endIdx = this.binIndeces[endBin];

        return this.features; //.slice(startIdx, endIdx);

    }


    //
    //
    /**
     * Default json -> variant converter function.  Can be overriden.
     * Convert webservice json to an array of variants
     *
     * @param json
     * @param config
     * @returns {Array|*}
     */
    function jsonToVariantsV2(json, config) {

        variants = [];
        json.variants.forEach(function (record) {

            var variant = {};
            record.forEach(function (object) {
                for (var property in object) {
                    if (object.hasOwnProperty(property)) {
                        if ("POS" === property) {
                            variant.start = object[property] - 1;
                        }
                        variant[property] = object[property];

                    }
                }

            });

            // "unwind" the pvalue, then null the nested array to save memory
            variant.pvalue = variant[config.pvalue][config.dataset][config.trait];
            variant[config.pvalue] = undefined;

            variants.push(variant);
        })
        return variants;
    }


    function queryJsonV2(queryChr, queryStart, queryEnd, config) {
        var phenotype = config.trait,
            pvalue = config.pvalue,
            dataset = config.dataset,
            properties = {
                "cproperty": ["VAR_ID", "DBSNP_ID", "CHROM", "POS"],
                "orderBy": ["CHROM"],
                "dproperty": {},
                "pproperty": JSON.parse('{"' + pvalue + '": {"' + dataset + '": ["' + phenotype + '"]}}')
            },

            filters =
                [
                    {
                        "dataset_id": "x",
                        "phenotype": "x",
                        "operand": "CHROM",
                        "operator": "EQ",
                        "value": queryChr,
                        "operand_type": "STRING"
                    },
                    {
                        "dataset_id": "x",
                        "phenotype": "x",
                        "operand": "POS",
                        "operator": "GTE",
                        "value": queryStart,
                        "operand_type": "INTEGER"
                    },
                    {
                        "dataset_id": "x",
                        "phenotype": "x",
                        "operand": "POS",
                        "operator": "LTE",
                        "value": queryEnd,
                        "operand_type": "INTEGER"
                    },
                    {
                        "dataset_id": dataset,
                        "phenotype": phenotype,
                        "operand": pvalue,
                        "operator": "LT",
                        "value": config.valueThreshold,
                        "operand_type": "FLOAT"
                    }
                ],
            data = {
                "passback": "x",
                "entity": "variant",
                "properties": properties,
                "filters": filters
            };

        return JSON.stringify(data);
    }


    function queryJsonV1(queryChr, queryStart, queryEnd, config) {

        var type = config.url.contains("variant") ? VARIANT : TRAIT,
            pvalue = config.pvalue ? config.pvalue : "PVALUE",

            filters =
                [
                    {"operand": "CHROM", "operator": "EQ", "value": queryChr, "filter_type": "STRING"},
                    {"operand": "POS", "operator": "GT", "value": queryStart, "filter_type": "FLOAT"},
                    {"operand": "POS", "operator": "LT", "value": queryEnd, "filter_type": "FLOAT"},
                    {"operand": pvalue, "operator": "LTE", "value": config.valueThreshold, "filter_type": "FLOAT"}
                ],
            columns = type === TRAIT ?
                ["CHROM", "POS", "DBSNP_ID", "PVALUE", "ZSCORE"] :
                ["CHROM", "POS", pvalue, "DBSNP_ID"],
            data = {
                "user_group": "ui",
                "filters": filters,
                "columns": columns
            };


        if (type === TRAIT) data.trait = config.trait;

        return config.proxy ? "url=" + config.url + "&data=" + JSON.stringify(data) : JSON.stringify(data);

    }

    function jsonToVariantsV1(json, config) {

        json.variants.forEach(function (variant) {
            variant.chr = variant.CHROM;
            variant.start = variant.POS - 1;
        })
        return json.variants;
    }


    return igv;
})(igv || {});


/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

//
// Chromosome ideogram
//

var igv = (function (igv) {

    igv.IdeoPanel = function (parentElement) {

        this.ideograms = {};

        // ideogram content
        this.contentDiv = $('<div class="igv-ideogram-content-div"></div>');
        $(parentElement).append(this.contentDiv[0]);

        var myself = this;
        this.contentDiv.click(function (e) {

            var xy,
                xPercentage,
                chr,
                chrLength,
                locusLength,
                chrCoveragePercentage,
                locus;

            xy = igv.translateMouseCoordinates(e, myself.contentDiv);
            xPercentage = xy.x / myself.contentDiv.width();

            locusLength = igv.browser.trackViewportWidthBP();
            chr = igv.browser.genome.getChromosome(igv.browser.referenceFrame.chr);
            chrLength = chr.bpLength;
            chrCoveragePercentage = locusLength / chrLength;

            if (xPercentage - (chrCoveragePercentage/2.0) < 0) {
                xPercentage = chrCoveragePercentage/2.0;
                //return;
            }

            if (xPercentage + (chrCoveragePercentage/2.0) > 1.0) {
                xPercentage = 1.0 - chrCoveragePercentage/2.0;
                //return;
            }

            locus = igv.browser.referenceFrame.chr + ":" + igv.numberFormatter(1 + Math.floor((xPercentage - (chrCoveragePercentage/2.0)) * chrLength)) + "-" + igv.numberFormatter(Math.floor((xPercentage + (chrCoveragePercentage/2.0)) * chrLength));
            //console.log("chr length " + igv.numberFormatter(chrLength) + " locus " + locus);

            igv.browser.search(locus, undefined);

        });

        this.canvas = $('<canvas class="igv-ideogram-canvas"></canvas>')[0];
        $(this.contentDiv).append(this.canvas);
        this.canvas.setAttribute('width', this.contentDiv.width());
        this.canvas.setAttribute('height', this.contentDiv.height());
        this.ctx = this.canvas.getContext("2d");

    };

    igv.IdeoPanel.prototype.resize = function () {

        this.canvas.setAttribute('width', this.contentDiv.width());
        this.canvas.setAttribute('height', this.contentDiv.height());

        this.ideograms = {};
        this.repaint();
    };

    igv.IdeoPanel.prototype.repaint = function () {

        try {
            var y,
                image,
                bufferCtx,
                chromosome,
                widthPercentage,
                xPercentage,
                width,
                widthBP,
                x,
                xBP,
                genome = igv.browser.genome,
                referenceFrame = igv.browser.referenceFrame,
                stainColors = [];

            this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

            if (!(genome && referenceFrame && genome.getChromosome(referenceFrame.chr))) {
                return;
            }

            image = this.ideograms[igv.browser.referenceFrame.chr];

            if (!image) {

                image = document.createElement('canvas');
                image.width = this.canvas.width;
                image.height = 13;

                bufferCtx = image.getContext('2d');

                drawIdeogram(bufferCtx, this.canvas.width, image.height);

                this.ideograms[igv.browser.referenceFrame.chr] = image;
            }

            y = (this.canvas.height - image.height) / 2.0;
            this.ctx.drawImage(image, 0, y);

            // Draw red box
            this.ctx.save();

            chromosome = igv.browser.genome.getChromosome(igv.browser.referenceFrame.chr);

            widthBP = Math.floor(igv.browser.trackViewportWidthBP());
                xBP = igv.browser.referenceFrame.start;

            if (widthBP < chromosome.bpLength) {

                widthPercentage = widthBP/chromosome.bpLength;
                    xPercentage =     xBP/chromosome.bpLength;

                x =     Math.floor(    xPercentage * this.canvas.width);
                width = Math.floor(widthPercentage * this.canvas.width);

                //console.log("canvas end " + this.canvas.width + " xEnd " + (x + width));

                x = Math.max(0, x);
                x = Math.min(this.canvas.width - width, x);

                this.ctx.strokeStyle = "red";
                this.ctx.lineWidth = 2;
                this.ctx.strokeRect(x, y, width, image.height + this.ctx.lineWidth - 1);
                this.ctx.restore();
            }

            //this.chromosomeNameLabel.innerHTML = referenceFrame.chr;

        } catch (e) {
            console.log("Error painting ideogram: " + e.message);
        }

        function drawIdeogram(bufferCtx, ideogramWidth, ideogramHeight) {

            var ideogramTop = 0;

            if (!genome) return;

            var cytobands = genome.getCytobands(referenceFrame.chr);

            if (cytobands) {

                var center = (ideogramTop + ideogramHeight / 2);

                var xC = [];
                var yC = [];

                var len = cytobands.length;
                if (len == 0) return;

                var chrLength = cytobands[len - 1].end;

                var scale = ideogramWidth / chrLength;

                var lastPX = -1;
                for (var i = 0; i < cytobands.length; i++) {
                    var cytoband = cytobands[i];

                    var start = scale * cytoband.start;
                    var end = scale * cytoband.end;
                    if (end > lastPX) {


                        if (cytoband.type == 'c') { // centermere: "acen"

                            if (cytoband.name.charAt(0) == 'p') {
                                xC[0] = start;
                                yC[0] = ideogramHeight + ideogramTop;
                                xC[1] = start;
                                yC[1] = ideogramTop;
                                xC[2] = end;
                                yC[2] = center;
                            } else {
                                xC[0] = end;
                                yC[0] = ideogramHeight + ideogramTop;
                                xC[1] = end;
                                yC[1] = ideogramTop;
                                xC[2] = start;
                                yC[2] = center;
                            }
                            bufferCtx.fillStyle = "rgb(150, 0, 0)"; //g2D.setColor(Color.RED.darker());
                            bufferCtx.strokeStyle = "rgb(150, 0, 0)"; //g2D.setColor(Color.RED.darker());
                            bufferCtx.polygon(xC, yC, 1, 0);
                            // g2D.fillPolygon(xC, yC, 3);
                        } else {

                            bufferCtx.fillStyle = getCytobandColor(cytoband); //g2D.setColor(getCytobandColor(cytoband));
                            bufferCtx.fillRect(start, ideogramTop, (end - start), ideogramHeight);
                            // context.fillStyle = "Black"; //g2D.setColor(Color.BLACK);
                            // context.strokeRect(start, y, (end - start), height);
                        }
                    }
                }
            }
            bufferCtx.strokeStyle = "black";
            bufferCtx.roundRect(0, ideogramTop, ideogramWidth, ideogramHeight, ideogramHeight / 2, 0, 1);
            //context.strokeRect(margin, y, trackWidth-2*margin, height);
            lastPX = end;


        }

        function getCytobandColor(data) {
            if (data.type == 'c') { // centermere: "acen"
                return "rgb(150, 10, 10)"

            } else {
                var stain = data.stain; // + 4;

                var shade = 230;
                if (data.type == 'p') {
                    shade = Math.floor(230 - stain / 100.0 * 230);
                }
                var c = stainColors[shade];
                if (c == null) {
                    c = "rgb(" + shade + "," + shade + "," + shade + ")";
                    stainColors[shade] = c;
                }
                return c;

            }
        }

    };

    return igv;
})
(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of ctx software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and ctx permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


// Collection of helper functions for canvas rendering.  The "ctx" paramter in these functions is a canvas 2d context.
//
// Example usage
//
//    igv.graphics.strokeLine(context, 0, 0, 10, 10);
//

var igv = (function (igv) {


    var debug = false;

    var log = function (msg) {
        if (debug) {
            var d = new Date();
            var time = d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds();
            if (typeof copy != "undefined") {
                copy(msg);
            }
            if (typeof console != "undefined") {
                console.log("igv-canvas: " + time + " " + msg);
            }

        }
    };


    igv.graphics = {


        setProperties: function (ctx, properties) {

            for (var key in properties) {
                if (properties.hasOwnProperty(key)) {
                    var value = properties[key];
                    ctx[key] = value;
                }
            }
        },

        strokeLine: function (ctx, x1, y1, x2, y2, properties) {

            x1 = Math.floor(x1) + 0.5;
            y1 = Math.floor(y1) + 0.5;
            x2 = Math.floor(x2) + 0.5;
            y2 = Math.floor(y2) + 0.5;

            log("stroke line, prop: " + properties);

            ctx.save();
            if (properties) igv.graphics.setProperties(ctx, properties);

            ctx.beginPath();
            ctx.moveTo(x1, y1);
            ctx.lineTo(x2, y2);
            ctx.stroke();
            ctx.restore();
        },

        fillRect: function (ctx, x, y, w, h, properties) {

            var c;
            x = Math.round(x);
            y = Math.round(y);

            if (properties) {
                ctx.save();
                igv.graphics.setProperties(ctx, properties);
            }

            ctx.fillRect(x, y, w, h);

            if (properties) ctx.restore();
        },

        fillPolygon: function (ctx, x, y, properties) {
            ctx.save();
            if (properties)   igv.graphics.setProperties(ctx, properties);
            doPath(ctx, x, y);
            ctx.fill();
            ctx.restore();
        },

        strokePolygon: function (ctx, x, y, properties) {
            ctx.save();
            if (properties)   igv.graphics.setProperties(ctx, properties);
            doPath(ctx, x, y);
            ctx.stroke();
            ctx.restore();
        },

        fillText: function (ctx, text, x, y, properties, transforms) {

            if (properties) {
                ctx.save();
                igv.graphics.setProperties(ctx, properties);
            }


            ctx.save();

            ctx.translate(x, y);
            if (transforms) {

                for (var transform in transforms) {
                    var value = transforms[transform];

                    // TODO: Add error checking for robustness
                    if (transform == 'translate') {
                        ctx.translate(value['x'], value['y']);
                    }
                    if (transform == 'rotate') {
                        ctx.rotate(value['angle'] * Math.PI / 180);
                    }
                }

            }

            ctx.fillText(text, 0, 0);
            ctx.restore();

            if (properties) ctx.restore();

        },

        strokeText: function (ctx, text, x, y, properties, transforms) {


            ctx.save();
            if (properties) {
                igv.graphics.setProperties(ctx, properties);
            }


            ctx.translate(x, y);
            if (transforms) {

                for (var transform in transforms) {
                    var value = transforms[transform];

                    // TODO: Add error checking for robustness
                    if (transform == 'translate') {
                        ctx.translate(value['x'], value['y']);
                    }
                    if (transform == 'rotate') {
                        ctx.rotate(value['angle'] * Math.PI / 180);
                    }
                }
            }


            ctx.strokeText(text, 0, 0);
            ctx.restore();

        },

        strokeCircle: function (ctx, x, y, radius) {

            ctx.beginPath();
            ctx.arc(x, y, radius, 0, 2 * Math.PI);
            ctx.stroke();
        },

        fillCircle: function (ctx, x, y, radius) {

            ctx.beginPath();
            ctx.arc(x, y, radius, 0, 2 * Math.PI);
            ctx.fill();
        },

        drawArrowhead: function (ctx, x, y, size, lineWidth) {

            ctx.save();
            if (!size) {
                size = 5;
            }
            if (lineWidth) {
                ctx.lineWidth = lineWidth;
            }
            ctx.beginPath();
            ctx.moveTo(x, y - size / 2);
            ctx.lineTo(x, y + size / 2);
            ctx.lineTo(x + size, y);
            ctx.lineTo(x, y - size / 2);
            ctx.closePath();
            ctx.fill();
            ctx.restore();
        },

        dashedLine: function (ctx, x1, y1, x2, y2, dashLen, properties) {
            ctx.save();
            x1 = Math.round(x1);
            y1 = Math.round(y1);
            x2 = Math.round(x2);
            y2 = Math.round(y2);
            dashLen = Math.round(dashLen);
            log("dashedLine");
            if (properties) igv.graphics.setProperties(ctx, properties);

            if (dashLen == undefined) dashLen = 2;
            ctx.moveTo(x1, y1);

            var dX = x2 - x1;
            var dY = y2 - y1;
            var dashes = Math.floor(Math.sqrt(dX * dX + dY * dY) / dashLen);
            var dashX = dX / dashes;
            var dashY = dY / dashes;

            var q = 0;
            while (q++ < dashes) {
                x1 += dashX;
                y1 += dashY;
                ctx[q % 2 == 0 ? 'moveTo' : 'lineTo'](x1, y1);
            }
            ctx[q % 2 == 0 ? 'moveTo' : 'lineTo'](x2, y2);

            ctx.restore();
        },


    }

    function doPath(ctx, x, y) {


        var i, len = x.length;
        for (i = 0; i < len; i++) {
            x[i] = Math.round(x[i]);
            y[i] = Math.round(y[i]);
        }

        ctx.beginPath();
        ctx.moveTo(x[0], y[0]);
        for (i = 1; i < len; i++) {
            ctx.lineTo(x[i], y[i]);
        }
        ctx.closePath();
    }

    return igv;
})(igv || {});



/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by turner on 2/24/14.
 */
var igv = (function (igv) {

    igv.hex2Color = function (hex) {

        var cooked = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);

        if (null === cooked) {
            return undefined;
        }

        return "rgb(" + parseInt(cooked[1], 16) + "," + parseInt(cooked[2], 16) + "," + parseInt(cooked[3], 16) + ")";
    };

    igv.rgbaColor = function (r, g, b, a) {

        r = clamp(r, 0, 255);
        g = clamp(g, 0, 255);
        b = clamp(b, 0, 255);
        a = clamp(a, 0.0, 1.0);

        return "rgba(" + r + "," + g + "," + b + "," + a + ")";
    };

    igv.rgbColor = function (r, g, b) {

        r = clamp(r, 0, 255);
        g = clamp(g, 0, 255);
        b = clamp(b, 0, 255);

        return "rgb(" + r + "," + g + "," + b + ")";
    };

    igv.addAlphaToRGB = function (rgbString, alpha) {

        if (rgbString.startsWith("rgb")) {
            return rgbString.replace("rgb", "rgba").replace(")", ", " + alpha + ")");
        } else {
            console.log(rgbString + " is not an rgb style string");
            return rgbString;
        }

    }

    igv.greyScale = function (value) {

        var grey = clamp(value, 0, 255);

        return "rgb(" + grey + "," + grey + "," + grey + ")";
    };

    igv.randomGrey = function (min, max) {

        min = clamp(min, 0, 255);
        max = clamp(max, 0, 255);

        var g = Math.round(igv.random(min, max)).toString(10);

        return "rgb(" + g + "," + g + "," + g + ")";
    };

    igv.randomRGB = function (min, max) {

        min = clamp(min, 0, 255);
        max = clamp(max, 0, 255);

        var r = Math.round(igv.random(min, max)).toString(10);
        var g = Math.round(igv.random(min, max)).toString(10);
        var b = Math.round(igv.random(min, max)).toString(10);

        return "rgb(" + r + "," + g + "," + b + ")";
    };

    igv.nucleotideColorComponents = {
        "A": [0, 200, 0],
        "C": [0, 0, 200],
        "T": [255, 0, 0],
        "G": [209, 113, 5],
        "a": [0, 200, 0],
        "c": [0, 0, 200],
        "t": [255, 0, 0],
        "g": [209, 113, 5]
    }

    igv.nucleotideColors = {
        "A": "rgb(  0, 200,   0)",
        "C": "rgb(  0,   0, 200)",
        "T": "rgb(255,   0,   0)",
        "G": "rgb(209, 113,   5)",
        "a": "rgb(  0, 200,   0)",
        "c": "rgb(  0,   0, 200)",
        "t": "rgb(255,   0,   0)",
        "g": "rgb(209, 113,   5)"
    };

    /**
     *
     * @param dest  RGB components as an array
     * @param src  RGB components as an array
     * @param alpha   alpha transparancy in the range 0-1
     * @returns {}
     */
    igv.getCompositeColor = function (dest, src, alpha) {

        var r = Math.floor(alpha * src[0] + (1 - alpha) * dest[0]),
            g = Math.floor(alpha * src[1] + (1 - alpha) * dest[1]),
            b = Math.floor(alpha * src[2] + (1 - alpha) * dest[2]);

        return "rgb(" + r + "," + g + "," + b + ")";

    }

    function clamp(value, min, max) {
        return Math.min(Math.max(value, min), max);
    };


    igv.createColorString = function (token) {
        if (token.includes(",")) {
            return token.startsWith("rgb") ? token : "rgb(" + token + ")";
        }
        else {
            return token;
        }
    }


    // Color scale objects.  Implement a single method,  getColor(value)

    /**
     *
     * @param thresholds - array of threshold values defining bin boundaries in ascending order
     * @param colors - array of colors for bins  (length == thresholds.length + 1)
     * @constructor
     */
    igv.BinnedColorScale = function (cs) {
        this.thresholds = cs.thresholds;
        this.colors = cs.colors;
    }

    igv.BinnedColorScale.prototype.getColor = function (value) {

        var i, len = this.thresholds.length;

        for (i = 0; i < len; i++) {
            if (value < this.thresholds[i]) {
                return this.colors[i];
            }
        }

        return this.colors[this.colors.length - 1];

    }

    /**
     *
     * @param scale - object with the following properties
     *           low
     *           lowR
     *           lowG
     *           lowB
     *           high
     *           highR
     *           highG
     *           highB
     *
     * @constructor
     */
    igv.GradientColorScale = function (scale) {

        this.scale = scale;
        this.lowColor = "rgb(" + scale.lowR + "," + scale.lowG + "," + scale.lowB + ")";
        this.highColor = "rgb(" + scale.highR + "," + scale.highG + "," + scale.highB + ")";
        this.diff = scale.high - scale.low;

    }

    igv.GradientColorScale.prototype.getColor = function (value) {

        var scale = this.scale, r, g, b, frac;

        if (value <= scale.low) return this.lowColor;
        else if (value >= scale.high) return this.highColor;

        frac = (value - scale.low) / this.diff;
        r = Math.floor(scale.lowR + frac * (scale.highR - scale.lowR));
        g = Math.floor(scale.lowG + frac * (scale.highG - scale.lowG));
        b = Math.floor(scale.lowB + frac * (scale.highB - scale.lowB));

        return "rgb(" + r + "," + g + "," + b + ")";
    }

    var colorPalettes = {
        Set1: ["rgb(228,26,28)", "rgb(55,126,184)", "rgb(77,175,74)", "rgb(166,86,40)",
            "rgb(152,78,163)", "rgb(255,127,0)", "rgb(247,129,191)", "rgb(153,153,153)",
            "rgb(255,255,51)"],
        Dark2: ["rgb(27,158,119)", "rgb(217,95,2)", "rgb(117,112,179)", "rgb(231,41,138)",
            "rgb(102,166,30)", "rgb(230,171,2)", "rgb(166,118,29)", "rgb(102,102,102)"],
        Set2: ["rgb(102, 194,165)", "rgb(252,141,98)", "rgb(141,160,203)", "rgb(231,138,195)",
            "rgb(166,216,84)", "rgb(255,217,47)", "rgb(229,196,148)", "rgb(179,179,179)"],
        Set3: ["rgb(141,211,199)", "rgb(255,255,179)", "rgb(190,186,218)", "rgb(251,128,114)",
            "rgb(128,177,211)", "rgb(253,180,98)", "rgb(179,222,105)", "rgb(252,205,229)",
            "rgb(217,217,217)", "rgb(188,128,189)", "rgb(204,235,197)", "rgb(255,237,111)"],
        Pastel1: ["rgb(251,180,174)", "rgb(179,205,227)", "rgb(204,235,197)", "rgb(222,203,228)",
            "rgb(254,217,166)", "rgb(255,255,204)", "rgb(229,216,189)", "rgb(253,218,236)"],
        Pastel2: ["rgb(173,226,207)", "rgb(253,205,172)", "rgb(203,213,232)", "rgb(244,202,228)",
            "rgb(230,245,201)", "rgb(255,242,174)", "rgb(243,225,206)"],
        Accent: ["rgb(127,201,127)", "rgb(190,174,212)", "rgb(253,192,134)", "rgb(255,255,153)",
            "rgb(56,108,176)", "rgb(240,2,127)", "rgb(191,91,23)"]
    }

    igv.PaletteColorTable = function (palette) {
        this.colors = colorPalettes[palette];
        if (!Array.isArray(this.colors)) this.colors = [];
        this.colorTable = {};
        this.nextIdx = 0;
        this.colorGenerator = new RColor();
    }

    igv.PaletteColorTable.prototype.getColor = function (key) {

        if (!this.colorTable.hasOwnProperty(key)) {
            if (this.nextIdx < this.colors.length) {
                this.colorTable[key] = this.colors[this.nextIdx];
            } else {
                this.colorTable[key] = this.colorGenerator.get();
            }
            this.nextIdx++;
        }
        return this.colorTable[key];
    }


    // Random color generator from https://github.com/sterlingwes/RandomColor/blob/master/rcolor.js
    // Free to use & distribute under the MIT license
    // Wes Johnson (@SterlingWes)
    //
    // inspired by http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/

    RColor = function () {
        this.hue = Math.random(),
            this.goldenRatio = 0.618033988749895;
        this.hexwidth = 2;
    }

    RColor.prototype.hsvToRgb = function (h, s, v) {
        var h_i = Math.floor(h * 6),
            f = h * 6 - h_i,
            p = v * (1 - s),
            q = v * (1 - f * s),
            t = v * (1 - (1 - f) * s),
            r = 255,
            g = 255,
            b = 255;
        switch (h_i) {
            case 0:
                r = v, g = t, b = p;
                break;
            case 1:
                r = q, g = v, b = p;
                break;
            case 2:
                r = p, g = v, b = t;
                break;
            case 3:
                r = p, g = q, b = v;
                break;
            case 4:
                r = t, g = p, b = v;
                break;
            case 5:
                r = v, g = p, b = q;
                break;
        }
        return [Math.floor(r * 256), Math.floor(g * 256), Math.floor(b * 256)];
    };

    RColor.prototype.padHex = function (str) {
        if (str.length > this.hexwidth) return str;
        return new Array(this.hexwidth - str.length + 1).join('0') + str;
    };

    RColor.prototype.get = function (saturation, value) {
        this.hue += this.goldenRatio;
        this.hue %= 1;
        if (typeof saturation !== "number")    saturation = 0.5;
        if (typeof value !== "number")        value = 0.95;
        var rgb = this.hsvToRgb(this.hue, saturation, value);

        return "#" + this.padHex(rgb[0].toString(16))
            + this.padHex(rgb[1].toString(16))
            + this.padHex(rgb[2].toString(16));

    };


    return igv;

})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    var igvjs_version = "beta";
    igv.version = igvjs_version;

    /**
     * Create an igv.browser instance.  This object defines the public API for interacting with the genome browser.
     *
     * @param parentDiv - DOM tree root
     * @param config - configuration options.
     *
     */
    igv.createBrowser = function (parentDiv, config) {

        var igvLogo,
            contentDiv,
            headerDiv,
            trackContainerDiv,
            browser,
            rootDiv,
            controlDiv,
            trackOrder = 1;

        if (igv.browser) {
            //console.log("Attempt to create 2 browsers.");
            igv.removeBrowser();
        }

        if (!config) config = {};

        setDefaults(config);

        oauth.google.apiKey = config.apiKey;
        oauth.google.access_token = config.oauthToken;

        // Deal with several legacy genome definition options
        if (config.genome) {
            config.reference = expandGenome(config.genome);
        }
        else if (config.fastaURL) {   // legacy property
            config.reference = {
                fastaURL: config.fastaURL,
                cytobandURL: config.cytobandURL
            }
        }
        else if (config.reference && config.reference.id !== undefined && config.reference.fastaURL === undefined) {
            config.reference = expandGenome(config.reference.id);
        }

        if (!(config.reference && config.reference.fastaURL)) {
            //alert("Fatal error:  reference must be defined");
            igv.presentAlert("Fatal error:  reference must be defined");
            throw new Error("Fatal error:  reference must be defined");
        }


        //Set order of tracks, otherwise they will be ordered randomly as each completes its async load
        if (config.tracks) {
            config.tracks.forEach(function (track) {
                if (track.order === undefined) {
                    track.order = trackOrder++;
                }
            });
        }

        trackContainerDiv = $('<div class="igv-track-container-div">')[0];
        browser = new igv.Browser(config, trackContainerDiv);
        rootDiv = browser.div;

        $(document).mousedown(function (e) {
            //console.log("browser.isMouseDown = true");
            browser.isMouseDown = true;
        });

        $(document).mouseup(function (e) {

            //console.log("browser.isMouseDown = undefined");
            browser.isMouseDown = undefined;

            if (browser.dragTrackView) {
                $(browser.dragTrackView.igvTrackDragScrim).hide();
            }

            browser.dragTrackView = undefined;

        });

        $(document).click(function (e) {
            var target = e.target;
            if (!igv.browser.div.contains(target)) {
                // We've clicked outside the IGV div.  Close any open popovers.
                igv.popover.hide();
            }
        });


        // DOM
        $(parentDiv).append($(rootDiv));

        // Create controls.  This can be customized by passing in a function, which should return a div containing the
        // controls

        if (config.showCommandBar !== false && config.showControls !== false) {
            controlDiv = config.createControls ? config.createControls(browser, config) : createStandardControls(browser, config);
            $(rootDiv).append($(controlDiv));
        }

        contentDiv = $('<div class="igv-content-div">')[0];
        $(rootDiv).append(contentDiv);

        headerDiv = $('<div>')[0];
        $(contentDiv).append(headerDiv);

        $(contentDiv).append(trackContainerDiv);

        // user feedback
        browser.userFeedback = new igv.UserFeedback($(contentDiv));
        browser.userFeedback.hide();

        // Popover object -- singleton shared by all components
        igv.popover = new igv.Popover($(contentDiv), "igv-popover");

        // ColorPicker object -- singleton shared by all components
        igv.colorPicker = new igv.ColorPicker($(rootDiv), config.palette, "igv-color-picker");
        igv.colorPicker.hide();

        // alert object -- singleton shared by all components
        igv.alert = new igv.AlertDialog($(rootDiv), "igv-alert");
        igv.alert.hide();

        // Dialog object -- singleton shared by all components
        igv.dialog = new igv.Dialog($(rootDiv), igv.Dialog.dialogConstructor, "igv-dialog");
        igv.dialog.hide();

        // Data Range Dialog object -- singleton shared by all components
        igv.dataRangeDialog = new igv.DataRangeDialog($(rootDiv), "igv-data-range-dialog");
        igv.dataRangeDialog.hide();

        if (!config.showNavigation) {
            igvLogo = $('<div class="igv-logo-nonav">');
            $(headerDiv).append(igvLogo[0]);
        }

        // ideogram
        if (config.hideIdeogram && true === config.hideIdeogram) {
            // do nothing
        } else {
            browser.ideoPanel = new igv.IdeoPanel(headerDiv);
            browser.ideoPanel.resize();
        }

        // phone home -- counts launches.  Count is anonymous, needed for our continued funding.  Please don't delete
        phoneHome();


        igv.loadGenome(config.reference).then(function (genome) {


            genome.id = config.reference.genomeId;
            browser.genome = genome;

            if (config.showRuler) {
                browser.addTrack(new igv.RulerTrack());
            }

            // viewport width -- must get this after adding ruler track
            var viewportWidth = browser.trackViewportWidth();
            if (viewportWidth === 0) viewportWidth = 500;


            // Set inital locus
            var firstChrName = browser.genome.chromosomeNames[0],
                firstChr = browser.genome.chromosomes[firstChrName];

            browser.referenceFrame = new igv.ReferenceFrame(firstChrName, 0, firstChr.bpLength / viewportWidth);
            browser.controlPanelWidth = 50;

            browser.updateLocusSearch(browser.referenceFrame);

            if (browser.ideoPanel) browser.ideoPanel.repaint();
            if (browser.karyoPanel) browser.karyoPanel.resize();

            // If an initial locus is specified go there first, then load tracks.  This avoids loading track data at
            // a default location then moving
            if (browser.initialLocus || config.locus) {

                var locus = browser.initialLocus ? browser.initialLocus : config.locus;

                igv.startSpinnerAtParentElement(parentDiv);
                browser.search(locus, function () {

                    igv.stopSpinnerAtParentElement(parentDiv);
                    var refFrame = browser.referenceFrame,
                        start = refFrame.start,
                        end = start + browser.trackViewportWidth() * refFrame.bpPerPixel,
                        range = start - end;
                    var sortBy = [refFrame.chr, start, end, "DESC"];
                    if (config.tracks) {
                        config.sortBy = sortBy;
                        browser.loadTracksWithConfigList(config.tracks, sortBy);


                    }

                }, true);

            } else if (config.tracks) {

                browser.loadTracksWithConfigList(config.tracks);

            }


        }).catch(function (error) {
            igv.presentAlert(error);
            console.log(error);
        });

        return browser;

    };

    function createStandardControls(browser, config) {

        var $igvLogo,
            $controls,
            contentKaryo,
            $navigation,
            $searchContainer,
            $faZoom,
            $trackLabelToggle,
            $cursorTrackingGuideToggle,
            $zoomContainer,
            $faZoomIn,
            $faZoomOut,
            $karyoPanelToggle,
            display;

        $controls = $('<div id="igvControlDiv">');

        if (config.showNavigation) {

            $navigation = $('<div class="igvNavigation">');
            $controls.append($navigation[0]);

            $igvLogo = $('<div class="igv-logo">');

            $searchContainer = $('<div class="igvNavigationSearch">');

            browser.$searchInput = $('<input class="igvNavigationSearchInput" type="text" placeholder="Locus Search">');

            browser.$searchInput.change(function () {

                browser.search($(this).val(), null, null, config);
            });

            $faZoom = $('<i class="igv-app-icon fa fa-search fa-18px shim-left-6">');

            $faZoom.click(function () {
                browser.search(browser.$searchInput.val(), null, null, config);
            });

            $searchContainer.append(browser.$searchInput[0]);
            $searchContainer.append($faZoom[0]);

            $navigation.append($igvLogo[0]);
            $navigation.append($searchContainer[0]);

            // search results presented in table
            browser.$searchResults = $('<div class="igvNavigationSearchResults">');
            browser.$searchResultsTable = $('<table class="igvNavigationSearchResultsTable">');

            browser.$searchResults.append(browser.$searchResultsTable[0]);

            $searchContainer.append(browser.$searchResults[0]);

            browser.$searchResults.hide();

            // window size panel
            browser.windowSizePanel = new igv.WindowSizePanel($navigation);

            // zoom in/out
            $faZoomOut = $('<i class="fa fa-minus-circle igv-app-icon fa-24px" style="padding-right: 4px;">');

            $faZoomOut.click(function () {
                igv.browser.zoomOut();
            });

            $faZoomIn = $('<i class="fa fa-plus-circle igv-app-icon fa-24px">');

            $faZoomIn.click(function () {
                igv.browser.zoomIn();
            });

            $zoomContainer = $('<div class="igvNavigationZoom">');
            $zoomContainer.append($faZoomOut[0]);
            $zoomContainer.append($faZoomIn[0]);
            $navigation.append($zoomContainer[0]);

            // toggle track labels
            $trackLabelToggle = $('<div class="igv-toggle-track-labels">');
            $trackLabelToggle.text("hide labels");
            $trackLabelToggle.click(function () {
                browser.trackLabelsVisible = !browser.trackLabelsVisible;
                $(this).text(true === browser.trackLabelsVisible ? "hide labels" : "show labels");
                $(browser.trackContainerDiv).find('.igv-track-label').toggle();
            });

            // one base wide center guide
            browser.centerGuide = new igv.CenterGuide($(browser.trackContainerDiv), config);

            // cursor tracking guide
            browser.$cursorTrackingGuide = $('<div class="igv-cursor-tracking-guide">');
            $(browser.trackContainerDiv).append(browser.$cursorTrackingGuide);
            browser.$cursorTrackingGuide.css("display", (config.showCursorTrackingGuide && true == config.showCursorTrackingGuide) ? "block" : "none");

            $cursorTrackingGuideToggle = $('<div class="igv-toggle-track-labels">');
            display = browser.$cursorTrackingGuide.css("display");
            $cursorTrackingGuideToggle.text("none" === display ? "show cursor guide" : "hide cursor guide");

            $cursorTrackingGuideToggle.on("click", function () {
                display = browser.$cursorTrackingGuide.css("display");
                if ("none" === display) {
                    browser.$cursorTrackingGuide.css("display", "block");
                    $cursorTrackingGuideToggle.text("hide cursor guide");
                } else {
                    browser.$cursorTrackingGuide.css("display", "none");
                    $cursorTrackingGuideToggle.text("show cursor guide");
                }
            });

            $navigation.append($cursorTrackingGuideToggle);
            $navigation.append(browser.centerGuide.$centerGuideToggle);
            $navigation.append($trackLabelToggle);

        }

        if (config.showKaryo) {
            contentKaryo = $('#igvKaryoDiv')[0];
            // if a karyo div already exists in the page, use that one.
            // this allows the placement of the karyo view on the side, for instance
            if (!contentKaryo) {
                contentKaryo = $('<div id="igvKaryoDiv" class="igv-karyo-div">')[0];
                $controls.append(contentKaryo);
            }
            browser.karyoPanel = new igv.KaryoPanel(contentKaryo);

            $karyoPanelToggle = $('<div class="igv-toggle-track-labels">');

            if (config.showKaryo === "hide") {
                $karyoPanelToggle.text("Show Karyotype");
                $(contentKaryo).addClass("igv-karyo-hide");
            } else {
                $karyoPanelToggle.text("Hide Karyotype");
            }

            $karyoPanelToggle.click(function () {
                var hidden = $(".igv-karyo-div").hasClass("igv-karyo-hide");
                if (hidden) {
                    $karyoPanelToggle.text("Hide Karyotype");
                    $(".igv-karyo-div").removeClass("igv-karyo-hide");
                } else {
                    $karyoPanelToggle.text("Show Karyotype");
                    $(".igv-karyo-div").addClass("igv-karyo-hide");
                }
            });

            $navigation.append($karyoPanelToggle[0]);
        }

        return $controls[0];
    }

    /**
     * Expands ucsc type genome identifiers to genome object.
     *
     * @param genomeId
     * @returns {{}}
     */
    function expandGenome(genomeId) {

        var reference = {id: genomeId};

        switch (genomeId) {

            case "hg18":
                reference.fastaURL = "https://s3.amazonaws.com/igv.broadinstitute.org/genomes/seq/hg18/hg18.fasta";
                reference.cytobandURL = "https://s3.amazonaws.com/igv.broadinstitute.org/genomes/seq/hg18/cytoBand.txt.gz";
                break;
            case "hg19":
            case "GRCh37":
            default:
            {
                reference.fastaURL = "https://s3.amazonaws.com/igv.broadinstitute.org/genomes/seq/hg19/hg19.fasta";
                reference.cytobandURL = "https://s3.amazonaws.com/igv.broadinstitute.org/genomes/seq/hg19/cytoBand.txt";
            }
        }
        return reference;
    }

    function setDefaults(config) {

        config.showKaryo = config.showKaryo || false;
        if (config.showControls === undefined) config.showControls = true;
        if (config.showNavigation === undefined) config.showNavigation = true;
        if (config.showRuler === undefined) config.showRuler = true;
        if (config.showSequence === undefined) config.showSequence = true;
        if (config.showIdeogram === undefined) config.showIdoegram = true;
        if (config.flanking === undefined) config.flanking = 1000;
        if (config.pairsSupported === undefined) config.pairsSupported = true;
        if (config.type === undefined) config.type = "IGV";

        if (!config.tracks) {
            config.tracks = [];
        }
        if (config.showSequence) {
            config.tracks.push({type: "sequence", order: -9999});
        }  // Sequence track

    }

    igv.removeBrowser = function () {
        $(igv.browser.div).remove();
        $(".igv-grid-container-colorpicker").remove();
        $(".igv-grid-container-dialog").remove();
        // $(".igv-grid-container-dialog").remove();
    }


    // Increments an anonymous usage count.  Essential for continued funding of igv.js, please do not remove.
    function phoneHome() {
        var url = "https://data.broadinstitute.org/igv/projects/current/counter_igvjs.php?version=" + igvjs_version;
        igvxhr.load(url).then(function (ignore) {
            console.log(ignore);
        }).catch(function (error) {
            console.log(error);
        });
    }

    return igv;
})
(igv || {});








/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// Extensions to javascript core classes to support porting of igv

CanvasRenderingContext2D.prototype.strokeLine = function (x1, y1, x2, y2, lineWidth) {

    this.save();
    this.beginPath();
    if (lineWidth) {
        this.lineWidth = lineWidth;
    }
    this.moveTo(x1, y1);
    this.lineTo(x2, y2);
    this.stroke();
    this.restore();
}

CanvasRenderingContext2D.prototype.drawArrowhead = function (x, y, size, lineWidth) {

    this.save();
    if (!size) {
        size = 5;
    }
    if (lineWidth) {
        this.lineWidth = lineWidth;
    }
    this.beginPath();
    this.moveTo(x, y - size / 2);
    this.lineTo(x, y + size / 2);
    this.lineTo(x + size, y);
    this.lineTo(x, y - size / 2);
    this.closePath();
    this.fill();
    this.restore();
}


CanvasRenderingContext2D.prototype.roundRect = function (x, y, width, height, radius, fill, stroke) {

    this.save();
    if (typeof stroke == "undefined") {
        stroke = true;
    }
    if (typeof radius === "undefined") {
        radius = 5;
    }
    this.beginPath();
    this.moveTo(x + radius, y);
    this.lineTo(x + width - radius, y);
    this.quadraticCurveTo(x + width, y, x + width, y + radius);
    this.lineTo(x + width, y + height - radius);
    this.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
    this.lineTo(x + radius, y + height);
    this.quadraticCurveTo(x, y + height, x, y + height - radius);
    this.lineTo(x, y + radius);
    this.quadraticCurveTo(x, y, x + radius, y);
    this.closePath();
    if (stroke) {
        this.stroke();
    }
    if (fill) {
        this.fill();
    }
    this.restore();
}

CanvasRenderingContext2D.prototype.polygon = function (x, y, fill, stroke) {

    this.save();
    if (typeof stroke == "undefined") {
        stroke = true;
    }

    this.beginPath();
    var len = x.length;
    this.moveTo(x[0], y[0]);
    for (var i = 1; i < len; i++) {
        this.lineTo(x[i], y[i]);
        // this.moveTo(x[i], y[i]);
    }

    this.closePath();
    if (stroke) {
        this.stroke();
    }
    if (fill) {
        this.fill();
    }
    this.restore();
}

CanvasRenderingContext2D.prototype.eqTriangle = function (side, cx, cy) {

    this.save();
    var h = side * (Math.sqrt(3) / 2);

    this.beginPath();
    this.moveTo(cx, cy - h / 2);
    this.lineTo(cx - side / 2, cy + h / 2);
    this.lineTo(cx + side / 2, cy + h / 2);
    this.lineTo(cx, cy - h / 2);
    this.closePath();

    this.stroke();
    this.fill();
    this.restore();
}


if (typeof String.prototype.startsWith === "undefined") {
    String.prototype.startsWith = function (aString) {
        if (this.length < aString.length) {
            return false;
        }
        else {
            return (this.substr(0, aString.length) == aString);
        }
    }
}

if (typeof String.prototype.endsWith === "undefined") {
    String.prototype.endsWith = function (aString) {
        if (this.length < aString.length) {
            return false;
        }
        else {
            return (this.substr(this.length - aString.length, aString.length) == aString);
        }
    }
}

if (typeof String.prototype.contains === "undefined") {
    String.prototype.contains = function (it) {
        return this.indexOf(it) != -1;
    };
}

if (typeof String.prototype.includes === "undefined") {
    String.prototype.includes = function (it) {
        return this.indexOf(it) != -1;
    };
}


if (typeof String.prototype.splitLines === "undefined") {
    String.prototype.splitLines = function () {
        return this.split(/\r\n|\n|\r/gm);
    }
}


if (typeof Uint8Array.prototype.toText === "undefined") {

    Uint8Array.prototype.toText = function () {

        // note, dont use forEach or apply -- will run out of stack
        var i, len, str;
        str = "";
        for (i = 0, len = this.byteLength; i < len; i++) {
            str += String.fromCharCode(this[i]);
        }
        return str;

    }

}

var log2 = Math.log(2);

if (typeof Math.log2 === "undefined") {
    Math.log2 = function (x) {
        return Math.log(x) / log2;
    }
}

// Implementation of bind().  This is included primarily for use with phantom.js, which does not implement it.
// Attributed to John Resig

if (typeof Function.prototype.bind === "undefined") {
    Function.prototype.bind = function () {
        var fn = this,
            args = Array.prototype.slice.call(arguments),
            object = args.shift();
        return function () {
            return fn.apply(object,
                args.concat(Array.prototype.slice.call(arguments)));
        }
    }
}

if (!Date.now) {
    Date.now = function now() {
        return new Date().getTime();
    };
}

if (!Object.keys) {
    Object.keys = (function() {
        'use strict';
        var hasOwnProperty = Object.prototype.hasOwnProperty,
            hasDontEnumBug = !({ toString: null }).propertyIsEnumerable('toString'),
            dontEnums = [
                'toString',
                'toLocaleString',
                'valueOf',
                'hasOwnProperty',
                'isPrototypeOf',
                'propertyIsEnumerable',
                'constructor'
            ],
            dontEnumsLength = dontEnums.length;

        return function(obj) {
            if (typeof obj !== 'object' && (typeof obj !== 'function' || obj === null)) {
                throw new TypeError('Object.keys called on non-object');
            }

            var result = [], prop, i;

            for (prop in obj) {
                if (hasOwnProperty.call(obj, prop)) {
                    result.push(prop);
                }
            }

            if (hasDontEnumBug) {
                for (i = 0; i < dontEnumsLength; i++) {
                    if (hasOwnProperty.call(obj, dontEnums[i])) {
                        result.push(dontEnums[i]);
                    }
                }
            }
            return result;
        };
    }());
}

if (!Array.isArray) {
    Array.isArray = function(arg) {
        return Object.prototype.toString.call(arg) === '[object Array]';
    };
}







/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    igv.presentAlert = function (string) {

        igv.alert.$dialogLabel.text(string);
        igv.alert.show(undefined);

        igv.popover.hide();

    };

    igv.trackMenuItems = function (popover, trackView) {

        var menuItems = [],
            trackItems;

        menuItems.push(igv.dialogMenuItem(
            popover,
            trackView,
            "Set track name",
            function () {
                return "Track Name"
            },
            trackView.track.name,
            function () {

                var alphanumeric = parseAlphanumeric(igv.dialog.$dialogInput.val());

                if (undefined !== alphanumeric) {
                    igv.setTrackLabel(trackView.track, alphanumeric);
                    trackView.update();
                }

                function parseAlphanumeric(value) {

                    var alphanumeric_re = /(?=.*[a-zA-Z].*)([a-zA-Z0-9 ]+)/,
                        alphanumeric = alphanumeric_re.exec(value);

                    return (null !== alphanumeric) ? alphanumeric[0] : "untitled";
                }

            }, undefined));

        menuItems.push(igv.dialogMenuItem(
            popover,
            trackView,
            "Set track height",
            function () {
                return "Track Height"
            },
            trackView.trackDiv.clientHeight,
            function () {

                var number = parseFloat(igv.dialog.$dialogInput.val(), 10);

                if (undefined !== number) {
                    // If explicitly setting the height adust min or max, if neccessary.
                    if (trackView.track.minHeight !== undefined && trackView.track.minHeight > number) {
                        trackView.track.minHeight = number;
                    }
                    if (trackView.track.maxHeight !== undefined && trackView.track.maxHeight < number) {
                        trackView.track.minHeight = number;
                    }
                    trackView.setTrackHeight(number);
                    trackView.track.autoHeight = false;   // Explicitly setting track height turns off autoHeight

                }

            }, undefined));

        if (trackView.track.popupMenuItems) {

            trackItems = trackView.track.popupMenuItems(popover);

            if (trackItems && trackItems.length > 0) {

                trackItems.forEach(function (trackItem, i) {

                    var str;

                    if (trackItem.name) {

                        str = (0 === i) ? '<div class=\"igv-track-menu-item igv-track-menu-border-top\">' : '<div class=\"igv-track-menu-item\">';
                        str = str + trackItem.name + '</div>';

                        menuItems.push({object: $(str), click: trackItem.click, init: trackItem.init});
                    } else {

                        if (0 === i) {
                            trackItem.object.addClass("igv-track-menu-border-top");
                            menuItems.push(trackItem);
                        }
                        else {
                            menuItems.push(trackItem);
                        }

                    }

                });
            }
        }

        if (trackView.track.removable !== false) {

            menuItems.push(
                igv.dialogMenuItem(
                    popover,
                    trackView,
                    "Remove track",
                    function () {
                        var label = "Remove " + trackView.track.name;
                        return '<div class="igv-dialog-label-centered">' + label + '</div>';
                    },
                    undefined,
                    function () {
                        popover.hide();
                        trackView.browser.removeTrack(trackView.track);
                    },
                    true)
            );

        }

        return menuItems;

    };

    igv.dialogMenuItem = function (popover, trackView, gearMenuLabel, labelHTMLFunction, inputValue, clickFunction, doDrawBorderOrUndefined) {

        var _div = (true === doDrawBorderOrUndefined) ? '<div class="igv-track-menu-item igv-track-menu-border-top">' : '<div class="igv-track-menu-item">';

        return {
            object: $(_div + gearMenuLabel + '</div>'),
            click: function () {

                igv.dialog.configure(labelHTMLFunction, inputValue, clickFunction);
                igv.dialog.show($(trackView.trackDiv));
                popover.hide();
            }
        }
    };

    igv.dataRangeMenuItem = function (popover, trackView) {

        return {
            object: $('<div class="igv-track-menu-item">' + "Set data range" + '</div>'),
            click: function () {
                igv.dataRangeDialog.configureWithTrackView(trackView);
                igv.dataRangeDialog.show();
                popover.hide();
            }
        }
    };

    igv.colorPickerMenuItem = function (popover, trackView) {

        return {
            object: $('<div class="igv-track-menu-item">' + "Set track color" + '</div>'),
            click: function () {
                igv.colorPicker.configure(trackView);
                igv.colorPicker.show();
                popover.hide();
            }
        }
    };

    igv.attachDialogCloseHandlerWithParent = function ($parent, closeHandler) {

        var $container = $('<div class="igv-dialog-close-container">'),
            $fa = $('<i class="fa fa-times igv-dialog-close-fa">');

        $container.append($fa[0]);
        $parent.append($container[0]);

        $fa.hover(
            function () {
                $fa.removeClass("fa-times");
                $fa.addClass("fa-times-circle");

                $fa.css({
                    "color": "#222"
                });
            },

            function () {
                $fa.removeClass("fa-times-circle");
                //$fa.removeClass("fa-times-circle fa-lg");
                $fa.addClass("fa-times");

                $fa.css({
                    "color": "#444"
                });

            }
        );

        $fa.click(closeHandler);

    };

    igv.spinner = function (size) {

        // spinner
        var $container,
            $spinner;

        $spinner = $('<i class="fa fa-spinner fa-spin">');
        if (size) {
            $spinner.css("font-size", size);
        }

        $container = $('<div class="igv-spinner-container">');
        $container.append($spinner[0]);

        return $container[0];
    };

    /**
     * Find spinner
     */
    igv.getSpinnerObjectWithParentElement = function (parentElement) {
        return $(parentElement).find("div.igv-spinner-container");
    };

    /**
     * Start the spinner for the parent element, if it has one
     */
    igv.startSpinnerAtParentElement = function (parentElement) {

        var spinnerObject = igv.getSpinnerObjectWithParentElement(parentElement);

        if (spinnerObject) {
            spinnerObject.show();
        }

    };

    /**
     * Stop the spinner for the parent element, if it has one
     * @param parentElement
     */
    igv.stopSpinnerAtParentElement = function (parentElement) {

        var spinnerObject = igv.getSpinnerObjectWithParentElement(parentElement);

        if (spinnerObject) {
            spinnerObject.hide();
        }

    };

    igv.parseUri = function (str) {

        var o = igv.parseUri.options,
            m = o.parser[o.strictMode ? "strict" : "loose"].exec(str),
            uri = {},
            i = 14;

        while (i--) uri[o.key[i]] = m[i] || "";

        uri[o.q.name] = {};
        uri[o.key[12]].replace(o.q.parser, function ($0, $1, $2) {
            if ($1) uri[o.q.name][$1] = $2;
        });

        return uri;
    };

    igv.parseUri.options = {
        strictMode: false,
        key: ["source", "protocol", "authority", "userInfo", "user", "password", "host", "port", "relative", "path", "directory", "file", "query", "anchor"],
        q: {
            name: "queryKey",
            parser: /(?:^|&)([^&=]*)=?([^&]*)/g
        },
        parser: {
            strict: /^(?:([^:\/?#]+):)?(?:\/\/((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?))?((((?:[^?#\/]*\/)*)([^?#]*))(?:\?([^#]*))?(?:#(.*))?)/,
            loose: /^(?:(?![^:@]+:[^:@\/]*@)([^:\/?#.]+):)?(?:\/\/)?((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?)(((\/(?:[^?#](?![^?#\/]*\.[^?#\/.]+(?:[?#]|$)))*\/?)?([^?#\/]*))(?:\?([^#]*))?(?:#(.*))?)/
        }
    };

    igv.domElementRectAsString = function (element) {
        return " x " + element.clientLeft + " y " + element.clientTop + " w " + element.clientWidth + " h " + element.clientHeight;
    };

    igv.isNumber = function (n) {

        if ("" === n) {

            return false
        } else if (undefined === n) {

            return false;
        } else {

            return !isNaN(parseFloat(n)) && isFinite(n);
        }

    };

    igv.guid = function () {
        return ("0000" + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4);
    };

    // Returns a random number between min (inclusive) and max (exclusive)
    igv.random = function (min, max) {
        return Math.random() * (max - min) + min;
    };

    // StackOverflow: http://stackoverflow.com/a/10810674/116169
    igv.numberFormatter = function (rawNumber) {

        var dec = String(rawNumber).split(/[.,]/),
            sep = ',',
            decsep = '.';

        return dec[0].split('').reverse().reduce(function (prev, now, i) {
                return i % 3 === 0 ? prev + sep + now : prev + now;
            }).split('').reverse().join('') + (dec[1] ? decsep + dec[1] : '');
    };

    igv.numberUnFormatter = function (formatedNumber) {

        return formatedNumber.split(",").join().replace(",", "", "g");
    };

    /**
     * Translate the mouse coordinates for the event to the coordinates for the given target element
     * @param e
     * @param target
     * @returns {{x: number, y: number}}
     */
    igv.translateMouseCoordinates = function (e, target) {

        var eFixed = $.event.fix(e),   // Sets pageX and pageY for browsers that don't support them
            posx = eFixed.pageX - $(target).offset().left,
            posy = eFixed.pageY - $(target).offset().top;

        return {x: posx, y: posy}
    };

    /**
     * Format markup for popover text from an array of name value pairs [{name, value}]
     */
    igv.formatPopoverText = function (nameValueArray) {

        var markup = "<table class=\"igv-popover-table\">";

        nameValueArray.forEach(function (nameValue) {

            if (nameValue.name) {
                //markup += "<tr><td class=\"igv-popover-td\">" + "<span class=\"igv-popoverName\">" + nameValue.name + "</span>" + "<span class=\"igv-popoverValue\">" + nameValue.value + "</span>" + "</td></tr>";
                markup += "<tr><td class=\"igv-popover-td\">" + "<div class=\"igv-popoverNameValue\">" + "<span class=\"igv-popoverName\">" + nameValue.name + "</span>" + "<span class=\"igv-popoverValue\">" + nameValue.value + "</span>" + "</div>" + "</td></tr>";
            }
            else {
                // not a name/value pair
                markup += "<tr><td>" + nameValue.toString() + "</td></tr>";
            }
        });

        markup += "</table>";
        return markup;


    };

    igv.throttle = function (fn, threshhold, scope) {
        threshhold || (threshhold = 200);
        var last, deferTimer;

        return function () {
            var context = scope || this;

            var now = +new Date,
                args = arguments;
            if (last && now < last + threshhold) {
                // hold on to it
                clearTimeout(deferTimer);
                deferTimer = setTimeout(function () {
                    last = now;
                    fn.apply(context, args);
                }, threshhold);
            } else {
                last = now;
                fn.apply(context, args);
            }
        }
    };

    igv.splitStringRespectingQuotes = function (string, delim) {

        var tokens = [],
            len = string.length,
            i,
            n = 0,
            quote = false,
            c;

        if (len > 0) {

            tokens[n] = string.charAt(0);
            for (i = 1; i < len; i++) {
                c = string.charAt(i);
                if (c === '"') {
                    quote = !quote;
                }
                else if (!quote && c === delim) {
                    n++;
                    tokens[n] = "";
                }
                else {
                    tokens[n] += c;
                }
            }
        }
        return tokens;
    };

    /**
     * Extend jQuery's ajax function to handle binary requests.   Credit to Henry Algus:
     *
     * http://www.henryalgus.com/reading-binary-files-using-jquery-ajax/
     */
    igv.addAjaxExtensions = function () {

        // use this transport for "binary" data type
        $.ajaxTransport("+binary", function (options, originalOptions, jqXHR) {

            return {
                // create new XMLHttpRequest
                send: function (_, callback) {
                    // setup all variables
                    var xhr = new XMLHttpRequest(),
                        url = options.url,
                        type = options.type,
                        responseType = "arraybuffer",
                        data = options.data || null;

                    xhr.addEventListener('load', function () {
                        var data = {};
                        data[options.dataType] = xhr.response;
                        // make callback and send data
                        callback(xhr.status, xhr.statusText, data, xhr.getAllResponseHeaders());
                    });

                    xhr.open(type, url);
                    xhr.responseType = responseType;

                    if (options.headers) {
                        for (var prop in options.headers) {
                            if (options.headers.hasOwnProperty(prop)) {
                                xhr.setRequestHeader(prop, options.headers[prop]);
                            }
                        }
                    }

                    // TODO -- set any other options values
                },
                abort: function () {
                    jqXHR.abort();
                }
            };

        });
    };

    /**
     * Test if the given value is a string or number.  Not using typeof as it fails on boxed primitives.
     *
     * @param value
     * @returns boolean
     */
    igv.isStringOrNumber = function (value) {
        return (value.substring || value.toFixed) ? true : false
    };

    igv.constrainBBox = function ($child, $parent) {

        var delta,
            topLeft,
            bboxChild = {},
            bboxParent = {};

        bboxParent.left = bboxParent.top = 0;
        bboxParent.right = $parent.outerWidth();
        bboxParent.bottom = $parent.outerHeight();

        topLeft = $child.offset();

        bboxChild.left = topLeft.left - $parent.offset().left;
        bboxChild.top = topLeft.top - $parent.offset().top;
        bboxChild.right = bboxChild.left + $child.outerWidth();
        bboxChild.bottom = bboxChild.top + $child.outerHeight();

        delta = bboxChild.bottom - bboxParent.bottom;
        if (delta > 0) {

            // clamp to trackContainer bottom
            topLeft.top -= delta;

            bboxChild.top -= delta;
            bboxChild.bottom -= delta;

            delta = bboxChild.top - bboxParent.top;
            if (delta < 0) {
                topLeft.top -= delta;
            }

        }

        return topLeft;

    };

    igv.log = function (message) {
        if (igv.enableLogging && console && console.log) {
            console.log(message);
        }
    };


    return igv;

})(igv || {});



/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igvxhr = (function (igvxhr) {

    // Compression types
    const NONE = 0;
    const GZIP = 1;
    const BGZF = 2;

    igvxhr.load = function (url, options) {

        if(!options) options = {};

        return new Promise(function (fulfill, reject) {

            var xhr = new XMLHttpRequest(),
                sendData = options.sendData,
                method = options.method || (sendData ? "POST" : "GET"),
                range = options.range,
                responseType = options.responseType,
                contentType = options.contentType,
                mimeType = options.mimeType,
                headers = options.headers,
                isSafari = navigator.vendor.indexOf("Apple") == 0 && /\sSafari\//.test(navigator.userAgent),
                withCredentials = options.withCredentials,
                header_keys, key, value, i;


            // Hack to prevent caching for google storage files.  Get weird net:err-cache errors otherwise
            if (range && url.includes("googleapis")) {
                url += url.includes("?") ? "&" : "?";
                url += "someRandomSeed=" + Math.random().toString(36);
            }

            xhr.open(method, url);

            if (range) {
                var rangeEnd = range.size ? range.start + range.size - 1 : "";
                xhr.setRequestHeader("Range", "bytes=" + range.start + "-" + rangeEnd);
            }
            if (contentType) {
                xhr.setRequestHeader("Content-Type", contentType);
            }
            if (mimeType) {
                xhr.overrideMimeType(mimeType);
            }
            if (responseType) {
                xhr.responseType = responseType;
            }
            if (headers) {
                header_keys = Object.keys(headers);
                for (i = 0; i < header_keys.length; i++) {
                    key = header_keys[i];
                    value = headers[key];
                    // console.log("Adding to header: " + key + "=" + value);
                    xhr.setRequestHeader(key, value);
                }
            }

            // let cookies go along to get files from any website we are logged in to
            // NOTE: using withCredentials with servers that return "*" for access-allowed-origin will fail
            if (withCredentials === true) {
                xhr.withCredentials = true;
            }

            xhr.onload = function (event) {
                // when the url points to a local file, the status is 0 but that is no error
                if (xhr.status == 0 || (xhr.status >= 200 && xhr.status <= 300)) {

                    if (range && xhr.status != 206) {
                        handleError("ERROR: range-byte header was ignored for url: " + url);
                    }
                    else {
                        fulfill(xhr.response, xhr);
                    }
                }
                else {

                    //
                    if (xhr.status === 416) {
                        //  Tried to read off the end of the file.   This shouldn't happen, but if it does return an
                        handleError("Unsatisfiable range");
                    }
                    else {// TODO -- better error handling
                        handleError("Error accessing resource: " + xhr.status);
                    }

                }

            };

            xhr.onerror = function (event) {

                if (isCrossDomain(url) && url && !options.crossDomainRetried && igv.browser.crossDomainProxy &&
                    url != igv.browser.crossDomainProxy) {

                    options.sendData = "url=" + url;
                    options.crossDomainRetried = true;

                    igvxhr.load(igv.browser.crossDomainProxy, options).then(fulfill);
                }
                else {
                    handleError("Error accessing resource: " + url + " Status: " + xhr.status);
                }
            }


            xhr.ontimeout = function (event) {
                handleError("Timed out");
            };

            xhr.onabort = function (event) {
                console.log("Aborted");
                reject(new igv.AbortLoad());
            };

            try {
                xhr.send(sendData);
            } catch (e) {
                console.log(e);
            }


            function handleError(message) {
                if (reject) {
                    reject(message);
                }
                else {
                    throw Error(message);
                }
            }
        });
    }

    igvxhr.loadArrayBuffer = function (url, options) {

        if (options === undefined) options = {};
        options.responseType = "arraybuffer";
        return igvxhr.load(url, options);
    };

    igvxhr.loadJson = function (url, options) {

        var method = options.method || (options.sendData ? "POST" : "GET");

        if (method == "POST") options.contentType = "application/json";

        return new Promise(function (fulfill, reject) {

            igvxhr.load(url, options).then(
                function (result) {
                    if (result) {
                        fulfill(JSON.parse(result));
                    }
                    else {
                        fulfill(result);
                    }
                }).catch(reject);
        })
    }

    /**
     * Load a "raw" string.
     */
    igvxhr.loadString = function (url, options) {

        var compression, fn, idx;

        if (options === undefined) options = {};

        // Strip parameters from url
        // TODO -- handle local files with ?
        idx = url.indexOf("?");
        fn = idx > 0 ? url.substring(0, idx) : url;

        if (options.bgz) {
            compression = BGZF;
        }
        else if (fn.endsWith(".gz")) {
            compression = GZIP;
        }
        else {
            compression = NONE;
        }

        if (compression === NONE) {
            options.mimeType = 'text/plain; charset=x-user-defined';
            return igvxhr.load(url, options);
        }
        else {
            options.responseType = "arraybuffer";

            return new Promise(function (fulfill, reject) {

                igvxhr.load(url, options).then(
                    function (data) {
                        var result = igvxhr.arrayBufferToString(data, compression);
                        fulfill(result);
                    }).catch(reject)
            })
        }

    };

    igvxhr.loadStringFromFile = function (localfile, options) {

        return new Promise(function (fulfill, reject) {

            var fileReader = new FileReader(),
                range = options.range;


            fileReader.onload = function (e) {

                var compression, result;

                if (options.bgz) {
                    compression = BGZF;
                }
                else if (localfile.name.endsWith(".gz")) {

                    compression = GZIP;
                }
                else {
                    compression = NONE;
                }

                result = igvxhr.arrayBufferToString(fileReader.result, compression);

                fulfill(result, localfile);

            };

            fileReader.onerror = function (e) {
                console.log("reject uploading local file " + localfile.name);
                reject(null, fileReader);
            };

            fileReader.readAsArrayBuffer(localfile);

        });
    }

    function isCrossDomain(url) {

        var origin = window.location.origin;

        return !url.startsWith(origin);

    }

    igvxhr.arrayBufferToString = function (arraybuffer, compression) {

        var plain, inflate;

        if (compression === GZIP) {
            inflate = new Zlib.Gunzip(new Uint8Array(arraybuffer));
            plain = inflate.decompress();
        }
        else if (compression === BGZF) {
            plain = new Uint8Array(igv.unbgzf(arraybuffer));
        }
        else {
            plain = new Uint8Array(arraybuffer);
        }

        var result = "";
        for (var i = 0, len = plain.length; i < len; i++) {
            result = result + String.fromCharCode(plain[i]);
        }
        return result;
    };


    igv.AbortLoad = function () {

    }

    return igvxhr;

})
(igvxhr || {});


/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/** An implementation of an interval tree, following the explanation.
 * from CLR.
 *
 * Public interface:
 *   Constructor  IntervalTree
 *   Insertion    insert
 *   Search       findOverlapping
 */



var igv = (function (igv) {

    var BLACK = 1;
    var RED = 2;

    var NIL = {}
    NIL.color = BLACK;
    NIL.parent = NIL;
    NIL.left = NIL;
    NIL.right = NIL;


    igv.IntervalTree = function () {
        this.root = NIL;
    }


    igv.IntervalTree.prototype.insert = function (start, end, value) {

        var interval = new Interval(start, end, value);
        var x = new Node(interval);
        this.treeInsert(x);
        x.color = RED;
        while (x != this.root && x.parent.color == RED) {
            if (x.parent == x.parent.parent.left) {
                var y = x.parent.parent.right;
                if (y.color == RED) {
                    x.parent.color = BLACK;
                    y.color = BLACK;
                    x.parent.parent.color = RED;
                    x = x.parent.parent;
                } else {
                    if (x == x.parent.right) {
                        x = x.parent;
                        leftRotate.call(this, x);
                    }
                    x.parent.color = BLACK;
                    x.parent.parent.color = RED;
                    rightRotate.call(this, x.parent.parent);
                }
            } else {
                var y = x.parent.parent.left;
                if (y.color == RED) {
                    x.parent.color = BLACK;
                    y.color = BLACK;
                    x.parent.parent.color = RED;
                    x = x.parent.parent;
                } else {
                    if (x == x.parent.left) {
                        x = x.parent;
                        rightRotate.call(this, x);
                    }
                    x.parent.color = BLACK;
                    x.parent.parent.color = RED;
                    leftRotate.call(this, x.parent.parent);
                }
            }
        }
        this.root.color = BLACK;
    }


    /**
     *
     * @param start - query interval
     * @param end - query interval
     * @returns Array of all intervals overlapping the query region
     */
    igv.IntervalTree.prototype.findOverlapping = function (start, end) {


        var searchInterval = new Interval(start, end, 0);

        if (this.root === NIL) return [];

        var intervals = searchAll.call(this, searchInterval, this.root, []);

        if(intervals.length > 1) {
            intervals.sort(function(i1, i2) {
                 return i1.low - i2.low;
            });
        }

        return intervals;
    }

    /**
     * Dump info on intervals to console.  For debugging.
     */
    igv.IntervalTree.prototype.logIntervals = function() {

        logNode(this.root, 0);

        function logNode(node, indent) {

            var space = "";
            for(var i=0; i<indent; i++) space += " ";
            console.log(space + node.interval.low + " " + node.interval.high); // + " " + (node.interval.value ? node.interval.value : " null"));

            indent += 5;

            if(node.left != NIL) logNode(node.left, indent);
            if(node.right != NIL) logNode(node.right, indent);
        }

    }


    igv.IntervalTree.prototype.mapIntervals = function(func) {

        applyInterval(this.root);

        function applyInterval(node) {

            func(node.interval);

            if(node.left != NIL) applyInterval(node.left);
            if(node.right != NIL) applyInterval(node.right);
        }
    }

    function searchAll(interval, node, results) {

        if (node.interval.overlaps(interval)) {
            results.push(node.interval);
        }

        if (node.left != NIL && node.left.max >= interval.low) {
            searchAll.call(this, interval, node.left, results);
        }

        if (node.right != NIL && node.right.min <= interval.high) {
            searchAll.call(this, interval, node.right, results);
        }

        return results;
    }

    function leftRotate(x) {
        var y = x.right;
        x.right = y.left;
        if (y.left != NIL) {
            y.left.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == NIL) {
            this.root = y;
        } else {
            if (x.parent.left == x) {
                x.parent.left = y;
            } else {
                x.parent.right = y;
            }
        }
        y.left = x;
        x.parent = y;

        applyUpdate.call(this, x);
        // no need to apply update on y, since it'll y is an ancestor
        // of x, and will be touched by applyUpdate().
    }


    function rightRotate(x) {
        var y = x.left;
        x.left = y.right;
        if (y.right != NIL) {
            y.right.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == NIL) {
            this.root = y;
        } else {
            if (x.parent.right == x) {
                x.parent.right = y;
            } else {
                x.parent.left = y;
            }
        }
        y.right = x;
        x.parent = y;


        applyUpdate.call(this, x);
        // no need to apply update on y, since it'll y is an ancestor
        // of x, and will be touched by applyUpdate().
    }


    /**
     * Note:  Does not maintain RB constraints,  this is done post insert
     *
     * @param x  a Node
     */
    igv.IntervalTree.prototype.treeInsert = function (x) {
        var node = this.root;
        var y = NIL;
        while (node != NIL) {
            y = node;
            if (x.interval.low <= node.interval.low) {
                node = node.left;
            } else {
                node = node.right;
            }
        }
        x.parent = y;

        if (y == NIL) {
            this.root = x;
            x.left = x.right = NIL;
        } else {
            if (x.interval.low <= y.interval.low) {
                y.left = x;
            } else {
                y.right = x;
            }
        }

        applyUpdate.call(this, x);
    }


    // Applies the statistic update on the node and its ancestors.
    function applyUpdate (node) {
        while (node != NIL) {
            var nodeMax = node.left.max > node.right.max ? node.left.max : node.right.max;
            var intervalHigh = node.interval.high;
            node.max = nodeMax > intervalHigh ? nodeMax : intervalHigh;

            var nodeMin = node.left.min < node.right.min ? node.left.min : node.right.min;
            var intervalLow = node.interval.low;
            node.min = nodeMin < intervalLow ? nodeMin : intervalLow;

            node = node.parent;
        }
    }


    function Interval (low, high, value) {
        this.low = low;
        this.high = high;
        this.value = value;
    }


    Interval.prototype.equals = function (other) {
        if (!other) {
            return false;
        }
        if (this == other) {
            return true;
        }
        return (this.low == otherInterval.low &&
            this.high == otherInterval.high);

    }


    Interval.prototype.compareTo = function (other) {
        if (this.low < other.low)
            return -1;
        if (this.low > other.low)
            return 1;

        if (this.high < other.high)
            return -1;
        if (this.high > other.high)
            return 1;

        return 0;
    }

    /**
     * Returns true if this interval overlaps the other.
     */
    Interval.prototype.overlaps = function (other) {
        try {
            return (this.low <= other.high && other.low <= this.high);
        } catch (e) {
            //alert(e);
            igv.presentAlert(e);
        }
    }

    function Node(interval) {
        this.parent = NIL;
        this.left = NIL;
        this.right = NIL;
        this.interval = interval;
        this.color = RED;
    }



//
//
//    function minimum(node) {
//        while (node.left != NIL) {
//            node = node.left;
//        }
//        return node;
//    }
//
//
//    function maximum(node) {
//
//        while (node.right != NIL) {
//            node = node.right;
//        }
//        return node;
//    }
//
//
//    function successor(x) {
//
//        if (x.right != NIL) {
//            return minimum(x.right);
//        }
//        var y = x.parent;
//        while (y != NIL && x == y.right) {
//            x = y;
//            y = y.parent;
//        }
//        return y;
//    }
//
//
//    function predecessor(x) {
//        if (x.left != NIL) {
//            return maximum(x.left);
//        }
//        var y = x.parent;
//        while (y != NIL && x == y.left) {
//            x = y;
//            y = y.parent;
//        }
//        return y;
//    }
//
//
//
//    igv.IntervalTree.prototype.allRedNodesFollowConstraints = function (node) {
//        if (node == NIL)
//            return true;
//
//        if (node.color == BLACK) {
//            return (this.allRedNodesFollowConstraints(node.left) &&
//                this.allRedNodesFollowConstraints(node.right));
//        }
//
//        // At this point, we know we're on a RED node.
//        return (node.left.color == BLACK &&
//            node.right.color == BLACK &&
//            this.allRedNodesFollowConstraints(node.left) &&
//            this.allRedNodesFollowConstraints(node.right));
//    }
//
//
//    // Check that both ends are equally balanced in terms of black height.
//    igv.IntervalTree.prototype.isBalancedBlackHeight = function (node) {
//        if (node == NIL)
//            return true;
//        return (blackHeight(node.left) == blackHeight(node.right) &&
//            this.isBalancedBlackHeight(node.left) &&
//            this.isBalancedBlackHeight(node.right));
//    }
//
//
//    // The black height of a node should be left/right equal.
//    igv.IntervalTree.prototype.blackHeight = function (node) {
//        if (node == NIL)
//            return 0;
//        var leftBlackHeight = blackHeight(node.left);
//        if (node.color == BLACK) {
//            return leftBlackHeight + 1;
//        } else {
//            return leftBlackHeight;
//        }
//    }


    /**
     * Test code: make sure that the tree has all the properties
     * defined by Red Black trees and interval trees
     * <p/>
     * o.  Root is black.
     * <p/>
     * o.  NIL is black.
     * <p/>
     * o.  Red nodes have black children.
     * <p/>
     * o.  Every path from root to leaves contains the same number of
     * black nodes.
     * <p/>
     * o.  getMax(node) is the maximum of any interval rooted at that node..
     * <p/>
     * This code is expensive, and only meant to be used for
     * assertions and testing.
     */
//
//    igv.IntervalTree.prototype.isValid = function () {
//        if (this.root.color != BLACK) {
//            logger.warn("root color is wrong");
//            return false;
//        }
//        if (NIL.color != BLACK) {
//            logger.warn("NIL color is wrong");
//            return false;
//        }
//        if (allRedNodesFollowConstraints(this.root) == false) {
//            logger.warn("red node doesn't follow constraints");
//            return false;
//        }
//        if (isBalancedBlackHeight(this.root) == false) {
//            logger.warn("black height unbalanced");
//            return false;
//        }
//
//        return hasCorrectMaxFields(this.root) &&
//            hasCorrectMinFields(this.root);
//    }
//
//
//    igv.IntervalTree.prototype.hasCorrectMaxFields = function (node) {
//        if (node == NIL)
//            return true;
//        return (getRealMax(node) == (node.max) &&
//            this.hasCorrectMaxFields(node.left) &&
//            this.hasCorrectMaxFields(node.right));
//    }
//
//
//    igv.IntervalTree.prototype.hasCorrectMinFields = function (node) {
//        if (node == NIL)
//            return true;
//        return (getRealMin(node) == (node.min) &&
//            this.hasCorrectMinFields(node.left) &&
//            this.hasCorrectMinFields(node.right));
//    }

    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    var log = function (txt) {
        // if (console) console.log("karyo: " + txt);
    };

    igv.KaryoPanel = function (parentElement) {

        this.ideograms = null;
        igv.guichromosomes = [];

        this.div = $('<div class="igv-karyo-div"></div>')[0];
        $(parentElement).append(this.div);

        var contentDiv = $('<div class="igv-karyo-content-div"></div>')[0];
        $(this.div).append(contentDiv);

        var canvas = $('<canvas class="igv-karyo-canvas"></canvas>')[0];
        $(contentDiv).append(canvas);
        canvas.setAttribute('width', contentDiv.offsetWidth);
        canvas.setAttribute('height', contentDiv.offsetHeight);
        this.canvas = canvas;
        this.ctx = canvas.getContext("2d");

        var tipCanvas = document.createElement('canvas');
        tipCanvas.style.position = 'absolute';    // => relative to first positioned ancestor
        tipCanvas.style.width = "100px";
        tipCanvas.style.height = "20px";
        tipCanvas.style.left = "-2000px";
        tipCanvas.setAttribute('width', "100px");    //Must set the width & height of the canvas
        tipCanvas.setAttribute('height', "20px");
        var tipCtx = tipCanvas.getContext("2d");
        contentDiv.appendChild(tipCanvas);

        this.canvas.onmousemove = function (e) {

            var mouseCoords = igv.translateMouseCoordinates(e, canvas);
            var mouseX = mouseCoords.x;
            var mouseY = mouseCoords.y;

            var hit = false;
            for (var i = 0; i < igv.guichromosomes.length; i++) {
                var g = igv.guichromosomes[i];
                if (g.x < mouseX && g.right > mouseX && g.y < mouseY && g.bottom > mouseY) {
                    var dy = mouseY - g.y;
                    var bp = Math.round(g.size * dy / g.h);
                    //log("Found chr "+g.name+", bp="+bp+", mousex="+mouseX+", mousey="+mouseY);
                    tipCanvas.style.left = Math.round(mouseX + 20) + "px";
                    tipCanvas.style.top = Math.round(mouseY - 5) + "px";

                    //log("width/height of tip canvas:"+tipCanvas.width+"/"+tipCanvas.height);
                    //log("tipCanvas.left="+tipCanvas.style.left);
                    tipCtx.clearRect(0, 0, tipCanvas.width, tipCanvas.height);
                    tipCtx.fillStyle = 'rgb(255,255,220)';
                    tipCtx.fillRect(0, 0, tipCanvas.width, tipCanvas.height);
                    tipCtx.fillStyle = 'rgb(0,0,0)';
                    var mb = Math.round(bp / 1000000);
                    tipCtx.fillText(g.name + " @ " + mb + " MB", 3, 12);
                    hit = true;
                    break;
                }
            }
            if (!hit) {
                tipCanvas.style.left = "-2000px";
            }
        }
        this.canvas.onclick = function (e) {

            var mouseCoords = igv.translateMouseCoordinates(e, canvas);
            var mouseX = mouseCoords.x;
            var mouseY = mouseCoords.y;
            igv.navigateKaryo(mouseX, mouseY);
        }

    };

    // Move location of the reference panel by clicking on the genome ideogram
    igv.navigateKaryo = function (mouseX, mouseY) {
        // check each chromosome if the coordinates are within its bound
        for (var i = 0; i < igv.guichromosomes.length; i++) {
            var g = igv.guichromosomes[i];
            if (g.x < mouseX && g.right > mouseX && g.y < mouseY && g.bottom > mouseY) {
                var dy = mouseY - g.y;
                var center = Math.round(g.size * dy / g.h);
                log("Going to position " + center);

                // the goto() signature is chr, start, end. We leave end undefined changing
                // the interpretation of start to the center of the locus extent.
                igv.browser.goto(g.name, center, undefined);
                break;
            }
        }

        igv.browser.update();
    };

    igv.KaryoPanel.prototype.resize = function () {

        var canvas = this.canvas;
        canvas.setAttribute('width', canvas.clientWidth);    //Must set the width & height of the canvas
        canvas.setAttribute('height', canvas.clientHeight);
        log("Resize called: width=" + canvas.clientWidth + "/" + canvas.clientHeight);
        this.ideograms = undefined;
        this.repaint();
    }

    igv.KaryoPanel.prototype.repaint = function () {


        var genome = igv.browser.genome,
            referenceFrame = igv.browser.referenceFrame,
            stainColors = [],
            w = this.canvas.width,
            h = this.canvas.height;

        this.ctx.clearRect(0, 0, w, h);

        if (!(genome && referenceFrame && genome.chromosomes && referenceFrame.chr)) return;

        var chromosomes = genome.getChromosomes();
        var image = this.ideograms;


        if (chromosomes.length < 1) {
            log("No chromosomes yet, returning");
            return;
        }
        var nrchr = 24;
        var nrrows = 1;
        if (w < 300) nrrows = 2;

        var totalchrwidth = Math.min(50, (w - 20) / (nrchr + 2) * nrrows);

        var chrwidth = Math.min(20, totalchrwidth / 2);
        // allow for 2 rows!

        var top = 25;
        var chrheight = ((h-25) / nrrows) - top;

        var longestChr = genome.getLongestChromosome();
        var cytobands = genome.getCytobands(longestChr.name);      // Longest chr

        var me = this;
        var maxLen = cytobands[cytobands.length - 1].end;

        if (!image || image == null) {
            drawImage.call(this);
        }

        this.ctx.drawImage(image, 0, 0);

        // Draw red box
        this.ctx.save();

        // Translate chr to official name
        var chr = referenceFrame.chr;
        if (this.genome) {
            chr = this.genome.getChromosomeName(chr);
        }
        var chromosome = igv.browser.genome.getChromosome(chr);
        if (chromosome) {
            var ideoScale = longestChr.bpLength / chrheight;   // Scale in bp per pixels

            var boxPY1 = chromosome.y - 3 + Math.round(referenceFrame.start / ideoScale);
            var boxHeight = Math.max(3, (igv.browser.trackViewportWidth() * referenceFrame.bpPerPixel) / ideoScale);

            //var boxPY2 = Math.round((this.browser.referenceFrame.start+100) * ideoScale);
            this.ctx.strokeStyle = "rgb(150, 0, 0)";
            this.ctx.lineWidth = 2;
            this.ctx.strokeRect(chromosome.x - 3, boxPY1, chrwidth + 6, boxHeight + 6);
            this.ctx.restore();
        }
        else log("Could not find chromosome " + chr);


        function drawImage() {
            image = document.createElement('canvas');
            image.width = w;

            image.height = h;
            var bufferCtx = image.getContext('2d');
            var nr = 0;
            var col = 0;
            var row = 1;
            var y = top;
            igv.guichromosomes = [];
            for (chr in chromosomes) {
                if (nr > nrchr) break;
                if (row == 1 && nrrows == 2 && nr + 1 > nrchr / 2) {
                    row = 2;
                    col = 0;
                    y = y + chrheight + top;
                }
                nr++;
                col++;
                //log("Found chr "+chr);
                var chromosome = genome.getChromosome(chr);
                if (chr == 'chrM' && !chromosome.bpLength) chromosome.bpLength = 16000;
                chromosome.x = col * totalchrwidth;
                chromosome.y = y;

                var guichrom = new Object();
                guichrom.name = chr;
                igv.guichromosomes.push(guichrom);

                drawIdeogram(guichrom, chromosome.x, chromosome.y, chromosome, bufferCtx, chrwidth, chrheight, maxLen);

            }
            this.ideograms = image;

            // now add some tracks?
            log("============= PROCESSING " + igv.browser.trackViews.length + " TRACKS");
            var tracknr = 0;
            for (var i = 0; i < igv.browser.trackViews.length; i++) {
                var trackPanel = igv.browser.trackViews[i];
                var track = trackPanel.track;
                if (track.getSummary && track.loadSummary) {
                    log("Found track with summary: " + track.name);

                    var source = track;

                    window.source = track;
                    source.loadSummary("chr1", 0, 1000000, function (featureList) {
                        if (featureList) {
                            //log("Got summary feature list, will add to karyo track")
                            nr = 0;
                            for (chr in chromosomes) {
                                var guichrom = igv.guichromosomes[nr];
                                //if (nr > 1) break;                       
                                nr++;
                                if (guichrom && guichrom.size) {
                                    loadfeatures(source, chr, 0, guichrom.size, guichrom, bufferCtx, tracknr);
                                }
                            }
                        }
                        else {
                            //  log("Track and chr "+chr+" has no summary features");
                        }
                    });
                    tracknr++;
                }
            }
        }

        function drawFeatures(source, featurelist, guichrom, ideogramLeft, top, bufferCtx, ideogramWidth, ideogramHeight, longestChr, tracknr) {
            if (!genome) {
                //log("no genome");
                return;
            }
            if (!guichrom) {
                //log("no chromosome");
                return;
            }
            if (!featurelist) {
                //log("Found no summary features on "+guichrom );
                return;
            }
            var len = featurelist.length;
            if (len == 0) {
                //log("Found no summary features on "+guichrom );
                return;
            }
            var scale = ideogramHeight / longestChr;
            //  log("drawing " + len + " feaures of chrom " + guichrom.name);
            var dx = 1;
            for (var i = 0; i < featurelist.length; i++) {
                var feature = featurelist[i];
                var color = 'rgb(0,0,150)';
                var value = feature.score;
                if (source.getColor) {
                    color = source.getColor(value);
                    // log("got color: "+color+" for value "+value);
                }

                var starty = scale * feature.start + top;
                var endy = scale * feature.end + top;
                var dy = Math.max(0.01, endy - starty);
                //    if (i < 3) log("Drawing feature  " + feature.start + "-" + feature.end + " -> " + starty + ", dy=" + dy);
                bufferCtx.fillStyle = color; //g2D.setColor(getCytobandColor(cytoband));
                bufferCtx.fillRect(ideogramLeft + ideogramWidth + tracknr * 2 + 1, starty, dx, dy);

            }
        }

        function drawIdeogram(guichrom, ideogramLeft, top, chromosome, bufferCtx, ideogramWidth, ideogramHeight, longestChr) {

            if (!genome) return;
            if (!chromosome) return;

            var cytobands = genome.getCytobands(chromosome.name);

            if (cytobands) {

                var centerx = (ideogramLeft + ideogramWidth / 2);

                var xC = [];
                var yC = [];

                var len = cytobands.length;
                if (len == 0) {
                    //log("Chr "+JSON.stringify(chromosome)+" has no length");
                    //return;
                }
                var scale = ideogramHeight / longestChr;

                guichrom.x = ideogramLeft;
                guichrom.y = top;
                guichrom.w = ideogramWidth;
                guichrom.right = ideogramLeft + ideogramWidth;
                var last = 0;
                var lastPY = -1;
                if (len > 0) {
                    last = cytobands[len - 1].end;
                    guichrom.h = scale * last;
                    guichrom.size = last;
                }
                else {
                    var MINH = 5;
                    lastPY = top + MINH;
                    guichrom.h = MINH;
                    guichrom.size = MINH / scale;
                }

                guichrom.longest = longestChr;
                guichrom.bottom = top + guichrom.h;

                if (len > 0) {
                    for (var i = 0; i < cytobands.length; i++) {
                        var cytoband = cytobands[i];

                        var starty = scale * cytoband.start + top;
                        var endy = scale * cytoband.end + top;
                        if (endy > lastPY) {
                            if (cytoband.type == 'c') { // centermere: "acen"
                                if (cytoband.name.charAt(0) == 'p') {
                                    yC[0] = starty;
                                    xC[0] = ideogramWidth + ideogramLeft;
                                    yC[1] = starty;
                                    xC[1] = ideogramLeft;
                                    yC[2] = endy;
                                    xC[2] = centerx;
                                } else {
                                    yC[0] = endy;
                                    xC[0] = ideogramWidth + ideogramLeft;
                                    yC[1] = endy;
                                    xC[1] = ideogramLeft;
                                    yC[2] = starty;
                                    xC[2] = centerx;
                                }
                                // centromer: carl wants another color
                                bufferCtx.fillStyle = "rgb(220, 150, 100)"; //g2D.setColor(Color.RED.darker());
                                bufferCtx.strokeStyle = "rgb(150, 0, 0)"; //g2D.setColor(Color.RED.darker());
                                bufferCtx.polygon(xC, yC, 1, 0);
                                // g2D.fillPolygon(xC, yC, 3);
                            } else {
                                var dy = endy - starty;

                                bufferCtx.fillStyle = getCytobandColor(cytoband); //g2D.setColor(getCytobandColor(cytoband));
                                bufferCtx.fillRect(ideogramLeft, starty, ideogramWidth, dy);
                            }
                        }

                        lastPY = endy;
                    }

                }
            }
            bufferCtx.fillStyle = null;
            bufferCtx.lineWidth = 1;
            bufferCtx.strokeStyle = "darkgray";
            var r = ideogramWidth / 2;
            bufferCtx.roundRect(ideogramLeft, top - r / 2, ideogramWidth, lastPY - top + r, ideogramWidth / 2, 0, 1);

            // draw chromosome name

            bufferCtx.font = "bold 10px Arial";
            bufferCtx.fillStyle = "rgb(0, 0, 0)";
            var name = chromosome.name;
            if (name.length > 3) name = name.substring(3);
            //log("Drawing chr name "+name+" at "+(ideogramLeft + ideogramWidth / 2 - 3*name.length));
            bufferCtx.fillText(name, ideogramLeft + ideogramWidth / 2 - 3 * name.length, top - 10);
        }

        function getCytobandColor(data) {
            if (data.type == 'c') { // centermere: "acen"
                return "rgb(150, 10, 10)"

            } else {
                var stain = data.stain; // + 4;

                var shade = 230;
                if (data.type == 'p') {
                    shade = Math.floor(230 - stain / 100.0 * 230);
                }
                var c = stainColors[shade];
                if (c == null) {
                    c = "rgb(" + shade + "," + shade + "," + shade + ")";
                    stainColors[shade] = c;
                }
                //log("Got color: "+c);
                return c;

            }
        }

        function loadfeatures(source, chr, start, end, guichrom, bufferCtx, tracknr) {
            //log("=== loadfeatures of chr " + chr + ", x=" + guichrom.x);            

            source.getSummary(chr, start, end, function (featureList) {
                if (featureList) {
                    len = featureList.length;
                    //log(" -->- loaded: chrom " + chr + " with " + len + " summary features, drawing them");
                    drawFeatures(source, featureList, guichrom, guichrom.x, guichrom.y, bufferCtx, chrwidth, chrheight, maxLen, tracknr);
                    me.repaint();
                }
                else {
                    //log("Track and chr "+chr+" has no summary features yet");
                }
            });

        }

    }

    return igv;
})
(igv || {});
/**
 * OAuth object provided for example pages.
 */
var oauth = (function (oauth) {

    // Define singleton object for google oauth

    if (!oauth.google) {

        var OAUTHURL = 'https://accounts.google.com/o/oauth2/auth?';
        var VALIDURL = 'https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=';
        var SCOPE = 'https://www.googleapis.com/auth/genomics';
        var CLIENTID = '661332306814-8nt29308rppg325bkq372vli8nm3na14.apps.googleusercontent.com';
        var REDIRECT = 'http://localhost/igv-web/emptyPage.html'
        var LOGOUT = 'http://accounts.google.com/Logout';
        var TYPE = 'token';
        var _url = OAUTHURL +
            "scope=https://www.googleapis.com/auth/cloud-platform https://www.googleapis.com/auth/genomics https://www.googleapis.com/auth/devstorage.read_only https://www.googleapis.com/auth/userinfo.profile&" +
            "state=%2Fprofile&" +
            "redirect_uri=http%3A%2F%2Flocalhost%2Figv-web%2FemptyPage.html&" +
            "response_type=token&" +
            "client_id=661332306814-8nt29308rppg325bkq372vli8nm3na14.apps.googleusercontent.com";

        var tokenType;
        var expiresIn;
        var user;
        var loggedIn = false;

        oauth.google = {

            login: function (callback) {
                var win = window.open(_url, "windowname1", 'width=800, height=600');

                var pollTimer = window.setInterval(function () {
                    try {
                        console.log(win.document.URL);
                        if (win.document.URL.indexOf(REDIRECT) != -1) {
                            window.clearInterval(pollTimer);
                            var url = win.document.URL;
                            oauth.google.access_token = oauth.google.gup(url, 'access_token');
                            tokenType = oauth.google.gup(url, 'token_type');
                            expiresIn = oauth.google.gup(url, 'expires_in');
                            win.close();

                            oauth.google.validateToken(oauth.google.access_token);

                            if(callback) {
                                callback();
                            }
                        }
                    } catch (e) {
                    }
                }, 500);
            },

            validateToken: function (token) {
                $.ajax({

                    url: VALIDURL + token,
                    data: null,
                    success: function (responseText) {
                        oauth.google.getUserInfo();
                        loggedIn = true;
                        //$('#loginText').hide();
                        //$('#logoutText').show();
                    },
                    dataType: "jsonp"
                });
            },

            getUserInfo: function () {
                $.ajax({
                    url: 'https://www.googleapis.com/oauth2/v1/userinfo?access_token=' + oauth.google.access_token,
                    data: null,
                    success: function (resp) {
                        user = resp;
                        console.log(user);
                        //$('#uName').text('Welcome ' + user.name);
                        //$('#imgHolder').attr('src', user.picture);
                    },
                    dataType: "jsonp"
                });
            },

            //credits: http://www.netlobo.com/url_query_string_javascript.html
            gup: function (url, name) {
                name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
                var regexS = "[\\#&]" + name + "=([^&#]*)";
                var regex = new RegExp(regexS);
                var results = regex.exec(url);
                if (results == null)
                    return "";
                else
                    return results[1];
            }
        }
    }

    return oauth;
})(oauth || {});


var igv = (function (igv) {

    igv.oauth = oauth;


    return igv;

})(igv || {});



function testOauth() {

    var url = "https://accounts.google.com/o/oauth2/auth?" +
        "scope=https://www.googleapis.com/auth/genomics&" +
        "state=%2Fprofile&" +
        "redirect_uri=http%3A%2F%2Flocalhost%2Figv-web%2FemptyPage.html&" +
        "response_type=token&" +
        "client_id=661332306814-8nt29308rppg325bkq372vli8nm3na14.apps.googleusercontent.com";


    $.ajax(url, {

        success: function (data, status, xhr) {
            console.log(status);
        },
        error: function (xhr, options, e) {
            var statusCode =  xhr.statusCode();
            console.log(xhr.getResponseHeader("location"));
        },
        complete: function (xhr, status) {
            console.log(status);
        }
    });


}
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by turner on 2/17/14.
 */
var igv = (function (igv) {

    igv.isBlank = function (line) {

        var meh = line.match(/\S+/g);
        return !meh;
    };

    igv.isComment = function (line) {

        var index = line.indexOf("#");
        return 0 == index;
    };


    /**
     * Parse the document url query string for the entered parameter.
     *
     * @param name
     * @returns {*}
     */
    igv.getQueryValue = function (name) {
        name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
        var regexS = "[\\?&]" + name + "=([^&#]*)";
        var regex = new RegExp(regexS);
        var results = regex.exec(window.location.href);
        if (results == null)
            return undefined;
        else
            return results[1];
    };




    return igv;
})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// Reference frame classes.  Converts domain coordinates (usually genomic) to pixel coordinates

var igv = (function (igv) {


    igv.ReferenceFrame = function (chr, start, bpPerPixel) {
        this.chr = chr;
        this.start = start;
        this.bpPerPixel = bpPerPixel;
    }

    igv.ReferenceFrame.prototype.toPixels = function (bp) {
        // TODO -- do we really need ot round this?
        return bp / this.bpPerPixel;
    }

    igv.ReferenceFrame.prototype.toBP = function(pixels) {
        return this.bpPerPixel * pixels;
    }

    igv.ReferenceFrame.prototype.shiftPixels = function(pixels) {
        this.start += pixels * this.bpPerPixel;
    }

    igv.ReferenceFrame.prototype.description = function() {
        return "ReferenceFrame " + this.chr + " " + igv.numberFormatter(Math.floor(this.start)) + " bpp " + this.bpPerPixel;
    }


    return igv;

})(igv || {})
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    //
    igv.RulerTrack = function () {

        this.height = 50;
        this.name = "";
        this.id = "ruler";
        this.disableButtons = true;
        this.ignoreTrackMenu = true;
        this.order = -Number.MAX_VALUE;

    };

    igv.RulerTrack.prototype.getFeatures = function (chr, bpStart, bpEnd) {

        return new Promise(function (fulfill, reject) {
            fulfill([]);
        });
    }

    igv.RulerTrack.prototype.draw = function (options) {

        var fontStyle,
            ctx = options.context,
            range,
            ts,
            spacing,
            nTick,
            x;

        fontStyle = { textAlign: 'center', font: '10px PT Sans', fillStyle: "rgba(64, 64, 64, 1)", strokeStyle: "rgba(64, 64, 64, 1)" };

        range = Math.floor(1100 * options.bpPerPixel);
        ts = findSpacing(range);
        spacing = ts.majorTick;

        // Find starting point closest to the current origin
        nTick = Math.floor(options.bpStart / spacing) - 1;
        x = 0;

        //canvas.setProperties({textAlign: 'center'});
        igv.graphics.setProperties(ctx, fontStyle );
        while (x < options.pixelWidth) {

            var l = Math.floor(nTick * spacing),
                shim = 2;

            x = Math.round(((l - 1) - options.bpStart + 0.5) / options.bpPerPixel);
            var chrPosition = formatNumber(l / ts.unitMultiplier, 0) + " " + ts.majorUnit;

            if (nTick % 1 == 0) {
                igv.graphics.fillText(ctx, chrPosition, x, this.height - 15);
            }

            igv.graphics.strokeLine(ctx, x, this.height - 10, x, this.height - shim);

            nTick++;
        }
        igv.graphics.strokeLine(ctx, 0, this.height - shim, options.pixelWidth, this.height - shim);


        function formatNumber(anynum, decimal) {
            //decimal  - the number of decimals after the digit from 0 to 3
            //-- Returns the passed number as a string in the xxx,xxx.xx format.
            //anynum = eval(obj.value);
            var divider = 10;
            switch (decimal) {
                case 0:
                    divider = 1;
                    break;
                case 1:
                    divider = 10;
                    break;
                case 2:
                    divider = 100;
                    break;
                default:       //for 3 decimal places
                    divider = 1000;
            }

            var workNum = Math.abs((Math.round(anynum * divider) / divider));

            var workStr = "" + workNum

            if (workStr.indexOf(".") == -1) {
                workStr += "."
            }

            var dStr = workStr.substr(0, workStr.indexOf("."));
            var dNum = dStr - 0
            var pStr = workStr.substr(workStr.indexOf("."))

            while (pStr.length - 1 < decimal) {
                pStr += "0"
            }

            if (pStr == '.') pStr = '';

            //--- Adds a comma in the thousands place.
            if (dNum >= 1000) {
                var dLen = dStr.length
                dStr = parseInt("" + (dNum / 1000)) + "," + dStr.substring(dLen - 3, dLen)
            }

            //-- Adds a comma in the millions place.
            if (dNum >= 1000000) {
                dLen = dStr.length
                dStr = parseInt("" + (dNum / 1000000)) + "," + dStr.substring(dLen - 7, dLen)
            }
            var retval = dStr + pStr
            //-- Put numbers in parentheses if negative.
            if (anynum < 0) {
                retval = "(" + retval + ")";
            }

            //You could include a dollar sign in the return value.
            //retval =  "$"+retval
            return retval;
        }


    };

    function TickSpacing(majorTick, majorUnit, unitMultiplier) {
        this.majorTick = majorTick;
        this.majorUnit = majorUnit;
        this.unitMultiplier = unitMultiplier;
    }

    function findSpacing(maxValue) {

        if (maxValue < 10) {
            return new TickSpacing(1, "", 1);
        }


        // Now man zeroes?
        var nZeroes = Math.floor(log10(maxValue));
        var majorUnit = "";
        var unitMultiplier = 1;
        if (nZeroes > 9) {
            majorUnit = "gb";
            unitMultiplier = 1000000000;
        }
        if (nZeroes > 6) {
            majorUnit = "mb";
            unitMultiplier = 1000000;
        } else if (nZeroes > 3) {
            majorUnit = "kb";
            unitMultiplier = 1000;
        }

        var nMajorTicks = maxValue / Math.pow(10, nZeroes - 1);
        if (nMajorTicks < 25) {
            return new TickSpacing(Math.pow(10, nZeroes - 1), majorUnit, unitMultiplier);
        } else {
            return new TickSpacing(Math.pow(10, nZeroes) / 2, majorUnit, unitMultiplier);
        }

        function log10(x) {
            var dn = Math.log(10);
            return Math.log(x) / dn;
        }
    }

    return igv;
})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    igv.SequenceTrack = function (config) {
        this.name = "";
        this.id = "sequence";
        this.sequenceType = config.sequenceType || "dna";             //   dna | rna | prot
        this.height = 15;
        this.disableButtons = true;
        this.order = config.order || 9999;
        this.ignoreTrackMenu = true;
    };

    igv.SequenceTrack.prototype.getFeatures = function (chr, bpStart, bpEnd) {

        return new Promise(function (fulfill, reject) {
            if (igv.browser.referenceFrame.bpPerPixel > 1/*igv.browser.trackViewportWidthBP() > 30000*/) {
                fulfill(null);
            }
            else {
                igv.browser.genome.sequence.getSequence(chr, bpStart, bpEnd).then(fulfill).catch(reject);
            }
        });
    }


    igv.SequenceTrack.prototype.draw = function (options) {

        var sequence = options.features,
            ctx = options.context,
            bpPerPixel = options.bpPerPixel,
            bpStart = options.bpStart,
            pixelWidth = options.pixelWidth,
            bpEnd = bpStart + pixelWidth * bpPerPixel + 1,
            len, w, y, pos, offset, b, p0, p1, pc, c;

        if (sequence) {

            len = sequence.length;
            w = 1 / bpPerPixel;

            y = this.height / 2;
            for (pos = bpStart; pos <= bpEnd; pos++) {

                offset = pos - bpStart;
                if (offset < len) {
//                            var b = sequence.charAt(offset);
                    b = sequence[offset];
                    p0 = Math.floor(offset * w);
                    p1 = Math.floor((offset + 1) * w);
                    pc = Math.round((p0 + p1) / 2);

                    if (this.color) {
                        c = this.color;
                    }
                    else if ("dna" === this.sequenceType) {
                        c = igv.nucleotideColors[b];
                    }
                    else {
                        c = "rgb(0, 0, 150)";
                    }

                    if (!c) c = "gray";

                    if (bpPerPixel > 1 / 10) {

                        igv.graphics.fillRect(ctx, p0, 0, p1 - p0, 10, {fillStyle: c});
                    }
                    else {

                        igv.graphics.strokeText(ctx, b, pc, y, {
                            strokeStyle: c,
                            font: 'normal 10px Arial',
                            textAlign: 'center'
                        });
                    }
                }
            }
        }

    };

    return igv;
})
(igv || {});




/*
 * Copyright - unknown
 */

"use strict";
// Source:  https://github.com/jfriend00/Javascript-Set/blob/master/set.js

//-------------------------------------------
// Implementation of a Set in javascript
//
// Supports any element type that can uniquely be identified
//    with its string conversion (e.g. toString() operator).
// This includes strings, numbers, dates, etc...
// It does not include objects or arrays though
//    one could implement a toString() operator
//    on an object that would uniquely identify
//    the object.
//
// Uses a javascript object to hold the Set
//
// s.add(key)                      // adds a key to the Set (if it doesn't already exist)
// s.add(key1, key2, key3)         // adds multiple keys
// s.add([key1, key2, key3])       // adds multiple keys
// s.add(otherSet)                 // adds another Set to this Set
// s.add(arrayLikeObject)          // adds anything that a subclass returns true on _isPseudoArray()
// s.remove(key)                   // removes a key from the Set
// s.remove(["a", "b"]);           // removes all keys in the passed in array
// s.remove("a", "b", ["first", "second"]);   // removes all keys specified
// s.has(key)                      // returns true/false if key exists in the Set
// s.hasAll(args)                  // returns true if s has all the keys in args
// s.equals(otherSet)              // returns true if s has exactly the same keys in it as otherSet
// s.isEmpty()                     // returns true/false for whether Set is empty
// s.keys()                        // returns an array of keys in the Set
// s.clear()                       // clears all data from the Set
// s.union(t)                      // return new Set that is union of both s and t
// s.intersection(t)               // return new Set that has keys in both s and t
// s.difference(t)                 // return new Set that has keys in s, but not in t
// s.isSubset(t)                   // returns boolean whether every element in s is in t
// s.isSuperset(t)                 // returns boolean whether every element of t is in s
// s.each(fn)                      // iterate over all items in the Set (return this for method chaining)
// s.eachReturn(fn)                // iterate over all items in the Set (return true/false if iteration was not stopped)
// s.filter(fn)                    // return a new Set that contains keys that passed the filter function
// s.map(fn)                       // returns a new Set that contains whatever the callback returned for each item
// s.every(fn)                     // returns true if every element in the Set passes the callback, otherwise returns false
// s.some(fn)                      // returns true if any element in the Set passes the callback, otherwise returns false
//-------------------------------------------


// polyfill for Array.isArray
if (!Array.isArray) {
    Array.isArray = function (vArg) {
        return Object.prototype.toString.call(vArg) === "[object Array]";
    };
}

if (typeof Set !== "undefined") {

    Set.prototype.isEmpty = function () {
        return this.size === 0;
    }

    Set.prototype.addAll = function (arrayOrSet) {

        if (Array.isArray(arrayOrSet) || this._isPseudoArray(arrayOrSet)) {
            for (var j = 0; j < arrayOrSet.length; j++) {
                this.add(arrayOrSet[j]);
            }
        } else if (arrayOrSet instanceof Set) {
            var self = this;
            arrayOrSet.each(function (val, key) {
                self.add(key, val);
            });
        }
    }
}
else {
    Set = function (/*initialData*/) {
        // Usage:
        // new Set()
        // new Set(1,2,3,4,5)
        // new Set(["1", "2", "3", "4", "5"])
        // new Set(otherSet)
        // new Set(otherSet1, otherSet2, ...)
        this.data = {};
        this.add.apply(this, arguments);
    }

    Set.prototype = {
        // usage:
        // add(key)
        add: function () {
            var key;
            for (var i = 0; i < arguments.length; i++) {
                key = arguments[i];
                if (Array.isArray(key) || this._isPseudoArray(key)) {
                    for (var j = 0; j < key.length; j++) {
                        this._add(key[j]);
                    }
                } else if (key instanceof Set) {
                    var self = this;
                    key.each(function (val, key) {
                        self._add(key, val);
                    });
                } else {
                    // just a key, so add it
                    this._add(key);
                }
            }
            return this;
        },

        addAll: function (arrayOrSet) {

            if (Array.isArray(arrayOrSet) || this._isPseudoArray(arrayOrSet)) {
                for (var j = 0; j < arrayOrSet.length; j++) {
                    this._add(arrayOrSet[j]);
                }
            } else if (arrayOrSet instanceof Set) {
                var self = this;
                arrayOrSet.each(function (val, key) {
                    self._add(key, val);
                });
            }

            return this;
        },
        // private methods (used internally only)
        // these make non-public assumptions about the internal data format
        // add a single item to the Set, make sure key is a string
        _add: function (key, val) {
            if (typeof val === "undefined") {
                // store the val (before being converted to a string key)
                val = key;
            }
            this.data[this._makeKey(key)] = val;
            return this;
        },
        // private: fetch current key
        // overridden by subclasses for custom key handling
        _getKey: function (arg) {
            return arg;
        },
        // private: fetch current key or coin a new one if there isn't already one
        // overridden by subclasses for custom key handling
        _makeKey: function (arg) {
            return arg;
        },
        // private: to remove a single item
        // does not have all the argument flexibility that remove does
        _removeItem: function (key) {
            delete this.data[this._getKey(key)];
        },
        // private: asks subclasses if this is something we want to treat like an array
        // default implementation is false
        _isPseudoArray: function (item) {
            return false;
        },
        // usage:
        // remove(key)
        // remove(key1, key2, key3)
        // remove([key1, key2, key3])
        delete: function (key) {
            // can be one or more args
            // each arg can be a string key or an array of string keys
            var item;
            for (var j = 0; j < arguments.length; j++) {
                item = arguments[j];
                if (Array.isArray(item) || this._isPseudoArray(item)) {
                    // must be an array of keys
                    for (var i = 0; i < item.length; i++) {
                        this._removeItem(item[i]);
                    }
                } else {
                    this._removeItem(item);
                }
            }
            return this;
        },
        // returns true/false on whether the key exists
        has: function (key) {
            key = this._makeKey(key);
            return Object.prototype.hasOwnProperty.call(this.data, key);
        },
        // returns true/false for whether the current Set contains all the passed in keys
        // takes arguments just like the constructor or .add()
        hasAll: function (args) {
            var testSet = this.makeNew.apply(this, arguments);
            var self = this;
            return testSet.every(function (data, key) {
                return self.has(key);
            });
        },
        // if first arg is not a set, make it into one
        // otherwise just return it
        makeSet: function (args) {
            if (!(args instanceof Set)) {
                // pass all arguments here
                return this.makeNew.apply(this, arguments);
            }
            return args;
        },
        equals: function (otherSet) {
            otherSet = this.makeSet(otherSet);
            // this is not particularly efficient, but it's simple
            // the only way you can be a subset and a superset it to be the same Set
            return this.isSubset(otherSet) && this.isSuperset(otherSet);
        },
        // tells you if the Set is empty or not
        isEmpty: function () {
            for (var key in this.data) {
                if (this.has(key)) {
                    return false;
                }
            }
            return true;
        },

        size: function () {
            var size = 0;
            for (var key in this.data) {
                if (this.has(key)) {
                    size++;
                }
            }
            return size;
        },

        // returns an array of all keys in the Set
        // returns the original key (not the string converted form)
        keys: function () {
            var results = [];
            this.each(function (data) {
                results.push(data);
            });
            return results;
        },
        // clears the Set
        clear: function () {
            this.data = {};
            return this;
        },
        // makes a new Set of the same type and configuration as this one
        // regardless of what derived type of object we actually are
        // accepts same arguments as a constructor for initially populating the Set
        makeNew: function () {
            var newSet = new this.constructor();
            if (arguments.length) {
                newSet.add.apply(newSet, arguments);
            }
            return newSet;
        },
        // s.union(t)
        // returns a new Set that is the union of two sets
        union: function (otherSet) {
            otherSet = this.makeSet(otherSet);
            var newSet = this.makeNew(this);
            newSet.add(otherSet);
            return newSet;
        },
        // s.intersection(t)
        // returns a new Set that contains the keys that are
        // in both sets
        intersection: function (otherSet) {
            otherSet = this.makeSet(otherSet);
            var newSet = this.makeNew();
            this.each(function (data, key) {
                if (otherSet.has(key)) {
                    newSet._add(key, data);
                }
            });
            return newSet;
        },
        // s.difference(t)
        // returns a new Set that contains the keys that are
        // s but not in t
        difference: function (otherSet) {
            otherSet = this.makeSet(otherSet);
            var newSet = this.makeNew();
            this.each(function (data, key) {
                if (!otherSet.has(key)) {
                    newSet._add(key, data);
                }
            });
            return newSet;
        },
        // s.notInBoth(t)
        // returns a new Set that contains the keys that
        // are in either Set, but not both sets
        notInBoth: function (otherSet) {
            otherSet = this.makeSet(otherSet);
            // get items in s, but not in t
            var newSet = this.difference(otherSet);
            // add to the result items in t, but not in s
            return newSet.add(otherSet.difference(this));
        },
        // s.isSubset(t)
        // returns boolean whether every element of s is in t
        isSubset: function (otherSet) {
            otherSet = this.makeSet(otherSet);
            return this.eachReturn(function (data, key) {
                if (!otherSet.has(key)) {
                    return false;
                }
            });
        },
        // s.isSuperset(t)
        // returns boolean whether every element of t is in s
        isSuperset: function (otherSet) {
            otherSet = this.makeSet(otherSet);
            var self = this;
            return otherSet.eachReturn(function (data, key) {
                if (!self.has(key)) {
                    return false;
                }
            });
        },
        // iterate over all elements in the Set until callback returns false
        // myCallback(key) is the callback form
        // If the callback returns false, then the iteration is stopped
        // returns the Set to allow method chaining
        each: function (fn) {
            this.eachReturn(fn);
            return this;
        },
        // iterate all elements until callback returns false
        // myCallback(key) is the callback form
        // returns false if iteration was stopped
        // returns true if iteration completed
        eachReturn: function (fn) {
            for (var key in this.data) {
                if (this.has(key)) {
                    if (fn.call(this, this.data[key], key) === false) {
                        return false;
                    }
                }
            }
            return true;
        },
        // iterate all elements and call callback function on each one
        // myCallback(key) - returns true to include in returned Set
        // returns new Set
        filter: function (fn) {
            var newSet = this.makeNew();
            this.each(function (data, key) {
                if (fn.call(this, key) === true) {
                    newSet._add(key, data);
                }
            });
            return newSet;
        },
        // iterate all elements and call callback on each one
        // myCallback(key) - whatever value is returned is put in the returned Set
        // if the  return value from the callback is undefined,
        //   then nothing is added to the returned Set
        // returns new Set
        map: function (fn) {
            var newSet = this.makeNew();
            this.each(function (data, key) {
                var ret = fn.call(this, key);
                if (typeof ret !== "undefined") {
                    newSet._add(key, data);
                }
            });
            return newSet;
        },
        // tests whether some element in the Set passes the test
        // myCallback(key) - returns true or false
        // returns true if callback returns true for any element,
        //    otherwise returns false
        some: function (fn) {
            var found = false;
            this.eachReturn(function (key) {
                if (fn.call(this, key) === true) {
                    found = true;
                    return false;
                }
            });
            return found;
        },
        // tests whether every element in the Set passes the test
        // myCallback(key) - returns true or false
        // returns true if callback returns true for every element
        every: function (fn) {
            return this.eachReturn(fn);
        }
    };

    Set.prototype.constructor = Set;

}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var igv = (function (igv) {

    var transformations;

    /**
     *
     * @constructor
     *
     */
    igv.SVG = function () {
        this.svg = '';
        this.contents = [];
        transformations = [];
    };


    /**
     * Set styling properties. Returns a string for the 'style' attribute.
     *
     * @param properties - object with SVG properties
     *
     * @returns {string}
     */
    igv.SVG.prototype.setProperties = function (properties) {

        var str = '';

        for (var key in properties) {
            if (properties.hasOwnProperty(key)) {
                var value = properties[key];

                if (key === 'font-family') {
                    str += 'font-family:' + value + ';';
                } else if (key === 'font-size') {
                    str += 'font-size:' + value + ';';
                } else if (key == 'fillStyle') {
                    str += 'fill:' + value + ';';
                } else if (key === 'fill') {
                    str += 'fill:' + value + ';';
                } else if (key == 'strokeStyle') {
                    str += 'stroke:' + value + ';';
                } else if (key === 'stroke') {
                    str += 'stroke:' + value + ';';
                } else if (key === 'stroke-width') {
                    str += 'stroke-width:' + value + ';';
                } else {
                    console.log('Unknown property: ' + key);
                }
            }
        }



        //if (str != '') {
        //    return str;
        //}

        // TODO: What should be done if there are no properties in the object?
        return str;

    };

    igv.SVG.prototype.setTransforms = function (transforms, x, y) {
        var str = '';

        for (var key in transforms) {
            if (transforms.hasOwnProperty(key)) {
                var value = transforms[key];

                if (key === 'rotate') {
                    str += 'rotate(' + value['angle'];

                    str += ',' + x;
                    str += ',' + y;

                    str += ')';

                } else if (key === 'translate') {
                    str += 'translate(' + value[x];
                    if ('y' in value) {
                        str += ',' + value['y'];
                    }

                    str += ')';
                } else {
                    console.log('Unknown transform: ' + key);
                }
            }

            str += ' ';
        }

        // TODO: What should be done if there are no transformations in the object?
        return str;
    };

    igv.SVG.prototype.clearRect = function (x, y, w, h) {

    }

    igv.SVG.prototype.strokeLine = function (x1, y1, x2, y2, properties, transforms) {
        var str = '';

        str += '<line x1="' + x1 + '" y1="' + y1 + '" x2="' + x2 + '" y2="' + y2 +'"';

        if (properties) {
            str += ' style="' + this.setProperties(properties) + '"';
        }

        if (transforms) {
            str += ' transform="' + this.setTransforms(transforms, x1, y1) + '"';
        }

        str += '/>';

        this.contents.push(str);
    };

    /**
     *
     * @param x - x coordinate - upper left corner.
     * @param y - y coordinate - upper left corner.
     * @param w - width of the rectangle expanding rightwards.
     * @param h - height of the rectangle expanding downwards.
     * @param properties - style attribute for the SVG rectangle.
     */
    igv.SVG.prototype.fillRect = function (x, y, w, h, properties, transforms) {
        var str = '';

        str += '<rect ' + 'x="' + x + '" y="' + y;
        str += '" width="' + w + '" height="' + h + '"';

        if (properties) {
            str += ' style="' + this.setProperties(properties) + '"';
        }

        if (transforms) {
            str += ' transform="' + this.setTransforms(transforms, x, y) + '"';
        }

        str += '/>';

        this.contents.push(str);

    };

    /**
     *
     * @param centerX - x coordinate - center of rectangle.
     * @param centerY - y coordinate - center of rectangle.
     * @param width - width of the rectangle.
     * @param height - height of the rectangle.
     * @param properties - style attribute for the SVG rectangle.
     */
    igv.SVG.prototype.fillRectWithCenter = function (centerX, centerY, width, height, properties, transforms) {
        var str = '';

        str += '<rect ' + 'x="' + (centerX - (width / 2)) + '" y="' + (centerY - (height / 2));
        str += '" width="' + width + '" height="' + height + '"';

        if (properties) {
            str += ' style="' + this.setProperties(properties) + '"';
        }

        if (transforms) {
            str += ' transform="' + this.setTransforms(transforms, centerX, centerY) + '"';
        }

        str += '/>';


        this.contents.push(str);
    };


    /**
     *
     * @param x - array of "x" values
     * @param y - array of "y" values
     * @param properties
     * @param transforms
     */
    igv.SVG.prototype.fillPolygon = function (x, y, properties, transforms) {
        var str = '';

        str += '<polygon points="';

        for (var index = 0; index < x.length; index++) {
            str += ' ' + x[index] + ',' + y[index];
        }

        str += '"';

        if (properties) {
            str += ' style="' + this.setProperties(properties) + '"';
        }

        if (transforms) {
            str += ' transform="' + this.setTransforms(transforms, x, y) + '"';
        }

        str += '/>';

        this.contents.push(str);

    };

    /**
     * Generates text on the svg canvas.
     *
     * @param text
     * @param x - x coordinate for the SVG text.
     * @param y - y coordinate for the SVG text.
     * @param properties - style attribute for the SVG text.
     * @param transforms
     */
    igv.SVG.prototype.fillText = function (text, x, y, properties, transforms) {
        var str = '';

        str += '<text x="' + x + '" y="' + y + '"';

        if (properties) {
            str += ' style="' + this.setProperties(properties) + '"';
        }

        if (transforms) {
            str += ' transform="' + this.setTransforms(transforms, x, y) + '"';
        }

        str += '>';
        str += text;
        str += '</text>';

        this.contents.push(str);
    };

    /**
     * TODO: This is a duplicate of fillText as SVG has fill and
     * TODO: stroke values for text instead of separate types.
     *
     * Generates text on the svg canvas.
     *
     * @param text
     * @param x - x coordinate for the SVG text.
     * @param y - y coordinate for the SVG text.
     * @param properties - style attribute for the SVG text.
     * @param transforms
     */
    igv.SVG.prototype.strokeText = function (text, x, y, properties, transforms) {
        var str = '';

        str += '<text x="' + x + '" y="' + y + '"';
        if (properties) {
            str += ' style="' + this.setProperties(properties) + '"';
        }

        if (transforms) {
            str += ' transform="' + this.setTransforms(transforms, x, y) + '"';
        }

        str += '>';
        str += text;
        str += '</text>';

        this.contents.push(str);
    };

    igv.SVG.prototype.strokeCircle = function (x, y, radius, properties, transforms) {
        var str = '';

        str += '<circle cx="' + x + '" cy="' + y + '" r="' + radius + '" stroke="black" fill-opacity="0.0"/>';

        this.contents.push(str);
    };

    /**
     * Convers the SVG object into a string to put in html.
     *
     * @returns {string}
     */
    igv.SVG.prototype.string = function () {
        var string = '';

        string += '<svg width="100%" height="100%" version="1.1" xmlns="http://www.w3.org/2000/svg">';

        for (var index = 0; index < this.contents.length; index++) {
            string += '\n' + this.contents[index];
        }

        //string += '<text x="350" y="250" transform="rotate(60 350 250)">Hello!</text>';

        string += '</svg>';

        return string;
    };

    igv.SVG.prototype.innerString = function () {
        var string = '';

        for (var index = 0; index < this.contents.length; index++) {
            string += '\n' + this.contents[index];
        }

        //string += '<text x="350" y="250" transform="rotate(60 350 250)">Hello!</text>';


        return string;
    };


    //igv.SVG.prototype.rotate = function(angle, x, y) {
    //    transformations.push('rotate(' + angle + ',' + x + ',' + y +')');
    //};

    //igv.SVG.prototype.translate

    return igv;
})(igv || {});



/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


// Generic functions applicable to all track types

var igv = (function (igv) {

    /**
     * Set defaults for properties applicable to all tracks.
     * Insure required "config" properties are set.
     * @param track
     * @param config
     */
    igv.configTrack = function (track, config) {

        track.config = config;
        track.url = config.url;

        config.name = config.name || config.label;   // synonym for name, label is deprecated
        if (config.name) {
            track.name = config.name;
        }
        else {
            if (config.localFile) track.name = config.localFile.name;
            else track.name = config.url;

        }

        track.id = config.id || track.name;   // TODO -- remove this property, not used

        track.order = config.order;
        track.color = config.color || igv.browser.constants.defaultColor;

        track.removable = config.removable === undefined ? true : config.removable;      // Defaults to true

        track.height = config.height || ('wig' === config.type ? 50 : 100);

        if(config.autoHeight === undefined)  config.autoHeight = config.autoheight; // Some case confusion in the initial releasae

        track.autoHeight = config.autoHeight === undefined ?
            (config.height === undefined ? true : false) :
            config.autoHeight;
        track.minHeight = config.minHeight || Math.min(50, track.height);
        track.maxHeight = config.maxHeight || Math.max(500, track.height);

        if (config.visibilityWindow) {
            track.visibilityWindow = config.visibilityWindow;
        }

        if(track.type === undefined) track.type = config.type;
    };


    igv.setTrackLabel = function (track, label) {

        track.name = label;

        $(track.trackView.viewportDiv).find('.igv-track-label').html(track.name);

        if (track.trackView) {
            track.trackView.repaint();
        }
    };

    igv.setTrackColor = function (track, color) {

        track.color = color;

        if (track.trackView) {

            track.trackView.repaint();

        }

    };

    igv.paintAxis = function (ctx, pixelWidth, pixelHeight) {

        var x1,
            x2,
            y1,
            y2,
            a,
            b,
            reference,
            shim,
            font = {
                'font': 'normal 10px Arial',
                'textAlign': 'right',
                'strokeStyle': "black"
            };

        if (undefined === this.dataRange || undefined === this.dataRange.max || undefined === this.dataRange.min) {
            return;
        }

        igv.graphics.fillRect(ctx, 0, 0, pixelWidth, pixelHeight, {'fillStyle': "rgb(255, 255, 255)"});

        reference = 0.95 * pixelWidth;
        x1 = reference - 8;
        x2 = reference;

        //shim = 0.5 * 0.125;
        shim = .01;
        y1 = y2 = shim * pixelHeight;

        a = {x: x2, y: y1};

        // tick
        igv.graphics.strokeLine(ctx, x1, y1, x2, y2, font);
        igv.graphics.fillText(ctx, prettyPrint(this.dataRange.max), x1 + 4, y1 + 12, font);

        //shim = 0.25 * 0.125;
        y1 = y2 = (1.0 - shim) * pixelHeight;

        b = {x: x2, y: y1};

        // tick
        igv.graphics.strokeLine(ctx, x1, y1, x2, y2, font);
        igv.graphics.fillText(ctx, prettyPrint(this.dataRange.min), x1 + 4, y1 - 4, font);

        igv.graphics.strokeLine(ctx, a.x, a.y, b.x, b.y, font);

        function prettyPrint(number) {
            // if number >= 100, show whole number
            // if >= 1 show 1 significant digits
            // if <  1 show 2 significant digits

            if (number === 0) {
                return "0";
            } else if (Math.abs(number) >= 10) {
                return number.toFixed();
            } else if (Math.abs(number) >= 1) {
                return number.toFixed(1);
            } else {
                return number.toFixed(2);
            }
        }

    };


    return igv;
})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


var igv = (function (igv) {

    igv.TrackView = function (track, browser) {

        var self = this,
            element;

        this.track = track;
        this.browser = browser;

        this.trackDiv = $('<div class="igv-track-div">')[0];
        $(browser.trackContainerDiv).append(this.trackDiv);

        // Optionally override CSS height
        if (track.height) {          // Explicit height set, perhaps track.config.height?
            this.trackDiv.style.height = track.height + "px";
        }

        this.appendLeftHandGutterDivToTrackDiv($(this.trackDiv));
        this.appendViewportDivToTrackDiv($(this.trackDiv));

        element = this.createRightHandGutter();
        if (element) {
            $(this.trackDiv).append(element);
        }

        this.trackDiv.appendChild(igv.spinner());

        // Track Drag & Drop
        makeTrackDraggable(this.track);

        if (this.track instanceof igv.RulerTrack) {

            this.trackDiv.dataset.rulerTrack = "rulerTrack";

            // ruler sweeper widget surface
            this.$rulerSweeper = $('<div class="igv-ruler-sweeper-div">');
            $(this.contentDiv).append(this.$rulerSweeper);

            addRulerTrackHandlers(this);

        } else {
            addTrackHandlers(this);
        }

        $('.igv-ideogram-content-div').addClass('igv-ideogram-gutter-shim');
        $('.igv-viewport-div').addClass('igv-gutter-shim');

        function makeTrackDraggable(track) {

            self.igvTrackDragScrim = $('<div class="igv-track-drag-scrim">')[0];
            $(self.viewportDiv).append(self.igvTrackDragScrim);
            $(self.igvTrackDragScrim).hide();

            self.igvTrackManipulationHandle = $('<div class="igv-track-manipulation-handle">')[0];
            $(self.trackDiv).append(self.igvTrackManipulationHandle);

            $(self.igvTrackManipulationHandle).mousedown(function (e) {

                self.isMouseDown = true;
                igv.browser.dragTrackView = self;
            });

            $(self.igvTrackManipulationHandle).mouseup(function (e) {
                self.isMouseDown = undefined;
            });

            $(self.igvTrackManipulationHandle).mouseenter(function (e) {

                self.isMouseIn = true;
                igv.browser.dragTargetTrackView = self;

                if (undefined === igv.browser.dragTrackView) {
                    $(self.igvTrackDragScrim).show();
                } else if (self === igv.browser.dragTrackView) {
                    $(self.igvTrackDragScrim).show();
                }

                if (igv.browser.dragTargetTrackView && igv.browser.dragTrackView) {

                    if (igv.browser.dragTargetTrackView !== igv.browser.dragTrackView) {

                        if (igv.browser.dragTargetTrackView.track.order < igv.browser.dragTrackView.track.order) {

                            igv.browser.dragTrackView.track.order = igv.browser.dragTargetTrackView.track.order;
                            igv.browser.dragTargetTrackView.track.order = 1 + igv.browser.dragTrackView.track.order;
                        } else {

                            igv.browser.dragTrackView.track.order = igv.browser.dragTargetTrackView.track.order;
                            igv.browser.dragTargetTrackView.track.order = igv.browser.dragTrackView.track.order - 1;
                        }

                        igv.browser.reorderTracks();
                    }
                }

            });

            $(self.igvTrackManipulationHandle).mouseleave(function (e) {

                self.isMouseIn = undefined;
                igv.browser.dragTargetTrackView = undefined;

                if (self !== igv.browser.dragTrackView) {
                    $(self.igvTrackDragScrim).hide();
                }

            });

        }
    };

    igv.TrackView.prototype.appendViewportDivToTrackDiv = function ($track) {

        var self = this,
            $dataRangeLabel,
            description,
            $trackLabel;

        // viewport
        this.viewportDiv = $('<div class="igv-viewport-div">')[0];
        $track.append(this.viewportDiv);

        // content  -- purpose of this div is to allow vertical scrolling on individual tracks,
        this.contentDiv = $('<div class="igv-content-div">')[0];
        $(this.viewportDiv).append(this.contentDiv);

        // track content canvas
        this.canvas = $('<canvas class = "igv-content-canvas">')[0];
        $(this.contentDiv).append(this.canvas);
        this.canvas.setAttribute('width', this.contentDiv.clientWidth);
        this.canvas.setAttribute('height', this.contentDiv.clientHeight);
        this.ctx = this.canvas.getContext("2d");

        // zoom in to see features
        if (this.track.visibilityWindow !== undefined) {
            self.$zoomInNotice = $('<div class="zoom-in-notice">');
            self.$zoomInNotice.text('Zoom in to see features');
            $(this.contentDiv).append(self.$zoomInNotice[0]);
            self.$zoomInNotice.hide();
        }

        // scrollbar,  default is to set overflow ot hidden and use custom scrollbar, but this can be overriden so check
        if ("hidden" === $(this.viewportDiv).css("overflow-y")) {
            this.scrollbar = new TrackScrollbar(this.viewportDiv, this.contentDiv);
            this.scrollbar.update();
            $(this.viewportDiv).append(this.scrollbar.outerScrollDiv);
        }

        //if (this.track instanceof igv.WIGTrack) {
        //
        //    $dataRangeLabel = $('<div class="igv-data-range-track-label">');
        //
        //    $dataRangeLabel.click(function (e) {
        //        igv.dataRangeDialog.configureWithTrackView(self);
        //        igv.dataRangeDialog.show();
        //    });
        //
        //    $(this.viewportDiv).append($dataRangeLabel[0]);
        //
        //}

        if (this.track.name) {

            description = this.track.description || this.track.name;
            $trackLabel = $('<div class="igv-track-label">');

            $trackLabel.html(this.track.name);

            $trackLabel.click(function (e) {
                igv.popover.presentTrackPopup(e.pageX, e.pageY, description, false);
            });

            $(this.viewportDiv).append($trackLabel[0]);
        }

    };

    igv.TrackView.prototype.appendLeftHandGutterDivToTrackDiv = function ($track) {

        var self = this,
            $leftHandGutter,
            $canvas,
            w,
            h;

        if (this.track.paintAxis) {

            $leftHandGutter = $('<div class="igv-left-hand-gutter">');
            $track.append($leftHandGutter[0]);

            $canvas = $('<canvas class ="igv-track-control-canvas">');

            w = $leftHandGutter.outerWidth();
            h = $leftHandGutter.outerHeight();
            $canvas.attr('width', w);
            $canvas.attr('height', h);

            $leftHandGutter.append($canvas[0]);

            this.controlCanvas = $canvas[0];
            this.controlCtx = this.controlCanvas.getContext("2d");


            if (this.track.dataRange) {

                $leftHandGutter.click(function (e) {
                    igv.dataRangeDialog.configureWithTrackView(self);
                    igv.dataRangeDialog.show();
                });

                $leftHandGutter.addClass('igv-clickable');
            }

            this.leftHandGutter = $leftHandGutter[0];

        }

    };

    igv.TrackView.prototype.createRightHandGutter = function () {

        var self = this,
            gearButton;

        if (this.track.ignoreTrackMenu) {
            return undefined;
        }

        gearButton = $('<i class="fa fa-gear fa-20px igv-track-menu-gear igv-app-icon">');

        $(gearButton).click(function (e) {
            igv.popover.presentTrackMenu(e.pageX, e.pageY, self);
        });

        this.rightHandGutter = $('<div class="igv-right-hand-gutter">')[0];
        $(this.rightHandGutter).append(gearButton[0]);

        return this.rightHandGutter;

    };

    igv.TrackView.prototype.resize = function () {
        var canvas = this.canvas,
            contentDiv = this.contentDiv,
            contentWidth = this.viewportDiv.clientWidth;

        if (contentWidth > 0) {
            contentDiv.style.width = contentWidth + "px";      // Not sure why css is not working for this
            canvas.style.width = contentWidth + "px";
            canvas.setAttribute('width', contentWidth);    //Must set the width & height of the canvas
            this.update();
        }
    };

    igv.TrackView.prototype.setTrackHeight = function (newHeight, update) {

        setTrackHeight_.call(this, newHeight, update || true);

    };

    /**
     * Set the content height of the track
     *
     * @param newHeight
     * @param update
     */
    igv.TrackView.prototype.setContentHeight = function (newHeight) {

        // Maximum height of a canvas is ~32,000 pixels on Chrome, possibly smaller on other platforms
        newHeight = Math.min(newHeight, 32000);

        if (this.track.minHeight) newHeight = Math.max(this.track.minHeight, newHeight);

        var contentHeightStr = newHeight + "px";

        // Optionally adjust the trackDiv and viewport height to fit the content height, within min/max bounds
        if (this.track.autoHeight) {
            setTrackHeight_.call(this, newHeight, false);
        }

        this.contentDiv.style.height = contentHeightStr;
        this.canvas.setAttribute("height", this.canvas.clientHeight);
        if (this.track.paintAxis) {
            this.controlCanvas.style.height = contentHeightStr;
            this.controlCanvas.setAttribute("height", newHeight);
        }

        if (this.scrollbar) this.scrollbar.update();
    };

    function setTrackHeight_(newHeight, update) {

        var trackHeightStr;

        if (this.track.minHeight) newHeight = Math.max(this.track.minHeight, newHeight);
        if (this.track.maxHeight) newHeight = Math.min(this.track.maxHeight, newHeight);
        // if (newHeight === this.track.height) return;   // Nothing to do

        trackHeightStr = newHeight + "px";

        this.track.height = newHeight;    // Recorded on track for use when saving sessions

        this.trackDiv.style.height = trackHeightStr;

        if (this.track.paintAxis) {
            this.controlCanvas.style.height = trackHeightStr;
            this.controlCanvas.setAttribute("height", newHeight);
        }

        this.viewportDiv.style.height = trackHeightStr;


        if (update === undefined || update === true) {
            this.update();
        }

    }

    igv.TrackView.prototype.update = function () {

        this.tile = null;
        if (this.scrollbar) this.scrollbar.update();
        this.repaint();

    };

    /**
     * Repaint the view, using a cached image if available.  If no image covering the view is available a new one
     * is created, delegating the draw details to the track object.
     */
    igv.TrackView.prototype.repaint = function () {


        var pixelWidth,
            bpWidth,
            bpStart,
            bpEnd,
            self = this,
            ctx,
            referenceFrame,
            chr,
            refFrameStart,
            refFrameEnd,
            success;

        if (!(viewIsReady.call(this))) {
            return;
        }

        if (this.track.visibilityWindow !== undefined && this.track.visibilityWindow > 0) {
            if (igv.browser.trackViewportWidthBP() > this.track.visibilityWindow) {
                this.tile = null;
                this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
                igv.stopSpinnerAtParentElement(this.trackDiv);      // TODO -  WHY DO WE HAVE TO DO THIS ???
                this.$zoomInNotice.show();
                return;
            } else {
                this.$zoomInNotice.hide();
            }
        }

        referenceFrame = this.browser.referenceFrame;
        chr = referenceFrame.chr;
        refFrameStart = referenceFrame.start;
        refFrameEnd = refFrameStart + referenceFrame.toBP(this.canvas.width);

        if (this.tile && this.tile.containsRange(chr, refFrameStart, refFrameEnd, referenceFrame.bpPerPixel)) {
            this.paintImage();
        }
        else {

            // Expand the requested range so we can pan a bit without reloading
            pixelWidth = 3 * this.canvas.width;
            bpWidth = Math.round(referenceFrame.toBP(pixelWidth));
            bpStart = Math.max(0, Math.round(referenceFrame.start - bpWidth / 3));
            bpEnd = bpStart + bpWidth;

            if (self.loading && self.loading.start === bpStart && self.loading.end === bpEnd) return;

            self.loading = {start: bpStart, end: bpEnd};

            igv.startSpinnerAtParentElement(self.trackDiv);

            this.track.getFeatures(referenceFrame.chr, bpStart, bpEnd)

                .then(function (features) {

                    self.loading = false;
                    igv.stopSpinnerAtParentElement(self.trackDiv);

                    if (features) {

                        // TODO -- adjust track height here.
                        if (typeof self.track.computePixelHeight === 'function') {
                            var requiredHeight = self.track.computePixelHeight(features);
                            if (requiredHeight != self.contentDiv.clientHeight) {
                                self.setContentHeight(requiredHeight);
                            }
                        }
                        var buffer = document.createElement('canvas');
                        buffer.width = pixelWidth;
                        buffer.height = self.canvas.height;
                        ctx = buffer.getContext('2d');

                        self.track.draw({
                            features: features,
                            context: ctx,
                            bpStart: bpStart,
                            bpPerPixel: referenceFrame.bpPerPixel,
                            pixelWidth: buffer.width,
                            pixelHeight: buffer.height
                        });

                        // Paint the axis if defined.  NOTE: its important that this is called after "draw" as
                        // autoscale for numeric tracks is called during the draw function
                        if (self.track.paintAxis && self.controlCanvas.width > 0 && self.controlCanvas.height > 0) {

                            var buffer2 = document.createElement('canvas');
                            buffer2.width = self.controlCanvas.width;
                            buffer2.height = self.controlCanvas.height;

                            var ctx2 = buffer2.getContext('2d');

                            self.track.paintAxis(ctx2, buffer2.width, buffer2.height);

                            self.controlCtx.drawImage(buffer2, 0, 0);
                        }

                        self.tile = new Tile(referenceFrame.chr, bpStart, bpEnd, referenceFrame.bpPerPixel, buffer);
                        self.paintImage();
                    }
                    else {
                        self.ctx.clearRect(0, 0, self.canvas.width, self.canvas.height);
                    }

                })
                .catch(function (error) {
                    self.loading = false;

                    if (error instanceof igv.AbortLoad) {
                        console.log("Aborted ---");
                    }
                    else {
                        igv.stopSpinnerAtParentElement(self.trackDiv);
                        console.log(error);
                        igv.presentAlert(error);
                    }
                });
        }


        function viewIsReady() {
            return this.track && this.browser && this.browser.referenceFrame;
        }

    };

    function Tile(chr, tileStart, tileEnd, scale, image) {
        this.chr = chr;
        this.startBP = tileStart;
        this.endBP = tileEnd;
        this.scale = scale;
        this.image = image;
    }

    Tile.prototype.containsRange = function (chr, start, end, scale) {
        return this.scale === scale && start >= this.startBP && end <= this.endBP && chr === this.chr;
    };

    Tile.prototype.overlapsRange = function (chr, start, end) {
        return this.chr === chr && this.endBP >= start && this.startBP <= end;
    };

    igv.TrackView.prototype.paintImage = function () {

        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        if (this.tile) {
            this.xOffset = Math.round(this.browser.referenceFrame.toPixels(this.tile.startBP - this.browser.referenceFrame.start));
            this.ctx.drawImage(this.tile.image, this.xOffset, 0);
            this.ctx.save();
            this.ctx.restore();
        }
    };

    function addRulerTrackHandlers(trackView) {

        var isMouseDown = undefined,
            isMouseIn = undefined,
            mouseDownXY = undefined,
            mouseMoveXY = undefined,
            left,
            rulerSweepWidth,
            rulerSweepThreshold = 1,
            dx;

        $(document).mousedown(function (e) {

            mouseDownXY = igv.translateMouseCoordinates(e, trackView.contentDiv);

            left = mouseDownXY.x;
            rulerSweepWidth = 0;
            trackView.$rulerSweeper.css({"display": "inline", "left": left + "px", "width": rulerSweepWidth + "px"});

            isMouseIn = true;
        });

        $(trackView.contentDiv).mousedown(function (e) {
            isMouseDown = true;
        });

        $(document).mousemove(function (e) {

            if (isMouseDown && isMouseIn) {

                mouseMoveXY = igv.translateMouseCoordinates(e, trackView.contentDiv);
                dx = mouseMoveXY.x - mouseDownXY.x;
                rulerSweepWidth = Math.abs(dx);

                if (rulerSweepWidth > rulerSweepThreshold) {

                    trackView.$rulerSweeper.css({"width": rulerSweepWidth + "px"});

                    if (dx < 0) {

                        if (mouseDownXY.x + dx < 0) {
                            isMouseIn = false;
                            left = 0;
                        } else {
                            left = mouseDownXY.x + dx;
                        }
                        trackView.$rulerSweeper.css({"left": left + "px"});
                    }
                }
            }
        });

        $(document).mouseup(function (e) {

            var locus,
                ss,
                ee;

            if (isMouseDown) {

                // End sweep
                isMouseDown = false;
                isMouseIn = false;

                trackView.$rulerSweeper.css({"display": "none", "left": 0 + "px", "width": 0 + "px"});

                ss = igv.browser.referenceFrame.start + (left * igv.browser.referenceFrame.bpPerPixel);
                ee = ss + rulerSweepWidth * igv.browser.referenceFrame.bpPerPixel;

                if (rulerSweepWidth > rulerSweepThreshold) {

                    locus = igv.browser.referenceFrame.chr + ":" + igv.numberFormatter(Math.floor(ss)) + "-" + igv.numberFormatter(Math.floor(ee));
                    igv.browser.search(locus);
                }
            }

        });

    }

    function addTrackHandlers(trackView) {

        // Register track handlers for popup.  Although we are not handling dragging here, we still need to check
        // for dragging on a mouseup

        var isMouseDown = false,
            lastMouseX = undefined,
            mouseDownX = undefined,
            lastClickTime = 0,
            popupTimer,
            doubleClickDelay = igv.browser.constants.doubleClickDelay;

        $(trackView.canvas).mousedown(function (e) {

            var canvasCoords = igv.translateMouseCoordinates(e, trackView.canvas);
            isMouseDown = true;
            lastMouseX = canvasCoords.x;
            mouseDownX = lastMouseX;


        });

        $(trackView.canvas).click(function (e) {

            var canvasCoords,
                referenceFrame,
                genomicLocation,
                trackViewportHalfWidth,
                genomicLocationViaTrackViewportHalfWidth,
                time;

            // Sets pageX and pageY for browsers that don't support them
            e = $.event.fix(e);

            e.stopPropagation();

            canvasCoords = igv.translateMouseCoordinates(e, trackView.canvas);
            trackViewportHalfWidth = Math.floor(trackView.browser.trackViewportWidth()/2);

            referenceFrame = trackView.browser.referenceFrame;
            genomicLocation = Math.floor((referenceFrame.start) + referenceFrame.toBP(canvasCoords.x));
            genomicLocationViaTrackViewportHalfWidth = Math.floor((referenceFrame.start) + referenceFrame.toBP(trackViewportHalfWidth));

            // console.log('trackViewClick canvas ' + igv.numberFormatter(genomicLocation) + ' trackViewportHalfWidth ' + igv.numberFormatter(genomicLocationViaTrackViewportHalfWidth));
            time = Date.now();

            if (!referenceFrame) return;

            if (time - lastClickTime < doubleClickDelay) {
                // This is a double-click

                if (popupTimer) {
                    // Cancel previous timer
                    window.clearTimeout(popupTimer);
                    popupTimer = undefined;
                }

                var newCenter = Math.round(referenceFrame.start + canvasCoords.x * referenceFrame.bpPerPixel);
                referenceFrame.bpPerPixel /= 2;
                igv.browser.goto(referenceFrame.chr, newCenter);
            }

            else {

                if (e.shiftKey) {

                    if (trackView.track.shiftClick && trackView.tile) {
                        trackView.track.shiftClick(genomicLocation, e);
                    }

                }
                else if (e.altKey) {

                    if (trackView.track.altClick && trackView.tile) {
                        trackView.track.altClick(genomicLocation, e);
                    }


                } else if (Math.abs(canvasCoords.x - mouseDownX) <= igv.browser.constants.dragThreshold && trackView.track.popupData) {

                    popupTimer = window.setTimeout(function () {

                            var popupData,
                                xOrigin;

                            if (undefined === genomicLocation) {
                                return;
                            }
                            if (null === trackView.tile) {
                                return;
                            }
                            xOrigin = Math.round(referenceFrame.toPixels((trackView.tile.startBP - referenceFrame.start)));
                            popupData = trackView.track.popupData(genomicLocation, canvasCoords.x - xOrigin, canvasCoords.y);

                            var handlerResult = igv.browser.fireEvent('trackclick', [trackView.track, popupData]);

                            // (Default) no external handlers or no input from handlers
                            if (handlerResult === undefined) {
                                if (popupData && popupData.length > 0) {
                                    igv.popover.presentTrackPopup(e.pageX, e.pageY, igv.formatPopoverText(popupData), false);
                                }
                                // A handler returned custom popover HTML to override default format
                            } else if (typeof handlerResult === 'string') {
                                igv.popover.presentTrackPopup(e.pageX, e.pageY, handlerResult, false);
                            }
                            // If handler returned false then we do nothing and let the handler manage the click

                            mouseDownX = undefined;
                            popupTimer = undefined;
                        },
                        doubleClickDelay);
                }
            }

            mouseDownX = undefined;
            isMouseDown = false;
            lastMouseX = undefined;
            lastClickTime = time;

        });


    }

    /**
     * Creates a vertical scrollbar to slide an inner "contentDiv" with respect to an enclosing "viewportDiv"
     *
     */
    TrackScrollbar = function (viewportDiv, contentDiv) {

        var outerScrollDiv = $('<div class="igv-scrollbar-outer-div">')[0],
            innerScrollDiv = $('<div class="igv-scrollbar-inner-div">')[0],
            offY;

        $(outerScrollDiv).append(innerScrollDiv);

        this.viewportDiv = viewportDiv;
        this.contentDiv = contentDiv;
        this.outerScrollDiv = outerScrollDiv;
        this.innerScrollDiv = innerScrollDiv;


        $(this.innerScrollDiv).mousedown(function (event) {
            offY = event.pageY - $(innerScrollDiv).position().top;
            $(window).on("mousemove .igv", null, null, mouseMove);
            $(window).on("mouseup .igv", null, null, mouseUp);
            event.stopPropagation();     // <= prevents start of horizontal track panning);
        });

        $(this.innerScrollDiv).click(function (event) {
            event.stopPropagation();  // "Eat" clicks on the inner div to prevent them bubbling up to outer
        });

        $(this.outerScrollDiv).click(function (event) {
            moveScrollerTo(event.offsetY - $(innerScrollDiv).height() / 2);
            event.stopPropagation();

        });

        // Mousewheel disabled -- it controls the outer (browser window) scrollbar
        //$(this.viewportDiv).mousewheel(function (event) {
        //
        //    var ratio = $(viewportDiv).height() / $(contentDiv).height();
        //
        //    if (ratio < 1) {
        //        var dist = Math.round(ratio * event.deltaY * event.deltaFactor),
        //            newY = $(innerScrollDiv).position().top + dist;
        //        moveScrollerTo(newY);
        //    }
        //});

        function mouseMove(event) {
            moveScrollerTo(event.pageY - offY);
            event.stopPropagation();
        }

        function mouseUp(event) {
            $(window).off("mousemove .igv", null, mouseMove);
            $(window).off("mouseup .igv", null, mouseUp);
        };

        function moveScrollerTo(y) {
            var H = $(outerScrollDiv).height(),
                h = $(innerScrollDiv).height();
            newTop = Math.min(Math.max(0, y), H - h),
                contentTop = -Math.round(newTop * ($(contentDiv).height() / $(viewportDiv).height()));
            $(innerScrollDiv).css("top", newTop + "px");
            $(contentDiv).css("top", contentTop + "px");
        }
    }


    TrackScrollbar.prototype.update = function () {
        var viewportHeight = $(this.viewportDiv).height(),
            contentHeight = $(this.contentDiv).height(),
            newInnerHeight = Math.round((viewportHeight / contentHeight) * viewportHeight);
        if (contentHeight > viewportHeight) {
            $(this.outerScrollDiv).show();
            $(this.innerScrollDiv).height(newInnerHeight);
        }
        else {
            $(this.outerScrollDiv).hide();
        }
    }


    igv.TrackView.prototype.redrawTile = function (features) {

        if (!this.tile) return;

        var self = this,
            chr = self.tile.chr,
            bpStart = self.tile.startBP,
            bpEnd = self.tile.endBP,
            buffer = document.createElement('canvas'),
            bpPerPixel = self.tile.scale;

        buffer.width = self.tile.image.width;
        buffer.height = self.tile.image.height;
        var ctx = buffer.getContext('2d');

        self.track.draw({
            features: features,
            context: ctx,
            bpStart: bpStart,
            bpPerPixel: bpPerPixel,
            pixelWidth: buffer.width,
            pixelHeight: buffer.height
        });


        self.tile = new Tile(chr, bpStart, bpEnd, bpPerPixel, buffer);
        self.paintImage();
    }


    return igv;


})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by turner on 4/29/15.
 */
var igv = (function (igv) {

    igv.AlertDialog = function ($parent, id) {

        var self = this,
            $header,
            $headerBlurb;

        this.$container = $('<div>', { "id": id, "class": "igv-grid-container-alert-dialog" });
        $parent.append(this.$container);

        $header = $('<div class="igv-grid-header">');
        $headerBlurb = $('<div class="igv-grid-header-blurb">');
        $header.append($headerBlurb);
        igv.attachDialogCloseHandlerWithParent($header, function () {
            self.hide();
        });
        this.$container.append($header);

        this.$container.append(this.alertTextContainer());

        this.$container.append(this.rowOfOk());

    };

    igv.AlertDialog.prototype.alertTextContainer = function() {

        var $rowContainer,
            $col;

        $rowContainer = $('<div class="igv-grid-rect">');

        this.$dialogLabel = $('<div>', { "class": "igv-col igv-col-4-4 igv-alert-dialog-text" });

        // $col = $('<div class="igv-col igv-col-4-4">');
        // $col.append(this.$dialogLabel);
        // $rowContainer.append($col);

        $rowContainer.append(this.$dialogLabel);

        return $rowContainer;

    };

    igv.AlertDialog.prototype.rowOfOk = function() {

        var self = this,
            $rowContainer,
            $col;

        $rowContainer = $('<div class="igv-grid-rect">');

        // shim
        $col = $('<div class="igv-col igv-col-1-4">');
        $rowContainer.append( $col );

        // ok button
        $col = $('<div class="igv-col igv-col-2-4">');
        this.$ok = $('<div class="igv-col-filler-ok-button">');
        this.$ok.text("OK");

        this.$ok.unbind();
        this.$ok.click(function() {
            self.hide();
        });

        $col.append( this.$ok );
        $rowContainer.append( $col );

        return $rowContainer;

    };

    igv.AlertDialog.prototype.hide = function () {

        if (this.$container.hasClass('igv-grid-container-dialog')) {
            this.$container.offset( { left: 0, top: 0 } );
        }
        this.$container.hide();
    };

    igv.AlertDialog.prototype.show = function ($host) {

        var body_scrolltop,
            track_origin,
            track_size,
            offset,
            _top,
            _left;

        body_scrolltop = $('body').scrollTop();

        if (this.$container.hasClass('igv-grid-container-dialog')) {

            offset = $host.offset();

            _top = offset.top + body_scrolltop;
            _left = $host.outerWidth() - 300;

            this.$container.offset( { left: _left, top: _top } );

            //track_origin = $host.offset();
            //track_size =
            //{
            //    width: $host.outerWidth(),
            //    height: $host.outerHeight()
            //};
            //this.$container.offset( { left: (track_size.width - 300), top: (track_origin.top + body_scrolltop) } );
            //this.$container.offset( igv.constrainBBox(this.$container, $(igv.browser.trackContainerDiv)) );
        }

        this.$container.show();

    };

    return igv;

})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 University of California San Diego
 * Author: Jim Robinson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by dat on 9/1/16.
 */
var igv = (function (igv) {

    igv.CenterGuide = function ($parent, config) {
        var self = this,
            cssDisplay;
        
        this.$container = $('<div class="igv-center-guide igv-center-guide-thin">');
        $parent.append(this.$container);
        this.$container.css("display", (config.showCenterGuide && true == config.showCenterGuide) ? "block" : "none");

        cssDisplay = this.$container.css("display");
        this.$centerGuideToggle = $('<div class="igv-toggle-track-labels">');
        this.$centerGuideToggle.text(("none" === cssDisplay) ? "show center guide" : "hide center guide");

        this.$centerGuideToggle.on("click", function () {
            cssDisplay = self.$container.css("display");
            if ("none" === cssDisplay) {
                self.$container.css("display", "block");
                self.$centerGuideToggle.text("hide center guide");
            } else {
                self.$container.css("display", "none");
                self.$centerGuideToggle.text("show center guide");
            }
        });

    };

    igv.CenterGuide.prototype.repaint = function () {

        var ppb,
            trackViewXY,
            trackViewHalfWidth,
            width,
            left,
            ls,
            ws,
            center,
            xBP;

        ppb = (igv.browser.referenceFrame !== undefined) ? 1.0/igv.browser.referenceFrame.bpPerPixel : 0;
        if (ppb > 1) {

            trackViewXY = $(igv.browser.trackViews[ 0 ].viewportDiv).position();
            trackViewHalfWidth = 0.5 * $(igv.browser.trackViews[ 0 ].viewportDiv).width();
            xBP = igv.browser.referenceFrame.toBP(trackViewHalfWidth) + igv.browser.referenceFrame.start;

            center = trackViewXY.left + trackViewHalfWidth;
            width = igv.browser.referenceFrame.toPixels(1);
            left = center - 0.5 * width;

            ls = Math.round(left).toString() + 'px';
            ws = Math.round(width).toString() + 'px';
            this.$container.css({ left:ls, width:ws });

            this.$container.removeClass('igv-center-guide-thin');
            this.$container.addClass('igv-center-guide-wide');
        } else {

            this.$container.css({ left:'50%', width:'1px' });

            this.$container.removeClass('igv-center-guide-wide');
            this.$container.addClass('igv-center-guide-thin');
        }

    };

    return igv;

}) (igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by turner on 4/15/15.
 */
var igv = (function (igv) {

    var columnCount = 8;

    igv.ColorPicker = function ($parent, userPalette, id) {

        var self = this,
            palette = userPalette || ["#666666", "#0000cc", "#009900", "#cc0000", "#ffcc00", "#9900cc", "#00ccff", "#ff6600", "#ff6600"],
            //palette = ["#666666", "#0000cc", "#009900", "#cc0000", "#ffcc00", "#9900cc", "#00ccff", "#ff6600", "#ff6600"],
            rowCount = Math.ceil(palette.length / columnCount),
            rowIndex;

        this.rgb_re = /^\s*(0|[1-9]\d?|1\d\d?|2[0-4]\d|25[0-5])\s*,\s*(0|[1-9]\d?|1\d\d?|2[0-4]\d|25[0-5])\s*,\s*(0|[1-9]\d?|1\d\d?|2[0-4]\d|25[0-5])\s*$/;
        this.hex_re = new RegExp('^#([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})$');

        this.$container = $('<div class="igv-grid-container-colorpicker">');
        if (id) {
            this.$container.attr("id", id);
        }
        $parent.append(this.$container);

        this.$container.draggable();

        this.$header = $('<div class="igv-grid-header">');
        this.$headerBlurb = $('<div class="igv-grid-header-blurb">');

        this.$header.append(this.$headerBlurb);

        igv.attachDialogCloseHandlerWithParent(this.$header, function () {
            self.hide();
        });

        this.$container.append(this.$header);


        // color palette
        for (rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            self.$container.append(makeRow(palette.slice(rowIndex * columnCount)));
        }

        // dividing line
        self.$container.append($('<hr class="igv-grid-dividing-line">'));

        // user colors
        self.$container.append(rowOfUserColors());

        //// dividing line
        //self.$container.append($('<hr class="igv-grid-dividing-line">')[ 0 ]);

        // initial track color
        self.$container.append(rowOfPreviousColor());

        //// dividing line
        //self.$container.append($('<hr class="igv-grid-dividing-line">')[ 0 ]);

        // initial track color
        self.$container.append(rowOfDefaultColor());

        function rowOfUserColors() {

            var $rowContainer,
                $row,
                $column,
                $userColorInput,
                digit;

            self.userColors = [];

            // Provide 5 rows of user color pallete real estate
            for (digit = 0; digit < 5; digit++) {

                $row = rowHidden(digit);
                self.userColors.push($row);
                self.$container.append( $row[ 0 ] );

                $row.find('.igv-col-filler-no-color').addClass("igv-grid-rect-hidden");
            }

            self.userColorsIndex = undefined;
            self.userColorsRowIndex = 0;

            $row = $('<div class="igv-grid-colorpicker">');

            // color input
            $column = $('<div class="igv-col igv-col-7-8">');
            $userColorInput = $('<input class="igv-user-input-colorpicker" type="text" placeholder="Ex: #ff0000 or 255,0,0">');
            $userColorInput.change(function () {

                var parsed = parseColor($(this).val());

                if (parsed) {

                    igv.setTrackColor(self.trackView.track, parsed);
                    self.trackView.update();
                    addUserColor(parsed);

                    $(this).val("");
                    $(this).attr("placeholder", "Ex: #ff0000 or 255,0,0");

                    self.$userColorFeeback.css("background-color", "white");
                    self.$userColorFeeback.hide();

                } else {
                    self.$userError.show();
                }

            });

            $userColorInput.mousedown(function () {
                $(this).attr("placeholder", "");
            });

            $userColorInput.keyup(function () {

                var parsed;

                if ("" === $(this).val()) {
                    self.$userError.hide();
                    $(this).attr("placeholder", "Ex: #ff0000 or 255,0,0");
                }

                parsed = parseColor($(this).val());

                if (undefined !== parsed) {
                    self.$userColorFeeback.css("background-color", parsed);
                    self.$userColorFeeback.show();
                } else {
                    self.$userColorFeeback.css("background-color", "white");
                    self.$userColorFeeback.hide();
                }

            });

            $column.append($userColorInput);
            $row.append($column);


            // color feedback chip
            $column = makeColumn(null);
            self.$userColorFeeback = $column.find("div").first();
            $row.append($column);
            self.$userColorFeeback.hide();

            $rowContainer = $('<div class="igv-grid-rect">');
            $rowContainer.append($row);



            // user feedback
            self.$userError = $('<span>');
            self.$userError.text("ERROR.    Ex: #ff0000 or 255,0,0");
            self.$userError.hide();

            $row = $('<div class="igv-grid-colorpicker-user-error">');
            $row.append(self.$userError);
            $rowContainer.append($row);

            function parseColor(value) {

                var rgb,
                    hex;

                rgb = self.rgb_re.exec(value);
                if (null !== rgb) {

                    return "rgb(" + rgb[0] + ")";
                } else {

                    hex = self.hex_re.exec(value);
                    if (null !== hex) {

                        return igv.hex2Color(hex[0]);
                    }
                }

                return undefined;
            }

            function addUserColor(color) {

                if (undefined === self.userColorsIndex) {

                    self.userColorsIndex = 0;
                    self.userColorsRowIndex = 0;
                } else if (columnCount === self.userColorsRowIndex) {

                    self.userColorsRowIndex = 0;
                    self.userColorsIndex = (1 + self.userColorsIndex) % self.userColors.length;
                }

                presentUserColor(color, self.userColorsIndex, self.userColorsRowIndex);

                ++(self.userColorsRowIndex);

            }

            function presentUserColor(color, c, r) {

                var $rowContainer,
                    $filler;

                $rowContainer = self.userColors[ c ];
                $rowContainer.removeClass("igv-grid-rect-hidden");
                $rowContainer.addClass("igv-grid-rect");

                $filler = $rowContainer.find(".igv-grid-colorpicker").find(".igv-col").find("div").eq( r );

                $filler.removeClass("igv-col-filler-no-color");
                $filler.removeClass("igv-grid-rect-hidden");

                $filler.addClass("igv-col-filler");

                $filler.css("background-color", color);

                $filler.click(function () {

                    igv.setTrackColor(self.trackView.track, $(this).css("background-color"));
                    self.trackView.update();

                });

            }

            return $rowContainer;

        }

        function rowOfDefaultColor() {

            var $rowContainer,
                $row,
                $column;

            $row = $('<div class="igv-grid-colorpicker">');

            // initial color tile
            self.$defaultColor = $('<div class="igv-col-filler">');
            self.$defaultColor.css("background-color", "#eee");

            $column = $('<div class="igv-col igv-col-1-8">');
            $column.append(self.$defaultColor);

            $column.click(function () {
                igv.setTrackColor(self.trackView.track, $(this).find(".igv-col-filler").css("background-color"));
                self.trackView.update();
            });

            $row.append($column);


            // default color label
            $column = $('<div class="igv-col igv-col-7-8 igv-col-label">');
            $column.text("Default Color");
            $row.append($column);


            $rowContainer = $('<div class="igv-grid-rect">');
            $rowContainer.append($row);

            return $rowContainer;
        }

        function rowOfPreviousColor() {

            var $rowContainer,
                $row,
                $column;

            $row = $('<div class="igv-grid-colorpicker">');

            // initial color tile
            self.$previousColor = $('<div class="igv-col-filler">');
            self.$previousColor.css("background-color", "#eee");

            $column = $('<div class="igv-col igv-col-1-8">');
            $column.append(self.$previousColor);

            $column.click(function () {
                igv.setTrackColor(self.trackView.track, $(this).find(".igv-col-filler").css("background-color"));
                self.trackView.update();
            });

            $row.append($column);


            // initial color label
            $column = $('<div class="igv-col igv-col-7-8 igv-col-label">');
            $column.text("Previous Color");
            $row.append($column);


            $rowContainer = $('<div class="igv-grid-rect">');
            $rowContainer.append($row);

            return $rowContainer;
        }

        function rowHidden(rowIndex) {

            var $rowContainer = $('<div class="igv-grid-rect-hidden">'),
                $row = $('<div class="igv-grid-colorpicker">'),
                columnIndex;

            for (columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                $row.append(makeColumn(null));
            }

            $rowContainer.append($row);
            return $rowContainer;
        }

        function makeRow(colors) {

            var $rowContainer = $('<div class="igv-grid-rect">'),
                $row = $('<div class="igv-grid-colorpicker">'),
                i;

            for (i = 0; i < Math.min(columnCount, colors.length); i++) {
                $row.append(makeColumn(colors[i]));
            }

            $rowContainer.append($row);
            return $rowContainer;
        }

        function makeColumn(colorOrNull) {

            var $column = $('<div class="igv-col igv-col-1-8">'),
                $filler = $('<div>');

            $column.append($filler);

            if (null !== colorOrNull) {

                $filler.addClass("igv-col-filler");
                $filler.css("background-color", colorOrNull);

                $filler.click(function () {

                    igv.setTrackColor(self.trackView.track, $(this).css("background-color"));
                    self.trackView.update();

                });

            } else {
                $filler.addClass("igv-col-filler-no-color");
                $filler.css("background-color", "white");
            }

            return $column;
        }

    };

    igv.ColorPicker.prototype.configure = function (trackView) {

        this.trackView = trackView;

        this.$defaultColor.css("background-color", trackView.track.config.color || igv.browser.constants.defaultColor);
        this.$previousColor.css("background-color", trackView.track.color);

    };

    igv.ColorPicker.prototype.hide = function () {
        $(this.$container).offset({left: 0, top: 0});
        this.$container.hide();
    };

    igv.ColorPicker.prototype.show = function () {

        var body_scrolltop = $("body").scrollTop(),
            track_origin = $(this.trackView.trackDiv).offset(),
            track_size = {
                width: $(this.trackView.trackDiv).outerWidth(),
                height: $(this.trackView.trackDiv).outerHeight()
            },
            size = {width: $(this.$container).outerWidth(), height: $(this.$container).outerHeight()},
            obj;

        $(this.$container).offset({left: (track_size.width - 300), top: (track_origin.top + body_scrolltop)});


        obj = $(".igv-user-input-color");
        obj.val("");
        obj.attr("placeholder", "Ex: #ff0000 or 255,0,0");

        this.$container.show();
        this.$userError.hide();

        $(this.$container).offset(igv.constrainBBox($(this.$container), $(igv.browser.trackContainerDiv)));

    };

    return igv;

})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by turner on 4/29/15.
 */
var igv = (function (igv) {

    igv.DataRangeDialog = function ($parent, id) {

        var self = this;

        this.container = $('<div class="igv-grid-container-dialog">');
        if (id) {
            this.container.attr("id", id);
        }
        $parent.append( this.container[ 0 ] );

        this.container.draggable();

        this.header = $('<div class="igv-grid-header">');
        this.headerBlurb = $('<div class="igv-grid-header-blurb">');

        this.header.append(this.headerBlurb[ 0 ]);

        igv.attachDialogCloseHandlerWithParent(this.header, function () {
            self.hide();
        });

        this.container.append(this.header[ 0 ]);

        self.container.append(doLayout()[ 0 ]);

        self.container.append(doOKCancel()[ 0 ]);

        function doOKCancel() {

            var rowContainer,
                row,
                column,
                columnFiller;


            row = $('<div class="igv-grid-dialog">');


            // shim
            column = $('<div class="igv-col igv-col-1-8">');
            //
            row.append( column[ 0 ] );


            // ok button
            column = $('<div class="igv-col igv-col-3-8">');
            self.ok = $('<div class="igv-col-filler-ok-button">');
            self.ok.text("OK");
            column.append( self.ok[ 0 ] );
            //
            row.append( column[ 0 ] );


            // cancel button
            column = $('<div class="igv-col igv-col-3-8">');
            columnFiller = $('<div class="igv-col-filler-cancel-button">');
            columnFiller.text("Cancel");
            columnFiller.click(function() { self.hide(); });
            column.append( columnFiller[ 0 ] );
            //
            row.append( column[ 0 ] );


            // shim
            column = $('<div class="igv-col igv-col-1-8">');
            //
            row.append( column[ 0 ] );


            rowContainer = $('<div class="igv-grid-rect">');
            rowContainer.append( row[ 0 ]);

            return rowContainer;
        }

        function doLayout() {

            var rowContainer = $('<div class="igv-grid-rect">'),
                row,
                column,
                columnFiller;


            // minimum
            row = $('<div class="igv-grid-dialog">');

            // vertical spacer
            column = $('<div class="spacer10">');
            row.append( column[ 0 ] );


            column = $('<div class="igv-col igv-col-3-8">');
            self.minLabel = $('<div class="igv-data-range-input-label">');
            self.minLabel.text("Minimum");
            column.append( self.minLabel[ 0 ] );
            row.append( column[ 0 ] );

            column = $('<div class="igv-col igv-col-3-8">');
            self.minInput = $('<input class="igv-data-range-input" type="text" value="125">');
            column.append( self.minInput[ 0 ] );
            row.append( column[ 0 ] );

            rowContainer.append( row[ 0 ]);



            // maximum
            row = $('<div class="igv-grid-dialog">');

            column = $('<div class="igv-col igv-col-3-8">');
            self.maxLabel = $('<div class="igv-data-range-input-label">');
            self.maxLabel.text("Maximum");
            column.append( self.maxLabel[ 0 ] );
            row.append( column[ 0 ] );

            column = $('<div class="igv-col igv-col-3-8">');
            self.maxInput = $('<input class="igv-data-range-input" type="text" value="250">');
            column.append( self.maxInput[ 0 ] );
            row.append( column[ 0 ] );
            rowContainer.append( row[ 0 ]);



            // logaritmic
            //row = $('<div class="igv-grid-dialog">');
            //
            //column = $('<div class="igv-col igv-col-3-8">');
            //columnFiller = $('<div class="igv-data-range-input-label">');
            //columnFiller.text("Log scale");
            //column.append( columnFiller[ 0 ] );
            //row.append( column[ 0 ] );
            //
            //column = $('<div class="igv-col igv-col-3-8">');
            //self.logInput = $('<input class="igv-data-range-input" type="checkbox">');
            //
            //// Disable until implemented in track
            //self.logInput[0].disabled = true;
            //
            //column.append( self.logInput[ 0 ] );
            //row.append( column[ 0 ] );
            //rowContainer.append( row[ 0 ]);

            return rowContainer;

        }

    };

    igv.DataRangeDialog.prototype.configureWithTrackView = function (trackView) {

        var self = this,
            min,
            max;

        this.trackView = trackView;

        if(trackView.track.dataRange) {
            min = trackView.track.dataRange.min;
            max = trackView.track.dataRange.max;
        } else {
            min = 0;
            max = 100;
        }

        this.minInput.val(min);
        this.maxInput.val(max);

        //this.logInput.prop('checked', false);

        this.ok.unbind();
        this.ok.click(function() {

            min = parseFloat(self.minInput.val());
            max = parseFloat(self.maxInput.val());

            if(isNaN(min) || isNaN(max)) {

                //alert("Must input numeric values");
                igv.presentAlert("Must input numeric values");
            } else {

                trackView.track.dataRange.min = min;
                trackView.track.dataRange.max = max;
                trackView.track.autoScale = false;

                self.hide();
                trackView.update();
            }

        });

    };

    igv.DataRangeDialog.prototype.hide = function () {
        this.container.offset( { left: 0, top: 0 } );
        this.container.hide();
    };

    igv.DataRangeDialog.prototype.show = function () {

        var body_scrolltop = $("body").scrollTop(),
            track_scrolltop = $(this.trackView.trackDiv).scrollTop(),
            track_origin = $(this.trackView.trackDiv).offset(),
            track_size = { width: $(this.trackView.trackDiv).outerWidth(), height: $(this.trackView.trackDiv).outerHeight()},
            size = { width: this.container.outerWidth(), height: this.container.outerHeight()};

        //console.log("scrollTop. body " + body_scrolltop + " track " + track_scrolltop);

        // centered left-right
        //this.container.offset( { left: (track_size.width - size.width)/2, top: track_origin.top } );

        this.container.show();

        this.container.offset( { left: (track_size.width - 300), top: (track_origin.top + body_scrolltop) } );

        this.container.offset( igv.constrainBBox(this.container, $(igv.browser.trackContainerDiv)) );

    };

    return igv;

})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by turner on 4/29/15.
 */
var igv = (function (igv) {

    igv.Dialog = function ($parent, constructorHelper, id) {

        var self = this,
            $header,
            $headerBlurb;

        this.$container = $('<div class="igv-grid-container-dialog">');
        if (id) {
            this.$container.attr("id", id);
        }
        $parent.append( this.$container[ 0 ] );

        $header = $('<div class="igv-grid-header">');
        $headerBlurb = $('<div class="igv-grid-header-blurb">');
        $header.append($headerBlurb[ 0 ]);

        this.$container.append($header[ 0 ]);

        constructorHelper(this);

        igv.attachDialogCloseHandlerWithParent($header, function () {
            self.hide();
        });

    };

    igv.Dialog.dialogConstructor = function (dialog) {

        dialog.$container.append(dialog.rowOfLabel()[ 0 ]);

        dialog.$container.append(dialog.rowOfInput()[ 0 ]);

        dialog.$container.append(dialog.rowOfOkCancel()[ 0 ]);

        dialog.$container.draggable();

    };

    igv.Dialog.prototype.rowOfOk = function() {

        var $rowContainer,
            $row,
            $column,
            $columnFiller;

        $row = $('<div class="igv-grid-dialog">');

        // shim
        $column = $('<div class="igv-col igv-col-1-4">');
        //
        $row.append( $column[ 0 ] );


        // ok button
        $column = $('<div class="igv-col igv-col-2-4">');
        $columnFiller = $('<div class="igv-col-filler-ok-button">');
        $columnFiller.text("OK");

        this.$ok = $columnFiller;

        $column.append( $columnFiller[ 0 ] );
        //
        $row.append( $column[ 0 ] );

        //
        $rowContainer = $('<div class="igv-grid-rect">');
        $rowContainer.append( $row[ 0 ]);

        return $rowContainer;

    };

    igv.Dialog.prototype.rowOfOkCancel = function() {

        var self = this,
            $rowContainer,
            $row,
            $column,
            $columnFiller;

        $row = $('<div class="igv-grid-dialog">');

        // shim
        $column = $('<div class="igv-col igv-col-1-8">');
        //
        $row.append( $column[ 0 ] );


        // ok button
        $column = $('<div class="igv-col igv-col-3-8">');
        $columnFiller = $('<div class="igv-col-filler-ok-button">');
        $columnFiller.text("OK");

        this.$ok = $columnFiller;

        $column.append( $columnFiller[ 0 ] );
        //
        $row.append( $column[ 0 ] );


        // cancel button
        $column = $('<div class="igv-col igv-col-3-8">');
        $columnFiller = $('<div class="igv-col-filler-cancel-button">');
        $columnFiller.text("Cancel");
        $columnFiller.click(function() {
            self.$dialogInput.val(undefined);
            self.hide();
        });
        $column.append( $columnFiller[ 0 ] );
        //
        $row.append( $column[ 0 ] );

        // shim
        $column = $('<div class="igv-col igv-col-1-8">');
        //
        $row.append( $column[ 0 ] );

        $rowContainer = $('<div class="igv-grid-rect">');
        $rowContainer.append( $row[ 0 ]);

        return $rowContainer;

    };

    igv.Dialog.prototype.rowOfLabel = function() {

        var rowContainer,
            row,
            column;

        // input
        row = $('<div class="igv-grid-dialog">');

        column = $('<div class="igv-col igv-col-4-4">');
        this.$dialogLabel = $('<div class="igv-user-input-label">');

        column.append( this.$dialogLabel[ 0 ] );
        row.append( column[ 0 ] );

        rowContainer = $('<div class="igv-grid-rect">');
        rowContainer.append( row[ 0 ]);

        return rowContainer;

    };

    igv.Dialog.prototype.rowOfInput = function() {

        var rowContainer,
            row,
            column;

        // input
        row = $('<div class="igv-grid-dialog">');

        column = $('<div class="igv-col igv-col-4-4">');
        this.$dialogInput = $('<input class="igv-user-input-dialog" type="text" value="#000000">');

        column.append( this.$dialogInput[ 0 ] );
        row.append( column[ 0 ] );

        rowContainer = $('<div class="igv-grid-rect">');
        rowContainer.append( row[ 0 ]);

        return rowContainer;

    };

    igv.Dialog.prototype.configure = function (labelHTMLFunction, inputValue, clickFunction) {

        var self = this,
            clickOK;

        if (labelHTMLFunction) {
            self.$dialogLabel.html(labelHTMLFunction());
            self.$dialogLabel.show();
        } else {
            self.$dialogLabel.hide();
        }

        if (inputValue !== undefined) {

            self.$dialogInput.val(inputValue);

            self.$dialogInput.unbind();
            self.$dialogInput.change(function(){

                if (clickFunction) {
                    clickFunction();
                }

                self.hide();
            });

            self.$dialogInput.show();
        } else {
            self.$dialogInput.hide();
        }

        self.$ok.unbind();
        self.$ok.click(function() {

            if (clickFunction) {
                clickFunction();
            }

            self.hide();
        });

    };

    igv.Dialog.prototype.hide = function () {

        if (this.$container.hasClass('igv-grid-container-dialog')) {
            this.$container.offset( { left: 0, top: 0 } );
        }
        this.$container.hide();
    };

    igv.Dialog.prototype.show = function ($host) {

        var body_scrolltop,
            track_origin,
            track_size,
            offset,
            _top,
            _left;

        body_scrolltop = $('body').scrollTop();

        if (this.$container.hasClass('igv-grid-container-dialog')) {

            offset = $host.offset();

            _top = offset.top + body_scrolltop;
            _left = $host.outerWidth() - 300;

            this.$container.offset( { left: _left, top: _top } );

            //track_origin = $host.offset();
            //track_size =
            //{
            //    width: $host.outerWidth(),
            //    height: $host.outerHeight()
            //};
            //this.$container.offset( { left: (track_size.width - 300), top: (track_origin.top + body_scrolltop) } );
            //this.$container.offset( igv.constrainBBox(this.$container, $(igv.browser.trackContainerDiv)) );
        }

        this.$container.show();

    };

    return igv;

})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by turner on 9/19/14.
 */
var igv = (function (igv) {

    igv.Popover = function ($parent, id) {

        this.markupWith$Parent($parent, id);

        this.$popoverContent.kinetic({});

    };

    igv.Popover.prototype.markupWith$Parent = function ($parent, id) {

        var self = this,
            popoverHeader;

        if (this.$parent) {
            return;
        }

        this.$parent = $parent;

        // popover container
        this.popover = $('<div class="igv-popover">');
        if (id) {
            this.popover.attr("id", id);
        }

        this.$parent.append(this.popover[ 0 ]);

        // popover header
        popoverHeader = $('<div class="igv-popoverHeader">');
        this.popover.append(popoverHeader[ 0 ]);

        igv.attachDialogCloseHandlerWithParent(popoverHeader, function () {
            self.hide();
        });

        // popover content
        this.$popoverContent = $('<div>');

        this.popover.append(this.$popoverContent[ 0 ]);

        this.popover.draggable();

    };

    igv.Popover.prototype.testData = function (rows) {
        var i,
            name,
            nameValues = [];

        for (i = 0; i < rows; i++) {
            name = "name " + i;
            nameValues.push({ name: name, value: "verbsgohuman" });
        }

        return nameValues;
    };

    igv.Popover.prototype.hide = function () {
        this.popover.hide();
    };

    igv.Popover.prototype.presentTrackMenu = function (pageX, pageY, trackView) {

        var $container = $('<div class="igv-track-menu-container">'),
            trackMenuItems = igv.trackMenuItems(this, trackView);

        trackMenuItems.forEach(function (item) {
            $container.append(item.object || item)
        });

        this.$popoverContent.empty();

        this.$popoverContent.removeClass("igv-popoverTrackPopupContent");
        this.$popoverContent.append($container);

        // Attach click handler AFTER inserting markup in DOM.
        // Insertion beforehand will cause it to have NO effect
        // when clicked.
        trackMenuItems.forEach(function (item) {

            if (item.object && item.click) {
                item.object.click( item.click );
            }

            if (item.init) {
                item.init();
            }

        });

        this.popover.css(popoverPosition(pageX, pageY, this));

        this.popover.show();

        this.popover.offset( igv.constrainBBox(this.popover, $(igv.browser.trackContainerDiv)) );

    };

    igv.Popover.prototype.presentTrackPopup = function (pageX, pageY, content, showOKButton) {

        var self = this,
            ok_button,
            markup;

        if (!content) {
            return;
        }

        markup = content;
        if (true === showOKButton) {
            ok_button = '<button name="button" class="igv-popover-ok-button">OK</button>';
            markup = content + ok_button;
        }

        this.$popoverContent.addClass("igv-popoverTrackPopupContent");

        this.$popoverContent.html(markup);

        this.popover.css(popoverPosition(pageX, pageY, this)).show();

        if (true === showOKButton) {
            $('.igv-dialog-close-container').hide();
            $('.igv-popover-ok-button').click(function(){
                self.hide();
            });
        } else {
            $('.igv-dialog-close-container').show();
        }

    };

    function popoverPosition(pageX, pageY, popoverWidget) {

        var left,
            containerCoordinates = { x: pageX, y: pageY },
            containerRect = { x: 0, y: 0, width: $(window).width(), height: $(window).height() },
            popupRect,
            popupX = pageX,
            popupY = pageY;

        popupX -= popoverWidget.$parent.offset().left;
        popupY -= popoverWidget.$parent.offset().top;
        popupRect = { x: popupX, y: popupY, width: popoverWidget.popover.outerWidth(), height: popoverWidget.popover.outerHeight() };

        left = popupX;
        if (containerCoordinates.x + popupRect.width > containerRect.width) {
            left = popupX - popupRect.width;
        }

        return { "left": left + "px", "top": popupY + "px" };
    }

    return igv;

})(igv || {});


/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by turner on 12/11/14.
 */
var igv = (function (igv) {

    igv.TrackMenuPopupDialog = function (trackMenu, dialogLabel, inputValue, ok, width, height) {

        var myself = this,
            dialogLabelRE,
            inputValueRE,
            htmlString;

        htmlString = '<div id="dialog-form" title="DIALOG_LABEL"><p class="validateTips"></p><form><fieldset><input type="text" name="name" id="name" value="INPUT_VALUE"></fieldset></form></div>';

        dialogLabelRE = new RegExp("DIALOG_LABEL", "g");
        htmlString = htmlString.replace(dialogLabelRE, dialogLabel);

        inputValueRE = new RegExp("INPUT_VALUE", "g");
        htmlString = htmlString.replace(inputValueRE, inputValue);

        $( "body" ).append( $.parseHTML( htmlString ) );

        this.dialogForm = $( "#dialog-form" );
        this.form = this.dialogForm.find( "form" );
        this.name = $( "#name" );
        this.tips = $( ".validateTips" );

        this.dialogForm.dialog({

            autoOpen: false,

            width: (width || 320),

            height: (height || 256),

            modal: true,

            buttons: {
                ok: ok,

                cancel: function() {

                    myself.dialogForm.dialog( "close" );
                }
            },

            close: function() {

                myself.form[ 0 ].reset();
                myself.dialogForm.remove();
                myself.dialogForm = undefined;
                if (trackMenu) trackMenu.hide();
            }
        });

        this.form.on("submit", function( event ) {

            event.preventDefault();

            $( "#users" ).find(" tbody").append( "<tr>" + "<td>" + myself.name.val() + "</td>" + "</tr>" );
            myself.dialogForm.dialog( "close" );
        });

    };

    igv.TrackMenuPopupDialog.prototype.updateTips = function ( t ) {

        var myself = this;

        this.tips.text( t ).addClass( "ui-state-highlight" );

        setTimeout(function() {
            myself.tips.removeClass( "ui-state-highlight", 1500 );
        }, 500 );
    };

    return igv;

})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by turner on 3/18/15.
 */
var igv = (function (igv) {

    igv.UserFeedback = function (parentObject) {

        var myself = this;

        this.userFeedback = $('<div class="igvUserFeedback">');
        parentObject.append(this.userFeedback[0]);

        // header
        this.userFeedbackHeader = $('<div class="igvUserFeedbackHeader">');
        this.userFeedback.append(this.userFeedbackHeader[0]);

        // alert
        this.userFeedbackAlert = $('<i class="fa fa-exclamation-triangle fa-20px igvUserFeedbackAlert">');
        this.userFeedbackHeader.append(this.userFeedbackAlert[0]);

        // dismiss
        this.userFeedbackDismiss = $('<i class="fa fa-times-circle fa-20px igvUserFeedbackDismiss">');
        this.userFeedbackHeader.append(this.userFeedbackDismiss[0]);

        this.userFeedbackDismiss.click(function () {
            myself.userFeedbackBodyCopy.html("");
            myself.userFeedback.hide();
        });

        // copy
        this.userFeedbackBodyCopy = $('<div class="igvUserFeedbackBodyCopy">');
        this.userFeedback.append(this.userFeedbackBodyCopy[0]);

    };

    igv.UserFeedback.prototype.show = function () {
        this.userFeedback.show();
    };

    igv.UserFeedback.prototype.hide = function () {
        this.userFeedback.hide();
    };

    igv.UserFeedback.prototype.bodyCopy = function (htmlString) {
        this.userFeedbackBodyCopy.html(htmlString);
    };

    return igv;

})(igv || {});
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


/**
 * Parser for VCF files.
 */

var igv = (function (igv) {


    igv.createVCFVariant = function (tokens) {

        var variant = new igv.Variant();

        variant.chr = tokens[0]; // TODO -- use genome aliases
        variant.pos = parseInt(tokens[1]) - 1;
        variant.names = tokens[2];    // id in VCF
        variant.referenceBases = tokens[3];
        variant.alternateBases = tokens[4];
        variant.quality = parseInt(tokens[5]);
        variant.filter = tokens[6];
        variant.info = tokens[7];

        computeStart(variant);

        return variant;

    }

    igv.createGAVariant = function (json) {

        var variant = new igv.Variant();

        variant.chr = json.referenceName;
        variant.pos = parseInt(json.start);
        variant.names = arrayToCommaString(json.names);
        variant.referenceBases = json.referenceBases + '';
        variant.alternateBases = json.alternateBases + '';
        variant.quality = json.quality;
        variant.filter = arrayToCommaString(json.filter);
        variant.info = json.info;

        // Need to build a hash of calls for fast lookup
        // Note from the GA4GH spec on call ID:
        //
        // The ID of the call set this variant call belongs to. If this field is not present,
        // the ordering of the call sets from a SearchCallSetsRequest over this GAVariantSet
        // is guaranteed to match the ordering of the calls on this GAVariant.
        // The number of results will also be the same.
        variant.calls = {};
        var order = 0, id;
        if(json.calls) {
            json.calls.forEach(function (call) {
                id = call.callSetId;
                variant.calls[id] = call;
                order++;

            })
        }

        computeStart(variant);

        return variant;

    }


    function computeStart(variant) {
        //Alleles
        altTokens = variant.alternateBases.split(",");

        if (altTokens.length > 0) {

            variant.alleles = [];
            variant.alleles.push(variant.referenceBases);

            variant.start = Number.MAX_VALUE;
            variant.end = 0;

            altTokens.forEach(function (alt) {
                var a, s, e, diff;

                variant.alleles.push(alt);

                if (alt.length > 0) {

                    diff = variant.referenceBases.length - alt.length;

                    if (diff > 0) {
                        // deletion, assume left padded
                        s = variant.pos + alt.length;
                        e = s + diff;
                    } else if (diff < 0) {
                        // Insertion, assume left padded, insertion begins to "right" of last ref base
                        s = variant.pos + variant.referenceBases.length;
                        e = s + 1;     // Insertion between s & 3
                    }
                    else {
                        // Substitution, SNP if seq.length == 1
                        s = variant.pos;
                        e = s + alt.length;
                    }
                    // variant.alleles.push({allele: alt, start: s, end: e});
                    variant.start = Math.min(variant.start, s);
                    variant.end = Math.max(variant.end, e);
                }

            });
        }
        else {
            // Is this even legal VCF?  (NO alt alleles)
            variant.start = variant.pos - 1;
            variant.end = variant.pos;
        }
    }


    igv.Variant = function () {

    }

    igv.Variant.prototype.popupData = function (genomicLocation) {

        var fields, gt,
            self = this;

        fields = [
            {name: "Chr", value: this.chr},
            {name: "Pos", value: (this.pos + 1)},
            {name: "Names", value: this.names ? this.names : ""},
            {name: "Ref", value: this.referenceBases},
            {name: "Alt", value: this.alternateBases},
            {name: "Qual", value: this.quality},
            {name: "Filter", value: this.filter},
         ];

        if(this.calls && this.calls.length === 1) {
            gt = this.alleles[this.calls[0].genotype[0]] + this.alleles[this.calls[0].genotype[1]];
            fields.push({name: "Genotype", value: gt});
        }

        if(this.info) {
            fields.push('<HR>');
            Object.keys(this.info).forEach(function (key) {
                fields.push({name: key, value: arrayToCommaString(self.info[key])});
            });
        }

        return fields;

    }


    function arrayToCommaString(array) {
        if (!array) return;
        var str = '', i;
        if (array.length > 0)
            str = array[0];
        for (i = 1; i < array.length; i++) {
            str += ", " + array[1];
        }
        return str;

    }

    return igv;
})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 University of California San Diego
 * Author: Jim Robinson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by jrobinson on 4/15/16.
 */

var igv = (function (igv) {

    var vGap = 2;
    var DEFAULT_VISIBILITY_WINDOW = 100000;

    igv.VariantTrack = function (config) {


        this.visibilityWindow = config.visibilityWindow === undefined ? 'compute' : config.visibilityWindow;

        igv.configTrack(this, config);

        this.displayMode = config.displayMode || "EXPANDED";    // COLLAPSED | EXPANDED | SQUISHED
        this.labelDisplayMode = config.labelDisplayMode;

        this.variantHeight = config.variantHeight || 10;
        this.squishedCallHeight = config.squishedCallHeight || 1;
        this.expandedCallHeight = config.expandedCallHeight || 10;

        this.featureHeight = config.featureHeight || 14;

        this.featureSource = new igv.FeatureSource(config);

        this.homrefColor = config.homrefColor || "rgb(200, 200, 200)"
        this.homvarColor = config.homvarColor || "rgb(17,248,254)";
        this.hetvarColor = config.hetvarColor || "rgb(34,12,253)";

        this.nRows = 1;  // Computed dynamically

    };


    igv.VariantTrack.prototype.getFileHeader = function () {
        var self = this;

        return new Promise(function (fulfill, reject) {
            if (typeof self.featureSource.getFileHeader === "function") {
                self.featureSource.getFileHeader().then(function (header) {
                    if (header) {
                        // Header (from track line).  Set properties,unless set in the config (config takes precedence)
                        if (header.name && !self.config.name) {
                            self.name = header.name;
                        }
                        if (header.color && !self.config.color) {
                            self.color = "rgb(" + header.color + ")";
                        }
                        self.callSets = header.callSets;

                        if ('compute' === self.visibilityWindow) {
                            computeVisibilityWindow.call(self);
                        }
                    }
                    fulfill(header);

                }).catch(reject);
            }
            else {
                fulfill(null);
            }
        });
    }


    function computeVisibilityWindow() {

        if (this.callSets) {
            if (this.callSets.length < 10) {
                this.visibilityWindow = DEFAULT_VISIBILITY_WINDOW;
            }
            else {
                this.visibilityWindow = 1000 + ((2500 / this.callSets.length) * 40);
            }
        }
        else {
            this.visibilityWindow = DEFAULT_VISIBILITY_WINDOW;
        }

        this.featureSource.visibilityWindow = this.visibilityWindow;


    }

    igv.VariantTrack.prototype.getFeatures = function (chr, bpStart, bpEnd) {

        var self = this;

        return new Promise(function (fulfill, reject) {

            self.featureSource.getFeatures(chr, bpStart, bpEnd).then(function (features) {
                fulfill(features);
            }).catch(reject);

        });
    }



    /**
     * The required height in pixels required for the track content.   This is not the visible track height, which
     * can be smaller (with a scrollbar) or larger.
     *
     * @param features
     * @returns {*}
     */
    igv.VariantTrack.prototype.computePixelHeight = function (features) {

        var callSets = this.callSets,
            nCalls = callSets ? callSets.length : 0,
            nRows,
            h;

        if (this.displayMode === "COLLAPSED") {
            this.nRows = 1;
            return 10 + this.variantHeight;
        }
        else {
            var maxRow = 0;
            if (features && (typeof features.forEach === "function")) {
                features.forEach(function (feature) {
                    if (feature.row && feature.row > maxRow) maxRow = feature.row;

                });
            }
            nRows = maxRow + 1;

            h = 10 + nRows * (this.variantHeight + vGap);
            this.nRows = nRows;  // Needed in draw function


            if ((nCalls * nRows * this.expandedCallHeight) > 2000) {
                this.expandedCallHeight = Math.max(1, 2000 / (nCalls * nRows));
            }


            return h + vGap + nCalls * nRows * (this.displayMode === "EXPANDED" ? this.expandedCallHeight : this.squishedCallHeight);

        }

    };

    igv.VariantTrack.prototype.draw = function (options) {

        var featureList = options.features,
            ctx = options.context,
            bpPerPixel = options.bpPerPixel,
            bpStart = options.bpStart,
            pixelWidth = options.pixelWidth,
            pixelHeight = options.pixelHeight,
            bpEnd = bpStart + pixelWidth * bpPerPixel + 1,
            callHeight = ("EXPANDED" === this.displayMode ? this.expandedCallHeight : this.squishedCallHeight),
            px, px1, pw, py, h, style, i, variant, call, callSet, j, allRef, allVar, callSets;

        this.variantBandHeight = 10 + this.nRows * (this.variantHeight + vGap);

        callSets = this.callSets;

        igv.graphics.fillRect(ctx, 0, 0, pixelWidth, pixelHeight, {'fillStyle': "rgb(255, 255, 255)"});

        if (callSets && callSets.length > 0 && "COLLAPSED" !== this.displayMode) {
            igv.graphics.strokeLine(ctx, 0, this.variantBandHeight, pixelWidth, this.variantBandHeight, {strokeStyle: 'rgb(224,224,224) '});
        }

        if (featureList) {
            for (i = 0, len = featureList.length; i < len; i++) {
                variant = featureList[i];
                if (variant.end < bpStart) continue;
                if (variant.start > bpEnd) break;

                py = 10 + ("COLLAPSED" === this.displayMode ? 0 : variant.row * (this.variantHeight + vGap));
                h = this.variantHeight;

                px = Math.round((variant.start - bpStart) / bpPerPixel);
                px1 = Math.round((variant.end - bpStart) / bpPerPixel);
                pw = Math.max(1, px1 - px);
                if (pw < 3) {
                    pw = 3;
                    px -= 1;
                } else if (pw > 5) {
                    px += 1;
                    pw -= 2;
                }

                ctx.fillStyle = this.color;
                ctx.fillRect(px, py, pw, h);


                if (callSets && variant.calls && "COLLAPSED" !== this.displayMode) {
                    h = callHeight;
                    for (j = 0; j < callSets.length; j++) {
                        callSet = callSets[j];
                        call = variant.calls[callSet.id];
                        if (call) {

                            // Determine genotype
                            allVar = allRef = true;  // until proven otherwise
                            call.genotype.forEach(function (g) {
                                if (g != 0) allRef = false;
                                if (g == 0) allVar = false;
                            });

                            if (allRef) {
                                ctx.fillStyle = this.homrefColor;
                            } else if (allVar) {
                                ctx.fillStyle = this.homvarColor;
                            } else {
                                ctx.fillStyle = this.hetvarColor;
                            }

                            py = this.variantBandHeight + vGap + (j + variant.row) * callHeight;
                            ctx.fillRect(px, py, pw, h);
                        }
                    }
                }
            }
        }
        else {
            console.log("No feature list");
        }

    };

    /**
     * Return "popup data" for feature @ genomic location.  Data is an array of key-value pairs
     */
    igv.VariantTrack.prototype.popupData = function (genomicLocation, xOffset, yOffset) {

        // We use the featureCache property rather than method to avoid async load.  If the
        // feature is not already loaded this won't work,  but the user wouldn't be mousing over it either.
        if (this.featureSource.featureCache) {

            var chr = igv.browser.referenceFrame.chr,  // TODO -- this should be passed in
                tolerance = Math.floor(2 * igv.browser.referenceFrame.bpPerPixel),  // We need some tolerance around genomicLocation, start with +/- 2 pixels
                featureList = this.featureSource.featureCache.queryFeatures(chr, genomicLocation - tolerance, genomicLocation + tolerance),
                popupData = [],
                self = this;


            if (featureList && featureList.length > 0) {

                featureList.forEach(function (variant) {

                    var row, callHeight, callSets, cs, call;

                    if ((variant.start <= genomicLocation + tolerance) &&
                        (variant.end > genomicLocation - tolerance)) {

                        if (popupData.length > 0) {
                            popupData.push('<HR>')
                        }

                        if ("COLLAPSED" == self.displayMode) {
                            Array.prototype.push.apply(popupData, variant.popupData(genomicLocation));
                        }
                        else {
                            if (yOffset <= self.variantBandHeight) {
                                // Variant
                                row = (Math.floor)((yOffset - 10 ) / (self.variantHeight + vGap));
                                if (variant.row === row) {
                                    Array.prototype.push.apply(popupData, variant.popupData(genomicLocation));
                                }
                            }
                            else {
                                // Call
                                callSets = self.callSets;
                                if (callSets && variant.calls) {
                                    callHeight = self.nRows * ("SQUISHED" === self.displayMode ? self.squishedCallHeight : self.expandedCallHeight);
                                    row = Math.floor((yOffset - self.variantBandHeight - vGap) / callHeight);
                                    cs = callSets[row];
                                    call = variant.calls[cs.id];
                                    Array.prototype.push.apply(popupData, extractPopupData(call, variant));
                                }
                            }
                        }
                    }
                });
            }
            return popupData;
        }
    }

    /**
     * Default popup text function -- just extracts string and number properties in random order.
     * @param feature
     * @returns {Array}
     */
    function extractPopupData(call, variant) {

        var gt = '', popupData;
        call.genotype.forEach(function (i) {
            if (i === 0) {
                gt += variant.referenceBases;
            }
            else {
                gt += variant.alternateBases[i - 1];
            }
        })

        popupData = [];

        if (call.callSetName !== undefined) {
            popupData.push({name: 'Name', value: call.callSetName});
        }
        popupData.push({name: 'Genotype', value: gt});
        if (call.phaseset !== undefined) {
            popupData.push({name: 'Phase set', value: call.phaseset});
        }
        if (call.genotypeLikelihood !== undefined) {
            popupData.push({name: 'genotypeLikelihood', value: call.genotypeLikelihood.toString()});
        }


        Object.keys(call.info).forEach(function (key) {
            popupData.push({name: key, value: call.info[key]});
        });

        return popupData;
    }

    igv.VariantTrack.prototype.popupMenuItems = function (popover) {

        var myself = this,
            menuItems = [],
            lut = {"COLLAPSED": "Collapse", "SQUISHED": "Squish", "EXPANDED": "Expand"},
            checkMark = '<i class="fa fa-check fa-check-shim"></i>',
            checkMarkNone = '<i class="fa fa-check fa-check-shim fa-check-hidden"></i>',
            trackMenuItem = '<div class=\"igv-track-menu-item\">',
            trackMenuItemFirst = '<div class=\"igv-track-menu-item igv-track-menu-border-top\">';

        menuItems.push(igv.colorPickerMenuItem(popover, this.trackView));

        ["COLLAPSED", "SQUISHED", "EXPANDED"].forEach(function (displayMode, index) {

            var chosen,
                str;

            chosen = (0 === index) ? trackMenuItemFirst : trackMenuItem;
            str = (displayMode === myself.displayMode) ? chosen + checkMark + lut[displayMode] + '</div>' : chosen + checkMarkNone + lut[displayMode] + '</div>';

            menuItems.push({
                object: $(str),
                click: function () {
                    popover.hide();
                    myself.displayMode = displayMode;
                    myself.trackView.update();
                }
            });

        });

        return menuItems;

    };


    return igv;

})
(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


/**
 * Parser for VCF files.
 */

var igv = (function (igv) {


    igv.VcfParser = function () {

    }

    igv.VcfParser.prototype.parseHeader = function (data) {

        var lines = data.splitLines(),
            len = lines.length,
            line,
            i,
            j,
            tokens,
            header = {},
            id,
            values,
            ltIdx,
            gtIdx,
            type;

        // First line must be file format
        if (lines[0].startsWith("##fileformat")) {
            header.version = lines[0].substr(13);
        }
        else {
            throw new Error("Invalid VCF file: missing fileformat line");
        }

        for (i = 1; i < len; i++) {
            line = lines[i].trim();
            if (line.startsWith("#")) {

                id = null;
                values = {};

                if (line.startsWith("##")) {

                    if (line.startsWith("##INFO") || line.startsWith("##FILTER") || line.startsWith("##FORMAT")) {

                        ltIdx = line.indexOf("<");
                        gtIdx = line.lastIndexOf(">");

                        if (!(ltIdx > 2 && gtIdx > 0)) {
                            console.log("Malformed VCF header line: " + line);
                            continue;
                        }

                        type = line.substring(2, ltIdx - 1);
                        if (!header[type])  header[type] = {};

                        //##INFO=<ID=AF,Number=A,Type=Float,Description="Allele frequency based on Flow Evaluator observation counts">
                        // ##FILTER=<ID=NOCALL,Description="Generic filter. Filtering details stored in FR info tag.">
                        // ##FORMAT=<ID=AF,Number=A,Type=Float,Description="Allele frequency based on Flow Evaluator observation counts">

                        tokens = igv.splitStringRespectingQuotes(line.substring(ltIdx + 1, gtIdx - 1), ",");

                        tokens.forEach(function (token) {
                            var kv = token.split("=");
                            if (kv.length > 1) {
                                if (kv[0] === "ID") {
                                    id = kv[1];
                                }
                                else {
                                    values[kv[0]] = kv[1];
                                }
                            }
                        });

                        if (id) {
                            header[type][id] = values;
                        }
                    }
                    else {
                        // Ignoring other ## header lines
                    }
                }
                else if (line.startsWith("#CHROM")) {
                    tokens = line.split("\t");

                    if (tokens.length > 8) {

                        // call set names -- use column index for id
                        header.callSets = [];
                        for (j = 9; j < tokens.length; j++) {
                            header.callSets.push({id: j, name: tokens[j]});
                        }
                    }
                }

            }
            else {
                break;
            }

        }

        this.header = header;  // Will need to intrepret genotypes and info field

        return header;
    }

    function extractCallFields(tokens) {

        var callFields = {
                genotypeIndex: -1,
                genotypeLikelihoodIndex: -1,
                phasesetIndex: -1,
                fields: tokens
            },
            i;

        for (i = 0; i < tokens.length; i++) {

            if ("GT" === tokens[i]) {
                callFields.genotypeIndex = i;
            }
            else if ("GL" === tokens[i]) {
                callFields.genotypeLikelihoodIndex = i;
            }
            else if ("PS" === tokens[i]) {
                callFields.phasesetIndex = i;
            }
        }
        return callFields;

    }

    /**
     * Parse data as a collection of Variant objects.
     *
     * @param data
     * @returns {Array}
     */
    igv.VcfParser.prototype.parseFeatures = function (data) {

        var lines = data.split("\n"),
            allFeatures = [],
            callSets = this.header.callSets;

        lines.forEach(function (line) {

            var variant,
                tokens,
                callFields,
                index,
                token;

            if (!line.startsWith("#")) {

                tokens = line.split("\t");

                if (tokens.length >= 8) {
                    variant = new Variant(tokens);
                    variant.header = this.header;       // Keep a pointer to the header to interpret fields for popup text
                    allFeatures.push(variant);

                    if (tokens.length > 9) {

                        // Format
                        callFields = extractCallFields(tokens[8].split(":"));

                        variant.calls = {};

                        for (index = 9; index < tokens.length; index++) {

                            token = tokens[index];

                            var callSet = callSets[index - 9],
                                call = {
                                    callSetName: callSet.name,
                                    info: {}
                                };

                            variant.calls[callSet.id] = call;

                            token.split(":").forEach(function (callToken, index) {
                                switch (index) {
                                    case callFields.genotypeIndex:
                                        call.genotype = [];
                                        callToken.split(/[\|\/]/).forEach(function (s) {
                                            call.genotype.push(parseInt(s));
                                        });
                                        break;

                                    case callFields.genotypeLikelihoodIndex:
                                        call.genotypeLikelihood = [];
                                        callToken.split(",").forEach(function (s) {
                                            call.genotype.push(parseFloat(s));
                                        });
                                        break;

                                    case callFields.phasesetIndex:
                                        call.phaseset = callToken;
                                        break;

                                    default:
                                        call.info[callFields.fields[index]] = callToken;
                                }
                            });
                        }

                    }

                }
            }
        });

        return allFeatures;

    }


    function Variant(tokens) {

        var self = this,
            altTokens;

        this.chr = tokens[0]; // TODO -- use genome aliases
        this.pos = parseInt(tokens[1]);
        this.names = tokens[2];    // id in VCF
        this.referenceBases = tokens[3];
        this.alternateBases = tokens[4];
        this.quality = parseInt(tokens[5]);
        this.filter = tokens[6];
        this.info = tokens[7];

        // "ids" ("names" in ga4gh)

        //Alleles
        altTokens = this.alternateBases.split(",");

        if (altTokens.length > 0) {

            this.alleles = [];

            this.start = Number.MAX_VALUE;
            this.end = 0;

            altTokens.forEach(function (alt) {
                var a, s, e, diff;
                if (alt.length > 0) {

                    diff = self.referenceBases.length - alt.length;

                    if (diff > 0) {
                        // deletion, assume left padded
                        s = self.pos - 1 + alt.length;
                        e = s + diff;
                    } else if (diff < 0) {
                        // Insertion, assume left padded, insertion begins to "right" of last ref base
                        s = self.pos - 1 + self.referenceBases.length;
                        e = s + 1;     // Insertion between s & 3
                    }
                    else {
                        // Substitution, SNP if seq.length == 1
                        s = self.pos - 1;
                        e = s + alt.length;
                    }
                    self.alleles.push({allele: alt, start: s, end: e});
                    self.start = Math.min(self.start, s);
                    self.end = Math.max(self.end, e);
                }

            });
        }
        else {
            // Is this even legal VCF?  (NO alt alleles)
            this.start = this.pos - 1;
            this.end = this.pos;
        }

        // TODO -- genotype fields
    }

    Variant.prototype.popupData = function (genomicLocation) {

        var fields, infoFields, nameString;


        fields = [
            {name: "Names", value: this.names},
            {name: "Ref", value: this.referenceBases},
            {name: "Alt", value: this.alternateBases},
            {name: "Qual", value: this.quality},
            {name: "Filter", value: this.filter},
            "<hr>"
        ];

        infoFields = this.info.split(";");
        infoFields.forEach(function (f) {
            var tokens = f.split("=");
            if (tokens.length > 1) {
                fields.push({name: tokens[0], value: tokens[1]});   // TODO -- use header to add descriptive tooltip
            }
        });


        return fields;

    }


    return igv;
})(igv || {});

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by turner on 5/22/15.
 */
var igv = (function (igv) {

    igv.WindowSizePanel = function ($parent) {

        this.contentDiv = $('<div class="igv-windowsizepanel-content-div"></div>');
        $parent.append(this.contentDiv[0]);

    };

    igv.WindowSizePanel.prototype.update = function (size) {

        var value,
            floored,
            denom,
            units;

        this.contentDiv.text( prettyNumber( size ) );

        function prettyNumber(size) {

            if (size > 1e7) {
                denom = 1e6;
                units = " mb";
            } else if (size > 1e4) {

                denom = 1e3;
                units = " kb";

                value = size/denom;
                floored = Math.floor(value);
                return igv.numberFormatter(floored) + units;
            } else {
                return igv.numberFormatter(size) + " bp";
            }

            value = size/denom;
            floored = Math.floor(value);

            return floored.toString() + units;
        }

    };


    return igv;
})
(igv || {});
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Javascript ZLib
// By Thomas Down 2010-2011
//
// Based very heavily on portions of jzlib (by ymnk@jcraft.com), who in
// turn credits Jean-loup Gailly and Mark Adler for the original zlib code.
//
// inflate.js: ZLib inflate code
//

//
// Shared constants
//

var MAX_WBITS=15; // 32K LZ77 window
var DEF_WBITS=MAX_WBITS;
var MAX_MEM_LEVEL=9;
var MANY=1440;
var BMAX = 15;

// preset dictionary flag in zlib header
var PRESET_DICT=0x20;

var Z_NO_FLUSH=0;
var Z_PARTIAL_FLUSH=1;
var Z_SYNC_FLUSH=2;
var Z_FULL_FLUSH=3;
var Z_FINISH=4;

var Z_DEFLATED=8;

var Z_OK=0;
var Z_STREAM_END=1;
var Z_NEED_DICT=2;
var Z_ERRNO=-1;
var Z_STREAM_ERROR=-2;
var Z_DATA_ERROR=-3;
var Z_MEM_ERROR=-4;
var Z_BUF_ERROR=-5;
var Z_VERSION_ERROR=-6;

var METHOD=0;   // waiting for method byte
var FLAG=1;     // waiting for flag byte
var DICT4=2;    // four dictionary check bytes to go
var DICT3=3;    // three dictionary check bytes to go
var DICT2=4;    // two dictionary check bytes to go
var DICT1=5;    // one dictionary check byte to go
var DICT0=6;    // waiting for inflateSetDictionary
var BLOCKS=7;   // decompressing blocks
var CHECK4=8;   // four check bytes to go
var CHECK3=9;   // three check bytes to go
var CHECK2=10;  // two check bytes to go
var CHECK1=11;  // one check byte to go
var DONE=12;    // finished check, done
var BAD=13;     // got an error--stay here

var inflate_mask = [0x00000000, 0x00000001, 0x00000003, 0x00000007, 0x0000000f, 0x0000001f, 0x0000003f, 0x0000007f, 0x000000ff, 0x000001ff, 0x000003ff, 0x000007ff, 0x00000fff, 0x00001fff, 0x00003fff, 0x00007fff, 0x0000ffff];

var IB_TYPE=0;  // get type bits (3, including end bit)
var IB_LENS=1;  // get lengths for stored
var IB_STORED=2;// processing stored block
var IB_TABLE=3; // get table lengths
var IB_BTREE=4; // get bit lengths tree for a dynamic block
var IB_DTREE=5; // get length, distance trees for a dynamic block
var IB_CODES=6; // processing fixed or dynamic block
var IB_DRY=7;   // output remaining window bytes
var IB_DONE=8;  // finished last block, done
var IB_BAD=9;   // ot a data error--stuck here

var fixed_bl = 9;
var fixed_bd = 5;

var fixed_tl = [
    96,7,256, 0,8,80, 0,8,16, 84,8,115,
    82,7,31, 0,8,112, 0,8,48, 0,9,192,
    80,7,10, 0,8,96, 0,8,32, 0,9,160,
    0,8,0, 0,8,128, 0,8,64, 0,9,224,
    80,7,6, 0,8,88, 0,8,24, 0,9,144,
    83,7,59, 0,8,120, 0,8,56, 0,9,208,
    81,7,17, 0,8,104, 0,8,40, 0,9,176,
    0,8,8, 0,8,136, 0,8,72, 0,9,240,
    80,7,4, 0,8,84, 0,8,20, 85,8,227,
    83,7,43, 0,8,116, 0,8,52, 0,9,200,
    81,7,13, 0,8,100, 0,8,36, 0,9,168,
    0,8,4, 0,8,132, 0,8,68, 0,9,232,
    80,7,8, 0,8,92, 0,8,28, 0,9,152,
    84,7,83, 0,8,124, 0,8,60, 0,9,216,
    82,7,23, 0,8,108, 0,8,44, 0,9,184,
    0,8,12, 0,8,140, 0,8,76, 0,9,248,
    80,7,3, 0,8,82, 0,8,18, 85,8,163,
    83,7,35, 0,8,114, 0,8,50, 0,9,196,
    81,7,11, 0,8,98, 0,8,34, 0,9,164,
    0,8,2, 0,8,130, 0,8,66, 0,9,228,
    80,7,7, 0,8,90, 0,8,26, 0,9,148,
    84,7,67, 0,8,122, 0,8,58, 0,9,212,
    82,7,19, 0,8,106, 0,8,42, 0,9,180,
    0,8,10, 0,8,138, 0,8,74, 0,9,244,
    80,7,5, 0,8,86, 0,8,22, 192,8,0,
    83,7,51, 0,8,118, 0,8,54, 0,9,204,
    81,7,15, 0,8,102, 0,8,38, 0,9,172,
    0,8,6, 0,8,134, 0,8,70, 0,9,236,
    80,7,9, 0,8,94, 0,8,30, 0,9,156,
    84,7,99, 0,8,126, 0,8,62, 0,9,220,
    82,7,27, 0,8,110, 0,8,46, 0,9,188,
    0,8,14, 0,8,142, 0,8,78, 0,9,252,
    96,7,256, 0,8,81, 0,8,17, 85,8,131,
    82,7,31, 0,8,113, 0,8,49, 0,9,194,
    80,7,10, 0,8,97, 0,8,33, 0,9,162,
    0,8,1, 0,8,129, 0,8,65, 0,9,226,
    80,7,6, 0,8,89, 0,8,25, 0,9,146,
    83,7,59, 0,8,121, 0,8,57, 0,9,210,
    81,7,17, 0,8,105, 0,8,41, 0,9,178,
    0,8,9, 0,8,137, 0,8,73, 0,9,242,
    80,7,4, 0,8,85, 0,8,21, 80,8,258,
    83,7,43, 0,8,117, 0,8,53, 0,9,202,
    81,7,13, 0,8,101, 0,8,37, 0,9,170,
    0,8,5, 0,8,133, 0,8,69, 0,9,234,
    80,7,8, 0,8,93, 0,8,29, 0,9,154,
    84,7,83, 0,8,125, 0,8,61, 0,9,218,
    82,7,23, 0,8,109, 0,8,45, 0,9,186,
    0,8,13, 0,8,141, 0,8,77, 0,9,250,
    80,7,3, 0,8,83, 0,8,19, 85,8,195,
    83,7,35, 0,8,115, 0,8,51, 0,9,198,
    81,7,11, 0,8,99, 0,8,35, 0,9,166,
    0,8,3, 0,8,131, 0,8,67, 0,9,230,
    80,7,7, 0,8,91, 0,8,27, 0,9,150,
    84,7,67, 0,8,123, 0,8,59, 0,9,214,
    82,7,19, 0,8,107, 0,8,43, 0,9,182,
    0,8,11, 0,8,139, 0,8,75, 0,9,246,
    80,7,5, 0,8,87, 0,8,23, 192,8,0,
    83,7,51, 0,8,119, 0,8,55, 0,9,206,
    81,7,15, 0,8,103, 0,8,39, 0,9,174,
    0,8,7, 0,8,135, 0,8,71, 0,9,238,
    80,7,9, 0,8,95, 0,8,31, 0,9,158,
    84,7,99, 0,8,127, 0,8,63, 0,9,222,
    82,7,27, 0,8,111, 0,8,47, 0,9,190,
    0,8,15, 0,8,143, 0,8,79, 0,9,254,
    96,7,256, 0,8,80, 0,8,16, 84,8,115,
    82,7,31, 0,8,112, 0,8,48, 0,9,193,

    80,7,10, 0,8,96, 0,8,32, 0,9,161,
    0,8,0, 0,8,128, 0,8,64, 0,9,225,
    80,7,6, 0,8,88, 0,8,24, 0,9,145,
    83,7,59, 0,8,120, 0,8,56, 0,9,209,
    81,7,17, 0,8,104, 0,8,40, 0,9,177,
    0,8,8, 0,8,136, 0,8,72, 0,9,241,
    80,7,4, 0,8,84, 0,8,20, 85,8,227,
    83,7,43, 0,8,116, 0,8,52, 0,9,201,
    81,7,13, 0,8,100, 0,8,36, 0,9,169,
    0,8,4, 0,8,132, 0,8,68, 0,9,233,
    80,7,8, 0,8,92, 0,8,28, 0,9,153,
    84,7,83, 0,8,124, 0,8,60, 0,9,217,
    82,7,23, 0,8,108, 0,8,44, 0,9,185,
    0,8,12, 0,8,140, 0,8,76, 0,9,249,
    80,7,3, 0,8,82, 0,8,18, 85,8,163,
    83,7,35, 0,8,114, 0,8,50, 0,9,197,
    81,7,11, 0,8,98, 0,8,34, 0,9,165,
    0,8,2, 0,8,130, 0,8,66, 0,9,229,
    80,7,7, 0,8,90, 0,8,26, 0,9,149,
    84,7,67, 0,8,122, 0,8,58, 0,9,213,
    82,7,19, 0,8,106, 0,8,42, 0,9,181,
    0,8,10, 0,8,138, 0,8,74, 0,9,245,
    80,7,5, 0,8,86, 0,8,22, 192,8,0,
    83,7,51, 0,8,118, 0,8,54, 0,9,205,
    81,7,15, 0,8,102, 0,8,38, 0,9,173,
    0,8,6, 0,8,134, 0,8,70, 0,9,237,
    80,7,9, 0,8,94, 0,8,30, 0,9,157,
    84,7,99, 0,8,126, 0,8,62, 0,9,221,
    82,7,27, 0,8,110, 0,8,46, 0,9,189,
    0,8,14, 0,8,142, 0,8,78, 0,9,253,
    96,7,256, 0,8,81, 0,8,17, 85,8,131,
    82,7,31, 0,8,113, 0,8,49, 0,9,195,
    80,7,10, 0,8,97, 0,8,33, 0,9,163,
    0,8,1, 0,8,129, 0,8,65, 0,9,227,
    80,7,6, 0,8,89, 0,8,25, 0,9,147,
    83,7,59, 0,8,121, 0,8,57, 0,9,211,
    81,7,17, 0,8,105, 0,8,41, 0,9,179,
    0,8,9, 0,8,137, 0,8,73, 0,9,243,
    80,7,4, 0,8,85, 0,8,21, 80,8,258,
    83,7,43, 0,8,117, 0,8,53, 0,9,203,
    81,7,13, 0,8,101, 0,8,37, 0,9,171,
    0,8,5, 0,8,133, 0,8,69, 0,9,235,
    80,7,8, 0,8,93, 0,8,29, 0,9,155,
    84,7,83, 0,8,125, 0,8,61, 0,9,219,
    82,7,23, 0,8,109, 0,8,45, 0,9,187,
    0,8,13, 0,8,141, 0,8,77, 0,9,251,
    80,7,3, 0,8,83, 0,8,19, 85,8,195,
    83,7,35, 0,8,115, 0,8,51, 0,9,199,
    81,7,11, 0,8,99, 0,8,35, 0,9,167,
    0,8,3, 0,8,131, 0,8,67, 0,9,231,
    80,7,7, 0,8,91, 0,8,27, 0,9,151,
    84,7,67, 0,8,123, 0,8,59, 0,9,215,
    82,7,19, 0,8,107, 0,8,43, 0,9,183,
    0,8,11, 0,8,139, 0,8,75, 0,9,247,
    80,7,5, 0,8,87, 0,8,23, 192,8,0,
    83,7,51, 0,8,119, 0,8,55, 0,9,207,
    81,7,15, 0,8,103, 0,8,39, 0,9,175,
    0,8,7, 0,8,135, 0,8,71, 0,9,239,
    80,7,9, 0,8,95, 0,8,31, 0,9,159,
    84,7,99, 0,8,127, 0,8,63, 0,9,223,
    82,7,27, 0,8,111, 0,8,47, 0,9,191,
    0,8,15, 0,8,143, 0,8,79, 0,9,255
];
var fixed_td = [
    80,5,1, 87,5,257, 83,5,17, 91,5,4097,
    81,5,5, 89,5,1025, 85,5,65, 93,5,16385,
    80,5,3, 88,5,513, 84,5,33, 92,5,8193,
    82,5,9, 90,5,2049, 86,5,129, 192,5,24577,
    80,5,2, 87,5,385, 83,5,25, 91,5,6145,
    81,5,7, 89,5,1537, 85,5,97, 93,5,24577,
    80,5,4, 88,5,769, 84,5,49, 92,5,12289,
    82,5,13, 90,5,3073, 86,5,193, 192,5,24577
];

  // Tables for deflate from PKZIP's appnote.txt.
  var cplens = [ // Copy lengths for literal codes 257..285
        3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31,
        35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258, 0, 0
  ];

  // see note #13 above about 258
  var cplext = [ // Extra bits for literal codes 257..285
        0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2,
        3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0, 112, 112  // 112==invalid
  ];

 var cpdist = [ // Copy offsets for distance codes 0..29
        1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193,
        257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145,
        8193, 12289, 16385, 24577
  ];

  var cpdext = [ // Extra bits for distance codes
        0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6,
        7, 7, 8, 8, 9, 9, 10, 10, 11, 11,
        12, 12, 13, 13];

//
// ZStream.java
//

function ZStream() {
}


ZStream.prototype.inflateInit = function(w, nowrap) {
    if (!w) {
	w = DEF_WBITS;
    }
    if (nowrap) {
	nowrap = false;
    }
    this.istate = new Inflate();
    return this.istate.inflateInit(this, nowrap?-w:w);
}

ZStream.prototype.inflate = function(f) {
    if(this.istate==null) return Z_STREAM_ERROR;
    return this.istate.inflate(this, f);
}

ZStream.prototype.inflateEnd = function(){
    if(this.istate==null) return Z_STREAM_ERROR;
    var ret=istate.inflateEnd(this);
    this.istate = null;
    return ret;
}
ZStream.prototype.inflateSync = function(){
    // if(istate == null) return Z_STREAM_ERROR;
    return istate.inflateSync(this);
}
ZStream.prototype.inflateSetDictionary = function(dictionary, dictLength){
    // if(istate == null) return Z_STREAM_ERROR;
    return istate.inflateSetDictionary(this, dictionary, dictLength);
}

/*

  public int deflateInit(int level){
    return deflateInit(level, MAX_WBITS);
  }
  public int deflateInit(int level, boolean nowrap){
    return deflateInit(level, MAX_WBITS, nowrap);
  }
  public int deflateInit(int level, int bits){
    return deflateInit(level, bits, false);
  }
  public int deflateInit(int level, int bits, boolean nowrap){
    dstate=new Deflate();
    return dstate.deflateInit(this, level, nowrap?-bits:bits);
  }
  public int deflate(int flush){
    if(dstate==null){
      return Z_STREAM_ERROR;
    }
    return dstate.deflate(this, flush);
  }
  public int deflateEnd(){
    if(dstate==null) return Z_STREAM_ERROR;
    int ret=dstate.deflateEnd();
    dstate=null;
    return ret;
  }
  public int deflateParams(int level, int strategy){
    if(dstate==null) return Z_STREAM_ERROR;
    return dstate.deflateParams(this, level, strategy);
  }
  public int deflateSetDictionary (byte[] dictionary, int dictLength){
    if(dstate == null)
      return Z_STREAM_ERROR;
    return dstate.deflateSetDictionary(this, dictionary, dictLength);
  }

*/

/*
  // Flush as much pending output as possible. All deflate() output goes
  // through this function so some applications may wish to modify it
  // to avoid allocating a large strm->next_out buffer and copying into it.
  // (See also read_buf()).
  void flush_pending(){
    int len=dstate.pending;

    if(len>avail_out) len=avail_out;
    if(len==0) return;

    if(dstate.pending_buf.length<=dstate.pending_out ||
       next_out.length<=next_out_index ||
       dstate.pending_buf.length<(dstate.pending_out+len) ||
       next_out.length<(next_out_index+len)){
      System.out.println(dstate.pending_buf.length+", "+dstate.pending_out+
			 ", "+next_out.length+", "+next_out_index+", "+len);
      System.out.println("avail_out="+avail_out);
    }

    System.arraycopy(dstate.pending_buf, dstate.pending_out,
		     next_out, next_out_index, len);

    next_out_index+=len;
    dstate.pending_out+=len;
    total_out+=len;
    avail_out-=len;
    dstate.pending-=len;
    if(dstate.pending==0){
      dstate.pending_out=0;
    }
  }

  // Read a new buffer from the current input stream, update the adler32
  // and total number of bytes read.  All deflate() input goes through
  // this function so some applications may wish to modify it to avoid
  // allocating a large strm->next_in buffer and copying from it.
  // (See also flush_pending()).
  int read_buf(byte[] buf, int start, int size) {
    int len=avail_in;

    if(len>size) len=size;
    if(len==0) return 0;

    avail_in-=len;

    if(dstate.noheader==0) {
      adler=_adler.adler32(adler, next_in, next_in_index, len);
    }
    System.arraycopy(next_in, next_in_index, buf, start, len);
    next_in_index  += len;
    total_in += len;
    return len;
  }

  public void free(){
    next_in=null;
    next_out=null;
    msg=null;
    _adler=null;
  }
}
*/


//
// Inflate.java
//

function Inflate() {
    this.was = [0];
}

Inflate.prototype.inflateReset = function(z) {
    if(z == null || z.istate == null) return Z_STREAM_ERROR;
    
    z.total_in = z.total_out = 0;
    z.msg = null;
    z.istate.mode = z.istate.nowrap!=0 ? BLOCKS : METHOD;
    z.istate.blocks.reset(z, null);
    return Z_OK;
}

Inflate.prototype.inflateEnd = function(z){
    if(this.blocks != null)
      this.blocks.free(z);
    this.blocks=null;
    return Z_OK;
}

Inflate.prototype.inflateInit = function(z, w){
    z.msg = null;
    this.blocks = null;

    // handle undocumented nowrap option (no zlib header or check)
    nowrap = 0;
    if(w < 0){
      w = - w;
      nowrap = 1;
    }

    // set window size
    if(w<8 ||w>15){
      this.inflateEnd(z);
      return Z_STREAM_ERROR;
    }
    this.wbits=w;

    z.istate.blocks=new InfBlocks(z, 
				  z.istate.nowrap!=0 ? null : this,
				  1<<w);

    // reset state
    this.inflateReset(z);
    return Z_OK;
  }

Inflate.prototype.inflate = function(z, f){
    var r, b;

    if(z == null || z.istate == null || z.next_in == null)
      return Z_STREAM_ERROR;
    f = f == Z_FINISH ? Z_BUF_ERROR : Z_OK;
    r = Z_BUF_ERROR;
    while (true){
      switch (z.istate.mode){
      case METHOD:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        if(((z.istate.method = z.next_in[z.next_in_index++])&0xf)!=Z_DEFLATED){
          z.istate.mode = BAD;
          z.msg="unknown compression method";
          z.istate.marker = 5;       // can't try inflateSync
          break;
        }
        if((z.istate.method>>4)+8>z.istate.wbits){
          z.istate.mode = BAD;
          z.msg="invalid window size";
          z.istate.marker = 5;       // can't try inflateSync
          break;
        }
        z.istate.mode=FLAG;
      case FLAG:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        b = (z.next_in[z.next_in_index++])&0xff;

        if((((z.istate.method << 8)+b) % 31)!=0){
          z.istate.mode = BAD;
          z.msg = "incorrect header check";
          z.istate.marker = 5;       // can't try inflateSync
          break;
        }

        if((b&PRESET_DICT)==0){
          z.istate.mode = BLOCKS;
          break;
        }
        z.istate.mode = DICT4;
      case DICT4:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need=((z.next_in[z.next_in_index++]&0xff)<<24)&0xff000000;
        z.istate.mode=DICT3;
      case DICT3:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need+=((z.next_in[z.next_in_index++]&0xff)<<16)&0xff0000;
        z.istate.mode=DICT2;
      case DICT2:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need+=((z.next_in[z.next_in_index++]&0xff)<<8)&0xff00;
        z.istate.mode=DICT1;
      case DICT1:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need += (z.next_in[z.next_in_index++]&0xff);
        z.adler = z.istate.need;
        z.istate.mode = DICT0;
        return Z_NEED_DICT;
      case DICT0:
        z.istate.mode = BAD;
        z.msg = "need dictionary";
        z.istate.marker = 0;       // can try inflateSync
        return Z_STREAM_ERROR;
      case BLOCKS:

        r = z.istate.blocks.proc(z, r);
        if(r == Z_DATA_ERROR){
          z.istate.mode = BAD;
          z.istate.marker = 0;     // can try inflateSync
          break;
        }
        if(r == Z_OK){
          r = f;
        }
        if(r != Z_STREAM_END){
          return r;
        }
        r = f;
        z.istate.blocks.reset(z, z.istate.was);
        if(z.istate.nowrap!=0){
          z.istate.mode=DONE;
          break;
        }
        z.istate.mode=CHECK4;
      case CHECK4:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need=((z.next_in[z.next_in_index++]&0xff)<<24)&0xff000000;
        z.istate.mode=CHECK3;
      case CHECK3:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need+=((z.next_in[z.next_in_index++]&0xff)<<16)&0xff0000;
        z.istate.mode = CHECK2;
      case CHECK2:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need+=((z.next_in[z.next_in_index++]&0xff)<<8)&0xff00;
        z.istate.mode = CHECK1;
      case CHECK1:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need+=(z.next_in[z.next_in_index++]&0xff);

        if(((z.istate.was[0])) != ((z.istate.need))){
          z.istate.mode = BAD;
          z.msg = "incorrect data check";
          z.istate.marker = 5;       // can't try inflateSync
          break;
        }

        z.istate.mode = DONE;
      case DONE:
        return Z_STREAM_END;
      case BAD:
        return Z_DATA_ERROR;
      default:
        return Z_STREAM_ERROR;
      }
    }
  }


Inflate.prototype.inflateSetDictionary = function(z,  dictionary, dictLength) {
    var index=0;
    var length = dictLength;
    if(z==null || z.istate == null|| z.istate.mode != DICT0)
      return Z_STREAM_ERROR;

    if(z._adler.adler32(1, dictionary, 0, dictLength)!=z.adler){
      return Z_DATA_ERROR;
    }

    z.adler = z._adler.adler32(0, null, 0, 0);

    if(length >= (1<<z.istate.wbits)){
      length = (1<<z.istate.wbits)-1;
      index=dictLength - length;
    }
    z.istate.blocks.set_dictionary(dictionary, index, length);
    z.istate.mode = BLOCKS;
    return Z_OK;
  }

//  static private byte[] mark = {(byte)0, (byte)0, (byte)0xff, (byte)0xff};
var mark = [0, 0, 255, 255]

Inflate.prototype.inflateSync = function(z){
    var n;       // number of bytes to look at
    var p;       // pointer to bytes
    var m;       // number of marker bytes found in a row
    var r, w;   // temporaries to save total_in and total_out

    // set up
    if(z == null || z.istate == null)
      return Z_STREAM_ERROR;
    if(z.istate.mode != BAD){
      z.istate.mode = BAD;
      z.istate.marker = 0;
    }
    if((n=z.avail_in)==0)
      return Z_BUF_ERROR;
    p=z.next_in_index;
    m=z.istate.marker;

    // search
    while (n!=0 && m < 4){
      if(z.next_in[p] == mark[m]){
        m++;
      }
      else if(z.next_in[p]!=0){
        m = 0;
      }
      else{
        m = 4 - m;
      }
      p++; n--;
    }

    // restore
    z.total_in += p-z.next_in_index;
    z.next_in_index = p;
    z.avail_in = n;
    z.istate.marker = m;

    // return no joy or set up to restart on a new block
    if(m != 4){
      return Z_DATA_ERROR;
    }
    r=z.total_in;  w=z.total_out;
    this.inflateReset(z);
    z.total_in=r;  z.total_out = w;
    z.istate.mode = BLOCKS;
    return Z_OK;
}

  // Returns true if inflate is currently at the end of a block generated
  // by Z_SYNC_FLUSH or Z_FULL_FLUSH. This function is used by one PPP
  // implementation to provide an additional safety check. PPP uses Z_SYNC_FLUSH
  // but removes the length bytes of the resulting empty stored block. When
  // decompressing, PPP checks that at the end of input packet, inflate is
  // waiting for these length bytes.
Inflate.prototype.inflateSyncPoint = function(z){
    if(z == null || z.istate == null || z.istate.blocks == null)
      return Z_STREAM_ERROR;
    return z.istate.blocks.sync_point();
}


//
// InfBlocks.java
//

var INFBLOCKS_BORDER = [16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15];

function InfBlocks(z, checkfn, w) {
    this.hufts=new Int32Array(MANY*3);
    this.window=new Uint8Array(w);
    this.end=w;
    this.checkfn = checkfn;
    this.mode = IB_TYPE;
    this.reset(z, null);

    this.left = 0;            // if STORED, bytes left to copy 

    this.table = 0;           // table lengths (14 bits) 
    this.index = 0;           // index into blens (or border) 
    this.blens = null;         // bit lengths of codes 
    this.bb=new Int32Array(1); // bit length tree depth 
    this.tb=new Int32Array(1); // bit length decoding tree 

    this.codes = new InfCodes();

    this.last = 0;            // true if this block is the last block 

  // mode independent information 
    this.bitk = 0;            // bits in bit buffer 
    this.bitb = 0;            // bit buffer 
    this.read = 0;            // window read pointer 
    this.write = 0;           // window write pointer 
    this.check = 0;          // check on output 

    this.inftree=new InfTree();
}




InfBlocks.prototype.reset = function(z, c){
    if(c) c[0]=this.check;
    if(this.mode==IB_CODES){
      this.codes.free(z);
    }
    this.mode=IB_TYPE;
    this.bitk=0;
    this.bitb=0;
    this.read=this.write=0;

    if(this.checkfn)
      z.adler=this.check=z._adler.adler32(0, null, 0, 0);
  }

 InfBlocks.prototype.proc = function(z, r){
    var t;              // temporary storage
    var b;              // bit buffer
    var k;              // bits in bit buffer
    var p;              // input data pointer
    var n;              // bytes available there
    var q;              // output window write pointer
    var m;              // bytes to end of window or read pointer

    // copy input/output information to locals (UPDATE macro restores)
    {p=z.next_in_index;n=z.avail_in;b=this.bitb;k=this.bitk;}
    {q=this.write;m=(q<this.read ? this.read-q-1 : this.end-q);}

    // process input based on current state
    while(true){
      switch (this.mode){
      case IB_TYPE:

	while(k<(3)){
	  if(n!=0){
	    r=Z_OK;
	  }
	  else{
	    this.bitb=b; this.bitk=k; 
	    z.avail_in=n;
	    z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    this.write=q;
	    return this.inflate_flush(z,r);
	  };
	  n--;
	  b|=(z.next_in[p++]&0xff)<<k;
	  k+=8;
	}
	t = (b & 7);
	this.last = t & 1;

	switch (t >>> 1){
        case 0:                         // stored 
          {b>>>=(3);k-=(3);}
          t = k & 7;                    // go to byte boundary

          {b>>>=(t);k-=(t);}
          this.mode = IB_LENS;                  // get length of stored block
          break;
        case 1:                         // fixed
          {
              var bl=new Int32Array(1);
	      var bd=new Int32Array(1);
              var tl=[];
	      var td=[];

	      inflate_trees_fixed(bl, bd, tl, td, z);
              this.codes.init(bl[0], bd[0], tl[0], 0, td[0], 0, z);
          }

          {b>>>=(3);k-=(3);}

          this.mode = IB_CODES;
          break;
        case 2:                         // dynamic

          {b>>>=(3);k-=(3);}

          this.mode = IB_TABLE;
          break;
        case 3:                         // illegal

          {b>>>=(3);k-=(3);}
          this.mode = BAD;
          z.msg = "invalid block type";
          r = Z_DATA_ERROR;

	  this.bitb=b; this.bitk=k; 
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  this.write=q;
	  return this.inflate_flush(z,r);
	}
	break;
      case IB_LENS:
	while(k<(32)){
	  if(n!=0){
	    r=Z_OK;
	  }
	  else{
	    this.bitb=b; this.bitk=k; 
	    z.avail_in=n;
	    z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    this.write=q;
	    return this.inflate_flush(z,r);
	  };
	  n--;
	  b|=(z.next_in[p++]&0xff)<<k;
	  k+=8;
	}

	if ((((~b) >>> 16) & 0xffff) != (b & 0xffff)){
	  this.mode = BAD;
	  z.msg = "invalid stored block lengths";
	  r = Z_DATA_ERROR;

	  this.bitb=b; this.bitk=k; 
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  this.write=q;
	  return this.inflate_flush(z,r);
	}
	this.left = (b & 0xffff);
	b = k = 0;                       // dump bits
	this.mode = left!=0 ? IB_STORED : (this.last!=0 ? IB_DRY : IB_TYPE);
	break;
      case IB_STORED:
	if (n == 0){
	  this.bitb=b; this.bitk=k; 
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  write=q;
	  return this.inflate_flush(z,r);
	}

	if(m==0){
	  if(q==end&&read!=0){
	    q=0; m=(q<this.read ? this.read-q-1 : this.end-q);
	  }
	  if(m==0){
	    this.write=q; 
	    r=this.inflate_flush(z,r);
	    q=this.write; m = (q < this.read ? this.read-q-1 : this.end-q);
	    if(q==this.end && this.read != 0){
	      q=0; m = (q < this.read ? this.read-q-1 : this.end-q);
	    }
	    if(m==0){
	      this.bitb=b; this.bitk=k; 
	      z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	      this.write=q;
	      return this.inflate_flush(z,r);
	    }
	  }
	}
	r=Z_OK;

	t = this.left;
	if(t>n) t = n;
	if(t>m) t = m;
	arrayCopy(z.next_in, p, window, q, t);
	p += t;  n -= t;
	q += t;  m -= t;
	if ((this.left -= t) != 0)
	  break;
	this.mode = (this.last != 0 ? IB_DRY : IB_TYPE);
	break;
      case IB_TABLE:

	while(k<(14)){
	  if(n!=0){
	    r=Z_OK;
	  }
	  else{
	    this.bitb=b; this.bitk=k; 
	    z.avail_in=n;
	    z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    this.write=q;
	    return this.inflate_flush(z,r);
	  };
	  n--;
	  b|=(z.next_in[p++]&0xff)<<k;
	  k+=8;
	}

	this.table = t = (b & 0x3fff);
	if ((t & 0x1f) > 29 || ((t >> 5) & 0x1f) > 29)
	  {
	    this.mode = IB_BAD;
	    z.msg = "too many length or distance symbols";
	    r = Z_DATA_ERROR;

	    this.bitb=b; this.bitk=k; 
	    z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    this.write=q;
	    return this.inflate_flush(z,r);
	  }
	t = 258 + (t & 0x1f) + ((t >> 5) & 0x1f);
	if(this.blens==null || this.blens.length<t){
	    this.blens=new Int32Array(t);
	}
	else{
	  for(var i=0; i<t; i++){
              this.blens[i]=0;
          }
	}

	{b>>>=(14);k-=(14);}

	this.index = 0;
	mode = IB_BTREE;
      case IB_BTREE:
	while (this.index < 4 + (this.table >>> 10)){
	  while(k<(3)){
	    if(n!=0){
	      r=Z_OK;
	    }
	    else{
	      this.bitb=b; this.bitk=k; 
	      z.avail_in=n;
	      z.total_in+=p-z.next_in_index;z.next_in_index=p;
	      this.write=q;
	      return this.inflate_flush(z,r);
	    };
	    n--;
	    b|=(z.next_in[p++]&0xff)<<k;
	    k+=8;
	  }

	  this.blens[INFBLOCKS_BORDER[this.index++]] = b&7;

	  {b>>>=(3);k-=(3);}
	}

	while(this.index < 19){
	  this.blens[INFBLOCKS_BORDER[this.index++]] = 0;
	}

	this.bb[0] = 7;
	t = this.inftree.inflate_trees_bits(this.blens, this.bb, this.tb, this.hufts, z);
	if (t != Z_OK){
	  r = t;
	  if (r == Z_DATA_ERROR){
	    this.blens=null;
	    this.mode = IB_BAD;
	  }

	  this.bitb=b; this.bitk=k; 
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  write=q;
	  return this.inflate_flush(z,r);
	}

	this.index = 0;
	this.mode = IB_DTREE;
      case IB_DTREE:
	while (true){
	  t = this.table;
	  if(!(this.index < 258 + (t & 0x1f) + ((t >> 5) & 0x1f))){
	    break;
	  }

	  var h; //int[]
	  var i, j, c;

	  t = this.bb[0];

	  while(k<(t)){
	    if(n!=0){
	      r=Z_OK;
	    }
	    else{
	      this.bitb=b; this.bitk=k; 
	      z.avail_in=n;
	      z.total_in+=p-z.next_in_index;z.next_in_index=p;
	      this.write=q;
	      return this.inflate_flush(z,r);
	    };
	    n--;
	    b|=(z.next_in[p++]&0xff)<<k;
	    k+=8;
	  }

//	  if (this.tb[0]==-1){
//            dlog("null...");
//	  }

	  t=this.hufts[(this.tb[0]+(b & inflate_mask[t]))*3+1];
	  c=this.hufts[(this.tb[0]+(b & inflate_mask[t]))*3+2];

	  if (c < 16){
	    b>>>=(t);k-=(t);
	    this.blens[this.index++] = c;
	  }
	  else { // c == 16..18
	    i = c == 18 ? 7 : c - 14;
	    j = c == 18 ? 11 : 3;

	    while(k<(t+i)){
	      if(n!=0){
		r=Z_OK;
	      }
	      else{
		this.bitb=b; this.bitk=k; 
		z.avail_in=n;
		z.total_in+=p-z.next_in_index;z.next_in_index=p;
		this.write=q;
		return this.inflate_flush(z,r);
	      };
	      n--;
	      b|=(z.next_in[p++]&0xff)<<k;
	      k+=8;
	    }

	    b>>>=(t);k-=(t);

	    j += (b & inflate_mask[i]);

	    b>>>=(i);k-=(i);

	    i = this.index;
	    t = this.table;
	    if (i + j > 258 + (t & 0x1f) + ((t >> 5) & 0x1f) ||
		(c == 16 && i < 1)){
	      this.blens=null;
	      this.mode = IB_BAD;
	      z.msg = "invalid bit length repeat";
	      r = Z_DATA_ERROR;

	      this.bitb=b; this.bitk=k; 
	      z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	      this.write=q;
	      return this.inflate_flush(z,r);
	    }

	    c = c == 16 ? this.blens[i-1] : 0;
	    do{
	      this.blens[i++] = c;
	    }
	    while (--j!=0);
	    this.index = i;
	  }
	}

	this.tb[0]=-1;
	{
	    var bl=new Int32Array(1);
	    var bd=new Int32Array(1);
	    var tl=new Int32Array(1);
	    var td=new Int32Array(1);
	    bl[0] = 9;         // must be <= 9 for lookahead assumptions
	    bd[0] = 6;         // must be <= 9 for lookahead assumptions

	    t = this.table;
	    t = this.inftree.inflate_trees_dynamic(257 + (t & 0x1f), 
					      1 + ((t >> 5) & 0x1f),
					      this.blens, bl, bd, tl, td, this.hufts, z);

	    if (t != Z_OK){
	        if (t == Z_DATA_ERROR){
	            this.blens=null;
	            this.mode = BAD;
	        }
	        r = t;

	        this.bitb=b; this.bitk=k; 
	        z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	        this.write=q;
	        return this.inflate_flush(z,r);
	    }
	    this.codes.init(bl[0], bd[0], this.hufts, tl[0], this.hufts, td[0], z);
	}
	this.mode = IB_CODES;
      case IB_CODES:
	this.bitb=b; this.bitk=k;
	z.avail_in=n; z.total_in+=p-z.next_in_index;z.next_in_index=p;
	this.write=q;

	if ((r = this.codes.proc(this, z, r)) != Z_STREAM_END){
	  return this.inflate_flush(z, r);
	}
	r = Z_OK;
	this.codes.free(z);

	p=z.next_in_index; n=z.avail_in;b=this.bitb;k=this.bitk;
	q=this.write;m = (q < this.read ? this.read-q-1 : this.end-q);

	if (this.last==0){
	  this.mode = IB_TYPE;
	  break;
	}
	this.mode = IB_DRY;
      case IB_DRY:
	this.write=q; 
	r = this.inflate_flush(z, r); 
	q=this.write; m = (q < this.read ? this.read-q-1 : this.end-q);
	if (this.read != this.write){
	  this.bitb=b; this.bitk=k; 
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  this.write=q;
	  return this.inflate_flush(z, r);
	}
	mode = DONE;
      case IB_DONE:
	r = Z_STREAM_END;

	this.bitb=b; this.bitk=k; 
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	this.write=q;
	return this.inflate_flush(z, r);
      case IB_BAD:
	r = Z_DATA_ERROR;

	this.bitb=b; this.bitk=k; 
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	this.write=q;
	return this.inflate_flush(z, r);

      default:
	r = Z_STREAM_ERROR;

	this.bitb=b; this.bitk=k; 
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	this.write=q;
	return this.inflate_flush(z, r);
      }
    }
  }

InfBlocks.prototype.free = function(z){
    this.reset(z, null);
    this.window=null;
    this.hufts=null;
}

InfBlocks.prototype.set_dictionary = function(d, start, n){
    arrayCopy(d, start, window, 0, n);
    this.read = this.write = n;
}

  // Returns true if inflate is currently at the end of a block generated
  // by Z_SYNC_FLUSH or Z_FULL_FLUSH. 
InfBlocks.prototype.sync_point = function(){
    return this.mode == IB_LENS;
}

  // copy as much as possible from the sliding window to the output area
InfBlocks.prototype.inflate_flush = function(z, r){
    var n;
    var p;
    var q;

    // local copies of source and destination pointers
    p = z.next_out_index;
    q = this.read;

    // compute number of bytes to copy as far as end of window
    n = ((q <= this.write ? this.write : this.end) - q);
    if (n > z.avail_out) n = z.avail_out;
    if (n!=0 && r == Z_BUF_ERROR) r = Z_OK;

    // update counters
    z.avail_out -= n;
    z.total_out += n;

    // update check information
    if(this.checkfn != null)
      z.adler=this.check=z._adler.adler32(this.check, this.window, q, n);

    // copy as far as end of window
    arrayCopy(this.window, q, z.next_out, p, n);
    p += n;
    q += n;

    // see if more to copy at beginning of window
    if (q == this.end){
      // wrap pointers
      q = 0;
      if (this.write == this.end)
        this.write = 0;

      // compute bytes to copy
      n = this.write - q;
      if (n > z.avail_out) n = z.avail_out;
      if (n!=0 && r == Z_BUF_ERROR) r = Z_OK;

      // update counters
      z.avail_out -= n;
      z.total_out += n;

      // update check information
      if(this.checkfn != null)
	z.adler=this.check=z._adler.adler32(this.check, this.window, q, n);

      // copy
      arrayCopy(this.window, q, z.next_out, p, n);
      p += n;
      q += n;
    }

    // update pointers
    z.next_out_index = p;
    this.read = q;

    // done
    return r;
  }

//
// InfCodes.java
//

var IC_START=0;  // x: set up for LEN
var IC_LEN=1;    // i: get length/literal/eob next
var IC_LENEXT=2; // i: getting length extra (have base)
var IC_DIST=3;   // i: get distance next
var IC_DISTEXT=4;// i: getting distance extra
var IC_COPY=5;   // o: copying bytes in window, waiting for space
var IC_LIT=6;    // o: got literal, waiting for output space
var IC_WASH=7;   // o: got eob, possibly still output waiting
var IC_END=8;    // x: got eob and all data flushed
var IC_BADCODE=9;// x: got error

function InfCodes() {
}

InfCodes.prototype.init = function(bl, bd, tl, tl_index, td, td_index, z) {
    this.mode=IC_START;
    this.lbits=bl;
    this.dbits=bd;
    this.ltree=tl;
    this.ltree_index=tl_index;
    this.dtree = td;
    this.dtree_index=td_index;
    this.tree=null;
}

InfCodes.prototype.proc = function(s, z, r){ 
    var j;              // temporary storage
    var t;              // temporary pointer (int[])
    var tindex;         // temporary pointer
    var e;              // extra bits or operation
    var b=0;            // bit buffer
    var k=0;            // bits in bit buffer
    var p=0;            // input data pointer
    var n;              // bytes available there
    var q;              // output window write pointer
    var m;              // bytes to end of window or read pointer
    var f;              // pointer to copy strings from

    // copy input/output information to locals (UPDATE macro restores)
    p=z.next_in_index;n=z.avail_in;b=s.bitb;k=s.bitk;
    q=s.write;m=q<s.read?s.read-q-1:s.end-q;

    // process input and output based on current state
    while (true){
      switch (this.mode){
	// waiting for "i:"=input, "o:"=output, "x:"=nothing
      case IC_START:         // x: set up for LEN
	if (m >= 258 && n >= 10){

	  s.bitb=b;s.bitk=k;
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  s.write=q;
	  r = this.inflate_fast(this.lbits, this.dbits, 
			   this.ltree, this.ltree_index, 
			   this.dtree, this.dtree_index,
			   s, z);

	  p=z.next_in_index;n=z.avail_in;b=s.bitb;k=s.bitk;
	  q=s.write;m=q<s.read?s.read-q-1:s.end-q;

	  if (r != Z_OK){
	    this.mode = r == Z_STREAM_END ? IC_WASH : IC_BADCODE;
	    break;
	  }
	}
	this.need = this.lbits;
	this.tree = this.ltree;
	this.tree_index=this.ltree_index;

	this.mode = IC_LEN;
      case IC_LEN:           // i: get length/literal/eob next
	j = this.need;

	while(k<(j)){
	  if(n!=0)r=Z_OK;
	  else{

	    s.bitb=b;s.bitk=k;
	    z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    s.write=q;
	    return s.inflate_flush(z,r);
	  }
	  n--;
	  b|=(z.next_in[p++]&0xff)<<k;
	  k+=8;
	}

	tindex=(this.tree_index+(b&inflate_mask[j]))*3;

	b>>>=(this.tree[tindex+1]);
	k-=(this.tree[tindex+1]);

	e=this.tree[tindex];

	if(e == 0){               // literal
	  this.lit = this.tree[tindex+2];
	  this.mode = IC_LIT;
	  break;
	}
	if((e & 16)!=0 ){          // length
	  this.get = e & 15;
	  this.len = this.tree[tindex+2];
	  this.mode = IC_LENEXT;
	  break;
	}
	if ((e & 64) == 0){        // next table
	  this.need = e;
	  this.tree_index = tindex/3 + this.tree[tindex+2];
	  break;
	}
	if ((e & 32)!=0){               // end of block
	  this.mode = IC_WASH;
	  break;
	}
	this.mode = IC_BADCODE;        // invalid code
	z.msg = "invalid literal/length code";
	r = Z_DATA_ERROR;

	s.bitb=b;s.bitk=k;
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	s.write=q;
	return s.inflate_flush(z,r);

      case IC_LENEXT:        // i: getting length extra (have base)
	j = this.get;

	while(k<(j)){
	  if(n!=0)r=Z_OK;
	  else{

	    s.bitb=b;s.bitk=k;
	    z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    s.write=q;
	    return s.inflate_flush(z,r);
	  }
	  n--; b|=(z.next_in[p++]&0xff)<<k;
	  k+=8;
	}

	this.len += (b & inflate_mask[j]);

	b>>=j;
	k-=j;

	this.need = this.dbits;
	this.tree = this.dtree;
	this.tree_index = this.dtree_index;
	this.mode = IC_DIST;
      case IC_DIST:          // i: get distance next
	j = this.need;

	while(k<(j)){
	  if(n!=0)r=Z_OK;
	  else{

	    s.bitb=b;s.bitk=k;
	    z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    s.write=q;
	    return s.inflate_flush(z,r);
	  }
	  n--; b|=(z.next_in[p++]&0xff)<<k;
	  k+=8;
	}

	tindex=(this.tree_index+(b & inflate_mask[j]))*3;

	b>>=this.tree[tindex+1];
	k-=this.tree[tindex+1];

	e = (this.tree[tindex]);
	if((e & 16)!=0){               // distance
	  this.get = e & 15;
	  this.dist = this.tree[tindex+2];
	  this.mode = IC_DISTEXT;
	  break;
	}
	if ((e & 64) == 0){        // next table
	  this.need = e;
	  this.tree_index = tindex/3 + this.tree[tindex+2];
	  break;
	}
	this.mode = IC_BADCODE;        // invalid code
	z.msg = "invalid distance code";
	r = Z_DATA_ERROR;

	s.bitb=b;s.bitk=k;
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	s.write=q;
	return s.inflate_flush(z,r);

      case IC_DISTEXT:       // i: getting distance extra
	j = this.get;

	while(k<(j)){
	  if(n!=0)r=Z_OK;
	  else{

	    s.bitb=b;s.bitk=k;
	    z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    s.write=q;
	    return s.inflate_flush(z,r);
	  }
	  n--; b|=(z.next_in[p++]&0xff)<<k;
	  k+=8;
	}

	this.dist += (b & inflate_mask[j]);

	b>>=j;
	k-=j;

	this.mode = IC_COPY;
      case IC_COPY:          // o: copying bytes in window, waiting for space
        f = q - this.dist;
        while(f < 0){     // modulo window size-"while" instead
          f += s.end;     // of "if" handles invalid distances
	}
	while (this.len!=0){

	  if(m==0){
	    if(q==s.end&&s.read!=0){q=0;m=q<s.read?s.read-q-1:s.end-q;}
	    if(m==0){
	      s.write=q; r=s.inflate_flush(z,r);
	      q=s.write;m=q<s.read?s.read-q-1:s.end-q;

	      if(q==s.end&&s.read!=0){q=0;m=q<s.read?s.read-q-1:s.end-q;}

	      if(m==0){
		s.bitb=b;s.bitk=k;
		z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
		s.write=q;
		return s.inflate_flush(z,r);
	      }  
	    }
	  }

	  s.window[q++]=s.window[f++]; m--;

	  if (f == s.end)
            f = 0;
	  this.len--;
	}
	this.mode = IC_START;
	break;
      case IC_LIT:           // o: got literal, waiting for output space
	if(m==0){
	  if(q==s.end&&s.read!=0){q=0;m=q<s.read?s.read-q-1:s.end-q;}
	  if(m==0){
	    s.write=q; r=s.inflate_flush(z,r);
	    q=s.write;m=q<s.read?s.read-q-1:s.end-q;

	    if(q==s.end&&s.read!=0){q=0;m=q<s.read?s.read-q-1:s.end-q;}
	    if(m==0){
	      s.bitb=b;s.bitk=k;
	      z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	      s.write=q;
	      return s.inflate_flush(z,r);
	    }
	  }
	}
	r=Z_OK;

	s.window[q++]=this.lit; m--;

	this.mode = IC_START;
	break;
      case IC_WASH:           // o: got eob, possibly more output
	if (k > 7){        // return unused byte, if any
	  k -= 8;
	  n++;
	  p--;             // can always return one
	}

	s.write=q; r=s.inflate_flush(z,r);
	q=s.write;m=q<s.read?s.read-q-1:s.end-q;

	if (s.read != s.write){
	  s.bitb=b;s.bitk=k;
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  s.write=q;
	  return s.inflate_flush(z,r);
	}
	this.mode = IC_END;
      case IC_END:
	r = Z_STREAM_END;
	s.bitb=b;s.bitk=k;
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	s.write=q;
	return s.inflate_flush(z,r);

      case IC_BADCODE:       // x: got error

	r = Z_DATA_ERROR;

	s.bitb=b;s.bitk=k;
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	s.write=q;
	return s.inflate_flush(z,r);

      default:
	r = Z_STREAM_ERROR;

	s.bitb=b;s.bitk=k;
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	s.write=q;
	return s.inflate_flush(z,r);
      }
    }
  }

InfCodes.prototype.free = function(z){
    //  ZFREE(z, c);
}

  // Called with number of bytes left to write in window at least 258
  // (the maximum string length) and number of input bytes available
  // at least ten.  The ten bytes are six bytes for the longest length/
  // distance pair plus four bytes for overloading the bit buffer.

InfCodes.prototype.inflate_fast = function(bl, bd, tl, tl_index, td, td_index, s, z) {
    var t;                // temporary pointer
    var   tp;             // temporary pointer (int[])
    var tp_index;         // temporary pointer
    var e;                // extra bits or operation
    var b;                // bit buffer
    var k;                // bits in bit buffer
    var p;                // input data pointer
    var n;                // bytes available there
    var q;                // output window write pointer
    var m;                // bytes to end of window or read pointer
    var ml;               // mask for literal/length tree
    var md;               // mask for distance tree
    var c;                // bytes to copy
    var d;                // distance back to copy from
    var r;                // copy source pointer

    var tp_index_t_3;     // (tp_index+t)*3

    // load input, output, bit values
    p=z.next_in_index;n=z.avail_in;b=s.bitb;k=s.bitk;
    q=s.write;m=q<s.read?s.read-q-1:s.end-q;

    // initialize masks
    ml = inflate_mask[bl];
    md = inflate_mask[bd];

    // do until not enough input or output space for fast loop
    do {                          // assume called with m >= 258 && n >= 10
      // get literal/length code
      while(k<(20)){              // max bits for literal/length code
	n--;
	b|=(z.next_in[p++]&0xff)<<k;k+=8;
      }

      t= b&ml;
      tp=tl; 
      tp_index=tl_index;
      tp_index_t_3=(tp_index+t)*3;
      if ((e = tp[tp_index_t_3]) == 0){
	b>>=(tp[tp_index_t_3+1]); k-=(tp[tp_index_t_3+1]);

	s.window[q++] = tp[tp_index_t_3+2];
	m--;
	continue;
      }
      do {

	b>>=(tp[tp_index_t_3+1]); k-=(tp[tp_index_t_3+1]);

	if((e&16)!=0){
	  e &= 15;
	  c = tp[tp_index_t_3+2] + (b & inflate_mask[e]);

	  b>>=e; k-=e;

	  // decode distance base of block to copy
	  while(k<(15)){           // max bits for distance code
	    n--;
	    b|=(z.next_in[p++]&0xff)<<k;k+=8;
	  }

	  t= b&md;
	  tp=td;
	  tp_index=td_index;
          tp_index_t_3=(tp_index+t)*3;
	  e = tp[tp_index_t_3];

	  do {

	    b>>=(tp[tp_index_t_3+1]); k-=(tp[tp_index_t_3+1]);

	    if((e&16)!=0){
	      // get extra bits to add to distance base
	      e &= 15;
	      while(k<(e)){         // get extra bits (up to 13)
		n--;
		b|=(z.next_in[p++]&0xff)<<k;k+=8;
	      }

	      d = tp[tp_index_t_3+2] + (b&inflate_mask[e]);

	      b>>=(e); k-=(e);

	      // do the copy
	      m -= c;
	      if (q >= d){                // offset before dest
		//  just copy
		r=q-d;
		if(q-r>0 && 2>(q-r)){           
		  s.window[q++]=s.window[r++]; // minimum count is three,
		  s.window[q++]=s.window[r++]; // so unroll loop a little
		  c-=2;
		}
		else{
		  s.window[q++]=s.window[r++]; // minimum count is three,
		  s.window[q++]=s.window[r++]; // so unroll loop a little
		  c-=2;
		}
	      }
	      else{                  // else offset after destination
                r=q-d;
                do{
                  r+=s.end;          // force pointer in window
                }while(r<0);         // covers invalid distances
		e=s.end-r;
		if(c>e){             // if source crosses,
		  c-=e;              // wrapped copy
		  if(q-r>0 && e>(q-r)){           
		    do{s.window[q++] = s.window[r++];}
		    while(--e!=0);
		  }
		  else{
		    arrayCopy(s.window, r, s.window, q, e);
		    q+=e; r+=e; e=0;
		  }
		  r = 0;                  // copy rest from start of window
		}

	      }

	      // copy all or what's left
              do{s.window[q++] = s.window[r++];}
		while(--c!=0);
	      break;
	    }
	    else if((e&64)==0){
	      t+=tp[tp_index_t_3+2];
	      t+=(b&inflate_mask[e]);
	      tp_index_t_3=(tp_index+t)*3;
	      e=tp[tp_index_t_3];
	    }
	    else{
	      z.msg = "invalid distance code";

	      c=z.avail_in-n;c=(k>>3)<c?k>>3:c;n+=c;p-=c;k-=c<<3;

	      s.bitb=b;s.bitk=k;
	      z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	      s.write=q;

	      return Z_DATA_ERROR;
	    }
	  }
	  while(true);
	  break;
	}

	if((e&64)==0){
	  t+=tp[tp_index_t_3+2];
	  t+=(b&inflate_mask[e]);
	  tp_index_t_3=(tp_index+t)*3;
	  if((e=tp[tp_index_t_3])==0){

	    b>>=(tp[tp_index_t_3+1]); k-=(tp[tp_index_t_3+1]);

	    s.window[q++]=tp[tp_index_t_3+2];
	    m--;
	    break;
	  }
	}
	else if((e&32)!=0){

	  c=z.avail_in-n;c=(k>>3)<c?k>>3:c;n+=c;p-=c;k-=c<<3;
 
	  s.bitb=b;s.bitk=k;
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  s.write=q;

	  return Z_STREAM_END;
	}
	else{
	  z.msg="invalid literal/length code";

	  c=z.avail_in-n;c=(k>>3)<c?k>>3:c;n+=c;p-=c;k-=c<<3;

	  s.bitb=b;s.bitk=k;
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  s.write=q;

	  return Z_DATA_ERROR;
	}
      } 
      while(true);
    } 
    while(m>=258 && n>= 10);

    // not enough input or output--restore pointers and return
    c=z.avail_in-n;c=(k>>3)<c?k>>3:c;n+=c;p-=c;k-=c<<3;

    s.bitb=b;s.bitk=k;
    z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
    s.write=q;

    return Z_OK;
}

//
// InfTree.java
//

function InfTree() {
}

InfTree.prototype.huft_build = function(b, bindex, n, s, d, e, t, m, hp, hn, v) {

    // Given a list of code lengths and a maximum table size, make a set of
    // tables to decode that set of codes.  Return Z_OK on success, Z_BUF_ERROR
    // if the given code set is incomplete (the tables are still built in this
    // case), Z_DATA_ERROR if the input is invalid (an over-subscribed set of
    // lengths), or Z_MEM_ERROR if not enough memory.

    var a;                       // counter for codes of length k
    var f;                       // i repeats in table every f entries
    var g;                       // maximum code length
    var h;                       // table level
    var i;                       // counter, current code
    var j;                       // counter
    var k;                       // number of bits in current code
    var l;                       // bits per table (returned in m)
    var mask;                    // (1 << w) - 1, to avoid cc -O bug on HP
    var p;                       // pointer into c[], b[], or v[]
    var q;                       // points to current table
    var w;                       // bits before this table == (l * h)
    var xp;                      // pointer into x
    var y;                       // number of dummy codes added
    var z;                       // number of entries in current table

    // Generate counts for each bit length

    p = 0; i = n;
    do {
      this.c[b[bindex+p]]++; p++; i--;   // assume all entries <= BMAX
    }while(i!=0);

    if(this.c[0] == n){                // null input--all zero length codes
      t[0] = -1;
      m[0] = 0;
      return Z_OK;
    }

    // Find minimum and maximum length, bound *m by those
    l = m[0];
    for (j = 1; j <= BMAX; j++)
      if(this.c[j]!=0) break;
    k = j;                        // minimum code length
    if(l < j){
      l = j;
    }
    for (i = BMAX; i!=0; i--){
      if(this.c[i]!=0) break;
    }
    g = i;                        // maximum code length
    if(l > i){
      l = i;
    }
    m[0] = l;

    // Adjust last length count to fill out codes, if needed
    for (y = 1 << j; j < i; j++, y <<= 1){
      if ((y -= this.c[j]) < 0){
        return Z_DATA_ERROR;
      }
    }
    if ((y -= this.c[i]) < 0){
      return Z_DATA_ERROR;
    }
    this.c[i] += y;

    // Generate starting offsets into the value table for each length
    this.x[1] = j = 0;
    p = 1;  xp = 2;
    while (--i!=0) {                 // note that i == g from above
      this.x[xp] = (j += this.c[p]);
      xp++;
      p++;
    }

    // Make a table of values in order of bit lengths
    i = 0; p = 0;
    do {
      if ((j = b[bindex+p]) != 0){
        this.v[this.x[j]++] = i;
      }
      p++;
    }
    while (++i < n);
    n = this.x[g];                     // set n to length of v

    // Generate the Huffman codes and for each, make the table entries
    this.x[0] = i = 0;                 // first Huffman code is zero
    p = 0;                        // grab values in bit order
    h = -1;                       // no tables yet--level -1
    w = -l;                       // bits decoded == (l * h)
    this.u[0] = 0;                     // just to keep compilers happy
    q = 0;                        // ditto
    z = 0;                        // ditto

    // go through the bit lengths (k already is bits in shortest code)
    for (; k <= g; k++){
      a = this.c[k];
      while (a--!=0){
	// here i is the Huffman code of length k bits for value *p
	// make tables up to required level
        while (k > w + l){
          h++;
          w += l;                 // previous table always l bits
	  // compute minimum size table less than or equal to l bits
          z = g - w;
          z = (z > l) ? l : z;        // table size upper limit
          if((f=1<<(j=k-w))>a+1){     // try a k-w bit table
                                      // too few codes for k-w bit table
            f -= a + 1;               // deduct codes from patterns left
            xp = k;
            if(j < z){
              while (++j < z){        // try smaller tables up to z bits
                if((f <<= 1) <= this.c[++xp])
                  break;              // enough codes to use up j bits
                f -= this.c[xp];           // else deduct codes from patterns
              }
	    }
          }
          z = 1 << j;                 // table entries for j-bit table

	  // allocate new table
          if (this.hn[0] + z > MANY){       // (note: doesn't matter for fixed)
            return Z_DATA_ERROR;       // overflow of MANY
          }
          this.u[h] = q = /*hp+*/ this.hn[0];   // DEBUG
          this.hn[0] += z;
 
	  // connect to last table, if there is one
	  if(h!=0){
            this.x[h]=i;           // save pattern for backing up
            this.r[0]=j;     // bits in this table
            this.r[1]=l;     // bits to dump before this table
            j=i>>>(w - l);
            this.r[2] = (q - this.u[h-1] - j);               // offset to this table
            arrayCopy(this.r, 0, hp, (this.u[h-1]+j)*3, 3); // connect to last table
          }
          else{
            t[0] = q;               // first table is returned result
	  }
        }

	// set up table entry in r
        this.r[1] = (k - w);
        if (p >= n){
          this.r[0] = 128 + 64;      // out of values--invalid code
	}
        else if (v[p] < s){
          this.r[0] = (this.v[p] < 256 ? 0 : 32 + 64);  // 256 is end-of-block
          this.r[2] = this.v[p++];          // simple code is just the value
        }
        else{
          this.r[0]=(e[this.v[p]-s]+16+64); // non-simple--look up in lists
          this.r[2]=d[this.v[p++] - s];
        }

        // fill code-like entries with r
        f=1<<(k-w);
        for (j=i>>>w;j<z;j+=f){
          arrayCopy(this.r, 0, hp, (q+j)*3, 3);
	}

	// backwards increment the k-bit code i
        for (j = 1 << (k - 1); (i & j)!=0; j >>>= 1){
          i ^= j;
	}
        i ^= j;

	// backup over finished tables
        mask = (1 << w) - 1;      // needed on HP, cc -O bug
        while ((i & mask) != this.x[h]){
          h--;                    // don't need to update q
          w -= l;
          mask = (1 << w) - 1;
        }
      }
    }
    // Return Z_BUF_ERROR if we were given an incomplete table
    return y != 0 && g != 1 ? Z_BUF_ERROR : Z_OK;
}

InfTree.prototype.inflate_trees_bits = function(c, bb, tb, hp, z) {
    var result;
    this.initWorkArea(19);
    this.hn[0]=0;
    result = this.huft_build(c, 0, 19, 19, null, null, tb, bb, hp, this.hn, this.v);

    if(result == Z_DATA_ERROR){
      z.msg = "oversubscribed dynamic bit lengths tree";
    }
    else if(result == Z_BUF_ERROR || bb[0] == 0){
      z.msg = "incomplete dynamic bit lengths tree";
      result = Z_DATA_ERROR;
    }
    return result;
}

InfTree.prototype.inflate_trees_dynamic = function(nl, nd, c, bl, bd, tl, td, hp, z) {
    var result;

    // build literal/length tree
    this.initWorkArea(288);
    this.hn[0]=0;
    result = this.huft_build(c, 0, nl, 257, cplens, cplext, tl, bl, hp, this.hn, this.v);
    if (result != Z_OK || bl[0] == 0){
      if(result == Z_DATA_ERROR){
        z.msg = "oversubscribed literal/length tree";
      }
      else if (result != Z_MEM_ERROR){
        z.msg = "incomplete literal/length tree";
        result = Z_DATA_ERROR;
      }
      return result;
    }

    // build distance tree
    this.initWorkArea(288);
    result = this.huft_build(c, nl, nd, 0, cpdist, cpdext, td, bd, hp, this.hn, this.v);

    if (result != Z_OK || (bd[0] == 0 && nl > 257)){
      if (result == Z_DATA_ERROR){
        z.msg = "oversubscribed distance tree";
      }
      else if (result == Z_BUF_ERROR) {
        z.msg = "incomplete distance tree";
        result = Z_DATA_ERROR;
      }
      else if (result != Z_MEM_ERROR){
        z.msg = "empty distance tree with lengths";
        result = Z_DATA_ERROR;
      }
      return result;
    }

    return Z_OK;
}
/*
  static int inflate_trees_fixed(int[] bl,  //literal desired/actual bit depth
                                 int[] bd,  //distance desired/actual bit depth
                                 int[][] tl,//literal/length tree result
                                 int[][] td,//distance tree result 
                                 ZStream z  //for memory allocation
				 ){

*/

function inflate_trees_fixed(bl, bd, tl, td, z) {
    bl[0]=fixed_bl;
    bd[0]=fixed_bd;
    tl[0]=fixed_tl;
    td[0]=fixed_td;
    return Z_OK;
}

InfTree.prototype.initWorkArea = function(vsize){
    if(this.hn==null){
        this.hn=new Int32Array(1);
        this.v=new Int32Array(vsize);
        this.c=new Int32Array(BMAX+1);
        this.r=new Int32Array(3);
        this.u=new Int32Array(BMAX);
        this.x=new Int32Array(BMAX+1);
    }
    if(this.v.length<vsize){ 
        this.v=new Int32Array(vsize); 
    }
    for(var i=0; i<vsize; i++){this.v[i]=0;}
    for(var i=0; i<BMAX+1; i++){this.c[i]=0;}
    for(var i=0; i<3; i++){this.r[i]=0;}
//  for(int i=0; i<BMAX; i++){u[i]=0;}
    arrayCopy(this.c, 0, this.u, 0, BMAX);
//  for(int i=0; i<BMAX+1; i++){x[i]=0;}
    arrayCopy(this.c, 0, this.x, 0, BMAX+1);
}

var testArray = new Uint8Array(1);
var hasSubarray = (typeof testArray.subarray === 'function');
var hasSlice = false; /* (typeof testArray.slice === 'function'); */ // Chrome slice performance is so dire that we're currently not using it...

function arrayCopy(src, srcOffset, dest, destOffset, count) {
    if (count == 0) {
        return;
    } 
    if (!src) {
        throw "Undef src";
    } else if (!dest) {
        throw "Undef dest";
    }

    if (srcOffset == 0 && count == src.length) {
        arrayCopy_fast(src, dest, destOffset);
    } else if (hasSubarray) {
        arrayCopy_fast(src.subarray(srcOffset, srcOffset + count), dest, destOffset); 
    } else if (src.BYTES_PER_ELEMENT == 1 && count > 100) {
        arrayCopy_fast(new Uint8Array(src.buffer, src.byteOffset + srcOffset, count), dest, destOffset);
    } else { 
        arrayCopy_slow(src, srcOffset, dest, destOffset, count);
    }

}

function arrayCopy_slow(src, srcOffset, dest, destOffset, count) {

    // dlog('_slow call: srcOffset=' + srcOffset + '; destOffset=' + destOffset + '; count=' + count);

     for (var i = 0; i < count; ++i) {
        dest[destOffset + i] = src[srcOffset + i];
    }
}

function arrayCopy_fast(src, dest, destOffset) {
    dest.set(src, destOffset);
}


  // largest prime smaller than 65536
var ADLER_BASE=65521; 
  // NMAX is the largest n such that 255n(n+1)/2 + (n+1)(BASE-1) <= 2^32-1
var ADLER_NMAX=5552;

function adler32(adler, /* byte[] */ buf,  index, len){
    if(buf == null){ return 1; }

    var s1=adler&0xffff;
    var s2=(adler>>16)&0xffff;
    var k;

    while(len > 0) {
      k=len<ADLER_NMAX?len:ADLER_NMAX;
      len-=k;
      while(k>=16){
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        k-=16;
      }
      if(k!=0){
        do{
          s1+=buf[index++]&0xff; s2+=s1;
        }
        while(--k!=0);
      }
      s1%=ADLER_BASE;
      s2%=ADLER_BASE;
    }
    return (s2<<16)|s1;
}



function jszlib_inflate_buffer(buffer, start, length, afterUncOffset) {
    if (!start) {
        buffer = new Uint8Array(buffer);
    } else {
        buffer = new Uint8Array(buffer, start, length);
    }

    var z = new ZStream();
    z.inflateInit(DEF_WBITS, true);
    z.next_in = buffer;
    z.next_in_index = 0;
    z.avail_in = buffer.length;

    var oBlockList = [];
    var totalSize = 0;
    while (true) {
        var obuf = new Uint8Array(32000);
        z.next_out = obuf;
        z.next_out_index = 0;
        z.avail_out = obuf.length;
        var status = z.inflate(Z_NO_FLUSH);
        if (status != Z_OK && status != Z_STREAM_END) {
            throw z.msg;
        }
        if (z.avail_out != 0) {
            var newob = new Uint8Array(obuf.length - z.avail_out);
            arrayCopy(obuf, 0, newob, 0, (obuf.length - z.avail_out));
            obuf = newob;
        }
        oBlockList.push(obuf);
        totalSize += obuf.length;
        if (status == Z_STREAM_END) {
            break;
        }
    }

    if (afterUncOffset) {
        afterUncOffset[0] = (start || 0) + z.next_in_index;
    }

    if (oBlockList.length == 1) {
        return oBlockList[0].buffer;
    } else {
        var out = new Uint8Array(totalSize);
        var cursor = 0;
        for (var i = 0; i < oBlockList.length; ++i) {
            var b = oBlockList[i];
            arrayCopy(b, 0, out, cursor, b.length);
            cursor += b.length;
        }
        return out.buffer;
    }
}
/** @license zlib.js 2012 - imaya [ https://github.com/imaya/zlib.js ] The MIT License */(function() {'use strict';function q(b){throw b;}var t=void 0,u=!0,aa=this;function A(b,a){var c=b.split("."),d=aa;!(c[0]in d)&&d.execScript&&d.execScript("var "+c[0]);for(var f;c.length&&(f=c.shift());)!c.length&&a!==t?d[f]=a:d=d[f]?d[f]:d[f]={}};var B="undefined"!==typeof Uint8Array&&"undefined"!==typeof Uint16Array&&"undefined"!==typeof Uint32Array;function F(b,a){this.index="number"===typeof a?a:0;this.m=0;this.buffer=b instanceof(B?Uint8Array:Array)?b:new (B?Uint8Array:Array)(32768);2*this.buffer.length<=this.index&&q(Error("invalid index"));this.buffer.length<=this.index&&this.f()}F.prototype.f=function(){var b=this.buffer,a,c=b.length,d=new (B?Uint8Array:Array)(c<<1);if(B)d.set(b);else for(a=0;a<c;++a)d[a]=b[a];return this.buffer=d};
F.prototype.d=function(b,a,c){var d=this.buffer,f=this.index,e=this.m,g=d[f],k;c&&1<a&&(b=8<a?(H[b&255]<<24|H[b>>>8&255]<<16|H[b>>>16&255]<<8|H[b>>>24&255])>>32-a:H[b]>>8-a);if(8>a+e)g=g<<a|b,e+=a;else for(k=0;k<a;++k)g=g<<1|b>>a-k-1&1,8===++e&&(e=0,d[f++]=H[g],g=0,f===d.length&&(d=this.f()));d[f]=g;this.buffer=d;this.m=e;this.index=f};F.prototype.finish=function(){var b=this.buffer,a=this.index,c;0<this.m&&(b[a]<<=8-this.m,b[a]=H[b[a]],a++);B?c=b.subarray(0,a):(b.length=a,c=b);return c};
var ba=new (B?Uint8Array:Array)(256),ca;for(ca=0;256>ca;++ca){for(var K=ca,da=K,ea=7,K=K>>>1;K;K>>>=1)da<<=1,da|=K&1,--ea;ba[ca]=(da<<ea&255)>>>0}var H=ba;function ja(b,a,c){var d,f="number"===typeof a?a:a=0,e="number"===typeof c?c:b.length;d=-1;for(f=e&7;f--;++a)d=d>>>8^O[(d^b[a])&255];for(f=e>>3;f--;a+=8)d=d>>>8^O[(d^b[a])&255],d=d>>>8^O[(d^b[a+1])&255],d=d>>>8^O[(d^b[a+2])&255],d=d>>>8^O[(d^b[a+3])&255],d=d>>>8^O[(d^b[a+4])&255],d=d>>>8^O[(d^b[a+5])&255],d=d>>>8^O[(d^b[a+6])&255],d=d>>>8^O[(d^b[a+7])&255];return(d^4294967295)>>>0}
var ka=[0,1996959894,3993919788,2567524794,124634137,1886057615,3915621685,2657392035,249268274,2044508324,3772115230,2547177864,162941995,2125561021,3887607047,2428444049,498536548,1789927666,4089016648,2227061214,450548861,1843258603,4107580753,2211677639,325883990,1684777152,4251122042,2321926636,335633487,1661365465,4195302755,2366115317,997073096,1281953886,3579855332,2724688242,1006888145,1258607687,3524101629,2768942443,901097722,1119000684,3686517206,2898065728,853044451,1172266101,3705015759,
2882616665,651767980,1373503546,3369554304,3218104598,565507253,1454621731,3485111705,3099436303,671266974,1594198024,3322730930,2970347812,795835527,1483230225,3244367275,3060149565,1994146192,31158534,2563907772,4023717930,1907459465,112637215,2680153253,3904427059,2013776290,251722036,2517215374,3775830040,2137656763,141376813,2439277719,3865271297,1802195444,476864866,2238001368,4066508878,1812370925,453092731,2181625025,4111451223,1706088902,314042704,2344532202,4240017532,1658658271,366619977,
2362670323,4224994405,1303535960,984961486,2747007092,3569037538,1256170817,1037604311,2765210733,3554079995,1131014506,879679996,2909243462,3663771856,1141124467,855842277,2852801631,3708648649,1342533948,654459306,3188396048,3373015174,1466479909,544179635,3110523913,3462522015,1591671054,702138776,2966460450,3352799412,1504918807,783551873,3082640443,3233442989,3988292384,2596254646,62317068,1957810842,3939845945,2647816111,81470997,1943803523,3814918930,2489596804,225274430,2053790376,3826175755,
2466906013,167816743,2097651377,4027552580,2265490386,503444072,1762050814,4150417245,2154129355,426522225,1852507879,4275313526,2312317920,282753626,1742555852,4189708143,2394877945,397917763,1622183637,3604390888,2714866558,953729732,1340076626,3518719985,2797360999,1068828381,1219638859,3624741850,2936675148,906185462,1090812512,3747672003,2825379669,829329135,1181335161,3412177804,3160834842,628085408,1382605366,3423369109,3138078467,570562233,1426400815,3317316542,2998733608,733239954,1555261956,
3268935591,3050360625,752459403,1541320221,2607071920,3965973030,1969922972,40735498,2617837225,3943577151,1913087877,83908371,2512341634,3803740692,2075208622,213261112,2463272603,3855990285,2094854071,198958881,2262029012,4057260610,1759359992,534414190,2176718541,4139329115,1873836001,414664567,2282248934,4279200368,1711684554,285281116,2405801727,4167216745,1634467795,376229701,2685067896,3608007406,1308918612,956543938,2808555105,3495958263,1231636301,1047427035,2932959818,3654703836,1088359270,
936918E3,2847714899,3736837829,1202900863,817233897,3183342108,3401237130,1404277552,615818150,3134207493,3453421203,1423857449,601450431,3009837614,3294710456,1567103746,711928724,3020668471,3272380065,1510334235,755167117],O=B?new Uint32Array(ka):ka;function P(){}P.prototype.getName=function(){return this.name};P.prototype.getData=function(){return this.data};P.prototype.X=function(){return this.Y};A("Zlib.GunzipMember",P);A("Zlib.GunzipMember.prototype.getName",P.prototype.getName);A("Zlib.GunzipMember.prototype.getData",P.prototype.getData);A("Zlib.GunzipMember.prototype.getMtime",P.prototype.X);function la(b){this.buffer=new (B?Uint16Array:Array)(2*b);this.length=0}la.prototype.getParent=function(b){return 2*((b-2)/4|0)};la.prototype.push=function(b,a){var c,d,f=this.buffer,e;c=this.length;f[this.length++]=a;for(f[this.length++]=b;0<c;)if(d=this.getParent(c),f[c]>f[d])e=f[c],f[c]=f[d],f[d]=e,e=f[c+1],f[c+1]=f[d+1],f[d+1]=e,c=d;else break;return this.length};
la.prototype.pop=function(){var b,a,c=this.buffer,d,f,e;a=c[0];b=c[1];this.length-=2;c[0]=c[this.length];c[1]=c[this.length+1];for(e=0;;){f=2*e+2;if(f>=this.length)break;f+2<this.length&&c[f+2]>c[f]&&(f+=2);if(c[f]>c[e])d=c[e],c[e]=c[f],c[f]=d,d=c[e+1],c[e+1]=c[f+1],c[f+1]=d;else break;e=f}return{index:b,value:a,length:this.length}};function ma(b){var a=b.length,c=0,d=Number.POSITIVE_INFINITY,f,e,g,k,h,l,s,n,m;for(n=0;n<a;++n)b[n]>c&&(c=b[n]),b[n]<d&&(d=b[n]);f=1<<c;e=new (B?Uint32Array:Array)(f);g=1;k=0;for(h=2;g<=c;){for(n=0;n<a;++n)if(b[n]===g){l=0;s=k;for(m=0;m<g;++m)l=l<<1|s&1,s>>=1;for(m=l;m<f;m+=h)e[m]=g<<16|n;++k}++g;k<<=1;h<<=1}return[e,c,d]};function na(b,a){this.k=qa;this.I=0;this.input=B&&b instanceof Array?new Uint8Array(b):b;this.b=0;a&&(a.lazy&&(this.I=a.lazy),"number"===typeof a.compressionType&&(this.k=a.compressionType),a.outputBuffer&&(this.a=B&&a.outputBuffer instanceof Array?new Uint8Array(a.outputBuffer):a.outputBuffer),"number"===typeof a.outputIndex&&(this.b=a.outputIndex));this.a||(this.a=new (B?Uint8Array:Array)(32768))}var qa=2,ra={NONE:0,v:1,o:qa,aa:3},sa=[],S;
for(S=0;288>S;S++)switch(u){case 143>=S:sa.push([S+48,8]);break;case 255>=S:sa.push([S-144+400,9]);break;case 279>=S:sa.push([S-256+0,7]);break;case 287>=S:sa.push([S-280+192,8]);break;default:q("invalid literal: "+S)}
na.prototype.g=function(){var b,a,c,d,f=this.input;switch(this.k){case 0:c=0;for(d=f.length;c<d;){a=B?f.subarray(c,c+65535):f.slice(c,c+65535);c+=a.length;var e=a,g=c===d,k=t,h=t,l=t,s=t,n=t,m=this.a,p=this.b;if(B){for(m=new Uint8Array(this.a.buffer);m.length<=p+e.length+5;)m=new Uint8Array(m.length<<1);m.set(this.a)}k=g?1:0;m[p++]=k|0;h=e.length;l=~h+65536&65535;m[p++]=h&255;m[p++]=h>>>8&255;m[p++]=l&255;m[p++]=l>>>8&255;if(B)m.set(e,p),p+=e.length,m=m.subarray(0,p);else{s=0;for(n=e.length;s<n;++s)m[p++]=
e[s];m.length=p}this.b=p;this.a=m}break;case 1:var r=new F(B?new Uint8Array(this.a.buffer):this.a,this.b);r.d(1,1,u);r.d(1,2,u);var v=ta(this,f),x,Q,y;x=0;for(Q=v.length;x<Q;x++)if(y=v[x],F.prototype.d.apply(r,sa[y]),256<y)r.d(v[++x],v[++x],u),r.d(v[++x],5),r.d(v[++x],v[++x],u);else if(256===y)break;this.a=r.finish();this.b=this.a.length;break;case qa:var E=new F(B?new Uint8Array(this.a.buffer):this.a,this.b),Ja,R,X,Y,Z,pb=[16,17,18,0,8,7,9,6,10,5,11,4,12,3,13,2,14,1,15],fa,Ka,ga,La,oa,wa=Array(19),
Ma,$,pa,C,Na;Ja=qa;E.d(1,1,u);E.d(Ja,2,u);R=ta(this,f);fa=ua(this.V,15);Ka=va(fa);ga=ua(this.U,7);La=va(ga);for(X=286;257<X&&0===fa[X-1];X--);for(Y=30;1<Y&&0===ga[Y-1];Y--);var Oa=X,Pa=Y,J=new (B?Uint32Array:Array)(Oa+Pa),w,L,z,ha,I=new (B?Uint32Array:Array)(316),G,D,M=new (B?Uint8Array:Array)(19);for(w=L=0;w<Oa;w++)J[L++]=fa[w];for(w=0;w<Pa;w++)J[L++]=ga[w];if(!B){w=0;for(ha=M.length;w<ha;++w)M[w]=0}w=G=0;for(ha=J.length;w<ha;w+=L){for(L=1;w+L<ha&&J[w+L]===J[w];++L);z=L;if(0===J[w])if(3>z)for(;0<
z--;)I[G++]=0,M[0]++;else for(;0<z;)D=138>z?z:138,D>z-3&&D<z&&(D=z-3),10>=D?(I[G++]=17,I[G++]=D-3,M[17]++):(I[G++]=18,I[G++]=D-11,M[18]++),z-=D;else if(I[G++]=J[w],M[J[w]]++,z--,3>z)for(;0<z--;)I[G++]=J[w],M[J[w]]++;else for(;0<z;)D=6>z?z:6,D>z-3&&D<z&&(D=z-3),I[G++]=16,I[G++]=D-3,M[16]++,z-=D}b=B?I.subarray(0,G):I.slice(0,G);oa=ua(M,7);for(C=0;19>C;C++)wa[C]=oa[pb[C]];for(Z=19;4<Z&&0===wa[Z-1];Z--);Ma=va(oa);E.d(X-257,5,u);E.d(Y-1,5,u);E.d(Z-4,4,u);for(C=0;C<Z;C++)E.d(wa[C],3,u);C=0;for(Na=b.length;C<
Na;C++)if($=b[C],E.d(Ma[$],oa[$],u),16<=$){C++;switch($){case 16:pa=2;break;case 17:pa=3;break;case 18:pa=7;break;default:q("invalid code: "+$)}E.d(b[C],pa,u)}var Qa=[Ka,fa],Ra=[La,ga],N,Sa,ia,za,Ta,Ua,Va,Wa;Ta=Qa[0];Ua=Qa[1];Va=Ra[0];Wa=Ra[1];N=0;for(Sa=R.length;N<Sa;++N)if(ia=R[N],E.d(Ta[ia],Ua[ia],u),256<ia)E.d(R[++N],R[++N],u),za=R[++N],E.d(Va[za],Wa[za],u),E.d(R[++N],R[++N],u);else if(256===ia)break;this.a=E.finish();this.b=this.a.length;break;default:q("invalid compression type")}return this.a};
function xa(b,a){this.length=b;this.P=a}
var ya=function(){function b(a){switch(u){case 3===a:return[257,a-3,0];case 4===a:return[258,a-4,0];case 5===a:return[259,a-5,0];case 6===a:return[260,a-6,0];case 7===a:return[261,a-7,0];case 8===a:return[262,a-8,0];case 9===a:return[263,a-9,0];case 10===a:return[264,a-10,0];case 12>=a:return[265,a-11,1];case 14>=a:return[266,a-13,1];case 16>=a:return[267,a-15,1];case 18>=a:return[268,a-17,1];case 22>=a:return[269,a-19,2];case 26>=a:return[270,a-23,2];case 30>=a:return[271,a-27,2];case 34>=a:return[272,
a-31,2];case 42>=a:return[273,a-35,3];case 50>=a:return[274,a-43,3];case 58>=a:return[275,a-51,3];case 66>=a:return[276,a-59,3];case 82>=a:return[277,a-67,4];case 98>=a:return[278,a-83,4];case 114>=a:return[279,a-99,4];case 130>=a:return[280,a-115,4];case 162>=a:return[281,a-131,5];case 194>=a:return[282,a-163,5];case 226>=a:return[283,a-195,5];case 257>=a:return[284,a-227,5];case 258===a:return[285,a-258,0];default:q("invalid length: "+a)}}var a=[],c,d;for(c=3;258>=c;c++)d=b(c),a[c]=d[2]<<24|d[1]<<
16|d[0];return a}(),Aa=B?new Uint32Array(ya):ya;
function ta(b,a){function c(a,c){var b=a.P,d=[],e=0,f;f=Aa[a.length];d[e++]=f&65535;d[e++]=f>>16&255;d[e++]=f>>24;var g;switch(u){case 1===b:g=[0,b-1,0];break;case 2===b:g=[1,b-2,0];break;case 3===b:g=[2,b-3,0];break;case 4===b:g=[3,b-4,0];break;case 6>=b:g=[4,b-5,1];break;case 8>=b:g=[5,b-7,1];break;case 12>=b:g=[6,b-9,2];break;case 16>=b:g=[7,b-13,2];break;case 24>=b:g=[8,b-17,3];break;case 32>=b:g=[9,b-25,3];break;case 48>=b:g=[10,b-33,4];break;case 64>=b:g=[11,b-49,4];break;case 96>=b:g=[12,b-
65,5];break;case 128>=b:g=[13,b-97,5];break;case 192>=b:g=[14,b-129,6];break;case 256>=b:g=[15,b-193,6];break;case 384>=b:g=[16,b-257,7];break;case 512>=b:g=[17,b-385,7];break;case 768>=b:g=[18,b-513,8];break;case 1024>=b:g=[19,b-769,8];break;case 1536>=b:g=[20,b-1025,9];break;case 2048>=b:g=[21,b-1537,9];break;case 3072>=b:g=[22,b-2049,10];break;case 4096>=b:g=[23,b-3073,10];break;case 6144>=b:g=[24,b-4097,11];break;case 8192>=b:g=[25,b-6145,11];break;case 12288>=b:g=[26,b-8193,12];break;case 16384>=
b:g=[27,b-12289,12];break;case 24576>=b:g=[28,b-16385,13];break;case 32768>=b:g=[29,b-24577,13];break;default:q("invalid distance")}f=g;d[e++]=f[0];d[e++]=f[1];d[e++]=f[2];var h,k;h=0;for(k=d.length;h<k;++h)m[p++]=d[h];v[d[0]]++;x[d[3]]++;r=a.length+c-1;n=null}var d,f,e,g,k,h={},l,s,n,m=B?new Uint16Array(2*a.length):[],p=0,r=0,v=new (B?Uint32Array:Array)(286),x=new (B?Uint32Array:Array)(30),Q=b.I,y;if(!B){for(e=0;285>=e;)v[e++]=0;for(e=0;29>=e;)x[e++]=0}v[256]=1;d=0;for(f=a.length;d<f;++d){e=k=0;
for(g=3;e<g&&d+e!==f;++e)k=k<<8|a[d+e];h[k]===t&&(h[k]=[]);l=h[k];if(!(0<r--)){for(;0<l.length&&32768<d-l[0];)l.shift();if(d+3>=f){n&&c(n,-1);e=0;for(g=f-d;e<g;++e)y=a[d+e],m[p++]=y,++v[y];break}0<l.length?(s=Ba(a,d,l),n?n.length<s.length?(y=a[d-1],m[p++]=y,++v[y],c(s,0)):c(n,-1):s.length<Q?n=s:c(s,0)):n?c(n,-1):(y=a[d],m[p++]=y,++v[y])}l.push(d)}m[p++]=256;v[256]++;b.V=v;b.U=x;return B?m.subarray(0,p):m}
function Ba(b,a,c){var d,f,e=0,g,k,h,l,s=b.length;k=0;l=c.length;a:for(;k<l;k++){d=c[l-k-1];g=3;if(3<e){for(h=e;3<h;h--)if(b[d+h-1]!==b[a+h-1])continue a;g=e}for(;258>g&&a+g<s&&b[d+g]===b[a+g];)++g;g>e&&(f=d,e=g);if(258===g)break}return new xa(e,a-f)}
function ua(b,a){var c=b.length,d=new la(572),f=new (B?Uint8Array:Array)(c),e,g,k,h,l;if(!B)for(h=0;h<c;h++)f[h]=0;for(h=0;h<c;++h)0<b[h]&&d.push(h,b[h]);e=Array(d.length/2);g=new (B?Uint32Array:Array)(d.length/2);if(1===e.length)return f[d.pop().index]=1,f;h=0;for(l=d.length/2;h<l;++h)e[h]=d.pop(),g[h]=e[h].value;k=Ca(g,g.length,a);h=0;for(l=e.length;h<l;++h)f[e[h].index]=k[h];return f}
function Ca(b,a,c){function d(b){var c=h[b][l[b]];c===a?(d(b+1),d(b+1)):--g[c];++l[b]}var f=new (B?Uint16Array:Array)(c),e=new (B?Uint8Array:Array)(c),g=new (B?Uint8Array:Array)(a),k=Array(c),h=Array(c),l=Array(c),s=(1<<c)-a,n=1<<c-1,m,p,r,v,x;f[c-1]=a;for(p=0;p<c;++p)s<n?e[p]=0:(e[p]=1,s-=n),s<<=1,f[c-2-p]=(f[c-1-p]/2|0)+a;f[0]=e[0];k[0]=Array(f[0]);h[0]=Array(f[0]);for(p=1;p<c;++p)f[p]>2*f[p-1]+e[p]&&(f[p]=2*f[p-1]+e[p]),k[p]=Array(f[p]),h[p]=Array(f[p]);for(m=0;m<a;++m)g[m]=c;for(r=0;r<f[c-1];++r)k[c-
1][r]=b[r],h[c-1][r]=r;for(m=0;m<c;++m)l[m]=0;1===e[c-1]&&(--g[0],++l[c-1]);for(p=c-2;0<=p;--p){v=m=0;x=l[p+1];for(r=0;r<f[p];r++)v=k[p+1][x]+k[p+1][x+1],v>b[m]?(k[p][r]=v,h[p][r]=a,x+=2):(k[p][r]=b[m],h[p][r]=m,++m);l[p]=0;1===e[p]&&d(p)}return g}
function va(b){var a=new (B?Uint16Array:Array)(b.length),c=[],d=[],f=0,e,g,k,h;e=0;for(g=b.length;e<g;e++)c[b[e]]=(c[b[e]]|0)+1;e=1;for(g=16;e<=g;e++)d[e]=f,f+=c[e]|0,f<<=1;e=0;for(g=b.length;e<g;e++){f=d[b[e]];d[b[e]]+=1;k=a[e]=0;for(h=b[e];k<h;k++)a[e]=a[e]<<1|f&1,f>>>=1}return a};function Da(b,a){this.input=b;this.b=this.c=0;this.i={};a&&(a.flags&&(this.i=a.flags),"string"===typeof a.filename&&(this.filename=a.filename),"string"===typeof a.comment&&(this.A=a.comment),a.deflateOptions&&(this.l=a.deflateOptions));this.l||(this.l={})}
Da.prototype.g=function(){var b,a,c,d,f,e,g,k,h=new (B?Uint8Array:Array)(32768),l=0,s=this.input,n=this.c,m=this.filename,p=this.A;h[l++]=31;h[l++]=139;h[l++]=8;b=0;this.i.fname&&(b|=Ea);this.i.fcomment&&(b|=Fa);this.i.fhcrc&&(b|=Ga);h[l++]=b;a=(Date.now?Date.now():+new Date)/1E3|0;h[l++]=a&255;h[l++]=a>>>8&255;h[l++]=a>>>16&255;h[l++]=a>>>24&255;h[l++]=0;h[l++]=Ha;if(this.i.fname!==t){g=0;for(k=m.length;g<k;++g)e=m.charCodeAt(g),255<e&&(h[l++]=e>>>8&255),h[l++]=e&255;h[l++]=0}if(this.i.comment){g=
0;for(k=p.length;g<k;++g)e=p.charCodeAt(g),255<e&&(h[l++]=e>>>8&255),h[l++]=e&255;h[l++]=0}this.i.fhcrc&&(c=ja(h,0,l)&65535,h[l++]=c&255,h[l++]=c>>>8&255);this.l.outputBuffer=h;this.l.outputIndex=l;f=new na(s,this.l);h=f.g();l=f.b;B&&(l+8>h.buffer.byteLength?(this.a=new Uint8Array(l+8),this.a.set(new Uint8Array(h.buffer)),h=this.a):h=new Uint8Array(h.buffer));d=ja(s,t,t);h[l++]=d&255;h[l++]=d>>>8&255;h[l++]=d>>>16&255;h[l++]=d>>>24&255;k=s.length;h[l++]=k&255;h[l++]=k>>>8&255;h[l++]=k>>>16&255;h[l++]=
k>>>24&255;this.c=n;B&&l<h.length&&(this.a=h=h.subarray(0,l));return h};var Ha=255,Ga=2,Ea=8,Fa=16;A("Zlib.Gzip",Da);A("Zlib.Gzip.prototype.compress",Da.prototype.g);function T(b,a){this.p=[];this.q=32768;this.e=this.j=this.c=this.u=0;this.input=B?new Uint8Array(b):b;this.w=!1;this.r=Ia;this.L=!1;if(a||!(a={}))a.index&&(this.c=a.index),a.bufferSize&&(this.q=a.bufferSize),a.bufferType&&(this.r=a.bufferType),a.resize&&(this.L=a.resize);switch(this.r){case Xa:this.b=32768;this.a=new (B?Uint8Array:Array)(32768+this.q+258);break;case Ia:this.b=0;this.a=new (B?Uint8Array:Array)(this.q);this.f=this.T;this.B=this.Q;this.s=this.S;break;default:q(Error("invalid inflate mode"))}}
var Xa=0,Ia=1,Ya={N:Xa,M:Ia};
T.prototype.h=function(){for(;!this.w;){var b=U(this,3);b&1&&(this.w=u);b>>>=1;switch(b){case 0:var a=this.input,c=this.c,d=this.a,f=this.b,e=t,g=t,k=t,h=d.length,l=t;this.e=this.j=0;e=a[c++];e===t&&q(Error("invalid uncompressed block header: LEN (first byte)"));g=e;e=a[c++];e===t&&q(Error("invalid uncompressed block header: LEN (second byte)"));g|=e<<8;e=a[c++];e===t&&q(Error("invalid uncompressed block header: NLEN (first byte)"));k=e;e=a[c++];e===t&&q(Error("invalid uncompressed block header: NLEN (second byte)"));k|=
e<<8;g===~k&&q(Error("invalid uncompressed block header: length verify"));c+g>a.length&&q(Error("input buffer is broken"));switch(this.r){case Xa:for(;f+g>d.length;){l=h-f;g-=l;if(B)d.set(a.subarray(c,c+l),f),f+=l,c+=l;else for(;l--;)d[f++]=a[c++];this.b=f;d=this.f();f=this.b}break;case Ia:for(;f+g>d.length;)d=this.f({F:2});break;default:q(Error("invalid inflate mode"))}if(B)d.set(a.subarray(c,c+g),f),f+=g,c+=g;else for(;g--;)d[f++]=a[c++];this.c=c;this.b=f;this.a=d;break;case 1:this.s(Za,$a);break;
case 2:ab(this);break;default:q(Error("unknown BTYPE: "+b))}}return this.B()};
var bb=[16,17,18,0,8,7,9,6,10,5,11,4,12,3,13,2,14,1,15],cb=B?new Uint16Array(bb):bb,db=[3,4,5,6,7,8,9,10,11,13,15,17,19,23,27,31,35,43,51,59,67,83,99,115,131,163,195,227,258,258,258],eb=B?new Uint16Array(db):db,fb=[0,0,0,0,0,0,0,0,1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,4,5,5,5,5,0,0,0],gb=B?new Uint8Array(fb):fb,hb=[1,2,3,4,5,7,9,13,17,25,33,49,65,97,129,193,257,385,513,769,1025,1537,2049,3073,4097,6145,8193,12289,16385,24577],ib=B?new Uint16Array(hb):hb,jb=[0,0,0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,
10,11,11,12,12,13,13],kb=B?new Uint8Array(jb):jb,lb=new (B?Uint8Array:Array)(288),V,mb;V=0;for(mb=lb.length;V<mb;++V)lb[V]=143>=V?8:255>=V?9:279>=V?7:8;var Za=ma(lb),nb=new (B?Uint8Array:Array)(30),ob,qb;ob=0;for(qb=nb.length;ob<qb;++ob)nb[ob]=5;var $a=ma(nb);function U(b,a){for(var c=b.j,d=b.e,f=b.input,e=b.c,g;d<a;)g=f[e++],g===t&&q(Error("input buffer is broken")),c|=g<<d,d+=8;g=c&(1<<a)-1;b.j=c>>>a;b.e=d-a;b.c=e;return g}
function rb(b,a){for(var c=b.j,d=b.e,f=b.input,e=b.c,g=a[0],k=a[1],h,l,s;d<k;){h=f[e++];if(h===t)break;c|=h<<d;d+=8}l=g[c&(1<<k)-1];s=l>>>16;b.j=c>>s;b.e=d-s;b.c=e;return l&65535}
function ab(b){function a(a,b,c){var d,e,f,g;for(g=0;g<a;)switch(d=rb(this,b),d){case 16:for(f=3+U(this,2);f--;)c[g++]=e;break;case 17:for(f=3+U(this,3);f--;)c[g++]=0;e=0;break;case 18:for(f=11+U(this,7);f--;)c[g++]=0;e=0;break;default:e=c[g++]=d}return c}var c=U(b,5)+257,d=U(b,5)+1,f=U(b,4)+4,e=new (B?Uint8Array:Array)(cb.length),g,k,h,l;for(l=0;l<f;++l)e[cb[l]]=U(b,3);g=ma(e);k=new (B?Uint8Array:Array)(c);h=new (B?Uint8Array:Array)(d);b.s(ma(a.call(b,c,g,k)),ma(a.call(b,d,g,h)))}
T.prototype.s=function(b,a){var c=this.a,d=this.b;this.C=b;for(var f=c.length-258,e,g,k,h;256!==(e=rb(this,b));)if(256>e)d>=f&&(this.b=d,c=this.f(),d=this.b),c[d++]=e;else{g=e-257;h=eb[g];0<gb[g]&&(h+=U(this,gb[g]));e=rb(this,a);k=ib[e];0<kb[e]&&(k+=U(this,kb[e]));d>=f&&(this.b=d,c=this.f(),d=this.b);for(;h--;)c[d]=c[d++-k]}for(;8<=this.e;)this.e-=8,this.c--;this.b=d};
T.prototype.S=function(b,a){var c=this.a,d=this.b;this.C=b;for(var f=c.length,e,g,k,h;256!==(e=rb(this,b));)if(256>e)d>=f&&(c=this.f(),f=c.length),c[d++]=e;else{g=e-257;h=eb[g];0<gb[g]&&(h+=U(this,gb[g]));e=rb(this,a);k=ib[e];0<kb[e]&&(k+=U(this,kb[e]));d+h>f&&(c=this.f(),f=c.length);for(;h--;)c[d]=c[d++-k]}for(;8<=this.e;)this.e-=8,this.c--;this.b=d};
T.prototype.f=function(){var b=new (B?Uint8Array:Array)(this.b-32768),a=this.b-32768,c,d,f=this.a;if(B)b.set(f.subarray(32768,b.length));else{c=0;for(d=b.length;c<d;++c)b[c]=f[c+32768]}this.p.push(b);this.u+=b.length;if(B)f.set(f.subarray(a,a+32768));else for(c=0;32768>c;++c)f[c]=f[a+c];this.b=32768;return f};
T.prototype.T=function(b){var a,c=this.input.length/this.c+1|0,d,f,e,g=this.input,k=this.a;b&&("number"===typeof b.F&&(c=b.F),"number"===typeof b.O&&(c+=b.O));2>c?(d=(g.length-this.c)/this.C[2],e=258*(d/2)|0,f=e<k.length?k.length+e:k.length<<1):f=k.length*c;B?(a=new Uint8Array(f),a.set(k)):a=k;return this.a=a};
T.prototype.B=function(){var b=0,a=this.a,c=this.p,d,f=new (B?Uint8Array:Array)(this.u+(this.b-32768)),e,g,k,h;if(0===c.length)return B?this.a.subarray(32768,this.b):this.a.slice(32768,this.b);e=0;for(g=c.length;e<g;++e){d=c[e];k=0;for(h=d.length;k<h;++k)f[b++]=d[k]}e=32768;for(g=this.b;e<g;++e)f[b++]=a[e];this.p=[];return this.buffer=f};
T.prototype.Q=function(){var b,a=this.b;B?this.L?(b=new Uint8Array(a),b.set(this.a.subarray(0,a))):b=this.a.subarray(0,a):(this.a.length>a&&(this.a.length=a),b=this.a);return this.buffer=b};function sb(b){this.input=b;this.c=0;this.t=[];this.D=!1}sb.prototype.W=function(){this.D||this.h();return this.t.slice()};
sb.prototype.h=function(){for(var b=this.input.length;this.c<b;){var a=new P,c=t,d=t,f=t,e=t,g=t,k=t,h=t,l=t,s=t,n=this.input,m=this.c;a.G=n[m++];a.H=n[m++];(31!==a.G||139!==a.H)&&q(Error("invalid file signature:"+a.G+","+a.H));a.z=n[m++];switch(a.z){case 8:break;default:q(Error("unknown compression method: "+a.z))}a.n=n[m++];l=n[m++]|n[m++]<<8|n[m++]<<16|n[m++]<<24;a.Y=new Date(1E3*l);a.ea=n[m++];a.da=n[m++];0<(a.n&4)&&(a.$=n[m++]|n[m++]<<8,m+=a.$);if(0<(a.n&Ea)){h=[];for(k=0;0<(g=n[m++]);)h[k++]=
String.fromCharCode(g);a.name=h.join("")}if(0<(a.n&Fa)){h=[];for(k=0;0<(g=n[m++]);)h[k++]=String.fromCharCode(g);a.A=h.join("")}0<(a.n&Ga)&&(a.R=ja(n,0,m)&65535,a.R!==(n[m++]|n[m++]<<8)&&q(Error("invalid header crc16")));c=n[n.length-4]|n[n.length-3]<<8|n[n.length-2]<<16|n[n.length-1]<<24;n.length-m-4-4<512*c&&(e=c);d=new T(n,{index:m,bufferSize:e});a.data=f=d.h();m=d.c;a.ba=s=(n[m++]|n[m++]<<8|n[m++]<<16|n[m++]<<24)>>>0;ja(f,t,t)!==s&&q(Error("invalid CRC-32 checksum: 0x"+ja(f,t,t).toString(16)+
" / 0x"+s.toString(16)));a.ca=c=(n[m++]|n[m++]<<8|n[m++]<<16|n[m++]<<24)>>>0;(f.length&4294967295)!==c&&q(Error("invalid input size: "+(f.length&4294967295)+" / "+c));this.t.push(a);this.c=m}this.D=u;var p=this.t,r,v,x=0,Q=0,y;r=0;for(v=p.length;r<v;++r)Q+=p[r].data.length;if(B){y=new Uint8Array(Q);for(r=0;r<v;++r)y.set(p[r].data,x),x+=p[r].data.length}else{y=[];for(r=0;r<v;++r)y[r]=p[r].data;y=Array.prototype.concat.apply([],y)}return y};A("Zlib.Gunzip",sb);A("Zlib.Gunzip.prototype.decompress",sb.prototype.h);A("Zlib.Gunzip.prototype.getMembers",sb.prototype.W);function tb(b){if("string"===typeof b){var a=b.split(""),c,d;c=0;for(d=a.length;c<d;c++)a[c]=(a[c].charCodeAt(0)&255)>>>0;b=a}for(var f=1,e=0,g=b.length,k,h=0;0<g;){k=1024<g?1024:g;g-=k;do f+=b[h++],e+=f;while(--k);f%=65521;e%=65521}return(e<<16|f)>>>0};function ub(b,a){var c,d;this.input=b;this.c=0;if(a||!(a={}))a.index&&(this.c=a.index),a.verify&&(this.Z=a.verify);c=b[this.c++];d=b[this.c++];switch(c&15){case vb:this.method=vb;break;default:q(Error("unsupported compression method"))}0!==((c<<8)+d)%31&&q(Error("invalid fcheck flag:"+((c<<8)+d)%31));d&32&&q(Error("fdict flag is not supported"));this.K=new T(b,{index:this.c,bufferSize:a.bufferSize,bufferType:a.bufferType,resize:a.resize})}
ub.prototype.h=function(){var b=this.input,a,c;a=this.K.h();this.c=this.K.c;this.Z&&(c=(b[this.c++]<<24|b[this.c++]<<16|b[this.c++]<<8|b[this.c++])>>>0,c!==tb(a)&&q(Error("invalid adler-32 checksum")));return a};var vb=8;function wb(b,a){this.input=b;this.a=new (B?Uint8Array:Array)(32768);this.k=W.o;var c={},d;if((a||!(a={}))&&"number"===typeof a.compressionType)this.k=a.compressionType;for(d in a)c[d]=a[d];c.outputBuffer=this.a;this.J=new na(this.input,c)}var W=ra;
wb.prototype.g=function(){var b,a,c,d,f,e,g,k=0;g=this.a;b=vb;switch(b){case vb:a=Math.LOG2E*Math.log(32768)-8;break;default:q(Error("invalid compression method"))}c=a<<4|b;g[k++]=c;switch(b){case vb:switch(this.k){case W.NONE:f=0;break;case W.v:f=1;break;case W.o:f=2;break;default:q(Error("unsupported compression type"))}break;default:q(Error("invalid compression method"))}d=f<<6|0;g[k++]=d|31-(256*c+d)%31;e=tb(this.input);this.J.b=k;g=this.J.g();k=g.length;B&&(g=new Uint8Array(g.buffer),g.length<=
k+4&&(this.a=new Uint8Array(g.length+4),this.a.set(g),g=this.a),g=g.subarray(0,k+4));g[k++]=e>>24&255;g[k++]=e>>16&255;g[k++]=e>>8&255;g[k++]=e&255;return g};function xb(b,a){var c,d,f,e;if(Object.keys)c=Object.keys(a);else for(d in c=[],f=0,a)c[f++]=d;f=0;for(e=c.length;f<e;++f)d=c[f],A(b+"."+d,a[d])};A("Zlib.Inflate",ub);A("Zlib.Inflate.prototype.decompress",ub.prototype.h);xb("Zlib.Inflate.BufferType",{ADAPTIVE:Ya.M,BLOCK:Ya.N});A("Zlib.Deflate",wb);A("Zlib.Deflate.compress",function(b,a){return(new wb(b,a)).g()});A("Zlib.Deflate.prototype.compress",wb.prototype.g);xb("Zlib.Deflate.CompressionType",{NONE:W.NONE,FIXED:W.v,DYNAMIC:W.o});}).call(this); //@ sourceMappingURL=zlib_and_gzip.min.js.map

/*!
 * jQuery Mousewheel 3.1.12
 *
 * Copyright 2014 jQuery Foundation and other contributors
 * Released under the MIT license.
 * http://jquery.org/license
 */

(function (factory) {
    factory(jQuery);

}(function ($) {

    var toFix = ['wheel', 'mousewheel', 'DOMMouseScroll', 'MozMousePixelScroll'],
        toBind = ( 'onwheel' in document || document.documentMode >= 9 ) ?
            ['wheel'] : ['mousewheel', 'DomMouseScroll', 'MozMousePixelScroll'],
        slice = Array.prototype.slice,
        nullLowestDeltaTimeout, lowestDelta;

    if ($.event.fixHooks) {
        for (var i = toFix.length; i;) {
            $.event.fixHooks[toFix[--i]] = $.event.mouseHooks;
        }
    }

    var special = $.event.special.mousewheel = {
        version: '3.1.12',

        setup: function () {
            if (this.addEventListener) {
                for (var i = toBind.length; i;) {
                    this.addEventListener(toBind[--i], handler, false);
                }
            } else {
                this.onmousewheel = handler;
            }
            // Store the line height and page height for this particular element
            $.data(this, 'mousewheel-line-height', special.getLineHeight(this));
            $.data(this, 'mousewheel-page-height', special.getPageHeight(this));
        },

        teardown: function () {
            if (this.removeEventListener) {
                for (var i = toBind.length; i;) {
                    this.removeEventListener(toBind[--i], handler, false);
                }
            } else {
                this.onmousewheel = null;
            }
            // Clean up the data we added to the element
            $.removeData(this, 'mousewheel-line-height');
            $.removeData(this, 'mousewheel-page-height');
        },

        getLineHeight: function (elem) {
            var $elem = $(elem),
                $parent = $elem['offsetParent' in $.fn ? 'offsetParent' : 'parent']();
            if (!$parent.length) {
                $parent = $('body');
            }
            return parseInt($parent.css('fontSize'), 10) || parseInt($elem.css('fontSize'), 10) || 16;
        },

        getPageHeight: function (elem) {
            return $(elem).height();
        },

        settings: {
            adjustOldDeltas: true, // see shouldAdjustOldDeltas() below
            normalizeOffset: true  // calls getBoundingClientRect for each event
        }
    };

    $.fn.extend({
        mousewheel: function (fn) {
            return fn ? this.bind('mousewheel', fn) : this.trigger('mousewheel');
        },

        unmousewheel: function (fn) {
            return this.unbind('mousewheel', fn);
        }
    });


    function handler(event) {
        var orgEvent = event || window.event,
            args = slice.call(arguments, 1),
            delta = 0,
            deltaX = 0,
            deltaY = 0,
            absDelta = 0,
            offsetX = 0,
            offsetY = 0;
        event = $.event.fix(orgEvent);
        event.type = 'mousewheel';

        // Old school scrollwheel delta
        if ('detail' in orgEvent) {
            deltaY = orgEvent.detail * -1;
        }
        if ('wheelDelta' in orgEvent) {
            deltaY = orgEvent.wheelDelta;
        }
        if ('wheelDeltaY' in orgEvent) {
            deltaY = orgEvent.wheelDeltaY;
        }
        if ('wheelDeltaX' in orgEvent) {
            deltaX = orgEvent.wheelDeltaX * -1;
        }

        // Firefox < 17 horizontal scrolling related to DOMMouseScroll event
        if ('axis' in orgEvent && orgEvent.axis === orgEvent.HORIZONTAL_AXIS) {
            deltaX = deltaY * -1;
            deltaY = 0;
        }

        // Set delta to be deltaY or deltaX if deltaY is 0 for backwards compatabilitiy
        delta = deltaY === 0 ? deltaX : deltaY;

        // New school wheel delta (wheel event)
        if ('deltaY' in orgEvent) {
            deltaY = orgEvent.deltaY * -1;
            delta = deltaY;
        }
        if ('deltaX' in orgEvent) {
            deltaX = orgEvent.deltaX;
            if (deltaY === 0) {
                delta = deltaX * -1;
            }
        }

        // No change actually happened, no reason to go any further
        if (deltaY === 0 && deltaX === 0) {
            return;
        }

        // Need to convert lines and pages to pixels if we aren't already in pixels
        // There are three delta modes:
        //   * deltaMode 0 is by pixels, nothing to do
        //   * deltaMode 1 is by lines
        //   * deltaMode 2 is by pages
        if (orgEvent.deltaMode === 1) {
            var lineHeight = $.data(this, 'mousewheel-line-height');
            delta *= lineHeight;
            deltaY *= lineHeight;
            deltaX *= lineHeight;
        } else if (orgEvent.deltaMode === 2) {
            var pageHeight = $.data(this, 'mousewheel-page-height');
            delta *= pageHeight;
            deltaY *= pageHeight;
            deltaX *= pageHeight;
        }

        // Store lowest absolute delta to normalize the delta values
        absDelta = Math.max(Math.abs(deltaY), Math.abs(deltaX));

        if (!lowestDelta || absDelta < lowestDelta) {
            lowestDelta = absDelta;

            // Adjust older deltas if necessary
            if (shouldAdjustOldDeltas(orgEvent, absDelta)) {
                lowestDelta /= 40;
            }
        }

        // Adjust older deltas if necessary
        if (shouldAdjustOldDeltas(orgEvent, absDelta)) {
            // Divide all the things by 40!
            delta /= 40;
            deltaX /= 40;
            deltaY /= 40;
        }

        // Get a whole, normalized value for the deltas
        delta = Math[delta >= 1 ? 'floor' : 'ceil'](delta / lowestDelta);
        deltaX = Math[deltaX >= 1 ? 'floor' : 'ceil'](deltaX / lowestDelta);
        deltaY = Math[deltaY >= 1 ? 'floor' : 'ceil'](deltaY / lowestDelta);

        // Normalise offsetX and offsetY properties
        if (special.settings.normalizeOffset && this.getBoundingClientRect) {
            var boundingRect = this.getBoundingClientRect();
            offsetX = event.clientX - boundingRect.left;
            offsetY = event.clientY - boundingRect.top;
        }

        // Add information to the event object
        event.deltaX = deltaX;
        event.deltaY = deltaY;
        event.deltaFactor = lowestDelta;
        event.offsetX = offsetX;
        event.offsetY = offsetY;
        // Go ahead and set deltaMode to 0 since we converted to pixels
        // Although this is a little odd since we overwrite the deltaX/Y
        // properties with normalized deltas.
        event.deltaMode = 0;

        // Add event and delta to the front of the arguments
        args.unshift(event, delta, deltaX, deltaY);

        // Clearout lowestDelta after sometime to better
        // handle multiple device types that give different
        // a different lowestDelta
        // Ex: trackpad = 3 and mouse wheel = 120
        if (nullLowestDeltaTimeout) {
            clearTimeout(nullLowestDeltaTimeout);
        }
        nullLowestDeltaTimeout = setTimeout(nullLowestDelta, 200);

        return ($.event.dispatch || $.event.handle).apply(this, args);
    }

    function nullLowestDelta() {
        lowestDelta = null;
    }

    function shouldAdjustOldDeltas(orgEvent, absDelta) {
        // If this is an older event and the delta is divisable by 120,
        // then we are assuming that the browser is treating this as an
        // older mouse wheel event and that we should divide the deltas
        // by 40 to try and get a more usable deltaFactor.
        // Side note, this actually impacts the reported scroll distance
        // in older browsers and can cause scrolling to be slower than native.
        // Turn this off by setting $.event.special.mousewheel.settings.adjustOldDeltas to false.
        return special.settings.adjustOldDeltas && orgEvent.type === 'mousewheel' && absDelta % 120 === 0;
    }

}));

(function e(t, n, r) {
  function s(o, u) {
    if (!n[o]) {
      if (!t[o]) {
        var a = typeof require == "function" && require;
        if (!u && a) return a(o, !0);
        if (i) return i(o, !0);
        var f = new Error("Cannot find module '" + o + "'");
        throw f.code = "MODULE_NOT_FOUND", f;
      }
      var l = n[o] = {
        exports: {}
      };
      t[o][0].call(l.exports, function(e) {
        var n = t[o][1][e];
        return s(n ? n : e);
      }, l, l.exports, e, t, n, r);
    }
    return n[o].exports;
  }
  var i = typeof require == "function" && require;
  for (var o = 0; o < r.length; o++) s(r[o]);
  return s;
})({
  1: [ function(require, module, exports) {
    "use strict";
    var asap = require("asap/raw");
    function noop() {}
    var LAST_ERROR = null;
    var IS_ERROR = {};
    function getThen(obj) {
      try {
        return obj.then;
      } catch (ex) {
        LAST_ERROR = ex;
        return IS_ERROR;
      }
    }
    function tryCallOne(fn, a) {
      try {
        return fn(a);
      } catch (ex) {
        LAST_ERROR = ex;
        return IS_ERROR;
      }
    }
    function tryCallTwo(fn, a, b) {
      try {
        fn(a, b);
      } catch (ex) {
        LAST_ERROR = ex;
        return IS_ERROR;
      }
    }
    module.exports = Promise;
    function Promise(fn) {
      if (typeof this !== "object") {
        throw new TypeError("Promises must be constructed via new");
      }
      if (typeof fn !== "function") {
        throw new TypeError("not a function");
      }
      this._37 = 0;
      this._12 = null;
      this._59 = [];
      if (fn === noop) return;
      doResolve(fn, this);
    }
    Promise._99 = noop;
    Promise.prototype.then = function(onFulfilled, onRejected) {
      if (this.constructor !== Promise) {
        return safeThen(this, onFulfilled, onRejected);
      }
      var res = new Promise(noop);
      handle(this, new Handler(onFulfilled, onRejected, res));
      return res;
    };
    function safeThen(self, onFulfilled, onRejected) {
      return new self.constructor(function(resolve, reject) {
        var res = new Promise(noop);
        res.then(resolve, reject);
        handle(self, new Handler(onFulfilled, onRejected, res));
      });
    }
    function handle(self, deferred) {
      while (self._37 === 3) {
        self = self._12;
      }
      if (self._37 === 0) {
        self._59.push(deferred);
        return;
      }
      asap(function() {
        var cb = self._37 === 1 ? deferred.onFulfilled : deferred.onRejected;
        if (cb === null) {
          if (self._37 === 1) {
            resolve(deferred.promise, self._12);
          } else {
            reject(deferred.promise, self._12);
          }
          return;
        }
        var ret = tryCallOne(cb, self._12);
        if (ret === IS_ERROR) {
          reject(deferred.promise, LAST_ERROR);
        } else {
          resolve(deferred.promise, ret);
        }
      });
    }
    function resolve(self, newValue) {
      if (newValue === self) {
        return reject(self, new TypeError("A promise cannot be resolved with itself."));
      }
      if (newValue && (typeof newValue === "object" || typeof newValue === "function")) {
        var then = getThen(newValue);
        if (then === IS_ERROR) {
          return reject(self, LAST_ERROR);
        }
        if (then === self.then && newValue instanceof Promise) {
          self._37 = 3;
          self._12 = newValue;
          finale(self);
          return;
        } else if (typeof then === "function") {
          doResolve(then.bind(newValue), self);
          return;
        }
      }
      self._37 = 1;
      self._12 = newValue;
      finale(self);
    }
    function reject(self, newValue) {
      self._37 = 2;
      self._12 = newValue;
      finale(self);
    }
    function finale(self) {
      for (var i = 0; i < self._59.length; i++) {
        handle(self, self._59[i]);
      }
      self._59 = null;
    }
    function Handler(onFulfilled, onRejected, promise) {
      this.onFulfilled = typeof onFulfilled === "function" ? onFulfilled : null;
      this.onRejected = typeof onRejected === "function" ? onRejected : null;
      this.promise = promise;
    }
    function doResolve(fn, promise) {
      var done = false;
      var res = tryCallTwo(fn, function(value) {
        if (done) return;
        done = true;
        resolve(promise, value);
      }, function(reason) {
        if (done) return;
        done = true;
        reject(promise, reason);
      });
      if (!done && res === IS_ERROR) {
        done = true;
        reject(promise, LAST_ERROR);
      }
    }
  }, {
    "asap/raw": 4
  } ],
  2: [ function(require, module, exports) {
    "use strict";
    var Promise = require("./core.js");
    module.exports = Promise;
    var TRUE = valuePromise(true);
    var FALSE = valuePromise(false);
    var NULL = valuePromise(null);
    var UNDEFINED = valuePromise(undefined);
    var ZERO = valuePromise(0);
    var EMPTYSTRING = valuePromise("");
    function valuePromise(value) {
      var p = new Promise(Promise._99);
      p._37 = 1;
      p._12 = value;
      return p;
    }
    Promise.resolve = function(value) {
      if (value instanceof Promise) return value;
      if (value === null) return NULL;
      if (value === undefined) return UNDEFINED;
      if (value === true) return TRUE;
      if (value === false) return FALSE;
      if (value === 0) return ZERO;
      if (value === "") return EMPTYSTRING;
      if (typeof value === "object" || typeof value === "function") {
        try {
          var then = value.then;
          if (typeof then === "function") {
            return new Promise(then.bind(value));
          }
        } catch (ex) {
          return new Promise(function(resolve, reject) {
            reject(ex);
          });
        }
      }
      return valuePromise(value);
    };
    Promise.all = function(arr) {
      var args = Array.prototype.slice.call(arr);
      return new Promise(function(resolve, reject) {
        if (args.length === 0) return resolve([]);
        var remaining = args.length;
        function res(i, val) {
          if (val && (typeof val === "object" || typeof val === "function")) {
            if (val instanceof Promise && val.then === Promise.prototype.then) {
              while (val._37 === 3) {
                val = val._12;
              }
              if (val._37 === 1) return res(i, val._12);
              if (val._37 === 2) reject(val._12);
              val.then(function(val) {
                res(i, val);
              }, reject);
              return;
            } else {
              var then = val.then;
              if (typeof then === "function") {
                var p = new Promise(then.bind(val));
                p.then(function(val) {
                  res(i, val);
                }, reject);
                return;
              }
            }
          }
          args[i] = val;
          if (--remaining === 0) {
            resolve(args);
          }
        }
        for (var i = 0; i < args.length; i++) {
          res(i, args[i]);
        }
      });
    };
    Promise.reject = function(value) {
      return new Promise(function(resolve, reject) {
        reject(value);
      });
    };
    Promise.race = function(values) {
      return new Promise(function(resolve, reject) {
        values.forEach(function(value) {
          Promise.resolve(value).then(resolve, reject);
        });
      });
    };
    Promise.prototype["catch"] = function(onRejected) {
      return this.then(null, onRejected);
    };
  }, {
    "./core.js": 1
  } ],
  3: [ function(require, module, exports) {
    "use strict";
    var rawAsap = require("./raw");
    var freeTasks = [];
    var pendingErrors = [];
    var requestErrorThrow = rawAsap.makeRequestCallFromTimer(throwFirstError);
    function throwFirstError() {
      if (pendingErrors.length) {
        throw pendingErrors.shift();
      }
    }
    module.exports = asap;
    function asap(task) {
      var rawTask;
      if (freeTasks.length) {
        rawTask = freeTasks.pop();
      } else {
        rawTask = new RawTask();
      }
      rawTask.task = task;
      rawAsap(rawTask);
    }
    function RawTask() {
      this.task = null;
    }
    RawTask.prototype.call = function() {
      try {
        this.task.call();
      } catch (error) {
        if (asap.onerror) {
          asap.onerror(error);
        } else {
          pendingErrors.push(error);
          requestErrorThrow();
        }
      } finally {
        this.task = null;
        freeTasks[freeTasks.length] = this;
      }
    };
  }, {
    "./raw": 4
  } ],
  4: [ function(require, module, exports) {
    (function(global) {
      "use strict";
      module.exports = rawAsap;
      function rawAsap(task) {
        if (!queue.length) {
          requestFlush();
          flushing = true;
        }
        queue[queue.length] = task;
      }
      var queue = [];
      var flushing = false;
      var requestFlush;
      var index = 0;
      var capacity = 1024;
      function flush() {
        while (index < queue.length) {
          var currentIndex = index;
          index = index + 1;
          queue[currentIndex].call();
          if (index > capacity) {
            for (var scan = 0, newLength = queue.length - index; scan < newLength; scan++) {
              queue[scan] = queue[scan + index];
            }
            queue.length -= index;
            index = 0;
          }
        }
        queue.length = 0;
        index = 0;
        flushing = false;
      }
      var BrowserMutationObserver = global.MutationObserver || global.WebKitMutationObserver;
      if (typeof BrowserMutationObserver === "function") {
        requestFlush = makeRequestCallFromMutationObserver(flush);
      } else {
        requestFlush = makeRequestCallFromTimer(flush);
      }
      rawAsap.requestFlush = requestFlush;
      function makeRequestCallFromMutationObserver(callback) {
        var toggle = 1;
        var observer = new BrowserMutationObserver(callback);
        var node = document.createTextNode("");
        observer.observe(node, {
          characterData: true
        });
        return function requestCall() {
          toggle = -toggle;
          node.data = toggle;
        };
      }
      function makeRequestCallFromTimer(callback) {
        return function requestCall() {
          var timeoutHandle = setTimeout(handleTimer, 0);
          var intervalHandle = setInterval(handleTimer, 50);
          function handleTimer() {
            clearTimeout(timeoutHandle);
            clearInterval(intervalHandle);
            callback();
          }
        };
      }
      rawAsap.makeRequestCallFromTimer = makeRequestCallFromTimer;
    }).call(this, typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {});
  }, {} ],
  5: [ function(require, module, exports) {
    if (typeof Promise.prototype.done !== "function") {
      Promise.prototype.done = function(onFulfilled, onRejected) {
        var self = arguments.length ? this.then.apply(this, arguments) : this;
        self.then(null, function(err) {
          setTimeout(function() {
            throw err;
          }, 0);
        });
      };
    }
  }, {} ],
  6: [ function(require, module, exports) {
    var asap = require("asap");
    if (typeof Promise === "undefined") {
      Promise = require("./lib/core.js");
      require("./lib/es6-extensions.js");
    }
    require("./polyfill-done.js");
  }, {
    "./lib/core.js": 1,
    "./lib/es6-extensions.js": 2,
    "./polyfill-done.js": 5,
    asap: 3
  } ]
}, {}, [ 6 ]);
//# sourceMappingURL=/polyfills/promise-7.0.4.js.map
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Support for module definition.  This code should be last in the concatenated "igv.js" file.
 *
 */

(function (factory) {
    if ( typeof define === 'function' && define.amd ) {
        // AMD. Register as an anonymous module.
        define(['jquery'], factory);
    } else if (typeof exports === 'object') {
        // Node/CommonJS style for Browserify
        module.exports = factory;
    }
}(function (ignored) {
    if(igv === undefined)  igv = {};  // Define global igv object
    return igv;
}));



/*! jquery.kinetic - v2.2.1 - 2015-09-09 http://the-taylors.org/jquery.kinetic 
 * Copyright (c) 2015 Dave Taylor; Licensed MIT */
!function(a){"use strict";var b="kinetic-active";window.requestAnimationFrame||(window.requestAnimationFrame=function(){return window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||window.oRequestAnimationFrame||window.msRequestAnimationFrame||function(a){window.setTimeout(a,1e3/60)}}()),a.support=a.support||{},a.extend(a.support,{touch:"ontouchend"in document});var c=function(b,c){return this.settings=c,this.el=b,this.$el=a(b),this._initElements(),this};c.DATA_KEY="kinetic",c.DEFAULTS={cursor:"move",decelerate:!0,triggerHardware:!1,threshold:0,y:!0,x:!0,slowdown:.9,maxvelocity:40,throttleFPS:60,invert:!1,movingClass:{up:"kinetic-moving-up",down:"kinetic-moving-down",left:"kinetic-moving-left",right:"kinetic-moving-right"},deceleratingClass:{up:"kinetic-decelerating-up",down:"kinetic-decelerating-down",left:"kinetic-decelerating-left",right:"kinetic-decelerating-right"}},c.prototype.start=function(b){this.settings=a.extend(this.settings,b),this.velocity=b.velocity||this.velocity,this.velocityY=b.velocityY||this.velocityY,this.settings.decelerate=!1,this._move()},c.prototype.end=function(){this.settings.decelerate=!0},c.prototype.stop=function(){this.velocity=0,this.velocityY=0,this.settings.decelerate=!0,a.isFunction(this.settings.stopped)&&this.settings.stopped.call(this)},c.prototype.detach=function(){this._detachListeners(),this.$el.removeClass(b).css("cursor","")},c.prototype.attach=function(){this.$el.hasClass(b)||(this._attachListeners(this.$el),this.$el.addClass(b).css("cursor",this.settings.cursor))},c.prototype._initElements=function(){this.$el.addClass(b),a.extend(this,{xpos:null,prevXPos:!1,ypos:null,prevYPos:!1,mouseDown:!1,throttleTimeout:1e3/this.settings.throttleFPS,lastMove:null,elementFocused:null}),this.velocity=0,this.velocityY=0,a(document).mouseup(a.proxy(this._resetMouse,this)).click(a.proxy(this._resetMouse,this)),this._initEvents(),this.$el.css("cursor",this.settings.cursor),this.settings.triggerHardware&&this.$el.css({"-webkit-transform":"translate3d(0,0,0)","-webkit-perspective":"1000","-webkit-backface-visibility":"hidden"})},c.prototype._initEvents=function(){var b=this;this.settings.events={touchStart:function(a){var c;b._useTarget(a.target,a)&&(c=a.originalEvent.touches[0],b.threshold=b._threshold(a.target,a),b._start(c.clientX,c.clientY),a.stopPropagation())},touchMove:function(a){var c;b.mouseDown&&(c=a.originalEvent.touches[0],b._inputmove(c.clientX,c.clientY),a.preventDefault&&a.preventDefault())},inputDown:function(a){b._useTarget(a.target,a)&&(b.threshold=b._threshold(a.target,a),b._start(a.clientX,a.clientY),b.elementFocused=a.target,"IMG"===a.target.nodeName&&a.preventDefault(),a.stopPropagation())},inputEnd:function(a){b._useTarget(a.target,a)&&(b._end(),b.elementFocused=null,a.preventDefault&&a.preventDefault())},inputMove:function(a){b.mouseDown&&(b._inputmove(a.clientX,a.clientY),a.preventDefault&&a.preventDefault())},scroll:function(c){a.isFunction(b.settings.moved)&&b.settings.moved.call(b,b.settings),c.preventDefault&&c.preventDefault()},inputClick:function(a){return Math.abs(b.velocity)>0?(a.preventDefault(),!1):void 0},dragStart:function(a){return b._useTarget(a.target,a)&&b.elementFocused?!1:void 0},selectStart:function(c){return a.isFunction(b.settings.selectStart)?b.settings.selectStart.apply(b,arguments):b._useTarget(c.target,c)?!1:void 0}},this._attachListeners(this.$el,this.settings)},c.prototype._inputmove=function(b,c){{var d=this.$el;this.el}if((!this.lastMove||new Date>new Date(this.lastMove.getTime()+this.throttleTimeout))&&(this.lastMove=new Date,this.mouseDown&&(this.xpos||this.ypos))){var e=b-this.xpos,f=c-this.ypos;if(this.settings.invert&&(e*=-1,f*=-1),this.threshold>0){var g=Math.sqrt(e*e+f*f);if(this.threshold>g)return;this.threshold=0}this.elementFocused&&(a(this.elementFocused).blur(),this.elementFocused=null,d.focus()),this.settings.decelerate=!1,this.velocity=this.velocityY=0;var h=this.scrollLeft(),i=this.scrollTop();this.scrollLeft(this.settings.x?h-e:h),this.scrollTop(this.settings.y?i-f:i),this.prevXPos=this.xpos,this.prevYPos=this.ypos,this.xpos=b,this.ypos=c,this._calculateVelocities(),this._setMoveClasses(this.settings.movingClass),a.isFunction(this.settings.moved)&&this.settings.moved.call(this,this.settings)}},c.prototype._calculateVelocities=function(){this.velocity=this._capVelocity(this.prevXPos-this.xpos,this.settings.maxvelocity),this.velocityY=this._capVelocity(this.prevYPos-this.ypos,this.settings.maxvelocity),this.settings.invert&&(this.velocity*=-1,this.velocityY*=-1)},c.prototype._end=function(){this.xpos&&this.prevXPos&&this.settings.decelerate===!1&&(this.settings.decelerate=!0,this._calculateVelocities(),this.xpos=this.prevXPos=this.mouseDown=!1,this._move())},c.prototype._useTarget=function(b,c){return a.isFunction(this.settings.filterTarget)?this.settings.filterTarget.call(this,b,c)!==!1:!0},c.prototype._threshold=function(b,c){return a.isFunction(this.settings.threshold)?this.settings.threshold.call(this,b,c):this.settings.threshold},c.prototype._start=function(a,b){this.mouseDown=!0,this.velocity=this.prevXPos=0,this.velocityY=this.prevYPos=0,this.xpos=a,this.ypos=b},c.prototype._resetMouse=function(){this.xpos=!1,this.ypos=!1,this.mouseDown=!1},c.prototype._decelerateVelocity=function(a,b){return 0===Math.floor(Math.abs(a))?0:a*b},c.prototype._capVelocity=function(a,b){var c=a;return a>0?a>b&&(c=b):0-b>a&&(c=0-b),c},c.prototype._setMoveClasses=function(a){var b=this.settings,c=this.$el;c.removeClass(b.movingClass.up).removeClass(b.movingClass.down).removeClass(b.movingClass.left).removeClass(b.movingClass.right).removeClass(b.deceleratingClass.up).removeClass(b.deceleratingClass.down).removeClass(b.deceleratingClass.left).removeClass(b.deceleratingClass.right),this.velocity>0&&c.addClass(a.right),this.velocity<0&&c.addClass(a.left),this.velocityY>0&&c.addClass(a.down),this.velocityY<0&&c.addClass(a.up)},c.prototype._move=function(){var b=this._getScroller(),c=b[0],d=this,e=d.settings;e.x&&c.scrollWidth>0?(this.scrollLeft(this.scrollLeft()+this.velocity),Math.abs(this.velocity)>0&&(this.velocity=e.decelerate?d._decelerateVelocity(this.velocity,e.slowdown):this.velocity)):this.velocity=0,e.y&&c.scrollHeight>0?(this.scrollTop(this.scrollTop()+this.velocityY),Math.abs(this.velocityY)>0&&(this.velocityY=e.decelerate?d._decelerateVelocity(this.velocityY,e.slowdown):this.velocityY)):this.velocityY=0,d._setMoveClasses(e.deceleratingClass),a.isFunction(e.moved)&&e.moved.call(this,e),Math.abs(this.velocity)>0||Math.abs(this.velocityY)>0?this.moving||(this.moving=!0,window.requestAnimationFrame(function(){d.moving=!1,d._move()})):d.stop()},c.prototype._getScroller=function(){var b=this.$el;return(this.$el.is("body")||this.$el.is("html"))&&(b=a(window)),b},c.prototype.scrollLeft=function(a){var b=this._getScroller();return"number"!=typeof a?b.scrollLeft():(b.scrollLeft(a),void(this.settings.scrollLeft=a))},c.prototype.scrollTop=function(a){var b=this._getScroller();return"number"!=typeof a?b.scrollTop():(b.scrollTop(a),void(this.settings.scrollTop=a))},c.prototype._attachListeners=function(){var b=this.$el,c=this.settings;a.support.touch&&b.bind("touchstart",c.events.touchStart).bind("touchend",c.events.inputEnd).bind("touchmove",c.events.touchMove),b.mousedown(c.events.inputDown).mouseup(c.events.inputEnd).mousemove(c.events.inputMove),b.click(c.events.inputClick).scroll(c.events.scroll).bind("selectstart",c.events.selectStart).bind("dragstart",c.events.dragStart)},c.prototype._detachListeners=function(){var b=this.$el,c=this.settings;a.support.touch&&b.unbind("touchstart",c.events.touchStart).unbind("touchend",c.events.inputEnd).unbind("touchmove",c.events.touchMove),b.unbind("mousedown",c.events.inputDown).unbind("mouseup",c.events.inputEnd).unbind("mousemove",c.events.inputMove),b.unbind("click",c.events.inputClick).unbind("scroll",c.events.scroll).unbind("selectstart",c.events.selectStart).unbind("dragstart",c.events.dragStart)},a.Kinetic=c,a.fn.kinetic=function(b,d){return this.each(function(){var e=a(this),f=e.data(c.DATA_KEY),g=a.extend({},c.DEFAULTS,e.data(),"object"==typeof b&&b);f||e.data(c.DATA_KEY,f=new c(this,g)),"string"==typeof b&&f[b](d)})}}(window.jQuery||window.Zepto);