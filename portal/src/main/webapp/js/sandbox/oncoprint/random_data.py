#!/usr/bin/python

import math
import random
import json
import sys

if len(sys.argv) != 3:
    print "usage: " + sys.argv[0] + " <clinical | gene> <n> the number of random samples to generate, the type of data to generate"
    sys.exit(-1)

n_samples = int(sys.argv[2])
data_type = sys.argv[1]
n_unaltered = 20

'''
creates an unaltered sample for an index i and a gene g
'''
def unaltered_sample(i,g):
    sample = {}
    sample["sample"] = "sample_" + str(i)
    sample["gene"] = g

    return sample

'''
Creates a random sample for an index i and a gene g
'''
def gene_sample(i,g):
    sample = unaltered_sample(i,g)

    sample["cna"] = random.choice(cna)
    #sample["mrna"] = random.choice(mrna_rppa)
    sample["rppa"] = random.choice(mrna_rppa)
    sample["mutation"] = random.choice(mutation)

    if sample["cna"] == "NORMAL":
        del sample["cna"]
    if sample["mutation"] == "NORMAL":
        del sample["mutation"]
    if sample["rppa"] == "NOTSHOWN":
        del sample["rppa"]
    #if sample["mrna"] == "NOTSHOWN":
    #    del sample["mrna"]

    return sample

if data_type == "gene":
    mutation = ["FOO MUTATION", "NORMAL"]
    cna = ["AMPLIFIED", "AMPLIFIED", "GAINED", "DIPLOID", "HEMIZYGOUSLYDELETED", \
    "HOMODELETED", "NORMAL"]
    mrna_rppa = ["UPREGULATED", "DOWNREGULATED", "NOTSHOWN"]

    samples = []
    for i in xrange(n_samples):
        samples.append(gene_sample(i, 'GeneA'))
        samples.append(gene_sample(i, 'GeneB'))

    for i in xrange(n_unaltered):
        samples.append(unaltered_sample(n_samples + i, 'GeneA'))
        samples.append(unaltered_sample(n_samples + i, 'GeneB'))

    print json.dumps(samples, indent=1)

# clinical data
if data_type == "clinical":

    #continuous = "OVERALL_SURVIVAL_DAYS"
    continuous = "CONTINUOUS"
    continuous_range = 7000

    #discrete = "VITAL_STATUS"
    discrete = "DISCRETE"
    discrete_range = ["I", "II"]

    samples = []
    for i in xrange(n_samples + n_unaltered):
        sample1 = {}
        sample1["sample"] = "sample_" + str(i)
        sample1["attr_id"] = continuous
        sample1["attr_val"] = random.randint(0, continuous_range)
        samples.append(sample1)
        sample2 = {}
        sample2["sample"] = "sample_" + str(i)
        sample2["attr_id"] = discrete
        sample2["attr_val"] = random.choice(discrete_range)
        samples.append(sample2)

    print json.dumps(samples, indent=1)
